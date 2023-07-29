package edu.udel.cis.vsl.abc.analysis.dataflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.abc.analysis.common.CallAnalyzer;
import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.label.LabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.label.OrdinaryLabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.GotoNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.JumpNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.JumpNode.JumpKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LabeledStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode.LoopKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.SwitchNode;
import edu.udel.cis.vsl.abc.util.IF.Pair;

/**
 * This class implements control flow analysis on the functions defined within
 * a given {@link AST}.   The control flow graph is defined by an overlay edge set on
 * the AST's nodes.   This edge set defines the {@link #entry(Function))} and {@link #exit(Function)}
 * nodes for a function and {@link #predecessors(ASTNode)} and {@link #successors(ASTNode)} for
 * an {@link ASTNode}.
 * 
 * Note that control flow edges are built to connect the smallest set of {@link ASTNode}s that
 * are needed to express program semantics.  Many nodes in an AST are not connected with edges, for
 * example, the node for a for loop has no incident control flow edges, instead the initializer,
 * condition, body, and incrementer are all related directly with appropriate control flow edges.
 * 
 * Control flow analysis proceeds in two phases:
 *    1) A shallow pass over each compound statement is performed (beginning with a function definitions
 *       body) in order to sequence the statements in the block.   This may introduce "false" control 
 *       flow edges, e.g., when a statement in the block is a compound statement with a return statement.
 *    2) A deep pass then descends into each statement in the block and uses the "false" control flow
 *       established above to determine how to properly target control flow from within the statement.
 *       Prior to completing this pass on a statement the "false" control flow edges are removed.
 * 
 * The class implements a singleton pattern which allows the control flow analysis to be performed
 * incrementally on functions as needed.
 * 
 * TBD:
 *    1) Identify library calls and skip their analysis
 *    2) Inlining for call sensitive analysis
 * 
 * @author dwyer
 * 
 */
public class ControlFlowAnalysis {
	private static ControlFlowAnalysis instance = null;

	protected Map<ASTNode, Set<ASTNode>> successors = new HashMap<ASTNode, Set<ASTNode>>();
	protected Map<ASTNode, Set<ASTNode>> predecessors = new HashMap<ASTNode, Set<ASTNode>>();
	
	protected Map<Entity, Pair<ASTNode, ASTNode>> functionEntryExit = new HashMap<Entity, Pair<ASTNode, ASTNode>>();
	
	// Function -> Label -> Statement
	protected Map<Entity, Map<Entity, ASTNode>> labelToStmt = new HashMap<Entity, Map<Entity, ASTNode>>();

	/*
	 * For a method: 
	 *   (1) The body of the definition is the entry statement 
	 *   (2) A new dummy null statement is the distinguished exit statement
	 */
	private ASTNode entryMarker = null;
	private ASTNode exitMarker = null;
	
	private ASTNode breakPoint = null;
	private ASTNode continuePoint = null;
	
	FunctionDefinitionNode currentFunction = null;
		
	/**
	 * CFA is a singleton.  This allows it to be applied incrementally across a code base.
	 */
	protected ControlFlowAnalysis() {}
	
	public static ControlFlowAnalysis getInstance() {
		if (instance == null) {
			instance = new ControlFlowAnalysis();
		}
		return instance;
	}
	
	/*
	 * Utility functions for CFG edges
	 */

	private void addEdge(ASTNode n1, ASTNode n2) {
		Set<ASTNode> succs = successors.get(n1);
		if (succs == null) {
			succs = new HashSet<ASTNode>();
			successors.put(n1, succs);
		}
		succs.add(n2);

		Set<ASTNode> preds = predecessors.get(n2);
		if (preds == null) {
			preds = new HashSet<ASTNode>();
			predecessors.put(n2, preds);
		}
		preds.add(n1);
	}
	
	private void removeEdge(ASTNode n1, ASTNode n2) {
		Set<ASTNode> succs = successors.get(n1);
		assert(succs != null);
		succs.remove(n2);
		
		Set<ASTNode> preds = predecessors.get(n2);
		assert(preds != null);
		preds.remove(n1);
	}

	private ASTNode soleSuccessor(ASTNode node) {
		Set<ASTNode> succs = successors.get(node);
		assert succs.size() == 1;
		return succs.iterator().next();
	}
	
	/*
	 * Collect labels from AST to target goto statements
	 */
	private void buildLabelMap(FunctionDefinitionNode funDef) {
		if (labelToStmt.get(funDef.getEntity()) == null) {
			// build the map for this function
			Map<Entity, ASTNode> localMap = new HashMap<Entity, ASTNode>();
			labelToStmt.put(funDef.getEntity(), localMap);
			
			buildLocalMap(funDef.getBody(), localMap);
		}
	}
	
	private void buildLocalMap(ASTNode node, Map<Entity, ASTNode> map) {
		if (node instanceof LabeledStatementNode) {
			LabelNode label = ((LabeledStatementNode) node).getLabel();
			if (label instanceof OrdinaryLabelNode) {
				map.put(((OrdinaryLabelNode) label).getEntity(), node);
			}
		} else {
			for (ASTNode child : node.children()) {
				if (child != null)
					buildLocalMap(child, map);
			}
		}
	}
	
	/*
	 * We use the structure of the AST to simplify the initial construction of the CFG, but
	 * that creates some intermediate nodes that don't have any semantics.  We remove
	 * those since they don't add any value for data flow analyses.
	 */
	private void removeIntermediateNodes(ASTNode entry) {
		Set<ASTNode> seen = new HashSet<ASTNode>();
		removeIntNodes(entry, seen);
	}
	
	private Set<ASTNode> copy(Set<ASTNode> s) {
		Set<ASTNode> result = new HashSet<ASTNode>();
		for (ASTNode n : s) {
			result.add(n);
		}
		return result;
	}
	
	private void removeIntNodes(ASTNode node, Set<ASTNode> seen) {
		if (!seen.contains(node)) {
			seen.add(node);

			if ( (node instanceof CompoundStatementNode) && (node.numChildren() == 0) && node != entryMarker) {
				// Empty block: splice control flow around it at the next level up
				ASTNode succ = soleSuccessor(node);
				for (ASTNode p : copy(predecessors.get(node))) {
					addEdge(p,succ);
					removeEdge(node,succ);
					removeEdge(p,node);
				}
				// The new traversal point in the CFG is the successor
				removeIntNodes(succ, seen);

			} else if (((node instanceof CompoundStatementNode) && (node != entryMarker)) ||
					(node instanceof LoopNode) || 
					(node instanceof IfNode) || 
					(node instanceof SwitchNode)) {
				// Intermediate AST nodes: bypass them in CFG to connect semantics-bearing nodes
				ASTNode target = soleSuccessor(node);
				for (ASTNode p : copy(predecessors.get(node))) {
					addEdge(p,target);
					removeEdge(p,node);
				}
				// The new traversal point in the CFG is the target
				removeIntNodes(target, seen);

			} 

			if (successors.get(node) != null) {
				for (ASTNode succ : copy(successors.get(node))) {
					removeIntNodes(succ, seen);
				}
			}
		}
	}
	
	/**
	 * Dead nodes/edges may exist in the CFG due to the proper targeting of jumps.  
	 * Here we clean them up.
	 */
	private void removeDeadEdges(ASTNode entry) {
		Set<ASTNode> reachableNodes = new HashSet<ASTNode>();
		collectReachableNodes(entry, reachableNodes);
		
		// filter unmarked nodes out of the domain of the map
		successors.keySet().removeIf(e -> !reachableNodes.contains(e));
		predecessors.keySet().removeIf(e -> !reachableNodes.contains(e));	
		
		// filter unmarked nodes out of the co-domain of the map
		for (ASTNode n : successors.keySet()) {
			successors.get(n).removeIf(e -> !reachableNodes.contains(e));
		}
		for (ASTNode n : predecessors.keySet()) {
			predecessors.get(n).removeIf(e -> !reachableNodes.contains(e));
		}
	}
	
	private void collectReachableNodes(ASTNode s, Set<ASTNode> reachableNodes) {
		if (!reachableNodes.contains(s)) {
			reachableNodes.add(s);
			if (successors.get(s) != null) {
				for (ASTNode succ : successors.get(s)) {
					collectReachableNodes(succ, reachableNodes);
				}
			}
		}
	}
	
	/*
	 * AST specific cfa builder nodes
	 * 
	 * Function definitions are the root of CFG building for a given function
	 */
	
	private void cfaFunctionDefinitionNode(FunctionDefinitionNode funDef) {
		// establish per function traversal recording fields; clean them up below
		currentFunction = funDef;
		entryMarker = funDef.getBody();
		NodeFactory nf = funDef.getOwner().getASTFactory().getNodeFactory();
		exitMarker = nf.newNullStatementNode(funDef.getSource());
		
		// map labels to statements in function
		buildLabelMap(funDef);	

		addEdge(funDef.getBody(), exitMarker);
		cfaCompoundStatementNode(funDef.getBody());
		removeEdge(funDef.getBody(), exitMarker);
		
		removeIntermediateNodes(entryMarker);
		removeDeadEdges(entryMarker);
		
		functionEntryExit.put(funDef.getEntity(), new Pair<ASTNode, ASTNode>(entryMarker, exitMarker));
		
		currentFunction = null;
		entryMarker = null;
		exitMarker = null;
	}

	/*
	 * Dispatch to statement-specific control flow edge building methods
	 */
	private void cfaStatement(ASTNode node) {
		if (node instanceof FunctionDefinitionNode) {
			cfaFunctionDefinitionNode((FunctionDefinitionNode)node);

		} else if (node instanceof CompoundStatementNode) {
			cfaCompoundStatementNode((CompoundStatementNode) node);

		} else if (node instanceof LoopNode) {
			cfaLoopNode((LoopNode) node);
			
		} else if (node instanceof IfNode) {
			cfaIfNode((IfNode) node);

		} else if (node instanceof SwitchNode) {
			cfaSwitchNode((SwitchNode) node);
			
		} else if (node instanceof JumpNode) {
			cfaJumpNode((JumpNode) node);
			
		} else if (node instanceof LabeledStatementNode) {
			cfaLabeledStatementNode((LabeledStatementNode) node);	
			
		} else {
			for (ASTNode child : node.children()) {
				if (child != null)
					cfaStatement(child);
			}
		}
	}

	private void cfaCompoundStatementNode(CompoundStatementNode node) {
		// skip empty blocks, they will be removed from the CFG in a later pass
		if (node.numChildren() == 0) return;				
				
		ASTNode compoundSucc = soleSuccessor(node);
		/* 
		 * Pass at this level to construct sequential control flow.  Note that if
		 * a statement is a compound statement then this will create a false
		 * successor, which will be cleaned up in the second pass below.
		 */
		ASTNode currStmt = node;
		for (BlockItemNode item : node) {
			if (item instanceof StatementNode  || item instanceof VariableDeclarationNode) {
				addEdge(currStmt, item);
				currStmt = item;
			}
		}
		addEdge(currStmt, compoundSucc);

		// This deeper pass builds the sub-CFG and splices it in 
		for (BlockItemNode item : node) {
			cfaStatement(item);
		}

		// remove false successor
		removeEdge(node, compoundSucc);
	}

	/*
	 * The LoopNode serves purely as an intermediary in CFG construction.
	 */
	private void cfaLoopNode(LoopNode node) {
		ASTNode loopSucc = soleSuccessor(node);
		
		StatementNode loopBody = node.getBody();
		ExpressionNode loopCondition = node.getCondition();
		
		ASTNode oldBreakPoint = breakPoint;
		ASTNode oldContinuePoint = continuePoint;
		breakPoint = loopSucc;
		continuePoint = loopCondition;

		if (node.getKind() == LoopKind.FOR) {
			ForLoopInitializerNode loopInit = ((ForLoopNode)node).getInitializer();
			ExpressionNode loopInc = ((ForLoopNode)node).getIncrementer();
			
			addEdge(node,loopInit);
			addEdge(loopInit,loopCondition);
			addEdge(loopCondition,loopBody);
			addEdge(loopCondition,loopSucc);
			addEdge(loopBody,loopInc);
			addEdge(loopInc,loopCondition);

			cfaStatement(loopBody);
			
		} else if (node.getKind() == LoopKind.WHILE) {
			addEdge(node,loopCondition);
			addEdge(loopCondition,loopBody);
			addEdge(loopCondition,loopSucc);
			addEdge(loopBody,loopCondition);
			
			cfaStatement(loopBody);

		} else if (node.getKind() == LoopKind.DO_WHILE) {
			addEdge(node,loopBody);
			addEdge(loopBody,loopCondition);
			addEdge(loopCondition,loopBody);
			addEdge(loopCondition,loopSucc);
			
			cfaStatement(loopBody);
			
		} else {
			assert false : "Unexpected LoopKind";
		}
		
		breakPoint = oldBreakPoint;
		continuePoint = oldContinuePoint;
		
		removeEdge(node, loopSucc);
	}

	private void cfaIfNode(IfNode node) {
		ASTNode ifSucc = soleSuccessor(node);

		ExpressionNode cond = node.getCondition();
		addEdge(node, cond);
		
		StatementNode trueStatement = node.getTrueBranch();
		addEdge(cond, trueStatement);
		addEdge(trueStatement, ifSucc);
		cfaStatement(trueStatement);
		
		StatementNode falseStatement = node.getFalseBranch();
		// suppress edge creation for "if without else"
		if (falseStatement != null) {
			addEdge(cond, falseStatement);
			addEdge(falseStatement, ifSucc);
			cfaStatement(falseStatement);

		} else {
			addEdge(cond, ifSucc);
		}

		removeEdge(node, ifSucc);
	}

	private void cfaLabeledStatementNode(LabeledStatementNode node) {
		ASTNode lsSucc = soleSuccessor(node);
		
		ASTNode stmt = node.getStatement();
		addEdge(node, stmt);
		
		// create a successor for nested statement (ala what happens in the first pass of compound statements)
		addEdge(stmt, lsSucc);
		
		cfaStatement(stmt);
		
		removeEdge(node, lsSucc);
	}

	private void cfaJumpNode(JumpNode node) {
		// rerouting control flow from "sequential" flow established in prior pass
		removeEdge(node, soleSuccessor(node));

		if (node.getKind() == JumpKind.BREAK) {
			assert breakPoint != null : "Expected a well-defined control flow target for break";
			addEdge(node, breakPoint);
			
		} else if (node.getKind() == JumpKind.CONTINUE) {
			assert breakPoint != null : "Expected a well-defined control flow target for continue";
			addEdge(node, continuePoint);
			
		} else if (node.getKind() == JumpKind.RETURN) {
			assert breakPoint != null : "Expected a well-defined control flow target for return";
			addEdge(node, exitMarker);
			
		} else if (node.getKind() == JumpKind.GOTO) {
			Entity targetLabel = ((GotoNode)node).getLabel().getEntity();
			
			ASTNode targetStmt = labelToStmt.get(currentFunction.getEntity()).get(targetLabel);
			addEdge(node, targetStmt);
			
		} else {
			assert false : "Unexpected LoopKind";
		}

	}

	/*
	 * n-way branching from the condition to each of the labeled case statements
	 * 
	 * chain the cases as successors and use break processing to override.  this
	 * uses a two-pass approach like the compound statement, i.e., shallow wiring
	 * of switch structure then deep traversal of case blocks to fix cf wiring
	 * 
	 */
	private void cfaSwitchNode(SwitchNode node) {
		ASTNode switchSucc = soleSuccessor(node);
		
		ASTNode oldBreakPoint = breakPoint;
		breakPoint = switchSucc;

		/*
		 * Establish the switch-case control flow edges.
		 */
		ExpressionNode cond = node.getCondition();
		addEdge(node, cond);
				
		for (Iterator<LabeledStatementNode> cases = node.getCases(); cases.hasNext();) {
			ASTNode c = cases.next();
			addEdge(cond, c);
		}
		
		ASTNode def = node.getDefaultCase();
		if (def != null) {
			addEdge(cond, def);
		}
		
		/*
		 *  Now treat the switch body like a block, but it DO NOT build sequential flow into
		 *  the beginning of the block.  This preserves the fact that block fragments are only reachable
		 *  through switch-case control flow.
		 *  
		 *  Note that this does create sequential control flow between statements in the block, but
		 *  this is then overridden by break processing to achieve the desired control flow.
		 *  
		 *  We need to establish a false successor edge for the body for the duration of the pass.
		 */
		addEdge(node.getBody(), switchSucc);
		cfaStatement(node.getBody());
		removeEdge(node.getBody(), switchSucc);
		
		breakPoint = oldBreakPoint;

		removeEdge(node, switchSucc);	
	}
	
	/*
	 * Public methods for accessing control flow analysis results
	 */
	public Set<ASTNode> successors(ASTNode s) {
		return successors.get(s);
	}
	
	public Set<ASTNode> predecessors(ASTNode s) {
		return predecessors.get(s);
	}
	
	public ASTNode entry(Function f) {
		return functionEntryExit.get(f.getDefinition().getEntity()).left;
	}
	
	public ASTNode exit(Function f) {
		return functionEntryExit.get(f.getDefinition().getEntity()).right;
	}
	
	public Set<ASTNode> allNodes(Function f) {
		Set<ASTNode> result = new HashSet<ASTNode>();
		collectReachableNodes(entry(f), result);
		return result;
	}
	

	public void clear() {
		successors.clear();
		predecessors.clear();
		functionEntryExit.clear();
		labelToStmt.clear();
	}

	/**
	 * Perform control flow analysis for the entire AST.  This relies on the
	 * fact that call graph construction is a standard analysis.
	 */
	public void analyze(AST unit)  {
		for (Function f : CallAnalyzer.functions(unit)) {
			analyze(f);	
		}
	}
	
	public void analyze(Function f) {
		if (functionEntryExit.get(f) != null) return;
		cfaFunctionDefinitionNode(f.getDefinition());
	}
	
	/*
	 * Print functions for debugging
	 */

	public void printControlFlowGraph(AST unit) {
		Set<Function> seen = new HashSet<Function>();
		seen.add(unit.getMain());
		printControlFlowGraph(unit.getMain(), seen);
	}

	private void printControlFlowGraph(Function f, Set<Function> seen) {
		System.out.print(f.getName() + " [\n");
		// Visit the function definition body and print out the pred/succ relation for each statement
		FunctionDefinitionNode def = f.getDefinition();
		printFunctionControlFlow(def.getBody());
		System.out.println(" ]");
		
		for (Function callee : f.getCallees()) {
			if (!seen.contains(callee)) {
				seen.add(callee);
				printControlFlowGraph(callee, seen);
				seen.remove(callee);
			}
		}
	}
	
	private void printFunctionControlFlow(ASTNode node) {
		if (node != null) {
			if (successors.get(node) != null) {
				System.out.println("   succs("+node+") = "+successors.get(node));
				System.out.println("   preds("+node+") = "+predecessors.get(node)+"\n");
			}
			for (ASTNode child : node.children()) {
				if (child != null)
					printFunctionControlFlow(child);
			}
		}
	}

}
