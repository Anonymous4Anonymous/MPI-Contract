package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonDependsNode extends CommonContractNode
		implements
			DependsNode {

	public CommonDependsNode(Source source,
			SequenceNode<DependsEventNode> eventList) {
		super(source, (ASTNode) eventList);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("Depends");
	}

	@Override
	public DependsNode copy() {
		return new CommonDependsNode(getSource(), duplicate(getEventList()));
	}

	@Override
	public ContractKind contractKind() {
		return ContractKind.DEPENDS;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<DependsEventNode> getEventList() {
		return (SequenceNode<DependsEventNode>) child(0);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonDependsNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof SequenceNode))
			throw new ASTException(
					"Child of CommonDependsNode must be a SequenceNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
