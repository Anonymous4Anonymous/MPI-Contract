package edu.udel.cis.vsl.abc.ast.node.IF.expression;

/**
 * Represents the CIVL-C null process constant <code>$proc_null</code>, which is
 * a constant of type <code>$proc</code>.
 * 
 * @author xxxx
 * 
 */
public interface ProcnullNode extends ConstantNode {
	@Override
	ProcnullNode copy();
}
