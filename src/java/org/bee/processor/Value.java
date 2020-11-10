// $Id: Value.java,v 1.32 2011/06/27 06:46:45 dmitriy Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 18, 2004
package org.bee.processor;

import java.util.Date;
import java.net.URL;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
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
public class Value extends AbstractValue {
	protected String separator;

	public Value(String xpath) {
		super(xpath);
	}

	public InfoHolder eval() {
		if (variable != null) {
			// traceInChain(variable);
			InfoHolder<String, InfoHolder, Object> d = lookupInChain(variable);
			// logger.finest("Found '"+variable+"' of "+d+" type "+type);
			if (d != null)
				if (type == Type.variable)
					return d.getValue();
				else {
					InfoHolder<String, String, Object> vi = d.getValue();
					if (vi != null)
						value = vi.getValue();
				}
			else {
				if (type == Type.variable)
					return new InfoHolder<String, String, Object>(variable, lookupStringValue(variable));
				else
					value = lookupStringValue(variable);
			}
		}
		switch (type) {
		case date:
			try {
				return new InfoHolder<String, String, Date>(name, value, SimpleDateFormat.getInstance().parse(value));
			} catch (java.text.ParseException pe) {
			} catch (NullPointerException npe) {
			}
			return new InfoHolder<String, String, Date>(name, value);
		case number:
			if (value == null)
				return new InfoHolder<String, String, Double>(name, null);
			return new InfoHolder<String, String, Double>(name, value, new Double(value));
		case file:			
			if (value == null)
				return new InfoHolder<String, String, File>(name, null);
			File file = makeFile(value);
			String fileName = file.getName();
			String parentPat = new File(value).getParent();			
			logger.finest("File made "+file+", work name "+fileName+", parent dir: "+parentPat);
			if (fileName.indexOf('*') < 0 && fileName.indexOf('?') < 0 && (parentPat== null || parentPat.indexOf('*') < 0 && parentPat.indexOf('?') < 0))
				return new InfoHolder<String, String, File>(name, value, new File(value));
			String baseDir = parentPat != null && parentPat.length() > 0?file.getParent().substring(0, file.getParent().length()-parentPat.length()):file.getParent();			
			fileName = fileName.replaceAll("\\?", "[^/\\]").replaceAll("\\*", "[^/\\]*");
			String[] parentPats = parentPat==null || baseDir.length() == 0?new String[]{}:parentPat.replaceAll("\\?", "[^/\\]").replaceAll("\\*", "[^/\\]*").split((File.separator.equals("\\")?"\\":"")+File.separator);
			final Pattern p = Pattern.compile(fileName);
			final List<String> fileList = new ArrayList<String>();
			// processes - /aaa/bbb/ff*/*/*f/*.txt
			if (baseDir.length() == 0)
				baseDir = parentPat;
			processDirectory(fileList, baseDir, parentPats, 0, p);
			logger.finest("For " + baseDir + ", files: " + fileList.toString());
			return new InfoHolder<String, String, List<String>>(name, fileList.toString(), fileList);
		case path:
			if (value == null)
				return new InfoHolder<String, String, File>(name, null);
			File filePath = makeFile(value);
			fileName = filePath.getName();
			if (fileName.indexOf('*') >= 0 || fileName.indexOf('?') >= 0)
				logger.warning("Not allowed characters in path " + value);
			// if (fileName.endsWith("."))
			// filePath = filePath.getParentFile();
			return new InfoHolder<String, String, File>(name, filePath.getAbsolutePath(), filePath);
		case directory:
			// logger.finest("Directory: " + value);
			// TODO if wild card, then returned all subdirectoories matching
			if (value == null)
				return new InfoHolder<String, String, File>(name, null);
			if (value.length() == 0)
				value = ".";
			// filePath = makeFile(value);
			// fileName = filePath.getName();
			filePath = new File(value);
			return new InfoHolder<String, String, File>(name, filePath.toString(), filePath);
		case array:
			if (value == null)
				return new InfoHolder<String, String, List>(name, null);
			if (separator == null)
				separator = "[\\s]";
			String[] values = value.split(separator);
			return new InfoHolder<String, String, List>(name, value, Arrays.asList(values));
		case environment:
			if (value == null)
				return new InfoHolder<String, String, Object>(name, System.getenv(name));
			break;
		case property:
			if (value == null) {
				String propName = name != null ? name : variable;
				if (propName != null) {
					logger.finest("Prop " + propName + "=" + System.getProperty(propName));
					return new InfoHolder<String, String, Object>(name, System.getProperty(propName));
				} else
					logger
							.warning("Requested 'property' value type, but neither 'name' nor 'variable' weren't specified.");
			}
			break;
		case repo_artifact:
			// parse repo as name-vendor-jar-version, for example maven:org.glassfish:javax.json:1.04
			if (value == null)
				break;
			String repDetails[] = value.split(":");
			if (repDetails.length != 4) {
				logger.warning("Requested 'repo_artifact' value type, but it doesn't match pattern: name:vendor:product:version.");
				break;
			}
			if ("maven".equals(repDetails[0]))
				try {
					File tempRepo = makeFile(".temp_repo");
					if (!tempRepo.exists())
						if (tempRepo.mkdirs())
							logger.finest("Temp repo directory " + tempRepo + " created");
					tempRepo = new File(tempRepo, repDetails[2]+"-"+repDetails[3] + ".jar");
					if (!tempRepo.exists()) { // TODO perhaps check date last modified
						String urlBase = System.getProperties().getProperty("maven_base");
						if (urlBase == null || !urlBase.startsWith("http"))
							urlBase = "https://repo1.maven.org/maven2/";
						URL url = new URL(urlBase + repDetails[1].replace('.', '/') + "/"
								+ repDetails[2] + "/" + repDetails[3] + "/" + repDetails[2] + "-" + repDetails[3]
								+ ".jar");
						InputStream uis = null;
						FileOutputStream fos = null;
						try {
							if (Misc.copyStream(uis = url.openStream(), fos = new FileOutputStream(tempRepo), 0) == 0) {
								throw new IOException("Empty file copied");
							}
						} catch (Exception e) {
							logger.warning("Can't load artifact from " + url + " : " + e);
						} finally {
							try {
								uis.close();
							} catch (Exception e) {

							}
							try {
								fos.close();
							} catch (Exception e) {

							}
						}
						if (tempRepo.length() == 0)
							tempRepo.delete();
					}
					tempRepo = tempRepo.getAbsoluteFile();
					return new InfoHolder<String, String, File>(name, tempRepo.getPath(), tempRepo);
				} catch (Exception e) {
					logger.fine("Value " + value + " can't be presented as repo URL, " + e);
					value = null;
				}
			break;
		case url:
			try {
				URL url = new URL(value);
				return new InfoHolder<String, String, URL>(name, url.toString(), url);
			} catch (Exception e) {
				logger.fine("Value " + value + " can't be present as URL, " + e);
				value = null;
			}
			break;
		case eval:
			InfoHolder<String, InfoHolder, Object> v = lookupInChain(value);
			if (v != null)
				return v.getValue();
			break;
		default:
		  logger.finest("Type unknown for "+value);
		}
		return new InfoHolder<Object, String, Object>(name, value);
	}

	private void processDirectory(final List<String> targetList, String currentDir, final String[] dirMasks, final int maskDirPos, final Pattern filePat) {
		if (maskDirPos < dirMasks.length) {
			final Pattern dirMaskPat = Pattern.compile(dirMasks[maskDirPos]);
			new File(currentDir).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (dirMaskPat.matcher(name).matches()) {
						File newCurrent = new File(dir, name);
						if (newCurrent.isDirectory())
							processDirectory(targetList, newCurrent.getPath(), dirMasks, maskDirPos+1, filePat);
					}
					return false;
				}
			});
		} else {
			new File(currentDir).listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (filePat.matcher(name).matches())
						targetList.add(dir.getPath() + File.separator + name);
					return false;
				}
			});
		}
	}
	
	public String getName() {
		return name;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		separator = attributes.getValue("", ATTR_SEPARATOR);
        if ( separator != null )
            separator = this.lookupStringValue(separator);
	}

	public String toString() {
		return super.toString() + ", name=" + name + ",var=" + variable + ",val=" + value+",sep="+separator;
	}
}
