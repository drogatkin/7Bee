package jdepend.framework;

import java.io.*;
import java.util.*;

/**
 * The <code>AbstractParser</code> class is the base
 * class for classes capable of parsing files to
 * create a <code>JavaClass</code> instance.
 *
 * @author <b>Mike Clark</b> (mike@clarkware.com)
 * @author Clarkware Consulting, Inc.
 */

public abstract class AbstractParser {
	
	private ArrayList parseListeners;
	
	private PackageFilter filter;
	
	public static boolean DEBUG = false;
	
	/**
	 * Constructs an <code>AbstractParser</code> instance
	 * with the default package filter.
	 */
	public AbstractParser() {
		this(new PackageFilter());
	}
	
	/**
	 * Constructs an <code>AbstractParser</code> instance
	 * with the specified package filter.
	 *
	 * @param filter Package filter.
	 */
	public AbstractParser(PackageFilter filter) {
		setFilter(filter);
		parseListeners = new ArrayList();
	}
	
	/**
	 * Registers the specified parser listener.
	 *
	 * @param listener Parser listener.
	 */
	public void addParseListener(ParserListener listener) {
		parseListeners.add(listener);
	}
      
    /**
     * Parses the specified input stream and returns 
     * a representative <code>JavaClass</code> instance.
     * <p>
     * Registered parser listeners are informed that the
     * resulting <code>JavaClass</code> was parsed.
     *
     * @param is Input stream to parse.
     * @return Java class.
     * @throws IOException If the input stream could not be parsed.
     */  
    public abstract JavaClass parse(InputStream is) 
        throws IOException;
	
	/**
	 * Informs registered parser listeners that the 
	 * specified <code>JavaClass</code> was parsed.
	 *
	 * @param jClass Parsed Java class.
	 */
	protected void onParsedJavaClass(JavaClass jClass) {
		Iterator i = parseListeners.iterator();
		while (i.hasNext()) {
			((ParserListener)i.next()).onParsedJavaClass(jClass);
		}
	}
	
	protected PackageFilter getFilter() {
		if (filter == null) {
			setFilter(new PackageFilter());
		}
		
		return filter;
	}
    
    protected void setFilter(PackageFilter filter) {
        this.filter = filter;
    }
	
	protected void debug(String message) {
		if (DEBUG) {
			System.err.println(message);
		}
	}
}
