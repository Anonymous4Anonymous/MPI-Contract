package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;

/**
 * This class realizes an intra-procedural set-based Data Flow Framework. 
 * The lattice is implicitly a set of the given element type.
 * 
 * NB: More subtle lattices, e.g., a lattice of functions/maps, would require
 * a generalization of this class.
 *
 * It is instantiated
 * by a sub-typing Data Flow Analysis instance that operates over
 * ABC {@link ASTNode}s which may represent statements or expressions.
 *
 * The direction of the analysis is determined by the definition of 
 * the {@link #succs()}, {@link #preds()} and {@link #start()} methods.
 *
 * The abstract semantics of program statements are given by the
 * {@link #update()} method.  For convenience, a default implementation
 * for gen-kill function spaces is provided using methods {@link #gen()} 
 * and {@link #kill()}.
 * 
 * Values are combined at control flow join points by the 
 * {@link #merge()} operation.
 * 
 * @param <E>
 *          The element type of the data flow lattice.
 *          
 * @author dwyer
 */
public abstract class DataFlowFramework<E> {
	// Can easily generalize this to work on other graphs, i.e., not ASTNode based
	protected Map<ASTNode, Set<E>> outMap = new HashMap<ASTNode, Set<E>>();
	
	// Record functions for which analysis results have already been computed
	protected Set<Function> analyzedFunctions = new HashSet<Function>();
	
	// Value for entry to CFG, i.e., it has no predecessors so use this value
	protected Set<E> init;
	
    // Value for CFG nodes that are never visited
	protected Set<E> bottom;
	
	/**
	 * Clear analysis results
	 */
	protected void clear() {
		outMap = new HashMap<ASTNode, Set<E>>();
		analyzedFunctions = new HashSet<Function>();
	}

	/*
	 * Computes the fix point solution for a given function
	 */
	public abstract void analyze(Function f);

	/**
	 * This is a recursive variant of Kildall's classic worklist algorithm   
	 * that performs a series of DFS passes over the CFG.   A pass is launched
	 * from the {@link #start()} of the CFG using {@link #iterate(Set, ASTNode)},
	 * with the set recording the DFS state cleared before each pass.  A pass
	 * indicates whether new information was computed within and, if so, another
	 * pass is launched.
	 * 
	 * @param bottom value to be used for uninitialized nodes
	 */
	protected void computeFixPoint(final Set<E> init, final Set<E> bottom) {
		this.init = init;
		this.bottom = bottom;
		final Set<ASTNode> seen = new HashSet<ASTNode>();
		while (iterate(seen, start())) {
			seen.clear();
		}
	}

	/**
	 * This is a DFS pass in the variant of Kildall's classic worklist algorithm.   The transfer
	 * function computation is performed in {@link DataFlowFramework#compute(ASTNode)}.
	 * 
	 * @param seen records the state of the DFS visit of the CFG on this pass
	 * @param s is the current CFG node
	 * @return indicates whether the data flow information has changed within this pass
	 */
	protected boolean iterate(final Set<ASTNode> seen, final ASTNode s) {
		if (seen.contains(s)) {
			return false;
		}
		boolean hasChanged = compute(s);
		seen.add(s);
		final Set<ASTNode> successors = succs(s);
		if (successors != null) {
			for (final ASTNode succ : successors) {
				hasChanged = iterate(seen, succ) || hasChanged;
			}
		}
		return hasChanged;
	}

	/**
	 * Computer the transfer function for the current CFG node and if that results
	 * in new values then update the out set for the node. 
	 * 
	 * @param n
	 * @return
	 */
	protected boolean compute(final ASTNode n) {
		Set<E> inSet = getInSet(n);
		inSet = update(inSet, n);
		final Set<E> outSet = getOutSet(n);
		if (!inSet.containsAll(outSet) || !outSet.containsAll(inSet)) {
			outSet.clear();
			outSet.addAll(inSet);
			inSet.clear();
			return true;
		}
		inSet.clear();
		return false;
	}

	/*
	 * Control flow definitions
	 */
	protected abstract Set<ASTNode> succs(ASTNode n);
	protected abstract Set<ASTNode> preds(ASTNode n);
	protected abstract ASTNode start();
	
	/*
	 * Lattice merge does not modify the two argument sets.
	 */
	protected abstract Set<E> merge(final Set<E> s1, final Set<E> s2);

	/*
	 * Function space definition
	 */
	
	/*
	 * Node gen-kill functions.  These return an empty set by default.
	 */
	protected Set<E> gen(Set<E> set, ASTNode n) {
		return new HashSet<E>();
	}
	
	protected Set<E> kill(Set<E> set, ASTNode n) {
		return new HashSet<E>();
	}

	/*
	 * Override this to define a non-gen-kill function space
	 * (still need to define {@link #gen()} and {@link #kill()} but they can be trivial)
	 */
	protected Set<E> update(Set<E> inSet, ASTNode n) {
		System.out.println(n);
//		System.out.println("Before\t" + inSet);

		Set<E> killSet = kill(inSet, n);
		Set<E> genSet = gen(inSet, n);		
		inSet.removeAll(killSet);
		inSet.addAll(genSet);

//		System.out.println("kill\t" + killSet);
//		System.out.println("Gen\t" + genSet);
		System.out.println("After\t" + inSet);
		System.out.println();
		
		return inSet;  
	}

	/*
	 * Returns the name of this analysis.
	 * 
	 * @return The name of this analysis.
	 */
	public abstract String getAnalysisName();

	/*
	 * Returns the in-set of a {@link ASTNode}.  
	 * 
	 * @param s
	 *          The {@link ASTNode}.
	 * @return The in-set of the given {@link ASTNode}.
	 */
	 public Set<E> getInSet(final ASTNode s) {
		assert s != null;
		Set<E> inSet = (s == start()) ? init : bottom;

		if (preds(s) != null) {
			for (final ASTNode pred : preds(s)) {
				inSet = merge(inSet, getOutSet(pred));
			}
		}

		return inSet;
	}

	/*
	 * Returns the out-set of a {@link ASTNode}.
	 * 
	 * @param s
	 *          The {@link ASTNode}.
	 * @return The out-set of the given {@link ASTNode}.
	 */
	 public Set<E> getOutSet(final ASTNode n) {
		assert n != null;

		Set<E> result = outMap.get(n);
		if (result == null) {
			result = new HashSet<E>(bottom);
			outMap.put(n, result);
		}
		return result;
	}

	/*
	 * Returns the {@link String} representation of all computed RD analysis results.
	 */
	public String getResultString() {
		final StringBuilder sb = new StringBuilder("*** " + getAnalysisName()
		+ " ***\n");
		sb.append("*** InSet Map ***\n");
		sb.append(getResultString(true));
		sb.append("*** OutSet Map ***\n");
		sb.append(getResultString(false));
		return sb.toString();
	}

	private String getResultString(final boolean useInSet) {
		final StringBuilder sb = new StringBuilder();
		final ArrayList<String> list = new ArrayList<String>();
		for (final ASTNode s : this.outMap.keySet()) {
			sb.append(s.getSource()+" ==> ");
			final TreeSet<String> ts = new TreeSet<String>();
			for (final E e : (useInSet ? getInSet(s) : getOutSet(s))) {
				ts.add(toString(e));
			}
			for (final String str : ts) {
				sb.append(str);
				sb.append("  #  ");
			}
			final String str = sb.toString();
			sb.setLength(0);
			list.add(str.substring(0, str.length() - 5) + "\n");
		}
		Collections.sort(list);
		for (final String s : list) {
			sb.append(s);
		}
		return sb.toString();
	}

	/*
	 * Returns the {@link String} representation of the MDF lattice element.
	 * 
	 * @param e
	 *          The MDF lattice element.
	 * @return The {@link String} representation of the MDF lattice element.
	 */
	public abstract String toString(E e);
}
