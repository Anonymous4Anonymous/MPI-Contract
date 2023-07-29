package edu.udel.cis.vsl.abc.ast.node.common.type;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

/**
 * Type node representing the type "$range".
 * 
 * @author xxxx
 * 
 */
public class CommonRangeTypeNode extends CommonTypeNode {

	public CommonRangeTypeNode(Source source) {
		super(source, TypeNodeKind.RANGE);
	}

	@Override
	public TypeNode copy() {
		CommonRangeTypeNode result = new CommonRangeTypeNode(getSource());

		copyData(result);
		return result;
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("$range");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		throw new ASTException(
				"CommonRangeTypeNode has no child, but saw index " + index);
	}
}
