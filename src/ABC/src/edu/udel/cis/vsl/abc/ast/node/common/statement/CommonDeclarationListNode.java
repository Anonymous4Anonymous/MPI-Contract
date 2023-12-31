package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.util.List;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.common.CommonSequenceNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonDeclarationListNode
		extends
			CommonSequenceNode<VariableDeclarationNode>
		implements
			DeclarationListNode {

	public CommonDeclarationListNode(Source source,
			List<VariableDeclarationNode> childList) {
		super(source, "DeclarationList", childList);
	}

	@Override
	public CommonDeclarationListNode copy() {
		return new CommonDeclarationListNode(getSource(), childListCopy());
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.DECLARATION_LIST;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (!(child == null || child instanceof VariableDeclarationNode))
			throw new ASTException(
					"Child of CommonDeclarationListNode must be a VariableDeclarationNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
