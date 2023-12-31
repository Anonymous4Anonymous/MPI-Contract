package edu.udel.cis.vsl.sarl.IF.config;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
import java.util.Set;

/**
 * Abstract interface representing information about an external theorem prover:
 * what kind of prover it is, where it can be found, etc.
 * 
 * @author xxxx
 *
 */
public interface ProverInfo extends Comparable<ProverInfo> {

	/**
	 * A classification of the different kinds of theorem provers. They are
	 * ordered from most preferred to least preferred by SARL.
	 */
	public static enum ProverKind {
		/**
		 * CVC4 using CVC4's presentation language through its command line
		 * interface.
		 */
		CVC4,
		/**
		 * Microsoft's Z3, using SMT-LIB2 through Z3's command line interface.
		 */
		Z3,
		/**
		 * Microsoft's Z3, using its Java API.
		 */
		Z3_API,
		/**
		 * CVC4, using its Java API.
		 */
		CVC4_API,
		/**
		 * Why3-prove-platform, calling through commandline.
		 */
		Why3
	};

	/**
	 * Returns the set of aliases associated to this theorem prover. The set
	 * should not be modified.
	 * 
	 * @return the set of aliases associated to this theorem prover
	 */
	Set<String> getAliases();

	/**
	 * Returns one of the alias strings in the set of aliases.
	 * 
	 * @return an alias string
	 */
	String getFirstAlias();

	/**
	 * Adds the given string to the set of aliases.
	 * 
	 * @param value
	 *            the string to add to the set of aliases
	 * @return <code>true</code> if the set of aliases did not already contain
	 *         the given value
	 */
	boolean addAlias(String value);

	/**
	 * Returns the sequence of command line options that should be used when
	 * invoking this theorem prover.
	 * 
	 * @return list of command line options
	 */
	List<String> getOptions();

	/**
	 * Adds the given string to the end of the list of options.
	 * 
	 * @param value
	 *            the string to add
	 */
	void addOption(String value);

	/**
	 * Returns time limit in seconds or -1 if the time is unlimited.
	 * 
	 * @return time limit in seconds
	 */
	double getTimeout();

	/**
	 * Sets the timeout, i.e., the time limit in seconds.
	 * 
	 * @param value
	 *            the time limit in seconds or -1 if the time is unlimited
	 */
	void setTimeout(double value);

	/**
	 * The kind of theorem prover this is. This is a classification by kind of
	 * input language the prover expects.
	 * 
	 * @return kind of this prover
	 */
	ProverKind getKind();

	/**
	 * Sets the prover kind.
	 * 
	 * @param value
	 *            the prover kind
	 */
	void setKind(ProverKind value);

	/**
	 * The path to the executable theorem prover. This should be an absolute
	 * path to an executable file.
	 * 
	 * @return path to executable
	 */
	File getPath();

	/**
	 * Sets the path to the executable theorem prover. This should be an
	 * absolute path to an executable file.
	 * 
	 * @param value
	 */
	void setPath(File value);

	/**
	 * @return The environment (the PATH for looking for executables) that is
	 *         needed by why3 to run the provers.
	 */
	String getEnv();

	/**
	 * @param environment
	 *            Set the environment (the PATH for looking for executables)
	 *            that is needed by why3 to run the provers.
	 */
	void setEnv(String environment);

	/**
	 * The version, e.g. "1.4".
	 * 
	 * @return version string
	 */
	String getVersion();

	/**
	 * Sets the version string.
	 * 
	 * @param value
	 *            the version string
	 */
	void setVersion(String value);

	/**
	 * Should the prover print all the queries?
	 * 
	 * @return <code>true</code> if this prover should print queries
	 */
	boolean getShowQueries();

	/**
	 * Tell this prover whether it should print every query.
	 * 
	 * @param value
	 *            <code>true</code> if this prover should print queries
	 */
	void setShowQueries(boolean value);

	/**
	 * Should the prover print a message every time it reports an inconclusive
	 * result?
	 * 
	 * @return <code>true</code> if a message should be printed for every
	 *         inconclusive result
	 */
	boolean getShowInconclusives();

	/**
	 * Sets whether the prover prints a message every time it reports an
	 * inconclusive result.
	 * 
	 * @param value
	 *            <code>true</code> if a message should be printed for every
	 *            inconclusive result
	 */
	void setShowInconclusives(boolean value);

	/**
	 * Should the prover print a message every time the underlying theorem
	 * prover reports an error? (In any case, the error is interpreted as an
	 * inconclusive result.)
	 * 
	 * @return <code>true</code> if a message should be printed for every error
	 */
	boolean getShowErrors();

	/**
	 * Sets whether the prover prints a message every time the underlying
	 * theorem prover reports an error. (In any case, the error is interpreted
	 * as an inconclusive result.)
	 * 
	 * @param <code>true</code>
	 *            if a message should be printed for every error
	 */
	void setShowErrors(boolean value);

	/**
	 * Prints a complete representation of this object in the format that is
	 * expected in a SARL configuration file.
	 * 
	 * @param out
	 *            where to print
	 */
	void print(PrintStream out);

	/**
	 * Is this an executable theorem prover (as opposed to an API-based prover)?
	 * 
	 * @return <code>true</code> if this is an executable prover
	 */
	boolean isExecutable();
}
