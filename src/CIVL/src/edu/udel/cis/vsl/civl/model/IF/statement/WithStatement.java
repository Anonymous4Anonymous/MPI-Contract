package edu.udel.cis.vsl.civl.model.IF.statement;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;

public interface WithStatement extends Statement {
	boolean isEnter();

	boolean isExit();

	Expression collateState();

	CIVLFunction function();
}
