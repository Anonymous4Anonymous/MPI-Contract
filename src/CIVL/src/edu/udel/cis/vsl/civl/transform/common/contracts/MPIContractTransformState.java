package edu.udel.cis.vsl.civl.transform.common.contracts;

/**
 * <p>
 * An immutable class for indicating the state of the MPI contract
 * transformation. A state of the MPI contract transformation indicates whether
 * the transformer currently is transforming a purely local contract or
 * collective contract, (collective) pre- or post- conditions, etc.
 * </p>
 * 
 * <p>
 * The reason to make it immutable is force the user to provide configurations
 * at everytime the state may change.
 * </p>
 * 
 * @author xxxx
 */
public class MPIContractTransformState {
	/**
	 * true iff now is transforming target function contract
	 */
	final private boolean isTarget;
	/**
	 * true iff now is transforming collective contract
	 */
	final private boolean isCollectiveBlock;

	/**
	 * true iff now is transforming a purely location function
	 */
	final private boolean isPurelyLocalFunction;

	/**
	 * true iff now is transforming precondition
	 */
	final private boolean isPrecondition;

	MPIContractTransformState(boolean isTarget, boolean isCollectiveBlock,
			boolean isPurelyLocalFunction, boolean isPrecondition) {
		this.isTarget = isTarget;
		this.isCollectiveBlock = isCollectiveBlock;
		this.isPrecondition = isPrecondition;
		this.isPurelyLocalFunction = isPurelyLocalFunction;
	}

	boolean isTarget() {
		return isTarget;
	}

	boolean isCollectiveBlock() {
		return isCollectiveBlock;
	}

	boolean isPurelyLocalFunction() {
		return this.isPurelyLocalFunction;
	}

	boolean isPrecondition() {
		return isPrecondition;
	}

	boolean isPostcondition() {
		return !isPrecondition;
	}

	/**
	 * @return True iff allocates memory space for valid expressions
	 */
	boolean allocation4valid() {
		return (isTarget && isPrecondition) || (!isTarget && !isPrecondition);
	}

	/**
	 * @return True iff allocates memory space for assigns clauses
	 */
	boolean allocation4assigns() {
		return !isTarget;
	}
}
