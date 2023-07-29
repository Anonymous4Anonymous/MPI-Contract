package edu.udel.cis.vsl.abc.analysis.dataflow.IF;

import java.util.Map;

import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;

public interface Evaluation<E> {
	E evaluate(ASTNode e, Map<Entity, E> map, E top);

}
