package edu.udel.cis.vsl.civl.kripke.common;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.kripke.IF.Enabler;
import edu.udel.cis.vsl.civl.kripke.IF.TraceStep;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.semantics.IF.Executor;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.CIVLHeapException.HeapErrorKind;
import edu.udel.cis.vsl.civl.state.IF.ProcessState;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.gmc.TraceStepIF;

/**
 * A collate state has n processes, where n>=1, 1 process has non-empty call
 * stack and is active (also called external process), and the remaining (n-1)
 * processes (also called internal processes) either have empty call stack or
 * are at the SLEEP location. The collate state manager is responsible for
 * producing the transitions executed by the active external process, and also
 * collect the final collate states. When the external process has an empty call
 * stack, the state becomes a FINAL collate state, i.e., the resulting state
 * after the external process finish its execution.
 * 
 * @author Manchun Zheng
 *
 */
public class ColStateManager extends CommonStateManager {

	/**
	 * The set of FINAL collate states.
	 */
	private Set<State> finalColStates;

	/**
	 * Creates a new instance of collate state manager.
	 * 
	 * @param enabler
	 * @param executor
	 * @param symbolicAnalyzer
	 * @param errorLogger
	 * @param config
	 */
	public ColStateManager(Enabler enabler, Executor executor,
			SymbolicAnalyzer symbolicAnalyzer, CIVLErrorLogger errorLogger,
			CIVLConfiguration config) {
		super(enabler, executor, symbolicAnalyzer, errorLogger, config);
		super.config.setSimplify(false);
		finalColStates = new HashSet<>();
		ignoredHeapErrors = new HashSet<>(2);
		ignoredHeapErrors.add(HeapErrorKind.NONEMPTY);
		ignoredHeapErrors.add(HeapErrorKind.UNREACHABLE);
	}

	@Override
	public TraceStepIF<State> nextState(State state, Transition transition) {
		int pid = transition.pid();
		TraceStep result = new CommonTraceStep(pid);

		try {
			nextStateWork(state, transition, result);
		} catch (UnsatisfiablePathConditionException e) {
			// problem is the interface requires an actual State
			// be returned. There is no concept of executing a
			// transition and getting null or an exception.
			// since the error has been logged, just return
			// some state with false path condition, so there
			// will be no next state...
			State lastState = result.getFinalState();

			if (lastState == null)
				lastState = state;
			result.setFinalState(
					stateFactory.addToPathcondition(lastState, pid, falseExpr));
		}

		State resultState = result.getFinalState();

		if (isFinalCollateState(resultState))
			this.finalColStates.add(resultState);
		return result;
	}

	public Collection<State> getFinalCollateStates() {
		return this.finalColStates;
	}

	private boolean isFinalCollateState(State state) {
		int numProcs = state.numProcs();

		for (int i = 0; i < numProcs; i++) {
			ProcessState proc = state.getProcessState(i);

			if (proc != null && !proc.hasEmptyStack()
					&& !proc.getLocation().isSleep())
				return false;
		}
		return true;
	}
	@Override
	public void normalize(TraceStepIF<State> traceStepIF) {
		super.normalize(traceStepIF);
	}
}
