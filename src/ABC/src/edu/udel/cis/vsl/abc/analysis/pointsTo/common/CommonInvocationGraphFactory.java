package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNode;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNode.IGNodeKind;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNodeFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;

public class CommonInvocationGraphFactory implements InvocationGraphNodeFactory {

	@Override
	public InvocationGraphNode newNode(Function function,
			InvocationGraphNode parent, AssignExprIF returnTo,
			AssignExprIF... actualArgs) {
		InvocationGraphNode ancestor = parent;
		IGNodeKind kind = IGNodeKind.ORDINARY;

		// decide kind:
		while (ancestor != null) {
			if (ancestor.function() == function) {
				kind = IGNodeKind.APPROXIMATE;
				ancestor.markRecursive();
				break;
			}
			ancestor = ancestor.parent();
		}

		InvocationGraphNode newNode;

		if (kind != IGNodeKind.APPROXIMATE)
			newNode = new CommonInvocationGraphNode(parent, function, kind,
					returnTo, actualArgs);
		else
			newNode = new CommonInvocationGraphNode(parent, ancestor, function,
					kind, returnTo, actualArgs);
		if (parent != null)
			parent.addChild(newNode);
		return newNode;
	}
}
