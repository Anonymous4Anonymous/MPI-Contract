package edu.udel.cis.vsl.civl.model.IF.contract;

import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.expression.Expression;

/**
 * This represents a <code>\read</code> or <code>\write</code> event of a
 * <code>depends</code> clause.
 * 
 * @author Manchun Zheng
 *
 */
public interface MemoryEvent extends DependsEvent {

	// /**
	// * The types of memory event
	// *
	// * @author Manchun
	// *
	// */
	// public enum MemoryEventKind {
	// /**
	// * a <code>\read</code> event
	// */
	// READ,
	// /**
	// * a <code>\write</code> event
	// */
	// WRITE,
	// /**
	// * a <code>\reach</code> event
	// */
	// REACH
	// }
	//
	// /**
	// * Returns the kind of this memory event. See also {@link
	// MemoryEventKind}.
	// *
	// * @return
	// */
	// MemoryEventKind memoryEventKind();

	/**
	 * Returns the memory units associated with this event.
	 * 
	 * @return
	 */
	Set<Expression> memoryUnits();

	/**
	 * Returns the number of memory units associated with this event.
	 * 
	 * @return
	 */
	int numMemoryUnits();

	/**
	 * Is this a <code>\read</code> event?
	 * 
	 * @return
	 */
	boolean isRead();

	/**
	 * Is this a <code>\write</code> event?
	 * 
	 * @return
	 */
	boolean isWrite();

	/**
	 * Is this a <code>\reach</code> event?
	 * 
	 * @return
	 */
	boolean isReach();
}
