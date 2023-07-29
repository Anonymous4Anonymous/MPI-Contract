package edu.udel.cis.vsl.civl.config.IF;

import java.io.PrintStream;
import java.util.Map;

import edu.udel.cis.vsl.civl.config.IF.CIVLConstants.DeadlockKind;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants.ErrorStateEquivalence;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.gmc.GMCSection;

/**
 * A CIVLConfiguration object encompasses all the parameters used to configure
 * CIVL for the execution of one or more tasks. It provides methods to get and
 * set these parameters. The types of the parameters are all simple Java types,
 * such as boolean or int, so this class does not use any other CIVL classes
 * 
 * @author xxxx
 * 
 */
public class CIVLConfiguration {

	/**
	 * What kind of deadlocks should CIVL search for?
	 */
	private DeadlockKind deadlock = DeadlockKind.ABSOLUTE;

	private boolean checkDivisionByZero = true;

	/**
	 * Should CIVL run in "debug" mode, printing lots and lots of output?
	 */
	private boolean debug = false;

	/**
	 * Should CIVL actually print the stuff that the program it is analyzing
	 * sends to <code>stdout</code>? Could lead to a lot of printing, esp. when
	 * searching a state space, which may entail executing the same statement
	 * over and over again.
	 */
	private boolean enablePrintf = true;

	/**
	 * Should CIVL save some states as it searches, as opposed to doing a
	 * "stateless" search? Even if this is true, CIVL will not necessarily save
	 * every state, only important ones that have a chance of being encountered
	 * again in the search.
	 */
	private boolean saveStates = true;

	/**
	 * Should CIVL show the Abstract Syntax Tree (AST) that it produces from
	 * parsing the source code (before any transformations)?
	 */
	private boolean showAST = false;

	/**
	 * Should CIVL show the ample set when there are more than one processes in
	 * the ample set?
	 */
	private boolean showAmpleSet = false;

	/**
	 * Should CIVL show the ample set with the state when there are more than
	 * one processes in the ample set?
	 */
	private boolean showAmpleSetWtStates = false;

	/**
	 * Should CIVL show the CIVL model of the program?
	 */
	private boolean showModel = false;

	/**
	 * Should CIVL show states that are saved?
	 */
	private boolean showSavedStates = false;

	/**
	 * Should CIVL show all states, including saved states and intermediate
	 * states?
	 */
	private boolean showStates = false;

	/**
	 * Should CIVL show all transitions?
	 */
	private boolean showTransitions = false;

	/**
	 * Should CIVL show unreachable code?
	 */
	private boolean showUnreach = false;

	/**
	 * Should CIVL simplify states using the path condition?
	 */
	private boolean simplify = true;

	/**
	 * Is printf stateless?
	 */
	private boolean statelessPrintf = true;

	/**
	 * verbose mode?
	 */
	private boolean verbose = false;

	/**
	 * Is svcomp transformation needed?
	 */
	private boolean svcomp16 = false;

	private boolean svcomp17 = false;

	/**
	 * Should CIVL show the program after all applicable transformations?
	 */
	private boolean showProgram = false;

	/**
	 * Disable OpenMP simplifier?
	 */
	private boolean ompNoSimplify = false;

	/**
	 * Only relies on the OpenMP simplifier?
	 */
	private boolean ompOnlySimplifier = false;

	/**
	 * The output stream
	 */
	private PrintStream out;

	/**
	 * The print stream for errors.
	 */
	private PrintStream err;

	/**
	 * Should CIVL show the path condition of each state?
	 */
	private String showPathConditon = "NONE";

	/**
	 * Should CIVL delete terminated processes and renumber all processes?
	 */
	private boolean collectProcesses = true;

	/**
	 * Should CIVL delete invalid dyscopes and renumber all dyscopes?
	 */
	private boolean collectScopes = true;

	private boolean collectSymbolicNames = true;

	/**
	 * Should CIVL collect heap objects?
	 */
	private boolean collectHeaps = true;

	/**
	 * Is this run for CIVL web interface?
	 */
	private boolean web = false;

	/**
	 * Should CIVL show the preprocessing result?
	 */
	private boolean showPreproc = false;

	/**
	 * Should CIVL show the list of input variables of the model?
	 */
	private boolean showInputVars = false;

	/**
	 * Should CIVL show the time usage for each translation phase?
	 */
	private boolean showTime = false;

	/**
	 * Should CIVL show the impact/reachable memory units of processes at each
	 * state?
	 */
	private boolean showMemoryUnits = false;

	/**
	 * Should CIVL ignore the output after executing the command line.
	 */
	private boolean quiet = false;

	/**
	 * The upper bound of integer.
	 */
	private int intBit = 32;

	/**
	 * apply int operation transformer? Default false.
	 */
	private boolean intOperationTransiformer = false;

	/**
	 * Should CIVL perform a slice analysis on the error trace.
	 */
	private boolean sliceAnalysis = false;

	/**
	 * Should CIVL generate a witness from the error trace.
	 */
	private boolean witness = false;

	/**
	 * Should CIVL tell SARL to use probabilistic techniques for verifying
	 * identities ?
	 */
	private boolean prob = false;

	/**
	 * The maximal number of processes allowed in a state. -1 means infinitely
	 * many processes are allowed.
	 */
	private int procBound = -1;

	private int maxProcs = 100;

	/**
	 * The loop decomposition strategy for OpenMP transformer, round robin by
	 * default.
	 */
	private int ompLoopDecomp = ModelConfiguration.DECOMP_ROUND_ROBIN;

	/**
	 * The error state equivalence semantics to suppress logging of redundant
	 * error states. All equivalences use the "kind" of error, but they vary in
	 * the portions of the state considered. LOC by default.
	 */
	private ErrorStateEquivalence errorStateEquiv = ErrorStateEquivalence.LOC;

	/**
	 * Direct symbolic execution based on file designating branches to direct
	 * and how to subset their outcomes.
	 */
	private String directSymEx = null;

	/**
	 * Is the current command replay? Not replay by default.
	 */
	private boolean isReplay = false;

	private boolean absAnalysis = false;

	private Map<String, Object> inputVariables;

	private boolean collectOutputs = false;

	/**
	 * If CIVL enables "MPI CONTRACT" mode
	 */
	private String mpiContractFunction = null;

	private boolean checkMemoryLeak = true;

	private int timeout = -1;

	private boolean unpreproc = false;

	private boolean checkExpressionError = true;

	private boolean isInSubprogram = false;

	/**
	 * set to true iff loop invariant option is enabled.
	 */
	private boolean loopInvariantEnabled = false;

	/**
	 * set to true iff CIVL will generate Junit test class for SARL:
	 */
	private boolean generateTestsForSARL;

	/**
	 * whether report cycles in state space as violations
	 */
	private boolean cyclesViolate;

	/**
	 * whether updater thread is in used for printing update info periodically
	 */
	private boolean runtimeUpdate;

	/**
	 * Constructs a new CIVL configuration object from the command line
	 * configuration.
	 * 
	 * @param config
	 *            The command line configuration.
	 */
	public CIVLConfiguration(GMCSection config) {
		String deadlockString = (String) config
				.getValue(CIVLConstants.deadlockO);
		String ompLoopDecompString = (String) config
				.getValue(CIVLConstants.ompLoopDecompO);
		String errorStateEquivString = (String) config
				.getValue(CIVLConstants.errorStateEquivO);

		if (ompLoopDecompString != null) {
			switch (ompLoopDecompString) {
				case "ALL" :
					this.setOmpLoopDecomp(ModelConfiguration.DECOMP_ALL);
					break;
				case "ROUND_ROBIN" :
					this.setOmpLoopDecomp(
							ModelConfiguration.DECOMP_ROUND_ROBIN);
					break;
				case "RANDOM" :
					this.setOmpLoopDecomp(ModelConfiguration.DECOMP_RANDOM);
					break;
				default :
					throw new CIVLInternalException(
							"invalid OpenMP loop decomposition strategy "
									+ deadlockString,
							(CIVLSource) null);
			}
		}
		if (deadlockString != null)
			switch (deadlockString) {
				case "absolute" :
					this.deadlock = DeadlockKind.ABSOLUTE;
					break;
				case "potential" :
					this.deadlock = DeadlockKind.POTENTIAL;
					break;
				case "none" :
					this.deadlock = DeadlockKind.NONE;
					break;
				default :
					throw new CIVLInternalException(
							"invalid deadlock kind " + deadlockString,
							(CIVLSource) null);
			}
		if (errorStateEquivString != null)
			switch (errorStateEquivString) {
				case "LOC" :
					this.errorStateEquiv = ErrorStateEquivalence.LOC;
					break;
				case "CALLSTACK" :
					this.errorStateEquiv = ErrorStateEquivalence.CALLSTACK;
					break;
				case "FULL" :
					this.errorStateEquiv = ErrorStateEquivalence.FULL;
					break;
				default :
					throw new CIVLInternalException(
							"invalid error state equivalence"
									+ errorStateEquivString,
							(CIVLSource) null);
			}
		this.intOperationTransiformer = config
				.isTrue(CIVLConstants.intOperationTransformer);
		this.setShowMemoryUnits(config.isTrue(CIVLConstants.showMemoryUnitsO));
		this.debug = config.isTrue(CIVLConstants.debugO);
		this.enablePrintf = config.isTrue(CIVLConstants.enablePrintfO);
		this.saveStates = config.isTrue(CIVLConstants.saveStatesO);
		this.showAmpleSet = config.isTrue(CIVLConstants.showAmpleSetO);
		this.showAmpleSetWtStates = config
				.isTrue(CIVLConstants.showAmpleSetWtStatesO);
		this.showSavedStates = config.isTrue(CIVLConstants.showSavedStatesO);
		this.showStates = config.isTrue(CIVLConstants.showStatesO);
		this.showTransitions = config.isTrue(CIVLConstants.showTransitionsO);
		this.setShowUnreach(config.isTrue(CIVLConstants.showUnreachedCodeO));
		this.setAbsAnalysis(config.isTrue(CIVLConstants.analyzeAbsO));
		this.simplify = config.isTrue(CIVLConstants.simplifyO);
		this.statelessPrintf = config.isTrue(CIVLConstants.statelessPrintfO);
		this.verbose = config.isTrue(CIVLConstants.verboseO);
		this.svcomp16 = config.isTrue(CIVLConstants.svcomp16O);
		this.svcomp17 = config.isTrue(CIVLConstants.svcomp17O);
		this.setShowProgram(config.isTrue(CIVLConstants.showProgramO));
		this.showPathConditon = (String) config
				.getValue(CIVLConstants.showPathConditionO);
		if (this.showPathConditon == null)
			showPathConditon = "NONE";
		this.ompNoSimplify = config.isTrue(CIVLConstants.ompNoSimplifyO);
		this.ompOnlySimplifier = config
				.isTrue(CIVLConstants.ompOnlySimplifierO);
		this.collectProcesses = config.isTrue(CIVLConstants.collectProcessesO);
		this.collectScopes = config.isTrue(CIVLConstants.collectScopesO);
		this.setCollectHeaps(config.isTrue(CIVLConstants.collectHeapsO));
		this.web = config.isTrue(CIVLConstants.webO);
		this.setShowPreproc(config.isTrue(CIVLConstants.preprocO));
		this.setShowAST(config.isTrue(CIVLConstants.astO));
		this.setShowModel(config.isTrue(CIVLConstants.showModelO));
		this.showInputVars = config.isTrue(CIVLConstants.showInputVarsO);
		this.setUnpreproc(config.isTrue(CIVLConstants.unpreprocO));
		this.showTime = config.isTrue(CIVLConstants.showTimeO);
		this.procBound = (Integer) config
				.getValueOrDefault(CIVLConstants.procBoundO);
		this.intBit = (Integer) config.getValueOrDefault(CIVLConstants.intBit);
		this.setInputVariables(config.getMapValue(CIVLConstants.inputO));
		this.collectOutputs = config.isTrue(CIVLConstants.collectOutputO);
		this.maxProcs = (Integer) config
				.getValueOrDefault(CIVLConstants.maxProcsO);
		this.setMpiContractFunction(
				(String) config.getValueOrDefault(CIVLConstants.mpiContractO));
		if (this.isEnableMpiContract())
			this.intOperationTransiformer = false;
		this.loopInvariantEnabled = config.isTrue(CIVLConstants.loopO);
		this.collectSymbolicNames = config
				.isTrue(CIVLConstants.collectSymbolicConstantsO)
				|| loopInvariantEnabled;
		this.setCheckDivisionByZero(
				config.isTrue(CIVLConstants.checkDivisionByZeroO));
		this.checkMemoryLeak = config.isTrue(CIVLConstants.checkMemoryLeakO);
		this.setTimeout((int) config.getValueOrDefault(CIVLConstants.timeoutO));
		this.quiet = config.isTrue(CIVLConstants.quietO);
		this.sliceAnalysis = config.isTrue(CIVLConstants.sliceAnalysisO);
		this.witness = config.isTrue(CIVLConstants.witnessO);
		this.prob = config.isTrue(CIVLConstants.probO);
		this.cyclesViolate = config.isTrue(CIVLConstants.cyclesViolateO);
		if (config.getValue(CIVLConstants.SARLTestGenO) != null)
			this.generateTestsForSARL = (boolean) config
					.getValue(CIVLConstants.SARLTestGenO);
		else
			this.generateTestsForSARL = (boolean) CIVLConstants.SARLTestGenO
					.defaultValue();
		if (this.svcomp16) {
			if (config.getValue(CIVLConstants.checkMemoryLeakO) == null)
				this.checkMemoryLeak = false;
			if (config.getValue(CIVLConstants.collectHeapsO) == null)
				this.collectHeaps = false;
			if (config.getValue(CIVLConstants.simplifyO) == null)
				this.simplify = false;
			if (config.getValue(CIVLConstants.deadlockO) == null)
				this.deadlock = DeadlockKind.NONE;
			if (config.getValue(CIVLConstants.procBoundO) == null)
				this.procBound = 6;
		}
		if (this.svcomp17) {
			if (config.getValue(CIVLConstants.checkMemoryLeakO) == null)
				this.checkMemoryLeak = false;
			if (config.getValue(CIVLConstants.collectHeapsO) == null)
				this.collectHeaps = false;
			if (config.getValue(CIVLConstants.simplifyO) == null)
				this.simplify = false;
			if (config.getValue(CIVLConstants.deadlockO) == null)
				this.deadlock = DeadlockKind.NONE;
			if (config.getValue(CIVLConstants.procBoundO) == null)
				this.procBound = 6;
			this.intBit = 2;
			this.enablePrintf = false;
			// this.enableIntDivTransformation = false;
			// 32-bit unsigned int bound
		}
		this.directSymEx = (String) config.getValue(CIVLConstants.direct0);
		this.runtimeUpdate = config.isTrue(CIVLConstants.runtimeUpdateO);
	}

	public CIVLConfiguration(CIVLConfiguration config) {
		this.checkDivisionByZero = config.checkDivisionByZero;
		this.checkMemoryLeak = config.checkMemoryLeak;
		this.absAnalysis = config.absAnalysis;
		this.collectHeaps = config.collectHeaps;
		this.collectOutputs = config.collectOutputs;
		this.collectProcesses = config.collectProcesses;
		this.collectScopes = config.collectScopes;
		this.collectSymbolicNames = config.collectSymbolicNames;
		this.deadlock = config.deadlock;
		this.debug = config.debug;
		this.err = config.err;
		this.enablePrintf = config.enablePrintf;
		this.errorStateEquiv = config.errorStateEquiv;
		this.isReplay = config.isReplay;
		this.mpiContractFunction = config.mpiContractFunction;
		this.ompLoopDecomp = config.ompLoopDecomp;
		this.ompNoSimplify = config.ompNoSimplify;
		this.ompOnlySimplifier = config.ompOnlySimplifier;
		this.out = config.out;
		this.procBound = config.procBound;
		this.prob = config.prob;
		this.quiet = config.quiet;
		this.saveStates = config.saveStates;
		this.showAmpleSet = config.showAmpleSet;
		this.showAmpleSetWtStates = config.showAmpleSetWtStates;
		this.showAST = config.showAST;
		this.showInputVars = config.showInputVars;
		this.showMemoryUnits = config.showMemoryUnits;
		this.showPathConditon = config.showPathConditon;
		this.showPreproc = config.showPreproc;
		this.showProgram = config.showProgram;
		this.showSavedStates = config.showSavedStates;
		this.showStates = config.showStates;
		this.showTime = config.showTime;
		this.showTransitions = config.showTransitions;
		this.simplify = config.simplify;
		this.timeout = config.timeout;
		this.unpreproc = config.unpreproc;
		this.verbose = config.verbose;
		this.web = config.web;
		this.witness = config.witness;
		this.directSymEx = config.directSymEx;
		this.intBit = config.intBit;
		this.maxProcs = config.maxProcs;
		this.intOperationTransiformer = config.intOperationTransiformer;
		this.runtimeUpdate = config.runtimeUpdate;
	}

	public CIVLConfiguration() {
		// TODO Auto-generated constructor stub
	}

	public void setOut(PrintStream out) {
		this.out = out;
	}

	public void setErr(PrintStream err) {
		this.err = err;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setEnablePrintf(boolean enablePrintf) {
		this.enablePrintf = enablePrintf;
	}

	public void setSaveStates(boolean saveStates) {
		this.saveStates = saveStates;
	}

	public void setShowAmpleSet(boolean showAmpleSet) {
		this.showAmpleSet = showAmpleSet;
	}

	public void setShowAmpleSetWtStates(boolean showAmpleSetWtStates) {
		this.showAmpleSetWtStates = showAmpleSetWtStates;
	}

	public void setShowSavedStates(boolean showSavedStates) {
		this.showSavedStates = showSavedStates;
	}

	public void setShowStates(boolean showStates) {
		this.showStates = showStates;
	}

	public void setShowTransitions(boolean showTransitions) {
		this.showTransitions = showTransitions;
	}

	public void setSimplify(boolean simplify) {
		this.simplify = simplify;
	}

	public void setStatelessPrintf(boolean statelessPrintf) {
		this.statelessPrintf = statelessPrintf;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public boolean debug() {
		return debug;
	}

	public boolean verbose() {
		return verbose;
	}

	public boolean debugOrVerbose() {
		return debug || verbose;
	}

	public boolean enablePrintf() {
		return this.enablePrintf;
	}

	public boolean saveStates() {
		return this.saveStates;
	}

	public boolean showAmpleSet() {
		return this.showAmpleSet;
	}

	public boolean showAmpleSetWtStates() {
		return this.showAmpleSetWtStates;
	}

	public boolean showSavedStates() {
		return this.showSavedStates;
	}

	public boolean showStates() {
		return this.showStates;
	}

	public boolean showTransitions() {
		return this.showTransitions;
	}

	public boolean simplify() {
		return this.simplify;
	}

	public boolean statelessPrintf() {
		return this.statelessPrintf;
	}

	public PrintStream out() {
		return this.out;
	}

	public PrintStream err() {
		return this.err;
	}

	public boolean printTransitions() {
		return this.showTransitions || this.verbose || this.debug;
	}

	public boolean printStates() {
		return this.showStates || this.verbose || this.debug;
	}

	public ErrorStateEquivalence errorStateEquiv() {
		return errorStateEquiv;
	}

	public void setErrorStateEquiv(ErrorStateEquivalence errorStateEquiv) {
		this.errorStateEquiv = errorStateEquiv;
	}

	public String directSymEx() {
		return directSymEx;
	}

	public void setDirectSymEx(String directSymEx) {
		this.directSymEx = directSymEx;
	}

	public boolean sliceAnalysis() {
		return sliceAnalysis;
	}

	public void setSliceAnalysis(boolean sliceAnalysis) {
		this.sliceAnalysis = sliceAnalysis;
	}

	public boolean witness() {
		return witness;
	}

	public void setWitness(boolean witness) {
		this.witness = witness;
	}

	public boolean prob() {
		return prob;
	}

	public void setProb(boolean enableProb) {
		this.prob = enableProb;
	}

	public boolean svcomp() {
		return svcomp16 || svcomp17;
	}

	public boolean svcomp16() {
		return svcomp16;
	}

	public void setSvcomp16(boolean svcomp) {
		this.svcomp16 = svcomp;
	}

	public DeadlockKind deadlock() {
		return deadlock;
	}

	public void setCollectProcesses(boolean collectProcesses) {
		this.collectProcesses = collectProcesses;
	}

	public void setCollectScopes(boolean collectScopes) {
		this.collectScopes = collectScopes;
	}

	public void setDeadlock(DeadlockKind deadlock) {
		this.deadlock = deadlock;
	}

	public boolean showProgram() {
		return showProgram;
	}

	public void setShowProgram(boolean showProgram) {
		this.showProgram = showProgram;
	}

	public boolean showPathConditonAsOneLine() {
		return showPathConditon.equals("LINE");
	}

	public boolean showPathConditonAsMultipleLine() {
		return showPathConditon.equals("BLOCK");
	}

	public boolean ompNoSimplify() {
		return ompNoSimplify;
	}

	public boolean ompOnlySimplifier() {
		return ompOnlySimplifier;
	}

	public void setOmpNoSimplifier(boolean ompNoSimplify) {
		this.ompNoSimplify = ompNoSimplify;
	}

	public void setOmpOnlySimplify(boolean ompOnlySimplify) {
		this.ompOnlySimplifier = ompOnlySimplify;
	}

	public boolean collectProcesses() {
		return this.collectProcesses;
	}

	public boolean collectScopes() {
		return this.collectScopes;
	}

	public boolean collectHeaps() {
		return collectHeaps;
	}

	public boolean web() {
		return web;
	}

	public void setCollectHeaps(boolean collectHeaps) {
		this.collectHeaps = collectHeaps;
	}

	public boolean showPreproc() {
		return showPreproc;
	}

	public void setShowPreproc(boolean showPreproc) {
		this.showPreproc = showPreproc;
	}

	public boolean showAST() {
		return showAST;
	}

	public void setShowAST(boolean showAST) {
		this.showAST = showAST;
	}

	public boolean showModel() {
		return showModel;
	}

	public void setShowModel(boolean showModel) {
		this.showModel = showModel;
	}

	public boolean showInputVars() {
		return showInputVars;
	}

	public void setShowInputVars(boolean showInputVars) {
		this.showInputVars = showInputVars;
	}

	public boolean showTime() {
		return showTime;
	}

	public void setShowTime(boolean showTime) {
		this.showTime = showTime;
	}

	public boolean showMemoryUnits() {
		return showMemoryUnits;
	}

	public void setShowMemoryUnits(boolean showMemoryUnits) {
		this.showMemoryUnits = showMemoryUnits;
	}

	/**
	 * returns the maximal number of processes allowed in a state. -1 means
	 * infinitely many processes are allowed.
	 */
	public int getProcBound() {
		return procBound;
	}

	public void setProcBound(int value) {
		this.procBound = value;
	}

	public int ompLoopDecomp() {
		return ompLoopDecomp;
	}

	public void setOmpLoopDecomp(int ompLoopDecomp) {
		this.ompLoopDecomp = ompLoopDecomp;
	}

	public boolean isReplay() {
		return isReplay;
	}

	public void setReplay(boolean isReplay) {
		this.isReplay = isReplay;
	}

	public void setRuntimeUpdate(boolean enableUpdateThread) {
		this.runtimeUpdate = enableUpdateThread;
	}

	public boolean runtimeUpdate() {
		return this.runtimeUpdate;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	/**
	 * @return the showUnreach
	 */
	public boolean showUnreach() {
		return showUnreach;
	}

	/**
	 * @param showUnreach
	 *            the showUnreach to set
	 */
	public void setShowUnreach(boolean showUnreach) {
		this.showUnreach = showUnreach;
	}

	/**
	 * @return the absAnalysis
	 */
	public boolean analyzeAbs() {
		return absAnalysis;
	}

	/**
	 * @param absAnalysis
	 *            the absAnalysis to set
	 */
	public void setAbsAnalysis(boolean absAnalysis) {
		this.absAnalysis = absAnalysis;
	}

	/**
	 * @return the inputVariables
	 */
	public Map<String, Object> inputVariables() {
		return inputVariables;
	}

	/**
	 * @param inputVariables
	 *            the inputVariables to set
	 */
	public void setInputVariables(Map<String, Object> inputVariables) {
		this.inputVariables = inputVariables;
	}

	/**
	 * @return the collectOutputs
	 */
	public boolean collectOutputs() {
		return collectOutputs;
	}

	/**
	 * @param collectOutputs
	 *            the collectOutputs to set
	 */
	public void setCollectOutputs(boolean collectOutputs) {
		this.collectOutputs = collectOutputs;
	}

	public boolean isEnableMpiContract() {
		return mpiContractFunction != CIVLConstants.CONTRACT_CHECK_NONE;
	}

	public void setMpiContractFunction(String function) {
		if (function.equals(CIVLConstants.CONTRACT_CHECK_ALL))
			mpiContractFunction = CIVLConstants.CONTRACT_CHECK_ALL;
		else if (function.equals(CIVLConstants.CONTRACT_CHECK_NONE))
			mpiContractFunction = CIVLConstants.CONTRACT_CHECK_NONE;
		else
			mpiContractFunction = function;
	}

	public boolean checkDivisionByZero() {
		return checkDivisionByZero;
	}

	public void setCheckDivisionByZero(boolean checkDivisionByZero) {
		this.checkDivisionByZero = checkDivisionByZero;
	}

	public boolean checkMemoryLeak() {
		return this.checkMemoryLeak;
	}

	public void setCheckMemoryLeak(boolean value) {
		this.checkMemoryLeak = value;
	}

	/**
	 * @return the timeout
	 */
	public int timeout() {
		return timeout;
	}

	/**
	 * @param timeout
	 *            the timeout to set
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public boolean unpreproc() {
		return unpreproc;
	}

	public void setUnpreproc(boolean unpreproc) {
		this.unpreproc = unpreproc;
	}

	/**
	 * @return the target function name which is the only one will be verified
	 *         under (mpi-)contract mode. Absent of the target function name
	 *         will cause this method returns <code>null</code> which means
	 *         verify all annotated functions.
	 */
	public String mpiContractFunction() {
		return this.mpiContractFunction;
	}

	/**
	 * @return the collectSymbolicNames
	 */
	public boolean collectSymbolicNames() {
		return collectSymbolicNames;
	}

	/**
	 * @param collectSymbolicNames
	 *            the collectSymbolicNames to set
	 */
	public void setCollectSymbolicNames(boolean collectSymbolicNames) {
		this.collectSymbolicNames = collectSymbolicNames;
	}

	/**
	 * @return the checkExpressionError
	 */
	public boolean checkExpressionError() {
		return checkExpressionError;
	}

	/**
	 * @param checkExpressionError
	 *            the checkExpressionError to set
	 */
	public void setCheckExpressionError(boolean checkExpressionError) {
		this.checkExpressionError = checkExpressionError;
	}

	public boolean showPathConditon() {
		return !this.showPathConditon.equals("NONE");
	}

	public boolean inSubprogram() {
		return isInSubprogram;
	}

	public void setInSubprogram(boolean isInSubprogram) {
		this.isInSubprogram = isInSubprogram;
	}

	/**
	 * @return the svcomp17
	 */
	public boolean svcomp17() {
		return svcomp17;
	}

	/**
	 * @param svcomp17
	 *            the svcomp17 to set
	 */
	public void setSvcomp17(boolean svcomp17) {
		this.svcomp17 = svcomp17;
	}

	public int getIntBit() {
		return intBit;
	}

	public void setIntBit(int intBit) {
		this.intBit = intBit;
	}

	public boolean isIntOperationTransiformer() {
		return intOperationTransiformer;
	}

	public void setIntOperationTransiformer(boolean intOperationTransiformer) {
		this.intOperationTransiformer = intOperationTransiformer;
	}

	public int getMaxProcs() {
		return maxProcs;
	}

	public void setMaxProcs(int maxProcs) {
		this.maxProcs = maxProcs;
	}

	/**
	 * @return true iff the SARL test generation has been specified as enabled
	 *         from UI.
	 */
	public boolean isSARLTestGenerationEnabled() {
		return this.generateTestsForSARL;
	}

	/**
	 * @return true iff loop invariant is enabled
	 */
	public boolean loopInvariantEnabled() {
		return this.loopInvariantEnabled;
	}

	/**
	 * 
	 * @return true if to report cycles in state space as violation; false,
	 *         otherwise
	 */
	public boolean cyclesViolate() {
		return this.cyclesViolate;
	}
}
