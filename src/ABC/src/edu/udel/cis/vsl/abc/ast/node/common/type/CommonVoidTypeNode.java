package edu.udel.cis.vsl.abc.ast.node.common.type;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonVoidTypeNode extends CommonTypeNode {

	public CommonVoidTypeNode(Source source) {
		super(source, TypeNodeKind.VOID);
	}

	@Override
	protected void printBody(PrintStream out) {
		String qualifiers = qualifierString();

		out.print("VoidType");
		if (!qualifiers.isEmpty())
			out.print("[" + qualifiers + "]");
	}

	@Override
	public TypeNode copy() {
		CommonVoidTypeNode result = new CommonVoidTypeNode(getSource());

		copyData(result);
		return result;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		throw new ASTException(
				"CommonVoidTypeNode has no child, but saw index " + index);
	}
}
