package edu.udel.cis.vsl.sarl.simplify.norm;

import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.ContextExtractor;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SimplifierUtility;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * Simplifies the context's substitution map. When this method returns, each
 * key-value pair in the subMap will be simplified using all other entries. It
 * may also modify the context's range map, because simplified formulas may
 * result in range restrictions that are added to the range map.
 */
public class SubstitutionNormalizer implements Normalizer {

	/**
	 * The context being simplified.
	 */
	private Context context;

	/**
	 * Creates new {@link SubstitutionNormalizer} for simplifying the given
	 * {@link Context}.
	 * 
	 * @param context
	 *            the context to be simplified
	 */
	public SubstitutionNormalizer(Context context) {
		this.context = context;
	}

	/**
	 * Finds all entries in the context's substitution map (but note: not in its
	 * ancestors' substitution maps) that involve a variable in the given dirty
	 * set. An entry involves a variable if that variable occurs in the key or
	 * value of the entry (or both).
	 * 
	 * @param dirtSet
	 *            a set of {@link SymbolicConstant}s (variables) of interest
	 * @return the list of keys from the entries in the substitution map which
	 *         involve at least one of those dirty variables.
	 */
	private LinkedList<SymbolicExpression> buildSubmapWorklist(
			Set<SymbolicConstant> dirtSet) {
		LinkedList<SymbolicExpression> result = new LinkedList<>();
		Set<Entry<SymbolicExpression, SymbolicExpression>> entrySet;

		entrySet = context.getSubEntries();
		for (Entry<SymbolicExpression, SymbolicExpression> entry : entrySet) {
			SymbolicExpression key = entry.getKey(), value = entry.getValue();

			if (SimplifierUtility.intersects(key, dirtSet)
					|| SimplifierUtility.intersects(value, dirtSet))
				result.add(key);
		}
		return result;
	}

	/**
	 * Simplifies the context's substitution map. When this method returns, each
	 * key-value pair in the subMap will be simplified using all other entries.
	 * 
	 * Given the original dirty set which determines the initial set of entries
	 * that must be simplified; upon return, dirtySet will contain all symbolic
	 * constants ...
	 * 
	 * @param dirtyIn
	 *            set of symbolic constants considered dirty, used to determine
	 *            the initial set of entries in the subMap which will be
	 *            simplified. More entries can be added as more variables become
	 *            dirty. This set is not modified by this method.
	 * 
	 * @param dirtyOut
	 *            the actual set of symbolic constants occurring in entries
	 *            which are modified or added to the subMap
	 *
	 */
	@Override
	public void normalize(Set<SymbolicConstant> dirtyIn,
			Set<SymbolicConstant> dirtyOut)
			throws InconsistentContextException {
		Set<SymbolicConstant> dirtyNow = SimplifierUtility
				.cloneDirtySet(dirtyIn);
		ContextExtractor extractor = new ContextExtractor(context, dirtyNow);

		while (!dirtyNow.isEmpty()) {
			LinkedList<SymbolicExpression> worklist = buildSubmapWorklist(
					dirtyNow);

			dirtyNow.clear();
			while (!worklist.isEmpty()) {
				SymbolicExpression key1 = worklist.remove(),
						value1 = context.getSub(key1);

				if (value1 == null)
					continue;
				context.removeSubkey(key1);

				SymbolicExpression key2 = context.simplify(key1),
						value2 = context.simplify(value1);
				Pair<SymbolicExpression, SymbolicExpression> pair = new Pair<>(
						key2, value2);

				context.standardizePair(pair);

				SymbolicExpression newKey = pair.left;

				if (newKey == null)
					continue; // a trivial substitution

				SymbolicExpression newValue = pair.right;
				SymbolicExpression oldValue = context.getSub(newKey);

				if (oldValue == newValue) {
					// do nothing: the new sub is already in the subMap
				} else if (newKey == key1 && newValue == value1) {
					// no change: put it back, but don't count it as dirty...
					context.putSub(key1, value1);
				} else if (newValue.isTrue()
						&& SimplifierUtility.isNumericRelational(newKey)) {
					// it goes to the rangeMap...
					extractor.extractClause((BooleanExpression) newKey);
				} else {
					// add the new sub, updating dirty...
					context.addSub(newKey, newValue, dirtyNow);
				}
			}
			dirtyOut.addAll(dirtyNow);
		}
	}

}
