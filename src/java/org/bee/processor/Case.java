package org.bee.processor;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class Case extends Block {
	
	public Case(String xpath) {
		super(xpath);
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		blockType = BlockType.t_case;
	}

	public String[] getAllowedAttributeNames() {
		return new String[] {ATTR_VALUE};
	}

}
