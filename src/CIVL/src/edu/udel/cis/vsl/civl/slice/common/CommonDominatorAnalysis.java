package edu.udel.cis.vsl.civl.slice.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.civl.slice.IF.DominatorAnalysis;

/**
 * This analysis uses an iterative fixed-point
 * data flow framework to find the dominators of
 * any node in a graph.
 * 
 * A node d dominates a node n if every path from
 * the entry node to n must go through d.
 * 
 * The algorithm comes from page 479 of 
 * "Engineering A Compiler".
 * 
 * @author mgerrard
 *
 * @param <E>
 */

public class CommonDominatorAnalysis<E> implements DominatorAnalysis<E> {
	
	private Set<E> stmts;
	private Set<E> nodesMinusStart;
	private Map<E,Set<E>> preds;
	private E start;
	private Map<E, Set<E>> doms;

	public CommonDominatorAnalysis (Set<E> nodes,
		      Map<E,Set<E>> preds, E start) {
		assert nodes != null;
		this.stmts = nodes;
		assert preds != null;
	    this.preds = preds;
	    assert start != null;
	    this.start = start;
	    Set<E> s = new HashSet<E>();
	    s.addAll(nodes);
	    s.remove(start);
	    this.nodesMinusStart = s;
	}
	
	/* Iterative algorithm from p.479 of Engineering A Compiler */
	public Map<E,Set<E>> computeDominators() {
		Map<E,Set<E>> dominators = new HashMap<E,Set<E>>();
		
		Set<E> init = new HashSet<E>();
		
		init.add(start);
		dominators.put((start), init);
		for(E s : nodesMinusStart){
			Set<E> allStmts = new HashSet<E>();
			allStmts.addAll(stmts);
			dominators.put(s, allStmts);
		}
		
		boolean changed = true;
		Set<E> temp = new HashSet<E>();
		while (changed) {
			changed = false;
			for (E s : nodesMinusStart) {
				
				Set<E> currentDominators = intersectionOfPredDoms(s,dominators);
				currentDominators.add(s);
				temp = currentDominators;
				
				if (!temp.equals(dominators.get(s))) {
					dominators.put(s, temp);
					changed = true;
				}
			}
		}
		
		/* Remove reflexive elements */
		for (E s : dominators.keySet()) {
			dominators.get(s).remove(s);
		}
		
		this.doms = dominators;
		return dominators;
	}
	
	private Set<E> intersectionOfPredDoms (E s, Map<E,Set<E>> dominators) {
		Set<E> result = new HashSet<E>(stmts);
		Set<E> predsOfStmt = this.preds.get(s);
		for (E p : predsOfStmt) {
			assert dominators.get(p) != null : s + " has no predecessors";
			result.retainAll(dominators.get(p));
		}
		return result;
	}

	public void print() {
		for (E k : doms.keySet()){
			Set<E> values = doms.get(k);
			if (values.isEmpty()){
				System.out.println("  ***NOTHING***");
			} else {
				for (E v : values){
					System.out.println("  "+v);
				}
			}
		}
	}

}
