package edu.udel.cis.vsl.civl.model.IF.expression;

/**
 * The CIVL-C <code>$value_at(state, PID, expr)</code> expression, evaluating
 * the given expression expr at the given state ($state).
 * 
 * @author Manchun Zheng
 *
 */
public interface ValueAtExpression extends Expression {
	/**
	 * returns the node that represents the state reference
	 * 
	 * @return
	 */
	Expression state();

	/**
	 * returns the pid whose context to be used when evaluating the expression
	 * 
	 * @return
	 */
	Expression pid();

	/**
	 * returns the expression to be evaluated
	 * 
	 * @return
	 */
	Expression expression();
}
