package edu.udel.cis.vsl.civl.semantics.common;

import static edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference.VSReferenceKind.IDENTITY;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.ErrorKind;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.CIVLTypeFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.AddressOfExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.BinaryExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.CastExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DereferenceExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DotExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression.LHSExpressionKind;
import edu.udel.cis.vsl.civl.model.IF.expression.SubscriptExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLMemType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLMemType.MemoryLocationReference;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLSetType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.state.IF.DynamicScope;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.Triple;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.ArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NTReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.OffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.expr.TupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.UnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.NTValueSetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArraySectionReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSOffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSTupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSUnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference.VSReferenceKind;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUnionType;

/**
 * 
 * <p>
 * This class is an implementation of {@link Evaluator} which evaluates
 * expressions that may have $mem type.
 * </p>
 * 
 * <p>
 * This class provides a method (
 * {@link #evaluateMemCastingExpression(State, int, Expression)}) to evaluate
 * expressions of (set-of) pointer type that will be converted to $mem type
 * objects. The evaluation result of such an expression will always be of
 * {@link SymbolicUniverse#valueSetTemplateType()}. See
 * {@link #evaluateMemCastingExpression(State, int, Expression)}.
 * </p>
 * 
 * <p>
 * This method requires that the converting expressions of (set-of) pointer type
 * satisfy the restrictions given by:
 * {@link edu.udel.cis.vsl.abc.ast.conversion.common.MemConversionRestriction},
 * so that this evaluator only needs to consider a limited number of cases.
 * </p>
 * 
 * <p>
 * To evaluate an expression <code>e</code> that will be converted to be of $mem
 * type and satisfies the restriction above, this class extends
 * {@link CommonEvaluator} with the following evaluation rules:
 * <ul>
 * <li>evaluates sub-expression of the form <code>P + I</code>: if I has set
 * type or P has pointer type, the result will be of
 * {@link #valueSetPointerType};otherwise, evaluate the expression as usual</li>
 * 
 * <li>evaluate sub-expression of the form <code>&LHS</code>: if
 * <code>&LHS</code> has set type, the result will be of
 * {@link #valueSetPointerType}; otherwise evaluate the expression as usual</li>
 * 
 * <li>if the value of <code>e</code> will have value set template type</li>
 * </ul>
 * </p>
 * 
 * <p>
 * In addition, this class provides PUBLIC methods for creating values of
 * dynamic $mem type:
 * <ul>
 * <li>{@link #pointer2memValue(State, int, SymbolicExpression, CIVLSource)}
 * </li>
 * <li>
 * {@link #makeMemValue(State, int, SymbolicExpression, SymbolicExpression, CIVLSource)}
 * </li>
 * </ul>
 * </p>
 * 
 * @author xxxx
 *
 */
public class MemEvaluator extends CommonEvaluator {

	/**
	 * <p>
	 * A mirror type of {@link CIVLTypeFactory#pointerSymbolicType()}, in which
	 * {@link ValueSetReference} is used to replace {@link ReferenceExpression}.
	 * </p>
	 * 
	 * <p>
	 * This type will only be used intermediately within this class to represent
	 * the value of sub-expressions of set-of pointer type.
	 * </p>
	 * 
	 */
	private SymbolicTupleType valueSetPointerType;

	public MemEvaluator(ModelFactory modelFactory, StateFactory stateFactory,
			LibraryEvaluatorLoader loader, LibraryExecutorLoader loaderExec,
			SymbolicUtility symbolicUtil, SymbolicAnalyzer symbolicAnalyzer,
			MemoryUnitFactory memUnitFactory, CIVLErrorLogger errorLogger,
			CIVLConfiguration config) {
		super(modelFactory, stateFactory, loader, loaderExec, symbolicUtil,
				symbolicAnalyzer, memUnitFactory, errorLogger, config);

		SymbolicType pointerType = typeFactory.pointerSymbolicType();

		// use assertions to make sure that if symbolic pointer type changes,
		// this mirror type shall be changed as well:
		assert pointerType
				.typeKind() == SymbolicTypeKind.TUPLE : "unexpected symbolic pointer type";
		assert ((SymbolicTupleType) pointerType).sequence().numTypes() == 3
				&& ((SymbolicTupleType) pointerType).sequence()
						.getType(2) == universe
								.referenceType() : "unexpected symbolic pointer type";

		SymbolicTupleType castedPointerType = (SymbolicTupleType) pointerType;

		this.valueSetPointerType = universe.tupleType(
				universe.stringObject("vsPointer"),
				Arrays.asList(castedPointerType.sequence().getType(0),
						castedPointerType.sequence().getType(1),
						universe.valueSetReferenceType()));
	}

	/* ***************** package method: interfaces **************** */
	/**
	 * <p>
	 * Evaluates an expression of (set-of) pointer type to a value of
	 * {@link CIVLTypeFactory#dynamicMemType()}.
	 * </p>
	 * 
	 * <p>
	 * This method shall only be used to evaluate a {@link CastExpression} which
	 * casts an expression to be of $mem type.
	 * </p>
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the running process
	 * @param expr
	 *            an expression that will be casted to be of $mem type
	 * @return
	 * @throws UnsatisfiablePathConditionException
	 */
	Evaluation evaluateMemCastingExpression(State state, int pid,
			Expression expr) throws UnsatisfiablePathConditionException {
		Evaluation eval = evaluate(state, pid, expr);
		ValueSetReference vsref;

		if (!expr.getExpressionType().isSetType()) {
			if (!symbolicUtil.isConcretePointer(eval.value))
				errorLogger.logSimpleError(expr.getSource(), state,
						state.getProcessState(pid).name(),
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.POINTER,
						"Cannot convert a non-concrete pointer value to a value of $mem type.\nPointer: "
								+ expr + "\nPointer value: " + eval.value);

			ReferenceExpression ref = (ReferenceExpression) getSymRef(
					eval.value);

			vsref = convertToValueSetReference(ref);
		} else {
			assert eval.value.type() == valueSetPointerType;
			vsref = (ValueSetReference) universe.tupleRead(eval.value,
					universe.intObject(2));
		}
		int vid = symbolicUtil.getVariableId(expr.getSource(), eval.value);
		int sid = stateFactory
				.getDyscopeId(symbolicUtil.getScopeValue(eval.value));

		return makeValueOfMemType(state, pid, vid, sid, vsref,
				expr.getSource());
	}

	/**
	 * <p>
	 * Convert a symbolic expression of pointer type to a symbolic expression of
	 * {@link CIVLMemType#dynamicType(SymbolicUniverse)}. The converted mem
	 * value contains a reference to the same object as is referred by the
	 * pointer.
	 * </p>
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the running process
	 * @param pointer
	 *            a concrete pointer value (see
	 *            {@link SymbolicUtility#isConcretePointer(SymbolicExpression)})
	 * @param source
	 *            the {@link CIVLSource} related to this method call
	 * @return an evaluation includes a symbolic expression of dynamic $mem type
	 * @throws UnsatisfiablePathConditionException
	 */
	public Evaluation pointer2memValue(State state, int pid,
			SymbolicExpression pointer, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		assert symbolicUtil.isConcretePointer(
				pointer) : "attempt to save a memory location, which is referred "
						+ "by a non-concrete pointer: " + pointer
						+ ", to write set";
		int vid = symbolicUtil.getVariableId(null, pointer);
		SymbolicExpression scope = symbolicUtil.getScopeValue(pointer);
		ValueSetReference ref = convertToValueSetReference(getSymRef(pointer));
		int sid = stateFactory.getDyscopeId(scope);

		return makeValueOfMemType(state, pid, vid, sid, ref, source);
	}

	/**
	 * <p>
	 * This is the general method for creating a symbolic expression of dynamic
	 * $mem type.
	 * </p>
	 * <p>
	 * Given a pointer to a variable or a memory heap object "obj" (see
	 * {@link SymbolicUtility#getPointer2MemoryBlock(SymbolicExpression)} for
	 * more information about "pointer to memory heap object") and a value set
	 * template "t", returns an evaluation including a value of dynamic $mem
	 * type which refers to a specific region corresponding to "t" in the "obj".
	 * </p>
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the running process
	 * @param pointer
	 *            a pointer to a variable or a memory heap object
	 * @param valueSetTemplate
	 *            a symbolic expression of
	 *            {@link SymbolicUniverse#valueSetTemplateType()}
	 * @param source
	 *            the {@link CIVLSource} related to this method call
	 * @return an evaluation containing the resulting value of dynamic $mem type
	 * @throws UnsatisfiablePathConditionException
	 */
	public Evaluation makeMemValue(State state, int pid,
			SymbolicExpression pointer, SymbolicExpression valueSetTemplate,
			CIVLSource source) throws UnsatisfiablePathConditionException {
		Evaluation eval = pointer2memValue(state, pid, pointer, source);
		Iterable<MemoryLocationReference> memRefs = typeFactory.civlMemType()
				.memValueIterator().apply(eval.value);
		List<SymbolicExpression[]> memValueComponents = new LinkedList<>();

		for (MemoryLocationReference memRef : memRefs) {
			SymbolicExpression vID, heapID, mallocID;

			vID = universe.integer(memRef.vid());
			heapID = universe.integer(memRef.heapID());
			mallocID = universe.integer(memRef.mallocID());
			memValueComponents.add(new SymbolicExpression[]{vID, heapID,
					mallocID, memRef.scopeValue(), valueSetTemplate});
		}
		eval.value = typeFactory.civlMemType().memValueCreator(universe)
				.apply(memValueComponents);
		return eval;
	}

	/* ***************** override methods of super class ******************* */
	/**
	 *
	 * Evaluates the result of an PLUS operation with two numeric or range type
	 * operands
	 *
	 */
	@Override
	protected SymbolicExpression evaluatePlus(SymbolicExpression left,
			SymbolicExpression right) {
		SymbolicType rangeType = typeFactory.rangeType()
				.getDynamicType(universe);

		if (left.type() == rangeType || right.type() == rangeType) {
			NumericExpression range0[] = new NumericExpression[3];
			NumericExpression range1[] = new NumericExpression[3];

			if (left.type() == rangeType) {
				range0[0] = symbolicUtil.getLowOfRegularRange(left);
				range0[1] = symbolicUtil.getHighOfRegularRange(left);
				range0[2] = symbolicUtil.getStepOfRegularRange(left);
			} else {
				range0[0] = (NumericExpression) left;
				range0[1] = (NumericExpression) left;
				range0[2] = one;
			}
			if (right.type() == rangeType) {
				range1[0] = symbolicUtil.getLowOfRegularRange(right);
				range1[1] = symbolicUtil.getHighOfRegularRange(right);
				range1[2] = symbolicUtil.getStepOfRegularRange(right);
			} else {
				range1[0] = (NumericExpression) right;
				range1[1] = (NumericExpression) right;
				range1[2] = one;
			}
			if (!range0[2].isOne() || !range1[2].isOne())
				throw new CIVLUnimplementedFeatureException(
						"Ranges addition with non-trivial steps:\n" + " left + "
								+ right);
			range0[0] = universe.add(range0[0], range1[0]);
			range0[1] = universe.add(range0[1], range1[1]);
			return universe.tuple((SymbolicTupleType) rangeType,
					Arrays.asList(range0[0], range0[1], range0[2]));
		}
		return universe.add((NumericExpression) left,
				(NumericExpression) right);
	}

	@Override
	public Evaluation evaluatePointerAdd(State state, int pid,
			BinaryExpression expr, SymbolicExpression ptrVal,
			SymbolicExpression oftVal)
			throws UnsatisfiablePathConditionException {
		if (ptrVal.type() == typeFactory.pointerSymbolicType())
			if (!symbolicUtil.isConcretePointer(ptrVal)) {
				errorLogger.logSimpleError(expr.getSource(), state,
						state.getProcessState(pid).name(),
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.POINTER,
						"Cannot perform pointer addition on a non-concrete pointer value.\nPointer: "
								+ expr.left() + "\nPointer value: " + ptrVal);
				throw new UnsatisfiablePathConditionException();
			}

		if (ptrVal.type() == valueSetPointerType
				|| oftVal.type() != universe.integerType()) {
			return valueSetPointerAdd(state, pid, expr, ptrVal, oftVal,
					expr.getSource());
		} else
			return super.evaluatePointerAdd(state, pid, expr, ptrVal, oftVal);
	}

	@Override
	protected Evaluation evaluateAddressOf(State state, int pid,
			AddressOfExpression expr)
			throws UnsatisfiablePathConditionException {
		if (expr.getExpressionType().isSetType())
			return memReference(state, pid, expr.operand());
		else
			return reference(state, pid, expr.operand());
	}

	/**
	 * pretty printing value of $mem type
	 */
	static String prettyPrintMemValue(CIVLTypeFactory typeFactory,
			SymbolicUniverse universe, State state, SymbolicExpression memValue,
			CIVLSource source) {
		CIVLMemType memType = typeFactory.civlMemType();
		Function<SymbolicExpression, IntegerNumber> scopeValToInt = typeFactory
				.scopeType().scopeValueToIdentityOperator(universe);
		String result = "{";

		for (CIVLMemType.MemoryLocationReference mlr : memType
				.memValueIterator().apply(memValue)) {
			int dyscopeId = scopeValToInt.apply(mlr.scopeValue()).intValue();

			if (dyscopeId < 0) // if memory location destroyed:
				continue;

			DynamicScope dyscope = state.getDyscope(dyscopeId);
			String obj;

			if (mlr.vid() > 0)
				obj = dyscope.lexicalScope().variable(mlr.vid()).name().name();
			else
				obj = "Dyscope" + dyscopeId + "_malloc_" + mlr.mallocID();
			for (String ref : prettyPrintValueSetTemplate(universe,
					mlr.valueSetTemplate(), source))
				result += obj + ref + ", ";
		}
		if (result.length() > 1)
			result = result.substring(0, result.length() - 2); // remove extra
																// ", "
		result += "}";
		return result;
	}

	/* ******** private methods deal with set type expressions ********* */
	/**
	 * Evaluates a reference to a {@link LHSExpression} which may have
	 * {@link CIVLSetType}.
	 */
	private Evaluation memReference(State state, int pid, LHSExpression operand)
			throws UnsatisfiablePathConditionException {
		if (operand.lhsExpressionKind() == LHSExpressionKind.DEREFERENCE)
			// eval(&(*P) = eval(P):
			return evaluate(state, pid,
					((DereferenceExpression) operand).pointer());

		Triple<Evaluation, Integer, SymbolicExpression> result = memReferenceWorker(
				state, pid, operand);
		int vid = result.second;
		SymbolicExpression scopeValue = result.third;

		result.first.value = valueSetPointer(vid, scopeValue,
				(ValueSetReference) result.first.value);
		return result.first;
	}

	/**
	 * 
	 * Worker method of {@link #memReference(State, LHSExpression)}.
	 * 
	 * @return a triple: an {@link Evaluation} of the {@link ValueSetReference}
	 *         that refers to the given left-hand side expression, a variable ID
	 *         and a scope value.
	 */
	private Triple<Evaluation, Integer, SymbolicExpression> memReferenceWorker(
			State state, int pid, LHSExpression operand)
			throws UnsatisfiablePathConditionException {
		Evaluation eval;
		Triple<Evaluation, Integer, SymbolicExpression> result;

		switch (operand.lhsExpressionKind()) {
			case DEREFERENCE : {
				int vid;
				SymbolicExpression scopeValue;
				// guaranteed that dereference can only be applied to a single
				// pointer:
				eval = evaluate(state, pid,
						((DereferenceExpression) operand).pointer());
				// convert ReferenceExpression to ValueSetReference:
				vid = symbolicUtil.getVariableId(
						((DereferenceExpression) operand).pointer().getSource(),
						eval.value);
				scopeValue = symbolicUtil.getScopeValue(eval.value);
				eval.value = convertToValueSetReference(getSymRef(eval.value));
				result = new Triple<>(eval, vid, scopeValue);
				break;
			}
			case DOT :
				result = memReferenceWorkerDot(state, pid, operand);
				break;
			case SUBSCRIPT :
				result = memReferenceWorkerForSubscript(state, pid, operand);
				break;
			case VARIABLE : {
				Variable variable = ((VariableExpression) operand).variable();
				int sid = state.getDyscopeID(pid, variable);
				int vid = variable.vid();

				eval = new Evaluation(state, universe.vsIdentityReference());
				result = new Triple<>(eval, vid, stateFactory.scopeValue(sid));
				break;
			}
			default :
				throw new CIVLInternalException("Unknown kind of LHSExpression",
						operand);
		}
		return result;
	}

	private Triple<Evaluation, Integer, SymbolicExpression> memReferenceWorkerForSubscript(
			State state, int pid, LHSExpression operand)
			throws UnsatisfiablePathConditionException {
		LHSExpression arrayExpr = ((SubscriptExpression) operand).array();
		Expression indexExpr = ((SubscriptExpression) operand).index();
		Triple<Evaluation, Integer, SymbolicExpression> result = memReferenceWorker(
				state, pid, arrayExpr);
		Evaluation eval = result.first;
		ValueSetReference arrayRef = (ValueSetReference) eval.value;
		ValueSetReference newRef;
		SymbolicExpression index;

		eval = evaluate(eval.state, pid, indexExpr);
		state = eval.state;
		index = eval.value;
		if (index.type() == typeFactory.rangeType().getDynamicType(universe)) {
			NumericExpression lower, upper, step;

			lower = symbolicUtil.getLowOfRegularRange(index);
			upper = symbolicUtil.getHighOfRegularRange(index);
			step = symbolicUtil.getStepOfRegularRange(index);
			newRef = universe.vsArraySectionReference(arrayRef, lower,
					universe.add(upper, one), step);
		} else
			newRef = universe.vsArrayElementReference(arrayRef,
					(NumericExpression) index);
		eval.value = newRef;
		result.first = eval;
		return result;
	}

	private Triple<Evaluation, Integer, SymbolicExpression> memReferenceWorkerDot(
			State state, int pid, LHSExpression operand)
			throws UnsatisfiablePathConditionException {
		DotExpression dot = (DotExpression) operand;
		int index = dot.fieldIndex();
		Triple<Evaluation, Integer, SymbolicExpression> result = memReferenceWorker(
				state, pid, (LHSExpression) dot.structOrUnion());
		Evaluation eval = result.first;
		ValueSetReference oldRef = (ValueSetReference) eval.value;
		ValueSetReference newRef;

		if (dot.isStruct())
			newRef = universe.vsTupleComponentReference(oldRef,
					universe.intObject(index));
		else
			newRef = universe.vsUnionMemberReference(oldRef,
					universe.intObject(index));
		eval.value = newRef;
		result.first = eval;
		return result;
	}

	/**
	 * Converts a {@link ReferenceExpression} to a {@link ValueSetReference}
	 * 
	 * @param ref
	 *            a {@link ReferenceExpression}
	 * @return a {@link ValueSetReference} which is converted from the given
	 *         "ref"
	 */
	private ValueSetReference convertToValueSetReference(
			SymbolicExpression ref) {
		if (ref.type() == universe.valueSetReferenceType())
			return (ValueSetReference) ref;
		ReferenceExpression theRef = (ReferenceExpression) ref;
		assert !theRef.isNullReference();
		if (theRef.isIdentityReference())
			return universe.vsIdentityReference();
		else {
			ValueSetReference parent = convertToValueSetReference(
					((NTReferenceExpression) theRef).getParent());

			switch (theRef.referenceKind()) {
				case ARRAY_ELEMENT :
					return universe.vsArrayElementReference(parent,
							((ArrayElementReference) theRef).getIndex());
				case OFFSET :
					return universe.vsOffsetReference(parent,
							((OffsetReference) theRef).getOffset());
				case TUPLE_COMPONENT :
					return universe.vsTupleComponentReference(parent,
							((TupleComponentReference) theRef).getIndex());
				case UNION_MEMBER :
					return universe.vsUnionMemberReference(parent,
							((UnionMemberReference) theRef).getIndex());
				case NULL :
				case IDENTITY :
				default :
					throw new CIVLInternalException("unreachable",
							(CIVLSource) null);
			}
		}
	}

	/**
	 * <p>
	 * Adds an integer or regular range to either a concrete pointer or a
	 * {@link #valueSetPointer(int, SymbolicExpression, ValueSetReference)}.
	 * Returns a
	 * {@link #valueSetPointer(int, SymbolicExpression, ValueSetReference)} as
	 * the result of the addition.
	 * </p>
	 * 
	 * <p>
	 * Note that it requires either the "valueSetPointer" has an
	 * {@link VSArraySectionReference} or the offset is a regular range.
	 * </p>
	 * 
	 * <p>
	 * See {@link SymbolicUtility#isConcretePointer(SymbolicExpression)} for the
	 * definition of "concrete pointer".
	 * </p>
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the running process
	 * @param expr
	 *            the binary expression representing a (set-of) pointer(s) plus
	 *            a (set-of) integer(s)
	 * @param ptrVal
	 *            either a concrete pointer or a
	 *            {@link #valueSetPointer(int, SymbolicExpression, ValueSetReference)}
	 * @param oftVal
	 *            an integer or regular range value representing a set of
	 *            offsets
	 * @returns {@link #valueSetPointer(int, SymbolicExpression, ValueSetReference)}
	 *          representing the result of the expression
	 * @throws UnsatisfiablePathConditionException
	 */
	private Evaluation valueSetPointerAdd(State state, int pid,
			BinaryExpression expr, SymbolicExpression ptrVal,
			SymbolicExpression oftVal, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		assert oftVal.type() == universe.integerType() || oftVal
				.type() == typeFactory.rangeType().getDynamicType(universe);
		boolean isRange = oftVal.type() != universe.integerType();
		NumericExpression range[] = new NumericExpression[3];
		SymbolicExpression parent = null;

		if (isRange) {
			range[0] = symbolicUtil.getLowOfRegularRange(oftVal);
			range[1] = symbolicUtil.getHighOfRegularRange(oftVal);
			range[2] = symbolicUtil.getStepOfRegularRange(oftVal);
		} else
			range[0] = (NumericExpression) oftVal;
		if (ptrVal.type() == typeFactory.pointerSymbolicType()) {
			/*
			 * Concrete pointer + Regular Range:
			 */
			ReferenceExpression ref = (ReferenceExpression) getSymRef(ptrVal);

			assert isRange;
			if (ref.isArrayElementReference()) {
				NumericExpression index = ((ArrayElementReference) ref)
						.getIndex();

				parent = ((NTReferenceExpression) ref).getParent();
				range[0] = universe.add(index, range[0]);
				range[1] = universe.add(index, range[1]);
				range[1] = universe.add(range[1], one); // to be exclusive
			} else {
				errorLogger.logSimpleError(source, state,
						state.getProcessState(pid).name(),
						symbolicAnalyzer.stateInformation(state),
						ErrorKind.OTHER,
						"Invalid pointer value for pointer addition:s\n"
								+ "Pointer: " + expr.left() + "\nOffsets: "
								+ expr.right() + "\nPointer value: " + ptrVal
								+ "\nOffsets value: " + oftVal);
				throw new UnsatisfiablePathConditionException();
			}
		} else {
			/*
			 * Value set pointer + Regular Range/Integer:
			 */
			assert ptrVal.type() == this.valueSetPointerType;
			ValueSetReference ref = (ValueSetReference) universe
					.tupleRead(ptrVal, universe.intObject(2));

			assert ref.isArraySectionReference();

			VSArraySectionReference secRef = (VSArraySectionReference) ref;
			NumericExpression section[] = new NumericExpression[3];

			parent = ((NTValueSetReference) ref).getParent();
			section[0] = secRef.lowerBound();
			section[1] = secRef.upperBound();// exclusive
			section[2] = secRef.step();
			if (isRange && (!section[2].isOne() || !range[2].isOne()))
				throw new CIVLUnimplementedFeatureException(
						"Ranges addition with non-trivial steps:" + expr);
			range[0] = universe.add(section[0], range[0]);
			range[1] = universe.add(section[1], range[isRange ? 1 : 0]);
			range[2] = section[2];
		}

		int vid = symbolicUtil.getVariableId(source, ptrVal);
		SymbolicExpression scopeVal = symbolicUtil.getScopeValue(ptrVal);
		ValueSetReference vsParent;

		if (parent.type() == universe.referenceType())
			vsParent = convertToValueSetReference(parent);
		else
			vsParent = (ValueSetReference) parent;
		return new Evaluation(state,
				valueSetPointer(vid, scopeVal, universe.vsArraySectionReference(
						vsParent, range[0], range[1], range[2])));
	}

	/**
	 * <p>
	 * Given a state, a variable ID vid, a dyscope ID sid and a
	 * {@link ValueSetReference} ref, returns a value of
	 * {@link CIVLMemType#getDynamicType(SymbolicUniverse}, which contains a
	 * component of value set template type <code>vst</code>. The component
	 * <code>vst</code> includes the given value set reference and is associated
	 * with the type of values of the variable that is identified by the given
	 * vid and sid.
	 * </p>
	 */
	private Evaluation makeValueOfMemType(State state, int pid, int vid,
			int sid, ValueSetReference vsRef, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		SymbolicExpression value = state.getVariableValue(sid, vid);
		SymbolicType valueType;
		int heapID = -1, mallocID = -1;
		Variable var = state.getDyscope(sid).lexicalScope().variable(vid);

		// variable type shall not (currently) contain sequence type:
		if (Utils.containSequenceType(var.type()))
			throw new CIVLUnimplementedFeatureException(
					"Capturing read/write footprints on"
							+ "variables whose types containing CIVL-C sequences at "
							+ source);
		// currently, no offset reference is allowed ...
		if (containsOffsetReference(vsRef)) {
			errorLogger.logSimpleError(source, state,
					state.getProcessState(pid).name(),
					symbolicAnalyzer.stateInformation(state), ErrorKind.OTHER,
					"Invalid memory location reference to variable: "
							+ var.name() + "\nVariable type: " + var.type()
							+ "\nReference: " + vsRef);
			throw new UnsatisfiablePathConditionException();
		}
		assert !value.isNull() || var.type()
				.isScalar() : "value-set references to an uninitialized aggregate-type variable";
		if (var.type().isScalar())
			valueType = var.type().getDynamicType(universe); // value maybe NULL
		else
			valueType = value.type();
		if (valueType == typeFactory.heapSymbolicType()) {
			/*
			 * Note that if the value type is dynamic heap type, the type is
			 * incomplete. The given value set reference shall only refer to one
			 * heap object (a heap object is allocated by a malloc call; one
			 * reference that refers to multiple heap objects makes no sense)
			 * and the complete type of the referred heap object can be obtained
			 * from the heap object value.
			 */
			/*
			 * Therefore, the $mem value should refer to the single heap object
			 * instead of the whole "heap variable".
			 */
			ValueSetReference ref2heapObject = vsReferenceToMemoryHeap(vsRef);
			ValueSetReference refOnHeapObject = vsReferenceOnMemoryHeap(vsRef);
			SymbolicType heapObjectType = getMalloctedObjectType(value,
					ref2heapObject);

			heapID = memoryHeapID(ref2heapObject);
			mallocID = memoryHeapMallocID(ref2heapObject);
			vsRef = refOnHeapObject;
			valueType = heapObjectType;
		}
		// error checking ...
		state = checkValueSetReferenceOutOfBound(state, pid, valueType, vsRef,
				source);

		// make value of dynamic $mem type ...
		Function<List<SymbolicExpression[]>, SymbolicExpression> memValueCreator = typeFactory
				.civlMemType().memValueCreator(universe);
		List<SymbolicExpression[]> result = new LinkedList<>();
		SymbolicExpression vst = universe.valueSetTemplate(valueType,
				new ValueSetReference[]{vsRef});

		result.add(new SymbolicExpression[]{universe.integer(vid),
				universe.integer(heapID), universe.integer(mallocID),
				stateFactory.scopeValue(sid), vst});
		return new Evaluation(state, memValueCreator.apply(result));
	}

	/**
	 * Check if any sub-{@link ValueSetReference}s of the kind of
	 * {@link VSReferenceKind#ARRAY_ELEMENT} or
	 * {@link VSReferenceKind#ARRAY_SECTION} refer to out-of bound regions
	 * 
	 * @param state
	 *            the current state
	 * @param pid
	 *            the PID of the running process
	 * @param valueType
	 *            the value type of what the given "ref" refers to
	 * @param ref
	 *            a value set reference that will be checked
	 * @param source
	 *            the {@link CIVLSource} that will be used for error reporting
	 * @return a state where some out-of bound error may has been logged.
	 * @throws UnsatisfiablePathConditionException
	 *             when the value set reference refers to regions that are out
	 *             of bound
	 */
	private State checkValueSetReferenceOutOfBound(State state, int pid,
			SymbolicType valueType, ValueSetReference ref, CIVLSource source)
			throws UnsatisfiablePathConditionException {
		BooleanExpression claim = checkValueSetReferenceOutOfBoundWorker(
				valueType, ref, source);
		ResultType resultType = null;

		if (claim.isTrue())
			return state;
		if (!claim.isFalse()) {
			Reasoner reasoner = universe
					.reasoner(state.getPathCondition(universe));

			resultType = reasoner.valid(claim).getResultType();
			if (resultType == ResultType.YES)
				return state;
		}

		String message = "value set reference refers to out-of bound areas\n"
				+ "value set reference: " + ref + "\nvalue: " + valueType;

		if (resultType != null)
			return errorLogger.logError(source, state, pid,
					symbolicAnalyzer.stateInformation(state), claim, resultType,
					ErrorKind.OUT_OF_BOUNDS, message);
		else
			errorLogger.logSimpleError(source, state,
					state.getProcessState(pid).name(),
					symbolicAnalyzer.stateInformation(state),
					ErrorKind.OUT_OF_BOUNDS, message);
		return state;
	}

	/**
	 * <p>
	 * Worker method for
	 * {@link #checkValueSetReferenceOutOfBound(State, int, SymbolicType, ValueSetReference, CIVLSource)}
	 * .
	 * </p>
	 * 
	 * <p>
	 * Note that there is a precondition for the parameter "valueType" and "ref"
	 * regarding to heap type:<br>
	 * If the given "valueType" is heap type, the given value set reference
	 * "ref" shall only refer to a single heap object (allocated by a malloc
	 * call). A value set reference refers to multiple heap objects makes no
	 * sense. Even though {@link CIVLTypeFactory#heapSymbolicType()} is
	 * incomplete, the caller of this method is responsible to make sure the
	 * type, which is a sub-type of "valueType", of the referred single heap
	 * object must be complete. (TODO: alas, CIVL-C sequence can break the
	 * pre-condition. So far, just skip checking for sequences)
	 * </p>
	 *
	 * 
	 * @param valueType
	 *            the dynamic type of the referred value
	 * @param ref
	 *            a value set reference
	 * @param source
	 *            the {@code CIVLSource} related to this method call and will be
	 *            used for error reporting
	 * @return
	 */
	private BooleanExpression checkValueSetReferenceOutOfBoundWorker(
			SymbolicType valueType, ValueSetReference ref, CIVLSource source) {
		BooleanExpression claim = universe.trueExpression();
		LinkedList<ValueSetReference> refStack = new LinkedList<>();

		while (!ref.isIdentityReference()) {
			refStack.push(ref);
			ref = ((NTValueSetReference) ref).getParent();
		}
		while (!refStack.isEmpty()) {
			ref = refStack.pop();
			switch (ref.valueSetReferenceKind()) {
				case ARRAY_ELEMENT :
				case ARRAY_SECTION : {
					SymbolicArrayType arrType = (SymbolicArrayType) valueType;

					valueType = arrType.elementType();
					if (!arrType.isComplete()) {
						// it must be the case that this "array" is a sequence.
						// The idea that "sequence is an incomplete array" is
						// kinda annoying.
						// Just not check for sequence.
						break;
					}
					if (ref.valueSetReferenceKind() == VSReferenceKind.ARRAY_ELEMENT) {
						NumericExpression index = ((VSArrayElementReference) ref)
								.getIndex();

						claim = universe.and(Arrays.asList(claim,
								universe.lessThanEquals(zero, index),
								universe.lessThan(index,
										((SymbolicCompleteArrayType) arrType)
												.extent())));
					} else {
						VSArraySectionReference secRef = (VSArraySectionReference) ref;
						NumericExpression lower = secRef.lowerBound(),
								upper = secRef.upperBound(),
								step = secRef.step();

						assert arrType.isComplete();

						NumericExpression extent = ((SymbolicCompleteArrayType) arrType)
								.extent();
						NumericSymbolicConstant bv = (NumericSymbolicConstant) symbolicUtil
								.freshBoundVariableFor(universe.integerType(),
										lower, upper, step, extent);
						BooleanExpression pred, restriction;

						// forall bv:
						// lower <= bv < upper && (bv - lower)%step == 0 -->
						// 0 <= bv < extent
						restriction = universe.and(Arrays.asList(
								universe.lessThanEquals(lower, bv),
								universe.lessThan(bv, upper),
								universe.equals(universe.modulo(
										universe.subtract(bv, lower), step),
										zero)));
						pred = universe.and(universe.lessThanEquals(zero, bv),
								universe.lessThan(bv, extent));
						claim = universe.forall(bv,
								universe.implies(restriction, pred));
					}
					break;
				}
				case TUPLE_COMPONENT :
					valueType = ((SymbolicTupleType) valueType).sequence()
							.getType(((VSTupleComponentReference) ref)
									.getIndex().getInt());
					break;
				case UNION_MEMBER :
					valueType = ((SymbolicUnionType) valueType).sequence()
							.getType(((VSUnionMemberReference) ref).getIndex()
									.getInt());
					break;
				case IDENTITY :
				case OFFSET :
				default :
					throw new CIVLInternalException("unreachable", source);
			}
		}
		return claim;
	}

	/**
	 * <p>
	 * Creates a symbolic expression of a mirror type of
	 * {@link CIVLTypeFactory#pointerSymbolicType()} in which
	 * {@link ReferenceExpression} type is replaced with
	 * {@link ValueSetReference} type.
	 * </p>
	 * 
	 * <p>
	 * Such a symbolic expression will be used only within this class to
	 * represent the evaluation of an sub-expression of a $mem-type-casting
	 * expression.
	 * </p>
	 * 
	 * <p>
	 * Such a symbolic expression can be used as arguments of
	 * {@link SymbolicUtility#getVariableId(CIVLSource, SymbolicExpression)} and
	 * {@link SymbolicUtility#getScopeValue(SymbolicExpression)} as well.
	 * </p>
	 */
	private SymbolicExpression valueSetPointer(int vid,
			SymbolicExpression scopeValue, ValueSetReference vsRef) {
		return universe.tuple(valueSetPointerType, new SymbolicExpression[]{
				scopeValue, universe.integer(vid), vsRef});
	}

	/**
	 * Returns true iff the given {@link ValueSetReference} contains at least
	 * one {@link VSOffsetReference}.
	 * 
	 * @param ref
	 *            a {@link ValueSetReference}
	 * @return true iff the given {@link ValueSetReference} contains at least
	 *         one {@link VSOffsetReference}.
	 */
	private boolean containsOffsetReference(ValueSetReference ref) {
		while (!ref.isIdentityReference()) {
			if (ref.isOffsetReference())
				return true;
			ref = ((NTValueSetReference) ref).getParent();
		}
		return false;
	}

	/**
	 * 
	 * @param pointer
	 *            a regular pointer or a
	 *            {@link #valueSetPointer(int, SymbolicExpression, ValueSetReference)}
	 * @return the {@link ReferenceExpression} or {@link ValueSetReference} in
	 *         the given pointer
	 */
	private SymbolicExpression getSymRef(SymbolicExpression pointer) {
		if (pointer.type() == valueSetPointerType)
			return universe.tupleRead(pointer, universe.intObject(2));
		else
			return symbolicUtil.getSymRef(pointer);
	}

	/* ********************** Related to Memory Heap ********************/
	/**
	 * <p>
	 * Given a {@link ValueSetReference} to some sub-value of a memory heap
	 * object, returns a {@link ValueSetReference} to the memory heap object
	 * </p>
	 * 
	 * @param ref
	 *            a {@link ValueSetReference} to some sub-value of a memory heap
	 *            object
	 * @return a {@link ValueSetReference} to the memory heap object
	 */
	private ValueSetReference vsReferenceToMemoryHeap(ValueSetReference ref) {
		ValueSetReference prev = null, prevprev = null;

		assert !ref.isIdentityReference();
		while (!ref.isIdentityReference()) {
			prevprev = prev;
			prev = ref;
			ref = ((NTValueSetReference) ref).getParent();
		}
		assert prevprev != null;
		assert prevprev
				.valueSetReferenceKind() == VSReferenceKind.ARRAY_ELEMENT;
		assert prev.valueSetReferenceKind() == VSReferenceKind.TUPLE_COMPONENT;
		return prevprev;
	}

	/**
	 * Given a value set reference <code>r</code> where the root identity
	 * reference refers to a heap variable, returns a value set reference
	 * <code>r'</code> where the root identity reference refers to the heap
	 * object that is referred by <code>r</code>. And, <code>r</code> and
	 * <code>r'</code> have the same sub-references on the heap object.
	 * 
	 * @param ref
	 *            a value set reference <code>r</code> where the root identity
	 *            reference refers to a heap variable
	 * @return a value set reference <code>r'</code> where the root identity
	 *         reference refers to the heap object that is referred by
	 *         <code>r</code>
	 */
	private ValueSetReference vsReferenceOnMemoryHeap(ValueSetReference ref) {
		ValueSetReference sentinel = vsReferenceToMemoryHeap(ref);
		LinkedList<ValueSetReference> descentStack = new LinkedList<ValueSetReference>();

		while (ref != sentinel) {
			descentStack.push(ref);
			ref = ((NTValueSetReference) ref).getParent();
		}
		sentinel = universe.vsIdentityReference();
		while (!descentStack.isEmpty()) {
			ref = descentStack.pop();
			switch (ref.valueSetReferenceKind()) {
				case ARRAY_ELEMENT :
					sentinel = universe.vsArrayElementReference(sentinel,
							((VSArrayElementReference) ref).getIndex());
					break;
				case ARRAY_SECTION : {
					VSArraySectionReference secRef = (VSArraySectionReference) ref;

					sentinel = universe.vsArraySectionReference(sentinel,
							secRef.lowerBound(), secRef.upperBound(),
							secRef.step());
					break;
				}
				case TUPLE_COMPONENT :
					sentinel = universe.vsTupleComponentReference(sentinel,
							((VSTupleComponentReference) ref).getIndex());
					break;
				case UNION_MEMBER :
					sentinel = universe.vsUnionMemberReference(sentinel,
							((VSUnionMemberReference) ref).getIndex());
					break;
				default :
					assert false : "unreachable";
			}
		}
		return sentinel;
	}

	/**
	 * <p>
	 * Given a $heap type symbolic expression <code>v</code> and a
	 * {@link ValueSetReference} to a memory heap object in <code>v</code>,
	 * returns the {@link SymbolicType} of the referred memory heap object.
	 * </p>
	 * 
	 * <p>
	 * see {@link CIVLTypeFactory#heapSymbolicType()}
	 * </p>
	 * 
	 * @param heapValue
	 *            a symbolic expression of
	 *            {@link CIVLTypeFactory#heapSymbolicType()}
	 * @param ref2heap
	 *            a {@link ValueSetReference} to a memory heap object
	 * @return the {@link SymbolicType} of the referred memory heap object
	 */
	private SymbolicType getMalloctedObjectType(SymbolicExpression heapValue,
			ValueSetReference ref2heap) {
		ValueSetReference parent = ((NTValueSetReference) ref2heap).getParent();
		IntObject tupleIdx = ((VSTupleComponentReference) parent).getIndex();
		NumericExpression mallocId = ((VSArrayElementReference) ref2heap)
				.getIndex();

		heapValue = universe.tupleRead(heapValue, tupleIdx);
		return universe.arrayRead(heapValue, mallocId).type();
	}

	/**
	 * Returns the heap ID of a heap object that is referred by the given
	 * reference. A heap ID is the ID of a lexical malloc statement.
	 */
	private int memoryHeapID(ValueSetReference ref2heap) {
		ValueSetReference parent = ((NTValueSetReference) ref2heap).getParent();
		IntObject tupleIdx = ((VSTupleComponentReference) parent).getIndex();

		return tupleIdx.getInt();
	}

	/**
	 * Returns the malloc ID of a heap object that is referred by the given
	 * reference. A malloc ID is the ID of a runtime malloc call of a lexically
	 * specific malloc statement.
	 */
	private int memoryHeapMallocID(ValueSetReference ref2heap) {
		NumericExpression mallocId = ((VSArrayElementReference) ref2heap)
				.getIndex();

		assert mallocId.operator() == SymbolicOperator.CONCRETE;
		return ((IntegerNumber) universe.extractNumber(mallocId)).intValue();
	}

	private static String[] prettyPrintValueSetTemplate(
			SymbolicUniverse universe, SymbolicExpression valueSetTemplate,
			CIVLSource source) {
		String result[];
		List<String> tmp = new LinkedList<>();

		for (ValueSetReference vsr : universe
				.valueSetReferences(valueSetTemplate)) {
			tmp.add(prettyPrintValueSetReference(vsr, source));
		}
		result = new String[tmp.size()];
		tmp.toArray(result);
		return result;
	}

	private static String prettyPrintValueSetReference(ValueSetReference vsr,
			CIVLSource source) {
		if (vsr.valueSetReferenceKind() == IDENTITY)
			return "";

		NTValueSetReference ntRef = (NTValueSetReference) vsr;
		String result = prettyPrintValueSetReference(ntRef.getParent(), source);

		switch (vsr.valueSetReferenceKind()) {
			case ARRAY_ELEMENT : {
				VSArrayElementReference vsElementRef = (VSArrayElementReference) vsr;

				return result + "[" + vsElementRef.getIndex() + "]";
			}
			case ARRAY_SECTION : {
				VSArraySectionReference vsSectionRef = (VSArraySectionReference) vsr;

				return result + "[" + vsSectionRef.lowerBound() + " .. "
						+ vsSectionRef.upperBound() + "]";
			}
			case TUPLE_COMPONENT : {
				VSTupleComponentReference vsTupleRef = (VSTupleComponentReference) vsr;

				// TODO: improve numeric field index with field name:
				return result + "." + vsTupleRef.getIndex();
			}
			case UNION_MEMBER : {
				VSUnionMemberReference vsUnionRef = (VSUnionMemberReference) vsr;

				return result + "." + vsUnionRef.getIndex();
			}
			case OFFSET : {
				VSOffsetReference vsOffsetReference = (VSOffsetReference) vsr;

				return result + "+" + vsOffsetReference.getOffset();
			}
			default :
				throw new CIVLUnimplementedFeatureException(
						"unknown value-set-ref kind "
								+ vsr.valueSetReferenceKind(),
						source);
		}
	}
}
