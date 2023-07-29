package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

/**
 * <p>
 * An abstract representation of one of the following assignments:
 * <ul>
 * <li>BASE: U = &U</li>
 * <li>SIMPLE: U = U</li>
 * <li>COMPLEX_LD: *U = U</li>
 * <li>COMPLEX_RD: U = *U</li>
 * </ul>
 * where U is a {@link AssignExpIF} which represents an abstract object.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface AssignmentIF {

	/**
	 * <p>
	 * BASE: p = &a
	 * 
	 * SIMPLE: p = a
	 * 
	 * COMPLEX_LD: *p = a
	 *
	 * COMPLEX_RD: p = *a
	 * </p>
	 */
	public static enum AssignmentKind {
		BASE, SIMPLE, COMPLEX_LD, COMPLEX_RD
	}

	/**
	 * @return: the left-hand side abstraction
	 * 
	 */
	public AssignExprIF lhs();

	/**
	 * @return the right-hand side abstraction
	 * 
	 */
	public AssignExprIF rhs();

	/**
	 * @return kind
	 */
	public AssignmentKind kind();
}
