package edu.udel.cis.vsl.civl.slice.common;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.util.IF.BranchConstraints;
import edu.udel.cis.vsl.sarl.SARL;
import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.preuniverse.IF.FactorySystem;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverses;
import edu.udel.cis.vsl.sarl.prove.z3.Z3Translator;
import edu.udel.cis.vsl.sarl.util.FastList;

public class ControlDependence {
	
	ErrorAutomaton trace;
	Map<CfaLoc,CfaLoc> ipdMap; /* One big map */
	Map<Location,CfaLoc> locToCfaLoc; /* One big map */
	Stack<ControlDependencyElement> cds;
	Map<ErrorCfaLoc,Stack<ControlDependencyElement>> cdsMap;
	Set<BooleanExpression> slicedPC;
	Set<BooleanExpression> minimizedSlice;
	boolean debug = false;
	
	/** Stdout: where most output is going to go, including error reports */
	private PrintStream out = System.out;
	
	public ControlDependence (ErrorAutomaton tr, Map<CfaLoc,CfaLoc> ipd,
			Map<Location,CfaLoc> locationMap) {
		
		trace = tr;
		ipdMap = ipd;
		locToCfaLoc = locationMap;
		cds = new Stack<>();
		cdsMap = new HashMap<>();
		slicedPC = new HashSet<>();
		minimizedSlice = new HashSet<>();
		
	}
	
	public Stack<ControlDependencyElement> collectControlDependencyStack () throws IOException {
		
		for (ErrorCfaLoc l : trace.errorTrace) {	
			if (isMerging(l)) mergingLogic(l);
			if (isBranching(l)) branchingLogic(l);
			updateCdsMap(l,cds);
		}
		if (debug) {
			out.println("\nControl Dependence Stack:\n");
			for (ControlDependencyElement e : cds) {
				out.println(e);
				for (ErrorCfaLoc bp : e.branchPoints) {
					BooleanExpression symExpr = bp.branchConstraint;
					String branchTaken = bp.nextTransition().statement.toString();
					out.println("   -> Conjunct from this region: "+symExpr+
							" (Branch taken: "+branchTaken+")");
				}
			}
		}
		
		assert BranchConstraints.evaluator != null : "BranchConstraints has no evaluator";
		for (ControlDependencyElement e : cds) {
			for (ErrorCfaLoc bp : e.branchPoints) {
				assert bp.branchConstraint != null : "This branch point has no associated branch constraint.";
				slicedPC.add(bp.branchConstraint);
			}
		}
		/* The following method can take a long time; we bound its running time */
		boolean minimize = false;
		if (minimize) {
			minimizedSlice = makeMinimizedPC(slicedPC);
		} else {
			minimizedSlice = slicedPC;
		}
		
		/* Test CdsMap collection */
		if (debug) printCdsMap();
		/* Test smt2 output format */
		if (debug) smt2SliceStrings();
		
		return cds;
		
	}
	
	private void updateCdsMap(ErrorCfaLoc l, Stack<ControlDependencyElement> currentCds) {
		Stack<ControlDependencyElement> cdsValue = new Stack<>();
		List<ControlDependencyElement> cdsList = new ArrayList<>();
		for (ControlDependencyElement e : currentCds) {
			cdsList.add(e);
		}
		cdsValue.addAll(cdsList);
		cdsMap.put(l, cdsValue);
		if (debug) {
			out.println("Updating map for ErrorCfaLoc: "+l);
			for (ControlDependencyElement e : cdsValue) {
				out.println("     "+e);
			}
		}
	}
	
	private void printCdsMap() {
		out.println("\n  Printing CDS map: \n\n");
		for (ErrorCfaLoc l : cdsMap.keySet()) {
			if (l.isExitLocation()) continue;
			out.println("   Stmt: "+l.nextTransition());
			out.println("    Stack:");
			for (ControlDependencyElement e : cdsMap.get(l)) {
				out.println("     "+e);
			}
			out.println();
		}
	}

	public Set<BooleanExpression> getSlicedPC() {
		return slicedPC;
	}
	
	/* This is setup to make the merging call as presented 
	 * in the 2007 paper */
	private void mergingLogic(ErrorCfaLoc l) {
		
		if (debug) out.println("Line "+l.sourceLine+" is a merge point");
		
		Location loc = l.getCIVLLocation();
		CfaLoc mergePoint = locToCfaLoc.get(loc);
		
		merging(mergePoint, l.callingContext);
		
	}
	
	/* This is setup to make the branching call as presented 
	 * in the 2007 paper */
	private void branchingLogic(ErrorCfaLoc branchPoint) {
		
		if (debug) out.println("Line "+branchPoint.sourceLine+" is a branch point");
		
		Location branchLoc = branchPoint.getCIVLLocation();
		CfaLoc branch = locToCfaLoc.get(branchLoc);
		CfaLoc mergePoint = ipdMap.get(branch);
		assert mergePoint != null : branchPoint.getCIVLLocation()+"'s merge point is null";
		
		branching(branchPoint, mergePoint, branchPoint.callingContext);
		
	}

	private boolean isMerging(ErrorCfaLoc l) {
		
		if (l.isExitLocation()) {
			return false;
		} else {
			Location loc = l.getCIVLLocation();
			assert loc != null : "No CIVL Location found for: "+l;
			CfaLoc cfaLoc = locToCfaLoc.get(loc);
			
			/* The instrumented branch directives don't have 
			 * corresponding cfaLocs */
			if (cfaLoc == null) {
				return false;
			} else {
				return cfaLoc.isIPD;
			}
			
		}
	}
	
	private boolean isBranching(ErrorCfaLoc l) {

		if (l.isExitLocation()) {
			return false;
		} else {
			//if (debug) out.println("Determining if "+l.nextTransition().statement
				//	+" (line "+l.sourceLine+") ("+l.getCIVLLocation().getSource().toString()+") is branching");
		
			Location loc = l.getCIVLLocation();
			CfaLoc cfaLoc = locToCfaLoc.get(loc);
			
			/* The instrumented branch directives don't have 
			 * corresponding cfaLocs */
			if (cfaLoc == null) {
				return false;
			} else if (cfaLoc.isBranching) {
				String guardString = cfaLoc.location.getOutgoing(0).guard().toString(); 
				if (guardString.startsWith("$sef$")) {
					return false; // This is an instrumented branch directive
				} else {
					return true;
				}
			} else {
				return false;
			}
		}
	}
	
	public void merging (CfaLoc mergePoint, int callingContext) {
		
		if (cds.isEmpty()) return;
		CfaLoc mergePointOnStack = cds.peek().mergePoint;
		int stackContext = cds.peek().context;
		
		if (mergePointOnStack.equals(mergePoint) &&
				(stackContext == callingContext)) {
			ControlDependencyElement c = cds.pop();
			if (debug) out.println("\n--- Just popped: "+c+"\n");
		}
		
	}

	public void branching (ErrorCfaLoc branchPoint, CfaLoc mergePoint, int callingContext) {
		
		if (!cds.isEmpty()) {
			CfaLoc mergePointOnStack = cds.peek().mergePoint;
			int stackContext = cds.peek().context;
			
			if (mergePointOnStack.equals(mergePoint) && 
					(stackContext == callingContext)) {
				if (debug) out.println("Branch points have the same merge point: "+mergePoint);
				if (debug) out.println("\n+++ Pushing "+branchPoint+" (line "+
						branchPoint.sourceLine+") onto the stack. Context: "+callingContext+"\n");
				cds.peek().branchPoints.add(branchPoint);
			} else {
				ControlDependencyElement c = new ControlDependencyElement(branchPoint,
						mergePoint, callingContext);
				if (debug) out.println("\n+++ Pushing "+c+" onto the stack. Context: "+c.context+"\n");
				cds.push(c);
			}
		} else {
			ControlDependencyElement c = new ControlDependencyElement(branchPoint, mergePoint, callingContext);
			if (debug) out.println("\n+++ Pushing "+c+" onto the stack");
			cds.push(c);
		}
		
		
	}
	
	private Set<BooleanExpression> makeMinimizedPC(Set<BooleanExpression> clauses) {
		
		boolean localDebug = true;
		Set<BooleanExpression> impliedClauses = new HashSet<BooleanExpression>();
		
		SymbolicUniverse universe = SARL.newStandardUniverse();
		Reasoner trueContextReasoner = universe.reasoner(universe.trueExpression());
		
		for (BooleanExpression c : clauses) {
			BooleanExpression antecedent = universe.trueExpression();
			for (BooleanExpression other : clauses) {
				if (other.equals(c)) continue;
				antecedent = universe.and(antecedent, other);
			}
			// Print out antecedent and c
			if (localDebug) out.println("Antecedent of stalling implication: "+antecedent);
			if (localDebug) out.println("Consequent of stalling implication: "+c);
			BooleanExpression implication = universe.implies(antecedent, c);
			if (localDebug) out.println("Stalling implication: "+implication);
			
			if (trueContextReasoner.isValid(implication)) {
				impliedClauses.add(c);
			}
		}
		
		Set<BooleanExpression> minimizedClauses = new HashSet<BooleanExpression>();
		minimizedClauses.addAll(clauses);
		minimizedClauses.removeAll(impliedClauses);
		
		return minimizedClauses;
	}
	
	private String smt2SliceStrings () {
		String s = "";
		
		FactorySystem factorySystem = PreUniverses.newIdealFactorySystem();
		PreUniverse universe = PreUniverses.newPreUniverse(factorySystem);
		BooleanExpression context = universe.trueExpression();
		
		Z3Translator startingContext = new Z3Translator(universe,context,true);
		
		for (BooleanExpression expression : minimizedSlice) {
			Z3Translator translator = new Z3Translator(startingContext, expression);
			FastList<String> predicateDecls = translator.getDeclarations();
			FastList<String> predicateText = translator.getTranslation();
			
			out.println("SMT2 Representation of "+expression+":\n");
			predicateDecls.print(out);
			predicateText.print(out);
			out.println();
		}
		
		return s;
	}

}
