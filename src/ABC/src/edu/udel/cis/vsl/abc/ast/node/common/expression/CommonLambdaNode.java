package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;
import java.util.Arrays;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.LambdaNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonLambdaNode extends CommonExpressionNode
		implements
			LambdaNode {

	/**
	 * @param source
	 *            The source code information for this expression.
	 * @param quantifier
	 *            The quantifier for this expression. One of {FORALL, EXISTS,
	 *            UNIFORM}.
	 * @param variableList
	 *            The list of bound variable declarations.
	 * @param restriction
	 *            Boolean-valued expression
	 * @param expression
	 *            the expression that is quantified
	 */
	public CommonLambdaNode(Source source,
			VariableDeclarationNode freeVariableDecl,
			ExpressionNode expression) {
		super(source, Arrays.asList(freeVariableDecl, null, expression));
	}

	public CommonLambdaNode(Source source,
			VariableDeclarationNode freeVariableDecl,
			ExpressionNode restriction, ExpressionNode expression) {
		super(source, Arrays.asList(freeVariableDecl, restriction, expression));
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public ExpressionNode copy() {
		return new CommonLambdaNode(this.getSource(), duplicate(freeVariable()),
				duplicate(lambdaFunction()));
	}

	@Override
	public VariableDeclarationNode freeVariable() {
		return (VariableDeclarationNode) this.child(0);
	}

	@Override
	public ExpressionNode restriction() {
		return (ExpressionNode) this.child(1);
	}

	@Override
	public ExpressionNode lambdaFunction() {
		return (ExpressionNode) this.child(2);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("lambda");
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.LAMBDA;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		boolean result = lambdaFunction()
				.isSideEffectFree(errorsAreSideEffects);

		if (restriction() != null)
			result &= restriction().isSideEffectFree(errorsAreSideEffects);
		return result;
	}

	@Override
	protected DifferenceObject diffWork(ASTNode that) {
		// if (that instanceof QuantifiedExpressionNode) {
		// QuantifiedExpressionNode thatQuan = (QuantifiedExpressionNode) that;
		//
		// if (this. == thatQuan.quantifier())
		// return null;
		// else
		// return new DifferenceObject(this, that, DiffKind.OTHER,
		// "different quantifier");
		// }
		return new DifferenceObject(this, that);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 3)
			throw new ASTException(
					"CommonLambdaNode has three children, but saw index "
							+ index);
		if (index == 0
				&& !(child == null || child instanceof VariableDeclarationNode))
			throw new ASTException("Child of CommonLambdaNode at index " + index
					+ " must be a VariableDeclarationNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index > 0 && index < 3
				&& !(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonLambdaNode at index " + index
					+ " must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
