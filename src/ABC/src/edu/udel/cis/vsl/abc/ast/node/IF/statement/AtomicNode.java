package edu.udel.cis.vsl.abc.ast.node.IF.statement;

/**
 * An atomic node represents a CIVL-C <code>$atomic</code> statement. Note that
 * <code>$atomic</code> places no restrictions on its statement body.
 * 
 * @author Manchun Zheng (zxxxx)
 * 
 */
public interface AtomicNode extends StatementNode {

	@Override
	AtomicNode copy();

	/**
	 * The body of the <code>$atomic</code> statement.
	 * 
	 * @return the body of the atomic statement
	 */
	StatementNode getBody();

	/**
	 * Sets the body of this atomic statement.
	 * 
	 * @param body
	 *            a statement to become the body
	 */
	void setBody(StatementNode body);
}
