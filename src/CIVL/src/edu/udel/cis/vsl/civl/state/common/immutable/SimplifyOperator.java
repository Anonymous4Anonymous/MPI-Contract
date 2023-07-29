package edu.udel.cis.vsl.civl.state.common.immutable;

import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * Make the simplification, which is carried out by a {@link Reasoner}, a
 * {@link UnaryOperator<SymbolicExpression>}.
 * 
 * @author xxxx
 *
 */
public class SimplifyOperator implements UnaryOperator<SymbolicExpression> {

	private Reasoner simplifier;

	SimplifyOperator(Reasoner reasoner) {
		this.simplifier = reasoner;
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		return simplifier.simplify(x);
	}
}
