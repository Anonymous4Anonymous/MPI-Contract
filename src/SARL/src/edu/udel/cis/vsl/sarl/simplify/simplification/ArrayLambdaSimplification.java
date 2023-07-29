package edu.udel.cis.vsl.sarl.simplify.simplification;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.expr.IF.BooleanExpressionFactory;
import edu.udel.cis.vsl.sarl.ideal.IF.IdealFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.simplify.simplifier.IdealSimplifierWorker;
import edu.udel.cis.vsl.sarl.simplify.simplifier.SubContext;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;

public class ArrayLambdaSimplification extends Simplification {

	public ArrayLambdaSimplification(IdealSimplifierWorker worker) {
		super(worker);
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression expr) {
		assert expr.operator() == SymbolicOperator.ARRAY_LAMBDA;
		SymbolicArrayType arrayType = (SymbolicArrayType) expr.type();
		SymbolicExpression function = (SymbolicExpression) expr.argument(0);
		BooleanExpressionFactory bf = info().getBooleanFactory();
		IdealFactory idf = idealFactory();
		SymbolicTypeFactory tf = idf.typeFactory();
		PreUniverse uv = universe();

		if (arrayType.isComplete()) {
			SymbolicCompleteArrayType completeType = (SymbolicCompleteArrayType) arrayType;
			SymbolicCompleteArrayType newCompleteType = (SymbolicCompleteArrayType) simplifyType(
					arrayType);
			NumericExpression length = newCompleteType.extent();
			// function : [0,length-1] -> elementType
			// when simplifying function, can assume domain is [0,length-1].
			// create temporary symbolic constant i
			// create sub-context, add assumption 0<=i<length
			// simplify (APPLY function i) in this context, yielding g.
			// result is lambda(i).g.
			// small optimization: if function is a lambda expression, just
			// use the existing bound variable of that lambda expression,
			// no need to create a new one
			if (function.operator() == SymbolicOperator.LAMBDA) {
				NumericSymbolicConstant boundVar = (NumericSymbolicConstant) function
						.argument(0);
				SymbolicExpression body = (SymbolicExpression) function
						.argument(1);
				BooleanExpression boundAssumption = bf.and(
						idf.lessThanEquals(idf.zeroInt(), boundVar),
						idf.lessThan(boundVar, length));
				SubContext subContext = newSubContext(boundAssumption);
				SymbolicExpression newBody = subContext.simplify(body);

				if (newBody == body && newCompleteType == completeType)
					return expr;

				SymbolicFunctionType functionType = (SymbolicFunctionType) function
						.type();
				SymbolicFunctionType newFunctionType = tf.functionType(
						functionType.inputTypes(), newBody.type());
				SymbolicExpression newFunction = uv.make(
						SymbolicOperator.LAMBDA, newFunctionType,
						new SymbolicObject[] { boundVar, newBody });
				SymbolicExpression result = uv.make(
						SymbolicOperator.ARRAY_LAMBDA, newCompleteType,
						new SymbolicObject[] { newFunction });

				return result;
			} else {
				// TODO: need a fresh bound variable
			}
		} else {
			// TODO: incomplete array type. // Still know i>=0.
		}
		return genericSimplify(expr);
		// return expr;
	}

	@Override
	public SimplificationKind kind() {
		return SimplificationKind.ARRAY_LAMBDA;
	}
}
