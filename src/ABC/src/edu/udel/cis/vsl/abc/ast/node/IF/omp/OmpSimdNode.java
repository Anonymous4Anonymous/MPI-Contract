package edu.udel.cis.vsl.abc.ast.node.IF.omp;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;

/**
 * A simd directive, which may have the following clauses:
 * 
 * <li>if([simd :]scalar-logical-expression)</li>
 * <li>safelen(length)</li>
 * <li>simdlen(length)</li>
 * <li>linear(list[ : linear-step])</li>
 * <li>aligned(list[ : alignment])</li>
 * <li>nontemporal(list)</li>
 * <li>private(list)</li>
 * <li>lastprivate([ lastprivate-modifier:] list)</li>
 * <li>reduction([ reduction-modifier,]reduction-identifier : list)</li>
 * <li>collapse(n)</li>
 * <li>order(concurrent)</li>
 * 
 * 
 * @author xxxx
 *
 */
public interface OmpSimdNode extends OmpExecutableNode {
	/**
	 * 
	 * @return the safelen clause argument which is a constant
	 */
	ConstantNode safeLen();

	/**
	 * 
	 * @param arg
	 *            a {@link ConstantNode} represents the argument of safelen
	 *            clause
	 * @return the old safelen argument node
	 */
	ASTNode setSafelen(ConstantNode arg);

	/**
	 * 
	 * @return the simdlen clause argument which is a constant
	 */
	ConstantNode simdLen();

	/**
	 * 
	 * @param arg
	 *            a {@link ConstantNode} represents the argument of simdlen
	 *            clause
	 * @return the old simdlen argument node
	 */
	ASTNode setSimdlen(ConstantNode arg);
}
