package edu.udel.cis.vsl.civl.dynamic.IF;

import edu.udel.cis.vsl.civl.library.civlc.LibcivlcExecutor;
import edu.udel.cis.vsl.civl.library.mpi.LibmpiExecutor;
import edu.udel.cis.vsl.civl.library.time.LibtimeExecutor;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLMemType;
import edu.udel.cis.vsl.civl.semantics.common.CommonExecutor;
import edu.udel.cis.vsl.civl.state.common.immutable.ImmutableStateFactory;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * <p>
 * This class is a immutable data structure that stores a set of memory
 * locations. See also {@link DynamicMemoryLocationSetFactory}
 * </p>
 * 
 * <p>
 * <b>Relation to the symbolic value of {@link CIVLMemType}:</b> 1. an instance
 * of this class can be converted to a symbolic value of mem type by calling
 * {@link #getMemValue()}. 2. a symbolic value of mem type can be union-ed with
 * an instance of this class with the method
 * {@link DynamicMemoryLocationSetFactory#addReference(DynamicMemoryLocationSet, SymbolicExpression)}.
 * </p>
 * 
 * <p>
 * <b>Instances of this class are used to dynamically keep track of read/write
 * sets.</b> Here we just take a note for where in the CIVL code base,
 * write/read operation will be recorded:
 * 
 * Where write sets are collected:
 * <ul>
 * <li>The private methods in {@link CommonExecutor}, there are two of them:
 * assignCore and assignLHS</li>
 * <li>
 * {@link ImmutableStateFactory#deallocate(edu.udel.cis.vsl.civl.state.IF.State, SymbolicExpression, int, int, int)}
 * </li>
 * <li>executeMalloc and malloc in {@link CommonExecutor}</li> <br>
 * TODO: can all the following go through the executor's assign() ?
 * <li>{@link LibmpiExecutor#executeNewGcomm} (TODO: is this needed to be
 * recorded ?)</li>
 * <li>{@link LibtimeExecutor#executeLocalTime}</li>
 * <li>{@link CommonExecutor#executeNextInDomain} (TODO: this one needs some
 * special non-concretet handling)</li>
 * <li>{@link LibcivlcExecutor#executeNextTimeCount}</li>
 * </ul>
 * 
 * Where read sets are collected: at {@link CommonExecutor#executeStatement}
 * method; Note also, read sets always contains write sets.
 * </p>
 * 
 * @author xxxx (Ziqing Luo)
 */
public interface DynamicMemoryLocationSet {

	/**
	 * @return a symbolic expression of
	 *         {@link CIVLMemType#getDynamicType(edu.udel.cis.vsl.sarl.IF.SymbolicUniverse)}
	 *         which contains all the references to objects that are stored in
	 *         this write set.
	 */
	SymbolicExpression getMemValue();

	/**
	 * <p>
	 * Apply an {@link UnaryOperator} on the set of memory location references.
	 * If the operator changes nothing, return this instance.
	 * </p>
	 * 
	 * @param operator
	 * @return An instance whose references are obtained by applying the
	 *         operator on ones of this
	 */
	public DynamicMemoryLocationSet apply(
			UnaryOperator<SymbolicExpression> operator);
}
