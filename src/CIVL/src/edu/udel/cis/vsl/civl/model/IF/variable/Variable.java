/**
 * 
 */
package edu.udel.cis.vsl.civl.model.IF.variable;

import edu.udel.cis.vsl.civl.model.IF.Identifier;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.Sourceable;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * A static variable. Each variable is declared in some static scope. Each
 * variable has a name, a type, and an integer variable ID. The ID is in the
 * range [0,n-1], where n is the number of variables in the static scope
 * containing this variable. This variable's ID is unique within its scope.
 * 
 * @author Manchun Zheng (zxxxx)
 * @author Timothy K. Zirkel (zirkel)
 * @author Timothy J. McClory (tmcclory)
 * 
 */
public interface Variable extends Sourceable {

	/**
	 * 
	 * @return The index of this variable in the containing scope.
	 */
	int vid();

	/**
	 * @return The type of this variable.
	 */
	CIVLType type();

	/**
	 * @return Whether this variable is const qualified.
	 */
	boolean isConst();

	/**
	 * @return Whether this variable is input qualified.
	 */
	boolean isInput();

	/**
	 * @return Whether this variable is output qualified.
	 */
	boolean isOutput();

	/**
	 * @return Whether this variable is a bound variable of a quantified
	 *         expression.
	 */
	boolean isBound();

	/**
	 * is this variable static qualified?
	 * 
	 * @return
	 */
	boolean isStatic();

	/**
	 * @param type
	 *            The type of this variable.
	 */
	void setType(CIVLType type);

	/**
	 * @param isConst
	 *            Whether this variable is a const.
	 */
	void setConst(boolean isConst);

	/**
	 * @param value
	 *            Whether this variable is an input.
	 */
	void setIsInput(boolean value);

	/**
	 * @param value
	 *            Whether this variable is an output.
	 */
	void setIsOutput(boolean value);

	/**
	 * @param value
	 *            Whether this variable is a bound variable.
	 */
	void setIsBound(boolean value);

	/**
	 * @return The name of this variable.
	 */
	Identifier name();

	/**
	 * @param name
	 *            The name of this variable.
	 */
	void setName(Identifier name);

	/**
	 * @param scope
	 *            The scope to which this variable belongs.
	 */
	void setScope(Scope scope);

	/**
	 * @param vid
	 *            The index of this variable in the containing scope.
	 */
	void setVid(int vid);

	/**
	 * @return The scope of this variable.
	 */
	Scope scope();

	/**
	 * return true iff the variable is purely local, i.e., v is purely local if
	 * there is no &v in the model, and in v's scope, if there are no spawned
	 * functions that refer to v
	 * 
	 * @return
	 */
	boolean purelyLocal();

	/**
	 * sets this variable to be purely local according to the given value
	 * 
	 * @param value
	 */
	void setPurelyLocal(boolean value);

	/**
	 * sets this variable to be static qualified according to the given value
	 * 
	 * @param value
	 */
	void setStatic(boolean value);

	/**
	 * returns true iff this variable has some pointer reference, e.g., when the
	 * variable is pointer type, or an array of pointer, etc.
	 * 
	 * @return
	 */
	boolean hasPointerRef();

	/**
	 * updates the pointer reference feature of this variable. This is for
	 * static analysis and is called in the procedure of the containing scope
	 * calculating variables with pointer references.
	 * 
	 * @param value
	 */
	void setPointerRef(boolean value);

	/**
	 * returns the constant value of this variable. If the variable is not
	 * constant or input qualified, then only
	 * 
	 * @return
	 */
	SymbolicExpression constantValue();

	/**
	 * sets the constant value of this variable
	 * 
	 * @param value
	 */
	void setConstantValue(SymbolicExpression value);

	/**
	 * Is this variable declared as a parameter of some function?
	 * 
	 * @return true iff this variable is declared as a parameter of some
	 *         function
	 */
	boolean isParameter();
}
