//Bee Copyright (c) 2004-2015 Dmitriy Rogatkin
// Created on Sep 21, 2015
package org.bee.func;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class region {

	public static List < String > eval(String s, String pattern) {
		Pattern p = Pattern.compile(pattern);
		Matcher  m = p.matcher(s);
		if (m.matches()) {
			int n = m.groupCount();
			ArrayList<String> result = new ArrayList<String>(n);
			for (int i=0; i<n; i++)
				result.add(m.group(i+1));
			return result;
		}
		return new ArrayList<String>();
	}
}
