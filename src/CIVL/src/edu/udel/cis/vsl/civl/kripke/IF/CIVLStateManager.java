package edu.udel.cis.vsl.civl.kripke.IF;

import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Printable;
import edu.udel.cis.vsl.gmc.seq.StateManager;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * StateManager extends {@link StateManager} for CIVL models.
 * 
 * @author Manchun Zheng
 * 
 */
public abstract class CIVLStateManager extends StateManager<State, Transition> {

	/**
	 * Returns the number of objects of type State that have been instantiated
	 * since this JVM started.
	 * 
	 * @return the number of states instantiated
	 */
	public abstract long getNumStateInstances();

	/**
	 * @return The maximum number of processes in any state encountered by this
	 *         state manager.
	 */
	public abstract int maxProcs();

	/**
	 * Print an update message at your earliest possible convenience.
	 */
	public abstract void printUpdate();

	/**
	 * Set the field savedStates.
	 * 
	 * @param updater
	 *            The value to be used.
	 */
	public abstract void setUpdater(Printable updater);

	/**
	 * @return the number of saved states explored by the state manager
	 */
	public abstract int numStatesExplored();

	/**
	 * Outputs collected for the model during the search.
	 * 
	 * @return all possible outputs
	 */
	public abstract Map<BooleanExpression, Set<Pair<State, SymbolicExpression[]>>> collectedOutputs();

	/**
	 * The names of output variables of the model.
	 * 
	 * @return the names of output variables
	 */
	public abstract String[] outptutNames();
}
