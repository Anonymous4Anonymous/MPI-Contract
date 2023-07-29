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
package edu.udel.cis.vsl.sarl.reason.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.prove.IF.ProverFunctionInterpretation;
import edu.udel.cis.vsl.sarl.prove.IF.TheoremProver;
import edu.udel.cis.vsl.sarl.prove.IF.TheoremProverFactory;
import edu.udel.cis.vsl.sarl.prove.why3.RobustWhy3ProvePlatformFactory;
import edu.udel.cis.vsl.sarl.reason.IF.ReasonerFactory;
import edu.udel.cis.vsl.sarl.simplify.IF.Simplifier;
import edu.udel.cis.vsl.sarl.simplify.IF.SimplifierFactory;

/**
 * Basic factory for producing instances of {@link CommonReasoner}.
 * 
 * @author Stephen F. Siegel
 */
public class CommonReasonerFactory implements ReasonerFactory {

	/**
	 * Cached results of previous creation of @{link Reasoner}s. The idea is to
	 * have at most one {@link Reasoner} for each boolean expression
	 * ("context").
	 */
	private Map<BooleanExpression, Reasoner> reasonerCache = new ConcurrentHashMap<>();

	/**
	 * The theorem prover factory associated to this reasoner factory. It will
	 * be used by the {@link Reasoner}s produced by this factory to instantiate
	 * new {@link TheoremProver}s as the need arises.
	 */
	private TheoremProverFactory proverFactory;

	/**
	 * The simplifier factory associated to this reasoner factory. It will be
	 * used by the {@link Reasoner}s produced by this factory to instantiate new
	 * {@link Simplifier}s as the need arises.
	 */
	private SimplifierFactory simplifierFactory;

	/**
	 * Produces a new {@link CommonReasonerFactory} based on the given
	 * <code>simplifierFactory</code> and <code>proverFactory</code>.
	 * 
	 * @param simplifierFactory
	 *            the factory that will be used by {@link Reasoner}s produced by
	 *            this factory to create new {@link Simplifier}s when the need
	 *            arises
	 * @param proverFactory
	 *            the factory that will be used by {@link Reasoner}s produced by
	 *            this factory to create new {@link TheoremProver}s when the
	 *            need arises
	 */
	public CommonReasonerFactory(SimplifierFactory simplifierFactory,
			TheoremProverFactory proverFactory,
			RobustWhy3ProvePlatformFactory why3Factory) {
		this.proverFactory = proverFactory;
		this.simplifierFactory = simplifierFactory;
	}

	@Override
	public Reasoner getReasoner(BooleanExpression context,
			boolean useBackwardSubstitution,
			ProverFunctionInterpretation[] proverPredicates) {
		Reasoner result = reasonerCache.get(context);

		if (result == null) {
			Simplifier simplifier = simplifierFactory.newSimplifier(context,
					useBackwardSubstitution);
			Reasoner newReasoner = new CommonReasoner(this, simplifier);

			result = reasonerCache.putIfAbsent(context, newReasoner);
			return result == null ? newReasoner : result;
		}
		return result;
	}

	@Override
	public TheoremProverFactory getTheoremProverFactory() {
		return proverFactory;
	}
}
