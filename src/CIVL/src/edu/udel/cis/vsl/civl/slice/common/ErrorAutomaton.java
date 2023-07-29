package edu.udel.cis.vsl.civl.slice.common;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.udel.cis.vsl.civl.kripke.IF.AtomicStep;
import edu.udel.cis.vsl.civl.kripke.IF.TraceStep;
import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression.LHSExpressionKind;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.statement.AssignStatement;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluation;
import edu.udel.cis.vsl.civl.semantics.IF.Transition;
import edu.udel.cis.vsl.civl.state.IF.State;
import edu.udel.cis.vsl.civl.state.IF.UnsatisfiablePathConditionException;
import edu.udel.cis.vsl.civl.util.IF.BranchConstraints;
import edu.udel.cis.vsl.gmc.Trace;
import edu.udel.cis.vsl.gmc.TraceStepIF;
import edu.udel.cis.vsl.sarl.SARL;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

public class ErrorAutomaton {
	
	Set<CfaLoc> locs = new HashSet<>();
	Map<Location,CfaLoc> locToCfaLoc = new HashMap<>();
	
	Set<ErrorCfaLoc> locations;
	Set<CfaTransitionRelation> transitionRelations;
	ErrorCfaLoc initialLocation;
	ErrorCfaLoc errorLocation;
	List<ErrorCfaLoc> errorTrace;
	Map<SymbolicExpression,String> inputVariableSyntacticMap;
	Map<Integer,Integer> inputFrequencyMap;
	Map<Integer,String> inputTypeMap;
	List<BooleanExpression> branchConstraints;
	Set<ErrorCfaLoc> allBranches = new HashSet<>();
	
	String inputReadInfix = "= __VERIFIER_nondet_";
	
	int theOnlyProcess = 0; /* We only work with single-threaded programs */
	boolean debug = false;
	private PrintStream out = System.out;
	
	public ErrorAutomaton (Model model, Trace<Transition, State> trace) {
		
		for (CIVLFunction f : model.functions()) {
			if (f.isSystemFunction() || f.toString().startsWith("__VERIFIER_")) continue;
			
			if (debug) out.println("  In function: "+f);
			for (Location l : f.locations()) {
				if (debug) out.println("    "+l+" has successors:");
				List<Statement> successors = new ArrayList<>();
				l.outgoing().forEach(successors::add);
				if (debug) for (Statement s : successors) out.println("      "+s);
			}
		}
		
		errorTrace = constructErrorTrace(trace);
		collectBranchConstraints();
		inputVariableSyntacticMap = makeSymbolicVariableMap(errorTrace);
		inputFrequencyMap = makeInputFrequencyMap(errorTrace);
		inputTypeMap = makeInputTypeMap(errorTrace);
		transitionRelations = transitionRelationsFromTrace(errorTrace);

		if (debug) {
			out.println("\n--- BEGIN Error trace ---\n");
			for (ErrorCfaLoc l : errorTrace) {
				if (!l.isExitLocation()) 
					out.println(l.nextTransition().statement	);
				else
					out.println("EXIT");
			}
			out.println("\n--- END Error trace ---\n");
		}
		
	}

	private Set<CfaTransitionRelation> transitionRelationsFromTrace(List<ErrorCfaLoc> errorTrace) {
		Set<CfaTransitionRelation> transitions = new HashSet<>();
		for (ErrorCfaLoc l : errorTrace) {
			if (l.isExitLocation()) break;
			
			ErrorCfaLoc source = l;
			CfaTransition transition = l.nextTransition();
			ErrorCfaLoc target = l.nextLocation();
			CfaTransitionRelation relation = new CfaTransitionRelation(source,transition,target);
			transitions.add(relation);
		}
		return transitions;
	}

	private Map<SymbolicExpression,String> makeSymbolicVariableMap(List<ErrorCfaLoc> errorTrace) {
		
		Map<SymbolicExpression,String> map = new LinkedHashMap<>();
		
		for (ErrorCfaLoc l : errorTrace) {
			if (l.isExitLocation()) break;
			
			Statement s = l.nextTransition().statement;
			if (debug) out.println("Statement string: "+s.toString());
			if (s.toString().contains(inputReadInfix)) {
				LHSExpression lhs;
				if (s instanceof CallOrSpawnStatement) {
					lhs = ((CallOrSpawnStatement) s).lhs();
				} else {
					/* If it's an array, we need to index correctly, otherwise
					 * we'll always be grabbing the zero-eth element.
					 */
					lhs = ((AssignStatement) s).getLhs(); 

				}
				Variable lhsVar;
				if (lhs.lhsExpressionKind() == LHSExpressionKind.SUBSCRIPT) {
					if (debug) out.print("  ->>>> The LHS is an array!!\n");
					lhsVar = lhs.variableWritten();
				} else {
					lhsVar = lhs.variableWritten();
				}
				State postState = l.nextLocation().state;
				assert (!(postState == null));
				SymbolicExpression symExpr = postState.valueOf(theOnlyProcess, lhsVar);
				String variableName = lhsVar.name().toString();
				map.put(symExpr, variableName);
			}
		}
		return map;
	}
	
	private Map<Integer,Integer> makeInputFrequencyMap(List<ErrorCfaLoc> errorTrace) {
		
		Map<Integer,Integer> map = new HashMap<>();
		
		for (ErrorCfaLoc l : errorTrace) {
			if (l.isExitLocation()) break;
			
			Statement s = l.nextTransition().statement;
			if (s.toString().contains(inputReadInfix)) {
				
				String line = getSourceLine(l.toString());
				Integer lineNumber = Integer.valueOf(line);
				
				if (map.containsKey(lineNumber)) {
					map.put(lineNumber, map.get(lineNumber) + 1);
				} else {
					map.put(lineNumber, 1);
				}
			}
		}
		return map;
	}
	
	private Map<Integer,String> makeInputTypeMap(List<ErrorCfaLoc> errorTrace) {
		
		Map<Integer,String> map = new HashMap<>();
		
		for (ErrorCfaLoc l : errorTrace) {
			if (l.isExitLocation()) break;
			
			Statement s = l.nextTransition().statement;
			if (s.toString().contains(inputReadInfix)) {
				
				String line = getSourceLine(l.toString());
				Integer lineNumber = Integer.valueOf(line);
				
				String type = getType(s.toString());
				
				if (!map.containsKey(lineNumber)) {
					map.put(lineNumber, type);
				}
			}
		}
		return map;
	}
	
	public List<ErrorCfaLoc> getTrace() {
		return this.errorTrace;
	}

	public void printBranchConstraints () {
		for (ErrorCfaLoc l : errorTrace) {
			if (l.isExitLocation()) continue;
			if (l.getCIVLLocation().getNumOutgoing() > 1) {
				Statement stmt = l.nextTransition().statement;
				int pid = 0;
				State state = l.state;
				BooleanExpression branch = getGuard(stmt,pid,state);
				out.println(branch);
			}
		}
	}
	
	public void collectBranchConstraints () {
		
		branchConstraints = new ArrayList<BooleanExpression>();
		
		for (ErrorCfaLoc l : errorTrace) {
			if (l.isExitLocation()) continue;
			if (l.getCIVLLocation().getNumOutgoing() > 1) {
				Statement stmt = l.nextTransition().statement;
				int pid = 0; /* We only analyze single-threaded programs */
				State state = l.state;
				BooleanExpression branch = getGuard(stmt,pid,state);
				branchConstraints.add(branch);
				l.branchConstraint = branch;
				allBranches.add(l);
			}
		}
		
	}
	
	public BooleanExpression getGuard(Statement statement, int pid,
			State state) {
		Evaluation eval;

		try {
			eval = BranchConstraints.evaluator.evaluate(state, pid, statement.guard());
			return (BooleanExpression) eval.value;
		} catch (UnsatisfiablePathConditionException ex) {
			SymbolicUniverse universe = SARL.newStandardUniverse();
			return universe.falseExpression();
		}
	}
	
	private List<ErrorCfaLoc> constructErrorTrace (Trace<Transition, State> trace) {
		
		boolean debugLocal = false;
		List<ErrorCfaLoc> errorTrace = new ArrayList<>();
		List<TraceStepIF<State>> steps = trace.traceSteps();
		if (debugLocal) out.println(steps.toString());
		Iterator<TraceStepIF<State>> it = steps.iterator();
		State preState = trace.init();
		
		/* Extract Location->Statement pairs from TraceStep Iterator */
		while(it.hasNext()) {
			TraceStep step = ((TraceStep) it.next());
			Iterable<AtomicStep> atomicSteps = step.getAtomicSteps();
			for(AtomicStep atom : atomicSteps){
				Location l = atom.getTransition().statement().source();
				if (notFromOriginalSource(l)) {
					preState = atom.getPostState();
					continue;
				}
				
				Statement s = atom.getTransition().statement();
				
				/* CfaLocation -> CfaTransition logic */
				ErrorCfaLoc loc = new ErrorCfaLoc(l, preState);
				CfaTransition tr = new CfaTransition(s);
				errorTrace.add(loc);
				loc.setNextTransition(tr);
				
				preState = atom.getPostState();
			}
		}
		
		/* Set Entry and Exit Locations */
		assert (!errorTrace.isEmpty()) : "Error trace is empty";
		errorTrace.get(0).setEntryLocation();
		errorTrace.add(new ErrorCfaLoc()); // Internally sets a virtual exit
		
		doublyLinkErrorTrace(errorTrace);
		return errorTrace;
	}
	
	public void printBranches () {
		out.println("\nBEGIN Control Dependent Slice\n");
		for (BooleanExpression e : branchConstraints) {
			out.println(e);
		}
		out.println("\nEND Control Dependent Slice\n");
	}
	
	private void doublyLinkErrorTrace (List<ErrorCfaLoc> errorTrace) {
		for (int i = 0; i < errorTrace.size()-1; i++) {
			ErrorCfaLoc l = errorTrace.get(i);
			if (!l.isExitLocation()) {
				ErrorCfaLoc next = errorTrace.get(i+1);
				l.setNextLocation(next);
				l.successors.add(next);
			}
		}
	}
	
	private boolean notFromOriginalSource (Location l) {
		String fileName = l.getSource().getFileName();
		
		if (fileName.endsWith(".cvl") || 
				fileName.endsWith(".h") ||
				fileName.endsWith("Transformer")	) {
			return true;
		} else if (l.getNumOutgoing() == 1 &&
				l.getSoleOutgoing().toString().startsWith("$direct")) {
			return true;
		} else {
			return false;
		}
	}
	
	private String getType(String locationString) {
		
		Pattern p = Pattern.compile(".*__VERIFIER_nondet_(\\D+)\\(\\)");
		Matcher m = p.matcher(locationString);
		String type = "";
		if (m.find()) type = m.group(1);
		assert !type.isEmpty() : "Couldn't grab the svcomp input type.";
		
		return type;
	}
	
	private String getSourceLine(String locationString) {
		
		Pattern p = Pattern.compile(":(\\d+)\\.");
		Matcher m = p.matcher(locationString);
		String line = "";
		if (m.find()) line = m.group(1);
		assert !line.isEmpty();
		
		return line;
	}
	
}
