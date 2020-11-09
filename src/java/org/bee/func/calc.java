// $Id: calc.java,v 1.6 2011/07/27 04:13:16 dmitriy Exp $
//Bee Copyright (c) 2011 Dmitriy Rogatkin
// Created on Jul 07, 2011

package org.bee.func;

import java.util.Collection;
import java.util.Stack;

/**
 * this function is arithmetic calculator
 * 
 * @author Dmitriy Rogatkin
 * 
 */
public class calc {
	enum opers {
		nop, left_par, right_par, plus, minus, mul, div, pwr, sqr, sqrt, cos, sin, tan, atan, log, ln
	};

	/**
	 * processes operands as arithmetic operations and functions the following
	 * +,-,*,/,(,),power, sqrt, sqr, sin, cos, tan, atan, log, ln
	 * <p>
	 * Only scalar value are currently supported, all arguments will be
	 * converted in double
	 * <p>
	 * Result is double value as well
	 * 
	 * @param args
	 * @return
	 */
	public static Object eval(Object... args) {
		if (args[0] instanceof Collection)
			args = ((Collection)args[0]).toArray();
		Stack<StateHolder> mem = new Stack<StateHolder>();
		double result = 0d;
		double current_oprd;
		opers current_oper, non_committed = opers.plus;
		opers deffered_oper = opers.nop;
		double current_result = result;
		current_oper = opers.plus;
		for (Object arg : args) {
			current_oprd = 0d;
			if (DEBUG)
				System.err.printf("oper %s %f %s = %f  deferred %s/%s%n",
						current_oper, current_result, arg, result,
						non_committed, deffered_oper);
			if (arg == null) {
			} else {
				if (arg instanceof Number) {
					current_oprd = ((Number) arg).doubleValue();
				} else {
					String normVal = arg.toString().trim().toLowerCase();
					if (normVal.length() > 0) {
						try {
							current_oprd = Double.parseDouble(normVal);
						} catch (NumberFormatException nfe) {
							opers last_oper = current_oper;
							current_oper = str2oper(normVal);
							switch (current_oper) {
							case plus:
							case minus:
								switch (non_committed) {
								case plus:
									result += current_result;
									break;
								case minus:
									result -= current_result;
									break;
								}
								non_committed = current_oper;
								deffered_oper = opers.nop;
								break;
							case left_par:
								// push current status in stack and reinit as in
								// start
								mem.push(new StateHolder(last_oper,
										non_committed, deffered_oper, result,
										current_result));
								current_result = result = 0d;
								current_oper = opers.plus;
								non_committed = opers.nop;
								deffered_oper = opers.nop;
								break;
							case right_par:
								if (mem.isEmpty())
									throw new RuntimeException(
											"No matching open ( for this )");
								switch (non_committed) {
								case plus:
									result += current_result;
									break;
								case minus:
									result -= current_result;
									break;
								}

								StateHolder sh = mem.pop();
								current_oprd = result;
								last_oper = current_oper;
								current_oper = sh.oper;
								non_committed = sh.non_comit_oper;
								current_result = sh.cur_res;
								result = sh.res;
								deffered_oper = sh.deffered;
								break; // to pass through
							case mul:
							case div:
							case pwr:
								deffered_oper = current_oper;
								break;
							default:
								
							}
							if (last_oper != opers.right_par)
								continue;
						}
					}
				}
				// found numeric operand
				switch (current_oper) {
				case sin:
					current_oprd = Math.sin(current_oprd);
					break;
				case cos:
					current_oprd = Math.cos(current_oprd);
					break;
				case tan:
					current_oprd = Math.tan(current_oprd);
					break;
				case atan:
					current_oprd = Math.atan(current_oprd);
					break;
				case log:
					current_oprd = Math.log10(current_oprd);
					break;
				case ln:
					current_oprd = Math.log(current_oprd);
					break;
				case sqrt:
					current_oprd = Math.sqrt(current_oprd);
					break;
				case sqr:
					current_oprd = current_oprd * current_oprd;
					break;
				}
				if (deffered_oper != opers.nop)
					current_oper = deffered_oper;
				switch (current_oper) {
				case mul:
					current_result *= current_oprd;
					break;
				case div:
					current_result /= current_oprd;
					break;
				case pwr:
					current_result = Math.pow(current_result, current_oprd);
					break;
				case plus:
				case minus:
				case nop:
					non_committed = current_oper;
				default:
					current_result = current_oprd;
				}
			}
		}
		switch (non_committed) {
		case plus:
			result += current_result;
			break;
		case minus:
			result -= current_result;
			break;
		}
		return result;
	}
	
	static private opers str2oper(String s) {
		if ("+".equals(s)) {
			return opers.plus;
		} else if ("-".equals(s)) {
			return opers.minus;
		} else if ("*".equals(s)) {
			return opers.mul;
		} else if ("/".equals(s)) {
			return opers.div;
		} else if ("(".equals(s)) {
			return opers.left_par;
		} else if (")".equals(s)) {
			return opers.right_par;
		} else if ("power".equals(s)) {
			return opers.pwr;
		} else if ("sqr".equals(s)) {
			return opers.sqr;
		} else if ("sqrt".equals(s)) {
			return opers.sqrt;
		} else if ("sin".equals(s)) {
			return opers.sin;
		} else if ("cos".equals(s)) {
			return opers.cos;
		} else if ("tan".equals(s)) {
			return opers.tan;
		} else if ("atan".equals(s)) {
			return opers.atan;
		} else if ("log".equals(s)) {
			return opers.log;
		} else if ("ln".equals(s)) {
			return opers.ln;
		} else
			throw new RuntimeException(
					"Unrecognized parameter:" + s);		
	}

	static class StateHolder {
		opers oper, non_comit_oper, deffered;
		double res, cur_res;

		StateHolder(opers o, opers nco, opers def, double r, double cr) {
			oper = o;
			non_comit_oper = nco;
			res = r;
			cur_res = cr;
			deffered = def;
		}
	}

	private final static boolean DEBUG = false;
}