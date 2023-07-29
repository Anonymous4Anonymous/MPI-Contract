package edu.udel.cis.vsl.civl.transform.common.contracts;

public class MPIContractLimitations {
//
//	/**
//	 * The \mpi_but_t type expr is limited to be one of the following forms:
//	 * <li>1. <code>\mpi_buf(p, c, dt), or</code></li>
//	 * <li>2. <code>\mpi_buf(p, c, dt) + n</code></li>
//	 * 
//	 * For the first form, this method does nothing. For the second form, this
//	 * method returns <code>\mpi_buf(p, (n+1) * c, dt)</code>
//	 * 
//	 * 
//	 * @param expr
//	 * @return
//	 */
//	static MPIContractExpressionNode complyWithMPIBufTypeExpr(ExpressionNode expr,
//			NodeFactory nf) {
//		if (expr.expressionKind() == ExpressionKind.MPI_CONTRACT_EXPRESSION) {
//			MPIContractExpressionNode mpiNode = (MPIContractExpressionNode) expr;
//
//			if (mpiNode
//					.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_BUF)
//				return mpiNode;
//		}
//		if (expr.expressionKind() == ExpressionKind.OPERATOR) {
//			OperatorNode opNode = (OperatorNode) expr;
//
//			if (opNode.getOperator() == Operator.PLUS) {
//				ExpressionNode buf = opNode.getArgument(0);
//				ExpressionNode oft = opNode.getArgument(1);
//
//				if (!oft.getType().isScalar()) {
//					buf = oft;
//					oft = opNode.getArgument(0);
//				}
//				if (isMPIBuf(buf)) {
//					MPIContractExpressionNode mpiBuf = (MPIContractExpressionNode) buf.copy();
//					ExpressionNode count = mpiBuf.getArgument(1);
//					// newCount = count * (oft + 1)
//					ExpressionNode newCount = nf.newOperatorNode(
//							oft.getSource(), Operator.TIMES, count.copy(),
//							nf.newOperatorNode(oft.getSource(), Operator.PLUS,
//									oft.copy(),
//									nf.newIntConstantNode(oft.getSource(), 1)));
//					int childIdx = count.childIndex();
//
//					count.remove();
//					mpiBuf.setChild(childIdx, newCount);
//					return mpiBuf;
//				}
//			}
//		}
//		throw new CIVLUnimplementedFeatureException(
//				"\\mpi_but_t type expression " + expr.prettyRepresentation()
//						+ " is beyond the supported two forms: \n"
//						+ "1. \\mpi_buf(ptr, count, datatype), or\n"
//						+ "2. \\mpi_buf(ptr, count, datatype) + i");
//	}
//
//	/**
//	 * Currently, a waitsfor argument cannot be ACSL set term. It can only be
//	 * one of the followings:
//	 * <li>1. C expression of integer type</li>
//	 * <li>2. explicit set containing elements of kind 1, 2, 3</li>
//	 * <li>3. set comprehension {term | binders; pred} where term is of kind
//	 * 1.</li>
//	 * 
//	 * @param waitsforArg
//	 * @return
//	 */
//	static List<GeneralSetComprehensionTriple> complyWithWaitsforLimit(
//			ExpressionNode waitsforArg) {
//		List<GeneralSetComprehensionTriple> result = new LinkedList<>();
//		Source source = waitsforArg.getSource();
//
//		if (waitsforArg.getType().isScalar())
//			result.add(new GeneralSetComprehensionTriple(waitsforArg, null,
//					null, source));
//		else if (waitsforArg.expressionKind() == CURLY_BRACED_TERM_SET) {
//			CurlyBracedTermSetNode termSet = (CurlyBracedTermSetNode) waitsforArg;
//
//			if (termSet.isExplicit())
//				for (ExpressionNode arg : termSet.getExplicitElements())
//					result.addAll(complyWithWaitsforLimit(arg));
//			else {
//				// set comprehension:
//				if (!termSet.getSetComprehensionTerms().getType().isScalar())
//					throw new CIVLUnimplementedFeatureException(
//							"The term of a set comprehension in a waitsfor clause has set-type. "
//									+ termSet.getSetComprehensionTerms()
//											.prettyRepresentation());
//				
//				List<VariableDeclarationNode> binders = new LinkedList<>();
//
//				for (VariableDeclarationNode binder : termSet.getBinders())
//					binders.add(binder);
//				result.add(new GeneralSetComprehensionTriple(
//						termSet.getSetComprehensionTerms(),
//						termSet.getPredicate(), binders, source));
//			}
//		} else if (waitsforArg.expressionKind() == ExpressionKind.NOTHING) {
//			result.add(new GeneralSetComprehensionTriple(waitsforArg, null,
//					null, waitsforArg.getSource()));
//		} else 
//			throw new CIVLUnimplementedFeatureException("The waitsfor argument "
//					+ waitsforArg.prettyRepresentation()
//					+ " that is not scalar or set of scalars.");
//		return result;
//	}
//
//	/**
//	 * Currently, an assign argument can only be one of the followings:
//	 * <li>1. a pure ACSL lvalue tset</li>
//	 * <li>2. *\mpi_buf(...)</li>
//	 * <li>3, {*\mpi_buf(..) | int x; pred}, where pred and \mpi_buf(...)
//	 * involve no x</li>
//	 * <li>4. {*\mpi_buf(..) | int x; lo <= x < hi}</li>
//	 * 
//	 * @param assignsArg
//	 * @return a tuple (term, restrict, binders)
//	 */
//	static List<GeneralSetComprehensionTriple> complyWithAssignsLimit(
//			ExpressionNode assignsArg) {
//		List<GeneralSetComprehensionTriple> result = new LinkedList<>();
//		boolean isSet = assignsArg.getType().kind() == TypeKind.SET;
//		Type eleType;
//
//		eleType = !isSet
//				? assignsArg.getType()
//				: ((SetType) assignsArg.getType()).elementType();
//		if (eleType.kind() == TypeKind.ACSL_MPI_TYPE) {
//			if (isSet) {
//				// case 3
//				if (assignsArg.expressionKind() == CURLY_BRACED_TERM_SET) {
//					CurlyBracedTermSetNode tset = (CurlyBracedTermSetNode) assignsArg;
//					if (tset.isExplicit()) {
//						for (ExpressionNode tsetEle : tset
//								.getExplicitElements())
//							result.addAll(complyWithAssignsLimit(tsetEle));
//						return result;
//					} else {
//						ExpressionNode term = tset.getSetComprehensionTerms();
//
//						if (isDereferenceMPIBufTypeExpr(term)) {
//							result.add(complyWithAssignsSetComprehensionLimit(
//									tset));
//							return result;
//						}
//					}
//				}
//			} else {
//				// case 2
//				if (isDereferenceMPIBufTypeExpr(assignsArg)) {
//					result.add(new GeneralSetComprehensionTriple(assignsArg,
//							null, null, assignsArg.getSource()));
//					return result;
//				}
//			}
//		} else {
//			// case 1
//			result.add(new GeneralSetComprehensionTriple(assignsArg, null, null,
//					assignsArg.getSource()));
//			return result;
//		}
//		throw new CIVLUnimplementedFeatureException(
//				"The argument of assigns clause "
//						+ assignsArg.prettyRepresentation()
//						+ " is not one of the following forms: \n"
//						+ "1. a pure ACSL lvalue tset, or\n"
//						+ "2. *\\mpi_buf(...), or\n"
//						+ "3. {*\\mpi_buf(..) | int x; pred}, where pred and \\mpi_buf(...) are independent of x, or\n"
//						+ "4. {*\\mpi_buf(..) | int x; lo <= x < hi}\n"
//						+ "5. an explicit set of elements of the above 4 forms.");
//	}
//
//	/**
//	 * @return non-null iff the given set comprehension is binder independent or
//	 *         is bounded strictly
//	 */
//	private static GeneralSetComprehensionTriple complyWithAssignsSetComprehensionLimit(
//			CurlyBracedTermSetNode setComph) {
//		ExpressionNode term = setComph.getSetComprehensionTerms();
//		SequenceNode<VariableDeclarationNode> binders = setComph.getBinders();
//		ExpressionNode restrict = setComph.getPredicate();
//		boolean isBinderIndependent = true;
//
//		// check if it is binder independent:
//		for (VariableDeclarationNode varDecl : binders) {
//			if (containsIdentifier(term, varDecl.getIdentifier())
//					|| containsIdentifier(term, varDecl.getIdentifier())) {
//				isBinderIndependent = false;
//				break;
//			}
//		}
//		if (isBinderIndependent)
//			return new GeneralSetComprehensionTriple(term, restrict, null,
//					setComph.getSource());
//		// check if it is single binder bounded by lower and higher bounds:
//		if (binders.numChildren() != 1)
//			return null;
//		if (!(restrict instanceof OperatorNode
//				&& ((OperatorNode) restrict).getOperator() == Operator.LAND))
//			return null;
//
//		List<VariableDeclarationNode> binderList = new LinkedList<>();
//
//		for (VariableDeclarationNode binder : binders)
//			binderList.add(binder);
//		if (isLowerBound(binders.getSequenceChild(0),
//				(ExpressionNode) restrict.child(0))
//				&& isHigherBound(binders.getSequenceChild(0),
//						(ExpressionNode) restrict.child(1)))
//			return new GeneralSetComprehensionTriple(term, restrict, binderList,
//					setComph.getSource());
//		else if (isLowerBound(binders.getSequenceChild(0),
//				(ExpressionNode) restrict.child(1))
//				&& isHigherBound(binders.getSequenceChild(0),
//						(ExpressionNode) restrict.child(0)))
//			return new GeneralSetComprehensionTriple(term, restrict, binderList,
//					setComph.getSource());
//		return null;
//	}
//
//	/* ******************* common methods *************************** */
//
//	static boolean containsIdentifier(ExpressionNode expr, IdentifierNode id) {
//		if (expr.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
//			IdentifierExpressionNode exprId = ((IdentifierExpressionNode) expr);
//
//			if (exprId.getIdentifier().name().equals(id.name()))
//				return true;
//		}
//
//		boolean result = false;
//
//		for (ASTNode child : expr.children()) {
//			if (child.nodeKind() == NodeKind.EXPRESSION)
//				result |= containsIdentifier((ExpressionNode) child, id);
//			if (result)
//				return result;
//		}
//		return false;
//	}
//
//	static boolean isDereferenceMPIBufTypeExpr(ExpressionNode node) {
//		if (node.expressionKind() != ExpressionKind.OPERATOR)
//			return false;
//
//		OperatorNode opNode = (OperatorNode) node;
//
//		if (opNode.getOperator() != Operator.DEREFERENCE)
//			return false;
//
//		if (opNode.getArgument(0).getType().kind() != TypeKind.ACSL_MPI_TYPE)
//			return false;
//
//		AcslMPIType argType = (AcslMPIType) opNode.getArgument(0).getType();
//
//		return argType.acslTypeKind() == AcslMPITypeKind.ACSL_MPI_BUF_TYPE;
//	}
//
//	static boolean isMPIBuf(ExpressionNode node) {
//		if (node.expressionKind() != ExpressionKind.MPI_CONTRACT_EXPRESSION)
//			return false;
//
//		MPIContractExpressionNode mpiNode = (MPIContractExpressionNode) node;
//
//		return mpiNode
//				.MPIContractExpressionKind() == MPIContractExpressionKind.MPI_BUF;
//	}
//
//	static boolean isLowerBound(VariableDeclarationNode var,
//			ExpressionNode lowerBound) {
//		if (lowerBound.expressionKind() != ExpressionKind.OPERATOR)
//			return false;
//
//		OperatorNode opNode = (OperatorNode) lowerBound;
//
//		if (opNode.getOperator() != Operator.LTE)
//			return false;
//		if (opNode.getArgument(1)
//				.expressionKind() != ExpressionKind.IDENTIFIER_EXPRESSION)
//			return false;
//
//		IdentifierExpressionNode idExpr = (IdentifierExpressionNode) opNode
//				.getArgument(1);
//
//		return idExpr.getIdentifier().name().equals(var.getIdentifier().name());
//	}
//
//	static boolean isHigherBound(VariableDeclarationNode var,
//			ExpressionNode higherBound) {
//		if (higherBound.expressionKind() != ExpressionKind.OPERATOR)
//			return false;
//
//		OperatorNode opNode = (OperatorNode) higherBound;
//
//		if (opNode.getOperator() != Operator.LT)
//			return false;
//		if (opNode.getArgument(0)
//				.expressionKind() != ExpressionKind.IDENTIFIER_EXPRESSION)
//			return false;
//
//		IdentifierExpressionNode idExpr = (IdentifierExpressionNode) opNode
//				.getArgument(0);
//
//		return idExpr.getIdentifier().name().equals(var.getIdentifier().name());
//	}
}
