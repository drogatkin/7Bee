// $Id: warit.java,v 1.15 2009/11/21 06:14:10 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 23, 2004
package org.bee.func;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.util.regex.Pattern;
import java.util.zip.ZipException;
import org.bee.util.Misc;
import org.bee.util.InfoHolder;

/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin </a>
 * 
 * This function create <b>.war </b> file specified by 1st parameter
 * <p>
 * 2nd provides location of <i>web.xml </i>, but it can be considered as content
 * of it, if the location can't be resolved.
 * <p>
 * Following parameters grouped by 2 or more parameters, and 1st in a group
 * provides
 * <p>
 * operation abbreviations, like
 * <ul>
 * <li><b>A [prfx] </b>- add files to .war
 * <ol>
 * <li>source path, can include wild cards like * or ?</li>
 * </ol>
 * </li>
 * <li><b>E [prfx] </b>- add files not in exclude list
 * <ol>
 * <li>source path, can include wild cards like * or ?</li>
 * <li>exclude list comma separated with names only, wild card can be used as
 * well</li>
 * </ol>
 * </li>
 * <li><b>C [prfx] </b>- add files in classes
 * <ol>
 * <li>source path</li>
 * <li>exclude list, can be empty</li>
 * </ol>
 * </li>
 * <li><b>L [prfx]</b>- add files in library
 * <ol>
 * <li>source path</li>
 * <li>exclude list, can be empty</li>
 * </ol>
 * </li>
 * <li><b>W destination</b>- add files in library
 * <ol>
 * <li>content to write</li>
 * </ol>
 * </li>
 * </ul>
 * <p>
 * Note: optional blank separated from command character prefix is used to
 * specify a dest path
 */
public class warit {
	public static final String WAR_EXT = ".war";

	protected enum Operation {
		add, exclude, classes, libraries, file
	};

	public static boolean eval(Object... args) {
		if (args.length == 0)
			return false; // nothing to do

		String warFileName = args[0].toString();
		if (warFileName.toLowerCase().endsWith(WAR_EXT) == false)
			warFileName += WAR_EXT;
		ZipOutputStream warFile = null;

		try {
			warFile = new ZipOutputStream(new FileOutputStream(warFileName));
			int pos = 1;
			pos = processWebXML(args, pos, warFile);

			while (pos < args.length) {
				if (args[pos] instanceof Object[])
					System.err.printf(
							"warit:error: an invalid type of parameter [%d] : %s, array of Object not supported%n", pos,
							Arrays.toString((Object[]) args[pos]));
				else if (args[pos] instanceof Collection) {
					args = insertTo(args, pos, (Collection) args[pos]);
				} else if (args[pos] == null) { // ignore
					pos++;
					continue;
				}
				InfoHolder<Operation, String, Object> op = parseCommand(args[pos++].toString());
				if (op == null) {
					System.err.printf("warit:error: invalid command argument %s\n", args[pos]);
					pos++;
					continue;
				}
				if (DEBUG_)
					System.out.printf("Process %d (%s) as %s\n", pos, args[pos], op.getKey());
				boolean recurs = true;
				switch (op.getKey()) {
				case add:
					for (String[] comps : parsePath(args[pos++]))
						addPath(op.getValue(), warFile, comps[0], comps[1], null, true);
					break;
				case libraries:
					recurs = false;
				case classes:
				case exclude:
					for (String[] comps : parsePath(args[pos++]))
						addPath(op.getValue(), warFile, comps[0], comps[1], (String)args[pos], recurs);
					pos++;
					break;
				case file:
					addContent(op.getValue(), warFile, args[pos++].toString().getBytes("UTF-8"));
					break;
				}
			}
			return true;
		} catch (ArrayIndexOutOfBoundsException aiobe) {
			//aiobe.printStackTrace();
			System.err.printf("warit:error: wrong number of parameters near %s, %s\n", args[args.length - 1], aiobe);
			try {
				warFile.close();
				warFile = null;
			} catch (IOException ioe) {
			}
			new File(warFileName).delete();
		} catch (IOException ioe) {
			System.err.printf("warit:error: %s\n", ioe);
		} finally {
			if (warFile != null)
				try {
					warFile.close();
				} catch (IOException e) {
				}
		}
		return false;
	}

	private static Object[] insertTo(Object[] args, int pos, Collection collection) {
		Object[] result = new Object[args.length + collection.size() - 1];
		if (pos > 0)
			System.arraycopy(args, 0, result, 0, pos);
		int i = pos;
		for (Object o : collection)
			result[i++] = o;
		if (result.length > i)
			System.arraycopy(args, pos + 1, result, i, args.length - pos - 1);
		return result;
	}

	protected static String[][] parsePath(Object p) {
		int size = 0;
		String result[][] = null;
		if (p instanceof List) {
			if (DEBUG_)
				System.out.printf("Processing %s as List\n", p);
			List pl = (List) p;
			size = pl.size();
			result = new String[size][];
			for (int i = 0; i < size; i++)
				result[i] = parseStringPath(pl.get(i).toString());
		} else if (p instanceof Object[]) {
			if (DEBUG_)
				System.out.printf("Processing %s as an Array\n", p);
			Object[] pa = (Object[]) p;
			size = pa.length;
			result = new String[size][];
			for (int i = 0; i < size; i++)
				result[i] = parseStringPath(pa[i].toString());
		} else {
			result = new String[1][];
			result[0] = parseStringPath(p.toString());
		}
		return result;
	}

	protected static String[] parseStringPath(String s) {
		char[] cs = s.toCharArray();
		int ls = -1;
		for (int p = 0; p < cs.length; p++) {
			char c = cs[p];
			if (c == '*' || c == '?') {
				return new String[] { s.substring(0, ls), s.substring(ls + 1) };
			} else if (c == '/' || c == '\\')
				ls = p;
		}
		File f = new File(s);
		if (f.isDirectory())
			return new String[] { s, "*" };
		return new String[] { s, "" };
	}

	protected static int processWebXML(Object[] args, int pos, ZipOutputStream warFile) throws IOException {
		InputStream wxis = null;
		long fileTime = System.currentTimeMillis();
		try {
			try {
				File inFile = new File(args[pos].toString());
				wxis = new FileInputStream(inFile);
				fileTime = inFile.lastModified();
			} catch (FileNotFoundException fnfe) {
				wxis = new ByteArrayInputStream(args[pos].toString().getBytes("UTF-8"));
			}
			if (wxis != null) {
				ZipEntry je = new ZipEntry("WEB-INF/web.xml");
				je.setTime(fileTime);
				warFile.putNextEntry(je);
				Misc.copyStream(wxis, warFile, 0);
			}
		} finally {
			if (wxis != null)
				wxis.close();
		}
		return pos + 1;
	}

	protected static InfoHolder<Operation, String, Object> parseCommand(String cmd) {
		if (cmd.length() == 0)
			return null;
		String prefix = null;
		if (cmd.length() > 2)
			prefix = cmd.substring(2).trim().replace('\\', '/');
		boolean isDir = prefix != null && prefix.length() > 0 && prefix.charAt(prefix.length() - 1) == '/';
		char cmdChr = cmd.charAt(0);
		if (cmdChr == 'A')
			return new InfoHolder<Operation, String, Object>(Operation.add, prefix);
		else if (cmdChr == 'E')
			return new InfoHolder<Operation, String, Object>(Operation.exclude, prefix);
		else if (cmdChr == 'C') {
			prefix = prefix == null ? "WEB-INF/classes/" : new File("WEB-INF/classes", prefix).toString().replace('\\', '/')
					+ (isDir ? "/" : "");
			return new InfoHolder<Operation, String, Object>(Operation.classes, prefix);
		} else if (cmdChr == 'L') {
			prefix = prefix == null ? "WEB-INF/lib/" : new File("WEB-INF/lib", prefix).toString().replace('\\', '/') + (isDir ? "/" : "");
			return new InfoHolder<Operation, String, Object>(Operation.libraries, prefix);
		} else if (cmdChr == 'W') {
			if (prefix != null)
				return new InfoHolder<Operation, String, Object>(Operation.file, prefix);
		}
		return null;
	}

	protected static void addPath(String targPath, ZipOutputStream warStream, String srcPath, String fileMask,
			String excludeMask, boolean processDirectories) throws IOException {
		if (DEBUG_)
			System.out.printf("Trying %s\n", srcPath);
		File filePath = new File(srcPath);
		if (filePath.isDirectory()) {
			File[] files = filePath.listFiles(new FileFilterImpl(fileMask, excludeMask));
			if (targPath == null)
				targPath = "";
			else if (targPath.endsWith("/") == false)
				targPath += '/';
			for (File file : files) {
				if (processDirectories || file.isDirectory() == false)
					addPath(targPath + file.getName(), warStream, file.toString(), fileMask, excludeMask,
							processDirectories);
			}
		} else if (filePath.isFile()) {
			Pattern excludePattern = patternFromWildCard(excludeMask);
			if (excludePattern != null && excludePattern.matcher(filePath.getName()).matches())
				return;
			if (DEBUG_)
				System.out.printf("adding %s to %s\n", filePath, targPath + '/' + filePath.getName());
			try {
				ZipEntry je = new ZipEntry(targPath == null ? filePath.getName() : targPath
						+ (targPath.endsWith("/") ? filePath.getName() : ""));
				je.setTime(filePath.lastModified());
				warStream.putNextEntry(je);
				Misc.copyStream(new FileInputStream(filePath), warStream, 0);
			} catch (ZipException ze) {
				if (ze.getMessage() != null && ze.getMessage().indexOf("duplicate entry") >= 0)
					System.err.printf("warit:warning: %s, the entry skipped.\n", ze.getMessage());
				else
					throw ze;
			}
		} else
			System.err.printf("warit:error: not found %s\n", filePath);
	}

	protected static void addContent(String targPath, ZipOutputStream warStream, byte[] content) throws IOException {
		InputStream is = new ByteArrayInputStream(content);
		warStream.putNextEntry(new ZipEntry(targPath));
		Misc.copyStream(is, warStream, 0);
		is.close();
	}

	static protected Pattern patternFromWildCard(String wc) {
		if (wc == null)
			return null;
		return Pattern.compile(wc.replace("?", "[^/\\\\:]").replace("*", "[^/\\\\?:*]*"));
	}

	protected static class FileFilterImpl implements FilenameFilter {
		protected Pattern maskPattern, excludePattern;

		FileFilterImpl(String mask, String exclude) {
			maskPattern = patternFromWildCard(mask);
			// TODO: consider exclude as ',' separated list, so split it by ','
			// first
			// NOTE: reg exp can include OR condition for the TODO above
			excludePattern = patternFromWildCard(exclude);
		}

		public boolean accept(File dir, String name) {
			if (excludePattern != null)
				return maskPattern.matcher(name).matches() && excludePattern.matcher(name).matches() == false;
			return maskPattern.matcher(name).matches();
		}
	}

	protected static final boolean DEBUG_ = false;
}
