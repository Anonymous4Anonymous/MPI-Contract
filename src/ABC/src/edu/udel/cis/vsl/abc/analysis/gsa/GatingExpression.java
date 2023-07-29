package edu.udel.cis.vsl.abc.analysis.gsa;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;

/**
 * Gating expressions encode the conditions that govern a path from
 * a source node to a destination.   
 * 
 * Gating expressions are partially defined.  If an edge is not explicitly
 * represented for a branch, then it is considered to be "not taken".
 * 
 * @author dwyer
 */
public class GatingExpression  {
	boolean unconditional = false;
	boolean notTaken = false;
	
	// source node for edge: null for unconditional or not taken expressions
	ASTNode src = null; 
	
	/**
	 * There is a map from destinations of branches, from a common source,
	 * to boolean AST expressions (conditions) and gating expressions.
	 */
	Map<ASTNode, ASTNode> edgeConditionMap;
	Map<ASTNode, GatingExpression> edgeGExprMap;

	/**
	 * Create an unconditional or not taken GatingExpression.
	 * 
	 * @param unconditionalOrNotTaken if true then an unconditional GExpr is created, else not taken
	 */
	public GatingExpression(boolean unconditionalOrNotTaken) {
		if (unconditionalOrNotTaken) {
			this.unconditional = true;
		} else {
			this.notTaken = true;
		}
	}
	
	
	/**
	 * Create a conditional GatingExpression for a CFG branch edge.  The designated edge has an unconditional
	 * GExpr associated with it and all other edges are marked as not taken.
	 * 
	 * @param src the source node of the edge
	 * @param dest  the destination node of the branch
	 * @param cond the conditional expression govrning the branch
	 */
	public GatingExpression(ASTNode src, ASTNode dest, ASTNode cond) {
		this.src = src;
		this.edgeConditionMap = new HashMap<ASTNode, ASTNode>();
		this.edgeGExprMap = new HashMap<ASTNode, GatingExpression>();
		this.edgeConditionMap.put(dest, cond);
		this.edgeGExprMap.put(dest, new GatingExpression(true));
	}
	
	/**
	 * Internal constructor used to implement GExpr operators.
	 * 
	 * @param src the source of a set of branch edges
	 */
	private GatingExpression(ASTNode src) {
		this.src = src;
		this.edgeConditionMap = new HashMap<ASTNode, ASTNode>();
		this.edgeGExprMap = new HashMap<ASTNode, GatingExpression>();
	}
	
	public boolean isUnconditional() {
		return unconditional;
	}
	
	public boolean isNotTaken() {
		return notTaken;
	}
	
	/**
	 * The disjunction of two gating expression builds a new gating expression
	 * that captures the alternatives and the conditions governing those.  For this
	 * operation a missing edge is interpreted as "not taken".
	 * 
	 * @param ge1 a conditional or not taken gating expression
	 * @param ge2 a conditional or not taken gating expression
	 * @return
	 */
	public GatingExpression or(GatingExpression ge1, GatingExpression ge2) {
		assert !ge1.isUnconditional() && !ge2.isUnconditional() : 
			"Unconditional GatingExpressions cannot be disjoined";
		
		if (ge1.isNotTaken()) {
			return ge2;
		} else if (ge2.isNotTaken()) {
			return ge1;
		} else {
			// Compound gating exprs must have a common source
			assert ge1.src == ge2.src : "Type mismatched GatingExpressions";

			// Maps for the disjunction should cover the operands
			Set<ASTNode> dests = new HashSet<ASTNode>();
			dests.addAll(ge1.edgeConditionMap.keySet());
			dests.addAll(ge2.edgeConditionMap.keySet());
			
			GatingExpression or = new GatingExpression(ge1.src);
			for (ASTNode dest : dests) {
				ASTNode c1 = ge1.edgeConditionMap.get(dest);
				ASTNode c2 = ge2.edgeConditionMap.get(dest);
				
				/*
				 * If both arguments define this edge explicitly, recursively disjoin their
				 * Gating Exprs.  Otherwise use the one that is explicitly defined.
				 */
				if (c1 != null) {
					or.edgeConditionMap.put(dest, c1);
					if (c2 != null) {
						// both arguments defined this edge
						GatingExpression geor = or(ge1.edgeGExprMap.get(dest), ge2.edgeGExprMap.get(dest));
						or.edgeGExprMap.put(dest, geor);
					} else {
						// only ge1 defined this edge
						or.edgeGExprMap.put(dest, ge1.edgeGExprMap.get(dest));
					}
				} else {
					// only ge2 defined this edge
					or.edgeConditionMap.put(dest, c2);
					or.edgeGExprMap.put(dest, ge2.edgeGExprMap.get(dest));	
				}				
			}
			return or;
		}
	}
	
	/**
	 * The concatenation of two gating expressions builds a new gating expression
	 * that captures the sequence and the conditions governing them.   For this 
	 * operation a missing edge is interpreted as "unconditional".
	 * 
	 * @param ge1 the first gating expression
	 * @param ge2 the second gating expression
	 * @return
	 */
	public GatingExpression concat(GatingExpression ge1, GatingExpression ge2) {
		if (ge1.isNotTaken() || ge2.isNotTaken()) {
			return new GatingExpression(false);
		} else if (ge1.isUnconditional()) {
			return ge2;
		} else if (ge2.isUnconditional()) {
			return ge1;
		} else {

			// Maps for the disjunction should cover the operands
			Set<ASTNode> dests = new HashSet<ASTNode>();
			dests.addAll(ge1.edgeConditionMap.keySet());
			dests.addAll(ge2.edgeConditionMap.keySet());
			
			GatingExpression or = new GatingExpression(ge1.src);
			for (ASTNode dest : dests) {
				ASTNode c1 = ge1.edgeConditionMap.get(dest);
				ASTNode c2 = ge2.edgeConditionMap.get(dest);
				
				/*
				 * If both arguments define this edge explicitly, recursively concatenate their
				 * Gating Exprs.  Otherwise use the one that is explicitly defined.
				 */
				if (c1 != null) {
					or.edgeConditionMap.put(dest, c1);
					if (c2 != null) {
						// both arguments defined this edge
						GatingExpression geconcat = concat(ge1.edgeGExprMap.get(dest), ge2.edgeGExprMap.get(dest));
						or.edgeGExprMap.put(dest, geconcat);
					} else {
						// only ge1 defined this edge
						or.edgeGExprMap.put(dest, ge1.edgeGExprMap.get(dest));
					}
				} else {
					// only ge2 defined this edge
					or.edgeConditionMap.put(dest, c2);
					or.edgeGExprMap.put(dest, ge2.edgeGExprMap.get(dest));	
				}				
			}
			return or;
		}
	}
	
	public String toString() {
		if (isUnconditional()) {
			return "uncond";
		} else if (isNotTaken()) {
			return "not taken";
		} else {
			String result = "{";
			for (ASTNode n : edgeConditionMap.keySet()) {
				result += "("+src+"->"+n+" on "+condString(edgeConditionMap.get(n))+" with "+edgeGExprMap.get(n)+") ";
			}
			return result+"}";
		}
	}
	
	private String condString(ASTNode expr) {
		assert expr instanceof ExpressionNode : "Condition must be an expression";
		String result = "";
		
		if (expr instanceof IdentifierExpressionNode) {
			result = ((IdentifierExpressionNode)expr).getIdentifier().name();
		} else if (expr instanceof ConstantNode) {
			result = ((ConstantNode)expr).getStringRepresentation();
		} else if (expr instanceof OperatorNode) {
			OperatorNode on = (OperatorNode)expr;
			result = "("+on.getOperator();
			for (int i=0; i<on.getNumberOfArguments(); i++) {
				result += " "+condString(on.getArgument(i));
			}
			result += ")";
		} else {
			assert false : "Unexpected subexpression in condition:"+expr;
		}
		return result;
	}
	
}
