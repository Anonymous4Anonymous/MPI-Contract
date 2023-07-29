package edu.udel.cis.vsl.sarl.simplify.simplification;

import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SimplifierUtility;

public class GenericSimplification extends Simplification {

	public GenericSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	/**
	 * Performs the work necessary for simplifying a sequence of symbolic
	 * expressions. The result is obtained by simplifying each component
	 * individually.
	 * 
	 * @param sequence
	 *            any canonic symbolic expression sequence
	 * @return the simplified sequence
	 */
	private SymbolicSequence<?> simplifySequenceWork(
			SymbolicSequence<?> sequence) {
		int size = sequence.size();
		SymbolicSequence<?> result = sequence;

		for (int i = 0; i < size; i++) {
			SymbolicExpression oldElement = sequence.get(i);
			SymbolicExpression newElement = simplifyExpression(oldElement);

			if (newElement != oldElement) {
				SymbolicExpression[] newElements = new SymbolicExpression[size];

				for (int j = 0; j < i; j++)
					newElements[j] = sequence.get(j);
				newElements[i] = newElement;
				for (int j = i + 1; j < size; j++)
					newElements[j] = simplifyExpression(sequence.get(j));
				result = universe().objectFactory().sequence(newElements);
				break;
			}
		}
		return result;
	}

	/**
	 * Performs the work necessary to simplify a non-simple symbolic object.
	 * This just redirects to the appropriate specific method, such as
	 * {@link #simplifySequenceWork(SymbolicSequence)},
	 * {@link #simplifyTypeWork(SymbolicType)}, etc.
	 * 
	 * @param object
	 *            a non-null non-simple symbolic object
	 * @return the simplified version of that object
	 */
	private SymbolicObject simplifyObjectWork(SymbolicObject object) {
		switch (object.symbolicObjectKind()) {
		case EXPRESSION:
			return simplifyExpressionWork((SymbolicExpression) object);
		case SEQUENCE:
			return simplifySequenceWork((SymbolicSequence<?>) object);
		case TYPE:
			return simplifyTypeWork((SymbolicType) object);
		case TYPE_SEQUENCE:
			return simplifyTypeSequenceWork((SymbolicTypeSequence) object);
		default:
			throw new SARLInternalException("unreachable");
		}
	}

	/**
	 * Simplifies a symbolic object by first looking in the cache for the
	 * previous result of simplifying that object, and, if not found there,
	 * invoking {@link #simplifyObjectWork(SymbolicObject)}
	 * 
	 * @param object
	 *            any non-<code>null</code> symbolic object
	 * @return result of simplification of <code>object</code>
	 */
	private SymbolicObject simplifyObject(SymbolicObject object) {
		if (SimplifierUtility.isSimpleObject(object))
			return object;

		SymbolicObject result = getCachedSimplification(object);

		if (result == null) {
			result = simplifyObjectWork(object);
			cacheSimplification(object, result);
		}
		return result;
	}

	/**
	 * <p>
	 * This method simplifies an expression in a generic way that should work
	 * correctly on any symbolic expression: it simplifies the type and the
	 * arguments of the expression, and then rebuilds the expression using
	 * method
	 * {@link PreUniverse#make(SymbolicOperator, SymbolicType, SymbolicObject[])}
	 * .
	 * </p>
	 * 
	 * <p>
	 * This method does <strong>not</strong> look in the table of cached
	 * simplification results for <code>expression</code>. However, the
	 * recursive calls to the arguments may look in the cache.
	 * </p>
	 * 
	 * <p>
	 * You will probably want to use this method in your implementation of
	 * {@link #simplifyExpressionWork(SymbolicExpression)}.
	 * </p>
	 * 
	 * @param expression
	 *            any non-<code>null</code> symbolic expression
	 * @return a simplified version of that expression
	 */
	@Override
	public SymbolicExpression apply(SymbolicExpression expression) {
		if (expression.isNull())
			return expression;

		SymbolicOperator operator = expression.operator();

		if (operator == SymbolicOperator.CONCRETE) {
			SymbolicObject object = (SymbolicObject) expression.argument(0);
			SymbolicObjectKind kind = object.symbolicObjectKind();

			switch (kind) {
			case BOOLEAN:
			case INT:
			case NUMBER:
			case STRING:
				return expression;
			default:
			}
		}

		SymbolicType type = expression.type();
		SymbolicType simplifiedType = simplifyType(type);
		int numArgs = expression.numArguments();
		SymbolicObject[] simplifiedArgs = null;

		if (type == simplifiedType) {
			for (int i = 0; i < numArgs; i++) {
				SymbolicObject arg = expression.argument(i);
				SymbolicObject simplifiedArg = simplifyObject(arg);

				assert simplifiedArg != null;
				if (simplifiedArg != arg) {
					simplifiedArgs = new SymbolicObject[numArgs];
					for (int j = 0; j < i; j++)
						simplifiedArgs[j] = expression.argument(j);
					simplifiedArgs[i] = simplifiedArg;
					for (int j = i + 1; j < numArgs; j++)
						simplifiedArgs[j] = simplifyObject(
								expression.argument(j));
					break;
				}
			}
		} else {
			simplifiedArgs = new SymbolicObject[numArgs];
			for (int i = 0; i < numArgs; i++)
				simplifiedArgs[i] = simplifyObject(expression.argument(i));
		}
		if (simplifiedArgs == null)
			return expression;
		return universe().make(operator, simplifiedType, simplifiedArgs);
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.GENERIC;
	}

}
