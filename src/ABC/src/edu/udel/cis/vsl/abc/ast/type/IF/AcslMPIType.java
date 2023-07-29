package edu.udel.cis.vsl.abc.ast.type.IF;

/**
 * Representing the set of types of the constructs in the ACSL-MPI contract
 * language that are disjoint with CIVL-C types.
 * 
 * @author xxxx
 */
public interface AcslMPIType extends UnqualifiedObjectType {
	static public enum AcslMPITypeKind {
		/**
		 * see {@link AcslMPIBufType}
		 */
		ACSL_MPI_BUF_TYPE,
		/**
		 * see {@link AcslMPISeqType}
		 */
		ACSL_MPI_SEQ_TYPE,
		/**
		 * see {@link AcslMPISigType}
		 */
		ACSL_MPI_SIG_TYPE
	}
	
	/**
	 * 
	 * @return the ACSL type kind
	 */
	AcslMPITypeKind acslTypeKind();
}
