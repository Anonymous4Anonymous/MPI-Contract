package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.value.IF.ValueFactory;
import edu.udel.cis.vsl.abc.ast.value.IF.ValueFactory.Answer;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.util.IF.Pair;

/**
 * The lattice is a map from variables, identified by entity, to singleton
 * constant values or "top" (indicated by the first element of the pair).
 * 
 * Really want to use a data flow framework that allows for a Map to be used,
 * not just a set.
 * 
 * @author dwyer
 */
public class ConditionalConstantPropagation
		extends
			EdgeDataFlowFramework<Pair<Entity, Pair<Boolean, ConstantNode>>> {
	private static ConditionalConstantPropagation instance = null;

	Function currentFunction;

	ControlFlowAnalysis cfa;
	AnalysisUtilities au;
	ValueFactory vf;

	/**
	 * DFAs are singletons. This allows them to be applied incrementally across
	 * a code base.
	 */
	protected ConditionalConstantPropagation() {
	}

	public static ConditionalConstantPropagation getInstance() {
		if (instance == null) {
			instance = new ConditionalConstantPropagation();
		}
		return instance;
	}

	@Override
	public void clear() {
		super.clear();
		instance = null;
		cfa.clear();
		au = null;
		vf = null;
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

		au = new AnalysisUtilities(cfa);
		vf = cfa.entry(f).getOwner().getASTFactory().getNodeFactory()
				.getValueFactory();

		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> init = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		// Unprocessed nodes are assigned an empty set
		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> bottom = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		computeFixPoint(init, bottom);
	}

	/**
	 * Determine if the branch condition is false under the given constant
	 * values for variable entities.
	 * 
	 * @param condition
	 * @param varConstMap
	 * @return Answer.YES, Answer.NO, or Answer.MAYBE
	 */
	private Answer isConditionFalse(ExpressionNode condition,
			Map<Entity, ConstantNode> varConstMap) {
		Answer result;
		ExpressionNode freshExpr = au.copyWithAttributes(condition);
		replaceIdWithConst(freshExpr, varConstMap);
		try {
			result = vf.isZero(vf.evaluate(freshExpr));
		} catch (SyntaxException se) {
			// If the expression was not made constant by the map then the
			// answer is indeterminate
			result = Answer.MAYBE;
		}
		return result;
	}

	/**
	 * Replaces one AST node with another.
	 * 
	 * @param current
	 * @param replacement
	 */
	private void replaceNode(ASTNode current, ASTNode replacement) {
		assert current != null && replacement != null;
		assert replacement.parent() == null && current.parent() != null;

		ASTNode parent = current.parent();

		int indexOf = current.childIndex();
		assert indexOf >= 0 : "current is not in an AST or is the root";

		current.remove();
		parent.setChild(indexOf, replacement);
	}

	/**
	 * Rewrite the expression replacing identifiers with constants given in the
	 * map.
	 * 
	 * @param expr
	 * @param map
	 */
	private void replaceIdWithConst(ExpressionNode expr,
			Map<Entity, ConstantNode> varConstMap) {
		// this is a recursive walk that is the usual huge switch structure with
		// recursion
		if (expr instanceof IdentifierExpressionNode) {
			Entity varEntity = ((IdentifierExpressionNode) expr).getIdentifier()
					.getEntity();
			ConstantNode newConst = varConstMap.get(varEntity);
			if (newConst != null) {
				replaceNode(expr, newConst.copy());
			}
		} else {
			Iterable<ASTNode> children = expr.children();
			for (ASTNode child : children) {
				if (child instanceof ExpressionNode) {
					ExpressionNode childExpr = (ExpressionNode) child;
					replaceIdWithConst(childExpr, varConstMap);
				}
			}
		}
	}

	@Override
	protected
	/*
	 * Kill constants that are assigned into for statements. Kill constants that
	 * are inconsistent with branch conditions for edges.
	 * 
	 * Note that for CP we are using the set of pairs to encode a map, so there
	 * is really a single abstract value being propagated with a mapping for
	 * each previously defined variable.
	 */
	Set<Pair<Entity, Pair<Boolean, ConstantNode>>> kill(
			final Set<Pair<Entity, Pair<Boolean, ConstantNode>>> set,
			final ASTNode n, final ASTNode s) {
		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> result = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		if (au.isBranch(n)) {
			ExpressionNode cond = au.branchCondition(n, s);

			/*
			 * If the in set falsifies the branch, then kill the entire set.
			 * Here we treat variables with "top" values as free in the
			 * evaluation by not adding them to the constant map.
			 */
			Map<Entity, ConstantNode> varConstMap = new HashMap<Entity, ConstantNode>();
			for (Pair<Entity, Pair<Boolean, ConstantNode>> cpEntry : set) {
				// test boolean indicator for top value
				if (!cpEntry.right.left) {
					varConstMap.put(cpEntry.left, cpEntry.right.right);
				}
			}

			if (isConditionFalse(cond, varConstMap) == Answer.YES) {
				for (Pair<Entity, Pair<Boolean, ConstantNode>> cpEntry : set) {
					result.add(cpEntry);
				}
			}

			return result;
		} else {
			// Extremely simple interpretation of assignment. No constant
			// folding, no copy propagation, etc.
			if (au.isAssignment(n) || au.isDefinition(n)) {
				Entity lhsVar = au.getLHSVar(n);
				for (Pair<Entity, Pair<Boolean, ConstantNode>> cpEntry : set) {
					if (cpEntry.left.equals(lhsVar)) {
						result.add(cpEntry);
					}
				}
			}
		}
		return result;
	}

	@Override
	protected
	/*
	 * Generate constants that are assigned from for statements.
	 */
	Set<Pair<Entity, Pair<Boolean, ConstantNode>>> gen(
			final Set<Pair<Entity, Pair<Boolean, ConstantNode>>> set,
			final ASTNode n, final ASTNode s) {
		Set<Pair<Entity, Pair<Boolean, ConstantNode>>> result = new HashSet<Pair<Entity, Pair<Boolean, ConstantNode>>>();

		// Extremely simple interpretation of assignment. No constant folding,
		// no copy propagation, etc.
		if (au.isAssignment(n) || au.isDefinition(n)) {
			Entity lhsVar = au.getLHSVar(n);
			ExpressionNode rhs = au.getRHS(n);

			// The constant pair is "top" if the rhs is not a ConstantNode,
			// otherwise we use the rhs
			Pair<Boolean, ConstantNode> constPair = null;
			if (rhs instanceof ConstantNode) {
				constPair = new Pair<Boolean, ConstantNode>(false,
						(ConstantNode) rhs);
			} else {
				constPair = new Pair<Boolean, ConstantNode>(true, null);
			}
			Pair<Entity, Pair<Boolean, ConstantNode>> cpEntry = new Pair<Entity, Pair<Boolean, ConstantNode>>(
					lhsVar, constPair);
			result.add(cpEntry);
		}
		return result;
	}

	@Override
	public String getAnalysisName() {
		return "Conditional Constant Propagation";
	}

	@Override
	/*
	 * This is a forward flow problem, so the successor direction for the
	 * analysis aligns with control flow.
	 * 
	 * @see
	 * edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#succs(edu.udel.
	 * cis.vsl.abc.ast.node.IF.ASTNode)
	 */
	protected Set<ASTNode> succs(ASTNode s) {
		return au.succs(s);
	}

	@Override
	/*
	 * This is a forward flow problem, so the predecessor direction for the
	 * analysis opposes control flow.
	 * 
	 * @see
	 * edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#preds(edu.udel.
	 * cis.vsl.abc.ast.node.IF.ASTNode)
	 */
	protected Set<ASTNode> preds(ASTNode s) {
		return au.preds(s);
	}

	@Override
	protected ASTNode start() {
		ASTNode n = cfa.entry(currentFunction);
		assert n != null;
		return n;
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
	public String toString(Pair<Entity, Pair<Boolean, ConstantNode>> e) {
		Pair<Boolean, ConstantNode> p = e.right;
		String entry = e.left.getName() + "->"
				+ ((p.left.booleanValue())
						? "top"
						: (p.right.getConstantValue()));
		return "<" + entry + ">";
	}

}
