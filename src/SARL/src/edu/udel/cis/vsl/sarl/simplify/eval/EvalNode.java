package edu.udel.cis.vsl.sarl.simplify.eval;

import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A node in the "tree" used to represent the polynomial. Leaf nodes are either
 * constants or variables. Non-leaf nodes represent either addition,
 * multiplication, or exponentiation. It's not really a tree, because we allow
 * sharing. So it's a DAG.
 * 
 * @author xxxx
 *
 */
abstract class EvalNode<T> {

	public static enum EvalNodeKind {
		ADD, MUL, CONST, VAR, POW
	}

	protected int nodeId = -1;

	/**
	 * The cached result of evaluating this node.
	 */
	protected T value = null;

	/**
	 * The parent nodes of this node, i.e., all nodes in the tree that have this
	 * node as a child. (So, it isn't really a tree.)
	 */
	protected List<EvalNode<T>> parents = new LinkedList<>();

	/**
	 * The number of times method {@link #evaluate()} has been called.
	 */
	protected int evalCount = 0;

	protected int isoCode = 0;

	/**
	 * Add the given node to the parent list of this node.
	 * 
	 * @param parent
	 *            the node to make a parent
	 */
	void addParent(EvalNode<T> parent) {
		parents.add(parent);
	}

	/**
	 * Returns the set of parents.
	 * 
	 * @return the parents of this node
	 */
	Collection<EvalNode<T>> getParents() {
		return parents;
	}

	void nullifyValue() {
		if (value != null) {
			value = null;
			for (EvalNode<T> parent : parents)
				parent.nullifyValue();
		}
	}

	/**
	 * Computes the value of this node, a concrete rational number. Might return
	 * a cached value.
	 * 
	 * @return the result of evaluating this node
	 */
	abstract T evaluate();

	/**
	 * Increments the evaluation count; if that count then equals the number of
	 * parents of this node, sets the {@link #value} to {@code null} so the
	 * {@link BigInteger}s can be swept up by the garbage collector.
	 * 
	 * @return the {@link #value} in the pre-state (before possibly setting it
	 *         to {@code null})
	 */
	T clearOnCount() {
		evalCount++;
		if (evalCount == parents.size()) {
			T result = value;

			value = null;
			return result;
		} else {
			return value;
		}
	}

	int depth() {
		return 1;
	}

	long numDescendants() {
		return 1;
	}

	public int numChildren() {
		return 0;
	}

	// numDistinctChildren?

	public EvalNode<T>[] getChildren() {
		return null;
	}

	public abstract EvalNodeKind kind();

	public abstract int isoCode();

	protected class EvalNodeValue {
	};
}