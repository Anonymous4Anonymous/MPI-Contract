package edu.udel.cis.vsl.civl.state.IF;

import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * <p>
 * This class provides a set of interfaces for help dealing with values of
 * $state type objects.
 * </p>
 * 
 * <p>
 * This class provides methods for dealing with scope values problems: Scopes in
 * the state referred by a $state object may not be consistent with other
 * states.
 * <ul>
 * <li>Method
 * {@link #scopeSubstituterForReferredState(State, SymbolicExpression)} creates
 * a substituter for mapping scope values in a "state" to scope values in a
 * state referred by a $state object "stateValue".</li>
 * </ul>
 * </p>
 * 
 * @author xxxx
 */
public interface StateValueHelper {
	/**
	 * <p>
	 * Given a current {@link State} and a $state type object
	 * "referredStateValue", returns a substituter. For the same scopes, the
	 * substituter substitutes their values in the current state to their values
	 * in the state referred by the $state object. For scopes that exist in the
	 * current state but not the state referred by $state object, this
	 * substituter substitutes them to an
	 * {@link ModelConfiguration#DYNAMIC_UNDEFINED_SCOPE}
	 * </p>
	 * 
	 * @param currState
	 *            the current state which contains the $state type object
	 * @param referredStateValue
	 *            the value of a $state type object in the current state
	 * @return a substituter
	 */
	UnaryOperator<SymbolicExpression> scopeSubstituterForReferredState(
			State currState, SymbolicExpression referredStateValue);

	/**
	 * <P>
	 * Given a current {@link State} and a $state type object, returns a
	 * substituter that maps scope values in the state referred by the $state
	 * object to the scope values in the current state. The current state is the
	 * state contains the $state object.
	 * </p>
	 * 
	 * @param currState
	 *            the current state which contains the $state type object
	 * @param referredStateValue
	 *            the value of a $state type object
	 * @return a substituter
	 */
	UnaryOperator<SymbolicExpression> scopeSubstituterForCurrentState(
			State currState, SymbolicExpression referredStateValue);
}
