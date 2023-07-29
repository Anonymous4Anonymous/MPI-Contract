package edu.udel.cis.vsl.civl.library.civlc;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;

public class BoundVariableNormalization {
	private SymbolicUniverse universe;
	
	BoundVariableNormalization(SymbolicUniverse universe) {
		this.universe = universe;
	}
	
	BooleanExpression apply(BooleanExpression expr) {
		if (expr.operator() != SymbolicOperator.AND)
			return applyWorker(expr);

		List<BooleanExpression> cnfs = new LinkedList<>();
		boolean changed = false;

		for (SymbolicObject cnf : expr.getArguments()) {
			BooleanExpression newCnf = applyWorker((BooleanExpression) cnf);

			if (newCnf != cnf)
				changed = true;
			cnfs.add(newCnf);
		}
		if (!changed)
			return expr;
		return universe.and(cnfs);
	}
	
	private BooleanExpression applyWorker(BooleanExpression expr) {
		if (expr.operator() != SymbolicOperator.FORALL)
			return expr;

		SymbolicConstant bv = (SymbolicConstant) expr.argument(0);
		SymbolicExpression pred = (SymbolicExpression) expr.argument(1);
		Set<SymbolicConstant> freeVars = expr.getFreeVars();

		int counter = 0;
		SymbolicConstant replacee = universe.symbolicConstant(
				universe.stringObject("i" + counter++), bv.type());

		while (freeVars.contains(replacee)) {
			replacee = universe.symbolicConstant(
					universe.stringObject("i" + counter++),
					bv.type());
		}
		pred = universe.simpleSubstituter(bv, replacee).apply(pred);
		return universe.forall(replacee, (BooleanExpression) pred);
	}
}
