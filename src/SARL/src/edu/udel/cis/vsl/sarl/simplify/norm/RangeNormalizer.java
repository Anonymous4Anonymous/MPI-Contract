package edu.udel.cis.vsl.sarl.simplify.norm;

import java.io.PrintStream;
import java.util.Map.Entry;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.Constant;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.simplify.IF.Range;
import edu.udel.cis.vsl.sarl.simplify.IF.RangeFactory;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.ContextExtractor;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SimplifierUtility;
import edu.udel.cis.vsl.sarl.util.Pair;
import edu.udel.cis.vsl.sarl.util.WorkMap;

/**
 * Normalizes the range map of a {@link Context}.
 * 
 * @author xxxx
 */
public class RangeNormalizer implements Normalizer {

	/**
	 * Print debugging information?
	 */
	public final static boolean debug = false;

	/**
	 * Where the debug information should go.
	 */
	public final static PrintStream out = System.out;

	/** The {@link Context} that is being normalized. */
	private Context context;

	/** A reference to the context's range map. */
	private WorkMap<Monic, Range> rangeMap;

	// Entry<Monic, Range> newEntry;

	private SimplifierUtility info;

	public RangeNormalizer(Context context) {
		this.context = context;
		this.rangeMap = context.getRangeMap();
		this.info = context.getInfo();
	}

	/**
	 * Computes the simplification of a certain kind of {@link Entry} (which is
	 * just an ordered pair). The left component of the entry is an int division
	 * expression and the right component is an integer range. The pair encodes
	 * the claim that the evaluation of the left expression lands in the range,
	 * for any point satisfying the assumption. If no change, the result
	 * returned will be == the given pair.
	 * 
	 * <pre>
	 * Suppose b>0.
	 * Write Q = Q- U Q0 U Q+, where 
	 * Q-={x in Q | x<0}, Q0={x in Q | x=0}, and Q+={x in Q | x>0}.
	 * 
	 * a div b in Q- <==> a in b*Q- + [1-b,0]
	 * a div b in Q0 <==> a in b*Q0 + [1-b,b-1]
	 * a div b in Q+ <==> a in b*Q+ + [0,b-1]
	 * 
	 * Therefore a div b in Q <==> a in union of above. 
	 *	
	 * Example:
	 * 
	 * a div 3 in {2} <==> a in {3*2}+[0,2] = {6,7,8}
	 * a div 3 in {0} <==> a in {3*0}+[-2,2] = {-2,-1,0,1,2}
	 * a div 3 in {-2} <==> a in {3*-2}+[-2,0] = {-8,-7,-6}.
	 * 
	 * If b<0:
	 * a div b in Q- <==> a in b*Q- + [0,-b-1]
	 * a div b in Q0 <==> a in b*Q0 + [1+b,-b-1]
	 * a div b in Q+ <==> a in b*Q+ + [1+b,0]
	 * </pre>
	 * 
	 * @param pair
	 *            an entry as described above
	 * @return the simplified entry
	 */
	private Entry<Monic, Range> simplifyIntDivide(Entry<Monic, Range> pair)
			throws InconsistentContextException {
		Monic monic = pair.getKey();
		NumericExpression a = (NumericExpression) monic.argument(0),
				b = (NumericExpression) monic.argument(1);
		Range b_range = context.computeRange(b);
		IntegerNumber b_number = (IntegerNumber) b_range.getSingletonValue();

		if (b_number == null)
			return pair;

		NumberFactory nf = info.getNumberFactory();
		RangeFactory rf = info.getRangeFactory();
		IntegerNumber zero = nf.zeroInteger(), one = nf.oneInteger();
		Range empty = rf.emptySet(true);
		Range q = pair.getValue();
		Range q_n = rf.intersect(rf.interval(true, nf.negativeInfinityInteger(),
				true, nf.integer(-1), false), q);
		boolean q_0 = q.containsNumber(zero);
		Range q_p = rf.intersect(rf.interval(true, one, false,
				nf.positiveInfinityInteger(), true), q);
		Range b_n, b_0, b_p;

		if (b_number.signum() > 0) {
			IntegerNumber lo = nf.subtract(one, b_number), hi = nf.negate(lo);

			b_n = rf.interval(true, lo, false, zero, false);
			b_0 = q_0 ? rf.interval(true, lo, false, hi, false) : empty;
			b_p = rf.interval(true, zero, false, hi, false);
		} else {
			IntegerNumber lo = nf.increment(b_number), hi = nf.negate(lo);

			b_n = rf.interval(true, zero, false, hi, false);
			b_0 = q_0 ? rf.interval(true, lo, false, hi, false) : empty;
			b_p = rf.interval(true, lo, false, zero, false);
		}

		Range set_n = q_n.isEmpty() ? empty
				: rf.add(rf.multiply(q_n, b_number), b_n);
		Range set_p = q_p.isEmpty() ? empty
				: rf.add(rf.multiply(q_p, b_number), b_p);
		Range union = rf.union(rf.union(set_n, b_0), set_p);
		Pair<Monic, Range> norm = info.normalize((Monomial) a, union);

		return norm;
	}

	/**
	 * Simplifies a range map entry which has been temporarily removed from the
	 * range map, and, if the simplification results in a change, adds the
	 * simplified constraints back to the context. If the simplification does
	 * not result in change, the range map is not modified.
	 * 
	 * @param entry
	 *            an entry that has been removed from the {@link #rangeMap}
	 * @return <code>true</code> iff there is a change: the simplified key from
	 *         the entry is not the same as the original key. If there is no
	 *         change, the caller will probably want to re-insert the entry into
	 *         the range map.
	 * 
	 * @throws InconsistentContextException
	 *             if, in the process of simplifying, it is discovered that the
	 *             context is inconsistent (equivalent to false)
	 */
	private boolean processEntry(Entry<Monic, Range> entry,
			Set<SymbolicConstant> dirtyOut)
			throws InconsistentContextException {
		Monic oldKey = entry.getKey();
		NumericExpression simpKey = (NumericExpression) context
				.simplify(oldKey);
		Range oldRange = entry.getValue();

		if (simpKey instanceof Constant) {
			if (!oldRange.containsNumber(((Constant) simpKey).number()))
				throw new InconsistentContextException();
		} else if (simpKey instanceof Monomial) {
			Entry<Monic, Range> result = entry;

			if (oldKey != simpKey)
				result = info.normalize((Monomial) simpKey, oldRange);
			if (result.getKey().operator() == SymbolicOperator.INT_DIVIDE)
				result = simplifyIntDivide(result);
			if (result == entry)
				return false;
			else {
				Range newRange = result.getValue();

				if (newRange != null)
					context.restrictRange(result.getKey(), newRange, dirtyOut);
			}
		} else {
			// in this case, simpKey must be a RationalExpression.
			// this is rare, but possible, e.g., if oldKey looked like
			// p?x/y:0, and p simplifies to true.
			// need to reduce to a restriction involving only Monics.
			BooleanExpression assumption = oldRange
					.symbolicRepresentation(simpKey, info.getUniverse());
			ContextExtractor ce = new ContextExtractor(context, dirtyOut);

			ce.extractClause(assumption);
		}
		return true;
	}

	/**
	 * Simplifies the {@link #rangeMap}. Removes one entry from the map,
	 * simplifies it, places it back. Repeats until stabilization. If an entry
	 * ever resolves to a single value, it is removed completely and added to
	 * the {@link #subMap}.
	 */
	public void normalize(Set<SymbolicConstant> dirtyIn,
			Set<SymbolicConstant> dirtyOut)
			throws InconsistentContextException {
		// for now, not using dirtyIn. We will assume all entries
		// are dirty. Eventually fix that.
		rangeMap.makeAllDirty(); // put everything on the work list
		for (Entry<Monic, Range> oldEntry = rangeMap
				.hold(); oldEntry != null; oldEntry = rangeMap.hold()) {
			context.clearSimplifications();

			boolean change = processEntry(oldEntry, dirtyOut);

			if (!change)
				rangeMap.release(); // put back the entry on hold
		}
	}
}
