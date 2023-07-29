package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.util.IF.Pair;

/**
 * Constant propagation implementation
 * 
 * The lattice is a map from variables, identified by entity, to singleton
 * constant values or "top" (indicated by the first element of the pair).
 * 
 * @author dxu Most methods are stolen from dwyer, some modifications are
 *         indicated.
 */

public class ConstantPropagation
		extends
			DataFlowFramework<Pair<Entity, Pair<Boolean, ConstantNode>>> {
	private static ConstantPropagation instance = null;

	Function currentFunction;

	ControlFlowAnalysis cfa;

	/**
	 * DFAs are singletons. This allows them to be applied incrementally across
	 * a code base.
	 */
	protected ConstantPropagation() {
	};

	public static ConstantPropagation getInstance() {
		if (instance == null) {
			instance = new ConstantPropagation();
		}
		return instance;
	}

	public void clear() {
		super.clear();
		instance = null;
		cfa.clear();
	}

	@Override
	public void analyze(Function f) {
		if (analyzedFunctions.contains(f))
			return;
		analyzedFunctions.add(f);
		currentFunction = f;

		// Perform control flow analysis (if needed)
		cfa = ControlFlowAnalysis.getInstance();
		cfa.analyze(f);

		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> init = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		// Unprocessed nodes are assigned an empty set
		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> bottom = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		computeFixPoint(init, bottom);
	}

	/*
	 * The next three methods are stolen from reaching definitions. They should
	 * be factored out into some utilities.
	 */
	private boolean isAssignment(final ASTNode s) {
		if (s instanceof ExpressionStatementNode) {
			ExpressionNode e = ((ExpressionStatementNode) s).getExpression();
			if (e instanceof OperatorNode) {
				Operator op = ((OperatorNode) e).getOperator();
				if ((op == Operator.ASSIGN) || (op == Operator.POSTINCREMENT)
						|| (op == Operator.POSTDECREMENT)
						|| (op == Operator.PREINCREMENT)
						|| (op == Operator.PREDECREMENT)
						|| (op == Operator.BITANDEQ) || (op == Operator.BITOREQ)
						|| (op == Operator.BITXOREQ) || (op == Operator.DIVEQ)
						|| (op == Operator.TIMESEQ) || (op == Operator.PLUSEQ)
						|| (op == Operator.MINUSEQ) || (op == Operator.MODEQ)
						|| (op == Operator.SHIFTLEFTEQ)
						|| (op == Operator.SHIFTRIGHTEQ)) {
					return true;
				}
			}
		}
		return false;
	}

	private IdentifierExpressionNode baseArray(OperatorNode subscript) {
		assert subscript.getOperator() == OperatorNode.Operator.SUBSCRIPT
				: "Expected subscript expression";
		if (subscript.getArgument(0) instanceof IdentifierExpressionNode) {
			return (IdentifierExpressionNode) subscript.getArgument(0);
		}
		return baseArray((OperatorNode) subscript.getArgument(0));
	}

	private Entity getLHSVar(final ASTNode s) {
		if (isAssignment(s)) {
			ExpressionNode lhs = ((OperatorNode) ((ExpressionStatementNode) s)
					.getExpression()).getArgument(0);
			if (lhs instanceof IdentifierExpressionNode) {
				IdentifierNode id = ((IdentifierExpressionNode) lhs)
						.getIdentifier();
				return id.getEntity();
			} else if (lhs instanceof OperatorNode) {
				OperatorNode opn = (OperatorNode) lhs;
				if (opn.getOperator() == Operator.SUBSCRIPT) {
					IdentifierExpressionNode idn = baseArray(opn);
					return idn.getIdentifier().getEntity();
				} else {
					assert false : "Unexpected operator node on LHS";
				}
			} else {
				assert false : "Unexpected LHS expression";
			}
		}
		return null;
	}

	private ExpressionNode getRHS(final ASTNode s) {
		if (isAssignment(s)) {
			ExpressionNode rhs = ((OperatorNode) ((ExpressionStatementNode) s)
					.getExpression()).getArgument(1);
			return rhs;
		}
		return null;
	}

	@Override
	protected Set<ASTNode> succs(ASTNode n) {
		return cfa.successors(n);
	}

	@Override
	protected Set<ASTNode> preds(ASTNode n) {
		return cfa.predecessors(n);
	}

	@Override
	protected ASTNode start() {
		ASTNode n = cfa.entry(currentFunction);
		assert n != null;
		return n;
	}

	@Override
	protected
	/*
	 * Generate constants that are assigned from for statements.
	 */
	Set<Pair<Entity, Pair<Boolean, ConstantNode>>> gen(
			Set<Pair<Entity, Pair<Boolean, ConstantNode>>> set, ASTNode n) {
		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> result = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		// Extremely simple interpretation of assignment. No constant folding,
		// no copy propagation, etc.
		if (isAssignment(n)) {
			Entity lhsVar = getLHSVar(n);
			ExpressionNode rhs = getRHS(n);

			// The constant pair is "top" if the rhs is not a ConstantNode,
			// otherwise we use the rhs
			Pair<Boolean, ConstantNode> constPair = new Pair<Boolean, ConstantNode>(
					!(rhs instanceof ConstantNode), (ConstantNode) rhs);
			Pair<Entity, Pair<Boolean, ConstantNode>> cpEntry = new Pair<Entity, Pair<Boolean, ConstantNode>>(
					lhsVar, constPair);
			result.add(cpEntry);
		}
		return result;
	}

	@Override
	protected
	/*
	 * MODIFIED Kill constants that are assigned into for statements.
	 */
	Set<Pair<Entity, Pair<Boolean, ConstantNode>>> kill(
			Set<Pair<Entity, Pair<Boolean, ConstantNode>>> set,
			final ASTNode n) {
		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> result = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		// Extremely simple interpretation of assignment. No constant folding,
		// no copy propagation, etc.
		if (isAssignment(n)) {
			Entity lhsVar = getLHSVar(n);
			for (Pair<Entity, Pair<Boolean, ConstantNode>> cpEntry : set) {
				if (cpEntry.left.equals(lhsVar)) {
					result.add(cpEntry);
				}
			}
		}
		return result;
	}

	@Override
	protected Set<Pair<Entity, Pair<Boolean, ConstantNode>>> merge(
			Set<Pair<Entity, Pair<Boolean, ConstantNode>>> s1,
			Set<Pair<Entity, Pair<Boolean, ConstantNode>>> s2) {
		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> result = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		Set<Entity> idOverlap = new HashSet<Entity>();

		// Compute the set of overlapping identifiers in the incoming sets of CP
		// entries
		for (Pair<Entity, Pair<Boolean, ConstantNode>> p1 : s1) {
			for (Pair<Entity, Pair<Boolean, ConstantNode>> p2 : s2) {
				if (p1.left.equals(p2.left)) {
					idOverlap.add(p1.left);
				}
			}
		}

		// For entries with common identifiers, merge their CP data
		for (Pair<Entity, Pair<Boolean, ConstantNode>> p1 : s1) {
			if (!idOverlap.contains(p1.left))
				continue;

			for (Pair<Entity, Pair<Boolean, ConstantNode>> p2 : s2) {
				if (!idOverlap.contains(p2.left))
					continue;

				if (p1.left.equals(p2.left)) {

					// always generate the same top CP value
					Pair<Boolean, ConstantNode> topCP = new Pair<Boolean, ConstantNode>(
							Boolean.valueOf(true), null);
					Pair<Entity, Pair<Boolean, ConstantNode>> top = new Pair<Entity, Pair<Boolean, ConstantNode>>(
							p1.left, topCP);
					if (!p1.right.left.booleanValue()
							&& !p2.right.left.booleanValue()
							&& p1.right.right.getConstantValue().equals(
									p2.right.right.getConstantValue())) {
						result.add(p1);
					} else {
						result.add(top);
					}
				}
			}
		}

		// Add the disjoint CP entries to the merge
		// TBD: this seems wrong. We want these entries to go to "top". What's
		// the cleanest way to do that with lambdas?
		result.addAll(s1.stream().filter(p -> !idOverlap.contains(p.left))
				.collect(Collectors.toSet()));
		result.addAll(s2.stream().filter(p -> !idOverlap.contains(p.left))
				.collect(Collectors.toSet()));

		return result;
	}

	@Override
	public String getAnalysisName() {
		return "Constant Propagation";
	}

	@Override
	public String toString(Pair<Entity, Pair<Boolean, ConstantNode>> e) {
		Pair<Boolean, ConstantNode> p = e.right;
		String entry = e.left + "->"
				+ ((p.left.booleanValue()) ? "top" : (p.right.toString()));
		return "<" + entry + ">";
	}
}
