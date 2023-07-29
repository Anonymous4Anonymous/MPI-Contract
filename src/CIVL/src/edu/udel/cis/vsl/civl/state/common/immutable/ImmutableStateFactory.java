package edu.udel.cis.vsl.civl.state.common.immutable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.DynamicMemoryLocationSet;
import edu.udel.cis.vsl.civl.dynamic.IF.DynamicMemoryLocationSetFactory;
import edu.udel.cis.vsl.civl.dynamic.IF.Dynamics;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.model.IF.CIVLException;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.Certainty;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.ErrorKind;
import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLStateType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.state.IF.CIVLHeapException;
import edu.udel.cis.vsl.civl.state.IF.CIVLHeapException.HeapErrorKind;
import edu.udel.cis.vsl.civl.state.IF.DynamicScope;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.IF.ProcessState;
import edu.udel.cis.vsl.civl.state.IF.StackEntry;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.StateValueHelper;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Singleton;
import edu.udel.cis.vsl.sarl.IF.CanonicalRenamer;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * An implementation of StateFactory based on the Immutable Pattern.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * @author Timothy J. McClory (tmcclory)
 * @author Stephen F. Siegel (xxxx)
 * 
 */
public class ImmutableStateFactory implements StateFactory {

	/* ************************** Instance Fields ************************** */
	/**
	 * A reference to a reasoner whose context is literal true.
	 */
	private Reasoner trueContextReasoner = null;

	/**
	 * The number of instances of states that have been created.
	 */
	private long initialNumStateInstances = ImmutableState.instanceCount;

	/**
	 * The model factory.
	 */
	protected ModelFactory modelFactory;

	private CIVLTypeFactory typeFactory;

	/**
	 * A reference to a helper class for dealing with $state object values
	 */
	private StateValueHelper stateValueHelper;

	/**
	 * The map of canonic process states. The key and the corresponding value
	 * should be the same, in order to allow fast checking of existence and
	 * returning the value.
	 */
	private Map<ImmutableProcessState, ImmutableProcessState> processMap = new ConcurrentHashMap<>(
			100000);

	/**
	 * The map of canonic dyscopes. The key and the corresponding value should
	 * be the same, in order to allow fast checking of existence and returning
	 * the value.
	 */
	private Map<ImmutableDynamicScope, ImmutableDynamicScope> scopeMap = new ConcurrentHashMap<>(
			100000);

	/**
	 * An instance of {@link ReferredStateStorage} which is used to save collate
	 * states.
	 */
	private ReferredStateStorage collateStateStorage;

	/**
	 * When normalizing a state s, there is a set T of states that are referred
	 * by variables in s must be normalized as well (depth 1). For each state t
	 * in T, there is a set D of states that are referred by variables in t must
	 * be normalized as well (depth 2).
	 */
	private static final int NORMALIZE_REFERRED_STATES_DEPTH = 2;

	protected final SymbolicExpression undefinedProcessValue;

	/**
	 * the CIVL configuration specified by the comamnd line
	 */
	private CIVLConfiguration config;

	/**
	 * The unique symbolic expression for the null process value, which has the
	 * integer value -2.
	 */
	private final SymbolicExpression nullProcessValue;

	/**
	 * The list of canonicalized symbolic expressions of process IDs, will be
	 * used in Executor, Evaluator and State factory to obtain symbolic process
	 * ID's.
	 */
	private SymbolicExpression[] processValues;

	/**
	 * The max number of processes which can be specified through command line.
	 */
	private int maxProcs;

	/**
	 * Amount by which to increase the list of cached scope values and process
	 * values when a new value is requested that is outside of the current
	 * range.
	 */
	private final static int CACHE_INCREMENT = 10;

	// TODO: whats the difference in between undefined and null scope value ??!!
	/**
	 * The unique symbolic expression for the undefined scope value, which has
	 * the integer value -1.
	 */
	private SymbolicExpression undefinedScopeValue;

	/**
	 * The unique symbolic expression for the null scope value, which has the
	 * integer value -2.
	 */
	private SymbolicExpression nullScopeValue;

	/** A list of nulls of length CACHE_INCREMENT */
	private List<SymbolicExpression> nullList = new LinkedList<SymbolicExpression>();

	/**
	 * The size of {@link #smallScopeValues}.
	 */
	private final int SCOPE_VALUES_INIT_SIZE = 500;

	/**
	 * The array which caches the canonicalized symbolic expression of small
	 * scope IDs which are less than {@link #SCOPE_VALUES_INIT_SIZE}.
	 */
	private SymbolicExpression[] smallScopeValues = new SymbolicExpression[500];

	/**
	 * The list of canonicalized symbolic expressions of scope IDs, will be used
	 * in Executor, Evaluator and State factory to obtain symbolic scope ID's.
	 * 
	 */
	private List<SymbolicExpression> bigScopeValues = new ArrayList<SymbolicExpression>();

	/**
	 * Class used to wrap integer arrays so they can be used as keys in hash
	 * maps. This is used to map dyscope ID substitution maps to SARL
	 * substituters, in order to reuse substituters when the same substitution
	 * map comes up again and again. Since the substituters cache their results,
	 * this has the potential to increase performance.
	 * 
	 * @author xxxx
	 *
	 */
	private class IntArray {
		private int[] contents;

		public IntArray(int[] contents) {
			this.contents = contents;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof IntArray) {
				return Arrays.equals(contents, ((IntArray) obj).contents);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(contents);
		}
	}

	private Map<IntArray, UnaryOperator<SymbolicExpression>> dyscopeSubMap = new ConcurrentHashMap<>();

	/**
	 * The symbolic universe, provided by SARL.
	 */
	protected SymbolicUniverse universe;

	protected SymbolicUtility symbolicUtil;

	private ImmutableMemoryUnitFactory memUnitFactory;

	private ReservedConstant isReservedSymbolicConstant;

	private List<Variable> inputVariables;

	protected Set<HeapErrorKind> emptyHeapErrorSet = new HashSet<>(0);

	protected Set<HeapErrorKind> fullHeapErrorSet = new HashSet<>();

	/**
	 * An operator that converts a scope value which is an instance of
	 * {@link SymbolicExpression} to a dyscope ID in the form of
	 * {@link IntegerNumber}.
	 */
	private Function<SymbolicExpression, IntegerNumber> scopeValueToDyscopeID = null;

	/**
	 * An operator that converts a dyscope ID in the form {@link IntegerNumber}
	 * to a scope value which is an instance of {@link SymbolicExpression}.
	 */
	private Function<Integer, SymbolicExpression> dyscopeIDToScopeValue = null;

	/**
	 * A reference to {@link DynamicMemoryLocationSetFactory} which produces new
	 * instances of {@link DynamicMemoryLocationSet}
	 */
	private DynamicMemoryLocationSetFactory memoryLocationSetFactory;

	/* **************************** Constructors *************************** */

	/**
	 * Factory to create all state objects.
	 */
	public ImmutableStateFactory(ModelFactory modelFactory,
			MemoryUnitFactory memFactory, CIVLConfiguration config) {
		this.modelFactory = modelFactory;
		this.inputVariables = modelFactory.inputVariables();
		this.typeFactory = modelFactory.typeFactory();
		this.universe = modelFactory.universe();
		this.memUnitFactory = (ImmutableMemoryUnitFactory) memFactory;
		this.undefinedProcessValue = modelFactory
				.undefinedValue(typeFactory.processSymbolicType());
		isReservedSymbolicConstant = new ReservedConstant();
		this.config = config;
		this.nullProcessValue = universe.tuple(
				typeFactory.processSymbolicType(),
				new Singleton<SymbolicExpression>(universe.integer(-2)));
		this.maxProcs = config.getMaxProcs();
		this.processValues = new SymbolicExpression[maxProcs];
		this.collateStateStorage = new ReferredStateStorage();
		for (HeapErrorKind kind : HeapErrorKind.class.getEnumConstants())
			fullHeapErrorSet.add(kind);
		for (int i = 0; i < maxProcs; i++) {
			processValues[i] = universe.tuple(typeFactory.processSymbolicType(),
					new Singleton<SymbolicExpression>(universe.integer(i)));
		}
		this.scopeValueToDyscopeID = typeFactory.scopeType()
				.scopeValueToIdentityOperator(universe);
		this.dyscopeIDToScopeValue = typeFactory.scopeType()
				.scopeIdentityToValueOperator(universe);
		this.undefinedScopeValue = dyscopeIDToScopeValue
				.apply(ModelConfiguration.DYNAMIC_UNDEFINED_SCOPE);
		this.nullScopeValue = dyscopeIDToScopeValue
				.apply(ModelConfiguration.DYNAMIC_NULL_SCOPE);
		for (int i = 0; i < SCOPE_VALUES_INIT_SIZE; i++) {
			smallScopeValues[i] = dyscopeIDToScopeValue.apply(i);
		}
		for (int i = 0; i < CACHE_INCREMENT; i++)
			nullList.add(null);
		this.trueContextReasoner = universe.reasoner(universe.trueExpression());
		memoryLocationSetFactory = Dynamics.newDynamicMemoryLocationSetFactory(
				universe, typeFactory, this.nullScopeValue);
	}

	/* ********************** Methods from StateFactory ******************** */

	@Override
	public ImmutableState addProcess(State state, CIVLFunction function,
			SymbolicExpression[] arguments, int callerPid,
			boolean selfDestructable) {
		ImmutableState theState = createNewProcess(state, selfDestructable);

		return pushCallStack2(theState, state.numProcs(), function, -1,
				arguments, callerPid);
	}

	@Override
	public State addProcess(State state, CIVLFunction function,
			int functionParentDyscope, SymbolicExpression[] arguments,
			int callerPid, boolean selfDestructable) {
		ImmutableState theState = createNewProcess(state, selfDestructable);

		return pushCallStack2(theState, state.numProcs(), function,
				functionParentDyscope, arguments, callerPid);
	}

	@Override
	public ImmutableState canonic(State state, boolean collectProcesses,
			boolean collectScopes, boolean collectHeaps,
			boolean collectSymbolicConstants, boolean simplify,
			Set<HeapErrorKind> toBeIgnored) throws CIVLHeapException {
		if (config.sliceAnalysis()) {
			return (ImmutableState) state;
		} else {
			return canonicWork(state, collectProcesses, collectScopes,
					collectHeaps, collectSymbolicConstants, simplify,
					toBeIgnored);
		}
	}

	/**
	 * <p>
	 * In this implementation of canonic: process states are collected, heaps
	 * are collected, dynamic scopes are collected, the flyweight representative
	 * is taken, simplify is called if that option is selected, then the
	 * flyweight representative is taken again.
	 * </p>
	 * 
	 * 
	 * @param state
	 *            The state that will be canonicalized
	 * @param collectProcesses
	 *            true to collect process states in the state during
	 *            canonicalization.
	 * @param collectScopes
	 *            true to collect dynamic scopes in the state during
	 *            canonicalization.
	 * @param collectHeaps
	 *            true to collect memory heaps in the state during
	 *            canonicalization.
	 * @param toBeIgnored
	 *            A set of {@link HeapErrorKind}s which will be supressed during
	 *            heap collection.
	 * @param isReferredState
	 *            <p>
	 *            True if and only if the given state is a referred state, i.e.
	 *            it is referred by a variable in current main state (currently
	 *            it is always a collate state). For referred state, their
	 *            simplification and symbolic constant collection must be
	 *            carried out along with their main state. Otherwise there will
	 *            be inconsistency in between them (referred and main states).
	 *            </p>
	 *            <p>
	 *            Here main state means the state where has variables referring
	 *            this referred state.
	 *            </p>
	 * @return
	 * @throws CIVLHeapException
	 */
	public ImmutableState canonicWork(State state, boolean collectProcesses,
			boolean collectScopes, boolean collectHeaps,
			boolean collectSymbolicConstants, boolean simplify,
			Set<HeapErrorKind> toBeIgnored) throws CIVLHeapException {
		ImmutableState theState = (ImmutableState) state;

		// performance experiment: seems to make no difference
		// theState = flyweight(theState);
		if (collectProcesses)
			theState = collectProcesses(theState);
		if (collectScopes)
			theState = collectScopes(theState, toBeIgnored);
		if (collectHeaps)
			theState = collectHeaps(theState, toBeIgnored);
		// theState = collectSymbolicConstants(theState, collectHeaps);
		if (collectSymbolicConstants)
			theState = collectHavocVariables(theState);
		if (simplify)
			theState = simplify(theState);
		theState.makeCanonic(universe, scopeMap, processMap);
		return theState;
	}

	@Override
	public ImmutableState collectHeaps(State state,
			Set<HeapErrorKind> toBeIgnored) throws CIVLHeapException {
		ImmutableState theState = (ImmutableState) state;

		// only collect heaps when necessary.
		if (!this.hasNonEmptyHeaps(theState))
			return theState;
		else {
			Set<SymbolicExpression> reachable = this
					.reachableHeapObjectsOfState(theState);
			int numDyscopes = theState.numDyscopes();
			int numHeapFields = typeFactory.heapType().getNumMallocs();
			Map<SymbolicExpression, SymbolicExpression> oldToNewHeapMemUnits = new HashMap<>();
			ImmutableDynamicScope[] newScopes = new ImmutableDynamicScope[numDyscopes];
			ReferenceExpression[] fieldRefs = new ReferenceExpression[numHeapFields];

			for (int mallocId = 0; mallocId < numHeapFields; mallocId++) {
				fieldRefs[mallocId] = universe.tupleComponentReference(
						universe.identityReference(),
						universe.intObject(mallocId));
			}
			for (int dyscopeId = 0; dyscopeId < numDyscopes; dyscopeId++) {
				DynamicScope dyscope = theState.getDyscope(dyscopeId);
				SymbolicExpression heap = dyscope.getValue(0);

				if (heap.isNull())
					continue;
				else {
					SymbolicExpression newHeap = heap;
					SymbolicExpression heapPointer = this.symbolicUtil
							.makePointer(dyscopeId, 0,
									universe.identityReference());

					for (int mallocId = 0; mallocId < numHeapFields; mallocId++) {
						SymbolicExpression heapField = universe.tupleRead(heap,
								universe.intObject(mallocId));
						int length = this.symbolicUtil.extractInt(null,
								(NumericExpression) universe.length(heapField));
						Map<Integer, Integer> oldID2NewID = new HashMap<>();
						int numRemoved = 0;
						SymbolicExpression newHeapField = heapField;
						boolean hasNew = false;

						for (int objectId = 0; objectId < length; objectId++) {
							ReferenceExpression objectRef = universe
									.arrayElementReference(fieldRefs[mallocId],
											universe.integer(objectId));
							SymbolicExpression objectPtr = this.symbolicUtil
									.setSymRef(heapPointer, objectRef);

							if (!reachable.contains(objectPtr)) {
								SymbolicExpression heapObj = universe.arrayRead(
										heapField, universe.integer(objectId));

								if (config.checkMemoryLeak()
										&& !symbolicUtil
												.isInvalidHeapObject(heapObj)
										&& !toBeIgnored.contains(
												HeapErrorKind.UNREACHABLE)) {
									throw new CIVLHeapException(
											ErrorKind.MEMORY_LEAK,
											Certainty.CONCRETE, theState,
											"d" + dyscopeId, dyscopeId, heap,
											mallocId, objectId,
											HeapErrorKind.UNREACHABLE,
											dyscope.lexicalScope().getSource());
								}
								// remove unreachable heap object
								// updates references
								for (int nextId = objectId
										+ 1; nextId < length; nextId++) {
									if (oldID2NewID.containsKey(nextId))
										oldID2NewID.put(nextId,
												oldID2NewID.get(nextId) - 1);
									else
										oldID2NewID.put(nextId, nextId - 1);
								}
								// remove object
								hasNew = true;
								newHeapField = universe.removeElementAt(
										newHeapField, objectId - numRemoved);
								numRemoved++;
							}
						}
						if (oldID2NewID.size() > 0)
							addOldToNewHeapMemUnits(oldID2NewID, heapPointer,
									fieldRefs[mallocId], oldToNewHeapMemUnits);
						if (hasNew)
							newHeap = universe.tupleWrite(newHeap,
									universe.intObject(mallocId), newHeapField);
					}
					if (symbolicUtil.isEmptyHeap(newHeap))
						newHeap = universe.nullExpression();
					theState = this.setVariable(theState, 0, dyscopeId,
							newHeap);
				}
			}
			computeOldToNewHeapPointers(theState, oldToNewHeapMemUnits,
					oldToNewHeapMemUnits);
			for (int i = 0; i < numDyscopes; i++)
				newScopes[i] = theState.getDyscope(i)
						.updateHeapPointers(oldToNewHeapMemUnits, universe);
			// update heap pointers in write set and partial path conditions:
			theState = applyToProcessStates(theState,
					universe.mapSubstituter(oldToNewHeapMemUnits));
			theState = theState.setScopes(newScopes);
			return theState;
		}
	}

	/**
	 * Apply an {@link UnaryOperator} to symbolic expressions in partial path
	 * conditions and write sets in {@link ProcessState}s of the given state.
	 * 
	 * @param state
	 *            The state where heap pointers are collected.
	 * @param substituteMap
	 *            A unary operator which will be applied to partial path
	 *            condition stacks and write set stacks of processes in the
	 *            given state.
	 * @return A new state in which heap pointers in process states are
	 *         collected.
	 */
	private ImmutableState applyToProcessStates(ImmutableState state,
			UnaryOperator<SymbolicExpression> substituter) {
		ImmutableProcessState[] newProcs = state.copyProcessStates();
		ImmutableProcessState newProcesses[] = new ImmutableProcessState[state
				.numProcs()];
		boolean procChanged = false;

		for (int i = 0; i < newProcs.length; i++) {
			if (state.getProcessState(i) == null) {
				newProcesses[i] = null;
				continue;
			} else
				newProcesses[i] = state.getProcessState(i);

			ImmutableProcessState newProcState = newProcesses[i]
					.apply(substituter);

			if (newProcState != newProcesses[i]) {
				procChanged = true;
				newProcesses[i] = newProcState;
			}
		}
		if (procChanged)
			return state.setProcessStates(newProcesses);
		else
			return state;
	}

	@Override
	public ImmutableState collectScopes(State state,
			Set<HeapErrorKind> toBeIgnored) throws CIVLHeapException {
		return collectScopesWorker(state, toBeIgnored, null);
	}

	/**
	 * The worker method which is called directly by
	 * {@link #collectScopes(State, Set)}. This method has one more output
	 * parameter that {@link #collectScopes(State, Set)} doesn't need.
	 * 
	 * @param state
	 *            the state whose scopes will be collected
	 * @param toBeIgnored
	 *            the set of {@link HeapErrorKind}s that will be ignored during
	 *            collection
	 * @param old2NewOutput
	 *            OUTPUT parameter. A map from old scope IDs to new scope IDs.
	 *            <b>pre-condition:</b>
	 *            <code>old2NewOutput.length == state.numDyscopes()</code>
	 * 
	 * @return
	 * @throws CIVLHeapException
	 */
	private ImmutableState collectScopesWorker(State state,
			Set<HeapErrorKind> toBeIgnored, int old2NewOutput[])
			throws CIVLHeapException {
		ImmutableState theState = (ImmutableState) state;
		int oldNumScopes = theState.numDyscopes();
		int[] oldToNew = numberScopes(theState);
		boolean change = false;
		int newNumScopes = 0;

		for (int i = 0; i < oldNumScopes; i++) {
			int id = oldToNew[i];

			if (id >= 0)
				newNumScopes++;
			if (!change && id != i)
				change = true;
			if (id < 0 && config.checkMemoryLeak()
					&& !toBeIgnored.contains(HeapErrorKind.NONEMPTY)) {
				ImmutableDynamicScope scopeToBeRemoved = theState.getDyscope(i);
				Variable heapVariable = scopeToBeRemoved.lexicalScope()
						.variable(ModelConfiguration.HEAP_VAR);
				SymbolicExpression heapValue = scopeToBeRemoved
						.getValue(heapVariable.vid());

				if (!(heapValue.isNull()
						|| symbolicUtil.isEmptyHeap(heapValue))) {
					throw new CIVLHeapException(ErrorKind.MEMORY_LEAK,
							Certainty.CONCRETE, state, "d" + i, i, heapValue,
							HeapErrorKind.NONEMPTY, heapVariable.getSource());
				}
			}
		}
		if (change) {
			IntArray key = new IntArray(oldToNew);
			UnaryOperator<SymbolicExpression> substituter = dyscopeSubMap
					.get(key);

			if (substituter == null) {
				substituter = universe.mapSubstituter(scopeSubMap(oldToNew));
				dyscopeSubMap.putIfAbsent(key, substituter);
			}

			ImmutableDynamicScope[] newScopes = new ImmutableDynamicScope[newNumScopes];
			int numProcs = theState.numProcs();
			ImmutableProcessState[] newProcesses = new ImmutableProcessState[numProcs];
			BooleanExpression newPathCondition = (BooleanExpression) substituter
					.apply(theState.getPermanentPathCondition());

			for (int i = 0; i < oldNumScopes; i++) {
				int newId = oldToNew[i];

				if (newId >= 0) {
					ImmutableDynamicScope oldScope = theState.getDyscope(i);
					int oldParent = oldScope.getParent();
					// int oldParentIdentifier = oldScope.identifier();

					newScopes[newId] = oldScope.updateDyscopeIds(substituter,
							universe,
							oldParent < 0 ? oldParent : oldToNew[oldParent]);
				}
			}
			for (int pid = 0; pid < numProcs; pid++) {
				newProcesses[pid] = theState.getProcessState(pid);
				if (newProcesses[pid] != null)
					newProcesses[pid] = newProcesses[pid]
							.updateDyscopes(oldToNew, substituter);
			}
			theState = ImmutableState.newState(theState, newProcesses,
					newScopes, newPathCondition);
		}
		if (theState.numDyscopes() == 1
				&& !toBeIgnored.contains(HeapErrorKind.NONEMPTY)
				&& theState.getProcessState(0).hasEmptyStack()) {
			// checks the memory leak for the final state
			DynamicScope dyscope = state.getDyscope(0);
			SymbolicExpression heap = dyscope.getValue(0);

			if (config.checkMemoryLeak() && !symbolicUtil.isEmptyHeap(heap))
				throw new CIVLHeapException(ErrorKind.MEMORY_LEAK,
						Certainty.CONCRETE, state, "d0", 0, heap,
						HeapErrorKind.NONEMPTY,
						dyscope.lexicalScope().getSource());

		}
		if (old2NewOutput != null)
			System.arraycopy(oldToNew, 0, old2NewOutput, 0, oldToNew.length);
		return theState;
	}

	// @Override
	public State getAtomicLock(State state, int pid) {
		Variable atomicVar = modelFactory.atomicLockVariableExpression()
				.variable();

		// assert state.getVariableValue(0, atomicVar.vid())
		return this.setVariable(state, atomicVar.vid(), 0, processValue(pid));
	}

	@Override
	public long getNumStateInstances() {
		return ImmutableState.instanceCount - initialNumStateInstances;
	}

	@Override
	public ImmutableState initialState(Model model) throws CIVLHeapException {
		// HashMap<Integer, Map<SymbolicExpression, Boolean>> reachableMUs = new
		// HashMap<Integer, Map<SymbolicExpression, Boolean>>();
		// HashMap<Integer, Map<SymbolicExpression, Boolean>> reachableMUwtPtr =
		// new HashMap<Integer, Map<SymbolicExpression, Boolean>>();
		ImmutableState state;
		CIVLFunction function = model.rootFunction();
		int numArgs = function.parameters().size();
		SymbolicExpression[] arguments = new SymbolicExpression[numArgs];
		Variable atomicVar = modelFactory.atomicLockVariableExpression()
				.variable();
		Variable timeCountVar = modelFactory.timeCountVariable();

		// reachableMUs.put(0, new HashMap<SymbolicExpression, Boolean>());
		state = new ImmutableState(new ImmutableProcessState[0],
				new ImmutableDynamicScope[0], universe.trueExpression());
		state.collectibleCounts = new int[ModelConfiguration.SYMBOL_PREFIXES.length];
		for (int i = 0; i < ModelConfiguration.SYMBOL_PREFIXES.length; i++) {
			state.collectibleCounts[i] = 0;
		}
		// system function doesn't have any argument, because the General
		// transformer has translated away all parameters of the main function.
		state = addProcess(state, function, arguments, -1, false);
		state = this.setVariable(state, atomicVar.vid(), 0,
				undefinedProcessValue);
		if (timeCountVar != null)
			state = this.setVariable(state, timeCountVar.vid(), 0,
					universe.zeroInt());
		// state = this.computeReachableMemUnits(state, 0);
		state = canonic(state, false, false, false, false, false,
				emptyHeapErrorSet);
		return state;
	}

	@Override
	public boolean isDescendantOf(State state, int ancestor, int descendant) {
		if (ancestor == descendant) {
			return false;
		} else {
			int parent = state.getParentId(descendant);

			while (parent >= 0) {
				if (ancestor == parent)
					return true;
				parent = state.getParentId(parent);
			}
		}
		return false;
	}

	@Override
	public boolean lockedByAtomic(State state) {
		SymbolicExpression symbolicAtomicPid = state.getVariableValue(0,
				modelFactory.atomicLockVariableExpression().variable().vid());
		int atomicPid = modelFactory.getProcessId(symbolicAtomicPid);

		return atomicPid >= 0;
	}

	@Override
	// TODO: improve the performance: keep track of depth of dyscopes
	public int lowestCommonAncestor(State state, int one, int another) {
		if (one == another) {
			return one;
		} else {
			int parent = one;

			while (parent >= 0) {
				if (parent == another
						|| this.isDescendantOf(state, parent, another))
					return parent;
				parent = state.getParentId(parent);
			}
		}
		return state.rootDyscopeID();
	}

	@Override
	public ImmutableState popCallStack(State state, int pid) {
		ImmutableState theState = (ImmutableState) state;
		ImmutableProcessState process = theState.getProcessState(pid);
		ImmutableProcessState[] processArray = theState.copyProcessStates();
		ImmutableDynamicScope[] newScopes = theState.copyScopes();
		// DynamicScope dyscopeExpired =
		// state.getDyscope(process.getDyscopeId());
		// Scope staticScope = dyscopeExpired.lexicalScope();
		// Map<Integer, Map<SymbolicExpression, Boolean>> reachableMUwoPtr =
		// null, reachableMUwtPtr = null;

		processArray[pid] = process.pop();
		setReachablesForProc(newScopes, processArray[pid]);
		// if (!processArray[pid].hasEmptyStack() && staticScope.hasVariable())
		// {
		// reachableMUwoPtr = this.setReachableMemUnits(theState, pid, this
		// .removeReachableMUwoPtrFromDyscopes(new HashSet<Integer>(
		// Arrays.asList(process.getDyscopeId())), theState,
		// pid), false);
		// if (staticScope.hasVariableWtPointer())
		// reachableMUwtPtr = this.setReachableMemUnits(theState, pid,
		// this.computeReachableMUofProc(theState, pid, true),
		// true);
		// }
		theState = ImmutableState.newState(theState, processArray, newScopes,
				null);
		return theState;
	}

	@Override
	public int processInAtomic(State state) {
		// TODO use a field for vid
		SymbolicExpression symbolicAtomicPid = state.getVariableValue(0,
				modelFactory.atomicLockVariableExpression().variable().vid());

		return modelFactory.getProcessId(symbolicAtomicPid);
	}

	@Override
	public ImmutableState pushCallStack(State state, int pid,
			CIVLFunction function, SymbolicExpression[] arguments) {
		return pushCallStack2((ImmutableState) state, pid, function, -1,
				arguments, pid);
	}

	@Override
	public State pushCallStack(State state, int pid, CIVLFunction function,
			int functionParentDyscope, SymbolicExpression[] arguments) {
		return pushCallStack2((ImmutableState) state, pid, function,
				functionParentDyscope, arguments, pid);
	}

	@Override
	public ImmutableState collectProcesses(State state) {
		ImmutableState theState = (ImmutableState) state;
		int numProcs = theState.numProcs();
		boolean change = false;
		int counter = 0;

		while (counter < numProcs) {
			if (theState.getProcessState(counter) == null) {
				change = true;
				break;
			}
			counter++;
		}
		if (change) {
			int newNumProcs = counter;
			int[] oldToNewPidMap = new int[numProcs];
			ImmutableProcessState[] newProcesses;
			ImmutableDynamicScope[] newScopes;
			// Map<Integer, Map<SymbolicExpression, Boolean>>
			// reachableMUsWtPointer, reachableMUsWoPointer;

			for (int i = 0; i < counter; i++)
				oldToNewPidMap[i] = i;
			oldToNewPidMap[counter] = -1;
			for (int i = counter + 1; i < numProcs; i++) {
				if (theState.getProcessState(i) == null) {
					oldToNewPidMap[i] = -1;
				} else {
					oldToNewPidMap[i] = newNumProcs;
					newNumProcs++;
				}
			}
			newProcesses = new ImmutableProcessState[newNumProcs];
			for (int i = 0; i < numProcs; i++) {
				int newPid = oldToNewPidMap[i];

				if (newPid >= 0)
					newProcesses[newPid] = theState.getProcessState(i)
							.setPid(newPid);
			}
			// newReachableMemUnitsMap =
			// updateProcessReferencesInReachableMemoryUnitsMap(
			// theState, oldToNewPidMap);
			// reachableMUsWtPointer = this.updatePIDsForReachableMUs(
			// oldToNewPidMap, theState, true);
			// reachableMUsWoPointer = this.updatePIDsForReachableMUs(
			// oldToNewPidMap, theState, false);
			newScopes = updateProcessReferencesInScopes(theState,
					oldToNewPidMap);
			theState = ImmutableState.newState(theState, newProcesses,
					newScopes, null);
		}
		return theState;
	}

	@Override
	public State terminateProcess(State state, int pid) {
		ImmutableState theState = (ImmutableState) state;
		ImmutableProcessState emptyProcessState = new ImmutableProcessState(pid,
				false);

		return theState.setProcessState(pid, emptyProcessState);
	}

	@Override
	public ImmutableState removeProcess(State state, int pid) {
		ImmutableState theState = (ImmutableState) state;

		theState = theState.setProcessState(pid, null);
		return theState;
	}

	@Override
	public State releaseAtomicLock(State state) {
		Variable atomicVar = modelFactory.atomicLockVariableExpression()
				.variable();

		return this.setVariable(state, atomicVar.vid(), 0, processValue(-1));
	}

	/**
	 * Procedure:
	 * 
	 * <ol>
	 * <li>get the current dynamic scope ds0 of the process. Let ss0 be the
	 * static scope associated to ds0.</li>
	 * <li>Let ss1 be the static scope of the new location to move to.</li>
	 * <li>Compute the join (youngest common ancestor) of ss0 and ss1. Also save
	 * the sequence of static scopes from join to ss1.</li>
	 * <li>Iterate UP over dynamic scopes from ds0 up (using parent field) to
	 * the first dynamic scope whose static scope is join.</li>
	 * <li>Iterate DOWN from join to ss1, creating NEW dynamic scopes along the
	 * way.</li>
	 * <li>Set the frame pointer to the new dynamic scope corresponding to ss1,
	 * and set the location to the given location.</li>
	 * <li>Remove all unreachable scopes.</li>
	 * </ol>
	 * 
	 * @param state
	 * @param pid
	 * @param location
	 * @return
	 */
	@Override
	// TODO UPDATE reachable mem units
	public ImmutableState setLocation(State state, int pid, Location location,
			boolean accessChanged) {
		ImmutableState theState = (ImmutableState) state;
		ImmutableProcessState[] processArray = theState.copyProcessStates();
		int dynamicScopeId = theState.getProcessState(pid).getDyscopeId();
		ImmutableDynamicScope dynamicScope = theState
				.getDyscope(dynamicScopeId);
		// int dynamicScopeIdentifier = dynamicScope.identifier();
		boolean stayInScope = location.isSleep();

		if (!location.isSleep()) {
			stayInScope = location.scope() == dynamicScope.lexicalScope();
		}
		if (stayInScope) {// remains in the same dyscope
			processArray[pid] = theState.getProcessState(pid)
					.replaceTop(stackEntry(location, dynamicScopeId));
			theState = theState.setProcessStates(processArray);
			// if (accessChanged)
			// theState = updateReachableMemUnitsAccess(theState, pid);
			return theState;
		} else {// a different dyscope is encountered
			Scope[] joinSequence = joinSequence(dynamicScope.lexicalScope(),
					location.scope());
			Scope join = joinSequence[0];
			Set<Integer> dyscopeIDsequence = new HashSet<>();

			// iterate UP...
			while (dynamicScope.lexicalScope() != join) {
				dyscopeIDsequence.add(dynamicScopeId);
				dynamicScopeId = theState.getParentId(dynamicScopeId);
				if (dynamicScopeId < 0)
					throw new RuntimeException("State is inconsistent");
				dynamicScope = theState.getDyscope(dynamicScopeId);
				// dynamicScopeIdentifier = dynamicScope.identifier();
			}
			if (joinSequence.length == 1) {
				// Map<Integer, Map<SymbolicExpression, Boolean>>
				// reachableMUwoPtr, reachableMUwtPtr;

				// the previous scope(s) just disappear
				processArray[pid] = theState.getProcessState(pid)
						.replaceTop(stackEntry(location, dynamicScopeId));
				// reachableMUwoPtr = this.setReachableMemUnits(theState, pid,
				// this.removeReachableMUwoPtrFromDyscopes(
				// dyscopeIDsequence, theState, pid), false);
				// reachableMUwtPtr = this.setReachableMemUnits(theState, pid,
				// this.computeReachableMUofProc(theState, pid, true),
				// true);
				theState = ImmutableState.newState(theState, processArray, null,
						null);
			} else {
				// iterate DOWN, adding new dynamic scopes...
				int oldNumScopes = theState.numDyscopes();
				int newNumScopes = oldNumScopes + joinSequence.length - 1;
				int index = 0;
				ImmutableDynamicScope[] newScopes = new ImmutableDynamicScope[newNumScopes];
				int[] newDyscopes = new int[joinSequence.length - 1];

				for (; index < oldNumScopes; index++)
					newScopes[index] = theState.getDyscope(index);
				for (int i = 1; i < joinSequence.length; i++) {
					// only this process can reach the new dyscope
					BitSet reachers = new BitSet(processArray.length);

					reachers.set(pid);
					newScopes[index] = initialDynamicScope(joinSequence[i],
							dynamicScopeId, index, reachers);
					dynamicScopeId = index;
					newDyscopes[i - 1] = dynamicScopeId;
					index++;
				}
				processArray[pid] = processArray[pid]
						.replaceTop(stackEntry(location, dynamicScopeId));
				setReachablesForProc(newScopes, processArray[pid]);
				theState = ImmutableState.newState(theState, processArray,
						newScopes, null);
				// theState = addReachableMemUnitsFromDyscope(newDyscopes,
				// newScopes, theState, pid);
			}
			// if (accessChanged)
			// theState = updateReachableMemUnitsAccess(theState, pid);
			return theState;
		}
	}

	@Override
	public State setProcessState(State state, ProcessState p) {
		ImmutableState theState = (ImmutableState) state;
		ImmutableState newState;
		ImmutableProcessState[] newProcesses;
		int pid = p.getPid();

		newProcesses = theState.copyProcessStates();
		newProcesses[pid] = (ImmutableProcessState) p;
		theState = theState.setProcessStates(newProcesses);
		newState = new ImmutableState(newProcesses, theState.copyScopes(),
				theState.getPermanentPathCondition());
		newState.collectibleCounts = theState.collectibleCounts;
		return newState;
	}

	@Override
	public ImmutableState setVariable(State state, int vid, int scopeId,
			SymbolicExpression value) {
		ImmutableState theState = (ImmutableState) state;
		ImmutableDynamicScope oldScope = (ImmutableDynamicScope) theState
				.getDyscope(scopeId);
		ImmutableDynamicScope[] newScopes = theState.copyScopes();
		SymbolicExpression[] newValues = oldScope.copyValues();
		ImmutableDynamicScope newScope;

		newValues[vid] = value;
		newScope = new ImmutableDynamicScope(oldScope.lexicalScope(),
				oldScope.getParent(), // TODO
										// oldScope.getParentIdentifier()
				newValues, oldScope.getReachers());
		newScopes[scopeId] = newScope;
		theState = theState.setScopes(newScopes);
		return theState;
	}

	@Override
	public ImmutableState setVariable(State state, Variable variable, int pid,
			SymbolicExpression value) {
		int scopeId = state.getDyscopeID(pid, variable);

		return setVariable(state, variable.vid(), scopeId, value);
	}

	@Override
	public ImmutableState simplify(State state) {
		ImmutableState theState = (ImmutableState) state;

		theState = simplifyReferencedStates(theState,
				theState.getPermanentPathCondition(),
				NORMALIZE_REFERRED_STATES_DEPTH);
		return simplifyWork(theState, true);
	}

	// TODO: why need this ?
	@SuppressWarnings("unused")
	private BooleanExpression getContextOfSizeofSymbols(Reasoner reasoner) {
		Map<SymbolicConstant, SymbolicExpression> map = reasoner
				.constantSubstitutionMap();
		BooleanExpression result = universe.trueExpression();

		for (Map.Entry<SymbolicConstant, SymbolicExpression> pair : map
				.entrySet()) {
			if ((this.config.isEnableMpiContract()
					&& this.config.inSubprogram())
					|| ModelConfiguration.SIZEOF_VARS.contains(pair.getKey())) {
				result = universe.and(result,
						universe.equals(pair.getValue(), pair.getKey()));
			}
		}
		return result;
	}

	/**
	 * Simplify the given state.
	 * 
	 * @param state
	 *            the state that will gets simplified
	 * @return
	 */
	private ImmutableState simplifyWork(State state,
			boolean reducePathcondition) {
		ImmutableState theState = (ImmutableState) state;

		if (theState.simplifiedState != null)
			return theState.simplifiedState;

		int numScopes = theState.numDyscopes();
		BooleanExpression pathCondition = theState.getPermanentPathCondition();
		ImmutableDynamicScope[] newDynamicScopes = null;
		Reasoner reasoner = universe.reasoner(pathCondition);
		BooleanExpression newPathCondition;

		newPathCondition = reasoner.getReducedContext();

		if (newPathCondition != pathCondition)
			if (nsat(newPathCondition))
				newPathCondition = universe.falseExpression();
		for (int i = 0; i < numScopes; i++) {
			ImmutableDynamicScope oldScope = theState.getDyscope(i);
			int numVars = oldScope.numberOfVariables();
			SymbolicExpression[] newVariableValues = null;

			for (int j = 0; j < numVars; j++) {
				SymbolicExpression oldValue = oldScope.getValue(j);
				SymbolicExpression newValue = reasoner.simplify(oldValue);

				if (oldValue != newValue && newVariableValues == null) {
					newVariableValues = new SymbolicExpression[numVars];
					for (int j2 = 0; j2 < j; j2++)
						newVariableValues[j2] = oldScope.getValue(j2);
				}
				if (newVariableValues != null)
					newVariableValues[j] = newValue;
			}
			if (newVariableValues != null && newDynamicScopes == null) {
				newDynamicScopes = new ImmutableDynamicScope[numScopes];
				for (int i2 = 0; i2 < i; i2++)
					newDynamicScopes[i2] = theState.getDyscope(i2);
			}
			if (newDynamicScopes != null)
				newDynamicScopes[i] = newVariableValues != null
						? oldScope.setVariableValues(newVariableValues)
						: oldScope;
		}

		boolean processChanged = false;
		ImmutableProcessState[] procStates = theState.copyProcessStates();
		SimplifyOperator simplifier = new SimplifyOperator(reasoner);

		for (int i = 0; i < procStates.length; i++) {
			if (procStates[i] == null)
				continue;

			ImmutableProcessState tmp = procStates[i].apply(simplifier);

			if (tmp != procStates[i])
				processChanged = true;
			procStates[i] = tmp;
		}
		if (newDynamicScopes != null || newPathCondition != null
				|| processChanged) {
			theState = ImmutableState.newState(theState, procStates,
					newDynamicScopes,
					reducePathcondition
							? newPathCondition
							: reasoner.getFullContext());
			theState.simplifiedState = theState;
		}
		return theState;
	}

	/**
	 * Search $state type variables in each dynamic scope (dyscope) of the given
	 * state. Returns a list of pairs: an dynamic scope ID and a list of state
	 * values in the dynamic scope
	 * 
	 * @param state
	 *            The state in which referred states will be returned.
	 * @return A list of pairs: one is the ID of the dyscope, in which contains
	 *         at least one $state variable, of the given state; the other is
	 *         the set of values of $state objects in aforementioned dyscope.
	 * 
	 */
	private List<Pair<Integer, List<SymbolicExpression>>> getStateReferences(
			ImmutableState state) {
		SymbolicType symbolicStateType = modelFactory.typeFactory()
				.stateSymbolicType();
		List<Pair<Integer, List<SymbolicExpression>>> allStateRefs = new LinkedList<>();
		int numDyscopes = state.numDyscopes();

		for (int i = 0; i < numDyscopes; i++) {
			ImmutableDynamicScope dyscope = state.getDyscope(i);
			Collection<Variable> variablesWithStateRef = dyscope.lexicalScope()
					.variablesWithStaterefs();
			List<SymbolicExpression> stateValues = new LinkedList<>();

			for (Variable var : variablesWithStateRef) {
				int vid = var.vid();
				SymbolicExpression value = dyscope.getValue(vid);
				List<SymbolicExpression> stateRefs = getSubExpressionsOfType(
						symbolicStateType, value);

				for (SymbolicExpression stateRef : stateRefs)
					stateValues.add(stateRef);
			}
			if (!stateValues.isEmpty())
				allStateRefs.add(new Pair<>(i, stateValues));
		}
		return allStateRefs;
	}

	/**
	 * Renaming symbolic constants in states referred by $state variables in
	 * current state. Every time the current state collects its symbolic
	 * constants, this method shall be called to make symbolic constants in
	 * referred states consistent with the current state.
	 * 
	 * @param state
	 *            The current state
	 * @param renamer
	 *            The symbolic constant renamer used by the current state, which
	 *            contains a mapping function from old collected symbolic
	 *            constants to new ones.
	 * @param collectReferredStateDepth
	 *            The depth of collecting symbolic constants in referred states.
	 *            To understand "depth", see
	 *            {@link #NORMALIZE_REFERRED_STATES_DEPTH}
	 * @return A new state which is same as the current state but $state
	 *         variables in it are updated.
	 * @throws CIVLHeapException
	 *             If unexpected heap exception happens during canonicalizing
	 *             referred states
	 */
	private ImmutableState collectHavocVariablesInReferredStates(
			ImmutableState state, UnaryOperator<SymbolicExpression> renamer,
			int collectReferredStateDepth) throws CIVLHeapException {
		// if (!config.isEnableMpiContract())
		// return state;
		if (collectReferredStateDepth <= 0)
			return state;

		List<Pair<Integer, List<SymbolicExpression>>> dyscopeRefStatePairs = getStateReferences(
				state);
		ImmutableDynamicScope newDyscopes[] = null;
		BitSet changedDyscopes = new BitSet(state.numDyscopes());
		CIVLStateType stateType = typeFactory.stateType();

		for (Pair<Integer, List<SymbolicExpression>> pair : dyscopeRefStatePairs) {
			int refStateDyId = pair.left;
			TreeMap<SymbolicExpression, SymbolicExpression> substituteMap = new TreeMap<>(
					universe.comparator());
			UnaryOperator<SymbolicExpression> stateValueUpdater;

			// Rename symbolic expressions in dyscopes, processStates and path
			// conditions in each referred state:
			for (SymbolicExpression oldStateValue : pair.right) {
				int oldStateKey = stateType.selectStateKey(universe,
						oldStateValue);
				SymbolicExpression oldStateScopeMapping = stateType
						.selectScopeValuesMap(universe, oldStateValue);
				ImmutableState oldReferredState = collateStateStorage
						.getSavedState(oldStateKey);
				ImmutableState newReferredState;
				boolean unchange = true;
				ImmutableDynamicScope[] newReferredDyscopes;

				if (oldReferredState == null)
					continue;
				newReferredDyscopes = oldReferredState.copyScopes();
				// Rename symbolic expressions in each dynamic scope of the
				// referred state:
				for (int k = 0; k < newReferredDyscopes.length; k++) {
					ImmutableDynamicScope tmp = newReferredDyscopes[k]
							.updateSymbolicConstants(renamer);

					unchange &= tmp == newReferredDyscopes[k];
					newReferredDyscopes[k] = tmp;
				}

				// Rename symbolic expressions in permanent path condition of
				// the referred state:
				BooleanExpression tmp, newPathCondition = oldReferredState
						.getPermanentPathCondition();

				tmp = (BooleanExpression) renamer.apply(newPathCondition);
				unchange &= tmp == newPathCondition;
				newPathCondition = tmp;

				// Rename symbolic expressions in write set stack and partial
				// path condition stack in each process state:
				newReferredState = applyToProcessStates(oldReferredState,
						renamer);
				unchange &= newReferredState == oldReferredState;
				if (!unchange) {
					int newStateKey;

					newReferredState = ImmutableState.newState(newReferredState,
							null, newReferredDyscopes, newPathCondition);
					newReferredState = collectHavocVariablesInReferredStates(
							newReferredState, renamer,
							collectReferredStateDepth - 1);
					// no need to collect scopes, processes and symbolic
					// constants again:
					newStateKey = saveState(newReferredState).left;
					substituteMap.put(oldStateValue, stateType.buildStateValue(
							universe, newStateKey, oldStateScopeMapping));
				}
			}
			stateValueUpdater = universe.mapSubstituter(substituteMap);
			// instantiate it at first time:
			if (newDyscopes == null)
				newDyscopes = new ImmutableDynamicScope[state.numDyscopes()];
			newDyscopes[refStateDyId] = state.getDyscope(refStateDyId)
					.updateSymbolicConstants(stateValueUpdater);
			changedDyscopes.set(refStateDyId);
		}
		if (!changedDyscopes.isEmpty()) {
			for (int i = 0; i < newDyscopes.length; i++)
				if (!changedDyscopes.get(i))
					newDyscopes[i] = state.getDyscope(i);
		} else
			assert newDyscopes == null;
		return ImmutableState.newState(state, null, newDyscopes, null);
	}

	/**
	 * Simplify states which are referred by $state variables in the current
	 * state with the context of the current state.
	 * 
	 * @param state
	 *            The current state.
	 * @param context
	 *            The permanent path condition of the current state.
	 * @param depth
	 *            The depth of simplification of referred states. To understand
	 *            the depth, see {@link #NORMALIZE_REFERRED_STATES_DEPTH}
	 * @return A new state which is the same as the current state but referred
	 *         states are updated.
	 */
	private ImmutableState simplifyReferencedStates(ImmutableState state,
			BooleanExpression context, int depth) {
		if (!config.isEnableMpiContract())
			return state;
		if (depth <= 0)
			return state;

		int numDyscopes = state.numDyscopes();
		Map<SymbolicExpression, SymbolicExpression> old2NewStateRefs = new TreeMap<>(
				universe.comparator());
		BitSet changedDysId = new BitSet(numDyscopes);
		UnaryOperator<SymbolicExpression> stateValueUpdater;
		List<Pair<Integer, List<SymbolicExpression>>> dyScopeReferedStatePairs = getStateReferences(
				state);
		ImmutableDynamicScope newDyscopes[] = null;
		CIVLStateType stateType = typeFactory.stateType();

		for (Pair<Integer, List<SymbolicExpression>> pair : dyScopeReferedStatePairs) {
			int refStateDysId = pair.left;

			for (SymbolicExpression oldStateValue : pair.right) {
				int oldStateKey = stateType.selectStateKey(universe,
						oldStateValue);
				SymbolicExpression scopeStateToRealMap = stateType
						.selectScopeValuesMap(universe, oldStateValue);
				ImmutableState oldRefState = collateStateStorage
						.getSavedState(oldStateKey);
				ImmutableState newRefState;
				Reasoner reasoner;
				UnaryOperator<SymbolicExpression> scopeIDReferred2CurrSubstituter;

				if (oldRefState == null)
					continue;
				/*
				 * construct the substituter that maps scope IDs of the current
				 * state to the scope IDs in referred state ...
				 */
				SymbolicExpression scopeValueReferred2Curr[] = symbolicUtil
						.symbolicArrayToConcreteArray(scopeStateToRealMap);
				int scopeIDReferred2Curr[] = new int[scopeValueReferred2Curr.length];
				int scopeIDCurr2Referred[] = new int[state.numDyscopes()];
				BooleanExpression mappedContext;

				for (int i = 0; i < scopeValueReferred2Curr.length; i++)
					scopeIDReferred2Curr[i] = getDyscopeId(
							scopeValueReferred2Curr[i]);
				mapReverse(scopeIDReferred2Curr, scopeIDCurr2Referred);
				scopeIDReferred2CurrSubstituter = universe
						.mapSubstituter(scopeSubMap(scopeIDCurr2Referred));
				mappedContext = (BooleanExpression) scopeIDReferred2CurrSubstituter
						.apply(context);
				reasoner = universe.reasoner(mappedContext);

				/*
				 * Recursively simplify states referred by variables in this
				 * state
				 */
				newRefState = simplifyReferencedStates(oldRefState,
						mappedContext, depth - 1);
				/*
				 * Update the path condition of the referred state with the
				 * current context. Current context should be stronger than (or
				 * equivalent to) the old path condition...
				 */
				newRefState = newRefState
						.setPermanentPathCondition(mappedContext);
				/*
				 * Note that here must use full context (from the reasoner) to
				 * simplify the referred state. It is incorrect to use reduced
				 * context, because equations like X=0 in the path condition
				 * will be removed from the full context (the reasoner knows
				 * that X should be replaced with 0 but this reasoner will not
				 * be used).
				 */
				newRefState = simplifyWork(newRefState, false);
				if (newRefState == oldRefState)
					continue;

				int newRefStateKey = saveState(newRefState).left;

				old2NewStateRefs.put(oldStateValue, stateType.buildStateValue(
						universe, newRefStateKey, scopeStateToRealMap));
			}
			stateValueUpdater = universe.mapSubstituter(old2NewStateRefs);
			// If it's first time, instantiate it:
			if (newDyscopes == null)
				newDyscopes = new ImmutableDynamicScope[numDyscopes];
			newDyscopes[refStateDysId] = state.getDyscope(refStateDysId)
					.updateSymbolicConstants(stateValueUpdater);
			changedDysId.set(refStateDysId);
			// Clear before re-used
			old2NewStateRefs.clear();
		}
		if (!changedDysId.isEmpty()) {
			for (int d = 0; d < numDyscopes; d++)
				if (!changedDysId.get(d))
					newDyscopes[d] = state.getDyscope(d);
		} else
			assert newDyscopes == null;
		return ImmutableState.newState(state, null, newDyscopes, null);
	}

	private List<SymbolicExpression> getSubExpressionsOfType(SymbolicType type,
			SymbolicExpression expr) {
		if (expr.isNull())
			return new ArrayList<>(0);
		if (expr.type().equals(type))
			return Arrays.asList(expr);

		List<SymbolicExpression> result = new LinkedList<>();
		int numObjects = expr.numArguments();

		for (int i = 0; i < numObjects; i++) {
			SymbolicObject arg = expr.argument(i);

			if (arg == null)
				continue;
			if (arg instanceof SymbolicExpression) {
				result.addAll(getSubExpressionsOfType(type,
						(SymbolicExpression) arg));
			} else if (arg instanceof SymbolicSequence) {
				@SuppressWarnings("unchecked")
				SymbolicSequence<SymbolicExpression> sequence = (SymbolicSequence<SymbolicExpression>) arg;
				int numEle = sequence.size();

				for (int j = 0; j < numEle; j++) {
					SymbolicExpression ele = sequence.get(j);

					if (ele != null)
						result.addAll(getSubExpressionsOfType(type, ele));
				}
			}
		}
		return result;
	}

	@Override
	public SymbolicUniverse symbolicUniverse() {
		return universe;
	}

	@Override
	public Pair<State, SymbolicExpression> malloc(State state, int dyscopeId,
			int mallocId, SymbolicExpression heapObject) {
		DynamicScope dyscope = state.getDyscope(dyscopeId);
		IntObject indexObj = universe.intObject(mallocId);
		SymbolicExpression heapValue = dyscope.getValue(0);
		SymbolicExpression heapField;
		SymbolicExpression heapAtomicObjectPtr;
		ReferenceExpression symRef;
		NumericExpression heapLength;

		if (heapValue.isNull())
			heapValue = typeFactory.heapType().getInitialValue();
		heapField = universe.tupleRead(heapValue, indexObj);
		heapLength = universe.length(heapField);
		heapField = universe.append(heapField, heapObject);
		heapValue = universe.tupleWrite(heapValue, indexObj, heapField);
		state = setVariable(state, 0, dyscopeId, heapValue);
		symRef = universe.identityReference();
		symRef = universe.tupleComponentReference(symRef, indexObj);
		symRef = universe.arrayElementReference(symRef, heapLength);
		symRef = universe.arrayElementReference(symRef, universe.zeroInt());
		heapAtomicObjectPtr = symbolicUtil.makePointer(dyscopeId, 0, symRef);
		return new Pair<>(state, heapAtomicObjectPtr);
	}

	@Override
	public Pair<State, SymbolicExpression> malloc(State state, int pid,
			int dyscopeId, int mallocId, SymbolicType elementType,
			NumericExpression elementCount) {
		DynamicScope dyscope = state.getDyscope(dyscopeId);
		SymbolicExpression heapValue = dyscope.getValue(0).isNull()
				? typeFactory.heapType().getInitialValue()
				: dyscope.getValue(0);
		IntObject index = universe.intObject(mallocId);
		SymbolicExpression heapField = universe.tupleRead(heapValue, index);
		int length = ((IntegerNumber) universe
				.extractNumber(universe.length(heapField))).intValue();
		StringObject heapObjectName = universe.stringObject(
				"Hp" + pid + "s" + dyscopeId + "f" + mallocId + "o" + length);
		SymbolicType heapObjectType = universe.arrayType(elementType,
				elementCount);
		SymbolicExpression heapObject = universe
				.symbolicConstant(heapObjectName, heapObjectType);

		return this.malloc(state, dyscopeId, mallocId, heapObject);
	}

	@Override
	public State deallocate(State state, SymbolicExpression heapObjectPointer,
			SymbolicExpression scopeOfPointer, int mallocId, int index) {
		int dyscopeId = getDyscopeId(scopeOfPointer);
		SymbolicExpression heapValue = state.getDyscope(dyscopeId).getValue(0);
		IntObject mallocIndex = universe.intObject(mallocId);
		SymbolicExpression heapField = universe.tupleRead(heapValue,
				mallocIndex);
		ImmutableState theState = (ImmutableState) state;
		SymbolicExpression oldVal = universe.arrayRead(heapField,
				universe.integer(index));
		SymbolicType oldValType = oldVal.type();

		assert oldValType instanceof SymbolicCompleteArrayType;
		heapField = universe.arrayWrite(heapField, universe.integer(index),
				symbolicUtil.invalidHeapObject(oldValType));
		heapValue = universe.tupleWrite(heapValue, mallocIndex, heapField);
		theState = this.setVariable(theState, 0, dyscopeId, heapValue);
		return theState;
	}

	/* *************************** Private Methods ************************* */

	/**
	 * Adds a new initial process state to the given state.
	 * 
	 * @param state
	 *            The old state.
	 * @param selfDestructable
	 *            If the created process is self-destructable
	 * @return A new instance of state with only the process states changed.
	 */
	protected ImmutableState createNewProcess(State state,
			boolean selfDestructable) {
		ImmutableState theState = (ImmutableState) state;
		int numProcs = theState.numProcs();
		ImmutableProcessState[] newProcesses;

		newProcesses = theState.copyAndExpandProcesses();
		newProcesses[numProcs] = new ImmutableProcessState(numProcs,
				selfDestructable);
		theState = theState.setProcessStates(newProcesses);
		return theState;
	}

	/**
	 * Creates a dyscope in its initial state.
	 * 
	 * @param lexicalScope
	 *            The lexical scope corresponding to this dyscope.
	 * @param parent
	 *            The parent of this dyscope. -1 only for the topmost dyscope.
	 * @return A new dynamic scope.
	 */
	private ImmutableDynamicScope initialDynamicScope(Scope lexicalScope,
			int parent, int dynamicScopeId, BitSet reachers) {
		return new ImmutableDynamicScope(lexicalScope, parent,
				initialValues(lexicalScope), reachers);
	}

	/**
	 * Creates the initial value of a given lexical scope.
	 * 
	 * @param lexicalScope
	 *            The lexical scope whose variables are to be initialized.
	 * @return An array of initial values of variables of the given lexical
	 *         scope.
	 */
	protected SymbolicExpression[] initialValues(Scope lexicalScope) {
		// TODO: special handling for input variables in root scope?
		SymbolicExpression[] values = new SymbolicExpression[lexicalScope
				.numVariables()];

		for (int i = 0; i < values.length; i++) {
			values[i] = universe.nullExpression();
		}
		return values;
	}

	// /**
	// * Checks if a heap is null or empty.
	// *
	// * @param heapValue
	// * The value of the heap to be checked.
	// * @return True iff the heap has null value or is empty.
	// */
	// private boolean isEmptyHeap(SymbolicExpression heapValue) {
	// if (heapValue.isNull())
	// return true;
	// else {
	// SymbolicSequence<?> heapFields = (SymbolicSequence<?>) heapValue
	// .argument(0);
	// int count = heapFields.size();
	//
	// for (int i = 0; i < count; i++) {
	// SymbolicExpression heapField = heapFields.get(i);
	// SymbolicSequence<?> heapFieldObjets = (SymbolicSequence<?>) heapField
	// .argument(0);
	// int size = heapFieldObjets.size();
	//
	// for (int j = 0; j < size; j++) {
	// SymbolicExpression heapFieldObj = heapFieldObjets.get(j);
	// SymbolicObject heapFieldObjValue = heapFieldObj.argument(0);
	//
	// if (heapFieldObjValue.symbolicObjectKind() == SymbolicObjectKind.STRING)
	// {
	// String value = ((StringObject) heapFieldObjValue)
	// .getString();
	//
	// if (value.equals("UNDEFINED"))
	// continue;
	// }
	// return false;
	// }
	// }
	// }
	// return true;
	// }

	/**
	 * Given two static scopes, this method computes a non-empty sequence of
	 * scopes with the following properties:
	 * <ul>
	 * <li>The first (0-th) element of the sequence is the join of scope1 and
	 * scope2.</li>
	 * <li>The last element is scope2.</li>
	 * <li>For each i (0<=i<length-1), the i-th element is the parent of the
	 * (i+1)-th element.</li>
	 * </ul>
	 * 
	 * @param scope1
	 *            a static scope
	 * @param scope2
	 *            a static scope
	 * @return join sequence as described above
	 * 
	 * @exception IllegalArgumentException
	 *                if the scopes do not have a common ancestor
	 */
	private Scope[] joinSequence(Scope scope1, Scope scope2) {
		if (scope1 == scope2)
			return new Scope[]{scope2};
		for (Scope scope1a = scope1; scope1a != null; scope1a = scope1a
				.parent())
			for (Scope scope2a = scope2; scope2a != null; scope2a = scope2a
					.parent())
				if (scope1a.equals(scope2a)) {
					Scope join = scope2a;
					int length = 1;
					Scope[] result;
					Scope s;

					for (s = scope2; s != join; s = s.parent())
						length++;
					result = new Scope[length];
					s = scope2;
					for (int i = length - 1; i >= 0; i--) {
						result[i] = s;
						s = s.parent();
					}
					return result;
				}
		throw new IllegalArgumentException(
				"No common scope:\n" + scope1 + "\n" + scope2);
	}

	/**
	 * Numbers the reachable dynamic scopes in a state in a canonical way.
	 * Scopes are numbered from 0 up, in the order in which they are encountered
	 * by iterating over the processes by increasing ID, iterating over the
	 * process' call stack frames from index 0 up, iterating over the parent
	 * scopes from the scope referenced by the frame.
	 * 
	 * Unreachable scopes are assigned the number -1.
	 * 
	 * Returns an array which of length numScopes in which the element at
	 * position i is the new ID number for the scope whose old ID number is i.
	 * Does not modify anything.
	 * 
	 * @param state
	 *            a state
	 * @return an array mapping old scope IDs to new.
	 */
	private int[] numberScopes(ImmutableState state) {
		int numScopes = state.numDyscopes();
		int numProcs = state.numProcs();
		int[] oldToNew = new int[numScopes];
		int nextScopeId = 1;

		// the root dyscope is forced to be 0
		oldToNew[0] = 0;
		for (int i = 1; i < numScopes; i++)
			oldToNew[i] = ModelConfiguration.DYNAMIC_NULL_SCOPE;
		for (int pid = 0; pid < numProcs; pid++) {
			ImmutableProcessState process = state.getProcessState(pid);
			int stackSize;

			if (process == null)
				continue;
			stackSize = process.stackSize();
			// start at bottom of stack so system scope in proc 0
			// is reached first
			for (int i = stackSize - 1; i >= 0; i--) {
				int dynamicScopeId = process.getStackEntry(i).scope();

				while (oldToNew[dynamicScopeId] < 0) {
					oldToNew[dynamicScopeId] = nextScopeId;
					nextScopeId++;
					dynamicScopeId = state.getParentId(dynamicScopeId);
					if (dynamicScopeId < 0)
						break;
				}
			}
		}
		return oldToNew;
	}

	/**
	 * Checks if a given claim is not satisfiable.
	 * 
	 * @param claim
	 *            The given claim.
	 * @return True iff the given claim is evaluated to be false.
	 */
	private boolean nsat(BooleanExpression claim) {
		if (config.sliceAnalysis() || config.svcomp()) {
			return trueContextReasoner.unsat(claim)
					.getResultType() == ResultType.YES;
		} else {
			return trueContextReasoner.unsat(claim)
					.getResultType() == ResultType.YES;
		}
	}

	/**
	 * Creates a map of process value's according to PID map from old PID to new
	 * PID.
	 * 
	 * @param oldToNewPidMap
	 *            The map of old PID to new PID, i.e, oldToNewPidMap[old PID] =
	 *            new PID.
	 * @return The map of process value's from old process value to new process
	 *         value.
	 */
	private Map<SymbolicExpression, SymbolicExpression> procSubMap(
			int[] oldToNewPidMap) {
		int size = oldToNewPidMap.length;
		Map<SymbolicExpression, SymbolicExpression> result = new HashMap<SymbolicExpression, SymbolicExpression>(
				size);

		for (int i = 0; i < size; i++) {
			SymbolicExpression oldVal = processValue(i);
			SymbolicExpression newVal = processValue(oldToNewPidMap[i]);

			result.put(oldVal, newVal);
		}
		return result;
	}

	/**
	 * General method for pushing a frame onto a call stack, whether or not the
	 * call stack is for a new process (and therefore empty).
	 * 
	 * @param state
	 *            the initial state
	 * @param pid
	 *            the PID of the process whose stack is to be modified; this
	 *            stack may be empty
	 * @param function
	 *            the called function that will be pushed onto the stack
	 * @param functionCallParentDyscope
	 *            The dyscope ID of the parent of the new function; If the
	 *            caller has no knowledge about what is suppose to be the
	 *            correct parent scope, caller can pass "-1" for this argument.
	 *            This method will attempt to use the dyscope of the static
	 *            parent scope of the function definition as the parent dyscope.
	 *            If this is not the case you want, don't pass '-1' here.
	 * @param arguments
	 *            the arguments to the function
	 * @param callerPid
	 *            the PID of the process that is creating the new frame. For an
	 *            ordinary function call, this will be the same as pid. For a
	 *            "spawn" command, callerPid will be different from pid and
	 *            process pid will be new and have an empty stack. Exception: if
	 *            callerPid is -1 then the new dynamic scope will have no
	 *            parent; this is used for pushing the original system function,
	 *            which has no caller
	 * @return new stack with new frame on call stack of process pid
	 */
	protected ImmutableState pushCallStack2(ImmutableState state, int pid,
			CIVLFunction function, int functionCallParentDyscope,
			SymbolicExpression[] arguments, int callerPid) {
		Scope StaticFuncDefiParent = function.containingScope();
		Scope StaticFuncOuter = function.outerScope();
		ImmutableProcessState[] newProcesses = state.copyProcessStates();
		int numScopes = state.numDyscopes();
		SymbolicExpression[] values;
		ImmutableDynamicScope[] newScopes;
		int sid;
		int functionCallParentDyscopeId = functionCallParentDyscope;
		BitSet bitSet = new BitSet(newProcesses.length);

		if (functionCallParentDyscopeId < 0 && callerPid >= 0) {
			// Find a dynamic instance of the static parent scope of the calle
			// function definition as the parent scope:
			ProcessState caller = state.getProcessState(callerPid);
			ImmutableDynamicScope containingDynamicScope;

			functionCallParentDyscopeId = caller.getDyscopeId();
			while (functionCallParentDyscopeId >= 0) {
				containingDynamicScope = (ImmutableDynamicScope) state
						.getDyscope(functionCallParentDyscopeId);
				// TODO: why comparing with "containingStaticScope" ? When
				// you push a function f, the parent dyscope of the called f
				// is not necessarily the static parent scope of the
				// definitions of f (xxxx). I think this may be incorrect.
				if (StaticFuncDefiParent == containingDynamicScope
						.lexicalScope())
					break;
				functionCallParentDyscopeId = state
						.getParentId(functionCallParentDyscopeId);
			}
		}
		newScopes = state.copyAndExpandScopes();
		sid = numScopes;
		values = initialValues(StaticFuncOuter);
		for (int i = 0; i < arguments.length; i++)
			if (arguments[i] != null)
				values[i + 1] = arguments[i];
		bitSet.set(pid);
		newScopes[sid] = new ImmutableDynamicScope(StaticFuncOuter,
				functionCallParentDyscopeId, values, bitSet);

		int id = functionCallParentDyscopeId;
		ImmutableDynamicScope scope;

		while (id >= 0) {
			scope = newScopes[id];
			bitSet = newScopes[id].getReachers();
			if (bitSet.get(pid))
				break;
			bitSet = (BitSet) bitSet.clone();
			bitSet.set(pid);
			newScopes[id] = scope.setReachers(bitSet);
			id = scope.getParent();
		}

		newProcesses[pid] = state.getProcessState(pid)
				.push(stackEntry(null, sid));
		state = ImmutableState.newState(state, newProcesses, newScopes, null);
		if (!function.isSystemFunction()) {
			state = setLocation(state, pid, function.startLocation());
		}
		return state;
	}

	/**
	 * Creates a map of scope value's according to the given dyscope map from
	 * old dyscope ID to new dyscope ID.
	 * 
	 * @param oldToNewSidMap
	 *            The map of old dyscope ID to new dyscoep ID, i.e,
	 *            oldToNewSidMap[old dyscope ID] = new dyscope ID.
	 * @return The map of scope value's from old scope value to new scope value.
	 */
	private Map<SymbolicExpression, SymbolicExpression> scopeSubMap(
			int[] oldToNewSidMap) {
		int size = oldToNewSidMap.length;
		Map<SymbolicExpression, SymbolicExpression> result = new HashMap<SymbolicExpression, SymbolicExpression>(
				size);

		for (int i = 0; i < size; i++) {
			SymbolicExpression oldVal = scopeValue(i);
			SymbolicExpression newVal = scopeValue(oldToNewSidMap[i]);

			result.put(oldVal, newVal);
		}
		return result;
	}

	/**
	 * Given an array of dynamic scopes and a process state, computes the actual
	 * dynamic scopes reachable from that process and modifies the array as
	 * necessary by replacing a dynamic scope with a scope that is equivalent
	 * except for the corrected bit set.
	 * 
	 * @param dynamicScopes
	 *            an array of dynamic scopes, to be modified
	 * @param process
	 *            a process state
	 */
	private void setReachablesForProc(ImmutableDynamicScope[] dynamicScopes,
			ImmutableProcessState process) {
		int stackSize = process.stackSize();
		int numScopes = dynamicScopes.length;
		boolean reached[] = new boolean[numScopes];
		int pid = process.getPid();

		for (int i = 0; i < stackSize; i++) {
			StackEntry frame = process.getStackEntry(i);
			int id = frame.scope();

			while (id >= 0) {
				if (reached[id])
					break;
				reached[id] = true;
				id = dynamicScopes[id].getParent();
			}
		}
		for (int j = 0; j < numScopes; j++) {
			ImmutableDynamicScope scope = dynamicScopes[j];
			BitSet bitSet = scope.getReachers();

			if (bitSet.get(pid) != reached[j]) {
				BitSet newBitSet = (BitSet) bitSet.clone();

				newBitSet.flip(pid);
				dynamicScopes[j] = dynamicScopes[j].setReachers(newBitSet);
			}
		}
	}

	/**
	 * Create a new call stack entry.
	 * 
	 * @param location
	 *            The location to go to after returning from this call.
	 * @param dyscopeId
	 *            The dynamic scope the process is in before the call.
	 */
	protected ImmutableStackEntry stackEntry(Location location, int dyscopeId) {
		return new ImmutableStackEntry(location, dyscopeId);

	}

	/**
	 * Given a BitSet indexed by process IDs, and a map of old PIDs to new PIDs,
	 * returns a BitSet equivalent to original but indexed using the new PIDs.
	 * 
	 * If no changes are made, the original BitSet (oldBitSet) is returned.
	 * 
	 * @param oldBitSet
	 * @param oldToNewPidMap
	 *            array of length state.numProcs in which element at index i is
	 *            the new PID of the process whose old PID is i. A negative
	 *            value indicates that the process of (old) PID i is to be
	 *            removed.
	 * @return
	 */
	private BitSet updateBitSet(BitSet oldBitSet, int[] oldToNewPidMap) {
		BitSet newBitSet = null;
		int length = oldBitSet.length();

		for (int i = 0; i < length; i++) {
			boolean flag = oldBitSet.get(i);

			if (flag) {
				int newIndex = oldToNewPidMap[i];

				if (newIndex >= 0) {
					if (newBitSet == null)
						newBitSet = new BitSet(length);
					newBitSet.set(newIndex);
				}
			}
		}
		if (newBitSet == null)
			return oldBitSet;
		return newBitSet;
	}

	/**
	 * Searches the dynamic scopes in the given state for any process reference
	 * value, and returns a new array of scopes equivalent to the old except
	 * that those process reference values have been replaced with new specified
	 * values. Used for garbage collection and canonicalization of PIDs.
	 * 
	 * Also updates the reachable BitSet in each DynamicScope: create a new
	 * BitSet called newReachable. iterate over all entries in old BitSet
	 * (reachable). If old entry is position i is true, set oldToNewPidMap[i] to
	 * true in newReachable (assuming oldToNewPidMap[i]>=0).
	 * 
	 * The method returns null if no changes were made.
	 * 
	 * @param state
	 *            a state
	 * @param oldToNewPidMap
	 *            array of length state.numProcs in which element at index i is
	 *            the new PID of the process whose old PID is i. A negative
	 *            value indicates that the process of (old) PID i is to be
	 *            removed.
	 * @return new dynamic scopes or null
	 */
	private ImmutableDynamicScope[] updateProcessReferencesInScopes(State state,
			int[] oldToNewPidMap) {
		Map<SymbolicExpression, SymbolicExpression> procSubMap = procSubMap(
				oldToNewPidMap);
		UnaryOperator<SymbolicExpression> substituter = universe
				.mapSubstituter(procSubMap);
		ImmutableDynamicScope[] newScopes = null;
		int numScopes = state.numDyscopes();

		for (int i = 0; i < numScopes; i++) {
			ImmutableDynamicScope dynamicScope = (ImmutableDynamicScope) state
					.getDyscope(i);
			Scope staticScope = dynamicScope.lexicalScope();
			Collection<Variable> procrefVariableIter = staticScope
					.variablesWithProcrefs();
			SymbolicExpression[] newValues = null;
			BitSet oldBitSet = dynamicScope.getReachers();
			BitSet newBitSet = updateBitSet(oldBitSet, oldToNewPidMap);

			for (Variable variable : procrefVariableIter) {
				int vid = variable.vid();
				SymbolicExpression oldValue = dynamicScope.getValue(vid);
				SymbolicExpression newValue = substituter.apply(oldValue);

				if (oldValue != newValue) {
					if (newValues == null)
						newValues = dynamicScope.copyValues();
					newValues[vid] = newValue;
				}
			}
			if (newValues != null || newBitSet != oldBitSet) {
				if (newScopes == null) {
					newScopes = new ImmutableDynamicScope[numScopes];
					for (int j = 0; j < i; j++)
						newScopes[j] = (ImmutableDynamicScope) state
								.getDyscope(j);
				}
				if (newValues == null)
					newScopes[i] = dynamicScope.setReachers(newBitSet);
				else
					newScopes[i] = new ImmutableDynamicScope(staticScope,
							dynamicScope.getParent(), newValues, newBitSet);
			} else if (newScopes != null) {
				newScopes[i] = dynamicScope;
			}
		}
		return newScopes;
	}

	private Set<SymbolicExpression> reachableHeapObjectsOfState(State state) {
		Set<SymbolicExpression> reachable = new LinkedHashSet<>();
		int numDyscopes = state.numDyscopes();

		for (int i = 0; i < numDyscopes; i++) {
			DynamicScope dyscope = state.getDyscope(i);
			int numVars = dyscope.numberOfValues();

			for (int vid = 0; vid < numVars; vid++) {
				SymbolicExpression value = dyscope.getValue(vid);

				reachableHeapObjectsOfValue(state, value, reachable);
			}
		}
		return reachable;
	}

	@SuppressWarnings("incomplete-switch")
	private void reachableHeapObjectsOfValue(State state,
			SymbolicExpression value, Set<SymbolicExpression> reachable) {
		if (value.isNull())
			return;
		else if (!this.isPointer(value)) {
			int numArgs = value.numArguments();

			for (int i = 0; i < numArgs; i++) {
				SymbolicObject arg = value.argument(i);
				SymbolicObjectKind kind = arg.symbolicObjectKind();

				switch (kind) {
					case BOOLEAN :
					case INT :
					case NUMBER :
					case STRING :
					case CHAR :
					case TYPE :
					case TYPE_SEQUENCE :
						break;
					default :
						switch (kind) {
							case EXPRESSION :
								reachableHeapObjectsOfValue(state,
										(SymbolicExpression) arg, reachable);
								break;
							case SEQUENCE : {
								Iterator<? extends SymbolicExpression> iter = ((SymbolicSequence<?>) arg)
										.iterator();

								while (iter.hasNext()) {
									SymbolicExpression expr = iter.next();

									reachableHeapObjectsOfValue(state, expr,
											reachable);
								}
							}
						}
				}
			}
		} else if (value.operator() != SymbolicOperator.TUPLE) {
			return;
		} else if (symbolicUtil.isPointerToHeap(value)) {
			SymbolicExpression heapObjPtr = this.symbolicUtil
					.heapMemUnit(value);

			// if (!reachable.contains(heapObjPtr))
			reachable.add(heapObjPtr);
		} else if (value.type()
				.equals(this.typeFactory.pointerSymbolicType())) {
			// other pointers
			SymbolicExpression scopeVal = symbolicUtil.getScopeValue(value);
			int dyscopeId = scopeValueToDyscopeID.apply(scopeVal).intValue();

			if (dyscopeId >= 0) {
				int vid = this.symbolicUtil.getVariableId(null, value);
				ReferenceExpression reference = this.symbolicUtil
						.getSymRef(value);
				SymbolicExpression varValue = state.getVariableValue(dyscopeId,
						vid);
				SymbolicExpression objectValue;

				try {
					objectValue = this.universe.dereference(varValue,
							reference);
				} catch (SARLException e) {
					return;
				}
				reachableHeapObjectsOfValue(state, objectValue, reachable);
			}
		}
	}

	private boolean isPointer(SymbolicExpression value) {
		if (value.type().equals(typeFactory.pointerSymbolicType()))
			return true;
		return false;
	}

	private boolean hasNonEmptyHeaps(State state) {
		int numDyscopes = state.numDyscopes();

		for (int dyscopeId = 0; dyscopeId < numDyscopes; dyscopeId++) {
			DynamicScope dyscope = state.getDyscope(dyscopeId);
			SymbolicExpression heap = dyscope.getValue(0);

			if (!heap.isNull())
				return true;
		}
		return false;
	}

	private void computeOldToNewHeapPointers(State state,
			Map<SymbolicExpression, SymbolicExpression> heapMemUnitsMap,
			Map<SymbolicExpression, SymbolicExpression> oldToNewExpressions) {
		if (heapMemUnitsMap.size() < 1)
			return;
		else {
			int numDyscopes = state.numDyscopes();

			for (int dyscopeID = 0; dyscopeID < numDyscopes; dyscopeID++) {
				DynamicScope dyscope = state.getDyscope(dyscopeID);
				int numVars = dyscope.numberOfValues();

				for (int vid = 0; vid < numVars; vid++) {
					computeNewHeapPointer(dyscope.getValue(vid),
							heapMemUnitsMap, oldToNewExpressions);
				}
			}
		}
	}

	// /**
	// * renames all collectible symbolic constants. Note: this method should
	// only
	// * be called when necessary.
	// *
	// * @param state
	// * @return
	// */
	// private ImmutableState updateAllSymbols(ImmutableState state) {
	//
	// }

	@SuppressWarnings("incomplete-switch")
	private void computeNewHeapPointer(SymbolicExpression value,
			Map<SymbolicExpression, SymbolicExpression> heapMemUnitsMap,
			Map<SymbolicExpression, SymbolicExpression> oldToNewHeapPointers) {
		if (value.isNull())
			return;
		else if (!this.isPointer(value)) {
			int numArgs = value.numArguments();

			for (int i = 0; i < numArgs; i++) {
				SymbolicObject arg = value.argument(i);
				SymbolicObjectKind kind = arg.symbolicObjectKind();

				switch (kind) {
					case BOOLEAN :
					case INT :
					case NUMBER :
					case STRING :
					case CHAR :
					case TYPE :
					case TYPE_SEQUENCE :
						break;
					default :
						switch (kind) {
							case EXPRESSION :
								computeNewHeapPointer((SymbolicExpression) arg,
										heapMemUnitsMap, oldToNewHeapPointers);
								break;
							case SEQUENCE : {
								Iterator<? extends SymbolicExpression> iter = ((SymbolicSequence<?>) arg)
										.iterator();

								while (iter.hasNext()) {
									SymbolicExpression expr = iter.next();

									computeNewHeapPointer(expr, heapMemUnitsMap,
											oldToNewHeapPointers);
								}
							}
						}
				}
			}
		} else if (symbolicUtil.isPointerToHeap(value)) {
			SymbolicExpression heapObjPtr = this.symbolicUtil
					.heapMemUnit(value);
			SymbolicExpression newHeapObjPtr = heapMemUnitsMap.get(heapObjPtr);

			if (newHeapObjPtr != null
					&& !oldToNewHeapPointers.containsKey(value)) {
				if (newHeapObjPtr.isNull())
					oldToNewHeapPointers.put(value, newHeapObjPtr);
				else {
					ReferenceExpression ref = symbolicUtil
							.referenceToHeapMemUnit(value);
					SymbolicExpression newPointer = symbolicUtil
							.extendPointer(newHeapObjPtr, ref);

					oldToNewHeapPointers.put(value, newPointer);
				}
			}
		}
	}

	private void addOldToNewHeapMemUnits(Map<Integer, Integer> oldID2NewID,
			SymbolicExpression heapPointer, ReferenceExpression fieldRef,
			Map<SymbolicExpression, SymbolicExpression> oldToNewMap) {
		for (Map.Entry<Integer, Integer> entry : oldID2NewID.entrySet()) {
			ReferenceExpression oldRef = universe.arrayElementReference(
					fieldRef, universe.integer(entry.getKey()));
			SymbolicExpression oldPtr = this.symbolicUtil.setSymRef(heapPointer,
					oldRef);
			ReferenceExpression newRef = universe.arrayElementReference(
					fieldRef, universe.integer(entry.getValue()));
			SymbolicExpression newPtr = this.symbolicUtil.setSymRef(heapPointer,
					newRef);

			oldToNewMap.put(oldPtr, newPtr);
		}
	}

	/**
	 * Rename all symbolic constants of the state. Trying to use the new
	 * interface (canonicRenamer) provided by SARL.
	 * 
	 * @param state
	 * @return
	 * @throws CIVLHeapException
	 */
	private ImmutableState collectHavocVariables(State state)
			throws CIVLHeapException {
		ImmutableState theState = (ImmutableState) state;

		if (theState.collectibleCounts[ModelConfiguration.HAVOC_PREFIX_INDEX] < 1)
			return theState;

		int numDyscopes = theState.numDyscopes();
		CanonicalRenamer canonicRenamer = universe.canonicalRenamer(
				ModelConfiguration.SYMBOL_PREFIXES[ModelConfiguration.HAVOC_PREFIX_INDEX],
				this.isReservedSymbolicConstant);
		ImmutableDynamicScope[] newScopes = new ImmutableDynamicScope[numDyscopes];
		boolean change = false;

		for (int dyscopeId = 0; dyscopeId < numDyscopes; dyscopeId++) {
			ImmutableDynamicScope oldScope = theState.getDyscope(dyscopeId);
			ImmutableDynamicScope newScope = oldScope
					.updateSymbolicConstants(canonicRenamer);

			change = change || newScope != oldScope;
			newScopes[dyscopeId] = newScope;
		}
		if (!change)
			newScopes = null;

		BooleanExpression oldPathCondition = theState
				.getPermanentPathCondition();
		BooleanExpression newPathCondition = (BooleanExpression) canonicRenamer
				.apply(oldPathCondition);

		if (oldPathCondition == newPathCondition)
			newPathCondition = null;
		else
			change = true;

		ImmutableState tmpState = applyToProcessStates(theState,
				canonicRenamer);

		if (tmpState != theState) {
			theState = tmpState;
			change = true;
		}
		if (change) {
			theState = ImmutableState.newState(theState, null, newScopes,
					newPathCondition);
			theState = collectHavocVariablesInReferredStates(theState,
					canonicRenamer, NORMALIZE_REFERRED_STATES_DEPTH);
			theState = theState.updateCollectibleCount(
					ModelConfiguration.HAVOC_PREFIX_INDEX,
					canonicRenamer.getNumNewNames());
		}
		return theState;
	}

	@Override
	public ImmutableState setLocation(State state, int pid, Location location) {
		return this.setLocation(state, pid, location, false);
	}

	@Override
	public MemoryUnitFactory memUnitFactory() {
		return this.memUnitFactory;
	}

	@Override
	public Map<Variable, SymbolicExpression> inputVariableValueMap(
			State state) {
		Map<Variable, SymbolicExpression> result = new LinkedHashMap<>();

		// If the root process has no stack entry, return a empty map:
		if (state.getProcessState(0).stackSize() > 0) {
			// If the parameter is a merged state, the dynamic scope id of the
			// root
			// lexical scope may not be 0:
			int rootDysid = state.getDyscope(0,
					ModelConfiguration.STATIC_ROOT_SCOPE);

			for (Variable variable : this.inputVariables) {
				assert variable.scope()
						.id() == ModelConfiguration.STATIC_ROOT_SCOPE;
				result.put(variable,
						state.getVariableValue(rootDysid, variable.vid()));
			}
		}
		return result;
	}

	/* **************** MPI contracts related functions ******************* */
	/**
	 * Renumbers and re-arranges an array of {@link DynamicScope} with an
	 * "oldToNew" array as a dictionary which is used for looking up new IDs
	 * (indices) by indexing old IDs (indices).
	 * 
	 * @precondition The largest new index in "oldToNew" table should be less
	 *               than the length of the output array.
	 * @param oldDyscopes
	 *            An array of old {@link DynamicScope}
	 * @param oldToNew
	 *            An array as a dictionary which is used for looking up new IDs
	 *            by indexing old IDs.
	 * @param outputDyscopes
	 *            An array of new {@link DynamicScope}
	 * @param oldPathCondition
	 *            The old path condition which may contains expressions
	 *            involving old dyscope IDs.
	 * @return The new path condition which is obtained from substituting old
	 *         dyscope IDs with new ones on the oldPathCondition>
	 */
	private BooleanExpression renumberDyscopes(
			ImmutableDynamicScope[] oldDyscopes, int[] oldToNew,
			ImmutableDynamicScope[] outputDyscopes,
			BooleanExpression oldPathCondition) {
		IntArray key = new IntArray(oldToNew);
		UnaryOperator<SymbolicExpression> substituter = dyscopeSubMap.get(key);
		int numOldDyscopes = oldDyscopes.length;

		if (substituter == null) {
			substituter = universe.mapSubstituter(scopeSubMap(oldToNew));
			dyscopeSubMap.putIfAbsent(key, substituter);
		}

		for (int i = 0; i < numOldDyscopes; i++) {
			int newId = oldToNew[i];

			if (newId >= 0) {
				ImmutableDynamicScope oldScope = oldDyscopes[i];
				int oldParent = oldScope.getParent();
				// int oldParentIdentifier = oldScope.identifier();

				outputDyscopes[newId] = oldScope.updateDyscopeIds(substituter,
						universe,
						oldParent < 0 ? oldParent : oldToNew[oldParent]);
			}
		}
		return (BooleanExpression) substituter.apply(oldPathCondition);
	}

	@Override
	public State enterAtomic(State state, int pid) {
		ProcessState procState = state.getProcessState(pid);
		int atomicCount = procState.atomicCount();

		if (atomicCount == 0)
			state = getAtomicLock(state, pid);
		return this.setProcessState(state, procState.incrementAtomicCount());
	}

	@Override
	public State leaveAtomic(State state, int pid) {
		ProcessState procState = state.getProcessState(pid);
		int atomicCount = procState.atomicCount();

		if (atomicCount == 1)
			state = releaseAtomicLock(state);
		return this.setProcessState(state, procState.decrementAtomicCount());
	}

	@Override
	public Pair<State, SymbolicConstant> getFreshSymbol(State state, int index,
			SymbolicType type) {
		ImmutableState immutableState = (ImmutableState) state;
		int count = immutableState.collectibleCounts[index];
		SymbolicConstant newSymbol = universe.symbolicConstant(
				universe.stringObject(
						ModelConfiguration.SYMBOL_PREFIXES[index] + count),
				type);
		State newState = immutableState.updateCollectibleCount(index,
				count + 1);

		return new Pair<>(newState, newSymbol);
	}

	@Override
	public SymbolicExpression getStateSnapshot(State state, int pid,
			int topDyscope) {
		int scopeIDSnapshot2Curr[] = new int[state.numDyscopes()];
		Pair<Integer, State> snapshotAndID = getStateSnapshotWorker(state, pid,
				topDyscope, scopeIDSnapshot2Curr);
		State snapshot = snapshotAndID.right;
		int numDyscopesInSnapshot = snapshot.numDyscopes();
		SymbolicExpression scopeValues[] = new SymbolicExpression[numDyscopesInSnapshot];

		for (int i = 0; i < numDyscopesInSnapshot; i++)
			scopeValues[i] = this.scopeValue(scopeIDSnapshot2Curr[i]);
		return typeFactory.stateType().buildStateValue(universe,
				snapshotAndID.left,
				universe.array(typeFactory.scopeSymbolicType(), scopeValues));
	}

	/**
	 * Carve a mono state out of the current state.
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the process who is associated with the mono state
	 * @param topDyscope
	 *            the dyscope ID of the "top scope", up to which all the
	 *            dyscopes that are reachable by the given process will be
	 *            included by the result.
	 * @param scopeIDs2Curr
	 *            a map that maps dyscope IDs in the mono state to current state
	 * @return a pair consists of the state ID of the mono state and the mono
	 *         state itself.
	 */
	private Pair<Integer, State> getStateSnapshotWorker(State state, int pid,
			int topDyscope, int scopeIDs2Curr[]) {
		// Pre-condition: topDyscope must be reachable from the call stack of
		// pid in state:
		ImmutableState theState = (ImmutableState) state;
		ImmutableProcessState processState = theState.getProcessState(pid);

		while (!processState.hasEmptyStack()) {
			StackEntry entry = processState.peekStack();
			int reachableDyscope = entry.scope();
			boolean reachable = false;

			do {
				if (reachableDyscope == topDyscope) {
					reachable = true;
					break;
				}
				reachableDyscope = state.getDyscope(reachableDyscope)
						.getParent();
			} while (reachableDyscope >= 0);
			if (reachable)
				break;
			else
				processState = processState.pop();
		}
		for (int otherPid = 0; otherPid < theState.numProcs(); otherPid++)
			if (otherPid != pid)
				theState = theState.setProcessState(otherPid, null);
			else
				theState = theState.setProcessState(pid, processState);
		try {
			// Get rid of other processes and unrelated dyscopes:
			theState = collectProcesses(theState);

			int old2new[] = new int[theState.numDyscopes()];
			ImmutableState result = collectScopesWorker(theState,
					fullHeapErrorSet, old2new);

			mapReverse(old2new, scopeIDs2Curr);
			return saveState(result);
		} catch (CIVLHeapException e) {
			throw new CIVLInternalException(
					"Canonicalization with ignorance of all kinds of heap errors still throws an Heap Exception",
					state.getProcessState(pid).getLocation());
		}
	}

	@Override
	public SymbolicExpression addInternalProcess(SymbolicExpression stateValue,
			SymbolicExpression monoStateValue, int newPid, int nprocs,
			CIVLSource source) {
		CIVLStateType stateType = typeFactory.stateType();
		State state;
		SymbolicExpression[] state2CurrArray;
		SymbolicExpression scopeIDMono2Curr = stateType
				.selectScopeValuesMap(universe, monoStateValue);
		SymbolicExpression[] mono2CurrArray = symbolicUtil
				.symbolicArrayToConcreteArray(scopeIDMono2Curr);

		if (stateValue == modelFactory.statenullConstantValue()) {
			state = emptyState(nprocs);
			state2CurrArray = Arrays.copyOf(mono2CurrArray,
					mono2CurrArray.length);
		} else {
			int stateKey = stateType.selectStateKey(universe, stateValue);
			SymbolicExpression scopeIDState2Curr = stateType
					.selectScopeValuesMap(universe, stateValue);
			SymbolicExpression[] oldState2CurrArray = symbolicUtil
					.symbolicArrayToConcreteArray(scopeIDState2Curr);

			state = getStateByReference(stateKey);
			state2CurrArray = new SymbolicExpression[state.numDyscopes()
					+ mono2CurrArray.length];
			System.arraycopy(oldState2CurrArray, 0, state2CurrArray, 0,
					oldState2CurrArray.length);
		}

		int monoStateKey = stateType.selectStateKey(universe, monoStateValue);
		Pair<Integer, State> newStateAndID = addInternalProcessWorker(state,
				getStateByReference(monoStateKey), newPid, state2CurrArray,
				mono2CurrArray);
		SymbolicExpression[] trimmedState2RealArray = new SymbolicExpression[newStateAndID.right
				.numDyscopes()];

		for (int i = 0; i < trimmedState2RealArray.length; i++)
			trimmedState2RealArray[i] = state2CurrArray[i];
		return stateType.buildStateValue(universe, newStateAndID.left,
				universe.array(typeFactory.scopeSymbolicType(),
						trimmedState2RealArray));
	}

	/**
	 * Merge a mono state to a existing state
	 * 
	 * @param state
	 *            the existing state
	 * @param monoState
	 *            the mono state
	 * @param newPid
	 *            the new PID for the process , which is associated with the
	 *            mono state, in the result merged state
	 * @param scopeValueState2Curr
	 *            INPUT/OUTPUT. As INPUT, this array maps scope values of the
	 *            given state, which is a merged state, to scope values of
	 *            current state. As OUTPUT, this array will be concatenated with
	 *            another map from scope values of the merging mono state to
	 *            current state. This array must be large enough for
	 *            concatenation, i.e. there are enough suffix spaces in the
	 *            array for taking new values from another map
	 * @param scopeValueMono2Curr
	 *            INPUT. this array maps scope values of the mono state to scope
	 *            values of current state
	 * @return
	 */
	// this method is quite long hence I use comments to partition each
	// segment...
	private Pair<Integer, State> addInternalProcessWorker(State state,
			State monoState, int newPid,
			SymbolicExpression scopeValueState2Curr[],
			SymbolicExpression scopeValueMono2Curr[]) {
		assert monoState.numProcs() == 1;
		ImmutableState theMono = (ImmutableState) monoState;
		ImmutableState theState = (ImmutableState) state;
		ImmutableState theResult;
		ImmutableDynamicScope dyscopes[];
		ImmutableProcessState[] processes;
		// Change the PID of the mono process to newPid:
		ImmutableProcessState monoProcess = theMono.getProcessState(0)
				.setPid(newPid);
		Scope leastCommonAncestor;
		/*
		 * This variable denotes if this is the first time a monoState being
		 * merged to an empty state. The invariants of this method in fact hold
		 * for any i-th time of merging a monoState, where i >= 0 and i <
		 * theState.numProcs(). This variable is only used for expressing
		 * properties checked by java assert ...
		 */
		boolean first = true;
		// The scope of the bottom entry in a process call stack is the process
		// scope. (the 'root' scope of the process)
		int bottomDyscopeId = monoProcess
				.getStackEntry(monoProcess.stackSize() - 1).scope();
		DynamicScope monoProcScope;

		monoProcScope = monoState.getDyscope(bottomDyscopeId);
		leastCommonAncestor = monoState.getDyscope(monoProcScope.getParent())
				.lexicalScope();
		/*
		 * For the initial case, there is only one process state, so the
		 * invariants must hold; Then for each time adding a new process state
		 * to the state, it always looking for the least common ancestor (LCA)
		 * scope in between the new process scope and the LCA of all processes
		 * in the state, thus the new LCA can only either be the old LCA or an
		 * ancestor of the old LCA. It is guaranteed that LCA and any ancestor
		 * of LCA has only one dyscope in the state:
		 */
		processes = theState.copyProcessStates();
		assert theState.numLiveProcs() > 0;
		for (ImmutableProcessState process : processes)
			if (!process.hasEmptyStack()) {
				Scope otherProcScope;

				first = false;
				bottomDyscopeId = process.getStackEntry(process.stackSize() - 1)
						.scope();
				otherProcScope = theState.getDyscope(bottomDyscopeId)
						.lexicalScope().function().outerScope();
				leastCommonAncestor = modelFactory.leastCommonAncestor(
						leastCommonAncestor, otherProcScope);
			}

		ImmutableDynamicScope monoDyscopes[] = theMono.copyScopes();
		// map from dyscope IDs in mono to dyscope IDs in the result state:
		int dyscopeMono2State[] = new int[monoDyscopes.length];
		int counter = theState.numDyscopes();
		BooleanExpression newMonoPC;

		/*
		 * For any dyscope whose scope is NOT an ancestor of the LCA (or NOT
		 * equal to LCA), then it is taken as a local dyscope (i.e. only
		 * reachable by the new process and will be added into the collate state
		 * as a new dyscope), otherwise it will replace the one already in the
		 * collate state (which means the shared dyscopes are updated)...
		 */

		/*
		 * For readability, an example is presented here:
		 * 
		 * Two processes are trying to merge into an (initially) empty state
		 * (merged state). The first process has dyscope array [0,1,2,3] and
		 * dyscope 1 is the default LCA. The following loop will construct a
		 * old2new array: [0,1,2,3]. Then the 4 dyscopes of the first process
		 * are added into the merged state.
		 * 
		 * Later the second process with dyscopes [0',1',2',3'] are trying to
		 * merge. 1' is the LCA then the following loop will construct a old2new
		 * array: [0, 1, 4, 5]. Then the dyscopes in the merged state will
		 * eventually be updated to [0', 1', 2, 3, 2', 3']. Note that shared
		 * dyscopes are updated.
		 */
		for (int i = 0; i < monoDyscopes.length; i++) {
			Scope currentScope = monoDyscopes[i].lexicalScope();

			// If current scope is an ancestor or equals to the LCA, it will be
			// a shared scope:
			if (leastCommonAncestor.isDescendantOf(currentScope)
					|| leastCommonAncestor.id() == currentScope.id()) {
				int uniqueDyscopeId = -1;

				for (int d = 0; d < theState.numDyscopes(); d++)
					if (theState.getDyscope(d).lexicalScope()
							.id() == currentScope.id()) {
						uniqueDyscopeId = d;
						// start from root dyscope in top-down order , the first
						// encountered dyscope d (top-most such d), which has
						// the same lexical scope as the current (lexical)
						// scope, is the shared one. Thus, no need continue
						// searching.
						break;
					}
				if (uniqueDyscopeId >= 0)
					dyscopeMono2State[i] = uniqueDyscopeId;
				else {
					/*
					 * If currentScope is an ancestor of LCA, there must be a
					 * dyscope in the merged state which is an instance of
					 * currentScope. Except for the first merge, there is no
					 * dyscopes in the merged state. The follow JAVA assertion
					 * checks for this.
					 */
					dyscopeMono2State[i] = i;
					counter++;
					assert first;
				}
			} else
				dyscopeMono2State[i] = counter++;
		}
		dyscopes = new ImmutableDynamicScope[counter];
		Arrays.fill(dyscopes, null);
		newMonoPC = renumberDyscopes(monoDyscopes, dyscopeMono2State, dyscopes,
				theMono.getPermanentPathCondition());
		// clear reacher for the monoDyscopes:
		for (int i = 0; i < counter; i++)
			if (dyscopes[i] != null) {
				BitSet reachers = dyscopes[i].getReachers();

				reachers.clear();
				dyscopes[i] = dyscopes[i].setReachers(reachers);
			}
		{
			// copy local dyscopes in theState to new dyscopes array:
//			System.arraycopy(theState.copyScopes(), 0, dyscopes, 0,
//					theState.numDyscopes());
			ImmutableDynamicScope[] theStateDyscopes = theState.copyScopes();
			
			for (int i = 0; i < theStateDyscopes.length; i++) {
				if (dyscopes[i] == null)
					dyscopes[i] = theStateDyscopes[i];
			}
		}
		IntArray key = new IntArray(dyscopeMono2State);
		UnaryOperator<SymbolicExpression> substituter = dyscopeSubMap.get(key);
		BooleanExpression newPermanentPathCondition;

		if (substituter == null) {
			substituter = universe
					.mapSubstituter(scopeSubMap(dyscopeMono2State));
			dyscopeSubMap.putIfAbsent(key, substituter);
		}
		newPermanentPathCondition = (BooleanExpression) substituter
				.apply(theState.getPermanentPathCondition());
		processes[newPid] = monoProcess.updateDyscopes(dyscopeMono2State,
				substituter);
		for (ImmutableProcessState proc : processes)
			if (!proc.hasEmptyStack())
				setReachablesForProc(dyscopes, proc);

		// Add sleep location:
		StackEntry top = processes[newPid].peekStack();

		processes[newPid] = processes[newPid].pop();
		top = new ImmutableStackEntry(modelFactory.model().sleepLocation(),
				top.scope());
		processes[newPid] = processes[newPid].push((ImmutableStackEntry) top);

		theResult = ImmutableState.newState(theState, processes, dyscopes,
				universe.and(newPermanentPathCondition, newMonoPC));
		concatenateMonoScopeMap(scopeValueMono2Curr, dyscopeMono2State,
				scopeValueState2Curr);
		return saveState(theResult);
	}

	@Override
	public SymbolicExpression addExternalProcess(
			SymbolicExpression colStateValue, State currentState, int pid,
			int place, CIVLFunction with) {
		CIVLStateType stateType = modelFactory.typeFactory().stateType();
		int mergedStateRefID = stateType.selectStateKey(universe,
				colStateValue);
		ImmutableState mergedState = getStateByReference(mergedStateRefID);
		SymbolicExpression scopeValue2Curr = stateType
				.selectScopeValuesMap(universe, colStateValue);
		SymbolicExpression oldScopeValue2Curr[] = symbolicUtil
				.symbolicArrayToConcreteArray(scopeValue2Curr);
		// #new-scopes <= #mergedState-scopes + #currentstate-scopes
		SymbolicExpression newScopeValue2Curr[] = new SymbolicExpression[mergedState
				.numDyscopes() + currentState.numDyscopes()];

		Arrays.fill(newScopeValue2Curr, nullScopeValue);
		System.arraycopy(oldScopeValue2Curr, 0, newScopeValue2Curr, 0,
				oldScopeValue2Curr.length);
		mergedState = addExternalProcessWorker(mergedState, currentState, pid,
				place, with, newScopeValue2Curr);
		// prune unused suffix in the newScopeValue2Curr array:
		newScopeValue2Curr = Arrays.copyOf(newScopeValue2Curr,
				mergedState.numDyscopes());
		mergedStateRefID = saveState(mergedState).left;
		return stateType.buildStateValue(universe, mergedStateRefID, universe
				.array(typeFactory.scopeSymbolicType(), newScopeValue2Curr));
	}

	private ImmutableState addExternalProcessWorker(State mergedState,
			State currentState, int pid, int place, CIVLFunction with,
			SymbolicExpression scopeValue2Curr[]) {
		if (mergedState.getProcessState(place).hasEmptyStack()) {
			/*
			 * If the merged state has no internal process (the process whose
			 * PID is the given place in the merged state) that is associated
			 * with the given external process, there must be something wrong.
			 */
			throw new CIVLInternalException(
					"A process attempts to execute a $with statement with a collate state"
							+ " without adding its own snapshot to the collate state first.",
					with.getSource());
		}

		ImmutableState theColState = (ImmutableState) mergedState;
		ImmutableState theCurrState = pushCallStack(currentState, pid, with,
				new SymbolicExpression[0]);
		ImmutableDynamicScope newDyscopes[];
		ImmutableProcessState external = theCurrState.getProcessState(pid);
		int newPid = theColState.numProcs();
		int counter = theColState.numDyscopes();
		int extScopesCurr2Merged[] = new int[theCurrState.numDyscopes()];
		BooleanExpression newRealPC;
		int oldDyscopeId = external.peekStack().scope();

		// TODO: update scopeValue2Curr, note that external process is sharing
		// common scopes with its corresponding internal scope:
		Arrays.fill(extScopesCurr2Merged,
				ModelConfiguration.DYNAMIC_NULL_SCOPE);
		while (oldDyscopeId >= 0) {
			ImmutableDynamicScope oldDyscope = theCurrState
					.getDyscope(oldDyscopeId);
			int newDid;

			newDid = theColState.getDyscope(place, oldDyscope.lexicalScope());
			if (newDid >= 0)
				extScopesCurr2Merged[oldDyscopeId] = newDid;
			else {
				extScopesCurr2Merged[oldDyscopeId] = counter;
				assert scopeValue2Curr[counter] == nullScopeValue : "Merged "
						+ "dyscope ID conflicts existing dyscope ID";
				scopeValue2Curr[counter] = scopeValue(counter);
				counter++;

			}
			oldDyscopeId = oldDyscope.getParent();
		}
		newDyscopes = new ImmutableDynamicScope[counter];
		newRealPC = renumberDyscopes(theCurrState.copyScopes(),
				extScopesCurr2Merged, newDyscopes,
				theCurrState.getPermanentPathCondition());
		// Clear reachers for those new dyscopes:
		for (int i = 0; i < counter; i++) {
			if (newDyscopes[i] != null) {
				BitSet reachers = newDyscopes[i].getReachers();

				reachers.clear();
				newDyscopes[i] = newDyscopes[i].setReachers(reachers);
			}
		}
		System.arraycopy(theColState.copyScopes(), 0, newDyscopes, 0,
				theColState.numDyscopes());

		ImmutableProcessState processes[] = theColState
				.copyAndExpandProcesses();
		StackEntry[] newStack = new StackEntry[1];

		newStack[0] = external.peekStack();
		processes[newPid] = new ImmutableProcessState(newPid, newStack, null,
				null, null, external.atomicCount(), true);

		IntArray key = new IntArray(extScopesCurr2Merged);
		UnaryOperator<SymbolicExpression> substituter = dyscopeSubMap.get(key);

		if (substituter == null) {
			substituter = universe
					.mapSubstituter(scopeSubMap(extScopesCurr2Merged));
			dyscopeSubMap.putIfAbsent(key, substituter);
		}
		processes[newPid] = processes[newPid]
				.updateDyscopes(extScopesCurr2Merged, substituter);
		setReachablesForProc(newDyscopes, processes[newPid]);
		return ImmutableState.newState(theColState, processes, newDyscopes,
				universe.and(newRealPC,
						theColState.getPermanentPathCondition()));
	}

	@Override
	public ImmutableState getStateByReference(int referenceID) {
		return collateStateStorage.getSavedState(referenceID);
	}

	@Override
	public Pair<Integer, State> saveState(State state) {
		ImmutableState result;

		try {
			result = canonicWork(state, true, true, true, false, false,
					fullHeapErrorSet);
		} catch (CIVLHeapException e) {
			throw new CIVLInternalException(
					"Canonicalization with ignorance of all kinds of heap errors "
							+ "still throws an Heap Exception",
					e.source());
		}

		int referenceID = collateStateStorage.saveCollateState(result);

		return new Pair<>(referenceID, result);
	}

	@Override
	public State emptyState(int nprocs) {
		ImmutableProcessState processes[] = new ImmutableProcessState[nprocs];
		ImmutableDynamicScope dyscopes[] = new ImmutableDynamicScope[0];
		ImmutableState result;

		for (int i = 0; i < nprocs; i++)
			processes[i] = new ImmutableProcessState(i, false);
		result = new ImmutableState(processes, dyscopes,
				universe.trueExpression());
		result.collectibleCounts = new int[ModelConfiguration.SYMBOL_PREFIXES.length];
		return result;
	}

	@Override
	public void setConfiguration(CIVLConfiguration config) {
		this.config = config;
	}

	@Override
	public ImmutableState addToPathcondition(State state, int pid,
			BooleanExpression clause) {
		ImmutableState imuState = (ImmutableState) state;
		BooleanExpression partialPathConditions[] = imuState
				.copyOfPartialPathConditionStack(pid);
		int head = partialPathConditions.length - 1;

		if (head >= 0) {
			partialPathConditions[head] = universe
					.and(partialPathConditions[head], clause);
			return imuState.setPartialPathConditionStack(pid,
					partialPathConditions);
		}
		BooleanExpression newPathCondition = universe
				.and(imuState.getPermanentPathCondition(), clause);

		return imuState.setPermanentPathCondition(newPathCondition);
	}

	@Override
	public SymbolicExpression processValue(int pid) {
		if (pid == -2)
			return this.nullProcessValue;
		if (pid < 0)
			return undefinedProcessValue;
		if (pid < maxProcs) {
			return processValues[pid];
		} else {
			String errorMessage = "pid is " + pid
					+ " which is greater the upper bound " + maxProcs
					+ ". So you need to specify a larger maxProcs(-maxProcs=num) through command line";

			throw new CIVLException(errorMessage, null);
		}
	}

	@Override
	public ImmutableState addReadWriteRecords(State state, int pid,
			SymbolicExpression memValue, boolean isRead) {
		ImmutableState imuState = ((ImmutableState) state);

		if (isRead) {
			DynamicMemoryLocationSet newRsStack[] = imuState
					.getProcessState(pid).getReadSets(true);
			int head = newRsStack.length - 1;

			newRsStack[head] = memoryLocationSetFactory
					.addReference(newRsStack[head], memValue);
			return imuState.setReadSetStack(pid, newRsStack);
		} else {
			DynamicMemoryLocationSet newWsStack[] = imuState
					.getProcessState(pid).getWriteSets(true);
			int head = newWsStack.length - 1;

			newWsStack[head] = memoryLocationSetFactory
					.addReference(newWsStack[head], memValue);
			return imuState.setWriteSetStack(pid, newWsStack);
		}
	}

	@Override
	public DynamicMemoryLocationSet peekReadWriteSet(State state, int pid,
			boolean isRead) {
		ImmutableState imuState = ((ImmutableState) state);
		DynamicMemoryLocationSet[] stack;

		if (isRead)
			stack = imuState.getProcessState(pid).getReadSets(false);
		else
			stack = imuState.getProcessState(pid).getWriteSets(false);
		if (stack.length > 0) {
			int head = stack.length - 1;

			return stack[head];
		} else
			return null;
	}

	@Override
	public State pushEmptyReadWrite(State state, int pid, boolean isRead) {
		DynamicMemoryLocationSet newEmptySet = memoryLocationSetFactory.empty();
		ImmutableState imuState = ((ImmutableState) state);

		if (isRead) {
			DynamicMemoryLocationSet[] rsStack = imuState.getProcessState(pid)
					.getReadSets(true);
			DynamicMemoryLocationSet[] newRsStack = Arrays.copyOf(rsStack,
					rsStack.length + 1);
			int head = rsStack.length;

			newRsStack[head] = newEmptySet;
			return imuState.setReadSetStack(pid, newRsStack);
		} else {
			DynamicMemoryLocationSet[] wsStack = imuState.getProcessState(pid)
					.getWriteSets(true);
			DynamicMemoryLocationSet[] newWsStack = Arrays.copyOf(wsStack,
					wsStack.length + 1);
			int head = wsStack.length;

			newWsStack[head] = newEmptySet;
			return imuState.setWriteSetStack(pid, newWsStack);
		}
	}

	@Override
	public State popReadWriteSet(State state, int pid, boolean isRead) {
		ImmutableState imuState = ((ImmutableState) state);

		if (isRead) {
			DynamicMemoryLocationSet[] rsStack = imuState.getProcessState(pid)
					.getReadSets(true);
			DynamicMemoryLocationSet[] newRsStack = Arrays.copyOf(rsStack,
					rsStack.length - 1);

			return imuState.setReadSetStack(pid, newRsStack);
		} else {
			DynamicMemoryLocationSet[] wsStack = imuState.getProcessState(pid)
					.getWriteSets(true);
			DynamicMemoryLocationSet[] newWsStack = Arrays.copyOf(wsStack,
					wsStack.length - 1);

			return imuState.setWriteSetStack(pid, newWsStack);
		}
	}

	@Override
	public State pushAssumption(State state, int pid,
			BooleanExpression assumption) {
		ImmutableState imuState = ((ImmutableState) state);
		BooleanExpression[] ppcStack = imuState
				.copyOfPartialPathConditionStack(pid);
		BooleanExpression[] ppcNewStack = Arrays.copyOf(ppcStack,
				ppcStack.length + 1);
		int head = ppcStack.length;

		ppcNewStack[head] = assumption;
		return imuState.setPartialPathConditionStack(pid, ppcNewStack);
	}

	@Override
	public State popAssumption(State state, int pid) {
		ImmutableState imuState = ((ImmutableState) state);
		BooleanExpression[] ppcStack = imuState
				.copyOfPartialPathConditionStack(pid);
		BooleanExpression[] ppcNewStack = Arrays.copyOf(ppcStack,
				ppcStack.length - 1);

		return imuState.setPartialPathConditionStack(pid, ppcNewStack);
	}

	@Override
	public SymbolicExpression scopeValue(int sid) {
		SymbolicExpression result;

		if (sid == ModelConfiguration.DYNAMIC_NULL_SCOPE)
			return this.nullScopeValue;
		if (sid == ModelConfiguration.DYNAMIC_UNDEFINED_SCOPE)
			return this.undefinedScopeValue;
		if (sid < SCOPE_VALUES_INIT_SIZE)
			return smallScopeValues[sid];

		// key := sid - INIT_SIZE;
		// value := scope_value_of(sid);
		int key = sid - SCOPE_VALUES_INIT_SIZE;

		while (key >= bigScopeValues.size())
			bigScopeValues.addAll(nullList);
		result = bigScopeValues.get(key);
		if (result == null) {
			result = dyscopeIDToScopeValue.apply(sid);
			bigScopeValues.set(key, result);
		}
		return result;
	}

	@Override
	public void setSymbolicUtility(SymbolicUtility symbolicUtility) {
		this.symbolicUtil = symbolicUtility;
		this.stateValueHelper = new ImmutableStateValueHelper(universe,
				typeFactory, symbolicUtil);
	}

	@Override
	public boolean isScopeIdDefined(int sid) {
		return ModelConfiguration.DYNAMIC_UNDEFINED_SCOPE == sid;
	}

	@Override
	public int getDyscopeId(SymbolicExpression scopeValue) {
		return scopeValueToDyscopeID.apply(scopeValue).intValue();
	}

	@Override
	public SymbolicExpression undefinedScopeValue() {
		return this.undefinedScopeValue;
	}

	@Override
	public SymbolicExpression nullScopeValue() {
		return this.nullScopeValue;
	}

	/**
	 * Converts a map, which maps from non-negative integer set A to B, to a new
	 * map, which maps non-negative integer set B to A. For the INPUT map, it
	 * <b>requires</b> that
	 * <ol>
	 * <li>every non-negative integer in A is mapped to a UNIQUE non-negative
	 * integer in B.</li>
	 * <li>the length of the OUTPUT array must greater than the maximum value of
	 * the non-negative set B.</li>
	 * </ol>
	 * 
	 * @param old2new
	 *            INPUT
	 * @param new2old
	 *            OUTPUT
	 */
	private void mapReverse(int[] old2new, int[] new2old) {
		Arrays.fill(new2old, ModelConfiguration.DYNAMIC_NULL_SCOPE);
		for (int i = 0; i < old2new.length; i++)
			if (old2new[i] >= 0)
				new2old[old2new[i]] = i;
	}

	/**
	 * Append the mono-to-current scope value map to the
	 * (merged)state-to-current scope value map with a converter which converts
	 * mono scope value to (merged)state scope value
	 * 
	 * @param mono2curr
	 *            INPUT. this array maps scope values of the mono state to scope
	 *            values of current state
	 * @param mono2state
	 *            the converting map from mono scope values to (merged)state
	 *            scope values
	 * @param state2curr
	 *            INPUT/OUTPUT. As INPUT, this array maps scope values of the
	 *            given state, which is a merged state, to scope values of
	 *            current state. As OUTPUT, this array will be concatenated with
	 *            another map from scope values of the merging mono state to
	 *            current state. This array must be large enough for
	 *            concatenation, i.e. there are enough suffix spaces in the
	 *            array for taking new values from another map
	 * 
	 */
	private void concatenateMonoScopeMap(SymbolicExpression[] mono2curr,
			int[] mono2state, SymbolicExpression[] state2curr) {
		for (int i = 0; i < mono2curr.length; i++) {
			assert state2curr[mono2state[i]] == null
					|| state2curr[mono2state[i]] == mono2curr[i] : ""
							+ "not concatenation";
			state2curr[mono2state[i]] = mono2curr[i];
		}
	}

	@Override
	public StateValueHelper stateValueHelper() {
		return this.stateValueHelper;
	}
}
