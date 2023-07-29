package edu.udel.cis.vsl.civl.slice.common;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.gmc.Trace;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public class SliceAnalysis {
	
	private Map<CfaLoc,CfaLoc> ipds;
	private Map<Location,CfaLoc> locToCfaLoc;
	private ErrorAutomaton errorTrace;
	private Set<ErrorCfaLoc> slice;
	private int numberSlicedAway;
	
	private PrintStream out = System.out;
	private boolean debug = false;
	
	public SliceAnalysis (Model model, Trace<Transition, State> trace) {
		
		ipds = new HashMap<>();
		locToCfaLoc = new HashMap<>();
		initializeDataStructures(model);
		errorTrace = new ErrorAutomaton(model, trace);
		numberSlicedAway = 0;
		
		if (debug) out.println("Running Dynamic Dependence Analysis");
		DynamicDependence dd = new DynamicDependence(errorTrace, ipds, locToCfaLoc);
		Set<ErrorCfaLoc> dependentBranches = dd.getDependentBranches();
		if (debug) {
			out.println("\n  Dependent branches:\n");
			for (ErrorCfaLoc l : dependentBranches) out.println("   "+l);
			out.println();
		}
		
		if (debug) out.println("Running Static Dependence Analysis");
		
		int totalBranchNum = getNumberOfSymbolicBranches(dd.getAllBranches());
		
		Set<ErrorCfaLoc> independentBranches = dd.getAllBranches();
		independentBranches.removeAll(dependentBranches);
				
		Map<Location,Set<Variable>> variablesOfInterestMap = dd.getVariablesOfInterestMap();
		StaticDependence sd = new StaticDependence(independentBranches, variablesOfInterestMap, 
				model, ipds, locToCfaLoc);
		
		if (debug) out.println("Returning union of Dependent and Questionable Branches");
		Set<ErrorCfaLoc> questionableBranches = sd.getQuestionableBranches();
		
		Set<ErrorCfaLoc> slice = new HashSet<>(dependentBranches);
		slice.addAll(questionableBranches);
		
		int branchesInSliceNum = getNumberOfSymbolicBranches(slice);
		int conjunctsSlicedAway = totalBranchNum - branchesInSliceNum;
		if (debug) out.println("Number of conjuncts sliced away: "+conjunctsSlicedAway);
		this.numberSlicedAway = conjunctsSlicedAway;
		this.slice = slice;
	}
	
	private int getNumberOfSymbolicBranches(Set<ErrorCfaLoc> branches) {
		int numberOfSymbolicBranches = 0;
		for (ErrorCfaLoc l : branches) {
			BooleanExpression c = l.branchConstraint;
			if (c != null && !c.toString().equals("true")) {
				numberOfSymbolicBranches++;
			}
		}
		return numberOfSymbolicBranches;
	}
	
	public Map<SymbolicExpression,String> getMapping() {
		return errorTrace.inputVariableSyntacticMap;
	}
	
	public Set<ErrorCfaLoc> getSlice() {
		return slice;
	}
	
	public int getNumberSliced() {
		return numberSlicedAway;
	}
	
	private void initializeDataStructures (Model model) {
		for (CIVLFunction f : model.functions()) {
			if (f.isSystemFunction() || f.toString().startsWith("__VERIFIER_")) continue;
			ControlFlowAutomaton cfa = new ControlFlowAutomaton(f);
			ipds.putAll(cfa.immediatePostDominators);
			locToCfaLoc.putAll(cfa.locToCfaLoc);
		}
	}

}
