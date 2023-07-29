package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

/**
 * <p>
 * This class represents an integral parameter "c|*" of an abstract object that
 * has the form of <code>U + (c|*)</code> or <code>U[c|*]</code>. The notation
 * "c|*" means either a constant integer "c" or an arbitrary integer "*".
 * </p>
 * 
 * <p>
 * An instance of this class represents a constant integer iff
 * {@link AssignOffsetIF#hasConstantValue()}; otherwise, this instance
 * represents an arbitrary integer.
 * </p>
 * 
 * @author xxxx
 *
 */
public interface AssignOffsetIF {

	Integer constantValue();

	boolean hasConstantValue();
}
