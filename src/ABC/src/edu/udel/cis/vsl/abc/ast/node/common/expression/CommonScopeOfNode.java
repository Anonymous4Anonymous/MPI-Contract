package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ScopeOfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeableNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonScopeOfNode extends CommonExpressionNode
		implements
			ScopeOfNode {

	public CommonScopeOfNode(Source source, SizeableNode argument) {
		super(source, argument);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("ScopeOfNode");
	}

	@Override
	public boolean isConstantExpression() {
		return true;
	}

	@Override
	public ScopeOfNode copy() {
		return new CommonScopeOfNode(getSource(), duplicate(expression()));
	}

	@Override
	public ExpressionNode expression() {
		return (ExpressionNode) child(0);
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.SCOPEOF;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		if (child(0).nodeKind() == NodeKind.EXPRESSION) {
			return ((ExpressionNode) child(0))
					.isSideEffectFree(errorsAreSideEffects);
		}
		return true;
	}

	@Override
	public void setExpression(ExpressionNode expr) {
		setChild(0, expr);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonScopeOfNode has one child, but saw index " + index);
		if (!(child == null || child instanceof SizeableNode))
			throw new ASTException("Child of CommonScopeOfNode at index "
					+ index + " must be a SizeableNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
