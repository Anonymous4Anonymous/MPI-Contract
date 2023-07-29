package edu.udel.cis.vsl.abc.ast.type.IF;

/**
 * This class represents a tuple type. A tuple type consists of n (n >= 1)
 * component types
 * 
 * @author xxxx
 *
 */
public interface TupleType extends Type {
	/**
	 * @return the number of component types of this tuple type
	 */
	int numComponentTypes();

	/**
	 * 
	 * @param idx
	 * @return the idx-th component type of this tuple type
	 */
	Type getComponentType(int idx);

	/**
	 * @return an iterable collection of the component types of this tuple type
	 */
	Iterable<Type> getComponentTypes();
}
