package edu.udel.cis.vsl.civl.model.IF.statement;

import edu.udel.cis.vsl.civl.model.IF.contract.LoopContract;

public interface LoopBranchStatement extends NoopStatement {
	/**
	 * Is this the loop enter or exit statement?
	 * 
	 * @return True iff this is the loop enter branch.
	 */
	boolean isEnter();

	/**
	 * Returns true if and only if this loop is attached with
	 * {@link LoopContract}
	 * 
	 * @return
	 */
	boolean isContracted();

	/**
	 * Returns an instance of {@link LoopContract} if {@link #isContracted()}
	 * returns true, otherwise, returns null.
	 * 
	 * @return
	 */
	LoopContract getLoopContract();
}
