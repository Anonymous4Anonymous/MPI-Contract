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
package edu.udel.cis.vsl.sarl.preuniverse.common;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * Replaces all bound variables in expressions with new ones so that each has a
 * unique name and a name different from any unbound symbolic constant (assuming
 * no one else uses the "special string").
 * 
 * Requirements:
 * 
 * 1. a free variable is never renamed
 * 
 * 2. after transformation, each bound variable will have a unique name ---
 * different from that of every other bound variable and different from that of
 * any free variable
 * 
 * 3. if a bound variable is renamed, its new name will have the form
 * "special string"+n, for some natural number n
 * 
 * Protocol: the special string is assumed to be "__b".
 * 
 * State:
 * 
 * usedNames: set of string, the names of all free and bound variables except
 * for those of the form __bn for some natural number n. Can be implemented as a
 * hash- or tree-set.
 * 
 * usedIndexes: set of natural numbers n such that __bn is in use (in free or
 * bound variable). Can be implemented using a BitSet.
 * 
 * nextIndex: the minimum natural number n such that n is not in usedIndexes
 * 
 * Note: a string x is "in use" if it is in usedNames or if x has the form __bn
 * and n is in usedIndexes.
 * 
 * Step 1: iterate over all free variables x. If x is of form __bn, add n to
 * usedIndexes and update nextIndex, otherwise add x to usedNames.
 * 
 * Step 2: Iterate over quantified variables x. If x is not in use, add it to
 * appropriate set (usedNames or usedIndexes) and update nextIndex if necessary.
 * If x is in use, let n = nextIndex, change name of x to __bn, and update
 * nextIndex and usedIndexes.
 * 
 * Example:
 * 
 * z[Q] x[Q] x y y[Q] y[Q] y x x[Q].
 * 
 * New names: z, __b0, x, y, __b1, __b2, y, x, __b3
 * 
 * @author Stephen F. Siegel
 */
public class BoundCleaner2 extends ExpressionSubstituter {

	/**
	 * Special string which will be used to give unique name to new variables.
	 * "'" is good for some provers, but not for Z3.
	 */
	private final static String specialString = "__b";

	/**
	 * State of search: stack of pairs of symbolic constants. Left component of
	 * a pair is the original bound symbolic constant, right component is the
	 * new bound symbolic constant that will be substituted for the old one. An
	 * entry is pushed onto the stack whenever a quantified expression is
	 * reached, then the body of the expression is searched, then the stack is
	 * popped.
	 * 
	 * @author xxxx
	 */
	class BoundStack implements SubstituterState {
		Deque<Pair<SymbolicConstant, SymbolicConstant>> stack = new ArrayDeque<>();

		SymbolicConstant get(SymbolicConstant key) {
			for (Pair<SymbolicConstant, SymbolicConstant> pair : stack) {
				if (pair.left.equals(key))
					return pair.right;
			}
			return null;
		}

		void push(SymbolicConstant key, SymbolicConstant value) {
			stack.push(
					new Pair<SymbolicConstant, SymbolicConstant>(key, value));
		}

		void pop() {
			stack.pop();
		}

		@Override
		public boolean isInitial() {
			return stack.isEmpty();
		}
	}

	/**
	 * The names of all free and bound variables seen so far, except for those
	 * of the form "special string"+n for some natural number n.
	 */
	private Set<String> usedNames = new HashSet<>();

	/**
	 * The set of all natural numbers n for which a (free or bound) variable
	 * named "special string"+n has been seen.
	 */
	private BitSet usedIndexes = new BitSet();

	/**
	 * The smallest natural number n such that n is not in {@link #usedIndexes}.
	 */
	private int nextIndex = 0;

	public BoundCleaner2(PreUniverse universe, ObjectFactory objectFactory,
			SymbolicTypeFactory typeFactory) {
		super(universe, objectFactory, typeFactory);
	}

	/**
	 * Given the name of a variable, if that name has the form "special string"
	 * +n for some natural number n, returns n, else returns -1.
	 * 
	 * @param name
	 *            the name of a variable
	 * @return either the special index or -1 if the name is not special
	 */
	private int getSpecialIndex(String name) {
		if (!name.startsWith(specialString))
			return -1;
		try {
			int result = Integer
					.parseInt(name.substring(specialString.length()));

			if (result >= 0)
				return result;
			else
				return -1;
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * Determines whether the given name is currently in use by some variable.
	 * That is, is the name in {@link #usedNames} or is the name
	 * "special string"+n for some n in {@link #usedIndexes}?
	 * 
	 * @param name
	 *            a variable name
	 * @return <code>true</code> iff the name is currently in use
	 */
	private boolean inUse(String name) {
		int index = getSpecialIndex(name);

		if (index >= 0) {
			return usedIndexes.get(index);
		} else {
			return usedNames.contains(name);
		}
	}

	/**
	 * Declares the given name to be in use. The fields {@link #usedIndexes},
	 * {@link #nextIndex}, {@link #usedNames} are updated as necessary.
	 * 
	 * @param name
	 *            a variable name
	 */
	private void use(String name) {
		int index = getSpecialIndex(name);

		if (index >= 0) {
			usedIndexes.set(index);
			if (index == nextIndex) {
				nextIndex = usedIndexes.nextClearBit(nextIndex + 1);
			}
		} else {
			usedNames.add(name);
		}
	}

	/**
	 * Returns a name of the form "special string"+n that is not currently in
	 * use. Marks that new name as in use.
	 * 
	 * @return the new name
	 */
	private String getNewName() {
		String result = specialString + nextIndex;

		usedIndexes.set(nextIndex);
		nextIndex = usedIndexes.nextClearBit(nextIndex + 1);
		return result;
	}

	@Override
	protected SubstituterState newState() {
		return new BoundStack();
	}

	@Override
	protected SymbolicExpression substituteQuantifiedExpression(
			SymbolicExpression expression, SubstituterState state) {
		SymbolicConstant oldBoundVariable = (SymbolicConstant) expression
				.argument(0);
		SymbolicExpression oldBody = (SymbolicExpression) expression
				.argument(1);
		SymbolicType oldBoundVariableType = oldBoundVariable.type();
		SymbolicType newBoundVariableType = substituteType(oldBoundVariableType,
				state);
		SymbolicType oldType = expression.type();
		SymbolicType newType = substituteType(oldType, state);
		String oldName = oldBoundVariable.name().getString();
		String newName = inUse(oldName) ? getNewName() : oldName;
		SymbolicConstant newBoundVariable = oldBoundVariableType == newBoundVariableType
				&& oldName == newName
						? oldBoundVariable
						: universe.symbolicConstant(
								universe.stringObject(newName),
								newBoundVariableType);

		((BoundStack) state).push(oldBoundVariable, newBoundVariable);

		SymbolicExpression newBody = substituteExpression(oldBody, state);

		((BoundStack) state).pop();

		SymbolicExpression result;

		if (newBody == oldBody && newBoundVariable == oldBoundVariable
				&& newType == oldType)
			result = expression;
		else
			result = universe.make(expression.operator(), newType,
					new SymbolicObject[] { newBoundVariable, newBody });
		return result;
	}

	@Override
	protected SymbolicExpression substituteNonquantifiedExpression(
			SymbolicExpression expression, SubstituterState state) {
		if (expression instanceof SymbolicConstant) {
			SymbolicConstant newConstant = ((BoundStack) state)
					.get(((SymbolicConstant) expression));

			if (newConstant != null)
				return newConstant;
			// still possible that type could change...
		}
		if (state.isInitial() && !expression.containsQuantifier()) {
			// this means neither the expression nor its type contains
			// any quantifier and the bound stack is empty, so no
			// change is possible
			return expression;
		}
		return super.substituteNonquantifiedExpression(expression, state);
	}

	public BoundCleaner2 clone() {
		BoundCleaner2 result = new BoundCleaner2(universe, objectFactory,
				typeFactory);

		result.usedNames.addAll(usedNames);
		result.usedIndexes = (BitSet) usedIndexes.clone();
		result.nextIndex = nextIndex;
		return result;
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression expression) {
		if (!expression.containsQuantifier())
			return expression;
		for (SymbolicConstant x : universe
				.getFreeSymbolicConstants(expression)) {
			use(x.name().getString());
		}
		return super.apply(expression);
	}

}
