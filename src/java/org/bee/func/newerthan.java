// $Id: newerthan.java,v 1.17 2005/07/27 18:40:05 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 24, 2004
package org.bee.func;

import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * @author <a href="dmitriy@mochamail.com">Dmitriy Rogatkin </a>
 * 
 * Provide class description here
 */
public class newerthan {

    /**
     * find all files names which are newer than corresponding files in another
     * tree
     *  
     */
    public newerthan() {
        super();
        // TODO Auto-generated constructor stub
    }

    public static List eval(String srcPath, String dstPath) {
        // TODO: add 3rd parameter for possible default result if null list
        // happened
        //System.out.printf("Parameters %s -> %s\n", srcPath, dstPath);
        ArrayList<String> result = new ArrayList<String>();
        srcPath = normalize(srcPath);
        dstPath = normalize(dstPath);
        String srcMask = extractFile(srcPath);
        String destMask = extractFile(dstPath);
        srcPath = extractPath(srcPath);
        dstPath = extractPath(dstPath);
        if (DEBUG_)
            System.out.printf(
                    "newerthan:debug: sp %s sm %s dp %s dm %s in %s\n",
                    new File(srcPath).getAbsolutePath(), srcMask, dstPath,
                    destMask, new File("./").getAbsolutePath());
        processDirectory(result, new File(srcPath)/*.getAbsoluteFile()*/,
                srcPath, srcMask, dstPath, destMask);
        if (DEBUG_)
            System.out.printf("newerthan:debug: result: %s\n", result);
        return result;
    }

    protected static String normalize(String s) {
        assert File.separatorChar == '/' || File.separatorChar == '\\';
        char tc = '\\';
        if (File.separatorChar == tc)
            tc = '/';
        return s.replace(tc, File.separatorChar);
    }

    protected static String extractPath(String s) {
        int p = s.lastIndexOf(File.separatorChar);
        if (p < 0)
            return "." + File.separatorChar;
        return s.substring(0, p);
    }

    protected static String extractFile(String s) {
        int p = s.lastIndexOf(File.separatorChar);
        if (p < 0)
            return s;
        return s.substring(p + 1);
    }

    protected static void processDirectory(List<String> result, File path,
            String srcPath, String srcMask, String destPath, String destMask) {
        if (path.exists() == false || path.isDirectory() == false)
            return;
        if (DEBUG_)
            System.out.printf("newerthan:debug: process %s\n", path);
        File[] lsf = path.listFiles(new SmartFileFilter(srcMask, srcPath,
                destPath, destMask));
        for (File f : lsf)
            result.add(f.getAbsolutePath());
        lsf = path.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File f : lsf)
            processDirectory(result, f, srcPath, srcMask, destPath, destMask);
    }

    protected static class SmartFileFilter implements FileFilter {
        Pattern p;

        int ml, sl;

        String srcPath, destPath, destMask;

        String srcMask;

        List<String> result;

        SmartFileFilter(/* List <String> result, */String mask,
                String srcPath, String destPath, String destMask) {
            p = Pattern.compile("[^/\\\\?:\\*]*" + mask);
            ml = mask.length();
            this.srcMask = mask;
            this.destPath = destPath;
            this.srcPath = srcPath;
            this.destMask = destMask;

            sl = srcPath.length();
            if (sl > 0
                    && (srcPath.charAt(sl - 1) == '\\' || srcPath
                            .charAt(sl - 1) == '/'))
                sl--; // for sep
        }

        public boolean accept(File pathname) {
            if (pathname.isDirectory())
                return false; // processDirectory(result, pathname, srcPath,
            // srcMask, destPath, destMask);
            // TODO: path name can be one .java file which generates multiple
            // .class
            // files, so every of them should count (consider only top level,
            // not nested classes
            String n = pathname.getName();
            if (DEBUG_)
                System.out
                        .printf(
                                "newerthan:Path to accept %s, current parent len %d, mask len %d\n",
                                n, sl, ml);
            if (p.matcher(n).matches()) {
                n = n.substring(0, n.length() - ml);
                String parent = pathname.getParent();
                if (DEBUG_)
                    System.out.printf("newerthan:Parent %s, src %s\n", parent,
                            srcPath);
                assert parent.indexOf(srcPath) == 0;
                parent = parent.substring(sl);
                File destFile = new File(destPath + parent, n + destMask);
                //System.out.println("DF"+destFile);
                if (destFile.exists() == false)
                    return true;
                return pathname.lastModified() > destFile.lastModified();
            }
            return false;
        }
    }

    protected static final boolean DEBUG_ = false;
}
