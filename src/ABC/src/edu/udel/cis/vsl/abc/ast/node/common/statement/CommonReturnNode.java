package edu.udel.cis.vsl.abc.ast.node.common.statement;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ReturnNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonReturnNode extends CommonJumpNode implements ReturnNode {

	public CommonReturnNode(Source source, ExpressionNode expression) {
		super(source, JumpKind.RETURN);
		addChild(expression);
	}

	@Override
	public ExpressionNode getExpression() {
		return (ExpressionNode) child(0);
	}

	@Override
	public ReturnNode copy() {
		return new CommonReturnNode(getSource(), duplicate(getExpression()));
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.JUMP;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonReturnNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonReturnNode must be a ExpressionNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
