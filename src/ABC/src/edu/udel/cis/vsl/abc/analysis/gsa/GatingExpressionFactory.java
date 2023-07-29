package edu.udel.cis.vsl.abc.analysis.gsa;

import java.util.HashMap;
import java.util.Map;

import edu.udel.cis.vsl.abc.analysis.dataflow.AnalysisUtilities;
import edu.udel.cis.vsl.abc.analysis.dataflow.ControlFlowAnalysis;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;

/**
 * Gating expressions are constructed exclusively through a factory.
 * The factory manages control flow, conditional, and other information
 * that is required to compute gating expressions for an edge.  The
 * factory also interns expressions for reuse.
 * 
 * @author dwyer
 */
public class GatingExpressionFactory  {
	AnalysisUtilities cfu;
	Map<ASTNode, Map<ASTNode, GatingExpression>> edgeGExprMap;	
	
	public GatingExpressionFactory(ControlFlowAnalysis cfa) {
		this.cfu = new AnalysisUtilities(cfa);
		this.edgeGExprMap = new HashMap<ASTNode, Map<ASTNode, GatingExpression>>();
	}
	
	public GatingExpression makeGatingExpression(ASTNode src, ASTNode dest) {
		Map<ASTNode, GatingExpression> destMap = edgeGExprMap.get(src);
		if (destMap == null) {
			destMap = new HashMap<ASTNode, GatingExpression>();
			edgeGExprMap.put(src, destMap);
		}
		
		GatingExpression result = destMap.get(dest);
		if (result == null) {	
			if (cfu.isBranch(src)) {
				result = new GatingExpression(src, dest, cfu.branchCondition(src, dest));
			} else {
				result = new GatingExpression(true);
			}
		}
		
		return result;
	}
		
}
