// $Id: rm.java,v 1.11 2012/04/01 05:48:15 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 20, 2004
package org.bee.func;
import java.io.File;
import java.io.FileFilter;

import org.bee.util.Misc;
/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Delete files specified in parameter list, wild cards are allowed, however array
 * parameters are not
 */
public class rm {
	public static boolean eval(String...delPats) {
		boolean result = true;
		for (String pat : delPats) {
			result &= delete(pat);
		}
		return result;
	}

	protected static boolean delete(String pat) {
		String[] parts = Misc.splitBy(pat);
		if (parts.length == 1)
			return new File(parts[0]).delete();
		else if (parts.length == 0)
			return false;
		return delete(new File(parts[0]), parts, 1);
	}
	
	protected static boolean delete(File leadPath, String[] parts, int index) {
		File[] result = leadPath.listFiles(new FileDeleter(parts, index));
		return result==null || result.length == 0;
	}

	protected static class FileDeleter implements FileFilter {
		protected String[] parts;
		protected int pos;
		protected String pat;
		protected String delMask;
		FileDeleter(String[] parts, int index) {
			this.parts = parts;
			pos = index;
			pat = Misc.wildCardToRegExpr(this.parts[pos]);
			if (pos != (this.parts.length - 1))
				//TODO: it can be calculated one and then spread, however it isn't big deal
				delMask = Misc.wildCardToRegExpr(this.parts[parts.length - 1]);
			else
				delMask = pat;
		}
		public boolean accept(File pathname) {
			String name = pathname.getName();
			if (name.matches(pat)) {
				if (pathname.isFile()) {
					if (name.matches(delMask))
						return !delete(pathname);
				} else if (pathname.isDirectory() && pos < parts.length - 1)
					return !rm.delete(pathname, parts, pos + 1) && pathname.delete() || !pathname.delete();
			}
			return false;
		}

		@SuppressWarnings("unused")
		protected boolean delete(File pathname) {
			if (DEBUG_)
				System.out.printf("rm: plan to delete %s\n", pathname.toString());
			else
				return pathname.delete();
			return false;
		}

	}

	private static final boolean DEBUG_ = false;
}
