package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.EnsuresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonEnsuresNode extends CommonContractNode
		implements
			EnsuresNode {

	private boolean isGuarantee = false;

	public CommonEnsuresNode(Source source, ExpressionNode expression) {
		super(source, expression);
	}

	@Override
	public ExpressionNode getExpression() {
		return (ExpressionNode) child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("Ensures");
	}

	@Override
	public EnsuresNode copy() {
		return new CommonEnsuresNode(getSource(), duplicate(getExpression()));
	}

	@Override
	public boolean isGuarantee() {
		return isGuarantee;
	}

	@Override
	public void setIsGuarantee(boolean isGuarantee) {
		this.isGuarantee = isGuarantee;
	}

	@Override
	public ContractKind contractKind() {
		return ContractKind.ENSURES;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonEnsuresNode has only one child, but saw index "
							+ index);
		if (!(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonEnsuresNode must be an ExpressionNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
