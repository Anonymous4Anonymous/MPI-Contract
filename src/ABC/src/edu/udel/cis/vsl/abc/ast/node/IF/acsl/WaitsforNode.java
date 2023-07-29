package edu.udel.cis.vsl.abc.ast.node.IF.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;

public interface WaitsforNode extends ContractNode {
	/**
	 * Returns all arguments of the "waitsfor" clause as a {@link SequenceNode}
	 * 
	 * @return
	 */
	SequenceNode<ExpressionNode> getArguments();

	@Override
	WaitsforNode copy();
}
