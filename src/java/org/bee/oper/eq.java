// $Id: eq.java,v 1.3 2004/07/10 05:19:28 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 16, 2004
package org.bee.oper;
import org.bee.util.InfoHolder;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class eq {
	
	public static InfoHolder<String, String, Boolean> doOperator(InfoHolder<String, String, Double> op1, InfoHolder<String, String, Double> op2) {
		Boolean result;
		if (op2 == null) {
			System.err.printf("eq:error: 'eq' is not unary operation.\n");
			result = Boolean.FALSE;
		} else
			result = op1.getValue() == op2.getValue() || (op1.getValue() != null && op1.getValue().equals(op2.getValue()));
		return new InfoHolder<String, String, Boolean>("eq", result.toString(), result);
	}
	
	public static InfoHolder<String, String, Boolean> proceed(InfoHolder<String, String, Double> op1, InfoHolder<String, String, Double> op2) {
		return doOperator(op1, op2);
	}
}
