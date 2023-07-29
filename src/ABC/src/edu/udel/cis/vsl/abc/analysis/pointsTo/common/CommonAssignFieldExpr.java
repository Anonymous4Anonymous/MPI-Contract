package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignFieldExprIF;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAssignFieldExpr extends CommonAssignExpr
		implements
			AssignFieldExprIF {

	private Field field;

	private AssignExprIF struct;

	CommonAssignFieldExpr(int id, Type type, AssignExprIF struct, Field field) {
		super(id, type);
		this.struct = struct;
		this.field = field;
		assert type != null;
		assert struct != null;
		assert field != null;
	}

	@Override
	public AssignExprIF struct() {
		return struct;
	}

	@Override
	public AssignExprKind kind() {
		return AssignExprKind.FIELD;
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public Field field() {
		return field;
	}

	@Override
	public AssignExprIF root() {
		return this.struct.root();
	}

	@Override
	public String toString() {
		String str = struct.toString();

		if (struct.kind() == AssignExprKind.OFFSET)
			str = "(" + str + ")";
		return str + "." + field.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonAssignFieldExpr) {
			CommonAssignFieldExpr that = (CommonAssignFieldExpr) obj;

			return that.type().equals(this.type()) && that.struct.equals(struct)
					&& that.field() == this.field;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return ((this.type().hashCode() * this.struct.hashCode()) / 17 + 1)
				* this.field.hashCode();
	}

	@Override
	public boolean mayEquals(AssignExprIF o) {
		if (o.kind() == AssignExprKind.FIELD) {
			AssignFieldExprIF other = (AssignFieldExprIF) o;

			if (other.field() == this.field)
				return other.struct().mayEquals(struct);
		}
		return false;
	}
}
