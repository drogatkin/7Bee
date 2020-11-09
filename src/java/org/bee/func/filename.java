// $Id: filename.java,v 1.5 2011/06/28 03:51:11 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 18, 2004
package org.bee.func;

import java.io.File;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public final class filename {

	/**
	 * 
	 */
	public filename() {
		super();
	}

	public static String eval(String path) {
		//System.out.printf("========>filename: for path %s\n", path);
		//new Exception("FIlename").printStackTrace(System.out);
		if (path == null)
			return null;
		File file = new File(path);
		if (file.exists()) {
			String result = file.getName();
			int di = result.lastIndexOf('.');
			if (di > 0)
				result = result.substring(0, di);
			return result;
		} else {
			int li = path.lastIndexOf('.');
			if (li > 0)
				path = path.substring(0, li);
			li = path.lastIndexOf('/');
			if (li >= 0)
				path = path.substring(li + 1);
			li = path.lastIndexOf('\\');
			if (li >= 0)
				path = path.substring(li + 1);
			return path;
		}
	}

}
