// $Id: stop.java,v 1.3 2012/03/02 04:07:28 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 18, 2004
package org.bee.func;
import org.bee.processor.ProcessException;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public final class stop {

	/**
	 * 
	 */
	public stop() {
		super();
	}

	public static Object eval(String code) {
		int result = 0;
		try {
			result = Integer.parseInt(code);
			
		}catch(NumberFormatException nfe) {
		}
		
		return new ProcessException("stop", result);
	}

}
