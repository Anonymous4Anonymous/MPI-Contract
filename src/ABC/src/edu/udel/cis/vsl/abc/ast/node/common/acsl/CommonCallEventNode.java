package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CallEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonCallEventNode extends CommonDependsEventNode
		implements
			CallEventNode {

	public CommonCallEventNode(Source source, IdentifierExpressionNode function,
			SequenceNode<ExpressionNode> arguments) {
		super(source, function, arguments);
	}

	@Override
	public DependsEventNodeKind getEventKind() {
		return DependsEventNodeKind.CALL;
	}

	@Override
	public CallEventNode copy() {
		return new CommonCallEventNode(getSource(), duplicate(getFunction()),
				duplicate(arguments()));
	}

	@Override
	public void printBody(PrintStream out) {
		out.print("\\call");
	}

	@Override
	public IdentifierExpressionNode getFunction() {
		return (IdentifierExpressionNode) this.child(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<ExpressionNode> arguments() {
		return (SequenceNode<ExpressionNode>) this.child(1);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonCallEventNode has two children, but saw index "
							+ index);
		if (index == 0 && !(child == null
				|| child instanceof IdentifierExpressionNode))
			throw new ASTException("Child of CommonCallEventNode at index "
					+ index + " must be a IdentifierExpressionNode, but saw "
					+ child + " with type " + child.nodeKind());
		if (index == 1 && !(child == null || child instanceof SequenceNode))
			throw new ASTException("Child of CommonCallEventNode at index "
					+ index + " must be a SequenceNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
