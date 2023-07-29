package edu.udel.cis.vsl.abc.ast.node.common.type;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.AtomicTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonAtomicTypeNode extends CommonTypeNode
		implements
			AtomicTypeNode {

	public CommonAtomicTypeNode(Source source, TypeNode baseType) {
		super(source, TypeNodeKind.ATOMIC, baseType);
	}

	@Override
	public TypeNode getBaseType() {
		return (TypeNode) child(0);
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("Atomic[" + qualifierString() + "]");
	}

	@Override
	public AtomicTypeNode copy() {
		CommonAtomicTypeNode result = new CommonAtomicTypeNode(getSource(),
				duplicate(getBaseType()));

		copyData(result);
		return result;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index != 0)
			throw new ASTException(
					"CommonAtomicTypeNode has 1 child, but saw index " + index);
		if (!(child == null || child instanceof TypeNode))
			throw new ASTException("Child of CommonAtomicTypeNode at index "
					+ index + " must be a TypeNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
