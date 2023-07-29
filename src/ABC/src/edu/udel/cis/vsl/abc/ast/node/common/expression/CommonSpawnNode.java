package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SpawnNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonSpawnNode extends CommonExpressionNode implements SpawnNode {

	public CommonSpawnNode(Source source, FunctionCallNode callNode) {
		super(source, callNode);
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public FunctionCallNode getCall() {
		return (FunctionCallNode) child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("SpawnNode");
	}

	@Override
	public SpawnNode copy() {
		return new CommonSpawnNode(getSource(), duplicate(getCall()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.SPAWN;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return false;
	}

	@Override
	public void setCall(FunctionCallNode call) {
		this.setChild(0, call);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonSpawnNode has one child, but saw index " + index);
		if (!(child == null || child instanceof SizeableNode))
			throw new ASTException("Child of CommonSpawnNode at index " + index
					+ " must be a FunctionCallNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
