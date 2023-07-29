package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIUsesNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonMPIUsesNode extends CommonContractNode implements MPIUsesNode {

	public CommonMPIUsesNode(Source source, SequenceNode<ExpressionNode> child) {
		super(source, child);
	}

	@Override
	public ContractKind contractKind() {
		return ContractKind.MPI_USES;
	}

	@Override
	public ContractNode copy() {
		return new CommonMPIUsesNode(getSource(), getArguments().copy());
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<ExpressionNode> getArguments() {
		return (SequenceNode<ExpressionNode>) child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("MPIUsesNode");
	}
}
