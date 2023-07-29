package edu.udel.cis.vsl.sarl.simplify.norm;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTupleType;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * <p>
 * Simplify non-concrete tuple type expressions to concrete tuples. A concrete
 * tuple is defined in {@link SymbolicTupleSimplifier}.
 * </p>
 */
public class TupleNormalizer implements Normalizer {

	/**
	 * The context being simplified.
	 */
	private Context context;

	/**
	 * A reference to {@link #PreUniverse}
	 */
	private PreUniverse universe;

	/**
	 * Creates new {@link TupleNormalizer} for simplifying the given
	 * {@link Context}.
	 * 
	 * @param context
	 *            the context to be simplified
	 */
	public TupleNormalizer(Context context) {
		this.context = context;
		this.universe = context.getInfo().getUniverse();
	}

	/**
	 * This class simplifies symbolic expressions of tuple type that are
	 * non-concrete to concrete tuple expressions. A concrete tuple expression
	 * is
	 * 
	 * <ol>
	 * 
	 * <li>a symbolic expression whose {@link SymbolicOperator} equals to
	 * {@link SymbolicOperator#TUPLE}</li>
	 * 
	 * <li>recursively that each tuple component must be a concrete tuple, a
	 * symbolic expression with CONCRETE operator or a
	 * {@link ReferenceExpression}</li>
	 * 
	 * </ol>
	 * 
	 * @author xxxx
	 */
	public class SymbolicTupleSimplifier {
		/**
		 * <p>
		 * A queue of entries that have not been processed.
		 * </p>
		 * 
		 * <p>
		 * The process of each entry is mainly analyzing that if the key of this
		 * entry is a component of a non-concrete tuple and simplify the
		 * non-concrete tuple once all of its components have a value that is
		 * either concrete or is a symbolic constant.
		 * </p>
		 */
		private LinkedList<Pair<SymbolicExpression, SymbolicExpression>> workingEntries;

		/**
		 * A map from non-concrete tuples to their concrete equivalences.
		 */
		private Map<SymbolicExpression, SymbolicExpression> tupleSubstitutions;

		/**
		 * A map from non-concrete tuples to their component values. For each
		 * component value, it can only be NULL, a concrete value or a symbolic
		 * constant (or an APPLY expression).
		 */
		private Map<SymbolicExpression, SymbolicExpression[]> tupleComponentsMap;

		SymbolicTupleSimplifier() {
			this.workingEntries = new LinkedList<>();
			this.tupleSubstitutions = new HashMap<>();
			this.tupleComponentsMap = new HashMap<>();
			// initialize:
			for (Entry<SymbolicExpression, SymbolicExpression> entry : context
					.getSubEntries())
				workingEntries
						.add(new Pair<>(entry.getKey(), entry.getValue()));
			simplify();
		}

		/**
		 * @return a map from non-concrete tuples to their concrete
		 *         equivalences.
		 */
		Map<SymbolicExpression, SymbolicExpression> getTupleSubstitutionMap() {
			return tupleSubstitutions;
		}

		/**
		 * Collecting concrete component values for tuples from entries of the
		 * {@link Context#subMap}. Add entries to {@link #tupleSubstitutions}
		 * once a non-concrete tuple can be simplified to a concrete one.
		 */
		private void simplify() {
			while (!workingEntries.isEmpty()) {
				Pair<SymbolicExpression, SymbolicExpression> equation = workingEntries
						.removeFirst();
				SymbolicExpression key = equation.left;
				SymbolicExpression value = equation.right;

				if (key.operator() == SymbolicOperator.TUPLE_READ)
					simplifyTupleRead(key, value);
				else if (key.operator() == SymbolicOperator.ADD
						&& value.isZero())
					simplifyReferenceExpression(key);
			}
		}

		/**
		 * <p>
		 * Given a TUPLE_READ expression : <code>tuple.field</code> that has a
		 * concrete value <code>val</code>, updates the
		 * {@link #tupleComponentsMap} by updating the corresponding component
		 * value to <code>val</code>.
		 * </p>
		 * 
		 * <p>
		 * If all components of the tuple have concrete values (or symbolic
		 * constant values), updates the {@link #tupleSubstitutions} map and add
		 * a new entry <code>tuple = {...} </code> to {@link #workingEntries}.
		 * </p>
		 */
		private void simplifyTupleRead(SymbolicExpression tupleRead,
				SymbolicExpression concreteValue) {
			SymbolicExpression tuple = (SymbolicExpression) tupleRead
					.argument(0);
			IntObject fieldIdx = (IntObject) tupleRead.argument(1);
			SymbolicExpression tupleComponents[];

			if (tuple.operator() == SymbolicOperator.TUPLE)
				return;
			tupleComponents = tupleComponentsMap.get(tuple);
			if (tupleComponents == null) {
				SymbolicTupleType tupleType = (SymbolicTupleType) tuple.type();
				int numTypes = tupleType.sequence().numTypes();

				tupleComponents = new SymbolicExpression[numTypes];
				Arrays.fill(tupleComponents, null);
			}
			tupleComponents[fieldIdx.getInt()] = concreteValue;

			boolean complete = true;

			for (int i = 0; i < tupleComponents.length; i++)
				if (tupleComponents[i] == null) {
					complete = false;
					break;
				}
			if (complete) {
				SymbolicExpression concreteTuple = universe.tuple(
						(SymbolicTupleType) tuple.type(), tupleComponents);

				tupleSubstitutions.put(tuple, concreteTuple);
				tupleComponentsMap.remove(tuple);
				workingEntries.add(new Pair<>(tuple, concreteTuple));
			} else
				tupleComponentsMap.put(tuple, tupleComponents);
		}

		/**
		 * <p>
		 * Process an equation that asserts two expressions of reference typeS
		 * are equivalent: <code>
		 *   Ref0.0 + (-1)*Ref1.0 = 0
		 * </code>. Such an equation means that Ref0 is identical to Ref1.
		 * </p>
		 * 
		 * <p>
		 * We consider that a {@link ReferenceExpression} is concrete. Hence, if
		 * Ref0 (resp. Ref1) is an expression of reference type but not a
		 * {@link ReferenceExpression} while Ref1 (resp. Ref0) is a
		 * {@link ReferenceExpression}, add Ref0 (resp. Ref1) as key and
		 * Ref1(resp. Ref0) as value to the {@link #tupleSubstitutions} map.
		 * </p>
		 */
		private void simplifyReferenceExpression(SymbolicExpression add) {
			SymbolicExpression op0 = (SymbolicExpression) add.argument(0);
			SymbolicExpression op1 = (SymbolicExpression) add.argument(1);

			op1 = universe.minus((NumericExpression) op1);
			if (op0.operator() != SymbolicOperator.TUPLE_READ
					|| op1.operator() != SymbolicOperator.TUPLE_READ)
				return;

			SymbolicExpression ref0 = (SymbolicExpression) op0.argument(0);
			SymbolicExpression ref1 = (SymbolicExpression) op1.argument(0);

			if (!ref0.type().equals(universe.referenceType())
					|| !ref1.type().equals(universe.referenceType()))
				return;
			if (ref0 instanceof ReferenceExpression
					&& !(ref1 instanceof ReferenceExpression)) {
				tupleSubstitutions.put(ref1, ref0);
				workingEntries.add(new Pair<>(ref1, ref0));
			} else if (ref1 instanceof ReferenceExpression
					&& !(ref0 instanceof ReferenceExpression)) {
				tupleSubstitutions.put(ref0, ref1);
				workingEntries.add(new Pair<>(ref0, ref1));
			}
		}
	}

	/**
	 * Simplify non-concrete tuple type expressions to concrete tuples. A
	 * concrete tuple is defined in {@link SymbolicTupleSimplifier}.
	 * 
	 * @param dirtyIn
	 *            symbolic constants which are dirty on input; used to determine
	 *            the set of tuple entries of interest
	 * 
	 * @param dirtyOut
	 *            symbolic constants involved in modified entries will be added
	 *            to this set
	 * 
	 * @throws InconsistentContextException
	 *             if any new substitution from a non-concrete tuple to a
	 *             concrete one violates the invariants of the {@link #subMap}.
	 */
	@Override
	public void normalize(Set<SymbolicConstant> dirtyIn,
			Set<SymbolicConstant> dirtyOut)
			throws InconsistentContextException {
		// TODO: for now, not using dirtyIn. Eventually use this to limit
		// the search in some way.
		Map<SymbolicExpression, SymbolicExpression> ncTuple2concrete = new SymbolicTupleSimplifier()
				.getTupleSubstitutionMap();

		// simplify non-concrete tuples:
		for (Entry<SymbolicExpression, SymbolicExpression> entry : ncTuple2concrete
				.entrySet())
			context.addSub(entry.getKey(), entry.getValue(), dirtyOut);
	}
}
