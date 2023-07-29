package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LambdaExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLFunctionType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

/**
 * @author Manchun Zheng (zxxxx)
 * 
 */
public class CommonLambdaExpression extends CommonExpression
		implements
			LambdaExpression {
	private Expression expression;
	private Variable freeVariable;

	/**
	 * creates a new array lambda expression
	 * 
	 * @param source
	 *            The source file information for this expression.
	 * @param type
	 *            the type of this lambda
	 * @param boundVariableList
	 *            The list of bound variables and their domains (optional).
	 * @param restriction
	 *            The restriction on the bound variables
	 * @param expression
	 *            The body expression.
	 */
	public CommonLambdaExpression(CIVLSource source, CIVLFunctionType type,
			Variable freeVariable, Expression expression) {
		super(source, expression.expressionScope(), expression.lowestScope(),
				type);
		assert (type.typeKind() == CIVLType.TypeKind.FUNCTION);
		this.freeVariable = freeVariable;
		this.expression = expression;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.LAMBDA;
	}

	@Override
	public Variable freeVariable() {
		return this.freeVariable;
	}

	@Override
	public Expression lambdaFunction() {
		return expression;
	}

	@Override
	public String toString() {
		String result = "$lambda (";

		result += freeVariable;
		result += ") ";
		result += expression.toString();
		return result;
	}

	@Override
	public void replaceWith(ConditionalExpression oldExpression,
			VariableExpression newExpression) {
		if (expression == oldExpression) {
			expression = newExpression;
			return;
		}
		expression.replaceWith(oldExpression, newExpression);
	}

	@Override
	public Expression replaceWith(ConditionalExpression oldExpression,
			Expression newExpression) {
		CommonLambdaExpression result = null;
		Expression newExpressionField = expression.replaceWith(oldExpression,
				newExpression);

		if (newExpressionField != null)
			result = new CommonLambdaExpression(this.getSource(),
					(CIVLFunctionType) this.expressionType, this.freeVariable,
					newExpressionField);
		return result;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		Set<Variable> variableSet = new HashSet<>();
		Set<Variable> operandResult;

		operandResult = expression.variableAddressedOf(scope);
		if (operandResult != null)
			variableSet.addAll(operandResult);
		return variableSet;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		Set<Variable> variableSet = new HashSet<>();
		Set<Variable> operandResult;

		operandResult = expression.variableAddressedOf();
		if (operandResult != null)
			variableSet.addAll(operandResult);
		return variableSet;
	}

	@Override
	protected boolean expressionEquals(Expression expression) {
		CommonLambdaExpression that = (CommonLambdaExpression) expression;

		return this.getExpressionType().equals(that.getExpressionType())
				&& this.freeVariable.equals(that.freeVariable)
				&& this.expression.equals(that.lambdaFunction());
	}

	@Override
	public boolean containsHere() {
		return expression.containsHere();
	}

	@Override
	public CIVLFunctionType getExpressionType() {
		return (CIVLFunctionType) this.expressionType;
	}
}
