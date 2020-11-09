// $Id: newerwithdependency.java,v 1.10 2004/12/23 10:30:33 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Apr 15, 2004
package org.bee.func;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import jdepend.framework.ClassFileParser;
import jdepend.framework.JavaClass;

/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class newerwithdependency extends newerthan {

	/** Figures out or changed Java files with dependencies
	 * 
	 */
	public newerwithdependency() {
		super();
		// TODO Auto-generated constructor stub
	}

	public static List eval(String srcPath, String dstPath, String defClass) {
		return eval(srcPath, dstPath, defClass, "");
	}

	public static List eval(String srcPath, String dstPath, String defClass, String pkgPrefPath) {
		if (DEBUG_)
			System.out.printf("newerwithdependency:parameters %s -> %s\n", srcPath, dstPath);
		List < String > result = newerthan.eval(srcPath, dstPath);
		// make it map for fast check
		String srcExt = extractExt(srcPath);
		int el = srcExt.length();
		String dstExt = extractExt(dstPath);
		srcPath = extractPath(normalize(srcPath));
		// make it absolute since
		srcPath = new File(srcPath).getAbsolutePath();
		if (result.size() == 0) {
			if (defClass != null && defClass.length()>0)
				// TODO: do conversion in file name if in form a class
				result.add(new File(srcPath, defClass.replace('.', File.separatorChar) + srcExt).getAbsolutePath());
			return result;
		}
		Map < String, String > srcClasses = new HashMap < String, String > (result.size());
		if (DEBUG_)
			System.out.println("newerwithdependency:src path:" + srcPath);
		if (pkgPrefPath != null && pkgPrefPath.length() > 0) {
			pkgPrefPath = pkgPrefPath.replace(File.separatorChar, '/');
			//pkgPrefPath = pkgPrefPath.replace('.', '/');
			if(pkgPrefPath.charAt(pkgPrefPath.length()-1) != '/')
				pkgPrefPath+='/';
			srcPath = srcPath.substring(0, srcPath.length()-pkgPrefPath.length());
			//cl-=pkgPrefPath.length();
		}
		int cl = srcPath.length();
		for (String srcFile : result) {
			String className = srcFile.substring(cl + 1, srcFile.length() - el).replace(File.separatorChar, '/');
			srcClasses.put(className, srcFile);
		}
		// traverse all files
		dstPath = extractPath(normalize(dstPath));
		if (DEBUG_)
			System.out.println("newerwithdependency:looking for:" + srcClasses);
		processDirectory(result, new File(dstPath), srcPath, srcClasses, srcExt, dstExt);
		// if a class has dependencies in the map, add to result
		// note no check for newly added dependent files since they are unchaged
		return result;
	}

	protected static String extractExt(String s) {
		int p = s.lastIndexOf('.');
		if (p < 0)
			return "";
		return s.substring(p);
	}

	protected static void processDirectory(List < String > result, File classPath, String srcPath, Map < String, String > dependencies, final String srcExt, final String dstExt) {
		if (classPath.exists() == false || classPath.isDirectory() == false)
			return;
		File[] lsf = classPath.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(dstExt);
			}
		});
		ClassFileParser classParser = new ClassFileParser(); // should be global?
		for (File f : lsf) {
			JavaClass javaClass = null;
			try {
				javaClass = classParser.parse(f);
			} catch (IOException ioe) {
				System.err.printf("newerwithdependency: %s was skipped due %s\n", f.toString(), ioe.toString());
				continue;
			}
			List < String > currentDepends = classParser.getDependencies();
			for (String dependName : currentDepends)
				if (dependencies.get(dependName) != null) {
					if (DEBUG_)
						System.out.printf("newerwithdependency: found %s dependent on %s\n", f.toString(), dependName);
					String className = javaClass.getName();
					int nested = className.indexOf('$');
					if (nested > 0)
						className = className.substring(0, nested);
					else {
						nested = className.indexOf('+');
						if (nested > 0)
							className = className.substring(0, nested);
					}
					nested = className.lastIndexOf('.');
					String packagePath = nested > 0 ? className.substring(0, nested + 1).replace('.', File.separatorChar) : "";
					// TODO: this is low efficient way to find duplication, so it can be reconsidered using map of already inserted
					String srcFile = new File(srcPath, packagePath + javaClass.getSourceFile()
					/*className.replace('.', File.separatorChar) + srcExt*/
					).getAbsolutePath();
//					 TODO: check if the file exists, since file can be moved
					if (result.contains(srcFile) == false && new File(srcFile).exists())					    
						result.add(srcFile);
					break;
				}
		}
		// can be joint in one step			
		lsf = classPath.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});
		for (File f : lsf)
			processDirectory(result, f, srcPath, dependencies, srcExt, dstExt);
	}

	public static void main(String[] args) {
	}
	
}
