package edu.udel.cis.vsl.sarl.simplify.eval;

/**
 * A node representing the sum of its children.
 * 
 * @author xxxx
 */
class EvalNodeRatAdd extends EvalNodeRat {
	private EvalNodeRat[] children;

	private int depth = -1;

	private long numDescendants = -1;

	EvalNodeRatAdd(EvalNodeRat[] children) {
		assert children.length >= 1;
		this.children = children;
		for (EvalNodeRat child : children)
			child.addParent(this);
	}

	@Override
	Rat evaluate() {
		if (value == null) {
			value = new Rat(children[0].evaluate());
			for (int i = 1; i < children.length; i++)
				value.add(children[i].evaluate());
		}
		return clearOnCount();
	}

	@Override
	int depth() {
		if (depth < 0) {
			int maxChildDepth = 0;

			for (EvalNode<Rat> child : children) {
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
	public int numChildren() {
		return children.length;
	}

	@Override
	public EvalNode<Rat>[] getChildren() {
		return children;
	}

	@Override
	public EvalNodeKind kind() {
		return EvalNodeKind.ADD;
	}

	@Override
	public int isoCode() {
		if (isoCode == 0) {
			for (int i = 0; i < children.length; i++)
				isoCode += children[i].isoCode;
			isoCode = isoCode ^ EvalNodeKind.ADD.hashCode()
					^ (depth() * 179426339) ^ parents.size();
		}
		return isoCode;
	}
}