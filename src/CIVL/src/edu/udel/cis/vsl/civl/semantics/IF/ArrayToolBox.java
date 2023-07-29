package edu.udel.cis.vsl.civl.semantics.IF;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * This interface provides a collection of methods that manipulating symbolic
 * expressions whose symbolic type has ARRAY kind.
 * 
 * @author xxxx
 */
public interface ArrayToolBox {
	/**
	 * The data structure for describing array shapes, including dimensions,
	 * extent for each dimension and slice size for each sub-array with lower
	 * dimension.
	 * 
	 * There are three fields can be read from an instance of this class:
	 * <ol>
	 * <li>dimensions: number of dimensions in the corresponding array</li>
	 * <li>extents: extents of dimensions in the corresponding array, extents
	 * are saved in a "Java array" which have the same order as order of
	 * declaring the corresponding array.</li>
	 * <li>subArraySizes: sizes of sub-array slices for each lower dimension,
	 * sizes are saved in a "java array" which have the order from largest to
	 * the smallest.</li>
	 * <li>baseType: the base type of the corresponding array. Notice that the
	 * base type doesn't have to have a non-array type.</li>
	 * </ol>
	 * 
	 * @author xxxx
	 *
	 */
	public static abstract class ArrayShape {
		public int dimensions;

		public NumericExpression extents[];

		public NumericExpression subArraySizes[];

		public NumericExpression arraySize;

		public SymbolicType baseType;
	}

	/**
	 * the data structure for describing array slices. An array slice is a
	 * sequence of objects of type t that is carved out from an array.
	 * 
	 * @author xxxx
	 *
	 */
	public static abstract class ArraySlice {
		/**
		 * the array where the slice is carved out
		 */
		public SymbolicExpression array;

		/**
		 * the starting indices for where the slice is carved out
		 */
		public NumericExpression startIndices[];

		/**
		 * the number of elements in the slice
		 */
		public NumericExpression count;
		/**
		 * the type of the elements
		 */
		public SymbolicType baseType;
		/**
		 * the type of the slice
		 */
		public SymbolicArrayType sliceType;
	}

	/**
	 * Reshape a multiple dimensional array of type t to a one dimensional array
	 * of type t. Notice that the type t is the base type of the given
	 * {@link ArrayShape}. (i.e. {@link ArrayShape#baseType}).
	 * 
	 * @param array
	 *            a multiple dimensional array
	 * @return a one dimensional array which is equivalent (physically in C
	 *         language) to the given array.
	 */
	SymbolicExpression arrayFlatten(SymbolicExpression array);

	/**
	 * Reshape an array <code>a</code> to the given target shape <code>t</code>.
	 * The target shape <code>t</code> must be physically equivalent to the
	 * shape of array <code>a</code>.
	 * 
	 * @param array
	 *            the array that will be reshaped.
	 * @param targetShape
	 *            the {@link ArrayShape} that the given array will be reshaped
	 *            to
	 * @return an array of the given target shape that is physically equivalent
	 *         to the given array.
	 */
	SymbolicExpression arrayReshape(SymbolicExpression array,
			ArrayShape targethape);

	/**
	 * create an {@link ArrayShape}, the base type will be (default) non-array
	 * type
	 * 
	 * @param arrayType
	 * @return The {@link ArrayShape} of the given array type, or null is the
	 *         array type is NOT {@link #allComplete(SymbolicArrayType)}
	 */
	ArrayShape newArrayShape(SymbolicArrayType arrayType);

	/**
	 * create an {@link ArrayShape} with a specified base type. The behavior is
	 * undefined if the given base type is not a sub-type of the given array
	 * type.
	 * 
	 * @param arrayType
	 * @param baseType
	 * @return The {@link ArrayShape} of the given array type, or null is the
	 *         array type is NOT {@link #allComplete(SymbolicArrayType)}
	 */
	ArrayShape newArrayShape(SymbolicArrayType arrayType,
			SymbolicType baseType);

	/**
	 * create an {@link ArraySlice}.
	 * 
	 * @param array
	 *            the array where the slice is carved out
	 * @param startIndices
	 *            the starting indices for carving out the slice from the array
	 * @param count
	 *            the number of base type elements in the slice, notice that the
	 *            base type is decided by <code>startIndices.length</code>
	 * @param baseType
	 *            the base type of this slice. The slice type is an array of
	 *            "count" baseType elements.
	 * @return an {@link ArraySlice}
	 */
	ArraySlice newArraySlice(SymbolicExpression array,
			NumericExpression startIndices[], NumericExpression count,
			SymbolicType baseType);

	/**
	 * Returns a {@link SymbolicExpression} representing the given
	 * {@link ArraySlice}. The type of the symbolic expression will be the
	 * {@link ArraySlice#sliceType} of <code>arraySlice</code>.
	 */
	SymbolicExpression extractArraySlice(ArraySlice arraySlice);

	/**
	 * <p>
	 * Projecting the indices of an array slice <code>s0</code> to another array
	 * slice <code>s1</code>. Requires that <code>s0</code> and <code>s1</code>
	 * have exact same base type.
	 * </p>
	 * 
	 * <p>
	 * Example, there is an array slice <code>s0</code> in an array
	 * <code>T a[10][10]</code>, from indices <code>{2,3}</code>, of length 10.
	 * Then the indices of such slice are
	 * <code>I : {{2, 3}, {2, 4}, ..., {2, 9}, {3, 0}, ..., {3, 2}}</code>.
	 * </p>
	 * 
	 * <p>
	 * And, there is another array slice <code>s1</code> in an array
	 * <code>T b[100]</code>, from index <code>{x}</code>, of length 20.
	 * </p>
	 * 
	 * <p>
	 * Now projects indices <code>{i, j}</code>, which belongs to <code>I</code>
	 * to indices of <code>s1</code>, the result should be: <code>
	 * k : i * 10 + j - (2 * 10 + 3) + x
	 * </code> <br>
	 * This projecting is mainly used for such case : the value of n-th element
	 * of <code>s0</code>, which is referred by <code>{i, j}</code>, is the
	 * value of n-th element of <code>s1</code>, which is referred by
	 * <code>k</code>.
	 * </p>
	 * 
	 * <p>
	 * Notice that the caller of this method is responsible to guarantee that
	 * the given indices belong to <code>s0</code> and the projected indices
	 * (returned indices) belong to <code>s1</code>.
	 * </p>
	 * 
	 * @param fromSliceArrayIndices
	 *            The indices over the array of slice <code>s0</code>, that
	 *            belongs to <code>s0</code>. They refer to the n-th element in
	 *            <code>s0</code>
	 * @param fromSliceArrayShape
	 *            the shape of the array of slice <code>s0</code>
	 * @param fromSliceStartIndices
	 *            the starting indices of slice <code>s0</code>
	 * @param toSliceArrayShape
	 *            the shape of the array of projected slice <code>s1</code>
	 * @param toSliceStartIndices
	 *            the starting indices of projected slice <code>s1</code>
	 * @return the indices of the n-th element in <code>s1</code>
	 */
	NumericExpression[] sliceIndiceProjecting(
			NumericExpression[] fromSliceArrayIndices,
			ArrayShape fromSliceArrayShape,
			NumericExpression[] fromSliceStartIndices,
			ArrayShape toSliceArrayShape,
			NumericExpression[] toSliceStartIndices);

	/**
	 * <p>
	 * Projecting indices <code>I</code> of an array <code>a0</code> to another
	 * array <code>a1</code>, i.e. the value of the n-th element, referred by I,
	 * in <code>a0</code> is same as the value of the n-th element in
	 * <code>a1</code>.
	 * </p>
	 * 
	 * <p>
	 * Notice that caller of this method is responsible to guarantee that the
	 * given indices I belongs to <code>a0</code> (is valid indices of
	 * <code>a0</code>) and the resulting indices belongs to <code>a1</code>
	 * </p>
	 * 
	 * @param fromArrayIndices
	 *            the indices of array <code>a0</code>, refers to the n-th
	 *            element of <code>a0</code>
	 * @param fromArrayShape
	 *            the shape of the array <code>a0</code>
	 * @param toArrayShape
	 *            the shape of the array <code>a1</code>
	 * @return indices referring to the n-th element of <code>a1</code>
	 */
	NumericExpression[] arrayIndiceProjecting(
			NumericExpression[] fromArrayIndices, ArrayShape fromArrayShape,
			ArrayShape toArrayShape);

	/**
	 * Test if two array shapes s0 and s1 are physically equivalent. s0 and s1
	 * are physically equivalent if and only if there exists an element type e_t
	 * such that array of s0 has N elements of e_t and array of s1 has M
	 * elements of e_t and N = M.
	 * 
	 * @param s0
	 * @param s1
	 * 
	 * @return True iff the two array shapes are physically equivalent.
	 */
	BooleanExpression areArrayShapesPhysicallyEquivalent(ArrayShape s0,
			ArrayShape s1);

	/**
	 * @param arrayType
	 * @return true iff the given type is an array type and is complete at every
	 *         dimension.
	 */
	boolean isArrayTypeallComplete(SymbolicArrayType arrayType);

	/**
	 * <p>
	 * Returns a symbolic expression representing an array slice which is an
	 * one-dimensional array of elements of type <code>t</code> that is carved
	 * out from the given array <code>a</code>, where <code>t</code> is
	 * associated to to the given indices:
	 * <code>{i<sub>0</sub>, i<sub>1</sub>, ... i<sub>n-1</sub>}</code>. <br>
	 * <code>t = typeof(a[i<sub>0</sub>][...][i<sub>n-1</sub>]</code>
	 * </p>
	 * 
	 * @param array
	 *            an array
	 * @param indices
	 *            the indices of the element in the given array which is the
	 *            first element in the returned array. (inclusive)
	 * @param count
	 *            the number of elements in the returned array
	 * @return a symbolic expression representing the array slice
	 */
	SymbolicExpression arraySliceRead(SymbolicExpression array,
			NumericExpression indices[], NumericExpression count);

	/**
	 * Same as
	 * {@link #arraySliceRead(SymbolicExpression, NumericExpression[], NumericExpression)}
	 * but the "count" is known as having a concrete value.
	 */
	SymbolicExpression arraySliceRead(SymbolicExpression array,
			NumericExpression indices[], int count);

	/**
	 * <p>
	 * Write an {@link ArraySlice} s into the <code>targetArray</code>, starting
	 * from the given <code>targetStartIndices</code>
	 * </p>
	 * 
	 * @param slice
	 *            the array slice
	 * @param targetArray
	 *            the target array where the slice will be written in
	 * @param targetShape
	 *            the {@link ArrayShape} of the target array.
	 *            <code>targetShape</code> and <code>sliceShape</code> must have
	 *            the same base type
	 * @param targetStartIndices
	 *            the start indices of the written slice in the target array.
	 *            (inclusive).
	 *            <code>targetStartIndices.length == targetShape.dimensions</code>
	 * @return the updated target array which has been written the slice
	 */
	SymbolicExpression arraySliceWrite(ArraySlice slice,
			SymbolicExpression targetArray, ArrayShape targetShape,
			NumericExpression[] targetStartIndices);

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

	/**
	 * @param arrayShape
	 *            the array shape of an array
	 * @param subArrayDimensions
	 *            the dimension of the sub-array, it must be greater than 0 and
	 *            LESS THAN OR EQUAL TO the dimensions of the given array shape.
	 * @return The {@link ArrayShape} of the sub array, which is a
	 *         "subArrayDimensions"-dimensional array of "arrayShape".baseType
	 */
	ArrayShape subArrayShape(ArrayShape arrayShape, int subArrayDimensions);

	/**
	 * Given an array shape <code>s</code>, returns an new array shape
	 * <code>s'</code>. <code>s</code> and <code>s'</code> represent exact the
	 * same array shape but the base type of <code>s'</code> may have an array
	 * type of the base type of <code>s</code>.
	 * 
	 * @param arrayShape
	 *            the {@link ArrayShape} of an array
	 * @param baseTypeDimensions
	 *            the dimensions of the new base type. The new base type is a
	 *            'baseTypeDimensions'-dimensional array of the old base type.
	 *            'baseTypeDimensions' must be GREATER THAN OR EQUAL TO 0 and
	 *            LESS THAN the dimensions of the given array shape.
	 * @return a new {@link ArrayShape} which represents the same shape as the
	 *         given one but has different base type.
	 */
	ArrayShape switchBaseType(ArrayShape arrayShape, int baseTypeDimensions);
}
