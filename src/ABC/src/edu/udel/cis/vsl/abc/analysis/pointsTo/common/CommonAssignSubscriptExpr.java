package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignSubscriptExprIF;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAssignSubscriptExpr extends CommonAssignExpr
		implements
			AssignSubscriptExprIF {

	private AssignExprIF array;

	private AssignOffsetIF index;

	CommonAssignSubscriptExpr(int id, Type type, AssignExprIF array,
			AssignOffsetIF index) {
		super(id, type);
		this.array = array;
		this.index = index;
		assert type != null;
		assert array != null;
		assert index != null;
	}

	@Override
	public AssignExprIF array() {
		return array;
	}

	@Override
	public AssignOffsetIF index() {
		return index;
	}

	@Override
	public AssignExprKind kind() {
		return AssignExprKind.SUBSCRIPT;
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public AssignExprIF root() {
		return this.array.root();
	}

	@Override
	public String toString() {
		String arrayStr = array.toString();
		String indexStr = index().toString();

		if (array.kind() == AssignExprKind.OFFSET)
			arrayStr = "(" + arrayStr + ")";

		return arrayStr + "[" + indexStr + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonAssignSubscriptExpr) {
			CommonAssignSubscriptExpr that = (CommonAssignSubscriptExpr) obj;

			return that.type().equals(this.type())
					&& that.array.equals(this.array)
					&& that.index.equals(this.index);
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = ((type().hashCode() * array.hashCode()) / 3 + 1)
				+ index.hashCode();

		return hashCode;
	}

	@Override
	public boolean mayEquals(AssignExprIF o) {
		if (o.kind() == AssignExprKind.SUBSCRIPT) {
			AssignSubscriptExprIF other = (AssignSubscriptExprIF) o;

			if (array.mayEquals(other.array())) {
				if (index.hasConstantValue())
					return other.index().hasConstantValue()
							? index.constantValue()
									.equals(other.index().constantValue())
							: true;
				else
					return true;
			}
		}
		return false;
	}
}
