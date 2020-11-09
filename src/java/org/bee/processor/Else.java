// $Id: Else.java,v 1.2 2007/06/28 22:32:35 rogatkin Exp $
//Bee Copyright (c) 2004-2007 Dmitriy Rogatkin
// Created on Jun 28, 2007
package org.bee.processor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public final class Else extends Block {

	public Else(String xpath) {
		super(xpath);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		blockType = BlockType.t_else;
	}

	public String[] getAllowedAttributeNames() {
		return null;
	}

}
