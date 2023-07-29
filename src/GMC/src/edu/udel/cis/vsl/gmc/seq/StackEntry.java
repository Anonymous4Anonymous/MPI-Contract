package edu.udel.cis.vsl.gmc.seq;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * The stack entry that is going to be pushed onto the stack during the search.
 * The stack entry also serves as the iterator of the ample set or ample set
 * complement of the source {@code state}.
 * 
 * @author Yihao Yan (xxxxxxxx)
 *
 */
public class StackEntry<STATE, TRANSITION> implements Iterator<TRANSITION> {
	/**
	 * The search node that wraps the source state with its search information
	 * like stack position or fullyExpanded flag.
	 */
	private SequentialNode<STATE> node;

	/**
	 * This collection could be the ample set or ample set complement of the
	 * source state.
	 */
	private Collection<TRANSITION> transitions;

	/**
	 * The iterator to iterate either the ample set or the ample set complement
	 * of the {@link #sourceState}. This iterator will iterate over all the
	 * transitions after {@link #current} transition.
	 */
	private Iterator<TRANSITION> transitionIterator;

	/**
	 * The index of the current transition. This is used to write the trace file
	 * which will be used later for replay.
	 */
	private int tid = -1;

	/**
	 * The current transition.
	 */
	private TRANSITION current = null;

	/**
	 * If a successor is on stack, then it will have an index on the stack. This
	 * variable will store the minimum value among all the successors.
	 */
	private int minimumSuccessorStackIndex = Integer.MAX_VALUE;

	/**
	 * If some transition from this stack entry is a potential "preemption",
	 * then this variable is the ID number of the process that executed the
	 * previous transition (and is being preempted). If no transition from this
	 * stack entry is a potential preemption, this variable is -1.
	 * 
	 * This entry is preemptible (preemptionPid >=0), when the set of outgoing
	 * transitions contains a transition from the previous process and at least
	 * one transition from another process. A preemption occurs when the current
	 * transition of this step is from another process. Note that if the set of
	 * outgoing transitions does not include one from the previous process, then
	 * there is no preemption.
	 */
	private int preemptionPid = -1;

	/**
	 * @param node
	 *                        The node that wraps the source state.
	 * @param transitions
	 *                        The ample set or ample set complement of the
	 *                        source state.
	 * @param offset
	 *                        the ID number that should be associated to the
	 *                        first transition in the sequence.
	 */
	public StackEntry(SequentialNode<STATE> node,
			Collection<TRANSITION> transitions, int offset) {
		this.node = node;
		this.transitions = transitions;
		this.transitionIterator = transitions.iterator();
		if (transitionIterator.hasNext()) {
			this.current = transitionIterator.next();
			tid = offset;
		}
	}

	/**
	 * @return the current transition but not move the
	 *         {@link #transitionIterator}.
	 */
	public TRANSITION peek() {
		if (current == null)
			throw new NoSuchElementException();
		return current;
	}

	/**
	 * @return the current transition and also move the
	 *         {@link #transitionIterator}.
	 */
	@Override
	public TRANSITION next() {
		TRANSITION result = current;

		if (result != null) {
			if (transitionIterator.hasNext()) {
				current = transitionIterator.next();
				tid++;
			} else
				current = null;
		} else
			throw new NoSuchElementException();
		return result;
	}

	public int getTid() {
		return tid;
	}

	public SequentialNode<STATE> getNode() {
		return node;
	}

	public Iterator<TRANSITION> getTransitionIterator() {
		return transitionIterator;
	}

	public STATE source() {
		return node.getState();
	}

	@Override
	public boolean hasNext() {
		return current != null;
	}

	public STATE getState() {
		return node.getState();
	}

	public int getMinimumSuccessorStackIndex() {
		return minimumSuccessorStackIndex;
	}

	public void setMinimumSuccessorStackIndex(int minimumSuccessorStackIndex) {
		this.minimumSuccessorStackIndex = minimumSuccessorStackIndex;
	}

	public Collection<TRANSITION> getTransitions() {
		return transitions;
	}

	/**
	 * Is this step preemptible, i.e., does the set of outgoing transitions
	 * include at least one transition from the previous process as well as at
	 * least one transition from another process?
	 * 
	 * @return <code>true</code> iff this step is preemptible
	 */
	public boolean isPreemptible() {
		return preemptionPid >= 0;
	}

	/**
	 * If this step is preemptible, this method returns the PID of the process
	 * that executed the previous transition. Otherwise the value returned is
	 * -1.
	 * 
	 * @return previous process PID, if this step is preemptible, else -1
	 */
	public int getPreemptionPid() {
		return preemptionPid;
	}

	/**
	 * Sets this entry to be preemptible, with the given <code>pid</code> to be
	 * the PID of the previous process.
	 */
	public void setPreemptionPid(int pid) {
		this.preemptionPid = pid;
	}

}
