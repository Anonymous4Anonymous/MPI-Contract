package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.StatenullExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public class CommonStatenullExpression extends CommonExpression
		implements StatenullExpression {

	public CommonStatenullExpression(CIVLSource source, CIVLType type,
			SymbolicExpression constantValue) {
		super(source, null, null, type);
		this.constantValue = constantValue;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.STATE_NULL;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		return new HashSet<>();
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		return new HashSet<>();
	}

	@Override
	protected boolean expressionEquals(Expression expression) {
		return expression instanceof StatenullExpression;
	}

	@Override
	public String toString() {
		return "$state_null";
	}
}
