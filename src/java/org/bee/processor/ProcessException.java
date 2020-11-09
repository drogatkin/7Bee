// $Id: ProcessException.java,v 1.2 2005/06/22 05:00:19 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 23, 2004
package org.bee.processor;

/**
 * @author <a href="dmitriy@mochamail.com">dmitriy Rogatkin</a>
 *
 * This exception is mostly used for controlling workflow
 * like interrupting certain processes. 
 */
public class ProcessException extends Error {

protected int exitCode;
	/**
	 * 
	 */
	public ProcessException() {
		super();
	}

	/**
	 * @param arg0
	 */
	public ProcessException(String arg0) {
		super(arg0);
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ProcessException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	/**
	 * @param arg0
	 */
	public ProcessException(Throwable arg0) {
		super(arg0);
	}

	public ProcessException(String arg0, int ec) {
		super(arg0);
		exitCode = ec;
	}
	
	public int getExitCode() {
		return exitCode;
	}
}
