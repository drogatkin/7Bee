// $Id: exclude.java,v 1.3 2005/07/27 18:40:05 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 28, 2004
package org.bee.oper;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.bee.util.InfoHolder;

/**
 * @author <a href="dmitriy@mochamail.com">dmitriy Rogatkin</a>
 *
 * Excludes elements from array based on reg exp
 */
public class exclude {
	public static InfoHolder < String,
		String,
		List < Object > > proceed(InfoHolder < String, String, List < Object > > op1, InfoHolder < String, String, Object > op2) {
		return doOperator(op1, op2);
	}

	public static InfoHolder < String,
		String,
		List < Object > > doOperator(InfoHolder < String, String, List < Object > > op1, InfoHolder < String, String, Object > op2) {
		if (op1 == null || op1.getType() == null)
			return null;
		if (op2 == null || op2.getValue() == null)
			return op1;
        Object excludeMask = op2.getType();
        if (excludeMask == null)
            excludeMask = op2.getValue();
        Object result = op1.getType();
        if (excludeMask instanceof List) 
            for(Object mask:(List)excludeMask)
                result = doExclude(result, mask.toString());
        else if (excludeMask instanceof Object[]) 
            for(Object mask:(Object[])excludeMask)
                result = doExclude(result, mask.toString());
        else
            result = doExclude(result, excludeMask.toString());
		return new InfoHolder < String, String, List < Object > > (op1.getKey(), result == null ? null : result.toString(), (List<Object>)result);
	}
    
    protected static List<Object> doExclude(Object targetList, String excludeMask) {
        List < Object > result = null;
        Pattern regExpPat = Pattern.compile(excludeMask);
        if (targetList instanceof Object[]) {
            Object[] a = ((Object[]) targetList);
            result = new ArrayList < Object > (a.length);
            for (Object e : a)
                if (regExpPat.matcher(e.toString()).matches() == false)
                    result.add(e);
        } else if (targetList instanceof List) {
            List < Object > l = (List < Object >) targetList;
            result = new ArrayList < Object > (l.size());
            for (Object e : l)
                if (regExpPat.matcher(e.toString()).matches() == false)
                    result.add(e); 
        } else {
            result = new ArrayList < Object > (1);
            if (regExpPat.matcher(targetList.toString()).matches() == false)
	            result.add(targetList);
        }
        return result;
    }
}
