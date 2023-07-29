/**
 * 
 */
package edu.udel.cis.vsl.civl.semantics.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import edu.udel.cis.vsl.civl.analysis.IF.Analysis;
import edu.udel.cis.vsl.civl.analysis.IF.CodeAnalyzer;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.log.IF.CIVLExecutionException;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.Certainty;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.ErrorKind;
import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.SystemFunction;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression.LHSExpressionKind;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.AssignStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.AtomicLockAssignStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.CivlParForSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.DomainIteratorStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.LoopBranchStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.MallocStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.NoopStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.NoopStatement.NoopKind;
import edu.udel.cis.vsl.civl.model.IF.statement.ParallelAssignStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.ReturnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement.StatementKind;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLArrayType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLFunctionType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPointerType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLStructOrUnionType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType.TypeKind;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.Executor;
import edu.udel.cis.vsl.civl.semantics.IF.Format;
import edu.udel.cis.vsl.civl.semantics.IF.Format.ConversionType;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryLoaderException;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.semantics.IF.Transition.AtomicLockAction;
import edu.udel.cis.vsl.civl.semantics.IF.TypeEvaluation;
import edu.udel.cis.vsl.civl.state.IF.ProcessState;
import edu.udel.cis.vsl.civl.state.IF.StackEntry;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Triple;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * An executor is used to execute a CIVL statement. The basic method provided
 * takes a state and a statement, and modifies the state according to the
 * semantics of that statement.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public class CommonExecutor implements Executor {

	/* *************************** Instance Fields ************************* */

	/** The Evaluator used to evaluate expressions. */
	private Evaluator evaluator;

	/** The Evaluator used to evaluate expressions and collecting read sets. */
	private ReadSetCollectEvaluator readSetCollectEvaluator = null;

	/**
	 * This instance refers to the regular evaluator when
	 * {@link #readSetCollectEvaluator} is active.
	 */
	private Evaluator evaluatorOnTheBench = null;

	/**
	 * The loader used to find Executors for system functions declared in
	 * libraries.
	 */
	protected LibraryExecutorLoader loader;

	/**
	 * The unique model factory used in the system.
	 */
	private ModelFactory modelFactory;

	/**
	 * The unique model factory used in the system.
	 */
	private CIVLTypeFactory typeFactory;

	/**
	 * The number of steps that have been executed by this executor. A "step" is
	 * defined to be a call to method
	 * {@link #executeWork(State, int, Statement)}.
	 */
	protected AtomicLong numSteps = new AtomicLong(0);

	/** The factory used to produce and manipulate model states. */
	private StateFactory stateFactory;

	private SymbolicUtility symbolicUtil;

	/** The symbolic universe used to manage all symbolic expressions. */
	private SymbolicUniverse universe;

	private CIVLErrorLogger errorLogger;

	protected CIVLConfiguration civlConfig;

	private SymbolicAnalyzer symbolicAnalyzer;

	private IntObject zeroObj;

	private IntObject oneObj;

	private IntObject twoObj;

	/**
	 * The set of characters that are used to construct a number in a format
	 * string.
	 */
	final private Set<Character> numbers;

	private List<CodeAnalyzer> analyzers;

	@SuppressWarnings("unused")
	private Int2PointerCaster int2PointerCaster;

	/* ***************************** Constructors ************************** */

	/**
	 * Create a new instance of executor.
	 * 
	 * @param modelFactory
	 *            The model factory of the system.
	 * @param stateFactory
	 *            The state factory of the system.
	 * @param log
	 *            The error logger of the system.
	 * @param loader
	 *            The library executor loader for executing system functions.
	 * @param evaluator
	 *            The CIVL evaluator for evaluating expressions.
	 * @param symbolicAnalyzer
	 *            The symbolic analyzer used in the system.
	 * @param errorLogger
	 *            The error logger to log errors
	 * @param civlConfig
	 *            The CIVL configuration.
	 */
	public CommonExecutor(ModelFactory modelFactory, StateFactory stateFactory,
			LibraryExecutorLoader loader, Evaluator evaluator,
			SymbolicAnalyzer symbolicAnalyzer, CIVLErrorLogger errorLogger,
			CIVLConfiguration civlConfig) {
		this.civlConfig = civlConfig;
		this.universe = modelFactory.universe();
		this.stateFactory = stateFactory;
		this.modelFactory = modelFactory;
		this.typeFactory = modelFactory.typeFactory();
		this.evaluator = evaluator;
		this.symbolicUtil = evaluator.symbolicUtility();
		this.int2PointerCaster = new Int2PointerCaster(universe, symbolicUtil,
				modelFactory.typeFactory().pointerSymbolicType());
		this.symbolicAnalyzer = symbolicAnalyzer;
		this.loader = loader;
		this.errorLogger = errorLogger;
		this.zeroObj = universe.intObject(0);
		this.oneObj = universe.intObject(1);
		this.twoObj = universe.intObject(2);
		numbers = new HashSet<Character>(10);
		for (int i = 0; i < 10; i++) {
			numbers.add(Character.forDigit(i, 10));
		}
		this.analyzers = modelFactory.codeAnalyzers();
	}

	/* ************************** Private methods ************************** */

	/**
	 * Executes an assignment statement. The state will be updated such that the
	 * value of the left-hand-side of the assignment statement is the result of
	 * evaluating the right-hand-side. The location of the state will be updated
	 * to the target location of the assignment.
	 * 
	 * @param state
	 *            The state of the program
	 * @param pid
	 *            The process id of the currently executing process
	 * @param statement
	 *            An assignment statement to be executed
	 * @return The updated state of the program
	 * @throws UnsatisfiablePathConditionException
	 */
	private State executeAssign(State state, int pid, String process,
			AssignStatement statement)
			throws UnsatisfiablePathConditionException {
		Evaluation eval = evaluator.evaluate(state, pid, statement.rhs());

		if (statement instanceof AtomicLockAssignStatement) {
			AtomicLockAssignStatement atomicLockAssign = (AtomicLockAssignStatement) statement;

			if (atomicLockAssign.enterAtomic()) {
				state = stateFactory.enterAtomic(state, pid);
			} else {// leave atomic
				state = stateFactory.leaveAtomic(state, pid);
			}
		} else {
			/*
			 * CIVLType lhsType = statement.getLhs().getExpressionType();
			 * Expression rhs = statement.rhs();
			 * 
			 * // The int2pointer remains as no-op for int-to-pointer-to-void
			 * conversion // this is to revert it when it is used to assign to a
			 * component of an object if (rhs instanceof CastExpression) {
			 * CastExpression cast = (CastExpression) rhs;
			 * 
			 * if (cast.getExpression().getExpressionType().isIntegerType()) {
			 * if (lhsType.isPointerType() && ((CIVLPointerType) lhsType)
			 * .baseType().isVoidType()) { if (eval.value.type().isInteger()) {
			 * eval.value = int2PointerCaster .forceCast(eval.value); } } } }
			 */
			state = assignLHS(eval.state, pid, process, statement.getLhs(),
					eval.value, statement.isInitialization());
		}
		state = stateFactory.setLocation(state, pid, statement.target(), true);
		return state;
	}

	private State executeParallelAssign(State state, int pid, String process,
			ParallelAssignStatement statement)
			throws UnsatisfiablePathConditionException {
		Evaluation eval;
		List<Pair<LHSExpression, Expression>> assignPairs = statement
				.assignments();

		for (Pair<LHSExpression, Expression> assign : assignPairs) {
			eval = evaluator.evaluate(state, pid, assign.right);
			state = assignLHS(eval.state, pid, process, assign.left, eval.value,
					false);
		}
		return stateFactory.setLocation(state, pid, statement.target());
	}

	/**
	 * Executes a call statement. The state will be updated such that the
	 * process is at the start location of the function, a new dynamic scope for
	 * the function is created, and function parameters in the new scope have
	 * the values that are passed as arguments.
	 * 
	 * @param state
	 *            The state of the program.
	 * @param pid
	 *            The process id of the currently executing process.
	 * @param statement
	 *            A call statement to be executed.
	 * @return The updated state of the program.
	 * @throws UnsatisfiablePathConditionException
	 */
	protected State executeCall(State state, int pid,
			CallOrSpawnStatement statement)
			throws UnsatisfiablePathConditionException {
		CIVLFunction function = statement.function();

		if (function != null && function.isNondet()) {
			LHSExpression lhs = statement.lhs();

			if (lhs != null) {
				CIVLFunctionType functionType = function.functionType();
				Evaluation eval = this.evaluator.havoc(state,
						functionType.returnType().getDynamicType(universe));

				state = eval.state;
				state = this.assign(state, pid, "p" + pid, lhs, eval.value,
						statement.isInitializer());
			}
			state = stateFactory.setLocation(state, pid, statement.target(),
					true);
			return state;
		} else if (function != null && function.isSystemFunction()) {
			state = this.executeSystemFunctionCall(state, pid, statement,
					(SystemFunction) function).state;
		} else {
			SymbolicExpression[] arguments;

			arguments = new SymbolicExpression[statement.arguments().size()];
			for (int i = 0; i < statement.arguments().size(); i++) {
				Evaluation eval = evaluator.evaluate(state, pid,
						statement.arguments().get(i));

				state = eval.state;
				arguments[i] = eval.value;
			}
			Analysis.analyzeCall(this.analyzers, state, pid, statement,
					arguments);
			if (function == null) {
				Triple<State, CIVLFunction, Integer> eval = evaluator
						.evaluateFunctionIdentifier(state, pid,
								statement.functionExpression(),
								statement.getSource());

				function = eval.second;
				state = eval.first;
				if (function.isSystemFunction()) {
					state = this.executeSystemFunctionCall(state, pid,
							statement, (SystemFunction) function).state;
				} else
					state = stateFactory.pushCallStack(state, pid, function,
							eval.third, arguments);
			} else
				state = stateFactory.pushCallStack(state, pid, function,
						arguments);
			if (!function.isSystemFunction() && function.isAtomicFunction())
				state = stateFactory.enterAtomic(state, pid);
		}
		// Right after the call stack entry is pushed into call stack, check
		// pre-conditions:
		if (civlConfig.isEnableMpiContract()) {
			// List<ContractClause> preconditions = statement.function()
			// .preconditions();
			//
			// if (preconditions != null && !preconditions.isEmpty())
			// state = assertMPIContractClauses(state, pid, preconditions);
		}
		return state;
	}

	protected Evaluation executeSystemFunctionCall(State state, int pid,
			CallOrSpawnStatement call, SystemFunction function)
			throws UnsatisfiablePathConditionException {
		String libraryName = function.getLibrary();
		String funcName = function.name().name();

		try {
			LibraryExecutor executor = loader.getLibraryExecutor(libraryName,
					this, this.modelFactory, this.symbolicUtil,
					symbolicAnalyzer);

			return executor.execute(state, pid, call, funcName);
		} catch (LibraryLoaderException exception) {
			String process = state.getProcessState(pid).name() + "(id=" + pid
					+ ")";

			errorLogger.logSimpleError(call.getSource(), state, process,
					symbolicAnalyzer.stateInformation(state), ErrorKind.LIBRARY,
					"unable to load the library executor for the library "
							+ libraryName + " to execute the function "
							+ funcName);
			if (call.lhs() != null)
				state = this.assign(state, pid, process, call.lhs(),
						universe.nullExpression(), call.isInitializer());
			state = this.stateFactory.setLocation(state, pid, call.target());
			return new Evaluation(state, universe.nullExpression());
		}
	}

	/**
	 * execute malloc statement. TODO complete javadocs
	 * 
	 * @param state
	 * @param pid
	 * @param statement
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private State executeMalloc(State state, int pid, String process,
			MallocStatement statement)
			throws UnsatisfiablePathConditionException {
		CIVLSource source = statement.getSource();
		LHSExpression lhs = statement.getLHS();
		Evaluation eval;
		SymbolicExpression scopeValue;
		int dyScopeID;
		NumericExpression mallocSize, elementSize;
		BooleanExpression pathCondition, claim;
		ResultType validity;
		NumericExpression elementCount;
		Pair<State, SymbolicExpression> mallocResult;
		SymbolicType dynamicElementType;

		eval = evaluator.evaluate(state, pid, statement.getScopeExpression());
		state = eval.state;
		scopeValue = eval.value;
		dyScopeID = stateFactory.getDyscopeId(scopeValue);
		eval = evaluator.evaluate(state, pid, statement.getSizeExpression());
		state = eval.state;
		mallocSize = (NumericExpression) eval.value;
		eval = evaluator.evaluateSizeofType(source, state, pid,
				statement.getStaticElementType());
		state = eval.state;
		elementSize = (NumericExpression) eval.value;
		pathCondition = state.getPathCondition(universe);
		if (!this.civlConfig.svcomp()) {
			claim = universe.divides(elementSize, mallocSize);
			validity = universe.reasoner(pathCondition).valid(claim)
					.getResultType();
			if (validity != ResultType.YES) {
				String elementType = statement.getStaticElementType()
						.toString();
				String message = "For a $malloc returning " + elementType
						+ "*, the size argument must be a multiple of sizeof("
						+ elementType + ")\n" + "      actual size argument: "
						+ mallocSize.toString() + "\n"
						+ "      expected size argument: a multile of "
						+ elementSize.toString();

				state = errorLogger.logError(source, state, pid,
						symbolicAnalyzer.stateInformation(state), claim,
						validity, ErrorKind.MALLOC, message);
				throw new UnsatisfiablePathConditionException();
			}
		}
		elementCount = universe.divide(mallocSize, elementSize);
		// If the type of the allocated element object is an struct or union
		// type, field types can be array types which should be evaluated
		// carefully to provide extents informations.
		if (statement.getStaticElementType().isStructType()) {
			CIVLStructOrUnionType staticType = (CIVLStructOrUnionType) statement
					.getStaticElementType();
			int numFields = staticType.numFields();
			SymbolicType fieldTypes[] = new SymbolicType[numFields];

			for (int i = 0; i < numFields; i++) {
				CIVLType civlfieldType = (CIVLType) staticType.getField(i)
						.type();

				if (civlfieldType.isArrayType()) {
					Pair<State, SymbolicArrayType> pair = evaluator
							.evaluateCIVLArrayType(state, pid,
									(CIVLArrayType) civlfieldType);

					state = pair.left;
					fieldTypes[i] = pair.right;
				} else
					fieldTypes[i] = civlfieldType.getDynamicType(universe);
			}
			dynamicElementType = universe.tupleType(
					universe.stringObject(staticType.name().name()),
					Arrays.asList(fieldTypes));
		} else {
			TypeEvaluation teval = evaluator.getDynamicType(state, pid,
					statement.getStaticElementType(), source, false);

			state = teval.state;
			dynamicElementType = teval.type;
		}
		mallocResult = stateFactory.malloc(state, pid, dyScopeID,
				statement.getMallocId(), dynamicElementType, elementCount);
		state = mallocResult.left;

		/*
		 * Comment out the following code for the reason that malloc shall not
		 * be recorded as a write footprint.
		 * 
		 * 
		 * boolean saveWrite = state.isMonitoringWrites(pid);
		 * 
		 * if (saveWrite) { SymbolicExpression pointer2memoryBlk = symbolicUtil
		 * .parentPointer(mallocResult.right);
		 * 
		 * eval = evaluator.memEvaluator().pointer2memValue(state, pid,
		 * pointer2memoryBlk, source); state = eval.state; // write is also a
		 * read state = stateFactory.addReadWriteRecords(state, pid, eval.value,
		 * false); }
		 */
		if (lhs != null)
			// note that malloc only assigns pointers which have scalar type, so
			// weather they are initialized by the malloc statement is not
			// important because initialization flag is used to help checking
			// dynamic types compatible of assignments for non-scalar types.
			state = assign(state, pid, process, lhs, mallocResult.right, false);
		state = stateFactory.setLocation(state, pid, statement.target(),
				lhs != null);
		return state;
	}

	/**
	 * Execute a return statement.
	 * 
	 * @param state
	 *            The state of the program.
	 * @param pid
	 *            The process id of the currently executing process.
	 * @param statement
	 *            The return statement to be executed.
	 * @return The updated state of the program.
	 * @throws UnsatisfiablePathConditionException
	 */
	private State executeReturn(State state, int pid, String process,
			ReturnStatement statement)
			throws UnsatisfiablePathConditionException {
		Expression expr = statement.expression();
		ProcessState processState;
		SymbolicExpression returnValue;
		CIVLFunction function;
		String functionName;

		processState = state.getProcessState(pid);
		function = processState.peekStack().location().function();
		functionName = function.name().name();
		if (function.isAtomicFunction())
			state = stateFactory.leaveAtomic(state, pid);
		if (functionName.equals(CIVLConstants.civlSystemFunction)) {
			assert pid == 0;
			if (state.numProcs() > 1) {
				for (ProcessState proc : state.getProcessStates()) {
					if (proc == null)
						continue;
					// If that process is self-destructable, then it is not a
					// process leak:
					if (proc.isSelfDestructable())
						continue;
					if (proc.getPid() == pid)
						continue;
					if (!this.civlConfig.svcomp() && !proc.hasEmptyStack()) {
						errorLogger.logSimpleError(statement.getSource(), state,
								process,
								symbolicAnalyzer.stateInformation(state),
								ErrorKind.PROCESS_LEAK,
								"attempt to terminate the main process while process "
										+ proc.name() + " is still running");
						throw new UnsatisfiablePathConditionException();
					}
				}
			}

		}
		if (expr == null)
			returnValue = null;
		else {
			Evaluation eval = evaluator.evaluate(state, pid, expr);

			returnValue = eval.value;
			state = eval.state;
			if (functionName.equals("_CIVL_system")) {
				if (universe.equals(returnValue, universe.integer(0))
						.isFalse()) {
					this.errorLogger.logSimpleError(statement.getSource(),
							state, process,
							symbolicAnalyzer.stateInformation(state),
							ErrorKind.OTHER,
							"program exits with error code: " + returnValue);
				}
			}
		}
		state = stateFactory.popCallStack(state, pid);
		processState = state.getProcessState(pid);
		if (!processState.hasEmptyStack()) {
			StackEntry returnContext = processState.peekStack();
			Location returnLocation = returnContext.location();
			CallOrSpawnStatement call = (CallOrSpawnStatement) returnLocation
					.getSoleOutgoing();

			if (call.lhs() != null) {
				if (returnValue == null) {
					errorLogger.logSimpleError(call.getSource(), state, process,
							symbolicAnalyzer.stateInformation(state),
							ErrorKind.OTHER,
							"attempt to use the return value of function "
									+ functionName + " when " + functionName
									+ " has returned without a return value.");
					returnValue = universe.nullExpression();
				}
				state = assign(state, pid, process, call.lhs(), returnValue,
						call.isInitializer());
			}
			state = stateFactory.setLocation(state, pid, call.target(),
					call.lhs() != null);
		}
		// If the process has an empty call stack and it is a self destructable
		// process, kill it:
		if (state.getProcessState(pid).hasEmptyStack()
				&& state.getProcessState(pid).isSelfDestructable())
			state = stateFactory.removeProcess(state, pid);
		return state;
	}

	/**
	 * Executes a spawn statement. The state will be updated with a new process
	 * whose start location is the beginning of the forked function.
	 * 
	 * @param state
	 *            The state of the program.
	 * @param pid
	 *            The process id of the currently executing process.
	 * @param statement
	 *            A spawn statement to be executed.
	 * @return The updated state of the program.
	 * @throws UnsatisfiablePathConditionException
	 */
	private State executeSpawn(State state, int pid, String process,
			CallOrSpawnStatement statement)
			throws UnsatisfiablePathConditionException {
		CIVLFunction function = statement.function();
		int newPid = state.numProcs();
		List<Expression> argumentExpressions = statement.arguments();
		int numArgs = argumentExpressions.size();
		SymbolicExpression[] arguments = new SymbolicExpression[numArgs];
		int parentDyscopeId = -1;
		boolean selfDestructable;

		// If the statement is a $spawn which is translated from a $run, then
		// the new process is self-destructable:
		selfDestructable = statement.isRun();
		assert !statement.isCall();
		if (function == null) {
			Triple<State, CIVLFunction, Integer> eval = evaluator
					.evaluateFunctionIdentifier(state, pid,
							statement.functionExpression(),
							statement.getSource());

			state = eval.first;
			function = eval.second;
			parentDyscopeId = eval.third;
		}
		for (int i = 0; i < numArgs; i++) {
			CIVLType expectedType = function.parameters().get(i).type();
			Evaluation eval;
			Expression actualArg = argumentExpressions.get(i);

			if (!actualArg.getExpressionType().equals(expectedType))
				eval = evaluator.evaluateCastWorker(state, pid, process,
						expectedType, actualArg);
			else
				eval = evaluator.evaluate(state, pid,
						argumentExpressions.get(i));
			state = eval.state;
			arguments[i] = eval.value;
		}
		if (parentDyscopeId >= 0)

			state = stateFactory.addProcess(state, function, parentDyscopeId,
					arguments, pid, selfDestructable);
		else
			state = stateFactory.addProcess(state, function, arguments, pid,
					selfDestructable);
		if (statement.lhs() != null)
			state = assign(state, pid, process, statement.lhs(),
					stateFactory.processValue(newPid),
					statement.isInitializer());
		state = stateFactory.setLocation(state, pid, statement.target(),
				statement.lhs() != null);
		// state = stateFactory.computeReachableMemUnits(state, newPid);
		return state;
	}

	/**
	 * Returns the state that results from executing the statement, or null if
	 * path condition becomes unsatisfiable.
	 * 
	 * @param state
	 * @param pid
	 * @param statement
	 * @return
	 */
	protected State executeStatement(State state, int pid, Statement statement)
			throws UnsatisfiablePathConditionException {
		try {
			statement.reached();

			boolean monitorReads = state.isMonitoringReads(pid);

			if (monitorReads) {
				if (this.readSetCollectEvaluator == null)
					this.readSetCollectEvaluator = evaluator
							.newReadSetCollectEvaluator();
				this.evaluatorOnTheBench = this.evaluator;
				this.evaluator = this.readSetCollectEvaluator;
			}
			state = executeWork(state, pid, statement);
			if (monitorReads)
				this.evaluator = evaluatorOnTheBench;
			return state;
		} catch (SARLException e) {
			throw new CIVLInternalException("SARL exception: " + e, statement);
		}
	}

	/**
	 * Execute a generic statement. All statements except a Choose should be
	 * handled by this method.
	 * 
	 * @param State
	 *            The state of the program.
	 * @param pid
	 *            The process id of the currently executing process.
	 * @param statement
	 *            The statement to be executed.
	 * @return The updated state of the program.
	 */
	private State executeWork(State state, int pid, Statement statement)
			throws UnsatisfiablePathConditionException {
		String process = "p" + pid;
		StatementKind kind = statement.statementKind();

		numSteps.getAndIncrement();
		switch (kind) {
			case ASSIGN :
				return executeAssign(state, pid, process,
						(AssignStatement) statement);
			case CALL_OR_SPAWN :
				CallOrSpawnStatement call = (CallOrSpawnStatement) statement;

				if (call.isCall())
					return executeCall(state, pid, call);
				else
					return executeSpawn(state, pid, process, call);
			case MALLOC :
				return executeMalloc(state, pid, process,
						(MallocStatement) statement);
			case NOOP : {
				NoopStatement noop = (NoopStatement) statement;
				Expression expression = noop.expression();

				// Evaluate branch condition, if there is error in evaluation,
				// report it.
				if (expression != null) {
					Evaluation eval = this.evaluator.evaluate(state, pid,
							expression);

					state = eval.state;
				}
				state = stateFactory.setLocation(state, pid,
						statement.target());
				if (noop.noopKind() == NoopKind.LOOP) {
					LoopBranchStatement loopBranch = (LoopBranchStatement) noop;

					if (!loopBranch.isEnter() && civlConfig.simplify()) {
						BooleanExpression pc = state.getPathCondition(universe);
						Reasoner reasoner = universe.reasoner(pc);

						if (reasoner.getReducedContext() != pc)
							state = this.stateFactory.simplify(state);
					}
				}
				return state;
			}
			case RETURN :
				return executeReturn(state, pid, process,
						(ReturnStatement) statement);
			case DOMAIN_ITERATOR :
				return executeNextInDomain(state, pid,
						(DomainIteratorStatement) statement);
			case CIVL_PAR_FOR_ENTER :
				return executeCivlParFor(state, pid,
						(CivlParForSpawnStatement) statement);
			case PARALLEL_ASSIGN :
				return executeParallelAssign(state, pid, process,
						(ParallelAssignStatement) statement);
			case WITH :
			case UPDATE :
				throw new CIVLInternalException("unreachable", statement);
			default :
				throw new CIVLInternalException(
						"Unknown statement kind: " + kind, statement);
		}
	}

	/**
	 * When the domain is empty, this is equivalent to a noop.
	 * 
	 * @param state
	 * @param pid
	 * @param parFor
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private State executeCivlParFor(State state, int pid,
			CivlParForSpawnStatement parFor)
			throws UnsatisfiablePathConditionException {
		CIVLSource source = parFor.getSource();
		Expression domain = parFor.domain();
		VariableExpression domSize = parFor.domSizeVar();
		Evaluation eval;
		SymbolicExpression domainValue;
		// TODO why initializes domSizeValue with 1?
		NumericExpression domSizeValue;
		// TODO: why is dim -1 sometimes?
		int dim = parFor.dimension();
		String process = state.getProcessState(pid).name() + "(id=" + pid + ")";
		Reasoner reasoner = universe.reasoner(state.getPathCondition(universe));
		IntegerNumber number_domSize;
		VariableExpression parProcsVar = parFor.parProcsVar();

		state = this.stateFactory.simplify(state);
		eval = evaluator.evaluate(state, pid, domain);
		domainValue = eval.value;
		state = eval.state;
		domSizeValue = symbolicUtil.getDomainSize(domainValue);
		state = assign(state, pid, process, domSize, domSizeValue, false);
		number_domSize = (IntegerNumber) reasoner.extractNumber(domSizeValue);
		if (number_domSize == null) {
			this.errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateToString(state), ErrorKind.OTHER,
					"The arguments of the domain for $parfor "
							+ "must be concrete.");
			// throw new UnsatisfiablePathConditionException();
		} else if (!number_domSize.isZero()) {
			// only spawns processes when the domain is not empty.
			state = this.executeSpawns(state, pid, parProcsVar,
					parFor.parProcFunction(), dim, domainValue);
		}
		state = stateFactory.setLocation(state, pid, parFor.target(), true);
		return state;
	}

	/**
	 * Spawns new processes as a part of the execution of $parfor. For EVERY
	 * ELEMENT in domain, it will spawn a process to execute it.
	 * 
	 * @param state
	 *            The current state
	 * @param pid
	 *            The PID of the process
	 * @param parProcs
	 *            The expression of the pointer to the first element of
	 *            processes array.
	 * @param parProcsPointer
	 *            The symbolic expression of the pointer to the first element of
	 *            processes array.
	 * @param function
	 *            The function will be spawned
	 * @param dim
	 *            The dimension number of the domain.
	 * @param domainValue
	 *            The symbolic expression of the domain object.
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private State executeSpawns(State state, int pid,
			VariableExpression parProcsVar, CIVLFunction function, int dim,
			SymbolicExpression domainValue)
			throws UnsatisfiablePathConditionException {
		String process = state.getProcessState(pid).name() + "(id=" + pid + ")";
		List<SymbolicExpression> myValues = null;
		// int procPtrOffset = 0;
		// CIVLSource source = parProcs.getSource();
		Iterator<List<SymbolicExpression>> domainIter;
		List<SymbolicExpression> processes = new ArrayList<>();

		// Here we assume this operation contains all iterations in the domain.
		// All iterations means that it iterates from the least element to the
		// greatest element in the given domain.
		domainIter = symbolicUtil.getDomainIterator(domainValue);
		while (domainIter.hasNext()) {
			SymbolicExpression[] arguments = new SymbolicExpression[dim];
			int newPid;

			myValues = domainIter.next();
			myValues.toArray(arguments);
			newPid = state.numProcs();
			state = stateFactory.addProcess(state, function, arguments, pid,
					false);
			processes.add(stateFactory.processValue(newPid));
		}
		state = this.assign(state, pid, process, parProcsVar,
				universe.array(
						this.modelFactory.typeFactory().processSymbolicType(),
						processes),
				true);
		return state;
	}

	/**
	 * Giving a domain and a element of the domain, returns the subsequence of
	 * the element in domain. <br>
	 * Pre-condition: it's guaranteed by a nextInDomain condition checking sthat
	 * the element has a subsequence in the domain.
	 * 
	 * @param state
	 *            The current state
	 * @param pid
	 *            The PID of the process
	 * @param nextInDomain
	 *            The nextInDomain statement.
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private State executeNextInDomain(State state, int pid,
			DomainIteratorStatement nextInDomain)
			throws UnsatisfiablePathConditionException {
		List<Variable> loopVars = nextInDomain.loopVariables();
		Expression domain = nextInDomain.domain();
		CIVLSource source = nextInDomain.getSource();
		SymbolicExpression domValue;
		Evaluation eval = evaluator.evaluate(state, pid, domain);
		int dim = loopVars.size();
		String process = state.getProcessState(pid).name() + "(id=" + pid + ")";
		List<SymbolicExpression> varValues = new LinkedList<>();
		List<SymbolicExpression> nextEleValues = new LinkedList<>();
		boolean isAllNull = true;

		domValue = eval.value;
		state = eval.state;
		// Evaluates the element given by the statement
		for (int i = 0; i < dim; i++) {
			SymbolicExpression varValue = state.valueOf(pid, loopVars.get(i));

			if (!varValue.isNull())
				isAllNull = false;
			varValues.add(varValue);
		}
		// Check if it's literal domain or rectangular domain
		try {
			if (symbolicUtil.isLiteralDomain(domValue)) {
				SymbolicExpression literalDomain;
				SymbolicExpression nextElement = null;
				SymbolicExpression counterValue;
				int counter = -1; // The concrete literal counter value
				Variable literalCounterVar;

				literalDomain = universe.unionExtract(oneObj,
						universe.tupleRead(domValue, twoObj));
				literalCounterVar = nextInDomain.getLiteralDomCounter();
				counterValue = state.valueOf(pid, literalCounterVar);
				// Evaluate the value of the counter variable. Here we can
				// initialize it as 0 or search the specific value from the
				// given domain element if the variable is uninitialized.If it
				// does initialization already, read the value from this
				// variable.
				// TODO why counterValue can be null (not SARL NULL)?
				if (counterValue.isNull() || counterValue == null) {
					// If the counter is not initialized
					if (isAllNull)// this is the first iteration
						counter = 0;
					else
						counter = symbolicUtil.literalDomainSearcher(
								literalDomain, varValues, dim);
				} else
					counter = ((IntegerNumber) universe
							.extractNumber((NumericExpression) counterValue))
									.intValue();

				if (counter == -1)
					throw new CIVLExecutionException(ErrorKind.OTHER,
							Certainty.CONCRETE, process,
							"Loop variables are not belong to the domain",
							state, source);
				// it's guaranteed that this iteration will have a
				// subsequence.
				if (counter < ((IntegerNumber) universe.extractNumber(
						(NumericExpression) universe.length(literalDomain)))
								.intValue())
					nextElement = universe.arrayRead(literalDomain,
							universe.integer(counter));
				else
					throw new CIVLInternalException(
							"Domain iteration out of bound", source);
				// increase the counter
				counter++;
				state = stateFactory.setVariable(state, literalCounterVar, pid,
						universe.integer(counter));
				// Put domain element into a list
				for (int i = 0; i < dim; i++)
					nextEleValues.add(universe.arrayRead(nextElement,
							universe.integer(i)));
				// This function is guaranteed have a next element, so it doesnt
				// need to consider the loop end situation
			} else if (symbolicUtil.isRectangularDomain(domValue)) {
				// If it's rectangular domain, just use the value to get the
				// next element
				SymbolicExpression recDomUnion = universe.tupleRead(domValue,
						twoObj);
				SymbolicExpression recDom = universe.unionExtract(zeroObj,
						recDomUnion);

				if (!isAllNull)
					nextEleValues = symbolicUtil
							.getNextInRectangularDomain(recDom, varValues, dim);
				else
					nextEleValues = symbolicUtil.getDomainInit(domValue);
			} else
				throw new CIVLExecutionException(ErrorKind.OTHER,
						Certainty.CONCRETE, process,
						"The domian object is neither a literal domain nor a rectangular domain",
						state, source);
		} catch (SARLException | ClassCastException e) {
			throw new CIVLInternalException(
					"Interanl errors happened in executeNextInDomain()",
					source);
		}
		// Set domain element components one by one.(Domain element is an array
		// of integers of length 'dim')
		for (int i = 0; i < dim; i++)
			state = stateFactory.setVariable(state, loopVars.get(i), pid,
					nextEleValues.get(i));
		// TODO: why set location here ?
		state = stateFactory.setLocation(state, pid, nextInDomain.target());
		return state;
	}

	@Override
	public Evaluation execute_printf(CIVLSource source, State state, int pid,
			String process, Expression[] arguments,
			SymbolicExpression[] argumentValues, boolean forcePrint)
			throws UnsatisfiablePathConditionException {
		StringBuffer stringOfSymbolicExpression;
		StringBuffer formatBuffer;
		List<StringBuffer> printedContents = new ArrayList<>();
		Triple<State, StringBuffer, Boolean> concreteString;
		List<Format> formats;
		List<Format> nonVoidFormats = new ArrayList<>();

		concreteString = this.evaluator.getString(arguments[0].getSource(),
				state, process, arguments[0], argumentValues[0]);
		formatBuffer = concreteString.second;
		state = concreteString.first;
		formats = this.splitFormat(arguments[0].getSource(), formatBuffer);
		for (Format format : formats) {
			if (format.type != ConversionType.VOID)
				nonVoidFormats.add(format);
		}
		assert nonVoidFormats.size() == argumentValues.length - 1;
		for (int i = 1; i < argumentValues.length; i++) {
			SymbolicExpression argumentValue = argumentValues[i];
			CIVLType argumentType = arguments[i].getExpressionType();

			if (argumentType instanceof CIVLPointerType
					&& ((CIVLPointerType) argumentType).baseType().isCharType()
					&& argumentValue.operator() == SymbolicOperator.TUPLE) {
				Format myFormat = nonVoidFormats.get(i - 1);

				if (myFormat.type == ConversionType.STRING) {
					concreteString = this.evaluator.getString(
							arguments[i].getSource(), state, process,
							arguments[i], argumentValue);
					stringOfSymbolicExpression = concreteString.second;
					state = concreteString.first;
					printedContents.add(stringOfSymbolicExpression);
				} else if (myFormat.type == ConversionType.POINTER) {
					printedContents.add(new StringBuffer(
							symbolicAnalyzer.symbolicExpressionToString(
									arguments[i].getSource(), state, null,
									argumentValue)));
				} else {
					throw new CIVLSyntaxException("Array pointer unaccepted",
							arguments[i].getSource());
				}

			} else if (argumentType instanceof CIVLPointerType
					&& this.symbolicUtil.isNullPointer(argumentValue)
					&& nonVoidFormats.get(i - 1).type == ConversionType.INT) {
				printedContents.add(new StringBuffer("0"));
			} else
				printedContents.add(new StringBuffer(this.symbolicAnalyzer
						.symbolicExpressionToString(arguments[i].getSource(),
								state, argumentType, argumentValue)));
		}
		if (!civlConfig.isQuiet() && (civlConfig.enablePrintf() || forcePrint))
			this.printf(civlConfig.out(), arguments[0].getSource(), process,
					formats, printedContents);
		return new Evaluation(state, null);
	}

	/**
	 * Parses the format string, according to C11 standards. For example,
	 * <code>"This is process %d.\n"</code> will be parsed into a list of
	 * strings: <code>"This is process "</code>, <code>"%d"</code>,
	 * <code>".\n"</code>.<br>
	 * 
	 * In Paragraph 4, Section 7.21.6.1, C11 Standards:<br>
	 * Each conversion specification is introduced by the character %. After the
	 * %, the following appear in sequence:
	 * <ul>
	 * <li>Zero or more flags (in any order) that modify the meaning of the
	 * conversion specification.</li>
	 * <li>An optional minimum field width. If the converted value has fewer
	 * characters than the field width, it is padded with spaces (by default) on
	 * the left (or right, if the left adjustment flag, described later, has
	 * been given) to the field width. The field width takes the form of an
	 * asterisk * (described later) or a nonnegative decimal integer.</li>
	 * <li>An optional precision that gives the minimum number of digits to
	 * appear for the d, i, o, u, x, and X conversions, the number of digits to
	 * appear after the decimal-point character for a, A, e, E, f, and F
	 * conversions, the maximum number of significant digits for the g and G
	 * conversions, or the maximum number of bytes to be written for s
	 * conversions. The precision takes the form of a period (.) followed either
	 * by an asterisk * (described later) or by an optional decimal integer; if
	 * only the period is specified, the precision is taken as zero. If a
	 * precision appears with any other conversion specifier, the behavior is
	 * undefined.</li>
	 * <li>An optional length modifier that specifies the size of the argument.
	 * </li>
	 * <li>A conversion specifier character that specifies the type of
	 * conversion to be applied.</li>
	 * </ul>
	 * 
	 * @param source
	 *            The source code element of the format argument.
	 * @param formatBuffer
	 *            The string buffer containing the content of the format string.
	 * @return A list of string buffers by splitting the format by conversion
	 *         specifiers.
	 */
	@Override
	public List<Format> splitFormat(CIVLSource source,
			StringBuffer formatBuffer) {
		int count = formatBuffer.length();
		List<Format> result = new ArrayList<>();
		StringBuffer stringBuffer = new StringBuffer();
		boolean inConversion = false;
		boolean hasFieldWidth = false;
		boolean hasPrecision = false;

		for (int i = 0; i < count; i++) {
			Character current = formatBuffer.charAt(i);
			Character code;
			ConversionType type = ConversionType.VOID;

			if (current.equals('%')) {
				code = formatBuffer.charAt(i + 1);

				if (code.equals('%')) {
					stringBuffer.append("%%");
					i = i + 1;
					continue;
				}
				if (stringBuffer.length() > 0) {
					if (stringBuffer.charAt(0) == '%'
							&& stringBuffer.charAt(1) != '%') {
						throw new CIVLSyntaxException("The format %"
								+ stringBuffer + " is not allowed in fprintf",
								source);
					}
					result.add(new Format(stringBuffer, type));
					stringBuffer = new StringBuffer();
				}
				inConversion = true;
				stringBuffer.append('%');
				current = formatBuffer.charAt(++i);
			}
			if (inConversion) {
				// field width
				if (current.equals('*')) {
					stringBuffer.append('*');
					current = formatBuffer.charAt(++i);
				} else if (numbers.contains(current)) {
					Character next = current;

					if (hasFieldWidth) {
						stringBuffer.append(next);
						throw new CIVLSyntaxException(
								"Duplicate field width in \"" + stringBuffer
										+ "\"...",
								source);
					}
					hasFieldWidth = true;
					while (numbers.contains(next)) {
						stringBuffer.append(next);
						next = formatBuffer.charAt(++i);
					}
					current = next;
				}
				// precision
				if (current.equals('.')) {
					Character next;

					next = formatBuffer.charAt(++i);
					stringBuffer.append('.');
					if (hasPrecision) {
						throw new CIVLSyntaxException(
								"Duplicate precision detected in \""
										+ stringBuffer + "\"...",
								source);
					}
					hasPrecision = true;
					if (next.equals('*')) {
						stringBuffer.append(next);
						next = formatBuffer.charAt(++i);
					} else {
						while (numbers.contains(next)) {
							stringBuffer.append(next);
							next = formatBuffer.charAt(++i);
						}
					}
					current = next;
				}
				// length modifier
				switch (current) {
					case 'h' :
					case 'l' :
						stringBuffer.append(current);
						if (i + 1 >= count)
							throw new CIVLSyntaxException("The format "
									+ stringBuffer + " is not allowed.",
									source);
						else {
							Character next = formatBuffer.charAt(i + 1);

							if (next.equals(current)) {
								i++;
								stringBuffer.append(next);
							}
							current = formatBuffer.charAt(++i);
						}
						break;
					case 'j' :
					case 'z' :
					case 't' :
					case 'L' :
						stringBuffer.append(current);
						i++;
						if (i >= count)
							throw new CIVLSyntaxException("Invalid format \"%"
									+ current + "\" for fprintf/printf",
									source);
						current = formatBuffer.charAt(i);
						break;
					default :
				}
				// conversion specifier
				switch (current) {
					case 'c' :
					case 'p' :
					case 'n' :
						if (hasFieldWidth || hasPrecision) {
							throw new CIVLSyntaxException(
									"Invalid precision for the format \"%"
											+ current + "\"...",
									source);
						}
					default :
				}
				switch (current) {
					case 'c' :
						type = ConversionType.CHAR;
						break;
					case 'p' :
					case 'n' :
						type = ConversionType.POINTER;
						break;
					case 'd' :
					case 'i' :
					case 'o' :
					case 'u' :
					case 'x' :
					case 'X' :
						type = ConversionType.INT;
						break;
					case 'a' :
					case 'A' :
					case 'e' :
					case 'E' :
					case 'f' :
					case 'F' :
					case 'g' :
					case 'G' :
						type = ConversionType.DOUBLE;
						break;
					case 's' :
						type = ConversionType.STRING;
						break;
					default :
						stringBuffer.append(current);
						throw new CIVLSyntaxException("The format %"
								+ stringBuffer + " is not allowed in fprintf",
								source);
				}
				stringBuffer.append(current);
				result.add(new Format(stringBuffer, type));
				inConversion = false;
				hasFieldWidth = false;
				hasPrecision = false;
				stringBuffer = new StringBuffer();
			} else if (current == CIVLConstants.EOS) {
				break;
			} else
				stringBuffer.append(current);
		}
		if (stringBuffer.length() > 0)
			result.add(new Format(stringBuffer, ConversionType.VOID));
		return result;
	}

	@Override
	public void printf(PrintStream printStream, CIVLSource source,
			String process, List<Format> formats,
			List<StringBuffer> arguments) {
		int argIndex = 0;
		int numArguments = arguments.size();

		for (Format format : formats) {
			String formatString = format.toString();

			switch (format.type) {
				case VOID :
					printStream.print(formatString);
					break;
				default :
					assert argIndex < numArguments;
					printStream.printf("%s", arguments.get(argIndex++));
			}
		}

	}

	/**
	 * <p>
	 * assigns a given value to a memory pointed to by a pointer at a given
	 * state by a certain process.
	 * </p>
	 * 
	 * <p>
	 * This is the core method which delivers changes in state, it is called by
	 * other higher-level assign methods.
	 * </p>
	 * 
	 * @param source
	 *            the source for error report
	 * @param state
	 *            the pre-state
	 * @param process
	 *            the process name for error report
	 * @param lhs
	 *            the left hand side expression that represents the memory to be
	 *            written
	 * @param value
	 *            the value to be used for the assignment
	 * @param isInitialization
	 *            true iff this is an initialization assignment in the model. if
	 *            this is true, then the checking of write-to-input-variable
	 *            error is disable.
	 * @param tocheckPointer
	 *            true iff checking of the validness of the pointer is enabled
	 * @return the post state resulting performing the assignment using the
	 *         given parameters
	 * @throws UnsatisfiablePathConditionException
	 *             if the memory represented by the lhs expression is invalid
	 */
	private State assignToPointer(CIVLSource source, State state, int pid,
			SymbolicExpression pointer, SymbolicExpression value,
			boolean isInitialization, boolean toCheckPointer)
			throws UnsatisfiablePathConditionException {
		Pair<BooleanExpression, ResultType> checkPointer = symbolicAnalyzer
				.isDerefablePointer(state, pointer);

		if (checkPointer.right != ResultType.YES) // {
			state = errorLogger.logError(source, state, pid,
					symbolicAnalyzer.stateInformation(state), checkPointer.left,
					checkPointer.right, ErrorKind.DEREFERENCE,
					"attempt to write to a memory location through the pointer "
							+ this.symbolicAnalyzer.symbolicExpressionToString(
									source, state, null, pointer)
							+ " which can't be dereferenced");
		// throw new UnsatisfiablePathConditionException();
		// } else {

		int vid = symbolicUtil.getVariableId(source, pointer);
		int sid = stateFactory
				.getDyscopeId(symbolicUtil.getScopeValue(pointer));
		ReferenceExpression symRef = symbolicUtil.getSymRef(pointer);
		Variable variable;
		// Evaluation eval;

		// eval = evaluator.dereference(source, state, process, null, pointer,
		// false);
		// state = eval.state;
		if (sid < 0) {
			String process = state.getProcessState(pid).name();

			errorLogger.logSimpleError(source, state, process,
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.DEREFERENCE,
					"Attempt to dereference pointer into scope which has been removed from state");
			throw new UnsatisfiablePathConditionException();
		}
		variable = state.getDyscope(sid).lexicalScope().variable(vid);
		if (!isInitialization) {
			if (variable.isInput()) {
				String process = state.getProcessState(pid).name();

				errorLogger.logSimpleError(source, state, process,
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.INPUT_WRITE,
						"Attempt to write to input variable "
								+ variable.name());
				throw new UnsatisfiablePathConditionException();
			} else if (variable.isConst()) {
				String process = state.getProcessState(pid).name();

				errorLogger.logSimpleError(source, state, process,
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.CONSTANT_WRITE,
						"Attempt to write to constant variable "
								+ variable.name());
				throw new UnsatisfiablePathConditionException();
			}
		}
		// write to variable:
		if (symRef.isIdentityReference()) {
			state = stateFactory.setVariable(state, vid, sid, value);
		} else {
			SymbolicExpression oldVariableValue = state.getVariableValue(sid,
					vid);

			try {
				SymbolicExpression newVariableValue = universe
						.assign(oldVariableValue, symRef, value);

				state = stateFactory.setVariable(state, vid, sid,
						newVariableValue);
			} catch (SARLException e) {
				String process = state.getProcessState(pid).name();

				errorLogger.logSimpleError(source, state, process,
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.DEREFERENCE,
						"Invalid assignment: " + e.getMessage());
				throw new UnsatisfiablePathConditionException();
			}
		}
		// write set recording:
		boolean saveWrite = state.isMonitoringWrites(pid);

		if (saveWrite) {
			Evaluation eval = evaluator.memEvaluator().pointer2memValue(state,
					pid, pointer, source);

			state = stateFactory.addReadWriteRecords(eval.state, pid,
					eval.value, false);
		}
		return state;
	}

	/**
	 * assigns a given value to a memory represented by a certain lhs expression
	 * at a given state by a certain process.
	 * 
	 * @param state
	 *            the pre-state
	 * @param pid
	 *            the PID of the process that executes the assignment
	 * @param process
	 *            the process name for error report
	 * @param lhs
	 *            the left hand side expression that represents the memory to be
	 *            written
	 * @param value
	 *            the value to be used for the assignment
	 * @param isInitializer
	 *            true iff this is an initialization assignment in the model. if
	 *            this is true, then the checking of write-to-input-variable
	 *            error is disable.
	 * @return the post state resulting performing the assignment using the
	 *         given parameters
	 * @throws UnsatisfiablePathConditionException
	 *             if the memory represented by the lhs expression is invalid
	 */
	private State assignLHS(State state, int pid, String process,
			LHSExpression lhs, SymbolicExpression value, boolean isInitializer)
			throws UnsatisfiablePathConditionException {
		boolean captureRead = state.isMonitoringReads(pid);

		if (captureRead)
			this.evaluator = this.evaluatorOnTheBench;

		LHSExpressionKind kind = lhs.lhsExpressionKind();
		Evaluation eval = processRHSValue(state, pid, lhs, value,
				isInitializer);

		value = eval.value;
		state = eval.state;
		if (kind == LHSExpressionKind.VARIABLE) {
			Variable variable = ((VariableExpression) lhs).variable();
			int dyscopeId = state.getDyscopeID(pid, variable);
			boolean saveWrite = state.isMonitoringWrites(pid);

			state = stateFactory.setVariable(state, variable, pid, value);
			if (saveWrite) {
				eval = evaluator.memEvaluator().pointer2memValue(
						state, pid, symbolicUtil.makePointer(dyscopeId,
								variable.vid(), universe.identityReference()),
						lhs.getSource());
				state = stateFactory.addReadWriteRecords(eval.state, pid,
						eval.value, false);
			}
		} else {
			boolean toCheckPointer = kind == LHSExpressionKind.DEREFERENCE;

			eval = evaluator.reference(state, pid, lhs);

			state = assignToPointer(lhs.getSource(), eval.state, pid,
					eval.value, value, isInitializer, toCheckPointer);
		}
		if (captureRead) {
			this.evaluator = readSetCollectEvaluator;
			state = readSetCollectEvaluator.collectForLHS(state, pid, lhs);
		}
		return state;
	}

	/**
	 * <p>
	 * This method processes the dynamic value of the right-hand side expression
	 * in two aspects:
	 * <ol>
	 * <li>1) Check if a value is assignable to a {@link LHSExpression} via
	 * checking if they have compatible dynamic types. The definition of the
	 * compatibility of dynamic types for assignment is given by
	 * {@link #areDynamicTypesCompatiableForAssign(SymbolicType, SymbolicType)}.
	 * </li>
	 * </ol>
	 * </p>
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the running process
	 * @param lhs
	 *            an instance of {@link LHSExpression} which is going to be
	 *            assigned
	 * @param value
	 *            the value that will assign to a {@link LHSExpression}, it is
	 *            an instance of {@link SymbolicExpression}
	 * @param isInitializer
	 *            true iff this assign operation is an initialization of a
	 *            variable.
	 * @throws UnsatisfiablePathConditionException
	 * @return the (maybe) updated right-hand side value
	 */
	Evaluation processRHSValue(State state, int pid, LHSExpression lhs,
			SymbolicExpression value, boolean isInitializer)
			throws UnsatisfiablePathConditionException {
		String process = state.getProcessState(pid).name();
		Evaluation eval = new Evaluation(state, value);

		// When types of lhs and rhs are 1) non-scalar types, 2) non-bundle
		// type and 3) non-mem type check if the types of lhs and rhs are
		// compatiable:
		if (!lhs.getExpressionType().isScalar()
				&& !lhs.getExpressionType().isBundleType()
				&& lhs.getExpressionType().typeKind() != TypeKind.MEM) {
			SymbolicType lhsType, rhsType;

			if (!isInitializer)
				lhsType = evaluator.evaluate(state, pid, lhs).value.type();
			else
				lhsType = evaluator.getDynamicType(state, pid,
						lhs.getExpressionType(), lhs.getSource(), false).type;
			rhsType = eval.value.type();
			if (!symbolicAnalyzer.areDynamicTypesCompatiableForAssign(lhsType,
					rhsType))
				errorLogger.logSimpleError(lhs.getSource(), state, process,
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.OTHER,
						"The dynamic types of the left-hand side and "
								+ "the right-hand side expression of the assignment\n"
								+ "operation are not compatible.\n"
								+ "LHS type: " + lhsType + "\nRHS type: "
								+ rhsType);
		}
		return eval;
	}

	/* *********************** Methods from Executor *********************** */

	@Override
	public State assign(CIVLSource source, State state, int pid,
			SymbolicExpression pointer, SymbolicExpression value)
			throws UnsatisfiablePathConditionException {
		return this.assignToPointer(source, state, pid, pointer, value, false,
				true);
	}

	@Override
	public State assign2(CIVLSource source, State state, int pid,
			SymbolicExpression pointerToVarOrHeapObj,
			SymbolicExpression newValueOfVarOrHeapObj,
			SymbolicExpression valueSetTemplate)
			throws UnsatisfiablePathConditionException {
		// check pointer that is valid and it is points to variable or
		// heap object:
		int vid = symbolicUtil.getVariableId(source, pointerToVarOrHeapObj);
		boolean validPointer;

		// "vid == 0" means that it is a pointer to heap:
		if (vid == 0)
			validPointer = symbolicUtil.isPointerToHeap(pointerToVarOrHeapObj);
		else
			validPointer = symbolicUtil.getSymRef(pointerToVarOrHeapObj)
					.isIdentityReference();
		if (!validPointer)
			throw new CIVLInternalException(
					"Calling method assign2 with unexpected pointer value:"
							+ pointerToVarOrHeapObj,
					source);

		String process = state.getProcessState(pid).name();
		SymbolicExpression oldValue;
		Evaluation eval = evaluator.dereference(source, state, process,
				pointerToVarOrHeapObj, true, false);
		int sid = stateFactory.getDyscopeId(
				symbolicUtil.getScopeValue(pointerToVarOrHeapObj));
		Variable var = eval.state.getDyscope(sid).lexicalScope().variable(vid);

		state = eval.state;
		oldValue = eval.value;
		// either oldValue or newValue is NULL, implies that the variable has
		// primitive type:
		assert !(newValueOfVarOrHeapObj.isNull() || oldValue.isNull())
				|| var.type().typeKind() == TypeKind.PRIMITIVE;
		if (var.type().typeKind() != TypeKind.PRIMITIVE)
			newValueOfVarOrHeapObj = universe.valueSetAssigns(oldValue,
					valueSetTemplate, newValueOfVarOrHeapObj);
		// sets variable value in state to complete the assignment:
		if (vid == 0) {
			SymbolicExpression oldHeapVar = state.getVariableValue(sid, vid);

			newValueOfVarOrHeapObj = universe.assign(oldHeapVar,
					symbolicUtil.getSymRef(pointerToVarOrHeapObj),
					newValueOfVarOrHeapObj);
		}
		state = stateFactory.setVariable(state, vid, sid,
				newValueOfVarOrHeapObj);

		boolean saveWrite = state.isMonitoringWrites(pid);

		if (saveWrite) {
			eval = evaluator.memEvaluator().makeMemValue(state, pid,
					pointerToVarOrHeapObj, valueSetTemplate, source);

			state = eval.state;
			// write is also read
			state = stateFactory.addReadWriteRecords(state, pid, eval.value,
					false);
		}
		return state;
	}

	@Override
	public State assign(State state, int pid, String process, LHSExpression lhs,
			SymbolicExpression value, boolean isInitializer)
			throws UnsatisfiablePathConditionException {
		return this.assignLHS(state, pid, process, lhs, value, isInitializer);
	}

	@Override
	public Evaluator evaluator() {
		return evaluator;
	}

	@Override
	public long getNumSteps() {
		return numSteps.longValue();
	}

	@Override
	public Evaluation malloc(CIVLSource source, State state, int pid,
			String process, Expression scopeExpression,
			SymbolicExpression scopeValue, CIVLType objectType,
			SymbolicExpression objectValue)
			throws UnsatisfiablePathConditionException {
		int mallocId = typeFactory.getHeapFieldId(objectType);
		int dyscopeID;
		SymbolicExpression heapObject;
		Pair<State, SymbolicExpression> result;

		dyscopeID = stateFactory.getDyscopeId(scopeValue);
		heapObject = universe.array(objectType.getDynamicType(universe),
				Arrays.asList(objectValue));
		result = stateFactory.malloc(state, dyscopeID, mallocId, heapObject);
		state = result.left;

		/*
		 * Comment out the following code for the reason of not recording malloc
		 * as a write footprint.
		 * 
		 * boolean saveWrite = state.isMonitoringWrites(pid);
		 * 
		 * if (saveWrite) { SymbolicExpression pointer2memoryBlk = symbolicUtil
		 * .parentPointer(result.right);
		 * 
		 * 
		 * 
		 * Evaluation eval = evaluator.memEvaluator().pointer2memValue(state,
		 * pid, pointer2memoryBlk, source);
		 * 
		 * state = eval.state; if (saveWrite) state =
		 * stateFactory.addReadWriteRecords(state, pid, eval.value, false); }
		 */
		return new Evaluation(state, result.right);
	}

	@Override
	public StateFactory stateFactory() {
		return stateFactory;
	}

	@Override
	public State execute(State state, int pid, Transition transition)
			throws UnsatisfiablePathConditionException {
		AtomicLockAction atomicLockAction = transition.atomicLockAction();

		switch (atomicLockAction) {
			case GRAB :
				state = stateFactory.getAtomicLock(state, pid);
				break;
			case RELEASE :
				state = stateFactory.releaseAtomicLock(state);
				break;
			case NONE :
				break;
			default :
				throw new CIVLUnimplementedFeatureException(
						"Executing a transition with the atomic lock action "
								+ atomicLockAction.toString(),
						transition.statement().getSource());
		}
		// if transition doesn't carry new clause, no need to update the path
		// condition, neither for simplifying the state
		if (!transition.clause().isTrue()) {
			state = stateFactory.addToPathcondition(state, pid,
					transition.clause());
			if (transition.simpifyState()
					&& (civlConfig.svcomp() || this.civlConfig.simplify()))
				state = this.stateFactory.simplify(state);
		}
		switch (transition.transitionKind()) {
			case NORMAL :
				state = this.executeStatement(state, pid,
						transition.statement());
				break;
			case NOOP :
				state = this.stateFactory.setLocation(state, pid,
						((NoopTransition) transition).statement().target());
				break;
			default :
				throw new CIVLUnimplementedFeatureException(
						"Executing a transition of kind "
								+ transition.transitionKind(),
						transition.statement().getSource());

		}
		return state;
	}

	@Override
	public CIVLErrorLogger errorLogger() {
		return this.errorLogger;
	}

	@Override
	public void setConfiguration(CIVLConfiguration config) {
		this.civlConfig = config;
	}
}
