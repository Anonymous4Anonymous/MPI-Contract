package edu.udel.cis.vsl.abc.ast.node.common;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.StaticAssertionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.StringLiteralNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonStaticAssertionNode extends CommonASTNode
		implements
			StaticAssertionNode {

	public CommonStaticAssertionNode(Source source, ExpressionNode expression,
			StringLiteralNode message) {
		super(source, expression, message);
	}

	@Override
	public ExpressionNode getExpression() {
		return (ExpressionNode) child(0);
	}

	@Override
	public StringLiteralNode getMessage() {
		return (StringLiteralNode) child(1);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("StaticAssertion");
	}

	@Override
	public StaticAssertionNode copy() {
		return new CommonStaticAssertionNode(getSource(),
				duplicate(getExpression()), duplicate(getMessage()));
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.STATIC_ASSERTION;
	}

	@Override
	public BlockItemKind blockItemKind() {
		return BlockItemKind.STATIC_ASSERTION;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonStaticAssertionNode has only two children, but saw index "
							+ index);
		if (index == 0 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonStaticAssertionNode at index " + index
							+ " must be a ExpressionNode, but saw " + child
							+ " with type " + child.nodeKind());
		if (index == 1
				&& !(child == null || child instanceof StringLiteralNode))
			throw new ASTException(
					"Child of CommonStaticAssertionNode at index " + index
							+ " must be a StringLiteralNode, but saw " + child
							+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
