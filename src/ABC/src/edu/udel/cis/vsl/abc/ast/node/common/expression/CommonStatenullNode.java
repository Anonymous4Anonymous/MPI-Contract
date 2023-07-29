package edu.udel.cis.vsl.abc.ast.node.common.expression;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.StatenullNode;
import edu.udel.cis.vsl.abc.ast.type.IF.ObjectType;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonStatenullNode extends CommonConstantNode
		implements
			StatenullNode {

	public CommonStatenullNode(Source source, ObjectType processType) {
		super(source, "$state_null", processType);
	}

	@Override
	public ObjectType getInitialType() {
		return (ObjectType) super.getInitialType();
	}

	@Override
	public ConstantKind constantKind() {
		return ConstantKind.STATENULL;
	}

	@Override
	public StatenullNode copy() {
		return new CommonStatenullNode(getSource(),
				(ObjectType) getInitialType());
	}

	@Override
	public boolean isSideEffectFree(boolean errorsAreSideEffects) {
		return true;
	}

	@Override
	protected void printBody(PrintStream out) {
		out.print("$state_null");
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		throw new ASTException(
				"CommonStatenullNode has no child, but saw index " + index);
	}
}
