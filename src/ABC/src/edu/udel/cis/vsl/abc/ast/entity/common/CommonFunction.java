package edu.udel.cis.vsl.abc.ast.entity.common;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.entity.IF.ProgramEntity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Scope;
import edu.udel.cis.vsl.abc.ast.entity.IF.Scope.ScopeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.FunctionType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;

public class CommonFunction extends CommonOrdinaryEntity implements Function {

	private boolean isInlined, doesNotReturn, isAtomic, isSystemFunction,
			isPure, isStateF, isAbstract, isLogic = false;

	private Set<Function> callers = new HashSet<>();
	private Set<Function> callees = new HashSet<>();
	static Function mainFunction;
	private String library = null;

	private List<ContractNode> contracts = new LinkedList<>();

	public CommonFunction(String name, ProgramEntity.LinkageKind linkage,
			Type type) {
		super(EntityKind.FUNCTION, name, linkage, type);
	}

	@Override
	public boolean isInlined() {
		return isInlined;
	}

	@Override
	public void setIsInlined(boolean value) {
		this.isInlined = value;
	}

	@Override
	public boolean doesNotReturn() {
		return doesNotReturn;
	}

	@Override
	public void setDoesNotReturn(boolean value) {
		this.doesNotReturn = value;
	}

	@Override
	public FunctionDefinitionNode getDefinition() {
		return (FunctionDefinitionNode) super.getDefinition();
	}

	@Override
	public Scope getScope() {
		Scope result = getDefinition().getBody().getScope();

		while (result != null) {
			if (result.getScopeKind() == ScopeKind.FUNCTION)
				break;
			result = result.getParentScope();
		}
		if (result == null)
			throw new ABCRuntimeException(
					"Could not find function scope of function " + this);
		return result;
	}

	@Override
	public FunctionType getType() {
		return (FunctionType) super.getType();
	}

	@Override
	public Set<Function> getCallers() {
		return callees;
	}

	@Override
	public Set<Function> getCallees() {
		return callers;
	}

	@Override
	public void addContract(ContractNode contract) {
		contracts.add(contract);
	}

	@Override
	public Iterable<ContractNode> getContracts() {
		return contracts;
	}

	@Override
	public boolean isAtomic() {
		return this.isAtomic;
	}

	@Override
	public void setAtomic(boolean value) {
		this.isAtomic = value;
	}

	@Override
	public void setSystemFunction(boolean value) {
		this.isSystemFunction = value;
	}

	@Override
	public boolean isSystemFunction() {
		return this.isSystemFunction;
	}

	@Override
	public boolean isAbstract() {
		return this.isAbstract;
	}

	@Override
	public void setAbstract(boolean value) {
		this.isAbstract = value;
	}

	@Override
	public String systemLibrary() {
		return this.library;
	}

	@Override
	public void setSystemLibrary(String library) {
		this.library = library;
	}

	@Override
	public boolean isPure() {
		return this.isPure;
	}

	@Override
	public void setPure(boolean value) {
		this.isPure = value;
	}

	@Override
	public boolean isStateFunction() {
		return this.isStateF;
	}

	@Override
	public void setStateFunction(boolean value) {
		this.isStateF = value;
	}

	@Override
	public boolean isLogic() {
		return isLogic;
	}

	@Override
	public void setLogic(boolean value) {
		this.isLogic = value;
	}
}
