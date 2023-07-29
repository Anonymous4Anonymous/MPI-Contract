package edu.udel.cis.vsl.abc.ast.node.common.type;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject.DiffKind;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonFunctionTypeNode extends CommonTypeNode
		implements
			FunctionTypeNode {

	private boolean hasIdentifierList;

	private boolean hasVariableArgs = false;

	public CommonFunctionTypeNode(Source source, TypeNode returnType,
			SequenceNode<VariableDeclarationNode> formals,
			boolean hasIdentifierList) {
		super(source, TypeNodeKind.FUNCTION, returnType, formals);
		this.hasIdentifierList = hasIdentifierList;
	}

	@Override
	public boolean hasIdentifierList() {
		return hasIdentifierList;
	}

	@Override
	public TypeNode getReturnType() {
		return (TypeNode) child(0);
	}

	@Override
	public void setReturnType(TypeNode type) {
		setChild(0, type);
	}

	@Override
	public boolean hasVariableArgs() {
		return hasVariableArgs;
	}

	@Override
	public void setVariableArgs(boolean value) {
		this.hasVariableArgs = value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<VariableDeclarationNode> getParameters() {
		return (SequenceNode<VariableDeclarationNode>) child(1);
	}

	@Override
	public void setParameters(
			SequenceNode<VariableDeclarationNode> parameters) {
		setChild(1, parameters);
	}

	@Override
	protected void printBody(PrintStream out) {
		String qualifiers = qualifierString();

		out.print("FunctionType[");
		if (hasIdentifierList) {
			out.print("identifierList");
		} else {
			out.print("prototypeForm");
		}
		if (hasVariableArgs) {
			out.print(", variableArgs");
		}
		if (!qualifiers.isEmpty())
			out.print(", " + qualifierString());
		out.print("]");
	}

	@Override
	public FunctionTypeNode copy() {
		CommonFunctionTypeNode result = new CommonFunctionTypeNode(getSource(),
				duplicate(getReturnType()), duplicate(getParameters()),
				this.hasIdentifierList());

		copyData(result);
		result.setVariableArgs(this.hasVariableArgs());
		return result;
	}

	@Override
	protected DifferenceObject diffWork(ASTNode that) {
		if (that instanceof FunctionTypeNode)
			if (this.hasVariableArgs == ((FunctionTypeNode) that)
					.hasVariableArgs())
				return null;
			else
				return new DifferenceObject(this, that, DiffKind.OTHER,
						"different specifier for variable number of arguments");
		return new DifferenceObject(this, that);
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index >= 2)
			throw new ASTException(
					"CommonFunctionTypeNode has two children, but saw index "
							+ index);
		if (index == 0 && !(child == null || child instanceof TypeNode))
			throw new ASTException("Child of CommonFunctionTypeNode at index "
					+ index + " must be a TypeNode, but saw " + child
					+ " with type " + child.nodeKind());
		if (index == 1 && !(child == null || child instanceof SequenceNode))
			throw new ASTException("Child of CommonFunctionTypeNode at index "
					+ index + " must be a SequenceNode, but saw " + child
					+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
