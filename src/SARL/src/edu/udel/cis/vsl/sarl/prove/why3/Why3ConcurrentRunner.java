package edu.udel.cis.vsl.sarl.prove.why3;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.ValidityResult;
import edu.udel.cis.vsl.sarl.IF.config.ProverInfo;
import edu.udel.cis.vsl.sarl.prove.IF.Prove;

public class Why3ConcurrentRunner implements Callable<ValidityResult> {
	/**
	 * Nick-name for <code>stderr</code>, where warnings and error messages will
	 * be sent.
	 */
	public static final PrintStream err = System.err;

	/**
	 * The ID of the query that will be reasoned by this Why3 runner.
	 */
	int id;
	/**
	 * The process runs Why3 with a specific theorem prover
	 */
	private ProcessBuilder processBuilder;

	/**
	 * The process who runs the why3 platform
	 */
	private Process bot;

	/**
	 * The time limit for this run
	 */
	private int timelimit;

	/**
	 * Total number of goals
	 */
	private int numGoals;

	/**
	 * A reference to the {@link ProverInfor}
	 */
	private ProverInfo info;

	/**
	 * The name of the file for logging errors.
	 */
	private String errFileName;

	/**
	 * matching a line showing a result of a goal:
	 */
	private static Pattern validGoalPattern = Pattern
			.compile("(.*)G[0-9]\\s(:)\\s(.*)");

	/**
	 * matching a line showing a valid result of a goal:
	 */
	private static Pattern validPattern = Pattern
			.compile("(.*)G[0-9]\\s(:)\\s(Valid)(.*)");

	Why3ConcurrentRunner(ProcessBuilder processBuilder, ProverInfo info,
			int numGoals, int id, String errFileName) {
		this.processBuilder = processBuilder;
		this.timelimit = (int) info.getTimeout();
		this.info = info;
		this.numGoals = numGoals;
		this.id = id;
		this.errFileName = errFileName;
	}

	@Override
	public ValidityResult call() {

		try {
			bot = processBuilder.start();
			bot.waitFor(timelimit, TimeUnit.SECONDS);

			BufferedReader stdout = new BufferedReader(
					new InputStreamReader(bot.getInputStream()));
			BufferedReader stderr = new BufferedReader(
					new InputStreamReader(bot.getErrorStream()));

			return readWhy3Output(stdout, stderr, id);
		} catch (InterruptedException e) {
			bot.destroyForcibly();
			return Prove.RESULT_MAYBE;
		} catch (IOException e) {
			err.print("IOException happens during running Why3: "
					+ e.getMessage());
			return Prove.RESULT_MAYBE;
		}
	}

	/**
	 * Parsing the Why3 output: Keep reading lines until:
	 * 
	 * <li>1. All results of goals are parsed</li>
	 * <li>or 2. No more new lines</li>
	 * <li>or 3. The result is confirmed negative</li>
	 * 
	 * @param y3Out
	 * @param y3Err
	 * @return
	 */
	private ValidityResult readWhy3Output(BufferedReader y3Out,
			BufferedReader y3Err, int id) {
		try {
			String line = y3Out.readLine();

			// no output or y3Err has error, directly return
			if (line == null || (y3Err.ready() && line == null)) {
				if (info.getShowErrors() || info.getShowInconclusives()) {
					try {
						if (y3Err.ready()) {
							PrintStream exp = new PrintStream(
									new File(errFileName));

							printProverUnexpectedException(y3Err, exp);
							exp.close();
						}
					} catch (IOException e) {
						printProverUnexpectedException(y3Err, err);
					}
				}
				return Prove.RESULT_MAYBE;
			}
			Matcher goalMatcher, validMatcher;
			boolean goalResult = true;
			int numGoalsParsed = 0;

			while (line != null && numGoalsParsed < numGoals) {
				line = line.trim();
				// Match goal results:
				goalMatcher = validGoalPattern.matcher(line);
				if (goalMatcher.find()) {
					numGoalsParsed++;
					validMatcher = validPattern.matcher(goalMatcher.group(0));
					if (!validMatcher.find()) {
						goalResult = false;
						break;
					}
				}
				line = y3Out.readLine();
			}
			// if fail to parse the results of all desired goals, return MAYBE
			// and print an error message:
			if (numGoalsParsed != numGoals) {
				if (info.getShowInconclusives()) {
					err.println(info.getFirstAlias()
							+ " inconclusive with message: " + line);
					for (line = y3Out.readLine(); line != null; line = y3Out
							.readLine()) {
						err.println(line);
					}
				}
				err.println("Why3 runner unexpected results: ..." + line);
			} else if (goalResult)
				return Prove.RESULT_YES;
			return Prove.RESULT_MAYBE;
		} catch (IOException e) {
			if (info.getShowErrors())
				err.println("I/O error reading " + info.getFirstAlias()
						+ " output: " + e.getMessage());
			return Prove.RESULT_MAYBE;
		}
	}

	void printProverUnexpectedException(BufferedReader proverErr,
			PrintStream exp) throws IOException {
		String errline;

		do {
			errline = proverErr.readLine();
			if (errline == null)
				break;
			exp.append(errline + "\n");
			err.print(errline);
		} while (errline != null);
		throw new SARLException(
				"Theorem prover unexpected exception:\n" + errline);
	}

	Process process() {
		return this.bot;
	}
}
