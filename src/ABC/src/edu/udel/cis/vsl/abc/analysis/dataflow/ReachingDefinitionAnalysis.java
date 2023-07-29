package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.util.IF.Pair;

/**
 * Intra-procedural reaching definitions analysis.
 * 
 * Reaching definitions (RD) are represented as a {@link Pair} of the {@link Entity} for the
 * defined variable and a {@link ASTNode} for a definition of that variable.
 * 
 * This is a forward flow problem data flow facts propagate in execution order beginning with the
 * entry node of the function see {@link #preds(ASTNode)}, {@link #succs(ASTNode))}, and {@link #start()}.
 * 
 * At control flow merge points sets of RD are combined via union; see {@link #merge(Set, Set)}
 * 
 * Statements are interpreted in terms of the RDs that they generate, see {@link #gen(Set, ASTNode)}, and
 * those that they supersede, see {@link #kill(Set, ASTNode).  These define a gen-kill function space for
 * the analysis.
 * 
 * Arrays are treated as a single aggregated unit from the perspective of this analysis, i.e., an 
 * assignment to any element of an array constitutes a reaching definition for any other element of the
 * array.
 * 
 * @author dwyer
 */
public class ReachingDefinitionAnalysis extends DataFlowFramework<Pair<Entity, ASTNode>> {
	private static ReachingDefinitionAnalysis instance = null;

	Function currentFunction;
	
	ControlFlowAnalysis cfa;
	
	/**
	 * DFAs are singletons.  This allows them to be applied incrementally across a code base.
	 */
	protected ReachingDefinitionAnalysis() {}
	
	public static ReachingDefinitionAnalysis getInstance() {
		if (instance == null) {
			instance = new ReachingDefinitionAnalysis();
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
		if (analyzedFunctions.contains(f)) return;
		analyzedFunctions.add(f);
		currentFunction = f;
		
		// Perform control flow analysis (if needed)
		cfa = ControlFlowAnalysis.getInstance();
		cfa.analyze(f);
		
		HashSet<Pair<Entity, ASTNode>> init = new HashSet<Pair<Entity, ASTNode>>();
		// should initialize this appropriately to reflect, e.g., parameters, global definitions, etc.
		computeFixPoint(init, new HashSet<Pair<Entity, ASTNode>>());
	}

	@Override
	/*
	 * If this is an assignment statement create a new RD pair for the definition.
	 * 
	 * @see edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#gen(java.util.Set, edu.udel.cis.vsl.abc.ast.node.IF.ASTNode)
	 */
	protected Set<Pair<Entity, ASTNode>> gen(final Set<Pair<Entity, ASTNode>> set, final ASTNode s) {
		final Set<Pair<Entity, ASTNode>> result = new HashSet<Pair<Entity, ASTNode>>();
		final Entity lhsLocal = getLHSVar(s);
		if (lhsLocal != null) {
			result.add(new Pair<Entity, ASTNode>(lhsLocal, s));
		}
		return result;
	}
	
	@Override
	/*
	 * Filter the incoming RD pairs retaining only those that involve the LHS of the
	 * given statement.
	 * 
	 * @see edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#kill(java.util.Set, edu.udel.cis.vsl.abc.ast.node.IF.ASTNode)
	 */
	protected Set<Pair<Entity, ASTNode>> kill(final Set<Pair<Entity, ASTNode>> set, final ASTNode s) {
		Set<Pair<Entity, ASTNode>> result = new HashSet<Pair<Entity, ASTNode>>();
		final Entity lhsLocal = getLHSVar(s);
		if (lhsLocal != null) {
			result = set.stream().filter(p -> p.left.equals(lhsLocal)).collect(Collectors.toSet());
		}
		return result;
	}

	@Override
	public String getAnalysisName() {
		return "Reaching Definitions";
	}

	private boolean isAssignment(final ASTNode s) {
		if (s instanceof ExpressionStatementNode) {
			ExpressionNode e = ((ExpressionStatementNode)s).getExpression();
			if (e instanceof OperatorNode) {
				Operator op = ((OperatorNode)e).getOperator();
				if ( (op == Operator.ASSIGN) || 
						(op == Operator.POSTINCREMENT) || (op == Operator.POSTDECREMENT) || 
						(op == Operator.PREINCREMENT) || (op == Operator.PREDECREMENT) || 
						(op == Operator.BITANDEQ) || (op == Operator.BITOREQ) || (op == Operator.BITXOREQ) ||
						(op == Operator.DIVEQ) || (op == Operator.TIMESEQ) || (op == Operator.PLUSEQ) || 
						(op == Operator.MINUSEQ) || (op == Operator.MODEQ) ||
						(op == Operator.SHIFTLEFTEQ) || (op == Operator.SHIFTRIGHTEQ) ) {
					return true;
				}
			} 
		}
		return false;
	}
	
	private IdentifierExpressionNode baseArray(OperatorNode subscript) {
		assert subscript.getOperator() == OperatorNode.Operator.SUBSCRIPT : "Expected subscript expression";
		if (subscript.getArgument(0) instanceof IdentifierExpressionNode) {
			return (IdentifierExpressionNode) subscript.getArgument(0);
		}
		return baseArray((OperatorNode) subscript.getArgument(0));
	}

	private Entity getLHSVar(final ASTNode s) {
		if (isAssignment(s)) {
			ExpressionNode lhs = ((OperatorNode)((ExpressionStatementNode)s).getExpression()).getArgument(0);
			if (lhs instanceof IdentifierExpressionNode) {
				IdentifierNode id = ((IdentifierExpressionNode)lhs).getIdentifier();
				return id.getEntity();
			} else if (lhs instanceof OperatorNode) {
				OperatorNode opn = (OperatorNode)lhs;
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

	@Override
	/*
	 * This is a forward flow problem, so the successor direction for the analysis aligns with control flow.
	 * 
	 * @see edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#succs(edu.udel.cis.vsl.abc.ast.node.IF.ASTNode)
	 */
	protected Set<ASTNode> succs(ASTNode s) {
		return cfa.successors(s);
	}

	@Override
	/*
	 * This is a forward flow problem, so the predecessor direction for the analysis opposes control flow.
	 * 
	 * @see edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#preds(edu.udel.cis.vsl.abc.ast.node.IF.ASTNode)
	 */
	protected Set<ASTNode> preds(ASTNode s) {
		return cfa.predecessors(s);
	}

	@Override
	protected ASTNode start() {
		ASTNode n = cfa.entry(currentFunction);
		assert n != null;
		return n;
	}

	@Override
	/*
	 * Compute union of arguments
	 * 
	 * @see edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#merge(java.util.Set, java.util.Set)
	 */
	protected Set<Pair<Entity, ASTNode>> merge(Set<Pair<Entity, ASTNode>> s1, Set<Pair<Entity, ASTNode>> s2) {
		Set<Pair<Entity, ASTNode>> result = new HashSet<Pair<Entity, ASTNode>>();
		assert s1 != null;
		assert s2 != null;
		result.addAll(s1);
		result.addAll(s2);
		return result;
	}
	

	@Override
	public String toString(Pair<Entity, ASTNode> e) {
		return "<"+e.left+"@"+e.right+">";
	}
}
