// $Id: anynewer.java,v 1.3 2005/10/07 05:15:58 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 25, 2004
package org.bee.func;
/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * scans for last modification time of files defined by first parameter
 * and last modification time specified by second and returns true if first time is later
 */
public class anynewer extends allnewer {
	public static boolean eval(String srcPath, String dstPath) {
		return allnewer.eval(srcPath, dstPath);
	}
}
