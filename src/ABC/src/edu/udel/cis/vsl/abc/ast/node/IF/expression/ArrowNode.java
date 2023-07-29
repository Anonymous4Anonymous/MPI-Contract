package edu.udel.cis.vsl.abc.ast.node.IF.expression;

import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;

/**
 * An expression in which the operator is the C <code>-></code> (arrow)
 * operator. In C, <code>e->f</code> is equivalent to <code>(*e).f</code>.
 * 
 * @author xxxx
 * 
 */
public interface ArrowNode extends ExpressionNode {

	/**
	 * Returns the node representing the left argument of the arrow operator.
	 * That argument is an expression which is a pointer to a structure or
	 * union.
	 * 
	 * @return the left argument of the arrow operator
	 * @see #setStructurePointer(ExpressionNode)
	 */
	ExpressionNode getStructurePointer();

	/**
	 * Sets the value that will be returned by {@link #getStructurePointer()}.
	 * 
	 * @param structure
	 *            the left argument of the arrow operator
	 */
	void setStructurePointer(ExpressionNode structure);

	/**
	 * Returns the node for the right argument of the arrow operator. That
	 * argument is an identifier which names a field in the structure or union.
	 * 
	 * @return the right argument of the arrow operator
	 * @see #setFieldName(IdentifierNode)
	 */
	IdentifierNode getFieldName();

	/**
	 * Sets the value that will be returned by {@link #getFieldName()}.
	 * 
	 * @param field
	 *            the right argument of the arrow operator
	 */
	void setFieldName(IdentifierNode field);

	@Override
	ArrowNode copy();
	
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

}
