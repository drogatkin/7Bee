// $Id: cp.java,v 1.13 2006/06/23 06:00:12 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 20, 2004
package org.bee.func;

import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileFilter;

import org.bee.util.Misc;

/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 * 
 * A function for copying files, sources can be URL
 */
public class cp {
	protected boolean append;

	public static List<String> eval(String... copyPairs) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < copyPairs.length - 1; i += 2) {
			new cp().copy(result, copyPairs[i], copyPairs[i + 1], false);
		}
		return result;
	}

	protected void copy(List<String> copied, String srcMask, String dstMask, boolean append) {
		if (DEBUG_)
			System.out.printf("cp: %s %s (append %b)\n", srcMask, dstMask, append);
		this.append = append;
		String[] srcParts = Misc.splitBy(srcMask);
		String[] dstParts = Misc.splitBy(dstMask);
		if (srcParts.length == 0 || dstParts.length == 0)
			return;
		File srcFile = new File(srcParts[0]);
		if (srcParts.length == 1) {
			File dstFile = new File(dstParts[0]);
			if (dstParts.length == 1) {
				if (dstFile.getParentFile() != null && dstFile.getParentFile().exists() == false)
					dstFile.getParentFile().mkdirs();
				action(srcFile, dstFile, append);
			} else {
				if (dstFile.exists() == false)
					dstFile.mkdirs();
				action(srcFile, new File(dstParts[0], srcFile.getName()), append);
			}
		} else
			copy(copied, srcFile, srcParts, 1, dstParts[0], dstParts.length > 1 ? dstParts[dstParts.length - 1] : "",
					append);
	}

	protected void copy(List<String> copied, File srcPath, String[] parts, int pos, String destPath, String renMask,
			boolean append) {
		srcPath.listFiles(new FileCopier(copied, parts, pos, destPath, renMask, append));
	}

	protected class FileCopier implements FileFilter {
		protected String[] parts;

		protected int pos;

		protected String pat;

		protected String srcMask, renMask;

		protected String destPath;

		protected int srcConstLen;

		protected boolean append;

		protected List<String> copied;

		FileCopier(List<String> copied, String[] parts, int index, String destPath, String renMask, boolean append) {
			this.copied = copied;
			this.parts = parts;
			this.append = append;
			pos = index;
			pat = Misc.wildCardToRegExpr(this.parts[pos]);
			this.destPath = destPath;
			srcMask = this.parts[parts.length - 1];
			int vp = srcMask.lastIndexOf('?');
			int vp2 = srcMask.lastIndexOf('*');
			if (vp >= 0 && vp > vp2)
				srcConstLen = srcMask.length() - vp - 1;
			else if (vp2 >= 0)
				srcConstLen = srcMask.length() - vp2 - 1;
			if (pos != (this.parts.length - 1))
				// TODO: it can be calculated one and then spreaded, however it isn't big deal
				srcMask = Misc.wildCardToRegExpr(this.parts[parts.length - 1]);
			else
				srcMask = pat;
			vp = renMask.lastIndexOf('?');
			vp2 = renMask.lastIndexOf('*');
			if (vp >= 0 && vp > vp2)
				renMask = renMask.substring(vp + 1);
			else if (vp2 >= 0)
				renMask = renMask.substring(vp2 + 1);
			if (renMask.length() == 0)
				srcConstLen = 0;
			this.renMask = renMask;
		}

		public boolean accept(File pathname) {
			String name = pathname.getName();
			if (name.matches(pat)) {
				if (pathname.isFile()) {
					if (cp.DEBUG_)
						System.out.printf("cp: %s(%s) \n", pathname, srcMask);
					if (name.matches(srcMask)) {
						String srcPath = pathname.toString(); // getParent();
						File destFile = new File(destPath, srcPath.substring(parts[0].length(), srcPath.length()
								- srcConstLen)
								+ renMask);
						// System.out.printf("cp: dest %s \n", destFile);
						if (destFile.getParentFile().exists() == false)
							destFile.getParentFile().mkdirs();
						return action(pathname, destFile, append) != null;
					}
				} else if (pathname.isDirectory() && pos < parts.length - 1)
					copy(copied, pathname, parts, pos + 1, destPath, renMask, append);
			}
			return false;
		}
	}

	protected String action(File srcFile, File destFile, boolean append) {
		if (DEBUG_)
			System.out.printf("cp: plan to copy %s %s (append %b)\n", srcFile.toString(), destFile.toString(), append);
		else {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				if (destFile.isDirectory())
					destFile = new File(destFile, srcFile.getName());
				Misc.copyStream(fis = new FileInputStream(srcFile), fos = new FileOutputStream(destFile, append), 0);
			} catch (IOException ioe) {
				System.err.printf("bee:func:cp:error Can't copy %s to %s due exception %s\n", srcFile, destFile, ioe);
				return null;
			} finally {
				if (fis != null)
					try {
						fis.close();
					} catch (IOException e) {
					}
				if (fos != null)
					try {
						fos.close();
					} catch (IOException e) {
					}
			}
		}
		return srcFile.getName();
	}

	static final boolean DEBUG_ = false;
}
