// $Id: lt.java,v 1.5 2006/01/24 05:44:09 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 16, 2004
package org.bee.oper;

import org.bee.util.InfoHolder;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 * 
 * Does less comparision of first operand with second
 */
public class lt {

	public static InfoHolder<String, String, Boolean> doOperator(InfoHolder<String, String, Comparable> op1,
			InfoHolder<String, String, Comparable> op2) {
		Boolean result;
		if (op1.getValue() == null) {
			result = (op2 != null && op2.getValue() != null);
		} else if (op2.getValue() != null) {
			if (op1.getType() != null)
				result = op1.getType().compareTo(op2.getType()) < 0;
			else
				result = op1.getValue().compareTo(op2.getValue()) < 0;
		} else
			result = Boolean.FALSE;
		return new InfoHolder<String, String, Boolean>("lt", result.toString(), result);
	}

	public static InfoHolder<String, String, Boolean> proceed(InfoHolder<String, String, Comparable> op1,
			InfoHolder<String, String, Comparable> op2) {
		return doOperator(op1, op2);
	}
}
