package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.UpdateNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonUpdateNode extends CommonStatementNode
		implements
			UpdateNode {

	public CommonUpdateNode(Source source, ExpressionNode collator,
			FunctionCallNode call) {
		super(source, collator, call);
	}

	@Override
	public UpdateNode copy() {
		return new CommonUpdateNode(getSource(),
				(ExpressionNode) duplicate(child(0)),
				(FunctionCallNode) duplicate(child(1)));
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.UPDATE;
	}

	@Override
	public FunctionCallNode getFunctionCall() {
		return (FunctionCallNode) child(1);
	}

	@Override
	public ExpressionNode getCollator() {
		return (ExpressionNode) child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("UpdateNode");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonUpdateNode has only two children, but saw index "
							+ index);
		if (index == 0 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException("Child of CommonUpdateNode at index " + index
					+ "  must be a ExpressionNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index == 1
				&& !(child == null || child instanceof FunctionCallNode)) {
			throw new ASTException("Child of CommonUpdateNode  at index "
					+ index + " must be a FunctionCallNode, but saw " + child
					+ " with type " + child.nodeKind());
		}
		return super.setChild(index, child);
	}
}
