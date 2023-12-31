package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.RegularRangeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonRegularRangeNode extends CommonExpressionNode
		implements
			RegularRangeNode {

	public CommonRegularRangeNode(Source source, ExpressionNode low,
			ExpressionNode high) {
		super(source, low, high);
	}

	public CommonRegularRangeNode(Source source, ExpressionNode low,
			ExpressionNode high, ExpressionNode step) {
		super(source, low, high, step);
	}

	@Override
	public ExpressionNode copy() {
		return new CommonRegularRangeNode(getSource(), duplicate(getLow()),
				duplicate(getHigh()), duplicate(getStep()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.REGULAR_RANGE;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		ExpressionNode stepNode = getStep();

		return getLow().isSideEffectFree(errorsAreSideEffects)
				&& getHigh().isSideEffectFree(errorsAreSideEffects)
				&& (stepNode == null
						|| stepNode.isSideEffectFree(errorsAreSideEffects));
	}

	@Override
	public ExpressionNode getLow() {
		return (ExpressionNode) child(0);
	}

	@Override
	public ExpressionNode getHigh() {
		return (ExpressionNode) child(1);
	}

	@Override
	public ExpressionNode getStep() {
		return numChildren() < 3 ? null : (ExpressionNode) child(2);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("RegularRange");
	}

	@Override
	public void setLow(ExpressionNode arg) {
		setChild(0, arg);
	}

	@Override
	public void setHigh(ExpressionNode arg) {
		setChild(1, arg);
	}

	@Override
	public void setStep(ExpressionNode arg) {
		setChild(2, arg);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 3)
			throw new ASTException(
					"CommonRegularRangeNode has at most three children, but saw index "
							+ index);
		if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonRegularRangeNode at index "
					+ index + " must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
