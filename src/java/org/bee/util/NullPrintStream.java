// $Id: NullPrintStream.java,v 1.1 2004/04/24 07:42:45 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 23, 2004
package org.bee.util;

import java.io.PrintStream;
import java.io.OutputStream;
import java.io.IOException;

/**
 * @author <a href="dmitriy@mochamail.com">dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class NullPrintStream extends PrintStream {
	protected PrintStream originalPrintStream;
	public NullPrintStream(PrintStream original) {
		super(new OutputStream() {
			/** Overrides the <code>OutputStream.write</code> function to do nothing.
			 */
			public void write(int b) throws IOException {
			}
		});
		originalPrintStream = original;
	}
	
	public PrintStream getOriginal() {
		return originalPrintStream;
	}
}
