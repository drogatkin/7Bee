// $Id: Interrupt.java,v 1.3 2011/08/30 03:35:58 dmitriy Exp $
//Bee Copyright (c) 2005 Dmitriy Rogatkin
// Created on Jun 10, 2005
package org.bee.processor;

import org.bee.util.InfoHolder;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class Interrupt extends AbstractValue {

	public Interrupt(String xpath) {
		super(xpath);
	}

	/* (non-Javadoc)
	 * @see org.bee.processor.Instruction#eval()
	 */
	public InfoHolder eval() {
		throw new ProcessException(getName());
	}

	public String getName() {
		return name;
	}
}
