package edu.udel.cis.vsl.abc.ast.node.IF.statement;

/**
 * Represents a CIVL-C <code>$run</code> expression, which has the form
 * <code>$run statement</code>.The statement s can be any single
 * {@link StatementNode} or a {@link CompoundStatementNode} which is wrapped by
 * a pair of curly braces. The whole expression means that the current process
 * will "spawn" another process to run statement s and SPECIALLY the new process
 * will terminate automatically after reaches the end of s without an explicit
 * $wait for it.
 * */
public interface RunNode extends StatementNode {

	/**
	 * Returns the function call node, which is like removing the
	 * <code>$spawn</code>.
	 * 
	 * @return the function call node
	 */
	StatementNode getStatement();

	@Override
	RunNode copy();
}
