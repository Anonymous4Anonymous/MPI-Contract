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
package edu.udel.cis.vsl.sarl.IF;

import java.util.Map;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.IF.number.Number;

/**
 * <p>
 * A reasoner provides methods to simplify {@link SymbolicExpression}s and prove
 * or disprove certain theorems, all under an over-arching assumption known as
 * the "context". The context is a {@link BooleanExpression} which is assumed to
 * hold. The context cannot change after the instantiation of the
 * {@link Reasoner}.
 * </p>
 * 
 * <p>
 * Note that the context may be simplified to an equivalent form when the
 * reasoner is instantiated. Example: if the context used to create this
 * {@link Reasoner} was <code>N>=0 &&
 * N>=1</code>, the actual context may become simply <code>N>=1</code>.
 * </p>
 * 
 * @author xxxx
 */
public interface Reasoner {

	/**
	 * In the process of simplifying the initial context, this simplifier may
	 * have "solved" for some of the symbolic constants occurring in the
	 * context. This method returns a map in which the keys are those symbolic
	 * constants and the value associated to a key is the "solved" value. The
	 * solved value will be substituted for the symbolic constants in any
	 * expression given to the {@link #simplify} method of this simplifier.
	 * 
	 * @return a mapping from some symbolic constants occurring in original
	 *         context to their solved values
	 */
	Map<SymbolicConstant, SymbolicExpression> constantSubstitutionMap();

	/**
	 * Returns the reduced context associated to this Reasoner. This expression
	 * may differ from the original one used to create the Reasoner because it
	 * was simplified or put into a canonical form. Moreover, symbolic constants
	 * which have been "solved" may be removed from the context. (For the
	 * context with additional equations giving those solved values, use method
	 * {@link #getFullContext}). This context will not change after creation.
	 * 
	 * @return the reduced context associated to this Reasoner
	 */
	BooleanExpression getReducedContext();

	/**
	 * Returns the full context associated to this Reasoner. This expression may
	 * differ from the original one used to create the Reasoner because it was
	 * simplified or put into a canonical form. The full context includes
	 * equations where one side is a symbolic constant and the other is the
	 * solved value. (For the context without those equations, use method
	 * {@link #getReducedContext}). Hence the expression returned is equivalent
	 * to the original given expression.
	 * 
	 * This context will not change after creation.
	 * 
	 * @return the reduced context associated to this Reasoner
	 */
	BooleanExpression getFullContext();

	/**
	 * If the context can be represented as a simple interval constraint, i.e.,
	 * an expression of the form A <= x <= B, where A and B are concrete
	 * numbers, x is the given symbolic constant, and <= could be < in either
	 * case, this returns the interval [A,B] (or (A,B], or, ...). Else returns
	 * null.
	 * 
	 * @param symbolicConstant
	 *            the variable s around which the context may be expressed as an
	 *            interval constraint
	 * 
	 * @return the interval constraint or null
	 */
	Interval assumptionAsInterval(SymbolicConstant symbolicConstant);

	/**
	 * Returns an interval over-approximation of the given expression. It is
	 * guaranteed that under this reasoner's context, any value taken on by the
	 * expression must be contained in the interval returned. The interval
	 * returned will have the same type as the expression.
	 * 
	 * @param expr
	 *            the numeric expression (either integer or real type)
	 * @return an interval which contains all possible values the expression may
	 *         assume under the context of this reasoner
	 */
	Interval intervalApproximation(NumericExpression expr);

	/**
	 * <p>
	 * Simplify the given expression under the context. The simplified
	 * expression is guaranteed to be equivalent to the given one under the
	 * context. I.e., if p and q are the two expressions, and c is the context,
	 * then given any assignment of concrete values to symbolic constants for
	 * which c holds, p and q will evaluate to the same concrete value.
	 * </p>
	 * 
	 * <p>
	 * Note that the expression can have any type, including array, function,
	 * tuple, etc. The simplified expression may have a different type, but the
	 * new type is guaranteed to be equivalent to the original under the
	 * context. For example, if the given expression has type int[N] (array of
	 * int of length N), the simplified expression might have type int[3] (e.g.,
	 * if the context was "N=3").
	 * </p>
	 * 
	 * <p>
	 * Example:
	 * 
	 * <pre>
	 * given context: N>=0 && N>=1
	 * simplified context: N>=1
	 * result of simplify (N>=1) : true
	 * result of simplify (N>=2) : N>=2
	 * result of simplify (N<0)  : false
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param expression
	 *            any symbolic expression
	 * @return simplified version of the expression
	 */
	SymbolicExpression simplify(SymbolicExpression expression);

	/**
	 * Simplifies the given boolean expression. Result is same as that of
	 * {@link #simplify(SymbolicExpression)}, but this saves you the trouble of
	 * casting to {@link BooleanExpression}.
	 * 
	 * @param expression
	 *            any BooleanExpression
	 * @return simplified version of the expression
	 */
	BooleanExpression simplify(BooleanExpression expression);

	/**
	 * Simplifies the given numeric expression. Result is same as that of
	 * {@link #simplify(SymbolicExpression)}, but this saves you the trouble of
	 * casting to {@link NumericExpression}.
	 * 
	 * @param expression
	 *            any NumericExpression
	 * @return simplified version of the expression
	 */
	NumericExpression simplify(NumericExpression expression);

	/**
	 * <p>
	 * Attempts to determine whether the statement p(x)=>q(x) is a tautology.
	 * Here, p is the "assumption", q is the "predicate", and x stands for the
	 * set of all symbolic constants which occur in p or q.
	 * </p>
	 * 
	 * <p>
	 * A result of YES implies forall x.(p(x)=>q(x)). A result of NO implies
	 * nsat(p)||exists x.(p(x)&&!q(x)). Nothing can be concluded from a result
	 * of MAYBE.
	 * </p>
	 * 
	 * <p>
	 * nsat(p) means p is not satisfiable, i.e., forall x.!p(x), or equivalently
	 * !exists x.p(x). Note that if p is not satisfiable then any of the three
	 * possible results could be returned.
	 * </p>
	 * 
	 * <p>
	 * Consider a call to valid(true,q). If this returns YES then forall x.q(x)
	 * (i.e., q is a tautology). If it returns NO then exists x.!q(x) (i.e., q
	 * is not a tautology).
	 * </p>
	 * 
	 * <p>
	 * Consider a call to valid(true,!q). If this returns YES then q is not
	 * satisfiable. If it returns no, then q is satisfiable.
	 * </p>
	 */
	ValidityResult valid(BooleanExpression predicate);

	/**
	 * 
	 * <p>
	 * Attempts to determine whether the statement p(x) && q(x) is
	 * unsatisfiable. Here, p is the "context", q is the "predicate", and x
	 * stands for the set of all symbolic constants which occur in p or q.
	 * </p>
	 * 
	 * 
	 * <p>
	 * A result of type YES implies <code>forall x. !(p(x) && q(x))</code>. A
	 * result of NO implies <code>exists x. p(x)&&q(x)</code>. Nothing can be
	 * concluded from a result of MAYBE.
	 * </p>
	 * 
	 * @param predicate
	 *            the boolean expression q(x)
	 * @throws TheoremProverException
	 *             if something goes wrong with the automated theorem prover
	 *             during this call
	 * @return a {@link ValidityResult} whose type must satisfy the constraints
	 *         described above
	 */
	ValidityResult unsat(BooleanExpression predicate);

	/**
	 * <p>
	 * Attempts to determine whether p(x)=>q(x) is valid, and, if not, also
	 * returns a model (counter-example). The specification is exactly the same
	 * as for {@link #valid}, except that a {@link ValidityResult} is returned.
	 * This provides a method {@link ValidityResult#getResultType} that returns
	 * the result type, but also a method {@link ModelResult#getModel} that
	 * provides the model. That method will return null if the result type is
	 * YES or MAYBE. It may return null even if the result type is NO, either
	 * because the assumption is not satisfiable or a model could not be found
	 * for some reason.
	 * </p>
	 * 
	 * <p>
	 * If the model is non-null, it will be a map in which the key set consists
	 * of all the symbolic constants of non-function type that occur in the
	 * assumption or predicate. The value associated to a key will be a concrete
	 * symbolic expression.
	 * </p>
	 * 
	 * @param predicate
	 *            the predicate q(x)
	 * @throws TheoremProverException
	 *             if something goes wrong with the automated theorem prover
	 *             during this call
	 * @return a validity result as specified above
	 */
	ValidityResult validOrModel(BooleanExpression predicate);

	/**
	 * Equivalent to
	 * <code>valid(predicate).getResultType()==ResultType.YES</code>.
	 * 
	 * @param predicate
	 *            a boolean expression
	 * @return if <code>true</code> is returned, the predicate is valid; nothing
	 *         can be concluded if <code>false</code> is returned
	 */
	boolean isValid(BooleanExpression predicate);

	/**
	 * If the given expression can be reduced to a concrete numeric value using
	 * the context, returns that concrete value, else returns null.
	 * 
	 * @param expression
	 *            any numeric expression
	 * @return the concrete (Number) numeric value of that expression or null
	 */
	Number extractNumber(NumericExpression expression);

	/**
	 * <p>
	 * Attempts to prove a uniform "Big-O" claim. The claim has the following
	 * form:
	 * 
	 * <pre>
	 * lhs = O(h1^n1) + ... + O(hk^nk)
	 * </pre>
	 * 
	 * Here, lhs or "left hand side expression" is an expression of real type.
	 * The h1, ..., hk or "limit variables" are symbolic constants of real type,
	 * the variables that are tending towards 0. The n1, ..., nk the
	 * corresponding "orders" of the limit variables; they are are concrete
	 * nonnegative integers.
	 * </p>
	 * 
	 * <p>
	 * The lhs may involve the hi and also other free variables; typically these
	 * will be index variables for indexing into arrays. Assumptions on any of
	 * these can be included in the <code>constraint</code> argument.
	 * </p>
	 * 
	 * @param constraint
	 *            any additional constraint (beyond the context) you want to
	 *            assume when checking the claim; typically range constraints on
	 *            index variables
	 * @param lhs
	 *            the left hand side expression, an expression of real type
	 *            involving any or all of the symbolic constants mentioned, as
	 *            well as others
	 * @param limitVars
	 *            the limit variables; an array of length k
	 * @param orders
	 *            the orders of the limit variables; the length must be the same
	 *            as the length of <code>limitVars</code> (k)
	 * @return <code>true</code> if the O-claim can be proved. A
	 *         <code>false</code> result does not mean the O-claim is false, it
	 *         just means it could not be proved
	 */
	boolean checkBigOClaim(BooleanExpression constraint, NumericExpression lhs,
			NumericSymbolicConstant[] limitVars, int[] orders);
}
