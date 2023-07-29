package edu.udel.cis.vsl.abc.ast.type.IF;

import edu.udel.cis.vsl.abc.ast.conversion.IF.MemConversion;

/**
 * <p>
 * A set type is the type for expressions that represent sets of objects. A set
 * type has an element type which is an instance of an {@link ObjectType} (see
 * also {@link #elementType()}).
 * </p>
 * 
 * <p>
 * The construction of a set type expression is defined in ACSL v1.16 "Sec.
 * 2.3.4 Memory locations and sets of terms". see also {@link SetTypeAnalyzer}.
 * </p>
 * 
 * <p>
 * Expressions of set-of NON POINTER type cannot be used in other places than
 * the argument lists of ACSL <code>assigns</code>, <code>loop assigns</code> or
 * CIVL-C POR contract <code>reads</code> clauses.
 * </p>
 * 
 * 
 * <p>
 * Expressions of set-of POINTER type can only be used in places where a $mem
 * type object is needed.
 * </p>
 * 
 * <p>
 * There is an implicit type conversion from {@link SetType} with element of
 * pointer type to {@link MemType}. see {@link MemConversion}.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface SetType extends Type {
	/**
	 * @return the element type of this set type; will not be <code>null</code>
	 *         , {@link MemType} or SetType.
	 */
	Type elementType();
}
