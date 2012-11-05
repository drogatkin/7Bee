// $Id: scalar.java,v 1.2 2008/03/11 03:46:53 dmitriy Exp $
//Bee Copyright (c) 2008 Dmitriy Rogatkin
// Created on Mar 3, 2008
package org.bee.func;

import java.util.List;

public class scalar {
	public static String eval(Object vector) {
		return eval(vector, null);
	}

	public static String eval(Object vector, Object sep) {
		StringBuffer result = new StringBuffer(512);
		if (vector instanceof List)
			for (Object o : (List) vector) {
				if (o != null)
					result.append(o.toString());
				if (sep != null)
					result.append(sep);
			}
		else if (vector != null && vector.getClass().isArray())
			for (Object o : (Object[]) vector) {
				if (o != null)
					result.append(o.toString());
				if (sep != null)
					result.append(sep);
			}

		if (sep == null)
			return result.toString();
		return result.substring(0, result.length() - sep.toString().length());
	}
}
