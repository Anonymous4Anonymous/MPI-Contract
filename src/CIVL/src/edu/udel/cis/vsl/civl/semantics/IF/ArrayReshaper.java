package edu.udel.cis.vsl.civl.semantics.IF;

import edu.udel.cis.vsl.civl.semantics.IF.ArrayToolBox.ArrayShape;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;

/**
 * <p>
 * This class provides methods for reshaping arrays. In C language, an array of
 * objects is physically a sequence of objects in memory but can be accessed in
 * various ways. For example, an array <code>T a[10]</code> can be access as a
 * one dimensional array of T with length 10, as a two dimensional array
 * <code>T a[2][5]</code>, a two dimensional array <code>T a[5][2]</code>, etc.
 * </p>
 * 
 * <p>
 * In CIVL, arrays are represented in a logical way, e.g. a two dimensional
 * array is a one dimensional array of arrays. But such difference will not
 * prevent us from providing array reshaping.
 * </p>
 * 
 * @author xxxx
 */
public interface ArrayReshaper {

	/**
	 * Reshape a multiple dimensional array of type t to a one dimensional array
	 * of type t. Notice that the type t is the base type of the given
	 * {@link ArrayShape}. (i.e. {@link ArrayShape#baseType}).
	 * 
	 * @param array
	 *            a multiple dimensional array
	 * @param shape
	 *            the {@link ArrayShape} of the given array
	 * @return a one dimensional array which is equivalent (physically in C
	 *         language) to the given array.
	 */
	SymbolicExpression arrayFlatten(SymbolicExpression array, ArrayShape shape);

	/**
	 * Reshape an array <code>a</code> to the given target shape <code>t</code>.
	 * The target shape <code>t</code> must be physically equivalent to the
	 * shape of array <code>a</code>.
	 * 
	 * @param array
	 *            the array that will be reshaped.
	 * @param originShape
	 *            the {@link ArrayShape} of the given array
	 * @param targetShape
	 *            the {@link ArrayShape} that the given array will be reshaped
	 *            to
	 * @return an array of the given target shape that is physically equivalent
	 *         to the given array.
	 */
	SymbolicExpression arrayReshape(SymbolicExpression array,
			ArrayShape originShape, ArrayShape targetShape);

	/**
	 * @param arrayType
	 * @return true iff the given type is an array type and is complete at every
	 *         dimension.
	 */
	boolean allComplete(SymbolicArrayType arrayType);

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
}
