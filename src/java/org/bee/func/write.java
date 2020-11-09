// $Id: write.java,v 1.9 2008/03/04 04:05:27 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 26, 2004
package org.bee.func;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class write {

	/**
	 * 
	 */
	public write() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static boolean eval(String...params) {
		return write(false, params);
	}
	
	protected static boolean write(boolean append, String...params) {
		if (params.length < 2)
			return false;
		FileWriter fw = null;
		File file = new File(params[0]);
		if (file.getParentFile() != null && file.getParentFile().exists() == false)
			if (file.getParentFile().mkdirs() == false)
				return false;
		try {
			//System.out.printf("write to %s %d\n", params[0],params.length );			
			fw = new FileWriter(file, append);
			for (int i = 1; i < params.length; i++) {
				if (params[i] != null)
					fw.write(params[i]);
				//fw.write("\r\n");
				//System.out.print(params[i]);
			}
			//System.out.println();
		} catch (IOException io) {
			System.err.printf("bee:func:write an exception %s in writing of %s.\n", io, params[0]);
			return false;
		} finally {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException io) {
				//
				}
		}
		return true;
	}
}
