package edu.udel.cis.vsl.civl.model.common.statement;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;

/**
 * When translating a if-else block, we need to create a noop statement for the
 * if branch and for the (explicit or implicit) else branch. In order to have
 * more information about the transition, we create this class to extend
 * {@link CommonNoopStatement}. Currently, there is a flag to tell if it is the
 * if branching or the else branching statement.
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public class CommonIfElseBranchStatement extends CommonNoopStatement {

	/* *************************** Instance Fields ************************* */

	/**
	 * Mark this statement to be the if branch or else branch.
	 */
	private boolean isIfBranch;

	/* **************************** Constructors *************************** */

	/**
	 * 
	 * @param civlSource
	 *            The CIVL source of this statement
	 * @param source
	 *            The source location of this statement
	 * @param isTrue
	 *            true iff this is the if branching, else the else branching.
	 */
	public CommonIfElseBranchStatement(CIVLSource civlSource, Location source,
			Expression guard, boolean isIf) {
		super(civlSource, source, guard, null);
		source.setBinaryBranching(true);
		this.noopKind = NoopKind.IF_ELSE;
		this.isIfBranch = isIf;
		this.statementScope = guard.expressionScope();
	}

	/* ************************ Methods from Object ************************ */

	@Override
	public String toString() {
		if (isIfBranch) {
			return "TRUE_BRANCH_IF";
		} else
			return "FALSE_BRANCH_IF";
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			if (obj instanceof CommonIfElseBranchStatement) {
				CommonIfElseBranchStatement other = (CommonIfElseBranchStatement) obj;

				return isIfBranch == other.isIfBranch;
			}
		}
		return false;
	}
}
