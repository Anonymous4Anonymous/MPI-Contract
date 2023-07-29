package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ObjectOrRegionOfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonObjectOrRegionOfNode extends CommonExpressionNode
		implements
			ObjectOrRegionOfNode {
	/**
	 * true iff this is an <code>$object_of</code> node; otherwise, this is a
	 * <code>$region_of</code> node
	 */
	private boolean isObject = true;

	public CommonObjectOrRegionOfNode(Source source, boolean isObject,
			ASTNode operand) {
		super(source, operand);
		this.isObject = isObject;
	}

	@Override
	public ObjectOrRegionOfNode copy() {
		return new CommonObjectOrRegionOfNode(this.getSource(), this.isObject,
				duplicate(this.operand()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.OBJECT_OR_REGION_OF;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return this.operand().isSideEffectFree(errorsAreSideEffects);
	}

	@Override
	public ExpressionNode operand() {
		return (ExpressionNode) this.child(0);
	}

	@Override
	public boolean isObjectOf() {
		return this.isObject;
	}

	@Override
	public boolean isRegionOf() {
		return !this.isObject;
	}

	@Override
	protected void printBody(PrintStream out) {
		if (this.isObject)
			out.println("OBJECT_OF");
		else
			out.println("REGION_OF");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonObjectOrRegionOfNode has only one child, but saw index "
							+ index);
		return super.setChild(index, child);
	}
}
