package edu.udel.cis.vsl.civl.slice.common;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.civl.model.IF.expression.AbstractFunctionCallExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.AddressOfExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.ArrayLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.BinaryExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.BooleanLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.BoundVariableExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.CastExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.CharLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DereferenceExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DerivativeCallExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DomainGuardExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DotExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DynamicTypeOfExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.FunctionGuardExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.FunctionIdentifierExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.HereOrRootExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.InitialValueExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.IntegerLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.LiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.MemoryUnitExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.ProcnullExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.QuantifiedExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.RealLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.RecDomainLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.RegularRangeExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.ScopeofExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.SelfExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.SizeofExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.SizeofTypeExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.StructOrUnionLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.SubscriptExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.SystemGuardExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.UnaryExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.UndefinedProcessExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.AssignStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement.StatementKind;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.util.IF.Pair;

/*
 * First run a dynamic control dependence analysis, as done in the
 * 2007 paper "Efficient Online Detection of Control Dependence."
 * This gives you the control dependency stack (CDS), which tells 
 * you, for each statement in a given trace, which branches it is
 * directly control dependent on.
 * 
 * Given the CDS, we first collect the control dependent branches
 * from the error statement.  We then step backwards along the
 * trace, keeping track of data dependent variables collected in
 * control dependent guards.
 */

public class DynamicDependence {
	
	private Set<ErrorCfaLoc> dependentBranches = new HashSet<>();
	private Set<ErrorCfaLoc> allBranches = new HashSet<>();
	/* TODO: This map needs to be from CfaLoc -> Set<Variable */
	private Map<Location,Set<Variable>> variablesMap = new HashMap<>();
	Map<ErrorCfaLoc,Stack<ControlDependencyElement>> cdsMap;
	
	private PrintStream out = System.out;
	private boolean debug = false;
	private boolean debugFull = false;
	
	public DynamicDependence(ErrorAutomaton tr, Map<CfaLoc,CfaLoc> ipd,
			Map<Location,CfaLoc> locationMap) {
		
		ControlDependence cd = new ControlDependence(tr, ipd, locationMap);
		try {cd.collectControlDependencyStack();} catch (IOException e) {e.printStackTrace();}
		cdsMap = cd.cdsMap;
		
		int errorIndex = tr.errorTrace.size() - 2; /* Second-to-last because of the virtual exit */
		ErrorCfaLoc errorLocation = tr.errorTrace.get(errorIndex);
		Stack<ControlDependencyElement> errorCds = cdsMap.get(errorLocation);
		collectAllBranches(tr);
		dependentBranches = collectControlDependentBranches(errorCds, dependentBranches);
		
		/* Examine the backwards trace starting from the location preceding the error */
		List<ErrorCfaLoc> subTrace = tr.errorTrace.subList(0, errorIndex);
		{
			Collections.reverse(subTrace);
			dependentBranches = findDataAndControlDependencies(subTrace, dependentBranches);
			Collections.reverse(subTrace);
		}
	}
	
	private void collectAllBranches(ErrorAutomaton ea) {
		allBranches.addAll(ea.allBranches);
	}

	private Set<ErrorCfaLoc> collectControlDependentBranches(
			Stack<ControlDependencyElement> cds, Set<ErrorCfaLoc> dependentBranches) {
		
		for (ControlDependencyElement c : cds) {
			dependentBranches.addAll(c.branchPoints);
		}
		
		return dependentBranches;
	}

	private Set<ErrorCfaLoc> findDataAndControlDependencies(List<ErrorCfaLoc> trace, 
			Set<ErrorCfaLoc> dependent) {
		
		Set<Variable> variables = new HashSet<>();
		/* Map later used in Static Dependence Analysis */
		Set<Variable> variablesOfInterest = new HashSet<>();
		
		for (ErrorCfaLoc l : trace) {
			
			if (debugFull) out.println("Looking at loc whose next transition is: "+l.nextTransition());
			if (isBranch(l)) {
				
				if (debug) out.println("Found a branch: "+l.nextTransition());
				
				if (dependent.contains(l)) {
					
					if (debug) out.println("...and this is a DEPENDENT branch.");
					Expression guard = l.nextTransition().statement.guard();
					Set<Variable> varsInGuard = collectVariables(guard);
					if (debug) {
						out.println("...Adding these variables to worklist:");
						for (Variable v : varsInGuard) out.println("    "+v);
					}
					variables.addAll(varsInGuard);
					
				}
				
			} else if (isFunctionCall(l)) {
				
				if (debug) out.println("Found a function call:"+l.nextTransition());
				
				CallOrSpawnStatement c = (CallOrSpawnStatement) l.nextTransition().statement;
				Map<Variable,Expression> paramToArgMap = mapParamsToArgs(c);
				if (debug) {
					out.println("\nParameter to Argument map:");
					for (Variable v : paramToArgMap.keySet()) {
						out.println(v+" maps to the expression: "+paramToArgMap.get(v));
					}
					out.println();
				}
				
				Set<Variable> params = paramToArgMap.keySet();
				/* This mapping can be seen as a parallel assignment,
				 * where the LHS variables are the formal parameters,
				 * and the RHS expressions are the actual parameters
				 */
				for (Variable lhs : params) {
					Expression rhs = paramToArgMap.get(lhs);
					assignUpdate(lhs, rhs, l, variables, dependent);
					/*
					if (variables.contains(v)) {
						variables.remove(v);
						Expression arg = paramToArgMap.get(v);
						Set<Variable> varsInArg = collectVariables(arg);
						for (Variable x : varsInArg) out.println("  "+x);
						variables.addAll(varsInArg);
					}
					*/
				}
				/* If the statement has an actual LHS, we can just remove it,
				 * because we already performed the RHS collection logic above.
				 */
				if (isAssign(l)) {
					Variable lhs = c.lhs().variableWritten();
					if (debug) out.println("Call statement with lhs: "+lhs);
					
					if (variables.contains(lhs)) {
						variables.remove(lhs);
					}
				}

				
			} else if (isAssign(l)) {
				
				if (debug) out.println("Found an assign: "+l.nextTransition());
				
				Statement s = l.nextTransition().statement;
				StatementKind kind = s.statementKind();
				Variable lhs = null;
				
				switch (kind) {
				
					case ASSIGN :
						AssignStatement as = (AssignStatement) s;
						assert as.getLhs() != null : "Assign statement has no LHS.";
						lhs = as.getLhs().variableWritten();
						if (debug) out.println("Assign statement with lhs: "+lhs);
						
						Expression rhs = as.rhs();
						assignUpdate(lhs, rhs, l, variables, dependent);
						/*
						if (variables.contains(lhs)) {
							variables.remove(lhs);
							variables.addAll(collectVariables(as.rhs()));
							dependent.addAll(collectControlDependentBranches(cdsMap.get(l),dependent));
						} */
						break;
						
					case CALL_OR_SPAWN :
						CallOrSpawnStatement cs = (CallOrSpawnStatement) s;
						lhs = cs.lhs().variableWritten();
						if (debug) out.println("Call statement with lhs: "+lhs);
						
						if (variables.contains(lhs)) {
							if (cs.toString().contains("__VERIFIER_nondet")) {
								variables.remove(lhs);
							} else {
								assert false : "LHS in variables worklist, but examining "
										+ "the RHS of a call statement not implemented.";
							}
						}
						break;
						
					default : assert false : "Neither assign nor call statement.";
					
				}
				
			} else if (l.isEntryLocation()) {
				
				if (debug) out.println("Program Entry; breaking out of loop");
				break;
				
			}
			/* Remove any instrumentation variables */
			/*
			Set<Variable> instrumentationVars = new HashSet<>();
			for (Variable v : variables) {
				String s = v.toString();
				if (s.contains("num_of_inputs_at_") || s.contains("input_at_")) {
					instrumentationVars.add(v);
				}
			}
			variables.removeAll(instrumentationVars);
			*/
			
			variablesOfInterest.addAll(variables);
			/* Overapproximate the variables-of-interest set if a location is in the map */
			if (variablesMap.containsKey(l.getCIVLLocation())) {
				variablesOfInterest.addAll(variablesMap.get(l.getCIVLLocation()));
			}
			variablesMap.put(l.getCIVLLocation(), variablesOfInterest);
		}
		
		return dependent;
	}
	
	private void assignUpdate(Variable lhs, Expression rhs, ErrorCfaLoc l,
			Set<Variable> worklist, Set<ErrorCfaLoc> dependent) {
		if (worklist.contains(lhs)) {
			worklist.remove(lhs);
			worklist.addAll(collectVariables(rhs));
			dependent.addAll(collectControlDependentBranches(cdsMap.get(l), dependent));
		}
	}
	
	private boolean isFunctionCall(ErrorCfaLoc l) {
		Statement stmt = l.nextTransition().statement;
		return (stmt instanceof CallOrSpawnStatement);
	}
	
	private boolean isAssign(ErrorCfaLoc l) {
		Statement stmt = l.nextTransition().statement;
		if (stmt instanceof AssignStatement) {
			return true;
		} else if (stmt instanceof CallOrSpawnStatement) {
			return (((CallOrSpawnStatement) stmt).lhs() != null);
		} else {
			return false;
		}
	}

	private boolean isBranch(ErrorCfaLoc l) {
		return (l.getCIVLLocation().getNumOutgoing() > 1);
	}
	
	public Set<ErrorCfaLoc> getDependentBranches() {
		return dependentBranches;
	}
	
	public Set<ErrorCfaLoc> getAllBranches() {
		return allBranches;
	}
	
	public Map<Location,Set<Variable>> getVariablesOfInterestMap() {
		return variablesMap;
	}
	
	private Map<Variable,Expression> mapParamsToArgs(CallOrSpawnStatement c) {
		List<Variable> params = c.function().parameters();
		List<Expression> args = c.arguments();
		
		Map<Variable,Expression> map = new HashMap<>();
		for (int i=0; i<params.size(); i++) {
			map.put(params.get(i), args.get(i));
		}
		return map;
	}
	
	/* Collect Variables from a given Expression */
	private Set<Variable> collectVariables(Expression expr) {
		Set<Variable> vars = new HashSet<Variable>();
		collectVariablesWorker(expr,vars);
		return vars;
	}
	
	private void collectVariablesWorker(Expression expr, Set<Variable> vars) {
		if (expr instanceof AbstractFunctionCallExpression) {
			List<Expression> args = ((AbstractFunctionCallExpression) expr).arguments();
			for (Expression e : args) 
				collectVariablesWorker(e,vars);
		} else if (expr instanceof AddressOfExpression) {
			collectVariablesWorker(((AddressOfExpression)expr).operand(),vars);
		} else if (expr instanceof ArrayLiteralExpression) {
			Expression[] elements = ((ArrayLiteralExpression) expr).elements();
			for (Expression e : elements)
				collectVariablesWorker(e,vars);
		} else if (expr instanceof BinaryExpression) {
			Expression left = ((BinaryExpression) expr).left();
			Expression right = ((BinaryExpression) expr).right();
			collectVariablesWorker(left,vars);
			collectVariablesWorker(right,vars);
		} else if (expr instanceof BooleanLiteralExpression) {
			// do nothing - is this correct?
		} else if (expr instanceof BoundVariableExpression) {
			// ask 
		} else if (expr instanceof CastExpression) {
			Expression e = ((CastExpression) expr).getExpression();
			collectVariablesWorker(e,vars);
		} else if (expr instanceof CharLiteralExpression) {
			// do nothing
		} else if (expr instanceof ConditionalExpression) {
			// ask 
			ConditionalExpression condExpr = (ConditionalExpression) expr;
			Expression cond = condExpr.getCondition();
			Expression trueBranch = condExpr.getTrueBranch();
			Expression falseBranch = condExpr.getFalseBranch();
			collectVariablesWorker(cond,vars);
			collectVariablesWorker(trueBranch,vars);
			collectVariablesWorker(falseBranch,vars);
		} else if (expr instanceof DereferenceExpression) {
			Expression p = ((DereferenceExpression) expr).pointer();
			collectVariablesWorker(p,vars);
		} else if (expr instanceof DerivativeCallExpression) {
			// what are these expressions?
			List<Pair<Variable, IntegerLiteralExpression>> partials = ((DerivativeCallExpression) expr).partials();
			for (Pair<Variable, IntegerLiteralExpression> p : partials) {
				vars.add(p.left);
			}
		} else if (expr instanceof DomainGuardExpression) {
			// ask what this is doing
		} else if (expr instanceof DotExpression) {
			// ask  
			Expression e = ((DotExpression) expr).structOrUnion();
			collectVariablesWorker(e,vars);
		} else if (expr instanceof DynamicTypeOfExpression) {
			// do nothing
		} else if (expr instanceof FunctionGuardExpression) {
			// ask 
			FunctionGuardExpression fgExpr = (FunctionGuardExpression) expr;
			Expression funcExpr = fgExpr.functionExpression();
			collectVariablesWorker(funcExpr,vars);
			List<Expression> args = fgExpr.arguments();
			for (Expression e : args) {
				collectVariablesWorker(e,vars);
			}
		} else if (expr instanceof FunctionIdentifierExpression) {
			// do nothing
		} else if (expr instanceof HereOrRootExpression) {
			// do nothing
		} else if (expr instanceof InitialValueExpression) {
			Variable v = ((InitialValueExpression) expr).variable();
			vars.add(v);
		} else if (expr instanceof IntegerLiteralExpression) {
			// do nothing
		} else if (expr instanceof LHSExpression) {
			Variable v = ((LHSExpression) expr).variableWritten();
			vars.add(v);
		} else if (expr instanceof LiteralExpression) {
			// do nothing
		} else if (expr instanceof MemoryUnitExpression) {
			Variable v = ((MemoryUnitExpression) expr).variable();
			vars.add(v);
		} else if (expr instanceof ProcnullExpression) {
			// do nothing (this is just an empty interface extending Expression)
		} else if (expr instanceof QuantifiedExpression) {
			// is this correct?
			Expression e = ((QuantifiedExpression) expr).expression();
			collectVariablesWorker(e,vars);
		} else if (expr instanceof RealLiteralExpression) {
			// do nothing
		} else if (expr instanceof RecDomainLiteralExpression) {
			// ask 
		} else if (expr instanceof RegularRangeExpression) {
			// this is a CIVL-C expression kind - can we ignore?
		} else if (expr instanceof ScopeofExpression) {
			LHSExpression lhsExpr = ((ScopeofExpression) expr).argument();
			Expression e = (Expression) lhsExpr;
			collectVariablesWorker(e,vars);
		} else if (expr instanceof SelfExpression) {
			// do nothing
		} else if (expr instanceof SizeofExpression) {
			Expression e = ((SizeofExpression) expr).getArgument();
			collectVariablesWorker(e,vars);
		} else if (expr instanceof SizeofTypeExpression) {
			// do nothing
		} else if (expr instanceof StructOrUnionLiteralExpression) {
			// do nothing
		} else if (expr instanceof SubscriptExpression) {
			// ask 
		}  else if (expr instanceof SystemGuardExpression) {
			List<Expression> args = ((SystemGuardExpression) expr).arguments();
			for (Expression e : args) {
				collectVariablesWorker(e,vars);
			}
		} else if (expr instanceof UnaryExpression) {
			Expression e = ((UnaryExpression) expr).operand();
			collectVariablesWorker(e,vars);
		} else if (expr instanceof UndefinedProcessExpression) {
			// do nothing	
		} else if (expr instanceof VariableExpression) {
			vars.add(((VariableExpression)expr).variable());
		} else {
			assert false : expr;
		}
	}

}
