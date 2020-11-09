// $Id: Operator.java,v 1.11 2005/06/15 08:02:23 rogatkin Exp $
//Bee Copyright (c) 2004 Dmitriy Rogatkin
// Created on Mar 11, 2004
package org.bee.processor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;
import static java.util.logging.Level.*;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import static org.bee.processor.Configuration.*;
import static org.bee.util.Logger.logger;
import org.bee.util.InfoHolder;

/**
 * @author <a href="Dmitriy@mochamail.com">Dmitriy Rogatkin</a>
 *
 * Provide class description here
 */
public class Operator extends AbstractBlock {
	//protected String type;
	protected Method method;
	protected List < AbstractValue > operands;

	public Operator(String xpath) {
		super(xpath);
		operands = new ArrayList < AbstractValue > ();
	}

	public InfoHolder eval() {
		InfoHolder result = null;
		int nop = operands.size() ;
		if (nop > 0) {
			result = operands.get(0).eval();
			if (nop < 2)
				result = doOperator(result, null);
			else
				for (int i = 1; i < nop; i++)
					result = doOperator(result, operands.get(i).eval());
		} else
			result = doOperator(result, null);
		if (variable != null) {
			//getNameSpace().inScope(new InfoHolder < String, InfoHolder, Object > (variable, result));
			if (parent.getNameSpace() != null)
				parent.getNameSpace().inScope(new InfoHolder < String, InfoHolder, Object > (variable, result));
		}
		logger.finest("'" + name + "'="+result);
		return result;
	}

	public void childDone(Instruction child) {
		operands.add((AbstractValue) child);
	}

	protected InfoHolder doOperator(InfoHolder result, InfoHolder operand) {
		logger.finest("'" + name + "' ("+result+", " + operand+')');
		if (method != null)
			try {
				return (InfoHolder) method.invoke(null, result, operand);
			} catch (InvocationTargetException ite) {
				if (logger.isLoggable(FINEST))
					logger.log(FINEST, "InvocationTargetException:", ite.getCause());
				else
					logger.warning("InvocationTargetException:" + ite.getCause());
				
			} catch (Exception ex) {
				if (logger.isLoggable(FINEST))
					logger.log(FINEST, "", ex);
				else
					logger.warning("" + ex);
				
			}
		return null;
	}

	public static Class findOperatorClass(String name) {
		if (name == null)
			return null;
		if (name.indexOf('.') < 0) // consider as full qualified name
			name = OPERATORS_PACKAGE + name;
		try {
			return Class.forName(name);
		} catch (Error er) {
			// too bad
			logger.severe("Class " + name + " " + er);
		} catch (Exception ex) {
			logger.severe("Class " + name + " not found or " + ex);
		}
		return null;
	}

	public static Method getMethod(Class classof, String name) {
		if (classof == null)
			return null;
		try {
			return classof.getMethod(name, InfoHolder.class, InfoHolder.class);
		} catch (NoSuchMethodException nsm) {
			logger.severe("Method " + name + " not found or " + nsm);
		}
		return null;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		super.startElement(uri, localName, qName, attributes);
		method = getMethod(findOperatorClass(name), OPERATOR_METHOD_NAME);
	}
}
