// $Id: ask.java,v 1.5 2004/12/23 10:30:33 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 23, 2004
package org.bee.func;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Scanner;

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
		// potentially https://github.com/jline/jline3
		if (is == null)
			return "";
		try (Scanner s = new Scanner(is);){	
			return s.nextLine().trim(); 
		}
	}
}