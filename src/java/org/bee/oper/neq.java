// $Id: neq.java,v 1.3 2004/03/23 09:47:04 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 16, 2004
package org.bee.oper;
import org.bee.util.InfoHolder;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class neq {
	
	public static InfoHolder<String, String, Boolean> doOperator(InfoHolder<String, String, Double> op1, InfoHolder<String, String, Double> op2) {
		InfoHolder<String, String, Boolean> result = eq.doOperator(op1, op2);
		return new InfoHolder<String, String, Boolean>("neq", result.getType()?Boolean.FALSE.toString():Boolean.TRUE.toString(), result.getType() == false);		
	}
	
	public static InfoHolder<String, String, Boolean> proceed(InfoHolder<String, String, Double> op1, InfoHolder<String, String, Double> op2) {
		return doOperator(op1, op2);
	}
}
