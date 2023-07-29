package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.StatementExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonStatementExpressionNode extends CommonExpressionNode
		implements
			StatementExpressionNode {

	private ExpressionNode expression;

	public CommonStatementExpressionNode(Source source,
			CompoundStatementNode statement) {
		super(source, statement);
		assert statement.getSequenceChild(
				statement.numChildren() - 1) instanceof ExpressionStatementNode;
		expression = ((ExpressionStatementNode) statement
				.getSequenceChild(statement.numChildren() - 1)).getExpression();
	}

	@Override
	public ExpressionNode copy() {
		return new CommonStatementExpressionNode(this.getSource(),
				duplicate(this.getCompoundStatement()));
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.STATEMENT_EXPRESSION;
	}

	@Override
	public boolean isConstantExpression() {
		return false;
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return false;
	}

	@Override
	public CompoundStatementNode getCompoundStatement() {
		return (CompoundStatementNode) this.child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("StatementExpressionNode");
	}

	@Override
	public ExpressionNode getExpression() {
		return this.expression;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonStatementExpressionNode has one child, but saw index "
							+ index);
		if (!(child == null || child instanceof SizeableNode))
			throw new ASTException(
					"Child of CommonStatementExpressionNode at index " + index
							+ " must be a CompoundStatementNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
