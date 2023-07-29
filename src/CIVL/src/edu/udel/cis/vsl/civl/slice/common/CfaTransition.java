package edu.udel.cis.vsl.civl.slice.common;

import edu.udel.cis.vsl.civl.model.IF.statement.Statement;

public class CfaTransition {
	
	public Statement statement;
	
	public CfaTransition (Statement s) {
		this.statement = s;
	}
	
	public String toString() {
		if (statement != null) {
			return statement.toString();
		} else {
			return "No source statement.";
		}
	}

}
