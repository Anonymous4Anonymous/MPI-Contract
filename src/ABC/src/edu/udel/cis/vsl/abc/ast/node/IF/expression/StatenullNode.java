package edu.udel.cis.vsl.abc.ast.node.IF.expression;

/**
 * Represents the CIVL-C null state constant <code>$state_null</code>, which is
 * a constant of type <code>$state</code>.
 * 
 * @author xxxx
 *
 */
public interface StatenullNode extends ConstantNode {
	@Override
	StatenullNode copy();
}
