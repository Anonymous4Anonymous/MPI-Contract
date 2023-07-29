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

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.IF.Simplifier;
import edu.udel.cis.vsl.sarl.simplify.IF.SimplifierFactory;

/**
 * A factory for producing new instances of {@link IdealSimplifier}.
 * 
 * @author Stephen F. Siegel (xxxx)
 */
public class IdealSimplifierFactory implements SimplifierFactory {

	/**
	 * A structure which packages references to several other factories and
	 * commonly-used objects that will be used by the {@link OldIdealSimplifier}
	 * s produced by this factory.
	 */
	private SimplifierUtility util;

	/**
	 * Constructs new {@link IdealSimplifierFactory} based on the given
	 * {@link IdealFactory} and {@link PreUniverse}.
	 * 
	 * @param idealFactory
	 *            the factory used for producing "ideal" mathematical symbolic
	 *            expressions
	 * @param universe
	 *            the symbolic universe for producing general symbolic
	 *            expressions
	 */
	public IdealSimplifierFactory(IdealFactory idealFactory,
			PreUniverse universe) {
		util = new SimplifierUtility(universe, idealFactory);
	}

	@Override
	public Simplifier newSimplifier(BooleanExpression assumption,
			boolean useBackwardSubstitution) {
		return new IdealSimplifier(util, assumption, useBackwardSubstitution);
	}

}
