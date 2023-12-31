package edu.udel.cis.vsl.sarl.preuniverse.common;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import edu.udel.cis.vsl.sarl.IF.CanonicalRenamer;
import edu.udel.cis.vsl.sarl.IF.Predicate;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * A substituter used to assign new, canonical names to all symbolic constants
 * occurring in a sequence of expressions. This class is provided with a root
 * {@link String}, e.g., "X". Then, as it encounters symbolic constants, it
 * renames them X0, X1, X2, ...., in that order.
 * 
 * @author Stephen F. Siegel
 */
public class CommonCanonicalRenamer extends ExpressionSubstituter
		implements CanonicalRenamer {

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
	 * The root {@link String} to use for the new names. The integer 0, 1, ...,
	 * will be appended to {@link #root} to form the names of the new symbolic
	 * constants.
	 */
	private String root;

	/**
	 * Map from original free (not bound) symbolic constants to their newly
	 * named versions.
	 */
	private Map<SymbolicConstant, SymbolicConstant> freeMap = new HashMap<>();

	/**
	 * The number of symbolic constants encountered so far. This includes both
	 * free and bound symbolic constants. Each declaration of a bound symbolic
	 * constant (i.e., its binding occurrence in a quantified expression) is
	 * considered a totally new symbolic constant, so will be given a unique
	 * name.
	 */
	private int varCount = 0;

	/**
	 * Which symbolic constants should be ignored? If <code>null</code>, none
	 * will be ignored.
	 */
	private Predicate<SymbolicConstant> ignore;

	/**
	 * Creates new renamer.
	 * 
	 * @param universe
	 *            symbolic universe for producing new {@link SymbolicExpression}
	 *            s
	 * @param collectionFactory
	 *            factory for producing new {@link SymbolicCollection}s
	 * @param typeFactory
	 *            factory for producing new {@link SymbolicType}s
	 * @param root
	 *            root of new names
	 * @param ignore
	 *            while symbolic constants should be ignored (i.e., not
	 *            renamed)? if <code>null</code>, none will be ignored (i.e.,
	 *            all will be renamed)
	 */
	public CommonCanonicalRenamer(PreUniverse universe,
			SymbolicTypeFactory typeFactory, ObjectFactory objectFactory,
			String root, Predicate<SymbolicConstant> ignore) {
		super(universe, objectFactory, typeFactory);
		this.root = root;
		this.ignore = ignore;
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
		SymbolicType newType = substituteType(expression.type(), state);
		SymbolicConstant boundVariable = oldBoundVariable;
		boolean renamedBoundVar = false;

		// Rename bound variable if the name of it starts with root:
		if (((StringObject) ((SymbolicConstant) oldBoundVariable).argument(0))
				.getString().startsWith(root)) {
			String newName = root + varCount;

			varCount++;

			SymbolicType newBoundVariableType = substituteType(
					oldBoundVariable.type(), state);
			SymbolicConstant newBoundVariable = universe.symbolicConstant(
					universe.stringObject(newName), newBoundVariableType);

			boundVariable = newBoundVariable;
			((BoundStack) state).push(oldBoundVariable, newBoundVariable);
		}

		SymbolicExpression newBody = substituteExpression(
				(SymbolicExpression) expression.argument(1), state);

		if (renamedBoundVar)
			((BoundStack) state).pop();

		SymbolicExpression result = universe.make(expression.operator(),
				newType, new SymbolicObject[] { boundVariable, newBody });

		return result;
	}

	@Override
	protected SymbolicExpression substituteNonquantifiedExpression(
			SymbolicExpression expr, SubstituterState state) {
		if (expr instanceof SymbolicConstant
				&& (ignore == null || !ignore.apply((SymbolicConstant) expr))) {
			// no op if the name of the symbolic constant expr doesn't start
			// with the root of this canonical renamer
			SymbolicType oldType = expr.type();
			SymbolicType newType = this.substituteType(oldType, state);

			if (!((StringObject) ((SymbolicConstant) expr).argument(0))
					.getString().startsWith(root))
				return newType == oldType ? expr
						: universe.symbolicConstant(
								((SymbolicConstant) expr).name(), newType);

			SymbolicConstant newVar = ((BoundStack) state)
					.get((SymbolicConstant) expr);

			if (newVar == null) {
				newVar = freeMap.get((SymbolicConstant) expr);
				if (newVar == null) {
					newVar = universe.symbolicConstant(
							universe.stringObject(root + varCount), newType);
					varCount++;
					freeMap.put((SymbolicConstant) expr, newVar);
				}
			}
			return newVar;
		} else {
			return super.substituteNonquantifiedExpression(expr, state);
		}
	}

	@Override
	public int getNumNewNames() {
		return varCount;
	}
}
