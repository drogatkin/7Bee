/* 7Bee - string.java
 * Copyright (C) 1999-2015 Dmitriy Rogatkin.  All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 *  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 *  ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Created on Sep 15, 2015
 */
package org.bee.func;

/** This function provides basic string operations as
 * converting to upper, lower or capital case, it can be also used for trimming string,
 * reversing, truncating and substringing. 
 * 
 * @author Dmitriy
 *
 */
public final class string {
	enum Oper {
		upper, lower, capital, trim, sub, trunc, reverse
	};

	public static String eval(String... args) {
		switch (args.length) {
		case 0:
			return "";
		case 1:
			return args[0];
		case 2:
			switch (Oper.valueOf(Oper.class, args[1])) {
			case upper:
				return args[0].toUpperCase();
			case lower:
				return args[0].toLowerCase();
			case capital:
				if (args[0].isEmpty())
					return args[0];
				else
					return Character.toUpperCase(args[0].charAt(0)) + args[0].substring(1).toLowerCase();
			case trim:
				return args[0].trim();
			case reverse:
				if (args[0].isEmpty())
					return args[0];
				char[] ca = args[0].toCharArray();
				StringBuffer sb = new StringBuffer();
				for (char c : ca)
					sb.append(c);
				return sb.toString();
			default:
				break;
			}
			break;
		case 3:
			int n = -1;
			try {
				n = Integer.parseInt(args[2]);
			} catch (Exception e) {
				throw new IllegalArgumentException("Invalid number " + args[2]);
			}

			switch (Oper.valueOf(Oper.class, args[1])) {
			case trunc:
				if (args[0].length() <= n)
					return args[0];
				else
					return args[0].substring(0, n);
			case sub:
				if (args[0].length() >= n)
					return args[0].substring(n);
				return "";
			default:
				break;
			}
		case 4:
			int e = -1;
			try {
				n = Integer.parseInt(args[2]);
				e = Integer.parseInt(args[3]);
			} catch (Exception ex) {
				throw new IllegalArgumentException("Invalid number " + args[2] + " or " + args[3]);
			}
			switch (Oper.valueOf(Oper.class, args[1])) {
			case sub:
				if (args[0].length() >= e)
					return args[0].substring(n, e);
				else if (args[0].length() >= n)
					return args[0].substring(n);
				return "";
			default:
				break;
			}
		}
		throw new IllegalArgumentException("Invalid operation " + args[1]);
	}
}
