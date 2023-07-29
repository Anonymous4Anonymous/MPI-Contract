package edu.udel.cis.vsl.abc.ast.node.IF.expression;

/**
 * Represents the CIVL-C built-in variable <code>$result</code>, which
 * represents the value returned by a function. It is used in a post-condition
 * of a procedure contract.
 * 
 * @author xxxx
 * 
 */
public interface ResultNode extends ExpressionNode {

	@Override
	ResultNode copy();
}
