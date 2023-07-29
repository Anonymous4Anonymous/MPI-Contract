package edu.udel.cis.vsl.sarl.prove.why3;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import edu.udel.cis.vsl.sarl.IF.TheoremProverException;
import edu.udel.cis.vsl.sarl.IF.ValidityResult;
import edu.udel.cis.vsl.sarl.IF.config.ProverInfo;
import edu.udel.cis.vsl.sarl.IF.config.ProverInfo.ProverKind;
import edu.udel.cis.vsl.sarl.IF.config.SARLConfig;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.prove.IF.Prove;
import edu.udel.cis.vsl.sarl.prove.IF.ProverFunctionInterpretation;
import edu.udel.cis.vsl.sarl.prove.IF.TheoremProver;

public class RobustWhy3ProvePlatform implements TheoremProver {

	/**
	 * Nick-name for <code>stderr</code>, where warnings and error messages will
	 * be sent.
	 */
	public static final PrintStream err = System.err;

	/**
	 * The symbolic universe used for managing symbolic expressions. Initialized
	 * by constructor and never changes.
	 */
	private PreUniverse universe;

	/**
	 * Information object for underlying prover, which must have
	 * {@link ProverKind} {@link ProverKind#Why3}.
	 */
	private ProverInfo info;

	/**
	 * Java object for producing new OS-level processes executing a specified
	 * command.
	 */
	private ProcessBuilder[] processBuilders;

	/**
	 * The {@link ForkJoinPool} which provides shutdowm method for why3
	 * processes:
	 */
	ForkJoinPool executor;

	/**
	 * The {@link ExecutorCompletionService} which provides
	 * {@link Why3ConcurrentRunner}s.
	 */
	ExecutorCompletionService<ValidityResult> why3RunnerPool;

	/**
	 * The context where bound variable names are cleaned.
	 */
	private BooleanExpression cleanedContext;

	/**
	 * A list of {@link ProverFunctionInterpretation}s, which is suppose to be
	 * used by both context and queries
	 */
	private ProverFunctionInterpretation[] ppreds;

	private static String prove_command = "prove";

	private File temporaryScriptFile = null;

	public RobustWhy3ProvePlatform(SARLConfig config, PreUniverse universe,
			ProverInfo info, BooleanExpression context,
			ProverFunctionInterpretation[] ppreds) {
		String[] command = new String[7];

		assert universe != null;
		assert context != null;
		assert info != null;
		this.universe = universe;
		this.info = info;
		cleanedContext = (BooleanExpression) universe
				.cleanBoundVariables(context);
		command[0] = info.getPath().getAbsolutePath();
		command[1] = prove_command;
		command[2] = "-P";
		command[4] = "-t";
		command[5] = String.valueOf((int) info.getTimeout());

		File outputdir = config.getOutputFileDir().toFile();

		try {
			if (!outputdir.exists())
				outputdir.mkdirs();
			temporaryScriptFile = File.createTempFile("_sarl_", ".why",
					config.getOutputFileDir().toFile());
		} catch (IOException e) {
			throw new TheoremProverException(
					"Why3 runner failed to create a temporary script file");
		}
		command[6] = temporaryScriptFile.getPath();

		// Initialize process builders:
		int proverCounter = 0;

		this.processBuilders = new ProcessBuilder[info.getOptions().size()];
		for (String proverOption : info.getOptions()) {
			command[3] = proverOption;
			processBuilders[proverCounter] = new ProcessBuilder(command);
			processBuilders[proverCounter++].environment().put("PATH",
					info.getEnv());
		}
		this.executor = new ForkJoinPool( // leave 2 spaces for processors
				Runtime.getRuntime().availableProcessors() - 2);
		this.why3RunnerPool = new ExecutorCompletionService<ValidityResult>(
				executor);
		this.ppreds = ppreds;
	}

	@Override
	public PreUniverse universe() {
		return universe;
	}

	@Override
	public ValidityResult valid(BooleanExpression predicate) {
		PrintStream out = universe.getOutputStream();
		int id = universe.numProverValidCalls();
		boolean show = universe.getShowProverQueries() || info.getShowQueries();

		predicate = (BooleanExpression) universe.cleanBoundVariables(predicate);
		universe.incrementProverValidCount();

		ValidityResult result;

		try {
			result = runWhy3(predicate, id, show, out, false, false);
			if (result == Prove.RESULT_MAYBE)
				result = runWhy3(predicate, id, show, out, true, false);
		} catch (TheoremProverException e) {
			if (show)
				err.println("Warning: " + e.getMessage());
			result = Prove.RESULT_MAYBE;
		}
		if (show) {
			out.println(info.getFirstAlias() + " result      " + id + ": "
					+ result);
			out.flush();
		}
		return result;
	}

	@Override
	public ValidityResult validOrModel(BooleanExpression predicate) {
		err.println("warning: Why3 cannot give model for invalid queries");
		return valid(predicate);
	}

	/**
	 * Run why3 to reason about the predicate
	 * 
	 * @param predicate
	 *            a boolean expression representing the predicate
	 * @param id
	 *            the ID number of the prover call
	 * @param show
	 *            true to print the why3 script
	 * @param out
	 *            the output stream
	 * @param doSplitGoals
	 *            true to split the predicate into several smaller predicates
	 * @param testUNSAT
	 *            true for testing if the predicate and the context is
	 *            unsatisfiable; false for testing if the context entails the
	 *            predicate
	 * @return
	 */
	private ValidityResult runWhy3(BooleanExpression predicate, int id,
			boolean show, PrintStream out, boolean doSplitGoals,
			boolean testUNSAT) {
		ValidityResult result = Prove.RESULT_MAYBE;
		Why3Translator why3Translator = new Why3Translator(universe,
				cleanedContext, ppreds);
		int numGoals;
		String goalsTexts[];

		if (doSplitGoals) {
			BooleanExpression goals[] = splitGoals(predicate);
			numGoals = goals.length;

			if (numGoals == 1)
				return result;
			goalsTexts = new String[numGoals];
			for (int i = 0; i < numGoals; i++)
				goalsTexts[i] = why3Translator.translateGoal(goals[i]);
		} else {
			numGoals = 1;
			goalsTexts = new String[1];
			goalsTexts[0] = why3Translator.translateGoal(predicate);
		}

		// Write the why3 translation into a temporary file, run process, then
		// delete it:
		String executableWhy3Script = why3Translator.getExecutableOutput(id,
				testUNSAT, goalsTexts);

		try {
			FileWriter filewriter = new FileWriter(temporaryScriptFile);

			filewriter.write(executableWhy3Script);
			filewriter.close();
		} catch (IOException e) {
			if (info.getShowErrors())
				err.println("I/O exception creating why3 script file "
						+ " output: " + e.getMessage());
		}
		// Launching processes:
		Why3ConcurrentRunner runners[] = new Why3ConcurrentRunner[processBuilders.length];
		LinkedList<Future<ValidityResult>> runnerHandles = new LinkedList<>();

		for (int i = 0; i < processBuilders.length; i++) {
			runners[i] = new Why3ConcurrentRunner(processBuilders[i], info,
					numGoals, id, universe.getErrFile());

			runnerHandles.add(why3RunnerPool.submit(runners[i]));
		}

		ValidityResult subResult = Prove.RESULT_MAYBE;

		try {
			for (int i = 0; i < processBuilders.length; i++) {
				subResult = why3RunnerPool.take().get();
				if (subResult == Prove.RESULT_YES) {
					result = subResult;
				}
			}
		} catch (InterruptedException | ExecutionException e) {
			err.print("Unexpected exception happens during running why3 prover"
					+ e.getMessage());
			result = Prove.RESULT_MAYBE;
		} finally {
			for (Future<ValidityResult> hd : runnerHandles)
				hd.cancel(true);
		}
		if (show) {
			out.print("\n" + info.getFirstAlias() + " script        " + id
					+ ":\n");
			out.println(executableWhy3Script);
			out.flush();
		}
		try {
			Files.delete(temporaryScriptFile.toPath());
		} catch (IOException e) {
			for (int i = 0; i < processBuilders.length; i++)
				while (runners[i].process().isAlive()) {
					// blocking until every one has been killed.
				}
			// then delete again:
			try {
				Files.delete(temporaryScriptFile.toPath());
			} catch (IOException e1) {
				err.print("File " + temporaryScriptFile
						+ " is not successfullt deleted.");
			}
		}
		return result;
	}

	/**
	 * Split the predicate P to a set of sub-goals S, that <code>
	 * P = s<sub>0</sub> && .. && s<sub>n-1</sub>,
	 * where s<sub>i</sub> is an element of S and |S| = n  
	 * </code>
	 * 
	 * @param predicate
	 * @return
	 */
	private BooleanExpression[] splitGoals(BooleanExpression predicate) {
		if (predicate.operator() == SymbolicOperator.AND) {
			int numArgs = predicate.numArguments();
			BooleanExpression result[] = new BooleanExpression[numArgs];

			for (int i = 0; i < numArgs; i++)
				result[i] = (BooleanExpression) predicate.argument(i);
			return result;
		} else {
			BooleanExpression result[] = { predicate };
			return result;
		}
	}

	@Override
	public ValidityResult unsat(BooleanExpression predicate)
			throws TheoremProverException {
		PrintStream out = universe.getOutputStream();
		int id = universe.numProverValidCalls();
		boolean show = universe.getShowProverQueries() || info.getShowQueries();

		predicate = (BooleanExpression) universe.cleanBoundVariables(predicate);
		universe.incrementProverValidCount();

		ValidityResult result;

		try {
			result = runWhy3(predicate, id, show, out, false, true);
			if (result == Prove.RESULT_MAYBE)
				result = runWhy3(predicate, id, show, out, true, true);
		} catch (TheoremProverException e) {
			if (show)
				err.println("Warning: " + e.getMessage());
			result = Prove.RESULT_MAYBE;
		}
		if (show) {
			out.println(info.getFirstAlias() + " result      " + id + ": "
					+ result);
			out.flush();
		}
		return result;
	}
}
