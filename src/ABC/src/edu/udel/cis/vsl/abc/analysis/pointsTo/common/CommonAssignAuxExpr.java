package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignAuxExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAssignAuxExpr extends CommonAssignExpr
		implements
			AssignAuxExprIF {

	CommonAssignAuxExpr(int id, Type type) {
		super(id, type);
	}

	@Override
	public AssignExprKind kind() {
		return AssignExprKind.AUX;
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonAssignAuxExpr) {
			CommonAssignAuxExpr that = (CommonAssignAuxExpr) obj;

			return that.id() == id();
		}
		return false;
	}

	@Override
	public String toString() {
		return "aux_" + id();
	}

	@Override
	public int hashCode() {
		return this.id() * 13;
	}

	@Override
	public int id() {
		return super.id();
	}

	@Override
	public boolean mayEquals(AssignExprIF o) {
		return this == o;
	}
}
