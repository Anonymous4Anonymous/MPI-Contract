package edu.udel.cis.vsl.sarl.IF.type;

import java.util.function.Function;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;

/**
 * <p>
 * This class represents uninterpreted types. Objects of an uninterpreted type
 * cannot participate any operations except for EQUALS, NOT_EQUALS and APPLY.
 * </p>
 * 
 * <p>
 * An uninterpreted type is defined by a name. Two uninterpreted types who have
 * the same name are identical, otherwise, they are different types. Objects of
 * different uninterpreted types cannot be compared.
 * </p>
 * 
 * <p>
 * An concrete expression of an uninterpreted type is indexed by a concrete
 * integral key. Two objects of the same uninterpreted type are identical if and
 * only if they have the exact same key.
 * </p>
 * 
 * @author xxxx
 */
public interface SymbolicUninterpretedType extends SymbolicType {
	/**
	 * @return the name of this uninterpreted type.
	 */
	StringObject name();

	/**
	 * @return an operator which is an instance of {@link Function<T,R>}. The
	 *         key of an symbolic expression of an uninterpreted type can be
	 *         obtained by applying the operator to the symbolic expression.
	 */
	Function<SymbolicExpression, IntObject> soleSelector();
}
