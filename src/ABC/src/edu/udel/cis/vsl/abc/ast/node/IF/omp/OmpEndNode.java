package edu.udel.cis.vsl.abc.ast.node.IF.omp;

import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;

public interface OmpEndNode extends OmpNode, StatementNode{

	public enum OmpEndType {
		PARALLEL, SECTIONS, SECTION, SINGLE, DO
	}
	
	public OmpEndType ompEndType();
}
