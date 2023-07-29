package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.WhenNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonWhenNode extends CommonStatementNode implements WhenNode {

	public CommonWhenNode(Source source, ExpressionNode guard,
			StatementNode body) {
		super(source, guard, body);
	}

	@Override
	public ExpressionNode getGuard() {
		return (ExpressionNode) child(0);
	}

	@Override
	public StatementNode getBody() {
		return (StatementNode) child(1);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("When");
	}

	@Override
	public WhenNode copy() {
		return new CommonWhenNode(getSource(), duplicate(getGuard()),
				duplicate(getBody()));
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.WHEN;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonWhenNode has only two children, but saw index "
							+ index);
		if (index == 0 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonWhenNode at index " + index
					+ "  must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index == 1 && !(child == null || child instanceof StatementNode)) {
			throw new ASTException("Child of CommonWhenNode at index " + index
					+ "  must be a StatementNode, but saw " + child
					+ " with type " + child.nodeKind());
		}
		return super.setChild(index, child);
	}
}
