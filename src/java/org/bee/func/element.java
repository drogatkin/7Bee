// $Id$
//Bee Copyright (c) 2013 Dmitriy Rogatkin
// Created on Mar 21, 2013
package org.bee.func;

import java.util.List;

public class element {
	public static Object eval(Object vector) {
		return eval(vector, null);
	}

	public static Object eval(Object vector, Object index) {
		if (index == null)
			return null;
		int i = 0;
		if (index instanceof String)
			i = Integer.valueOf((String) index);
		else if (index instanceof Number) 
			i = ((Number)index).intValue();
		if (vector instanceof List)
			return ((List) vector).get(i);
		else if (vector != null && vector.getClass().isArray())
			return ( (Object[]) vector)[i];
		return vector;
	}
}
