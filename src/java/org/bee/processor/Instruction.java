// $Id: Instruction.java,v 1.19 2006/07/07 05:48:40 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 10, 2004
package org.bee.processor;
import java.util.Iterator;
import org.xml.sax.ContentHandler;
import org.bee.util.XmlPath;
import org.bee.util.InfoHolder;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public interface Instruction {
	// TODO: split to two intarfaces, one is base used for value, variables and so on not
	// including any 
	public static final String ATTR_NAME = "name";
	public static final String ATTR_VARIABLE = "variable";
	public static final String ATTR_TARGET = "target";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_PROPERTY = "property";
	public static final String ATTR_IN = "in";
	//public static final String ATTR_CONDITION = "condition";
	public static final String ATTR_EXEC = "exec";
	public static final String ATTR_CODE = "code";
	public static final String ATTR_DIR = "dir";
	public static final String ATTR_URL = "url";
	public static final String ATTR_COMMENT = "comment";
	public static final String ATTR_VALUE = "value";
	public static final String ATTR_PATH = "path";
	public static final String ATTR_OPTIONS = "options";
	public static final String ATTR_SEPARATOR = "separator";
	public static final String ATTR_PROCESSONLY = "process_only";
	public static final String ATTR_STDOUT = "stdout";
	public static final String ATTR_ERROUT = "errout";
	public static final String ATTR_STDIN = "stdin";
	public static final String ATTR_STDOUT_STREAM = "stdout_stream";
	public static final String ATTR_ERROUT_STREAM = "errout_stream";

	public static final String RESERVE_NAME_DIR = "~#dir#~";
	public static final String RESERVE_NAME_ARGS = "~#args#~";
	public static final String RESERVE_NAME_ERROR = "~#error#~";
	public static final String RESERVE_NAME_EXCEPTION = "~#exception#~";
	public static final String RESERVE_OPTION_NOINPUT = "~#option-noinput#~";
	public static final String RESERVE_BUILD_FILE = "~#build-file#~";
	public static final String RESERVE_CLASS_LIB = "~#class-lib#~";

	public static final String TYPE_VARIABLE = "variable";
	public static final String TYPE_FILE = "file";
	public static final String TYPE_DIRECTORY = "directory";
	public static final String TYPE_URL = "url";
	public static final String TYPE_NUMBER = "number";
	public static final String TYPE_DATE = "date";
	public static final String TYPE_BOOL = "bool";

	/** provides a handler to parce instruction
	 * 
	 * @return
	 */
	ContentHandler getHandler();

	/** Provides path to the instruction
	 * 
	 * @return <code>XmlPath</path> path<br>
	 * generally path can be modified outside, so it should return clone
	 * if path is statically calculated inside
	 */
	XmlPath getPath();

	/** Returns value of instruction
	 * 
	 * @return <code>InfoHolder</code> 
	 */
	InfoHolder eval(); // eval

	/** call back from processor to set parent instruction
	 * 
	 * @param parent
	 */
	void setParent(Instruction parent);

	/** returns parent, not sure that we need it at all
	 * 
	 * @return parent
	 */
	Instruction getParent();

	/** Child notifies a parent that evaluation done, and a parent can use it
	 * for own evaluation
	 * @param child
	 */
	void childDone(Instruction child);

	/** returns instruction name
	 * 
	 * @return name
	 */
	String getName();
	
	/** Funtions used only for diagnostic purpose to check if
	 * not allowed attribute for instruction used.
	 * 
	 * @return String[] names of allowed attribute, can return null
	 * if any attribute is allowed
	 */
	String[] getAllowedAttributeNames();

	/** return current name space
	 * 
	 * @return NameSpace where children can be stored
	 * makes no sense for simple variables
	 */
	NameSpace getNameSpace();

	/** Generally name space keep object values reachable by keys
	 * 
	 */
	public static interface NameSpace {
		/** Gives access to name space content
		 * 
		 * < String, InfoHolder, Object >
		 * <String, String, ? extends Object>
		 * < String, InfoHolder, ScopeType >
		 */
		Iterator < InfoHolder < String, InfoHolder, Object >> iterator();
		/** Add expression or variable to inscope
		 * 
		 *
		 */
		void inScope(InfoHolder < String, InfoHolder, Object > var);
		/** Remove expression or variable out of scope
		 * 
		 *
		 */
		void outScope(InfoHolder < String, InfoHolder, Object > var);
		/** Lookup for specific variable or expression in scope
		 * 
		 */
		InfoHolder < String, InfoHolder, Object > lookup(String name);
	}

}
