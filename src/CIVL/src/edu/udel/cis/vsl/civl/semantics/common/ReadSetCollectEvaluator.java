package edu.udel.cis.vsl.civl.semantics.common;

import java.util.Set;
import java.util.TreeSet;

import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.expression.*;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * An evaluator that extends {@link CommonEvaluator} for collecting the read set
 * from the expression that is going to be evaluated.
 * @author xxxx
 */
public class ReadSetCollectEvaluator extends CommonEvaluator implements Evaluator {
    /**
     * a reference to {@link ReadSetAnalyzer}, which carries out the read-set
     * analysis work
     */
    private ReadSetAnalyzer readSetAnalyzer;

    /**
     * a reference to a {@link CommonEvaluator}, which carries out the
     * Evaluation work
     */
    private Evaluator superEvaluator;

    public ReadSetCollectEvaluator(CommonEvaluator superEvaluator) {
        super(superEvaluator.modelFactory, superEvaluator.stateFactory,
                superEvaluator.libLoader, superEvaluator.libExeLoader,
                superEvaluator.symbolicUtil, superEvaluator.symbolicAnalyzer,
                superEvaluator.memUnitFactory, superEvaluator.errorLogger,
                superEvaluator.civlConfig);
        this.superEvaluator = superEvaluator;
        ((CommonSymbolicAnalyzer)this.symbolicAnalyzer).setEvaluator(superEvaluator);
        readSetAnalyzer = new ReadSetAnalyzer(superEvaluator);
    }

    @Override
    public Evaluation evaluate(State state, int pid, Expression expression,
            boolean checkUndefinedValue)
            throws UnsatisfiablePathConditionException {
        Set<SymbolicExpression> readSets =
                readSetAnalyzer.analyze(expression, state, pid, false);
        Evaluation eval = superEvaluator
                .evaluate(state, pid, expression, checkUndefinedValue);

        for (SymbolicExpression memVal : readSets)
            eval.state = stateFactory
                    .addReadWriteRecords(eval.state, pid, memVal, true);
        return eval;
    }

    @Override
    public Evaluation reference(State state, int pid, LHSExpression operand)
            throws UnsatisfiablePathConditionException {
        Set<SymbolicExpression> readSets =
                readSetAnalyzer.analyze(operand, state, pid, false);
        Evaluation eval = superEvaluator.reference(state, pid, operand);

        for (SymbolicExpression memVal : readSets)
            eval.state = stateFactory
                    .addReadWriteRecords(eval.state, pid, memVal, true);
        return eval;
    }

    /**
     * Collect read set for the LHS expression.  The thing that is special is
     * that the memory location represented by the LHS expression itself is not
     * included in the collected set.  Returns the state where read set has
     * been collected.
     */
	public State collectForLHS(State state, int pid,
			LHSExpression lhsExpression)
			throws UnsatisfiablePathConditionException {
		Set<SymbolicExpression> readSets;

		switch (lhsExpression.lhsExpressionKind()) {
			case DEREFERENCE: {
				DereferenceExpression derefExpr =
						(DereferenceExpression) lhsExpression;
				readSets = readSetAnalyzer
						.analyze(derefExpr.pointer(), state, pid, false);
				break;
			}
			case DOT: {
				DotExpression dotExpr = (DotExpression) lhsExpression;

				readSets = readSetAnalyzer
						.analyze(dotExpr.structOrUnion(), state, pid, true);
				break;
			}
			case SUBSCRIPT: {
				SubscriptExpression subsExpr =
						(SubscriptExpression) lhsExpression;

				readSets = readSetAnalyzer
						.analyze(subsExpr.array(), state, pid, true);
				readSets.addAll(readSetAnalyzer
						.analyze(subsExpr.index(), state, pid, false));
				break;
			}
			case VARIABLE:
				readSets = new TreeSet<>(universe.comparator());
				break;
			default:
				throw new CIVLInternalException(
						"unknown LHS expression kind: " + lhsExpression
								.lhsExpressionKind(),
						lhsExpression.getSource());
		}
		for (SymbolicExpression memVal : readSets)
			state = stateFactory.addReadWriteRecords(state, pid, memVal, true);
		return state;
	}
}
