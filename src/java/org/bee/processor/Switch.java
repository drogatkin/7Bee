// $Id: Switch.java,v 1.4 2005/04/04 22:30:20 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 23, 2004
package org.bee.processor;
import java.util.List;
import java.util.ArrayList;
import org.bee.util.InfoHolder;
import static org.bee.util.Logger.logger;
/**
 * @author <a href="dmitriy@mochamail.com">dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class Switch extends AbstractBlock {
    protected Expression expression;
    protected List<Block> blockCases;
	/**
	 * @param xpath
	 */
	public Switch(String xpath) {
		super(xpath);
		blockCases = new ArrayList<Block>();
	}

	public void childDone(Instruction child) {
		if (child instanceof Block)
			blockCases.add((Block)child);
		else if (child instanceof Expression) {
			if (expression != null)
				logger.severe("Only one expression allowed in 'switch'. An extra expression "+child+" ignored.");
			else {
				expression = (Expression)child;
				if (blockCases.size() > 0)
					logger.warning("An expression has to be first element of 'switch'.");
			}
		} else 
			logger.severe("Not allowed element of 'switch' "+child+" ignored.");
	}

	/* (non-Javadoc)
	 * @see org.bee.processor.Instruction#eval()
	 */
	public InfoHolder eval() {
		InfoHolder switchVal = null;
		if (expression != null) {
			 switchVal = expression.eval();
		}
		if (switchVal == null && variable != null) {
			InfoHolder < String, InfoHolder, Object > switchVar = lookupInChain(variable);
			if (switchVar != null)
				switchVal = switchVar.getValue();
		}
		if (switchVal == null) {
			logger.severe("Can't evaluate 'switch' value (null), the switch ignored.");
			return null;
		}
		InfoHolder result = null;
		logger.finest("Switch value="+switchVal.getValue());
		Block defaultBlock = null;
		for (Block block:blockCases) {
			if (block.blockType != Block.BlockType.t_case) {
				if (block.blockType == Block.BlockType.t_default)
					defaultBlock = block;
				else
					logger.warning("Block "+block+" was not defined as type 'case' or 'default', skipped.");
				continue;
			}
			if(block.value != null) {
				logger.finest("trying "+block.value);
				if (block.value.equals(switchVal.getValue()))
					result = block.eval();
			} else if (block.variable != null) {
				String blockVal = lookupStringValue(variable);
				if (blockVal != null && blockVal.equals(switchVal.getValue()))
					result = block.eval();
			} else 
				logger.severe("Case value of 'block' "+block+" can't be calculated");
		}
		if (result == null && defaultBlock != null) // not quite reliable since result can be returned as null
			result = defaultBlock.eval();
		return result;
	}
}
