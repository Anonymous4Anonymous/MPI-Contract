package edu.udel.cis.vsl.abc.ast.conversion.common;

import edu.udel.cis.vsl.abc.ast.conversion.IF.MemConversion;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ArrowNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CastNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.DotNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.type.IF.SetType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

/**
 * This class provides a method {@link #check(ExpressionNode)} to do the check
 * of the restrictions mentioned in {@link MemConversion}:
 * 
 * <p>
 * Given an expression <code>e</code> that will be converted to have $mem type,
 * the following restrictions will be applied to <code>e</code>:
 * 
 * <ul>
 * <li>If e has the form: <code>&(*p)</code>, apply the rest of the restrictions
 * on <code>p</code>; Or if e has the form: <code>&(p[e'])</code> and
 * <code>p</code> has pointer type, apply the rest of the restrictions on
 * <code>p</code>.</li>
 * 
 * <li><code>e</code> and any sub-expressions of <code>e</code> must not be any
 * of these form: <code>*p, p[e'], p->id</code> where <code>p</code> has the
 * type of a set-of pointers (<b>no pointer set dereference rule</b>)</li>
 * 
 * <li>If <code>e</code> and any true sub-expressions of <code>e</code> has type
 * of a set-of pointers, it can only either be one of the two following cases:
 * (<b>pointer set has one base address rule</b>)
 * <ul>
 * <li><code>&lhs</code>, where <code>lhs</code> has set type</li>
 * <li><code>e' + I</code>, where <code>e'</code> is a (set-of) pointer type
 * expression and <code>I</code> is an expression of integer or range type</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * @author xxxx
 *
 */
public class MemConversionRestriction {

	/**
	 * check restrictions on the given expression "e", which will be converted
	 * to have $mem type. Restrictions are defined in
	 * {@link MemConversionRestriction}
	 * 
	 * @param e
	 *            an expression that will be converted to have $mem type
	 * 
	 * @throws SyntaxException
	 *             when any restriction is violated
	 */
	public static void check(ExpressionNode e) throws SyntaxException {
		e = cancelAddressofDereference(decast(e));
		checkWorker(e);
	}

	/**
	 * <p>
	 * checks if a set-of pointer type expression belongs to one of the
	 * following forms:
	 * <ul>
	 * <li><code>P + I</code> while <code>P</code> has (set-of) pointer type and
	 * <code>I</code> is either an integer or a range</li>
	 * <li><code>&LHS</code> where <code>LHS</code> is a left-hand side
	 * expression and {@link #findAndCheckPointerSetInLHS(ExpressionNode)} on
	 * <code>LHS</code> has been executed.</li>
	 * <li>an identifier</li>
	 * </ul>
	 * </p>
	 * 
	 * @param e
	 *            an expression has either pointer or set-of pointer type
	 * @throws SyntaxException
	 */
	private static void checkWorker(ExpressionNode e) throws SyntaxException {
		if (e.getType().kind() != TypeKind.SET)
			// identifier falls into this case:
			return;
		e = decast(e);
		if (e.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode op = (OperatorNode) e;

			if (op.getOperator() == Operator.ADDRESSOF) {
				findAndCheckPointerSetInLHS(op.getArgument(0));
				return;
			}
			if (op.getOperator() == Operator.PLUS) {
				ExpressionNode arg0 = op.getArgument(0);
				ExpressionNode arg1 = op.getArgument(1);
				Type type0 = arg0.getType();
				Type type1 = arg1.getType();

				if (type0.kind() == TypeKind.SET)
					type0 = ((SetType) type0).elementType();
				if (type0.kind() == TypeKind.POINTER) {
					// arg1 is integer or range:
					if (type1.kind() == TypeKind.RANGE
							|| type1.kind() != TypeKind.SET) {
						checkWorker(arg0);
						return;
					}
				} else {
					// arg0 is integer or range:
					if (type0.kind() == TypeKind.RANGE
							|| type0.kind() != TypeKind.SET) {
						checkWorker(arg1);
						return;
					}
				}
			}
		}
		throw new SyntaxException(e.prettyRepresentation()
				+ " is not allowed to be an sub-expression of an expression that will be converted to have $mem type",
				e.getSource());
	}

	/**
	 * <p>
	 * finds any set-of pointer type sub-expression <code>e</code> in the given
	 * left-hand side expression and calls {@link #checkWorker(ExpressionNode)}
	 * on <code>e</code>.
	 * </p>
	 * 
	 * @param lhs
	 * @throws SyntaxException
	 */
	private static void findAndCheckPointerSetInLHS(ExpressionNode lhs)
			throws SyntaxException {
		lhs = decast(lhs);
		if (lhs.expressionKind() == ExpressionKind.ARROW) {
			ArrowNode arrow = (ArrowNode) lhs;

			if (arrow.getType().kind() == TypeKind.SET)
				throw new SyntaxException(arrow.prettyRepresentation()
						+ " is not allowed because "
						+ arrow.getStructurePointer().prettyRepresentation()
						+ " has set type", arrow.getSource());
			return;
		}
		if (lhs.expressionKind() == ExpressionKind.DOT) {
			DotNode dot = (DotNode) lhs;

			findAndCheckPointerSetInLHS(dot.getStructure());
			return;
		}
		if (lhs.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode op = (OperatorNode) lhs;

			if (op.getOperator() == Operator.DEREFERENCE)
				if (op.getType().kind() != TypeKind.SET)
					return;
			if (op.getOperator() == Operator.SUBSCRIPT) {
				ExpressionNode arrOrPtr = op.getArgument(0);
				Type type = arrOrPtr.getType();

				if (type.kind() == TypeKind.SET) {
					type = ((SetType) type).elementType();
					if (type.kind() != TypeKind.POINTER) {
						findAndCheckPointerSetInLHS(arrOrPtr);
						return;
					}
				} else
					return;
			}
		}
		throw new SyntaxException(lhs.prettyRepresentation()
				+ " is not allowed to be an sub-expression of an expression that will be converted to have $mem type",
				lhs.getSource());
	}

	/**
	 * 
	 * Simplify 1) <code>&(*p)</code> to <code>p</code> <br>
	 * 2) <code>&(a[I])</code> to <code>a + I</code> iff <code>a</code> have
	 * pointer type
	 */
	private static ExpressionNode cancelAddressofDereference(ExpressionNode e) {
		if (e.expressionKind() != ExpressionKind.OPERATOR)
			return e;
		if (((OperatorNode) e).getOperator() != Operator.ADDRESSOF)
			return e;

		ExpressionNode arg = ((OperatorNode) e).getArgument(0);

		arg = decast(arg);
		if (arg.expressionKind() != ExpressionKind.OPERATOR)
			return e;
		if (((OperatorNode) arg).getOperator() == Operator.DEREFERENCE)
			return ((OperatorNode) arg).getArgument(0);
		if (((OperatorNode) arg).getOperator() == Operator.SUBSCRIPT) {
			ExpressionNode ptr = ((OperatorNode) arg).getArgument(0);
			Type type = ptr.getType();

			if (type.kind() == TypeKind.POINTER)
				return ptr;
		}
		return e;
	}

	private static ExpressionNode decast(ExpressionNode e) {
		if (e.expressionKind() == ExpressionKind.CAST)
			return ((CastNode) e).getArgument();
		return e;
	}
}
