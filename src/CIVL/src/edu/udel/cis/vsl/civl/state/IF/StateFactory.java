/**
 * 
 */
package edu.udel.cis.vsl.civl.state.IF;

import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.dynamic.IF.DynamicMemoryLocationSet;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLMemType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLStateType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.state.IF.CIVLHeapException.HeapErrorKind;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * The state factory is used to create all state objects.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * @author Timothy J. McClory (tmcclory)
 * @author Stephen F. Siegel (xxxx)
 * 
 */
public interface StateFactory {

	/**
	 * Returns the symbolic universe used by this factory to manipulate symbolic
	 * expressions.
	 * 
	 * @return the symbolic universe
	 */
	SymbolicUniverse symbolicUniverse();

	/**
	 * Return the "canonical" version of the given state.
	 * 
	 * The state returned will satisfy all of the following:
	 * <ul>
	 * <li>it will be observationally equivalent to the given state, i.e., there
	 * is no way a CIVL-C program can distinguish between the two states</li>
	 * <li>there will be no gaps in the dynamic scope IDs and no null dynamic
	 * scopes</li>
	 * <li>there will be no gaps in the PIDs and no null process states</li>
	 * <li>every dynamic scope will be reachable (starting from the frame of the
	 * call stack of one of the processes and following parent edges in the
	 * dyscope tree)
	 * <li>the state returned will be the unique representative of its
	 * equivalence class, i.e., if this method is invoked with two equivalent
	 * states, it will return the same object</li>
	 * </ul>
	 * 
	 * Note that the state returned may in fact be the same as the one given.
	 * 
	 * Note that this does everything that methods {@link #collectScopes(State)}
	 * and {@link #collectProcesses} do. So there is no need to call those
	 * methods if you are already calling this method.
	 * 
	 * This method may go further in simplifying the state. This is up to the
	 * particular implementation.
	 * 
	 * @param state
	 *            any non-null CIVL state
	 * @param collectProcesses
	 *            shall processes be collected?
	 * @param collectScopes
	 *            shall scopes be collected?
	 * @param collectHeaps
	 *            shall heaps be collected?
	 * 
	 * @return the canonical version of the given state
	 */
	State canonic(State state, boolean collectProcesses, boolean collectScopes,
			boolean collectHeaps, boolean collectSymbolicConstants,
			boolean simplify, Set<HeapErrorKind> toBeIgnored)
			throws CIVLHeapException;

	/**
	 * Returns the canonic, initial state for a CIVL Model.
	 * 
	 * @return the initial state
	 */
	State initialState(Model model) throws CIVLHeapException;

	/**
	 * Updates the value assigned to a variable in the state. Specifically,
	 * returns a state which is equivalent to the given one, except that the
	 * value assigned to the specified variable is replaced by the given value.
	 * 
	 * @param state
	 *            The old state
	 * @param variable
	 *            The variable to update
	 * @param pid
	 *            The PID of the process containing the variable
	 * @param value
	 *            The new value to be assigned to the variable
	 * @return A new state that is the old state modified by updating the value
	 *         of the variable
	 */
	State setVariable(State state, Variable variable, int pid,
			SymbolicExpression value);

	/**
	 * <p>
	 * Updates the value assigned to a variable in the state. Specifically,
	 * returns a state which is equivalent to the given one, except that the
	 * value assigned to the specified variable is replaced by the given value.
	 * </p>
	 * <p>
	 * In this version of the method, the variable is specified by its dynamic
	 * scope ID and variable ID.
	 * </p>
	 * 
	 * @param state
	 *            The old state
	 * @param vid
	 *            variable ID number
	 * @param scopeID
	 *            The ID of the dynamic scope containing the variable. This
	 *            version of the method is useful when setting the target of a
	 *            pointer. For a variable in the current lexical scope, use the
	 *            version of the method without this argument
	 * @param value
	 *            The new value to assign to the variable
	 * @return A new state that is the old state modified by updating the value
	 *         of the variable
	 * @see #setVariable(State, Variable, int, SymbolicExpression)
	 */
	State setVariable(State state, int vid, int scopeId,
			SymbolicExpression value);

	/**
	 * <p>
	 * Adds a new process. The new process is created and one entry is pushed
	 * onto its call stack. That entry will have a dynamic scope whose parent is
	 * determined by the calling process (the process that is executing the
	 * spawn command to create this new process) and the given function. The
	 * parent dynamic scope is computed by starting with the current dynamic
	 * scope of the caller, and working up the parent chain, stopping at the
	 * first dynamic scope whose static scope matches the containing scope of
	 * the function. If no such dynamic scope is found in the chain, an
	 * IllegalArgumentException is thrown. Hence the calling process must have a
	 * non-empty call stack.
	 * </p>
	 * <p>
	 * The PID of the new process will be {@link State#numProcs()}, where state
	 * is the pre-state (the given state), not the new state.
	 * </p>
	 * 
	 * @param state
	 *            The old state.
	 * @param function
	 *            The function in which the new process starts.
	 * @param arguments
	 *            The arguments to this function call.
	 * @param callerPid
	 *            the PID of the process that is creating the new process
	 * @param isSelfDestructable
	 *            If the process is self-destructable. See
	 *            {@link ProcessState#isSelfDestructable()}
	 * @return A new state that is the old state modified by adding a process
	 *         whose location is the start location of the function and with a
	 *         new dynamic scope corresponding to the outermost lexical scope of
	 *         the function.
	 */
	State addProcess(State state, CIVLFunction function,
			SymbolicExpression[] arguments, int callerPid,
			boolean isSelfDestructable);

	/**
	 * <p>
	 * Add a boolean value clause to the path condition of the given state. The
	 * new path condition is the conjunction of the old path condition of the
	 * given state and the given clause.
	 * 
	 * Returns a new state which is same as the given one but owns the new path
	 * condition.
	 * </p>
	 * 
	 * <p>
	 * Based on the semantics of symbolic execution, this is the one of the only
	 * two ways to update a path condition (1. conjunction with a new clause; 2.
	 * simplification).
	 * </p>
	 * 
	 * @param state
	 *            The state before the path condition being updated.
	 * @param pid
	 *            The PID of the calling process.
	 * @param clause
	 *            The boolean value symbolic expression which will be added to
	 *            the path condition.
	 * @return A new state which is same as the given one but owns the new path
	 *         condition.
	 */
	State addToPathcondition(State state, int pid, BooleanExpression clause);

	/**
	 * <p>
	 * Adds a new process. The new process is created and one entry is pushed
	 * onto its call stack. That entry will have a dynamic scope whose parent is
	 * determined by the calling process (the process that is executing the
	 * spawn command to create this new process) and the given function. The
	 * parent dynamic scope is computed by starting with the current dynamic
	 * scope of the caller, and working up the parent chain, stopping at the
	 * first dynamic scope whose static scope matches the containing scope of
	 * the function. If no such dynamic scope is found in the chain, an
	 * IllegalArgumentException is thrown. Hence the calling process must have a
	 * non-empty call stack.
	 * </p>
	 * <p>
	 * The PID of the new process will be {@link State#numProcs()}, where state
	 * is the pre-state (the given state), not the new state.
	 * </p>
	 * 
	 * @param state
	 *            The old state.
	 * @param function
	 *            The function in which the new process starts.
	 * @param functionParentDyscope
	 *            The dyscope ID of the parent of the new function
	 * @param arguments
	 *            The arguments to this function call.
	 * @param callerPid
	 *            the PID of the process that is creating the new process
	 * @param isSelfDestructable
	 *            If the process is self-destructable. See
	 *            {@link ProcessState#isSelfDestructable()}
	 * @return A new state that is the old state modified by adding a process
	 *         whose location is the start location of the function and with a
	 *         new dynamic scope corresponding to the outermost lexical scope of
	 *         the function.
	 */
	State addProcess(State state, CIVLFunction function,
			int functionParentDyscope, SymbolicExpression[] arguments,
			int callerPid, boolean isSelfDestructable);

	/**
	 * Sets the process state for the designated process to be the process state
	 * with the empty stack.
	 * 
	 * @param state
	 *            the old state
	 * @param pid
	 *            the PID of the process to terminate
	 * @return state that is identical to old except that the process state for
	 *         process PID has been set to the process state with the empty
	 *         stack
	 */
	State terminateProcess(State state, int pid);

	/**
	 * Removes a process from the state. The process state associated to that
	 * process is set to null. No other part of the state is affected. To really
	 * get rid of the process state you need to call {@link #collectProcesses}.
	 * 
	 * @param state
	 *            The old state
	 * @param pid
	 *            The PID
	 * @return A new state that is the same as the old state with the process
	 *         state set to null
	 */
	State removeProcess(State state, int pid);

	/**
	 * Sets the location of a process. This changes the top stack frame for the
	 * process so that it points to the new location. The given process must
	 * have a non-empty stack (although the location component of that frame is
	 * not used, so it is OK if it is null). There is no change of the access of
	 * variables from the current location to the target location.
	 * 
	 * This may involve adding and removing scopes, if the scope of the new
	 * location differs from the original scope.
	 * 
	 * @param state
	 *            The old state.
	 * @param pid
	 *            The PID of the process making the move.
	 * @param location
	 *            The target location.
	 * @return A new state that is the same as the old state with the given
	 *         process at a new location, and scopes added and removed as
	 *         necessary
	 */
	State setLocation(State state, int pid, Location location);

	/**
	 * Sets the location of a process. This changes the top stack frame for the
	 * process so that it points to the new location. The given process must
	 * have a non-empty stack (although the location component of that frame is
	 * not used, so it is OK if it is null).
	 * 
	 * This may involve adding and removing scopes, if the scope of the new
	 * location differs from the original scope.
	 * 
	 * @param state
	 *            The old state.
	 * @param pid
	 *            The PID of the process making the move.
	 * @param location
	 *            The target location.
	 * @param accessChanged
	 *            True iff there is change of variable accessing (write or
	 *            read-only) from the current location to the target location
	 * @return A new state that is the same as the old state with the given
	 *         process at a new location, and scopes added and removed as
	 *         necessary
	 */
	State setLocation(State state, int pid, Location location,
			boolean accessChanged);

	/**
	 * Pushes a new entry onto the call stack for a process. Used when a process
	 * calls a function. The process should already exist and have a non-empty
	 * call stack.
	 * 
	 * @param state
	 *            The old state
	 * @param pid
	 *            The PID of the process making the call
	 * @param function
	 *            The function being called
	 * @param arguments
	 *            The (actual) arguments to the function being called
	 * @return A new state that is the same as the old state with the given
	 *         process having a new entry on its call stack.
	 */
	State pushCallStack(State state, int pid, CIVLFunction function,
			SymbolicExpression[] arguments);

	/**
	 * Pushes a new entry onto the call stack for a process. Used when a process
	 * calls a function. The process should already exist and have a non-empty
	 * call stack.
	 * 
	 * @param state
	 *            The old state
	 * @param pid
	 *            The PID of the process making the call
	 * @param function
	 *            The function being called
	 * @param functionParentDyscope
	 *            The dyscope ID of the parent of the new function
	 * @param arguments
	 *            The (actual) arguments to the function being called
	 * @return A new state that is the same as the old state with the given
	 *         process having a new entry on its call stack.
	 */
	State pushCallStack(State state, int pid, CIVLFunction function,
			int functionParentDyscope, SymbolicExpression[] arguments);

	/**
	 * Pops an entry off the call stack for a process. Does not modify or remove
	 * and dynamic scopes (even if they become unreachable). Does not nullify or
	 * remove the process state (even if the call stack becomes empty).
	 * 
	 * @param state
	 *            The old state.
	 * @param pid
	 *            The PID of the process returning from a call.
	 * @return A new state that is the same as the old state but with the call
	 *         stack for the given process popped.
	 */
	State popCallStack(State state, int pid);

	/**
	 * Simplifies all variable values in the state, using the path condition as
	 * the simplification context. A symbolic constant which is determined to
	 * have a concrete value (based on the path condition), may be entirely
	 * removed from the state by replacing every occurrence of that symbol with
	 * the concrete value.
	 * 
	 * @param state
	 *            Any State
	 * @return The simplified state
	 */
	State simplify(State state);

	/**
	 * Returns the number of objects of type State that have been instantiated
	 * since this JVM started.
	 * 
	 * @return the number of states instantiated
	 */
	long getNumStateInstances();

	/**
	 * Performs a garbage collection and canonicalization of heaps.
	 * 
	 * Computes the set of reachable heap objects, and removes all unreachable
	 * heap objects. Renumbers heap objects in a canonic way. Updates all
	 * pointers in the state accordingly. This operation should be completely
	 * invisible to the user.
	 * 
	 * @param state
	 *            a state
	 * @return the state after canonicalizing heaps, which may be this state or
	 *         a new one
	 */
	State collectHeaps(State state, Set<HeapErrorKind> toBeIgnored)
			throws CIVLStateException;

	/**
	 * Performs a garbage collection and canonicalization of dynamic scopes.
	 * 
	 * Compute the set of reachable dynamic scopes, and removes any which are
	 * unreachable. Renumbers the dynamic scopes in a canonic way. Updates all
	 * scope references in the state. This operation should be completely
	 * invisible to the user.
	 * 
	 * @param state
	 *            a state
	 * @return the state after canonicalizing scopes, which may be this state or
	 *         a new one
	 */
	State collectScopes(State state, Set<HeapErrorKind> toBeIgnored)
			throws CIVLStateException;

	/**
	 * Performs a garbage collection and canonicalization of the process states.
	 * Removes any process state that is null. Renumbers the PIDs so that there
	 * are no gaps (and start from 0).
	 * 
	 * @param state
	 *            any non-null CIVL state
	 * @return the state with processes collected
	 */
	State collectProcesses(State state);

	/**
	 * Checks if any process at the state is holding the atomic lock, i.e, the
	 * process is executing some atomic blocks.
	 * <p>
	 * This information is maintained as a global variable of <code>$proc</code>
	 * type in the root scope in the CIVL model (always with index 0), and it
	 * gets automatically updated when process id's are renumbered.
	 * </p>
	 * 
	 * @param state
	 *            The state to be checked
	 * @return True iff the value of the variable atomic lock is not undefined.
	 */
	boolean lockedByAtomic(State state);

	/**
	 * Returns the PID of the process that holds the atomic lock at a certain
	 * state
	 * 
	 * @param state
	 *            The state to be checked
	 * @return -1 iff there is no process holding the atomic lock, otherwise
	 *         return the process that holds the atomic lock
	 */
	int processInAtomic(State state);

	/**
	 * Declares that the process with the given PID now owns the atomic lock.
	 * Precondition: no process is holding the atomic lock in the given state.
	 * 
	 * @param state
	 *            any non-null CIVL state
	 * @param pid
	 *            The PID of the process that is going to take the atomic lock
	 * @return a state equivalent to given one except that process PID now owns
	 *         the atomic lock
	 */
	State getAtomicLock(State state, int pid);

	/**
	 * Process pid enters a new atomic section. <br>
	 * Precondition: no other processes hold the atomic lock<br>
	 * If the process already holds the atomic lock, then its atomic count is
	 * incremented; <br>
	 * if the process doesn't hold the atomic lock, then the atomic lock is
	 * obtained and its atomic count is set to be 1.
	 * 
	 * @param state
	 * @param pid
	 * @return the new state after process pid enters a new atomic section
	 */
	State enterAtomic(State state, int pid);

	/**
	 * Process pid leaves an atomic section.<br>
	 * Precondition: in the given state, the process pid holds the atomic lock
	 * and its atomic count is greater than zero.<br>
	 * The atomic count is decremented by 1 after this method;<br>
	 * if the resultant atomic count is 0, then the atomic lock is released as
	 * well.
	 * 
	 * @param state
	 * @param pid
	 * @return the new state after process pid leaves an atomic section
	 */
	State leaveAtomic(State state, int pid);

	/**
	 * Releases the atomic lock, by updating the atomic lock variable with the
	 * undefined process value. If atomic lock of the given state is already
	 * released, this is a no op.
	 * 
	 * @param state
	 *            any non-null CIVL state
	 * @return a state equivalent to given one except that no state owns the
	 *         atomic lock
	 */
	State releaseAtomicLock(State state);

	/**
	 * <p>
	 * Updates the state by replacing the process state with the given one where
	 * the PID of the old process state is the same as the given process state.
	 * </p>
	 * <p>
	 * Precondition: the PID of the given process state should be in [0,
	 * numProcs-1].
	 * </p>
	 * 
	 * @param state
	 *            A non-null CIVL state
	 * @param processState
	 *            The process state to assign to PID
	 * @return The new state after updating the process with the specified PID
	 */
	State setProcessState(State state, ProcessState processState);

	/**
	 * Checks if one dyscope is strictly the descendant of the other (not equal
	 * to).
	 * 
	 * @param state
	 *            The current state.
	 * @param ancestor
	 *            The ID of the ancestor dyscope.
	 * @param descendant
	 *            The ID of the descendant dyscope.
	 * @return True iff ancestor dyscope is really an ancestor of the descendant
	 *         dyscope and they must not be equal to each other.
	 */
	boolean isDescendantOf(State state, int ancestor, int descendant);

	/**
	 * Computes the lowest common ancestor of two given dyscopes. The returned
	 * value is always a dyscope ID.
	 * 
	 * @param state
	 *            The current state.
	 * @param one
	 *            One dyscope.
	 * @param another
	 *            Another dynamic scope.
	 * @return The dyscope ID of the lowest common ancestor of the two given
	 *         dyscopes.
	 */
	int lowestCommonAncestor(State state, int one, int another);

	/**
	 * Allocates an object, of the given value, for the given malloc ID in the
	 * heap of the given dyscope. For handle objects that are allocated by
	 * system functions instead of malloc statement, they all have a
	 * corresponding fake malloc ID assigned by the model builder.
	 * 
	 * @param state
	 *            The pre-state.
	 * @param dyscopeID
	 *            The dyscope ID.
	 * @param mallocID
	 *            The ID the malloc statement.
	 * @param heapObject
	 *            The value of the new heap object.
	 * @return The new state after the new heap object
	 */
	Pair<State, SymbolicExpression> malloc(State state, int dyscopeID,
			int mallocID, SymbolicExpression heapObject);

	/**
	 * Allocates an object for the given malloc ID in the heap of the given
	 * dyscope. For handle objects that are allocated by system functions
	 * instead of malloc statement, they all have a corresponding fake malloc ID
	 * assigned by the model builder. Since no value of the heap object is
	 * provided, the method will create a symbolic constant representing the
	 * heap object.
	 * 
	 * @param state
	 *            The pre-state.
	 * @param pid
	 *            The PID of the process that triggers this execution.
	 * @param dyscopeID
	 *            The dyscope ID.
	 * @param mallocID
	 *            The ID the malloc statement.
	 * @param elementType
	 *            The symbolic type of the element to be contained in the new
	 *            heap object.
	 * @param elementCount
	 *            The number of elements contained by the new heap object.
	 * @return The new state after the new heap object is added.
	 */
	Pair<State, SymbolicExpression> malloc(State state, int pid, int dyscopeID,
			int mallocID, SymbolicType elementType,
			NumericExpression elementCount);

	/**
	 * Deallocates a heap object from the heap of a given dyscope. It marks the
	 * heap object as INVALID instead of removing it, updates any pointer to
	 * that removed object to be an UNDEFINED pointer, which is defined by the
	 * symbolic utility. The removal of the heap object happens later when the
	 * heap gets collected during state canonicalization.
	 * 
	 * @see malloc
	 * 
	 * @param state
	 *            The pre-state.
	 * @param heapObjectPointer
	 *            The pointer which points to the heap object to be removed.
	 * @param scopeOfPointer
	 *            The scope that is referred by the heapObjectPointer
	 * @param mallocId
	 *            The malloc ID of the heap object to be removed, i.e., the
	 *            index of the heap field in the heap.
	 * @param index
	 *            The index of the heap object in the heap field.
	 * @return A new state after the heap object is removed from the heap, and
	 *         corresponding pointers updated.
	 */
	State deallocate(State state, SymbolicExpression heapObjectPointer,
			SymbolicExpression scopeOfPointer, int mallocId, int index);

	/**
	 * returns the memory unit factory associated with this state factory, which
	 * contains utility functions for
	 * 
	 * @return the memory unit factory associated with this state factory.
	 */
	MemoryUnitFactory memUnitFactory();

	/**
	 * Returns the map of input variable and their value at the given state;
	 * empty map if there are no input variables.
	 * 
	 * @param state
	 *            the given state
	 * @return the map of input variable and their value at the given state
	 */
	Map<Variable, SymbolicExpression> inputVariableValueMap(State state);

	/**
	 * Creates a fresh symbolic constant of the given type at the given state.
	 * The name of the symbolic constant is formed by a sequence of alphabets
	 * (i.e., the prefix) followed by an integer (i.e., the unique id), like
	 * "X4", "Y5", "H10". The prefix is decided by the index and
	 * {@link ModelConfiguration#SYMBOL_PREFIXES}. This method has side effect
	 * on the state because it increases the count of the corresponding symbol.
	 * 
	 * Precondition: the index is greater than or equal to zero and is less than
	 * the length of {@link ModelConfiguration#SYMBOL_PREFIXES}.
	 * 
	 * @param state
	 *            the given state
	 * @param index
	 *            the index of the prefix to be used in the name of the symbolic
	 *            constant to be created
	 * @param type
	 *            the type of the symbolic constant to be created
	 * @return the new state and the new symbolic constant of the given type
	 *         with a unique name and has the prefix corresponding to the given
	 *         index
	 */
	Pair<State, SymbolicConstant> getFreshSymbol(State state, int index,
			SymbolicType type);

	/* ****************** Snapshots related method ****************** */
	/* Note: Snapshots are objects with type ImmutableMonoState */
	/**
	 * <p>
	 * Take a snapshot for the process pid on the given state, returns a
	 * symbolic expression representing the value of a $state type object. The
	 * value contains the snapshot---a new state which only contains that one
	 * process state and associated dyscopes.
	 * </p>
	 * 
	 * @param state
	 *            The state which will be taken snapshot.
	 * @param pid
	 *            The pid of the process for the snapshot
	 * @param topDyscope
	 *            A dyscope that is reachable from the call stack of the process
	 *            pid. It controls the top call stack frame of the process in
	 *            the returned state: the top call stack frame is the toppest
	 *            one that can reach topDyscope.
	 * @return a symbolic expression representing the value of a $state type
	 *         object. The value contains the snapshot---a new state which only
	 *         contains that one process state and associated dyscopes
	 */
	SymbolicExpression getStateSnapshot(State state, int pid, int topDyscope);

	/**
	 * <p>
	 * <b>Pre-conditions:</b>
	 * <ul>
	 * <li>A 'monoState' is a state consists of exact one process.</li>
	 * <li>The 'state' must have a number n of processes such that n greater
	 * than newPid.</li>
	 * <li>The call stack of the process newPid in 'state' is empty.</li>
	 * </ul>
	 * </p>
	 * 
	 * <p>
	 * <b>Summary:</b> This method combines a combining state which is a state
	 * during the combination (The combination of states may take several steps)
	 * and a monoState which is a state contains exact one process. It sets the
	 * only process in the monoState to the newPid process in the combining
	 * state.
	 * </p>
	 * 
	 * <p>
	 * <b>Invariants:</b>
	 * <ul>
	 * <li>Let s be the lexical function scope associates with the bottom call
	 * stack entry of the only process in the monoState, S be the set of lexical
	 * function scopes associates with the bottom call stack entries of all
	 * processes in the state. There must be a lexical scope s' that is the
	 * least common ancester of scopes in set S ^ {s} (S union with {s}).s' and
	 * its' ancestors can only have one dynamic scope in the combining state.
	 * One can proof that for a concurrency model among the total 'nprocs'
	 * processes, that there is no shared storage in any decesdant scope of s',
	 * the combination is sound.</li>
	 * <li>The number of processes in the combineing state is unchanged</li>
	 * </ul>
	 * </p>
	 * 
	 * @param stateValue
	 *            the object of {@link CIVLStateType} representing the state
	 *            that will be merged with a mono state
	 * @param monoStateValue
	 *            the object of {@link CIVLStateType} representing the mono
	 *            state that will be merged into a state.
	 * @param newPid
	 *            The new pid of the only process in monoState in the returned
	 *            state after combination.
	 * @return The new state which is obtained by combine combining state and
	 *         the monoState.
	 */
	SymbolicExpression addInternalProcess(SymbolicExpression stateValue,
			SymbolicExpression monoStateValue, int newPid, int nprocs,
			CIVLSource source);

	/**
	 * <p>
	 * Combines a process from the given real state to the given collate state
	 * as an external process.
	 * </p>
	 * 
	 * @param colState
	 *            the symbolic value of a $state object representing a collate
	 *            (merged) state
	 * @param currentState
	 *            The current (real) state
	 * @param pid
	 *            The PID of the calling process
	 * @param place
	 *            The place of the corresponding internal process
	 * @param with
	 *            The function translated from $with
	 * @return a symbolic value of a $state object representing the merged state
	 */
	SymbolicExpression addExternalProcess(SymbolicExpression colStateValue,
			State currentState, int pid, int place, CIVLFunction with);

	/**
	 * Creates an empty state which contains no dyscopes but an array of process
	 * states with length 'nprocs'. Each process state has an empty call stack.
	 * 
	 * @param nprocs
	 *            Number of process states in the created state.
	 * @return A new state which contains no dyscopes but an array of process
	 *         states with length 'nprocs'. Each process state has an empty call
	 *         stack. The path condtion is simply 'true'.
	 */
	State emptyState(int nprocs);

	/**
	 * Get a saved state by the state reference. The reference can be obtained
	 * from saving states with the methods {@link #saveState(State)},
	 * {@link #combineStates(int, State, int, int, CIVLSource)} or
	 * {@link #getStateSnapshot(State, int, int)}}.
	 * 
	 * @param stateReference
	 *            The reference of a saved state.
	 * @return The saved state
	 */
	State getStateByReference(int stateReference);

	/**
	 * Save a state, returns a int type reference to the saved state and the
	 * saved state itself.
	 * 
	 * @param state
	 *            The state will be saved in the stateFactory.
	 * @return A reference that can be used to get the state back with
	 *         {@link #getStateByReference(int)} and the saved state itself.
	 */
	Pair<Integer, State> saveState(State state);

	/* ****************** End of Snapshots related method ****************** */
	/**
	 * Records a collection of pointers to changed memory locations. The change
	 * was done by the given process.
	 * 
	 * @param state
	 *            The current state
	 * @param pid
	 *            The PID of the calling process who may change some memory
	 *            locations.
	 * @param memValue
	 *            a symbolic expression of
	 *            {@link CIVLMemType#getDynamicType(SymbolicUniverse)} which
	 *            includes references to objects
	 * @param isRead
	 *            true iff the given memory locations are added to read set;
	 *            false iff the given memory locations are added to write set.
	 * @return A state in which the given memory locations are recorded.
	 */
	State addReadWriteRecords(State state, int pid, SymbolicExpression memValue,
			boolean isRead);

	/**
	 * @param state
	 *            The current state
	 * @param pid
	 *            The PID of the calling process whose write set stack will be
	 *            peeked.
	 * @param isRead
	 *            true iff peek a read set; false iff peek a write set.
	 * @return the top frame in the write set stack associates to the given
	 *         process or Java null if the stack is empty.
	 */
	DynamicMemoryLocationSet peekReadWriteSet(State state, int pid,
			boolean isRead);

	/**
	 * @param state
	 *            The current state
	 * @param The
	 *            PID of the calling process whose write set stack will be
	 *            pushed.
	 * @param isRead
	 *            true iff push an empty read set; false iff push an empty write
	 *            set.
	 * @return A new state in which the process state of the given pid will be
	 *         updated. The write set stack of the process state has one more
	 *         empty stack.
	 */
	State pushEmptyReadWrite(State state, int pid, boolean isRead);

	/**
	 * @param state
	 *            The current state
	 * @param The
	 *            PID of the calling process whose write set stack will be
	 *            popped.
	 * @param isRead
	 *            true iff pop a read set; false iff pop a write set.
	 * @return A new state in which the process state of the given pid will be
	 *         updated. The write set stack of the process state has been popped
	 */
	State popReadWriteSet(State state, int pid, boolean isRead);

	/**
	 * @param state
	 *            The current state
	 * @param The
	 *            PID of the calling process whose partial path condition stack
	 *            will be pushed.
	 * @return A new state in which the process state of the given pid will be
	 *         updated. The partial path condition stack of the process state
	 *         has one more empty stack.
	 */
	State pushAssumption(State state, int pid, BooleanExpression assumption);

	/**
	 * @param state
	 *            The current state
	 * @param The
	 *            PID of the calling process whose partial path condition stack
	 *            will be popped.
	 * @return A new state in which the process state of the given pid will be
	 *         updated. The partial path condition stack of the process state
	 *         has been popped.
	 */
	State popAssumption(State state, int pid);

	/**
	 * Converts an integer dynamic scope id into a symbolic expression
	 * 
	 * @param sid
	 *            The scope id to be translated
	 * @return The symbolic expression representing the scope id
	 */
	SymbolicExpression scopeValue(int sid);

	/**
	 * Converts a value of scope type to a dynamic scope ID.
	 * 
	 * @param scopeValue
	 *            a value of scope type
	 * @return The dynamic scope ID which is associated with the given scope
	 *         type value.
	 */
	int getDyscopeId(SymbolicExpression scopeValue);

	/**
	 * @return constant value of scope type which represents an undefined scope.
	 */
	SymbolicExpression undefinedScopeValue();

	/**
	 * @return constant value of scope type which represents an collected scope.
	 */
	SymbolicExpression nullScopeValue();

	/**
	 * 
	 * @param sid
	 *            a dynamic scope ID
	 * @return true iff the given dynamic scope ID stands for an undefined scope
	 */
	boolean isScopeIdDefined(int sid);

	/**
	 * @return a reference to {@link StateValueHelper}
	 */
	StateValueHelper stateValueHelper();

	void setConfiguration(CIVLConfiguration config);

	public SymbolicExpression processValue(int pid);

	void setSymbolicUtility(SymbolicUtility symbolicUtility);
}
