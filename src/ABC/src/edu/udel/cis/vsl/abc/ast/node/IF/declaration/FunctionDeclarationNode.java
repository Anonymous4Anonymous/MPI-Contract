package edu.udel.cis.vsl.abc.ast.node.IF.declaration;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypedefNameNode;

/**
 * <p>
 * A node representing a function declaration. This includes a function
 * prototype as well as a function definition.
 * </p>
 * 
 * <p>
 * The children include: (0) an identifier node, the name of the function; (1) a
 * type node which is the type of the function (not necessarily a function type
 * node; e.g., it could be a {@link TypedefNameNode}), and (2) a contract node
 * for the function contract, which may be <code>null</code>.
 * </p>
 * 
 * <p>
 * A C function declaration may contain addition specifiers (e.g.,
 * <code>_Noreturn</code>). These specifiers are represented by boolean fields
 * in this node; they do not require additional children nodes.
 * </p>
 * 
 * @author xxxx
 */
public interface FunctionDeclarationNode extends OrdinaryDeclarationNode {

	@Override
	Function getEntity();

	/**
	 * Does the declaration include the <code>inline</code> function specifier?
	 * 
	 * @return <code>true</code> iff declaration contains <code>inline</code>
	 * @see #setInlineFunctionSpecifier(boolean)
	 */
	boolean hasInlineFunctionSpecifier();

	/**
	 * Set the inline function specifier bit to the given value.
	 * 
	 * @param value
	 *            if <code>true</code>, says that this function declaration
	 *            contains the <code>inline</code> specifier, if
	 *            <code>false</code>, it doesn't
	 * @see #hasInlineFunctionSpecifier()
	 */
	void setInlineFunctionSpecifier(boolean value);

	/**
	 * Does the declaration include the <code>_Noreturn</code> function
	 * specifier?
	 * 
	 * @return <code>true</code> iff declaration contains <code>_Noreturn</code>
	 * @see #setNoreturnFunctionSpecifier(boolean)
	 */
	boolean hasNoreturnFunctionSpecifier();

	/**
	 * Sets the <code>_Noreturn</code> bit to the given value.
	 * 
	 * @param value
	 *            if <code>true</code>, says that this function declaration
	 *            contains the <code>_Noreturn</code> specifier, if
	 *            <code>false</code>, it doesn't
	 * @see #hasNoreturnFunctionSpecifier()
	 */
	void setNoreturnFunctionSpecifier(boolean value);

	/**
	 * Does the declaration include the <code>__global__</code> CUDA function
	 * specifier?
	 * 
	 * @return <code>true</code> iff declaration contains
	 *         <code>__global__</code>
	 * @see #setGlobalFunctionSpecifier(boolean)
	 */
	boolean hasGlobalFunctionSpecifier();

	/**
	 * Set the global function specifier bit to the given value.
	 * 
	 * @param value
	 *            if <code>true</code>, says that this function declaration
	 *            contains the <code>__global__</code> specifier, if
	 *            <code>false</code>, it doesn't
	 * @see #hasGlobalFunctionSpecifier()
	 */
	void setGlobalFunctionSpecifier(boolean value);

	/**
	 * Does the declaration include the <code>$pure</code> function specifier? A
	 * $pure function is a function whose return value is only determined by its
	 * input values, without observable side effects.
	 * 
	 * @return <code>true</code> iff declaration contains <code>$pure</code>
	 * @see #setPureFunctionSpecifier(boolean)
	 */
	boolean hasPureFunctionSpecifier();

	/**
	 * Set the $pure function specifier bit to the given value.
	 * 
	 * @param value
	 *            if <code>true</code>, says that this function declaration
	 *            contains the <code>$pure</code> specifier, if
	 *            <code>false</code>, it doesn't
	 * @see #hasPureFunctionSpecifier()
	 */
	void setPureFunctionSpecifier(boolean value);

	/**
	 * Does the declaration include the <code>$state_f</code> function
	 * specifier? A $state_f function is a function whose return value is only
	 * determined by its input values and the current state, without observable
	 * side effects.
	 * 
	 * @return <code>true</code> iff declaration contains <code>$state_f</code>
	 * @see #setStatefFunctionSpecifier(boolean)
	 */
	boolean hasStatefFunctionSpecifier();

	/**
	 * Set the $state_f function specifier bit to the given value.
	 * 
	 * @param value
	 *            if <code>true</code>, says that this function declaration
	 *            contains the <code>$state_f</code> specifier, if
	 *            <code>false</code>, it doesn't
	 * @see #hasStatefFunctionSpecifier()
	 */
	void setStatefFunctionSpecifier(boolean value);

	/**
	 * Does the declaration include the <code>$atomic_f</code> function
	 * specifier?
	 * 
	 * @return <code>true</code> iff declaration contains <code>$atomic_f</code>
	 * @see #setAtomicFunctionSpecifier(boolean)
	 */
	boolean hasAtomicFunctionSpecifier();

	/**
	 * Set the atomic function specifier bit to the given value.
	 * 
	 * @param value
	 *            if <code>true</code>, says that this function declaration
	 *            contains the <code>$atomic_f</code> specifier, if
	 *            <code>false</code>, it doesn't
	 * @see #hasAtomicFunctionSpecifier()
	 */
	void setAtomicFunctionSpecifier(boolean value);

	/**
	 * Does the declaration include the <code>$system</code> function specifier?
	 * 
	 * @return <code>true</code> iff declaration contains <code>$system</code>
	 * @see #setSystemFunctionSpecifier(boolean)
	 */
	boolean hasSystemFunctionSpecifier();

	/**
	 * Set the system function specifier bit to the given value.
	 * 
	 * @param value
	 *            if <code>true</code>, says that this function declaration
	 *            contains the <code>$system</code> specifier, if
	 *            <code>false</code>, it doesn't
	 * @see #hasSystemFunctionSpecifier()
	 */
	void setSystemFunctionSpecifier(boolean value);

	/**
	 * gets the library name of this system function
	 * 
	 * @return
	 */
	String getSystemLibrary();

	/**
	 * sets the library name of this system function
	 * 
	 * @param library
	 */
	void setSystemLibrary(String library);

	/**
	 * Returns the contract node for this function declaration. May be
	 * <code>null</code>. It is a child node of this node.
	 * 
	 * @return the contract node child of this node
	 * @see #setContract(SequenceNode)
	 */
	SequenceNode<ContractNode> getContract();

	/**
	 * Sets the contract node child of this node to the given node.
	 * 
	 * @param contract
	 *            the contract node to be made a child of this node
	 * @see #getContract()
	 */
	void setContract(SequenceNode<ContractNode> contract);

	@Override
	FunctionDeclarationNode copy();

	/**
	 * @return true iff this function declaration node represents a logic
	 *         function declaration. A logic function is a function declared to
	 *         be logic (including predicates and functions defined in ACSL
	 *         annotations). The function definition is optional but if it
	 *         exists, the function defintion is a (return of a) side-effect
	 *         free expression.
	 * 
	 */
	boolean isLogicFunction();

	/**
	 * Sets weather this function definition node represents a logic function
	 * declaration.
	 * 
	 * @param isLogicFunction
	 *            true if to set this node to be a logic function declaration,
	 *            false otherwise.
	 */
	void setIsLogicFunction(boolean isLogicFunction);
}
