package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignStoreExprIF;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonAssignStoreExpr extends CommonAssignExpr
		implements
			AssignStoreExprIF {

	private ExpressionNode store = null;

	private Variable variable = null;

	CommonAssignStoreExpr(int id, ExpressionNode store, Type type) {
		super(id, type);
		this.store = store;
		assert type != null;
		assert store != null;
	}

	CommonAssignStoreExpr(int id, Variable variable, Type type) {
		super(id, type);
		this.variable = variable;
		assert type != null;
		assert variable != null;
	}

	@Override
	public AssignExprKind kind() {
		return AssignExprKind.STORE;
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public ExpressionNode store() {
		return store;
	}

	@Override
	public String toString() {
		if (isAllocation())
			return store.prettyRepresentation().toString();
		else
			return variable.getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonAssignStoreExpr) {
			CommonAssignStoreExpr that = (CommonAssignStoreExpr) obj;

			if (isAllocation())
				return this.type().equals(that.type())
						&& this.store.equals(that.store);
			else
				return this.type().equals(that.type())
						&& this.variable.equals(that.variable);
		}
		return false;
	}

	@Override
	public boolean isAllocation() {
		return this.store != null;
	}

	@Override
	public Variable variable() {
		return variable;
	}

	@Override
	public int hashCode() {
		if (isAllocation())
			return this.store.hashCode() * this.type().hashCode() * 7;
		else
			return this.variable.hashCode() * this.type().hashCode() * 3;
	}

	@Override
	public boolean mayEquals(AssignExprIF o) {
		return equals(o);
	}
}
