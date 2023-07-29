package edu.udel.cis.vsl.sarl.simplify.simplification;

import java.util.Arrays;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUnionType;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SimplifierUtility;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SubContext;

/**
 * A {@link Simplification} takes a {@link SymbolicExpression} and returns an
 * equivalent {@link SymbolicExpression} in a simplified form. Each instance of
 * {@link Simplification} does one and only one thing. A typical simplification
 * object may be applicable to only a certain kind of {@link SymbolicExpression}
 * , for example, to those expressions whose operator is
 * {@link SymbolicOperator#OR}. A client can chain together multiple
 * {@link Simplification}s to form a more powerful simplification engine.
 * 
 * This base class provides utility methods that will be commonly used by most
 * instances.
 * 
 * @author xxxx
 *
 */
public abstract class Simplification
		implements UnaryOperator<SymbolicExpression> {

	/**
	 * An enumerated type corresponding 1-1 with the different
	 * {@link Simplification} classes.
	 * 
	 * @author xxxx
	 */
	public static enum SimplificationKind {
		ARRAY_LAMBDA, ARRAY_READ, COND, GENERIC, LAMBDA, OR, NUMERIC_OR, POLYNOMIAL, POWER, QUANTIFIER, RATIONAL, SUBCONTEXT, MODULO
	};

	/**
	 * The worker used by the simplifier to carry out simplification tasks.
	 */
	private IdealSimplifierWorker worker;

	/**
	 * Constructs a new instance based on given worker. Does not compute
	 * anything.
	 */
	Simplification(IdealSimplifierWorker worker) {
		this.worker = worker;
	}

	/**
	 * Returns the kind of this {@link Simplification}.
	 * 
	 * @return the kind
	 */
	public abstract SimplificationKind kind();

	/**
	 * Gets the factory responsible for creating ideal expressions
	 * 
	 * @return the factory responsible for creating ideal expressions
	 */
	IdealFactory idealFactory() {
		return worker.getContext().getInfo().getIdealFactory();
	}

	SubContext newSubContext(BooleanExpression assumption) {
		return new SubContext(worker.getContext(), simplificationStack(),
				assumption);
	}

	SubContext newSubContext() {
		return new SubContext(worker.getContext(), simplificationStack());
	}

	/**
	 * Gets the number factory used by the context.
	 * 
	 * @return the number factory
	 */
	NumberFactory numberFactory() {
		return worker.getContext().getInfo().getNumberFactory();
	}

	PreUniverse universe() {
		return info().getUniverse();
	}

	/**
	 * Gets the {@link SimplifierUtility} object associated to the context.
	 * 
	 * @return the {@link SimplifierUtility} object
	 */
	SimplifierUtility info() {
		return worker.getContext().getInfo();
	}

	/**
	 * Caches the given simplification result within {@link #theContext}.
	 * 
	 * @param object
	 *            any non-<code>null</code> {@link SymbolicObject}
	 * @param result
	 *            the result returned of simplifying that object
	 */
	void cacheSimplification(SymbolicObject object, SymbolicObject result) {
		worker.getContext().cacheSimplification(object, result);
	}

	/**
	 * Retrieves a cached simplification result. Simplification results are
	 * cached using method
	 * {@link #cacheSimplification(SymbolicObject, SymbolicObject)}, which in
	 * turns uses {@link #theContext}'s simplification cache to cache results.
	 * Note that every time {@link #theContext} changes, its cache is cleared.
	 * 
	 * @param object
	 *            the object to be simplified
	 * @return the result of a previous simplification applied to {@code object}
	 *         , or <code>null</code> if no such result is cached
	 */
	SymbolicObject getCachedSimplification(SymbolicObject object) {
		return worker.getContext().getSimplification(object);
	}

	SymbolicExpression simplifyExpression(SymbolicExpression expression) {
		return worker.simplifyExpression(expression);
	}

	SymbolicExpression simplifyExpressionWork(SymbolicExpression expression) {
		return worker.simplifyExpressionWork(expression);
	}

	/**
	 * Performs the work required to simplify a non-simple symbolic type. A
	 * primitive type is returned unchanged. For compound types, simplification
	 * is recursive on the structure of the type. Ultimately a non-trivial
	 * simplification can occur because array types may involve an expression
	 * for the length of the array.
	 *
	 * <p>
	 * A subtle point is that the types must be simplified based on the *global*
	 * context, i.e., the last ancestor context of the current context, which is
	 * not an instance of {@link SubContext}. Otherwise, the simplified
	 * expression could end up with two different versions of a variable with
	 * two different types. The following example illustrates this:
	 * </p>
	 * 
	 * <pre>
	 * N:int
	 * A:int[N]
	 * A[0]>7         // this A has type int[N]
	 * N=1 -> A[0]=6  // this A has type int[1]
	 * </pre>
	 * 
	 * @param type
	 *            any non-null non-simple symbolic type
	 * @return simplified version of that type
	 */
	SymbolicType simplifyTypeWork(SymbolicType type) {
		SymbolicTypeKind kind = type.typeKind();

		switch (kind) {
		case ARRAY: {
			SymbolicArrayType arrayType = (SymbolicArrayType) type;
			SymbolicType elementType = arrayType.elementType();
			SymbolicType simplifiedElementType = simplifyType(elementType);

			if (arrayType.isComplete()) {
				NumericExpression extent = ((SymbolicCompleteArrayType) arrayType)
						.extent();
				Context globalContext = worker.getContext().getGlobalContext();
				NumericExpression simplifiedExtent = (NumericExpression) globalContext
						.simplify(extent);
				// NumericExpression simplifiedExtent =(NumericExpression)
				// simplifyExpression(extent);

				if (elementType != simplifiedElementType
						|| extent != simplifiedExtent)
					return universe().arrayType(simplifiedElementType,
							simplifiedExtent);
				return arrayType;
			} else {
				if (elementType != simplifiedElementType)
					return universe().arrayType(simplifiedElementType);
				return arrayType;
			}
		}
		case FUNCTION: {
			SymbolicFunctionType functionType = (SymbolicFunctionType) type;
			SymbolicTypeSequence inputs = functionType.inputTypes();
			SymbolicTypeSequence simplifiedInputs = simplifyTypeSequence(
					inputs);
			SymbolicType output = functionType.outputType();
			SymbolicType simplifiedOutput = simplifyType(output);

			if (inputs != simplifiedInputs || output != simplifiedOutput)
				return universe().functionType(simplifiedInputs,
						simplifiedOutput);
			return type;
		}
		case TUPLE: {
			SymbolicTypeSequence sequence = ((SymbolicTupleType) type)
					.sequence();
			SymbolicTypeSequence simplifiedSequence = simplifyTypeSequence(
					sequence);

			if (simplifiedSequence != sequence)
				return universe().tupleType(((SymbolicTupleType) type).name(),
						simplifiedSequence);
			return type;
		}
		case UNION: {
			SymbolicTypeSequence sequence = ((SymbolicUnionType) type)
					.sequence();
			SymbolicTypeSequence simplifiedSequence = simplifyTypeSequence(
					sequence);

			if (simplifiedSequence != sequence)
				return universe().unionType(((SymbolicUnionType) type).name(),
						simplifiedSequence);
			return type;
		}
		default:
			throw new SARLInternalException("unreachable");
		}
	}

	/**
	 * Simplifies a symbolic type, using caching.
	 * 
	 * @param type
	 *            a non-{@code null} symbolic type
	 * @return the simplified version of the type
	 */
	SymbolicType simplifyType(SymbolicType type) {
		if (SimplifierUtility.isSimpleType(type))
			return type;

		SymbolicType result = (SymbolicType) getCachedSimplification(type);

		if (result == null) {
			result = simplifyTypeWork(type);
			cacheSimplification(type, result);
		}
		return result;
	}

	/**
	 * Performs the work necessary to simplify a type sequence. The
	 * simplification of a type sequence is the sequence resulting from
	 * simplifying each component type individually.
	 * 
	 * @param sequence
	 *            any non-{@code null} type sequence
	 * @return the simplified sequence
	 */
	SymbolicTypeSequence simplifyTypeSequenceWork(
			SymbolicTypeSequence sequence) {
		int size = sequence.numTypes();

		for (int i = 0; i < size; i++) {
			SymbolicType type = sequence.getType(i);
			SymbolicType simplifiedType = simplifyType(type);

			if (type != simplifiedType) {
				SymbolicType[] newTypes = new SymbolicType[size];

				for (int j = 0; j < i; j++)
					newTypes[j] = sequence.getType(j);
				newTypes[i] = simplifiedType;
				for (int j = i + 1; j < size; j++)
					newTypes[j] = simplifyType(sequence.getType(j));

				return universe().typeSequence(Arrays.asList(newTypes));
			}
		}
		return sequence;
	}

	/**
	 * Simplifies a type sequence, using caching.
	 * 
	 * @param seq
	 *            and non-{@code null} type sequence
	 * @return the simplified version of that type sequence
	 */
	SymbolicTypeSequence simplifyTypeSequence(SymbolicTypeSequence seq) {
		SymbolicTypeSequence result = (SymbolicTypeSequence) getCachedSimplification(
				seq);

		if (result == null) {
			result = simplifyTypeSequenceWork(seq);
			cacheSimplification(seq, result);
		}
		return result;
	}

	Set<SymbolicExpression> simplificationStack() {
		return worker.getSimplificationStack();
	}

	Interval intervalApproximation(NumericExpression expr) {
		return worker.getContext().computeRange((RationalExpression) expr)
				.intervalOverApproximation();
	}

	SymbolicExpression getSub(SymbolicExpression x) {
		return worker.getContext().getSub(x);
	}

	SymbolicExpression genericSimplify(SymbolicExpression x) {
		return new GenericSimplification(worker).apply(x);
	}

}
