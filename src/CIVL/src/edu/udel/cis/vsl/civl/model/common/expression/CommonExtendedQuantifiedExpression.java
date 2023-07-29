package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ExtendedQuantifiedExpressionNode.ExtendedQuantifier;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.ExtendedQuantifiedExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

public class CommonExtendedQuantifiedExpression extends CommonExpression
		implements
			ExtendedQuantifiedExpression {
	private ExtendedQuantifier quantifier;
	private Expression lower;
	private Expression higher;
	private Expression function;

	public CommonExtendedQuantifiedExpression(CIVLSource source, CIVLType type,
			ExtendedQuantifier quant, Expression lo, Expression hi,
			Expression function) {
		super(source, null, null, type);
		this.quantifier = quant;
		this.lower = lo;
		this.higher = hi;
		this.function = function;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.EXTENDED_QUANTIFIER;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		Set<Variable> result = new HashSet<>(), subResult;

		subResult = lower.variableAddressedOf(scope);
		if (subResult != null)
			result = subResult;
		subResult = higher.variableAddressedOf(scope);
		if (subResult != null)
			result.addAll(subResult);
		if (result.isEmpty())
			return null;
		return result;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		Set<Variable> result = new HashSet<>(), subResult;

		subResult = lower.variableAddressedOf();
		if (subResult != null)
			result = subResult;
		subResult = higher.variableAddressedOf();
		if (subResult != null)
			result.addAll(subResult);
		if (result.isEmpty())
			return null;
		return result;
	}

	@Override
	public ExtendedQuantifier extendedQuantifier() {
		return this.quantifier;
	}

	@Override
	public Expression lower() {
		return this.lower;
	}

	@Override
	public Expression higher() {
		return this.higher;
	}

	@Override
	public Expression function() {
		return this.function;
	}

	@Override
	public String toString() {
		return quantifier + "(" + lower + ", " + higher + "," + function + ")";
	}

	@Override
	protected boolean expressionEquals(Expression expression) {
		if (expression instanceof ExtendedQuantifiedExpression) {
			ExtendedQuantifiedExpression that = (ExtendedQuantifiedExpression) expression;

			return this.quantifier == that.extendedQuantifier()
					&& this.lower.equals(that.lower())
					&& this.higher.equals(that.higher())
					&& this.function.equals(that.function());
		}
		return false;
	}

}
