package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ValueAtNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonValueAtNode extends CommonExpressionNode
		implements
			ValueAtNode {

	public CommonValueAtNode(Source source, ExpressionNode state,
			ExpressionNode pid, ExpressionNode expr) {
		super(source, state, pid, expr);
	}

	@Override
	public ExpressionNode copy() {
		return new CommonValueAtNode(getSource(), duplicate(stateNode()),
				duplicate(pidNode()), duplicate(expressionNode()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.VALUE_AT;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return stateNode().isSideEffectFree(errorsAreSideEffects)
				&& pidNode().isSideEffectFree(errorsAreSideEffects);
	}

	@Override
	public ExpressionNode stateNode() {
		return (ExpressionNode) child(0);
	}

	@Override
	public ExpressionNode pidNode() {
		return (ExpressionNode) child(1);
	}

	@Override
	public ExpressionNode expressionNode() {
		return (ExpressionNode) child(2);

	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("$value_at");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 3)
			throw new ASTException(
					"CommonValueAtNode has three children, but saw index "
							+ index);
		if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonValueAtNode at index "
					+ index + " must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
