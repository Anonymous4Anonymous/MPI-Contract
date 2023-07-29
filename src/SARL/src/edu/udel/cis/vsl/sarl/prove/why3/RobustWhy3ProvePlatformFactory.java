package edu.udel.cis.vsl.sarl.prove.why3;

import edu.udel.cis.vsl.sarl.IF.config.ProverInfo;
import edu.udel.cis.vsl.sarl.IF.config.ProverInfo.ProverKind;
import edu.udel.cis.vsl.sarl.IF.config.SARLConfig;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.prove.IF.ProverFunctionInterpretation;
import edu.udel.cis.vsl.sarl.prove.IF.TheoremProver;
import edu.udel.cis.vsl.sarl.prove.IF.TheoremProverFactory;

public class RobustWhy3ProvePlatformFactory implements TheoremProverFactory {

	/**
	 * The symbolic universe used for managing symbolic expressions. Initialized
	 * by constructor and never changes.
	 */
	private PreUniverse universe;

	/**
	 * Information object for underlying prover, which must have
	 * {@link ProverKind} {@link ProverKind#Why3}.
	 */
	private ProverInfo prover;

	private SARLConfig config;

	/**
	 * Constructs new Why3 prover platform factory with the given symbolic
	 * universe.
	 * 
	 * @param universe
	 *            symbolic universe used to manage symbolic expressions
	 * @param prover
	 *            information object for underlying prover, which must have
	 *            {@link ProverKind} {@link ProverKind#Why3}
	 */
	public RobustWhy3ProvePlatformFactory(PreUniverse universe,
			ProverInfo prover, SARLConfig config) {
		this.universe = universe;
		this.prover = prover;
		this.config = config;
	}

	@Override
	public RobustWhy3ProvePlatform newProver(BooleanExpression context) {
		return new RobustWhy3ProvePlatform(config, universe, prover, context,
				new ProverFunctionInterpretation[0]);
	}

	@Override
	public TheoremProver newProver(BooleanExpression context,
			ProverFunctionInterpretation[] ppreds) {
		RobustWhy3ProvePlatform why3prover = new RobustWhy3ProvePlatform(config,
				universe, prover, context, ppreds);

		return why3prover;
	}
}
