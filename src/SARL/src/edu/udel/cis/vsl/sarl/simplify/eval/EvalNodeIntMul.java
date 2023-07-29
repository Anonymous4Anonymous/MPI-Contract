package edu.udel.cis.vsl.sarl.simplify.eval;

import java.math.BigInteger;

/**
 * A node representing the product of its children
 * 
 * @author xxxx
 */
class EvalNodeIntMul extends EvalNodeInt {
	private EvalNodeInt[] children;

	private int depth = -1;

	private long numDescendants = -1;

	EvalNodeIntMul(EvalNodeInt[] children) {
		assert children.length >= 1;
		this.children = children;
		for (EvalNodeInt child : children)
			child.addParent(this);
	}

	@Override
	BigInteger evaluate() {
		if (value == null) {
			value = children[0].evaluate();
			for (int i = 1; i < children.length; i++)
				value.multiply(children[i].evaluate());
		}
		return clearOnCount();
	}

	@Override
	int depth() {
		if (depth < 0) {
			int maxChildDepth = 0;

			for (EvalNodeInt child : children) {
				int childDepth = child.depth();

				maxChildDepth = childDepth > maxChildDepth ? childDepth
						: maxChildDepth;
			}
			depth = 1 + maxChildDepth;
		}
		return depth;
	}

	@Override
	long numDescendants() {
		if (numDescendants < 0) {
			numDescendants = children.length;

			for (int i = 0; i < children.length; i++)
				numDescendants += children[i].numDescendants();
		}
		return numDescendants;
	}

	@Override
	public EvalNodeKind kind() {
		return EvalNodeKind.MUL;
	}

	@Override
	public int numChildren() {
		return children.length;
	}

	@Override
	public EvalNodeInt[] getChildren() {
		return children;
	}

	@Override
	public int isoCode() {
		if (isoCode == 0) {
			for (int i = 0; i < children.length; i++)
				isoCode += children[i].isoCode;
			isoCode = isoCode ^ EvalNodeKind.MUL.hashCode()
					^ ((depth() * parents.size()) * 15486277);
		}
		return isoCode;
	}
}