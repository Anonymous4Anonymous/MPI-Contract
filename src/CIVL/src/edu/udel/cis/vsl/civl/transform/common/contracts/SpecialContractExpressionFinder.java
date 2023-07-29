package edu.udel.cis.vsl.civl.transform.common.contracts;

/**
 * <p>
 * This class scans an expression in a contract and returns an instance of
 * {@link SpecialConstructs}, which is a collection of all references to
 * special constructs in the given expression. A special construct is a contract
 * construct that needs special handling during code transformation.
 * </p>
 * <p>
 * This class only contains static methods hence no runtime instance of this
 * class is needed.
 * </p>
 * 
 * @author xxxx
 *
 */
public class SpecialContractExpressionFinder {

//	/**
//	 * This class is a collection of all references to special constructs in
//	 * some contract expressions
//	 * 
//	 * @author xxxx
//	 */
//	static class SpecialConstructs {
//
//		List<ExpressionNode> remoteExpressions;
//
//		List<ExpressionNode> acslOldExpressions;
//
//		List<ExpressionNode> mpiDatatypes;
//
//		List<ExpressionNode> mpiSigs;
//
//		List<ExpressionNode> acslValidExpressions;
//
//		List<GeneralSetComprehensionTriple> mpiValidExpressions;
//
//		List<ExpressionNode> acslResults;
//
//		SpecialConstructs() {
//			remoteExpressions = new LinkedList<>();
//			acslOldExpressions = new LinkedList<>();
//			mpiDatatypes = new LinkedList<>();
//			mpiSigs = new LinkedList<>();
//			acslValidExpressions = new LinkedList<>();
//			mpiValidExpressions = new LinkedList<>();
//			acslResults = new LinkedList<>();
//		}
//	}
//
//	/**
//	 * finds and returns all references to special constructs in the given
//	 * expression.
//	 * 
//	 * @param expression
//	 * @return a {@link SpecialConstructs} which is a collection of all
//	 *         references to special constructs in the given expression.
//	 */
//	static SpecialConstructs findSpecialExpressions(
//			ExpressionNode expression) {
//		SpecialConstructs specials = new SpecialConstructs();
//
//		return findSpecialExpressions(expression, specials);
//	}
//
//	static SpecialConstructs findSpecialExpressions(ExpressionNode expression,
//			SpecialConstructs specials) {
//		specials.remoteExpressions.addAll(findRemoteExpressions(expression));
//		specials.acslOldExpressions.addAll(findOldExpressions(expression));
//		specials.mpiDatatypes
//				.addAll(findMPIDatatypesInMPIExpressions(expression));
//		specials.mpiSigs.addAll(findMPISigs(expression));
//
//		List<GeneralSetComprehensionTriple> validExprs = findAcslValidContext(
//				expression, new Stack<>());
//
//		processFoundValidExpressions(validExprs, specials.acslValidExpressions,
//				specials.mpiValidExpressions);
//		specials.acslResults.addAll(findAcslResult(expression));
//		return specials;
//	}
//	
//	private static List<ExpressionNode> findRemoteExpressions(
//			ExpressionNode expr) {
//		List<ExpressionNode> results = new LinkedList<>();
//
//		if (expr.expressionKind() == MPI_CONTRACT_EXPRESSION) {
//			MPIContractExpressionNode mpiExprNode = (MPIContractExpressionNode) expr;
//
//			if (mpiExprNode.MPIContractExpressionKind() == MPI_ON)
//				results.add(expr);
//		}
//
//		int numChildren = expr.numChildren();
//
//		for (int i = 0; i < numChildren;) {
//			ASTNode child = expr.child(i++);
//
//			if (child != null && child.nodeKind() == NodeKind.EXPRESSION)
//				results.addAll(findRemoteExpressions((ExpressionNode) child));
//		}
//		return results;
//	}
//
//	private static List<ExpressionNode> findOldExpressions(
//			ExpressionNode expr) {
//		List<ExpressionNode> results = new LinkedList<>();
//
//		if (expr.expressionKind() == ExpressionKind.OPERATOR)
//			if (((OperatorNode) expr).getOperator() == Operator.OLD) {
//				results.add(expr);
//				// nested old ? I think it should not happen
//				return results;
//			}
//
//		int numChildren = expr.numChildren();
//
//		for (int i = 0; i < numChildren;) {
//			ASTNode child = expr.child(i++);
//
//			if (child != null && child.nodeKind() == NodeKind.EXPRESSION)
//				results.addAll(findOldExpressions((ExpressionNode) child));
//		}
//		return results;
//	}
//
//	private static List<ExpressionNode> findMPIDatatypesInMPIExpressions(
//			ExpressionNode expr) {
//		List<ExpressionNode> results = new LinkedList<>();
//
//		if (expr.expressionKind() == MPI_CONTRACT_EXPRESSION) {
//			MPIContractExpressionNode mpiexpr = (MPIContractExpressionNode) expr;
//			MPIContractExpressionKind mpiExprKind = mpiexpr
//					.MPIContractExpressionKind();
//
//			if (mpiExprKind == MPI_BUF)
//				results.add(mpiexpr.getArgument(2));
//			else if (mpiExprKind == MPI_SIG)
//				results.add(mpiexpr.getArgument(0));
//			// skip MPI_NONOVERLAPPING
//		}
//
//		int numChildren = expr.numChildren();
//
//		for (int i = 0; i < numChildren;) {
//			ASTNode child = expr.child(i++);
//
//			if (child != null && child.nodeKind() == NodeKind.EXPRESSION)
//				results.addAll(findMPIDatatypesInMPIExpressions(
//						(ExpressionNode) child));
//		}
//		return results;
//	}
//
//	private static List<ExpressionNode> findMPISigs(ExpressionNode expr) {
//		List<ExpressionNode> results = new LinkedList<>();
//
//		if (expr.expressionKind() == MPI_CONTRACT_EXPRESSION) {
//			MPIContractExpressionNode mpiexpr = (MPIContractExpressionNode) expr;
//			MPIContractExpressionKind mpiExprKind = mpiexpr
//					.MPIContractExpressionKind();
//
//			if (mpiExprKind == MPI_SIG) {
//				results.add(mpiexpr);
//				return results;
//			}
//		}
//
//		int numChildren = expr.numChildren();
//
//		for (int i = 0; i < numChildren;) {
//			ASTNode child = expr.child(i++);
//
//			if (child != null && child.nodeKind() == NodeKind.EXPRESSION)
//				results.addAll(findMPISigs((ExpressionNode) child));
//		}
//		return results;
//	}
//
//	/**
//	 * Worker method of {@link #findAcslValidContext}. If the given expression
//	 * expr is \\valid expression, creates GeneralSetComprehensionTriple with
//	 * the current contexts and returns. Otherwise, recursively explore.
//	 */
//	private static List<GeneralSetComprehensionTriple> findAcslValid(
//			ExpressionNode expr,
//			Stack<List<VariableDeclarationNode>> contexts) {
//		List<GeneralSetComprehensionTriple> results = new LinkedList<>();
//
//		if (expr.expressionKind() == ExpressionKind.OPERATOR) {
//			OperatorNode opNode = (OperatorNode) expr;
//
//			if (opNode.getOperator() == Operator.VALID) {
//				GeneralSetComprehensionTriple contextualValidArg;
//				List<VariableDeclarationNode> binders = new LinkedList<>();
//
//				for (List<VariableDeclarationNode> ctx : contexts)
//					binders.addAll(ctx);
//
//				contextualValidArg = new GeneralSetComprehensionTriple(opNode,
//						null, binders.isEmpty() ? null : binders,
//						opNode.getSource());
//				results.add(contextualValidArg);
//				return results;
//			}
//		}
//
//		int numChildren = expr.numChildren();
//
//		for (int i = 0; i < numChildren;) {
//			ASTNode child = expr.child(i++);
//
//			if (child != null && child.nodeKind() == NodeKind.EXPRESSION)
//				results.addAll(
//						findAcslValidContext((ExpressionNode) child, contexts));
//		}
//		return results;
//	}
//	
//	/**
//	 * Finds all \\valid expressions while keeping track of contexts (, i.e.,
//	 * binders). Returns found \\valid expressions in form of
//	 * {@link GeneralSetComprehensionTriple} where
//	 * {@link GeneralSetComprehensionTriple#binders} holds context and
//	 * {@link GeneralSetComprehensionTriple#term} holds the \\valid expression.
//	 * Rest of the non-source fields in GeneralSetComprehensionTriple are
//	 * insignificant.
//	 * 
//	 * @param expr
//	 * @param contexts
//	 * @param nf
//	 * @return
//	 */
//	private static List<GeneralSetComprehensionTriple> findAcslValidContext(
//			ExpressionNode expr, Stack<List<VariableDeclarationNode>> contexts) {
//		List<GeneralSetComprehensionTriple> result;
//
//		if (expr.expressionKind() == ExpressionKind.QUANTIFIED_EXPRESSION) {
//			QuantifiedExpressionNode quantifiedExpr = (QuantifiedExpressionNode) expr;
//			List<VariableDeclarationNode> binders = new LinkedList<>();
//
//			for (PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode> pair : quantifiedExpr
//					.boundVariableList())
//				for (VariableDeclarationNode binder : pair.getLeft())
//					binders.add(binder);
//			contexts.push(binders);
//			expr = quantifiedExpr.expression();
//			result = findAcslValidContext(expr, contexts);
//			contexts.pop();
//			// TODO: more contextual expression can be added, e.g., lambda, set
//			// comprehension ...
//		} else
//			result = findAcslValid(expr, contexts);
//		return result;
//	}
//	
//	private static void processFoundValidExpressions(
//			List<GeneralSetComprehensionTriple> foundOnes,
//			List<ExpressionNode> acslValids,
//			List<GeneralSetComprehensionTriple> mpiBufTypeValid) {
//		for (GeneralSetComprehensionTriple triple : foundOnes) {
//			ExpressionNode arg = (ExpressionNode) triple.term.child(0);
//			
//			if (triple.binders != null)
//				throw new CIVLUnimplementedFeatureException(
//						"\\valid expression "
//								+ triple.term.prettyRepresentation()
//								+ " in quantified expression.");
//			if (arg.getType().kind() != TypeKind.ACSL_MPI_TYPE) {
//				acslValids.add(triple.term);
//			} else
//				mpiBufTypeValid.add(triple);
//		}
//	}
//
//	private static List<ExpressionNode> findAcslResult(ExpressionNode expr) {
//		List<ExpressionNode> results = new LinkedList<>();
//
//		if (expr.expressionKind() == ExpressionKind.RESULT) {
//			results.add(expr);
//			return results;
//		}
//
//		int numChildren = expr.numChildren();
//
//		for (int i = 0; i < numChildren;) {
//			ASTNode child = expr.child(i++);
//
//			if (child != null && child.nodeKind() == NodeKind.EXPRESSION)
//				results.addAll(findAcslResult((ExpressionNode) child));
//		}
//		return results;
//	}
}
