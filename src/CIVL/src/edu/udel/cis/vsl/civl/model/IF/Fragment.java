package edu.udel.cis.vsl.civl.model.IF;

import java.io.PrintStream;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;

/**
 * A fragment is a sequence of statements. It has a "pointer" to the start
 * location and a set of statements as its final statements which should all
 * emanate from the same location.
 * 
 * @author Manchun Zheng
 * 
 */
public interface Fragment {

	/**
	 * Add a specified guard to the all statements of the start location. If a
	 * statement has an existing guard, then it will have a new guard which is a
	 * conjunction of the both. This method is used for translating a when
	 * statement, where it adds the guard of the when statement to the start
	 * location of its body fragment.
	 * 
	 * @param guard
	 *            The guard that is to be combined with
	 * @param factory
	 *            The model factory that provides some helper methods that are
	 *            useful in checking if an expression is True.
	 */
	void addGuardToStartLocation(Expression guard, ModelFactory factory);

	/**
	 * Add a statement to the final statement set
	 * 
	 * @param statement
	 *            the new statement to be added to the final statement set
	 */
	void addFinalStatement(Statement statement);

	/**
	 * Add a set of statements to the final statement set
	 * 
	 * @param stmtSet
	 *            the set of new statements to be added to the final statement
	 *            set
	 */
	void addFinalStatementSet(Set<Statement> stmtSet);

	/**
	 * Add a new statement to the fragment, which will be considered the
	 * subsequent statement of the current final statements.s
	 * 
	 * @param statement
	 *            the new statement to be added to the fragment
	 */
	void addNewStatement(Statement statement);

	/**
	 * Combine two fragment in sequential order. <br>
	 * Precondition: <code>this.lastStatement == null</code>
	 * 
	 * @param next
	 *            the fragment that comes after the current fragment
	 * @return the sequential combination of both fragments
	 */
	Fragment combineWith(Fragment next);

	/**
	 * Check if the fragment is empty
	 * 
	 * @return true iff both the start location and the last statement are null
	 */
	boolean isEmpty();

	/**
	 * 
	 * @return The set of final statements of this fragment
	 */
	Set<Statement> finalStatements();

	/**
	 * Precondition: finalStatements().size() == 1
	 * 
	 * @return The unique final statement of this fragment
	 */
	Statement uniqueFinalStatement();

	/**
	 * Combine this fragment and another fragment in parallel, i.e., merge the
	 * start location, and add the last statement of both fragments as the last
	 * statement of the result fragment
	 * 
	 * @param parallel
	 *            the second fragment to be combined with <dt>
	 *            <b>Preconditions:</b>
	 *            <dd>
	 *            this.startLocation.id() === parallel.startLocation.id()
	 * 
	 * @return the new fragment after the combination
	 */
	Fragment parallelCombineWith(Fragment parallel);

	/**
	 * Print the fragment
	 * 
	 * @param out
	 *            the print stream
	 */
	void print(PrintStream out);

	/**
	 * Update the start location of this fragment
	 * 
	 * @param location
	 *            The new start location
	 */
	void setStartLocation(Location location);

	/**
	 * Update the last statement of this fragment
	 * 
	 * @param statements
	 *            The new last statements
	 */
	void setFinalStatements(Set<Statement> statements);

	/**
	 * 
	 * @return The start location of this fragment
	 */
	Location startLocation();

	/**
	 * Update the start location with a new location
	 * 
	 * @param newLocation
	 *            the new start location
	 */
	void updateStartLocation(Location newLocation);

}
