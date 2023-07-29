package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompositeEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonCompositeEventNode extends CommonDependsEventNode
		implements
			CompositeEventNode {

	private EventOperator operator;

	public CommonCompositeEventNode(Source source, EventOperator op,
			DependsEventNode left, DependsEventNode right) {
		super(source, left, right);
		this.operator = op;
	}

	@Override
	public DependsEventNodeKind getEventKind() {
		return DependsEventNodeKind.COMPOSITE;
	}

	@Override
	public CompositeEventNode copy() {
		return new CommonCompositeEventNode(getSource(), this.operator,
				duplicate(getLeft()), duplicate(getRight()));
	}

	@Override
	public DependsEventNode getLeft() {
		return (DependsEventNode) this.child(0);
	}

	@Override
	public DependsEventNode getRight() {
		return (DependsEventNode) this.child(1);
	}

	@Override
	public EventOperator eventOperator() {
		return this.operator;
	}

	@Override
	protected void printBody(PrintStream out) {
		out.println("OperatorEvent");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonCompositeEventNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof DependsEventNode))
			throw new ASTException("Child of CommonCompositeEventNode at index "
					+ index + " must be a DependsEventNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
