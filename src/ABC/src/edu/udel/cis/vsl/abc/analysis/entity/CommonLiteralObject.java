package edu.udel.cis.vsl.abc.analysis.entity;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.LiteralObject;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonLiteralObject implements LiteralObject {

	private LiteralTypeNode typeNode;

	private ASTNode sourceNode;

	public CommonLiteralObject(LiteralTypeNode typeNode, ASTNode sourceNode) {
		this.typeNode = typeNode;
		this.sourceNode = sourceNode;
	}

	public LiteralTypeNode getTypeNode() {
		return typeNode;
	}

	public ASTNode getSourceNode() {
		return sourceNode;
	}

	@Override
	public Type getType() {
		return typeNode.getType();
	}

}
