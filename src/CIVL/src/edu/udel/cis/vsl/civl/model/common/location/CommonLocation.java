/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common.location;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import edu.udel.cis.vsl.civl.model.IF.CIVLFunction;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.BooleanLiteralExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.MemoryUnitExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.CallOrSpawnStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.NoopStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.model.common.CommonSourceable;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * The parent of all locations.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public class CommonLocation extends CommonSourceable implements Location {

	/* ************************** Instance Fields ************************** */

	/**
	 * Store the static analysis result. True iff all outgoing statements from
	 * this location are purely local.
	 */
	private boolean allOutgoingPurelyLocal = false;

	/**
	 * The atomic kind of this location, initialized as NONE.
	 */
	private AtomicKind atomicKind = AtomicKind.NONE;

	/**
	 * The function that this location belongs to
	 */
	private CIVLFunction function;

	/**
	 * Return true if the current location or any location reachable from the
	 * current location has any dereference operation.
	 */
	private boolean hasDeref = false;

	/**
	 * The unique id of this location
	 */
	private int id;

	/**
	 * The impact scope of a location is required in the enabler when an
	 * atomic/atom block is encountered, in which case the impact scope of all
	 * statements in the atomic block should be considered.
	 */
	private Scope impactScopeOfAtomicOrAtomBlock;

	/**
	 * The list of incoming statements, i.e., statements that targeting at this
	 * location.
	 */
	private ArrayList<Statement> incoming = new ArrayList<>();

	/**
	 * Store the static loop analysis result. True iff this location is possible
	 * to form a loop. When translating a loop node, the loop branch location is
	 * marked to be loopPossible, which captures most cases of loops. There
	 * might also be loops caused by goto statements, which only could be
	 * detected after the whole AST tree is translated.
	 */
	private boolean loopPossible = false;

	/**
	 * The list of outgoing statements, i.e., statements that has this location
	 * as the source location.
	 */
	private ArrayList<Statement> outgoing = new ArrayList<>();

	/**
	 * The status denoting if this location is purely local, initialized as
	 * false. A location is considered as purely local if it satisfies the
	 * following conditions.
	 * <ol>
	 * <li>it has exactly one incoming edge; (to avoid loop)</li>
	 * <li>if it is not the starting point of an $atomic block, then all its
	 * outgoing statements should be purely local;</li>
	 * <li>if it is the starting point of an $atomic block, then all statements
	 * reachable within that $atomic block are purely local.</li>
	 * </ol>
	 */
	private boolean purelyLocal = true;

	private Set<Variable> writableVariables;

	/**
	 * The static scope that this location belongs to.
	 */
	private Scope scope;

	/**
	 * Is this the started location of a function?
	 */
	private boolean isStart = false;

	private Set<MemoryUnitExpression> impactMemUnits;
	private Set<MemoryUnitExpression> reachableMemUnitsWtPointer;
	private Set<MemoryUnitExpression> reachableMemUnitsWoPointer;
	private Set<CallOrSpawnStatement> systemCalls;

	private Set<Location> reachableLoacations;
	/**
	 * upper bound of the number of spawn statements reachable from this
	 * location. -1 stands for infinity.
	 */
	private int spawnBound = 0;

	/**
	 * true iff this location has more than one incoming location and is inside
	 * a loop.
	 */
	private boolean isInLoop = false;

	private boolean isSafeLoop = false;

	/**
	 * true iff this location is in a side-effect-free infinite loop
	 */
	private boolean isInNoopLoop = false;

	/**
	 * true iff this location has two outgoing statements with guards being expr
	 * and !expr.
	 */
	private boolean isBinaryBranch = false;

	/**
	 * true iff this location is a switch or $choose statement location and it
	 * has a 'default' case.
	 */
	private boolean isSwitchOrChooseWithDefault = false;

	private boolean isGuarded = false;

	private boolean isSleep = false;

	/**
	 * <p>
	 * a flag indicating if this location is the entry of a local block
	 * </p>
	 * <p>
	 * The location is the entry of a local block if it is associated with a
	 * system function call to <code>$local_start()</code>
	 * </p>
	 */
	private boolean isEntryOfLocalBlock = false;

	/**
	 * True iff this location is an atomic block entry and the termination of
	 * the atomic block is NOT determined.
	 */
	private boolean isUnsafeAtomicEntry = false;

	/* **************************** Constructors *************************** */

	public CommonLocation(CIVLSource source, boolean isSleep) {
		super(source);
		this.isSleep = isSleep;
		this.id = -1;
	}

	/**
	 * The parent of all locations.
	 * 
	 * @param source
	 *            The corresponding source (file, line, column, text, etc) of
	 *            this location
	 * @param scope
	 *            The scope containing this location.
	 * @param id
	 *            The unique id of this location.
	 */
	public CommonLocation(CIVLSource source, Scope scope, int id) {
		super(source);
		this.scope = scope;
		this.id = id;
		this.function = scope.function();
		this.reachableLoacations = new HashSet<>();
	}

	/* ************************ Methods from Location ********************** */

	@Override
	public void addIncoming(Statement statement) {
		incoming.add(statement);
	}

	@Override
	public void addOutgoing(Statement statement) {
		outgoing.add(statement);
	}

	@Override
	public boolean allOutgoingPurelyLocal() {
		return this.allOutgoingPurelyLocal;
	}

	@Override
	public AtomicKind atomicKind() {
		return this.atomicKind;
	}

	@Override
	public boolean enterAtomic() {
		return this.atomicKind == AtomicKind.ATOMIC_ENTER;
	}

	@Override
	public CIVLFunction function() {
		return function;
	}

	@Override
	public Statement getIncoming(int i) {
		return incoming.get(i);
	}

	@Override
	public int getNumIncoming() {
		return incoming.size();
	}

	@Override
	public int getNumOutgoing() {
		return outgoing.size();
	}

	@Override
	public Statement getOutgoing(int i) {
		return outgoing.get(i);
	}

	@Override
	public Statement getSoleOutgoing() {
		int size = outgoing.size();

		if (size >= 1) {
			Statement result = outgoing.iterator().next();

			if (size > 1) {
				throw new CIVLInternalException(
						"Expected 1 outgoing transition but saw " + size,
						result.getSource());
			}
			return result;
		}
		throw new CIVLInternalException(
				"Expected 1 outgoing transition but saw 0 at " + this
						+ " in function " + function,
				this.getSource());
	}

	@Override
	public int id() {
		return id;
	}

	@Override
	public Scope impactScopeOfAtomicOrAtomBlock() {
		return this.impactScopeOfAtomicOrAtomBlock;
	}

	@Override
	public Iterable<Statement> incoming() {
		return incoming;
	}

	@Override
	public boolean isPurelyLocal() {
		return this.purelyLocal;
	}

	@Override
	public boolean leaveAtomic() {
		return this.atomicKind == AtomicKind.ATOMIC_EXIT;
	}

	private Statement singleNoopOutgoing(Location location) {
		Statement outgoing = null;

		for (Statement stmt : location.outgoing()) {
			SymbolicExpression guard = stmt.guard().constantValue();

			if (guard != null && guard.isFalse())
				continue;
			if (outgoing != null) {
				// more than one enabled outgoing statement
				return null;
			}
			outgoing = stmt;
		}
		if (outgoing instanceof NoopStatement)
			return outgoing;
		return null;
	}

	@Override
	public void loopAnalysis() {
		if (!this.isInNoopLoop) {
			Stack<CommonLocation> visited = new Stack<>();
			CommonLocation current = this;
			Location loopLocation = null;

			while (current != null) {
				Statement singleEnabledNoop = this.singleNoopOutgoing(current);

				visited.push(current);
				if (singleEnabledNoop != null) {
					CommonLocation target = (CommonLocation) singleEnabledNoop
							.target();

					if (visited.contains(target)) {
						loopLocation = target;
						target.setInNoopLoop(true);
						current = null;
					} else {
						current = target;
					}
				} else
					return;
			}
			if (loopLocation != null) {
				do {
					current = visited.pop();
					current.setInNoopLoop(true);
				} while (!visited.isEmpty() && !current.equals(loopLocation));
			}
		}

		// if (this.loopPossible) {
		// // this is the loop entrance of a loop statement
		// // check if it is finite
		// } else {
		// // this is not the loop entrance of a loop statement, check if it is
		// // possible to form a loop by other statements like goto
		// }
	}

	@Override
	public Iterable<Statement> outgoing() {
		return outgoing;
	}

	@Override
	public void print(String prefix, PrintStream out, boolean isDebug) {
		String targetLocation = null;
		String guardString = "(true)";
		String gotoString;
		String headString = null;

		if (isDebug) {
			headString = prefix + "location " + id() + " (scope: " + scope.id();
			if (purelyLocal)
				headString += ", purely local";
			if (loopPossible)
				headString += ", loop";
			headString += ", " + atomicKind;
			if (this.enterAtomic())
				headString += ", atomic's impact scope: "
						+ this.impactScopeOfAtomicOrAtomBlock.id();
			if (this.leaveAtomic())
				headString += ", atomic-end";
			if (this.writableVariables != null
					&& !this.writableVariables.isEmpty()) {
				headString += ", variablesWritten <";
				for (Variable variable : this.writableVariables) {
					headString += variable.name() + "-" + variable.scope().id()
							+ ",";
				}
				headString += ">";
			}
			headString += ")";
		} else {
			headString = prefix + "location " + id() + " (scope: " + scope.id()
					+ ")";
		}
		out.println(headString);
		if (isDebug) {
			if (impactMemUnits != null && !impactMemUnits.isEmpty()) {
				out.print(prefix);
				out.print("impact memory units: ");
				for (MemoryUnitExpression memUnit : this.impactMemUnits) {
					out.print(memUnit + "\t");
				}
				out.println();
			}
			if (!reachableMemUnitsWoPointer.isEmpty()
					|| !reachableMemUnitsWtPointer.isEmpty()) {
				out.print(prefix);
				out.print("reachable memory units: ");
				for (MemoryUnitExpression memUnit : this.reachableMemUnitsWoPointer) {
					out.print(memUnit + "\t");
				}
				for (MemoryUnitExpression memUnit : this.reachableMemUnitsWtPointer) {
					out.print(memUnit + "\t");
				}
				out.println();
			}
		}
		for (Statement statement : outgoing) {
			if (statement.target() != null) {
				targetLocation = "" + statement.target().id();
			}
			if (statement.guard() != null) {
				guardString = "(" + statement.guard() + ")";
			}
			gotoString = prefix + "| " + "when " + guardString + " " + statement
					+ " @ " + statement.getSource().getSummary(false) + " ;";
			if (targetLocation != null) {
				gotoString += " goto location " + targetLocation;
			}
			out.println(gotoString);
		}
	}

	private void purelyLocalAnalysisForAtomic() {
		int atomicCount = 0;
		Set<Integer> visited = new HashSet<Integer>();
		Stack<Location> working = new Stack<>();

		working.add(this);
		while (!working.isEmpty()) {
			Location current = working.pop();

			visited.add(current.id());
			if (current.enterAtomic())
				atomicCount++;
			else if (current.leaveAtomic())
				atomicCount--;
			for (Statement stmt : current.outgoing()) {
				Location target;

				if (!stmt.isPurelyLocal()) {
					this.purelyLocal = false;
					return;
				}
				target = stmt.target();
				if (target != null && atomicCount != 0
						&& !visited.contains(target.id()))
					working.push(target);
			}
		}

	}

	@Override
	public void purelyLocalAnalysis() {
		// Usually, a location is purely local if it has exactly one outgoing
		// statement that is purely local
		if ((this.isStart && incoming.size() > 0)
				|| (incoming.size() > 1 && !this.isSafeLoop)) {
			this.purelyLocal = false;
			return;
		}
		// a location that enters an atomic/atom block is considered as purely
		// local only
		// if all the statements that are to be executed in the atomic block are
		// purely local
		if (this.atomicKind == AtomicKind.ATOMIC_ENTER)
			purelyLocalAnalysisForAtomic();
		else {
			for (Statement s : this.outgoing) {
				this.purelyLocal = this.purelyLocal && s.isPurelyLocal();
			}
		}
	}

	// @Override
	// public void purelyLocalAnalysisForOutgoing() {
	// // a location that enters an atomic block is considered as atomic only
	// // if all the statements that are to be executed in the atomic block are
	// // purely local
	// if (this.atomicKind == AtomicKind.ATOM_ENTER) {
	// Stack<Integer> atomicFlags = new Stack<Integer>();
	// Location newLocation = this;
	// Set<Integer> checkedLocations = new HashSet<Integer>();
	//
	// do {
	// Statement s = newLocation.getOutgoing(0);
	//
	// checkedLocations.add(newLocation.id());
	// if (!s.isPurelyLocal()) {
	// this.allOutgoingPurelyLocal = false;
	// return;
	// }
	// if (newLocation.enterAtom())
	// atomicFlags.push(1);
	// if (newLocation.leaveAtom())
	// atomicFlags.pop();
	// newLocation = s.target();
	// if (checkedLocations.contains(newLocation.id()))
	// newLocation = null;
	// } while (newLocation != null && !atomicFlags.isEmpty());
	// this.allOutgoingPurelyLocal = true;
	// return;
	// }
	//
	// for (Statement s : outgoing) {
	// if (!s.isPurelyLocal())
	// this.allOutgoingPurelyLocal = false;
	// }
	// this.allOutgoingPurelyLocal = true;
	// }

	@Override
	public void removeIncoming(Statement statement) {
		incoming.remove(statement);
	}

	@Override
	public void removeOutgoing(Statement statement) {
		outgoing.remove(statement);
	}

	@Override
	public Scope scope() {
		return scope;
	}

	@Override
	public void setEnterAtomic() {
		this.atomicKind = AtomicKind.ATOMIC_ENTER;
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setImpactScopeOfAtomicOrAtomBlock(Scope scope) {
		this.impactScopeOfAtomicOrAtomBlock = scope;
	}

	@Override
	public void setLeaveAtomic() {
		this.atomicKind = AtomicKind.ATOMIC_EXIT;
	}

	// TODO improve the static analysis of loop locations
	@Override
	public void setLoopPossible(boolean possible) {
		this.loopPossible = possible;
	}

	@Override
	public void setScope(Scope scope) {
		this.scope = scope;
		this.function = scope.function();
	}

	/* ************************ Methods from Object ************************ */

	@Override
	public boolean equals(Object that) {
		if (that instanceof CommonLocation) {
			return (((CommonLocation) that).id() == id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		// final int prime = 31;
		// int result = 1;
		//
		// result = prime * result + id;
		// result = prime * result + ((scope == null) ? 0 : scope.hashCode());
		// return result;
		return id;
	}

	@Override
	public String toString() {
		return "Location " + id;
	}

	@Override
	public void computeWritableVariables(Set<Variable> addressedOfVariables) {
		Set<Integer> checkedLocationIDs = new HashSet<>();
		Stack<Location> workings = new Stack<>();
		Set<CIVLFunction> checkedFunctions = new HashSet<>();
		Stack<CIVLFunction> workingFunctions = new Stack<>();
		Scope scope = this.scope;
		Set<Variable> variableWritten = new HashSet<>();

		workings.push(this);
		while (!workings.isEmpty()) {
			Location location = workings.pop();

			checkedLocationIDs.add(location.id());
			for (Statement statement : location.outgoing()) {
				// TODO special handling for call statements with reads/assigns
				// contracts
				// for example
				// assigns \nothing;
				// reads *x;
				// int getValue(int *x){return *x;}
				// then: getValue(&a); wouldn't add a into the written-variable
				// set.
				Set<Variable> statementResult = statement
						.variableAddressedOf(scope);
				Location target = statement.target();

				if (statement.hasDerefs()) {
					if (!this.hasDeref) {
						this.hasDeref = true;
						variableWritten.addAll(addressedOfVariables);
					}
				}
				if (statementResult != null)
					variableWritten.addAll(statementResult);
				if (target != null)
					if (!checkedLocationIDs.contains(target.id()))
						workings.push(target);
				if (statement instanceof CallOrSpawnStatement) {
					CallOrSpawnStatement call = (CallOrSpawnStatement) statement;

					// if (call.isCall()) {
					CIVLFunction function = call.function();

					if (function != null
							&& !checkedFunctions.contains(function)) {
						workingFunctions.add(function);
					}
					// }
				}
			}
		}
		while (!workingFunctions.isEmpty()) {
			CIVLFunction function = workingFunctions.pop();

			checkedFunctions.add(function);
			for (Location location : function.locations()) {
				for (Statement statement : location.outgoing()) {
					Set<Variable> statementResult = statement
							.variableAddressedOf(scope);

					if (statement.hasDerefs()) {
						if (!this.hasDeref) {
							this.hasDeref = true;
							variableWritten.addAll(addressedOfVariables);
						}
					}
					if (statementResult != null)
						variableWritten.addAll(statementResult);
					if (statement instanceof CallOrSpawnStatement) {
						CIVLFunction newFunction = ((CallOrSpawnStatement) statement)
								.function();

						if (newFunction != null
								&& !checkedFunctions.contains(newFunction)) {
							workingFunctions.add(newFunction);
						}
					}
				}
			}
		}
		this.writableVariables = variableWritten;
	}

	private void computeSpawnBound() {
		for (Location location : this.reachableLoacations) {
			if (location.hasSpawn()) {
				this.spawnBound++;
				return;
			}
			for (Statement statement : location.outgoing()) {
				if (statement instanceof CallOrSpawnStatement) {
					if (((CallOrSpawnStatement) statement).isSpawn()) {
						this.spawnBound++;
						return;
					}
				}
			}
		}
	}

	@Override
	public void staticAnalysis() {
		isGuardedAnalysis();
		isInLoopAnalysis();
		this.computeReachableLocations();
		this.computeSpawnBound();
	}

	private void isInLoopAnalysis() {
		if (this.incoming.size() > 1) {
			Stack<Location> working = new Stack<>();
			Set<Integer> visited = new HashSet<>();
			Location current;

			working.push(this);
			visited.add(this.id);
			while (!working.isEmpty()) {
				current = working.pop();
				for (Statement stmt : current.outgoing()) {
					Location target = stmt.target();

					if (target != null) {
						int targetId = target.id();

						if (targetId == this.id) {
							this.isInLoop = true;
							return;
						}
						if (!visited.contains(targetId)) {
							working.push(target);
							visited.add(targetId);
						}
					}
				}
			}
		}
	}

	private void isGuardedAnalysis() {
		if (this.outgoing.size() == 1) {
			Expression guard = outgoing.get(0).guard();

			if (!(guard instanceof BooleanLiteralExpression))
				this.isGuarded = true;
		}
	}

	/**
	 * computes the set of reachable locations from the current location.
	 */
	private void computeReachableLocations() {
		BitSet checkedLocationIDs = new BitSet();
		Stack<Location> workings = new Stack<>();
		Set<CIVLFunction> checkedFunctions = new HashSet<>();

		workings.push(this);
		checkedLocationIDs.set(this.id());
		this.reachableLoacations.add(this);
		while (!workings.isEmpty()) {
			Location location = workings.pop();

			for (Statement statement : location.outgoing()) {
				Location target = statement.target();

				if (target != null)
					if (!checkedLocationIDs.get(target.id())) {
						workings.push(target);
						checkedLocationIDs.set(target.id());
						this.reachableLoacations.add(target);
					}
				if (statement instanceof CallOrSpawnStatement) {
					CallOrSpawnStatement call = (CallOrSpawnStatement) statement;

					if (call.isCall()) {
						CIVLFunction function = call.function();

						if (function != null
								&& !checkedFunctions.contains(function)) {
							checkedFunctions.add(function);
							for (Location functionLoc : function.locations()) {
								if (functionLoc != null) {
									int funcLocID = functionLoc.id();

									if (!checkedLocationIDs.get(funcLocID)) {
										workings.push(functionLoc);
										checkedLocationIDs.set(funcLocID);
										this.reachableLoacations.add(target);
									}
								}
							}

						}
					}

				}
			}
		}
	}

	private void setInNoopLoop(boolean value) {
		this.isInNoopLoop = value;
	}

	@Override
	public Set<Variable> writableVariables() {
		return this.writableVariables;
	}

	@Override
	public boolean hasDerefs() {
		return this.hasDeref;
	}

	@Override
	public void setAsStart(boolean value) {
		this.isStart = value;
	}

	@Override
	public boolean isStart() {
		return isStart;
	}

	@Override
	public Set<MemoryUnitExpression> impactMemUnits() {
		return this.impactMemUnits;
	}

	@Override
	public Set<MemoryUnitExpression> reachableMemUnitsWtPointer() {
		return this.reachableMemUnitsWtPointer;
	}

	@Override
	public Set<MemoryUnitExpression> reachableMemUnitsWoPointer() {
		return this.reachableMemUnitsWoPointer;
	}

	@Override
	public void setImpactMemoryUnit(Set<MemoryUnitExpression> impacts) {
		this.impactMemUnits = impacts;
	}

	@Override
	public void setReachableMemUnitsWtPointer(
			Set<MemoryUnitExpression> reachable) {
		this.reachableMemUnitsWtPointer = reachable;
	}

	@Override
	public void setReachableMemUnitsWoPointer(
			Set<MemoryUnitExpression> reachable) {
		this.reachableMemUnitsWoPointer = reachable;
	}

	@Override
	public void setSystemCalls(Set<CallOrSpawnStatement> systemCalls) {
		this.systemCalls = systemCalls;
	}

	@Override
	public Set<CallOrSpawnStatement> systemCalls() {
		return this.systemCalls;
	}

	@Override
	public boolean hasSpawn() {
		return this.spawnBound > 0;
	}

	@Override
	public void setSafeLoop(boolean value) {
		this.isSafeLoop = value;
	}

	@Override
	public boolean isSafeLoop() {
		return this.isSafeLoop;
	}

	@Override
	public boolean isInNoopLoop() {
		return this.isInNoopLoop;
	}

	@Override
	public Expression pathCondition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPathcondition(Expression expression) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isBinaryBranching() {
		assert !isBinaryBranch || this.outgoing.size() == 2;
		return this.isBinaryBranch;
	}

	@Override
	public void setBinaryBranching(boolean value) {
		this.isBinaryBranch = value;
	}

	@Override
	public boolean isGuardedLocation() {
		return isGuarded;
	}

	@Override
	public boolean isInLoop() {
		return this.isInLoop;
	}

	@Override
	public boolean isSleep() {
		return this.isSleep;
	}

	@Override
	public void setSwitchOrChooseWithDefault() {
		isSwitchOrChooseWithDefault = true;
	}

	@Override
	public boolean isSwitchOrChooseWithDefault() {
		return isSwitchOrChooseWithDefault;
	}

	@Override
	public boolean isEntryOfUnsafeAtomic() {
		return this.isUnsafeAtomicEntry;
	}

	@Override
	public void setEntryOfUnsafeAtomic(boolean unsafe) {
		this.isUnsafeAtomicEntry = unsafe;
	}

	@Override
	public boolean isEntryOfLocalBlock() {
		return isEntryOfLocalBlock;
	}

	@Override
	public void setIsEntryOfLocalBlock(boolean isEntryOfLocalBlock) {
		this.isEntryOfLocalBlock = isEntryOfLocalBlock;
	}
}
