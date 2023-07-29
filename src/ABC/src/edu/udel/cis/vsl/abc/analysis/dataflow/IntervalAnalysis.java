package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.udel.cis.vsl.abc.analysis.dataflow.common.MyInterval;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.BasicTypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.ast.value.IF.IntegerValue;
import edu.udel.cis.vsl.abc.ast.value.IF.Value;
import edu.udel.cis.vsl.abc.util.IF.Pair;

public class IntervalAnalysis extends DataFlowFramework<Pair<Entity, MyInterval>>{
	private static IntervalAnalysis instance = null;

	Function currentFunction;

	ControlFlowAnalysis cfa;

	private EvaluateIntervalExpression ee;

	/**
	 * DFAs are singletons.  This allows them to be applied incrementally across a code base.
	 */
	protected IntervalAnalysis() {}

	public static IntervalAnalysis getInstance() {
		if (instance == null) {
			instance = new IntervalAnalysis();
		}
		return instance;
	}

	@Override
	public void clear() {
		super.clear();
		instance = null;
		cfa.clear();
	}

	@Override
	public void analyze(Function f) {
		if (analyzedFunctions.contains(f)) return;
		analyzedFunctions.add(f);
		currentFunction = f;

		// Perform control flow analysis (if needed)
		cfa = ControlFlowAnalysis.getInstance();
		cfa.analyze(f);

		Set<Pair<Entity, MyInterval>> init = 
				new HashSet<Pair<Entity,MyInterval>>();

		// Unprocessed nodes are assigned an empty set
		Set<Pair<Entity, MyInterval>> bottom = 
				new HashSet<Pair<Entity, MyInterval>>();

		computeFixPoint(init, bottom);
	}

	private boolean isAssignment(final ASTNode s) {
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

	private IdentifierExpressionNode baseArray(OperatorNode subscript) {
		assert subscript.getOperator() == OperatorNode.Operator.SUBSCRIPT : "Expected subscript expression";
		if (subscript.getArgument(0) instanceof IdentifierExpressionNode) {
			return (IdentifierExpressionNode) subscript.getArgument(0);
		}
		return baseArray((OperatorNode) subscript.getArgument(0));
	}

	private Entity getLHSVar(final ASTNode s) {
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
		}
		return null;
	}
	private ExpressionNode getRHS(final ASTNode s) {
		if (isAssignment(s)) {
			ExpressionNode rhs = ((OperatorNode)((ExpressionStatementNode)s).getExpression()).getArgument(1);
			return rhs;
		}
		return null;
	}

	private boolean isDefinition(final ASTNode s) {
		if (s instanceof VariableDeclarationNode) {
			VariableDeclarationNode vdn = (VariableDeclarationNode)s;
			return vdn.isDefinition() && vdn.getInitializer() != null;
		}
		return false;
	}

	@Override
	protected
	/*
	 * Generate constants that are assigned from for statements.
	 */
	Set<Pair<Entity, MyInterval>> gen(final Set<Pair<Entity,MyInterval>> set, final ASTNode n) {
		Set<Pair<Entity,MyInterval>> result = new HashSet<Pair<Entity,MyInterval>>();

		// Extremely simple interpretation of assignment.  No constant folding, no copy propagation, etc.
		if (isAssignment(n) || isDefinition(n)) {
			Entity lhsVar = getLHSVar(n);
			ExpressionNode rhs = getRHS(n);

			Map<Entity, MyInterval> map = new HashMap<Entity, MyInterval>();
			for(Pair<Entity, MyInterval> setElement : set){
				map.put(setElement.left, setElement.right);
			}

			MyInterval interval = ee.evaluate(rhs,map);


			/*if (rhs instanceof ConstantNode) {
				ConstantNode conNode = (ConstantNode)rhs;
				Value v = conNode.getConstantValue();
				long value;

				if (v.getType().kind() == TypeKind.BASIC) {
					BasicTypeNode btn = (BasicTypeNode)v.getType();
					switch (btn.getBasicTypeKind()) {
					case INT:
					case LONG:
					case LONG_LONG:
					case SHORT:
						value = (long) ((IntegerValue)v).getIntegerValue().intValue();
						interval = new Interval(value);
						break;
					default:
						break;
					}
				} else
					assert false : "Expected a basic type for a ConstantNode";
			}
			 */
			Pair<Entity, MyInterval> inEntry = 
					new Pair<Entity, MyInterval>(lhsVar, interval);
			result.add(inEntry);
		}
		return result;	
	}

	@Override
	protected
	/*
	 * MODIFIED
	 * Kill constants that are assigned into for statements.
	 */
	Set<Pair<Entity, MyInterval>> kill( Set<Pair<Entity, MyInterval>> set, final ASTNode n) {
		Set<Pair<Entity, MyInterval>> result = new HashSet<Pair<Entity, MyInterval>>();

		// Extremely simple interpretation of assignment.  No constant folding, no copy propagation, etc.
		if (isAssignment(n)) {
			Entity lhsVar = getLHSVar(n);
			for (Pair<Entity, MyInterval> inEntry : set) {
				if (inEntry.left.equals(lhsVar)) {
					result.add(inEntry);
				}
			}
		}
		return result;	
	}

	@Override
	protected Set<ASTNode> succs(ASTNode n) {
		return cfa.successors(n);
	}

	@Override
	protected Set<ASTNode> preds(ASTNode n) {
		return cfa.predecessors(n);
	}

	@Override
	protected ASTNode start() {
		ASTNode n = cfa.entry(currentFunction);
		assert n != null;
		return n;
	}


	@Override
	public String getAnalysisName() {
		return "Interval Analysis";
	}


	@Override
	protected Set<Pair<Entity, MyInterval>> merge(Set<Pair<Entity, MyInterval>> s1, Set<Pair<Entity, MyInterval>> s2) {
		Set<Pair<Entity,MyInterval>> result = new HashSet<Pair<Entity,MyInterval>>();

		Set<Entity> idOverlap = new HashSet<Entity>();

		// Compute the set of overlapping identifiers in the incoming sets of CP entries
		for (Pair<Entity, MyInterval> p1 : s1) {
			for (Pair<Entity, MyInterval> p2 : s2) {
				if (p1.left.equals(p2.left)) {
					idOverlap.add(p1.left);
				}
			}
		}

		// For entries with common identifiers, merge their CP data
		for (Pair<Entity, MyInterval> p1 : s1) {
			if (!idOverlap.contains(p1.left)) continue;

			for (Pair<Entity, MyInterval> p2 : s2) {
				if (!idOverlap.contains(p2.left)) continue;

				if (p1.left.equals(p2.left)) {

					//Merge
					MyInterval interval = new MyInterval(p1.right.getLow(),p2.right.getHigh());
					Pair<Entity, MyInterval> top = new Pair<Entity, MyInterval>(p1.left, interval);					
					result.add(top);
				}
			}
		}

		// Add the disjoint CP entries to the merge
		// TBD: this seems wrong.  We want these entries to go to "top".  What's the cleanest way to do that with lambdas?
		result.addAll(s1.stream().filter(p -> !idOverlap.contains(p.left)).collect(Collectors.toSet()));
		result.addAll(s2.stream().filter(p -> !idOverlap.contains(p.left)).collect(Collectors.toSet()));

		return result;
	}

	@Override
	public String toString(Pair<Entity, MyInterval> e) {
		String entry = e.left+"->"+ e.right;
		return "<"+entry+">";
	}


	
	private class EvaluateIntervalExpression{

		public MyInterval evaluate(ASTNode expr, Map<Entity, MyInterval> map) {

			//Handles an operator node
			if (expr instanceof OperatorNode){
				ASTNode leftNode = expr.child(0);
				ASTNode rightNode = expr.child(1);
				MyInterval leftValue = evaluate(leftNode, map);
				MyInterval rightValue = evaluate(rightNode, map);

				Operator op = ((OperatorNode) expr).getOperator();

				switch(op){
				
				case PLUS: leftValue.plus(rightValue); break;
				case MINUS: leftValue.minus(rightValue); break;
				case TIMES: leftValue.multiply(rightValue); break;
				case DIV: leftValue.divide(rightValue); break;
//				case MOD: leftValue.modulo(rightValue);break;

				default:
					assert false : "Unsupported operation!";
				}

				return leftValue;
			}

			//Handles an identifier node
			if (expr instanceof IdentifierExpressionNode){
				Entity e = ((IdentifierExpressionNode) expr).getIdentifier().getEntity();
				MyInterval i = map.get(e);
				return i;
			}

			//Handles a constant node
			if (expr instanceof ConstantNode){

				ConstantNode conNode = (ConstantNode) expr;
				Value v = conNode.getConstantValue();
				MyInterval interval = null;
				long value;

				if (v.getType().kind() == TypeKind.BASIC) {
					BasicTypeNode btn = (BasicTypeNode)v.getType();
					switch (btn.getBasicTypeKind()) {
					case INT:
					case LONG:
					case LONG_LONG:
					case SHORT:
						value = (long) ((IntegerValue)v).getIntegerValue().intValue();
						interval = new MyInterval(value);
						break;
					default:
						assert false : "Expected an integral type for a ConstantNode";
					}
				} else
					assert false : "Expected a basic type for a ConstantNode";

				return interval;
			}
			
			assert false : "Unsupported node type";

			return null;
		}
	}

}
