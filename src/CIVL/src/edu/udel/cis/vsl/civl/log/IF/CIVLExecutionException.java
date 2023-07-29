package edu.udel.cis.vsl.civl.log.IF;

import edu.udel.cis.vsl.civl.model.IF.CIVLException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.common.SystemCIVLSource;
import edu.udel.cis.vsl.civl.state.IF.StackEntry;
import edu.udel.cis.vsl.civl.state.IF.State;

/**
 * This represents an error during the execution of a program.
 * 
 * @author Manchun Zheng
 *
 */
public class CIVLExecutionException extends CIVLException {

	/**
	 * Added by Eclipse.
	 */
	private static final long serialVersionUID = 1L;

	private int pid = -1;

	private State state = null;

	private StringBuffer stateString;

	private Certainty certainty;

	private ErrorKind kind;

	private String process;

	private boolean reported = false;

	/**
	 * @param kind
	 *            the kind of error
	 * @param certainty
	 *            the certainty with which this is known to be an error in the
	 *            program being verified
	 * @param process
	 *            process name, i.e., "p"+process identifier
	 * @param message
	 *            a message explaining the error
	 * @param stateString
	 *            the string representation of the state where the error occurs;
	 *            may be null
	 * @param source
	 *            the source code element associated to the error; may be null
	 */
	public CIVLExecutionException(ErrorKind kind, Certainty certainty,
			String process, String message, State state, CIVLSource source) {
		super(message, source);
		assert kind != null;
		assert certainty != null;
		this.process = process;
		this.state = state;
		this.kind = kind;
		this.certainty = certainty;
		this.pid = -1;
	}

	public CIVLExecutionException(ErrorKind kind, Certainty certainty,
			String process, String message, StringBuffer stateString,
			State state, int pid, CIVLSource source) {
		this(kind, certainty, process, message, state, source);
		this.stateString = stateString;
		this.pid = pid;
	}

	/**
	 * @return the certainty of this error.
	 */
	public Certainty certainty() {
		return certainty;
	}

	/**
	 * @return the kind of this error.
	 */
	public ErrorKind kind() {
		return kind;
	}

	/**
	 * @return the state in which this error occurred.
	 */
	public State state() {
		return state;
	}

	/**
	 * Is this error reported?
	 * 
	 * @return true iff the error has already been reported
	 */
	public boolean isReported() {
		return this.reported;
	}

	/**
	 * Set this error to be reported.
	 */
	public void setReported() {
		this.reported = true;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		String sourceAbsPathName = source.getAbsoluteFilePath();
		String sysResourcePrefix = "/include";
		String transformersuffix = "Transformer";

		result.append("CIVL execution violation ");
		if (process != null) {
			result.append("in ");
			result.append(process);
			result.append(" ");
		}
		result.append("(kind: ");
		result.append(kind);
		result.append(", certainty: ");
		result.append(certainty);
		result.append(")");
		// The violation source
		if (source != null && !(source instanceof SystemCIVLSource)
				&& !sourceAbsPathName.startsWith(sysResourcePrefix)) {
			result.append("\nat ");
			if (sourceAbsPathName.endsWith(transformersuffix)) {
				result.append(this.source.getSummary(false));
			} else {
				result.append(this.source.getSummary(true));
			}
		}
		// The call stack info
		else if (pid >= 0) {
			for (StackEntry e : state.getProcessState(pid).getStackEntries()) {
				Location location = e.location();

				if (location != null) {
					CIVLSource tempSource = location.getSource();
					String tempSourceAbsPath = tempSource.getAbsoluteFilePath();

					if (tempSource != null
							&& !(tempSource instanceof SystemCIVLSource)
							&& !tempSourceAbsPath.startsWith(sysResourcePrefix)) {
						result.append("\nat ");
						if (tempSourceAbsPath.endsWith(transformersuffix)) {
							result.append(tempSource.getSummary(false));
						} else {
							result.append(tempSource.getSummary(true));
						}
						break;
					}
				}
			}
		}
		result.append("\n");
		result.append(this.getMessage());
		if (this.stateString != null) {
			result.append("\n");
			result.append(stateString);
		} else if (this.state != null) {
			result.append("\n");
			result.append(this.state.callStackToString());
		}
		return result.toString();
	}
}
