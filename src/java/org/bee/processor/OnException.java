/* $Id: OnException.java,v 1.3 2005/06/15 08:02:23 rogatkin Exp $
 * Bee Copyright (c) 2004 Dmitriy Rogatkin
 * Created on 30.04.2004
 *
 *
 */
package org.bee.processor;
import org.bee.util.InfoHolder;
import static org.bee.util.Logger.logger;
/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Exception handler
 */
public class OnException extends OnExit {
	protected Block block;
	/**
	 * @param xpath
	 */
	public OnException(String xpath) {
		super(xpath);
	}

	public void childDone(Instruction child) {
		if (child instanceof Block && block == null)
			block = (Block) child;
		else
			logger.severe("Only 'block' and only one is allowed on exception.");
	}

	public InfoHolder eval() {
		//logger.log(java.util.logging.Level.FINEST,"call trace", new Exception());
		if (block != null)
			return block.eval();
		return null;
	}
}
