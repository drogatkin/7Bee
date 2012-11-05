// $Id: ask.java,v 1.5 2004/12/23 10:30:33 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 23, 2004
package org.bee.func;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bee.util.NullPrintStream;

/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin </a>
 * 
 * Provide class description here
 */
public class ask {

	public static String eval(String prompt, String defVal/* , String encoding */) {
		PrintStream printStream = System.out;
		if (printStream instanceof NullPrintStream)
			printStream = ((NullPrintStream) printStream).getOriginal();
		if (prompt != null)
			printStream.print(prompt);
		else
			printStream.print("?");
		String result = readLine(System.in);
		return result.length() == 0 ? defVal : result;
	}

	static protected String readLine(InputStream is/* , String encoding */) {
		if (is == null)
			return "";
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int c;
		try {
			do {
				c = is.read();
				//System.out.println("Char:" + (int) c);
				if (c == 0xA)  // to work on Unix/Windows
					break;
				buffer.write(c);
			} while (c > 0);
			while (is.available() > 0)
				is.read();
		} catch (IOException ioe) {
			System.err.println("func:ask:eval: read exception: " + ioe);
		}
		return new String(buffer.toByteArray()/* , encoding */).trim();
	}
}