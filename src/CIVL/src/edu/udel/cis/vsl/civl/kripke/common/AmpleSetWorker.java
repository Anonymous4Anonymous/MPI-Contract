package edu.udel.cis.vsl.civl.kripke.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants.DeadlockKind;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.kripke.IF.LibraryEnabler;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.SystemFunction;
import edu.udel.cis.vsl.civl.model.IF.contract.CompositeEvent;
import edu.udel.cis.vsl.civl.model.IF.contract.CompositeEvent.CompositeEventOperator;
import edu.udel.cis.vsl.civl.model.IF.contract.DependsEvent;
import edu.udel.cis.vsl.civl.model.IF.contract.DependsEvent.DependsEventKind;
import edu.udel.cis.vsl.civl.model.IF.contract.FunctionContract;
import edu.udel.cis.vsl.civl.model.IF.contract.MemoryEvent;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression.ExpressionKind;
import edu.udel.cis.vsl.civl.model.IF.expression.FunctionCallExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.MemoryUnitExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement.StatementKind;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryLoaderException;
import edu.udel.cis.vsl.civl.semantics.IF.MemoryUnitExpressionEvaluator;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnit;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitSet;
import edu.udel.cis.vsl.civl.state.IF.ProcessState;
import edu.udel.cis.vsl.civl.state.IF.StackEntry;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Triple;
import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * This class is responsible for computing the ample processes set at a given
 * state. It is a helper of Enabler.
 * 
 * Basic ingredients. Need to know, in a state s:
 * 
 * For each process p, what is the set of memory units that p can reach from its
 * call stack?
 * 
 * For each process p, given an enabled statement in p, what are the memory
 * units that could read/written to by that statement.
 * 
 * Questions:
 * 
 * Representation of set of memory units:
 * 
 * How much of this can be computed statically?
 * 
 * Can this information be stored in state and updated incrementally with each
 * transition?
 * 
 * <pre>
 * Fix a process <code>p</code>, computes the set of processes that have to be
 * in the ample set by examining the relation of the impact/reachable memory
 * units of the processes.
 * 
 * Impact memory unit set: all memory units to be accessed (read or written) by a
 * process <code>p</code> at a certain state <code>s</code>. Usually this
 * includes the memory units through the variables appearing in the statements 
 * (including its guard) that originates from <code>p</code>'s location at 
 * <code>s</code>.
 * 
 * Reachable memory unit set: all memory units reachable by a process
 * <code>p</code> at a certain state <code>s</code>. This includes all memory units 
 * reachable through all the variables in the dyscopes visible to <code>p</code>.
 * 
 * Reachable memory unit access annotation: for each element in the reachable memory
 * unit set, annotates the information if the process is possible to write it now or
 * in the future. Immediately, any variable appearing as the left-hand-side of
 * Note, all variables that ever appear as the operand of the address-of (&)
 * operator are to be considered as possibly written by any process. Given a memory 
 * unit <code>m</code> and a process <code>p</code>, <code>w(m, p, s)</code> is true 
 * iff <code>p</code> is possible to write to <code>m</code> from <code>s</code>.
 * 
 * Note: the heap is excluded when computing the impact/reachable memory units; 
 * memory of handle types (such as gcomm/comm, gbarrier/barrier) are ignored.
 * 
 * Ample set algorithm: 
 * 0. Let <code>amp(p)</code> be the ample set of <code>p</code>. Initially, 
 *    <code>amp(p) = { p }</code>. Let <code>work = { p }</code> be the 
 *    set of working processes.
 * 1. Let <code>sys(p, s)</code> be the set of system function calls of <code>p</code>
 * 	  origins at <code>s</code>. Let <code>imp(p, s)</code> be the impact memory set 
 *    of <code>p</code> at state <code>s</code>; remove <code>p</code> from work.
 * 2. For every system call <code>c</code> of <code>sys(p, s)</code>, obtain the ample
 *    set <code>amp(c, p, s)</code> from the corresponding library. Then, for every 
 *    <code>q</code> in <code>amp(c, p, s)</code>, perform 2.1:
 *    2.1. add <code>q</code> to <code>amp(p)</code>, and add <code>q</code> to <code>work</code> 
 *         if <code>q</code> hasn't been added to <code>work</code> before.
 * 3. For every process <code>q</code> active at state <code>s</code>, 
 *    let <code>rea(q, s)</code> be the map of reachable memory units and 
 *    the access annotation (read only or possible write) of process <code>q</code> 
 *    at state <code>s</code>, then do the following:
 *    - for every memory unit <code>m</code> in <code>imp(p, s)</code>, 
 *      find out all memory units <code>m'</code> belonging to <code>rea(q, s)</code> 
 *      that intersects with <code>m</code>;
 *    - if there exists <code>m'</code>, such that <code>w(m, p, s)</code> or
 *      <code>w(m', q, s)</code>, then perform step 2.1 for <code>q</code>.
 * 4. Repeat steps 1-3 until <code>work</code> is empty.
 * </pre>
 * 
 * The ample set worker always return the minimal ample set, i.e., the set with
 * the smallest number of processes. To achieve this, it greedily computes the
 * ample set of all active processes. Sometimes, it doesn't have to iterates
 * over all processes if it finds an ample set of size one.
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public class AmpleSetWorker {

	/* ********************************* Types ***************************** */

	/**
	 * The status of the computation of memory units: used in
	 * {@link#impactMemoryUnits}, when the result is INCOMPLETE it means that
	 * the approximation of the impact memory units not done and thus it could
	 * be anything (thus the ample set will be all processes); in contrast,
	 * NORMAL means that the computation is done and can be used to calculate
	 * the ample set.
	 * 
	 * @author Manchun Zheng (zxxxx)
	 * 
	 */

	/* *************************** Instance Fields ************************* */

	/**
	 * The value of the guards of the statements of all processes. Key is PID,
	 * value is a map of statement and the guard value. This caches the results
	 * of evaluating guards for later usage of generating new path condition, so
	 * as to avoid duplicate/redundant valid calls.
	 */
	Map<Integer, Map<Statement, BooleanExpression>> newGuardMap;

	/**
	 * The value of the guards of the statements of all processes. Index of
	 * first dimension is PID, and index of second dimension is statement id.
	 */
	BooleanExpression newGuards[][];

	/**
	 * The processes being waited for of each process. Index is PID, bit set for
	 * the PID of the processes being waited for.
	 */
	private BitSet waitMap[];

	/**
	 * Bit set for the PID of processes that has a non-empty call stack.
	 */
	private BitSet nonEmptyProcesses = new BitSet();

	/**
	 * Bit set for the PID of active processes (i.e., non-null processes with
	 * non-empty stack that have at least one enabled statements)
	 */
	private BitSet activeProcesses = new BitSet();

	/**
	 * The unique enabler used in the system. Used here for evaluating the guard
	 * of statements.
	 */
	private CommonEnabler enabler;

	/**
	 * The evaluator for memory unit expressions.
	 */
	private MemoryUnitExpressionEvaluator memUnitExprEvaluator;

	/**
	 * Turn on/off the printing of debugging information for the ample set
	 * algorithm.
	 */
	private boolean debugging = true;

	/**
	 * The output stream used for debugging.
	 */
	private PrintStream debugOut = System.out;

	/**
	 * The CIVL model factory
	 */
	private ModelFactory modelFactory;

	/**
	 * The CIVL type factory
	 */
	private CIVLTypeFactory typeFactory;

	/**
	 * Impact memory units of processes. Index is PID, content is the impact
	 * memory unit set. A null impact memory unit set means that the computation
	 * is incomplete and all active processes should be included in the ample
	 * set.
	 */
	private MemoryUnitSet[] impactMemUnits;

	/**
	 * The maximal number of live processes allowed at a state. Negative means
	 * infinite. If non-negative, then executing $spawn statements becomes
	 * dependent with other $spawn statements.
	 */
	// private int procBound = -1;

	/**
	 * map of process ID's and the set of enabled system call statements.
	 */
	private List<Set<CallOrSpawnStatement>> enabledSystemCallMap = new ArrayList<>();

	/**
	 * The current state at which the ample set is to be computed.
	 */
	private State state;

	/**
	 * A reference to the {@link Evaluator}
	 */
	private Evaluator evaluator;

	/**
	 * The symbolic utility
	 */
	private SymbolicUtility symbolicUtil;

	/**
	 * The symbolic analyzer
	 */
	private SymbolicAnalyzer symbolicAnalyzer;

	/**
	 * The symbolic universe
	 */
	private SymbolicUniverse universe;

	/**
	 * The memory unit factory for operations on memory units
	 */
	private MemoryUnitFactory memUnitFactory;

	/**
	 * The reachable memory unit sets of processes which have no pointers and
	 * are read-only. Index is PID.
	 */
	private MemoryUnitSet[] reachableNonPtrReadonly;

	/**
	 * The reachable memory unit sets of processes which have no pointers and
	 * are writable. Index is PID.
	 */
	private MemoryUnitSet[] reachableNonPtrWritable;

	/**
	 * The reachable memory unit sets of processes which have some pointers and
	 * are read-only. Index is PID.
	 */
	private MemoryUnitSet[] reachablePtrReadonly;

	/**
	 * The reachable memory unit sets of processes which have some pointers and
	 * are writable. Index is PID.
	 */
	private MemoryUnitSet[] reachablePtrWritable;

	/**
	 * processes at a location of infinite loop
	 */
	private BitSet infiniteLoopProcesses = new BitSet();

	private CIVLConfiguration config;

	/* ***************************** Constructors ************************** */

	/**
	 * Creates a new instance of ample set worker for a given state.
	 * 
	 * @param state
	 *            The state that this ample set is going to work for.
	 * @param enabler
	 *            The enabler used in the system.
	 * @param evaluator
	 *            The evaluator used in the system.
	 * @param symbolicAnalyzer
	 *            The symbolic analyzer used in the system.
	 * @param debug
	 *            The option to turn on/off the printing of debugging
	 *            information.
	 * @param debugOut
	 *            The print stream for debugging information.
	 */
	AmpleSetWorker(State state, CommonEnabler enabler, Evaluator evaluator,
			MemoryUnitFactory muFactory, CIVLConfiguration config) {
		this.evaluator = evaluator;
		this.memUnitExprEvaluator = evaluator.memoryUnitEvaluator();
		this.modelFactory = evaluator.modelFactory();
		this.typeFactory = this.modelFactory.typeFactory();
		this.state = state;
		this.enabler = enabler;
		this.symbolicUtil = evaluator.symbolicUtility();
		this.symbolicAnalyzer = evaluator.symbolicAnalyzer();
		this.universe = evaluator.universe();
		impactMemUnits = new MemoryUnitSet[state.numProcs()];
		this.memUnitFactory = muFactory;
		this.config = config;
		this.debugging = config.debug() || config.showMemoryUnits();
		this.debugOut = config.out();
	}

	/* *********************** Package-private Methods ********************* */

	/**
	 * Obtains the set of ample processes for the current state.
	 * 
	 * @return the set of ample processes and a boolean denoting if all active
	 *         processes are included
	 */
	Triple<Boolean, Boolean, Set<ProcessState>> ampleProcesses() {
		BitSet ampleProcessIDs;
		Set<ProcessState> ampleProcesses = new LinkedHashSet<>();
		Boolean containingAll = false;
		boolean visibleToDL = false;

		computeActiveProcesses();
		if (activeProcesses.cardinality() <= 1) {
			// return immediately if at most one process is activated.
			ampleProcessIDs = this.activeProcesses;
		} else {
			Pair<BitSet, Boolean> pair = ampleProcessesWork();
			ampleProcessIDs = pair.left;
			visibleToDL = pair.right;
		}
		for (int pid = 0; pid < ampleProcessIDs.length(); pid++) {
			pid = ampleProcessIDs.nextSetBit(pid);
			ampleProcesses.add(state.getProcessState(pid));
		}
		containingAll = ampleProcessIDs.cardinality() == activeProcesses
				.cardinality();
		return new Triple<>(containingAll, visibleToDL, ampleProcesses);
	}

	/* *************************** Private Methods ************************* */

	/**
	 * Computes the ample set when there are more than one active processes.
	 * When the number of active processes are greater than one, this method is
	 * called.
	 * 
	 * Fixed one process, and then find out its ample set, i.e., processes that
	 * are dependent with it. Repeat for all processes
	 * 
	 * Processes at a side-effect-free self-loop location are not chosen
	 * purposely. If all active processes are at a side-effect-free self-loop
	 * location, then the ample set is the whole enable set.
	 * 
	 * 
	 * @return The set of process ID's to be contained in the ample set.
	 */
	private Pair<BitSet, Boolean> ampleProcessesWork() {
		BitSet result = new BitSet();
		int minimalAmpleSetSize = activeProcesses.cardinality() + 1;
		int procEnterLocal = existSoleEnabledEnterLocal();

		if (procEnterLocal >= 0) {
			/*
			  For an active process, if the location it is currently at has only
			  one outgoing statement and the statement is a "local block enter",
			  this process forms an ample set:
			 */
			result.clear();
			result.set(procEnterLocal);
			return new Pair<>(result, false);
		}
		preprocessing();
		
		boolean visibleToDL = false;
		
		for (int pid = 0; pid < this.activeProcesses.length(); pid++) {
			// a set of procs the transitions of which form an ample set:
			BitSet ampleProcSet;
			int currentSize;

			visibleToDL = false;
			pid = activeProcesses.nextSetBit(pid);
			if (this.infiniteLoopProcesses.get(pid))
				continue;
			ampleProcSet = ampleSetOfProcess(pid, minimalAmpleSetSize);
			if (existProcessEnteringUnsafeAtomicBlock(ampleProcSet))
				ampleProcSet = activeProcesses;
			this.difference(ampleProcSet, infiniteLoopProcesses);
			if (!allDeadlockInvisible(ampleProcSet)) {
				ampleProcSet = activeProcesses;
				visibleToDL = true;
			}
			currentSize = ampleProcSet.cardinality();
			if (currentSize == 1)
				return new Pair<>(ampleProcSet, visibleToDL);
			if (currentSize < minimalAmpleSetSize) {
				result = ampleProcSet;
				minimalAmpleSetSize = currentSize;
			}
		}
		// TODO: it looks like the ample set can be one of the ample sets of all
		// processes, when all processes are at self-loop location. Does it
		// violate the method specification given by the doc ?
		if (result.isEmpty() && !this.infiniteLoopProcesses.isEmpty()) {
			for (int pid = 0; pid < this.infiniteLoopProcesses
					.length(); pid++) {
				BitSet ampleProcSet;
				int currentSize;

				visibleToDL = false;
				pid = infiniteLoopProcesses.nextSetBit(pid);
				ampleProcSet = ampleSetOfProcess(pid, minimalAmpleSetSize);
				if (!allDeadlockInvisible(ampleProcSet)) {
					ampleProcSet = activeProcesses;
					visibleToDL = true;
				}
				currentSize = ampleProcSet.cardinality();
				if (currentSize == 1)
					return new Pair<>(ampleProcSet, visibleToDL);
				if (currentSize < minimalAmpleSetSize) {
					result = ampleProcSet;
					minimalAmpleSetSize = currentSize;
				}
			}
		}
		return new Pair<>(result, visibleToDL);
	}

	/**
	 *
	 * @return a PID of the process which is at a location such that 1) the
	 * location only has one outgoing statement; and 2) the location is marked
	 * as {@link Location#isEntryOfLocalBlock()}
	 */
	private int existSoleEnabledEnterLocal() {
		for (int pid = 0; pid < this.activeProcesses.length(); pid++) {
			pid = activeProcesses.nextSetBit(pid);

			Location loc = state.getProcessState(pid).getLocation();

			if (loc.getNumOutgoing() == 1 && loc.isEntryOfLocalBlock())
				return pid;
		}
		return -1;
	}


	/**
	 * Checks if there exist a process in the given ample process set that is
	 * right before an entry of an atomic block and the termination of the
	 * atomic block cannot be determined.
	 * 
	 * @param ampleProcSet
	 *            An ample processes set
	 * @return True iff there exist a process in the given ample process set
	 *         that is right before an entry of an atomic block and the
	 *         termination of the atomic block cannot be determined.
	 */
	private boolean existProcessEnteringUnsafeAtomicBlock(BitSet ampleProcSet) {
		if (ampleProcSet.cardinality() < activeProcesses.cardinality()) {
			for (int apid = ampleProcSet.nextSetBit(
					0); apid >= 0; apid = ampleProcSet.nextSetBit(apid + 1)) {
				ProcessState procState = state.getProcessState(apid);
				Location currentLocation;

				if (procState.hasEmptyStack())
					continue;
				currentLocation = procState.peekStack().location();
				if (currentLocation != null
						&& currentLocation.isEntryOfUnsafeAtomic()) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * <p>
	 * The ample set enabled by the given ample process set candidate is
	 * invisible for deadlock property iff the disjunction of the guards of all
	 * transitions in it is true (for the current state and any future state).
	 * </p>
	 * 
	 * <p>
	 * We don't have to consider the situation that transitions outside of the
	 * candidate ample set may affect the evaluation of the guards because the
	 * candidate ample set has already being analyzed and guarantees that
	 * outside transitions are independent with this ones inside (por-condition:
	 * C1).
	 * </p>
	 * 
	 * 
	 * @param ampleProcsCandidate
	 *            A group of processes which is a candidate group to form an
	 *            ample set.
	 * @return True iff at least one process p in the given ample proc candidate
	 *         set satisfies one of the following conditions: <br>
	 *         <li>location of p is a binary branching location</li>
	 *         <li>location of p is at a switch or choose statement who has
	 *         default case being specified</li>
	 *         <li>at least one guard of enabled transitions of p is a literal
	 *         true or a system guard</li>
	 * 
	 * @author xxxx
	 */
	private boolean allDeadlockInvisible(BitSet ampleProcsCandidate) {
		if (config.deadlock() == DeadlockKind.NONE)
			return true; // it is not invisible but deadlock is not checking

		for (int pid = ampleProcsCandidate.nextSetBit(
				0); pid >= 0; pid = ampleProcsCandidate.nextSetBit(pid + 1)) {
			ProcessState proc = state.getProcessState(pid);
			Location location = proc.getLocation();

			// optimization, disjunction of guards at binary branching location
			// will always be true:
			if (location.isBinaryBranching()
					|| location.isSwitchOrChooseWithDefault())
				return true;
			// heuristic: if the location is in a POR-contracted function, it is
			// invisible for deadlock property:
			if (location.function().isContracted() && location.function()
					.functionContract().hasDependsClause())
				return true;
			for (Statement stmt : location.outgoing()) {
				Expression guard = stmt.guard();

				if (guard.expressionKind() == ExpressionKind.FUNC_CALL) {
					FunctionCallExpression funcCallGuard = (FunctionCallExpression) guard;
					String funcName = funcCallGuard.callStatement().function()
							.name().name();

					// TODO: temporarily hard code the hack condition, shall be
					// removed if CIVL is able to specify if a guard is
					// invisible or not:
					/*
					 * The reason these two guards are invisible is that they
					 * will never evaluate to MAYBE. Thus CIVL won't MISS a
					 * state where all guards are MAYBE if we ignore these two
					 * here.
					 */
					if (funcName.equals("$collate_complete")
							|| funcName.equals("$collate_arrived"))
						return true;
				}
				if ((guard.hasConstantValue() && guard.constantValue().isTrue())
						|| guard.expressionKind() == ExpressionKind.SYSTEM_GUARD)
					return true;
				// If the statement is a function call through a pointer,
				// evaluate it to get the function :
				if (stmt.statementKind() == StatementKind.CALL_OR_SPAWN) {
					CallOrSpawnStatement call = (CallOrSpawnStatement) stmt;

					if (call.isCall()) {
						if (call.function() != null)
							return !call.function().isSystemFunction();
						try {
							return !evaluator.evaluateFunctionIdentifier(state,
									pid, call.functionExpression(),
									call.getSource()).second.isSystemFunction();
						} catch (UnsatisfiablePathConditionException e) {
							// Evaluate a function identifier error will and
							// definitely will be reached in later execution, so
							// for here, just let it pass as invisible since
							// this path has errors.
							return true;
						}
					} else
						return true;
				}
			}
		}
		return false;
	}

	private void difference(BitSet lhs, BitSet rhs) {
		for (int i = 0; i < lhs.length(); i++) {
			i = lhs.nextSetBit(i);
			if (rhs.get(i))
				lhs.clear(i);
		}
	}

	/**
	 * Computes the ample set by fixing a certain process and looking at system
	 * calls and impact/reachable memory units.
	 * 
	 * @param startPid
	 *            The id of the process to start with.
	 * @return The set of process ID's to be contained in the ample set.
	 */
	private BitSet ampleSetOfProcess(int startPid, int minAmpleSize) {
		BitSet ampleProcessIDs = new BitSet();
		Stack<Integer> workingProcessIDs = new Stack<>();
		int myAmpleSetActiveSize = 1;

		workingProcessIDs.add(startPid);
		ampleProcessIDs.set(startPid);
		while (!workingProcessIDs.isEmpty()) {
			int pid = workingProcessIDs.pop();
			ProcessState procState = state.getProcessState(pid);
			MemoryUnitSet impactMUSet = impactMemUnits[pid];
			Set<CallOrSpawnStatement> systemCalls = this.enabledSystemCallMap
					.get(pid);

			if (impactMUSet == null) {
				ampleProcessIDs = activeProcesses;
				return ampleProcessIDs;
			}
			if (config.getProcBound() > 0) {
				for (Statement statement : procState.getLocation().outgoing()) {
					if (statement instanceof CallOrSpawnStatement) {
						CallOrSpawnStatement callOrSpawn = (CallOrSpawnStatement) statement;

						if (callOrSpawn.isSpawn()) {
							for (int otherPid = 0; otherPid < nonEmptyProcesses
									.length(); otherPid++) {
								otherPid = nonEmptyProcesses
										.nextSetBit(otherPid);
								if (otherPid == pid
										|| ampleProcessIDs.get(otherPid))
									continue;
								if (state.getProcessState(otherPid)
										.getLocation().hasSpawn()) {
									if (this.activeProcesses.get(otherPid)) {
										myAmpleSetActiveSize++;
										workingProcessIDs.add(otherPid);
										ampleProcessIDs.set(otherPid);
									} else if (!this.isWaitingFor(otherPid, pid)
											&& !state.getProcessState(otherPid)
													.hasEmptyStack()) {
										workingProcessIDs.add(otherPid);
										ampleProcessIDs.set(otherPid);
									}
									if (myAmpleSetActiveSize >= minAmpleSize
											|| myAmpleSetActiveSize == activeProcesses
													.cardinality()) {
										return this.intersects(ampleProcessIDs,
												activeProcesses);
									}
								}
							}
						}
					}
				}
			}
			if (systemCalls != null && !systemCalls.isEmpty()) {
				for (CallOrSpawnStatement call : systemCalls) {
					SystemFunction systemFunction = (SystemFunction) call
							.function();
					BitSet ampleSubSet = null;
					Scope parameterScope = systemFunction.outerScope();
					SymbolicExpression[] argumentValues = new SymbolicExpression[parameterScope
							.numVariables()];
					Evaluation eval = null;

					for (int i = 1; i < parameterScope.numVariables(); i++) {
						Expression argument = call.arguments().get(i - 1);

						if (!argument.getExpressionType().isPointerType())
							argumentValues[i] = universe.nullExpression();
						else {
							try {

								eval = this.enabler.evaluator.evaluate(state,
										pid, call.arguments().get(i - 1),
										false);
							} catch (UnsatisfiablePathConditionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							argumentValues[i] = eval.value;
							state = eval.state;
						}
					}
					ampleSubSet = ampleSetByContract(
							new Pair<>(parameterScope, argumentValues),
							ampleProcessIDs, pid, systemFunction);
					if (ampleSubSet == null) {
						try {
							LibraryEnabler lib = enabler.libraryEnabler(
									call.getSource(),
									systemFunction.getLibrary());

							ampleSubSet = lib.ampleSet(state, pid, call,
									this.reachablePtrWritable,
									this.reachablePtrReadonly,
									this.reachableNonPtrWritable,
									this.reachableNonPtrReadonly);
						} catch (LibraryLoaderException e) {
							// when neither dependency contract nor library
							// enabler is present,
							// we have to assume that the system function is
							// unsafe and all processes should be explored.
							// this is the worst case and should be always
							// avoided if possible
							return activeProcesses;
						} catch (UnsatisfiablePathConditionException e) {
							// error occur in the library enabler, returns all
							// processes as the ample set.
							ampleProcessIDs = activeProcesses;
							return ampleProcessIDs;
						}
					}
					if (ampleSubSet != null && !ampleSubSet.isEmpty()) {
						for (int amplePid = 0; amplePid < ampleSubSet
								.length(); amplePid++) {
							amplePid = ampleSubSet.nextSetBit(amplePid);
							if (amplePid != pid
									&& !ampleProcessIDs.get(amplePid)
									&& !workingProcessIDs.contains(amplePid)) {
								if (this.activeProcesses.get(amplePid)) {
									myAmpleSetActiveSize++;
									workingProcessIDs.add(amplePid);
									ampleProcessIDs.set(amplePid);
								} else if (!this.isWaitingFor(amplePid, pid)
										&& !state.getProcessState(amplePid)
												.hasEmptyStack()) {
									workingProcessIDs.add(amplePid);
									ampleProcessIDs.set(amplePid);
								}
								// early return
								if (myAmpleSetActiveSize >= minAmpleSize
										|| myAmpleSetActiveSize == activeProcesses
												.cardinality()) {
									return intersects(ampleProcessIDs,
											activeProcesses);
								}
							}
						}
					}
				}
			}
			for (int otherPid = 0; otherPid < nonEmptyProcesses
					.length(); otherPid++) {
				otherPid = nonEmptyProcesses.nextSetBit(otherPid);
				if (otherPid == pid || ampleProcessIDs.get(otherPid))
					continue;
				for (MemoryUnit mu : impactMUSet.memoryUnits()) {
					if (this.hasAccessConflict(pid, otherPid, mu)) {
						if (this.activeProcesses.get(otherPid)) {
							myAmpleSetActiveSize++;
							workingProcessIDs.add(otherPid);
							ampleProcessIDs.set(otherPid);
						} else if (!this.isWaitingFor(otherPid, pid)) {
							workingProcessIDs.add(otherPid);
							ampleProcessIDs.set(otherPid);
						}
						break;
					}
				}
				// early return
				if (myAmpleSetActiveSize >= minAmpleSize
						|| myAmpleSetActiveSize == activeProcesses
								.cardinality()) {
					return this.intersects(ampleProcessIDs, activeProcesses);
				}
			}
		}
		ampleProcessIDs = intersects(ampleProcessIDs, activeProcesses);
		return ampleProcessIDs;
	}

	/**
	 * decides the ample set of a function by its contract.
	 * 
	 * This function returns null when:
	 * <ul>
	 * <li>If the function is neither a system function nor an atomic function,
	 * returns null;</li>
	 * <li>if the contract is absent or there is no depends clause, returns
	 * null.</li>
	 * </ul>
	 * 
	 * When this function returns null, it means that there is no valid contract
	 * for dependency and the library enabler needs to be invoked for computing
	 * the ample set.
	 * 
	 * @param function
	 * @return
	 */
	private BitSet ampleSetByContract(
			Pair<Scope, SymbolicExpression[]> parameterScope,
			BitSet currentAmpleSet, int pid, SystemFunction function) {
		if (!function.isSystemFunction() && !function.isAtomicFunction())
			return null;
		if (function.name().name().equals("$comm_enqueue")
				&& function.getLibrary().equals("comm")
				&& this.config.deadlock() == DeadlockKind.POTENTIAL)
			return null;
		if (function.name().name().equals("$comm_dequeue")
				&& function.getLibrary().equals("comm")) {
			// check for wildcard
			return null;
		}

		BitSet result = new BitSet();
		FunctionContract functionContract = function.functionContract();

		if (functionContract != null) {
			if (functionContract.defaultBehavior().dependsNoact())
				return new BitSet();
			else if (functionContract.defaultBehavior()
					.numDependsEvents() > 0) {
				for (DependsEvent event : functionContract.defaultBehavior()
						.dependsEvents()) {
					result.or(this.ampleSetByDependsEvent(parameterScope,
							currentAmpleSet, pid, event));
				}
				return result;
			}
		}
		return null;
	}

	private BitSet ampleSetByDependsEvent(
			Pair<Scope, SymbolicExpression[]> parameterScope,
			BitSet currentAmpleSet, int pid, DependsEvent event) {
		DependsEventKind kind = event.dependsEventKind();
		BitSet result = new BitSet();

		switch (kind) {
			case READ :
			case WRITE :
			case REACH : {
				MemoryEvent memoryEvent = (MemoryEvent) event;
				Set<Expression> mus = memoryEvent.memoryUnits();

				result = ampleSetByMemoryEvent(parameterScope, currentAmpleSet,
						pid, kind, mus);
				break;
			}
			case CALL :
				break;
			case COMPOSITE : {
				CompositeEvent compositeEvent = (CompositeEvent) event;
				CompositeEventOperator operator = compositeEvent.operator();
				BitSet right = this.ampleSetByDependsEvent(parameterScope,
						currentAmpleSet, pid, compositeEvent.right());

				result = this.ampleSetByDependsEvent(parameterScope,
						currentAmpleSet, pid, compositeEvent.left());
				switch (operator) {
					case UNION :
						result.or(right);
						break;
					case DIFFERENCE :
						result.xor(right);
						break;
					case INTERSECT :
						result.and(right);
						break;
					default :
						throw new CIVLUnimplementedFeatureException(
								"unknown composite event operator: "
										+ operator);
				}
				break;
			}
			case ANYACT :
				result = this.activeProcesses;
				break;
			default :
				throw new CIVLUnimplementedFeatureException(
						"unknown kind of depends evnet: " + kind);
		}
		return result;
	}

	/**
	 * computes the ample set of a given process by some memory event, which
	 * could be either READ, WRITE or REACH.
	 * 
	 * @param pid
	 * @param kind
	 * @param mus
	 * @return
	 */
	private BitSet ampleSetByMemoryEvent(
			Pair<Scope, SymbolicExpression[]> parameterScope,
			BitSet currentAmpleSet, int pid, DependsEventKind kind,
			Set<Expression> muExprs) {
		BitSet result = new BitSet();
		MemoryUnitSet criticalMuSet = this.memUnitFactory.newMemoryUnitSet();

		for (Expression muExpr : muExprs) {
			try {
				criticalMuSet = memUnitFactory.union(criticalMuSet,
						this.memUnitExprEvaluator.evaluateMemoryUnit(state,
								parameterScope, pid, muExpr));
			} catch (UnsatisfiablePathConditionException ex) {
				// ignore the exception
			}
		}
		if (criticalMuSet.isEmpty())
			return result;
		switch (kind) {
			case READ :
				break;
			case WRITE :
				break;
			case REACH : {
				for (int thatPid = 0; thatPid < this.nonEmptyProcesses
						.length(); thatPid++) {
					thatPid = nonEmptyProcesses.nextSetBit(thatPid);
					if (thatPid == pid || currentAmpleSet.get(thatPid))
						continue;
					if (memUnitFactory.isJoint(criticalMuSet,
							this.reachableNonPtrReadonly[thatPid])
							|| memUnitFactory.isJoint(criticalMuSet,
									this.reachableNonPtrWritable[thatPid])
							|| memUnitFactory.isJoint(criticalMuSet,
									this.reachablePtrReadonly[thatPid])
							|| memUnitFactory.isJoint(criticalMuSet,
									this.reachablePtrWritable[thatPid]))
						result.set(thatPid);
				}
				break;
			}
			default :
				throw new CIVLUnimplementedFeatureException(
						"unknown memory event kind " + kind);
		}
		return result;
	}

	// is pid1 waiting for pid2?
	/**
	 * Returns true iff process pid1 is waiting for process pid2 at the current
	 * state.
	 * 
	 * @param pid1
	 * @param pid2
	 * @return
	 */
	private boolean isWaitingFor(int pid1, int pid2) {
		if (this.waitMap[pid1] != null)
			return waitMap[pid1].get(pid2);
		else {
			Set<CallOrSpawnStatement> systemCalls1 = this.enabledSystemCallMap
					.get(pid1);

			if (systemCalls1 != null && systemCalls1.size() == 1) {
				for (CallOrSpawnStatement call : systemCalls1) {
					SystemFunction systemFunction = (SystemFunction) call
							.function();

					if ((systemFunction.name().name().equals("$wait")
							|| systemFunction.name().name().equals("$waitall"))
							&& systemFunction.getLibrary().equals("civlc")) {
						BitSet ampleSubSet;

						try {
							LibraryEnabler lib = enabler.libraryEnabler(
									call.getSource(),
									systemFunction.getLibrary());

							ampleSubSet = lib.ampleSet(state, pid1, call,
									this.reachablePtrWritable,
									this.reachablePtrReadonly,
									this.reachableNonPtrWritable,
									this.reachableNonPtrReadonly);
							this.waitMap[pid1] = ampleSubSet;
						} catch (LibraryLoaderException e) {
							throw new CIVLInternalException(
									"This is unreachable because the earlier execution "
											+ "has already checked that the library enabler "
											+ "gets loaded successfully otherwise an error should have been reported there",
									call.getSource());
						} catch (UnsatisfiablePathConditionException e) {
							return false;
						}
						if (ampleSubSet.get(pid2))
							return true;
					}
				}
			}
		}
		return false;
	}

	BitSet intersects(BitSet set1, BitSet set2) {
		BitSet result = new BitSet();

		for (int i = 0; i < set1.length(); i++) {
			i = set1.nextSetBit(i);
			if (set2.get(i))
				result.set(i);
		}
		return result;
	}

	/**
	 * Computes active processes at the current state, i.e., non-null processes
	 * with non-empty stack that have at least one enabled statements.
	 */
	private void computeActiveProcesses() {
		this.newGuardMap = new HashMap<>();
		this.newGuards = new BooleanExpression[state.numProcs()][];
		for (ProcessState p : state.getProcessStates()) {
			boolean active = false;
			int pid;
			Map<Statement, BooleanExpression> myGuards = new HashMap<>();
			Location location;
			int numOutgoing;

			if (p == null || p.hasEmptyStack())
				continue;
			pid = p.getPid();
			this.nonEmptyProcesses.set(pid);
			location = p.getLocation();
			if (location.isBinaryBranching()) {
				active = true;
			} else {
				numOutgoing = location.getNumOutgoing();
				newGuards[pid] = new BooleanExpression[location
						.getNumOutgoing()];
				for (int i = 0; i < numOutgoing; i++) {
					Statement s = location.getOutgoing(i);
					BooleanExpression myGuard;

					if (config.getProcBound() > 0
							&& s instanceof CallOrSpawnStatement
							&& ((CallOrSpawnStatement) s).isSpawn()
							&& state.numLiveProcs() >= config.getProcBound())
						continue;
					myGuard = enabler.getGuard(s, pid, state);
					newGuards[pid][i] = myGuard;
					if (!myGuard.isFalse())
						active = true;
					myGuards.put(s, myGuard);
					if (active)
						break;
				}
			}
			if (active) {
				activeProcesses.set(pid);
				this.newGuardMap.put(pid, myGuards);
				if (p.getLocation().isInNoopLoop())
					this.infiniteLoopProcesses.set(pid);
			}
		}
	}

	// private boolean isInfiniteLoopLocation(Location location) {
	// if (location.getNumOutgoing() == 1) {
	// Statement outgoing = location.getOutgoing(0);
	//
	// if (outgoing instanceof NoopStatement) {
	// if (outgoing.source().id() == outgoing.target().id())
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * Computes the impact memory units of a certain process at the current
	 * state, which are usually decided by the variables appearing in the
	 * statements (including guards) originating at the process's current
	 * location. The computation could be incomplete when there is atomic/atom
	 * block that contains function calls.
	 * 
	 * @param proc
	 *            The process whose impact memory units are to be computed.
	 * @return The impact memory units of the process and the status to denote
	 *         if the computation is complete.
	 */
	// private Pair<MemoryUnitsStatus, Set<SymbolicExpression>>
	// impactMemoryUnits(
	// ProcessState proc) {
	// Set<SymbolicExpression> memUnits = new HashSet<>();
	// int pid = proc.getPid();
	// Location pLocation = proc.getLocation();
	// Pair<MemoryUnitsStatus, Set<SymbolicExpression>> partialResult;
	// Pair<MemoryUnitsStatus, Set<SymbolicExpression>> result = null;
	//
	// // this.enabledSystemCallMap.put(pid, new
	// // HashSet<CallOrSpawnStatement>());
	// if (debugging)
	// debugOut.println("impact memory units of " + proc.name() + "(id="
	// + proc.getPid() + "):");
	// if (pLocation.enterAtom() || pLocation.enterAtomic()
	// || proc.atomicCount() > 0)
	// // special handling of atomic blocks
	// result = impactMemoryUnitsOfAtomicFragment(pLocation, pid);
	// else {
	// for (Statement s : pLocation.outgoing()) {
	// try {
	// partialResult = impactMemoryUnitsOfStatement(s, pid);
	// if (partialResult.left == MemoryUnitsStatus.INCOMPLETE) {
	// result = partialResult;
	// break;
	// }
	// memUnits.addAll(partialResult.right);
	// } catch (UnsatisfiablePathConditionException e) {
	// continue;
	// }
	// }
	// }
	// if (result == null)
	// result = new Pair<>(MemoryUnitsStatus.NORMAL, memUnits);
	// if (debugging)
	// if (result.left == MemoryUnitsStatus.INCOMPLETE)
	// debugOut.println("INCOMPLETE");
	// else {
	// CIVLSource source = pLocation.getSource();
	//
	// for (SymbolicExpression memUnit : result.right) {
	// debugOut.print(symbolicAnalyzer.symbolicExpressionToString(
	// source, state, memUnit) + "\t");
	// }
	// debugOut.println();
	// }
	// return result;
	// }

	/**
	 * Computes the set of impact memory units of an atomic or atom block. All
	 * system function bodies are assumed to be independent (only the arguments
	 * are taken for computation). If there is any normal function calls, then
	 * the computation is terminated immediately and an empty set is returned
	 * with the INCOMPLETE status. This implementation is chosen because
	 * checking the impact memory units of function calls could be expensive and
	 * complicated and would be not worthy.
	 * 
	 * @param location
	 *            The start location of the atomic/atom block.
	 * @param pid
	 *            The ID of the current process.
	 * @return The set of impact memory units of the atomic/atom block and a
	 *         status variable to denote if the computation can be done
	 *         completely.
	 */
	private MemoryUnitSet impactMemoryUnitsOfAtomicFragment(Location location,
			int pid) {
		int atomicCount = state.getProcessState(pid).atomicCount();
		Set<CallOrSpawnStatement> systemCalls = new HashSet<>();
		BitSet checkedLocationIDs = new BitSet();
		Stack<Location> workings = new Stack<Location>();
		// Set<SymbolicExpression> memUnits = new HashSet<>();
		MemoryUnitSet muSet = this.memUnitFactory.newMemoryUnitSet();

		assert atomicCount > 0;
		workings.add(location);
		// DFS searching for reachable statements inside the $atomic/$atom
		// block
		while (!workings.isEmpty()) {
			Location currentLocation = workings.pop();
			Set<MemoryUnitExpression> impactMemUnitExprs = currentLocation
					.impactMemUnits();

			if (impactMemUnitExprs == null)
				return null;
			checkedLocationIDs.set(currentLocation.id());
			if (currentLocation.enterAtomic())
				atomicCount++;
			if (currentLocation.leaveAtomic())
				atomicCount--;
			if (atomicCount == 0 && !currentLocation.enterAtomic()) {
				atomicCount++;
				continue;
			}
			systemCalls.addAll(currentLocation.systemCalls());
			for (MemoryUnitExpression memUnitExpr : impactMemUnitExprs) {
				try {
					muSet = this.memUnitExprEvaluator.evaluates(state, pid,
							memUnitExpr, muSet);
				} catch (UnsatisfiablePathConditionException e) {
					// do nothing
				}
			}
			for (Statement statement : currentLocation.outgoing()) {
				if (statement.target() != null) {
					if (!checkedLocationIDs.get(statement.target().id())) {
						workings.push(statement.target());
					}
				}
			}
		}
		this.enabledSystemCallMap.set(pid, systemCalls);
		return muSet;
	}

	private MemoryUnitSet impactMemoryUnitsOfProcess(int pid) {
		Location location = state.getProcessState(pid).getLocation();
		Set<MemoryUnitExpression> impactMemUnitExprs = location
				.impactMemUnits();
		MemoryUnitSet impactMemUnits = this.memUnitFactory.newMemoryUnitSet();

		if (state.getProcessState(pid).atomicCount() > 0)
			return this.impactMemoryUnitsOfAtomicFragment(location, pid);
		if (impactMemUnitExprs == null)
			return null;
		this.enabledSystemCallMap.set(pid, location.systemCalls());
		for (MemoryUnitExpression memUnitExpr : impactMemUnitExprs) {
			// Set<SymbolicExpression> subResult = new HashSet<>();

			try {
				impactMemUnits = this.memUnitExprEvaluator.evaluates(state, pid,
						memUnitExpr, impactMemUnits);
			} catch (UnsatisfiablePathConditionException e) {
				// do nothing
			}
			// impactMemUnits.addAll(subResult);
		}
		return impactMemUnits;
	}

	/**
	 * Computes the impact memory units of a given statement of a certain
	 * process at the current state.
	 * 
	 * @param statement
	 *            The statement whose impact memory units are to be computed.
	 * @param pid
	 *            The id of the process that owns the statement.
	 * @return the impact memory units of the statement
	 * @throws UnsatisfiablePathConditionException
	 */
	/**
	 * Computes the set of memory units accessed by a given expression of a
	 * certain process at the current state.
	 * 
	 * @param expression
	 *            The expression whose impact memory units are to be computed.
	 * @param pid
	 *            The id of the process that the expression belongs to.
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */

	/**
	 * Pre-processing for ample set computation, including:
	 * <ul>
	 * <li>Computes the impact memory units for each process; and</li>
	 * <li>Computes the reachable memory units for each process.</li>
	 * </ul>
	 */
	private void preprocessing() {
		this.waitMap = new BitSet[state.numProcs()];
		if (debugging) {
			debugOut.println("===============memory analysis at state "
					+ state.toString() + "================");
		}
		for (int i = 0; i < state.numProcs(); i++) {
			this.enabledSystemCallMap.add(null);
		}
		reachableMemoryAnalysis();
		for (int pid = 0; pid < nonEmptyProcesses.length(); pid++) {
			pid = nonEmptyProcesses.nextSetBit(pid);
			// reachableMemoryUnits = state.getReachableMemUnitsWoPointer(pid);
			// reachableMemoryUnits.putAll(state
			// .getReachableMemUnitsWtPointer(pid));
			// impactMemUnitsMap.put(pid, this.impactMemoryUnitsOfProcess(pid));
			this.impactMemUnits[pid] = this.impactMemoryUnitsOfProcess(pid);
			// reachableMemUnitsMap.put(pid, reachableMemoryUnits);
			if (debugging) {
				debugOut.println("impact memory units of process " + pid + ":");
				this.printMemoryUnitSet(debugOut, this.impactMemUnits[pid]);
				// this.impactMemUnits[pid].print(debugOut);
				debugOut.println();
				// debugOut.println("reachable memory units of process " + pid
				// + ":");
				// for (Map.Entry<SymbolicExpression, Boolean> entry :
				// this.reachableMemUnitsMap
				// .get(pid).entrySet()) {
				// debugOut.print(entry.getKey());
				// if (entry.getValue())
				// debugOut.print(" (w)");
				// debugOut.print("\t");
				// }
				// debugOut.println();
			}
		}
		if (debugging) {
			// enabler.stateFactory.;
			this.printReachableMemoryUnits();
		}
	}

	private void printMemoryUnitSet(PrintStream out, MemoryUnitSet muSet) {
		int i = 0;

		out.print("{");
		for (MemoryUnit mu : muSet.memoryUnits()) {
			if (i != 0)
				out.print(", ");
			out.print(this.symbolicAnalyzer.memoryUnitToString(state, mu));
			i++;
		}
		out.print("}");
	}

	// /**
	// * Given a process, computes the set of reachable memory units and if the
	// * memory unit could be modified at the current location or any future
	// * location.
	// *
	// * @param proc
	// * The process whose reachable memory units are to be computed.
	// * @return A map of reachable memory units and if they could be modified
	// by
	// * the process. //
	// */
	// private Map<SymbolicExpression, Boolean> reachableMemoryUnits(
	// ProcessState proc) {
	// Set<Integer> checkedDyScopes = new HashSet<>();
	// Map<SymbolicExpression, Boolean> memUnitPermissionMap = new HashMap<>();
	// Set<Variable> writableVariables = proc.getLocation()
	// .writableVariables();
	// // only look at the top stack is sufficient
	// StackEntry callStack = proc.peekStack();
	// int dyScopeID = callStack.scope();
	// String process = "p" + proc.identifier() + " (id = " + proc.getPid()
	// + ")";
	//
	// if (debugging)
	// debugOut.println("reachable memory units of " + proc.name()
	// + "(id=" + proc.getPid() + "):");
	// while (dyScopeID >= 0) {
	// if (checkedDyScopes.contains(dyScopeID))
	// break;
	// else {
	// DynamicScope dyScope = state.getDyscope(dyScopeID);
	// int size = dyScope.numberOfValues();
	//
	// for (int vid = 0; vid < size; vid++) {
	// Variable variable = dyScope.lexicalScope().variable(vid);
	// Set<SymbolicExpression> varMemUnits;
	// boolean permission;
	//
	// // ignore the heap
	// if (variable.type().isHeapType())// && vid != 0)
	// continue;
	// if (variable.hasPointerRef())
	// varMemUnits = evaluator
	// .memoryUnitsReachableFromVariable(
	// variable.type(), dyScope.getValue(vid),
	// dyScopeID, vid, state, process);
	// else {
	// varMemUnits = new HashSet<SymbolicExpression>(1);
	// varMemUnits.add(evaluator.symbolicUtility()
	// .makePointer(
	// dyScopeID,
	// vid,
	// evaluator.universe()
	// .identityReference()));
	// }
	// permission = writableVariables.contains(variable) ? true
	// : false;
	// for (SymbolicExpression unit : varMemUnits) {
	// if (!memUnitPermissionMap.containsKey(unit)) {
	// memUnitPermissionMap.put(unit, permission);
	// }
	// }
	// }
	// checkedDyScopes.add(dyScopeID);
	// dyScopeID = state.getParentId(dyScopeID);
	// }
	// }
	// if (debugging) {
	// CIVLSource source = proc.getLocation().getSource();
	//
	// for (SymbolicExpression memUnit : memUnitPermissionMap.keySet()) {
	// debugOut.print(symbolicAnalyzer.symbolicExpressionToString(
	// source, state, memUnit));
	// debugOut.print("(");
	// if (memUnitPermissionMap.get(memUnit))
	// debugOut.print("W");
	// else
	// debugOut.print("R");
	// debugOut.print(")\t");
	// }
	// debugOut.println();
	// }
	// return memUnitPermissionMap;
	// }

	private void printReachableMemoryUnits() {
		for (int i = 0; i < state.numProcs(); i++) {
			ProcessState proc = state.getProcessState(i);
			// ImmutableState theState = (ImmutableState) state;

			if (proc == null || proc.hasEmptyStack())
				continue;
			debugOut.println(
					"reachable memory units (non-ptr, readonly) of process " + i
							+ ":");
			this.printMemoryUnitSet(debugOut, reachableNonPtrReadonly[i]);
			// reachableNonPtrReadonly[i].print(debugOut);
			debugOut.println();
			debugOut.println(
					"reachable memory units (non-ptr, writable) of process " + i
							+ ":");
			printMemoryUnitSet(debugOut, reachableNonPtrWritable[i]);
			// reachableNonPtrWritable[i].print(debugOut);
			debugOut.println();
			debugOut.println(
					"reachable memory units (ptr, readonly) of process " + i
							+ ":");
			// reachablePtrReadonly[i].print(debugOut);
			printMemoryUnitSet(debugOut, reachablePtrReadonly[i]);
			debugOut.println();
			debugOut.println(
					"reachable memory units (ptr, writable) of process " + i
							+ ":");
			// reachablePtrWritable[i].print(debugOut);
			printMemoryUnitSet(debugOut, reachablePtrWritable[i]);
			debugOut.println();
		}
	}

	private void reachableMemoryAnalysis() {
		int numProcs = state.numProcs();
		ReferenceExpression identity = universe.identityReference();

		this.reachableNonPtrReadonly = new MemoryUnitSet[numProcs];
		this.reachableNonPtrWritable = new MemoryUnitSet[numProcs];
		this.reachablePtrReadonly = new MemoryUnitSet[numProcs];
		this.reachablePtrWritable = new MemoryUnitSet[numProcs];
		for (int pid = 0; pid < numProcs; pid++) {
			Set<Variable> writableVars = new HashSet<>();
			ProcessState process = state.getProcessState(pid);
			Set<MemoryUnitExpression> reachableNonPtrExpr = new HashSet<>(),
					reachablePtrExpr = new HashSet<>();
			MemoryUnitSet nonPtrReadonly = memUnitFactory.newMemoryUnitSet(),
					nonPtrWritable = memUnitFactory.newMemoryUnitSet(),
					ptrReadonly = memUnitFactory.newMemoryUnitSet(),
					ptrWritable = memUnitFactory.newMemoryUnitSet();

			if (process != null && !process.hasEmptyStack())
				for (StackEntry call : process.getStackEntries()) {
					Location location = call.location();

					writableVars.addAll(location.writableVariables());
					for (MemoryUnitExpression memUnit : location
							.reachableMemUnitsWtPointer()) {
						if (memUnit.writable())
							reachablePtrExpr.remove(memUnit);
						reachablePtrExpr.add(memUnit);
					}
					for (MemoryUnitExpression memUnit : location
							.reachableMemUnitsWoPointer()) {
						if (memUnit.writable())
							reachableNonPtrExpr.remove(memUnit);
						reachableNonPtrExpr.add(memUnit);
					}
				}
			for (MemoryUnitExpression memUnitExpr : reachablePtrExpr) {
				int dyscopeID = state.getDyscope(pid, memUnitExpr.scopeId());
				int varID = memUnitExpr.variableId();
				MemoryUnit mu = memUnitFactory.newMemoryUnit(dyscopeID, varID,
						identity);
				SymbolicExpression varValue = state.getVariableValue(dyscopeID,
						varID);

				if (writableVars.contains(memUnitExpr.variable())) {
					// ptrWritable =
					memUnitFactory.add(ptrWritable, mu);
					// ptrWritable =
					findPointersInExpression(varValue, ptrWritable, state);
				} else {
					// ptrReadonly =
					memUnitFactory.add(ptrReadonly, mu);
					// ptrReadonly =
					findPointersInExpression(varValue, ptrReadonly, state);
				}
			}
			for (MemoryUnitExpression memUnitExpr : reachableNonPtrExpr) {
				int dyscopeID = state.getDyscope(pid, memUnitExpr.scopeId());
				int varID = memUnitExpr.variableId();
				MemoryUnit mu = memUnitFactory.newMemoryUnit(dyscopeID, varID,
						identity);
				// Variable variable = memUnitExpr.variable();

				// if (variable.type().isHandleType()) {
				// SymbolicExpression value = state.getVariableValue(
				// dyscopeID, varID);
				// CIVLSource source = variable.getSource();
				//
				// if (!value.isNull()
				// && symbolicAnalyzer
				// .isDerefablePointer(state, value).right == ResultType.YES)
				// memUnitFactory.add(nonPtrReadonly, memUnitFactory
				// .newMemoryUnit(symbolicUtil.getDyscopeId(
				// source, value), symbolicUtil
				// .getVariableId(source, value),
				// symbolicUtil.getSymRef(value)));
				// }
				if (writableVars.contains(memUnitExpr.variable()))
					memUnitFactory.add(nonPtrWritable, mu);
				else
					memUnitFactory.add(nonPtrReadonly, mu);
			}
			reachableNonPtrReadonly[pid] = nonPtrReadonly;
			reachableNonPtrWritable[pid] = nonPtrWritable;
			reachablePtrReadonly[pid] = ptrReadonly;
			reachablePtrWritable[pid] = ptrWritable;
		}
	}

	private boolean hasAccessConflict(int thisPid, int thatPid, MemoryUnit mu) {
		MemoryUnitSet reachablePtrWritableThat = this.reachablePtrWritable[thatPid];
		MemoryUnitSet reachablePtrReadonlyThat = this.reachablePtrReadonly[thatPid];
		MemoryUnitSet reachableNonPtrWritableThat = this.reachableNonPtrWritable[thatPid];
		MemoryUnitSet reachableNonPtrReadonlyThat = this.reachableNonPtrReadonly[thatPid];
		MemoryUnitSet reachablePtrWritableThis = this.reachablePtrWritable[thisPid];
		MemoryUnitSet reachablePtrReadonlyThis = this.reachablePtrReadonly[thisPid];
		MemoryUnitSet reachableNonPtrWritableThis = this.reachableNonPtrWritable[thisPid];
		MemoryUnitSet reachableNonPtrReadonlyThis = this.reachableNonPtrReadonly[thisPid];
		boolean thisRead = false, thisWrite = false, thatRead = false,
				thatWrite = false;

		if (memUnitFactory.isJoint(reachablePtrWritableThis, mu)
				|| memUnitFactory.isJoint(reachableNonPtrWritableThis, mu))
			thisWrite = true;
		else if (memUnitFactory.isJoint(reachablePtrReadonlyThis, mu)
				|| memUnitFactory.isJoint(reachableNonPtrReadonlyThis, mu))
			thisRead = true;
		if (memUnitFactory.isJoint(reachablePtrWritableThat, mu)
				|| memUnitFactory.isJoint(reachableNonPtrWritableThat, mu))
			thatWrite = true;
		else if (memUnitFactory.isJoint(reachablePtrReadonlyThat, mu)
				|| memUnitFactory.isJoint(reachableNonPtrReadonlyThat, mu))
			thatRead = true;
		if ((thisWrite && thatRead) || (thisRead && thatWrite)
				|| (thisWrite && thatWrite))
			return true;
		return false;
	}

	/**
	 * Finds pointers contained in a given expression recursively.
	 * 
	 * @param expr
	 * @param set
	 * @param state
	 */
	private void findPointersInExpression(SymbolicExpression expr,
			MemoryUnitSet muSet, State state) {
		SymbolicType type = expr.type();
		MemoryUnitSet result = muSet;

		if (type != null && !type.equals(typeFactory.heapSymbolicType())
				&& !type.equals(typeFactory.bundleSymbolicType())) {
			// need to eliminate heap type as well. each proc has its own.
			if (typeFactory.pointerSymbolicType().equals(type)) {
				SymbolicExpression pointerValue;
				SymbolicExpression eval;
				Variable variable;

				if (expr.operator() != SymbolicOperator.TUPLE)
					return;

				int dyscopeid = evaluator.stateFactory()
						.getDyscopeId(symbolicUtil.getScopeValue(expr));

				if (dyscopeid < 0)
					return;

				variable = state.getDyscope(dyscopeid).lexicalScope()
						.variable(symbolicUtil.getVariableId(null, expr));
				if (variable.isConst() || variable.isInput())
					return;
				this.memUnitFactory.add(result, expr, evaluator.stateFactory());
				if (expr.operator() == SymbolicOperator.TUPLE) {
					/*
					 * If the expression is an arrayElementReference expression,
					 * and finally it turns that the array type has length 0,
					 * return immediately. Because we can not dereference it and
					 * the dereference exception shouldn't report here.
					 */
					if (symbolicUtil.getSymRef(expr)
							.isArrayElementReference()) {
						SymbolicExpression arrayPointer = symbolicUtil
								.parentPointer(expr);

						try {
							eval = this.dereference(state, arrayPointer);
						} catch (SARLException ex) {
							return;
						}
						/* Check if it's length == 0 */
						if (eval == null || universe.length(eval).isZero())
							return;
					}
					pointerValue = this.dereference(state, expr);
					// TODO what's this?
					if (pointerValue == null)
						return;
					if (pointerValue.operator() == SymbolicOperator.TUPLE
							&& pointerValue.type() != null
							&& pointerValue.type()
									.equals(typeFactory.pointerSymbolicType()))
						findPointersInExpression(pointerValue, result, state);
				}
			} else {
				int numArgs = expr.numArguments();

				for (int i = 0; i < numArgs; i++) {
					SymbolicObject arg = expr.argument(i);

					findPointersInObject(arg, result, state);
				}
			}
		}
		return;
	}

	/**
	 * Finds all the pointers that can be dereferenced inside a symbolic object.
	 * 
	 * @param object
	 *            a symbolic object
	 * @param set
	 *            a set to which the pointer values will be added
	 * @param heapType
	 *            the heap type, which will be ignored
	 */
	private void findPointersInObject(SymbolicObject object,
			MemoryUnitSet muSet, State state) {
		switch (object.symbolicObjectKind()) {
			case EXPRESSION : {
				findPointersInExpression((SymbolicExpression) object, muSet,
						state);
				return;
			}
			case SEQUENCE : {
				MemoryUnitSet result = muSet;

				for (SymbolicExpression expr : (SymbolicSequence<?>) object)
					findPointersInExpression(expr, result, state);

				return;
			}
			default :
				// ignore types and primitives, they don't have any pointers
				// you can dereference.
		}
		return;
	}

	/**
	 * 
	 * @param state
	 * @param pointer
	 * @return
	 */
	private SymbolicExpression dereference(State state,
			SymbolicExpression pointer) {
		int sid = evaluator.stateFactory()
				.getDyscopeId(symbolicUtil.getScopeValue(pointer));
		int vid = symbolicUtil.getVariableId(null, pointer);
		ReferenceExpression symRef = symbolicUtil.getSymRef(pointer);
		SymbolicExpression variableValue;
		SymbolicExpression deref;

		variableValue = state.getDyscope(sid).getValue(vid);
		try {
			deref = universe.dereference(variableValue, symRef);
		} catch (Exception e) {
			return null;
		}
		return deref;
	}

}
