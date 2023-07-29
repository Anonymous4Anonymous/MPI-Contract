package edu.udel.cis.vsl.civl.semantics.common;

import java.util.ArrayList;
import java.util.List;

import edu.udel.cis.vsl.civl.semantics.IF.ArrayCutter;
import edu.udel.cis.vsl.civl.semantics.IF.ArrayReshaper;
import edu.udel.cis.vsl.civl.semantics.IF.ArrayToolBox.ArrayShape;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 * A simple implementation of {@link ArrayCutter} where no prover is called.
 * 
 * @author xxxx
 *
 */
public class SimpleArrayCutter implements ArrayCutter {
	/**
	 * a reference to {@link SymbolicUniverse}.
	 */
	private SymbolicUniverse universe;

	/**
	 * a reference to {@link ArrayReshaper}
	 */
	private ArrayReshaper reshaper;

	SimpleArrayCutter(SymbolicUniverse universe, ArrayReshaper reshaper) {
		this.universe = universe;
		this.reshaper = reshaper;
	}

	@Override
	public SymbolicExpression arraySlice(SymbolicExpression array,
			ArrayShape shape, NumericExpression[] indices,
			NumericExpression count) {
		assert shape.dimensions == indices.length;
		Number countConcreteValue = universe.extractNumber(count);
		NumericExpression zero = universe.zeroInt();
		boolean isWholeArray = true;

		// special case 1: if the slice is the whole array
		for (NumericExpression idx : indices)
			if (idx != zero) {
				isWholeArray = false;
				break;
			}
		if (isWholeArray && shape.arraySize == count)
			return array;
		// special case 2: if count is concrete
		if (countConcreteValue != null)
			return arraySlice(array, shape, indices,
					((IntegerNumber) countConcreteValue).intValue());
		// general case:
		return arraySlice_general(array, shape, indices, count);
	}

	@Override
	public SymbolicExpression arraySlice(SymbolicExpression array,
			ArrayShape shape, NumericExpression[] indices, int count) {
		List<SymbolicExpression> elements = arraySlice_concreteCount(array,
				shape, indices, count);

		return universe.array(shape.baseType, elements);
	}

	@Override
	public SymbolicExpression arraySliceWrite(SymbolicExpression sliceArray,
			ArrayShape sliceArrayShape, NumericExpression[] sliceStartIndices,
			NumericExpression count, SymbolicExpression targetArray,
			ArrayShape targetShape, NumericExpression[] targetStartIndices) {
		assert sliceArrayShape.dimensions == sliceStartIndices.length;
		assert targetShape.dimensions == targetStartIndices.length;
		// baseTypes of sliceArrayShape and targetArray must be equivalent, but
		// the check requires reasoner, so it will not be checked as an
		// assertion here ...

		Number countConcreteValue = universe.extractNumber(count);

		if (countConcreteValue != null) {
			int countIntValue = ((IntegerNumber) countConcreteValue).intValue();
			int targetDims = targetShape.dimensions;
			ArrayList<SymbolicExpression> elements = arraySlice_concreteCount(
					sliceArray, sliceArrayShape, sliceStartIndices,
					countIntValue);
			NumericExpression writtenPos = universe.zeroInt();
			NumericExpression[] writtenIndices = new NumericExpression[targetDims];

			// general formula for computing written indices ...
			for (int i = 0; i < targetDims; i++)
				writtenPos = universe.add(writtenPos, universe.multiply(
						targetStartIndices[i], targetShape.subArraySizes[i]));
			for (int i = 0; i < countIntValue; i++) {
				NumericExpression currPos = universe.add(writtenPos,
						universe.integer(i));
				// TODO: optimize this:
				// write elements one by one ...
				for (int j = 0; j < targetDims; j++) {
					writtenIndices[j] = universe.divide(currPos,
							targetShape.subArraySizes[j]);
					currPos = universe.modulo(currPos,
							targetShape.subArraySizes[j]);
				}
				targetArray = mdArrayWrite(targetArray, writtenIndices,
						elements.get(i));
			}
			return targetArray;
		} else
			return sliceReadThenWrite_general(sliceArray, sliceArrayShape,
					sliceStartIndices, count, targetArray, targetShape,
					targetStartIndices);
	}

	@Override
	public SymbolicExpression mdArrayRead(SymbolicExpression array,
			NumericExpression indices[]) {
		SymbolicExpression result = array;

		for (int i = 0; i < indices.length; i++)
			result = universe.arrayRead(result, indices[i]);
		return result;
	}

	@Override
	public SymbolicExpression mdArrayWrite(SymbolicExpression array,
			NumericExpression indices[], SymbolicExpression value) {
		return mdArrayWriteWorker(array, indices, 0, value);
	}

	/* ************************** private methods **********************/

	private SymbolicExpression mdArrayWriteWorker(SymbolicExpression array,
			NumericExpression indices[], int indicesHead,
			SymbolicExpression value) {
		if (indices.length == indicesHead)
			return array;
		if (indices.length - indicesHead == 1)
			return universe.arrayWrite(array, indices[indicesHead], value);
		else
			return universe.arrayWrite(array, indices[indicesHead],
					mdArrayWriteWorker(
							universe.arrayRead(array, indices[indicesHead]),
							indices, indicesHead + 1, value));
	}
	/**
	 * <p>
	 * The general solution for "array slice read-then-write" operation. Reading
	 * an array slice from a "data" array <code>d</code>, starting from indices
	 * <code>d_I</code>, then writes the slice into a target array
	 * <code>t</code>, starting from indices <code>t_I</code>.
	 * </p>
	 * 
	 * <p>
	 * This solution uses array lambda and conditional expression to represent
	 * such operation.
	 * </p>
	 * 
	 * @param dataArray
	 *            the array where a slice is carved out
	 * @param dataShape
	 *            the shape of the dataArray
	 * @param dataStartIndices
	 *            the starting indices of the slice.
	 *            <code>dataStartIndices.length == dataShape.dimensions</code>
	 * @param count
	 *            the number of elements in the slice
	 * @param targetArray
	 *            the array where the slice will be written into
	 * @param targetShape
	 *            the shape of the targetArray
	 * @param targetStartIndices
	 *            the starting indices of where the slice being written.
	 *            <code>targetStartIndices.length == targetShape.dimensions</code>
	 * @return the updated targetArray
	 */
	private SymbolicExpression sliceReadThenWrite_general(
			SymbolicExpression dataArray, ArrayShape dataShape,
			NumericExpression[] dataStartIndices, NumericExpression count,
			SymbolicExpression targetArray, ArrayShape targetShape,
			NumericExpression[] targetStartIndices) {
		int targetDims = targetShape.dimensions;
		NumericSymbolicConstant symConsts[] = new NumericSymbolicConstant[targetDims];

		for (int i = 0; i < symConsts.length; i++)
			symConsts[i] = (NumericSymbolicConstant) universe.symbolicConstant(
					universe.stringObject("i" + i), universe.integerType());

		// compute indices that can access data array as if it has the target
		// shape
		NumericExpression projectedConstsToDataArray[] = reshaper
				.sliceIndiceProjecting(symConsts, targetShape,
						targetStartIndices, dataShape, dataStartIndices);
		SymbolicExpression dataSliceLambda = dataArray;
		int dataDims = dataShape.dimensions;

		// read slice in data array as if it has the target shape
		for (int i = 0; i < dataDims; i++)
			dataSliceLambda = universe.arrayRead(dataSliceLambda,
					projectedConstsToDataArray[i]);

		// the range of the slice in the target array:
		NumericExpression lower = universe.zeroInt();
		// upper = lower + count (exclusive)

		for (int i = 0; i < targetDims; i++)
			if (targetStartIndices[i].isZero())
				continue;
			else
				lower = universe.add(lower, universe.multiply(
						targetStartIndices[i], targetShape.subArraySizes[i]));

		// lower <= position < upper ? dataSliceLambda : targetArrayLambda
		NumericExpression pos = universe.zeroInt();
		BooleanExpression cond;
		SymbolicExpression targetArrayLambda, resultLambda;

		for (int i = 0; i < targetDims; i++)
			pos = universe.add(pos, universe.multiply(symConsts[i],
					targetShape.subArraySizes[i]));
		cond = universe.and(universe.lessThanEquals(lower, pos),
				universe.lessThan(pos, universe.add(lower, count)));
		targetArrayLambda = mdArrayRead(targetArray, symConsts);
		resultLambda = universe.cond(cond, dataSliceLambda, targetArrayLambda);

		// build array lambda then return ...
		SymbolicType elementType = resultLambda.type();
		for (int i = targetDims - 1; i >= 0; i--) {
			resultLambda = universe.lambda(symConsts[i], resultLambda);
			resultLambda = universe.arrayLambda(
					universe.arrayType(elementType, targetShape.extents[i]),
					resultLambda);
			elementType = resultLambda.type();
		}
		return resultLambda;
	}

	/**
	 * Read a slice from an array from a starting indices
	 * 
	 * @param array
	 *            the array where a slice will be carved out
	 * @param shape
	 *            the shape of the array
	 * @param indices
	 *            the starting indices of the slice
	 *            <code>indices.length == shape.dimensions</code>
	 * @param count
	 *            the number of elements in the slice.
	 *            <code>element.type() == shape.baseType</code>
	 * @return a slice which is represented as an {@link ArrayList} of elements
	 */
	private ArrayList<SymbolicExpression> arraySlice_concreteCount(
			SymbolicExpression array, ArrayShape shape,
			NumericExpression[] indices, int count) {
		ArrayList<SymbolicExpression> elements = new ArrayList<>();
		NumericExpression pos = universe.zeroInt();

		for (int j = 0; j < indices.length; j++)
			pos = universe.add(pos,
					universe.multiply(indices[j], shape.subArraySizes[j]));

		for (int i = 0; i < count; i++) {
			NumericExpression remain = universe.add(pos, universe.integer(i));

			for (int j = 0; j < indices.length; j++) {
				indices[j] = universe.divide(remain, shape.subArraySizes[j]);
				remain = universe.modulo(remain, shape.subArraySizes[j]);
			}
			elements.add(mdArrayRead(array, indices));
		}
		return elements;
	}

	/**
	 * Read a slice from an array from a starting indices
	 * 
	 * @param array
	 *            the array where a slice will be carved out
	 * @param shape
	 *            the shape of the array
	 * @param indices
	 *            the starting indices of the slice
	 *            <code>indices.length == shape.dimensions</code>
	 * @param count
	 *            the number of elements in the slice.
	 *            <code>element.type() == shape.baseType</code>
	 * @return a slice which is represented as an {@link ArrayList} of elements
	 */
	private SymbolicExpression arraySlice_general(SymbolicExpression array,
			ArrayShape arrayShape, NumericExpression[] indices,
			NumericExpression count) {
		// a 1d array lambda ...
		NumericSymbolicConstant symConst = (NumericSymbolicConstant) universe
				.symbolicConstant(universe.stringObject("i"),
						universe.integerType());
		NumericExpression pos = universe.zeroInt();

		for (int i = 0; i < indices.length; i++)
			pos = universe.add(pos,
					universe.multiply(indices[i], arrayShape.subArraySizes[i]));

		SymbolicExpression lambdaFunc = array;

		pos = universe.add(pos, symConst);
		for (int i = 0; i < indices.length; i++) {
			lambdaFunc = universe.arrayRead(lambdaFunc,
					universe.divide(pos, arrayShape.subArraySizes[i]));
			pos = universe.modulo(pos, arrayShape.subArraySizes[i]);
		}
		return universe.arrayLambda(
				universe.arrayType(arrayShape.baseType, count),
				universe.lambda(symConst, lambdaFunc));
	}
}
