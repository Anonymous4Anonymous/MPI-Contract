package edu.udel.cis.vsl.abc.front.fortran.astgen;

import java.util.HashMap;
import java.util.HashSet;

import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.front.common.astgen.SimpleScope;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

/**
 * FORTRAN program scope extends {@link SimpleScope} to:<br>
 * 1). keep implicit-typing information for different translation unit scope
 * instances. <br>
 * 2). keep type information for the associated function, its parameters and
 * locals.
 * 
 * @author xxxx (xxxx@udel.edu)
 *
 */
public class MFScope extends SimpleScope {
	static private String DEFAULT_TYPE_INFO = "Default implicit type declaration.";

	private String scopeName = null;
	private FunctionTypeNode funcTypeNode = null;
	private TypeNode[] implicitTypeNodes = null;
	private HashMap<String, TypeNode> localName2ArrayBaseType = null;
	private HashMap<String, VariableDeclarationNode> localName2VarDeclNode = null;
	/**
	 * Store the mapping from parameter name to its declaration node.<br>
	 * Parameters that may be written are changed from scalar type to its
	 * pointer type.<br>
	 * Extents of array types are removed.
	 */
	private HashMap<String, VariableDeclarationNode> paramName2VarDeclNode = null;
	private HashSet<String> undeclaredIdentifiers = null;
	private HashSet<String> declaredLabels = null;
	private HashSet<String> derivedTypeNames = null;

	/**
	 * Construct the outer-most scope for CIVL AST root node of a Fortran
	 * program.
	 */
	MFScope() {
		super(null, false);
		this.funcTypeNode = null;
		this.implicitTypeNodes = null;
		this.localName2VarDeclNode = new HashMap<>();
		this.localName2ArrayBaseType = new HashMap<>();
		this.paramName2VarDeclNode = new HashMap<>();
		this.undeclaredIdentifiers = new HashSet<>();
		this.declaredLabels = new HashSet<>();
		this.derivedTypeNames = new HashSet<>();
	}

	/**
	 * Construct a {@link MFScope} instance for a FORTRAN structure which is NOT
	 * a translation-unit. <br>
	 * The type implicit mapping array will inherit from its <code>host</code>
	 * by default. [See Fortran2018 Std. Chp. 8.7 IMPLICIT Statement Sec. 3]
	 * <br>
	 * (i.e., the default for a `BLOCK` construct, internal subprogram, or
	 * module subprogram is the mapping in the host scoping unit.)
	 * 
	 * @param host
	 *                 a non-<code>null</code> host {@link MFScope}
	 */
	MFScope(MFScope host) {
		super(host, false);
		assert host != null;
		this.funcTypeNode = host.funcTypeNode;
		this.implicitTypeNodes = host.implicitTypeNodes;
		this.localName2VarDeclNode = new HashMap<>();
		this.localName2VarDeclNode.putAll(host.localName2VarDeclNode);
		this.localName2ArrayBaseType = new HashMap<>();
		this.localName2ArrayBaseType.putAll(host.localName2ArrayBaseType);
		this.paramName2VarDeclNode = host.paramName2VarDeclNode;
		this.undeclaredIdentifiers = host.undeclaredIdentifiers;
		this.declaredLabels = host.declaredLabels;
		this.scopeName = host.scopeName;
		this.derivedTypeNames = host.derivedTypeNames;
	}

	/**
	 * Construct a root {@link MFScope} instance for a FORTRAN translation unit;
	 * Initialize the default implicit-types [See Fortran2018 Std. Chp. 8.7
	 * IMPLICIT Statement Sec. 3] <br>
	 * (i.e., the default for a program unit or an interface body is default
	 * integer ({@link BasicTypeKind.INT}); if the letter is I, J, ..., or N and
	 * default real ({@link BasicTypeKind.FLOAT}) otherwise)
	 * 
	 * @param host
	 *                             the host {@link MFScope}
	 * @param functionTypeNode
	 *                             provides type information for the program
	 *                             unit associated with <code>this</code>;
	 *                             parameter/return types may be updated based
	 *                             on type declarations in the specification
	 *                             part of associated program unit.
	 * @param scopeId
	 * @param argsMap
	 * @param astF
	 *                             is used for constructing default
	 *                             implicit-types and their dummy {@link Source}
	 */
	MFScope(MFScope host, FunctionTypeNode functionTypeNode, String scopeId,
			ASTFactory astF) {
		super(host, true);
		assert functionTypeNode != null && astF != null;

		TokenFactory tF = astF.getTokenFactory();
		NodeFactory nF = astF.getNodeFactory();
		Source src = tF.newSource(tF.newCivlcToken(CivlcTokenConstant.ABSENT,
				DEFAULT_TYPE_INFO,
				tF.newTransformFormation(getClass().getName(), "Constructor"),
				TokenVocabulary.FORTRAN));
		TypeNode typeNodeInt = nF.newBasicTypeNode(src, BasicTypeKind.INT);
		TypeNode typeNodeReal = nF.newBasicTypeNode(src, BasicTypeKind.FLOAT);

		funcTypeNode = functionTypeNode;
		implicitTypeNodes = new TypeNode[31];
		for (int i = 0; i < 8; i++) // 8 == 'H' - 'A' + 1;
			implicitTypeNodes[i] = typeNodeReal;
		for (int i = 8; i < 14; i++) // 14 == 'N' - 'A' + 1;
			implicitTypeNodes[i] = typeNodeInt;
		for (int i = 14; i < 26; i++) // 26 == 'Z' - 'A' + 1;
			implicitTypeNodes[i] = typeNodeReal;
		implicitTypeNodes[30] = typeNodeReal; // 30 == '_' - 'A' + 1;
		localName2VarDeclNode = new HashMap<>();
		localName2ArrayBaseType = new HashMap<>();
		paramName2VarDeclNode = new HashMap<>();
		undeclaredIdentifiers = new HashSet<>();
		declaredLabels = new HashSet<>();
		derivedTypeNames = new HashSet<>();
		if (host != null) {
			localName2VarDeclNode.putAll(host.localName2VarDeclNode);
			declaredLabels.addAll(host.declaredLabels);
			derivedTypeNames.addAll(host.derivedTypeNames);
		}
		for (VariableDeclarationNode parameterDeclNode : funcTypeNode
				.getParameters()) {
			String parameterName = parameterDeclNode.getName();

			paramName2VarDeclNode.put(parameterName, parameterDeclNode);
			localName2VarDeclNode.put(parameterName, parameterDeclNode);
		}
		this.scopeName = scopeId;
	}

	/**
	 * Set <code>this</code> scope to require explicit type declarations for all
	 * variables.
	 */
	void setImplicitNone() {
		implicitTypeNodes = null;
	}

	/**
	 * @return </code>true</code> iff `IMPLICIT NONE` is associated with
	 *         <code>this</code> {@link MFScope}.
	 */
	boolean isImplicitNone() {
		return implicitTypeNodes == null;
	}

	/**
	 * Set the implicit type mapping array based on `IMPLICIT` statement. The
	 * index range is [<code>lchar - 'A'</code> .. <code>rchar - 'A'</code>].
	 * 
	 * @param lchar
	 *                     the starting letter representing variable initials.
	 * @param rchar
	 *                     the ending letter representing variable initials.
	 * @param typeNode
	 *                     the new {@link TypeNode} specified by an `IMPLICIT`
	 *                     statement.
	 */
	void setImplicitTypes(char lchar, char rchar, TypeNode baseTypeNode,
			TypeNode paramTypeNode) {
		int start = lchar - 'A';
		int end = rchar - 'A' + 1;

		assert 0 <= start && start <= end && end <= 27; // 27 == 'Z' - 'A' + 1;
		for (int i = start; i < end; i++)
			implicitTypeNodes[i] = baseTypeNode;
		for (VariableDeclarationNode paramDeclNode : paramName2VarDeclNode
				.values()) {
			char initialParam = paramDeclNode.getIdentifier().name().charAt(0);

			if (lchar <= initialParam && initialParam <= rchar)
				paramDeclNode.setTypeNode(paramTypeNode.copy());
		}
	}

	/**
	 * Get the array element type based on the given indentifier name
	 * 
	 * @param varIdStr
	 * @return
	 */
	TypeNode getArrayElementTypeByName(String varName) {
		if (localName2ArrayBaseType.containsKey(varName))
			return localName2ArrayBaseType.get(varName);
		else
			return getImplicitType(varName.toUpperCase().charAt(0));
	}

	Boolean hasArrayType(String varName) {
		return localName2ArrayBaseType.containsKey(varName);
	}

	/**
	 * Get the implicit type based on the given identifier initial.
	 * 
	 * @param initial
	 *                    the initial letter of a identifier.
	 * @return the implicit type associated with the given <code>initial</code>
	 *         by default or `IMPLICIT` statement.
	 */
	TypeNode getImplicitType(char initial) {
		return implicitTypeNodes[initial - 'A'].copy();
	}

	/**
	 * Let <code>this</code> record local variable declaration in it.
	 * 
	 * @param localName
	 *                        a name String of a local variable.
	 * @param varDeclNode
	 *                        a CIVL Node representing the declaration of the
	 *                        local variable.
	 */
	void addVarDeclNode(String localName, VariableDeclarationNode varDeclNode) {
		localName2VarDeclNode.put(localName, varDeclNode);
	}

	/**
	 * Get the local variable declaration node identified as
	 * <code>varName</code>. Note that:<br>
	 * 1. A Sub-scope contains local variable declarations in its parent scope.
	 * <br>
	 * 2. For multiple declarations with a same name, only the latest one is
	 * returned.
	 * 
	 * @param localName
	 *                      a name String of a local variable.
	 * @return a CIVL Node representing the declaration of the local variable
	 *         with the given name.
	 */
	VariableDeclarationNode getLocalVarDeclNode(String localName) {
		return localName2VarDeclNode.get(localName);
	}

	/**
	 * Let <code>this</code> record parameter variable declaration in its
	 * parameter list.
	 * 
	 * @param parameterName
	 *                          The name String of the parameter variable.
	 * @param varDeclNode
	 *                          The CIVL Node representing the declaration of
	 *                          the parameter variable.
	 */
	void addParameterVarDeclNode(String parameterName,
			VariableDeclarationNode varDeclNode) {
		paramName2VarDeclNode.put(parameterName, varDeclNode);
	}

	/**
	 * 
	 * @param parameterName
	 *                          a name String of a parameter variable.
	 * @return a CIVL Node representing the declaration of the parameter
	 *         variable with the given name.
	 */
	VariableDeclarationNode getParameterVarDeclNode(String parameterName) {
		return paramName2VarDeclNode.get(parameterName);
	}

	/**
	 * @return the outer-most scope unit name, which is usually a function,
	 *         program or subroutine name
	 */
	String getScopeName() {
		return this.scopeName;
	}

	/**
	 * Check whether <code>varName</code> is an identifier of a parameter. Note
	 * that:<br>
	 * 1. Sub-scopes in a program unit (program, function, subroutine, etc)
	 * share a same parameter name to declaration map.
	 * 
	 * @param varName
	 *                    an identifier name of a variable
	 * @return <code>true</code> iff the name String
	 */
	boolean isParameter(String varName) {
		return paramName2VarDeclNode.keySet().contains(varName);
	}

	/**
	 * Get the type of given variable name under this scope
	 * 
	 * @param varName
	 * @return
	 */
	TypeNode getTypeByName(String varName) {
		if (isDeclared(varName))
			return localName2VarDeclNode.get(varName).getTypeNode();
		else
			return getImplicitType(varName.toUpperCase().charAt(0));
	}

	/**
	 * Check whether the given identifier is declared under this scope.s
	 * 
	 * @param varName
	 * @return
	 */
	boolean isDeclared(String varName) {
		return localName2VarDeclNode.containsKey(varName);
	}

	void addDerivedTypeName(String name) {
		derivedTypeNames.add(name);
	}

	/**
	 * Check whether the given identifier is a derived type name
	 * 
	 * @param name
	 * @return
	 */
	boolean isDerivedTypeName(String name) {
		return derivedTypeNames.contains(name);
	}

	void addUndeclaredIdentifiers(String identifier) {
		this.undeclaredIdentifiers.add(identifier);
	}

	HashSet<String> undeclaredIdentifiers() {
		return this.undeclaredIdentifiers;
	}

	void addArrayDecl(String name, TypeNode baseTypeNode) {
		this.localName2ArrayBaseType.put(name, baseTypeNode);
	}

	void addLabels(String lblText) {
		declaredLabels.add(lblText);
	}

	boolean containsLabel(String lblText) {
		return declaredLabels.contains(lblText);
	}
}
