package edu.udel.cis.vsl.civl.model.IF.contract;

import java.io.PrintStream;
import java.util.Iterator;

import edu.udel.cis.vsl.civl.model.IF.Sourceable;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;

/**
 * This represents a block of ACSL contract for a function.
 * 
 * @author Manchun Zheng
 *
 */
public interface FunctionContract extends Sourceable {

	/**
	 * ContractKind: This kind is used to denotes all kinds of contracts.
	 * Currently, there are 5 kinds of contracts:
	 * <ul>
	 * <li>REQUIRES: denotes that the requirements of the contracts are used to
	 * infer some states.</li>
	 * <li>ENSURES:denotes that the ensurances of the contracts are guaranteed
	 * by some states.</li>
	 * <li>INFER: denotes that the ensurances of the contracts are used to infer
	 * some states.</li>
	 * <li>WAITSFOR: delivers an explicit synchronization knowledge.</li>
	 * </ul>
	 * 
	 * @author xxxx
	 *
	 */
	static public enum ContractKind {
		REQUIRES, ENSURES, INFER, WAITSFOR, LOOP
	}

	/**
	 * Returns the default behavior of the function.
	 * 
	 * @return
	 */
	FunctionBehavior defaultBehavior();

	/**
	 * Returns the named behaviors of the function.
	 * 
	 * @return
	 */
	Iterable<NamedFunctionBehavior> namedBehaviors();

	/**
	 * Returns the guard of the function.
	 * 
	 * @return
	 */
	Expression guard();

	/**
	 * Does the contract contains <code>pure</code> clause?
	 * 
	 * @return
	 */
	boolean isPure();

	/**
	 * Updates the contract to denote if it contains <code>pure</code> clause.
	 * 
	 * @param value
	 */
	void setPure(boolean value);

	/**
	 * Sets the guard of the function.
	 * 
	 * @param expression
	 */
	void setGuard(Expression expression);

	/**
	 * Sets the default behavior of the function.
	 * 
	 * @param behavior
	 */
	void setDefaultBehavior(FunctionBehavior behavior);

	/**
	 * Adds a named behavior to the function.
	 * 
	 * @param behavior
	 */
	void addNamedBehavior(NamedFunctionBehavior behavior);

	/**
	 * Add an {@link MPICollectiveBehavior}
	 * 
	 * @param behavior
	 */
	void addMPICollectiveBehavior(MPICollectiveBehavior behavior);

	/**
	 * Returns a {@link Iterator} of a set of {@link MPICollectiveBehavior}s.
	 * 
	 * @return
	 */
	Iterable<MPICollectiveBehavior> getMPIBehaviors();

	/**
	 * returns the behavior with the given name; null if no such behavior
	 * exists.
	 * 
	 * @param name
	 * @return
	 */
	NamedFunctionBehavior getBehavior(String name);

	/**
	 * prints this function contract
	 * 
	 * @param prefix
	 * @param out
	 * @param isDebug
	 */
	void print(String prefix, PrintStream out, boolean isDebug);

	/**
	 * is there reads clause in this contract?
	 * 
	 * @return
	 */
	boolean hasReadsClause();

	/**
	 * is there assigns clause in this contract?
	 * 
	 * @return
	 */
	boolean hasAssignsClause();

	/**
	 * is there depends clause in this contract?
	 * 
	 * @return
	 */
	boolean hasDependsClause();

	/**
	 * is there any requirements or ensurances ?
	 * 
	 * @return
	 */
	boolean hasRequirementsOrEnsurances();

	/**
	 * Returns the number of {@link MPICollectiveBehavior}s.
	 * 
	 * @return
	 */
	int numMPICollectiveBehaviors();

	/**
	 * An efficient mark to indicate weather the {@link FunctionContract} has
	 * MPI waits-for.
	 * 
	 * @return
	 */
	boolean hasMPIWaitsfor();

	/**
	 * Set the efficient mark to tell if the function contracts has MPI
	 * waits-fors.
	 * 
	 * @param hasWaitsfor
	 */
	void setHasMPIWaitsfor(boolean hasWaitsfor);
}
