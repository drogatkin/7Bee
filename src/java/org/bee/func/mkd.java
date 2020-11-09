// $Id: mkd.java,v 1.2 2012/04/17 22:02:49 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 20, 2004
package org.bee.func;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * 7Bee function for creation directories
*
 */
public class mkd {

	public static List < Boolean > eval(String...directoriesList) {
		List < Boolean > result = new ArrayList < Boolean > (directoriesList.length);
		for (String dirName : directoriesList) {
			result.add(new Boolean(new File(dirName).mkdirs()));
		}
		return result;
	}
		
}
