package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.DotNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonDotNode extends CommonExpressionNode implements DotNode {

	private Field[] navigationSequence = null;

	public CommonDotNode(Source source, ExpressionNode structure,
			IdentifierNode fieldName) {
		super(source, structure, fieldName);
	}

	@Override
	public ExpressionNode getStructure() {
		return (ExpressionNode) child(0);
	}

	@Override
	public void setStructure(ExpressionNode structure) {
		setChild(0, structure);
	}

	@Override
	public IdentifierNode getFieldName() {
		return (IdentifierNode) child(1);
	}

	@Override
	public void setFieldName(IdentifierNode field) {
		setChild(1, field);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("DotNode");
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public DotNode copy() {
		return new CommonDotNode(getSource(), duplicate(getStructure()),
				duplicate(getFieldName()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.DOT;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return getStructure().isSideEffectFree(errorsAreSideEffects);
	}

	@Override
	public Field[] getNavigationSequence() {
		return navigationSequence;
	}

	@Override
	public void setNavigationSequence(Field[] sequence) {
		this.navigationSequence = sequence;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonDotNode has two children, but saw index " + index);
		if (index == 0 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonDotNode at index " + index
					+ " must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index == 1 && !(child == null || child instanceof IdentifierNode))
			throw new ASTException("Child of CommonDotNode at index " + index
					+ " must be a IdentifierNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
