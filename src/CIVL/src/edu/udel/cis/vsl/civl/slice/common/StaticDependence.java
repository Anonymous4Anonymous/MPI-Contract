package edu.udel.cis.vsl.civl.slice.common;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.AssignStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

public class StaticDependence {
	
	private Set<Location> visited;
	private Set<ErrorCfaLoc> questionableBranches;
	private Map<Location, Set<Variable>> varsMap;
	private boolean suspicious;
	
	private PrintStream out = System.out;
	private boolean debug = false;

	public StaticDependence(Set<ErrorCfaLoc> independentBranches, 
			Map<Location, Set<Variable>> variablesOfInterestMap, 
			Model model, Map<CfaLoc, CfaLoc> ipds, Map<Location, CfaLoc> locToCfaLoc) {
		
		visited = new HashSet<>();
		questionableBranches = new HashSet<>();
		varsMap = variablesOfInterestMap;
		if (debug) {
			out.println("\nIncoming Variables of Interest:\n");
			for (Location l : variablesOfInterestMap.keySet()) {
				out.println("  "+l+" -> "+variablesOfInterestMap.get(l));
			}
			out.println("\nIncoming Independent Branches:\n");
			for (ErrorCfaLoc l : independentBranches) out.println("  "+l);
			out.println();
		}
		
		for (ErrorCfaLoc l : independentBranches) {
			
			if (l.isExitLocation()) continue;
			
			/* Run bounded DFS from off-branch up to merge point Location*/
			Location offBranch = findOffBranch(l);
			Location mergePoint = findMergePoint(l, ipds, locToCfaLoc);
			visited.clear(); suspicious = false;
			boundedDfs(offBranch, mergePoint);
			if (suspicious) questionableBranches.add(l);
		}
		
	}
	
	private Location findMergePoint(ErrorCfaLoc l, Map<CfaLoc, CfaLoc> ipds, Map<Location, CfaLoc> locToCfaLoc) {
		
		CfaLoc cfaLoc = locToCfaLoc.get(l.getCIVLLocation());
		/* Within assert(cond), __VERIFIER_error() needs to go to the
		 * exit node, but right now the cfaLoc is empty...Location 24 
		 */
		assert cfaLoc != null : "CfaLoc for location "+l.toString()+", or CIVL location: "+l.getCIVLLocation().toString()+" is null.";
		CfaLoc ipd = ipds.get(cfaLoc);
		assert ipd != null : "IPD for "+cfaLoc.toString()+" is null.";
		assert ipd.location != null : "The location for "+ipd.toString()+" is null.";
		return ipd.location;
		
	}

	private Location findOffBranch(ErrorCfaLoc l) {
		
		Statement branchTakenStmt = l.nextTransition().statement;
		assert branchTakenStmt != null : "Outgoing transition of "+l+
				" has no associated statement";
		Location offBranch = null;
		
		for (Statement s : l.getCIVLLocation().outgoing()) {		
			if (!s.equals(branchTakenStmt)) {
				offBranch = s.target(); 
				break;
			}		
		}
		
		assert offBranch != null : "Off-branch is null";
		return offBranch;
	}

	private void boundedDfs (Location l, Location mergePoint) {
		visited.add(l); if (debug) out.println("Visiting: "+l);
		
		for (Statement s : l.outgoing()) {
			Set<Variable> varsOfInterest = varsMap.get(mergePoint);
			assert varsOfInterest != null : "Variables of interest is null";
			if (isSuspicious(s, varsOfInterest)) { suspicious = true; return; }
			if (isReturn(s)) continue;
			Location t = s.target(); if (debug) out.print("Finding target of :"+s);
			assert t != null : "Target location of "+s+" is null";
			if (!visited.contains(t) && !t.equals(mergePoint)) {
				boundedDfs(t,mergePoint);
			}
		}
		
	}
	
	private boolean isReturn(Statement s) {
		return s.toString().startsWith("return");
	}

	private boolean isSuspicious(Statement stmt, Set<Variable> varsOfInterest) {
		/* A suspicious statement shouldn't include the error function; because
		 * if the trace hits the error function on both the true and false branch,
		 * then that branch should be sliced away */
		if (isRelevantFunction(stmt)) {
			
			if (debug) out.println("Suspicious statement (function): "+stmt);
			return true;
			
		} else if (isAssign(stmt)) {
			
			AssignStatement as = (AssignStatement) stmt;
			Variable lhs = as.getLhs().variableWritten();
			assert lhs != null : "LHS is null";
			
			if (varsOfInterest.contains(lhs)) {
				if (debug) out.println("Suspicious statement (LHS variable): "+stmt);
				return true;
			} else if (lhs.hasPointerRef()) {
				if (debug) out.println("Suspicious statement (LHS variable): "+stmt);
				return true;
			} else {
				return false;
			}
			
		} else {
			return false;
		}
	}
	
	private boolean isRelevantFunction (Statement stmt) {
		if (stmt instanceof CallOrSpawnStatement) {
			
			CIVLFunction f = ((CallOrSpawnStatement) stmt).function();
			String functionName = f.name().toString();
			
			if (functionName.startsWith("__VERIFIER_")) {
				return false;
			} else if (functionName.startsWith("$")) {
				return false;
			} else if (f.isPureFunction() || f.isPurelyLocal()) {
				return false;
			} else {
				return true;				
			}
		} else {
			return false;
		}
	}
	
	private boolean isAssign (Statement stmt) {
		return (stmt instanceof AssignStatement);
	}

	public Set<ErrorCfaLoc> getQuestionableBranches() {
		return questionableBranches;
	}

}
