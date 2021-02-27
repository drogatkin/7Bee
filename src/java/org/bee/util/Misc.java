// $Id: Misc.java,v 1.7 2005/12/20 06:43:17 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 23, 2004
package org.bee.util;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="dmitriy@mochamail.com">dmitriy Rogatkin</a>
 *
 * Miscellenious utility methods
 */
public class Misc {

	protected static final int BUF_SIZE = 1024 * 32;
	/** formats time in ms in the string (dd)d:hh:mm:ss.mss, <br>
	 * for example 10d:12:34:56.103
	 */
	public static String formatTime(long time) {
		if (time <= 0)
			return "0:00";
		StringBuffer result = new StringBuffer();
		int val = 0;
		if (time % 1000 > 0)
			result.append('.').append(String.valueOf(time % 1000));
		time /= 1000;

		if (time > 0) {
			val = (int) time % 60;
			result.insert(0, val);
			if (val < 10)
				result.insert(0, '0');
			time /= 60;
			if (time > 0) {
				result.insert(0, ':');
				val = (int) time % 60;
				result.insert(0, val);
				if (val < 10)
					result.insert(0, '0');
				time /= 60;
				if (time > 0) { // hours
					result.insert(0, ':');
					val = (int) time % 24;
					result.insert(0, val);
					if (val < 10)
						result.insert(0, '0');
					time /= 24;
					if (time > 0) {
						result.insert(0, "d:");
						result.insert(0, val);
					}
				}
			} else
				result.insert(0, "0:");
		}

		return result.toString();
	}

	public static long copyStream(InputStream is, OutputStream os, long maxLen) throws IOException {
		byte[] buffer = new byte[BUF_SIZE];
		int len;
		long result = 0;
		while ((len = is.read(buffer)) > 0) {
			os.write(buffer, 0, len);
			result += len;
			if (maxLen > 0 && result > maxLen)
				break;
		}
		return result;
	}
	
	private static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+^$\\\\|]");
	public static String wildCardToRegExpr(String wildCard) {
		// Pattern.quote(s) may work better, or https://stackoverflow.com/questions/10664434/escaping-special-characters-in-java-regular-expressions
		wildCard = SPECIAL_REGEX_CHARS.matcher(wildCard).replaceAll("\\\\$0");
		return wildCard.replace("?", "[^/\\\\:]").replace("*", "[^/\\\\?:*]*");
	}
	
	public static String[] splitBy(String pat) {
		String[] components = pat.split("[/\\\\]");
		if (DEBUG_)
			System.out.printf("pp: %s to %s \n", pat, java.util.Arrays.asList(components));
		String path = "";
		List < String > result = new ArrayList < String > ();
		for (String pathComp : components) {
			if (result.size() > 0)
				result.add(pathComp);
			else if (pathComp.indexOf('?') >= 0 || pathComp.indexOf('*') >= 0) {
				result.add(path);
				result.add(pathComp);
			} else
				path += pathComp + File.separator;
		}
		if (result.size() == 0)
			result.add(path);
		if (DEBUG_)
			System.out.println("rm:" + pat + result);
		return result.toArray(new String[result.size()]);
	}
	
	public static String join(String delim, String[] elements, int numElems) {
		// similar to Java 8+ String join
		StringBuilder sbStr = new StringBuilder();
	    for (int i = 0, il = numElems <= 0 || numElems >= elements.length?elements.length:numElems; i < il; i++) {
	        if (i > 0)
	            sbStr.append(delim);
	        sbStr.append(elements[i]);
	    }
	    return sbStr.toString();
	}
	
	public static String streamToString(InputStream is, String encoding,
			int bufSize) throws IOException {
		StringBuffer result = new StringBuffer(100);
		byte[] buffer = new byte[bufSize];
		int len;
		if (encoding == null)
			while ((len = is.read(buffer)) > 0)
				result.append(new String(buffer, 0, len));
		else {
			byte[] bres = null;
			while ((len = is.read(buffer)) > 0) {
				if (bres == null)
					bres = new byte[len];
				else
					bres = Arrays.copyOf(bres, bres.length + len);
				System.arraycopy(buffer, 0, bres, bres.length - len, len);
			}
			try {
				return new String(bres, encoding);
			} catch (UnsupportedEncodingException uee) {
				throw new IOException("Can't apply encoding:" + encoding, uee);
			}
		}
		return result.toString();
	}

	public static String[] merge(String[] ar1, String[] ar2) {
	    if (ar1 == null) {
	        ar1 = ar2;
	        ar2 = null;
	    }
	    if (ar1 != null) {
	        if (ar2 == null || ar2.length == 0)
	            return ar1;
	        String [] res = new String[ar1.length+ar2.length];
	        System.arraycopy(ar1, 0, res, 0, ar1.length);
	        System.arraycopy(ar2, 0, res, ar1.length, ar2.length);
	        return res;
	    }
	    return null;
	}
	
	public static boolean hasValidClassLibExtension(String name) {
		name = name.toLowerCase();
		return name.endsWith(".jar") || name.endsWith(".zip");
	}
	
	static final boolean DEBUG_ = false;
}
