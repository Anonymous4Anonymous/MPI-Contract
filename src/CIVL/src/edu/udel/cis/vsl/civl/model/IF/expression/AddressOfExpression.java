package edu.udel.cis.vsl.civl.model.IF.expression;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;

/**
 * This represents an address-of expression, which contains one operand, and has
 * the format: <code>&x</code>, where <code>&</code> is the address-of operator
 * and <code>x</code> is the operand.
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public interface AddressOfExpression extends Expression {

	/**
	 * @return The operand.
	 */
	LHSExpression operand();

	boolean isFieldOffset();

	void setFieldOffset(boolean value);

	void setTypeForOffset(CIVLType type);

	void setFieldIndex(int index);

	CIVLType getTypeForOffset();

	int getFieldIndex();
}
