package edu.udel.cis.vsl.civl.model.IF.type;

import java.util.List;
import java.util.function.Function;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * <p>
 * A {@link CIVLType} representing a set of memory locations.
 * </p>
 * @author xxxx
 */
public interface CIVLMemType extends CIVLSetType {

	/**
	 * Implementations of {@link MemoryLocationReference} represent references
	 * to a subset of a value of a variable/heap object
	 * 
	 * @author xxxx
	 *
	 */
	public interface MemoryLocationReference {
		/**
		 * @return true iff this class represents references to a heap object
		 */
		boolean isHeapObject();

		/**
		 * The variable ID of the corresponding variable
		 */
		int vid();

		/**
		 * <p>
		 * significant iff {@link #isHeapObject()}
		 * </p>
		 * 
		 * @return the ID of a lexical malloc statement which creates the heap
		 *         object
		 */
		int heapID();

		/**
		 * <p>
		 * significant iff {@link #isHeapObject()}
		 * </p>
		 * 
		 * @return the ID of a run-time call to a lexically specific malloc statement which creates
		 *         the heap object
		 */
		int mallocID();

		/**
		 * The scope value of the scope where the corresponding variable
		 * declared
		 */
		SymbolicExpression scopeValue();

		/**
		 * A {@link SymbolicUniverse#valueSetTemplate(SymbolicType,
		 * ValueSetReference[])} that provides references to sub-values of the
		 * variable
		 */
		SymbolicExpression valueSetTemplate();
	}

	/**
	 * @return a {@link Function} that maps dynamic $mem type values to a set of
	 *         {@link MemoryLocationReference}.
	 */
	Function<SymbolicExpression, Iterable<MemoryLocationReference>> memValueIterator();

	/**
	 * @param u
	 *            a reference to {@link SymbolicUniverse}
	 * @return a {@link Function} that maps a set of symbolic expression arrays,
	 *         to a value of dynamic $mem type. Each symbolic expression array
	 *         must have five elements, each of which represents: 1. variable
	 *         ID, 2. heap ID, 3. malloc ID, 4. scopeValue and 5. value set
	 *         template respectively. Such five elements will form a reference
	 *         to a subset of a variable/heap object.
	 * 
	 *         <p>
	 *         heap ID is the ID of a lexical malloc statement which creates the
	 *         heap object; malloc ID is the ID of a run-time call to a malloc
	 *         statement which creates the heap object. When the symbolic
	 *         expression array represents a reference to a subset of a variable
	 *         value (not heap object), heapID and mallocID are not significant.
	 *         </p>
	 */
	Function<List<SymbolicExpression[]>, SymbolicExpression> memValueCreator(
			SymbolicUniverse u);

	/**
	 * Returns a {@link UnaryOperator} to $mem values that will clean up
	 * references in the $mem value. The references in the $mem value that refer
	 * to the variable/heap objects, that are no longer alive, will be
	 * collected.
	 * 
	 * @param u
	 *            a reference to {@link SymbolicUniverse}
	 * @param collectedScopeValue
	 *            the unique scope value representing a collected scope. Once a
	 *            scope is collected, its scope value must equal to this
	 *            parameter.
	 * @return a {@link UnaryOperator} to $mem values that will clean up
	 *         references in the $mem value.
	 */
	UnaryOperator<SymbolicExpression> memValueCollector(SymbolicUniverse u,
			SymbolicExpression collectedScopeValue);
}
