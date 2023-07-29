package edu.udel.cis.vsl.abc.ast.node.IF.compound;

import edu.udel.cis.vsl.abc.ast.type.IF.Type;

/**
 * A literal object is either a {@link ScalarLiteralObject} or a
 * {@link CompoundLiteralObject}. The elements of a compound initializer
 * designate literal objects in a hierarchical way.
 * 
 * @author xxxx
 * 
 */
public interface LiteralObject {
	Type getType();
}
