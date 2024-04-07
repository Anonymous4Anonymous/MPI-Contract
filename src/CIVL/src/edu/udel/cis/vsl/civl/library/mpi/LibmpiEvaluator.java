package edu.udel.cis.vsl.civl.library.mpi;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.library.common.BaseLibraryEvaluator;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.BinaryExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LambdaExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.MPIContractExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.MPIContractExpression.MPI_CONTRACT_EXPRESSION_KIND;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLPrimitiveType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.semantics.common.ErrorSideEffectFreeEvaluator;
import edu.udel.cis.vsl.civl.semantics.common.QuantifiedExpressionEvaluator;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * <p>
 * <b>Summary</b> This class is an evaluator for evaluating expressions with MPI
 * specific semantics, including (partial) collective evaluation, semantics of
 * {@link MPIContractExpression}s and snap-shooting etc.
 * </p>
 * 
 * <p>
 * (Partial) Collective evaluation is an approach of evaluating expressions that
 * involving variables come from different MPI processes. Although it is one of
 * the most well-known feature of MPI that there is no shared storage between
 * any pair of MPI processes, one can use some auxiliary variables to expression
 * properties that involving a set of MPI processes and prove if they holds.
 * 
 * <ul>
 * <li><b>Collective evaluation c[E, comm, merge, Sp]:</b> A collective
 * evaluation is a tuple: a set of expressions E, an MPI communicator comm ,a
 * function merge(Sp) which maps a set of snapshots Sp to a state s and a set of
 * snapshots Sp. The MPI communicator comm associates to a set of MPI processes
 * P, for each process p in P, it matches a unique snapshot sp in Sp. Thus |Sp|
 * == |P|. The result of the collective evaluation is a set of symbolic values.
 * </li>
 * 
 * <li><b>Partial collective evaluation pc[E, comm, merge', Sp', s]:</b> A
 * partial collective evaluation is a tuple, in addition to the 4 elements of
 * c[E, comm, merge', Sp'], there is one more which is the current state s.
 * Compare to collective evaluation, there are some constraints: the function
 * merge'(Sp', s) maps a set of snapshots Sp' and a state s to a merged state
 * s'. Snapshots in Sp' are committed by the set of processes P', P' is a subset
 * of P. There exists one process set P'' which is also a subset of P. P' and
 * P'' are disjoint, the union of P' and P'' equals to P. s' consists of all
 * snapshots in Sp' and another set of snapshots Sp'' taken on s for processes
 * in P''. The result of the collective evaluation is a set of symbolic values.
 * .</li>
 * 
 * <li><b>Synchronization requirements [WP, a, comm, l]:</b>A synchronization
 * requirement is a tuple: A set of MPI processes WP, an assumption a , an MPI
 * communicator comm and a program location l. It expresses such a
 * synchronization property: It current process satisfies assumption a, the
 * current process can not keep executing until all processes in WP have reached
 * the location l. WP must be a subset of P which is associated to comm.</li>
 * </ul>
 * </p>
 * 
 * 
 * @author xxxx
 *
 */
public class LibmpiEvaluator extends BaseLibraryEvaluator
		implements
			LibraryEvaluator {
	public final IntObject queueIDField = universe.intObject(4);
	public final NumericExpression p2pCommIndexValue = zero;
	public final NumericExpression colCommIndexValue = one;
	public final static String mpiExtentName = "_uf_$mpi_extent";

	private static final String nonoverlappingAbsFunName = "_uf_$mpi_non_overlapping";

	private static final String mpiSeqValueWrapperName = "_uf_$mpi_seq_wrap";

	private SymbolicConstant nonoverlappingAbsFun = null;

	public LibmpiEvaluator(String name, Evaluator evaluator,
			ModelFactory modelFactory, SymbolicUtility symbolicUtil,
			SymbolicAnalyzer symbolicAnalyzer, CIVLConfiguration civlConfig,
			LibraryEvaluatorLoader libEvaluatorLoader) {
		super(name, evaluator, modelFactory, symbolicUtil, symbolicAnalyzer,
				civlConfig, libEvaluatorLoader);
	}

	public static Pair<CIVLPrimitiveType, NumericExpression> mpiTypeToCIVLType(
			SymbolicUniverse universe, CIVLTypeFactory typeFactory,
			int MPI_TYPE, CIVLSource source) {
		CIVLPrimitiveType primitiveType;
		NumericExpression count = universe.oneInt();

		switch (MPI_TYPE) {
			case 0 : // char
			case 1 : // character
			case 2 : // byte
				primitiveType = typeFactory.charType();
				break;
			case 3 : // short
			case 4 : // int
			case 5 : // long
			case 6 : // long long int
			case 7 : // unsigned long long
			case 8 : // long long
				primitiveType = typeFactory.integerType();
				break;
			case 9 : // float
			case 10 : // double
			case 11 : // long double
				primitiveType = typeFactory.realType();
				break;
			case 12 : // 2int
				primitiveType = typeFactory.integerType();
				count = universe.integer(2);
				break;
			default :
				throw new CIVLUnimplementedFeatureException(
						"CIVL doesn't have such a CIVLPrimitiveType", source);
		}
		return new Pair<>(primitiveType, count);
		/*
		 * MPI_CHAR, MPI_CHARACTER, MPI_SIGNED_CHAR, MPI_UNSIGNED_CHAR,
		 * MPI_BYTE, MPI_WCHAR, MPI_SHORT, MPI_UNSIGNED_SHORT, MPI_INT,
		 * MPI_INT16_T, MPI_INT32_T, MPI_INT64_T, MPI_INT8_T, MPI_INTEGER,
		 * MPI_INTEGER1, MPI_INTEGER16, MPI_INTEGER2, MPI_INTEGER4,
		 * MPI_INTEGER8, MPI_UNSIGNED, MPI_LONG, MPI_UNSIGNED_LONG, MPI_FLOAT,
		 * MPI_DOUBLE, MPI_LONG_DOUBLE, MPI_LONG_LONG_INT,
		 * MPI_UNSIGNED_LONG_LONG, MPI_LONG_LONG, MPI_PACKED, MPI_LB, MPI_UB,
		 * MPI_UINT16_T, MPI_UINT32_T, MPI_UINT64_T, MPI_UINT8_T, MPI_FLOAT_INT,
		 * MPI_DOUBLE_INT, MPI_LONG_INT, MPI_SHORT_INT, MPI_2INT,
		 * MPI_LONG_DOUBLE_INT, MPI_AINT, MPI_OFFSET, MPI_2DOUBLE_PRECISION,
		 * MPI_2INTEGER, MPI_2REAL, MPI_C_BOOL, MPI_C_COMPLEX,
		 * MPI_C_DOUBLE_COMPLEX, MPI_C_FLOAT_COMPLEX, MPI_C_LONG_DOUBLE_COMPLEX,
		 * MPI_COMPLEX, MPI_COMPLEX16, MPI_COMPLEX32, MPI_COMPLEX4,
		 * MPI_COMPLEX8, MPI_REAL, MPI_REAL16, MPI_REAL2, MPI_REAL4, MPI_REAL8
		 */
	}
	/**************************** Contract section ****************************/
	/**
	 * <p>
	 * <b>Summary:</b> Evaluates an {@link MPIContractExpression}.
	 * </p>
	 * 
	 * @param state
	 *            The current state.
	 * @param pid
	 *            The PID of the process.
	 * @param process
	 *            The String identifier of the process.
	 * @param expression
	 *            The MPIContractExpression
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	public Evaluation evaluateMPIContractExpression(State state, int pid,
			String process, MPIContractExpression expression)
			throws UnsatisfiablePathConditionException {
		MPI_CONTRACT_EXPRESSION_KIND mpiContractKind = expression
				.mpiContractKind();

		switch (mpiContractKind) {
			case MPI_AGREE :
				return evaluateMPIAgreeExpression(state, pid, process,
						expression);
			case MPI_BUF :
				return evaluateMPIBuf(state, pid, process, expression);
			case MPI_SEQ :
				return evaluateMPISeq(state, pid, process, expression);
			case MPI_REDUCE :
				return evaluateMPIReduce(state, pid, process, expression);
			// case MPI_EXTENT :
			// return evaluateMPIExtent(state, pid, process, expression);
			// case MPI_VALID :
			// return evaluateMPIValid(state, pid, process, expression);
			default :
				throw new CIVLInternalException(
						"Unreachable: " + mpiContractKind,
						expression.getSource());
		}
	}

	/**
	 * <p>
	 * <b>Pre-condition:</b> The state is a collate state and the pid represents
	 * the process in the collate state. By looking at the call stack of a
	 * collate state, one can decide weather a process has committed its'
	 * snapshot to the collate state.
	 * </p>
	 * 
	 * <p>
	 * Let eval(e, p, s) denote the evaluation of expression e on process p in
	 * state s. There is a set P of processes in the collate state c that for
	 * each p in P, p has a non-empty call stack (i.e. process p has committed
	 * its snapshot), then <code> for all p_i and p_j in P (p_i != p_j),
	 * eval(expression, p_i, c) == eval(expression, p_j, c)
	 * </code>
	 * </p>
	 * 
	 * @param state
	 *            The current state
	 * @param pid
	 *            The current PID of the process
	 * @param process
	 *            The String identifier of the process
	 * @param expression
	 *            The \mpi_agree(expr) expression
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation evaluateMPIAgreeExpression(State state, int pid,
			String process, MPIContractExpression expression)
			throws UnsatisfiablePathConditionException {
		int nprocs = state.numProcs();
		BooleanExpression pred = universe.trueExpression();
		SymbolicExpression value;
		Evaluation eval;
		Expression expr = expression.arguments()[0];
		boolean isMPISeqType = expr.getExpressionType()
				.typeKind() == CIVLType.TypeKind.MPI_SEQ;

		eval = evaluator.evaluate(state, pid, expr);
		state = eval.state;
		value = eval.value;
		for (int i = 0; i < nprocs; i++)
			if (i != pid && !state.getProcessState(i).hasEmptyStack()) {
				eval = evaluator.evaluate(state, i, expr);
				state = eval.state;
				if (isMPISeqType)
					pred = universe.and(pred, assertMPISeqValueEqual(value,
							eval.value, universe));
				else
					pred = universe.and(pred,
							universe.equals(value, eval.value));
			}
		eval.state = state;
		eval.value = pred;
		return eval;
	}

	private Evaluation evaluateMPINonoverlapping(State state, int pid,
			String process, MPIContractExpression mpiNonoverlapping)
			throws UnsatisfiablePathConditionException {
		Expression datatype = mpiNonoverlapping.arguments()[0];
		Evaluation eval = evaluator.evaluate(state, pid, datatype);

		if (nonoverlappingAbsFun == null) {
			SymbolicFunctionType funcType = universe.functionType(
					Arrays.asList(eval.value.type()), universe.booleanType());

			nonoverlappingAbsFun = universe.symbolicConstant(
					universe.stringObject(nonoverlappingAbsFunName), funcType);
		}
		if (universe.extractNumber((NumericExpression) eval.value) != null)
			eval.value = universe.trueExpression();
		else
			eval.value = universe.apply(nonoverlappingAbsFun,
					Arrays.asList(eval.value));
		return eval;
	}

	private Evaluation evaluateMPISeq(State state, int pid, String process,
			MPIContractExpression mpiSeq)
			throws UnsatisfiablePathConditionException {
		Expression mpiBuf = mpiSeq.arguments()[0];
		Evaluation eval;

		if (mpiBuf instanceof BinaryExpression) {
			eval = evaluator.evaluate(state, pid, mpiBuf);
		} else
			eval = evaluator.evaluate(state, pid, mpiBuf);
		// tuple (ptr, count, datatype):
		SymbolicExpression mpiBufVal = eval.value;
		SymbolicExpression basePtrVal, countVal, datatypeVal;

		state = eval.state;
		basePtrVal = universe.tupleRead(mpiBufVal, zeroObject);
		countVal = universe.tupleRead(mpiBufVal, oneObject);
		datatypeVal = universe.tupleRead(mpiBufVal, twoObject);

		Pair<SymbolicExpression, NumericExpression> mpiPtr_datatypeSize = processMPIPointer(
				state, pid, process, basePtrVal,
				(NumericExpression) datatypeVal, mpiSeq.getSource(),
				symbolicAnalyzer, symbolicUtil, evaluator);
		NumericExpression realCount = universe.multiply(
				(NumericExpression) countVal, mpiPtr_datatypeSize.right);

		basePtrVal = mpiPtr_datatypeSize.left;
		eval = getDataFrom(state, pid, process, mpiBuf, basePtrVal, realCount,
				false, false, mpiSeq.getSource());

		SymbolicType mpiSeqWrapperFuncType = universe.functionType(
				Arrays.asList(eval.value.type(), universe.integerType(),
						universe.integerType()),
				typeFactory.mpiSeqType().getDynamicType(universe));
		SymbolicConstant mpiSeqWrapper = universe.symbolicConstant(
				universe.stringObject(mpiSeqValueWrapperName),
				mpiSeqWrapperFuncType);

		eval.value = universe.apply(mpiSeqWrapper,
				Arrays.asList(eval.value, countVal, datatypeVal));
		return eval;
	}

	private Evaluation evaluateMPIBuf(State state, int pid, String process,
			MPIContractExpression mpiBuf)
			throws UnsatisfiablePathConditionException {
		Expression ptr = mpiBuf.arguments()[0];
		Expression count = mpiBuf.arguments()[1];
		Expression datatype = mpiBuf.arguments()[2];
		SymbolicExpression ptrVal, countVal, datatypeVal;
		Evaluation eval;

		eval = evaluator.evaluate(state, pid, ptr);
		ptrVal = eval.value;
		eval = evaluator.evaluate(eval.state, pid, count);
		countVal = eval.value;
		eval = evaluator.evaluate(eval.state, pid, datatype);
		state = eval.state;
		datatypeVal = eval.value;

		SymbolicTupleType dyBufType = typeFactory.mpiBufType()
				.getDynamicType(universe);

		eval.value = universe.tuple(dyBufType,
				Arrays.asList(ptrVal, countVal, datatypeVal));
		return eval;
	}

	private Evaluation evaluateMPIReduce(State state, int pid, String process,
			MPIContractExpression expression)
			throws UnsatisfiablePathConditionException {
		Expression seq = expression.arguments()[0];
		Expression lo = expression.arguments()[1];
		Expression hi = expression.arguments()[2];
		Expression op = expression.arguments()[3];
		Expression lambda = expression.arguments()[4];
		Evaluation eval;
		SymbolicExpression seqVal, loVal, hiVal, opVal;
		NumericExpression seqValCount, seqValElementsize;

		eval = evaluator.evaluate(state, pid, seq);
		seqVal = eval.value;
		// extract data out of mpi_seq wrapper
		seqValElementsize = getMPISeqElementsize(seqVal);
		seqValCount = getMPISeqCount(seqVal);
		seqVal = getMPISeqData(seqVal);
		eval = evaluator.evaluate(eval.state, pid, lo);
		loVal = eval.value;
		eval = evaluator.evaluate(eval.state, pid, hi);
		hiVal = eval.value;
		eval = evaluator.evaluate(eval.state, pid, op);
		opVal = eval.value;
		state = eval.state;

		IntegerNumber loNum = (IntegerNumber) universe
				.extractNumber((NumericExpression) loVal);
		IntegerNumber hiNum = (IntegerNumber) universe
				.extractNumber((NumericExpression) hiVal);
		IntegerNumber opNum = (IntegerNumber) universe
				.extractNumber((NumericExpression) opVal);

		// currently, lower bound and upper bound must be concrete:
		if (loNum == null || hiNum == null)
			throw new CIVLUnimplementedFeatureException(
					"The lower bound or upper bound of an \\mpi_reduce expression is non-concrete:\n"
							+ "lower:" + lo + "\nupper:" + hi);

		Pair<State, SymbolicExpression[][]> evaledOperands = getMPIReduceOperandsDataCountElementsize(
				loNum, hiNum, state, pid, (LambdaExpression) lambda);
		SymbolicExpression[][] operands = evaledOperands.right;

		eval.state = evaledOperands.left;
		if (opNum != null) {
			CIVLOperator operator = translateOperator(opNum.intValue());

			// if loVal, hiVal and opVal are all concrete, we have the
			// concrete case:
			eval.value = concreteMPIReductionAssertion(operands, operator,
					seqVal, seqValCount, seqValElementsize,
					expression.getSource());
			return eval;
		}
		// otherwise, we use the general reduction representation provided by
		// SARL:
		eval.value = uninterpretedMPIReductionAssertion(operands, opVal, seqVal,
				seqValCount, seqValElementsize);
		return eval;
	}

	private BooleanExpression concreteMPIReductionAssertion(
			SymbolicExpression[][] operands, CIVLOperator op,
			SymbolicExpression value, NumericExpression count,
			NumericExpression elementsize, CIVLSource source) {
		BiFunction<NumericExpression, NumericExpression, NumericExpression> operation = null;
		BooleanExpression cond = universe.trueExpression();
		NumericExpression valueSignature = universe.multiply(count,
				elementsize);
		Set<SymbolicConstant> freeVars = value.getFreeVars();
		NumericSymbolicConstant fv;

		for (int i = 0; i < operands[0].length; i++) {
			cond = universe.and(cond, universe.equals(
					universe.multiply((NumericExpression) operands[1][i],
							(NumericExpression) operands[2][i]),
					valueSignature));
			freeVars.addAll(operands[0][i].getFreeVars());
		}

		int counter = 0;

		fv = (NumericSymbolicConstant) universe.symbolicConstant(
				universe.stringObject("i" + counter++), universe.integerType());
		while (freeVars.contains(fv)) {
			fv = (NumericSymbolicConstant) universe.symbolicConstant(
					universe.stringObject("i" + counter++),
					universe.integerType());
		}

		SymbolicType type = ((SymbolicArrayType) value.type()).elementType();
		NumericExpression rdcResult = type.isInteger()
				? universe.zeroInt()
				: universe.zeroReal();
		BooleanExpression result;

		switch (op) {
			case CIVL_SUM : {
				for (SymbolicExpression operand : operands[0])
					rdcResult = universe.add(rdcResult,
							(NumericExpression) universe.arrayRead(operand,
									fv));
				result = universe.equals(universe.arrayRead(value, fv),
						rdcResult);
				result = universe.forallInt(fv, universe.zeroInt(), count,
						result);
				break;
			}
			case CIVL_MAX : {
			    for (SymbolicExpression operand : operands[0]) {
				NumericExpression NewOp = (NumericExpression) universe.arrayRead(operand,
												 fv);				
				BooleanExpression claim = universe.lessThan((NumericExpression) rdcResult,
							  NewOp);
				rdcResult = (NumericExpression)universe.cond(claim, NewOp, rdcResult);
			    }
				result = universe.equals(universe.arrayRead(value, fv),
						rdcResult);
				result = universe.forallInt(fv, universe.zeroInt(), count,
						result);
				break;
			}
			default :
				throw new CIVLInternalException("unreachable", source);
		}
		// TODO: special handling for sigma!
		// TODO: maybe need unroll operand as well?
		return result;
	}

	/**
	 * @return a list of pairs, each pair represents a possible case of the
	 *         reduction under a certain condition. If a return case has its
	 *         reduction be null, it represents a invalid case.
	 */
	private BooleanExpression uninterpretedMPIReductionAssertion(
			SymbolicExpression[][] operands, SymbolicExpression op,
			SymbolicExpression value, NumericExpression count,
			NumericExpression elementSize) {
		// [0][#operands]: data array
		// [1][#operands]: count array
		// [2][#operands]: element-size array
		SymbolicExpression generalReduction = universe.reduction(operands[0],
				elementSize, op, new LinkedList<>());
		List<Pair<BooleanExpression, SymbolicExpression>> conditionalReductions = new LinkedList<>();

		// break the general reduction down to clauses of the form "condition ->
		// reduction"
		generalReduction = breakDownGeneralReduction(generalReduction,
				conditionalReductions);
		if (generalReduction != null)
			conditionalReductions.add(
					new Pair<>(universe.trueExpression(), generalReduction));

		BooleanExpression result = universe.trueExpression();
		NumericExpression valueSignature = universe.multiply(count,
				elementSize);

		for (int i = 0; i < operands[0].length; i++) {
			result = universe.and(result, universe.equals(
					universe.multiply((NumericExpression) operands[1][i],
							(NumericExpression) operands[2][i]),
					valueSignature));
		}
		for (Pair<BooleanExpression, SymbolicExpression> conditionalReduction : conditionalReductions) {
			BooleanExpression clause;

			if (conditionalReduction.right.isNull())
				clause = universe.not(conditionalReduction.left);
			else
				clause = universe.implies(conditionalReduction.left,
						universe.equals(value, conditionalReduction.right));
			result = universe.and(result, clause);
		}
		return result;
	}

	/**
	 * Recursively parses the reduction "rdc" in the general form of <code>
	 *  rdc := 
	 *   c ? rdc : rdc
	 *   | $reduction(p, {...})
	 *   | null 
	 * </code> to a list of pairs, each of which represents a reduction case
	 * under a certaion condition. Note null represents a invalid case.
	 * 
	 * @param generalReduction
	 * @param output
	 *            the output argument holding the parsed list of pairs.
	 * @return a non-null symbolic expression if the given general reduction is
	 *         not a conditional expression.
	 */
	private SymbolicExpression breakDownGeneralReduction(
			SymbolicExpression generalReduction,
			List<Pair<BooleanExpression, SymbolicExpression>> output) {
		if (generalReduction.operator() != SymbolicOperator.COND)
			return generalReduction;

		BooleanExpression cond = (BooleanExpression) generalReduction
				.argument(0);
		SymbolicExpression trueBranch = (SymbolicExpression) generalReduction
				.argument(1);
		SymbolicExpression falseBranch = (SymbolicExpression) generalReduction
				.argument(2);

		trueBranch = breakDownGeneralReduction(trueBranch, output);
		falseBranch = breakDownGeneralReduction(falseBranch, output);
		if (trueBranch != null)
			output.add(new Pair<>(cond, trueBranch));
		if (falseBranch != null)
			output.add(new Pair<>(universe.not(cond), falseBranch));
		return null;
	}

	/**
	 * <p>
	 * Assuming the lambda function is a function from integers to mpi seq type
	 * values. Applies integers from given loNum to hiNum (exclusive) to the
	 * lambda function. Returns the sequence of data extracted out of the
	 * applied results. (see {@link #getMPISeqData(SymbolicExpression)} for
	 * extracting data out of mpi seq values.)
	 * </p>
	 * 
	 * @throws UnsatisfiablePathConditionException
	 */
	private Pair<State, SymbolicExpression[][]> getMPIReduceOperandsDataCountElementsize(
			IntegerNumber loNum, IntegerNumber hiNum, State state, int pid,
			LambdaExpression lambda)
			throws UnsatisfiablePathConditionException {
		int loInt = loNum.intValue();
		int hiInt = hiNum.intValue();
		SymbolicExpression[][] operands = new SymbolicExpression[3][hiInt
				- loInt];
		QuantifiedExpressionEvaluator qeEvaluator;

		if (evaluator instanceof QuantifiedExpressionEvaluator) {
			qeEvaluator = (QuantifiedExpressionEvaluator) evaluator;
		} else {
			qeEvaluator = ((ErrorSideEffectFreeEvaluator) evaluator)
					.newQuantifiedExpressionEvaluator();
		}

		for (int i = loInt; i < hiInt; i++) {
			int index = i - loInt;

			Evaluation eval = qeEvaluator
					.evaluateLambdaWithConcreteFreeVar(state, pid, lambda, i);

			state = eval.state;
			operands[0][index] = eval.value;
			operands[1][index] = getMPISeqCount(operands[0][index]);
			operands[2][index] = getMPISeqElementsize(operands[0][index]);
			operands[0][index] = getMPISeqData(operands[0][index]);
		}
		return new Pair<>(state, operands);
	}

	/**
	 * <p>
	 * An MPI_Valid expression will evaluates to true if and only if the given
	 * pointer points to a sequence of objects that satisfiy the given type
	 * signiture.
	 * </p>
	 * 
	 * @param state
	 *            The program state when this expression evaluates
	 * @param pid
	 *            The PID of the calling process
	 * @param process
	 *            The String identifier of the calling process
	 * @param expression
	 *            The {@link MPIContractExpression} which represents an
	 *            MPI_Valid expression.
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation evaluateMPIValid(State state, int pid, String process,
			MPIContractExpression expression)
			throws UnsatisfiablePathConditionException {
		Expression ptrExpr = expression.arguments()[0];
		Expression countExpr = expression.arguments()[1];
		Expression datatypeExpr = expression.arguments()[2];
		SymbolicExpression ptr;
		NumericExpression count, datatype, realOffset;
		BooleanExpression result;
		Evaluation eval;

		eval = evaluator.evaluate(state, pid, ptrExpr);
		state = eval.state;
		ptr = eval.value;
		eval = evaluator.evaluate(state, pid, countExpr);
		state = eval.state;
		count = (NumericExpression) eval.value;
		eval = evaluator.evaluate(state, pid, datatypeExpr);
		state = eval.state;
		datatype = (NumericExpression) eval.value;

		// Currently the valid checking is done by this:
		// 1. The object type pointed by the given pointer must be consistent
		// with the given datatype;
		// 2. The given pointer is dereferable;
		// 3. The given pointer must be an array element reference &a[i]
		// and length(a) > i + (count * \mpi_extent(datatype))
		Pair<SymbolicExpression, NumericExpression> mpiPtr_datatypeSize;
		ReferenceExpression mpiPtrRef;

		mpiPtr_datatypeSize = processMPIPointer(state, pid, process, ptr,
				datatype, expression.getSource(), symbolicAnalyzer,
				symbolicUtil, evaluator);
		mpiPtrRef = symbolicUtil.getSymRef(mpiPtr_datatypeSize.left);
		// ptr + (real_count - 1):
		realOffset = universe.subtract(
				universe.multiply(count, mpiPtr_datatypeSize.right), one);
		result = symbolicAnalyzer.isDerefablePointer(state,
				mpiPtr_datatypeSize.left).left;
		if (!mpiPtrRef.isArrayElementReference()) {
			eval.value = universe.equals(result,
					universe.equals(realOffset, universe.zeroInt()));
			return eval;
		}
		eval = evaluator.arrayElementReferenceAdd(state, pid,
				mpiPtr_datatypeSize.left, realOffset,
				expression.getSource()).left;
		state = eval.state;
		result = universe.and(result,
				symbolicAnalyzer.isDerefablePointer(state, eval.value).left);
		eval.value = result;
		return eval;
	}

	/**
	 * <p>
	 * An MPI_Extent(datatype) expression will evaluates to the size of an given
	 * MPI_Datatype in number of bytes.
	 * </p>
	 * 
	 * @param state
	 *            The program state when this expression evaluates
	 * @param pid
	 *            The PID of the calling process
	 * @param process
	 *            The String identifier of the calling process
	 * @param expression
	 *            The {@link MPIContractExpression} which represents an
	 *            MPI_Extent expression.
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation evaluateMPIExtent(State state, int pid, String process,
			MPIContractExpression expression)
			throws UnsatisfiablePathConditionException {
		Expression datatypeExpr = expression.arguments()[0];
		Evaluation eval;

		eval = evaluator.evaluate(state, pid, datatypeExpr);
		state = eval.state;
		return eval;
	}

	/**
	 * <p>
	 * The MPI_Offset(ptr, count, datatype) expression semantically menas:
	 * <code> (char *)ptr + count * \mpi_extent(datatype) </code>
	 * </p>
	 * 
	 * @param state
	 *            The program state when this expression evaluates
	 * @param pid
	 *            The PID of the calling process
	 * @param process
	 *            The String identifier of the calling process
	 * @param expression
	 *            The {@link MPIContractExpression} which represents an
	 *            MPI_Offset expression.
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation evaluateMPIOffset(State state, int pid, String process,
			MPIContractExpression expression)
			throws UnsatisfiablePathConditionException {
		Expression ptrExpr = expression.arguments()[0];
		Expression countExpr = expression.arguments()[1];
		Expression datatypeExpr = expression.arguments()[2];
		SymbolicExpression ptr;
		NumericExpression count, datatype;
		Evaluation eval;

		eval = evaluator.evaluate(state, pid, ptrExpr);
		state = eval.state;
		ptr = eval.value;
		eval = evaluator.evaluate(state, pid, countExpr);
		state = eval.state;
		count = (NumericExpression) eval.value;
		eval = evaluator.evaluate(state, pid, datatypeExpr);
		state = eval.state;
		datatype = (NumericExpression) eval.value;

		Pair<SymbolicExpression, NumericExpression> mpiPtr_datatypeSize;

		mpiPtr_datatypeSize = processMPIPointer(state, pid, process, ptr,
				datatype, expression.getSource(), symbolicAnalyzer,
				symbolicUtil, evaluator);
		return evaluator.arrayElementReferenceAdd(state, pid,
				mpiPtr_datatypeSize.left,
				universe.multiply(count, mpiPtr_datatypeSize.right),
				expression.getSource()).left;
	}

	/**
	 * <p>
	 * Checks if the type of the leaf node pointed by the given pointer 'ptr' on
	 * the pointed object is consistnt with the given MPI_Datatype
	 * 'mpiDatatype'.
	 * 
	 * Returns a pair of objects:
	 * <ul>
	 * <li><b>left</b>A new pointer which is obtained by replace the reference
	 * expression of the given pointer with a new reference to the leaf node of
	 * the pointed object. (e.g. int a[0]; &a is casted to &a[0])</li>
	 * <li><b>right</b>The number of primitive types which compose the given
	 * MPI_Datatype. (e.g. MPI_2INT is composed of 2 primitive types)</li>
	 * </ul>
	 * </p>
	 * 
	 * @param state
	 *            The program state when this method is called
	 * @param pid
	 *            The PID of the calling process
	 * @param process
	 *            The String identifier of the process
	 * @param ptrExpr
	 *            The {@link Expression} of the given pointer
	 * @param ptr
	 *            The value of the given pointer
	 * @param mpiDatatypeExtent
	 *            The {@link Expression} of MPI_Datatype
	 * @param mpiDatatype
	 *            The value of the MPI_Datatype.
	 * @return a pair of objects:
	 *         <ul>
	 *         <li><b>left</b>A new pointer which is obtained by replace the
	 *         reference expression of the given pointer with a new reference to
	 *         the leaf node of the pointed object. (e.g. int a[0]; &a is casted
	 *         to &a[0])</li>
	 *         <li><b>right</b>The number of primitive types which compose the
	 *         given MPI_Datatype. (e.g. MPI_2INT is composed of 2 primitive
	 *         types)</li>
	 *         </ul>
	 * @throws UnsatisfiablePathConditionException
	 */
	static Pair<SymbolicExpression, NumericExpression> processMPIPointer(
			State state, int pid, String process, SymbolicExpression ptr,
			NumericExpression mpiDatatype, CIVLSource source,
			SymbolicAnalyzer symbolicAnalyzer, SymbolicUtility symbolicUtil,
			Evaluator evaluator) throws UnsatisfiablePathConditionException {
		ReferenceExpression baseRef = symbolicAnalyzer
				.getLeafNodeReference(state, ptr, source);
		SymbolicExpression basePtr = symbolicUtil.makePointer(ptr, baseRef);
		CIVLType baseType = symbolicAnalyzer.civlTypeOfObjByPointer(source,
				state, basePtr);
		NumericExpression numPrimitives;
		Evaluation eval = evaluator.evaluateSizeofType(source, state, pid,
				baseType);
		NumericExpression sizeof;
		SymbolicUniverse universe = symbolicAnalyzer.getUniverse();

		state = eval.state;
		sizeof = (NumericExpression) eval.value;
		// Now the "mpiDatatype" value is the sizeof(datatype) which encodes
		// SIZE_OF_TYPE symbols:
		numPrimitives = universe.divide(mpiDatatype, sizeof);
		// typeChecking = universe.divides(sizeof, mpiDatatype);
		// reasoner = universe.reasoner(state.getPathCondition());
		// if (!reasoner.isValid(typeChecking)) {
		// String ptrStr = symbolicAnalyzer.expressionEvaluation(state, pid,
		// ptrExpr, true).right;
		// String datatypeStr = symbolicAnalyzer.expressionEvaluation(state,
		// pid, mpiDatatypeExpr, true).right;
		//
		// errorLogger.logSimpleError(source, state, process,
		// symbolicAnalyzer.stateInformation(state),
		// ErrorKind.MPI_ERROR,
		// "Objects pointed by " + ptrStr
		// + " is inconsistent with the given MPI_Datatype: "
		// + datatypeStr);
		// }
		return new Pair<>(basePtr, numPrimitives);
	}

	public static Pair<SymbolicExpression, NumericExpression> mpiBufAddition(
			State state, int pid, SymbolicExpression ptr,
			NumericExpression datatype, NumericExpression offset,
			SymbolicAnalyzer symbolicAnalyzer, SymbolicUtility symbolicUtil,
			Evaluator evaluator, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		String process = state.getProcessState(pid).name();
		Pair<SymbolicExpression, NumericExpression> mpiPtr_datatypeSize = processMPIPointer(
				state, pid, process, ptr, datatype, source, symbolicAnalyzer,
				symbolicUtil, evaluator);

		mpiPtr_datatypeSize.right = symbolicAnalyzer.getUniverse()
				.multiply(offset, mpiPtr_datatypeSize.right);
		return mpiPtr_datatypeSize;
	}

	// TODO: doc, used by evaluateValid
	public static Evaluation isDerefableMPIBuf(State state, int pid,
			Expression mpiBuf, SymbolicExpression mpiBufVal,
			SymbolicAnalyzer symbolicAnalyzer, SymbolicUtility symbolicUtil,
			Evaluator evaluator) throws UnsatisfiablePathConditionException {
		SymbolicExpression basePtrVal;
		NumericExpression countVal, datatypeVal;
		SymbolicUniverse universe = symbolicAnalyzer.getUniverse();
		Evaluation eval = new Evaluation(state, null);

		basePtrVal = universe.tupleRead(mpiBufVal, universe.intObject(0));
		countVal = (NumericExpression) universe.tupleRead(mpiBufVal,
				universe.intObject(1));
		datatypeVal = (NumericExpression) universe.tupleRead(mpiBufVal,
				universe.intObject(2));
		if (symbolicUtil.isNullPointer(basePtrVal)) {
			eval.value = universe.falseExpression();
			return eval;
		}

		String process = state.getProcessState(pid).name();
		Pair<SymbolicExpression, NumericExpression> mpiPtr_datatypeSize = processMPIPointer(
				state, pid, process, basePtrVal, datatypeVal,
				mpiBuf.getSource(), symbolicAnalyzer, symbolicUtil, evaluator);

		countVal = universe.multiply(countVal, mpiPtr_datatypeSize.right);
		countVal = universe.subtract(countVal, universe.integer(1));

		if (!symbolicUtil.getSymRef(mpiPtr_datatypeSize.left)
				.isArrayElementReference()) {
			eval.value = universe.equals(countVal, universe.zeroInt());
			return eval;
		}
		eval = evaluator.arrayElementReferenceAdd(state, pid,
				mpiPtr_datatypeSize.left, countVal, mpiBuf.getSource()).left;
		eval.value = symbolicAnalyzer.isDerefablePointer(state,
				eval.value).left;
		return eval;
	}

	// TODO: doc: used by evaluateEquals and evaluateMPIAgree becuase mpi seq
	// values can only be used to compare.
	// TODO: need better way to consistently handle MPI seq value wrapper.
	public static BooleanExpression assertMPISeqValueEqual(
			SymbolicExpression mpiSeqVal0, SymbolicExpression mpiSeqVal1,
			SymbolicUniverse universe) {
		NumericExpression count0 = getMPISeqCount(mpiSeqVal0),
				datatype0 = getMPISeqElementsize(mpiSeqVal0),
				count1 = getMPISeqCount(mpiSeqVal1),
				datatype1 = getMPISeqElementsize(mpiSeqVal1);
		BooleanExpression cond = universe.equals(
				universe.multiply(count0, datatype0),
				universe.multiply(count1, datatype1));

		return universe.and(universe.equals(getMPISeqData(mpiSeqVal0),
				getMPISeqData(mpiSeqVal1)), cond);
	}

	/**
	 * @param mpiSeqVal
	 *            a symbolic expression of dynamic mpi seq type. It must be of
	 *            the form of {@link #mpiSeqValueWrapperName}(data)
	 * @return the data in mpiSeqVal
	 */
	private static SymbolicExpression getMPISeqData(
			SymbolicExpression mpiSeqVal) {
		assert mpiSeqVal.operator() == SymbolicOperator.APPLY;
		SymbolicSequence<?> args = ((SymbolicSequence<?>) mpiSeqVal
				.argument(1));

		return args.get(0);
	}

	private static NumericExpression getMPISeqElementsize(
			SymbolicExpression mpiSeqVal) {
		assert mpiSeqVal.operator() == SymbolicOperator.APPLY;
		SymbolicSequence<?> args = ((SymbolicSequence<?>) mpiSeqVal
				.argument(1));

		return (NumericExpression) args.get(2);
	}

	private static NumericExpression getMPISeqCount(
			SymbolicExpression mpiSeqVal) {
		assert mpiSeqVal.operator() == SymbolicOperator.APPLY;
		SymbolicSequence<?> args = ((SymbolicSequence<?>) mpiSeqVal
				.argument(1));

		return (NumericExpression) args.get(1);
	}
}
