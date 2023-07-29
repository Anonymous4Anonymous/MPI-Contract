package edu.udel.cis.vsl.abc.ast.conversion.IF;

import edu.udel.cis.vsl.abc.ast.type.IF.MemType;
import edu.udel.cis.vsl.abc.ast.type.IF.SetType;

/**
 * <p>
 * A MemConversion converts an expression of pointer or set-of pointer type to
 * $mem type.
 * </p>
 * 
 * 
 * <p>
 * Given an expression <code>e</code> that will be converted to have $mem type,
 * the following restrictions will be applied to <code>e</code>:
 * 
 * <ul>
 * <li>If e has the form: <code>&(*p)</code>, apply the rest of the restrictions
 * on <code>p</code>; Or if e has the form: <code>&(p[e'])</code> and
 * <code>p</code> has (set-of) pointer type, apply the rest of the restrictions
 * on <code>p</code>.</li>
 * 
 * <li><code>e</code> and any sub-expressions of <code>e</code> must not be any
 * of these form: <code>*p, p[e'], p->id</code> where <code>p</code> has the
 * type of a set-of pointers</li>
 * 
 * <li>If <code>e</code> and any sub-expressions of <code>e</code> has type of a
 * set-of pointers, it can only have such a form <code>e' + I</code>, where
 * <code>e'</code> is a sub-expression of <code></code> and <code>I</code> is an
 * expression of (set-of) integer type</li>
 * </ul>
 * </p>
 * 
 * <p>
 * The restrictions above is used to prevent cases like <code>
 * int * p[N];
 * 
 * $mem m = p[0 .. n]</code>. It is hard, if not impossible to represent the
 * value of <code>m</code> if <code>n, N</code> have non-concrete values.
 * </p>
 * 
 * <p>
 * See {@link SetType}, {@link MemType} for more informations
 * </p>
 *
 * @author xxxx
 */
public interface MemConversion extends Conversion {
	@Override
	MemType getNewType();
}
