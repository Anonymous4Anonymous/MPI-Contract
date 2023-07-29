package edu.udel.cis.vsl.sarl.reason.common;

import java.util.Stack;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicCompleteArrayType;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

/**
 * <p>
 * Briefly, this transformer canonicalizes all bound variable names of the
 * lambda expressions in array lambdas.
 * </p>
 * 
 * @author xxxx
 *
 */
public class ArrayLambdaCanonicalization extends ExpressionVisitor
		implements UnaryOperator<SymbolicExpression> {

	private PreUniverse universe;

	/**
	 * the canonicalized bound variable name prefix. The suffix of a bound
	 * variable name depends on the dimensions of the array lambda.
	 */
	private static final String canonicalBoundVarNameRoot = "_cano_arr_bv_";

	/**
	 * a stack of bound variables. During visiting an array lambda expression, a
	 * stack of bound variables will be maintain. An entry will be pushed if a
	 * sub-array (array is a special case of a sub-array) is an array lambda.
	 * 
	 * <p>
	 * As mentioned in {@link #canonicalBoundVarNameRoot}, the suffix of a
	 * canonicalized bound variable name is depending on its position in this
	 * stack.
	 * </p>
	 */
	private Stack<SymbolicConstant> boundVars;

	ArrayLambdaCanonicalization(PreUniverse universe) {
		super(universe);
		this.universe = universe;
		this.boundVars = new Stack<>();
	}

	@Override
	public SymbolicExpression apply(SymbolicExpression x) {
		return visitExpression(x);
	}

	@Override
	SymbolicExpression visitExpression(SymbolicExpression expr) {
		if (expr.operator() == SymbolicOperator.ARRAY_LAMBDA) {
			SymbolicExpression lambda = (SymbolicExpression) expr.argument(0);
			SymbolicConstant boundVar = (SymbolicConstant) lambda.argument(0);
			SymbolicExpression function = (SymbolicExpression) lambda
					.argument(1);

			boundVars.push(boundVar);
			visitExpressionChildren(function);
			boundVars.pop();
			// rename the bound var:
			if (boundVar.name().getString()
					.startsWith(canonicalBoundVarNameRoot))
				throw new SARLException(
						"There is a conflict in between internal artificial constant name "
								+ "and an exsiting name in the context : "
								+ boundVar.name());

			String canonicalizedBoundVarName = canonicalBoundVarNameRoot
					+ boundVars.size();
			SymbolicConstant newBoundVar = universe.symbolicConstant(
					universe.stringObject(canonicalizedBoundVarName),
					boundVar.type());
			SymbolicExpression newFunction = universe
					.simpleSubstituter(boundVar, newBoundVar)
					.apply((SymbolicExpression) function);

			return universe.arrayLambda((SymbolicCompleteArrayType) expr.type(),
					universe.lambda(newBoundVar, newFunction));
		} else
			expr = visitExpressionChildren(expr);
		return expr;
	}
}
