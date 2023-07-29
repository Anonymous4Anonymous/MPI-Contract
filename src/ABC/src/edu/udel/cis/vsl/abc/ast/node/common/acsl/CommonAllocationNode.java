package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AllocationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonAllocationNode extends CommonContractNode
		implements
			AllocationNode {

	/**
	 * True if this is an allocates clause; otherwise, this is a frees clause.
	 */
	private boolean isAllocates;

	public CommonAllocationNode(Source source, boolean isAllocates,
			SequenceNode<ExpressionNode> memoryList) {
		super(source, memoryList);
		this.isAllocates = isAllocates;
	}

	@Override
	public ContractKind contractKind() {
		return ContractKind.ALLOCATES_OR_FREES;
	}

	@Override
	public AllocationNode copy() {
		return new CommonAllocationNode(this.getSource(), this.isAllocates,
				duplicate(this.memoryList()));
	}

	@Override
	public boolean isAllocates() {
		return this.isAllocates;
	}

	@Override
	public boolean isFrees() {
		return !this.isAllocates;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<ExpressionNode> memoryList() {
		return (SequenceNode<ExpressionNode>) this.child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		if (this.isAllocates)
			out.println("AllocatesNode");
		else
			out.println("FreesNode");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonAllocationNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof SequenceNode))
			throw new ASTException(
					"Child of CommonAllocationNode must be a SequenceNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
