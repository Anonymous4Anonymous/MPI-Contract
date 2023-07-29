package edu.udel.cis.vsl.abc.ast.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.sarl.SARL;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/*
 * This class provides support for evaluating certain classes of ABC
 * expressions.   It does this by transforming  ABC expressions to SARL 
 * expressions and evaluating those.
 */
public class ExpressionEvaluator {
	// Record mapping from ABC IDs, defined by their Entity, and the translated
	// SARL representation
	private static Map<String, NumericSymbolicConstant> translateID;

	private static SymbolicUniverse universe = SARL.newStandardUniverse();

	public static boolean checkEqualityWithConditions(ExpressionNode o1,
			ExpressionNode o2, List<ExpressionNode> conditions) {
		return checkEqualityWithConditions2(0, o1, 0, o2, conditions);
	}

	public static boolean checkEqualityWithConditions2(Integer oft1,
			ExpressionNode o1, Integer oft2, ExpressionNode o2,
			List<ExpressionNode> conditions) {
		/*
		 * Should check that operator nodes are of an integer type
		 */
		// System.out.println("ExpressionEvaluator: "+o1+", "+o2+",
		// "+conditions);

		translateID = new HashMap<String, NumericSymbolicConstant>();

		NumericExpression n1 = toSarlNumeric(o1);
		NumericExpression n2 = toSarlNumeric(o2);
		BooleanExpression equiv;

		n1 = universe.add(n1, universe.integer(oft1));
		n2 = universe.add(n2, universe.integer(oft2));
		equiv = universe.equals(n1, n2);

		BooleanExpression current = equiv;
		// System.out.println("ExpressionEvaluator Check "+equiv);
		// System.out.println("ExpressionEvaluator Check with Conditions
		// "+current);
		BooleanExpression context = universe.trueExpression();

		for (ExpressionNode e : conditions) {
			context = universe.and(context, (BooleanExpression) toSarlBool(e));
		}

		Reasoner reasoner = universe.reasoner(context);

		return reasoner.isValid(current);
	}

	public static boolean checkInequalityWithConditions(ExpressionNode o1,
			ExpressionNode o2, List<ExpressionNode> conditions) {
		/*
		 * Should check that operator nodes are of an integer type
		 */
		// System.out.println("ExpressionEvaluator: "+o1+", "+o2+",
		// "+conditions);

		translateID = new HashMap<String, NumericSymbolicConstant>();

		NumericExpression n1 = toSarlNumeric(o1);
		NumericExpression n2 = toSarlNumeric(o2);
		BooleanExpression equiv = universe.neq(n1, n2);

		BooleanExpression current = equiv;
		return current.isTrue();
	}

	/**
	 * Given two functions f and f', returns true if and only if <code>
	 * f(x, y, z, ...) != f'(x', y', z', ...) if
	 * x != x' && y != y' && ...
	 * </code> and if <code>safeLen</code> is not null, add the assumption
	 * implied by safeLen.
	 */
	public static boolean checkFunctionDisagrement(Integer oft0,
			ExpressionNode func0, Integer oft1, ExpressionNode func1,
			Set<Variable> inputs, Set<Variable> threadVars, Integer safeLen,
			List<ExpressionNode> assumptions) {
		translateID = new HashMap<String, NumericSymbolicConstant>();
		NumericExpression symFunc0 = func0 == null
				? universe.zeroInt()
				: toSarlNumeric(func0);
		NumericExpression symFunc1 = func1 == null
				? universe.zeroInt()
				: toSarlNumeric(func1);
		Map<SymbolicExpression, SymbolicExpression> symInputSub = new HashMap<>();
		BooleanExpression assump = universe.trueExpression();

		symFunc0 = universe.add(symFunc0, universe.integer(oft0));
		symFunc1 = universe.add(symFunc1, universe.integer(oft1));
		for (Variable var : inputs) {
			NumericExpression symIn = translateID.get(var.getName());
			NumericExpression symInPrime;

			symInPrime = (NumericExpression) universe.symbolicConstant(
					universe.stringObject("_X_" + var.getName()), symIn.type());
			symInputSub.put(symIn, symInPrime);
			assump = universe.and(assump, safeLenAssump(symIn, symInPrime,
					safeLen, threadVars.contains(var)));
		}

		NumericExpression symFunc1Prime = (NumericExpression) universe
				.mapSubstituter(symInputSub).apply(symFunc1);

		// symFunc != symFuncPrime -> inputs != inputsPrime &&
		// symFunc != symFuncPrime <- inputs != inputsPrime
		BooleanExpression funcNeq = universe.neq(symFunc0, symFunc1Prime);

		if (funcNeq.isTrue())
			return true;
		else if (funcNeq.isFalse())
			return false;

		Reasoner reasoner = universe.reasoner(assump);

		return reasoner.isValid(funcNeq);
	}

	private static BooleanExpression safeLenAssump(NumericExpression thv,
			NumericExpression thvPrime, Integer safeLen, boolean isThreadVar) {
		BooleanExpression ret = universe.neq(thv, thvPrime);

		if (safeLen == null || !isThreadVar)
			return ret;

		NumericExpression diff0 = universe.subtract(thv, thvPrime);
		NumericExpression diff1 = universe.subtract(thvPrime, thv);
		BooleanExpression assump0 = universe.lessThanEquals(universe.zeroInt(),
				diff0);
		BooleanExpression assump1 = universe.lessThanEquals(universe.zeroInt(),
				diff1);

		assump0 = universe.and(assump0,
				universe.lessThan(diff0, universe.integer(safeLen)));
		assump1 = universe.and(assump1,
				universe.lessThan(diff1, universe.integer(safeLen)));
		ret = universe.and(ret, universe.or(assump0, assump1));
		return ret;
	}

	/*
	 * Visit the operator node and convert it to a SARL expression. The
	 * following are not supported in operator nodes: - function calls - array
	 * references
	 */

	private static BooleanExpression toSarlBool(ExpressionNode o) {
		if (o instanceof OperatorNode) {
			OperatorNode op = (OperatorNode) o;
			/*
			 * Works with basic logical and relational operators now. Could be
			 * extended to handle arrays, etc. (not sure how well that will
			 * work, but ...)
			 */
			OperatorNode.Operator oper = op.getOperator();
			if (oper == OperatorNode.Operator.NEQ) {
				return universe.not(toSarlBool(op.getArgument(1)));
			} else if (oper == OperatorNode.Operator.LAND
					|| oper == OperatorNode.Operator.LOR) {
				BooleanExpression op1 = toSarlBool(op.getArgument(0));
				BooleanExpression op2 = toSarlBool(op.getArgument(1));
				switch (oper) {
					case LAND :
						return universe.and(op1, op2);
					case LOR :
						return universe.or(op1, op2);
					default :
						assert false : "ExpressionEvaluator : cannot translate "
								+ oper + " to SARL";
				}
			} else {
				NumericExpression op1 = toSarlNumeric(op.getArgument(0));
				NumericExpression op2 = toSarlNumeric(op.getArgument(1));

				switch (oper) {
					case LT :
						return universe.lessThan(op1, op2);
					case LTE :
						return universe.lessThanEquals(op1, op2);
					case GT :
						return universe.not(universe.lessThanEquals(op1, op2));
					case GTE :
						return universe.not(universe.lessThan(op1, op2));
					case EQUALS :
						return universe.equals(op1, op2);
					default :
						assert false : "ExpressionEvaluator : cannot translate "
								+ oper + " to SARL";
				}
			}

		} else {
			assert false : "ExpressionEvaluator : cannot translate " + o
					+ " to SARL";
		}
		return null;
	}

	private static NumericExpression toSarlNumeric(ExpressionNode o) {
		if (o instanceof OperatorNode) {
			OperatorNode op = (OperatorNode) o;

			/*
			 * Works with basic integer operators now. Could be extended to
			 * handle arrays, etc. (not sure how well that will work, but ...)
			 */
			NumericExpression op1 = (NumericExpression) toSarlNumeric(
					op.getArgument(0));
			OperatorNode.Operator oper = op.getOperator();
			if (oper == OperatorNode.Operator.UNARYPLUS) {
				return op1;
			} else if (oper == OperatorNode.Operator.UNARYMINUS) {
				return universe.minus(op1);
			} else if (oper == OperatorNode.Operator.SUBSCRIPT) {
				/*
				 * Handle the case where this expression is an array reference
				 * expression. We use uninterpreted functions for this where the
				 * name of the function is "subscript" and we have parameters
				 * for both the base array and index expressions. This should
				 * work for multi-dimensional arrays.
				 */
				return universe.divide(op1, op1); // TBD change second operand
													// and replace this with
													// subscript
			} else {
				NumericExpression op2 = (NumericExpression) toSarlNumeric(
						op.getArgument(1));
				switch (oper) {
					case DIV :
						return universe.divide(op1, op2);
					case MINUS :
						return universe.subtract(op1, op2);
					case MOD :
						return universe.modulo(op1, op2);
					case PLUS :
						return universe.add(op1, op2);
					case TIMES :
						return universe.multiply(op1, op2);
					default :
						assert false : "ExpressionEvaluator : cannot translate "
								+ oper + " to SARL";
				}
			}

		} else if (o instanceof IntegerConstantNode) {
			return universe.integer(((IntegerConstantNode) o).getConstantValue()
					.getIntegerValue());

		} else if (o instanceof IdentifierExpressionNode) {
			String idName = ((IdentifierExpressionNode) o).getIdentifier()
					.name();
			if (translateID.containsKey(idName)) {
				return translateID.get(idName);
			} else {
				NumericSymbolicConstant idSarl = (NumericSymbolicConstant) universe
						.symbolicConstant(universe.stringObject(idName),
								universe.integerType());
				translateID.put(idName, idSarl);
				return idSarl;
			}
		} else {
			assert false : "ExpressionEvaluator : cannot translate " + o
					+ " to SARL";
		}
		return null;
	}

}
