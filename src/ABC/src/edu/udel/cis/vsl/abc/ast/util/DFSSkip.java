package edu.udel.cis.vsl.abc.ast.util;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;

public class DFSSkip {
	/**
	 * Returns the next node (in DFS order) by skipping the whole sub-tree of
	 * the given node.
	 * 
	 * @param node
	 * @return
	 */
	static public ASTNode nextDFSSkip(ASTNode node) {
		ASTNode parent = node.parent();
		int childIdx = node.childIndex();

		if (parent == null)
			return null;
		if (parent.numChildren() == childIdx + 1)
			return nextDFSSkip(parent);

		ASTNode result = null;

		childIdx = childIdx + 1;
		while (result == null && childIdx < parent.numChildren())
			result = parent.child(childIdx++);
		if (result == null)
			return nextDFSSkip(parent);
		return result;
	}
}
