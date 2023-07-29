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
package edu.udel.cis.vsl.sarl.universe.IF;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.config.Configurations;
import edu.udel.cis.vsl.sarl.IF.config.ProverInfo;
import edu.udel.cis.vsl.sarl.IF.config.SARLConfig;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.expr.IF.ExpressionFactory;
import edu.udel.cis.vsl.sarl.expr.IF.Expressions;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.number.IF.Numbers;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.object.IF.Objects;
import edu.udel.cis.vsl.sarl.preuniverse.IF.FactorySystem;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverses;
import edu.udel.cis.vsl.sarl.prove.IF.Prove;
import edu.udel.cis.vsl.sarl.prove.IF.TheoremProverFactory;
import edu.udel.cis.vsl.sarl.reason.IF.Reason;
import edu.udel.cis.vsl.sarl.reason.IF.ReasonerFactory;
import edu.udel.cis.vsl.sarl.reason.common.Why3ReasonerFactory;
import edu.udel.cis.vsl.sarl.simplify.IF.SimplifierFactory;
import edu.udel.cis.vsl.sarl.simplify.IF.Simplify;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;
import edu.udel.cis.vsl.sarl.type.IF.Types;
import edu.udel.cis.vsl.sarl.universe.common.CommonSymbolicUniverse;

/**
 * This class provides static methods for the creation of new
 * {@link SymbolicUniverse}s.
 * 
 * @author xxxx
 */
public class Universes {

	public static SymbolicUniverse newIdealUniverse(SARLConfig config,
			ProverInfo prover) {
		FactorySystem system = PreUniverses.newIdealFactorySystem();
		CommonSymbolicUniverse universe = new CommonSymbolicUniverse(system);
		SimplifierFactory simplifierFactory = Simplify.newIdealSimplifierFactory(
				(IdealFactory) system.numericFactory(), universe);
		TheoremProverFactory proverFactory = prover == null
				? Prove.newMultiProverFactory(universe, config)
				: Prove.newProverFactory(universe, prover);
		ReasonerFactory reasonerFactory = Reason.newReasonerFactory(universe,
				simplifierFactory, proverFactory);

		if (config.getWhy3ProvePlatform() != null) {
			Why3ReasonerFactory why3ReasonerFactory = Reason
					.newWhy3ReasonerFactory(config, universe, simplifierFactory,
							Prove.newWhy3ProvePlatformFactory(universe,
									config.getWhy3ProvePlatform(), config));

			universe.setWhy3ReasonerFactory(why3ReasonerFactory);
		}
		universe.setReasonerFactory(reasonerFactory);
		return universe;
	}

	public static SymbolicUniverse newIdealUniverse() {
		return newIdealUniverse(Configurations.getDefaultConfiguration(), null);
	}

	public static SymbolicUniverse newHerbrandUniverse(SARLConfig config,
			ProverInfo prover) {
		FactorySystem system = PreUniverses.newHerbrandFactorySystem();
		CommonSymbolicUniverse universe = new CommonSymbolicUniverse(system);
		SimplifierFactory simplifierFactory = Simplify
				.newIdentitySimplifierFactory(universe);
		TheoremProverFactory proverFactory = prover == null
				? Prove.newMultiProverFactory(universe, config)
				: Prove.newProverFactory(universe, prover);
		ReasonerFactory reasonerFactory = Reason.newReasonerFactory(universe,
				simplifierFactory, proverFactory);

		if (config.getWhy3ProvePlatform() != null) {
			Why3ReasonerFactory why3ReasonerFactory = Reason
					.newWhy3ReasonerFactory(config, universe, simplifierFactory,
							Prove.newWhy3ProvePlatformFactory(universe,
									config.getWhy3ProvePlatform(), config));

			universe.setWhy3ReasonerFactory(why3ReasonerFactory);
		}

		universe.setReasonerFactory(reasonerFactory);
		return universe;
	}

	public static SymbolicUniverse newHerbrandUniverse() {
		return newHerbrandUniverse(Configurations.getDefaultConfiguration(),
				null);
	}

	public static SymbolicUniverse newStandardUniverse(SARLConfig config,
			ProverInfo prover) {
		NumberFactory numberFactory = Numbers.REAL_FACTORY;
		ObjectFactory objectFactory = Objects.newObjectFactory(numberFactory);
		SymbolicTypeFactory typeFactory = Types.newTypeFactory(objectFactory);
		ExpressionFactory expressionFactory = Expressions
				.newStandardExpressionFactory(numberFactory, objectFactory,
						typeFactory);
		FactorySystem system = PreUniverses.newFactorySystem(objectFactory,
				typeFactory, expressionFactory);
		CommonSymbolicUniverse universe = new CommonSymbolicUniverse(system);
		SimplifierFactory simplifierFactory = Expressions
				.standardSimplifierFactory(expressionFactory, universe);
		TheoremProverFactory proverFactory = prover == null
				? Prove.newMultiProverFactory(universe, config)
				: Prove.newProverFactory(universe, prover);
		ReasonerFactory reasonerFactory = Reason.newReasonerFactory(universe,
				simplifierFactory, proverFactory);

		if (config.getWhy3ProvePlatform() != null) {
			Why3ReasonerFactory why3ReasonerFactory = Reason
					.newWhy3ReasonerFactory(config, universe, simplifierFactory,
							Prove.newWhy3ProvePlatformFactory(universe,
									config.getWhy3ProvePlatform(), config));

			universe.setWhy3ReasonerFactory(why3ReasonerFactory);
		}
		universe.setReasonerFactory(reasonerFactory);
		return universe;
	}

	public static SymbolicUniverse newStandardUniverse() {
		return newStandardUniverse(Configurations.getDefaultConfiguration(),
				null);
	}

}
