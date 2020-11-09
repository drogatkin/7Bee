// $Id: AbstractValue.java,v 1.25 2009/04/07 05:23:55 dmitriy Exp $
// Bee Copyright (c) 2004-2007 Dmitriy Rogatkin
package org.bee.processor;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import java.util.logging.Level;
import org.bee.util.XmlPath;
import org.bee.util.InfoHolder;
import static org.bee.util.Logger.logger;
/** @author <a href="mailto:dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 */
public abstract class AbstractValue extends DefaultHandler implements Instruction {
	public enum Type {
		variable, file, directory, url, number, date, array, path, environment, property, eval, block, project, repo_artifact };
		
	protected String xpath;
	protected String value;
	protected String variable;
	protected String name;
	protected Instruction parent;
	protected StringBuffer valueBuffer;
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
	    return new String[] {ATTR_NAME, ATTR_VARIABLE, ATTR_TYPE, ATTR_PROPERTY, ATTR_VALUE, ATTR_SEPARATOR};
	}
	
	public InfoHolder < String, InfoHolder, Object > lookupInChain(String lookName) {
		InfoHolder < String, InfoHolder, Object > result = null;
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

	public InfoHolder < String, InfoHolder, Object > lookupOnTop(String lookName) {
		Instruction instruction = this;
		while (instruction.getParent() != null) {
			instruction = instruction.getParent();
		}
		return instruction.getNameSpace().lookup(lookName);
	}

	protected void updateInNameSpace(String lookName, InfoHolder<String, InfoHolder, Object> nv) {
		/*for (Instruction i=this;i!=null;i=i.getParent())  			
		    if (i.getNameSpace() != null && i.getNameSpace().lookup(name) != null)
		        i.getNameSpace().inScope(nv);*/
	    NameSpace topNS = null;
	    NameSpace nnullNS = null;
		for (Instruction i=getParent();i!=null;i=i.getParent()) {
		    if (i.getNameSpace() != null) {
		        if (nnullNS == null)
		            nnullNS = i.getNameSpace();
		        if(i.getNameSpace().lookup(lookName) != null)
		            topNS = i.getNameSpace();
		    }		        		    
		}
		if (topNS != null)
		    topNS.inScope(nv);
		else if (nnullNS != null)
		    nnullNS.inScope(nv);
	}
	
	public String lookupStringValue(String lookName) {
		InfoHolder < String, InfoHolder, Object > v = lookupInChain(lookName);
		if (v == null) {
			if (lookName.startsWith("~#") && lookName.endsWith("#~"))
				return null;
			if (System.getenv(lookName) != null)
				return System.getenv(lookName);
			return lookName;
		}
		InfoHolder < String, String, Object > v1 = v.getValue();
		return v1==null?null:v1.getValue();
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
				} catch(IllegalArgumentException iae) {
					logger.warning(String.format("Unrecognized type value of '%s' ignored for element %s (%s)", strType, name, iae ));
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
	    //if (logger.isLoggable(Level.WARNING))
	        this.locator  = locator;
	}

	protected void verifyAttributes(Attributes attrs) {
	    if (logger.isLoggable(Level.WARNING)) {
	        String[] aas = getAllowedAttributeNames();
	        if (aas != null && attrs.getLength() > 0) {
	            Map<String,String> aam = new HashMap<String,String>(aas.length);
	            for (String an:aas)
	                aam.put(an,an);
	            for (int i=0; i<attrs.getLength(); i++)
	                if (aam.get(attrs.getQName(i))==null)
	                    logger.warning("Not allowed attribute '"+attrs.getQName(i)+"' for "+getClass().getName()+" at "+locator);
	        }
	    }
	}
	
	protected File makeFile(String pathName) {
		char fc = pathName.charAt(0);
		if (fc == '/' || fc == '\\' || value.indexOf(':') > 0) // absolute path criteria
			return new File(pathName);
		else if (pathName.startsWith("./") || pathName.startsWith(".\\") 
				|| pathName.startsWith("../") || pathName.startsWith("..\\"))
			try {
				return new File(pathName).getCanonicalFile();
			} catch(IOException ioe) {
				logger.warning("Path '"+pathName+"' cannot be found, "+ioe);
				return new File(pathName).getAbsoluteFile();
			}
		else {
			String baseDir = lookupStringValue(RESERVE_NAME_DIR);
			if (baseDir == null)
				baseDir = System.getProperty("user.dir");//new File(".").getAbsolutePath();
			return new File(baseDir, pathName);  // TODO ?? .getAbsolutePath();
		}
	}

}