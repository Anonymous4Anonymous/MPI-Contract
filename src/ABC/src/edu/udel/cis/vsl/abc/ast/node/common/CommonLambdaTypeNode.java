package edu.udel.cis.vsl.abc.ast.node.common;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.LambdaTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.node.common.type.CommonTypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonLambdaTypeNode extends CommonTypeNode
		implements
			LambdaTypeNode {

	public CommonLambdaTypeNode(Source source, TypeNode freeVariableType,
			TypeNode functionType) {
		super(source, TypeNodeKind.LAMBDA, freeVariableType, functionType);
	}

	@Override
	public TypeNode copy() {
		return new CommonLambdaTypeNode(getSource(),
				duplicate(freeVariableType()), duplicate(lambdaFunctionType()));
	}

	@Override
	public TypeNode freeVariableType() {
		return (TypeNode) this.child(0);
	}

	@Override
	public TypeNode lambdaFunctionType() {
		return (TypeNode) this.child(1);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("$lambda_t");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonLambdaTypeNode has two children, but saw index "
							+ index);
		if (!(child == null || child instanceof TypeNode))
			throw new ASTException("Child of CommonLambdaTypeNode at index "
					+ index + " must be a TypeNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
