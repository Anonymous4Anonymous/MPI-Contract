package edu.udel.cis.vsl.civl.semantics.common;

import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.Identifier;
import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.civl.model.IF.expression.*;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression.ExpressionKind;
import edu.udel.cis.vsl.civl.model.IF.type.*;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * This class analyzes an expression "e" with a state "s" and a process "p" and
 * returns a symbolic expression of
 * {@link CIVLMemType#dynamicType(SymbolicUniverse)}, which represents the
 * precise set of memory locations that will be read during the evaluation of
 * the expression.
 * </p>
 * 
 * <p>
 * The basic analysis idea: for an expression <code>e</code>, recursively
 * collecting all the variables that <code>e</code> involves.
 * 
 * During recursion, a sub-expression <code>e'</code> refers to a memory
 * location iff <code>e'</code> is a {@link LHSExpression}.
 * 
 * 
 * Following rules must be inductively applied to {@link LHSExpression} during
 * the recursion: let "ban(e)" denote that the memory location referred by a
 * sub-expression "e" MUST NOT be saved otherwise the analysis is not precise.
 * <ul>
 * <li><code>*p</code>: the memory location referred by <code>*p</code> shall be
 * saved.</li>
 * <li><code>a[i]</code>: "ban(a)"; the memory location referred by
 * <code>a[i]</code> shall be saved, if not "ban(a[i])"</li>
 * <li><code>s.t</code>: "ban(s)"; and the memory location referred by
 * <code>s.t</code> should be saved, if not "ban(s.t)".</li>
 * <li><code>id</code>: trivial</li>
 * </ul>
 * </p>
 * 
 * @author xxxx
 *
 */
public class ReadSetAnalyzer {
	/**
	 * a reference to the {@link Evaluator}
	 */
	private Evaluator evaluator;

	/**
	 * a reference to the {@link SymbolicUniverse}
	 */
	private SymbolicUniverse universe;

	/**
	 * the dynamic constant scope value
	 */
	private final SymbolicExpression constantDyScopeVal;

	/* constructor */
	ReadSetAnalyzer(Evaluator evaluator) {
		this.evaluator = evaluator;
		this.universe = evaluator.universe();
		constantDyScopeVal = evaluator.modelFactory().typeFactory().scopeType()
				.scopeIdentityToValueOperator(universe)
				.apply(ModelConfiguration.DYNAMIC_CONSTANT_SCOPE);
	}

	/* the sole package interface */

	/**
	 * Analyze an expression with respect to a state and a process, returns a
	 * set of mem values, each of which represents a memory location subset of
	 * the precise memory location set that is read during an expression
	 * evaluation.
	 * 
	 * @param expr
	 *            an {@link Expression}
	 * @param state
	 *            a {@link State}
	 * @param pid
	 *            the PID of a process
	 * @param isPartOfLHS
	 *            true if the given expression is a part of LHS. If the given
	 *            expression is part of LHS, then for any LHSExpression that is
	 *            reached recursively by this method, the memory location
	 *            referred by the LHSExpression will not be saved. But other
	 *            memory locations that are read during evaluation will still be
	 *            saved.
	 * @return the set of subsets of the precise memory location set that is
	 *         read during evaluation
	 * @throws UnsatisfiablePathConditionException
	 */
	Set<SymbolicExpression> analyze(Expression expr, State state, int pid,
			boolean isPartOfLHS) {
		try {
			if (isPartOfLHS)
				return analyzeMemWorker(expr, state, pid, true);
			else if (expr instanceof LHSExpression)
				return analyzeMemForLHS((LHSExpression) expr, state, pid);
			else
				return analyzeMemWorker(expr, state, pid, false);
		} catch (UnsatisfiablePathConditionException e) {
			// if analysis runs into an UnsatisfiablePathConditionException, so
			// does the actual evaluation, hence the exception can be ignored
			// here:
			return new TreeSet<>(universe.comparator());
		}
	}

	/**
	 * <p>
	 * Worker method for {@link #analyze}. This method analyzes for
	 * {@link LHSExpression}s. The result will include two parts:
	 * <ol>
	 * <li>The memory location referred by the LHSExpression</li>
	 *
	 * <li>The rest of the read set analyzed from the LHSExpression</li>
	 * </ol>
	 * </p>
	 *
	 * <p>
	 * Note that the two parts shall not overlap. For example, for an expression
	 * <code>a[i]</code> where <code>a, i</code> are variables. The returned
	 * result is <code>&a[i], &i</code>. The <code>&a</code> is not needed since
	 * <code>&a[i]</code> is a more precise and <code>&a</code> overlaps with
	 * <code>&a[i]</code>.
	 * </p>
	 */
	private Set<SymbolicExpression> analyzeMemForLHS(LHSExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		Evaluation eval = evaluator.reference(state, pid, expr);
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		if (!isPointToConstantScope(eval.value)) {
			// the referred memory location:
			eval = evaluator.memEvaluator()
					.pointer2memValue(state, pid, eval.value, expr.getSource());
			result.add(eval.value);
		}
		// the rest of the read set:
		switch (expr.lhsExpressionKind()) {
			case DEREFERENCE : {
				DereferenceExpression derefExpr = (DereferenceExpression) expr;

				result.addAll(analyzeMemWorker(derefExpr.pointer(), state, pid,
						true));
				break;
			}
			case DOT : {
				DotExpression dotExpr = (DotExpression) expr;

				result.addAll(analyzeMemWorker(dotExpr.structOrUnion(), state,
						pid, true));
				break;
			}
			case SUBSCRIPT : {
				SubscriptExpression subsExpr = (SubscriptExpression) expr;

				result.addAll(
						analyzeMemWorker(subsExpr.index(), state, pid, false));
				result.addAll(
						analyzeMemWorker(subsExpr.array(), state, pid, true));
				break;
			}
			case VARIABLE :
			default :
				break;
		}
		return result;
	}

	/**
	 * <p>
	 * The general analysis method for collecting the precise memory location
	 * set that is read during the expression evaluation.
	 * </p>
	 *
	 * <p>
	 * The flag "partOfLHS" controls the algorithm:
	 * <ul>
	 * <li>If it is true, for any LHSExpression that is reached recursively by
	 * this method, the memory location referred by the LHSExpression will not
	 * be saved. But other memory locations that are read during evaluation will
	 * still be saved.</li>
	 *
	 * <li>If it is false, for a LHSExpression that is reached recursively by
	 * this method, both the memory location referred by the LHSExpression as
	 * well as the other memory locations that are read during evaluation will
	 * still be saved.</li>
	 * </ul>
	 * </p>
	 *
	 * @param expr
	 *            the expression that is analyzed
	 * @param state
	 *            the state
	 * @param pid
	 *            the PID of the process
	 * @param partOfLHS
	 *            whether the given expression is a sub-expression "sub" of a
	 *            LHSExpression "e" such that "sub" refers to a memory location
	 *            that contains the memory location referred by "e". For
	 *            example, "a" is the sub-expression of "a[i]" that refers to
	 *            the containing memory location.
	 * @return the set of subsets of the precisely analyzed memory location set
	 * @throws UnsatisfiablePathConditionException
	 */
	private Set<SymbolicExpression> analyzeMemWorker(Expression expr,
			State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		ExpressionKind kind = expr.expressionKind();
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		switch (kind) {
			case ABSTRACT_FUNCTION_CALL :
				result.addAll(analyzeAbstractFuncCall(
						(AbstractFunctionCallExpression) expr, state, pid));
				break;
			case ADDRESS_OF :
				result.addAll(analyzeAddressOf((AddressOfExpression) expr,
						state, pid));
				break;
			case ARRAY_LAMBDA :
				result.addAll(analyzeArrayLambda((ArrayLambdaExpression) expr,
						state, pid, partOfLHS));
				break;
			case ARRAY_LITERAL :
				result.addAll(analyzeArrayLiteral((ArrayLiteralExpression) expr,
						state, pid, partOfLHS));
				break;
			case BINARY :
				result.addAll(analyzeBinary((BinaryExpression) expr, state, pid,
						partOfLHS));
				break;
			case CAST :
				result.addAll(analyzeCast((CastExpression) expr, state, pid,
						partOfLHS));
				break;
			case COND :
				result.addAll(analyzeCond((ConditionalExpression) expr, state,
						pid, partOfLHS));
				break;
			case DEREFERENCE :
				result.addAll(analyzeDeref((DereferenceExpression) expr, state,
						pid, partOfLHS));
				break;
			case DOT :
				result.addAll(analyzeDot((DotExpression) expr, state, pid,
						partOfLHS));
				break;
			case DYNAMIC_TYPE_OF :
				result.addAll(analyzeDyTypeOf((DynamicTypeOfExpression) expr,
						state, pid));
				break;
			case EXTENDED_QUANTIFIER :
				result.addAll(analyzeExtQuantifier(
						(ExtendedQuantifiedExpression) expr, state, pid));
				break;
			case FUNCTION_GUARD :
				result.addAll(analyzeFuncGuard((FunctionGuardExpression) expr,
						state, pid));
				break;
			case FUNC_CALL :
				result.addAll(analyzeFuncCall((FunctionCallExpression) expr,
						state, pid));
				break;
			case INITIAL_VALUE :
				result.addAll(analyzeInitVal((InitialValueExpression) expr,
						state, pid));
				break;
			case LAMBDA :
				result.addAll(
						analyzeLambda((LambdaExpression) expr, state, pid));
				break;
			case REC_DOMAIN_LITERAL :
				result.addAll(analyzeRecDomLit(
						(RecDomainLiteralExpression) expr, state, pid));
				break;
			case REGULAR_RANGE :
				result.addAll(analyzeRange((RegularRangeExpression) expr, state,
						pid));
				break;
			case SCOPEOF :
				result.addAll(
						analyzeScopeof((ScopeofExpression) expr, state, pid));
				break;
			case SIZEOF_EXPRESSION :
				result.addAll(
						analyzeSizeof((SizeofExpression) expr, state, pid));
				break;
			case SIZEOF_TYPE :
				result.addAll(analyzeSizeofType((SizeofTypeExpression) expr,
						state, pid));
				break;
			case SUBSCRIPT :
				result.addAll(analyzeSubscript((SubscriptExpression) expr,
						state, pid, partOfLHS));
				break;
			case UNARY :
				result.addAll(analyzeUnaryExpression((UnaryExpression) expr,
						state, pid, partOfLHS));
				break;
			case VALUE_AT :
				result.addAll(analyzeValueAt((ValueAtExpression) expr, state,
						pid, partOfLHS));
				break;
			case VARIABLE :
				if (!partOfLHS)
					result.addAll(analyzeVariable((VariableExpression) expr,
							state, pid));
				break;
			/* Ignor-able kinds section */
			case BOOLEAN_LITERAL :
			case BOUND_VARIABLE :
			case CHAR_LITERAL :
			case FUNCTION_IDENTIFIER :
			case HERE_OR_ROOT :
			case INTEGER_LITERAL :
			case NOTHING :
			case NULL_LITERAL :
			case PROC_NULL :
			case QUANTIFIER :
			case REAL_LITERAL :
			case RESULT :
			case SELF :
			case STATE_NULL :
			case STRING_LITERAL :
			case UNDEFINED_PROC :
			case WILDCARD :
			case SYSTEM_GUARD :
				break;
			/* shall not happen section */
			case STRUCT_OR_UNION_LITERAL :
			case MEMORY_UNIT :
			case MPI_CONTRACT_EXPRESSION :
				/* I don't know if ignor-able or not kinds section */
			case DERIVATIVE :
			case DIFFERENTIABLE :
			case DOMAIN_GUARD :// what is this ?
			case NON_DET_FUNC :// what is this ?
				throw new CIVLUnimplementedFeatureException(
						"dynamic analysis of read set during evaluation of "
								+ "expression of " + kind + " kind");
			default :
				throw new CIVLInternalException(
						"unknown expression kind " + kind, expr.getSource());
		}
		return result;
	}

	/**
	 * Analyze expressions in types
	 *
	 */
	private Set<SymbolicExpression> analyzeType(CIVLType type, State state,
			int pid, Set<Identifier> seenStructOrUnions)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		switch (type.typeKind()) {
			case ARRAY :
			case COMPLETE_ARRAY : {
				CIVLArrayType arrType = (CIVLArrayType) type;

				if (arrType.isComplete())
					result.addAll(
							analyze(((CIVLCompleteArrayType) arrType).extent(),
									state, pid, false));
				result.addAll(analyzeType(arrType.elementType(), state, pid,
						seenStructOrUnions));
				break;
			}
			case POINTER : {
				CIVLPointerType ptrType = (CIVLPointerType) type;

				result.addAll(analyzeType(ptrType.baseType(), state, pid,
						seenStructOrUnions));
				break;
			}
			case STRUCT_OR_UNION : {
				CIVLStructOrUnionType structOrUnionType =
						(CIVLStructOrUnionType) type;

				if (seenStructOrUnions.contains(structOrUnionType.name()))
					return result;
				seenStructOrUnions.add(structOrUnionType.name());

				if (seenStructOrUnions.contains(structOrUnionType.name()))
					return result;
				seenStructOrUnions.add(structOrUnionType.name());
				for (StructOrUnionField field : structOrUnionType.fields())
					result.addAll(analyzeType(field.type(), state, pid,
							seenStructOrUnions));
				break;
			}
			case BUNDLE :
			case DOMAIN :
			case ENUM :
			case PRIMITIVE :
			case FUNCTION :
			case HEAP :
			case MEM :
			case SET :
				break;
			default :
				throw new CIVLUnimplementedFeatureException(
						"dynamic analysis of read set during evaluation of "
								+ "expression in " + type);

		}
		return result;
	}

	/* *********** Induction on different expression kinds **************/

	private Set<SymbolicExpression> analyzeVariable(VariableExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		Evaluation eval = evaluator.reference(state, pid, expr);
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		if (!isPointToConstantScope(eval.value)) {
			eval = evaluator.memEvaluator()
					.pointer2memValue(state, pid, eval.value, expr.getSource());
			result.add(eval.value);
		}
		return result;
	}

	private Set<SymbolicExpression> analyzeValueAt(ValueAtExpression expr,
			State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(expr.expression(),
				state, pid, partOfLHS);

		result.addAll(analyzeMemWorker(expr.pid(), state, pid, partOfLHS));
		result.addAll(analyzeMemWorker(expr.state(), state, pid, partOfLHS));
		return result;
	}

	private Set<SymbolicExpression> analyzeSubscript(SubscriptExpression expr,
			State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(expr.index(), state,
				pid, false);

		if (!partOfLHS) {
			Evaluation eval = evaluator.reference(state, pid, expr);

			if (!isPointToConstantScope(eval.value)) {
				eval = evaluator.memEvaluator()
						.pointer2memValue(state, pid, eval.value,
								expr.getSource());
				result.add(eval.value);
			}
		}
		result.addAll(analyzeMemWorker(expr.array(), state, pid, true));
		return result;
	}

	private Set<SymbolicExpression> analyzeSizeofType(SizeofTypeExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		return analyzeType(expr.getTypeArgument(), state, pid, new HashSet<>());
	}

	private Set<SymbolicExpression> analyzeSizeof(SizeofExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		return analyzeMemWorker(expr.getArgument(), state, pid, false);
	}

	private Set<SymbolicExpression> analyzeScopeof(ScopeofExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		return analyzeMemWorker(expr.argument(), state, pid, false);
	}

	private Set<SymbolicExpression> analyzeRange(RegularRangeExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(expr.getLow(), state,
				pid, false);

		result.addAll(analyzeMemWorker(expr.getHigh(), state, pid, false));
		if (expr.getStep() != null)
			result.addAll(analyzeMemWorker(expr.getStep(), state, pid, false));
		return result;
	}

	private Set<SymbolicExpression> analyzeRecDomLit(
			RecDomainLiteralExpression expr, State state, int pid)
			throws UnsatisfiablePathConditionException {
		int dims = expr.dimension();
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		for (int i = 0; i < dims; i++)
			result.addAll(analyzeMemWorker(expr.rangeAt(i), state, pid, false));
		return result;
	}

	private Set<SymbolicExpression> analyzeInitVal(InitialValueExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		return analyzeType(expr.getExpressionType(), state, pid,
				new HashSet<>());
	}

	private Set<SymbolicExpression> analyzeLambda(LambdaExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		return analyzeMemWorker(expr.lambdaFunction(), state, pid, false);
	}

	private Set<SymbolicExpression> analyzeFuncCall(FunctionCallExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		for (Expression arg : expr.callStatement().arguments())
			result.addAll(analyzeMemWorker(arg, state, pid, false));
		return result;
	}

	private Set<SymbolicExpression> analyzeFuncGuard(
			FunctionGuardExpression expr, State state, int pid)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(
				expr.functionExpression(), state, pid, false);

		for (Expression arg : expr.arguments())
			result.addAll(analyzeMemWorker(arg, state, pid, false));
		return result;
	}

	private Set<SymbolicExpression> analyzeExtQuantifier(
			ExtendedQuantifiedExpression expr, State state, int pid)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(expr.lower(), state,
				pid, false);

		result.addAll(analyzeMemWorker(expr.higher(), state, pid, false));
		result.addAll(analyzeMemWorker(expr.function(), state, pid, false));
		return result;
	}

	private Set<SymbolicExpression> analyzeDyTypeOf(
			DynamicTypeOfExpression expr, State state, int pid)
			throws UnsatisfiablePathConditionException {
		return analyzeType(expr.getType(), state, pid, new HashSet<>());
	}

	private Set<SymbolicExpression> analyzeDot(DotExpression expr, State state,
			int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(expr.structOrUnion(),
				state, pid, true);

		if (!partOfLHS) {
			Evaluation eval = evaluator.reference(state, pid, expr);

			if (!isPointToConstantScope(eval.value)) {
				eval = evaluator.memEvaluator()
						.pointer2memValue(state, pid, eval.value, expr.getSource());
				result.add(eval.value);
			}
		}
		return result;
	}

	private Set<SymbolicExpression> analyzeDeref(DereferenceExpression expr,
			State state, int pid, boolean isPartOfLHS)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(expr.pointer(), state,
				pid, false);

		if (!isPartOfLHS) {
			Evaluation eval = evaluator.reference(state, pid, expr);

			if (!isPointToConstantScope(eval.value)) {
				eval = evaluator.memEvaluator()
						.pointer2memValue(state, pid, eval.value, expr.getSource());
				result.add(eval.value);
			}
		}
		return result;
	}

	private Set<SymbolicExpression> analyzeCond(ConditionalExpression expr,
			State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		Evaluation eva = evaluator.evaluate(state, pid, expr.getCondition());
		BooleanExpression conEval = (BooleanExpression) eva.value;
		Set<SymbolicExpression> result = analyzeMemWorker(expr.getCondition(),
				state, pid, partOfLHS);

		if (!conEval.isFalse())
			result.addAll(analyzeMemWorker(expr.getTrueBranch(), state, pid,
					partOfLHS));
		if (!conEval.isTrue())
			result.addAll(analyzeMemWorker(expr.getFalseBranch(), state, pid,
					partOfLHS));
		return result;
	}

	private Set<SymbolicExpression> analyzeCast(CastExpression expr,
			State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		CIVLType type = expr.getCastType();
		Set<SymbolicExpression> result = analyzeType(type, state, pid,
				new HashSet<>());

		result.addAll(
				analyzeMemWorker(expr.getExpression(), state, pid, partOfLHS));
		return result;
	}

	private Set<SymbolicExpression> analyzeArrayLiteral(
			ArrayLiteralExpression expr, State state, int pid,
			boolean partOfLHS) throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());

		for (Expression ele : expr.elements())
			result.addAll(analyzeMemWorker(ele, state, pid, partOfLHS));
		return result;
	}

	private Set<SymbolicExpression> analyzeArrayLambda(
			ArrayLambdaExpression expr, State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		CIVLCompleteArrayType arrType = expr.getExpressionType();
		Set<SymbolicExpression> result = analyzeType(arrType, state, pid,
				new HashSet<>());

		result.addAll(
				analyzeMemWorker(expr.expression(), state, pid, partOfLHS));
		return result;
	}

	private Set<SymbolicExpression> analyzeAddressOf(AddressOfExpression expr,
			State state, int pid) throws UnsatisfiablePathConditionException {
		return analyzeMemWorker(expr.operand(), state, pid, false);
	}

	private Set<SymbolicExpression> analyzeAbstractFuncCall(
			AbstractFunctionCallExpression expr, State state, int pid)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = new TreeSet<>(universe.comparator());
		for (Expression arg : expr.arguments())
			result.addAll(analyzeMemWorker(arg, state, pid, false));
		return result;
	}

	private Set<SymbolicExpression> analyzeBinary(BinaryExpression expr,
			State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> result = analyzeMemWorker(expr.left(), state,
				pid, partOfLHS);

		result.addAll(analyzeMemWorker(expr.right(), state, pid, partOfLHS));
		return result;
	}

	private Set<SymbolicExpression> analyzeUnaryExpression(UnaryExpression expr,
			State state, int pid, boolean partOfLHS)
			throws UnsatisfiablePathConditionException {
		return this.analyzeMemWorker(expr.operand(), state, pid, partOfLHS);
	}

	/**
	 * It's kind confusing that why DYNAMIC_CONSTANT_SCOPE is -1.  To make sure
	 * the mem value contains no negative scope value, here has to ignore such
	 * reference.
	 */
	private boolean isPointToConstantScope(SymbolicExpression pointer) {
		SymbolicExpression scopeVal =
				evaluator.symbolicUtility().getScopeValue(pointer);

		return scopeVal.equals(constantDyScopeVal);
	}
}
