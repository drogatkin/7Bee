package jdepend.framework;

import java.io.*;
import java.util.*;

/**
 * The <code>PackageFilter</code> class is used to
 * filter imported package names.
 * <p>
 * The default filter contains any packages declared in 
 * the <code>jdepend.properties</code> file, if such a
 * file exists either in the user's home directory or 
 * somewhere in the classpath.
 *
 * @author <b>Mike Clark</b> (mike@clarkware.com)
 * @author Clarkware Consulting, Inc.
 */

public class PackageFilter {
	
	private Collection filtered;
	
    /**
     * Constructs a <code>PackageFilter</code> instance
     * containing the filters specified in the 
     * <code>jdepend.properties</code>
     * file, if it exists.
     */
    public PackageFilter() {
        this(new ArrayList());
        PropertyConfigurator config = new PropertyConfigurator();
        addPackages(config.getFilteredPackages());
    }
    
    /**
     * Constructs a <code>PackageFilter</code> instance
     * containing the filters contained in the specified
     * file.
     *
     * @param f Property file.
     */
    public PackageFilter(File f) {
        this(new ArrayList());
        PropertyConfigurator config = new PropertyConfigurator(f);
        addPackages(config.getFilteredPackages());
    }
    
	/**
	 * Constructs a <code>PackageFilter</code> instance
	 * with the specified collection of package names
	 * to filter.
	 *
	 * @param packageNames Package names to filter.
	 */
	public PackageFilter(Collection packageNames) {
        filtered = new ArrayList();
		addPackages(packageNames);
	}
    
    /**
     * Returns the collection of filtered package names.
     * 
     * @return Filtered package names.
     */
    public Collection getFilters() {
        return filtered;
    }
		
	/**
	 * Indicates whether the specified package name passes
	 * this package filter.
	 *
	 * @param packageName Package name.
	 * @return <code>true</code> if the package name should
	 *         be included; <code>false</code> otherwise.
	 */
	public boolean accept(String packageName) {
		
		Iterator names = filtered.iterator();
		while (names.hasNext()) {
			String nameToFilter = (String)names.next();
			if (packageName.startsWith(nameToFilter)) {
				return false;
			}
		}		
		
		return true;
	}
	
    public void addPackages(Collection packageNames) {		
        Iterator nameIter = packageNames.iterator();
        while (nameIter.hasNext()) {
            addPackage((String)nameIter.next());
        }
    }
	
	public void addPackage(String packageName) {
		
		if (packageName.endsWith("*")) {
            packageName = packageName.substring(0, packageName.length()-1);
		}
		
		if (packageName.length() > 0) {
			filtered.add(packageName);
		}
	}
}