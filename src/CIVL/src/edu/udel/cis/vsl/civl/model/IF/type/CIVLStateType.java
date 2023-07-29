package edu.udel.cis.vsl.civl.model.IF.type;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public interface CIVLStateType extends CIVLPrimitiveType {

	/**
	 * Extract the scope value mapper from an object of the CIVLStateType. A
	 * scope value mapper is an array where indices are integral keys of scope
	 * values of the state represented by the object and indexed elements are
	 * corresponding scope values in the current state.
	 * 
	 * @param universe
	 *            a reference to {@link SymbolicUniverse}
	 * @param stateValue
	 *            the value of an object of CIVLStateType
	 * @return the integer array which maps scope values in the state value to
	 *         scope values in the current state.
	 */
	SymbolicExpression selectScopeValuesMap(SymbolicUniverse universe,
			SymbolicExpression stateValue);

	/**
	 * Extracts the integral key from an object of CIVLStateType.
	 * 
	 * @param universe
	 *            a reference to {@link SymbolicUniverse}
	 * @param stateValue
	 *            the value of an object of CIVLStateType
	 * @return a unique integral key value which identifies the state value
	 */
	int selectStateKey(SymbolicUniverse universe,
			SymbolicExpression stateValue);

	/**
	 * Translate an integer canonical state id into a symbolic expression
	 * 
	 * @param universe
	 *            a reference to {@link SymbolicUniverse}
	 * @param stateKey
	 *            The integer identifier of the generating state value
	 * @param scopeValuesToReal
	 *            an integer array that maps scope values in the state value to
	 *            scope values in the real state.
	 * @return The symbolic expression representing a state
	 */
	SymbolicExpression buildStateValue(SymbolicUniverse universe, int stateKey,
			SymbolicExpression scopeValuesToReal);
}
