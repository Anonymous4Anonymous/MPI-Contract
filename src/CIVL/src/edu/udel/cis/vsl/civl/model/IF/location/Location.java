/**
 * 
 */
package edu.udel.cis.vsl.civl.model.IF.location;

import java.io.PrintStream;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.Sourceable;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.MemoryUnitExpression;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.NoopStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

/**
 * The parent of all locations.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public interface Location extends Sourceable {

	/**
	 * Atomic flags of a location:
	 * <ul>
	 * <li>NONE: no $atomic boundary;</li>
	 * <li>ATOMIC_ENTER/ATOM_ENTER: the location is the starting point of an
	 * $atomic block;</li>
	 * <li>ATOMIC_EXIT/ATOM_EXIT: the location is the ending point of an $atomic
	 * block.</li>
	 * </ul>
	 * 
	 * @author Manchun Zheng
	 * @author Wenhao Wu (xxxx@udel.edu)
	 */
	public enum AtomicKind {
		NONE, ATOMIC_ENTER, ATOMIC_EXIT
	}

	/**
	 * @return The unique ID number of this location.
	 */
	public int id();

	/**
	 * @return The scope of this location.
	 */
	public Scope scope();

	/**
	 * @return The function containing this location.
	 */
	public CIVLFunction function();

	/**
	 * @return The iterable object of incoming statements.
	 */
	public Iterable<Statement> incoming();

	/**
	 * @return The iterable object of outgoing statements.
	 */
	public Iterable<Statement> outgoing();

	/**
	 * @return The number of outgoing statements.
	 */
	public int getNumOutgoing();

	/**
	 * @return The number of incoming statements.
	 */
	public int getNumIncoming();

	/**
	 * 
	 * @param i
	 *            index of the statement
	 * @return The i'th outgoing statement
	 */
	public Statement getOutgoing(int i);

	/**
	 * 
	 * @param i
	 *            index of the statement
	 * @return The i'th incoming statement
	 */
	public Statement getIncoming(int i);

	/**
	 * Returns the sole outgoing statement from this location.
	 * 
	 * @return the outgoing statement
	 * @throws CIVLInternalException
	 *             if the number of outgoing statements from this location is
	 *             not 1
	 */
	public Statement getSoleOutgoing();

	/**
	 * Set the unique ID number of this location.
	 * 
	 * @param id
	 *            The unique ID number of this location.
	 */
	public void setId(int id);

	/**
	 * @param scope
	 *            The scope of this location.
	 */
	public void setScope(Scope scope);

	/**
	 * @param statement
	 *            A new incoming statement.
	 */
	public void addIncoming(Statement statement);

	/**
	 * @param statement
	 *            A new outgoing statement.
	 */
	public void addOutgoing(Statement statement);

	/**
	 * Print this location and all outgoing transitions.
	 * 
	 * @param prefix
	 *            The prefix string for all lines of this printout.
	 * @param out
	 *            The PrintStream to use for printing this location.
	 * @param isDebug
	 *            True iff the debugging option is enabled
	 */
	public void print(String prefix, PrintStream out, boolean isDebug);

	/**
	 * 
	 * @return true iff the location is purely local
	 */
	public boolean isPurelyLocal();

	/**
	 * Analyze if the location is purely local
	 */
	public void purelyLocalAnalysis();

	/**
	 * Remove a certain outgoing statement
	 * 
	 * @param statement
	 *            The outgoing statement to be removed
	 */
	void removeOutgoing(Statement statement);

	/**
	 * Remove a certain incoming statement
	 * 
	 * @param statement
	 *            The incoming statement to be removed
	 */
	void removeIncoming(Statement statement);

	/**
	 * This location is the start location of a certain atomic block
	 */
	void setEnterAtomic();

	/**
	 * This location is the end location of a certain atomic block
	 */
	void setLeaveAtomic();

	/**
	 * Check if the location is entering a general atomic block.
	 * 
	 * @return true iff the location is entering a general atomic block.
	 * 
	 */
	boolean enterAtomic();

	/**
	 * 
	 * @return true iff the location is leaving a general atomic block
	 */
	boolean leaveAtomic();

	/**
	 * Result might be:
	 * <ol>
	 * <li>NONE: a normal location</li>
	 * <li>ENTER: the start location of an $atomic block</li>
	 * <li>LEAVE: the end location of an $atomic block</li>
	 * </ol>
	 * 
	 * @return the atomic kind of the location
	 */
	AtomicKind atomicKind();

	/**
	 * This is different from isPurelyLocal(), because the latter is more
	 * restricted. Because the latter requires the location have exactly one
	 * incoming edge in order to avoid loop.
	 * 
	 * @return True iff every outgoing statement is purely local
	 */
	boolean allOutgoingPurelyLocal();

	// /**
	// * Analyze each outgoing statement to see if they are purely local
	// */
	// void purelyLocalAnalysisForOutgoing();

	/**
	 * During the translation of AST node into CIVL model, it is possible to
	 * know if a location with more than one incoming statement possible to be a
	 * loop location
	 * 
	 * @param possible
	 *            The value to be used to mark whether this location is possible
	 *            to be a loop location or not
	 */
	void setLoopPossible(boolean possible);

	/**
	 * Determines whether this location lies on a cycle in which every location
	 * has exactly one outgoing statement and that outgoing statement is a
	 * {@link NoopStatement}, then calls {@link #setInNoopLoop(boolean)} with
	 * appropriate boolean value.
	 */
	void loopAnalysis();

	/**
	 * The impact scope of a location is required in the enabler when an
	 * atomic/atom block is encountered, in which case the impact scope of all
	 * statements in the atomic block should be considered.
	 * 
	 * @return
	 */
	Scope impactScopeOfAtomicOrAtomBlock();

	/**
	 * set the impact scope of a location, only called when this.AtomicKind ==
	 * ATOM_ENTER or ATOMIC_ENTER.
	 * 
	 * @return
	 */
	void setImpactScopeOfAtomicOrAtomBlock(Scope scope);

	void computeWritableVariables(Set<Variable> addressedOfVariables);

	Set<Variable> writableVariables();

	/**
	 * This location or some location in the future contains dereferences of
	 * some pointers.
	 * 
	 * @return
	 */
	boolean hasDerefs();

	void setAsStart(boolean value);

	/**
	 * Returns true if this location is the start location of a function.
	 * 
	 * @return
	 */
	boolean isStart();

	/* Memory analysis information of location */

	/**
	 * Returns the impact memory unit expressions of this location.
	 * 
	 * @return
	 */
	Set<MemoryUnitExpression> impactMemUnits();

	Set<MemoryUnitExpression> reachableMemUnitsWtPointer();

	Set<MemoryUnitExpression> reachableMemUnitsWoPointer();

	void setImpactMemoryUnit(Set<MemoryUnitExpression> impacts);

	void setReachableMemUnitsWtPointer(Set<MemoryUnitExpression> reachable);

	void setReachableMemUnitsWoPointer(Set<MemoryUnitExpression> reachable);

	void setSystemCalls(Set<CallOrSpawnStatement> systemCalls);

	Set<CallOrSpawnStatement> systemCalls();

	/**
	 * Is a spawn statement reachable from this location?
	 * 
	 * @return true iff a spawn statement is reachable from this location.
	 */
	boolean hasSpawn();

	void staticAnalysis();

	/**
	 * Mark if the loop is a safe loop. For "safe loop", see
	 * {@link #isSafeLoop()}
	 * 
	 * @param value
	 *            True to mark the loop as safe loop; false as may not be a safe
	 *            loop.
	 */
	void setSafeLoop(boolean value);

	/**
	 * <p>
	 * Returns true if this loop satisfies the following conditions:
	 *
	 * <ol>
	 * <li>has one iteration variable</li>
	 * <li>the iteration variable is only modified by the last statement
	 * (incremental)</li>
	 * <li>the condition has the form <code>i < N</code> (or <code>i > N</code>)
	 * </li>
	 * <li>the loop has finite iterations (can be decided statically)</li>
	 * </ol>
	 * </p>
	 * 
	 * @return
	 */
	boolean isSafeLoop();

	/**
	 * are the disjunction of the guards of all outgoing statements of this
	 * location guarded not TRUE?
	 * 
	 * @return
	 */
	boolean isGuardedLocation();

	/**
	 * Determines if this location lies on a cycle in which every location has
	 * exactly one outgoing statement and that outgoing statement is a
	 * {@link NoopStatement}.
	 * 
	 * 
	 * @return True iff the location is in a cycle in which every location has
	 *         exactly one outgoing statement and that outgoing statement is a
	 *         {@link NoopStatement}.
	 */
	boolean isInNoopLoop();

	/**
	 * returns the path condition of this location from the start location
	 * 
	 * @return
	 */
	Expression pathCondition();

	void setPathcondition(Expression expression);

	/**
	 * update this location to denote if it is a binary branching location
	 * 
	 * @param value
	 *            the value to be used
	 */
	void setBinaryBranching(boolean value);

	/**
	 * if this is a location that contains two outgoing statement and the guards
	 * are expr and !expr, repectively.
	 * 
	 * @return
	 */
	boolean isBinaryBranching();

	/**
	 * Marks this location as a switch or $choose statement location who has a
	 * 'default' case. Lets {@link #isSwitchOrChooseWithDefault()} return true.
	 */
	void setSwitchOrChooseWithDefault();

	/**
	 * Returns true iff this is a switch or $choose statement location where a
	 * set of branch statements emanate from and a default case for it was
	 * specified.
	 * 
	 * @return true iff this is a switch or $choose statement location where a
	 *         set of branch statements emanate from and a default case for it
	 *         was specified.
	 */
	boolean isSwitchOrChooseWithDefault();

	/**
	 * returns true iff this location has more than one incoming location and is
	 * inside a loop.
	 * 
	 * @return true iff this location has more than one incoming location and is
	 *         inside a loop.
	 */
	boolean isInLoop();

	/**
	 * @return True iff this location is an atomic block entry and the
	 *         termination of the atomic block is NOT determined.
	 */
	boolean isEntryOfUnsafeAtomic();

	/**
	 * Set the mark of atomic block termination.
	 * 
	 * @param unsafe
	 *            Set to true if this location is an atomic block entry and the
	 *            termination of the atomic block is NOT determined.
	 */
	void setEntryOfUnsafeAtomic(boolean unsafe);

	/**
	 * returns true iff this location is the SLEEP location, which has no
	 * outgoing statement
	 * 
	 * @return
	 */
	boolean isSleep();

	/**
	 * @return true iff this location is the entry of a local block, i.e., this
	 *         location is associated with a system function call
	 *         <code>$local_start</code>
	 */
	boolean isEntryOfLocalBlock();

	/**
	 * @param isEntryOfLocalBlock
	 *            true to mark that this location {@link #isEntryOfLocalBlock()}
	 */
	void setIsEntryOfLocalBlock(boolean isEntryOfLocalBlock);
}
