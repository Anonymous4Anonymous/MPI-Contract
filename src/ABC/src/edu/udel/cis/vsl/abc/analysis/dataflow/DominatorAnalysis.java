package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;

/**
 * Intra-procedural dominator analysis.
 * 
 * The dominates relation is defined over {@link ASTNode}s and as such the analysis
 * simply records a set of dominated {@link ASTNode}s for each node in the CFG.
 * 
 * This is a forward flow problem data flow facts propagate in execution order beginning with the
 * entry node of the function see {@link #preds(ASTNode)}, {@link #succs(ASTNode))}, and {@link #start()}.
 * 
 * At control flow merge points sets of dominators are combined via intersection; see {@link #merge(Set, Set)}.
 * Initially, {@link #init(ASTNode)}, the set of all CFG nodes are used, so this is computes a greatest lower bound.
 * 
 * A node dominates itself, see {@link #gen(Set, ASTNode)}, and does not supersede any of its 
 * dominators, see {@link #kill(Set, ASTNode).  These define a gen-kill function space for
 * the analysis.
 * 
 * The interface is extended to provide accessors for the dominators of a node {@link #dom(ASTNode)} and the
 * immediate dominator {@link @idom(ASTNode)}.
 * 
 * @author dwyer
 */
public class DominatorAnalysis extends DataFlowFramework<ASTNode> {
	private static DominatorAnalysis instance = null;
	
	Map<ASTNode, ASTNode> idom = new HashMap<ASTNode, ASTNode>();
	Map<ASTNode, Set<ASTNode>> idomR = new HashMap<ASTNode, Set<ASTNode>>();
	
	Function currentFunction;
	
	ControlFlowAnalysis cfa;
	
	protected DominatorAnalysis() {}
	
	public static DominatorAnalysis getInstance() {
		if (instance == null) {
			instance = new DominatorAnalysis();
		}
		return instance;
	}
	
	public  void clear() {
		super.clear();
		instance = null;
		idom.clear();
		idomR.clear();
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
			
		HashSet<ASTNode> init = new HashSet<ASTNode>();
		init.add(cfa.entry(currentFunction));
		computeFixPoint(init, cfa.allNodes(currentFunction));
		
		computeImmediateDominators();
	}
	
	/**
	 * The dominators of a node are simply the outgoing set of dominators computed for the node.
	 */
	public Set<ASTNode> dom(ASTNode n) {
		return getOutSet(n);
	}
	
	public ASTNode idom(ASTNode n) {
		return idom.get(n);
	}
	
	public Set<ASTNode> idomR(ASTNode n) {
		return idomR.get(n);
	}
	
	protected void computeImmediateDominators() {
		for (ASTNode n : cfa.allNodes(currentFunction)) {
			Set<ASTNode> candidates = new HashSet<ASTNode>();
			candidates.addAll(dom(n));
			candidates.remove(n);
			
			for (ASTNode potentialIdom : candidates) {
				if (dom(potentialIdom).containsAll(candidates)) {
					idom.put(n, potentialIdom);
					if (idomR.get(potentialIdom) == null) {
						idomR.put(potentialIdom, new HashSet<ASTNode>());
					}
					idomR.get(potentialIdom).add(n);
				}
			}
			//TBD: think about the correctness condition here: assert idom.get(n) != null;
		}		
	}

	@Override
	/*
	 * Dominance is reflexive, so all statements dominate themselves.
	 * 
	 * @see edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#gen(java.util.Set, edu.udel.cis.vsl.abc.ast.node.IF.ASTNode)
	 */
	protected Set<ASTNode> gen(final Set<ASTNode> set, final ASTNode s) {
		final Set<ASTNode> result = new HashSet<ASTNode>();
		result.add(s);
		return result;
	}
	
	// kill function uses the default implementation

	@Override
	public String getAnalysisName() {
		return "Dominators";
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
	 * Compute the intersection on the arguments
	 * 
	 * @see edu.udel.cis.vsl.abc.analysis.dataflow.DataFlowFramework#merge(java.util.Set, java.util.Set)
	 */
	protected Set<ASTNode> merge(final Set<ASTNode> s1, final Set<ASTNode> s2) {
		Set<ASTNode> result = new HashSet<ASTNode>();
		assert s1 != null;
		assert s2 != null;
		result.addAll(s1);
		result.retainAll(s2);
		return result;
	}

	@Override
	public String toString(ASTNode e) {
		return e.toString();
	}
	
	public void printDominatorTree(Function f) {
		ASTNode entry = cfa.entry(f);
		printDomTree(entry, "");
	}
	
	private void printDomTree(ASTNode n, String indent) {
		System.out.println(indent+n);
		if (idomR.get(n) != null) {
			for (ASTNode d : idomR.get(n)) {
				printDomTree(d, indent+"  ");
			}
		}
	}
	
}
