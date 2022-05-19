package org.bee.processor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Default extends Block {
	
	public Default(String xpath) {
		super(xpath);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		blockType = BlockType.t_default;
	}

	public String[] getAllowedAttributeNames() {
		return new String[] {};
	}
}
