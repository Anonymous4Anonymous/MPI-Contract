package edu.udel.cis.vsl.sarl.simplify.simplifier;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.simplify.IF.Range;
import edu.udel.cis.vsl.sarl.util.WorkMap;

/**
 * A sub-context represents a boolean expression that holds within the context
 * of some other assumption. Hence everything in the super-context is assumed to
 * hold, in addition to everything in the sub-context. This is used to provide
 * scoping to contexts.
 * 
 * @author xxxx
 */
public class SubContext extends Context {

	// Static fields...

	/** Should we print debugging information? */
	public static boolean debug = false;

	/** Where the debugging output goes. */
	public final static PrintStream out = System.out;

	// Instance fields...

	/**
	 * The super-context.
	 */
	private Context superContext;

	/**
	 * Set of expressions currently in the process of being simplified.s
	 */
	private Set<SymbolicExpression> simplificationStack;

	/**
	 * The length of the super-context chain starting from this
	 * {@link SubContext}. Used for debugging.
	 */
	private int depth;

	// Constructors ...

	/**
	 * New empty sub-context (equivalent to assumption true).
	 * 
	 * @param superContext
	 *            the (non-{@code null}) context containing this one
	 * @param simplificationStack
	 *            the symbolic expressions that have already been seen; used to
	 *            prevent cycles (currently only in debug mode)
	 */
	public SubContext(Context superContext,
			Set<SymbolicExpression> simplificationStack) {
		super(superContext.getInfo(), superContext.backwardsSub);
		this.superContext = superContext;
		this.simplificationStack = simplificationStack;
		if (debug) {
			if (superContext instanceof SubContext)
				depth = ((SubContext) superContext).depth + 1;
			else
				depth = 1;
			out.println("SubContext depth = " + depth);
		}
	}

	/**
	 * Creates new sub-context and initializes it using the given assumption.
	 * 
	 * @param superContext
	 *            the (non-{@code null}) context containing this one
	 * @param simplificationStack
	 *            the symbolic expressions that have already been seen; used to
	 *            prevent cycles (currently only in debug mode)
	 * @param assumption
	 *            the boolean expression to be represented by this sub-context
	 */
	public SubContext(Context superContext,
			Set<SymbolicExpression> simplificationStack,
			BooleanExpression assumption) {
		this(superContext, simplificationStack);
		initialize(assumption);
	}

	// Package-private methods...

	/**
	 * Returns the super-context of this sub-context, which will not be
	 * {@code null}
	 * 
	 * @return the super-context of this context
	 */
	Context getSuperContext() {
		return superContext;
	}

	@Override
	void addSubsToMap(Map<SymbolicExpression, SymbolicExpression> map) {
		superContext.addSubsToMap(map);
		map.putAll(subMap);
	}

	@Override
	Map<SymbolicExpression, SymbolicExpression> getFullSubMap() {
		Map<SymbolicExpression, SymbolicExpression> map = new HashMap<>();

		addSubsToMap(map);
		return map;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Looks first in this {@link SubContext} for an entry in the range map for
	 * the given {@link Monic}. If none is found, then looks in the
	 * super-context.
	 * </p>
	 * 
	 * @return the range associated to {@code monic}, or {@code null}
	 */
	@Override
	Range getRange(Monic monic) {
		Range result = super.getRange(monic);

		if (result != null)
			return result;
		result = superContext.getRange(monic);
		return result;
	}

	/**
	 * Collapses this {@link SubContext} and all its super-contexts into a
	 * single {@link Context}. The collapsed context is equivalent to this
	 * sub-context but is not an instance of {@link SubContext}.
	 * 
	 * @return a new {@link Context} that is not a {@link SubContext} and
	 *         contains all the mappings specified by this {@link SubContext}
	 *         and its ancestors, with the sub-context mappings take precedence
	 *         (overwriting) those of the parent
	 */
	@Override
	Context collapse() {
		Context superCollapsed = superContext.collapse();
		Map<SymbolicExpression, SymbolicExpression> map1 = new TreeMap<>(
				util.universe.comparator());

		map1.putAll(superCollapsed.subMap);
		map1.putAll(subMap);

		WorkMap<Monic, Range> map2 = new WorkMap<>(
				util.idealFactory.monicComparator());

		map2.putAll(superCollapsed.rangeMap);
		map2.putAll(rangeMap);

		Context collapse = new Context(util, map1, map2, this.backwardsSub);

		return collapse;
	}

	@Override
	Context collapsedClone() {
		return collapse();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * The change here over the super-method is that the precise {@link Range}s
	 * on {@link Monic}s are expanded in order to keep expressions involving
	 * them in a simple form. This means that if a {@link Context} says 1<=x<=9,
	 * then simplifying the expression x<=5 will result in x<=5 rather than
	 * 1<=x<=5. Similarly, 0<=x<=5 and -10<=x<=5 will all be simplified to the
	 * same expression, x<=5.
	 * </p>
	 */
	@Override
	BooleanExpression getAssumption(boolean full) {
		BooleanExpression result = util.trueExpr;

		for (Entry<SymbolicExpression, SymbolicExpression> subEntry : subMap
				.entrySet()) {
			SymbolicExpression key = subEntry.getKey();

			if (full || !(key instanceof SymbolicConstant))
				result = util.universe.and(result,
						util.universe.equals(key, subEntry.getValue()));
		}
		for (Entry<Monic, Range> rangeEntry : rangeMap.entrySet()) {
			Monic monic = rangeEntry.getKey();
			Range range = rangeEntry.getValue();
			// the following is an over-estimate of the range of monic:
			Range contextRange = superContext.computeRange(monic);

			if (!contextRange.isUniversal()) {
				Range expansion = util.rangeFactory.expand(range, contextRange);

				if (debug && expansion != range)
					out.println("Range for " + monic + " expanded from " + range
							+ " to " + expansion);
				range = expansion;
			}
			result = util.universe.and(result,
					range.symbolicRepresentation(monic, util.universe));
		}
		return result;
	}

	// Public methods...

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * Looks first in this sub-context for an entry in the sub map for the given
	 * key. If none is found, then looks in the super-context.
	 * </p>
	 * 
	 * @return the simplified expression that should replace {@code key}, or
	 *         {@code null}
	 */
	@Override
	public SymbolicExpression getSub(SymbolicExpression key) {
		SymbolicExpression result = super.getSub(key);

		if (result != null)
			return result;
		result = superContext.getSub(key);
		return result;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * For this sub-context, a form of "relative" Gaussian elimination is
	 * performed. The linear equalities of this sub-context are simplified using
	 * the information from the super-context before ordinary Gaussian
	 * elimination is performed.
	 * </p>
	 * 
	 * @return a linear solver based on relative Gaussian elimination that can
	 *         be used to simplify the substitution map of this sub-context
	 *         assuming all substitutions in the super context
	 */
	@Override
	public LinearSolver getLinearSolver() {
		if (subMap.isEmpty())
			return null;

		Map<SymbolicExpression, SymbolicExpression> superSubMap = superContext
				.getFullSubMap();
		LinearSolver ls = LinearSolver.reduceRelative(util, superSubMap, subMap,
				util.monicComparator, backwardsSub);

		return ls;
	}

	@Override
	public SymbolicExpression simplify(SymbolicExpression expr) {
		// note: collapsing is too slow and doesn't seem to make any difference
		return new IdealSimplifierWorker(this /* .collapse() */,
				simplificationStack).simplifyExpression(expr);
	}

	public Context getGlobalContext() {
		return superContext.getGlobalContext();
	}
}
