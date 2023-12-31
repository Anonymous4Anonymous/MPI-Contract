package edu.udel.cis.vsl.civl.library.common;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.kripke.IF.Enabler;
import edu.udel.cis.vsl.civl.kripke.IF.LibraryEnabler;
import edu.udel.cis.vsl.civl.kripke.IF.LibraryEnablerLoader;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.Semantics;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.semantics.IF.Transition.AtomicLockAction;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitSet;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * This class provides the common data and operations of library enablers.
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public abstract class BaseLibraryEnabler extends LibraryComponent
		implements
			LibraryEnabler {

	/* *************************** Instance Fields ************************* */

	/**
	 * The enabler for normal CIVL execution.
	 */
	protected Enabler primaryEnabler;

	/**
	 * The state factory for state-related computation.
	 */
	protected StateFactory stateFactory;

	protected LibraryEnablerLoader libEnablerLoader;

	protected MemoryUnitFactory memUnitFactory;

	/* ***************************** Constructor *************************** */

	/**
	 * Creates a new instance of library enabler.
	 * 
	 * @param primaryEnabler
	 *            The enabler for normal CIVL execution.
	 * @param modelFactory
	 *            The model factory of the system.
	 * @param symbolicUtil
	 *            The symbolic utility used in the system.
	 * @param symbolicAnalyzer
	 *            The symbolic analyzer used in the system.
	 */
	public BaseLibraryEnabler(String name, Enabler primaryEnabler,
			Evaluator evaluator, ModelFactory modelFactory,
			SymbolicUtility symbolicUtil, SymbolicAnalyzer symbolicAnalyzer,
			CIVLConfiguration civlConfig, LibraryEnablerLoader libEnablerLoader,
			LibraryEvaluatorLoader libEvaluatorLoader) {
		super(name, evaluator.universe(), symbolicUtil, symbolicAnalyzer,
				civlConfig, libEvaluatorLoader, modelFactory,
				evaluator.errorLogger(), evaluator);
		this.primaryEnabler = primaryEnabler;
		this.stateFactory = evaluator.stateFactory();
		this.memUnitFactory = stateFactory.memUnitFactory();

	}

	/* ********************* Methods from LibraryEnabler ******************* */

	@Override
	public BitSet ampleSet(State state, int pid, CallOrSpawnStatement statement,
			MemoryUnitSet[] reachablePtrWritableMap,
			MemoryUnitSet[] reachablePtrReadonlyMap,
			MemoryUnitSet[] reachableNonPtrWritableMap,
			MemoryUnitSet[] reachableNonPtrReadonlyMap)
			throws UnsatisfiablePathConditionException {
		return new BitSet(0);
	}

	@Override
	public List<Transition> enabledTransitions(State state,
			CallOrSpawnStatement call, BooleanExpression clause, int pid,
			AtomicLockAction atomicLockAction)
			throws UnsatisfiablePathConditionException {
		List<Transition> localTransitions = new LinkedList<>();

		localTransitions.add(
				Semantics.newTransition(pid, clause, call, atomicLockAction));
		return localTransitions;
	}

	/**
	 * Computes the ample set by analyzing the given handle object for the
	 * statement.
	 * 
	 * @param state
	 *            The current state
	 * @param pid
	 *            The pid of the process
	 * @param handleObj
	 *            The expression of the given handle object
	 * @param handleObjValue
	 *            The symbolic expression of the given handle object
	 * @param reachableMemUnitsMap
	 *            The map contains all reachable memory units of all processes
	 * @return
	 */
	protected BitSet computeAmpleSetByHandleObject(State state, int pid,
			Expression handleObj, SymbolicExpression handleObjValue,
			MemoryUnitSet[] reachablePtrWritableMap,
			MemoryUnitSet[] reachablePtrReadonlyMap,
			MemoryUnitSet[] reachableNonPtrWritableMap,
			MemoryUnitSet[] reachableNonPtrReadonlyMap) {
		MemoryUnitSet handleObjMemUnits = memUnitFactory.newMemoryUnitSet();
		BitSet ampleSet = new BitSet();
		int numProcs = state.numProcs();
		// Evaluation eval = evaluator.evaluate(state, pid, handleObj);
		CIVLSource source = handleObj.getSource();

		handleObjMemUnits.add(memUnitFactory.newMemoryUnit(
				stateFactory.getDyscopeId(
						symbolicUtil.getScopeValue(handleObjValue)),
				symbolicUtil.getVariableId(source, handleObjValue),
				symbolicUtil.getSymRef(handleObjValue)));
		// try {
		// handleObjMemUnits = evaluator.memoryUnitsOfExpression(state, pid,
		// handleObj, handleObjMemUnits);
		//
		// dd
		//
		// } catch (UnsatisfiablePathConditionException e) {
		// memUnitFactory.add(handleObjMemUnits, handleObjValue);
		// // handleObjMemUnits.add(handleObjValue);
		// }
		for (int otherPid = 0; otherPid < numProcs; otherPid++) {
			if (otherPid == pid || ampleSet.get(otherPid))
				continue;
			else {
				MemoryUnitSet reachablePtrWritable = reachablePtrWritableMap[otherPid];
				MemoryUnitSet reachableNonPtrWritable = reachableNonPtrWritableMap[otherPid];
				MemoryUnitSet reachablePtrReadonly = reachablePtrReadonlyMap[otherPid];
				MemoryUnitSet reachableNonPtrReadonly = reachableNonPtrReadonlyMap[otherPid];

				if (memUnitFactory.isJoint(handleObjMemUnits,
						reachablePtrWritable)
						|| memUnitFactory.isJoint(handleObjMemUnits,
								reachableNonPtrWritable)
						|| memUnitFactory.isJoint(handleObjMemUnits,
								reachablePtrReadonly)
						|| memUnitFactory.isJoint(handleObjMemUnits,
								reachableNonPtrReadonly))
					ampleSet.set(otherPid);
			}
		}
		return ampleSet;
	}
}
