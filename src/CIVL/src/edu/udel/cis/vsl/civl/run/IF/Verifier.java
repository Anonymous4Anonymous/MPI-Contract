package edu.udel.cis.vsl.civl.run.IF;

import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.collectOutputO;
import static edu.udel.cis.vsl.civl.config.IF.CIVLConstants.maxdepthO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;
import edu.udel.cis.vsl.civl.log.IF.CIVLExecutionException;
import edu.udel.cis.vsl.civl.log.IF.CIVLLogEntry;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.Certainty;
import edu.udel.cis.vsl.civl.model.IF.CIVLException.ErrorKind;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.predicate.IF.Predicates;
import edu.udel.cis.vsl.civl.run.common.VerificationStatus;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.CIVLStateException;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Printable;
import edu.udel.cis.vsl.gmc.CommandLineException;
import edu.udel.cis.vsl.gmc.ExcessiveErrorException;
import edu.udel.cis.vsl.gmc.GMCConfiguration;
import edu.udel.cis.vsl.gmc.StateSpaceCycleException;
import edu.udel.cis.vsl.gmc.seq.DfsSearcher;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public class Verifier extends Player {

	private String errorBoundExceeds;

	VerificationStatus verificationStatus;

	class SearchUpdater implements Printable {
		@Override
		public void print(PrintStream out) {
			long time = (long) Math
					.ceil((System.currentTimeMillis() - startTime) / 1000.0);
			long megabytes = (long) (((double) Runtime.getRuntime()
					.totalMemory()) / (double) 1048576.0);

			out.print(time + "s: ");
			out.print("mem=" + megabytes + "Mb");
			out.print(" trans=" + executor.getNumSteps());
			out.print(" traceSteps=" + searcher.numTransitions());
			out.print(" explored=" + stateManager.numStatesExplored());
			out.print(" saved=" + searcher.numOfSearchNodeSaved());
			out.print(
					" prove=" + modelFactory.universe().numProverValidCalls());
			out.println();
		}
	}

	/**
	 * Updater for the web app. Instead of printing to the given stream, it
	 * ignores that stream and instead creates a new file and prints the data to
	 * that file.
	 * 
	 * @author xxxx
	 *
	 */
	class WebUpdater implements Printable {

		/**
		 * String used to form the name of the file that will be created. This
		 * string will have the time step appended to it to create a unique
		 * file.
		 */
		private String filenameRoot;

		/**
		 * The directory in which the new file will be created.
		 */
		private File directory;

		/**
		 * The number of times {@link #print(PrintStream)} has been called on
		 * this updater.
		 */
		private int timeStep = 0;

		public WebUpdater(File directory, String filenameRoot) {
			this.directory = directory;
			this.filenameRoot = filenameRoot;
		}

		private void fail(File file, String msg) {
			throw new CIVLInternalException(msg + " " + file.getAbsolutePath(),
					(CIVLSource) null);
		}

		private void print(boolean isFinal) {
			double time = Math.ceil(
					(System.currentTimeMillis() - startTime) / 100.0) / 10.0;
			long megabytes = (long) (((double) Runtime.getRuntime()
					.totalMemory()) / (double) 1048576.0);
			File file;

			timeStep++;
			file = new File(directory, filenameRoot + "_" + timeStep + ".txt");
			try {
				if (file.exists())
					file.delete();
				file.createNewFile();

				FileOutputStream stream = new FileOutputStream(file);
				FileChannel channel = stream.getChannel();
				FileLock lock = channel.lock();
				PrintStream out = new PrintStream(stream);

				out.println("{");
				out.println("\"time\" : " + time + " ,");
				out.println("\"mem\" : " + megabytes + " ,");
				out.println("\"steps\" : " + searcher.numTransitions() + " ,");
				out.println("\"trans\" : " + executor.getNumSteps() + " ,");
				out.println("\"seen\" : " + searcher.numStatesSeen() + " ,");
				out.println("\"saved\" : " + searcher.numOfSearchNodeSaved()
						+ " ,");
				out.println("\"prove\" : "
						+ modelFactory.universe().numProverValidCalls() + " ,");
				out.println("\"counterexample\" : "
						+ log.getMinimalCounterexampleSize() + ",");
				out.println("\"isFinal\" : " + isFinal);
				out.println("}");
				out.flush();
				lock.release();
				out.close();
			} catch (IOException e) {
				fail(file, "Could not write to file");
			}
		}

		@Override
		public void print(PrintStream out) {
			print(false);
		}

		public void printFinal() {
			print(true);
		}
	}

	/**
	 * Runnable to be used to create a thread that every so many seconds tells
	 * the state manager to print an update message.
	 * 
	 * @author xxxx
	 * 
	 */
	class UpdaterRunnable implements Runnable {

		/**
		 * Number of milliseconds between sending update message to state
		 * manager.
		 */
		private long millis;

		/**
		 * Constructs new runnable with given number of milliseconds.
		 * 
		 * @param millis
		 *            number of milliseconds between update messages
		 */
		public UpdaterRunnable(long millis) {
			this.millis = millis;
		}

		/**
		 * Runs this thread. The thread will loop forever until interrupted,
		 * then it will terminate.
		 */
		@Override
		public void run() {
			while (alive) {
				try {
					Thread.sleep(millis);
					stateManager.printUpdate();
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Should the update thread run?
	 */
	private volatile boolean alive = true;

	/**
	 * Number of seconds between printing updates.
	 */
	private int updatePeriod;

	/**
	 * The object used to print the update message.
	 */
	private Printable updater;

	/**
	 * The update thread itself.
	 */
	private Thread updateThread = null;

	/**
	 * The object used to perform the depth-first search of the state space.
	 * 
	 */
	private DfsSearcher<State, Transition> searcher;

	// private boolean shortFileNamesShown;

	/**
	 * The time at which execution started, as a double.
	 */
	private double startTime;

	public Verifier(GMCConfiguration config, Model model, PrintStream out,
			PrintStream err, double startTime, boolean collectOutputs,
			String[] outputNames,
			Map<BooleanExpression, Set<Pair<State, SymbolicExpression[]>>> specOutputs)
			throws CommandLineException {
		super(config, model, out, err, collectOutputs);
		if (random) {
			throw new CommandLineException(
					"\"-random\" mode is incompatible with civl verify command.");
		}
		this.startTime = startTime;
		if (outputNames != null && specOutputs != null)
			this.addPredicate(
					Predicates.newFunctionalEquivalence(modelFactory.universe(),
							symbolicAnalyzer, outputNames, specOutputs));
		searcher = new DfsSearcher<State, Transition>(enabler, stateManager,
				predicate, config);
		if (civlConfig.debug())
			searcher.setDebugOut(out);
		searcher.setReportCycleAsViolation(civlConfig.cyclesViolate());
		searcher.setName(sessionName);
		log.setSearcher(searcher);
		if (minimize)
			log.setMinimize(true);
		if (config.getAnonymousSection().getValue(maxdepthO) != null)
			searcher.boundDepth(maxdepth);
		if (config.getAnonymousSection().isTrue(CIVLConstants.webO)) {
			updatePeriod = CIVLConstants.webUpdatePeriod;
			updater = new WebUpdater(new File(CIVLConstants.CIVLREP), "update");
		} else {
			updatePeriod = CIVLConstants.consoleUpdatePeriod;
			updater = new SearchUpdater();
		}
		stateManager.setUpdater(updater);
		this.errorBoundExceeds = "Terminating search after finding "
				+ this.log.errorBound() + " violation";
		if (log.errorBound() > 1)
			errorBoundExceeds += "s";
		errorBoundExceeds += ".";
	}

	public Verifier(GMCConfiguration config, Model model, PrintStream out,
			PrintStream err, double startTime, boolean collectOutputs)
			throws CommandLineException {
		this(config, model, out, err, startTime, collectOutputs, null, null);
	}

	public Verifier(GMCConfiguration config, Model model, PrintStream out,
			PrintStream err, double startTime, String[] outputNames,
			Map<BooleanExpression, Set<Pair<State, SymbolicExpression[]>>> specOutputs)
			throws CommandLineException {
		this(config, model, out, err, startTime, false, outputNames,
				specOutputs);
	}

	public Verifier(GMCConfiguration config, Model model, PrintStream out,
			PrintStream err, double startTime) throws CommandLineException {
		this(config, model, out, err, startTime,
				config.getAnonymousSection().isTrue(collectOutputO), null,
				null);
	}

	/**
	 * Prints only those metrics specific to this Verifier. General metrics,
	 * including time, memory, symbolic expressions, etc., are dealt with in the
	 * general UserInterface class.
	 */
	public void printStats() {
		civlConfig.out().print("   max process count   : ");
		civlConfig.out().println(stateManager.maxProcs());
		civlConfig.out().print("   states              : ");
		civlConfig.out().println(stateManager.numStatesExplored());
		civlConfig.out().print("   states saved        : ");
		civlConfig.out().println(searcher.numOfSearchNodeSaved());
		// civlConfig.out().print(" statesSeen : ");
		// civlConfig.out().println(searcher.numStatesSeen());
		civlConfig.out().print("   state matches       : ");
		civlConfig.out().println(searcher.numStatesMatched());
		civlConfig.out().print("   transitions         : ");
		civlConfig.out().println(executor.getNumSteps());
		civlConfig.out().print("   trace steps         : ");
		civlConfig.out().println(searcher.numTransitions());
	}

	public boolean run() throws FileNotFoundException, InterruptedException,
			ExecutionException {
		final int timeout = this.civlConfig.timeout();

		if (timeout > 0) {
			final ScheduledExecutorService verifier_scheduler = Executors
					.newScheduledThreadPool(2);
			final Callable<Boolean> verify_run_work = new Callable<Boolean>() {
				public Boolean call() {
					try {
						return run_work();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return false;
				}
			};
			final ScheduledFuture<?> verify_handler = verifier_scheduler
					.schedule(verify_run_work, 0, TimeUnit.MILLISECONDS);
			final Runnable timeOutWork = new Runnable() {
				public void run() {
					verify_handler.cancel(true);
					if (result == null) {
						result = "Time out.";
						verificationStatus = new VerificationStatus(
								stateManager.maxProcs(),
								stateManager.numStatesExplored(),
								searcher.numOfSearchNodeSaved(),
								searcher.numStatesMatched(),
								executor.getNumSteps(),
								searcher.numTransitions());
					}
					return;
				}
			};

			verifier_scheduler.schedule(timeOutWork, timeout, TimeUnit.SECONDS);
			Object tmp = verify_handler.get();
			verifier_scheduler.shutdownNow();
			if (tmp != null)
				return (boolean) tmp;
			return false;
		} else {
			return run_work();
		}
	}

	public boolean run_work() throws FileNotFoundException {
		try {
			State initialState = stateFactory.initialState(model);
			boolean violationFound = false;

			updateThread = new Thread(new UpdaterRunnable(updatePeriod * 1000));
			if (civlConfig.runtimeUpdate())
				updateThread.start();
			if (civlConfig.debugOrVerbose() || civlConfig.showStates()
					|| civlConfig.showSavedStates()) {
				civlConfig.out().println();
				civlConfig.out().print(
						symbolicAnalyzer.stateToString(initialState, 0, -1));
				civlConfig.out().println();
			}
			try {
				while (true) {
					boolean workRemains;

					try {
						if (violationFound) {
							// may throw ExcessiveErrorException...
							workRemains = searcher.proceedToNewState()
									? searcher.search()
									: false;
						} else {
							// may throw ExcessiveErrorException...
							workRemains = searcher.search(initialState);
						}
						if (!workRemains)
							break;
					} catch (StateSpaceCycleException e) {
						// a cycle in state space detected:
						int stackPos = e.stackPos();
						int stackSize = searcher.stack().size();
						Transition lastTran = (stackPos < stackSize - 1)
								? searcher.stack().get(stackSize - 2).peek()
								: searcher.stack().peek().peek();
						State lastState = searcher.stack().peek().getState();
						String process = lastState
								.getProcessState(lastTran.pid()).name();
						CIVLSource source = lastTran.statement().getSource();
						CIVLExecutionException cycleException = new CIVLExecutionException(
								ErrorKind.TERMINATION, Certainty.CONCRETE,
								process,
								"A cycle in state space detected.  This execution will not terminate.",
								lastState, source);
						CIVLLogEntry entry = new CIVLLogEntry(civlConfig,
								config, cycleException, evaluator.universe());

						log.report(entry); // may throw ExcessiveErrorException
						continue; // cycle violation logged, continue the search
					}
					violationFound = true;

					CIVLLogEntry entry = new CIVLLogEntry(civlConfig, config,
							this.predicate.getUnreportedViolation(),
							evaluator.universe());

					log.report(entry); // may throw ExcessiveErrorException
				}
			} catch (ExcessiveErrorException e) {
				violationFound = true;
				if (!civlConfig.isQuiet()) {
					civlConfig.out().println(errorBoundExceeds);
				}
			}
			terminateUpdater();
			if (violationFound || log.numEntries() > 0) {
				result = "The program MAY NOT be correct.  See "
						+ log.getLogFile();
				try {
					log.save();
				} catch (FileNotFoundException e) {
					System.err.println(
							"Failed to print log file " + log.getLogFile());
				}
			} else {
				result = "The standard properties hold for all executions.";
			}
			this.verificationStatus = new VerificationStatus(
					stateManager.maxProcs(), stateManager.numStatesExplored(),
					searcher.numOfSearchNodeSaved(),
					searcher.numStatesMatched(), executor.getNumSteps(),
					searcher.numTransitions());
			return !violationFound && log.numEntries() == 0;
		} catch (CIVLStateException stateException) {
			throw new CIVLExecutionException(stateException.kind(),
					stateException.certainty(), "", stateException.getMessage(),
					stateException.state(), stateException.source());
		}

	}

	/**
	 * Terminates the update thread. This will be called automatically if
	 * control exits normally from {@link #run_work()}, but if an exception is
	 * thrown and caught elsewhere, this method should be called.
	 */
	public void terminateUpdater() {
		alive = false;
		if (updateThread != null)
			updateThread.interrupt();
		updateThread = null;
		if (civlConfig.web()) {
			// last update with final stats needed for web page...
			((WebUpdater) updater).printFinal();
		}
	}
}
