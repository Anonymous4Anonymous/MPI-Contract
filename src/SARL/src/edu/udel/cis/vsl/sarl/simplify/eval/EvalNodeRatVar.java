package edu.udel.cis.vsl.sarl.simplify.eval;

/**
 * A variable node. This is a leaf node in the tree.
 * 
 * @author xxxx
 */
class EvalNodeRatVar extends EvalNodeRat {

	/**
	 * Sets the value of this variable. This automatically nullifies all
	 * ancestor nodes of this node, since their values depend on this value.
	 * 
	 * @param value
	 *            the value to associate to this node
	 */
	public void setValue(Rat value) {
		this.value = value;
		for (EvalNode<Rat> parent : getParents()) {
			parent.nullifyValue();
		}
	}

	@Override
	Rat evaluate() {
		return value;
	}

	@Override
	public EvalNodeKind kind() {
		return EvalNodeKind.VAR;
	}

	@Override
	public int isoCode() {
		if (isoCode == 0) {
			isoCode = 345 * EvalNodeKind.VAR.hashCode()
					^ ((parents.size() + 345) * 179426339);
		}
		return isoCode;
	}
}