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

import java.util.HashSet;
import java.util.Map;

import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.Interval;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.IF.Range;
import edu.udel.cis.vsl.sarl.simplify.IF.Simplifier;

/**
 * <p>
 * An implementation of {@link Simplifier} for the "ideal" numeric factory
 * {@link IdealFactory}.
 * </p>
 *
 */
public class IdealSimplifier implements Simplifier {

	/**
	 * Keeps count of the number of simplifications performed, for performance
	 * debugging.
	 */
	static int simplifyCount = 0;

	// Instance fields...

	/**
	 * The operator used to rename bound variables so that their names do not
	 * conflict with those of free variables.
	 */
	UnaryOperator<SymbolicExpression> boundCleaner;

	/**
	 * Abstract representation of the {@link #fullContext}.
	 */
	private Context theContext;

	// Constructors...

	/**
	 * Constructs new simplifier based on the given assumption. The assumption
	 * is analyzed to extract information such as bounds, and the maps which are
	 * fields of this class are populated based on that information.
	 * 
	 * @param info
	 *            the info object wrapping together references to all objects
	 *            needed for this simplifier to do its job
	 * @param assumption
	 *            the assumption ("context") on which this simplifier will be
	 *            based
	 */
	public IdealSimplifier(SimplifierUtility info, BooleanExpression assumption,
			boolean useBackwardSubstitution) {
		this.boundCleaner = info.universe.newMinimalBoundCleaner();
		// rename bound variables so every variable has a unique name...
		assumption = (BooleanExpression) boundCleaner.apply(assumption);
		this.theContext = new Context(info, assumption,
				useBackwardSubstitution);
	}

	// Private methods ...

	private IdealSimplifierWorker newWorker() {
		return new IdealSimplifierWorker(theContext, new HashSet<>());
	}

	// Package-private methods...

	/**
	 * Attempts to find, in the context, a clause which states the
	 * differentiability of the given <code>function</code>. This is a clause
	 * with operator {@link SymbolicOperator#DIFFERENTIABLE} and with the
	 * function argument (argument 0) equal to <code>function</code>.
	 * 
	 * @param function
	 *            the function for which a differentiability claim is sought
	 * @return a clause in the context dealing with the differentiability of
	 *         <code>function</code>, or <code>null</code> if no such clause is
	 *         found.
	 */
	BooleanExpression findDifferentiableClaim(SymbolicExpression function) {
		return theContext.findDifferentiableClaim(function);
	}

	// Public methods...

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		// no need to create new worker in these basic cases...
		if (SimplifierUtility.isSimpleConstant(x))
			return x;
		simplifyCount++;
		// rename bound variables with counts starting from where the
		// original assumption renaming left off. This ensures that
		// all bound variables in the assumption and x are unique, but
		// two different x's can have same bound variables (thus
		// improving canonicalization)...
		x = theContext.util.universe.cloneBoundCleaner(boundCleaner).apply(x);
		return newWorker().simplifyNonSimpleConstant(x);
	}

	@Override
	public Interval assumptionAsInterval(SymbolicConstant symbolicConstant) {
		return theContext.assumptionAsInterval(symbolicConstant);
	}

	@Override
	public Map<SymbolicConstant, SymbolicExpression> constantSubstitutionMap() {
		return theContext.getSolvedVariables();
	}

	@Override
	public BooleanExpression getReducedContext() {
		return theContext.getReducedAssumption();
	}

	@Override
	public BooleanExpression getFullContext() {
		return theContext.getFullAssumption();
	}

	@Override
	public Interval intervalApproximation(NumericExpression expr) {
		Range range = theContext.computeRange((RationalExpression) expr);
		Interval result = range.intervalOverApproximation();

		return result;
	}

	@Override
	public PreUniverse universe() {
		return theContext.util.universe;
	}

	@Override
	public boolean useBackwardSubstitution() {
		return theContext.backwardsSub;
	}
}
