package edu.udel.cis.vsl.civl.model.IF.expression;

import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ExtendedQuantifiedExpressionNode.ExtendedQuantifier;

/**
 * The representation for the "fold expression" in ACSL, e.g.
 * <code>\sum(low, high, f)</code> where <code>f</code> is a function from
 * integer to integer/real
 * 
 * @author xxxx
 *
 */
public interface ExtendedQuantifiedExpression extends Expression {
	/**
	 * returns the quantifier of this expression
	 * 
	 * @return
	 */
	ExtendedQuantifier extendedQuantifier();

	/**
	 * returns the lower bound
	 * 
	 * @return
	 */
	Expression lower();

	/**
	 * returns the higher bound
	 * 
	 * @return
	 */
	Expression higher();

	/**
	 * returns the function
	 * 
	 * @return
	 */
	Expression function();
}
