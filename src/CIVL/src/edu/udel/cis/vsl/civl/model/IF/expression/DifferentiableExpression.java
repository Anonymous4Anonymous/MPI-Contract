package edu.udel.cis.vsl.civl.model.IF.expression;

import edu.udel.cis.vsl.civl.model.IF.AbstractFunction;

/**
 * This expression encodes the claim that a real, abstract function is
 * differentiable. The number of continuous derivatives is specified and the
 * domain is specified as the Cartesian product of closed intervals.
 * 
 * @author xxxx
 *
 */
public interface DifferentiableExpression extends Expression {
	
	/**
	 * The function that is claimed to be differentiable.
	 */
	AbstractFunction function();

	/**
	 * The number of derivatives that exist and are continuous. (Also known as,
	 * "continuity".)
	 * 
	 * @return the degree of the differentiability
	 */
	int degree();

	/**
	 * Gets the lower bounds of the intervals. If the function maps from R^n to
	 * R, then this array has length n. If the domain of the function is
	 * [a1,b1]x...x[an,bn], then this returns the array {a1, ..., an}.
	 * 
	 * @return the lower bounds of the domain intervals
	 */
	Expression[] lowerBounds();

	/**
	 * Gets the upper bounds of the intervals. If the function maps from R^n to
	 * R, then this array has length n. If the domain of the function is
	 * [a1,b1]x...x[an,bn], then this returns the array {b1, ..., bn}.
	 * 
	 * @return the upper bounds of the domain intervals
	 */
	Expression[] upperBounds();

}
