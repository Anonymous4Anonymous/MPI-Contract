package edu.udel.cis.vsl.abc.ast.node.IF.expression;

/**
 * The CIVL-C <code>$value_at(state, PID, expr)</code> expression, evaluating
 * the given expression expr at the given state ($state).
 * 
 * @author Manchun Zheng
 *
 */
public interface ValueAtNode extends ExpressionNode {
	/**
	 * returns the node that represents the state reference
	 * 
	 * @return
	 */
	ExpressionNode stateNode();

	/**
	 * returns the pid whose context to be used when evaluating the expression
	 * 
	 * @return
	 */
	ExpressionNode pidNode();

	/**
	 * returns the expression to be evaluated
	 * 
	 * @return
	 */
	ExpressionNode expressionNode();
}
