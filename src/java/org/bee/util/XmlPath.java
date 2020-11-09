// $Id: XmlPath.java,v 1.2 2005/06/15 08:02:23 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 10, 2004
package org.bee.util;
import java.util.Stack;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class XmlPath extends Stack < String > {

	public String toString() {
		String result = "/";
		for (String element : this)
			result += element + '/';
		return result;
	}

	public static XmlPath wildCard(XmlPath xpath) {
		XmlPath result = new XmlPath();
		boolean startAdd = false;
		for (String element : xpath) {
			if ("*".equals(element) == false)
				if (startAdd == false) {
					result.push("*");
					startAdd = true;
				} else
					result.push(element);
			//else Exception  
		}
		return result;
	}
	
	public static XmlPath fromString(String xpath) {
		XmlPath result = new XmlPath();
		for (String element : xpath.split("/"))
			result.push(element);
		return result;

	}
}
