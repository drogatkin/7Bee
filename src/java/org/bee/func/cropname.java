// $Id: cropname.java,v 1.6 2008/04/21 04:32:00 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 1, 2004
package org.bee.func;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.ArrayList;
import static org.bee.util.Misc.*;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class cropname {

	/**
	 * 
	 */
	public cropname() {
		super();
	}

	public static List < String > eval(String param1, String param2) {
            return eval(param1, param2, "");
	}

	public static List < String > eval(String param1, String param2, final String replaceTo) {
           return eval(param1, param2, replaceTo, null);
	}

	public static List < String > eval(String param1, String param2, final String replaceTo, final String all) {
		final String mask = //wildCardToRegExpr(param2);
			param2.replaceAll("\\?", "[^/\\]").replaceAll("\\*", "[^/\\]*");
		final List < String > result = new ArrayList < String > ();
		if (param1.indexOf('*') < 0 && param1.indexOf('?') < 0 || param1.indexOf('\n') >= 0)
			if (all == null)
				result.add(param1.replaceFirst(mask, replaceTo));
			else
				result.add(param1.replaceAll(mask, replaceTo));
		else {
			final String[] pe = splitBy(param1);
			// TODO: pe.length <= 1
			if (pe.length > 1) {
				new File(pe[0]).listFiles(new FileFilter() {
					public boolean accept(File pathname) {
						String n = pathname.getName();
						if (n.matches(wildCardToRegExpr(pe[pe.length - 1])))
							if (all == null)
								result.add((pe[0] + n).replaceFirst(mask, replaceTo));
							else
								result.add((pe[0] + n).replaceAll(mask, replaceTo));
						return false;
					}
				});
			}
		}
		return result;
	}
}
