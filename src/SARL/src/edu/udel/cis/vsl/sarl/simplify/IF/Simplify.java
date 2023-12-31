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
package edu.udel.cis.vsl.sarl.simplify.IF;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.common.CommonContextPartition;
import edu.udel.cis.vsl.sarl.simplify.common.IdentitySimplifier;
import edu.udel.cis.vsl.sarl.simplify.common.IdentitySimplifierFactory;
import edu.udel.cis.vsl.sarl.simplify.common.IntervalUnionFactory;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierFactory;

/**
 * Entry point for module "simplify", providing static method to create basic
 * simplifiers, simplifier factories, and range factories.
 * 
 * @author Stephen F. Siegel
 */
public class Simplify {

	/**
	 * Creates a new "trivial" simplifier: given any expression this simplifiers
	 * just returns the expression.
	 * 
	 * @param universe
	 *            the pre-universe associated to the simplifier
	 * @param assumption
	 *            the boolean expression context, which is just not used
	 * @return the new trivial simplifier
	 */
	public static Simplifier identitySimplifier(PreUniverse universe,
			BooleanExpression assumption) {
		return new IdentitySimplifier(universe, assumption);
	}

	/**
	 * A factory for producing trivial simplifiers.
	 * 
	 * @param universe
	 *            the pre-universe to associate to the simplifier
	 * @return a new trivial simplifier factory
	 * @see #identitySimplifier(PreUniverse, BooleanExpression)
	 */
	public static SimplifierFactory newIdentitySimplifierFactory(
			PreUniverse universe) {
		return new IdentitySimplifierFactory(universe);
	}

	public static RangeFactory newIntervalUnionFactory() {
		return new IntervalUnionFactory();
	}

	public static ContextPartition newContextPartition(PreUniverse universe,
			BooleanExpression context) {
		return new CommonContextPartition(context, universe);
	}

	/**
	 * Constructs a new {@link SimplifierFactory} based on ideal arithmetic. The
	 * simplifiers produced by the new factory will deal with ideal numeric
	 * expressions and use all the rules of ideal arithmetic to simplify
	 * expressions.
	 * 
	 * @param idealFactory
	 *            the {@link IdealFactory} that the simplifiers will use to
	 *            simplify expressions
	 * @param universe
	 *            the symbolic universe that the simplifiers will use to create
	 *            and manipulate expressions that are not dealt with by the
	 *            ideal factory (specifically, non-numeric expressions)
	 * @return a new simplifier factory based on ideal arithmetic
	 */
	public static SimplifierFactory newIdealSimplifierFactory(
			IdealFactory idealFactory, PreUniverse universe) {
		return new IdealSimplifierFactory(idealFactory, universe);
	}

}
