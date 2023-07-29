package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;
import java.util.Arrays;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ArrayLambdaNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonArrayLambdaNode extends CommonExpressionNode
		implements
			ArrayLambdaNode {

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
	public CommonArrayLambdaNode(Source source, TypeNode type,
			SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> variableList,
			ExpressionNode restriction, ExpressionNode expression) {
		super(source,
				Arrays.asList(type, variableList, restriction, expression));
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public ExpressionNode copy() {
		return new CommonArrayLambdaNode(this.getSource(), duplicate(type()),
				duplicate(boundVariableList()), duplicate(restriction()),
				duplicate(expression()));
	}

	@Override
	public TypeNode type() {
		return (TypeNode) this.child(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVariableList() {
		return (SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>>) this
				.child(1);
	}

	@Override
	public ExpressionNode restriction() {
		return (ExpressionNode) this.child(2);
	}

	@Override
	public ExpressionNode expression() {
		return (ExpressionNode) this.child(3);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("array_lambda");
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.ARRAY_LAMBDA;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		boolean result = expression().isSideEffectFree(errorsAreSideEffects);

		if (restriction() != null)
			result = result && this.restriction()
					.isSideEffectFree(errorsAreSideEffects);
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
		if (index >= 4)
			throw new ASTException(
					"CommonArrayLambdaNode has four children, but saw index "
							+ index);
		switch (index) {
			case 0 :
				if (!(child == null || child instanceof TypeNode))
					throw new ASTException(
							"Child of CommonArrayLambdaNode at index " + index
									+ " must be a TypeNode, but saw " + child
									+ " with type " + child.nodeKind());
				break;
			case 1 :
				if (!(child == null || child instanceof SequenceNode))
					throw new ASTException(
							"Child of CommonArrayLambdaNode at index " + index
									+ " must be a SequenceNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
			case 2 :
				if (!(child == null || child instanceof ExpressionNode))
					throw new ASTException(
							"Child of CommonArrayLambdaNode at index " + index
									+ " must be a ExpressionNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
			case 3 :
				if (!(child == null || child instanceof ExpressionNode))
					throw new ASTException(
							"Child of CommonArrayLambdaNode at index " + index
									+ " must be a ExpressionNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
			default :
				break;
		}
		return super.setChild(index, child);
	}
}
