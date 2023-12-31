package edu.udel.cis.vsl.abc.ast.node.IF.expression;

import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;

/**
 * A C expression in which the operator is the <code>.</code> (dot) operator,
 * used to specify a member of a structure or union.
 * 
 * @author xxxx
 * 
 */
public interface DotNode extends ExpressionNode {

	/**
	 * Returns the node representing the left operand, which must have structure
	 * or union type.
	 * 
	 * @return the left operand
	 */
	ExpressionNode getStructure();

	/**
	 * Sets the value returned by {@link #getStructure()}.
	 * 
	 * @param structure
	 *            the left operand
	 */
	void setStructure(ExpressionNode structure);

	/**
	 * Returns the node representing the right operand, which must be an
	 * identifier which names a field in the structure or union type which is
	 * the type of the left operand.
	 * 
	 * @return the right operand
	 */
	IdentifierNode getFieldName();

	/**
	 * Sets the value returned by {@link #getFieldName()}.
	 * 
	 * @param field
	 *            the right operand
	 */
	void setFieldName(IdentifierNode field);

	/**
	 * Returns the sequence of nested fields navigates from an outer structure
	 * or union member to an inner member through anonymous structure or union
	 * members.  Example:
	 * 
	 * <pre>
	 * struct S {
	 *   union {       // call this field "f0"
	 *     struct {    // call this field "f1"
	 *       union {   // call this field "f2"
	 *         int x;
	 *       };
	 *     };
	 *   };
	 * } u;
	 * </pre>
	 * 
	 * For the dot expression <code>u.x</code>, the navigation sequence is
	 * the sequence of Field objects {f0, f1, f2, x}.
	 * If those anonymous fields were given the
	 * names f0, f1, and f2, then the dot expression would
	 * be transformed to <code>u.f0.f1.f2.x</code>.
	 * 
	 * @return the navigation sequence
	 */
	Field[] getNavigationSequence();

	void setNavigationSequence(Field[] sequence);

	@Override
	DotNode copy();

}
