package edu.udel.cis.vsl.abc.ast.type.IF;

import edu.udel.cis.vsl.abc.ast.conversion.IF.MemConversion;

/**
 * <p>
 * This class represents the <code>$mem</code> type which stands for a set of
 * (typed) memory locations.
 * </p>
 * 
 * <p>
 * There is always an implicit type conversion from {@link SetType} with element
 * of pointer type to {@link MemType}. An expression <code>e</code> in CIVL-C
 * language of <code>$mem</code> type can only be one of the following forms:
 * <ul>
 * <li>e is an identifier</li>
 * <li>e is a function call, to which returns $mem type object</li>
 * <li>e has form: <code>($mem)e'</code> where the cast can be either an
 * explicit cast or an implicit conversion. The expression <code>e'</code> is an
 * expression of (set-of) pointer types</li>
 * </ul>
 * </p>
 * 
 * <p>
 * In CIVL-C, an expression <code>e</code> of set type can only appear as a
 * sub-expression of such a form: <code>($mem)e'</code> where the cast can be
 * either an explicit cast or an implicit conversion. Hence <code>e</code> is
 * either <code>e'</code> or a sub-expression of <code>e'</code>. See
 * {@link SetType} for more informations.
 * </p>
 * 
 * <p>
 * In addition, for an expression of the form: <code>($mem)e'</code> where the
 * cast can be either an explicit cast or an implicit conversion, there are
 * restrictions on the form of <code>e'</code>. See {@link MemConversion} for
 * more informations.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface MemType extends UnqualifiedObjectType, SetType {
}
