package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import edu.udel.cis.vsl.abc.analysis.dataflow.IF.AbstractValue;
import edu.udel.cis.vsl.abc.analysis.dataflow.common.IntervalValue;
import edu.udel.cis.vsl.abc.analysis.dataflow.common.IntervalValue.IntervalRelation;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonIdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.value.IF.ValueFactory;
import edu.udel.cis.vsl.abc.util.IF.Pair;

public class ConditionalIntervalAnalysis extends EdgeDataFlowFramework<Pair<Entity, IntervalValue>>{
	private static ConditionalIntervalAnalysis instance = null;
	Function currentFunction;
	
	ControlFlowAnalysis cfa;
	AnalysisUtilities au;
	ValueFactory vf;
	

	private EvaluationCommon evaluator;

	private boolean debug = false;

	/**
	 * DFAs are singletons.  This allows them to be applied incrementally across a code base.
	 */
	protected ConditionalIntervalAnalysis() {
		evaluator = new EvaluationCommon();
	}

	public static ConditionalIntervalAnalysis getInstance() {
		if (instance == null) {
			instance = new ConditionalIntervalAnalysis();
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

		//
		au = new AnalysisUtilities(cfa);

		Set<Pair<Entity, IntervalValue>> init = new HashSet<Pair<Entity,IntervalValue>>(); 

		// Unprocessed nodes are assigned an empty set
		Set<Pair<Entity, IntervalValue>> bottom = new HashSet<Pair<Entity, IntervalValue>>();

		computeFixPoint(init, bottom);
	}
	

	/*
	 * New implementation of the set update function.
	 * Satisfiable???

	@Override
	protected Set<Pair<Entity, IntervalValue>> update(Set<Pair<Entity, IntervalValue>> inSet, ASTNode n) {
		inSet = gen(inSet, n);
		return inSet;  
	}
	 */

	@Override
	/*
	 * Generate constants that are assigned from for statements.
	 */
	protected Set<Pair<Entity, IntervalValue>> gen(final Set<Pair<Entity,IntervalValue>> set, final ASTNode n) {

		final Set<Pair<Entity,IntervalValue>> result = new HashSet<Pair<Entity,IntervalValue>>();

		final Entity lhsVar = au.getLHSVar(n);

		if(debug) System.out.println("\nGen\n"+n);

		if (au.isAssignment(n) || au.isDefinition(n)) {
			assert lhsVar != null;

			ExpressionNode rhs = au.getRHS(n);

			assert rhs != null : "not simple assignment!";

			Map<Entity, AbstractValue> map = new HashMap<Entity, AbstractValue>();
			for(Pair<Entity, IntervalValue> setElement : set){
				map.put(setElement.left, setElement.right);
			}

			AbstractValue abValue = evaluator.evaluate(rhs, map, new IntervalValue());

			//if(debug) System.out.println("RS\t"+abValue);

			IntervalValue interval = (IntervalValue) abValue;

			assert !interval.isEmpty() : "empty interval?";

			Pair<Entity, IntervalValue> inEntry = new Pair<Entity, IntervalValue>(lhsVar, interval);

			if(debug) System.out.println("Before\t"+set);

			//			for(Pair<Entity, IntervalValue> a : set){
			//				if(!a.left.equals(inEntry.left)){
			//					result.add(a);
			//				}
			//			}

			if(debug) System.out.println("After\t"+result);

			result.add(inEntry);

		}

		// Handles branch???
		else if(au.isBranch(n)){
			System.out.println("BRANCH");

			// defines lhs is an id
			if(n.child(0) instanceof CommonIdentifierExpressionNode || n.child(0) instanceof OperatorNode){

				Entity lhs;
				ExpressionNode rhs = (ExpressionNode) n.child(1);

				if(n.child(0) instanceof CommonIdentifierExpressionNode){					
				  lhs = ((CommonIdentifierExpressionNode) n.child(0)).getIdentifier().getEntity();
				}
				else{
				  lhs = ((CommonIdentifierExpressionNode) n.child(0).child(0)).getIdentifier().getEntity();
				}

				System.out.println("not simple assignment!" + lhs +" " + rhs);

				Map<Entity, AbstractValue> map = new HashMap<Entity, AbstractValue>();
				for(Pair<Entity, IntervalValue> setElement : set){
					map.put(setElement.left, setElement.right);
				}

				AbstractValue abValue = evaluator.evaluate(rhs, map, new IntervalValue());
				IntervalValue rhsInterval = (IntervalValue) abValue;
				IntervalValue lhsInterval = (IntervalValue) map.get(lhs);

				IntervalValue intersection = lhsInterval.intersection(rhsInterval);
				System.out.println("INTERSECTIONNN"+intersection);

				IntervalRelation ir = lhsInterval.relation(rhsInterval);

				IntervalValue lhsUpdated = new IntervalValue();
				Operator op = ((OperatorNode) n).getOperator();
				switch(op){
					case GT: break;
					case GTE:
						if(intersection.isEmpty()){
							if(ir != IntervalRelation.GTE){
								lhsUpdated = intersection;
							}
							else{
								lhsUpdated = lhsInterval;
							}
						}
						else
							lhsUpdated = intersection;

						Pair<Entity, IntervalValue> inEntry = new Pair<Entity, IntervalValue>(lhs, lhsUpdated);
						result.add(inEntry);

						break;
					case LT: ; break;
					case LTE: ; break;
					case EQUALS: ; break;
					case NEQ: ; break;
					default:
						assert false: "Unsupported operation of condition. " + op;
				}
			}
			else{
				assert false: "Unsupported complicated lhs condition. " + n;
			}
		}

		else{
			System.out.println("Unknown node: " + n);
			
			
			
			result.addAll(set);
		}

		if(debug) System.out.println("result:\t"+result);
		return result;	
	}

	@Override
	protected
	/*
	 * MODIFIED
	 * Kill constants that are assigned into for statements.
	 */
	Set<Pair<Entity, IntervalValue>> kill( Set<Pair<Entity, IntervalValue>> set, final ASTNode n) {
		Set<Pair<Entity, IntervalValue>> result = new HashSet<Pair<Entity, IntervalValue>>();

		// Extremely simple interpretation of assignment.  No constant folding, no copy propagation, etc.
		if (au.isAssignment(n) || au.isDefinition(n)) {
			Entity lhsVar = au.getLHSVar(n);
			assert lhsVar != null: "null?";
			for (Pair<Entity, IntervalValue> inEntry : set) {
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
	protected Set<Pair<Entity, IntervalValue>> merge(Set<Pair<Entity, IntervalValue>> s1, Set<Pair<Entity, IntervalValue>> s2) {
		Set<Pair<Entity,IntervalValue>> result = new HashSet<Pair<Entity,IntervalValue>>();

		Set<Entity> idOverlap = new HashSet<Entity>();

		// Compute the set of overlapping identifiers in the incoming sets of CP entries
		for (Pair<Entity, IntervalValue> p1 : s1) {
			for (Pair<Entity, IntervalValue> p2 : s2) {
				if (p1.left.equals(p2.left)) {
					idOverlap.add(p1.left);
				}
			}
		}

		// For entries with common identifiers, merge their CP data
		for (Pair<Entity, IntervalValue> p1 : s1) {
			if (!idOverlap.contains(p1.left)) continue;

			for (Pair<Entity, IntervalValue> p2 : s2) {
				if (!idOverlap.contains(p2.left)) continue;

				if (p1.left.equals(p2.left)) {

					//Merge
					IntervalValue iv = new IntervalValue();
					iv = (IntervalValue) iv.union(p1.right,p2.right);

					Pair<Entity, IntervalValue> top = new Pair<Entity, IntervalValue>(p1.left, iv);					
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
	public String toString(Pair<Entity, IntervalValue> e) {
		String entry = e.left+"->"+ e.right;
		return "<"+entry+">";
	}
	
	@SuppressWarnings("unused")
	private boolean isBottom(final Set<Pair<Entity, IntervalValue>> set){
		for(Pair<Entity, IntervalValue> p: set)
			if(!p.right.isBottom())
				return false;
		return true;
		
	}
}
