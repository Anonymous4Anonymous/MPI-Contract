package edu.udel.cis.vsl.abc.ast.node.IF.omp;

/**
 * This represents an OpenMP reduction clause with the reduction operator being
 * one of the following operators: <br>
 * For C:
 * <code>max</code>,<code>min</code>,<code>+</code>,<code>-</code>,<code>*</code>,
 * <code>&</code>,<code>|</code>,<code>^</code>,<code>&&</code>, and
 * <code>||</code> <br>
 * For Fortran: (NOT case sensitive)
 * <code>MAX</code>,<code>MIN</code>,<code>+</code>,<code>-</code>,<code>*</code>,
 * <code>IAND</code>,<code>IOR</code>,<code>IEOR</code>,<code>.AND.</code>,
 * <code>.OR.</code>, <code>.EQV.</code> and <code>.NEQV.</code>
 * 
 * @author Manchun Zheng
 * 
 */
public interface OmpSymbolReductionNode extends OmpReductionNode {
	/**
	 * Returns the operator of this node.
	 * 
	 * @return the operator.
	 */
	OmpReductionOperator operator();
}
