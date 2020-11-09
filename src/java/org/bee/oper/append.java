// $Id: append.java,v 1.8 2005/12/20 08:50:06 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 16, 2004
package org.bee.oper;
import org.bee.util.InfoHolder;
import java.util.List;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class append {
	
	/** provides appending string values
	 * 
	 * @param op1 first operand = result
	 * @param op2 second operand
	 * 
	 * @return concatenation first to second operand
	 * 
	 * TODO currently if operand is array and have size/length==1 then 1st
	 * element is used, otherwise string value of it. A good solution can
	 * be concatenation elements of array, but what should be a separator?
	 * Maybe next argument?
	 */
	public static InfoHolder<String, String, Object> doOperator(InfoHolder<String, String, Object> op1, InfoHolder<String, String, Object> op2) {
		if (op1 == null || op1.getValue() == null || op1.getValue().length() == 0)
			return op2;
		Object type = op1.getType();
		Object valOp1 = null;
        if (type instanceof Object[]) {
		    valOp1 = ((Object[])type).length == 1?((Object[])type)[0]:op1.getValue();
		} else if (type instanceof List) {
		    valOp1 = ((List)type).size() == 1?((List)type).get(0):op1.getValue();
		} else
		    valOp1 = op1.getValue();
		if (valOp1 == null)
		    valOp1 = "";
        if (op2 == null || op2.getValue() == null) 
             return new InfoHolder<String, String, Object>(op1.getKey(), valOp1.toString() , valOp1); 
        Object valOp2 = null;
        Object op2Type = op2.getType();
		if (op2Type instanceof Object[]) {
		     valOp2 = ((Object[])op2Type).length == 1?((Object[])op2Type)[0]:op2.getValue();
		} else if (op2Type instanceof List) {
		    valOp2 = ((List)op2Type).size() == 1?((List)op2Type).get(0):op2.getValue();        
		} else
		    valOp2 = op2.getValue();
		if (valOp2 == null)
		    valOp2 = "";
		return new InfoHolder<String, String, Object>(op1.getKey(), valOp1.toString()+valOp2.toString());
	}
	
	public static InfoHolder<String, String, Object> proceed(InfoHolder<String, String, Object> op1, InfoHolder<String, String, Object> op2) {
		return doOperator(op1, op2);
	}
}
