package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

/**
 * <p>
 * the base, kindless implementation of {@link AssignExprIF} that will be
 * extended by implementations of AssignExprIF of different kinds. Recall that a
 * AssignExprIF of no kind represents the FULL.
 * </p>
 * 
 * @author xxxx
 *
 */
public class CommonAssignExpr implements AssignExprIF {

	private Type type;

	private int id;

	CommonAssignExpr(int id, Type type) {
		this.type = type;
		this.id = id;
	}

	@Override
	public AssignExprKind kind() {
		return null;
	}

	@Override
	public boolean isFull() {
		return true;
	}

	@Override
	public Type type() {
		return type;
	}

	@Override
	public AssignExprIF root() {
		return this;
	}

	@Override
	public String toString() {
		return "FULL";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonAssignExpr)
			return ((CommonAssignExpr) obj).isFull();
		else
			return false;
	}

	@Override
	public int hashCode() {
		return 0;
	}

	protected int id() {
		return id;
	}

	@Override
	public boolean mayEquals(AssignExprIF o) {
		return o.isFull();
	}

}
