package edu.udel.cis.vsl.civl.dynamic.IF;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLMemType;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public interface DynamicMemoryLocationSetFactory {

	/**
	 * @return an empty {@link DynamicMemoryLocationSet}
	 */
	DynamicMemoryLocationSet empty();

	/**
	 * Add references in a "memValue" into the given "writeSet", returns a new
	 * write set
	 * 
	 * @param writeSet
	 *            a DynamicWriteSet
	 * @param memValue
	 *            a symbolic expression of
	 *            {@link CIVLMemType#getDynamicType(edu.udel.cis.vsl.sarl.IF.SymbolicUniverse)}
	 *            type
	 * @return
	 */
	DynamicMemoryLocationSet addReference(DynamicMemoryLocationSet writeSet,
			SymbolicExpression memValue);
}
