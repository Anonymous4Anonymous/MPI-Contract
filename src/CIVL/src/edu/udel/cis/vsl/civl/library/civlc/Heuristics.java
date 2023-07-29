package edu.udel.cis.vsl.civl.library.civlc;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public class Heuristics {

	private SymbolicUniverse universe;
	static double overhead = 0;

	public static class Query {
		public final BooleanExpression context;
		public final BooleanExpression query;

		public Query(BooleanExpression context, BooleanExpression query) {
			this.context = context;
			this.query = query;
		}
	}

	public Heuristics(SymbolicUniverse universe) {
		this.universe = universe;
	}

	public Query applyHeuristicSimplifications(BooleanExpression context,
			BooleanExpression predicate) {
		if (!predicate.isTrue()) {
			SubArrayEquivalenceReplace subArrayReplacer = new SubArrayEquivalenceReplace(
					universe);

			Query query = subArrayReplacer.apply(context, predicate);
			
			if (query.context != context || query.query != predicate) {
				BoundVariableNormalization boundVarsNormalizer = new BoundVariableNormalization(
						universe);

				predicate = boundVarsNormalizer.apply(query.query);
				context = boundVarsNormalizer.apply(query.context);
			}
			
			UnaryOperator<SymbolicExpression> simplifier = new SteppedUniversalCombination(
					universe);

			context = (BooleanExpression) simplifier.apply(context);
			predicate = (BooleanExpression) simplifier.apply(predicate);

			simplifier = new ConditionalSimplification(universe);
			context = (BooleanExpression) simplifier.apply(context);
			predicate = (BooleanExpression) simplifier.apply(predicate);
		}
		return new Query(context, predicate);
	}
}
