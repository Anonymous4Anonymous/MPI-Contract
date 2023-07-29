package edu.udel.cis.vsl.civl.semantics.common;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.model.IF.SystemFunction;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;

public class FunctionCallExecutor extends CommonExecutor {

	public FunctionCallExecutor(ModelFactory modelFactory,
			StateFactory stateFactory, LibraryExecutorLoader loader,
			Evaluator evaluator, SymbolicAnalyzer symbolicAnalyzer,
			CIVLErrorLogger errorLogger, CIVLConfiguration civlConfig) {
		super(modelFactory, stateFactory, loader, evaluator, symbolicAnalyzer,
				errorLogger, civlConfig);
	}

	Evaluation evaluateAtomicPureFunction(State state, int pid,
			CallOrSpawnStatement call)
			throws UnsatisfiablePathConditionException {
		return this.executeSystemFunctionCall(state, pid, call,
				(SystemFunction) call.function());
	}
}
