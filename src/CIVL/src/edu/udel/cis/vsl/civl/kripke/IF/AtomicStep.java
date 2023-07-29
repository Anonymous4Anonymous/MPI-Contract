package edu.udel.cis.vsl.civl.kripke.IF;

import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.State;

/**
 * This represents an atomic execution step, which represents the execution of
 * exactly one transition.
 * 
 * @author Manchun Zheng
 * 
 */
public interface AtomicStep {
	/**
	 * Updates the resulting state of this atomic step.
	 * 
	 * @param state
	 *            The state to be used as the resulting state.
	 */
	void setPostState(State state);

	/**
	 * Returns the resulting state of this atomic step.
	 * 
	 * @return the resulting state of this atomic step.
	 */
	State getPostState();

	/**
	 * Returns the transition associated to this atomic step.
	 * 
	 * @return the transition associated to this atomic step.
	 */
	Transition getTransition();
}
