package edu.udel.cis.vsl.civl.model.common.statement;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.statement.UpdateStatement;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;

public class CommonUpdateStatement extends CommonStatement
		implements UpdateStatement {

	private Expression collator;

	private CIVLFunction function;

	private Expression[] arguments;

	public CommonUpdateStatement(CIVLSource source, Location sourceLoc,
			Expression guard, Expression collator, CallOrSpawnStatement call) {
		super(source, collator.expressionScope(), collator.lowestScope(),
				sourceLoc, guard);
		this.collator = collator;
	}

	public CommonUpdateStatement(CIVLSource source, Location sourceLoc,
			Expression guard, Expression collator, CIVLFunction function,
			Expression[] arguments) {
		super(source, collator.expressionScope(), collator.lowestScope(),
				sourceLoc, guard);
		this.collator = collator;
		this.function = function;
		this.arguments = arguments;
	}

	@Override
	public Statement replaceWith(ConditionalExpression oldExpression,
			Expression newExpression) {
		Expression newGuard = this.guard().replaceWith(oldExpression,
				newExpression), newCollator = collator;
		Expression[] newArgs = this.arguments;
		boolean hasNew = false;

		if (newGuard != null)
			hasNew = true;
		else
			newGuard = this.guard();
		if (!hasNew) {
			newCollator = collator.replaceWith(oldExpression, newExpression);
			if (newCollator != null)
				hasNew = true;
			else
				newCollator = collator;
		}
		if (!hasNew) {
			int numArgs = arguments.length;

			newArgs = new Expression[numArgs];
			for (int i = 0; i < numArgs; i++) {
				Expression newArg = arguments[i].replaceWith(oldExpression,
						newExpression);

				if (newArg != null) {
					hasNew = true;
					break;
				} else
					newArg = arguments[i];
				newArgs[i] = newArg;
			}
		}
		if (hasNew)
			return new CommonUpdateStatement(this.getSource(), this.source(),
					this.guard(), newCollator, function, newArgs);
		return null;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		Set<Variable> result = new HashSet<>();
		Set<Variable> subResult = collator.variableAddressedOf();

		if (subResult != null)
			result.addAll(subResult);
		for (Expression arg : arguments) {
			subResult = arg.variableAddressedOf(scope);
			if (subResult != null)
				result.addAll(subResult);
		}
		if (result.size() > 0)
			return result;
		return null;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		Set<Variable> result = new HashSet<>();
		Set<Variable> subResult = collator.variableAddressedOf();

		if (subResult != null)
			result.addAll(subResult);
		for (Expression arg : arguments) {
			subResult = arg.variableAddressedOf();
			if (subResult != null)
				result.addAll(subResult);
		}
		if (result.size() > 0)
			return result;
		return null;
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.UPDATE;
	}

	@Override
	public Expression collator() {
		return collator;
	}

	@Override
	public CallOrSpawnStatement call() {
		return null;
	}

	@Override
	protected void calculateConstantValueWork(SymbolicUniverse universe) {
		this.collator.calculateConstantValue(universe);
		for (Expression arg : arguments)
			arg.calculateConstantValue(universe);
	}

	@Override
	public CIVLFunction function() {
		return this.function;
	}

	@Override
	public Expression[] arguments() {
		return this.arguments;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		int numArgs = arguments.length;
		boolean isFirst = true;

		sb.append("$update (");
		sb.append(collator);
		sb.append(") ");
		sb.append(function.name().name());
		sb.append("(");
		for (int i = 0; i < numArgs; i++) {
			if (isFirst)
				isFirst = false;
			else
				sb.append(", ");
			sb.append(arguments[i]);
		}
		sb.append(")");
		return sb.toString();
	}
}
