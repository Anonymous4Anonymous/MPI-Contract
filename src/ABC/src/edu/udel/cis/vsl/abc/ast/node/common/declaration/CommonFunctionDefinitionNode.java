package edu.udel.cis.vsl.abc.ast.node.common.declaration;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ReturnNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonFunctionDefinitionNode extends CommonFunctionDeclarationNode
		implements
			FunctionDefinitionNode {

	public CommonFunctionDefinitionNode(Source source,
			IdentifierNode identifier, FunctionTypeNode type,
			SequenceNode<ContractNode> contract,
			CompoundStatementNode statement) {
		super(source, identifier, type, contract);
		addChild(statement);
	}

	@Override
	public CompoundStatementNode getBody() {
		return (CompoundStatementNode) child(3);
	}

	@Override
	public void setBody(CompoundStatementNode statement) {
		setChild(3, statement);
	}

	@Override
	protected void printKind(PrintStream out) {
		out.print("FunctionDefinition");
	}

	@Override
	public FunctionDefinitionNode copy() {
		CommonFunctionDefinitionNode result = new CommonFunctionDefinitionNode(
				getSource(), duplicate(getIdentifier()),
				duplicate(getTypeNode()), duplicate(getContract()),
				duplicate(getBody()));

		result.setInlineFunctionSpecifier(hasInlineFunctionSpecifier());
		result.setNoreturnFunctionSpecifier(hasNoreturnFunctionSpecifier());
		result.setGlobalFunctionSpecifier(hasGlobalFunctionSpecifier());
		copyStorage(result);
		return result;
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.FUNCTION_DEFINITION;
	}

	@Override
	public OrdinaryDeclarationKind ordinaryDeclarationKind() {
		return OrdinaryDeclarationKind.FUNCTION_DEFINITION;
	}

	@Override
	public FunctionTypeNode getTypeNode() {
		return (FunctionTypeNode) super.getTypeNode();
	}

	@Override
	public ExpressionNode getLogicDefinition() {
		if (!isLogicFunction())
			return null;
		return ((ReturnNode) getBody().getSequenceChild(0)).getExpression();
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 4)
			throw new ASTException(
					"CommonFunctionDefinitionNode has only four children, but saw index "
							+ index);
		if (index == 3
				&& !(child == null || child instanceof CompoundStatementNode))
			throw new ASTException(
					"Child of CommonFunctionDefinitionNode at index " + index
							+ " must be an CompoundStatementNode, but saw "
							+ child + " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
