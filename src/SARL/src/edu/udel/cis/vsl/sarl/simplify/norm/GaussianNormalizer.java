package edu.udel.cis.vsl.sarl.simplify.norm;

import java.io.PrintStream;
import java.util.Set;
import java.util.Map.Entry;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;
import edu.udel.cis.vsl.sarl.simplify.simplifier.LinearSolver;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SimplifierUtility;

/**
 * Performs Gaussian Elimination on the numeric entries of a {@link Context}'s
 * substitution map. Does not read or modify the context's range map.
 */
public class GaussianNormalizer implements Normalizer {

	public static boolean debug = false;

	public final static PrintStream out = System.out;

	/**
	 * The context being simplified.
	 */
	private Context context;

	public GaussianNormalizer(Context context) {
		this.context = context;
	}

	/**
	 * Does there exist an entry in the subMap with a monic or monomial key
	 * containing a symbolic constant in the given dirty set? If not, there is
	 * no need to re-do Gaussian elimination because nothing in the linear
	 * system could have changed since the last time you did it.
	 * 
	 * @param dirty
	 *            the set of symbolic constants that are "dirty"
	 * @return true iff there exist an entry in the subMap with a monic or
	 *         monomial key containing a symbolic constant in dirty
	 */
	private boolean linearChange(Set<SymbolicConstant> dirty) {
		for (Entry<SymbolicExpression, SymbolicExpression> entry : context
				.getSubEntries()) {
			SymbolicExpression key = entry.getKey();

			if ((key instanceof Monic || key instanceof Monomial)
					&& SimplifierUtility.intersects(key, dirty)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Performs Gaussian Elimination on the numeric entries of the
	 * {@link #subMap}.
	 * 
	 * Does not read or modify {@link #rangeMap}.
	 * 
	 * @throws InconsistentContextException
	 *             if an inconsistency is detected when modifying the
	 *             {@link #subMap}
	 */
	@Override
	public void normalize(Set<SymbolicConstant> dirtyIn,
			Set<SymbolicConstant> dirtyOut)
			throws InconsistentContextException {
		// TODO: change the monic comparator to one that orders
		// from least to greatest:
		// - symbolic constants
		// - array reads
		// ...
		// - function applications
		// - constants
		if (!linearChange(dirtyIn))
			return;

		LinearSolver ls = context.getLinearSolver();

		if (ls == null)
			return;
		if (!ls.isConsistent())
			throw new InconsistentContextException();
		if (!ls.hasChanged())
			return;
		for (SymbolicExpression key : ls.getKeysToRemove())
			context.removeSubkey(key);
		for (Entry<Monic, Monomial> entry : ls.getNewEntries())
			context.addSub(entry.getKey(), entry.getValue(), dirtyOut);
	}

}
