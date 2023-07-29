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
package edu.udel.cis.vsl.sarl.expr.cnf;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.BooleanObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.expr.IF.BooleanExpressionFactory;
import edu.udel.cis.vsl.sarl.expr.IF.NumericExpressionFactory;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;
import edu.udel.cis.vsl.sarl.util.SetFactory;

/**
 * A CNF factory is an implementation of {@link BooleanExpressionFactory} that
 * works by putting all boolean expressions into a conjunctive normal form.
 * 
 * @author xxxx
 */
public class CnfFactory implements BooleanExpressionFactory {

	// Types ...

	/**
	 * A comparator on {@link CnfExpression}. This is needed to keep the
	 * arguments to AND and OR expressions sorted.
	 * 
	 * TODO: think about caching comparisons using an order rational number,
	 * similar to what is done with numeric Monics.
	 * 
	 * @author xxxx
	 */
	private class BooleanComparator implements Comparator<BooleanExpression> {

		private Comparator<SymbolicObject> objectComparator;

		BooleanComparator(Comparator<SymbolicObject> objectComparator) {
			this.objectComparator = objectComparator;
		}

		@Override
		public int compare(BooleanExpression o1, BooleanExpression o2) {
			int result = o1.operator().compareTo(o2.operator());

			if (result != 0)
				return result;

			int numArgs = o1.numArguments();

			result = numArgs - o2.numArguments();
			if (result != 0)
				return result;
			for (int i = 0; i < numArgs; i++) {
				result = objectComparator.compare(o1.argument(i),
						o2.argument(i));
				if (result != 0)
					return result;
			}
			return 0;
		}
	};

	private class BooleanSetFactory extends SetFactory<BooleanExpression> {

		BooleanSetFactory(Comparator<BooleanExpression> booleanComparator) {
			super(booleanComparator);
		}

		@Override
		protected BooleanExpression[] newSet(int size) {
			return new BooleanExpression[size];
		}
	};

	// Fields ...

	private ObjectFactory objectFactory;

	private SymbolicTypeFactory typeFactory;

	private NumericExpressionFactory numericExpressionFactory;

	private SymbolicType _booleanType;

	private BooleanExpression trueExpr, falseExpr;

	private Comparator<BooleanExpression> booleanComparator;

	private SetFactory<BooleanExpression> setFactory;

	// Constructors...

	public CnfFactory(SymbolicTypeFactory typeFactory,
			ObjectFactory objectFactory) {
		this.typeFactory = typeFactory;
		this.objectFactory = objectFactory;
		this.booleanComparator = new BooleanComparator(
				objectFactory.comparator());
		this.setFactory = new BooleanSetFactory(booleanComparator);
	}

	// The initializer...

	@Override
	public void init() {
		this._booleanType = typeFactory.booleanType();
		this.trueExpr = booleanExpression(SymbolicOperator.CONCRETE,
				new SymbolicObject[] { objectFactory.trueObj() });
		this.falseExpr = booleanExpression(SymbolicOperator.CONCRETE,
				new SymbolicObject[] { objectFactory.falseObj() });
	}

	// Helpers...

	/**
	 * Returns a symbolic set with one or two elements. If <code>x</code> and
	 * <code>y</code> are equal, this will return the singleton set containing
	 * the element; otherwise the set with exactly two elements.
	 * 
	 * @param x
	 *            one of the two elements, non-<code>null</code>
	 * @param y
	 *            the other element, non-<code>null</code>
	 * @return the set with members x and y
	 */
	private BooleanExpression[] theSet(BooleanExpression x,
			BooleanExpression y) {
		int c = booleanComparator.compare(x, y);

		if (c == 0)
			return new BooleanExpression[] { x };
		if (c < 0)
			return new BooleanExpression[] { x, y };
		return new BooleanExpression[] { y, x };
	}

	// Public functions specified in BooleanExpressionFactory...

	@Override
	public BooleanExpression booleanExpression(SymbolicOperator operator,
			SymbolicObject... args) {
		return objectFactory
				.canonic(new BooleanPrimitive(operator, _booleanType, args));
	}

	private CompoundBooleanExpression andExpr(BooleanExpression[] args) {
		return objectFactory.canonic(new CompoundBooleanExpression(
				SymbolicOperator.AND, _booleanType, args));
	}

	private CompoundBooleanExpression orExpr(BooleanExpression[] args) {
		return objectFactory.canonic(new CompoundBooleanExpression(
				SymbolicOperator.OR, _booleanType, args));
	}

	private CompoundBooleanExpression notExpr(BooleanExpression arg) {
		return objectFactory.canonic(new CompoundBooleanExpression(
				SymbolicOperator.NOT, _booleanType, arg));
	}

	@Override
	public BooleanSymbolicConstant booleanSymbolicConstant(StringObject name) {
		return objectFactory
				.canonic(new CnfSymbolicConstant(name, _booleanType));
	}

	@Override
	public BooleanExpression trueExpr() {
		return trueExpr;
	}

	@Override
	public BooleanExpression falseExpr() {
		return falseExpr;
	}

	@Override
	public BooleanExpression symbolic(BooleanObject object) {
		return object.getBoolean() ? trueExpr : falseExpr;
	}

	@Override
	public BooleanExpression symbolic(boolean value) {
		return value ? trueExpr : falseExpr;
	}

	private BooleanExpression[] args(BooleanExpression expr) {
		return ((CompoundBooleanExpression) expr).arguments();
	}

	@Override
	public BooleanExpression and(BooleanExpression arg0,
			BooleanExpression arg1) {
		if (arg0 == trueExpr)
			return arg1;
		if (arg1 == trueExpr)
			return arg0;
		if (arg0 == falseExpr || arg1 == falseExpr)
			return falseExpr;
		if (arg0.equals(arg1))
			return arg0;
		else {
			boolean isAnd0 = arg0.operator() == SymbolicOperator.AND;
			boolean isAnd1 = arg1.operator() == SymbolicOperator.AND;

			if (isAnd0 && isAnd1)
				return andExpr(setFactory.union(args(arg0), args(arg1)));
			if (isAnd0 && !isAnd1)
				return andExpr(setFactory.put(args(arg0), arg1));
			if (!isAnd0 && isAnd1)
				return andExpr(setFactory.put(args(arg1), arg0));
			return andExpr(theSet(arg0, arg1));
		}
	}

	@Override
	public BooleanExpression or(BooleanExpression c0, BooleanExpression c1) {
		if (c0 == trueExpr || c1 == trueExpr)
			return trueExpr;
		if (c0 == falseExpr)
			return c1;
		if (c1 == falseExpr)
			return c0;
		if (c0.equals(c1))
			return c1;

		SymbolicOperator op0 = c0.operator();
		SymbolicOperator op1 = c1.operator();

		if (op0 == SymbolicOperator.AND) {
			BooleanExpression result = trueExpr;

			if (op1 == SymbolicOperator.AND) {
				for (BooleanExpression clause0 : args(c0))
					for (BooleanExpression clause1 : args(c1))
						result = and(result, or(clause0, clause1));
			} else {
				for (BooleanExpression clause0 : args(c0))
					result = and(result, or(clause0, c1));
			}
			return result;
		}
		if (op1 == SymbolicOperator.AND) {
			BooleanExpression result = trueExpr;

			for (BooleanExpression clause1 : args(c1))
				result = and(result, or(c0, clause1));
			return result;
		}
		if (op0 == SymbolicOperator.OR && op1 == SymbolicOperator.OR) {
			BooleanExpression[] set0 = args(c0), set1 = args(c1);

			return orExpr(setFactory.union(set0, set1));
		}
		if (op0 == SymbolicOperator.OR) {
			BooleanExpression[] set0 = args(c0);

			return orExpr(setFactory.put(set0, c1));
		}
		if (op1 == SymbolicOperator.OR) {
			BooleanExpression[] set1 = args(c1);

			return orExpr(setFactory.put(set1, c0));
		}
		return orExpr(theSet(c0, c1));
	}

	/**
	 * Assume nothing about the list of args.
	 */
	@Override
	public BooleanExpression or(Iterable<? extends BooleanExpression> args) {
		BooleanExpression result = falseExpr;

		for (BooleanExpression arg : args)
			result = or(result, arg);
		return result;
	}

	private BooleanExpression and(Iterable<? extends BooleanExpression> args) {
		BooleanExpression result = trueExpr;

		for (BooleanExpression arg : args)
			result = and(result, arg);
		return result;
	}

	private BooleanExpression getNegation(BooleanExpression expr) {
		if (expr instanceof BooleanPrimitive)
			return ((BooleanPrimitive) expr).getNegation();
		return ((CompoundBooleanExpression) expr).getNegation();
	}

	private void setNegation(BooleanExpression e1, BooleanExpression e2) {
		if (e1 instanceof BooleanPrimitive)
			((BooleanPrimitive) e1).setNegation(e2);
		else
			((CompoundBooleanExpression) e1).setNegation(e2);
	}

	@Override
	public BooleanExpression not(BooleanExpression expr) {
		BooleanExpression result = getNegation(expr);

		if (result == null) {
			SymbolicOperator operator = expr.operator();

			switch (operator) {
			case CONCRETE: {
				BooleanObject value = (BooleanObject) expr.argument(0);
				boolean booleanValue = value.getBoolean();

				result = booleanValue ? falseExpr : trueExpr;
				break;
			}
			case AND:
				result = falseExpr;
				for (BooleanExpression clause : args(expr))
					result = (BooleanExpression) or(result, not(clause));
				break;
			case OR:
				result = trueExpr;
				for (BooleanExpression clause : args(expr))
					result = (BooleanExpression) and(result, not(clause));
				break;
			case NOT:
				result = (BooleanExpression) expr.argument(0);
				break;
			case FORALL:
				result = booleanExpression(SymbolicOperator.EXISTS,
						(SymbolicConstant) expr.argument(0),
						not((BooleanExpression) expr.argument(1)));
				break;
			case EXISTS:
				result = booleanExpression(SymbolicOperator.FORALL,
						(SymbolicConstant) expr.argument(0),
						not((BooleanExpression) expr.argument(1)));
				break;
			case EQUALS:
				result = booleanExpression(SymbolicOperator.NEQ,
						(SymbolicExpression) expr.argument(0),
						(SymbolicExpression) expr.argument(1));
				break;
			case NEQ:
				result = booleanExpression(SymbolicOperator.EQUALS,
						(SymbolicExpression) expr.argument(0),
						(SymbolicExpression) expr.argument(1));
				break;
			case LESS_THAN:
				result = numericExpressionFactory.notLessThan(
						(NumericExpression) expr.argument(0),
						(NumericExpression) expr.argument(1));
				break;
			case LESS_THAN_EQUALS:
				result = numericExpressionFactory.notLessThanEquals(
						(NumericExpression) expr.argument(0),
						(NumericExpression) expr.argument(1));
				break;
			default:
				result = notExpr(expr);
				break;
			}
			// cache the negation in bi-direction:
			setNegation(expr, result);
			setNegation(result, expr);
			// assert expr is trivial <-> result is trivial
			assert !(expr.isTrue() || expr.isFalse())
					|| (result.isTrue() || result.isFalse());
			assert (expr.isTrue() || expr.isFalse())
					|| !(result.isTrue() || result.isFalse());
		}
		return result;
	}

	@Override
	public BooleanExpression forall(SymbolicConstant boundVariable,
			BooleanExpression predicate) {
		// TODO: checking these special cases is now redundant,
		// since it is done in the PreUniverse. Remove them.
		// Update exists in the same way.
		if (predicate == trueExpr)
			return trueExpr;
		if (predicate == falseExpr)
			return falseExpr;

		SymbolicOperator op = predicate.operator();

		if (op == SymbolicOperator.AND) {
			BooleanExpression result = trueExpr;

			for (BooleanExpression clause : args(predicate))
				result = and(result, forall(boundVariable, clause));
			return result;
		}
		if (op == SymbolicOperator.OR) {
			// forall x . (p || q) is equiv. to p || forall x. q
			// if p does not involve x ...
			LinkedList<BooleanExpression> varClauses = new LinkedList<>(),
					constClauses = new LinkedList<>();

			for (BooleanExpression clause : args(predicate)) {
				if (clause.getFreeVars().contains(boundVariable))
					varClauses.add(clause);
				else
					constClauses.add(clause);
			}
			if (!constClauses.isEmpty()) {
				constClauses.add(forall(boundVariable, or(varClauses)));

				BooleanExpression result = or(constClauses);

				return result;
			}
		}
		return booleanExpression(SymbolicOperator.FORALL, boundVariable,
				predicate);
	}

	@Override
	public BooleanExpression exists(SymbolicConstant boundVariable,
			BooleanExpression predicate) {
		if (predicate == trueExpr)
			return trueExpr;
		if (predicate == falseExpr)
			return falseExpr;

		SymbolicOperator op = predicate.operator();

		if (op == SymbolicOperator.OR) {
			BooleanExpression result = falseExpr;

			for (BooleanExpression clause : args(predicate))
				result = or(result, exists(boundVariable, clause));
			return result;
		}
		if (op == SymbolicOperator.AND) {
			// exists x . (p && q) is equiv. to p && exists x. q
			// if p does not involve x ...
			LinkedList<BooleanExpression> varClauses = new LinkedList<>(),
					constClauses = new LinkedList<>();

			for (BooleanExpression clause : args(predicate)) {
				if (clause.getFreeVars().contains(boundVariable))
					varClauses.add(clause);
				else
					constClauses.add(clause);
			}
			if (!constClauses.isEmpty()) {
				constClauses.add(exists(boundVariable, and(varClauses)));

				BooleanExpression result = and(constClauses);

				return result;
			}
		}
		return booleanExpression(SymbolicOperator.EXISTS, boundVariable,
				predicate);
	}

	@Override
	public void setNumericExpressionFactory(
			NumericExpressionFactory numericExpressionFactory) {
		this.numericExpressionFactory = numericExpressionFactory;
	}

	@Override
	public Comparator<BooleanExpression> getBooleanComparator() {
		return booleanComparator;
	}

	@Override
	public boolean containsArgument(BooleanExpression expression,
			SymbolicObject arg) {
		if (expression instanceof CompoundBooleanExpression)
			return arg instanceof BooleanExpression ? setFactory.contains(
					((CompoundBooleanExpression) expression).arguments(),
					(BooleanExpression) arg) : false;
		if (expression instanceof CnfSymbolicConstant) {
			return arg == ((CnfSymbolicConstant) expression).argument(0);
		}
		throw new SARLInternalException("unreachable");
	}

	@Override
	public BooleanExpression[] getArgumentsAsArray(
			BooleanExpression expression) {
		SymbolicOperator op = expression.operator();

		switch (op) {
		case AND:
		case OR:
		case NOT:
			return ((CompoundBooleanExpression) expression).arguments();
		default:
			throw new SARLException("Expected AND, OR, or NOT, but saw: " + op);
		}
	}

	private BooleanExpression makeOrFromOrderedArray(BooleanExpression[] args) {
		return or(Arrays.asList(args));
		// TODO: optimize...
		/*
		int len = args.length;
		BooleanExpression result;

		if (len == 0) {
			result = falseExpr;
		} else if (len == 1) {
			result = args[0];
		} else {
			result = orExpr(args);
		}
		return result;
		*/
	}

	@Override
	public BooleanExpression factorOrs(BooleanExpression[] orExpressions) {
		int n = orExpressions.length;
		BooleanExpression[][] argSets = new BooleanExpression[n][];

		for (int i = 0; i < n; i++)
			argSets[i] = ((CompoundBooleanExpression) orExpressions[i])
					.arguments();

		BooleanExpression[] commonPart = setFactory.factor(argSets);
		BooleanExpression result = makeOrFromOrderedArray(commonPart);

		for (int i = 0; i < n; i++)
			orExpressions[i] = makeOrFromOrderedArray(argSets[i]);
		return result;
	}
}
