// $Id: timestamp.java,v 1.1 2004/03/19 06:43:45 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 18, 2004
package org.bee.func;
import java.util.Date;
import java.io.File;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public final class timestamp {

	/**
	 * 
	 */
	public timestamp() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static Date eval(String fileName) {
		File file = new File(fileName);
		if (file.exists())
			return new Date(file.lastModified());
		return null;
	}

}
