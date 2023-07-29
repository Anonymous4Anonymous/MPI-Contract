package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ExtendedQuantifiedExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonExtendedQuantifiedExpressionNode extends CommonExpressionNode
		implements
			ExtendedQuantifiedExpressionNode {

	private ExtendedQuantifier quantifier;

	public CommonExtendedQuantifiedExpressionNode(Source source,
			ExtendedQuantifier quant, ExpressionNode lo, ExpressionNode hi,
			ExpressionNode function) {
		super(source, lo, hi, function);
		this.quantifier = quant;
	}

	@Override
	public ExpressionNode copy() {
		return new CommonExtendedQuantifiedExpressionNode(this.getSource(),
				this.quantifier, duplicate(this.lower()),
				duplicate(this.higher()), duplicate(this.function()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.EXTENDED_QUANTIFIED;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return this.lower().isSideEffectFree(errorsAreSideEffects)
				&& this.higher().isSideEffectFree(errorsAreSideEffects)
				&& this.function().isSideEffectFree(errorsAreSideEffects);
	}

	@Override
	public ExtendedQuantifier extQuantifier() {
		return this.quantifier;
	}

	@Override
	protected void printBody(PrintStream out) {
		switch (this.quantifier) {
			case MAX :
				out.print("\\max");
				break;
			case MIN :
				out.print("\\max");
				break;
			case SUM :
				out.print("\\max");
				break;
			case PROD :
				out.print("\\max");
				break;
			case NUMOF :
				out.print("\\max");
				break;
			default :
		}
	}

	@Override
	public ExpressionNode lower() {
		return (ExpressionNode) this.child(0);
	}

	@Override
	public ExpressionNode higher() {
		return (ExpressionNode) child(1);
	}

	@Override
	public ExpressionNode function() {
		return (ExpressionNode) child(2);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 3)
			throw new ASTException(
					"CommonExtendedQuantifiedExpressionNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonExtendedQuantifiedExpressionNode at index "
							+ index + " must be an ExpressionNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
