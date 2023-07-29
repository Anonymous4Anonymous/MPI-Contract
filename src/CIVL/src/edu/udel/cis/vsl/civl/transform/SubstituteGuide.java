package edu.udel.cis.vsl.civl.transform;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;

public abstract class SubstituteGuide {

	protected boolean used = false;

	protected ASTNode[] newNodeComponents;

	protected ASTNode oldNode;

	protected SubstituteGuide(ASTNode[] newNodeComponents, ASTNode oldNode) {
		assert oldNode.parent() != null;
		this.newNodeComponents = newNodeComponents;
		this.oldNode = oldNode;
	}

	/**
	 * substitute the old node with a new created node
	 * 
	 * @param nf
	 *            a reference to {@link NodeFactory}
	 * @return the reference to the new node
	 */
	public ASTNode subsitute(NodeFactory nf) {
		if (used)
			throw new CIVLInternalException(
					"This substitute guide has been used.",
					oldNode.getSource());
		assert oldNode.parent() != null;

		ASTNode parent = oldNode.parent();
		int childIdx = oldNode.childIndex();
		ASTNode newNode = buildNewNode(nf);

		newNode.remove();
		oldNode.remove();
		parent.setChild(childIdx, newNode);
		used = true;
		return newNode;
	}

	/**
	 * How to construct the new node with the {@link #newNodeComponents} depends
	 * on the implementation.
	 * 
	 * @param nf
	 * @return
	 */
	protected abstract ASTNode buildNewNode(NodeFactory nf);

	@Override
	public String toString() {
		return oldNode.prettyRepresentation().toString();
	}
}
