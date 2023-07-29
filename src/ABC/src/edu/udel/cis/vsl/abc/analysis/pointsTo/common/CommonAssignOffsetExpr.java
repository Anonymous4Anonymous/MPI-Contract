package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAssignOffsetExpr extends CommonAssignExpr
		implements
			AssignOffsetExprIF {

	private AssignExprIF base;

	private AssignOffsetIF offset;

	CommonAssignOffsetExpr(int id, Type type, AssignExprIF base,
			AssignOffsetIF offset) {
		super(id, type);
		this.base = base;
		this.offset = offset;
		assert base != null;
		assert offset != null;
		assert type != null;
	}

	@Override
	public AssignExprIF base() {
		return base;
	}

	@Override
	public AssignOffsetIF offset() {
		return offset;
	}

	@Override
	public AssignExprKind kind() {
		return AssignExprKind.OFFSET;
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public AssignExprIF root() {
		return this.base.root();
	}

	@Override
	public String toString() {
		return base.toString() + " + " + offset.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonAssignOffsetExpr) {
			CommonAssignOffsetExpr that = (CommonAssignOffsetExpr) obj;

			return that.type().equals(this.type()) && that.base().equals(base)
					&& that.offset().equals(offset);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((this.type().hashCode() * this.base.hashCode()) / 31 + 1)
				* this.offset.hashCode();
	}

	@Override
	public boolean mayEquals(AssignExprIF o) {
		if (o.kind() == AssignExprKind.OFFSET) {
			AssignOffsetExprIF other = (AssignOffsetExprIF) o;

			if (this.base.mayEquals(other.base()))
				if (this.offset.hasConstantValue())
					return other.offset().hasConstantValue()
							? offset.constantValue()
									.equals(other.offset().constantValue())
							: true;
				else
					return true;
			else
				return false;
		}
		return this.base.mayEquals(o);
	}
}
