package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.Map;

import edu.udel.cis.vsl.abc.analysis.dataflow.IF.AbstractValue;
import edu.udel.cis.vsl.abc.analysis.dataflow.IF.Evaluation;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.ast.value.IF.IntegerValue;
import edu.udel.cis.vsl.abc.ast.value.IF.Value;


/**
 * This class is the common implementation of evaluation interface.
 * 
 * This is a general implementation that evaluates different calculations over AST
 * for the general value type {@link AbstractValue}
 *          
 * @author dxu
 */

public class EvaluationCommon implements Evaluation<AbstractValue>{
	boolean debug = false;
	DataflowUtilities untilities = new DataflowUtilities(null);
	
	public AbstractValue evaluate(ASTNode expr, Map<Entity, AbstractValue> map, AbstractValue returnValue) {
	
		returnValue = evaluateIterator(expr,map,returnValue);
		
		ASTNode assignedVar = expr.parent().child(0);
		if(assignedVar instanceof OperatorNode){
			Operator op = ((OperatorNode) assignedVar).getOperator();
			if(op == Operator.SUBSCRIPT){
				System.out.println("AssignVar\t" + assignedVar);
				
//				Entity e = ((IdentifierExpressionNode) assignedVar.child(0)).getIdentifier().getEntity();
//				AbstractValue originalValue = map.get(e);
				AbstractValue originalValue = evaluateIterator(assignedVar, map, returnValue);
				if(originalValue != null)
					returnValue = returnValue.union(returnValue, originalValue);
			}
		}
		
		return returnValue;
	}
	


	public AbstractValue evaluateIterator(ASTNode expr, Map<Entity, AbstractValue> map, AbstractValue returnValue) {
		//Handles an operator node
		if (expr instanceof OperatorNode){
			ASTNode leftNode = expr.child(0);
			ASTNode rightNode = expr.child(1);
			AbstractValue leftValue = evaluateIterator(leftNode, map, returnValue);
			AbstractValue rightValue = evaluateIterator(rightNode, map, returnValue);

			Operator op = ((OperatorNode) expr).getOperator();

			switch(op){
				case PLUS: returnValue = returnValue.plus(leftValue,rightValue); break;
				case MINUS: returnValue = returnValue.minus(leftValue,rightValue); break;
				case TIMES: returnValue = returnValue.multiply(leftValue,rightValue); break;
				case DIV: returnValue = returnValue.divide(leftValue,rightValue); break;
				case SUBSCRIPT:
					
					IdentifierExpressionNode arrayID = untilities.baseArray((OperatorNode)expr);
					Entity e = arrayID.getIdentifier().getEntity();

					returnValue = map.get(e);
					break;

				default:
					assert false : "Unsupported operation: " + op;
			}

			if (debug) System.out.println("OP\t"+returnValue);
		}

		//Handles an identifier node
		else if (expr instanceof IdentifierExpressionNode){

			Entity e = ((IdentifierExpressionNode) expr).getIdentifier().getEntity();
			returnValue = map.get(e);
			
			if (debug) System.out.println("ID\t"+returnValue);
		}

		//Handles a constant node
		else if (expr instanceof ConstantNode){

			ConstantNode conNode = (ConstantNode) expr;
			Value v = conNode.getConstantValue();
			long value;

			if (v.getType().kind() == TypeKind.BASIC) {
				StandardBasicType btn = (StandardBasicType)v.getType();

				switch (btn.getBasicTypeKind()) {
					case INT:
					case LONG:
					case LONG_LONG:
					case SHORT:
						value = (long) ((IntegerValue)v).getIntegerValue().intValue();
						returnValue = returnValue.setValue(value);
						break;

					default:
						assert false : "Expected an integral type for a ConstantNode";
				}
			} else{
				assert false : "Expected a basic type for a ConstantNode";
			}
			
			if(debug) System.out.println("CO\t"+returnValue);
		}
		else{
			assert false : "Unsupported node type" + expr;
		return null;
		}
		
		return returnValue;
	}
}
