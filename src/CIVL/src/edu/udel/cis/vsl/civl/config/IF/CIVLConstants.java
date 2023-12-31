package edu.udel.cis.vsl.civl.config.IF;

import static edu.udel.cis.vsl.gmc.Option.OptionType.BOOLEAN;
import static edu.udel.cis.vsl.gmc.Option.OptionType.INTEGER;
import static edu.udel.cis.vsl.gmc.Option.OptionType.STRING;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.gmc.Option;
import edu.udel.cis.vsl.gmc.Option.OptionType;

/**
 * This class manages all constant configurations of the system.
 * 
 * NOTE: when you add a new option, add it here, give it name ending in "O",
 * like the others, AND add it to the list in method {@link #getAllOptions()}.
 * And keep them in alphabetical order.
 * 
 * @author Manchun Zheng
 * 
 */
public class CIVLConstants {

	/**
	 * Kinds of deadlock: absolute, potential or none.
	 * 
	 * @author Manchun Zheng
	 *
	 */
	public enum DeadlockKind {
		ABSOLUTE, POTENTIAL, NONE
	}

	/**
	 * Error state equivalence semantics for suppressing logging of redundant
	 * errors.
	 * 
	 * @author Matt Dwyer
	 */
	public enum ErrorStateEquivalence {
		LOC, // Require the current location to match
		CALLSTACK, // Require the call stacks to match
		FULL // Require the full trace to match
	}

	/**
	 * The root of the include directory.
	 */
	public final static File ROOT_INCLUDE_PATH = new File(
			File.separator + "include");

	/**
	 * Where the CIVL library implementations files (.cvl) are located. This
	 * path is relative to the class path. Since the "include" directory is in
	 * the class path, this will cause ABC to look in include/civl.
	 */
	public final static File CIVL_INCLUDE_PATH = new File(ROOT_INCLUDE_PATH,
			"civl");

	/**
	 * Where the CIVL header files (suffix .h and .cvh) are located. This path
	 * is relative to the class path.
	 */
	public final static File FRONT_END_INCLUDE_PATH = new File(
			ROOT_INCLUDE_PATH, "abc");

	/** The version of this release of CIVL. */
	public final static String version = "1.20+";

	/**
	 * The date of this release of CIVL. Format: YYYY-MM-DD in accordance with
	 * ISO 8601.
	 */
	public final static String date = "2019-09-27";

	/**
	 * The prefix of the full name of the class of a library enabler/executor.
	 */
	public final static String LIBRARY_PREFIX = "edu.udel.cis.vsl.civl.library.";

	/**
	 * A string printed before and after titles of sections of output to make
	 * them stand out among the clutter.
	 */
	public final static String bar = "===================";

	public final static String statsBar = "===";

	/**
	 * The name of the directory into which CIVL will store the artifacts it
	 * generates.
	 */
	public final static String CIVLREP = "CIVLREP";

	/**
	 * Number of seconds between printing of update messages.
	 */
	public final static int consoleUpdatePeriod = 15;

	/**
	 * Number of seconds between saving update messages to disk when in web app
	 * mode.
	 */
	public final static int webUpdatePeriod = 1;

	// Option names
	public static String DEBUG = "debug";

	public static String TIMEOUT = "timeout";

	public static String CHECK_DIV_BY_ZERO = "checkDivisionByZero";

	public static String CHECK_MEM_LEAK = "checkMemoryLeak";

	public static String ENABLE_PRINTF = "enablePrintf";

	public static String ERROR_BOUND = "errorBound";

	public static String MAX_PROCS = "maxProcs";

	public static String ERROR_STATE_EQUIV = "errorStateEquiv";

	public static String GUIDED = "guided";

	public static String ID = "id";

	public static String INPUT = "input";

	public static String MAX_DEPTH = "maxdepth";

	public static String MIN = "min";

	public static String MPI_CONTRACT = "mpiContract";
	public static String LOOP_INV = "loop";
	public static String PROC_BOUND = "procBound";
	public static String RANDOM = "random";
	public static String SAVE_STATES = "saveStates";
	public static String SEED = "seed";
	public static String ANALYZE_ABS = "analyze_abs";
	public static String AST = "ast";
	public static String UNPREPROC = "unpreproc";
	public static String SHOW_AMPLE_SET = "showAmpleSet";
	public static String SHOW_AMPLE_SET_STATES = "showAmpleSetWtStates";
	public static String SHOW_MEM_UNITS = "showMemoryUnits";
	public static String SHOW_MODEL = "showModel";
	public static String SHOW_PROVER_QUERIES = "showProverQueries";
	public static String SHOW_QUERIES = "showQueries";
	public static String SHOW_SAVED_STATES = "showSavedStates";
	public static String SHOW_STATES = "showStates";
	public static String SHOW_TIME = "showTime";
	public static String SHOW_TRANSITIONS = "showTransitions";
	public static String SHOW_UNREACHED = "showUnreached";
	public static String SIMPLIFY = "simplify";
	public static String SOLVE = "solve";
	public static String STATELESS_PRINTF = "statelessPrintf";
	public static String STRICT = "strict";
	public static String SYS_INCLUDE_PATH = "sysIncludePath";
	public static String TRACE = "trace";
	public static String USER_INCLUDE_PATH = "userIncludePath";
	public static String VERBOSE = "verbose";
	public static String GUI = "gui";
	public static String DEADLOCK = "deadlock";
	public static String SVCOMP16 = "svcomp16";
	public static String SVCOMP17 = "svcomp17";
	public static String SHOW_INPUTS = "showInputs";
	public static String PREPROC = "preproc";
	public static String PROB = "prob";
	public static String SHOW_PROGRAM = "showProgram";
	public static String SHOW_PATH_CONDITION = "showPathCondition";
	public static String OMP_NO_SIMPLIFY = "ompNoSimplify";
	public static String OMP_ONLY_SIMPLIFIER = "ompOnlySimplifier";
	public static String COLLECT_OUTPUT = "collectOutput";
	public static String COLLECT_PROCESSES = "collectProcesses";
	public static String COLLECT_SCOPES = "collectScopes";
	public static String COLLECT_SYMBOLIC_CONSTANTS = "collectSymbolicConstants";
	public static String COLLECT_HEAPS = "collectHeaps";
	public static String LINK = "link";
	public static String MACRO = "D";
	public static String WEB = "web";
	public static String OMP_LOOP_DECOMP = "ompLoopDecomp";
	public static String CIVL_MACRO = "_CIVL";
	public static String QUIET = "quiet";
	public static String INT_OPERATION_TRANSFORMER = "intOperationTransformer";
	public static String SLICE_ANALYSIS = "sliceAnalysis";
	public static String WITNESS = "witness";
	public static String DIRECT = "direct";
	public static String INTBIT = "int_bit";
	public static String TEST_GEN = "testGen";
	public static String CYCLES_VIOLATE = "cyclesViolate";
	public static String RUNTIME_UPDATE = "runtimeUpdate";

	// Option objects
	/**
	 * Default option value for -mpiContract option {@link #mpiContractO}
	 */
	public final static String CONTRACT_CHECK_ALL = "_CIVL_CONTRACT_ALL";

	/**
	 * Default option value for -mpiContract option {@link #mpiContractO}
	 */
	public final static String CONTRACT_CHECK_NONE = "_CIVL_CONTRACT_NONE";

	/**
	 * Debug option, false by default.
	 */
	public final static Option debugO = Option.newScalarOption(DEBUG, BOOLEAN,
			"debug mode: print very detailed information", false);

	public final static Option timeoutO = Option.newScalarOption(TIMEOUT,
			OptionType.INTEGER,
			"time out in seconds, default is never time out", -1);

	/**
	 * Debug option, false by default.
	 */
	public final static Option checkDivisionByZeroO = Option.newScalarOption(
			CHECK_DIV_BY_ZERO, BOOLEAN, "check division-by-zero?", true);

	/**
	 * Debug option, false by default.
	 */
	public final static Option checkMemoryLeakO = Option.newScalarOption(
			CHECK_MEM_LEAK, BOOLEAN, "check memory-leak errors?", true);

	/**
	 * Enables printf? true by default. When false, nothing is printed for
	 * printf function.
	 */
	public final static Option enablePrintfO = Option.newScalarOption(
			ENABLE_PRINTF, BOOLEAN, "enable printf function", true);

	/**
	 * The maximal number of errors allowed before terminating CIVL. 1 by
	 * default.
	 */
	public final static Option errorBoundO = Option.newScalarOption(ERROR_BOUND,
			INTEGER, "stop after finding this many errors", 1);

	public final static Option maxProcsO = Option.newScalarOption(MAX_PROCS,
			INTEGER, "the maximum number of processes", 1000);

	/**
	 * The semantics for used to determine when error states are equivalent;
	 * CIVL suppresses logging of equivalent states. All semantics use the kind
	 * of error, but they may vary in the portion of the state that is checked.
	 * Current options include using the current location (LOC), the call stacks
	 * (CALLSTACK), and the full trace (FULL), but others are possible. LOC by
	 * default.
	 */
	public final static Option errorStateEquivO = Option.newScalarOption(
			ERROR_STATE_EQUIV, STRING,
			"semantics for equivalent error states: (LOC|CALLSTACK|FULL)",
			"LOC");

	/**
	 * User guided simulation?
	 */
	public final static Option guidedO = Option.newScalarOption(GUIDED, BOOLEAN,
			"user guided simulation; applies only to run, ignored\n"
					+ "    for all other commands",
			null);

	/**
	 * The id of the trace for replay, 0 by default.
	 */
	public final static Option idO = Option.newScalarOption(ID, INTEGER,
			"ID number of trace to replay; applies only to replay command", 0);

	/**
	 * Specify values of input variables.
	 */
	public final static Option inputO = Option.newMapOption(INPUT,
			"initialize input variable KEY to VALUE; applies only to run and verify");

	/**
	 * The maximal depth for search. Infinite by default.
	 */
	public final static Option maxdepthO = Option.newScalarOption(MAX_DEPTH,
			INTEGER, "bound on search depth", Integer.MAX_VALUE);

	/**
	 * Search for the minimum counterexample? false by default.
	 */
	public final static Option minO = Option.newScalarOption(MIN, BOOLEAN,
			"search for minimal counterexample", false);

	/**
	 * MPI contract mode? Disable by default.
	 */
	public final static Option mpiContractO = Option.newScalarOption(
			MPI_CONTRACT, STRING,
			"MPI contract mode, specify the name of the function to be verified, "
					+ "or specify \"" + CONTRACT_CHECK_ALL
					+ "\" for checking all annotated functions."
					+ " Default value \"" + CONTRACT_CHECK_NONE
					+ "\" is equivalent to disble MPI contract mode",
			CONTRACT_CHECK_NONE);

	/**
	 * Enable all settings that are required for verifying with loop invariants.
	 * Disable by default.
	 */
	public final static Option loopO = Option.newScalarOption(LOOP_INV, BOOLEAN,
			"Enable all settings that are required for verifying with loop invariants. "
					+ "(e.g. collect symbolic constants will be enabled)",
			false);

	/**
	 * The bound on number of live processes (no bound if negative). No bound by
	 * default.
	 */
	public final static Option procBoundO = Option.newScalarOption(PROC_BOUND,
			INTEGER, "bound on number of live processes (no bound if negative)",
			-1);

	/**
	 * Use probabilistic techniques for verifying numeric identifies. False by
	 * default.
	 */
	public final static Option probO = Option.newScalarOption(PROB, BOOLEAN,
			"use probabilistic techniques for verifying numeric identifies",
			false);

	/**
	 * TODO can it be cleaned up? Select enabled transitions randomly? Default
	 * for run.
	 */
	public final static Option randomO = Option.newScalarOption(RANDOM, BOOLEAN,
			"select enabled transitions randomly; default for run,\n"
					+ "    ignored for all other commands",
			null);

	/**
	 * set <code>false</code> to disable CIVL {@link UpdaterRunnable} thread.
	 * The default value is <code>true</code>
	 */
	public final static Option runtimeUpdateO = Option.newScalarOption(
			RUNTIME_UPDATE, BOOLEAN,
			"set as `false` to disable CIVL updater thread, \n"
					+ "which printing update-info periodically",
			true);

	/**
	 * Save states during depth-first search? true by default.
	 */
	public final static Option saveStatesO = Option.newScalarOption(SAVE_STATES,
			BOOLEAN, "save states during depth-first search", true);

	/**
	 * Set the random seed for run mode.
	 */
	public final static Option seedO = Option.newScalarOption(SEED, INTEGER,
			"set the random seed; applies only to run", null);

	/**
	 * Set the upper bound of integers.
	 */
	public final static Option intBit = Option.newScalarOption(INTBIT, INTEGER,
			"set the number of bits of integer", 32);

	/**
	 * Analyze abs calls? false by default.
	 */
	public final static Option analyzeAbsO = Option.newScalarOption(ANALYZE_ABS,
			BOOLEAN, "analyze abs calls? false by default", false);

	/**
	 * Show the AST of the program? false by default.
	 */
	public final static Option astO = Option.newScalarOption(AST, BOOLEAN,
			"print the AST of the program", false);

	/**
	 * Print the ample set when it contains more than one processes? false by
	 * default.
	 */
	public final static Option showAmpleSetO = Option.newScalarOption(
			SHOW_AMPLE_SET, BOOLEAN,
			"print the ample set when it contains more than one processes",
			false);

	/**
	 * Print ample set and state when ample set contains more than one
	 * processes? false by default.
	 */
	public final static Option showAmpleSetWtStatesO = Option.newScalarOption(
			SHOW_AMPLE_SET_STATES, BOOLEAN,
			"print ample set and state when ample set contains >1 processes",
			false);

	/**
	 * Print the impact/reachable memory units when the state contains more than
	 * one processes? false by default.
	 */
	public final static Option showMemoryUnitsO = Option.newScalarOption(
			SHOW_MEM_UNITS, BOOLEAN,
			"print the impact/reachable memory units when the state contains more than one processes",
			false);

	/**
	 * Show the CIVL model of the program? false by default.
	 */
	public final static Option showModelO = Option.newScalarOption(SHOW_MODEL,
			BOOLEAN, "print the model", false);

	/**
	 * Show theorem prover queries? false by default.
	 */
	public final static Option showProverQueriesO = Option.newScalarOption(
			SHOW_PROVER_QUERIES, BOOLEAN, "print theorem prover queries only",
			false);

	/**
	 * Show all SARL queries? false by default.
	 */
	public final static Option showQueriesO = Option
			.newScalarOption(SHOW_QUERIES, BOOLEAN, "print all queries", false);

	/**
	 * Show all states that are saved? false by default.
	 */
	public final static Option showSavedStatesO = Option.newScalarOption(
			SHOW_SAVED_STATES, BOOLEAN, "print saved states only", false);

	/**
	 * Show all states? false by default.
	 */
	public final static Option showStatesO = Option.newScalarOption(SHOW_STATES,
			BOOLEAN, "print all states", false);

	/**
	 * Show the time used by each translation phase? false by default.
	 */
	public final static Option showTimeO = Option.newScalarOption(SHOW_TIME,
			BOOLEAN, "print timings", false);

	/**
	 * Show all transitions? false by default;
	 */
	public final static Option showTransitionsO = Option.newScalarOption(
			SHOW_TRANSITIONS, BOOLEAN, "print transitions", false);

	/**
	 * Show unreachable code? false by default;
	 */
	public final static Option showUnreachedCodeO = Option.newScalarOption(
			SHOW_UNREACHED, BOOLEAN, "print the unreachable code", false);

	/**
	 * Simplify states using path conditions? true by default.
	 */
	public final static Option simplifyO = Option.newScalarOption(SIMPLIFY,
			BOOLEAN, "simplify states?", true);

	/**
	 * Try to solve for concrete counterexample? false by default.
	 */
	public final static Option solveO = Option.newScalarOption(SOLVE, BOOLEAN,
			"try to solve for concrete counterexample", false);

	/**
	 * Don't modify file system when running printf? true by default.
	 */
	public final static Option statelessPrintfO = Option.newScalarOption(
			STATELESS_PRINTF, BOOLEAN,
			"prevent printf function modifying the file system", true);

	/**
	 * Print the impact/reachable memory units when the state contains more than
	 * one processes? false by default.
	 */
	public final static Option strictCompareO = Option.newScalarOption(STRICT,
			BOOLEAN, "check strict functional equivalence?", true);

	/**
	 * Set the system include path.
	 */
	public final static Option sysIncludePathO = Option.newScalarOption(
			SYS_INCLUDE_PATH, STRING,
			"set the system include path, using : to separate multiple paths",
			null);

	/**
	 * Unpreprocess the source? false by default.
	 */
	public final static Option unpreprocO = Option.newScalarOption(UNPREPROC,
			BOOLEAN, "unpreprocess the source?", false);

	/**
	 * File name of trace to replay
	 */
	public final static Option traceO = Option.newScalarOption(TRACE, STRING,
			"filename of trace to replay", null);

	/**
	 * Sets user include path.
	 */
	public final static Option userIncludePathO = Option.newScalarOption(
			USER_INCLUDE_PATH, STRING,
			"set the user include path, using : to separate multiple paths",
			null);

	/**
	 * Verbose mode? false by default
	 */
	public final static Option verboseO = Option.newScalarOption(VERBOSE,
			BOOLEAN, "verbose mode", false);

	/**
	 * What kind of deadlock is to be checked, potential, absolute or none?
	 * absolute by default.
	 */
	public final static Option deadlockO = Option.newScalarOption(DEADLOCK,
			STRING, "deadlock kind? (potential|absolute|none)", "absolute");

	/**
	 * Perform svcomp16 transformation? false by default.
	 */
	public final static Option svcomp16O = Option.newScalarOption(SVCOMP16,
			BOOLEAN, "translate program for sv-comp 2016?", false);

	/**
	 * Perform svcomp transformation? false by default.
	 */
	public final static Option svcomp17O = Option.newScalarOption(SVCOMP17,
			BOOLEAN, "translate program for sv-comp 2017?", false);

	/**
	 * Show the input variables of this model? false by default.
	 */
	public final static Option showInputVarsO = Option.newScalarOption(
			SHOW_INPUTS, BOOLEAN, "show input variables of my program?", false);

	/**
	 * Show the preprocessing result? false by default.
	 */
	public final static Option preprocO = Option.newScalarOption(PREPROC,
			BOOLEAN, "show the preprocessing result?", false);

	/**
	 * Show the program after all applicable transformations? false by default.
	 */
	public final static Option showProgramO = Option.newScalarOption(
			SHOW_PROGRAM, BOOLEAN, "show my program after transformations?",
			false);

	/**
	 * Show the path condition of each state? false by default.
	 */
	public final static Option showPathConditionO = Option.newScalarOption(
			SHOW_PATH_CONDITION, STRING,
			"show the path condition of each state? "
					+ "(LINE (display as one line) | BLOCK (display as multiple lines))",
			"LINE");

	/**
	 * Don't simplify OpenMP pragmas? false by default.
	 */
	public final static Option ompNoSimplifyO = Option.newScalarOption(
			OMP_NO_SIMPLIFY, BOOLEAN, "don't simplify omp pragmas", false);

	/**
	 * Only relies on the OpenMP simplifier ? i.e., either simplify an omp
	 * program or report possible data-race
	 */
	public final static Option ompOnlySimplifierO = Option.newScalarOption(
			OMP_ONLY_SIMPLIFIER, BOOLEAN,
			"only relies on the OpenMP simplifier, i.e.,"
					+ "either simplify an omp program or report possible data-race",
			false);

	/**
	 * Collect output? false by default.
	 */
	public final static Option collectOutputO = Option
			.newScalarOption(COLLECT_OUTPUT, BOOLEAN, "collect output?", false);

	/**
	 * Collect processes? true by default.
	 */
	public final static Option collectProcessesO = Option.newScalarOption(
			COLLECT_PROCESSES, BOOLEAN, "collect processes?", true);

	/**
	 * Collect scopes? true by default.
	 */
	public final static Option collectScopesO = Option.newScalarOption(
			COLLECT_SCOPES, BOOLEAN, "collect dyscopes?", true);

	/**
	 * Collect symbolic constants ? false by default.
	 */
	public final static Option collectSymbolicConstantsO = Option
			.newScalarOption(COLLECT_SYMBOLIC_CONSTANTS, BOOLEAN,
					"collect symbolic constant?", false);

	/**
	 * Collect heaps? true by default.
	 */
	public final static Option collectHeapsO = Option
			.newScalarOption(COLLECT_HEAPS, BOOLEAN, "collect heaps?", true);

	/**
	 * Link a source file with the target program.
	 */
	public final static Option linkO = Option.newScalarOption(LINK, STRING,
			"link a source file with the target program", null);

	/**
	 * Define macros.
	 */
	public final static Option macroO = Option.newMapOption(MACRO,
			"macro definitions: <macro> or <macro>=<object>");

	/**
	 * Write output for web app? false by default.
	 */
	public final static Option webO = Option.newScalarOption(WEB, BOOLEAN,
			"write output for web app?", false);

	/**
	 * Set the loop decomposition strategy for OpenMP transformer. Round robin
	 * by default.
	 */
	public final static Option ompLoopDecompO = Option.newScalarOption(
			OMP_LOOP_DECOMP, STRING,
			"loop decomposition strategy? (ALL|ROUND_ROBIN|RANDOM)",
			"ROUND_ROBIN");

	/**
	 * Collect heaps? true by default.
	 */
	public final static Option CIVLMacroO = Option.newScalarOption(CIVL_MACRO,
			BOOLEAN, "Define _CIVL macro?", true);

	/**
	 * Ignore the output? false by default.
	 */
	public final static Option quietO = Option.newScalarOption(QUIET, BOOLEAN,
			"ignore output?", false);

	/**
	 * apply int operation transformer? true by default.
	 */
	public final static Option intOperationTransformer = Option.newScalarOption(
			INT_OPERATION_TRANSFORMER, BOOLEAN,
			"apply int operation transformer?", false);

	/**
	 * Perform slice analysis on trace? false by default.
	 */
	public final static Option sliceAnalysisO = Option.newScalarOption(
			SLICE_ANALYSIS, BOOLEAN, "Perform slice analysis on trace?", false);

	/**
	 * Generate witness from trace? false by default.
	 */
	public final static Option witnessO = Option.newScalarOption(WITNESS,
			BOOLEAN, "Generate witness from trace?", false);

	/**
	 * Inject instrumentation to direct the branches at the line numbers in
	 * given file so as to explore a sub-space of execution. Note: currently
	 * assumes you are given one C file (no linking)
	 */
	public final static Option direct0 = Option.newScalarOption(DIRECT, STRING,
			"Direct branching at line numbers in the given file", null);

	public final static Option cyclesViolateO = Option.newScalarOption(
			CYCLES_VIOLATE, BOOLEAN,
			"Report cycles in the state space as violations", false);

	/**
	 * An option to enable test generation for SARL, i.e. generate SARL tests
	 * for some validity checks that CIVL encounters during a run.
	 */
	public final static Option SARLTestGenO = Option.newScalarOption(TEST_GEN,
			BOOLEAN,
			"Generating SARL Junit tests for some validity tests that CIVL encountered",
			false);

	/**
	 * The name of the CIVL system function, which is the starting point of a
	 * CIVL model.
	 */
	public final static String civlSystemFunction = "main";

	/**
	 * Returns all options defined for CIVL in alphabetic order.
	 * 
	 * @return all options defined for CIVL in alphabetic order.
	 */
	public final static Option[] getAllOptions() {
		return new Option[]{astO, collectHeapsO, collectProcessesO,
				collectScopesO, collectSymbolicConstantsO, deadlockO, debugO,
				enablePrintfO, errorBoundO, errorStateEquivO, guidedO,
				idO, inputO, linkO, loopO, macroO, maxdepthO, minO,
				mpiContractO, ompLoopDecompO, ompNoSimplifyO,
				ompOnlySimplifierO, probO, preprocO, procBoundO, randomO,
				runtimeUpdateO, saveStatesO, seedO, showAmpleSetO,
				showAmpleSetWtStatesO, showInputVarsO, showMemoryUnitsO,
				showModelO, showPathConditionO, showProgramO,
				showProverQueriesO, showQueriesO, showSavedStatesO, showStatesO,
				showTimeO, showTransitionsO, showUnreachedCodeO, simplifyO,
				solveO, statelessPrintfO, svcomp16O, svcomp17O, quietO,
				sysIncludePathO, traceO, userIncludePathO, verboseO, webO,
				CIVLMacroO, analyzeAbsO, strictCompareO, collectOutputO,
				checkDivisionByZeroO, checkMemoryLeakO, timeoutO, unpreprocO,
				sliceAnalysisO, witnessO, direct0, intBit,
				intOperationTransformer, maxProcsO, SARLTestGenO,
				cyclesViolateO};
	}

	// headers...
	public final static String BUNDLE = "bundle.cvh";
	public final static String CIVLC = "civlc.cvh";
	public final static String CIVL_MPI = "civl-mpi.cvh";
	public final static String CIVL_PTHREAD = "civl-pthread.cvh";
	public final static String COMM = "comm.cvh";
	public final static String CONCURRENCY = "concurrency.cvh";
	public final static String CIVL_OMP = "civl-omp.cvh";
	public final static String CIVL_OMP_IMP = "civl-omp.cvl";
	public final static String MEM = "mem.cvh";
	// public final static String CIVL_DOMAIN = "domain.cvh";
	public final static String MPI = "mpi.h";
	public final static String MATH = "math.h";
	public final static String OMP = "omp.h";
	public final static String PTHREAD = "pthread.h";
	public final static String SEQ = "seq.cvh";
	public final static String STRING_LIB = "string.h";
	public final static String SVCOMP = "svcomp.h";
	public final static String STDIO = "stdio.h";
	public final static String STDLIB = "stdlib.h";
	public final static String SYS_TIME = "sys/time.h";
	public final static String SYS_TIMES = "sys/times.h";
	public final static String TIME = "time.h";
	public final static String CUDA = "cuda.h";
	public final static String CIVL_CUDA = "civl-cuda.cvh";
	public final static String COLLATE = "collate.cvh";

	public final static String ASSERT = "assert.h";
	public final static String COMPLEX = "complex.h";
	public final static String CTYPE = "ctype.h";
	public final static String FLOAT = "float.h";
	public final static String GD = "gd.h";
	public final static String GDFX = "gdfx.h";
	public final static String LIMITS = "limits.h";
	public final static String OP = "op.h";
	public final static String STDARG = "stdarg.h";
	public final static String STDBOOL = "stdbool.h";
	public final static String STDDEF = "stddef.h";
	public final static String STDINT = "stdint.h";
	public final static String UNISTD = "unistd.h";

	/**
	 * The int value of the char '\0', which represents the end of string.
	 */
	public final static int EOS = 0;

	/**
	 * Returns all CIVL-C libraries.
	 * 
	 * @return all CIVL-C libraries.
	 */
	public final static Set<String> getAllCivlLibs() {
		return new HashSet<String>(
				Arrays.asList(BUNDLE, CIVLC, CIVL_MPI, CIVL_PTHREAD, COMM,
						CONCURRENCY, CIVL_OMP, SEQ, CIVL_CUDA, COLLATE));
	}

	/**
	 * Returns all standard c libraries.
	 * 
	 * @return all standard c libraries.
	 */
	public final static Set<String> getCinterfaces() {
		return new HashSet<String>(Arrays.asList(ASSERT, MPI, MATH, OMP,
				PTHREAD, STRING_LIB, SVCOMP, STDIO, STDLIB, TIME, CUDA,
				SYS_TIME, UNISTD, STDINT));
	}

	/**
	 * Returns all standard c libraries.
	 * 
	 * @return all standard c libraries.
	 */
	public final static Set<String> getAllCLibraries() {
		return new HashSet<String>(Arrays.asList(MPI, MATH, OMP, PTHREAD,
				STRING_LIB, SVCOMP, STDIO, STDLIB, TIME, CUDA, SYS_TIME, ASSERT,
				COMPLEX, CTYPE, FLOAT, GD, GDFX, LIMITS, OP, STDARG, STDBOOL,
				STDDEF, STDINT, UNISTD));
	}
}
