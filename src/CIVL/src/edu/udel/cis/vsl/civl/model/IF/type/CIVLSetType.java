package edu.udel.cis.vsl.civl.model.IF.type;

/**
 * <p>
 * A {@link CIVLType} representing a set of a non-set kind CIVLType. Note that
 * there is no implicit conversion between a {@link CIVLType} and a
 * {@link CIVLSetType}.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface CIVLSetType extends CIVLType {
	/**
	 * 
	 * @return the element type of this CIVLSetType which can have any
	 *         {@link TypeKind} other than {@link TypeKind#SET}
	 */
	CIVLType elementType();
}
