// $Id: NameSpaceImpl.java,v 1.4 2004/03/26 05:56:02 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 16, 2004
package org.bee.util;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.bee.processor.Instruction;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class NameSpaceImpl implements Instruction.NameSpace {
	Map <String, InfoHolder < String, InfoHolder, Object >> nameSpace;
	public NameSpaceImpl() {
		nameSpace = new HashMap<String, InfoHolder< String, InfoHolder, Object >>();
	}
	
	public Iterator < InfoHolder < String, InfoHolder, Object >> iterator() {
		return nameSpace.values().iterator();
	}
	
	public void inScope(InfoHolder < String, InfoHolder, Object > var) {
		InfoHolder < String, InfoHolder, Object > oldVar = nameSpace.get(var.getKey());
		if (oldVar == null) 
			nameSpace.put(var.getKey(), var);
		else {
			nameSpace.put(var.getKey(), var);
		}
	}
	
	public void outScope(InfoHolder < String, InfoHolder, Object > var) {
		nameSpace.remove(var.getKey());
	}
	
	public InfoHolder < String, InfoHolder, Object > lookup(String name) {
		return nameSpace.get(name);
	}
}
