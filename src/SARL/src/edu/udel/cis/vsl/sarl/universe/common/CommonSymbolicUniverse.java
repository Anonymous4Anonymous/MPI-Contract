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
package edu.udel.cis.vsl.sarl.universe.common;

import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.ValidityResult.ResultType;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.expr.IF.NumericExpressionFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.FactorySystem;
import edu.udel.cis.vsl.sarl.preuniverse.common.CommonPreUniverse;
import edu.udel.cis.vsl.sarl.prove.IF.ProverFunctionInterpretation;
import edu.udel.cis.vsl.sarl.reason.IF.ReasonerFactory;
import edu.udel.cis.vsl.sarl.reason.common.Why3ReasonerFactory;
import edu.udel.cis.vsl.sarl.util.autotg.TestTranslator;

/**
 * A standard implementation of {@link SymbolicUniverse}, relying heavily on a
 * given {@link NumericExpressionFactory} for dealing with numeric issues and a
 * BooleanExpressionFactory for dealing with boolean expressions.
 * 
 * @author xxxx
 */
public class CommonSymbolicUniverse extends CommonPreUniverse
		implements SymbolicUniverse {

	/**
	 * The factory for producing new Reasoner instances.
	 */
	private ReasonerFactory reasonerFactory;

	/**
	 * The factory for producing new {@link Why3Reasoner} instances.
	 */
	private Why3ReasonerFactory why3ReasonerFactory = null;

	/**
	 * A reference to a {@link TestTranslator}, which is instantiated when
	 * {@link #enableSARLTestGeneration(boolean)} is called with argument equals
	 * to true.
	 */
	private TestTranslator sarlTestGenerator = null;

	/**
	 * A list of logic function definitions that will be sent to a reasoner
	 * whenever a reasoner is created because a prover query may invloves logic
	 * functions.
	 */
	private ProverFunctionInterpretation logicFunctions[] = new ProverFunctionInterpretation[0];

	// Constructor...

	/**
	 * Constructs a new CommonSymbolicUniverse from the given system of
	 * factories.
	 * 
	 * @param system
	 *            a factory system
	 */
	public CommonSymbolicUniverse(FactorySystem system) {
		super(system);
	}

	// Helper methods...

	@Override
	public Reasoner reasoner(BooleanExpression context) {
		return reasonerFactory.getReasoner(context,
				getUseBackwardSubstitution(), logicFunctions);
	}

	public void setReasonerFactory(ReasonerFactory reasonerFactory) {
		this.reasonerFactory = reasonerFactory;
	}

	@Override
	public Number extractNumber(BooleanExpression assumption,
			NumericExpression expression) {
		Number result = extractNumber(expression);

		if (result != null)
			return result;
		return reasoner(assumption).extractNumber(expression);
	}

	@Override
	public Reasoner why3Reasoner(BooleanExpression context) {
		if (why3ReasonerFactory == null)
			return reasoner(context);
		else
			return why3ReasonerFactory.getReasoner(context,
					getUseBackwardSubstitution(), logicFunctions);
	}

	public void setWhy3ReasonerFactory(Why3ReasonerFactory reasonerFactory) {
		this.why3ReasonerFactory = reasonerFactory;
	}

	@Override
	public void enableSARLTestGeneration(boolean enable) {
		if (enable) {
			if (sarlTestGenerator == null)
				sarlTestGenerator = new TestTranslator();
		} else
			sarlTestGenerator = null;
	}

	@Override
	public void saveValidCallAsSARLTest(BooleanExpression context,
			BooleanExpression predicate, ResultType expectedResult,
			boolean useWhy3, String testName, String... comments) {
		if (sarlTestGenerator != null)
			sarlTestGenerator.generateValidCheckMethod(context, predicate,
					expectedResult, useWhy3, testName, comments);
	}

	@Override
	public void generateTestClass(String name) {
		if (sarlTestGenerator != null)
			sarlTestGenerator.generateTestClass(name);
	}

	@Override
	public void setLogicFunctions(
			ProverFunctionInterpretation[] logicFunctions) {
		if (logicFunctions != null)
			this.logicFunctions = logicFunctions;
		else
			this.logicFunctions = new ProverFunctionInterpretation[0];
	}
}
