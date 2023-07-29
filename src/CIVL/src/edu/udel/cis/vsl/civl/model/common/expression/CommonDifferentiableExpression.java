package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.AbstractFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.DifferentiableExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

public class CommonDifferentiableExpression extends CommonExpression
		implements DifferentiableExpression {

	private AbstractFunction function;

	private int degree;

	private Expression[] lowerBounds;

	private Expression[] upperBounds;

	public CommonDifferentiableExpression(CIVLSource source, Scope hscope,
			Scope lscope, CIVLType booleanType, AbstractFunction function,
			int degree, Expression[] lowerBounds, Expression[] upperBounds) {
		super(source, hscope, lscope, booleanType);
		this.function = function;
		this.degree = degree;
		this.lowerBounds = lowerBounds;
		this.upperBounds = upperBounds;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.DIFFERENTIABLE;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		// TODO : fix me
		// the union of the sets from lowerBounds and upperBounds?
		return new HashSet<>();
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		// TODO Auto-generated method stub
		return new HashSet<>();
	}

	@Override
	public int degree() {
		return degree;
	}

	@Override
	public Expression[] lowerBounds() {
		return lowerBounds;
	}

	@Override
	public Expression[] upperBounds() {
		return upperBounds;
	}

	@Override
	protected boolean expressionEquals(Expression expression) {
		// TODO check the fields...
		return expression == this;
	}

	@Override
	public AbstractFunction function() {
		return function;
	}

}
