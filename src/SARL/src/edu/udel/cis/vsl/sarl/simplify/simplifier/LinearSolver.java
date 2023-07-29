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
package edu.udel.cis.vsl.sarl.simplify.simplifier;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.ideal.IF.Constant;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.Monic;
import edu.udel.cis.vsl.sarl.ideal.IF.Monomial;
import edu.udel.cis.vsl.sarl.ideal.IF.Polynomial;
import edu.udel.cis.vsl.sarl.util.Pair;

/**
 * <p>
 * A class used to simplify a constant map by applying linear reasoning. The
 * numeric (integer and real) entries in the constant map are separated and used
 * to form coefficient matrices, where the "variables" are {@link Monic}s. These
 * matrices are placed in reduced row echelon form using Gaussian elimination.
 * The new entries for the substitution map are obtained by translating each row
 * of these matrices back to a substitution. The client may choose to use
 * backwards substitution or not.
 * </p>
 * 
 * <p>
 * The easiest way to use this class is through the two static methods
 * {@link #reduce(SimplifierUtility, Map, Comparator, boolean)} and
 * {@link #reduceRelative(SimplifierUtility, Map, Map, Comparator, boolean)}.
 * </p>
 * 
 * <p>
 * If an inconsistency exists (for example, X+Y maps to 0, X maps to 0, Y maps
 * to 1) in the map, this will be discovered in the elimination. The method
 * {@link #isConsistent()} can be used to determine if an inconsistency has been
 * detected. Another method {@link #hasChanged()} can tell if any changes
 * resulted from the process.
 * </p>
 * 
 * <p>
 * This class will not modify the original substitution map. Rather, it provides
 * two methods which specify the changes that must be made to the original
 * substitution map in order to bring it into sync with this solver. This is for
 * efficiency reasons, and also because it is best to let the client modify her
 * substitution map, since she may want to apply complicated normalized forms
 * before adding new entries, and those transformations are not available to
 * this solver.
 * </p>
 */
public class LinearSolver {

	// Private instance fields ...

	/**
	 * The number factory used to perform infinite precision rational
	 * arithmetic.
	 */
	private NumberFactory nf;

	/**
	 * The factory used to manipulate {@link Monic}s, {@link Polynomial}s, etc.
	 */
	private IdealFactory idf;

	/**
	 * Ideal simplifier utility.
	 */
	private SimplifierUtility info;

	/**
	 * An organized view of a set of "variables", which are actually
	 * {@link Monic}s. Places a total order on the set, assigns an ID number to
	 * each element, and so on.
	 */
	private LinearVariableSet keySet;

	/**
	 * The comparator to use to order the "variables" (columns) in the
	 * coefficient matrices. This is relevant only if using backwards
	 * substitution, because it will determine which variable is "solved for"
	 * (will occur as a key). In general, when using backwards substitution, a
	 * solved variable x will be assigned a value which is a linear combination
	 * of variables that are strictly higher than x in this order.
	 */
	private Comparator<Monic> monicComparator;

	/**
	 * The original substitution map, provided by the client at construction
	 * time. It is not modified by this class.
	 */
	private Map<SymbolicExpression, SymbolicExpression> originalMap;

	/**
	 * New entries created by analyzing the results of the Gaussian elimination.
	 * Some of the entries may be identical to entries in the original map.
	 */
	private Map<Monic, Monomial> newMap;

	/**
	 * The matrix of coefficients in the integer system of equations. There is
	 * one row for each integer constraint, and one column for each integer
	 * "variable" (actually, a {@link Monic}), and one additional column for the
	 * right hand side of the equation.
	 */
	private RationalNumber[][] intMatrix;

	/**
	 * The matrix of coefficients in the real system of equations. There is one
	 * row for each real constraint, and one column for each real "variable"
	 * (actually, a {@link Monic}), and one additional column for the right hand
	 * side of the equation.
	 */
	private RationalNumber[][] realMatrix;

	private int numIntMonics = 0;

	private int numRealMonics = 0;

	/**
	 * The number of integer entries in {@link #originalMap}.
	 */
	private int numIntConstraints = 0;

	/**
	 * The number of real entries in {@link #originalMap}.
	 */
	private int numRealConstraints = 0;

	/**
	 * Will the original substitution map need to be changed in order to bring
	 * it into sync with this solver?
	 */
	private boolean change = false;

	/**
	 * Are the linear systems consistent?
	 */
	private boolean consistent = true;

	/**
	 * Should this solver use backwards substitution to form the new
	 * substitutions?
	 */
	private boolean backwardsSub = false;

	private IntegerNumber zeroInt;

	private RationalNumber zeroReal;

	// Constructors ...

	protected LinearSolver(SimplifierUtility info, LinearVariableSet keySet,
			int numIntConstraints, int numRealConstraints,
			Map<SymbolicExpression, SymbolicExpression> map,
			Comparator<Monic> monicComparator, boolean backwardsSub) {
		this.info = info;
		this.originalMap = map; // it will not be modified
		this.backwardsSub = backwardsSub;
		this.idf = info.idealFactory;
		this.nf = info.numberFactory;
		this.zeroInt = nf.zeroInteger();
		this.zeroReal = nf.zeroRational();
		this.monicComparator = monicComparator;
		this.keySet = keySet;
		this.numIntMonics = keySet.numIntMonics();
		this.numRealMonics = keySet.numRealMonics();
		this.numIntConstraints = numIntConstraints;
		this.numRealConstraints = numRealConstraints;
		this.intMatrix = new RationalNumber[numIntConstraints][numIntMonics
				+ 1];
		this.realMatrix = new RationalNumber[numRealConstraints][numRealMonics
				+ 1];
		initialize();
	}

	// Private methods...

	private void addIntConstraint(int intConstraintId, Monic key,
			Monomial value) {
		// For backwards sub, you must figure out if solved form.
		// in solved form there will be exactly one monic
		// in the key and all monics in the value will be
		// greater in the order.
		Monic lhs = null;

		for (Monomial term : key.termMap(idf)) {
			RationalNumber coefficient = nf
					.rational(term.monomialConstant(idf).number());
			Monic monic = term.monic(idf);
			int col = keySet.getIntId(monic);
			RationalNumber oldEntry = intMatrix[intConstraintId][col];

			intMatrix[intConstraintId][col] = nf.add(oldEntry, coefficient);
			if (lhs == null)
				lhs = monic;
			else if (!change && backwardsSub)
				// backwards substitution can never result in a key with
				// more than 1 term, so a change must take place:
				change = true;
		}
		for (Monomial term : value.termMap(idf)) {
			Monic monic = term.monic(idf);
			RationalNumber coefficient = nf
					.rational(term.monomialConstant(idf).number());

			if (monic.isOne()) {
				intMatrix[intConstraintId][numIntMonics] = coefficient;
			} else {
				int col = keySet.getIntId(monic);
				RationalNumber oldEntry = intMatrix[intConstraintId][col];

				intMatrix[intConstraintId][col] = nf.subtract(oldEntry,
						coefficient);
				if (!change) {
					if (backwardsSub) {
						if (monicComparator.compare(lhs, monic) >= 0)
							change = true;
					} else {
						// without backwards substitution, a non-constant can
						// never occur on the right, so a change must take
						// place.
						change = true;
					}
				}
			}
		}
	}

	private void addRealConstraint(int realConstraintId, Monic key,
			Monomial value) {
		Monic lhs = null;

		for (Monomial term : ((Monic) key).termMap(idf)) {
			RationalNumber coefficient = (RationalNumber) term
					.monomialConstant(idf).number();
			Monic monic = term.monic(idf);
			int col = keySet.getRealId(monic);
			RationalNumber oldEntry = realMatrix[realConstraintId][col];

			realMatrix[realConstraintId][col] = nf.add(oldEntry, coefficient);
			if (lhs == null)
				lhs = monic;
			else if (!change && backwardsSub)
				change = true;
		}
		for (Monomial term : ((Monomial) value).termMap(idf)) {
			Monic monic = term.monic(idf);
			RationalNumber coefficient = (RationalNumber) term
					.monomialConstant(idf).number();

			if (monic.isOne()) {
				realMatrix[realConstraintId][numRealMonics] = coefficient;
			} else {
				int col = keySet.getRealId(monic);
				RationalNumber oldEntry = realMatrix[realConstraintId][col];

				realMatrix[realConstraintId][col] = nf.subtract(oldEntry,
						coefficient);
				if (!change) {
					if (backwardsSub) {
						if (monicComparator.compare(lhs, monic) >= 0)
							change = true;
					} else {
						change = true;
					}
				}
			}
		}
	}

	/**
	 * Builds the matrix representations of the maps. For the integer
	 * constraints, there is one row for each integer entry in the map and one
	 * column for each monic which occurs as a term in some key, of integer
	 * type, plus one additional column to hold the value associated to the
	 * constant value associated to the map entry. The real map is similar.
	 */
	private void initialize() {
		int intConstraintId = 0, realConstraintId = 0;

		for (int i = 0; i < numIntConstraints; i++)
			for (int j = 0; j <= numIntMonics; j++)
				intMatrix[i][j] = zeroReal;
		for (int i = 0; i < numRealConstraints; i++)
			for (int j = 0; j <= numRealMonics; j++)
				realMatrix[i][j] = zeroReal;
		for (Entry<SymbolicExpression, SymbolicExpression> entry : originalMap
				.entrySet()) {
			SymbolicExpression key = entry.getKey();
			SymbolicExpression value = entry.getValue();

			if (!(key instanceof Monic) || !(value instanceof Monomial))
				continue;

			SymbolicType type = key.type();

			if (type.isInteger()) {
				addIntConstraint(intConstraintId, (Monic) key,
						(Monomial) value);
				intConstraintId++;
			} else { // a real constraint
				addRealConstraint(realConstraintId, (Monic) key,
						(Monomial) value);
				realConstraintId++;
			}
		}
	}

	private void addEntry(Monic key, Monomial value) {
		newMap.put(key, value);
	}

	/**
	 * Adds to {@link #newMap} entries corresponding to the rows of the
	 * {@link #intMatrix} without using backwards substitution. A key will be
	 * the sum of terms corresponding to all columns except the last. The value
	 * will be the constant corresponding to the last column. Denominators will
	 * be cleared by multiplying both sides by the least common multiple of the
	 * denominators, in order to yield an integer constraint.
	 * 
	 * This method should be called only once, after Gaussian elimination has
	 * completed. Only one of {@link #buildNewIntMapSimple()},
	 * {@link #buildNewIntMapBackwardsSub()} should be called.
	 * 
	 * @return {@code false} if an inconsistency is discovered, else
	 *         {@code true}
	 */
	private boolean buildNewIntMapSimple() {
		Monic[] intMonics = keySet.getIntMonics();
		int numIntMonics = intMonics.length;
		int leadingCol = 0;
		int i = 0;

		for (; i < numIntConstraints; i++) {
			for (; leadingCol < numIntMonics; leadingCol++) {
				if (!intMatrix[i][leadingCol].isZero())
					break;
			}
			if (leadingCol == numIntMonics)
				break;
			assert intMatrix[i][leadingCol].isOne();

			Monomial poly = idf.zeroInt();
			IntegerNumber lcm = nf.oneInteger();

			// clear the denominators in row i by multiplying
			// every entry in the row by the LCM
			for (int j = leadingCol + 1; j <= numIntMonics; j++) {
				RationalNumber a = intMatrix[i][j];

				if (!a.isZero()) {
					IntegerNumber denominator = nf.denominator(a);

					if (!denominator.isOne())
						lcm = nf.lcm(lcm, denominator);
				}
			}
			for (int j = leadingCol; j < numIntMonics; j++) {
				RationalNumber a = intMatrix[i][j];

				if (!a.isZero()) {
					IntegerNumber coefficient = nf.multiply(nf.numerator(a),
							nf.divide(lcm, nf.denominator(a)));

					poly = idf.addMonomials(poly, idf
							.monomial(idf.constant(coefficient), intMonics[j]));
				}
			}

			RationalNumber constant = intMatrix[i][numIntMonics];
			IntegerNumber value = nf.multiply(nf.numerator(constant),
					nf.divide(lcm, nf.denominator(constant)));
			Pair<Monic, Number> pair = info.normalize(poly, value);

			if (pair == null)
				return false;
			addEntry(pair.left, idf.constant(pair.right));
		}
		for (; i < numIntConstraints; i++) {
			if (!intMatrix[i][numIntMonics].isZero())
				return false;
		}
		return true;
	}

	/**
	 * Adds to {@link #newMap} entries corresponding to the rows of the
	 * {@link #realMatrix} without using backwards substitution.
	 * 
	 * @return {@code false} if an inconsistency is discovered, else
	 *         {@code true}
	 */
	private boolean buildNewRealMapSimple() {
		Monic[] realMonics = keySet.getRealMonics();
		int numRealMonics = realMonics.length;
		int leadingCol = 0;
		int i = 0;

		for (; i < numRealConstraints; i++) {
			for (; leadingCol < numRealMonics; leadingCol++) {
				if (!realMatrix[i][leadingCol].isZero())
					break;
			}
			if (leadingCol == numRealMonics)
				break;
			assert realMatrix[i][leadingCol].isOne(); // reduced row-echelon

			RationalNumber value = realMatrix[i][numRealMonics];
			Monomial poly = realMonics[leadingCol];

			for (int j = leadingCol + 1; j < numRealMonics; j++) {
				RationalNumber a = realMatrix[i][j];

				if (a.signum() != 0)
					poly = idf.addMonomials(poly,
							idf.monomial(idf.constant(a), realMonics[j]));
			}

			Pair<Monic, Number> pair = info.normalize(poly, value);

			if (pair == null)
				return false;
			addEntry(pair.left, idf.constant(pair.right));
		}
		for (; i < numRealConstraints; i++) {
			if (!realMatrix[i][numRealMonics].isZero())
				return false;
		}
		return true;
	}

	/**
	 * Adds to {@link #newMap} entries corresponding to the rows of
	 * {@link #intMatrix}, using backwards substitution. This means that the key
	 * of an entry will correspond to the entry for the pivot column, and the
	 * value for the entry will be obtained by summing terms corresponding to
	 * the other columns. This method may add more entries saying that a modulus
	 * is zero.
	 * 
	 * @return {@code false} if an inconsistency is discovered, else
	 *         {@code true}
	 */
	private boolean buildNewIntMapBackwardsSub() {
		Monic[] intMonics = keySet.getIntMonics();
		int numIntMonics = intMonics.length;
		int leadingCol = 0;
		int i = 0;

		for (; i < numIntConstraints; i++) {
			for (; leadingCol < numIntMonics; leadingCol++) {
				if (!intMatrix[i][leadingCol].isZero())
					break;
			}
			if (leadingCol == numIntMonics)
				break;
			assert intMatrix[i][leadingCol].isOne();
			// clear the denominators in row i by multiplying
			// every entry in the row by the LCM of the denominators.
			// First, compute the LCM...

			IntegerNumber lcm = nf.oneInteger();

			for (int j = leadingCol + 1; j <= numIntMonics; j++) {
				RationalNumber a = intMatrix[i][j];

				if (!a.isZero()) {
					IntegerNumber denominator = nf.denominator(a);

					if (!denominator.isOne())
						lcm = nf.lcm(lcm, denominator);
				}
			}

			// create sub lhs_monic -> rhs
			Monic lhs_monic = intMonics[leadingCol];
			RationalNumber constantTerm = intMatrix[i][numIntMonics];
			Monomial rhs = idf.constant(nf.multiply(nf.numerator(constantTerm),
					nf.divide(lcm, nf.denominator(constantTerm))));

			for (int j = leadingCol + 1; j < numIntMonics; j++) {
				RationalNumber a = intMatrix[i][j];

				if (!a.isZero()) {
					IntegerNumber coefficient = nf
							.negate(nf.multiply(nf.numerator(a),
									nf.divide(lcm, nf.denominator(a))));

					rhs = idf.addMonomials(rhs, idf
							.monomial(idf.constant(coefficient), intMonics[j]));
				}
			}
			if (!rhs.isZero()) {
				Monic rhs_monic = rhs.monic(idf);
				IntegerNumber lhs_co = lcm, rhs_co = (IntegerNumber) rhs
						.monomialConstant(idf).number();
				IntegerNumber gcd = nf.gcd(lhs_co,
						(IntegerNumber) nf.abs(rhs_co));

				if (!gcd.isOne()) {
					lhs_co = nf.divide(lcm, gcd);
					rhs_co = nf.divide(rhs_co, gcd);
					rhs = idf.monomial(idf.constant(rhs_co), rhs_monic);
				}
				if (!lhs_co.isOne()) { // add assumption rhs%lhs_co==0
					Constant lhs_const = idf.constant(lhs_co);
					Monomial modKey = idf.modulo(rhs, lhs_const);
					Pair<Monic, Number> modPair = info.normalize(modKey,
							zeroInt);

					if (modPair == null)
						return false;
					addEntry(modPair.left, idf.constant(modPair.right));
					rhs = idf.divideIntegerMonomials(rhs, lhs_const);
				}
			}
			addEntry(lhs_monic, rhs);
		}
		for (; i < numIntConstraints; i++) {
			if (!intMatrix[i][numIntMonics].isZero())
				return false;
		}
		return true;
	}

	/**
	 * Adds entries to the {@link #newMap} corresponding to the rows of the
	 * {@link #realMatrix}, using backwards substitution.
	 * 
	 * @return {@code false} if an inconsistency is discovered, else
	 *         {@code true}
	 */
	private boolean buildNewRealMapBackwardsSub() {
		Monic[] realMonics = keySet.getRealMonics();
		int numRealMonics = realMonics.length;
		int leadingCol = 0;
		int i = 0;

		for (; i < numRealConstraints; i++) {
			for (; leadingCol < numRealMonics; leadingCol++) {
				if (!realMatrix[i][leadingCol].isZero())
					break;
			}
			if (leadingCol == numRealMonics)
				break;
			assert realMatrix[i][leadingCol].isOne(); // reduced row-echelon

			// add sum lhs->rhs ...
			Monic lhs = realMonics[leadingCol];
			Monomial rhs = idf.constant(realMatrix[i][numRealMonics]);

			for (int j = leadingCol + 1; j < numRealMonics; j++) {
				RationalNumber a = realMatrix[i][j];

				if (!a.isZero())
					rhs = idf.addMonomials(rhs, idf.monomial(
							idf.constant(nf.negate(a)), realMonics[j]));
			}
			addEntry(lhs, rhs);
		}
		for (; i < numRealConstraints; i++) {
			if (!realMatrix[i][numRealMonics].isZero())
				return false;
		}
		return true;
	}

	/**
	 * Builds {@link #newMap}.
	 */
	private void makeNewMap() {
		newMap = new TreeMap<Monic, Monomial>(monicComparator);
		if (backwardsSub)
			consistent = buildNewIntMapBackwardsSub()
					&& buildNewRealMapBackwardsSub();
		else
			consistent = buildNewIntMapSimple() && buildNewRealMapSimple();
	}

	// public methods...

	/**
	 * Will the new map result in any change to the original substitution map?
	 * If not, nothing more need be done.
	 * 
	 * @return {@code true} if some change must happen to the substitution map,
	 *         else {@code false}
	 */
	public boolean hasChanged() {
		return change;
	}

	/**
	 * Is the linear system consistent?
	 * 
	 * @return {@code false} if the linear system has been determined to be
	 *         inconsistent, else {@code true}
	 */
	public boolean isConsistent() {
		return consistent;
	}

	/**
	 * Performs Gaussian elimination on the {@link #intMatrix} and
	 * {@link #realMatrix} to place those matrices into reduced row echelon
	 * form.
	 */
	public void reduce() {
		change = nf.gaussianElimination(intMatrix) || change;
		change = nf.gaussianElimination(realMatrix) || change;
	}

	/**
	 * Performs a Gaussian elimination "relative to" a fixed context.
	 * 
	 * @param context
	 */
	public void reduceRelativeTo(LinearSolver context) {
		change = nf.relativeGaussianElimination(context.intMatrix, intMatrix)
				|| change;
		change = nf.relativeGaussianElimination(context.realMatrix, realMatrix)
				|| change;
	}

	/**
	 * Returns the set of keys in the original map which should be removed as
	 * the first step to bring that map in sync with the state of this solver.
	 * 
	 * @return a set of symbolic expressions, each of which occurs as a key in
	 *         {@link #originalMap}; these keys should be removed from the map
	 *         by the client
	 */
	public Collection<SymbolicExpression> getKeysToRemove() {
		LinkedList<SymbolicExpression> result = new LinkedList<>();

		if (newMap == null)
			makeNewMap();
		for (Entry<SymbolicExpression, SymbolicExpression> entry : originalMap
				.entrySet()) {
			SymbolicExpression key = entry.getKey();

			if (key instanceof Monic && entry.getValue() instanceof Monomial
					&& !newMap.containsKey(key))
				result.add(key);
		}
		return result;
	}

	/**
	 * Returns the set of entries which should be entered into the original map
	 * in order to bring it in sync with the state of this solver. The client
	 * should perform that modification after removing the keys that need to be
	 * removed.
	 * 
	 * @return the set of new entries that must be applied to the original map
	 */
	public Collection<Entry<Monic, Monomial>> getNewEntries() {
		LinkedList<Entry<Monic, Monomial>> result = new LinkedList<>();

		if (newMap == null)
			makeNewMap();
		for (Entry<Monic, Monomial> entry : newMap.entrySet()) {
			if (originalMap.get(entry.getKey()) != entry.getValue())
				result.add(entry);
		}
		return result;
	}

	// Public static methods ...

	public static LinearSolver reduce(SimplifierUtility info,
			Map<SymbolicExpression, SymbolicExpression> map,
			Comparator<Monic> monicComparator, boolean backwardsSub) {
		LinearVariableSet keySet = new LinearVariableSet(info.idealFactory,
				monicComparator);
		Pair<Integer, Integer> numConstraints = keySet
				.addEntries(map.entrySet());

		keySet.finish(backwardsSub);

		LinearSolver result = new LinearSolver(info, keySet,
				numConstraints.left, numConstraints.right, map, monicComparator,
				backwardsSub);

		result.reduce();
		return result;
	}

	public static LinearSolver reduceRelative(SimplifierUtility info,
			Map<SymbolicExpression, SymbolicExpression> context,
			Map<SymbolicExpression, SymbolicExpression> map,
			Comparator<Monic> monicComparator, boolean backwardsSub) {
		LinearVariableSet keySet = new LinearVariableSet(info.idealFactory,
				monicComparator);
		Pair<Integer, Integer> numConstraints1 = keySet
				.addEntries(context.entrySet()),
				numConstraints2 = keySet.addEntries(map.entrySet());

		keySet.finish(backwardsSub);

		LinearSolver solver1 = new LinearSolver(info, keySet,
				numConstraints1.left, numConstraints1.right, context,
				monicComparator, backwardsSub);
		LinearSolver solver2 = new LinearSolver(info, keySet,
				numConstraints2.left, numConstraints2.right, map,
				monicComparator, backwardsSub);

		solver2.reduceRelativeTo(solver1);
		return solver2;
	}
}
