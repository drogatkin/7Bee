package jdepend.framework;

import java.util.*;

/**
 * The <code>JavaPackage</code> class represents
 * a Java package.
 *
 * @author <b>Mike Clark</b> (mike@clarkware.com)
 * @author Clarkware Consulting, Inc.
 */

public class JavaPackage {
	
	private String name;
	private int volatility;
	
	private HashSet classes;
	
	//
	// Packages that use this package.
	//
	private List afferents;
	
	//
	// Packages that this package uses.
	//
	private List efferents; 
	
	/**
	 * Constructs a <code>JavaPackage</code> instance with
	 * the specified package name.
	 *
	 * @param name Package name.
	 */
	public JavaPackage(String name) {
		this(name, 1);
	}
	
    public JavaPackage(String name, int volatility) {
        this.name = name;
        setVolatility(volatility);
        classes = new HashSet();
        afferents = new ArrayList();
        efferents = new ArrayList();
    }
	
	/**
	 * Returns the package name.
	 *
	 * @return Name.
	 */
	public String getName() {
		return name;
	}
	
    /**
     * Returns the volatility of this package.
     *
     * @return Volatility (0-1).
     */
    public int getVolatility() {
        return volatility;
    }
    
    /**
     * Sets the volatility of this package.
     *
     * @param v Volatility (0-1).
     */
    public void setVolatility(int v) {
        volatility = v;
    }
	
	/**
	 * Indicates whether the package contains
	 * a package dependency cycle.
	 *
	 * @return <code>true</code> if a cycle exist;
	 *         <code>false</code> otherwise.
	 */
	public boolean containsCycle() {	
		return collectCycle(new ArrayList());
	}
		
	/**
	 * Collects the packages participating in the
     * first package dependency cycle detected which
     * originates from this package. 
	 *
	 * @param list Collecting object to be populated with
	 *        the list of JavaPackage instances in a cycle.
	 * @return <code>true</code> if a cycle exist;
	 *         <code>false</code> otherwise.
	 */
	public boolean collectCycle(List list) {
		
		if (list.contains(this)) {
			list.add(this);
			return true;
		}
		
		list.add(this);
		
		Iterator efferents = getEfferents().iterator();
		while (efferents.hasNext()) {
			JavaPackage efferent = (JavaPackage)efferents.next();
			if (efferent.collectCycle(list)) {
				return true;
			}
		}
		
		list.remove(this);
		
		return false;	
	}
    
    /**
     * Collects all the packages participating in
     * a package dependency cycle which originates
     * from this package.
     * <p>
     * This is a more exhaustive search than that
     * employed by <code>collectCycle</code>.
     *
     * @param list Collecting object to be populated with
     *        the list of JavaPackage instances in a cycle.
     * @return <code>true</code> if a cycle exist;
     *         <code>false</code> otherwise.
     */
    public boolean collectAllCycles(List list) {

        if (list.contains(this)) {
            list.add(this);
            return true;
        }

        list.add(this);

        Iterator efferents = getEfferents().iterator();
        boolean containsCycle = false;
        while (efferents.hasNext()) {
            JavaPackage efferent = (JavaPackage)efferents.next();
            if (efferent.collectAllCycles(list)) {
                containsCycle = true;
            }
        }
        
        if (containsCycle) {
            return true;
        } else {
            list.remove(this);
            return false;
        }
    }
	
	/**
	 * Adds the specified Java class to the package.
	 *
	 * @param clazz Java class to add.
	 */
	public void addClass(JavaClass clazz) {
		classes.add(clazz);
	}
	
	/**
	 * Returns the collection of Java classes
	 * in this package.
	 *
	 * @return Collection of Java classes.
	 */
	public Collection getClasses() {
		return classes;
	}
	
	/**
	 * Returns the total number of classes in
	 * this package.
	 *
	 * @return Number of classes.
	 */
	public int getClassCount() {
		return classes.size();
	}
		
	/**
	 * Returns the number of abstract classes 
	 * (and interfaces) in this package.
	 *
	 * @return Number of abstract classes.
	 */
	public int getAbstractClassCount() {
		int count = 0;
		
		Iterator classIter = classes.iterator();
		while (classIter.hasNext()) {
			JavaClass clazz = (JavaClass)classIter.next();
			if (clazz.isAbstract()) {
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * Returns the number of concrete classes in
	 * this package.
	 *
	 * @return Number of concrete classes.
	 */
	public int getConcreteClassCount() {
		int count = 0;
		
		Iterator classIter = classes.iterator();
		while (classIter.hasNext()) {
			JavaClass clazz = (JavaClass)classIter.next();
			if (!clazz.isAbstract()) {
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * Adds the specified Java package as an efferent 
	 * of this package and adds this package as an
	 * afferent of it.
	 *
	 * @param imported Java package.
	 */
	public void dependsUpon(JavaPackage imported) {
		addEfferent(imported);
		imported.addAfferent(this);
	}
	
	/**
	 * Adds the specified Java package as an afferent 
	 * of this package.
	 *
	 * @param jPackage Java package.
	 */
	public void addAfferent(JavaPackage jPackage) {
		if (!jPackage.getName().equals(getName())) {
			if (!afferents.contains(jPackage)) {
				afferents.add(jPackage);
			}
		}
	}
	
	/**
	 * Returns the collection of afferent packages.
	 *
	 * @return Collection of afferent packages.
	 */
	public Collection getAfferents() {
		return afferents;
	}
	
	/**
	 * Sets the collection of afferent packages.
	 *
	 * @param afferents Collection of afferent packages.
	 */
	public void setAfferents(Collection afferents) {
		this.afferents = new ArrayList(afferents);
	}
	
	/**
	 * Adds the specified Java package as an efferent 
	 * of this package.
	 *
	 * @param jPackage Java package.
	 */
	public void addEfferent(JavaPackage jPackage) {
		if (!jPackage.getName().equals(getName())) {
			if (!efferents.contains(jPackage)) {
				efferents.add(jPackage);
			}
		}
	}
	
	/**
	 * Returns the collection of efferent packages.
	 *
	 * @return Collection of efferent packages.
	 */
	public Collection getEfferents() {
		return efferents;
	}
	
	/**
	 * Sets the collection of efferent packages.
	 *
	 * @param efferents Collection of efferent packages.
	 */
	public void setEfferents(Collection efferents) {
		this.efferents = new ArrayList(efferents);
	}
	
	/**
	 * Returns the afferent coupling (Ca) of this package.
	 *
	 * @return Ca
	 */
	public int afferentCoupling() {
		return afferents.size();
	}
	
	/**
	 * Returns the efferent coupling (Ce) of this package.
	 *
	 * @return Ce
	 */
	public int efferentCoupling() {
		return efferents.size();
	}
		
	/**
	 * Returns the instability (I) of this package.
	 *
	 * @return Instability (0-1).
	 */
	public float instability() {
		
		float totalCoupling = 
			(float)efferentCoupling() + (float)afferentCoupling();
		
		if (totalCoupling > 0) {
			return ((float)efferentCoupling()) / totalCoupling;
		}
		
		return 0;
	}
		
 	/**
	 * Returns the abstractness (A) of this package.
	 *
	 * @return Abstractness (0-1).
	 */
	public float abstractness() {   
		
		if (getClassCount() > 0) {
			return (float)getAbstractClassCount() / 
				(float)getClassCount();
		}
		
		return 0;
	}
		
	/**
	 * Returns this package's distance from the main sequence (D).
	 *
	 * @return Distance.
	 */
	public float distance() {
		float d = Math.abs(abstractness() + instability() - 1);
		return d * volatility;
	}
	
	/**
	 * Indicates whether the specified package is equal
	 * to this package.
	 *
	 * @param other Other package.
	 * @return <code>true</code> if the packages are equal;
	 *         <code>false</code> otherwise.
	 */
	public boolean equals(Object other) {
	
		if (other instanceof JavaPackage) {
			JavaPackage otherPackage = (JavaPackage)other;
			return otherPackage.getName().equals(getName());
		}
		
		return false;
	}
}