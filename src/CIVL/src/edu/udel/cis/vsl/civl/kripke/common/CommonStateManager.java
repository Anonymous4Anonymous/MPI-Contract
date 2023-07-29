/**
 * 
 */
package edu.udel.cis.vsl.civl.kripke.common;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.kripke.IF.AtomicStep;
import edu.udel.cis.vsl.civl.kripke.IF.CIVLStateManager;
import edu.udel.cis.vsl.civl.kripke.IF.Enabler;
import edu.udel.cis.vsl.civl.kripke.IF.TraceStep;
import edu.udel.cis.vsl.civl.kripke.common.StateStatus.EnabledStatus;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.semantics.IF.Executor;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.semantics.IF.Transition.TransitionKind;
import edu.udel.cis.vsl.civl.state.IF.CIVLHeapException;
import edu.udel.cis.vsl.civl.state.IF.CIVLHeapException.HeapErrorKind;
import edu.udel.cis.vsl.civl.state.IF.ProcessState;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Printable;
import edu.udel.cis.vsl.civl.util.IF.Utils;
import edu.udel.cis.vsl.gmc.TraceStepIF;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * @author Timothy K. Zirkel (zirkel)
 * @author Manchun Zheng (zxxxx)
 * @author Stephen F. Siegel (xxxx)
 * 
 */
public class CommonStateManager extends CIVLStateManager {

	/* *************************** Instance Fields ************************* */

	/**
	 * The unique enabler instance used by the system
	 */
	protected CommonEnabler enabler;

	/**
	 * The unique executor instance used by the system
	 */
	protected Executor executor;

	protected CIVLConfiguration config;

	/**
	 * The maximal number of processes at a state, initialized as 0.
	 */
	protected AtomicInteger maxProcs = new AtomicInteger(0);

	/**
	 * The unique state factory used by the system.
	 */
	protected StateFactory stateFactory;

	/**
	 * The object whose toString() method will be used to print the periodic
	 * update. The toString method of this object should print a short
	 * (one-line) message on the state of the search.
	 */
	private Printable updater;

	/**
	 * If true, print a short one-line update message on the state of the search
	 * at the next opportunity, and then set this flag back to false. This flag
	 * is typically set by a separate thread. Access to this thread is protected
	 * by the lock on this StateManager.
	 */

	protected CIVLErrorLogger errorLogger;

	/**
	 * The symbolic analyzer to be used.
	 */
	protected SymbolicAnalyzer symbolicAnalyzer;

	protected BooleanExpression falseExpr;

	protected AtomicInteger numStatesExplored = new AtomicInteger(1);

	private AtomicInteger MaxNormalizedId = new AtomicInteger(0);;

	private OutputCollector outputCollector;

	protected boolean printTransitions;

	protected boolean printAllStates;

	protected boolean printSavedStates;

	protected Set<HeapErrorKind> ignoredHeapErrors;

	// TODO: trying to fix this:
	// private boolean saveStates;

	/* ***************************** Constructor *************************** */

	/**
	 * Creates a new instance of state manager.
	 * 
	 * @param enabler
	 *            The enabler to be used.
	 * @param executor
	 *            The unique executor to by used in the system.
	 * @param symbolicAnalyzer
	 *            The symbolic analyzer to be used.
	 * @param errorLogger
	 *            The error logger to be used.
	 * @param config
	 *            The configuration of the civl model.
	 */
	public CommonStateManager(Enabler enabler, Executor executor,
			SymbolicAnalyzer symbolicAnalyzer, CIVLErrorLogger errorLogger,
			CIVLConfiguration config) {
		this.executor = executor;
		this.enabler = (CommonEnabler) enabler;
		this.stateFactory = executor.stateFactory();
		this.config = config;
		printTransitions = this.config.printTransitions()
				|| config.debugOrVerbose();
		printAllStates = this.config.debugOrVerbose()
				|| this.config.showStates();
		printSavedStates = this.config.debugOrVerbose()
				|| this.config.showSavedStates();
		this.errorLogger = errorLogger;
		this.symbolicAnalyzer = symbolicAnalyzer;
		this.falseExpr = symbolicAnalyzer.getUniverse().falseExpression();
		if (config.collectOutputs())
			this.outputCollector = new OutputCollector(
					this.enabler.modelFactory.model(), this.enabler.universe);
		ignoredHeapErrors = new HashSet<>(0);
	}

	/* *************************** Private Methods ************************* */
	/**
	 * Execute a transition (obtained by the enabler) from a state. When the
	 * corresponding process is in atomic execution, continues to execute more
	 * statements---as many as possible. Also executes more statements if
	 * possible.
	 * 
	 * @param state
	 *            the current state
	 * @param transition
	 *            the transition to be executed from that state
	 * @param traceStep
	 *            the trace step that will be modified by appending the result
	 *            of execution
	 * @throws UnsatisfiablePathConditionException
	 */
	protected void nextStateWork(State state, Transition transition,
			TraceStep traceStep) throws UnsatisfiablePathConditionException {
		int pid;
		int numProcs;
		Transition firstTransition;
		StateStatus stateStatus;
		String process;

		pid = transition.pid();
		process = "p" + pid;
		firstTransition = (Transition) transition;
		state = executor.execute(state, pid, firstTransition);
		traceStep.addAtomicStep(new CommonAtomicStep(state, firstTransition));
		for (stateStatus = singleEnabled(state, pid,
				process); stateStatus.canExecuteMore; stateStatus = singleEnabled(
						state, pid, process)) {
			assert stateStatus.enabledTransition != null;
			assert stateStatus.enabledStatus == EnabledStatus.DETERMINISTIC;
			state = executor.execute(state, stateStatus.enabledTransition.pid(),
					stateStatus.enabledTransition);
			numStatesExplored.getAndIncrement();
			traceStep.addAtomicStep(
					new CommonAtomicStep(state, stateStatus.enabledTransition));
		}
		assert stateStatus.enabledStatus != EnabledStatus.DETERMINISTIC;
		if (stateStatus.enabledStatus == EnabledStatus.BLOCKED
				&& stateFactory.lockedByAtomic(state))
			state = stateFactory.releaseAtomicLock(state);
		traceStep.setFinalState(state);
		numProcs = state.numLiveProcs();
		Utils.biggerAndSet(maxProcs, numProcs);
		// if (numProcs > maxProcs)
		// maxProcs = numProcs;
		if (config.collectOutputs())
			this.outputCollector.collectOutputs(state);
	}

	/**
	 * Analyzes if the current process has a single (deterministic) enabled
	 * transition at the given state. The point of this is that a sequence of
	 * these kind of transitions can be launched together to form a big
	 * transition. TODO Predicates are not checked for intermediate states.
	 * Conditions for a process p at a state s to execute more:
	 * <ul>
	 * <li>p is currently holding the atomic lock:
	 * <ol>
	 * <li>the current location of p has exactly one incoming statement;</li>
	 * <li>the size of enabled(p, s) should be exactly 1.</li>
	 * </ol>
	 * </li> or
	 * <li>the current location of p is purely local;
	 * <ul>
	 * <li>the size of enabled(p, s) is exactly 1.</li>
	 * </ul>
	 * </li>
	 * </ul>
	 * 
	 * @param state
	 *            The current state.
	 * @param pid
	 *            The ID of the current process.
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	protected StateStatus singleEnabled(State state, int pid, String process)
			throws UnsatisfiablePathConditionException {
		List<Transition> enabled;
		ProcessState procState = state.getProcessState(pid);
		Location pLocation;
		boolean inAtomic = false;

		if (procState == null || procState.hasEmptyStack())
			return new StateStatus(false, null, EnabledStatus.TERMINATED);
		else
			pLocation = procState.getLocation();
		assert pLocation != null;
		if (pLocation.isGuardedLocation())
			return new StateStatus(false, null, EnabledStatus.BLOCKED);
		enabled = enabler.enabledTransitionsOfProcess(state, pid);

		// Atomic processing
		if (stateFactory.processInAtomic(state) == pid) {
			// the process is in atomic execution
			// assert pidInAtomic == pid;
			if ((pLocation.isInLoop() && !pLocation.isSafeLoop())
					|| (pLocation.isStart() && pLocation.getNumIncoming() > 0))
				// possible loop, save state
				return new StateStatus(false, null,
						EnabledStatus.LOOP_POSSIBLE);
			inAtomic = true;
		}
		if (inAtomic || pLocation.isPurelyLocal()) {
			if (enabled.size() == 1)
				return new StateStatus(true, enabled.get(0),
						EnabledStatus.DETERMINISTIC);
			else if (enabled.size() > 1) // blocking
				return new StateStatus(false, null,
						EnabledStatus.NONDETERMINISTIC);
			else
				return new StateStatus(false, null, EnabledStatus.BLOCKED);
		}
		return new StateStatus(false, null, EnabledStatus.NONE);

	}

	/**
	 * Print a step of a statement, in the following form:
	 * <code>src->dst: statement at file:location text;</code>For example,<br>
	 * <code>32->17: sum = (sum+(3*i)) at f0:20.14-24 "sum += 3*i";</code><br>
	 * When the atomic lock variable is changed during executing the statement,
	 * then the corresponding information is printed as well. For example,<br>
	 * <code>13->6: ($ATOMIC_LOCK_VAR = $self) x = 0 at f0:30.17-22
	 "x = 0";</code>
	 * 
	 * @param s
	 *            The statement that has been executed in the current step.
	 * @param atomicKind
	 *            The atomic kind of the source location of the statement.
	 * @param atomicLockVarChanged
	 *            True iff the atomic lock variable is changed during the
	 *            execution of the statement.
	 * @throws UnsatisfiablePathConditionException
	 */
	protected void printStatement(State currentState, State newState,
			Transition transition) {
		Statement stmt = transition.statement();

		config.out().print("  ");
		config.out().print(stmt.locationStepString());
		config.out().print(": ");
		try {
			config.out().print(symbolicAnalyzer.statementEvaluation(
					currentState, newState, transition.pid(), stmt));
		} catch (UnsatisfiablePathConditionException e) {
			throw new CIVLInternalException(
					"UnsatisfiablePathConditionException happens when printing a statement",
					stmt.getSource());
		}
		if (transition.transitionKind() == TransitionKind.NOOP) {
			BooleanExpression assumption = transition.clause();

			if (assumption != null) {
				config.out().print(" [$assume(");
				config.out()
						.print(symbolicAnalyzer
								.symbolicExpressionToString(stmt.getSource(),
										currentState, this.enabler.modelFactory
												.typeFactory().booleanType(),
										assumption));
				config.out().print(")]");
			}
		}
		config.out().print(" at ");
		config.out().print(stmt.summaryOfSource());
		config.out().println();
	}

	/**
	 * Print the prefix of a transition.
	 * 
	 * @param printTransitions
	 *            True iff each step is to be printed.
	 * @param state
	 *            The source state of the transition.
	 * @param processIdentifier
	 *            The identifier of the process that this transition associates
	 *            with.
	 */
	private void printTransitionPrefix(State state, int processIdentifier,
			int stateID) {
		// Executed by p0 from State 1
		config.out().print("Executed by p");
		if (stateID < 0)
			config.out().println(processIdentifier + " from State " + state);
		else
			config.out().println(
					processIdentifier + " from State " + stateID + " " + state);
		config.out().flush();
	}

	/**
	 * Print the updated status.
	 */
	private void printUpdateWork() {
		updater.print(config.out());
		config.out().flush();
	}

	/* ********************* Methods from StateManagerIF ******************* */

	@Override
	public TraceStepIF<State> nextState(State state, Transition transition) {
		int pid = transition.pid();
		TraceStep result = new CommonTraceStep(pid);

		// nextStateCalls++;
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
		return result;
	}

	@Override
	public void printAllStatesLong(PrintStream arg0) {

	}

	@Override
	public void printAllStatesShort(PrintStream arg0) {

	}

	@Override
	public void printStateLong(PrintStream out, State state) {
		out.print(this.symbolicAnalyzer.stateToString(state));
	}

	@Override
	public void printStateShort(PrintStream out, State state) {
		out.print(state.toString());
	}

	@Override
	public void printTransitionLong(PrintStream out, Transition transition) {
		out.print(transition.toString());
	}

	@Override
	public void printTransitionShort(PrintStream out, Transition transition) {
		out.print(transition.toString());
	}

	@Override
	public void printTraceStep(State source, TraceStepIF<State> traceStepIF) {
		if (!(printTransitions || printAllStates || printSavedStates))
			return;

		TraceStep traceStep = (TraceStep) traceStepIF;
		int pid = traceStep.processIdentifier();
		int startStateId = getId(source);
		int sequenceId = 1;
		Iterator<AtomicStep> atomicStepIter = traceStep.getAtomicSteps()
				.iterator();
		State oldState = source;
		AtomicStep atomicStep;

		if (!atomicStepIter.hasNext())
			return;
		atomicStep = atomicStepIter.next();
		// print the first transition from the source state:
		if (printTransitions) {
			if (this.printAllStates)
				config.out().println();
			printTransitionPrefix(source, pid, startStateId);
			printStatement(source, atomicStep.getPostState(),
					atomicStep.getTransition());
		}
		oldState = atomicStep.getPostState();
		while (atomicStepIter.hasNext()) {
			atomicStep = atomicStepIter.next();
			if (this.printAllStates) {
				config.out().println();
				config.out().print(symbolicAnalyzer.stateToString(oldState,
						startStateId, sequenceId++));
			}
			if (printTransitions) {
				if (this.printAllStates)
					config.out().println();
				printStatement(oldState, atomicStep.getPostState(),
						atomicStep.getTransition());
			}
			oldState = atomicStep.getPostState();
		}
	}

	@Override
	public void printTraceStepFinalState(State finalState, int normalizedID) {
		boolean newState = true;

		// Print transitions:
		if (printTransitions) {
			String stateID = "--> State ";

			if (normalizedID >= 0)
				stateID += normalizedID + " ";
			stateID += finalState;
			this.config.out().println(stateID);
			config.out().flush();
		}
		// I don't like increase "numStatesExplored" here but there is no other
		// place it can be in without further modification in GMC or CIVL,
		// because it needs to know if the final state is a seen state.
		if (normalizedID >= 0) {
			if (normalizedID > MaxNormalizedId.intValue()) {
				Utils.biggerAndSet(MaxNormalizedId, normalizedID);
				numStatesExplored.getAndIncrement();
			} else
				newState = false; // a seen state
		} else
			numStatesExplored.getAndIncrement();
		// Print states:
		if (newState) {
			if (printAllStates) {
				config.out().println();
				config.out().println(symbolicAnalyzer.stateToString(finalState,
						normalizedID, -1));
			} else if (printSavedStates) {
				if (normalizedID >= 0) {
					config.out().println();
					config.out().println(symbolicAnalyzer
							.stateToString(finalState, normalizedID, -1));
				}
			}
			config.out().flush();
		}
		// Print path conditions:
		if (config.showPathConditon()) {
			String prefix = "--> State ";
			String stateID = normalizedID >= 0
					? prefix + normalizedID
					: finalState.toString();

			config.out().print(stateID);
			config.out().print(" -- path condition: ");
			if (config.showPathConditonAsOneLine())
				config.out()
						.println(finalState.getPathCondition(enabler.universe));
			else
				config.out()
						.println(this.symbolicAnalyzer.pathconditionToString(
								null, finalState, "\t",
								finalState.getPathCondition(enabler.universe)));
			config.out().flush();
		}
	}

	/* ****************** Public Methods from StateManager ***************** */

	@Override
	public long getNumStateInstances() {
		return stateFactory.getNumStateInstances();
	}

	@Override
	public int maxProcs() {
		return maxProcs.intValue();
	}

	@Override
	public void printUpdate() {
		printUpdateWork();
	}

	@Override
	public void setUpdater(Printable updater) {
		this.updater = updater;
	}

	@Override
	public int numStatesExplored() {
		return numStatesExplored.get();
	}

	@Override
	public Map<BooleanExpression, Set<Pair<State, SymbolicExpression[]>>> collectedOutputs() {
		return this.outputCollector.collectedOutputs;
	}

	@Override
	public String[] outptutNames() {
		if (outputCollector != null)
			return this.outputCollector.outptutNames;
		return null;
	}

	@Override
	public void normalize(TraceStepIF<State> traceStepIF) {
		TraceStep traceStep = (TraceStep) traceStepIF;
		State state = traceStep.getFinalState();

		try {
			if (config.saveStates() || config.collectProcesses()
					|| config.collectScopes() || config.collectHeaps()
					|| config.collectSymbolicNames() || config.simplify()) {
				Set<HeapErrorKind> ignoredErrorSet = new HashSet<>(
						ignoredHeapErrors);
				boolean finished = false;

				do {
					try {
						if (ignoredErrorSet
								.size() == HeapErrorKind.values().length)
							finished = true;
						state = stateFactory.canonic(state,
								config.collectProcesses(),
								config.collectScopes(), config.collectHeaps(),
								config.collectSymbolicNames(),
								config.simplify(), ignoredErrorSet);
						finished = true;
					} catch (CIVLHeapException hex) {
						// TODO state never gets canonicalized and then gmc
						// can't
						// figure out if it has been seen before.
						String message = "";
						String process = "p" + traceStep.processIdentifier();

						state = hex.state();
						switch (hex.heapErrorKind()) {
							case NONEMPTY :
								message = "The dyscope " + hex.dyscopeName()
										+ "(id=" + hex.dyscopeID()
										+ ") has a non-empty heap upon termination.\n";
								break;
							case UNREACHABLE :
								message = "An unreachable object (mallocID="
										+ hex.heapFieldID() + ", objectID="
										+ hex.heapObjectID()
										+ ") is detected in the heap of dyscope "
										+ hex.dyscopeName() + "(id="
										+ hex.dyscopeID() + ").\n";
								break;
							default :
						}
						message = message + "heap"
								+ symbolicAnalyzer.symbolicExpressionToString(
										hex.source(), hex.state(), null,
										hex.heapValue());
						errorLogger.logSimpleError(hex.source(), state, process,
								symbolicAnalyzer.stateInformation(hex.state()),
								hex.kind(), message);
						ignoredErrorSet.add(hex.heapErrorKind());
					}
				} while (!finished);
			} else if (config.simplify())
				state = stateFactory.simplify(state);
		} catch (UnsatisfiablePathConditionException e) {
			// Since the error has been logged, just return
			// some state with false path condition, so there
			// will be no next state...
			traceStep.setFinalState(stateFactory.addToPathcondition(state,
					traceStep.processIdentifier(), falseExpr));
		}
		traceStep.setFinalState(state);
	}
	
	@Override
	public int getPid(Transition transition) {
		return transition.pid();
	}
}
