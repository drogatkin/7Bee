// $Id: OnExit.java,v 1.5 2004/06/04 05:56:37 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 18, 2004
package org.bee.processor;

import org.bee.util.InfoHolder;
import static org.bee.util.Logger.logger;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class OnExit extends AbstractBlock {
	protected If ifClause;
	/**
	 * @param xpath
	 */
	public OnExit(String xpath) {
		super(xpath);
		// TODO Auto-generated constructor stub
	}

	public void childDone(Instruction child) {
		if (child instanceof If && ifClause == null)
			ifClause = (If) child;
		else
			logger.severe("Only 'if' and only one is allowed on exit.");
	}

	/* (non-Javadoc)
	 * @see org.bee.processor.Instruction#eval()
	 */
	public InfoHolder eval() {
		if (ifClause != null)
			return ifClause.eval();
		logger.severe("No 'if' defined in exit block");
		return null;
	}

}
