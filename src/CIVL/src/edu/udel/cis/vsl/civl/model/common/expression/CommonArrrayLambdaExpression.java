package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ArrayLambdaExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLCompleteArrayType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/**
 * @author Manchun Zheng (zxxxx)
 * 
 */
public class CommonArrrayLambdaExpression extends CommonExpression
		implements
			ArrayLambdaExpression {
	private Expression restriction;
	private Expression expression;
	private List<Pair<List<Variable>, Expression>> boundVariableList;

	/**
	 * creates a new array lambda expression
	 * 
	 * @param source
	 *            The source file information for this expression.
	 * @param scope
	 *            he scope of this expression
	 * @param type
	 *            the type of this array lambda
	 * @param boundVariableList
	 *            The list of bound variables and their domains (optional).
	 * @param restriction
	 *            The restriction on the bound variables
	 * @param expression
	 *            The body expression.
	 */
	public CommonArrrayLambdaExpression(CIVLSource source, Scope scope,
			CIVLType type,
			List<Pair<List<Variable>, Expression>> boundVariableList,
			Expression restriction, Expression expression) {
		super(source, scope, scope, type);
		this.boundVariableList = boundVariableList;
		this.restriction = restriction;
		this.expression = expression;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.ARRAY_LAMBDA;
	}

	@Override
	public List<Pair<List<Variable>, Expression>> boundVariableList() {
		return this.boundVariableList;
	}

	@Override
	public Expression restriction() {
		return restriction;
	}

	@Override
	public Expression expression() {
		return expression;
	}

	@Override
	public String toString() {
		String result = "(";
		boolean isFirstVariableSubList = true;

		result += this.getExpressionType();
		result += ") $lambda (";
		for (Pair<List<Variable>, Expression> variableSubList : this.boundVariableList) {
			boolean isFirstVariable = true;

			if (isFirstVariableSubList)
				isFirstVariableSubList = false;
			else
				result += "; ";
			for (Variable variable : variableSubList.left) {
				if (isFirstVariable) {
					result += variable.type() + " " + variable.name();
					isFirstVariable = false;
				} else {
					result += ", ";
					result += variable.name();
				}
				if (variableSubList.right != null) {
					result += ": ";
					result += variableSubList.right;
				}
			}

		}
		if (restriction != null) {
			result += " | ";
			result += restriction;
		}
		result += ") ";
		result += expression.toString();
		return result;
	}

	@Override
	public void replaceWith(ConditionalExpression oldExpression,
			VariableExpression newExpression) {
		if (restriction == oldExpression) {
			restriction = newExpression;
			return;
		}
		if (expression == oldExpression) {
			expression = newExpression;
			return;
		}
		restriction.replaceWith(oldExpression, newExpression);
		expression.replaceWith(oldExpression, newExpression);
	}

	@Override
	public Expression replaceWith(ConditionalExpression oldExpression,
			Expression newExpression) {
		Expression newRestriction = restriction.replaceWith(oldExpression,
				newExpression);
		CommonArrrayLambdaExpression result = null;

		if (newRestriction != null) {
			result = new CommonArrrayLambdaExpression(this.getSource(),
					this.expressionScope(), this.expressionType,
					this.boundVariableList, newRestriction, expression);
		} else {
			Expression newExpressionField = expression
					.replaceWith(oldExpression, newExpression);

			if (newExpressionField != null)
				result = new CommonArrrayLambdaExpression(this.getSource(),
						this.expressionScope(), this.expressionType,
						boundVariableList, restriction, newExpressionField);
		}
		return result;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		Set<Variable> variableSet = new HashSet<>();
		Set<Variable> operandResult;

		operandResult = this.restriction.variableAddressedOf(scope);
		if (operandResult != null)
			variableSet.addAll(operandResult);
		operandResult = expression.variableAddressedOf(scope);
		if (operandResult != null)
			variableSet.addAll(operandResult);
		return variableSet;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		Set<Variable> variableSet = new HashSet<>();
		Set<Variable> operandResult;

		operandResult = this.restriction.variableAddressedOf();
		if (operandResult != null)
			variableSet.addAll(operandResult);
		operandResult = expression.variableAddressedOf();
		if (operandResult != null)
			variableSet.addAll(operandResult);
		return variableSet;
	}

	@Override
	protected boolean expressionEquals(Expression expression) {
		ArrayLambdaExpression that = (ArrayLambdaExpression) expression;

		return this.getExpressionType().equals(that.getExpressionType())
				&& this.boundVariableList.equals(that.boundVariableList())
				&& this.expression.equals(that.expression())
				&& this.restriction.equals(that.restriction());
	}

	@Override
	public CIVLCompleteArrayType getExpressionType() {
		return (CIVLCompleteArrayType) expressionType;
	}

	@Override
	public boolean containsHere() {
		return restriction.containsHere() || expression.containsHere();
	}
}
