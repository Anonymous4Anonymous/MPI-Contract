/*******************************************************************************
 * Copyright (c) 2013 Stephen F. Siegel, University of Delaware.
 * 
 * This file is part of SARL.
 * 
 * SARL is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * SARL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SARL. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.udel.cis.vsl.sarl.expr.IF;

import java.util.Comparator;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.BooleanObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;

public interface BooleanExpressionFactory {

	/**
	 * The boolean factory needs a numeric expression factory in order to negate
	 * expressions like "a<b" or "a<=b".
	 * 
	 * @param numericFactory
	 */
	void setNumericExpressionFactory(
			NumericExpressionFactory numericExpressionFactory);

	/**
	 * Returns an expression, given the operator and an array of arguments
	 * 
	 * @param operator
	 *            A SymbolicOperator
	 * @param args
	 *            Array of arguments
	 * @return Returns a BooleanExpression
	 */
	BooleanExpression booleanExpression(SymbolicOperator operator,
			SymbolicObject... args);

	/**
	 * Returns a true BooleanExpression
	 * 
	 * @return BooleanExpression
	 */
	BooleanExpression trueExpr();

	/**
	 * Returns a false BooleanExpression
	 * 
	 * @return BooleanExpression
	 */
	BooleanExpression falseExpr();

	/**
	 * The symbolic expression wrapping the given boolean object (true or
	 * false).
	 */
	BooleanExpression symbolic(BooleanObject object);

	/**
	 * Short cut for symbolic(booleanObject(value)).
	 * 
	 * @param value
	 * @return symbolic expression wrapping boolean value
	 */
	BooleanExpression symbolic(boolean value);

	BooleanSymbolicConstant booleanSymbolicConstant(StringObject name);

	/**
	 * Returns a symbolic expression representing the conjunction of the two
	 * given arguments. Each argument must be non-null and have boolean type.
	 * 
	 * @param arg0
	 *            a symbolic expression of boolean type
	 * @param arg1
	 *            a symbolic expression of boolean type
	 * @return conjunction of arg0 and arg1
	 */
	BooleanExpression and(BooleanExpression arg0, BooleanExpression arg1);

	/**
	 * Returns a symbolic expression representing the disjunction of the two
	 * given arguments. Each argument must be non-null and have boolean type.
	 * 
	 * @param arg0
	 *            a symbolic expression of boolean type
	 * @param arg1
	 *            a symbolic expression of boolean type
	 * @return disjunction of arg0 and arg1
	 */
	BooleanExpression or(BooleanExpression arg0, BooleanExpression arg1);

	/**
	 * Returns a symbolic expression which represents the disjunction of the
	 * expressions in the given array args. Each expression in args must have
	 * boolean type. args must be non-null, and may have any length, including
	 * 0. If the length of args is 0, the resulting expression is equivalent to
	 * "false".
	 * 
	 * @param args
	 *            a sequence of expressions of boolean type
	 * @return the disjunction of the expressions in args
	 */
	BooleanExpression or(Iterable<? extends BooleanExpression> args);

	/**
	 * Returns a symbolic expression representing the logical negation of the
	 * given expression arg. arg must be non-null and have boolean type.
	 * 
	 * @param arg
	 *            a symbolic expression of boolean type
	 * @return negation of arg
	 */
	BooleanExpression not(BooleanExpression arg);

	// /**
	// * Returns a symbolic expression representing "p implies q", i.e., p=>q.
	// *
	// * @param arg0
	// * a symbolic expression of boolean type (p)
	// * @param arg1
	// * a symbolic expression of boolean type (q)
	// * @return p=>q
	// */
	// BooleanExpression implies(BooleanExpression arg0, BooleanExpression
	// arg1);
	//
	// /**
	// * Returns a symbolic expression representing "p is equivalent to q",
	// i.e.,
	// * p<=>q.
	// *
	// * @param arg0
	// * a symbolic expression of boolean type (p)
	// * @param arg1
	// * a symbolic expression of boolean type (q)
	// * @return p<=>q
	// */
	// BooleanExpression equiv(BooleanExpression arg0, BooleanExpression arg1);

	/**
	 * Returns the universally quantified expression forall(x).e.
	 * 
	 * @param boundVariable
	 *            the bound variable x
	 * @param predicate
	 *            the expression e (of boolean type)
	 * @return the expression forall(x).e
	 */
	BooleanExpression forall(SymbolicConstant boundVariable,
			BooleanExpression predicate);

	/**
	 * Returns the existentially quantified expression exists(x).e.
	 * 
	 * @param boundVariable
	 *            the bound variable x
	 * @param predicate
	 *            the expression e (of boolean type)
	 * @return the expression exists(x).e
	 */
	BooleanExpression exists(SymbolicConstant boundVariable,
			BooleanExpression predicate);

	/**
	 * Returns a {@link Comparator} on {@link BooleanExpression}s produced by
	 * this factory.
	 * 
	 * @return the {@link Comparator} for the boolean expressions produced by
	 *         this factory
	 */
	Comparator<BooleanExpression> getBooleanComparator();

	/**
	 * Initializes fields; should be called after comparators have been set and
	 * linked.
	 */
	void init();

	/**
	 * Is one of the arguments of the given expression equal to {@code arg}?
	 * 
	 * @param expression
	 *            a non-{@code null} boolean expression
	 * @param arg
	 *            a symbolic object
	 * @return {@code true} if and only if one of the arguments of the
	 *         expression equals {@code arg}
	 */
	boolean containsArgument(BooleanExpression expression, SymbolicObject arg);

	/**
	 * Given a {@link BooleanExpression} with operator
	 * {@link SymbolicOperator#AND}, {@link SymbolicOperator#OR}, or
	 * {@link SymbolicOperator#NOT}, this method returns the arguments of that
	 * operator as an array. The arguments occur in order, the same order as
	 * would be returned by invoking {@link SymbolicExpression#argument(int)}.
	 * The array returned should only be read, not modified. Any attempts to
	 * modify it will result in undefined behavior.
	 * 
	 * @param expression
	 *            a boolean expression with operator AND, OR, or NOT.
	 * @return the arguments of that expression
	 */
	BooleanExpression[] getArgumentsAsArray(BooleanExpression expression);

	/**
	 * Given an array of or-expressions (expressions with operation
	 * {@link SymbolicOperator#OR}), this method computes the
	 * "greatest common divisor" of those expressions and replaces each element
	 * of the array with the expressions that results from "dividing by" that
	 * gcd. NOTE this method modifies the given array.
	 * 
	 * @param orExpressions
	 *            an array of symbolic expressions with operator
	 *            {@link SymbolicOperator#OR}
	 * @return the expression that results by taking the "or" over all clauses
	 *         which occur in every one of the given or-expressions
	 */
	BooleanExpression factorOrs(BooleanExpression[] orExpressions);
}
