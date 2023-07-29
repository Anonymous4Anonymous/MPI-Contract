package edu.udel.cis.vsl.sarl.simplify.norm;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.expr.IF.BooleanExpressionFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.simplifier.Context;
import edu.udel.cis.vsl.sarl.simplify.simplifier.ContextExtractor;
import edu.udel.cis.vsl.sarl.simplify.simplifier.InconsistentContextException;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SimplifierUtility;

public class MultiOrNormalizer implements Normalizer {

	public static boolean debug = false;

	public final static PrintStream out = System.out;

	/**
	 * The context being simplified.
	 */
	private Context context;

	/**
	 * A reference to {@link #PreUniverse}
	 */
	private PreUniverse universe;

	private BooleanExpressionFactory booleanFactory;

	private BooleanExpression trueExpr;

	public MultiOrNormalizer(Context context) {
		this.context = context;
		this.universe = context.getInfo().getUniverse();
		this.booleanFactory = context.getInfo().getBooleanFactory();
		this.trueExpr = booleanFactory.trueExpr();
	}

	/**
	 * A structured representation of an or-expression which occurs in the
	 * substitution map (as mapped to true), and which is part of a collection
	 * of such entries that share a common factor.
	 */
	private class StructuredOrClause {
		/**
		 * The whole or-expression, including the common part.
		 */
		BooleanExpression wholeExpression;

		/**
		 * The clauses of the or-expression that remain after removing the
		 * common part.
		 */
		BooleanExpression remainingExpression;
	}

	/**
	 * A structured representation of a set of entries in the substitution map,
	 * each of which is an or-expression mapping to true, and which share some
	 * non-empty common set of clauses.
	 */
	private class FactoredOrSection {
		/**
		 * The common part shared by all entries.
		 */
		BooleanExpression commonPart;

		/**
		 * The entries, each in a structured form.
		 */
		StructuredOrClause[] clauses;
	}

	/**
	 * Produces a {@link FactoredOrSection} based on a list of "or" expressions.
	 * This method will find the greatest common factor of the or expressions
	 * (i.e., the set of clauses which is the intersection of the set of clauses
	 * of each or expression). And for each or expression, it will construct the
	 * expression obtained by removing those common clauses.
	 * 
	 * @param orExprs
	 *            a list of expressions, each with operator
	 *            {@link SymbolicOperator#OR}
	 * @return the factored or section determined by those expressions
	 */
	private FactoredOrSection makeFactoredOrSection(
			List<BooleanExpression> orExprs) {
		FactoredOrSection result = new FactoredOrSection();
		int n = orExprs.size();
		BooleanExpression[] orArray = orExprs.toArray(new BooleanExpression[n]);
		Iterator<BooleanExpression> orExprIter = orExprs.iterator();

		result.commonPart = booleanFactory.factorOrs(orArray);
		result.clauses = new StructuredOrClause[n];
		for (int i = 0; i < n; i++) {
			StructuredOrClause soc = new StructuredOrClause();

			soc.wholeExpression = orExprIter.next();
			soc.remainingExpression = orArray[i];
			result.clauses[i] = soc;
		}
		return result;
	}

	/**
	 * Carries out the modification to the context required by a multi-or
	 * reduction on a given set of or entries.
	 * 
	 * @param fos
	 *            a structure representation of a set of or-expressions which
	 *            are in the context and have some non-trivial set of common
	 *            clauses
	 * @param dirt
	 *            the symbolic constants which become dirty in the process of
	 *            executing this method will be added to this set
	 * @return {@code true} iff a change is made to the context
	 * @throws InconsistentContextException
	 *             if an inconsistency in the context is discovered in the
	 *             process of executing this method
	 */
	private boolean executeOrSimplification(FactoredOrSection fos,
			Set<SymbolicConstant> dirt) throws InconsistentContextException {
		ContextExtractor extractor = new ContextExtractor(context, dirt);

		// remove all the relevant or clauses from the context...
		for (StructuredOrClause soc : fos.clauses) {
			context.removeSubkey(soc.wholeExpression);
		}

		// let r0 be the conjunction of the "remaining parts" of those
		// or clauses. E.g., if the clauses are p||q1 and p||q2,
		// then r0 = q1 && q2...
		BooleanExpression r0 = trueExpr;

		for (StructuredOrClause soc : fos.clauses)
			r0 = universe.and(r0, soc.remainingExpression);

		// Try to simplify r0...
		// TODO: consider assuming !commonPart in a sub-context first
		BooleanExpression r1 = (BooleanExpression) context.simplify(r0);

		if (r0 == r1) {
			// put them back, nothing is dirty
			for (StructuredOrClause soc : fos.clauses)
				context.putSub(soc.wholeExpression, trueExpr);
			return false;
		} else { // a change has occurred
			if (debug) {
				out.println("MultiOrReduction: replacing...");
				out.println("   " + r0);
				out.println("with...");
				out.println("   " + r1);
			}

			// let r2 = p || simplify(q1&&q2) ...
			BooleanExpression r2 = universe.or(fos.commonPart, r1);

			extractor.extractCNF(r2);
			return true;
		}
	}

	/**
	 * Returns the list of all or-expressions in the given {@link Collection}
	 * that contain the given {@link clause}.
	 * 
	 * @param orExpressions
	 *            a set of expressions in which the operator is
	 *            {@link SymbolicOperator#OR}.
	 * @param clause
	 *            a boolean expression, the clause to look for
	 * @return the list of all expressions in the collection containing
	 *         {@code clause} as a clause
	 */
	private LinkedList<BooleanExpression> orExpressionsContainingClause(
			Collection<BooleanExpression> orExpressions,
			BooleanExpression clause) {
		LinkedList<BooleanExpression> result = new LinkedList<>();

		for (BooleanExpression orExpr : orExpressions) {
			if (booleanFactory.containsArgument(orExpr, clause))
				result.add(orExpr);
		}
		return result;
	}

	/**
	 * Carries out at most one instance of a multi-or reduction. It will try all
	 * possible multi-or reductions, stopping after the first one that results
	 * in a change to the context, or after all possibilities are exhausted.
	 * 
	 * @param dirtIn
	 *            the current set of dirty variables; context clauses which do
	 *            not involve those variables cannot be impacted by any change
	 *            since the last normalization
	 * @param dirtOut
	 *            the variables that are made dirty by this normalization will
	 *            be added to this set
	 * @return {@code true} iff this method results in a change to the context
	 * @throws InconsistentContextException
	 *             if in the process of carrying out this normalization, an
	 *             inconsistency is discovered in the context
	 */
	private boolean orAttempt(Set<SymbolicConstant> dirtIn,
			Set<SymbolicConstant> dirtOut) throws InconsistentContextException {
		LinkedList<BooleanExpression> orExpressions = new LinkedList<>();
		LinkedList<BooleanExpression> dirtyOrExpressions = new LinkedList<>();
		Set<BooleanExpression> clauses = new HashSet<>();
		Set<List<BooleanExpression>> orSets = new HashSet<>();

		for (Entry<SymbolicExpression, SymbolicExpression> entry : context
				.getSubEntries()) {
			if (entry.getValue().isTrue()
					&& entry.getKey().operator() == SymbolicOperator.OR) {
				BooleanExpression orExpr = (BooleanExpression) entry.getKey();

				orExpressions.add(orExpr);
				if (SimplifierUtility.intersects(orExpr, dirtIn))
					dirtyOrExpressions.add(orExpr);
			}
		}
		for (BooleanExpression orExpr : dirtyOrExpressions) {
			for (SymbolicObject obj : orExpr.getArguments()) {
				BooleanExpression clause = (BooleanExpression) obj;

				if (clauses.add(clause)) {
					LinkedList<BooleanExpression> orExprsContainingClause = orExpressionsContainingClause(
							orExpressions, clause);

					if (orExprsContainingClause.size() > 1
							&& orSets.add(orExprsContainingClause)) {
						FactoredOrSection fos = makeFactoredOrSection(
								orExprsContainingClause);

						if (executeOrSimplification(fos, dirtOut))
							return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Carry out multi-or reduction on the context until stabilization.
	 * 
	 * @param dirtIn
	 *            the current set of dirty variables; context clauses which do
	 *            not involve those variables cannot be impacted by any change
	 *            since the last normalization
	 * @param dirtOut
	 *            the variables that are made dirty by this normalization will
	 *            be added to this set
	 * @throws InconsistentContextException
	 *             if in the process of carrying out this normalization, an
	 *             inconsistency is discovered in the context
	 */
	@Override
	public void normalize(Set<SymbolicConstant> dirtyIn,
			Set<SymbolicConstant> dirtyOut)
			throws InconsistentContextException {
		Set<SymbolicConstant> dirt = SimplifierUtility.cloneDirtySet(dirtyIn);
		Set<SymbolicConstant> tmpDirt = SimplifierUtility.newDirtySet();

		while (orAttempt(dirt, tmpDirt)) {
			dirtyOut.addAll(tmpDirt);
			dirt = tmpDirt;
			tmpDirt = SimplifierUtility.newDirtySet();
		}

	}

}
