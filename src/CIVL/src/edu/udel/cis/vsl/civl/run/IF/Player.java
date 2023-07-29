package edu.udel.cis.vsl.civl.run.IF;

import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.errorBoundO;
import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.maxdepthO;
import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.minO;
import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.randomO;
import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.solveO;
import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.statsBar;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants.DeadlockKind;
import edu.udel.cis.vsl.civl.dynamic.IF.Dynamics;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.kripke.IF.CIVLStateManager;
import edu.udel.cis.vsl.civl.kripke.IF.Enabler;
import edu.udel.cis.vsl.civl.kripke.IF.Kripkes;
import edu.udel.cis.vsl.civl.kripke.IF.LibraryEnablerLoader;
import edu.udel.cis.vsl.civl.log.IF.CIVLErrorLogger;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.predicate.IF.AndPredicate;
import edu.udel.cis.vsl.civl.predicate.IF.CIVLStatePredicate;
import edu.udel.cis.vsl.civl.predicate.IF.Predicates;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.Executor;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryExecutorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.Semantics;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.semantics.common.LogicFunctionInterpretor;
import edu.udel.cis.vsl.civl.state.IF.MemoryUnitFactory;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.StateFactory;
import edu.udel.cis.vsl.civl.state.IF.States;
import edu.udel.cis.vsl.gmc.CommandLineException;
import edu.udel.cis.vsl.gmc.GMCConfiguration;
import edu.udel.cis.vsl.gmc.seq.EnablerIF;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;

/**
 * Base class for various tools that require executing a CIVL model. It provides
 * some of the services needed by most such tools. A concrete tool can extend
 * this class, or delegate to an instance of it.
 * 
 * @author Stephen F. Siegel
 * 
 */
public abstract class Player {

	protected GMCConfiguration config;

	protected Model model;

	protected String sessionName;

	protected ModelFactory modelFactory;

	protected StateFactory stateFactory;

	protected MemoryUnitFactory memUnitFactory;

	protected Evaluator evaluator;

	protected EnablerIF<State, Transition> enabler;

	protected CIVLStatePredicate predicate = null;

	protected LibraryEnablerLoader libraryEnablerLoader;

	protected LibraryExecutorLoader libraryExecutorLoader;

	protected LibraryEvaluatorLoader libraryEvaluatorLoader;

	protected Executor executor;

	protected CIVLStateManager stateManager;

	protected boolean random;

	protected String result;

	protected boolean minimize;

	protected int maxdepth;

	protected boolean solve; // false by default

	protected boolean gui; // false by default, only works with Replay mode.

	protected SymbolicUtility symbolicUtil;

	protected SymbolicAnalyzer symbolicAnalyzer;

	protected CIVLErrorLogger log;

	protected CIVLConfiguration civlConfig;

	public Player(GMCConfiguration gmcConfig, Model model, PrintStream out,
			PrintStream err, boolean collectOutputs)
			throws CommandLineException {
		SymbolicUniverse universe;

		this.config = gmcConfig;
		this.model = model;
		civlConfig = new CIVLConfiguration(gmcConfig.getAnonymousSection());
		gmcConfig.setPrintTransition(civlConfig.showTransitions());
		gmcConfig.setQuiet(civlConfig.isQuiet());
		gmcConfig.setSaveStates(civlConfig.saveStates());
		if (civlConfig.isQuiet()) {
			PrintStream dump = new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					// doing nothing
				}
			});
			civlConfig.setOut(dump);
			civlConfig.setErr(dump);
			gmcConfig.setPrintStream(dump);
		} else {
			civlConfig.setOut(out);
			civlConfig.setErr(err);
			gmcConfig.setPrintStream(out);
		}
		// civlConfig.setOut(out);
		// civlConfig.setErr(err);
		civlConfig.setCollectOutputs(collectOutputs);
		this.sessionName = model.name();
		this.modelFactory = model.factory();
		universe = modelFactory.universe();
		// Set the probabilistic bound to zero if 'prob' option is disabled:
		if (!civlConfig.prob())
			universe.setProbabilisticBound(
					universe.numberFactory().zeroRational());
		this.solve = (Boolean) gmcConfig.getAnonymousSection()
				.getValueOrDefault(solveO);
		this.memUnitFactory = States.newImmutableMemoryUnitFactory(universe);
		this.stateFactory = States.newImmutableStateFactory(modelFactory,
				memUnitFactory, civlConfig);
		this.symbolicUtil = Dynamics.newSymbolicUtility(universe, modelFactory,
				stateFactory);
		this.stateFactory.setSymbolicUtility(symbolicUtil);
		this.log = new CIVLErrorLogger(new File("CIVLREP"), sessionName, out,
				civlConfig, gmcConfig, this.stateFactory, universe, solve);
		this.log.setErrorBound((int) gmcConfig.getAnonymousSection()
				.getValueOrDefault(errorBoundO));
		this.libraryEvaluatorLoader = Semantics
				.newLibraryEvaluatorLoader(this.civlConfig);
		this.symbolicAnalyzer = Semantics.newSymbolicAnalyzer(this.civlConfig,
				this.log, universe, modelFactory, symbolicUtil);
		this.libraryExecutorLoader = Semantics.newLibraryExecutorLoader(
				this.libraryEvaluatorLoader, this.civlConfig);
		this.libraryEnablerLoader = Kripkes.newLibraryEnablerLoader(
				this.libraryEvaluatorLoader, this.civlConfig);
		this.evaluator = Semantics.newEvaluator(modelFactory, stateFactory,
				libraryEvaluatorLoader, libraryExecutorLoader, symbolicUtil,
				symbolicAnalyzer, memUnitFactory, log, this.civlConfig);
		this.executor = Semantics.newExecutor(modelFactory, stateFactory,
				libraryExecutorLoader, evaluator, symbolicAnalyzer, log,
				civlConfig);
		this.enabler = Kripkes.newEnabler(stateFactory, this.evaluator,
				executor, symbolicAnalyzer, memUnitFactory,
				this.libraryEnablerLoader, log, civlConfig, gmcConfig);
		this.random = gmcConfig.getAnonymousSection().isTrue(randomO);
		this.minimize = gmcConfig.getAnonymousSection().isTrue(minO);
		this.maxdepth = (int) gmcConfig.getAnonymousSection()
				.getValueOrDefault(maxdepthO);
		if (civlConfig.deadlock() == DeadlockKind.ABSOLUTE) {
			this.addPredicate(Predicates.newDeadlock(universe,
					(Enabler) this.enabler, symbolicAnalyzer));
		} else if (civlConfig.deadlock() == DeadlockKind.POTENTIAL) {
			this.addPredicate(Predicates.newPotentialDeadlock(universe,
					(Enabler) this.enabler, libraryEnablerLoader,
					this.evaluator, modelFactory, symbolicUtil,
					symbolicAnalyzer));
		} else {
			this.addPredicate(Predicates.newTrivialPredicate());
		}
		stateManager = Kripkes.newStateManager((Enabler) enabler, executor,
				symbolicAnalyzer, log, civlConfig);
		universe.setErrFile("CIVLREP/" + sessionName + "_ProverOutput.txt");
		// use evaluator to evaluate constant values of logic functions:
		// make up a dummy state and process ID:
		universe.setLogicFunctions(LogicFunctionInterpretor
				.evaluateLogicFunctions(model.getAllLogicFunctions(),
						Semantics.newErrorSideEffectFreeEvaluator(modelFactory,
								stateFactory, libraryEvaluatorLoader,
								libraryExecutorLoader, symbolicUtil,
								symbolicAnalyzer, memUnitFactory, log,
								civlConfig),
						stateFactory));
	}

	// protected CIVLExecutionException getCurrentViolation() {
	// CIVLExecutionException violation = null;
	//
	// for (CIVLStatePredicate predicate : this.predicates) {
	// violation = predicate.getUnreportedViolation();
	// if (violation != null)
	// break;
	// }
	// return violation;
	// }

	public void printResult() {
		civlConfig.out().println(statsBar + " Result " + statsBar);
		civlConfig.out().println(result);
	}

	public void addPredicate(CIVLStatePredicate newPredicate) {
		if (this.predicate == null)
			this.predicate = newPredicate;
		else {
			if (!this.predicate.isAndPredicate()) {
				this.predicate = Predicates.newAndPredicate(this.predicate);
			}
			((AndPredicate) this.predicate).addClause(newPredicate);
		}
	}

	public CIVLStateManager stateManager() {
		return this.stateManager;
	}
}
