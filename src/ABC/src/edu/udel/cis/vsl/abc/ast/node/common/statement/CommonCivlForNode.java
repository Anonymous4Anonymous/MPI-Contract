package edu.udel.cis.vsl.abc.ast.node.common.statement;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject.DiffKind;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CivlForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonCivlForNode extends CommonStatementNode
		implements
			CivlForNode {

	private boolean isParallel;

	public CommonCivlForNode(Source source, boolean isParallel,
			DeclarationListNode variables, ExpressionNode domain,
			StatementNode body, SequenceNode<ContractNode> invariant) {
		super(source, variables, domain, body);
		addChild(invariant);
		this.isParallel = isParallel;
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.CIVL_FOR;
	}

	@Override
	public boolean isParallel() {
		return isParallel;
	}

	@Override
	public ExpressionNode getDomain() {
		return (ExpressionNode) child(1);
	}

	@Override
	public StatementNode getBody() {
		return (StatementNode) child(2);
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<ContractNode> loopContracts() {
		return (SequenceNode<ContractNode>) child(3);
	}

	@Override
	public DeclarationListNode getVariables() {
		return (DeclarationListNode) child(0);
	}

	@Override
	public CivlForNode copy() {
		return new CommonCivlForNode(getSource(), isParallel,
				duplicate(getVariables()), duplicate(getDomain()),
				duplicate(getBody()), duplicate(loopContracts()));
	}

	@Override
	protected void printBody(PrintStream out) {
		if (isParallel)
			out.print("$parfor");
		else
			out.print("$for");
	}

	@Override
	protected DifferenceObject diffWork(ASTNode that) {
		if (that instanceof CivlForNode)
			if (this.isParallel == ((CivlForNode) that).isParallel())
				return null;
			else
				return new DifferenceObject(this, that, DiffKind.OTHER,
						"different parallel specifier");
		return new DifferenceObject(this, that);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 4)
			throw new ASTException(
					"CommonCivlForNode has four children, but saw index "
							+ index);
		switch (index) {
			case 0 :
				if (!(child == null || child instanceof DeclarationListNode))
					throw new ASTException(
							"Child of CommonCivlForNode at index " + index
									+ " must be a DeclarationListNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
			case 1 :
				if (!(child == null || child instanceof ExpressionNode))
					throw new ASTException(
							"Child of CommonCivlForNode at index " + index
									+ " must be an ExpressionNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
			case 2 :
				if (!(child == null || child instanceof StatementNode))
					throw new ASTException(
							"Child of CommonCivlForNode at index " + index
									+ " must be an StatementNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
			case 3 :
				if (!(child == null || child instanceof SequenceNode))
					throw new ASTException(
							"Child of CommonCivlForNode at index " + index
									+ " must be an SequenceNode, but saw "
									+ child + " with type " + child.nodeKind());
				break;
		}
		return super.setChild(index, child);
	}

}
