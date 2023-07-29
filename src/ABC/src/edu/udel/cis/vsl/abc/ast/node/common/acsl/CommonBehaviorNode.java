package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.BehaviorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonBehaviorNode extends CommonContractNode
		implements
			BehaviorNode {

	public CommonBehaviorNode(Source source, IdentifierNode name,
			SequenceNode<ContractNode> child) {
		super(source, name, (ASTNode) child);
	}

	@Override
	public ContractKind contractKind() {
		return ContractKind.BEHAVIOR;
	}

	@Override
	public IdentifierNode getName() {
		return (IdentifierNode) this.child(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<ContractNode> getBody() {
		return (SequenceNode<ContractNode>) this.child(1);
	}

	@Override
	public BehaviorNode copy() {
		return new CommonBehaviorNode(getSource(), duplicate(getName()),
				duplicate(getBody()));
	}

	@Override
	protected void printBody(PrintStream out) {
		out.println("behavior");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonBehaviorNode has two children, but saw index "
							+ index);
		if (index == 0 && !(child == null || child instanceof IdentifierNode))
			throw new ASTException("Child of CommonBehaviorNode at index "
					+ index + " must be a IdentifierNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index == 1 && !(child == null || child instanceof SequenceNode))
			throw new ASTException("Child of CommonBehaviorNode at index "
					+ index + " must be a SequenceNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
