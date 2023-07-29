package edu.udel.cis.vsl.civl.transform.common.contracts.translators;

import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

public class TargetWaitsforTranslator extends ContractTranslatorCommonBase {

	TargetWaitsforTranslator(NodeFactory nodeFactory,
			TokenFactory tokenFactory) {
		super(nodeFactory, tokenFactory);
	}

	public List<BlockItemNode> translateWaitsforExpressions(
			Iterable<ExpressionNode> waitsforArgs) {
		List<BlockItemNode> results = new LinkedList<>();

		// TODO: impl me
		return results;
	}
}
