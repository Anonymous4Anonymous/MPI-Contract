package edu.udel.cis.vsl.civl.slice.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement.StatementKind;
import edu.udel.cis.vsl.civl.slice.IF.DominatorAnalysis;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/* 
 * A Control Flow Automaton is the dual of a Control Flow Graph.
 * Let A be a Control Flow Automaton; A = (L,G,l_0,l_e), consisting
 * of a set of locations L = {0,...,n} and edges in
 * G \in L x Transitions x L labeled with CIVL Statements, an initial 
 * location, l_0, an error location, l_e.  An error trace is a finite 
 * path through G starting at l_0 and ending at l_e.
 * 
 * @author mgerrard
 */

public class ControlFlowAutomaton {
	
	Set<CfaLoc> locs = new HashSet<>();
	public Map<Location,CfaLoc> locToCfaLoc = new HashMap<>();
	public Map<CfaLoc, CfaLoc> immediatePostDominators;
	
	Set<ErrorCfaLoc> locations;
	Set<CfaTransitionRelation> transitionRelations;
	ErrorCfaLoc initialLocation;
	ErrorCfaLoc errorLocation;
	List<ErrorCfaLoc> errorTrace;
	Map<SymbolicExpression,String> inputVariableSyntacticMap;
	int theOnlyProcess = 0; /* We only work with single-threaded programs */
	boolean debug = false;
	
	public ControlFlowAutomaton(CIVLFunction f) {
		
		CfaLoc exit = new CfaLoc("Virtual Exit"); locs.add(exit);
		CfaLoc abort = new CfaLoc("Virtual Abort"); locs.add(abort);
		
		for (Location l : f.locations()) {
			CfaLoc cfaLoc = new CfaLoc(l);
			locs.add(cfaLoc);
			locToCfaLoc.put(l, cfaLoc);
		}
		
		for (CfaLoc cfaLoc : locs) {
			if (cfaLoc.isExit || cfaLoc.isAbort) continue;
			
			/* Change Iterable to List */
			List<Statement> successors = new ArrayList<>();
			cfaLoc.location.outgoing().forEach(successors::add);
			
			if (debug) System.out.println("Looking at successors of CfaLoc: "+cfaLoc);
			for (Statement s : successors) {
				if (debug) System.out.println("  "+s);
				if (s.toString().contains("\"exit\"")) {
					assert false : "Need to implement Dummy Abort node logic. Aborting :)";
					cfaLoc.successors.add(abort);
				}
				if (s.target() == null || s.statementKind().equals(StatementKind.RETURN)) {
					cfaLoc.successors.add(exit);
				} else {
					CfaLoc succ = locToCfaLoc.get(s.target());
					cfaLoc.successors.add(succ);	
					if (cfaLoc.successors.size() > 1) cfaLoc.isBranching = true;
				}
			}
		}
		
		if (debug) {
			System.out.println("  In function: "+f);
			for (CfaLoc cfaLoc : locs) {
				if (cfaLoc.isExit) {
					System.out.println("   * "+f.name().name()+"()'s EXIT Location has no successors");
				} else {
					System.out.println("   "+cfaLoc+" has successors:");
					for (CfaLoc cl : cfaLoc.successors) System.out.println("    "+cl);
				}
			}
		}
		
		Map<CfaLoc,Set<CfaLoc>> successorMap = new HashMap<>();
		for (CfaLoc cfaLoc : locs) {
			if (cfaLoc.isExit) {
				continue;
			} else {
				successorMap.put(cfaLoc, cfaLoc.successors);
			}
		}
		/* We pass in the successor (not pred) map and the exit (not entry) node to compute the dual (postdominator) analysis */
		DominatorAnalysis<CfaLoc> postDom = new CommonDominatorAnalysis<CfaLoc>(locs, successorMap, exit);
		Map<CfaLoc,Set<CfaLoc>> postDominators = postDom.computeDominators();
		if (debug) {
			System.out.println("\n  Post Dominator Map for "+f.name().name()+"() :");
			System.out.println("    "+Arrays.toString(postDominators.entrySet().toArray()));
		}
		
		/* Vertex v is the immediate postdominator of w if v postdominates w
		 * and every other postdominator of w postdominates v */
		Map<CfaLoc,CfaLoc> immediatePostDominators = new HashMap<>();
		for (CfaLoc l : postDominators.keySet()) {
			if (l.isExit || l.isAbort) continue;
			
			Set<CfaLoc> origPdSet = postDominators.get(l);
			if (origPdSet.size() == 1) {
				CfaLoc singleton = origPdSet.stream().findAny().get();
				immediatePostDominators.put(l, singleton);
				continue;
			}
			for (CfaLoc pd : origPdSet) {
				Set<CfaLoc> transitivePdSet = new HashSet<>();
				transitivePdSet.addAll(postDominators.get(pd)); 
				transitivePdSet.add(pd);
				if (origPdSet.equals(transitivePdSet)) {
					immediatePostDominators.put(l, pd);
					break;
				}
			}
			assert immediatePostDominators.containsKey(l) : "No IPD found for: "+l;
		}
		
		Set<CfaLoc> ipds = new HashSet<CfaLoc>(immediatePostDominators.values());
		for (CfaLoc l : ipds) l.isIPD = true;
		this.immediatePostDominators = immediatePostDominators;
		
		if (debug) {
			System.out.println("\n  Immediate Post Dominator Map for "+f.name().name()+"() :");
			System.out.println("    "+Arrays.toString(immediatePostDominators.entrySet().toArray()));
			System.out.println();
		}
	}
	

	/*
	private void print() {
		
		System.out.println("CONTROL FLOW AUTOMATON:\n");
		
		System.out.println("  Transition relations");
		System.out.println("  --------------------");
		for (CfaTransitionRelation t : transitionRelations) {
			System.out.println();
			System.out.println("    Source: "+t.source.location);
			System.out.println("    Transition: "+t.transition.statement);
			System.out.println("    Target: "+t.target.location);
		}
		
		System.out.println();
		System.out.println("  Locations");
		System.out.println("  ---------\n");
		for (CfaLocation l : locations) {
			System.out.println("    "+l.location);
		}
		System.out.println();
		System.out.println("  Initial Location:\n    Not implemented yet.\n");
		System.out.println("  Error Location:\n    Not implemented yet.\n");
		System.out.println("  Error trace:\n    Not implemented yet.\n");
		
	}
	*/

}
