// $Id: If.java,v 1.5 2004/05/06 07:43:37 rogatkin Exp $
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
public final class If extends AbstractBlock {
	protected Expression expression;
	protected Block thenBlock;
	protected Block elseBlock;
	/**
	 * @param xpath
	 */
	public If(String xpath) {
		super(xpath);
	}

	public void childDone(Instruction child) {
		if (child instanceof Expression)
			if (expression == null)
				expression = (Expression) child;
			else
				logger.severe("Only one expression is allowed in 'if'.");
		else if (child instanceof Block) {
			Block block = (Block) child;
			if (block.blockType == Block.BlockType.t_then && thenBlock == null)
				thenBlock = block;
			else if (block.blockType == Block.BlockType.t_else && elseBlock == null)
				elseBlock = block;
			else
				logger.severe("Unexpected block /" + block.blockType);
		} else
			logger.severe("unexpected instruction in 'if' /" + child);
	}

	/* (non-Javadoc)
	 * @see org.bee.processor.Instruction#eval()
	 */
	public InfoHolder eval() {
		if (expression != null) {
			InfoHolder < String, String, Object > er = expression.eval();
			if (er != null && Boolean.TRUE.toString().equalsIgnoreCase(er.getValue())) {
				if (thenBlock != null)
					return thenBlock.eval();
				logger.warning("'if' <condition> is true, however no type [then] block defined.");
			} else {
				if (elseBlock != null)
					return elseBlock.eval();
				logger.warning("'if' <condition> is false, however no type [else] block defined.");
			}
		} else
			logger.severe("An expression missed 'if'.");
		return null;
	}

}
