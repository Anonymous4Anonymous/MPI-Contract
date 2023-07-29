package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.util.List;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.common.CommonSequenceNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonCompoundStatementNode
		extends
			CommonSequenceNode<BlockItemNode>
		implements
			CompoundStatementNode {

	public CommonCompoundStatementNode(Source source,
			List<BlockItemNode> childList) {
		super(source, "CompoundStatement", childList);
	}

	@Override
	public CompoundStatementNode copy() {
		return new CommonCompoundStatementNode(getSource(), childListCopy());
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.STATEMENT;
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.COMPOUND;
	}

	@Override
	public BlockItemKind blockItemKind() {
		return BlockItemKind.STATEMENT;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (!(child == null || child instanceof BlockItemNode))
			throw new ASTException(
					"Child of CommonCompoundStatementNode must be a BlockItemNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
