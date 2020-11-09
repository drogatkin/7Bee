// $Id: touch.java,v 1.3 2004/07/24 07:57:10 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 18, 2004
package org.bee.func;
import java.io.File;
import java.io.IOException;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public final class touch {

	/**
	 * 
	 */
	public touch() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static boolean eval(String fileName) {
		File file = new File(fileName);

		if (file.exists() == false)
			try {
				return file.createNewFile();
			} catch (IOException ioe) {
			} 
		else
			return file.setLastModified(System.currentTimeMillis());
		return false;
	}
}
