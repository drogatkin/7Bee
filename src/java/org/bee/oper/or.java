/* bee - or.java
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
 *  $Id: or.java,v 1.3 2006/04/09 08:10:57 rogatkin Exp $
 * Created on Jul 9, 2004
 */

package org.bee.oper;
import java.util.Collection;
import org.bee.util.InfoHolder;
/**
 * @author dmitriy
 * 
 *  
 */
public class or {
	public static InfoHolder < String, String, Boolean > doOperator(InfoHolder < String, String, Object > op1, InfoHolder < String, String, Object > op2) {
		Boolean result = toBoolean(op1) || toBoolean(op2);
		return new InfoHolder < String, String, Boolean > ("or", result.toString(), result);
	}

	public static InfoHolder < String, String, Boolean > proceed(InfoHolder < String, String, Object > op1, InfoHolder < String, String, Object > op2) {
		return doOperator(op1, op2);
	}

	protected static Boolean toBoolean(InfoHolder<String, String, Object> ih) {
		if (ih == null)
			return Boolean.FALSE;
		Object o = ih.getType();
		if (o != null) {
			if (o instanceof Boolean)
				return (Boolean)o;
			else if (o instanceof Object[]) 				
				return ((Object[])o).length > 0;
			else if (o instanceof Collection)
				return ((Collection)o).size() >0;
		}
		o = ih.getValue();
		if (o == null)
			return Boolean.FALSE;
		if (o instanceof Object[])
			return ((Object[])o).length > 0;
		else if (o instanceof Collection)
			return ((Collection)o).size() >0;
		else if (o instanceof Boolean)
			return (Boolean)o;
		return new Boolean(o.toString());
	}
}
