/* bee - StreamCatcher.java
 * Copyright (C) 1999-2004 Dmitriy Rogatkin.  All rights reserved.
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
 *  $Id: StreamCatcher.java,v 1.5 2007/04/01 01:53:05 rogatkin Exp $
 * Created on Aug 12, 2004
 */

package org.bee.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.PropertyResourceBundle;

/**
 * @author dmitriy
 *
 * 
 */
public class StreamCatcher extends Thread {
	public static final String EMPTY = "";

	public static final String CRLF = "\r\n";

	public static final char NL = '\n';

	public static final char CR = '\r';

	public static final char EQUAL = '=';

	protected InputStream is;

	protected PrintStream ps;

	int buf_size = 4 * 1024;

	protected StringBuffer sb;

	public StreamCatcher(InputStream is) {
		this(is, null);
	}

	public StreamCatcher(InputStream is, PrintStream ps) {
		this.is = is;
		this.ps = ps;
		if (ps == null)
			sb = new StringBuffer();
		setName("Stream catcher for " + is);
	}

	public void run() {
		try {
			byte[] buf = new byte[buf_size];
			while (true) {
				//if (is.available() > 0)
				int n = is.read(buf);
				if (n > 0) {
					if (ps != null)
						ps.write(buf, 0, n);
					if (sb != null)
						sb.append(new String(buf, 0, n));
				} else {
					//Logger. getLogger("Bee").finest("EOF");
					break;
				}
			}
			//is.close();
		} catch (IOException ioe) {
			Logger.getLogger("Bee").severe("Exception:" + ioe);
		}
	}

	public StringBuffer getContent() {
		return sb;
	}

	public String toString() {
		if (sb != null)
			return sb.toString();
		return "";
	}

	public Properties toProperties() {
		List<String> names = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		Properties envProperties = new Properties();
		char[] env = toString().toCharArray();

		try {
			String value = EMPTY;
			for (int i = 0; i < env.length; i++) {
				if (env[i] == EQUAL) {
					names.add(value);
					value = EMPTY;
				} else if (env[i] == CR) {
					i++;
					if (env[i] == NL) {
						values.add(value);
						value = EMPTY;
					}
				}

				if (env[i] != EQUAL && env[i] != NL) {
					value = value + env[i];
				}
			}

			for (int i = 0; i < names.size(); i++) {
				envProperties.setProperty(names.get(i).toString().toUpperCase(), values.get(i).toString());
			}
		} catch (Exception e) {
			Logger.getLogger("Bee").severe("Can't parse the string buffer output" + sb.toString());
			return null;
		}
		return envProperties;
	}

	public java.util.ResourceBundle getAsResourceBundle() {
		try {
			// TODO: work when Unicode used
			return new PropertyResourceBundle(new ByteArrayInputStream(sb.toString().getBytes()));
		} catch (IOException ioe) {
			Logger.getLogger("Bee").severe("IO problem in getting bundle " + ioe);
		}
		return null;
	}

	public boolean isEmpty() {
		return sb == null || sb.length() == 0;
	}
}
