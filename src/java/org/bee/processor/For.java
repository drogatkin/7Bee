// $Id: For.java,v 1.16 2005/06/22 05:00:19 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 17, 2004
package org.bee.processor;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileFilter;
import java.util.regex.Pattern;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.bee.util.InfoHolder;
import org.bee.util.Misc;
import static org.bee.util.Logger.logger;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class For extends AbstractBlock {
    protected String inRange;

    protected String separator;

    protected List<Instruction> children;

    public For(String xpath) {
        super(xpath);
        children = new ArrayList<Instruction>();
    }

    public void childDone(Instruction child) {
        children.add(child);
    }

    public InfoHolder eval() {
        //List<InfoHolder> result = new ArrayList<InfoHolder>();
        InfoHolder result = null;
        //clearNameSpace();
        try {
            for (InfoHolder var : expandRange()) {
                getNameSpace().inScope(
                        new InfoHolder<String, InfoHolder, Object>(variable,
                                var));
                for (Instruction child : children) {
                    //result.add(child.eval());
                    result = child.eval();
                }
            }
        } catch (ProcessException pe) {
            // interrupt processing
            if (pe.getCause() != null || pe.getMessage() == null
                    || pe.getMessage().equals(name) == false)
                throw pe;
            logger.finest("For '"+name+"' interrupted");
        }
        //		logger.finest("For returned "+result);
        return result;
    }

    protected InfoHolder[] expandRange() {
        InfoHolder<String, InfoHolder, Object> rh = lookupInChain(inRange);
        String currSep = lookupStringValue(separator);
        if (currSep == null /*|| currSep.length()==0*/)
            currSep = separator;
        if (rh == null)
            return arrayToInfoHolders(inRange, inRange.split(currSep));
        InfoHolder<String, String, Type> rangeVal = rh.getValue();
        if (rangeVal == null)
            return arrayToInfoHolders(null, null);

        String s = rangeVal.getValue();
        Object clarifier = rangeVal.getType();
        if (clarifier instanceof Type) {
            Type type = (Type) clarifier;
            switch (type) {
            case file:
                InfoHolder<String, InfoHolder, Object> dh = lookupInChain(RESERVE_NAME_DIR);
                String dir = null;
                if (dh != null) {
                    InfoHolder<String, String, Type> dv = dh.getValue();
                    if (dv != null)
                        dir = dv.getValue();
                }
                if (dir == null)
                    dir = "."; // TODO: need to look in project dir, then .
                s = s.replace("?", "[^/\\\\:]");
                s = s.replace("*", "[^/\\\\?:*]*");
                final Pattern p = Pattern.compile(s);
                String[] names = new File(dir).list(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return p.matcher(name).matches();
                    }
                });
                return arrayToInfoHolders(s, names);
            //break;
            case directory:
                File fdir = makeFile(s);
                if (fdir.exists() && fdir.isDirectory()) {
                    List<String> dirs = new ArrayList<String>();
                    traverse(dirs, fdir);
                    return arrayToInfoHolders(s, dirs.toArray(new String[dirs
                            .size()]));
                }
                break;
            case variable:
            default:
                if (currSep.length() == 0)
                    return new InfoHolder[] { new InfoHolder<String, String, Object>(
                            s, s) };
                return arrayToInfoHolders(s, s.split(currSep));
            }
        } else if (clarifier instanceof List) {
            List<String> list = (List<String>) clarifier;
            return arrayToInfoHolders(s, list.toArray(new String[list.size()]));
        } else if (clarifier instanceof Object[]) {
            Object[] ao = (Object[]) clarifier;
            String[] as = new String[ao.length];
            for (int i = 0; i < ao.length; i++)
                as[i] = ao[i] != null ? ao[i].toString() : null;
            return arrayToInfoHolders(s, as);
        } else if (s != null && s.length() > 0) {
            return arrayToInfoHolders(variable, s.split(currSep));
        }
        return arrayToInfoHolders(null, null);
    }

    // arrays of generic types are not allowed
    protected InfoHolder /*< String, String, Object >*/
    [] arrayToInfoHolders(String s, String[] ss) {
        logger.finest("array:" + Arrays.toString(ss));
        if (ss == null || ss.length == 0)
            return new InfoHolder[0];
        InfoHolder[] result = new InfoHolder[ss.length];
        for (int i = 0; i < result.length; i++)
            result[i] = new InfoHolder<String, String, Object>(s, ss[i]);
        return result;
    }

    protected void traverse(final List<String> dirs, File dir) {
        dir.listFiles(new FileFilter() {
            public boolean accept(File file) {
                if (file.isDirectory())
                    dirs.add(file.getPath());
                traverse(dirs, file);
                return false;
            }
        });
    }

    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        super.startElement(uri, localName, qName, attributes);
        variable = attributes.getValue("", ATTR_VARIABLE);
        inRange = attributes.getValue("", ATTR_IN);
        separator = attributes.getValue("", ATTR_SEPARATOR);
        if (separator == null)
            separator = "[ \t\n\\x0B\f\r]";
    }

    public String[] getAllowedAttributeNames() {
        return Misc.merge(new String[] { ATTR_IN, ATTR_SEPARATOR }, super
                .getAllowedAttributeNames());
    }
}
