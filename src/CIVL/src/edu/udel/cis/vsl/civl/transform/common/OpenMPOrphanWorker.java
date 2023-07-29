package edu.udel.cis.vsl.civl.transform.common;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpParallelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.transform.IF.OpenMPOrphanTransformer;

/**
 * This transformer will move or copy orphan constructs into the omp parallel
 * region. It will first search for {@link OmpParallelNode}, then search for
 * {@link FunctionCallNode} whose definition uses shared variables or omp. If
 * the {@link FunctionDefinitionNode} uses omp, then move the
 * {@link FunctionDefinitionNode} into the {@link OmpParallelNode}. If the
 * {@link FunctionDefinitionNode} just uses shared variables, copy the
 * {@link FunctionDefinitionNode} into the {@link OmpParallelNode}.
 * 
 * @author xxxxxxxx
 *
 */
public class OpenMPOrphanWorker extends BaseWorker {

	public OpenMPOrphanWorker(ASTFactory astFactory) {
		super(OpenMPOrphanTransformer.LONG_NAME, astFactory);
		this.identifierPrefix = "$omp_orphan_";
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		SequenceNode<BlockItemNode> root = ast.getRootNode();
		AST newAst;
		Function main = ast.getMain();
		Set<Function> visitedFunctions = new HashSet<>();
		Set<String> globalVars = retrieveVars(root.iterator());

		ast.release();
		searchOMPParallel(main, visitedFunctions, globalVars);
		newAst = astFactory.newAST(root, ast.getSourceFiles(),
				ast.isWholeProgram());

		return newAst;
	}

	/**
	 * Search for #pragma omp parallel blocks and process its body.
	 * 
	 * @param visitedFunctions
	 *            Functions that have already been visited.
	 */
	private void searchOMPParallel(Function function,
			Set<Function> visitedFunctions, Set<String> globalVars) {
		if (function == null || function.getDefinition() == null
				|| visitedFunctions.contains(function))
			return;
		else
			visitedFunctions.add(function);

		CompoundStatementNode body = function.getDefinition().getBody();
		Iterator<BlockItemNode> bodyIter = body.iterator();

		while (bodyIter.hasNext()) {
			BlockItemNode item = bodyIter.next();

			if (item instanceof OmpParallelNode) {
				// An #pragma omp parallel statement
				OmpParallelNode ompParallelNode = (OmpParallelNode) item;
				ASTNode eighthChild = ompParallelNode.child(7);

				if (eighthChild instanceof CompoundStatementNode) {
					CompoundStatementNode ompParallelBody = (CompoundStatementNode) eighthChild;
					Set<FunctionDefinitionNode> toBeInserted = searchFunctionToBeInserted(
							ompParallelBody, globalVars);
					Set<String> alreadyExistsFunctionDef = getFunctionDefs(
							ompParallelBody);
					List<FunctionDefinitionNode> insertion = new LinkedList<>();

					for (FunctionDefinitionNode functionDef : toBeInserted) {
						if (!alreadyExistsFunctionDef
								.contains(functionDef.getIdentifier().name()))
							insertion.add(functionDef);
					}
					ompParallelBody.insertChildren(0,
							new LinkedList<>(insertion));
				}
			}
		}
		for (Function callee : function.getCallees())
			searchOMPParallel(callee, visitedFunctions, globalVars);
	}

	/**
	 * Get the set of identifiers of {@link FunctionDefinitionNode} inside a
	 * {@link CompoundStatementNode}.
	 * 
	 * @param compoundStatementNode
	 * @return The set of identifiers of {@link FunctionDefinitionNode}.
	 */
	private Set<String> getFunctionDefs(
			CompoundStatementNode compoundStatementNode) {
		Set<String> functionDefs = new HashSet<>();
		Iterator<BlockItemNode> itemsIter = compoundStatementNode.iterator();

		while (itemsIter.hasNext()) {
			BlockItemNode item = itemsIter.next();

			if (item instanceof FunctionDefinitionNode) {
				functionDefs.add(
						((FunctionDefinitionNode) item).getIdentifier().name());
			}
		}
		return functionDefs;
	}

	/**
	 * Search for {@link Function}s that are either use shared variable or use
	 * omp.
	 * 
	 * @param node
	 *            The node to be searched.
	 * @param globalVars
	 * 
	 * @return The set of {@link FunctionDefinitionNode} to be inserted at the
	 *         beginning of the {@link OmpParallelNode}.
	 */
	private Set<FunctionDefinitionNode> searchFunctionToBeInserted(ASTNode node,
			Set<String> globalVars) {
		Set<FunctionDefinitionNode> result = new HashSet<>();
		Set<Function> seen = new HashSet<>();
		LinkedList<Function> queue = new LinkedList<>();
		LinkedList<ASTNode> tempQueue = new LinkedList<>();

		tempQueue.add(node);
		while (!tempQueue.isEmpty()) {
			ASTNode n = tempQueue.removeFirst();

			if (n instanceof FunctionCallNode)
				queue.addLast(getFunction((FunctionCallNode) n));
			for (ASTNode child : n.children())
				if (child != null)
					tempQueue.addLast(child);
		}
		while (!queue.isEmpty()) {
			Function curFunction = queue.removeFirst();
			FunctionDefinitionNode functionDefinitionNode = curFunction
					.getDefinition();
			Set<Function> callees = curFunction.getCallees();

			seen.add(curFunction);
			if (useSharedVariableOrOmp(functionDefinitionNode,
					globalVars) >= 2) {

				functionDefinitionNode.remove();
				result.add(functionDefinitionNode);
			} else if (useSharedVariableOrOmp(functionDefinitionNode,
					globalVars) == 1) {
				result.add(functionDefinitionNode.copy());
			}
			for (Function f : callees) {
				if (!seen.contains(f))
					queue.add(f);
			}
		}
		return result;
	}

	/**
	 * Check if a {@link FunctionDefinitionNode} contains shared variable, any
	 * omp functions or any omp pragmas.
	 * 
	 * @param functionDefNode
	 *            The {@link FunctionDefinitionNode} to be checked.
	 * @param globalVars
	 *            The set of variables declared in the global scope.
	 * @return
	 *         <ul>
	 *         <li>0 if contain neither.
	 *         <li>1 if contain only shared variable.
	 *         <li>2 if contain omp usage.
	 *         <li>3 if contain both.
	 *         </ul>
	 */
	private int useSharedVariableOrOmp(FunctionDefinitionNode functionDefNode,
			Set<String> globalVars) {
		if (functionDefNode == null)
			return 0;
		int result = 0;
		boolean useShared = false;
		boolean useOmp = false;
		LinkedList<ASTNode> queue = new LinkedList<>();
		Set<String> localVars = retrieveVars(
				functionDefNode.getBody().iterator());

		queue.add(functionDefNode);
		while (!queue.isEmpty() && !useOmp) {
			ASTNode node = queue.removeFirst();

			if (!useShared && (node instanceof IdentifierNode)) {
				IdentifierNode idNode = (IdentifierNode) node;

				if (globalVars.contains(idNode.name())
						&& !localVars.contains(idNode.name()))
					useShared = true;
			}
			if (node instanceof OmpSyncNode
					|| node instanceof OmpWorksharingNode)
				useOmp = true;
			if (!useOmp && (node instanceof FunctionCallNode)) {
				String identifier = ((IdentifierExpressionNode) ((FunctionCallNode) node)
						.getFunction()).getIdentifier().name();

				if (identifier.startsWith("omp_"))
					useOmp = true;
			}
			for (ASTNode child : node.children())
				if (child != null)
					queue.addLast(child);
		}
		if (useOmp)
			result += 2;
		if (useShared)
			result += 1;
		return result;
	}

	/**
	 * Get the {@link Function} entity of a {@link FunctionCallNode}.
	 * 
	 * @param functionCallNode
	 *            The target {@link FunctionCallNode}
	 * @return the {@link Function} entity of the target
	 *         {@link FunctionCallNode}.
	 */
	private Function getFunction(FunctionCallNode functionCallNode) {
		ExpressionNode identiferExpression = functionCallNode.getFunction();

		if (!(identiferExpression instanceof IdentifierExpressionNode)) {
			String msg = "The transformation of function pointers in OpenMP orphan constructs.";

			throw new CIVLUnimplementedFeatureException(
					"The following feature is not yet implemented: " + msg);
		}

		IdentifierExpressionNode identifierExpressionNode = (IdentifierExpressionNode) identiferExpression;
		IdentifierNode identifierNode = identifierExpressionNode
				.getIdentifier();
		Entity entity = identifierNode.getEntity();

		return (Function) entity;
	}

	/**
	 * Retrieve the set of variables declared in an {@link Iterable}.
	 * 
	 * @param itemsIter
	 *            The iterator to iterate an {@link Iterable}.
	 * @return The set of identifiers declared inside an {@link Iterable}.
	 */
	private Set<String> retrieveVars(Iterator<BlockItemNode> itemsIter) {
		Set<String> vars = new HashSet<>();

		while (itemsIter.hasNext()) {
			BlockItemNode item = (BlockItemNode) itemsIter.next();

			if (item instanceof VariableDeclarationNode) {
				vars.add(((VariableDeclarationNode) item).getIdentifier()
						.name());
			}
		}
		return vars;
	}
}
