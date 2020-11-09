package jdepend.framework;

import java.io.*;
import java.util.*;

/**
 * The <code>PropertyConfigurator</code> class contains
 * configuration information contained in the 
 * <code>jdepend.properties</code> file, if such a
 * file exists either in the user's home directory or 
 * somewhere in the classpath.
 *
 * @author <b>Mike Clark</b> (mike@clarkware.com)
 * @author Clarkware Consulting, Inc.
 */

public class PropertyConfigurator {
	
	private Properties properties;
	
	public static final String DEFAULT_PROPERTY_FILE = 
		"jdepend.properties";
	
	
    /**
     * Constructs a <code>PropertyConfigurator</code> instance
     * containing the properties specified in the 
     * <code>jdepend.properties</code>file, if it exists.
     */
    public PropertyConfigurator() {
        this(getDefaultPropertyFile());
    }
    
    /**
     * Constructs a <code>PropertyConfigurator</code> instance
     * with the specified property set.
     *
     * @param p Property set.
     */
    public PropertyConfigurator(Properties p) {
        this.properties = p;
    }
    
    /**
     * Constructs a <code>PropertyConfigurator</code> instance
     * with the specified property file.
     *
     * @param f Property file.
     */
    public PropertyConfigurator(File f) {
        this(loadProperties(f));
    }
	
    /**
     * Returns the collection of filtered package names.
     * 
     * @return Filtered package names.
     */
    public Collection getFilteredPackages() {		
		
		Collection packages = new ArrayList();
		
		Enumeration e = properties.propertyNames(); 
		while (e.hasMoreElements()) {
			String key = (String)e.nextElement();
			if (key.startsWith("ignore")) {
				String path = properties.getProperty(key);
				System.out.println(this.getClass() .toString()+": read "+key+"="+path);
				StringTokenizer st = new StringTokenizer(path, ",");
				while (st.hasMoreTokens()) {
					String name = (String)st.nextToken();
					name = name.trim();
					packages.add(name);		
				}
			}
		}
		
		return packages;
	}
	
	public Collection getConfiguredPackages() {
	    
	    Collection packages = new ArrayList();
	    
        Enumeration e = properties.propertyNames(); 
        while (e.hasMoreElements()) {
            String key = (String)e.nextElement();
            if (!key.startsWith("ignore") && (!key.equals("analyzeInnerClasses"))) {
                String v = properties.getProperty(key);
                packages.add(new JavaPackage(key, new Integer(v).intValue()));
            }
        }
        
	    return packages;
	}
    
    public boolean getAnalyzeInnerClasses() {
        
        String key = "analyzeInnerClasses";
        if (properties.containsKey(key)) {
            String value = properties.getProperty(key);
            return new Boolean(value).booleanValue();
        }
        
        return true;
    }
	
    public static File getDefaultPropertyFile() {
		String home = System.getProperty("user.home");
		String configuration = System.getProperty("jdepend.configuration");
		if (null == configuration) {
		 		return new File(home, DEFAULT_PROPERTY_FILE);
		} else {
			return new File(configuration); 
		}
	}
	
    public static Properties loadProperties(File file) {

		Properties p = new Properties();
		
		InputStream is = null;

		try {
		
			is = new FileInputStream(file);
		
		} catch(Exception e) {
			is = PropertyConfigurator.class.
				getResourceAsStream("/" + DEFAULT_PROPERTY_FILE);
		}

		try {
			if (is != null) {
				p.load(is);
			}
		} catch(IOException ignore) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch(IOException ignore) {
			}
		}

		return p;
	}
}