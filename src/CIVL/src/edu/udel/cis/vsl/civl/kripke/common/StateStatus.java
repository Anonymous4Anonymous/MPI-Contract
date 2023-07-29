package edu.udel.cis.vsl.civl.kripke.common;

import edu.udel.cis.vsl.civl.semantics.IF.Transition;

/**
 * A helper class to keep track of analysis result of states in the sense that
 * if they are allowed to be executed further.
 * 
 * @author Manchun Zheng
 * @author Wenhao Wu (xxxx@udel.edu)
 */
class StateStatus {
	/**
	 * The enabling status of a process at a state.
	 * 
	 * @author Manchun Zheng
	 * 
	 */
	enum EnabledStatus {
		BLOCKED, DETERMINISTIC, LOOP_POSSIBLE, NONDETERMINISTIC, NONE, TERMINATED, UNSAFE
	}

	/**
	 * The enabling status of the current process at the current state.
	 */
	EnabledStatus enabledStatus;

	/**
	 * The result of the enabling analysis: i.e., whether the process is allowed
	 * to execute more.
	 */
	boolean canExecuteMore;

	/**
	 * The current enabled transition of the current process. Not NULL only when
	 * the process is allowed to execute more.
	 */
	Transition enabledTransition;

	/**
	 * Creates a new instance of StateStatus.
	 * 
	 * @param result
	 *            The result of the state status analysis.
	 * @param transition
	 *            The single transition enabled. NULL if status is not
	 *            DETERMINISTIC.
	 * @param status
	 *            The status of the state.
	 */
	StateStatus(boolean result, Transition transition, EnabledStatus status) {
		this.canExecuteMore = result;
		this.enabledTransition = transition;
		this.enabledStatus = status;
	}
}
