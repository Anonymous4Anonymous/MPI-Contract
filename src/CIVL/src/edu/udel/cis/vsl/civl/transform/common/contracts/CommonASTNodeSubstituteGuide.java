package edu.udel.cis.vsl.civl.transform.common.contracts;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.civl.transform.SubstituteGuide;

public class CommonASTNodeSubstituteGuide extends SubstituteGuide {

	public CommonASTNodeSubstituteGuide(ExpressionNode newNode,
			ExpressionNode oldNode) {
		super(new ASTNode[]{newNode}, oldNode);
	}

	@Override
	protected ASTNode buildNewNode(NodeFactory nf) {
		assert super.newNodeComponents.length == 1;
		return super.newNodeComponents[0];
	}
}
