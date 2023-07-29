package edu.udel.cis.vsl.civl.model.IF.expression;

import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;

/**
 * A system guard expression is a sudo guard expression for system function
 * calls. Its evaluation is actually done in the corresponding library executor.
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public interface SystemGuardExpression extends Expression {
	/**
	 * The name of the library that the invoked function belongs to.
	 * 
	 * @return
	 */
	String library();

	/**
	 * The invoked function.
	 * 
	 * @return
	 */
	CIVLFunction function();

	/**
	 * The list of arguments that this function call uses.
	 * 
	 * @return
	 */
	List<Expression> arguments();
}
