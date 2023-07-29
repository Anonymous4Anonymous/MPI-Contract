/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common.statement;

import java.util.ArrayList;
import java.util.List;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Model;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.location.Location.AtomicKind;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.common.CommonSourceable;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;

/**
 * The parent of all statements.
 * 
 * @author Timothy K. Zirkel (zirkel)
 * 
 */
public abstract class CommonStatement extends CommonSourceable
		implements
			Statement {

	private Location source;
	private Location target;
	private Expression guard;
	private Model model;
	/**
	 * The highest scope that this statement accesses. Null if no variable is
	 * accessed.
	 */
	protected Scope statementScope = null;
	protected boolean hasDerefs = false;
	protected boolean purelyLocal = true;
	private boolean isPurelyLocalAnalysisDone = false;
	/**
	 * The lowest scope that this statement accesses. Null if no variable is
	 * accessed.
	 */
	protected Scope lowestScope = null;

	/**
	 * Has this statement been reached? false initially.
	 */
	protected boolean reached = false;

	/**
	 * The parent of all statements.
	 * 
	 * @param source
	 *            The location that is the source of this statement.
	 */
	public CommonStatement(CIVLSource civlSource, Scope hscope, Scope lscope,
			Location source, Expression guard) {
		super(civlSource);
		this.source = source;
		this.guard = guard;
		if (source != null)
			source.addOutgoing(this);
		this.statementScope = hscope;
		this.lowestScope = lscope;
	}

	public CommonStatement() {
		super(null);
	}

	/**
	 * @return The location that is the source of this statement.
	 */
	@Override
	public Location source() {
		return source;
	}

	/**
	 * @return The location that is the target of this statement.
	 */
	@Override
	public Location target() {
		return target;
	}

	/**
	 * @return The boolean-valued guard expression for this statement.
	 */
	@Override
	public Expression guard() {
		return guard;
	}

	/**
	 * @return The model to which this statement belongs.
	 */
	@Override
	public Model model() {
		return model;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	@Override
	public void setSource(Location source) {
		if (this.source != null) {
			this.source.removeOutgoing(this);
		}
		this.source = source;
		source.addOutgoing(this);
	}

	@Override
	public void setTarget(Location target) {
		if (this.target != null) {
			this.target().removeIncoming(this);
		}
		if (target != null) {
			this.target = target;
			target.addIncoming(this);
		}
	}

	@Override
	public void setTargetTemp(Location target) {
		if (this.target != null) {
			this.target().removeIncoming(this);
		}
		this.target = target;
	}

	@Override
	public void setSourceTemp(Location source) {
		this.source = source;
	}

	@Override
	public void setGuard(Expression guard) {
		this.guard = guard;
		statementScope = join(statementScope, guard.expressionScope());
	}

	/**
	 * @param model
	 *            The Model to which this statement belongs.
	 */
	@Override
	public void setModel(Model model) {
		this.model = model;
	}

	/**
	 * @return The highest scope accessed by this statement. Null if no
	 *         variables accessed.
	 */
	@Override
	public Scope statementScope() {
		return statementScope;
	}

	// /**
	// * @param statementScope
	// * The highest scope accessed by this statement. Null if no
	// * variables accessed.
	// */
	// @Override
	// public void setStatementScope(Scope statementScope) {
	// this.statementScope = statementScope;
	// }

	/**
	 * @param s0
	 *            A scope. May be null.
	 * @param s1
	 *            A scope. May be null.
	 * @return The scope that is the join, or least common ancestor in the scope
	 *         tree, of s0 and s1. Null if both are null. If exactly one of s0
	 *         and s1 are null, returns the non-null scope.
	 */
	protected Scope join(Scope s0, Scope s1) {
		List<Scope> s0Ancestors = new ArrayList<Scope>();
		Scope s0Ancestor = s0;
		Scope s1Ancestor = s1;

		if (s0 == null) {
			return s1;
		} else if (s1 == null) {
			return s0;
		}
		s0Ancestors.add(s0Ancestor);
		while (s0Ancestor.parent() != null) {
			s0Ancestor = s0Ancestor.parent();
			s0Ancestors.add(s0Ancestor);
		}
		while (true) {
			if (s0Ancestors.contains(s1Ancestor)) {
				return s1Ancestor;
			}
			s1Ancestor = s1Ancestor.parent();
		}
	}

	@Override
	public void calculateDerefs() {
		this.hasDerefs = false;
	}

	@Override
	public boolean hasDerefs() {
		return this.hasDerefs;
	}

	@Override
	public void purelyLocalAnalysisOfVariables(Scope funcScope) {
		if (guard != null) {
			guard.purelyLocalAnalysisOfVariables(funcScope);
		}
	}

	@Override
	public boolean isPurelyLocal() {
		if (!isPurelyLocalAnalysisDone) {
			purelyLocalAnalysis();
			isPurelyLocalAnalysisDone = true;
		}
		return purelyLocal;
	}

	protected void purelyLocalAnalysis() {
		this.guard.purelyLocalAnalysis();
		this.purelyLocal = this.guard.isPurelyLocal();
	}

	@Override
	public void replaceWith(ConditionalExpression oldExpression,
			VariableExpression newExpression) {
		this.guard.replaceWith(oldExpression, newExpression);
	}

	/**
	 * Attempt to generate a new guard by replacing a certain conditional
	 * expression with a new expression
	 * 
	 * @param oldExpression
	 *            The conditional expression
	 * @param newExpression
	 *            The new expression
	 * @return Null if guard doesn't contain the given conditional expression,
	 *         otherwise return the new guard
	 */
	protected Expression guardReplaceWith(ConditionalExpression oldExpression,
			Expression newExpression) {
		Expression newGuard = guard.replaceWith(oldExpression, newExpression);

		return newGuard;
	}

	@Override
	public String toStepString(AtomicKind atomicKind, int atomCount,
			boolean atomicLockVarChanged) {
		String result = "  " + this.locationStepString();

		result += ": ";
		switch (atomicKind) {
			case ATOMIC_ENTER :
				if (atomicLockVarChanged) {
					result += toString() + " ";
				} else
					result += "ENTER_ATOMIC (atomicCount++) ";
				result += Integer.toString(atomCount - 1);
				break;
			case ATOMIC_EXIT :
				if (atomicLockVarChanged) {
					result += toString() + " ";
				} else
					result += "LEAVE_ATOMIC (atomicCount--) ";
				result += Integer.toString(atomCount);
				break;
			default :
				result += toString();
		}
		result += " at ";
		result += this.summaryOfSource();
		result += ";";
		return result;
	}

	@Override
	public String summaryOfSource() {
		if (getSource() != null)
			return getSource().getSummary(false);
		else
			return source.getSource().getSummary(false);
	}

	@Override
	public String locationStepString() {
		String result = (source == null ? "??" : source.id()) + "->";

		if (this.target != null)
			result += target.id();
		else
			result += "RET";
		return result;
	}

	@Override
	public void calculateConstantValue(SymbolicUniverse universe) {
		this.guard.calculateConstantValue(universe);
		this.calculateConstantValueWork(universe);
	}

	@Override
	public Scope lowestScope() {
		return this.lowestScope;
	}

	protected abstract void calculateConstantValueWork(
			SymbolicUniverse universe);

	@Override
	public void reached() {
		this.reached = true;
	}

	@Override
	public boolean reachable() {
		return this.reached;
	}

	@Override
	public boolean containsHere() {
		if (guard.containsHere())
			return true;
		return containsHereWork();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonStatement) {
			CommonStatement other = (CommonStatement) obj;

			if (other.source.id() == source.id()) {
				if (statementScopeEquals(other))
					if (target == null)
						return other.target == null;
					else if (other.target != null)
						return target.id() == other.target.id();
			}
		}
		return false;
	}

	/**
	 * @param other
	 *            Another instance of CommonStatement
	 * @return True if and only if the 'other' instance has a equivalent
	 *         statement scope as this ones.
	 */
	private boolean statementScopeEquals(CommonStatement other) {
		if (other.statementScope == statementScope)
			return true;
		else if (other.statementScope != null && statementScope != null)
			return other.statementScope.id() == statementScope.id();
		return false;
	}

	/**
	 * Compare two objects, both of them can be null.
	 * 
	 * @param o0
	 * @param o1
	 * @return True if and only if o0 is equivalent to o1 or both of them are
	 *         null.
	 */
	protected boolean nullableObjectEquals(Object o0, Object o1) {
		if (o0 == o1)
			return true;
		if (o0 != null && o1 != null)
			return o0.equals(o1);
		return false;
	}

	protected boolean containsHereWork() {
		return false;
	}

}
