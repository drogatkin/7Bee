// $Id: allnewer.java,v 1.10 2010/07/15 17:56:42 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 25, 2004
package org.bee.func;

import java.io.File;

import org.bee.util.Misc;

/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class allnewer {

	/** Determinate if content of some tree or individual file 
	 *  newer than content of another tree or individual file
	 * 
	 */
	public allnewer() {
		super();
	}

	public static boolean eval(String srcPath, String dstPath) {
		FileWithMask srcMask = getMask(new File(srcPath));
		FileWithMask dstMask = getMask(new File(dstPath));
		if (DEBUG_) {
			System.out.printf("allnewer: masks: %s <---> %s %n", srcMask.mask, dstMask.mask);
			System.out.printf("allnewer: %s @%d, %s @%d\n", srcPath, newest(0, srcMask.path, srcMask.mask), dstPath,
					newest(0, dstMask.path, dstMask.mask));
		}
		return newest(0, srcMask.path, srcMask.mask) > newest(0, dstMask.path, dstMask.mask);
	}

	protected static long newest(long newest, File path, String mask) {
		if (path.exists()) {
			if (path.isDirectory()) {
				File[] fs = path.listFiles();
				if (fs != null)
					for (File f : fs)
						if (mask == null || f.getName().matches(mask))
							newest = Math.max(newest, newest(newest, f, mask));
			} else {
				if (newest < path.lastModified()) {
					newest = path.lastModified();
					if (DEBUG_)
						System.out.printf("allnewer: updated for %s%n", path);
				}
			}
		} else if (DEBUG_)
			System.out.printf("allnewer: Non existent %s\n", path);
		return newest;
	}

	protected static FileWithMask getMask(File path) {
		String mask = path.getName();
		if (mask.indexOf('?') >= 0 || mask.indexOf('*') >= 0) {
			path = path.getParentFile();
			if (path == null)
				path = new File("." + File.separator);
			return new FileWithMask(path, Misc.wildCardToRegExpr(mask).replace(".", "\\."));
		}
		return new FileWithMask(path, null);
	}

	protected static final class FileWithMask {
		protected FileWithMask(File p, String m) {
			path = p;
			mask = m;
		}

		public String mask;

		public File path;
	}

	static final boolean DEBUG_ = false;
}
