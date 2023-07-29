package edu.udel.cis.vsl.sarl.reason.common;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

/**
 * Transforms some special SARL expresisons (e.g. array lambda, sigma
 * expression) to forms that can be accepted by all provers.
 * 
 * @author xxxx
 */
class SARLProverAdaptor {

	private PreUniverse pu;

	/* **** Specific Transformers for Adapting Prover Languages *****/
	private StatefulArrayLambdaAdaptor arrayLambdaAdaptor;

	private StatefulSigmaAdaptor sigmaAdaptor;

	SARLProverAdaptor(PreUniverse pu) {
		this.pu = pu;
		this.arrayLambdaAdaptor = new StatefulArrayLambdaAdaptor(pu);
		this.sigmaAdaptor = new StatefulSigmaAdaptor(pu);
	}

	BooleanExpression apply(BooleanExpression formula) {
		formula = (BooleanExpression) arrayLambdaAdaptor.apply(formula);
		return (BooleanExpression) sigmaAdaptor.apply(formula);
	}

	BooleanExpression getAxioms() {
		return pu.and(arrayLambdaAdaptor.getIndependentArrayLambdaAxioms(),
				pu.and(sigmaAdaptor.getAxioms()));
	}
}
