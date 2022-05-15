// $Id: AbstractValue.java,v 1.25 2009/04/07 05:23:55 dmitriy Exp $
// Bee Copyright (c) 2004-2007 Dmitriy Rogatkin
package org.bee.processor;

import static org.bee.util.Logger.logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;

import org.bee.util.InfoHolder;
import org.bee.util.XmlPath;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author <a href="mailto:dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 */
public abstract class AbstractValue extends DefaultHandler implements Instruction {
	public enum Type {
		variable, file, directory, url, number, date, array, path, environment, property, eval, block, project,
		repo_artifact
	};

	public static final int MAX_TEMPL_VAR_LEN = 128;

	protected String xpath;
	protected String value;
	protected String variable;
	protected String name;
	protected Instruction parent;
	protected StringBuffer valueBuffer; // ? Builder
	protected Type type;
	protected Locator locator;

	protected final static String TYPE_RESERVED = "then|else|project";
	protected final static String TYPE_SHORTCUTS = "variable|directory|number|environment|property";

	public AbstractValue(String xpath) {
		this.xpath = xpath;
	}

	public ContentHandler getHandler() {
		return this;
	}

	public XmlPath getPath() {
		return XmlPath.fromString(xpath);
	}

	public void childDone(Instruction child) {
		// no children we have
	}

	public void setParent(Instruction parent) {
		this.parent = parent;
	}

	public Instruction getParent() {
		return parent;
	}

	public Instruction.NameSpace getNameSpace() {
		return null;
	}

	public String[] getAllowedAttributeNames() {
		return new String[] { ATTR_NAME, ATTR_VARIABLE, ATTR_TYPE, ATTR_PROPERTY, ATTR_VALUE, ATTR_SEPARATOR };
	}

	public InfoHolder<String, InfoHolder, Object> lookupInChain(String lookName) {
		InfoHolder<String, InfoHolder, Object> result = null;
		Instruction instruction = this;
		while (result == null && instruction != null) {
			NameSpace ns = instruction.getNameSpace();
			if (ns != null)
				result = ns.lookup(lookName);
			instruction = instruction.getParent();
		}
		return result;
	}

	public NameSpace lookupNameSpace(String lookName) {
		for (Instruction instruction = this; instruction != null; instruction = instruction.getParent()) {
			NameSpace result = instruction.getNameSpace();
			if (result != null && result.lookup(lookName) != null)
				return result;
		}
		return null;
	}

	protected void traceInChain(String lookName) {
		Instruction instruction = this;
		while (instruction != null) {
			NameSpace ns = instruction.getNameSpace();
			if (ns != null)
				logger.finest("Trace name '" + lookName + "' in ns of " + instruction + " is " + ns.lookup(lookName));
			instruction = instruction.getParent();
		}
	}

	public InfoHolder<String, InfoHolder, Object> lookupOnTop(String lookName) {
		Instruction instruction = this;
		while (instruction.getParent() != null) {
			instruction = instruction.getParent();
		}
		return instruction.getNameSpace().lookup(lookName);
	}

	protected void updateInNameSpace(String lookName, InfoHolder<String, InfoHolder, Object> nv) {
		/*
		 * for (Instruction i=this;i!=null;i=i.getParent()) if (i.getNameSpace() != null
		 * && i.getNameSpace().lookup(name) != null) i.getNameSpace().inScope(nv);
		 */
		NameSpace topNS = null;
		NameSpace nnullNS = null;
		for (Instruction i = getParent(); i != null; i = i.getParent()) {
			if (i.getNameSpace() != null) {
				if (nnullNS == null)
					nnullNS = i.getNameSpace();
				if (i.getNameSpace().lookup(lookName) != null)
					topNS = i.getNameSpace();
			}
		}
		if (topNS != null)
			topNS.inScope(nv);
		else if (nnullNS != null)
			nnullNS.inScope(nv);
	}

	public String lookupStringValue(String lookName) {
		return lookupStringValue(lookName, null);
	}

	public String lookupStringValue(String lookName, String sep) {
		InfoHolder<String, InfoHolder, Object> v = lookupInChain(lookName);
		if (v == null) {
			if (lookName.startsWith("~#") && lookName.endsWith("#~"))
				return null;
			if (System.getenv(lookName) != null)
				return System.getenv(lookName);
			return sep!=null?null:lookName;
		}
		InfoHolder<String, String, Object> v1 = v.getValue();
		Object type = v1==null?null:v1.getType();
		//System.err.printf("type %s of %s%n", type==null?null:type.getClass(), lookName);
		if (type != null) {
			Object[] elements = null;
			if (type instanceof Collection) {
				elements = ((Collection) type).toArray();
			} else if (type.getClass().isArray()) {
				elements = (Object[]) type;
			}
			if (elements != null) {
				if (elements.length == 0)
					return "";
				StringBuilder strArr = new StringBuilder();
				strArr.append(elements[0]);
				for (int ei = 1; ei < elements.length; ++ei) {
					if (sep != null && !sep.isEmpty())
						strArr.append(sep);
					strArr.append(elements[ei]);
				}
				return strArr.toString();
			}
		}
		return v1 == null ? null : v1.getValue();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		verifyAttributes(attributes);
		valueBuffer = new StringBuffer(20);
		variable = attributes.getValue("", ATTR_VARIABLE);
		value = attributes.getValue("", ATTR_VALUE);
		if (value == null && attributes.getValue("", ATTR_PROPERTY) != null)
			value = System.getProperty(attributes.getValue("", ATTR_PROPERTY));
		name = attributes.getValue("", ATTR_NAME);
		if (type != null)
			return;
		String strType = attributes.getValue("", ATTR_TYPE);
		if (strType == null)
			type = Type.variable;
		else {
			strType = extendShortcut(strType);
			if (TYPE_RESERVED.indexOf(strType) < 0) {
				try {
					type = Type.valueOf(strType);
				} catch (IllegalArgumentException iae) {
					logger.warning(String.format("Unrecognized type value of '%s' ignored for element %s (%s)", strType,
							name, iae));
					type = Type.variable;
				}
			} else
				type = Type.variable;
		}
	}

	private static String extendShortcut(String strType) {
		int vi = TYPE_SHORTCUTS.indexOf(strType);
		if (vi < 0)
			return strType;
		int ei = TYPE_SHORTCUTS.indexOf('|', vi);
		if (ei > 0)
			return TYPE_SHORTCUTS.substring(vi, ei);
		return TYPE_SHORTCUTS.substring(vi);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (valueBuffer.length() > 0 && value == null)
			value = valueBuffer.toString();
		valueBuffer = null;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		valueBuffer.append(ch, start, length);
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		characters(ch, start, length);
	}

	public void setDocumentLocator(Locator locator) {
		// if (logger.isLoggable(Level.WARNING))
		this.locator = locator;
	}

	enum templStat {
		normal, dol, cbo, var, cbe
	}

	public String processTemplate(String templateValue, Stack<Set<String>> nestedVariables) {
		if (templateValue == null)
			return null;
		if (nestedVariables != null)
			nestedVariables.push(new HashSet<String>());
		char[] templateArr = templateValue.toCharArray();
		templStat st = templStat.normal;
		int staVar = -1; // position of beginning a var name (MAX_TEMPL_VAR_LEN)
		int staData = 0; // a position to start no template not committed data
		String varName;
		StringBuilder accum = null;
		for (int ci = 0; ci < templateArr.length; ++ci) {
			char c = templateArr[ci];
			switch (c) {
			case '$':
				switch (st) {
				case normal:
				case dol:
					st = templStat.dol;
					break;
				case cbo:
					st = templStat.var;
					break;
				case cbe:
					st = templStat.dol;
					staData = ci;
					break;
				}
				break;
			case '{':
				switch (st) {
				case dol:
					st = templStat.cbo;
					break;
				}
				break;
			case '}':
				switch (st) {
				case var:
					st = templStat.cbe;
					varName = new String(templateArr, staVar, ci - staVar);
					// check if array
					String val;
					int cp = varName.indexOf(',');
					if (cp > 0) {
						val = lookupStringValue(varName.substring(0, cp), varName.substring(cp + 1));
					} else
						val = lookupStringValue(varName, "");
					if (val == null) { // no such var
						st = templStat.normal;
					} else {
						if (accum == null)
							accum = new StringBuilder();
						accum.append(templateArr, staData, staVar - staData - 2);
						if (!val.isEmpty()) {
							if (nestedVariables == null) {
								nestedVariables = new Stack<>();
								nestedVariables.push(new HashSet<String>());
							}
							if (!contains(nestedVariables, val)) {
								accum.append(processTemplate(val, nestedVariables));
								nestedVariables.peek().add(val);
								staVar = -1;
							} else
								st = templStat.normal;
						}
					}
					break;
				case dol:
					st = templStat.normal;
					break;
				}
				break;
			//case ',':
			default:
				switch (st) {
				case cbo:
					staVar = ci;
					st = templStat.var;
					break;
				case cbe:
					staData = ci;
					st = templStat.normal;
					break;
				case dol:
					st = templStat.var;
					staVar = ci;
					break;
				case normal:

					break;
				case var:
					if (ci - staVar > MAX_TEMPL_VAR_LEN) {
						staVar = -1;
						st = templStat.normal;
					}
					break;
				}
			}
		}

		switch (st) {
		case cbe:
			return accum.toString();
		case cbo:
		case var:
		case dol:
		case normal:
			if (staData > 0 && accum != null) {
				accum.append(templateArr, staData, templateArr.length - staData);
				return accum.toString();
			}
			break;
		}

		return templateValue;
	}

	static boolean contains(Stack<Set<String>> stk, String var) {
		for (int sl=0, ss=stk.size(); sl < ss-1; ++sl) {
			if (stk.get(sl).contains(var))
				return true;
		}
		return false;
	}

	protected void verifyAttributes(Attributes attrs) {
		if (logger.isLoggable(Level.WARNING)) {
			String[] aas = getAllowedAttributeNames();
			if (aas != null && attrs.getLength() > 0) {
				Map<String, String> aam = new HashMap<String, String>(aas.length);
				for (String an : aas)
					aam.put(an, an);
				for (int i = 0; i < attrs.getLength(); i++)
					if (aam.get(attrs.getQName(i)) == null)
						logger.warning("Not allowed attribute '" + attrs.getQName(i) + "' for " + getClass().getName()
								+ " at " + locator);
			}
		}
	}

	protected File makeFile(String pathName) {
		char fc = pathName.charAt(0);
		if (fc == '/' || fc == '\\' || value.indexOf(':') > 0) // absolute path criteria
			return new File(pathName);
		else if (pathName.startsWith("./") || pathName.startsWith(".\\") || pathName.startsWith("../")
				|| pathName.startsWith("..\\")) {
			//System.err.printf("file: %s for %s%n", pathName, new File(pathName).getAbsoluteFile());
				return new File(pathName).getAbsoluteFile();//.getCanonicalFile();
		} else {
			String baseDir = lookupStringValue(RESERVE_NAME_DIR);
			if (baseDir == null)
				baseDir = System.getProperty("user.dir");// new File(".").getAbsolutePath();
			return new File(baseDir, pathName); // TODO ?? .getAbsolutePath();
		}
	}

}