package edu.udel.cis.vsl.abc.ast.node.IF.statement;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

/**
 * <b>Syntax</b>
 * <p>
 * Represents a CIVL-C <code>$with(p)</code> node. It has the form:
 * <code>$with(expr) statement</code> or
 * <code>$with(expr, &write_set) statement </code>.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface WithNode extends StatementNode {
	/**
	 * Returns the state reference expression attached with this WithNode.
	 * 
	 * A state reference expression represents reference (or pointer) to some
	 * object that can evaluate to a program state.
	 * 
	 * @return
	 */
	ExpressionNode getStateReference();

	/**
	 * If {@link #isCallWith()} returns true, the returned statement node must
	 * wraps a function call expression node. Else, it can be any kind of
	 * statement.
	 * 
	 * @return A {@link StatementNode} attached with the whole with expression.
	 */
	StatementNode getBodyNode();

	/**
	 * Returns true if and only if this node represents a "parallel" $with
	 * statement, i.e. the execution of this statement will not affect any thing
	 * outside of the $with scope.
	 * 
	 * A parallel $with statement takes an extra output argument : write_set
	 * 
	 * @return
	 */
	boolean isParallelStatement();

	@Override
	WithNode copy();
}
