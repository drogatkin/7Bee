// $Id: Echo.java,v 1.8 2005/06/15 08:02:22 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 10, 2004
package org.bee.processor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.bee.util.Misc;
import org.bee.util.InfoHolder;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class Echo extends Value {
	protected String options;
	private static final String NAME = "echo";
	public Echo(String xpath) {
		super(xpath);
	}

	public InfoHolder eval() {
		//if (variable != null)
			//value = lookupStringValue(variable);
		InfoHolder<String, String, Object> result = super.eval();
		value = result!=null?result.getValue():null;
		boolean noNewLine = false;
		boolean allowInterp = true;
		if (options != null) {
			noNewLine = options.indexOf("-n") >= 0;
			allowInterp = options.indexOf("-E") < 0;
		}
		if (allowInterp) {
			if (noNewLine)
				System.out.printf(value);
			else
				System.out.printf(value+"\n");
		} else {
			System.out.printf("Echo: %s"+(noNewLine?"":"\n"), value);
		}		
		return new InfoHolder(NAME, value);
	}

	public String getName() {
		return NAME;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		options = attributes.getValue("", ATTR_OPTIONS);
	}

	public String[] getAllowedAttributeNames() {
	    return Misc.merge(new String[] {ATTR_OPTIONS}, super.getAllowedAttributeNames());
	}
}
