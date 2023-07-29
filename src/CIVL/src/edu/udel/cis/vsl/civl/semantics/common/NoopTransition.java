package edu.udel.cis.vsl.civl.semantics.common;

import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;

/**
 * A noop transition which represents a kind of transitions that either have no
 * statement needs to be executed or the associated statements are unnecessary
 * to be executed (e.g. $elaborate statements).
 * 
 * @author xxxx
 *
 */
public class NoopTransition extends CommonTransition implements Transition {

	public NoopTransition(int pid, BooleanExpression assumption,
			Statement statement, boolean symplifyState,
			AtomicLockAction atomicLockAction) {
		super(assumption, pid, statement, symplifyState, atomicLockAction);
	}

	@Override
	public TransitionKind transitionKind() {
		return TransitionKind.NOOP;
	}
}
