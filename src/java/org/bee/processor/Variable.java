// $Id: Variable.java,v 1.10 2005/06/15 08:02:23 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 11, 2004
package org.bee.processor;
import org.bee.util.InfoHolder;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class Variable extends Value {
	
	public Variable(String xpath) {
		super(xpath);
	}
	
	public InfoHolder eval() {
		InfoHolder<String, String, Object> result = super.eval();
		InfoHolder<String, InfoHolder, Object> varResult = new InfoHolder<String, InfoHolder, Object>(name,
                         result);
		updateInNameSpace(name, varResult);
		return result;
	}
}
