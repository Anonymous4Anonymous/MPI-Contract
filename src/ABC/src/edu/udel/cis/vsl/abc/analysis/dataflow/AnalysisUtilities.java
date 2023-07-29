package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.label.SwitchLabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LabeledStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.SwitchNode;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonOperatorNode;
import edu.udel.cis.vsl.abc.token.IF.Source;



/**
 * Utility functions for data flow analyses.
 * 
 * @author dwyer
 *
 */
public class AnalysisUtilities {
	ControlFlowAnalysis cfa;
	
	public AnalysisUtilities(ControlFlowAnalysis cfa) {
		this.cfa = cfa;
	}
	
	public Set<ASTNode> succs(ASTNode s) {
		return cfa.successors(s);
	}

	public Set<ASTNode> preds(ASTNode s) {
		return cfa.predecessors(s);
	}
	
	public boolean isBranch(ASTNode n) {
		return (cfa.successors(n) != null) && (cfa.successors(n).size() > 1);
	}
	
	public boolean isMerge(ASTNode n) {
		return (cfa.predecessors(n) != null) && (cfa.predecessors(n).size() > 1);
	}

	public boolean isNested(ASTNode n, ASTNode c) {
		if (n instanceof StatementNode || n instanceof ExpressionNode) {
			if (n.equals(c)) {
				return true;
			} else {
				Iterable<ASTNode> children = n.children();
				for (ASTNode child : children) {
					if (isNested(child, c)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean isAssignment(final ASTNode s) {
		if (s instanceof ExpressionStatementNode) {
			ExpressionNode e = ((ExpressionStatementNode)s).getExpression();
			if (e instanceof OperatorNode) {
				Operator op = ((OperatorNode)e).getOperator();
				if ( (op == Operator.ASSIGN) || 
						(op == Operator.POSTINCREMENT) || (op == Operator.POSTDECREMENT) || 
						(op == Operator.PREINCREMENT) || (op == Operator.PREDECREMENT) || 
						(op == Operator.BITANDEQ) || (op == Operator.BITOREQ) || (op == Operator.BITXOREQ) ||
						(op == Operator.DIVEQ) || (op == Operator.TIMESEQ) || (op == Operator.PLUSEQ) || 
						(op == Operator.MINUSEQ) || (op == Operator.MODEQ) ||
						(op == Operator.SHIFTLEFTEQ) || (op == Operator.SHIFTRIGHTEQ) ) {
					return true;
				}
			} 
		} 
		return false;
	}
	
	public boolean isDefinition(final ASTNode s) {
		if (s instanceof VariableDeclarationNode) {
			VariableDeclarationNode vdn = (VariableDeclarationNode)s;
			return vdn.isDefinition() && vdn.getInitializer() != null;
		}
		return false;
	}

	/**
	 * A variant of the ASTNode copy method that propagates Entity and Type attributes for
	 * IdentifierExpressionNode.
	 * 
	 * @param n
	 * @return copy of n with entity attributes
	 */ 
	protected ExpressionNode copyWithAttributes(ExpressionNode n) {
		ExpressionNode result = null;
		
		if (n instanceof IdentifierExpressionNode) {
			IdentifierExpressionNode original = (IdentifierExpressionNode)n;
			IdentifierExpressionNode ien = original.copy();
			ien.getIdentifier().setEntity(original.getIdentifier().getEntity());
			result = ien;
			
		} else if (n instanceof ConstantNode) {
			result = ((ConstantNode)n).copy();
			
		} else if (n instanceof OperatorNode) {
			OperatorNode original = (OperatorNode)n;
			List<ExpressionNode> arguments = new LinkedList<ExpressionNode>();
			int numArgs = original.getNumberOfArguments();

			for (int i = 0; i < numArgs; i++) {
				arguments.add(copyWithAttributes(original.getArgument(i)));
			}
			
			result = new CommonOperatorNode(original.getSource(), original.getOperator(), arguments);
			
		} else {
			assert false : "Unexpected expression node: "+n;
		}
		
		result.setInitialType(n.getInitialType());
		
		return result;
	}
	
	
	/**
	 * Access the branch condition for the branch edge.  This is purely an
	 * operation on {@link ASTNode}s, but it is in this class because it is
	 * essential for branched data flow analyses.
	 * 
	 * The returned expression does not share any ASTNodes with the existing
	 * AST; other AST structures, e.g., IdentifierNodes, may be shared or not
	 * depending on the semantics of copy.
	 * 
	 * @param n node at the source of the edge
	 * @param s successor at the destination of the edge
	 * @return expression encoding branch condition
	 */
	public ExpressionNode branchCondition(ASTNode n, ASTNode s) {
		NodeFactory nf = n.getOwner().getASTFactory().getNodeFactory();
		Source source = n.getSource();
		
		ExpressionNode result = nf.newBooleanConstantNode(source, true);
		
		if (isBranch(n)) {
			assert n instanceof ExpressionNode : "Expected expression node for branch condition";
			
			if (succs(n).size() == 2) {
				if (n.parent() instanceof IfNode) {
					IfNode ifn = (IfNode)n.parent();
					
					// if the successor is in the true branch somewhere
					if (isNested(ifn.getTrueBranch(),s)) {
						// true branch has the original condition
						result = ifn.getCondition();
					} else {
						// false branch requires wrapping with a negation
						result = nf.newOperatorNode(source, Operator.NOT, copyWithAttributes(ifn.getCondition()));
					}		
				} else if (n.parent() instanceof LoopNode) {
					LoopNode ln = (LoopNode)n.parent();

					// if the successor is in the body
					if (isNested(ln.getBody(),s)) {
						// true branch has the original condition
						result = ln.getCondition();
					} else {
						// false branch requires wrapping with a negation
						result = nf.newOperatorNode(source, Operator.NOT, copyWithAttributes(ln.getCondition()));
					}	
				} else {
					assert false : "Unexpected branching node";
				}
		
			} else {				
				// branch is a switch, which means that the condition is the expression that is compared to cases
				SwitchNode swn = (SwitchNode) n.parent();
				ExpressionNode swc = swn.getCondition();
				
				if (swn.getDefaultCase().equals(s)) {
					// default condition is the conjunction of the negation of all case label conditions
					ExpressionNode defaultCondition = nf.newBooleanConstantNode(source, true);
					for (Iterator<LabeledStatementNode> iter = swn.getCases(); iter.hasNext();) {
						LabeledStatementNode c = iter.next();
						SwitchLabelNode sln = (SwitchLabelNode) c.getLabel();

						// Copy the case constant to assemble the switch edge condition
						ExpressionNode caseConst = copyWithAttributes(sln.getExpression());
						OperatorNode caseCompare = nf.newOperatorNode(source, Operator.NEQ, copyWithAttributes(swc), caseConst);

						defaultCondition = nf.newOperatorNode(source, Operator.LAND, defaultCondition, caseCompare);	
					}
					result = defaultCondition;
				} else {
					// match the case label and return its condition
					for (Iterator<LabeledStatementNode> iter = swn.getCases(); iter.hasNext();) {
						LabeledStatementNode c = iter.next();

						if (c.equals(s)) {
							SwitchLabelNode sln = (SwitchLabelNode) c.getLabel();
							
							// Copy the case constant to assemble the switch edge condition
							ExpressionNode caseConst = copyWithAttributes(sln.getExpression());
							OperatorNode caseCompare = nf.newOperatorNode(source, Operator.EQUALS, copyWithAttributes(swc), caseConst);
							result = caseCompare;	
							return result;
						}
					}
					assert false : "Expected a matching case label";
				}
				
			}
		} 
		
		return result;	
	}
	
	public IdentifierExpressionNode baseArray(OperatorNode subscript) {
		assert subscript.getOperator() == OperatorNode.Operator.SUBSCRIPT : "Expected subscript expression";
		if (subscript.getArgument(0) instanceof IdentifierExpressionNode) {
			return (IdentifierExpressionNode) subscript.getArgument(0);
		}
		return baseArray((OperatorNode) subscript.getArgument(0));
	}

	public Entity getLHSVar(final ASTNode s) {
		if (isAssignment(s)) {
			ExpressionNode lhs = ((OperatorNode)((ExpressionStatementNode)s).getExpression()).getArgument(0);
			if (lhs instanceof IdentifierExpressionNode) {
				IdentifierNode id = ((IdentifierExpressionNode)lhs).getIdentifier();
				return id.getEntity();
			} else if (lhs instanceof OperatorNode) {
				OperatorNode opn = (OperatorNode)lhs;
				if (opn.getOperator() == Operator.SUBSCRIPT) {
					IdentifierExpressionNode idn = baseArray(opn);
					return idn.getIdentifier().getEntity();
				} else {
					assert false : "Unexpected operator node on LHS";
				}
			} else {
				assert false : "Unexpected LHS expression";
			}
		} else if (isDefinition(s)) {
			VariableDeclarationNode vdn = (VariableDeclarationNode)s;
			if ( vdn.isDefinition() && vdn.getInitializer() != null ) {
				return vdn.getEntity();
			}
		}
		return null;
	}
	
	public ExpressionNode getRHS(final ASTNode s) {
		if (isAssignment(s)) {
			OperatorNode opn = (OperatorNode) ((ExpressionStatementNode)s).getExpression();
			ExpressionNode rhs = null;
			if (opn.getNumberOfArguments() == 1) {
				rhs = ((OperatorNode)((ExpressionStatementNode)s).getExpression()).getArgument(0);
			} else if (opn.getNumberOfArguments() == 2) {
				// This might need refinement for, e.g., PLUSEQ, which has arg 0 on the LHS and RHS
				rhs = ((OperatorNode)((ExpressionStatementNode)s).getExpression()).getArgument(1);
			}
			return rhs;
		} else if (isDefinition(s)) {
			VariableDeclarationNode vdn = (VariableDeclarationNode)s;
			if ( vdn.isDefinition() && vdn.getInitializer() != null ) {
				return (ExpressionNode)vdn.getInitializer();
			}
		}
		return null;
	}
	

	public Set<Entity> collectRefs(ASTNode node) {
		Set<Entity> refs = new HashSet<Entity>();
		collectRefs(node, refs);
		return refs;
	}
	
	private void collectRefs(ASTNode node, Set<Entity> refs) {
		if (node instanceof IdentifierExpressionNode) {
			Entity idEnt = ((IdentifierExpressionNode) node).getIdentifier()
					.getEntity();
			refs.add(idEnt);

		} else if (node instanceof OperatorNode
				&& ((OperatorNode) node).getOperator() == Operator.SUBSCRIPT) {
			Entity idEnt = baseArray((OperatorNode) node).getIdentifier()
					.getEntity();
			refs.add(idEnt);

		} else if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				collectRefs(child, refs);
			}
		}
	}
}
