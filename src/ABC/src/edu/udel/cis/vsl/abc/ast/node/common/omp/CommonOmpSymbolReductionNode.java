package edu.udel.cis.vsl.abc.ast.node.common.omp;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject.DiffKind;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSymbolReductionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonOmpSymbolReductionNode extends CommonOmpReductionNode
		implements
			OmpSymbolReductionNode {

	private OmpReductionOperator operator;

	public CommonOmpSymbolReductionNode(Source source,
			OmpReductionOperator operator,
			SequenceNode<IdentifierExpressionNode> variables) {
		super(source);
		this.operator = operator;
		this.addChild(variables);
	}

	@Override
	public OmpReductionNodeKind ompReductionOperatorNodeKind() {
		return OmpReductionNodeKind.OPERATOR;
	}

	@Override
	public ASTNode copy() {
		return new CommonOmpSymbolReductionNode(getSource(), this.operator(),
				duplicate(this.variables()));
	}

	@Override
	public OmpReductionOperator operator() {
		return this.operator;
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("OmpSymbolReductionNode");
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<IdentifierExpressionNode> variables() {
		return (SequenceNode<IdentifierExpressionNode>) this.child(0);
	}

	@Override
	protected DifferenceObject diffWork(ASTNode that) {
		if (that instanceof OmpSymbolReductionNode)
			if (this.operator == ((OmpSymbolReductionNode) that).operator())
				return null;
			else
				return new DifferenceObject(this, that, DiffKind.OTHER,
						"different reduction symbol");
		return new DifferenceObject(this, that);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonOmpSymbolReductionNode has 1 child, but saw index "
							+ index);
		if (!(child == null || child instanceof SequenceNode))
			throw new ASTException(
					"Child of CommonOmpSymbolReductionNode at index " + index
							+ " must be a SequenceNode, but saw " + child
							+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
