package edu.udel.cis.vsl.abc.ast.node.common.declaration;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject;
import edu.udel.cis.vsl.abc.ast.IF.DifferenceObject.DiffKind;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonFunctionDeclarationNode extends CommonOrdinaryDeclarationNode
		implements
			FunctionDeclarationNode {

	private boolean inlineFunctionSpecifier = false;

	private boolean noreturnFunctionSpecifier = false;

	private boolean globalFunctionSpecifier = false;

	private boolean atomicFunctionSpecifier = false;// $atomic_f
	private boolean stateFunctionSpecifier = false;
	private boolean pureFunctionSpecifier = false;
	private boolean systemFunctionSpecifier = false;
	private String systemLibrary = null;

	/**
	 * a flag indicating if the declared function is a logic function
	 */
	private boolean isLogic = false;

	public CommonFunctionDeclarationNode(Source source,
			IdentifierNode identifier, TypeNode type,
			SequenceNode<ContractNode> contract) {
		super(source, identifier, type);
		addChild(contract); // child 2
	}

	@Override
	public Function getEntity() {
		return (Function) super.getEntity();
	}

	@Override
	public boolean hasInlineFunctionSpecifier() {
		return inlineFunctionSpecifier;
	}

	@Override
	public void setInlineFunctionSpecifier(boolean value) {
		this.inlineFunctionSpecifier = value;
	}

	@Override
	public boolean hasNoreturnFunctionSpecifier() {
		return this.noreturnFunctionSpecifier;
	}

	@Override
	public void setNoreturnFunctionSpecifier(boolean value) {
		this.noreturnFunctionSpecifier = value;
	}

	@Override
	public boolean hasGlobalFunctionSpecifier() {
		return this.globalFunctionSpecifier;
	}

	@Override
	public void setGlobalFunctionSpecifier(boolean value) {
		this.globalFunctionSpecifier = value;
	}

	protected void printKind(PrintStream out) {
		out.print("FunctionDeclaration");
	}

	@Override
	protected void printBody(PrintStream out) {
		boolean needSeparator = false;

		printKind(out);
		if (hasExternStorage()) {
			out.print("[");
			out.print("extern");
			needSeparator = true;
		}
		if (hasStaticStorage()) {
			out.print(needSeparator ? ", " : "[");
			out.print("static");
			needSeparator = true;
		}
		if (inlineFunctionSpecifier) {
			out.print(needSeparator ? ", " : "[");
			out.print("inline");
			needSeparator = true;
		}
		if (noreturnFunctionSpecifier) {
			out.print(needSeparator ? ", " : "[");
			out.print("noreturn");
			needSeparator = true;
		}
		if (needSeparator)
			out.print("]");
	}

	@SuppressWarnings("unchecked")
	@Override
	public SequenceNode<ContractNode> getContract() {
		return (SequenceNode<ContractNode>) child(2);
	}

	@Override
	public void setContract(SequenceNode<ContractNode> contract) {
		setChild(2, contract);
	}

	@Override
	public FunctionDeclarationNode copy() {
		CommonFunctionDeclarationNode result = new CommonFunctionDeclarationNode(
				getSource(), duplicate(getIdentifier()),
				duplicate(getTypeNode()), duplicate(getContract()));

		result.setInlineFunctionSpecifier(hasInlineFunctionSpecifier());
		result.setNoreturnFunctionSpecifier(hasNoreturnFunctionSpecifier());
		result.setGlobalFunctionSpecifier(hasGlobalFunctionSpecifier());
		result.setAtomicFunctionSpecifier(this.hasAtomicFunctionSpecifier());
		result.setSystemFunctionSpecifier(this.hasSystemFunctionSpecifier());
		result.setPureFunctionSpecifier(this.hasPureFunctionSpecifier());
		result.setStatefFunctionSpecifier(this.hasStatefFunctionSpecifier());
		copyStorage(result);
		result.setSystemLibrary(this.systemLibrary);
		return result;
	}

	@Override
	public NodeKind nodeKind() {
		return NodeKind.FUNCTION_DECLARATION;
	}

	@Override
	public OrdinaryDeclarationKind ordinaryDeclarationKind() {
		return OrdinaryDeclarationKind.FUNCTION_DECLARATION;
	}

	@Override
	protected DifferenceObject diffWork(ASTNode that) {
		if (that instanceof FunctionDeclarationNode) {
			FunctionDeclarationNode thatFunction = (FunctionDeclarationNode) that;

			if (!(this.inlineFunctionSpecifier == thatFunction
					.hasInlineFunctionSpecifier()
					&& this.noreturnFunctionSpecifier == thatFunction
							.hasNoreturnFunctionSpecifier()))
				return new DifferenceObject(this, that, DiffKind.OTHER,
						"different function inline/noreturn specifier");
			else
				return null;
		}
		return new DifferenceObject(this, that);
	}

	@Override
	public void setStatefFunctionSpecifier(boolean value) {
		this.stateFunctionSpecifier = value;
	}

	@Override
	public void setAtomicFunctionSpecifier(boolean value) {
		this.atomicFunctionSpecifier = value;
	}

	@Override
	public void setSystemFunctionSpecifier(boolean value) {
		this.systemFunctionSpecifier = value;
	}

	@Override
	public boolean hasStatefFunctionSpecifier() {
		return this.stateFunctionSpecifier;
	}

	@Override
	public boolean hasAtomicFunctionSpecifier() {
		return this.atomicFunctionSpecifier;
	}

	@Override
	public boolean hasSystemFunctionSpecifier() {
		return this.systemFunctionSpecifier;
	}

	@Override
	public String getSystemLibrary() {
		return this.systemLibrary;
	}

	@Override
	public void setSystemLibrary(String library) {
		this.systemLibrary = library;
	}

	@Override
	public boolean hasPureFunctionSpecifier() {
		return this.pureFunctionSpecifier;
	}

	@Override
	public void setPureFunctionSpecifier(boolean value) {
		this.pureFunctionSpecifier = value;
	}

	@Override
	public boolean isLogicFunction() {
		return isLogic;
	}

	@Override
	public void setIsLogicFunction(boolean isLogicFunction) {
		this.isLogic = isLogicFunction;
	}

	@Override
	public ASTNode setChild(int index, ASTNode child) {
		if (index == 2 && !(child == null || child instanceof SequenceNode))
			throw new ASTException(
					"Child of CommonFunctionDeclarationNode at index " + index
							+ " must be an SequenceNode, but saw " + child
							+ " with type " + child.nodeKind());
		return super.setChild(index, child);
	}
}
