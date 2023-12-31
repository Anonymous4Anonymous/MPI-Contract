package edu.udel.cis.vsl.abc.ast.node.common.declaration;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FieldDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonFieldDeclarationNode extends CommonDeclarationNode
		implements
			FieldDeclarationNode {

	public CommonFieldDeclarationNode(Source source, IdentifierNode identifier,
			TypeNode type) {
		super(source, identifier, type);
	}

	public CommonFieldDeclarationNode(Source source, IdentifierNode identifier,
			TypeNode type, ExpressionNode width) {
		super(source, identifier, type, width);
	}

	@Override
	public TypeNode getTypeNode() {
		return (TypeNode) child(1);
	}

	@Override
	public void setTypeNode(TypeNode typeNode) {
		setChild(1, typeNode);
	}

	@Override
	public ExpressionNode getBitFieldWidth() {
		if (numChildren() > 2)
			return (ExpressionNode) child(2);
		return null;
	}

	@Override
	public void setBitFieldWidth(ExpressionNode width) {
		setChild(2, width);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("FieldDeclaration");
	}

	@Override
	public Field getEntity() {
		return (Field) super.getEntity();
	}

	@Override
	public FieldDeclarationNode copy() {
		IdentifierNode identifierCopy = duplicate(getIdentifier());
		TypeNode typeCopy = duplicate(getTypeNode());
		ExpressionNode width = duplicate(getBitFieldWidth());

		if (width == null)
			return new CommonFieldDeclarationNode(getSource(), identifierCopy,
					typeCopy);
		else
			return new CommonFieldDeclarationNode(getSource(), identifierCopy,
					typeCopy, width);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 3)
			throw new ASTException(
					"CommonFieldDeclarationNode has at most three children, but saw index "
							+ index);
		if (index == 1 && !(child == null || child instanceof TypeNode))
			throw new ASTException(
					"Child of CommonFieldDeclarationNode at index " + index
							+ " must be a TypeNode, but saw " + child
							+ " with type " + child.nodeKind());
		if (index == 2 && !(child == null || child instanceof ExpressionNode))
			throw new ASTException(
					"Child of CommonFieldDeclarationNode at index " + index
							+ " must be an ExpressionNode, but saw " + child
							+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.FIELD_DECLARATION;
	}

}
