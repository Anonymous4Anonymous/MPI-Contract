package edu.udel.cis.vsl.abc.ast.node.IF.statement;

/**
 * A null statement: ";". AKA "no-op".
 * 
 * @author xxxx
 * 
 */
public interface NullStatementNode extends StatementNode {
	@Override
	NullStatementNode copy();
}
