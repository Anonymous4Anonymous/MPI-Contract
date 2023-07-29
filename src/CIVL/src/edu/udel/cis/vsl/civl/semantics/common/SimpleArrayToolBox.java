package edu.udel.cis.vsl.civl.semantics.common;

import java.util.Arrays;

import edu.udel.cis.vsl.civl.semantics.IF.ArrayCutter;
import edu.udel.cis.vsl.civl.semantics.IF.ArrayReshaper;
import edu.udel.cis.vsl.civl.semantics.IF.ArrayToolBox;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;

public class SimpleArrayToolBox implements ArrayToolBox {

	static class CommonArrayShape extends ArrayShape {
		CommonArrayShape(int dimensions, NumericExpression extents[],
				NumericExpression sliceSizes[], NumericExpression arraySize,
				SymbolicType baseType) {
			this.dimensions = dimensions;
			this.extents = extents;
			this.subArraySizes = sliceSizes;
			this.arraySize = arraySize;
			this.baseType = baseType;
		}
	}

	static class CommonArraySlice extends ArraySlice {
		CommonArraySlice(SymbolicExpression array,
				NumericExpression startIndices[], NumericExpression count,
				SymbolicType baseType, SymbolicArrayType sliceType) {
			this.array = array;
			this.startIndices = startIndices;
			this.count = count;
			this.baseType = baseType;
			this.sliceType = sliceType;
		}
	}

	/**
	 * a reference to {@link ArrayReshaper}
	 */
	private ArrayReshaper arrayReshaper;
	/**
	 * a reference to {@link ArrayCutter}
	 */
	private ArrayCutter arrayCutter;

	/**
	 * a reference to {@link SymbolicUniverse}
	 */
	private SymbolicUniverse universe;

	public SimpleArrayToolBox(SymbolicUniverse universe) {
		this.universe = universe;
		this.arrayReshaper = new SimpleArrayReshaper(universe);
		this.arrayCutter = new SimpleArrayCutter(universe, arrayReshaper);
	}

	@Override
	public SymbolicExpression arrayFlatten(SymbolicExpression array) {
		return arrayReshaper.arrayFlatten(array,
				newArrayShape((SymbolicArrayType) array.type()));
	}

	@Override
	public SymbolicExpression arrayReshape(SymbolicExpression array,
			ArrayShape arrayShape) {
		return arrayReshaper.arrayReshape(array,
				newArrayShape((SymbolicArrayType) array.type()), arrayShape);
	}

	@Override
	public BooleanExpression areArrayShapesPhysicallyEquivalent(ArrayShape s0,
			ArrayShape s1) {
		if (s0.baseType == s1.baseType)
			return universe.equals(
					universe.multiply(s0.subArraySizes[0], s0.extents[0]),
					universe.multiply(s1.subArraySizes[0], s1.extents[0]));
		return universe.falseExpression();
	}

	@Override
	public boolean isArrayTypeallComplete(SymbolicArrayType arrayType) {
		return arrayReshaper.allComplete(arrayType);
	}

	@Override
	public SymbolicExpression arraySliceRead(SymbolicExpression array,
			NumericExpression[] indices, NumericExpression count) {
		return arrayCutter.arraySlice(array,
				newArrayShape((SymbolicArrayType) array.type()), indices,
				count);
	}

	@Override
	public SymbolicExpression arraySliceRead(SymbolicExpression array,
			NumericExpression[] indices, int count) {
		return arrayCutter.arraySlice(array,
				newArrayShape((SymbolicArrayType) array.type()), indices,
				count);
	}

	@Override
	public SymbolicExpression arraySliceWrite(ArraySlice arraySlice,
			SymbolicExpression targetArray, ArrayShape targetShape,
			NumericExpression[] targetStartIndices) {
		assert targetStartIndices.length == targetShape.dimensions;
		// AND targetArray.baseType == arraySlice.baseType
		ArrayShape sliceArrayShape = newArrayShape(
				(SymbolicArrayType) arraySlice.array.type(),
				arraySlice.baseType);

		return arrayCutter.arraySliceWrite(arraySlice.array, sliceArrayShape,
				arraySlice.startIndices, arraySlice.count, targetArray,
				targetShape, targetStartIndices);
	}

	@Override
	public ArrayShape newArrayShape(SymbolicArrayType arrayType) {
		return newArrayShape(arrayType, arrayType.baseType());
	}

	@Override
	public ArrayShape newArrayShape(SymbolicArrayType arrayType,
			SymbolicType baseType) {
		if (!arrayReshaper.allComplete(arrayType))
			return null;

		int dimensions = arrayType.dimensions();
		NumericExpression extents[];
		NumericExpression sliceSizes[];

		if (baseType.typeKind() == SymbolicTypeKind.ARRAY)
			dimensions = dimensions
					- ((SymbolicArrayType) baseType).dimensions();

		extents = new NumericExpression[dimensions];
		sliceSizes = new NumericExpression[dimensions];
		// Get extents:
		for (int i = 0; i < dimensions; i++) {
			assert arrayType.isComplete();
			SymbolicCompleteArrayType completeType = (SymbolicCompleteArrayType) arrayType;

			extents[i] = completeType.extent();
			if (i < dimensions - 1)
				arrayType = (SymbolicArrayType) arrayType.elementType();
		}
		baseType = arrayType.elementType();

		// Compute slice sizes:
		NumericExpression sliceSize = universe.oneInt();

		for (int i = dimensions; --i >= 0;) {
			sliceSizes[i] = sliceSize;
			sliceSize = universe.multiply(sliceSize, extents[i]);
		}
		return new CommonArrayShape(dimensions, extents, sliceSizes,
				universe.multiply(extents[0], sliceSizes[0]), baseType);
	}

	@Override
	public ArraySlice newArraySlice(SymbolicExpression array,
			NumericExpression[] startIndices, NumericExpression count,
			SymbolicType baseType) {
		return new CommonArraySlice(array, startIndices, count, baseType,
				universe.arrayType(baseType, count));
	}

	@Override
	public SymbolicExpression extractArraySlice(ArraySlice slice) {
		if (slice.array.type()
				.equals(universe.arrayType(slice.baseType, slice.count))) {
			// the slice is the whole array, iff it satisfies the following
			// conditions:
			// 1. All starting indices are zero
			// 2. 'array type' == array of 'base type' with length 'count'
			boolean allIndicesZero = true;

			for (int i = 0; i < slice.startIndices.length; i++)
				if (!slice.startIndices[i].isZero()) {
					allIndicesZero = false;
					break;
				}
			if (allIndicesZero)
				return slice.array;
		}

		ArrayShape sliceArrayShape = newArrayShape(
				(SymbolicArrayType) slice.array.type(), slice.baseType);

		return arrayCutter.arraySlice(slice.array, sliceArrayShape,
				slice.startIndices, slice.count);
	}

	@Override
	public SymbolicExpression mdArrayRead(SymbolicExpression array,
			NumericExpression[] indices) {
		return arrayCutter.mdArrayRead(array, indices);
	}

	@Override
	public SymbolicExpression mdArrayWrite(SymbolicExpression array,
			NumericExpression[] indices, SymbolicExpression value) {
		return arrayCutter.mdArrayWrite(array, indices, value);
	}

	@Override
	public ArrayShape subArrayShape(ArrayShape arrayShape,
			int subArrayDimensions) {
		assert 0 < subArrayDimensions
				&& subArrayDimensions <= arrayShape.dimensions;
		if (subArrayDimensions == arrayShape.dimensions)
			return arrayShape;
		NumericExpression subExtents[] = new NumericExpression[subArrayDimensions];
		NumericExpression subSubArraySizes[] = new NumericExpression[subArrayDimensions];
		int dims = arrayShape.dimensions;

		for (int i = 0; i < subArrayDimensions; i++) {
			subExtents[i] = arrayShape.extents[dims - subArrayDimensions + i];
			subSubArraySizes[i] = arrayShape.subArraySizes[dims
					- subArrayDimensions + i];
		}
		return new CommonArrayShape(subArrayDimensions, subExtents,
				subSubArraySizes,
				universe.multiply(subExtents[0], subSubArraySizes[0]),
				arrayShape.baseType);
	}

	@Override
	public ArrayShape switchBaseType(ArrayShape arrayShape,
			int baseTypeDimensions) {
		assert 0 < baseTypeDimensions
				&& baseTypeDimensions < arrayShape.dimensions;
		int dimToBase = arrayShape.dimensions - baseTypeDimensions;
		SymbolicType newBaseType = arrayShape.baseType;

		for (int i = arrayShape.dimensions - 1; i >= dimToBase; i--)
			newBaseType = universe.arrayType(newBaseType,
					arrayShape.extents[i]);

		NumericExpression newExtents[] = Arrays.copyOfRange(arrayShape.extents,
				0, dimToBase);
		NumericExpression newSubArraySizes[] = new NumericExpression[dimToBase];
		NumericExpression subArraySize = universe.oneInt();

		for (int i = dimToBase - 1; i >= 0; i--) {
			newSubArraySizes[i] = subArraySize;
			subArraySize = universe.multiply(subArraySize, newExtents[i]);
		}
		return new CommonArrayShape(dimToBase, newExtents, newSubArraySizes,
				universe.multiply(newExtents[0], newSubArraySizes[0]),
				newBaseType);
	}

	@Override
	public NumericExpression[] sliceIndiceProjecting(
			NumericExpression[] fromSliceArrayIndices,
			ArrayShape fromSliceArrayShape,
			NumericExpression[] fromSliceStartIndices,
			ArrayShape toSliceArrayShape,
			NumericExpression[] toSliceStartIndices) {
		return arrayReshaper.sliceIndiceProjecting(fromSliceArrayIndices,
				fromSliceArrayShape, fromSliceStartIndices, toSliceArrayShape,
				toSliceStartIndices);
	}

	@Override
	public NumericExpression[] arrayIndiceProjecting(
			NumericExpression[] fromArrayIndices, ArrayShape fromArrayShape,
			ArrayShape toArrayShape) {
		return arrayReshaper.arrayIndiceProjecting(fromArrayIndices,
				fromArrayShape, toArrayShape);
	}
}
