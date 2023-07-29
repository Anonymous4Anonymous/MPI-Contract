package edu.udel.cis.vsl.civl.model.IF.statement;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;

public interface UpdateStatement extends Statement {

	Expression collator();

	CallOrSpawnStatement call();

	CIVLFunction function();

	Expression[] arguments();
}
