package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.StructOrUnionLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLStructOrUnionType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public class CommonStructOrUnionLiteralExpression extends CommonExpression
		implements
			StructOrUnionLiteralExpression {

	public CommonStructOrUnionLiteralExpression(CIVLSource source, Scope hscope,
			Scope lscope, CIVLType type, SymbolicExpression constantValue) {
		super(source, hscope, lscope, type);
		this.constantValue = constantValue;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.STRUCT_OR_UNION_LITERAL;
	}

	@Override
	public CIVLStructOrUnionType structOrUnionType() {
		assert this.expressionType instanceof CIVLStructOrUnionType;
		return (CIVLStructOrUnionType) this.expressionType;
	}

	@Override
	public String toString() {
		return "(" + getExpressionType() + ")" + "{" + constantValue + "}";
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
	public boolean isStruct() {
		return this.structOrUnionType().isStructType();
	}

	@Override
	public LiteralKind literalKind() {
		return LiteralKind.STRUCT_OR_UNION;
	}

	@Override
	protected boolean expressionEquals(Expression expression) {
		StructOrUnionLiteralExpression that = (StructOrUnionLiteralExpression) expression;

		if (that.getExpressionType().equals(this.getExpressionType()))
			return that.constantValue().equals(this.constantValue());
		return false;
	}

	@Override
	public void setLiteralConstantValue(SymbolicExpression value) {
		this.constantValue = value;
	}
}
