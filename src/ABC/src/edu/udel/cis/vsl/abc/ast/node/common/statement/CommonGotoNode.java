package edu.udel.cis.vsl.abc.ast.node.common.statement;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.GotoNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonGotoNode extends CommonJumpNode implements GotoNode {

	public CommonGotoNode(Source source, IdentifierNode label) {
		super(source, JumpKind.GOTO);
		addChild(label);
	}

	@Override
	public IdentifierNode getLabel() {
		return (IdentifierNode) child(0);
	}

	@Override
	public GotoNode copy() {
		return new CommonGotoNode(getSource(), duplicate(getLabel()));
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.STATEMENT;
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.JUMP;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonGotoNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof IdentifierNode))
			throw new ASTException(
					"Child of CommonGotoNode must be a IdentifierNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
