package edu.udel.cis.vsl.civl.semantics.IF;

import edu.udel.cis.vsl.civl.semantics.IF.ArrayToolBox.ArrayShape;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * <p>
 * This interface provides methods for dealing with array slices, e.g. carving
 * an array slice out of an array or writing an ArraySlice to an array.
 * </p>
 * <p>
 * An array slice is a sequence of elements in an array.
 * </p>
 * 
 * @author xxxx
 */
public interface ArrayCutter {
	/**
	 * <p>
	 * Return an array slice value which is an one-dimensional array of elements
	 * of type t, where t is the base type of the given array shape (
	 * {@link ArrayShape#baseType}).
	 * </p>
	 * 
	 * <p>
	 * Notice that the {@link ArrayShape#baseType} doesn't have to be non-array
	 * type.
	 * </p>
	 * 
	 * <p>
	 * Notice that the given indices must refer to element of the type t, i.e.
	 * <code> {@link ArrayShape#dimensions} == indices.length</code>
	 * </p>
	 * 
	 * @param array
	 *            an array
	 * @param shape
	 *            the {@link ArrayShape} of the given array, the
	 *            {@link ArrayShape#dimensions} must equals to the
	 *            <code>indices.length</code>
	 * @param indices
	 *            the indices of the element in the given array which is the
	 *            first element in the returned array. (inclusive)
	 * @param count
	 *            the number of elements in the returned array
	 * @return an another array that contains a consecutive part of elements of
	 *         the given array.
	 */
	SymbolicExpression arraySlice(SymbolicExpression array, ArrayShape shape,
			NumericExpression indices[], NumericExpression count);

	/**
	 * Same as
	 * {@link #arraySliceRead(SymbolicExpression, NumericExpression[], NumericExpression)}
	 * but the "count" is known as having a concrete value.
	 */
	SymbolicExpression arraySlice(SymbolicExpression array, ArrayShape shape,
			NumericExpression indices[], int count);

	/**
	 * <p>
	 * Writes an array slice s, which is represents by the three given arguments
	 * : <code>sliceArray, sliceIndices, count</code>, into the
	 * <code>targetArray</code>, starting from the given
	 * <code>targetStartIndices</code>
	 * </p>
	 * 
	 * 
	 * @param sliceArray
	 *            the array where the slice is carved out
	 * @param sliceArrayShape
	 *            the {@link ArrayShape} of the <code>sliceArray</code>
	 * @param sliceIndices
	 *            the starting indices of the slice in the
	 *            <code>sliceArray</code>. The {@link ArrayShape#dimensions} of
	 *            sliceArrayShape must equal to <code>sliceIndices.length</code>
	 * @param count
	 *            the number of elements in the slice, the type of the element
	 *            is {@link ArrayShape#baseType}
	 * @param targetArray
	 *            the target array where the slice will be written into
	 * @param targetShape
	 *            the {@link ArrayShape} of the target array. The
	 *            {@link ArrayShape#baseType} of targetShape must equal to the
	 *            one of sliceArrayShape
	 * @param targetStartIndices
	 *            The {@link ArrayShape#dimensions} of targetShape must equal to
	 *            <code>targetStartIndices.length</code>
	 * @return
	 */
	SymbolicExpression arraySliceWrite(SymbolicExpression sliceArray,
			ArrayShape sliceArrayShape, NumericExpression[] sliceIndices,
			NumericExpression count, SymbolicExpression targetArray,
			ArrayShape targetShape, NumericExpression[] targetStartIndices);

	/**
	 * Read a multi-dimensional array
	 * 
	 * @param array
	 *            a multi-dimensional array
	 * @param indices
	 *            indices for reading
	 * @return The element indexed by the given indices
	 */
	SymbolicExpression mdArrayRead(SymbolicExpression array,
			NumericExpression indices[]);

	/**
	 * Write a value to a multi-dimensional array
	 * 
	 * @param array
	 *            a multi-dimensional array
	 * @param indices
	 *            indices for reading
	 * @return new array after writing
	 */
	SymbolicExpression mdArrayWrite(SymbolicExpression array,
			NumericExpression indices[], SymbolicExpression value);
}
