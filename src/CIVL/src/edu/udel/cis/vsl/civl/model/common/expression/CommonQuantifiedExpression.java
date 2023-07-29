/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.QuantifiedExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/**
 * @author zirkel
 * 
 */
public class CommonQuantifiedExpression extends CommonExpression
		implements QuantifiedExpression {

	private Quantifier quantifier;

	private Expression restriction;

	private Expression expression;

	private List<Pair<List<Variable>, Expression>> boundVariableList;

	private int numBoundVariables;

	/**
	 * @param source
	 *            The source file information for this expression.
	 * @param quantifier
	 *            The type of quantifier.
	 * @param boundVariableName
	 *            The name of the bound variable.
	 * @param boundVariableType
	 *            The type of the bound variable.
	 * @param restriction
	 *            The restriction on the quantified variable.
	 * @param expression
	 *            The quantified expression.
	 */
	public CommonQuantifiedExpression(CIVLSource source, Scope scope,
			CIVLType type, Quantifier quantifier,
			List<Pair<List<Variable>, Expression>> boundVariableList,
			Expression restriction, Expression expression) {
		super(source, scope, scope, type);
		this.quantifier = quantifier;
		this.boundVariableList = boundVariableList;
		this.restriction = restriction;
		this.expression = expression;
		numBoundVariables = 0;
		for (Pair<List<Variable>, Expression> sublist : boundVariableList) {
			numBoundVariables += sublist.left.size();
		}
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.QUANTIFIER;
	}

	@Override
	public Quantifier quantifier() {
		return quantifier;
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
		String result = "";
		boolean isFirstVariableSubList = true;

		switch (quantifier) {
		case EXISTS:
			result += "EXISTS";
			break;
		case FORALL:
			result += "FORALL";
			break;
		case UNIFORM:
			result += "UNIFORM";
			break;
		default:
			result += "UNKNOWN_QUANTIFIER";
			break;
		}
		result += "(";
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
		CommonQuantifiedExpression result = null;

		if (newRestriction != null) {
			result = new CommonQuantifiedExpression(this.getSource(),
					this.expressionScope(), this.expressionType, quantifier,
					this.boundVariableList, newRestriction, expression);
		} else {
			Expression newExpressionField = expression
					.replaceWith(oldExpression, newExpression);

			if (newExpressionField != null)
				result = new CommonQuantifiedExpression(this.getSource(),
						this.expressionScope(), this.expressionType, quantifier,
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
		QuantifiedExpression that = (QuantifiedExpression) expression;

		return this.quantifier.equals(that.quantifier())
				&& this.boundVariableList.equals(that.boundVariableList())
				&& this.expression.equals(that.expression())
				&& this.restriction.equals(that.restriction());
	}

	@Override
	public boolean containsHere() {
		return restriction.containsHere() || expression.containsHere();
	}

	@Override
	public int numBoundVariables() {
		return this.numBoundVariables;
	}
}
