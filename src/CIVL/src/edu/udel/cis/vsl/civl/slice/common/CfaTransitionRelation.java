package edu.udel.cis.vsl.civl.slice.common;

public class CfaTransitionRelation {
	
	protected ErrorCfaLoc source;
	protected CfaTransition transition;
	protected ErrorCfaLoc target;
	
	public CfaTransitionRelation (ErrorCfaLoc source, CfaTransition transition,
			ErrorCfaLoc target) {
		
		this.source = source;
		this.transition = transition;
		this.target = target;
		
	}

}
