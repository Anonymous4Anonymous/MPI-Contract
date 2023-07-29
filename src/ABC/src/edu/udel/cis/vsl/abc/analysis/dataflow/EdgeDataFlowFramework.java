package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.util.IF.Pair;

/**
 * This class realizes an intra-procedural branched set-based Data Flow Framework. 
 * The analysis builds on {@link DataFlowFramework} by adding the ability to compute
 * data flow facts for outgoing edges of branch nodes.   These branches are represented
 * by pairs of nodes, i.e., the branch and a successor of the branch.
 * 
 * @param <E>
 *          The element type of the data flow lattice.
 *          
 * @author dwyer
 */
public abstract class EdgeDataFlowFramework<E> extends DataFlowFramework<E> {
	// Record outputs for edges in addition to nodes in base framework
	protected Map<Pair<ASTNode, ASTNode>, Set<E>> edgeOutMap = new HashMap<Pair<ASTNode, ASTNode>, Set<E>>();

	/**
	 * Clear analysis results
	 */
	@Override
	protected void clear() {
		super.clear();
		edgeOutMap = new HashMap<Pair<ASTNode, ASTNode>, Set<E>>();
	}

	@Override
	/**
	 * Compute the transfer function for all edges whose source is the 
	 * current CFG node. If that results in new values then update the 
	 * out set for the edge. 
	 * 
	 * @param n
	 * @return
	 */
	protected boolean compute(final ASTNode n) {
		boolean newValues = false;
		if (succs(n) != null) {
			for (ASTNode s : succs(n)) {
				//System.out.println("Edge ("+n.getSource()+"->"+s.getSource()+")");

				Set<E> inSet = getInSet(n);
				
				//System.out.println("   in = "+inSet);
				
				inSet = update(inSet, n, s);
				final Set<E> outSet = getOutSet(n, s);
				
				if (!inSet.containsAll(outSet) || !outSet.containsAll(inSet)) {
					outSet.clear();
					outSet.addAll(inSet);
					inSet.clear();
					newValues |= true;
				}
				
				//System.out.println("   out = "+outSet);

			}
		}
		return newValues;
	}

	/*
	 * Function space definition
	 */
	
	/*
	 * Edge gen-kill functions
	 */
	protected Set<E> gen(Set<E> set, ASTNode n, ASTNode s) {
		return new HashSet<E>();
	}
	
	protected Set<E> kill(Set<E> set, ASTNode n, ASTNode s) {
		return new HashSet<E>();
	}

	/*
	 * Override this to define a non-gen-kill function space
	 */	
	protected Set<E> update(Set<E> inSet, ASTNode n, ASTNode s) {
		inSet.removeAll(kill(inSet, n, s));
		inSet.addAll(gen(inSet, n, s));
		return inSet;  
	}
	
	/*
	 * Returns the in-set of an {@link ASTNode}.  
	 * 
	 * @param s The {@link ASTNode}.
	 * @return The in-set of the given {@link ASTNode}.
	 */
	 public Set<E> getInSet(final ASTNode s) {
		assert s != null;
		Set<E> inSet = (s == start()) ? init : bottom;

		if (preds(s) != null) {
			for (final ASTNode pred : preds(s)) {
				inSet = merge(inSet, getOutSet(pred, s));
			}
		}

		return inSet;
	}
 
	/**
	 * Returns the out-set for the branch of {@link ASTNode} n leading
	 * to {@link ASTNode} s.
	 * 
	 * @param n the source of the branch
	 * @param s the destination of the branch
	 * @return the out-set of the branch
	 */
	public Set<E> getOutSet(final ASTNode n, final ASTNode s) {
			assert n != null;

			Pair<ASTNode, ASTNode> edge = new Pair<ASTNode, ASTNode>(n, s);
			Set<E> result = edgeOutMap.get(edge);
			if (result == null) {
				result = new HashSet<E>(bottom);
				edgeOutMap.put(edge, result);
			}
			return result;
	}

	/*
	 * Returns the {@link String} representation of all computed RD analysis results.
	 */
	@Override
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
		for (final Pair<ASTNode,ASTNode> p : this.edgeOutMap.keySet()) {
				sb.append("("+p.left.getSource()+","+p.right.getSource()+") ==> ");
				final TreeSet<String> ts = new TreeSet<String>();
				if (useInSet) {
					for (final E e : getInSet(p.left)) {
						ts.add(toString(e));
					}
				} else {
					for (final E e : getOutSet(p.left,p.right)) {
						ts.add(toString(e));
					}
				}
				sb.append("{");
				for (final String str : ts) {
					sb.append(str);
					sb.append(", ");
				}
				final String str = sb.toString();
				sb.setLength(0);
				list.add(str.substring(0, str.length() - 2) + "}\n");
		}
		Collections.sort(list);
		for (final String s : list) {
			sb.append(s);
		}
		return sb.toString();
	}
}
