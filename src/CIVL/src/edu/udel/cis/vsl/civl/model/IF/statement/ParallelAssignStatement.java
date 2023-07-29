package edu.udel.cis.vsl.civl.model.IF.statement;

import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.util.IF.Pair;

public interface ParallelAssignStatement extends Statement {
	List<Pair<LHSExpression, Expression>> assignments();
}
