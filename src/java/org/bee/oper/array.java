// $Id: array.java,v 1.8 2006/06/25 09:02:48 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 25, 2004
package org.bee.oper;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import org.bee.util.InfoHolder;

/**
 * @author <a href="dmitriy@mochamail.com">dmitriy Rogatkin</a>
 * 
 * Provide class description here
 */
public class array {

	/**
	 * 
	 */
	public array() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static InfoHolder<String, String, Object> proceed(InfoHolder<String, String, Object> op1,
			InfoHolder<String, String, Object> op2) {
		return doOperator(op1, op2);
	}

	public static InfoHolder<String, String, Object> doOperator(InfoHolder<String, String, Object> op1,
			InfoHolder<String, String, Object> op2) {
		Object o1 = op1 == null ? null : op1.getType();
		Object o2 = op2 == null ? null : op2.getType();
		// System.out.printf("o1: %s o2 %s\n", op1, op2);
		List<String> result = null; // List<InfoHolder>  
		if (o1 == null) {
			result = new ArrayList<String>();
			if (op1 != null && op1.getValue() != null)
				result.add(op1.getValue());
		} else {
			if (o1 instanceof List) {
				result = (List<String>) o1;
			} else if (o1 instanceof String[])
				result = Arrays.asList((String[]) o1);
			else if (o1 instanceof Object[])
				throw new IllegalArgumentException("Unexpected type Object[] for "+op1);
			else {
				result = new ArrayList<String>();
				result.add(op1.getValue());
			}
		}
		if (op2 != null)
			fillListFromObject(result, op2.getValue(), o2);
		// System.out.printf("o1: %s o2 %s r %s\n", op1, op2, result);
		return new InfoHolder<String, String, Object>(op1 == null ? null : op1.getKey(), result.toString(), result);
	}

	protected static void fillListFromObject(List<String> l1, String v2, Object o2) {
		if (o2 != null) {
			if (o2 instanceof List)
				l1.addAll((List<String>) o2);
			else if (o2 instanceof Object[])
				for (Object oe : (Object[]) o2)
					if (oe != null)
						l1.add(oe.toString());
					else
						l1.add(v2); // can be null, but weird
			else {
				// System.err.printf("bee:warning::oper:array fillListFromObject() Unexpected type for argument '%s' class '%s'\n", o2,
				// o2.getClass().getName());
				l1.add(v2);
			}
		} else
			l1.add(v2);
	}
}