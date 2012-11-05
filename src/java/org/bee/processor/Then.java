// $Id: Then.java,v 1.1 2007/06/28 19:49:48 rogatkin Exp $
//7Bee Copyright (c) 2004-2007 Dmitriy Rogatkin
// Created on Jun 28, 2007
package org.bee.processor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class Then extends Block {

	public Then(String xpath) {
		super(xpath);		
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		blockType = BlockType.t_then;
	}
	
	public String[] getAllowedAttributeNames() {
		return null;
	}
}
