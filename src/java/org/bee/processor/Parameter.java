// $Id: Parameter.java,v 1.6 2006/07/09 05:28:50 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 11, 2004
package org.bee.processor;
import org.bee.util.InfoHolder;
import static org.bee.util.Logger.logger;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class Parameter extends Value {
	protected AbstractBlock calculatedValue;
	public Parameter(String xpath) {
		super(xpath);
	}

	public void childDone(Instruction child) {
		if (calculatedValue != null) {
			// TODO think about auto array for parameter
			logger.severe("Only one calculated value can be used for parameter /" + child);
		} else if (child instanceof AbstractBlock)
			calculatedValue = (AbstractBlock) child;
		else
			logger.severe("Not allowed here /" + child);
	}

	public InfoHolder eval() {
		if (calculatedValue != null)
			return calculatedValue.eval();
		return super.eval();
	}
}
