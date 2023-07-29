package edu.udel.cis.vsl.abc.ast.node.IF.omp;

/**
 * This represents an OpenMP atomic construct, which has the syntax:
 * 
 * <pre>
 * #pragma omp atomic [seq_cst][read | write | update][seq_cst] new-line
 *   expression-stmt
 * </pre>
 * 
 * or
 * 
 * <pre>
 * #pragma omp atomic capture [seq_cst] new-line 
 *   structured-block
 * </pre>
 * 
 * @author xxxx, zxxxx
 *
 */
public interface OmpAtomicNode extends OmpSyncNode {

	public static enum OmpAtomicClause {
		READ, WRITE, UPDATE, CAPTURE
	};

	/**
	 * Returns the atomic clause of this OpenMP atomic construct, whether it is
	 * READ, WRITE, UPDATE or CAPTURE
	 * 
	 * @return the atomic clause of this OpenMP atomic construct
	 */
	OmpAtomicClause atomicClause();

	/**
	 * is this atomic construct sequentially consistent ?
	 * 
	 * @return true iff this atomic construct is sequentially consistent
	 */
	boolean seqConsistent();
}
