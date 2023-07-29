package edu.udel.cis.vsl.civl.slice.IF;

import java.util.Map;
import java.util.Set;

/**
 * This analysis uses an iterative fixed-point
 * data flow framework to find the dominators of
 * any node in a graph.
 * 
 * A node d dominates a node n if every path from
 * the entry node to n must go through d.
 * 
 * @author mgerrard
 *
 * @param <E>
 */

public interface DominatorAnalysis<E> {
	
	public Map<E,Set<E>> computeDominators();
	
	public void print();

}
