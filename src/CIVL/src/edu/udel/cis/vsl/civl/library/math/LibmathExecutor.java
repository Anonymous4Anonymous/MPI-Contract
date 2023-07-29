package edu.udel.cis.vsl.civl.library.math;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.library.common.BaseLibraryExecutor;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Executor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.Number;

public class LibmathExecutor extends BaseLibraryExecutor
		implements
			LibraryExecutor {

	public LibmathExecutor(String name, Executor primaryExecutor,
			ModelFactory modelFactory, SymbolicUtility symbolicUtil,
			SymbolicAnalyzer symbolicAnalyzer, CIVLConfiguration civlConfig,
			LibraryExecutorLoader libExecutorLoader,
			LibraryEvaluatorLoader libEvaluatorLoader) {
		super(name, primaryExecutor, modelFactory, symbolicUtil,
				symbolicAnalyzer, civlConfig, libExecutorLoader,
				libEvaluatorLoader);
	}

	@Override
	protected Evaluation executeValue(State state, int pid, String process,
			CIVLSource source, String functionName, Expression[] arguments,
			SymbolicExpression[] argumentValues)
			throws UnsatisfiablePathConditionException {
		Evaluation callEval = null;

		switch (functionName) {
			case "ceil" :
			case "ceilf" :
			case "ceill" :
				callEval = executeCeil(state, pid, process, source, arguments,
						argumentValues);
				break;
			case "floor" :
			case "floorf" :
			case "floorl" :
				callEval = executeFloor(state, pid, process, source, arguments,
						argumentValues);
				break;
			default :
				throw new CIVLInternalException(
						"Unknown civl-mpi function: " + name, source);
		}
		return callEval;
	}

	/**
	 * Executes the series of "floor" functions in C11 math library. Including
	 * <code>floor, floorf, and floorl</code>. The execution returns the
	 * {@link SymbolicUniverse#floor(NumericExpression)} of the sole argument.
	 */
	private Evaluation executeFloor(State state, int pid, String process,
			CIVLSource source, Expression[] arguments,
			SymbolicExpression[] argumentValues) {
		NumericExpression ret;
		NumericExpression number = (NumericExpression) argumentValues[0];
		Number concreteValue = universe.extractNumber(number);

		if (concreteValue == null) {
			Reasoner reasoner = universe
					.reasoner(state.getPathCondition(universe));

			concreteValue = reasoner.extractNumber(number);
		}
		if (concreteValue != null)
			// The number has a concrete value:
			ret = universe.floor(universe.number(concreteValue));
		else
			ret = universe.floor(number);
		ret = (NumericExpression) universe.cast(universe.realType(), ret);
		return new Evaluation(state, ret);
	}

	/**
	 * Executes the series of "ceil" functions in C11 math library. Including
	 * <code>ceil, ceilf, and ceill</code>. The execution returns the
	 * {@link SymbolicUniverse#ceil(NumericExpression)} of the sole argument.
	 */
	private Evaluation executeCeil(State state, int pid, String process,
			CIVLSource source, Expression[] arguments,
			SymbolicExpression[] argumentValues) {
		NumericExpression ret;
		NumericExpression number = (NumericExpression) argumentValues[0];
		Number concreteValue = universe.extractNumber(number);

		if (concreteValue == null) {
			Reasoner reasoner = universe
					.reasoner(state.getPathCondition(universe));

			concreteValue = reasoner.extractNumber(number);
		}
		if (concreteValue != null)
			// The number has a concrete value:
			ret = universe.ceil(universe.number(concreteValue));
		else
			ret = universe.ceil(number);
		ret = (NumericExpression) universe.cast(universe.realType(), ret);
		return new Evaluation(state, ret);
	}
}
