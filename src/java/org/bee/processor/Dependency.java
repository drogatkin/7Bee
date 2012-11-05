// $Id: Dependency.java,v 1.11 2011/08/28 05:54:13 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 17, 2004
package org.bee.processor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.bee.util.InfoHolder;
import org.bee.util.Misc;
import static org.bee.util.Logger.logger;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class Dependency extends Parameter {
	protected String target;
	protected boolean processOnly;
	public Dependency(String xpath) {
		super(xpath);
	}

	public InfoHolder eval() {
		InfoHolder result = null;
		if (target != null) { // find and eval target and if not fail
			InfoHolder < String, InfoHolder, Object > th = lookupOnTop(target);
			if (th == null) {
				logger.severe("Target '" + target + "' to depend on was not found.");
				return null;
			}
			InfoHolder < String, Target, Object > tv = th.getValue();
			if (tv.getValue() instanceof Target == false 
					/* && tv.getType() instanceof Target == false*/) { 
				logger.finer("Target: "+target+" found as: "+tv+" and considered as already processed, skipped.");
				// TODO: can be reconsidered in case of cyclic dependencies
				result = tv;
			} else
				result = tv.getValue().eval();
			if (processOnly)
				return new InfoHolder < String, String, Boolean > ((String) result.getKey(), Boolean.FALSE.toString(), Boolean.FALSE);
			else
				return result;
		}
		result = super.eval();
		if (result == null) {
			logger.finest("Can't evaluate, result: null");
			return null;
		}
		if (name != null)
			getParent().getNameSpace().inScope(new InfoHolder < String, InfoHolder, Object > (name, result));
		Object o = result.getValue();
		// TODO: replace [] by more robust instanceof List or Object[]
		if (o == null || Boolean.FALSE.toString().equals(o) || "[]".equals(o))
			o = Boolean.FALSE;
		else
			o = Boolean.TRUE;
		logger.finest("Dependency result: "+o);
		return new InfoHolder < String, String, Object > ((String) result.getKey(), o.toString(), o);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		target = attributes.getValue("", ATTR_TARGET);
		processOnly = "yes".equals(attributes.getValue("", ATTR_PROCESSONLY));
	}

	public String[] getAllowedAttributeNames() {
	    return Misc.merge(new String[] {ATTR_PROCESSONLY, ATTR_TARGET}, super.getAllowedAttributeNames());
	}
}
