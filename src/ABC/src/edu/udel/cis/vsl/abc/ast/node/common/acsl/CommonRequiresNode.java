package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.RequiresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonRequiresNode extends CommonContractNode
		implements
			RequiresNode {

	private boolean isRequirement = false;

	public CommonRequiresNode(Source source, ExpressionNode expression) {
		super(source, expression);
	}

	@Override
	public ExpressionNode getExpression() {
		return (ExpressionNode) child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("RequiresNode");
	}

	@Override
	public RequiresNode copy() {
		return new CommonRequiresNode(getSource(), duplicate(getExpression()));
	}

	@Override
	public boolean isRequirement() {
		return isRequirement;
	}

	@Override
	public void setIsRequirement(boolean isRequirement) {
		this.isRequirement = isRequirement;
	}

	@Override
	public ContractKind contractKind() {
		return ContractKind.REQUIRES;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonRequiresNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonRequiresNode must be a ExpressionNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
