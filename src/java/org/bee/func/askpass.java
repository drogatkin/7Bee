// $Id: askpass.java,v 1.1 2006/06/07 23:17:21 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on June 7, 2006
package org.bee.func;

import java.io.Console;

/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 * 
 * Used for reading sensitive information from console without echo
 */
public class askpass {

	public static String eval(String prompt, String defVal/* , String encoding */) {
		Console cons;
		char[] passwd;
		if ((cons = System.console()) != null
				&& (passwd = cons.readPassword("%s", prompt == null ? "?" : prompt)) != null) {
			return passwd.length > 0 ? new String(passwd) : defVal;
		}
		return defVal;
	}
}