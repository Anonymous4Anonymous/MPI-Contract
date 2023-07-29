package edu.udel.cis.vsl.civl.model.common.contract;

import java.io.PrintStream;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.contract.NamedFunctionBehavior;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;

public class CommonNamedFunctionBehavior extends CommonFunctionBehavior
		implements NamedFunctionBehavior {
	private String name;
	private Expression assumptions;

	public CommonNamedFunctionBehavior(CIVLSource source, String name) {
		super(source);
		this.name = name;
	}

	@Override
	public String name() {
		return this.name;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean isDebug) {
		String subPrefix = prefix + "| ";

		out.println(prefix + "behavior " + this.name + ":");
		out.print(subPrefix + "assumes ");
		out.print(assumptions.toString());
		out.println();
		super.print(subPrefix, out, isDebug);
	}

	@Override
	public Expression assumptions() {
		return this.assumptions;
	}

	@Override
	public void setAssumption(Expression assumption) {
		this.assumptions = assumption;
	}
}
