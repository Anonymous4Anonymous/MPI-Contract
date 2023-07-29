package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonExpressionStatementNode extends CommonStatementNode
		implements
			ExpressionStatementNode {

	public CommonExpressionStatementNode(Source source,
			ExpressionNode expression) {
		super(source, expression);
	}

	@Override
	public ExpressionNode getExpression() {
		return (ExpressionNode) child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("ExpressionStatement");
	}

	@Override
	public ExpressionStatementNode copy() {
		return new CommonExpressionStatementNode(getSource(),
				duplicate(getExpression()));
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.EXPRESSION;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonExpressionStatementNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonExpressionStatementNode must be an ExpressionNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}

}
