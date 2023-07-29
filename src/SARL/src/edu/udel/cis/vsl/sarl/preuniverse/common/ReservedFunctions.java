package edu.udel.cis.vsl.sarl.preuniverse.common;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.expr.IF.ExpressionFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

public class ReservedFunctions {

	static private String permut = "permut";

	/**
	 * The name of a fold expression: Sigma. A sigma expression represents a
	 * summation: Sigma(x, y, function(int)).
	 * <code>Sigma(x, y, function(int)) = function(x) + function(x+1) + ... function(y)</code>
	 */
	static private String sigma = "sigma";

	static Set<String> ReservedFunctionName = new HashSet<>(
			Arrays.asList("$sigma", "$permut"));

	/**
	 * @param functionName
	 *            a function name
	 * @return true iff this function name is reserved and hence cannot be used
	 *         as a function name.
	 */
	static boolean isNameReserved(String functionName) {
		return ReservedFunctionName.contains(functionName);

	}

	/**
	 * <code>$permut(elementType[], elementType[], int, int)</code>
	 * 
	 * @param pu
	 * @param elementType
	 * @return
	 */
	static SymbolicConstant permutation(PreUniverse pu, ExpressionFactory ef,
			SymbolicType elementType) {
		SymbolicType arrayType = pu.arrayType(elementType);
		SymbolicFunctionType funcType = pu.functionType(Arrays.asList(arrayType,
				arrayType, pu.integerType(), pu.integerType()),
				pu.booleanType());

		return ef.symbolicConstant(pu.stringObject(permut), funcType);
	}

	/**
	 * 
	 * @param expr
	 *            a symbolic expression
	 * @return true iff the given symbolic expression "expr" is a call to
	 *         {@link #sigma(PreUniverse, SymbolicFunctionType)}
	 */
	static boolean isPermutCall(SymbolicExpression expr) {
		return isReservedFunctionCall(expr, permut);
	}

	/**
	 * <code>$sigma(int, int, function)</code>
	 * 
	 * @param pu
	 * @param funcType
	 * @return
	 */
	static SymbolicConstant sigma(PreUniverse pu, ExpressionFactory ef,
			SymbolicFunctionType funcType) {
		SymbolicFunctionType sigmaType = pu.functionType(
				Arrays.asList(pu.integerType(), pu.integerType(), funcType),
				funcType.outputType());

		return ef.symbolicConstant(pu.stringObject(sigma), sigmaType);
	}

	/**
	 * 
	 * @param expr
	 *            a symbolic expression
	 * @return true iff the given symbolic expression "expr" is a call to
	 *         {@link #sigma(PreUniverse, SymbolicFunctionType)}
	 */
	static boolean isSigmaCall(SymbolicExpression expr) {
		return isReservedFunctionCall(expr, sigma);
	}

	static private boolean isReservedFunctionCall(SymbolicExpression expr,
			String reservedFunctionName) {
		if (expr.operator() == SymbolicOperator.APPLY) {
			SymbolicExpression function = (SymbolicExpression) expr.argument(0);

			// Uninterpreted function is a symbolic constant:
			if (function.operator() == SymbolicOperator.SYMBOLIC_CONSTANT) {
				StringObject functionName = (StringObject) function.argument(0);

				return functionName.getString().equals(reservedFunctionName);
			}
			return false;
		}
		return false;
	}
}
