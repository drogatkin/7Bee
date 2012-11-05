/* bee - read.java
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
 *  $Id: read.java,v 1.2 2007/04/20 08:14:05 rogatkin Exp $
 * Created on Aug 11, 2004
 */

package org.bee.func;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import org.bee.util.Misc;
import org.bee.processor.ProcessException;

/**
 * @author Dmitriy
 *
 *  reads file content in a variable
 */
public class read {
	
	public static Object eval(String fileName, String sep, String enc) {
		// TODO: make it working with URLs
		// new File(fileName).toURL().openStream()
		InputStream is = null;
		try {
			String result = Misc.streamToString(is = new FileInputStream(fileName), enc, 10*1024);
			if (sep == null || sep.length() == 0)
				return result;
			if (result != null)
				return result.split(sep);
		} catch(IOException ioe) {
			System.err.println("func:read:error "+ioe);
			return new ProcessException("File reading error.", ioe);
		} finally {
			try {
				is.close();
			} catch(Exception e) {
				//
			}
		}
		return null;
	}
}