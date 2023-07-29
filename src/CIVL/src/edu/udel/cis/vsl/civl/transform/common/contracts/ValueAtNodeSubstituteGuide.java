package edu.udel.cis.vsl.civl.transform.common.contracts;

import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.civl.transform.SubstituteGuide;

//TODO: doc. The point is make up the plan of substitution with unreleased nodes.
/* Here all the ASTNodes in the private fields can have parent 
 * (which is very important to accomplish recursive substitution). 
 * They will not be touch before the actual substitution happens. 
 * If there are other substitutions will happen in either "state, process or expression",
 * that wil be fine.
*/
public class ValueAtNodeSubstituteGuide extends SubstituteGuide {

	ValueAtNodeSubstituteGuide(ExpressionNode state, ExpressionNode process,
			ExpressionNode valueAtExpression, ExpressionNode oldNode) {
		super(new ExpressionNode[]{state, process, valueAtExpression}, oldNode);
	}

	@Override
	protected ASTNode buildNewNode(NodeFactory nf) {
		assert super.newNodeComponents.length == 3;
		for (int i = 0; i < 3; i++)
			newNodeComponents[i].remove();
		return nf.newValueAtNode(super.oldNode.getSource(),
				(ExpressionNode) newNodeComponents[0],
				(ExpressionNode) newNodeComponents[1],
				(ExpressionNode) newNodeComponents[2]);
	}
}
