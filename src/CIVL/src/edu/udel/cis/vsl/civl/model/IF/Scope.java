/**
 * 
 */
package edu.udel.cis.vsl.civl.model.IF;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

/**
 * <p>
 * A scope. A scope contains the variables exclusive to this scope and
 * references to any subscopes.
 * </p>
 * 
 * <p>
 * Maintainer: Stephen Siegel (xxxx)
 * </p>
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public interface Scope extends Sourceable {

	/**
	 * @return The containing scope of this scope. If this is the top-most
	 *         scope, returns null.
	 */
	Scope parent();

	/**
	 * <p>
	 * <b>Important notice: </b> Never ever modify the variable!
	 * </p>
	 * 
	 * @return The set of variables contained in this scope. The iterator over
	 *         the returned set will iterate in variable ID order.
	 */
	Variable[] variables();

	/**
	 * @return The number of variables contained in this scope.
	 */
	int numVariables();

	/**
	 * @return The number of functions contained in this scope.
	 */
	int numFunctions();

	/**
	 * @return The id of this scope. This id is unique within the model.
	 */
	int id();

	/**
	 * @return The scopes contained by this scope.
	 */
	Set<Scope> children();

	/**
	 * @return The model to which this scope belongs.
	 */
	Model model();

	/**
	 * @param parent
	 *            The containing scope of this scope.
	 */
	void setParent(Scope parent);

	/**
	 * @param variables
	 *            The set of variables contained in this scope.
	 */
	void setVariables(Set<Variable> variables);

	/**
	 * @param children
	 *            The scopes contained by this scope.
	 */
	void setChildren(Set<Scope> children);

	/**
	 * @param A
	 *            new scope contained by this scope.
	 */
	void addChild(Scope child);

	/**
	 * A new variable in this scope.
	 */
	void addVariable(Variable variable);

	/**
	 * If a variable is already included in this scope, return the included
	 * variable. Otherwise return null.
	 * 
	 * @param variable
	 *            The variable which is being checked.
	 * @return included variable iff variable already exists in this scope, null
	 *         otherwise.
	 */
	Variable contains(Variable variable);

	/**
	 * Get the variable associated with an identifier. If this scope does not
	 * contain such a variable, parent scopes will be recursively checked.
	 * 
	 * @param name
	 *            The identifier for the variable.
	 * @return The model representation of the variable in this scope hierarchy,
	 *         or null if not found.
	 */
	Variable variable(Identifier name);

	/**
	 * Get the variable at the specified array index.
	 * 
	 * @param vid
	 *            The index of the variable. Should be in the range
	 *            [0,numVariable()-1].
	 * @return The variable at the index.
	 */
	Variable variable(int vid);

	/**
	 * @param function
	 *            The function containing this scope.
	 */
	void setFunction(CIVLFunction function);

	/**
	 * @return The function containing this scope.
	 */
	CIVLFunction function();

	/**
	 * @return The identifier of the function containing this scope.
	 */
	Identifier functionName();

	/**
	 * A variables has a "procRefType" if it is of type Process, if it is an
	 * array with element of procRefType, or if it is a struct with fields of
	 * procRefType.
	 * 
	 * @return A collection of the variables in this scope with a procRefType.
	 */
	Collection<Variable> variablesWithProcrefs();

	/**
	 * A variables has a "$state" type, if it is of type $state, if it is an
	 * array with element of type $state, or if it is a struct with fields of
	 * type $state.
	 * 
	 * @return A collection of the variables in this scope with a type $state.
	 */
	Collection<Variable> variablesWithStaterefs();

	/**
	 * A variables has a "scopeRefType" if it is of type Scope, if it is an
	 * array with element of scopeRefType, if it is a struct with fields of
	 * scopeRefType, or if it contains a pointer.
	 * 
	 * @return A collection of the variables in this scope with a scopeRefType.
	 */
	Collection<Variable> variablesWithScoperefs();

	/**
	 * A variable contains a pointer type if it is of type PointerType, if it is
	 * an array with elements containing pointer type, or if it is a struct with
	 * fields containing pointer type.
	 * 
	 * @return A collection of the variables in this scope containing pointer
	 *         types.
	 */
	Collection<Variable> variablesWithPointers();

	/**
	 * A variable whose type is not a primitive type.
	 * 
	 * @return A collection of the variables in this scope containing pointer
	 *         types.
	 */
	Collection<Variable> varsNeedSymbolicConstant();

	/**
	 * Print the scope and all children.
	 * 
	 * @param prefix
	 *            String prefix to print on each line
	 * @param out
	 *            The PrintStream to use for printing.
	 * @param isDebug
	 *            True iff the debugging option is enabled, when more
	 *            information will be printed, such as if a variable is purely
	 *            local
	 */
	void print(String prefix, PrintStream out, boolean isDebug);

	// TODO: Is this necessary? vid contained in variable.
	// Maybe, since this can check that the variable is actually a member of
	// this scope and throw an exception otherwise. If it throws an exception,
	// that should also be in this contract.
	int getVid(Variable staticVariable);

	/**
	 * Return true if the scope is a descendant of the scope anc
	 * 
	 * @param des
	 * @param anc
	 * @return true or false
	 */
	boolean isDescendantOf(Scope anc);

	boolean containsVariable(String name);

	Variable variable(String name);

	void addFunction(CIVLFunction function);

	CIVLFunction getFunction(Identifier name);

	CIVLFunction getFunction(String name);

	CIVLFunction getFunction(int fid);

	void complete();

	boolean hasVariable();

	boolean hasVariableWtPointer();

}
