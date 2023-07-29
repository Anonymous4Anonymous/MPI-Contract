package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

/**
 * <p>
 * This class produces {@link InsensitiveFlow}s, {@link AssignmentIF}s as well
 * as {@link AssignExprIF}s
 * </p>
 * 
 * <p>
 * Remark that {@link AssignExprIF}s produced by an instance of this class are
 * all canonicalized. Comparison between {@link AssignExprIF}s produced by
 * different instances of this class is undefined.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface InsensitiveFlowFactory {

	/**
	 * <p>
	 * Creates a new {@link InsensitiveFlow} of the function body for a call
	 * instance of a function.
	 * </p>
	 * 
	 * @param function
	 *            a {@link Function} entity that must be defined with a function
	 *            body
	 * @param igNode
	 *            the {@link InvocationGraphNode} associated with a call
	 *            instance to the given function
	 * @return the generated insensitive flow representation
	 */
	InsensitiveFlow InsensitiveFlow(Function function,
			InvocationGraphNode igNode);

	/**
	 * <p>
	 * creates a new instance of {@link AssignmentIF}
	 * </p>
	 * 
	 * @param lhs
	 *            the {@link AssignExprIF} of the left-hand side of the
	 *            assignment
	 * @param lhsDeref
	 *            Is the left-hand side dereferenced ?
	 * @param rhs
	 *            lhs the {@link AssignExprIF} of the right-hand side of the
	 *            assignment
	 * @param rhsDeref
	 *            Is the right-hand side dereferenced ?
	 * @param rhsAddrof
	 *            Does the right-hand side be taken address-of ?
	 * @return
	 */
	AssignmentIF assignment(AssignExprIF lhs, boolean lhsDeref,
			AssignExprIF rhs, boolean rhsDeref, boolean rhsAddrof);

	/**
	 * creates an abstract object representing an allocation or a string literal
	 * 
	 * @param store
	 *            an allocation (malloc call) or a string literal (string
	 *            literal constant)
	 */
	AssignStoreExprIF assignStoreExpr(ExpressionNode store);

	/**
	 * creates an abstract object representing a variable
	 * 
	 * @param store
	 *            a variable
	 */
	AssignStoreExprIF assignStoreExpr(Variable store);

	/**
	 * creates an abstract object representing a struct/union field of another
	 * abstract object
	 * 
	 * @param struct
	 *            an abstract object
	 * @param field
	 *            a {@link Field}
	 */
	AssignFieldExprIF assignFieldExpr(AssignExprIF struct, Field field);

	/**
	 * creates an abstract object representing an array element of another
	 * abstract object
	 * 
	 * @param array
	 *            an abstract object
	 * @param index
	 *            an instance of {@link AssignOffsetIF} representing the index
	 */
	AssignSubscriptExprIF assignSubscriptExpr(AssignExprIF array,
			AssignOffsetIF index);

	/**
	 * creates an abstract object representing a pointer with an offset
	 * 
	 * @param array
	 *            an abstract object
	 * @param index
	 *            an instance of {@link AssignOffsetIF} representing the offset
	 */
	AssignOffsetExprIF assignOffsetExpr(AssignExprIF base,
			AssignOffsetIF offset);

	/**
	 * creates a typed auxiliary abstract object
	 * 
	 * @param type
	 *            the type of the creating auxiliary abstract object
	 */
	AssignAuxExprIF assignAuxExpr(Type type);

	/**
	 * <p>
	 * creates an {@link AssignOffsetIF} from an ExpressionNode of integer type
	 * </p>
	 * <p>
	 * Whether the created integral parameter represents a constant integer
	 * depends on whether the given expression node has a constant value.
	 * </p>
	 * 
	 * @param offset
	 *            an {@link ExpressionNode} of integer type
	 * @param positive
	 *            whether the given "offset" argument is positive or negative
	 */
	AssignOffsetIF assignOffset(ExpressionNode offset, boolean positive);

	/**
	 * <p>
	 * creates an {@link AssignOffsetIF} represents constant integral zero.
	 * </p>
	 * 
	 */
	AssignOffsetIF assignOffsetZero();

	/**
	 * <p>
	 * creates an {@link AssignOffsetIF} represents the given constant integer.
	 * </p>
	 */
	AssignOffsetIF assignOffset(Integer val);

	/**
	 * 
	 * @return an instance representing an arbitrary offset
	 */
	AssignOffsetIF assignOffsetWild();

	/**
	 * <p>
	 * creates an abstract object representing a pointer to any possible object
	 * </p>
	 */
	AssignExprIF full();
}
