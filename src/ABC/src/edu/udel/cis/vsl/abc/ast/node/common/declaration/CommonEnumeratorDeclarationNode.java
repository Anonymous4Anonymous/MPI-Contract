package edu.udel.cis.vsl.abc.ast.node.common.declaration;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.EnumeratorDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Enumerator;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonEnumeratorDeclarationNode extends CommonDeclarationNode
		implements
			EnumeratorDeclarationNode {

	public CommonEnumeratorDeclarationNode(Source source,
			IdentifierNode identifier, ExpressionNode value) {
		super(source, identifier, value);
	}

	@Override
	public ExpressionNode getValue() {
		return (ExpressionNode) child(1);
	}

	@Override
	public void setValue(ExpressionNode value) {
		setChild(1, value);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("Enumerator");
	}

	@Override
	public Enumerator getEntity() {
		return (Enumerator) super.getEntity();
	}

	@Override
	public EnumeratorDeclarationNode copy() {
		return new CommonEnumeratorDeclarationNode(getSource(),
				duplicate(getIdentifier()), duplicate(getValue()));
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.ENUMERATOR_DECLARATION;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonEnumeratorDeclarationNode has two children, but saw index "
							+ index);
		if (index == 1 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonEnumeratorDeclarationNode at index " + index
							+ " must be a ExpressionNode, but saw " + child
							+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}

}
