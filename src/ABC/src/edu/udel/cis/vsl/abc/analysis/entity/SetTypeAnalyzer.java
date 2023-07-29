package edu.udel.cis.vsl.abc.analysis.entity;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.type.IF.ArithmeticType;
import edu.udel.cis.vsl.abc.ast.type.IF.ArrayType;
import edu.udel.cis.vsl.abc.ast.type.IF.IntegerType;
import edu.udel.cis.vsl.abc.ast.type.IF.ObjectType;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.SetType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardSignedIntegerType.SignedIntKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.TypeFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

/**
 * <p>
 * This class provides static methods for helping analyzing expressions (in
 * {@link ExpressionAnalyzer}) of {@link SetType}.
 * </p>
 * 
 * 
 * <p>
 * The typing rules of expressions that may have {@link SetType} are defined as
 * follows (<code>set</code> denotes an expression of set type; <code>e</code>
 * denotes an expression of any type; for brevity {@link RangeType} here is
 * considered as a {@link SetType}):
 * <ul>
 * <li><code>set->id</code>: if <code>set</code> has set type with element of
 * struct type "s *", <code>set->id</code> has set type whose element type is
 * decided by "s.id".</li>
 * 
 * <li><code>set.id</code>: if <code>set</code> has set type with element of
 * struct type "s", <code>set.id</code> has set type whose element type is
 * decided by "s.id".</li>
 * 
 * <li><code>*set</code>: if <code>set</code> has set type with element of "T *"
 * type and "T" is not void, <code>*set</code> has set type whose element type
 * is "T".</li>
 * 
 * <li><code>&set</code>: if <code>set</code> has set type with element of "T"
 * type, <code>&set</code> has set type with element of "T *" type.</li>
 *
 * <li><code>set[e]</code>: if <code>set</code> has set type with element of
 * array type "T a[]" or pointer type "T *" and <code>e</code> has integer type
 * or set type with integer element type, <code>set[e]</code> has set type with
 * element type of "T".</li>
 * 
 * 
 * <li><code>e[set]</code>: if <code>set</code> has set type with element of
 * integer and <code>e</code> has array type "T a[]" or pointer type "T *",
 * <code>e[set]</code> has set type with element type of "T"</li>
 * 
 * <li><code>set + e</code> (or <code>e + set</code>): if <code>set</code> has
 * set type with element of pointer type "T *" and <code>e</code> has integer
 * type or set type with element of integer type, <code>set + e</code> has set
 * type with element of pointer type "T *"; if <code>set</code> has set type
 * with element of integer type and <code>e</code> has integer type or set type
 * with element of integer type, <code>set + e</code> has set type with element
 * of integer type.</li>
 * </ul>
 * </p>
 * 
 * @author xxxx
 */
class SetTypeAnalyzer {

	/* ******************** process ADDRESSOF ******************** */
	/**
	 * <p>
	 * process an ADDRESSOF operation which may have {@link SetType}: if the
	 * sole operand has set of "T" type, the operation has set of "T *" type.
	 * </p>
	 * <p>
	 * <b>pre-condition:</b> the operand is an lvalue expression
	 * </p>
	 * 
	 * @param primaryAnalyzer
	 *            a reference to the {@link ExpressionAnalyzer}
	 * @param arg0
	 *            the operand of the ADDRESSOF operation
	 * @param node
	 *            the ADDRESSOF operation node
	 * @return true iff the operation node has been set to set of pointer type
	 */
	static boolean processSetTypeForADDRESSOF(
			ExpressionAnalyzer primaryAnalyzer, ExpressionNode arg0,
			OperatorNode node) {
		if (arg0.getType().kind() == TypeKind.SET) {
			Type elementType = ((SetType) arg0.getType()).elementType();
			TypeFactory tf = primaryAnalyzer.typeFactory;

			node.setInitialType(tf.theSetType(tf.pointerType(elementType)));
			return true;
		}
		return false;
	}
	/* ******************** process SUBSCRIPT ******************** */
	/**
	 * <p>
	 * process SUBSCRIPT operation if at least one of its operand has set type
	 * </p>
	 * 
	 * @param primaryAnalyzer
	 *            a reference to the {@link ExpressionAnalyzer}
	 * @param arg0
	 *            the operand of the SUBSCRIPT operation
	 * @param arg1
	 *            the operand of the SUBSCRIPT operation
	 * @param node
	 *            the SUBSCRIPT operation node
	 * @return true iff the node has been set to set of "T" type where "T" is
	 *         the referred type of "arg0" if "arg0" has pointer type or "T" is
	 *         the element type of "arg0" if "arg0" has an array type; false
	 *         otherwise
	 * @throws SyntaxException
	 *             if the first operand does not have (set of) pointer to
	 *             complete object type or (set of) array type, or the second
	 *             argument does not have integer or set of integer type
	 */
	static boolean processSetTypeForSUBSCRIPT(
			ExpressionAnalyzer primaryAnalyzer, ExpressionNode arg0,
			ExpressionNode arg1, OperatorNode node) throws SyntaxException {
		Type type0 = arg0.getType();
		Type type1 = arg1.getType();

		if (type0.kind() == TypeKind.SET || type1.kind() == TypeKind.SET
				|| type1.kind() == TypeKind.RANGE) {
			ObjectType elementType;

			// process SUBSCRIPT expression "A[I]" of set type:
			// if "A" has set type:
			if (type0.kind() == TypeKind.SET) {
				type0 = ((SetType) type0).elementType();
				elementType = pointerReferredTypeORArrayElementType(
						primaryAnalyzer, type0);
			} else
				elementType = pointerReferredTypeORArrayElementType(
						primaryAnalyzer, type0);

			if (elementType == null)
				throw primaryAnalyzer.error(
						"First argument to subscript operator not pointer to complete object type:\n",
						node);

			// if "I" has set type:
			if (type1.kind() == TypeKind.SET) {
				type1 = ((SetType) type1).elementType();
				if (!(type1 instanceof IntegerType))
					throw primaryAnalyzer.error("The term "
							+ arg1.prettyRepresentation() + " in expression "
							+ node.prettyRepresentation()
							+ " shall have integer type or set of integer types",
							node);
			}
			node.setInitialType(
					primaryAnalyzer.typeFactory.theSetType(elementType));
			return true;
		}
		return false;
	}

	/**
	 * Attempt to get the type of a SUBSCRIPT operation <code>a[i]</code> from
	 * the type of its "left-hand side" operand <code>a</code>. The type of
	 * <code>a</code> can only be either a pointer to a complete object type or
	 * an array type. If the type of <code>a</code> is neither the
	 * aforementioned two, return <code>null</code>.
	 * 
	 * @param primaryAnalyzer
	 *            a reference to the {@link ExpressionAnalyzer}
	 * @param type
	 *            the type of the "left-hand side" operand <code>a</code> in a
	 *            SUBSCRIPT operation <code>a[i]</code>
	 * @return the referred type of <code>a</code> if <code>a</code> has pointer
	 *         type; or the element of <code>a</code> if <code>a</code> has
	 *         array type; or <code>null</code> otherwise.
	 */
	static private ObjectType pointerReferredTypeORArrayElementType(
			ExpressionAnalyzer primaryAnalyzer, Type type) {
		if (primaryAnalyzer.isPointerToCompleteObjectType(type))
			return (ObjectType) ((PointerType) type).referencedType();
		else if (type.kind() == TypeKind.ARRAY)
			return ((ArrayType) type).getElementType();
		return null;
	}

	/* ******************** process DEREFERENCE ******************** */
	/**
	 * <p>
	 * process DEREFERENCE operation if the operand has set of pointer type
	 * </p>
	 * 
	 * @param primaryAnalyzer
	 *            a reference to the {@link ExpressionAnalyzer}
	 * @param arg
	 *            the operand of the DEREFERENCE operation
	 * @param node
	 *            the DEREFERENCE operation node
	 * @return true iff the operand has set of "T *" type and the operation node
	 *         has been set of "T" type
	 * @throws SyntaxException
	 *             if the operand has set of non-pointer type
	 */
	static boolean processSetTypeForDEREFERENCE(
			ExpressionAnalyzer primaryAnalyzer, ExpressionNode arg,
			OperatorNode node) throws SyntaxException {
		Type type = arg.getType();

		if (arg.getType().kind() == TypeKind.SET) {
			type = ((SetType) type).elementType();
			if (!primaryAnalyzer.isPointerToCompleteObjectType(type))
				throw primaryAnalyzer.error(
						"Argument to * has non-pointer type: " + type, arg);
			node.setInitialType(primaryAnalyzer.typeFactory.theSetType(
					(ObjectType) ((PointerType) type).referencedType()));
			return true;
		}
		return false;
	}

	/* ******************** process PLUS *************************** */
	/**
	 * <p>
	 * process PLUS operation node if at least one operand has set type
	 * </p>
	 * 
	 * @param expressionAnalyzer
	 *            a reference to the {@link ExpressionAnalyzer}
	 * @param operand0
	 *            one operand of the processing operation node
	 * @param operand1
	 *            the other operand of the processing operation node
	 * @param node
	 *            the operation node
	 * @return true iff the operation node has been set a set of integer or
	 *         pointer type
	 */
	static boolean processSetTypeForPLUSOperands(
			ExpressionAnalyzer expressionAnalyzer, ExpressionNode operand0,
			ExpressionNode operand1, ExpressionNode node) {
		TypeKind kind0 = operand0.getType().kind();
		TypeKind kind1 = operand1.getType().kind();

		if (!(kind0 == TypeKind.SET || kind0 == TypeKind.RANGE
				|| kind1 == TypeKind.SET || kind1 == TypeKind.RANGE))
			return false;

		Type type0 = operand0.getType();
		Type type1 = operand1.getType();

		if (kind0 == TypeKind.SET)
			type0 = ((SetType) type0).elementType();
		if (expressionAnalyzer.isPointerToCompleteObjectType(type0))
			return processSetTypeForPointerAdd(expressionAnalyzer.typeFactory,
					operand0, operand1, node);
		if (kind1 == TypeKind.SET)
			type1 = ((SetType) type1).elementType();
		if (expressionAnalyzer.isPointerToCompleteObjectType(type1))
			return processSetTypeForPointerAdd(expressionAnalyzer.typeFactory,
					operand1, operand0, node);
		return processSetTypeForArithmeticAdd(expressionAnalyzer.typeFactory,
				operand0, operand1, node);
	}

	/**
	 * <p>
	 * process "pointer addition" operation where at least one operand has set
	 * type
	 * </p>
	 * 
	 * @param typeFactory
	 *            a reference to {@link TypeFactory}
	 * @param pointer
	 *            the operand has pointer type or set of pointer type
	 * @param offset
	 *            the operand has integer type or set of integer type (range
	 *            type)
	 * @param node
	 *            the pointer addition operation node
	 * @return true iff the operation node has been set set of pointer type
	 */
	private static boolean processSetTypeForPointerAdd(TypeFactory typeFactory,
			ExpressionNode pointer, ExpressionNode offset,
			ExpressionNode node) {
		PointerType pointerType;
		Type offsetType = offset.getType();

		if (pointer.getType().kind() == TypeKind.SET) {
			pointerType = (PointerType) ((SetType) pointer.getType())
					.elementType();
		} else
			pointerType = (PointerType) pointer.getType();

		if (offset.getType().kind() == TypeKind.SET)
			offsetType = ((SetType) offset.getType()).elementType();
		if (offsetType.kind() != TypeKind.RANGE
				&& !(offsetType instanceof IntegerType))
			return false;
		node.setInitialType(typeFactory.theSetType(pointerType));
		return true;
	}

	/**
	 * <p>
	 * process operation node with arithmetic PLUS operator where at least one
	 * operand has set type
	 * </p>
	 * 
	 * @param typeFactory
	 *            a reference to {@link TypeFactory}
	 * @param num0
	 *            one operand of a PLUS operation
	 * @param num1
	 *            the other operand of a PLUS operation
	 * @param node
	 *            the PLUS operation node
	 * @return true iff the PLUS operation node has been set of integer type
	 * 
	 */
	private static boolean processSetTypeForArithmeticAdd(
			TypeFactory typeFactory, ExpressionNode num0, ExpressionNode num1,
			ExpressionNode node) {
		TypeKind kind0 = num0.getType().kind();
		TypeKind kind1 = num1.getType().kind();
		Type elementType0, elementType1;

		// element type of num0
		if (kind0 == TypeKind.RANGE)
			elementType0 = typeFactory.signedIntegerType(SignedIntKind.INT);
		else if (kind0 == TypeKind.SET)
			elementType0 = ((SetType) num0.getType()).elementType();
		else
			elementType0 = num0.getType();
		// element type of num1
		if (kind1 == TypeKind.RANGE)
			elementType1 = typeFactory.signedIntegerType(SignedIntKind.INT);
		else if (kind1 == TypeKind.SET)
			elementType1 = ((SetType) num1.getType()).elementType();
		else
			elementType1 = num1.getType();

		if (!(elementType0 instanceof ArithmeticType)
				|| !(elementType1 instanceof ArithmeticType))
			// regular type error:
			return false;

		ArithmeticType resultElementType = typeFactory
				.usualArithmeticConversion((ArithmeticType) elementType0,
						(ArithmeticType) elementType1);

		node.setInitialType(typeFactory.theSetType(resultElementType));
		return true;
	}
}
