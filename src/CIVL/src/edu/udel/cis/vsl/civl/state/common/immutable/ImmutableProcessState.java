/**
 * 
 */
package edu.udel.cis.vsl.civl.state.common.immutable;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import edu.udel.cis.vsl.civl.dynamic.IF.DynamicMemoryLocationSet;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.state.IF.ProcessState;
import edu.udel.cis.vsl.civl.state.IF.StackEntry;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * An ImmutableProcessState represents the state of a single process in a CIVL
 * model. It is one component of a CIVL model state.
 * 
 * A immutable process state is composed of a nonnegative integer PID, an
 * "atomic count" and a call stack. The atomic count records the current atomic
 * depth of the process: how many atomic blocks it has entered and not exited.
 * The call stack is a sequence of activation frames (aka "stack entries"). Each
 * frame in a pair specifying a dyscope ID and a location in the static scope
 * corresponding to that dyscope.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * @author Timothy J. McClory (tmcclory)
 * @author Stephen F. Siegel (xxxx)
 * 
 */
public class ImmutableProcessState implements ProcessState {

	/**
	 * An iterator that iterates over the elements of an array in reverse order
	 * (i.e., starting with highest-index and moving down to 0).
	 * 
	 * @author xxxx
	 * 
	 */
	class ReverseIterator implements Iterator<StackEntry> {

		/* ************************ Instance Fields ************************ */

		/**
		 * The array over which we are iterating.
		 */
		private StackEntry[] array;

		/**
		 * The index of the next element that will be returned by the next call
		 * to method {@link #next()}.
		 */
		private int i;

		/* ******************** Package-private Methods ******************** */

		/**
		 * Creates a new reverse iterator for the given array.
		 * 
		 * @param array
		 *            array over which to iterate
		 */
		ReverseIterator(StackEntry[] array) {
			this.array = array;
			i = array.length - 1;
		}

		/* ********************* Methods from Iterator ********************* */

		@Override
		public boolean hasNext() {
			return i >= 0;
		}

		@Override
		public StackEntry next() {
			StackEntry result = array[i];

			i--;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	/* ************************** Instance Fields ************************** */

	/**
	 * Is this instance the unique representative of its equivalence class?
	 */
	private boolean canonic = false;

	/**
	 * If the hash code of this object has been computed, it is cached here.
	 */
	private int hashCode = -1;

	/**
	 * Has the hash code of this object been computed?
	 */
	private boolean hashed = false;

	private boolean selfDestructable = false;

	/**
	 * The PID.
	 */
	private int pid;

	/**
	 * Number of atomic blocks that have been entered and not exited. This is
	 * incremented when entering an atomic block, and decremented when leaving
	 * it.
	 */
	private int atomicCount;

	/**
	 * The call stack of this process: a non-null array in which entry 0 is the
	 * TOP of the stack.
	 */
	private StackEntry[] callStack;

	// /**
	// * This identifier is not part of the state. It is never renamed, helping
	// to
	// * identify a specific process when processes get collected.
	// */
	// private int identifier;

	private Map<SymbolicExpression, Boolean> reachableMemoryUnitsWoPointer;
	// private Set<SymbolicExpression> reachableMemoryUnits;

	private Map<SymbolicExpression, Boolean> reachableMemoryUnitsWtPointer;

	private BooleanExpression[] partialPathConditions = null;

	private DynamicMemoryLocationSet[] writeSets = null;

	private DynamicMemoryLocationSet[] readSets = null;

	/* **************************** Constructors *************************** */

	/**
	 * Constructs new process state from given fields. No information is cloned;
	 * the given objects just become the fields.
	 * 
	 * @param pid
	 *            the process ID
	 * @param stack
	 *            the call stack
	 * @param ppcs
	 *            a stack of partial path conditions
	 * @param writeSets
	 *            a stack of write sets
	 * @param readSets
	 *            a stack of read sets
	 * @param atomicCount
	 *            the atomic count
	 * @param selfDestructable
	 *            The flag indicates weather the process is self-destructable,
	 *            see {@link #isSelfDestructable()}
	 */
	ImmutableProcessState(int pid, StackEntry[] stack, BooleanExpression[] ppcs,
			DynamicMemoryLocationSet[] writeSets,
			DynamicMemoryLocationSet[] readSets, int atomicCount,
			boolean selfDestructable) {
		this.pid = pid;
		this.callStack = stack;
		this.partialPathConditions = ppcs;
		this.writeSets = writeSets;
		this.readSets = readSets;
		this.atomicCount = atomicCount;
		this.selfDestructable = selfDestructable;
	}

	/**
	 * Constructs a new process state with empty stack and atomic count 0.
	 * 
	 * @param pid
	 *            The process ID
	 * @param identifier
	 *            The identifier of the process, which is not part of the state.
	 * @param selfDestructable
	 *            The flag indicates weather the process is self-destructable,
	 *            see {@link #isSelfDestructable()}
	 */
	ImmutableProcessState(int pid, boolean selfDestructable) {
		this(pid, new ImmutableStackEntry[0], null, null, null, 0,
				selfDestructable);
	}

	/* ********************** Package-private Methods ********************** */
	/**
	 * Makes this instance the unique representative of its equivalence class.
	 * 
	 * Nothing to do except set canonic flag to true, since the components of
	 * this class do not contain anything that can be made canonic: locations,
	 * dynamic scope IDs, ints.
	 */
	void makeCanonic() {
		canonic = true;
	}

	/**
	 * Removes top entry from call stack. More precisely, returns a new process
	 * state equivalent to this one but with the top entry removed from the call
	 * stack.
	 * 
	 * Behavior is undefined if call stack is empty.
	 * 
	 * @return new process state will top frame removed from stack
	 */
	ImmutableProcessState pop() {
		ImmutableStackEntry[] newStack = new ImmutableStackEntry[callStack.length
				- 1];

		System.arraycopy(callStack, 1, newStack, 0, callStack.length - 1);
		return new ImmutableProcessState(pid, newStack, partialPathConditions,
				writeSets, readSets, this.atomicCount, selfDestructable);
	}

	/**
	 * Pushes given frame onto call stack. More precisely, returns a new process
	 * state equivalent to this one, but with new frame pushed onto top of
	 * stack.
	 * 
	 * @param newStackEntry
	 *            the new stack entry
	 * @return new process state obtained by pushing entry onto stack
	 */
	ImmutableProcessState push(ImmutableStackEntry newStackEntry) {
		ImmutableStackEntry[] newStack = new ImmutableStackEntry[callStack.length
				+ 1];

		System.arraycopy(callStack, 0, newStack, 1, callStack.length);
		newStack[0] = newStackEntry;
		return new ImmutableProcessState(pid, newStack, partialPathConditions,
				writeSets, readSets, this.atomicCount, selfDestructable);
	}

	/**
	 * Replaces the top entry on this process state's call stack with the given
	 * entry. Functionally equivalent to doing a pop, then a push, but this
	 * version may be more efficient.
	 * 
	 * Behavior is undefined if stack is empty.
	 * 
	 * @param newStackEntry
	 *            the new stack entry
	 * @return new process state obtained by replacing top entry on call stack
	 *         with given one
	 */
	ImmutableProcessState replaceTop(ImmutableStackEntry newStackEntry) {
		int length = callStack.length;
		ImmutableStackEntry[] newStack = new ImmutableStackEntry[length];

		System.arraycopy(callStack, 1, newStack, 1, length - 1);
		newStack[0] = newStackEntry;
		return new ImmutableProcessState(pid, newStack, partialPathConditions,
				writeSets, readSets, this.atomicCount, selfDestructable);
	}

	/**
	 * Returns i-th entry on stack, where 0 is the TOP of the stack, and
	 * stackSize-1 is the BOTTOM of the stack.
	 * 
	 * @param i
	 *            int in [0,stackSize-1]
	 * @return i-th entry on stack
	 */
	StackEntry getStackEntry(int i) {
		return callStack[i];
	}

	/**
	 * Is this object the unique representative of its equivalence class?
	 * 
	 * @return true iff this is canonic
	 */
	boolean isCanonic() {
		return canonic;
	}

	/**
	 * Updates the PID.
	 * 
	 * @param pid
	 *            The new process ID.
	 * @return A new instance of process state with only the PID being changed.
	 */
	ImmutableProcessState setPid(int pid) {
		return new ImmutableProcessState(pid, callStack, partialPathConditions,
				writeSets, readSets, this.atomicCount, selfDestructable);
	}

	/**
	 * Updates the call stack using a given array of stack entries.
	 * 
	 * @param frames
	 *            The new call stack to be used.
	 * @return A new instance of process state with only the call stack being
	 *         changed.
	 */
	ProcessState setStackEntries(StackEntry[] frames) {
		return new ImmutableProcessState(pid, frames, partialPathConditions,
				writeSets, readSets, this.atomicCount, selfDestructable);
	}

	/**
	 * Updates a certain entry of the call stack.
	 * 
	 * @param index
	 *            The index of the stack entry to be updated.
	 * @param frame
	 *            The new stack entry to be used.
	 * @return A new instance of process state with only the stack entry of the
	 *         given index being changed.
	 */
	ProcessState setStackEntry(int index, StackEntry frame) {
		int n = callStack.length;
		StackEntry[] newStack = new StackEntry[n];

		System.arraycopy(callStack, 0, newStack, 0, n);
		newStack[index] = frame;
		return new ImmutableProcessState(pid, newStack, partialPathConditions,
				writeSets, readSets, this.atomicCount, selfDestructable);
	}

	/**
	 * Updates the call stack entries by substituting new values for dyscope IDs
	 * according to the given map.
	 * 
	 * @param oldToNew
	 *            an array which maps old dyscope IDs to their new values, i.e.,
	 *            <code>oldToNew[oldId] = newId</code>.
	 * @param substituter
	 *            a {@link UnaryOperator} that updates dyscope values in
	 *            symbolic expressions
	 * @return an ImmutableProcessState which is equivalent to this one except
	 *         that the dyscopeIDs in the call stack entries have been replaced
	 *         with new values according to the given map
	 */
	ImmutableProcessState updateDyscopes(int[] oldToNew,
			UnaryOperator<SymbolicExpression> substituter) {
		int stackSize = callStack.length;
		StackEntry[] newStack = new StackEntry[stackSize];
		boolean change = false;

		for (int j = 0; j < stackSize; j++) {
			StackEntry oldFrame = callStack[j];
			int oldScope = oldFrame.scope();
			int newScope = oldToNew[oldScope];

			if (oldScope == newScope) {
				newStack[j] = oldFrame;
			} else {
				change = true;
				newStack[j] = new ImmutableStackEntry(oldFrame.location(),
						newScope);
			}
		}

		ImmutableProcessState newProcState = apply(substituter);

		if (change)
			return (ImmutableProcessState) newProcState
					.setStackEntries(newStack);
		else
			return newProcState;
	}

	/**
	 * Set the partial path condition array to the given one.
	 * 
	 * @param newPpcs
	 *            An array of partial path conditions
	 * @return A new instance whose partial path condition array field has been
	 *         updated.
	 */
	ImmutableProcessState setPartialPathConditions(
			BooleanExpression[] newPpcs) {
		return new ImmutableProcessState(pid, callStack, newPpcs, writeSets,
				readSets, atomicCount, selfDestructable);
	}

	/**
	 * @return The reference to the partial path condition array.
	 */
	BooleanExpression[] getPartialPathConditions() {
		if (partialPathConditions == null)
			return new BooleanExpression[0];
		else
			return partialPathConditions;
	}

	/**
	 * Set the write set stack of this process state to the given
	 * "newWriteSets".
	 * 
	 * @param newWriteSets
	 *            An array of write sets
	 * @return A new instance whose write set array field has been updated.
	 */
	ImmutableProcessState setWriteSets(
			DynamicMemoryLocationSet[] newWriteSets) {
		return new ImmutableProcessState(pid, callStack, partialPathConditions,
				newWriteSets, readSets, atomicCount, selfDestructable);
	}

	/**
	 * @param getCopy
	 *            true iff the returned array of
	 *            {@link DynamicMemoryLocationSet}s are copied. If copied, any
	 *            modification to the returned array will not affect this
	 *            process state.
	 * @return The reference to the {@link DynamicMemoryLocationSet} array,
	 *         which represents the write set.
	 */
	DynamicMemoryLocationSet[] getWriteSets(boolean getCopy) {
		if (writeSets == null)
			return new DynamicMemoryLocationSet[0];
		else if (getCopy)
			return Arrays.copyOf(writeSets, writeSets.length);
		else
			return writeSets;
	}

	/**
	 * Set the read set stack of this process state to the given "newReadSets".
	 * 
	 * @param newReadSets
	 *            An array of read sets
	 * @return A new instance whose read set array field has been updated.
	 */
	ImmutableProcessState setReadSets(DynamicMemoryLocationSet[] newReadSets) {
		return new ImmutableProcessState(pid, callStack, partialPathConditions,
				writeSets, newReadSets, atomicCount, selfDestructable);
	}

	/**
	 * @param getCopy
	 *            true iff the returned array of
	 *            {@link DynamicMemoryLocationSet}s are copied. If copied, any
	 *            modification to the returned array will not affect this
	 *            process state.
	 * @return The reference to the {@link DynamicMemoryLocationSet} array,
	 *         which represents the read set.
	 */
	DynamicMemoryLocationSet[] getReadSets(boolean getCopy) {
		if (readSets == null)
			return new DynamicMemoryLocationSet[0];
		else if (getCopy)
			return Arrays.copyOf(readSets, readSets.length);
		else
			return readSets;
	}

	/**
	 * <p>
	 * Applies a {@link UnaryOperator<SymbolicExpression>} to all symbolic
	 * expressions in this process state. Returns a new process state where
	 * symbolic expressions have been updated.
	 * </p>
	 * 
	 * @param operator
	 *            a unary operator to symbolic expressions
	 * @return a new process state where symbolic expressions have been updated.
	 */
	ImmutableProcessState apply(UnaryOperator<SymbolicExpression> operator) {
		boolean anyChange = false, change = false;
		BooleanExpression ppcNew[] = null;
		DynamicMemoryLocationSet writeSetsNew[] = null, readSetsNew[] = null;

		if (partialPathConditions != null) {
			ppcNew = Arrays.copyOf(partialPathConditions,
					partialPathConditions.length);
			for (int i = 0; i < partialPathConditions.length; i++) {
				BooleanExpression tmp = (BooleanExpression) operator
						.apply(partialPathConditions[i]);

				if (tmp != partialPathConditions[i]) {
					ppcNew[i] = tmp;
					change = true;
				}
			}
		}
		if (!change)
			ppcNew = partialPathConditions;
		anyChange |= change;
		change = false;
		if (writeSets != null) {
			writeSetsNew = Arrays.copyOf(writeSets, writeSets.length);
			for (int i = 0; i < writeSets.length; i++) {
				DynamicMemoryLocationSet tmp = writeSets[i].apply(operator);

				if (tmp != writeSets[i]) {
					writeSetsNew[i] = tmp;
					change = true;
				}
			}
		}
		if (!change)
			writeSetsNew = writeSets;
		anyChange |= change;
		change = false;
		if (readSets != null) {
			readSetsNew = Arrays.copyOf(readSets, readSets.length);
			for (int i = 0; i < readSets.length; i++) {
				DynamicMemoryLocationSet tmp = readSets[i].apply(operator);

				if (tmp != readSets[i]) {
					readSetsNew[i] = tmp;
					change = true;
				}
			}
		}
		if (!change)
			readSetsNew = readSets;
		anyChange |= change;
		if (anyChange)
			return new ImmutableProcessState(pid, callStack, ppcNew,
					writeSetsNew, readSetsNew, atomicCount, selfDestructable);
		return this;
	}

	/* ********************* Methods from ProcessState ********************* */

	@Override
	public int atomicCount() {
		return this.atomicCount;
	}

	@Override
	public Iterator<StackEntry> bottomToTopIterator() {
		return new ReverseIterator(callStack);
	}

	@Override
	public ProcessState decrementAtomicCount() {
		return new ImmutableProcessState(this.pid, this.callStack,
				partialPathConditions, writeSets, readSets,
				this.atomicCount - 1, selfDestructable);
	}

	@Override
	public int getDyscopeId() {
		if (callStack.length == 0)
			return -1;
		return callStack[0].scope();
	}

	@Override
	public Location getLocation() {
		if (callStack.length == 0)
			return null;
		return callStack[0].location();
	}

	@Override
	public int getPid() {
		return pid;
	}

	@Override
	public Iterable<StackEntry> getStackEntries() {
		return Arrays.asList(callStack);
	}

	@Override
	public boolean hasEmptyStack() {
		return callStack.length == 0;
	}

	@Override
	public boolean inAtomic() {
		return this.atomicCount > 0;
	}

	@Override
	public ProcessState incrementAtomicCount() {
		return new ImmutableProcessState(this.pid, this.callStack,
				partialPathConditions, writeSets, readSets,
				this.atomicCount + 1, selfDestructable);
	}

	/**
	 * {@inheritDoc} Look at the first entry on the call stack, but do not
	 * remove it.
	 * 
	 * @throws ArrayIndexOutOfBoundsException
	 *             if stack is empty
	 */
	@Override
	public StackEntry peekStack() {
		return callStack[0];
	}

	@Override
	public void print(PrintStream out, String prefix) {
		out.print(this.toStringBuffer(prefix));
		out.flush();
	}

	@Override
	public int stackSize() {
		return callStack.length;
	}

	@Override
	public StringBuffer toStringBuffer(String prefix) {
		StringBuffer result = new StringBuffer();

		result.append(prefix + "process " + pid + "\n");
		if (atomicCount != 0)
			result.append(prefix + "| atomicCount=" + atomicCount + "\n");
		result.append(prefix + "| call stack\n");
		for (int i = 0; i < callStack.length; i++) {
			StackEntry frame = callStack[i];

			result.append(prefix + "| | " + frame);
			result.append("\n");
		}
		return result;
	}

	@Override
	public StringBuffer toSBrieftringBuffer() {
		StringBuffer result = new StringBuffer();

		result.append("process " + pid + ":\n");
		if (callStack.length < 1)
			result.append("  terminated");
		else
			for (int i = 0; i < callStack.length; i++) {
				StackEntry frame = callStack[i];
				Location location = frame.location();
				CIVLSource source = location.getSource();
				String locationString = source == null
						? ""
						: " at " + source.getSummary(false);
				String frameString = (location.function() == null)
						? "null "
						: location.function().name() + locationString;

				if (i != 0)
					result.append(" called from\n");
				result.append("  ");
				result.append(frameString);
			}
		result.append("\n");
		return result;
	}

	@Override
	public String name() {
		return "p" + this.pid;
	}

	public Map<SymbolicExpression, Boolean> getReachableMemUnitsWoPointer() {
		return this.reachableMemoryUnitsWoPointer;
	}

	public Map<SymbolicExpression, Boolean> getReachableMemUnitsWtPointer() {
		return this.reachableMemoryUnitsWtPointer;
	}

	/* ************************ Methods from Object ************************ */

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof ImmutableProcessState) {
			ImmutableProcessState that = (ImmutableProcessState) obj;

			if (canonic && that.canonic)
				return false;
			if (hashed && that.hashed && hashCode != that.hashCode)
				return false;
			if (partialPathConditions != null && !Arrays
					.equals(partialPathConditions, that.partialPathConditions))
				return false;
			if (writeSets != null && !Arrays.equals(writeSets, that.writeSets))
				return false;
			if (readSets != null && !Arrays.equals(readSets, that.readSets))
				return false;
			if (!Arrays.equals(callStack, that.callStack))
				return false;
			if (pid != that.pid)
				return false;
			if (this.atomicCount != that.atomicCount)
				return false;
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (!hashed) {
			hashCode = Arrays.hashCode(callStack)
					^ (48729 * (pid ^ (31 * this.atomicCount)));
			if (partialPathConditions != null)
				hashCode ^= Arrays.hashCode(partialPathConditions);
			if (writeSets != null)
				hashCode ^= Arrays.hashCode(writeSets);
			if (readSets != null)
				hashCode ^= Arrays.hashCode(readSets);
			hashed = true;
		}
		return hashCode;
	}

	@Override
	public String toString() {
		return "State of process " + pid + " (call stack length = "
				+ callStack.length + ")";
	}

	@Override
	public StackEntry peekSecondLastStack() {
		if (callStack != null && callStack.length >= 2)
			return callStack[1];
		return null;
	}

	@Override
	public boolean isSelfDestructable() {
		return selfDestructable;
	}

}
