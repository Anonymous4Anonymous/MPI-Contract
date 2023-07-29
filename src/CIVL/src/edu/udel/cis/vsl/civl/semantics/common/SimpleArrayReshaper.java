package edu.udel.cis.vsl.civl.semantics.common;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.civl.semantics.IF.ArrayReshaper;
import edu.udel.cis.vsl.civl.semantics.IF.ArrayToolBox.ArrayShape;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
/**
 * A simple {@link ArrayReshaper} implementation. No reasoning will be
 * performed. State independent.
 * 
 * @author xxxx
 */
public class SimpleArrayReshaper implements ArrayReshaper {

	SymbolicUniverse universe;

	SimpleArrayReshaper(SymbolicUniverse universe) {
		this.universe = universe;
	}

	@Override
	public SymbolicExpression arrayFlatten(SymbolicExpression array,
			ArrayShape shape) {
		int dims = shape.dimensions;

		if (dims == 1)
			return array;

		Number extentNums[] = new Number[dims];

		for (int d = 0; d < dims; d++) {
			if ((extentNums[d] = universe
					.extractNumber(shape.extents[d])) == null)
				break;
			if (d == dims - 1) // last iteration
				return arrayFlattenConcrete(array, extentNums, shape);
		}
		return arrayFlattenLambda(array, extentNums, shape);
	}

	@Override
	public SymbolicExpression arrayReshape(SymbolicExpression array,
			ArrayShape originShape, ArrayShape targetShape) {
		Number targetExtentNums[] = new Number[targetShape.dimensions];
		Number originExtentNums[] = new Number[originShape.dimensions];

		// if two shapes are the same:
		if (targetShape.dimensions == originShape.dimensions) {
			boolean isSame = true;

			for (int i = 0; i < targetShape.dimensions; i++)
				if (!originShape.extents[i].equals(targetShape.extents[i])) {
					isSame = false;
					break;
				}
			if (isSame)
				return array;
		}
		// if both shape have concrete shape:
		for (int i = 0; i < targetShape.dimensions; i++) {
			targetExtentNums[i] = universe
					.extractNumber(targetShape.extents[i]);
			if (targetExtentNums[i] == null)
				return arrayReshapeLambda(array, originShape, targetShape);
		}

		for (int i = 0; i < originShape.dimensions; i++) {
			originExtentNums[i] = universe
					.extractNumber(originShape.extents[i]);
			if (originExtentNums[i] == null)
				return arrayReshapeLambda(array, originShape, targetShape);
		}
		return arrayReshapeConcrete(array, originShape, originExtentNums,
				targetShape, targetExtentNums);
	}

	@Override
	public boolean allComplete(SymbolicArrayType arrayType) {
		return allCompleteWorker(arrayType);

	}

	/* ***************************** private methods *********************/
	static private boolean allCompleteWorker(SymbolicArrayType arrayType) {
		int dims = arrayType.dimensions();

		for (int i = 0; i < dims - 1; i++) {
			if (!arrayType.isComplete())
				return false;
			arrayType = (SymbolicArrayType) arrayType.elementType();
		}
		return true;
	}

	private SymbolicExpression arrayFlattenConcrete(SymbolicExpression array,
			Number[] extentNums, ArrayShape shape) {
		int extents[] = new int[extentNums.length];

		for (int i = 0; i < extents.length; i++)
			extents[i] = ((IntegerNumber) extentNums[i]).intValue();

		List<SymbolicExpression> elements = arrayFlattenConcreteWorker(array,
				extents, extents.length);

		return universe.array(shape.baseType, elements);
	}

	private List<SymbolicExpression> arrayFlattenConcreteWorker(
			SymbolicExpression array, int extents[], int dim) {
		List<SymbolicExpression> elements = new LinkedList<>();
		int extent = extents[extents.length - dim - 1];

		if (dim == 0)
			for (int i = 0; i < extent; i++)
				elements.add(universe.arrayRead(array, universe.integer(i)));
		else
			for (int i = 0; i < extent; i++)
				elements.addAll(arrayFlattenConcreteWorker(
						universe.arrayRead(array, universe.integer(i)), extents,
						dim - 1));
		return elements;
	}

	private SymbolicExpression arrayFlattenLambda(SymbolicExpression array,
			Number extentNums[], ArrayShape shape) {
		int dims = shape.dimensions;
		int newDims, i;
		NumericExpression[] arraySliceSizes = shape.subArraySizes;
		NumericExpression[] arrayExtents = shape.extents;

		// pre-optimize 1: transform an array that has such form
		// a[1][1][...][1][n][...][m] to a'[n][...][m]:
		for (i = 0; i < dims - 1; i++)
			if (shape.extents[i].isOne())
				array = universe.arrayRead(array, universe.zeroInt());
			else
				break;
		newDims = dims - i;
		if (newDims == 1)
			return array;
		if (newDims < dims) {
			arraySliceSizes = Arrays.copyOfRange(arraySliceSizes,
					dims - newDims, dims);
			extentNums = Arrays.copyOfRange(extentNums, dims - newDims, dims);
			arrayExtents = Arrays.copyOfRange(shape.extents, dims - newDims,
					dims);
			dims = newDims;
		}
		// end of pre-optimize
		return arrayFlattenLambdaWorker(array, arraySliceSizes, arrayExtents);
	}

	private SymbolicExpression arrayFlattenLambdaWorker(
			SymbolicExpression array, NumericExpression[] arraySliceSizes,
			NumericExpression[] arrayExtents) {
		int dims = arrayExtents.length;
		NumericSymbolicConstant symConst = (NumericSymbolicConstant) universe
				.symbolicConstant(universe.stringObject("i"),
						universe.integerType());
		NumericExpression index = symConst;
		SymbolicExpression arrayReadFunc = array;

		for (int d = 0; d < dims; d++) {
			arrayReadFunc = universe.arrayRead(arrayReadFunc,
					universe.divide(index, arraySliceSizes[d]));
			index = universe.modulo(index, arraySliceSizes[d]);
		}

		SymbolicCompleteArrayType arrayType = universe.arrayType(
				arrayReadFunc.type(),
				universe.multiply(arraySliceSizes[0], arrayExtents[0]));

		return universe.arrayLambda(arrayType,
				universe.lambda(symConst, arrayReadFunc));
	}

	private SymbolicExpression arrayReshapeConcrete(SymbolicExpression array,
			ArrayShape originShape, Number[] originExtentNums,
			ArrayShape targetShape, Number[] targetExtentNums) {
		int originExtents[] = new int[originExtentNums.length];
		int targetExtents[] = new int[targetExtentNums.length];

		for (int i = 0; i < originExtents.length; i++)
			originExtents[i] = ((IntegerNumber) originExtentNums[i]).intValue();

		for (int i = 0; i < targetExtents.length; i++)
			targetExtents[i] = ((IntegerNumber) targetExtentNums[i]).intValue();

		// read all elements from origin
		LinkedList<SymbolicExpression> queue = new LinkedList<>();
		int queueSize, dimIter = 1;

		for (int i = 0; i < originExtents[0]; i++)
			queue.add(universe.arrayRead(array, universe.integer(i)));
		queueSize = queue.size();
		while (dimIter < originExtents.length) {
			for (int i = 0; i < queueSize; i++) {
				SymbolicExpression sub = queue.removeFirst();

				for (int j = 0; j < originExtents[dimIter]; j++)
					queue.add(universe.arrayRead(sub, universe.integer(j)));
			}
			dimIter++;
			queueSize = queue.size();
		}

		// build new array using the list of total elements:
		SymbolicType arrayType = targetShape.baseType;

		for (int d = 0; d < targetShape.dimensions; d++) {
			int extent = targetExtents[targetShape.dimensions - 1 - d];
			int numSubArrays = queue.size() / extent;

			for (int j = 0; j < numSubArrays; j++) {
				List<SymbolicExpression> subArraysElements = new LinkedList<>();

				for (int i = 0; i < extent; i++)
					subArraysElements.add(queue.removeFirst());
				queue.add(universe.array(arrayType, subArraysElements));
			}
			arrayType = universe.arrayType(arrayType, universe.integer(extent));
		}
		return queue.removeFirst();
	}

	private SymbolicExpression arrayReshapeLambda(SymbolicExpression array,
			ArrayShape originShape, ArrayShape targetShape) {
		NumericSymbolicConstant lambdaConstants[] = new NumericSymbolicConstant[targetShape.dimensions];

		for (int i = 0; i < lambdaConstants.length; i++)
			lambdaConstants[i] = (NumericSymbolicConstant) universe
					.symbolicConstant(universe.stringObject("i_" + i),
							universe.integerType());

		NumericExpression indices4origin[] = arrayIndiceProjecting(
				lambdaConstants, targetShape, originShape);
		// build array lambda:
		SymbolicType arrayType = targetShape.baseType;
		SymbolicExpression lambdaFunction = array;

		for (int i = 0; i < originShape.dimensions; i++)
			lambdaFunction = universe.arrayRead(lambdaFunction,
					indices4origin[i]);
		for (int i = targetShape.dimensions - 1; i >= 0; i--) {
			arrayType = universe.arrayType(arrayType, targetShape.extents[i]);
			lambdaFunction = universe.lambda(lambdaConstants[i],
					lambdaFunction);
			lambdaFunction = universe.arrayLambda(
					(SymbolicCompleteArrayType) arrayType, lambdaFunction);
		}
		return lambdaFunction;
	}

	@Override
	public NumericExpression[] sliceIndiceProjecting(
			NumericExpression[] fromSliceArrayIndices,
			ArrayShape fromSliceArrayShape,
			NumericExpression[] fromSliceStartIndices,
			ArrayShape toSliceArrayShape,
			NumericExpression[] toSliceStartIndices) {
		// assert fromSliceArrayShape.baseType == toSliceArrayShape.baseType
		assert fromSliceStartIndices.length <= fromSliceArrayShape.dimensions;
		assert toSliceStartIndices.length <= toSliceArrayShape.dimensions;

		NumericExpression pos = universe.zeroInt();
		NumericExpression fromSliceStartOffsets = pos,
				toSliceStartOffsets = pos;

		for (int i = 0; i < fromSliceArrayIndices.length; i++)
			pos = universe.add(pos, universe.multiply(fromSliceArrayIndices[i],
					fromSliceArrayShape.subArraySizes[i]));
		for (int i = 0; i < fromSliceStartIndices.length; i++)
			fromSliceStartOffsets = universe.add(fromSliceStartOffsets,
					universe.multiply(fromSliceStartIndices[i],
							fromSliceArrayShape.subArraySizes[i]));
		pos = universe.subtract(pos, fromSliceStartOffsets);
		for (int i = 0; i < toSliceStartIndices.length; i++)
			toSliceStartOffsets = universe.add(toSliceStartOffsets,
					universe.multiply(toSliceStartIndices[i],
							toSliceArrayShape.subArraySizes[i]));
		pos = universe.add(pos, toSliceStartOffsets);

		NumericExpression projectedIndices[] = new NumericExpression[toSliceArrayShape.dimensions];

		for (int i = 0; i < projectedIndices.length; i++) {
			projectedIndices[i] = universe.divide(pos,
					toSliceArrayShape.subArraySizes[i]);
			pos = universe.modulo(pos, toSliceArrayShape.subArraySizes[i]);
		}
		return projectedIndices;

	}

	@Override
	public NumericExpression[] arrayIndiceProjecting(
			NumericExpression[] fromArrayIndices, ArrayShape fromArrayShape,
			ArrayShape toArrayShape) {
		NumericExpression zero[] = {universe.zeroInt()};

		return this.sliceIndiceProjecting(fromArrayIndices, fromArrayShape,
				zero, toArrayShape, zero);
	}

	/* ********* Testing *********** */
	// public static void main(String args[]) {
	// SymbolicUniverse universe = SARL.newIdealUniverse();
	// NumericSymbolicConstant n, m;
	// SymbolicConstant arr4d, brr4d, brr3d;
	// SymbolicArrayType arrayType1d, arrayType2d, arrayType3d, arrayType4d;
	// SymbolicArrayType brrayType1d, brrayType2d, brrayType3d, brrayType4d;
	// BooleanExpression context;
	//
	// n = (NumericSymbolicConstant) universe.symbolicConstant(
	// universe.stringObject("n"), universe.integerType());
	// m = (NumericSymbolicConstant) universe.symbolicConstant(
	// universe.stringObject("m"), universe.integerType());
	//
	// context = universe.and(universe.lessThan(universe.zeroInt(), n),
	// universe.lessThan(universe.zeroInt(), m));
	//
	// arrayType1d = universe.arrayType(universe.integerType(), n);
	// arrayType2d = universe.arrayType(arrayType1d, m);
	// arrayType3d = universe.arrayType(arrayType2d, universe.integer(4));
	// arrayType4d = universe.arrayType(arrayType3d, universe.integer(3));
	// arr4d = universe.symbolicConstant(universe.stringObject("arr4d"),
	// arrayType4d);
	//
	// brrayType1d = universe.arrayType(universe.integerType(), m);
	// brrayType2d = universe.arrayType(brrayType1d, n);
	// brrayType3d = universe.arrayType(brrayType2d, universe.integer(3));
	// brrayType4d = universe.arrayType(brrayType3d, universe.integer(4));
	// brr4d = universe.symbolicConstant(universe.stringObject("brr4d"),
	// brrayType4d);
	// brr3d = universe.symbolicConstant(universe.stringObject("brr3d"),
	// universe.arrayType(brrayType2d, universe.integer(12)));
	//
	// ArrayReshaper reshaper = new SimpleArrayReshaper(universe);
	// SymbolicExpression flat_arr = reshaper.arrayFlatten(arr4d);
	// SymbolicExpression arr4d_to_brr4d, flat_arr4d_to_brr4d, arr4d_to_brr3d,
	// brr3d_to_arr4d;
	//
	// System.out.println(flat_arr);
	//
	// arr4d_to_brr4d = reshaper.arrayReshape(arr4d,
	// (SymbolicArrayType) brr4d.type());
	//
	// assert reshaper.isPhysicallyEquivalent((SymbolicArrayType) arr4d.type(),
	// (SymbolicArrayType) brr3d.type()).isTrue();
	// assert reshaper.allComplete((SymbolicArrayType) arr4d.type());
	//
	// arr4d_to_brr3d = reshaper.arrayReshape(arr4d,
	// (SymbolicArrayType) brr3d.type());
	// flat_arr4d_to_brr4d = reshaper.arrayReshape(flat_arr,
	// (SymbolicArrayType) brr4d.type());
	//
	// ValidityResult validity = universe.reasoner(universe.trueExpression())
	// .validWhy3(
	// universe.equals(arr4d_to_brr4d, flat_arr4d_to_brr4d));
	//
	// assert validity.getResultType() == ResultType.YES;
	//
	// // arr4d_to_brr3d [0][0][0] == arr4d[0][0][0][0]
	// validity = universe
	// .reasoner(
	// universe.trueExpression())
	// .validWhy3(
	// universe.equals(universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// arr4d_to_brr3d,
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt()), universe
	// .arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// arr4d,
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt())));
	// assert validity.getResultType() == ResultType.YES;
	//
	// // arr4d_to_brr3d [11][n-1][m-1] == arr4d[3][2][m-1][n-1]
	// validity = universe
	// .reasoner(
	// context)
	// .validWhy3(universe.equals(universe.arrayRead(
	// universe.arrayRead(universe.arrayRead(arr4d_to_brr3d,
	// universe.integer(11)),
	// universe.subtract(n, universe.oneInt())),
	// universe.subtract(m,
	// universe.oneInt())),
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(arr4d,
	// universe.integer(2)),
	// universe.integer(3)),
	// universe.subtract(m,
	// universe.oneInt())),
	// universe.subtract(n, universe.oneInt()))));
	//
	// assert validity.getResultType() == ResultType.YES;
	//
	// brr3d_to_arr4d = reshaper.arrayReshape(brr3d,
	// (SymbolicArrayType) arr4d.type());
	//
	// // brr3d_to_arr4d[0][0][0][0] == brr3d[0][0][0]
	// validity = universe.reasoner(universe.trueExpression())
	// .validWhy3(
	// universe.equals(
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// brr3d_to_arr4d,
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(brr3d,
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt())));
	// assert validity.getResultType() == ResultType.YES;
	// // brr3d_to_arr4d[2][3][m-1][n-1] == brr3d[11][n-1][m-1]
	// validity = universe
	// .reasoner(
	// context)
	// .validWhy3(
	// universe.equals(
	// universe.arrayRead(universe
	// .arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// brr3d_to_arr4d,
	// universe.integer(
	// 2)),
	// universe.integer(3)),
	// universe.subtract(m,
	// universe.oneInt())),
	// universe.subtract(n,
	// universe.oneInt())),
	// universe.arrayRead(
	// universe.arrayRead(universe.arrayRead(
	// brr3d, universe.integer(11)),
	// universe.subtract(n,
	// universe.oneInt())),
	// universe.subtract(m,
	// universe.oneInt()))));
	//
	// assert validity.getResultType() == ResultType.YES;
	// // ******** nested reshaping test: **********
	// // *** multi-d to 1-d
	// SymbolicExpression brr3d_to_arr4d_to_1d = reshaper
	// .arrayFlatten(brr3d_to_arr4d);
	// SymbolicExpression brr3d_to_1d = reshaper.arrayFlatten(brr3d);
	//
	// // brr3d_to_4d[0][0][0][0] == brr3d_to_arr4d_to_1d[0]
	// validity = universe.reasoner(context)
	// .validWhy3(
	// universe.equals(
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// brr3d_to_arr4d,
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.arrayRead(brr3d_to_arr4d_to_1d,
	// universe.zeroInt())));
	//
	// assert validity.getResultType() == ResultType.YES;
	//
	// // brr3d_to_4d[0][0][0][1] == brr3d_to_arr4d_to_1d[1]
	// validity = universe.reasoner(context)
	// .valid(universe
	// .equals(universe
	// .arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// brr3d_to_arr4d,
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.zeroInt()),
	// universe.oneInt()),
	// universe.arrayRead(brr3d_to_arr4d_to_1d,
	// universe.oneInt())));
	//
	// assert validity.getResultType() != ResultType.NO;
	//
	// // brr3d_to_4d[2][3][m-1][n-1] == brr3d_to_arr4d_to_1d[12*m*n-1]
	// validity = universe
	// .reasoner(
	// context)
	// .valid(universe
	// .equals(universe
	// .arrayRead(universe
	// .arrayRead(
	// universe.arrayRead(
	// universe.arrayRead(
	// brr3d_to_arr4d,
	// universe.integer(
	// 2)),
	// universe.integer(3)),
	// universe.subtract(m,
	// universe.oneInt())),
	// universe.subtract(n,
	// universe.oneInt())),
	// universe.arrayRead(
	// brr3d_to_arr4d_to_1d, universe
	// .subtract(
	// universe.multiply(
	// Arrays.asList(
	// universe.integer(
	// 12),
	// m, n)),
	// universe.oneInt()))));
	//
	// assert validity.getResultType() != ResultType.NO;
	//
	// validity = universe.reasoner(context)
	// .valid(universe.equals(brr3d_to_arr4d_to_1d, brr3d_to_1d));
	// System.out.println(universe.equals(brr3d_to_arr4d_to_1d, brr3d_to_1d));
	// assert validity.getResultType() != ResultType.NO;
	// }
}
