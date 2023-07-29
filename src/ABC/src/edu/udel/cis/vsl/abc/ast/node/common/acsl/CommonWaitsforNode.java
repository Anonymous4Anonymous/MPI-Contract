package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.WaitsforNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonWaitsforNode extends CommonContractNode
		implements
			WaitsforNode {
	SequenceNode<ExpressionNode> arguments;

	public CommonWaitsforNode(Source source,
			SequenceNode<ExpressionNode> children) {
		super(source, children);
		arguments = children;
	}

	@Override
	public ContractKind contractKind() {
		return ContractKind.WAITSFOR;
	}

	@Override
	public SequenceNode<ExpressionNode> getArguments() {
		return arguments;
	}

	@Override
	public WaitsforNode copy() {
		return new CommonWaitsforNode(getSource(), arguments);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.println("waitsfor " + arguments.prettyRepresentation());
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index == 0)
			throw new ASTException(
					"CommonWaitsforNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof SequenceNode))
			throw new ASTException(
					"Child of CommonWaitsforNode must be a SequenceNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
