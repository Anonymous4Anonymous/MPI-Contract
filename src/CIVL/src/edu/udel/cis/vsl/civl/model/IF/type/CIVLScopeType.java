package edu.udel.cis.vsl.civl.model.IF.type;

import java.util.function.Function;

import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;

/**
 *
 * <p>
 * The $scope type in the CIVL-C language.
 * </p>
 * 
 * <p>
 * For any object of $scope type, its value has a {@link SymbolicType} which is
 * called the dynamic scope type and is uniquely associated with the $scope
 * type. The definition of the dynamic scope type is defined in this class as
 * well.
 * </p>
 * 
 * <p>
 * The definition of a dynamic scope type is up to the implementation of this
 * interface but it must satisfy one restriction: a symbolic value of dynamic
 * scope type must be associated with an integer. The integer is called the
 * identity of a scope value. Two scope values are equivalent iff their
 * identities are equal.
 * </p>
 * 
 * 
 * @author xxxx
 *
 */
public interface CIVLScopeType extends CIVLPrimitiveType {
	/**
	 * 
	 * @param universe
	 *            a reference to {@link SymbolicUniverse}
	 * @return A java {@link Function} which maps a scope value to its integral
	 *         identity.
	 */
	Function<SymbolicExpression, IntegerNumber> scopeValueToIdentityOperator(
			SymbolicUniverse universe);

	/**
	 * 
	 * @param universe
	 *            a reference to {@link SymbolicUniverse}
	 * @return A java {@link Function} which maps an integral identity to a
	 *         scope value.
	 */
	Function<Integer, SymbolicExpression> scopeIdentityToValueOperator(
			SymbolicUniverse universe);
}
