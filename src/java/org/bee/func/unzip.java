// $Id: unzip.java,v 1.6 2012/03/07 05:28:01 dmitriy Exp $
//Bee Copyright (c) 2011 Dmitriy Rogatkin
// Created on Jun 26, 2011
package org.bee.func;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.bee.util.Misc;

/** A similar to zip utility
 * 
 * @author Dmitriy
 *
 */
public class unzip {
	/** main method
	 * 
	 * @param args 1 - zip archive location, 2 - target directory to unzip (optional, 3.. specific entry names (optional) 
	 * @return
	 */
	public static boolean eval(Object... args) {
		if (args.length == 0)
			return false;
		File srcFile = new File(args[0].toString());
		if (srcFile.exists() == false || srcFile.isFile() == false)
			return false;
		File targetLoc = args.length > 1 ? new File(args[1].toString()) : new File(System.getProperty("user.dir"));
		try {
			ZipFile zipFile = new ZipFile(srcFile);
			if (args.length > 2) {
				for (int i = 2; i< args.length; i++) {
					
					if (args[i] instanceof Collection) {
						ArrayList entries = new ArrayList();
						entries.addAll((Collection) args[i]);
						extractEntries(zipFile, targetLoc, entries.toArray());
					} else if (args[i] instanceof Object[] ) {
						extractEntries(zipFile, targetLoc, (Object[])args[i]);	
					} else
						extractEntries(zipFile, targetLoc, new Object[]{args[i]});
				}
			} else {
				Enumeration<? extends ZipEntry> entries = zipFile.entries();
				while (entries.hasMoreElements()) {
					extractEntry(zipFile, targetLoc, entries.nextElement());
				}
			}
			zipFile.close();
			return true;
		} catch (ZipException e) {
			System.err.printf("zip:error: %s%n", e);
		} catch (IOException e) {
			System.err.printf("zip:error: %s%n", e);
		}

		return false;
	}
	
	private static void extractEntries(ZipFile zipFile, File destDir, Object [] entries) throws IOException{
		for (int e =0; e < entries.length; e++) {
			String name = entries[e].toString();
			name = name .replace('\\', '/');
			//name = new String(name.getBytes(Charset.forName("utf-8")), Charset.forName("ASCII"));
			if (name.indexOf('*') > -1 || name.indexOf('?') > -1) {
				boolean wildStart = name.startsWith("./");
				Pattern pattern = Pattern.compile(wildStart?".*"+Misc.wildCardToRegExpr(name.substring(2)):Misc.wildCardToRegExpr(name));
				
				Enumeration<? extends ZipEntry> zipEntries = 
					zipFile.entries();
				while(zipEntries.hasMoreElements()) {
					ZipEntry ze = zipEntries.nextElement();
					//System.out.printf("Check entry %s to %s %n" , ze.getName(), pattern.pattern());
					if (pattern.matcher(ze.getName()).matches()) {
						extractEntry(zipFile, destDir, ze);
					}
			    }
			} else {
				ZipEntry entry = zipFile.getEntry(name);
				if (entry != null) {
					extractEntry(zipFile, destDir, entry);
				}	
			}
		}
	}
	
	private static void extractEntry(ZipFile zipFile, File targetLoc, ZipEntry zipEntry) throws IOException{
		String name = zipEntry.getName();
		//name = new String(name.getBytes(Charset.forName("ASCII")), "UTF-8");
		name = name.replace('/', File.separatorChar);
		File zipEntryFile = new File(targetLoc, name);
		boolean dir = zipEntry.isDirectory();
		File zipPath = dir?zipEntryFile:zipEntryFile.getParentFile();
		if (zipPath != null) {
			if (zipPath.exists() == false && zipPath.mkdirs() == false)
				throw new IOException("Can't create directory "+zipPath.getAbsolutePath());
		}
		if (dir == false && zipEntryFile.createNewFile()) {
			FileOutputStream os;
			Misc.copyStream(zipFile.getInputStream(zipEntry), os= new FileOutputStream(zipEntryFile), -1);
			os.close();
			zipEntryFile.setLastModified(zipEntry.getTime());
		}	
	}
}
