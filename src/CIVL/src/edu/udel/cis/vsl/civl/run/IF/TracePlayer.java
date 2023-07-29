package edu.udel.cis.vsl.civl.run.IF;

import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.seedO;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;

import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;
import edu.udel.cis.vsl.civl.log.IF.CIVLExecutionException;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.CIVLStateException;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.gmc.CommandLineException;
import edu.udel.cis.vsl.gmc.GMCConfiguration;
import edu.udel.cis.vsl.gmc.GuidedTransitionChooser;
import edu.udel.cis.vsl.gmc.MisguidedExecutionException;
import edu.udel.cis.vsl.gmc.RandomTransitionChooser;
import edu.udel.cis.vsl.gmc.Simulator;
import edu.udel.cis.vsl.gmc.Trace;
import edu.udel.cis.vsl.gmc.TransitionChooser;

/**
 * A tool to replay a trace saved by a previous CIVL session.
 * 
 * NOTE: need to have the new options override the ones specified in the trace
 * file.
 * 
 * @author xxxx
 * 
 */
public class TracePlayer extends Player {

	private TransitionChooser<State, Transition> chooser;

	private Simulator<State, Transition> simulator;

	private boolean isRandom = false;

	private long seed = 0;

	public static TracePlayer guidedPlayer(GMCConfiguration config, Model model,
			File traceFile, PrintStream out, PrintStream err)
			throws CommandLineException, IOException,
			MisguidedExecutionException {
		TracePlayer result = new TracePlayer(config, model, out, err);
		GuidedTransitionChooser<State, Transition> guidedChooser = new GuidedTransitionChooser<>(
				result.enabler, traceFile);

		result.civlConfig.setReplay(true);
		result.chooser = guidedChooser;
		return result;
	}

	public static TracePlayer randomPlayer(GMCConfiguration config, Model model,
			PrintStream out, PrintStream err) throws CommandLineException,
			IOException, MisguidedExecutionException {
		TracePlayer result = new TracePlayer(config, model, out, err);
		BigInteger seedValue = (BigInteger) config.getAnonymousSection()
				.getValue(seedO);
		RandomTransitionChooser<State, Transition> chooser;

		if (seedValue == null)
			chooser = new RandomTransitionChooser<>(result.enabler);
		else {
			long seed;

			try {
				seed = seedValue.longValue();
			} catch (NumberFormatException e) {
				throw new CommandLineException(
						"Expected long value for seed, saw " + seedValue);
			}
			chooser = new RandomTransitionChooser<>(result.enabler, seed);
		}
		result.seed = chooser.getSeed();
		result.isRandom = true;
		result.chooser = chooser;
		return result;
	}

	TracePlayer(GMCConfiguration config, Model model, PrintStream out,
			PrintStream err) throws CommandLineException {
		super(config, model, out, err, false);
		civlConfig.setShowSavedStates(config.getAnonymousSection()
				.isTrue(CIVLConstants.showSavedStatesO));
		civlConfig.setVerbose(false);
		log.setSearcher(null);
		simulator = new Simulator<State, Transition>(stateManager, out);
		simulator.setPrintAllStates(false);
		simulator.setQuiet(civlConfig.isQuiet());
		simulator.setPredicate(predicate);
	}

	public TracePlayer(GMCConfiguration config, Model model,
			TransitionChooser<State, Transition> chooser, PrintStream out,
			PrintStream err) throws CommandLineException {
		this(config, model, out, err);
		this.chooser = chooser;
	}

	public TracePlayer(GMCConfiguration config, Model model, File traceFile,
			PrintStream out, PrintStream err) throws CommandLineException,
			IOException, MisguidedExecutionException {
		this(config, model, out, err);
		this.chooser = new GuidedTransitionChooser<State, Transition>(enabler,
				traceFile);
	}

	public Trace<Transition, State> run() throws MisguidedExecutionException {
		try {
			State initialState = stateFactory.initialState(model);
			Trace<Transition, State> trace = simulator.play(initialState,
					chooser, this.civlConfig.showTransitions())[0];
			boolean violation = trace.violation();

			violation = violation || log.numErrors() > 0;
			if (violation && !simulator.isQuiet()) {
				civlConfig.out().println("Violation(s) found.");
				civlConfig.out().flush();
			}
			trace.setViolation(violation);
			return trace;
		} catch (CIVLStateException stateException) {
			throw new CIVLExecutionException(stateException.kind(),
					stateException.certainty(), "", stateException.getMessage(),
					stateException.state(), stateException.source());
		}
	}

	public void printStats() {
		civlConfig.out()
				.println("   max process count   : " + stateManager.maxProcs());
		civlConfig.out().print("   states              : ");
		civlConfig.out().println(stateManager.numStatesExplored());
		if (isRandom)
			civlConfig.out().println("   seed                : " + seed);
	}

	/**
	 * Returns the random seed if this is a random simulator, otherwise 0.
	 * 
	 * @return the random seed
	 */
	public long getSeed() {
		return seed;
	}

	public boolean isRandom() {
		return isRandom;
	}

}
