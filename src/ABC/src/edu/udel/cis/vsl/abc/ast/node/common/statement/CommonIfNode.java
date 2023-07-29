package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonIfNode extends CommonStatementNode implements IfNode {

	public CommonIfNode(Source source, ExpressionNode condition,
			StatementNode trueBranch) {
		super(source, condition, trueBranch);
	}

	public CommonIfNode(Source source, ExpressionNode condition,
			StatementNode trueBranch, StatementNode falseBranch) {
		super(source, condition, trueBranch, falseBranch);
	}

	@Override
	public ExpressionNode getCondition() {
		return (ExpressionNode) child(0);
	}

	@Override
	public StatementNode getTrueBranch() {
		return (StatementNode) child(1);
	}

	@Override
	public StatementNode getFalseBranch() {
		if (numChildren() < 3)
			return null;
		else
			return (StatementNode) child(2);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("IfStatement");
	}

	@Override
	public IfNode copy() {
		StatementNode falseBranch = getFalseBranch();

		if (falseBranch == null)
			return new CommonIfNode(getSource(), duplicate(getCondition()),
					duplicate(getTrueBranch()));
		else
			return new CommonIfNode(getSource(), duplicate(getCondition()),
					duplicate(getTrueBranch()), duplicate(falseBranch));
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 3)
			throw new ASTException(
					"CommonIfNode has at most three children, but saw index "
							+ index);
		switch (index) {
			case 0 :
				if (!(child == null || child instanceof ExpressionNode))
					throw new ASTException("Child of CommonIfNode at index "
							+ index + " must be a ExpressionNode, but saw "
							+ child + " with type " + child.nodeKind());
				break;
			case 1 :
				if (!(child == null || child instanceof StatementNode))
					throw new ASTException("Child of CommonIfNode at index "
							+ index + "  must be a StatementNode, but saw "
							+ child + " with type " + child.nodeKind());
				break;
			case 2 :
				if (!(child == null || child instanceof StatementNode))
					throw new ASTException("Child of CommonIfNode at index "
							+ index + "  must be a StatementNode, but saw "
							+ child + " with type " + child.nodeKind());
				break;
			default :
		}
		return super.setChild(index, child);
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.IF;
	}
}
