package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeofNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonSizeofNode extends CommonExpressionNode
		implements
			SizeofNode {

	public CommonSizeofNode(Source source, SizeableNode argument) {
		super(source, argument);
	}

	@Override
	public SizeableNode getArgument() {
		return (SizeableNode) child(0);
	}

	@Override
	public void setArgument(SizeableNode argument) {
		setChild(0, argument);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("SizeOfNode");
	}

	@Override
	public boolean isConstantExpression() {
		return !getArgument().getType().isVariablyModified();
	}

	@Override
	public SizeofNode copy() {
		return new CommonSizeofNode(getSource(), duplicate(getArgument()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.SIZEOF;
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
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonSizeofNode has one child, but saw index " + index);
		if (!(child == null || child instanceof SizeableNode))
			throw new ASTException("Child of CommonSizeofNode at index " + index
					+ " must be a SizeableNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
