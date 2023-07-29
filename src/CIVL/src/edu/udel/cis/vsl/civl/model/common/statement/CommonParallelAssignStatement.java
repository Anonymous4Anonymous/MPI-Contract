package edu.udel.cis.vsl.civl.model.common.statement;

import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.location.Location;
import edu.udel.cis.vsl.civl.model.IF.statement.ParallelAssignStatement;
import edu.udel.cis.vsl.civl.model.IF.statement.Statement;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.sarl.IF.SymbolicUniverse;

public class CommonParallelAssignStatement extends CommonStatement
		implements
			ParallelAssignStatement {

	private List<Pair<LHSExpression, Expression>> assignPairs;

	public CommonParallelAssignStatement(CIVLSource source, Location sourceLoc,
			Expression guard,
			List<Pair<LHSExpression, Expression>> assignPairs) {
		super(source, guard.expressionScope(), guard.lowestScope(), sourceLoc,
				guard);
		this.assignPairs = assignPairs;
	}

	@Override
	public Statement replaceWith(ConditionalExpression oldExpression,
			Expression newExpression) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.PARALLEL_ASSIGN;
	}

	@Override
	public List<Pair<LHSExpression, Expression>> assignments() {
		return this.assignPairs;
	}

	@Override
	protected void calculateConstantValueWork(SymbolicUniverse universe) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj))
			if (obj instanceof CommonParallelAssignStatement) {
				CommonParallelAssignStatement other = (CommonParallelAssignStatement) obj;

				return other.assignPairs.equals(assignPairs);
			}
		return false;
	}

}
