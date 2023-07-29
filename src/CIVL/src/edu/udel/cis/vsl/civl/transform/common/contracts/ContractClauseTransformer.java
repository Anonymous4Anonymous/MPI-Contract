package edu.udel.cis.vsl.civl.transform.common.contracts;

class ContractClauseTransformer {
//
//	public static final String ContractClauseTransformerName = "Contract-clause";
//
//	/**
//	 * A reference to an instance of {@link NodeFactory}
//	 */
//	private NodeFactory nodeFactory;
//
//	/**
//	 * A reference to an instance of {@link ASTFactory}
//	 */
//	private ASTFactory astFactory;
//
//	// /**
//	// * A reference to an instance of {@link MemoryLocationManager}
//	// */
//	// private MemoryLocationManager memoryLocationManager;
//
//	/**
//	 * <p>
//	 * This class consists of the final transformed ASTNodes which are divided
//	 * into two groups: ASTNodes before the "function" and ASTNodes after the
//	 * "function".
//	 * </p>
//	 * 
//	 * <p>
//	 * Here "function" refers to two kinds of code:
//	 * <ol>
//	 * <li>The target function call in the driver of the target function</li>
//	 * <li>The transformation of "assigns" clauses of the callee functions since
//	 * these are the parts "simulate" the side-effect modification of the
//	 * function.</li>
//	 * </ol>
//	 * </p>
//	 * 
//	 * @author xxxx
//	 */
//	class TransformedPair {
//		List<BlockItemNode> before;
//		List<BlockItemNode> after;
//
//		TransformedPair(List<BlockItemNode> before, List<BlockItemNode> after) {
//			this.before = before;
//			this.after = after;
//		}
//	}
//
//	ContractClauseTransformer(ASTFactory astFactory,
//			MemoryLocationManager memoryLocationManager) {
//		this.astFactory = astFactory;
//		this.nodeFactory = astFactory.getNodeFactory();
//		// this.memoryLocationManager = memoryLocationManager;
//	}
//
//	/**
//	 * <p>
//	 * This methods analyzes the given {@link FunctionContractBlock}, creates
//	 * {@link ClauseTransformGuide}s for requirements and ensurances. A
//	 * {@link ClauseTransformGuide} is associated with one ACSL clauses such as
//	 * a <code>requires</code> or a <code>ensures</code> clause.
//	 * </p>
//	 * 
//	 * <p>
//	 * The analysis process does not modify the ASTree hence this is suppose to
//	 * happen before the release of the tree. During the analysis, new nodes
//	 * will be generated in {@link ClauseTransformGuide}s which encodes the
//	 * information of how to transform (modify) the program with those generated
//	 * nodes.
//	 * </p>
//	 * 
//	 * @param block
//	 *            The {@link FunctionContractBlock} which is either the
//	 *            sequential contract block or one of the MPI collective blocks
//	 * @param isCallee
//	 *            true iff the given contracts belong to a callee function
//	 * @param isPurelyLocalFunction
//	 *            true iff the given contracts is sequential contract and it
//	 *            belongs to a function has no MPI collective contract
//	 * @param globalMpidatay2intermediateNames
//	 *            the gloabl map from MPI_Datatype expression string
//	 *            representations to unique intermediate variable names
//	 * @param requiresGuides
//	 *            output. {@link ClauseTransformGuide}s for
//	 *            <code>requires</code> clauses
//	 * @param ensuresGuides
//	 *            output. {@link ClauseTransformGuide}s for <code>ensures</code>
//	 *            clauses
//	 * @throws SyntaxException
//	 */
//	void analysisContractBlock(FunctionContractBlock block, boolean isCallee,
//			boolean isPurelyLocalFunction,
//			Map<String, String> globalMpidatay2intermediateNames,
//			List<ClauseTransformGuide> requiresGuides,
//			List<ClauseTransformGuide> ensuresGuides) throws SyntaxException {
//		int nameCounter = 0;
//		Set<String> blockLocalDeclaredIntermediateVars = new HashSet<>();
//
//		for (ConditionalClauses condClause : block.getConditionalClauses()) {
//			ContractClause requires = condClause.getRequires();
//			ClauseTransformGuide reqTransGuide = new ClauseTransformGuide(
//					requires, condClause.getConditions(),
//					condClause.getWaitsfors(), globalMpidatay2intermediateNames,
//					blockLocalDeclaredIntermediateVars, nameCounter);
//			boolean isLocal = block.isSequentialBlock();
//
//			if (isCallee)
//				ClauseTransformGuideGenerator.transformAssert(requires,
//						astFactory, isLocal, !isPurelyLocalFunction, null,
//						reqTransGuide);
//			else
//				ClauseTransformGuideGenerator.transformAssume(requires,
//						astFactory, isLocal, !isPurelyLocalFunction, null,
//						reqTransGuide);
//
//			nameCounter = reqTransGuide.nameCounter; // update name counter
//			requiresGuides.add(reqTransGuide);
//		}
//		for (ConditionalClauses condClause : block.getConditionalClauses()) {
//			ContractClause ensures = condClause.getEnsures();
//			ClauseTransformGuide ensTransGuide = new ClauseTransformGuide(
//					ensures, condClause.getConditions(),
//					condClause.getWaitsfors(), globalMpidatay2intermediateNames,
//					blockLocalDeclaredIntermediateVars, nameCounter);
//			boolean isLocal = block.isSequentialBlock();
//			ExpressionNode civlcPreState = identifierExpressionNode(
//					block.getContractBlockSource(),
//					MPIContractUtilities.PRE_STATE);
//
//			if (isCallee)
//				ClauseTransformGuideGenerator.transformAssume(ensures,
//						astFactory, isLocal, !isPurelyLocalFunction,
//						civlcPreState, ensTransGuide);
//			else
//				ClauseTransformGuideGenerator.transformAssert(ensures,
//						astFactory, isLocal, !isPurelyLocalFunction,
//						civlcPreState, ensTransGuide);
//			nameCounter = ensTransGuide.nameCounter; // update name counter
//			ensuresGuides.add(ensTransGuide);
//		}
//	}
//
//	/**
//	 * <p>
//	 * Transform sequential function contracts to a {@link TransformedPair}.
//	 * This process will actually modify the AST hence it must happen after the
//	 * release of the AST.
//	 * </p>
//	 * 
//	 * @param requiresGuides
//	 *            a list of {@link ClauseTransformGuide} for
//	 *            <code>requires</code> clauses
//	 * @param ensuresGuides
//	 *            a list of {@link ClauseTransformGuide} for
//	 *            <code>ensures</code> clauses
//	 * @param localBlock
//	 *            The {@link FunctionContractBlock} which contains (but not only
//	 *            contains) the <code>requires</code> and <code>ensures</code>
//	 *            clauses, which are associated with the given guides.
//	 * @param isPurelyLocalFunction
//	 *            true iff the function, to which the contract belongs, has no
//	 *            MPI collective contract
//	 * @param isCallee
//	 *            true iff the function, to which the contract belongs, is not
//	 *            the target function
//	 * @return a {@link TransformedPair} which is the result of the
//	 *         transformation of the given contract block
//	 * @throws SyntaxException
//	 */
//	TransformedPair transformLocalBlock(
//			List<ClauseTransformGuide> requiresGuides,
//			List<ClauseTransformGuide> ensuresGuides,
//			FunctionContractBlock localBlock, boolean isPurelyLocalFunction,
//			boolean isCallee) throws SyntaxException {
//		List<BlockItemNode> reqTranslations = new LinkedList<>();
//		List<BlockItemNode> ensTranslations = new LinkedList<>();
//		List<BlockItemNode> assignsTranslations = new LinkedList<>();
//
//		// transform requires:
//		for (ClauseTransformGuide reqGuide : requiresGuides)
//			reqTranslations.addAll(reqGuide.prefix);
//		for (ClauseTransformGuide reqGuide : requiresGuides) {
//			substitute(reqGuide);
//			reqTranslations.addAll(createConditionalAssumeOrAssert(!isCallee,
//					reqGuide.conditions,
//					reqGuide.clause.getClauseExpressions()));
//		}
//		for (ClauseTransformGuide reqGuide : requiresGuides)
//			reqTranslations.addAll(reqGuide.suffix);
//
//		// transform ensures:
//		for (ClauseTransformGuide ensGuide : ensuresGuides)
//			ensTranslations.addAll(ensGuide.prefix);
//		for (ClauseTransformGuide ensGuide : ensuresGuides) {
//			substitute(ensGuide);
//			ensTranslations.addAll(createConditionalAssumeOrAssert(isCallee,
//					ensGuide.conditions,
//					ensGuide.clause.getClauseExpressions()));
//		}
//		for (ClauseTransformGuide ensGuide : ensuresGuides)
//			ensTranslations.addAll(ensGuide.suffix);
//
//		if (isCallee) {
//			// Transformation of "assigns" ...
//			for (ConditionalClauses condClause : localBlock
//					.getConditionalClauses())
//				assignsTranslations
//						.addAll(transformAssignsClause(!isCallee, condClause));
//		}
//		// TODO: check assigns for target (!isCallee)
//		// create pre-state:
//		if (isPurelyLocalFunction)
//			reqTranslations.addAll(createState4PureLocalFunction(
//					localBlock.getContractBlockSource()));
//		reqTranslations.addAll(assignsTranslations);
//		return new TransformedPair(reqTranslations, ensTranslations);
//	}
//
//	/**
//	 * <p>
//	 * Transform a collective contract block to a {@link TransformedPair}. This
//	 * process will actually modify the AST hence it must happen after the
//	 * release of the AST.
//	 * </p>
//	 * 
//	 * @param requiresGuides
//	 *            a list of {@link ClauseTransformGuide} for
//	 *            <code>requires</code> clauses
//	 * @param ensuresGuides
//	 *            a list of {@link ClauseTransformGuide} for
//	 *            <code>ensures</code> clauses
//	 * @param mpiBlock
//	 *            The {@link FunctionContractBlock} which contains (but not only
//	 *            contains) the <code>requires</code> and <code>ensures</code>
//	 *            clauses, which are associated with the given guides.
//	 * @param isTarget
//	 *            true iff the function, to which the contract belongs, is the
//	 *            target function
//	 * @return a {@link TransformedPair} which is the result of the
//	 *         transformation of the given contract block
//	 * @throws SyntaxException
//	 */
//	TransformedPair transformMPICollectiveBlock(
//			List<ClauseTransformGuide> requiresGuides,
//			List<ClauseTransformGuide> ensuresGuides,
//			FunctionContractBlock mpiBlock, boolean isTarget)
//			throws SyntaxException {
//		LinkedList<BlockItemNode> reqTranslation = new LinkedList<>();
//		LinkedList<BlockItemNode> ensTranslation = new LinkedList<>();
//		LinkedList<BlockItemNode> assignsTranslation = new LinkedList<>();
//		ExpressionNode mpiComm = mpiBlock.getMPIComm();
//		Source source = mpiComm.getSource();
//		// a $collate_state object for pre-state...
//		VariableDeclarationNode preCoStateDecl = createCollateStateInitializer(
//				MPIContractUtilities.COLLATE_PRE_STATE, mpiComm);
//		// a $state object which hold the value of the $state field in a collate
//		// state. This is a manual side-effect removing in order to by-pass the
//		// checking of side-effects in quantified expressions ...
//		IdentifierNode preState = nodeFactory.newIdentifierNode(source,
//				MPIContractUtilities.PRE_STATE);
//		TypeNode pointer2stateType = nodeFactory.newPointerTypeNode(source,
//				nodeFactory.newStateTypeNode(source));
//		VariableDeclarationNode preStateDecl = nodeFactory
//				.newVariableDeclarationNode(source, preState, pointer2stateType,
//						createCollateGetStateCall(
//								identifierExpressionNode(source,
//										MPIContractUtilities.COLLATE_PRE_STATE),
//								source));
//		// a $collate_state object for post-state ...
//		VariableDeclarationNode postCoStateDecl = createCollateStateInitializer(
//				MPIContractUtilities.COLLATE_POST_STATE, mpiComm);
//
//		reqTranslation.addAll(mpiConstantsInitialization(mpiComm));
//		ensTranslation.add(postCoStateDecl);
//
//		ExpressionNode collatePreState = identifierExpressionNode(source,
//				MPIContractUtilities.COLLATE_PRE_STATE);
//		ExpressionNode collatePostState = identifierExpressionNode(source,
//				MPIContractUtilities.COLLATE_POST_STATE);
//
//		// transform requires:
//		for (ClauseTransformGuide reqGuide : requiresGuides)
//			reqTranslation.addAll(reqGuide.prefix);
//		// prefix should be visible for pre-state:
//		reqTranslation.add(preCoStateDecl);
//		reqTranslation.add(preStateDecl);
//		for (ClauseTransformGuide reqGuide : requiresGuides) {
//			substitute(reqGuide);
//			if (isTarget)
//				reqTranslation.addAll(transformClause2Assumption(
//						conjunct(reqGuide.conditions),
//						conjunct(reqGuide.clause.getClauseExpressions()),
//						collatePreState, reqGuide.arrivends, isTarget));
//			else
//				reqTranslation.addAll(
//						transformClause2Checking(conjunct(reqGuide.conditions),
//								conjunct(
//										reqGuide.clause.getClauseExpressions()),
//								collatePreState, reqGuide.arrivends, isTarget));
//		}
//		for (ClauseTransformGuide reqTuple : requiresGuides)
//			reqTranslation.addAll(reqTuple.suffix);
//
//		// transform ensures:
//		for (ClauseTransformGuide ensGuide : ensuresGuides)
//			ensTranslation.addAll(ensGuide.prefix);
//		for (ClauseTransformGuide ensGuide : ensuresGuides) {
//			substitute(ensGuide);
//			if (isTarget)
//				ensTranslation.addAll(transformClause2Checking(
//						conjunct(ensGuide.conditions),
//						conjunct(ensGuide.clause.getClauseExpressions()),
//						collatePostState, ensGuide.arrivends, isTarget));
//			else
//				ensTranslation.addAll(transformClause2Assumption(
//						conjunct(ensGuide.conditions),
//						conjunct(ensGuide.clause.getClauseExpressions()),
//						collatePostState, ensGuide.arrivends, isTarget));
//		}
//		for (ClauseTransformGuide ensTuple : ensuresGuides)
//			ensTranslation.addAll(ensTuple.suffix);
//
//		if (!isTarget)
//			for (ConditionalClauses condClause : mpiBlock
//					.getConditionalClauses())
//				assignsTranslation
//						.addAll(transformAssignsClause(isTarget, condClause));
//		// TODO: check assigns for target
//
//		StatementNode unsnapshotPre = nodeFactory.newExpressionStatementNode(
//				createMPIUnsnapshotCall(mpiBlock.getMPIComm().copy(),
//						identifierExpressionNode(source,
//								MPIContractUtilities.COLLATE_PRE_STATE)));
//		StatementNode unsnapshotPost = nodeFactory.newExpressionStatementNode(
//				createMPIUnsnapshotCall(mpiBlock.getMPIComm().copy(),
//						identifierExpressionNode(source,
//								MPIContractUtilities.COLLATE_POST_STATE)));
//		ensTranslation.add(unsnapshotPre);
//		ensTranslation.add(unsnapshotPost);
//		reqTranslation.addAll(assignsTranslation);
//		if (isTarget)
//			ensTranslation.addLast(nodeFactory.newExpressionStatementNode(
//					createMPICommEmptyCall(mpiComm)));
//		return new TransformedPair(reqTranslation, ensTranslation);
//	}
//
//	private List<BlockItemNode> transformClause2Checking(
//			ExpressionNode condition, ExpressionNode predicate,
//			ExpressionNode collateState,
//			List<GeneralSetComprehensionTriple> arrivends, boolean isTarget)
//			throws SyntaxException {
//		if (predicate == null)
//			return Arrays.asList();
//
//		StatementNode assertion = createAssertion(predicate.copy());
//
//		assertion = withStatementWrapper(assertion, collateState, arrivends,
//				isTarget, false // false means is not assume
//		);
//		// conditional transformation:
//		if (condition != null)
//			assertion = nodeFactory.newIfNode(condition.getSource(),
//					condition.copy(), assertion);
//
//		List<BlockItemNode> results = new LinkedList<>();
//
//		// elaborate waited arguments:
////		if (arrivends != null && !isTarget)
////			for (ExpressionNode arrivend : arrivends) {
////				if (arrivend.expressionKind() == ExpressionKind.REGULAR_RANGE) {
////					RegularRangeNode range = (RegularRangeNode) arrivend;
////
////					results.add(createElaborateFor(range.getLow()));
////					results.add(createElaborateFor(range.getHigh()));
////				} else
////					results.add(createElaborateFor(arrivend));
////			}
//		results.add(assertion);
//		return results;
//	}
//
//	/**
//	 * Create assertions to check side conditions
//	 */
//	Collection<BlockItemNode> checkSideConditions(
//			List<ClauseTransformGuide> guides) {
//		List<ExpressionNode> sideConditions = new LinkedList<>();
//
//		for (ClauseTransformGuide guide : guides) {
//			ExpressionNode sideCondition = conjunct(guide.sideConditions);
//			ExpressionNode assumptions = conjunct(guide.conditions);
//
//			if (sideCondition == null)
//				continue;
//			if (assumptions != null)
//				sideCondition = nodeFactory.newOperatorNode(
//						sideCondition.getSource(), Operator.IMPLIES,
//						assumptions, sideCondition);
//			sideConditions.add(sideCondition);
//		}
//		if (!sideConditions.isEmpty())
//			return Arrays.asList(createAssertion(conjunct(sideConditions)));
//		else
//			return Arrays.asList();
//	}
//
//	/**
//	 * Transform a predicate specified by a contract clause into assumptions A.
//	 * Each a in A is a condition that will be assumed hold. The returned set of
//	 * {@link BlockItemNode} can be any kind of nodes serving such a assuming
//	 * purpose, they can be declarations of temporary variables, CIVL-C $assume
//	 * statements or assignments ( which is a direct way to assume some variable
//	 * has some value), etc.
//	 * 
//	 * @param condition
//	 *            The condition or assumption under where the predicate should
//	 *            hold.
//	 * @param predicate
//	 *            The predicate expression
//	 * @param evalKind
//	 *            The {@link CollectiveEvaluationKind} for this predicate.
//	 * @param collateState
//	 *            The reference to a collate state, it's significant only when
//	 *            the 'evalKind' is chosen
//	 *            {@link CollectiveEvaluationKind#ARRIVED_WITH} or
//	 *            {@link CollectiveEvaluationKind#COMPLETE_WITH}.
//	 * @param arriveds
//	 *            A set of places of processes, it's significant only when the
//	 *            'evalKind' is chosen
//	 *            {@link CollectiveEvaluationKind#ARRIVED_WITH}.
//	 * @return
//	 * @throws SyntaxException
//	 */
//	private List<BlockItemNode> transformClause2Assumption(
//			ExpressionNode condition, ExpressionNode predicate,
//			ExpressionNode collateState,
//			List<GeneralSetComprehensionTriple> arrivends, boolean isTarget)
//			throws SyntaxException {
//		if (predicate == null)
//			return Arrays.asList();
//
//		StatementNode assumes = createAssumption(predicate.copy());
//		List<BlockItemNode> results = new LinkedList<>();
//
//		assumes = withStatementWrapper(assumes, collateState, arrivends,
//				isTarget, true // true means is assume
//		);
//		// conditional transformation:
//		if (condition != null)
//			assumes = nodeFactory.newIfNode(condition.getSource(),
//					condition.copy(), assumes);
//		// elaborate waited process places:
////		if (arrivends != null && !isTarget)
////			for (ExpressionNode arrivend : arrivends) {
////				if (arrivend.expressionKind() == ExpressionKind.REGULAR_RANGE) {
////					RegularRangeNode range = (RegularRangeNode) arrivend;
////
////					results.add(createElaborateFor(range.getLow()));
////					results.add(createElaborateFor(range.getHigh()));
////				} else
////					results.add(createElaborateFor(arrivend));
////			}
//		results.add(assumes);
//		return results;
//	}
//
//	/**
//	 * <p>
//	 * Transforms "assigns" clauses under some behavior. The assigns clauses
//	 * will be checked hold if they belong to the main verifying function;
//	 * otherwise they will be transformed to a set of statements that havocs the
//	 * memory locations specified by the assigns clauses.
//	 * </p>
//	 * 
//	 * @param condClauses
//	 *            {@link ConditionalClauses} containing "assigns" clauses
//	 * @param isPurelyLocal
//	 *            true iff the transforming "assigns" clauses belongs to local
//	 *            contract block
//	 * @param isTarget
//	 *            true iff the function who owns the transforming "assigns"
//	 *            clauses is the main verifying function;
//	 * @return the transformed result
//	 * @throws SyntaxException
//	 */
//	private List<BlockItemNode> transformAssignsClause(boolean isTarget,
//			ConditionalClauses condClauses) throws SyntaxException {
//		if (condClauses.getAssignsArgs().isEmpty())
//			return Arrays.asList();
//
//		List<BlockItemNode> refreshments = new LinkedList<>();
//		Source source = null;
//
//		for (GeneralSetComprehensionTriple triple : condClauses
//				.getAssignsArgs()) {
//			ExpressionNode memoryLocationSet = triple.term;
//			ExpressionNode pred = triple.restrict;
//			List<BlockItemNode> refreshment = refreshMemoryLocationSetExpression(
//					memoryLocationSet);
//			// see MPIContractLimitations#complyWithAssignsSetComprehensionLimit
//			// for detail about what can be expected from the given triple:
//			VariableDeclarationNode binder = triple.binders != null
//					? triple.binders.get(0)
//					: null;
//			
//			
//			if (pred != null)
//				refreshments.add(MPIContractUtilities.makeGuardedByRestriction(
//						binder, pred, refreshment,
//						memoryLocationSet.getSource(), nodeFactory));
//			else
//				refreshments.addAll(refreshment);
//			if (source == null)
//				source = memoryLocationSet.getSource();
//		}
//
//		ExpressionNode condition = conjunct(condClauses.getConditions());
//
//		if (condition != null) {
//			StatementNode guardedStmt = MPIContractUtilities
//					.makeGuardedByRestriction(null, condition.copy(),
//							refreshments, source, nodeFactory);
//
//			refreshments.clear();
//			refreshments.add(guardedStmt);
//		}
//		return refreshments;
//	}
//
//	/*
//	 * ************************************************************************
//	 * Package artificial node creating helper methods:
//	 **************************************************************************/
//
//	/**
//	 * <p>
//	 * <b>Summary: </b> Creates an assertion function call with an argument
//	 * "predicate".
//	 * </p>
//	 * 
//	 * @param predicate
//	 *            The {@link ExpressionNode} which represents a predicate. It is
//	 *            the only argument of an assertion call.
//	 * @return A created assert call statement node;
//	 */
//	private StatementNode createAssertion(ExpressionNode predicate) {
//		ExpressionNode assertIdentifier = nodeFactory
//				.newIdentifierExpressionNode(predicate.getSource(),
//						nodeFactory.newIdentifierNode(predicate.getSource(),
//								BaseWorker.ASSERT));
//		FunctionCallNode assumeCall = nodeFactory.newFunctionCallNode(
//				predicate.getSource(), assertIdentifier,
//				Arrays.asList(predicate), null);
//		return nodeFactory.newExpressionStatementNode(assumeCall);
//	}
//
//	/**
//	 * <p>
//	 * <b>Summary: </b> Creates an assumption function call with an argument
//	 * "predicate".
//	 * </p>
//	 * 
//	 * @param predicate
//	 *            The {@link ExpressionNode} which represents a predicate. It is
//	 *            the only argument of an assumption call.
//	 * @return A created assumption call statement node;
//	 */
//	private StatementNode createAssumption(ExpressionNode predicate) {
//		ExpressionNode assumeIdentifier = identifierExpressionNode(
//				predicate.getSource(), BaseWorker.ASSUME);
//		FunctionCallNode assumeCall = nodeFactory.newFunctionCallNode(
//				predicate.getSource(), assumeIdentifier,
//				Arrays.asList(predicate), null);
//		return nodeFactory.newExpressionStatementNode(assumeCall);
//	}
//
//	/**
//	 * Create a <code>$collate_state</code> which only contains one process:
//	 * 
//	 * <code>
//	 *    $state _s_pre_obj = $get_state();
//	 *    $state * _s_pre = &_s_pre_obj;
//	 * </code>
//	 * 
//	 * @return
//	 * @throws SyntaxException
//	 */
//	private List<BlockItemNode> createState4PureLocalFunction(Source source)
//			throws SyntaxException {
//		TypeNode stateType = nodeFactory.newStateTypeNode(source);
//		TypeNode pointer2stateType = nodeFactory.newPointerTypeNode(source,
//				nodeFactory.newStateTypeNode(source));
//		String PRE_STATE_OBJ = "_s_pre_obj";
//		VariableDeclarationNode stateObjectVarDecl = nodeFactory
//				.newVariableDeclarationNode(source,
//						nodeFactory.newIdentifierNode(source, PRE_STATE_OBJ),
//						stateType, createGetStateCall(source));
//		ExpressionNode addressofStateObj = nodeFactory.newOperatorNode(source,
//				Operator.ADDRESSOF,
//				identifierExpressionNode(source, PRE_STATE_OBJ));
//		VariableDeclarationNode point2StateVarDecl = nodeFactory
//				.newVariableDeclarationNode(source,
//						nodeFactory.newIdentifierNode(source,
//								MPIContractUtilities.PRE_STATE),
//						pointer2stateType, addressofStateObj);
//		List<BlockItemNode> results = new LinkedList<>();
//
//		results.add(stateObjectVarDecl);
//		results.add(point2StateVarDecl);
//		return results;
//	}
//
//	/*
//	 * ************************************************************************
//	 * Private artificial node creating helper methods:
//	 **************************************************************************/
//
//	/**
//	 * @return a function call expression:
//	 *         <code>$collate_get_state(colStateRef) </code>
//	 */
//	private ExpressionNode createCollateGetStateCall(ExpressionNode colStateRef,
//			Source source) {
//		return nodeFactory.newFunctionCallNode(source,
//				identifierExpressionNode(source,
//						MPIContractUtilities.COLLATE_GET_STATE_CALL),
//				Arrays.asList(colStateRef.copy()), null);
//	}
//
//	/**
//	 * @return a function call expression: <code>$get_state()</code>
//	 */
//	private ExpressionNode createGetStateCall(Source source) {
//		return nodeFactory.newFunctionCallNode(source,
//				identifierExpressionNode(source,
//						MPIContractUtilities.REGULAR_GET_STATE_CALL),
//				Arrays.asList(), null);
//	}
//
//	private VariableDeclarationNode createCollateStateInitializer(
//			String collateStateName, ExpressionNode mpiComm) {
//		Source source = mpiComm.getSource();
//		InitializerNode initializer = createMPISnapshotCall(mpiComm.copy());
//		TypeNode collateStateTypeName = nodeFactory
//				.newTypedefNameNode(nodeFactory.newIdentifierNode(source,
//						MPIContractUtilities.COLLATE_STATE_TYPE), null);
//		IdentifierNode varIdent = nodeFactory.newIdentifierNode(source,
//				collateStateName);
//
//		return nodeFactory.newVariableDeclarationNode(source, varIdent,
//				collateStateTypeName, initializer);
//	}
//
//	/**
//	 * Create Sempty for loop statement for elaborating expressions:
//	 * <code> for (int i = 0; i < expression; i++); </code>
//	 * 
//	 * @param expression
//	 *            The expression will be elaborated
//	 * @return an empty for loop statement
//	 * @throws SyntaxException
//	 *             when unexpected exception happens during creating zero
//	 *             constant node.
//	 */
//	private StatementNode createElaborateFor(ExpressionNode expression)
//			throws SyntaxException {
//		TokenFactory tokenFactory = astFactory.getTokenFactory();
//		Formation formation = tokenFactory.newTransformFormation(
//				ContractClauseTransformerName,
//				"Elaborate " + expression.prettyRepresentation());
//		CivlcToken token = tokenFactory.newCivlcToken(CivlcTokenConstant.FOR,
//				"inserted text", formation, TokenVocabulary.DUMMY);
//		Source source = tokenFactory.newSource(token);
//		IdentifierExpressionNode forLoopVarExpr = nodeFactory
//				.newIdentifierExpressionNode(source,
//						nodeFactory.newIdentifierNode(source,
//								BaseWorker.ELABORATE_LOOP_VAR));
//		VariableDeclarationNode forLoopVarDecl = nodeFactory
//				.newVariableDeclarationNode(source,
//						forLoopVarExpr.getIdentifier().copy(),
//						nodeFactory.newBasicTypeNode(source, BasicTypeKind.INT),
//						nodeFactory.newIntegerConstantNode(source, "0"));
//		ForLoopInitializerNode forLoopInitializer = nodeFactory
//				.newForLoopInitializerNode(source,
//						Arrays.asList(forLoopVarDecl));
//		ExpressionNode forLoopCondition = nodeFactory.newOperatorNode(source,
//				Operator.LT, Arrays.asList(forLoopVarExpr, expression.copy()));
//		ExpressionNode forLoopIncrementor = nodeFactory.newOperatorNode(source,
//				Operator.POSTINCREMENT, Arrays.asList(forLoopVarExpr.copy()));
//
//		return nodeFactory.newForLoopNode(source, forLoopInitializer,
//				forLoopCondition, forLoopIncrementor,
//				nodeFactory.newNullStatementNode(source), null);
//	}
//
//	private StatementNode withStatementWrapper(StatementNode body,
//			ExpressionNode collateState,
//			List<GeneralSetComprehensionTriple> dependents, boolean isTarget,
//			boolean isAssume) throws SyntaxException {
//		StatementNode withStmt = nodeFactory.newWithNode(body.getSource(),
//				collateState.copy(), body.copy());
//		boolean run = !isAssume && !isTarget;
//		boolean complete = isTarget || !isAssume || dependents.isEmpty();
//
//		if (complete) {
//			ExpressionNode functionIdentifier = nodeFactory
//					.newIdentifierExpressionNode(body.getSource(),
//							nodeFactory.newIdentifierNode(body.getSource(),
//									MPIContractUtilities.COLLATE_COMPLETE));
//			ExpressionNode collateComplete = nodeFactory.newFunctionCallNode(
//					collateState.getSource(), functionIdentifier,
//					Arrays.asList(collateState.copy()), null);
//			StatementNode withCompleteStmt = nodeFactory.newWhenNode(
//					collateComplete.getSource(), collateComplete, withStmt);
//
//			if (run)
//				return nodeFactory.newRunNode(withCompleteStmt.getSource(),
//						withCompleteStmt);
//			else
//				return withCompleteStmt;
//		}
//		if (!isTarget && !complete) {
//			List<BlockItemNode> arriveds = new LinkedList<>();
//			Source allArrivedSource = null;
//
//			for (GeneralSetComprehensionTriple dependent : dependents) {
//				StatementNode arrived = waitsforArgToCollateArrivedPredicate(
//						dependent, collateState.copy());
//				arriveds.add(arrived);
//				allArrivedSource = allArrivedSource == null
//						? arrived.getSource()
//						: astFactory.getTokenFactory().join(allArrivedSource,
//								arrived.getSource());
//			}
//			allArrivedSource = allArrivedSource == null
//					? body.getSource()
//					: allArrivedSource;
//			arriveds.add(withStmt);
//
//			StatementNode allArrived = nodeFactory
//					.newCompoundStatementNode(allArrivedSource, arriveds);
//
//			if (run)
//				return nodeFactory.newRunNode(withStmt.getSource(), allArrived);
//			else
//				return allArrived;
//		}
//		return body;
//	}
//
//	private List<BlockItemNode> mpiConstantsInitialization(
//			ExpressionNode mpiComm) {
//		List<BlockItemNode> results = new LinkedList<>();
//		ExpressionNode mpiCommWorldExpr = nodeFactory
//				.newIdentifierExpressionNode(mpiComm.getSource(),
//						nodeFactory.newIdentifierNode(mpiComm.getSource(),
//								MPIContractUtilities.MPI_COMM_WORLD));
//		ExpressionNode initComm = nodeFactory.newOperatorNode(
//				mpiComm.getSource(), Operator.ASSIGN, mpiComm.copy(),
//				mpiCommWorldExpr);
//		ExpressionNode rank = nodeFactory.newIdentifierExpressionNode(
//				mpiComm.getSource(),
//				nodeFactory.newIdentifierNode(mpiComm.getSource(),
//						MPIContractUtilities.MPI_COMM_RANK_CONST));
//		ExpressionNode size = nodeFactory.newIdentifierExpressionNode(
//				mpiComm.getSource(),
//				nodeFactory.newIdentifierNode(mpiComm.getSource(),
//						MPIContractUtilities.MPI_COMM_SIZE_CONST));
//
//		results.add(nodeFactory.newExpressionStatementNode(initComm));
//		results.add(createMPICommRankCall(mpiComm.copy(), rank));
//		results.add(createMPICommSizeCall(mpiComm.copy(), size));
//		return results;
//	}
//
//	private StatementNode createMPICommRankCall(ExpressionNode mpiComm,
//			ExpressionNode rankVar) {
//		ExpressionNode callIdentifier = nodeFactory.newIdentifierExpressionNode(
//				rankVar.getSource(),
//				nodeFactory.newIdentifierNode(rankVar.getSource(),
//						MPIContractUtilities.MPI_COMM_RANK_CALL));
//		ExpressionNode addressOfRank = nodeFactory.newOperatorNode(
//				rankVar.getSource(), Operator.ADDRESSOF, rankVar.copy());
//		FunctionCallNode call = nodeFactory.newFunctionCallNode(
//				rankVar.getSource(), callIdentifier,
//				Arrays.asList(mpiComm.copy(), addressOfRank), null);
//		return nodeFactory.newExpressionStatementNode(call);
//	}
//
//	private StatementNode createMPICommSizeCall(ExpressionNode mpiComm,
//			ExpressionNode sizeVar) {
//		ExpressionNode callIdentifier = nodeFactory.newIdentifierExpressionNode(
//				sizeVar.getSource(),
//				nodeFactory.newIdentifierNode(sizeVar.getSource(),
//						MPIContractUtilities.MPI_COMM_SIZE_CALL));
//		ExpressionNode addressOfSize = nodeFactory.newOperatorNode(
//				sizeVar.getSource(), Operator.ADDRESSOF, sizeVar.copy());
//		FunctionCallNode call = nodeFactory.newFunctionCallNode(
//				sizeVar.getSource(), callIdentifier,
//				Arrays.asList(mpiComm.copy(), addressOfSize), null);
//		return nodeFactory.newExpressionStatementNode(call);
//	}
//
//	@SuppressWarnings("unused")
//	private ExpressionNode createMPIBarrier(ExpressionNode mpiComm) {
//		ExpressionNode functionIdentifierExpression = nodeFactory
//				.newIdentifierExpressionNode(mpiComm.getSource(),
//						nodeFactory.newIdentifierNode(mpiComm.getSource(),
//								MPIContractUtilities.MPI_BARRIER_CALL));
//
//		return nodeFactory.newFunctionCallNode(mpiComm.getSource(),
//				functionIdentifierExpression, Arrays.asList(mpiComm.copy()),
//				null);
//	}
//
//	private ExpressionNode createMPICommEmptyCall(ExpressionNode mpiComm) {
//		ExpressionNode functionIdentifierExpression = nodeFactory
//				.newIdentifierExpressionNode(mpiComm.getSource(),
//						nodeFactory.newIdentifierNode(mpiComm.getSource(),
//								MPIContractUtilities.MPI_CHECK_EMPTY_COMM));
//
//		return nodeFactory.newFunctionCallNode(mpiComm.getSource(),
//				functionIdentifierExpression,
//				Arrays.asList(mpiComm.copy()), null);
//	}
//
//	private ExpressionNode createMPISnapshotCall(ExpressionNode mpiComm) {
//		Source source = mpiComm.getSource();
//		ExpressionNode callIdentifier = nodeFactory.newIdentifierExpressionNode(
//				source, nodeFactory.newIdentifierNode(source,
//						MPIContractUtilities.MPI_SNAPSHOT));
//		ExpressionNode hereNode = nodeFactory.newHereNode(source);
//		FunctionCallNode call = nodeFactory.newFunctionCallNode(source,
//				callIdentifier, Arrays.asList(mpiComm.copy(), hereNode), null);
//
//		return call;
//	}
//
//	/**
//	 * <p>
//	 * <b>Summary: </b> Creates an $mpi_unsnapshot function call:<code>
//	 * $mpi_unsnapshot(mpiComm);</code>
//	 * </p>
//	 * 
//	 * @param mpiComm
//	 *            An {@link ExpressionNode} representing an MPI communicator.
//	 * @return The created $mpi_unsnapshot call statement node.
//	 */
//	private ExpressionNode createMPIUnsnapshotCall(ExpressionNode mpiComm,
//			ExpressionNode collateStateRef) {
//		Source source = mpiComm.getSource();
//		ExpressionNode callIdentifier = identifierExpressionNode(source,
//				MPIContractUtilities.MPI_UNSNAPSHOT);
//		FunctionCallNode call = nodeFactory.newFunctionCallNode(source,
//				callIdentifier, Arrays.asList(mpiComm, collateStateRef), null);
//
//		return call;
//	}
//
//	private IdentifierExpressionNode identifierExpressionNode(Source source,
//			String name) {
//		return nodeFactory.newIdentifierExpressionNode(source,
//				nodeFactory.newIdentifierNode(source, name));
//	}
//
//	/*
//	 * *************************************************************************
//	 * Methods process ASSIGNS clauses
//	 **************************************************************************/
//	/**
//	 * <p>
//	 * IF the expression is an MPI_BUF expression, transform it to
//	 * <code>$mpi_assigns</code> function, which is defined in "civl-mpi.cvh".
//	 * </p>
//	 * 
//	 * <p>
//	 * Otherwise, transform it to $mem_havoc function, which is defined in
//	 * "mem.cvh"
//	 * </p>
//	 * 
//	 * @param expression
//	 *            An expression represents a memory location set
//	 * @param isPurelyLocal
//	 *            if the given expression belongs to a local contract block
//	 * @return A {@link BlockItemNode} which consists of statements that will
//	 *         assign fresh new symbolic constants to the given expression
//	 * @throws SyntaxException
//	 *             When the given expression is not a valid memory location set
//	 *             expression.
//	 */
//	private List<BlockItemNode> refreshMemoryLocationSetExpression(
//			ExpressionNode expression) throws SyntaxException {
//		if (expression.getType().kind() == TypeKind.ACSL_MPI_TYPE) {
//			// must be of \mpi_seq_t type and the form "*(\mpi_buf(...) + n)":
//			assert expression.expressionKind() == ExpressionKind.OPERATOR;
//
//			OperatorNode derefBuf = (OperatorNode) expression;
//
//			return refreshMPIRegion(
//					MPIContractLimitations.complyWithMPIBufTypeExpr(
//							derefBuf.getArgument(0), nodeFactory));
//		} else
//			return refreshACSLMemoryLocationSetExpression(expression);
//	}
//
//	/**
//	 * <code>
//	 * $mem_havoc(m); 
//	 * </code>
//	 */
//	private List<BlockItemNode> refreshACSLMemoryLocationSetExpression(
//			ExpressionNode expression) {
//		Source source = expression.getSource();
//		ExpressionNode memHavocFuncIdent = identifierExpressionNode(source,
//				MPIContractUtilities.MEM_HAVOC);
//		ExpressionNode addrof = nodeFactory.newOperatorNode(source,
//				Operator.ADDRESSOF, expression.copy());
//		ExpressionNode tmp = nodeFactory.newFunctionCallNode(source,
//				memHavocFuncIdent, Arrays.asList(addrof), null);
//
//		List<BlockItemNode> results = new LinkedList<>();
//
//		results.add(nodeFactory.newExpressionStatementNode(tmp));
//		return results;
//	}
//
//	/**
//	 * <p>
//	 * $mpi_assigns(ptr, count, datatype);
//	 * </p>
//	 * 
//	 * @param expression
//	 *            a MPI_Region expression
//	 * @return
//	 */
//	private List<BlockItemNode> refreshMPIRegion(
//			MPIContractExpressionNode expression) {
//		Source source = expression.getSource();
//		ExpressionNode mpiAssignsFuncIdent = identifierExpressionNode(source,
//				MPIContractUtilities.MPI_ASSIGNS);
//		ExpressionNode ptr, count, datatype, tmp;
//
//		ptr = expression.getArgument(0);
//		count = expression.getArgument(1);
//		datatype = expression.getArgument(2);
//		tmp = nodeFactory.newFunctionCallNode(source, mpiAssignsFuncIdent,
//				Arrays.asList(ptr.copy(), count.copy(), datatype.copy()), null);
//
//		List<BlockItemNode> results = new LinkedList<>();
//
//		results.add(nodeFactory.newExpressionStatementNode(tmp));
//		return results;
//	}
//
//	/**
//	 * @param rangeOrInteger
//	 *            Either an integer type expression or a regular range
//	 *            expression.
//	 * @return A regular range whose low and high are both equal to the given
//	 *         expression 'rangeOrInteger' iff 'rangeOrInteger' is not a regular
//	 *         range expression. Otherwise return 'rangeOrInteger' directly.
//	 */
//	private ExpressionNode makeItRange(ExpressionNode rangeOrInteger) {
//		if (rangeOrInteger.getType().kind() == TypeKind.RANGE)
//			return rangeOrInteger.copy();
//		assert rangeOrInteger.getType().kind() == TypeKind.BASIC;
//		return nodeFactory.newRegularRangeNode(rangeOrInteger.getSource(),
//				rangeOrInteger.copy(), rangeOrInteger.copy());
//	}
//
//	/**
//	 * Substitutes sub-expressions in clause expressions with transformed
//	 * expressions.
//	 * 
//	 * @param guide
//	 *            a instance of {@link ClauseTransformGuide} which encodes the
//	 *            substitution map and clause expressions that will be
//	 *            substituted.
//	 */
//	private void substitute(ClauseTransformGuide guide) {
//		Map<ASTNode, ASTNode> substituted = new HashMap<>();
//
//		for (ExpressionNode clause : guide.clause.getClauseExpressions())
//			if (clause != null)
//				visitAndSubstitute(clause, guide.substitutions, substituted);
//	}
//
//	/**
//	 * Bottom-up substitution for the given expression with the map
//	 * 
//	 * @param expression
//	 *            the expression that will be substituted
//	 * @param subMap
//	 *            a substitution map from the old node references in the
//	 *            substituting expression to the {@link ASTNodeSubstituteGuide}s
//	 * @param subHistory
//	 *            the history of substituted sub-expressions, which is needed to
//	 *            solve the problem that both a parent <code>A</code> and its
//	 *            child <code>B</code> in a subtree <code>A -> B</code> will be
//	 *            substituted. Since it's bottom-up, <code>B</code> is firstly
//	 *            substituted, which results in <code>A->B'</code>. Later,
//	 *            <code>A</code> is substituted to <code>A'</code> then the
//	 *            children of <code>A'</code> must be updated as well otherwise
//	 *            <code>B'</code> is lost.
//	 */
//	private void visitAndSubstitute(ExpressionNode expression,
//			Map<ExpressionNode, SubstituteGuide> subMap,
//			Map<ASTNode, ASTNode> subHistory) {
//		int nchildren = expression.numChildren();
//
//		for (int i = 0; i < nchildren; i++) {
//			ASTNode child = expression.child(i);
//
//			if (child == null || child.nodeKind() != NodeKind.EXPRESSION)
//				continue;
//			visitAndSubstitute((ExpressionNode) child, subMap, subHistory);
//		}
//
//		SubstituteGuide subNode = subMap.get(expression);
//
//		if (subNode != null) {
//			ASTNode substed = subNode.subsitute(nodeFactory);
//
//			subHistory.put(expression, substed);
//			nchildren = substed.numChildren();
//			for (int i = 0; i < nchildren; i++) {
//				ASTNode child = substed.child(i);
//				ASTNode update = subHistory.get(child);
//
//				if (update != null) {
//					update.remove();
//					child.remove();
//					substed.setChild(i, update);
//				}
//			}
//		}
//	}
//
//	private ExpressionNode conjunct(List<ExpressionNode> exprs) {
//		Iterator<ExpressionNode> iter = exprs.iterator();
//		ExpressionNode result = null;
//		Source source = null;
//		TokenFactory tf = astFactory.getTokenFactory();
//
//		while (iter.hasNext()) {
//			ExpressionNode expr = iter.next();
//
//			source = source != null
//					? tf.join(source, expr.getSource())
//					: expr.getSource();
//			result = result != null
//					? nodeFactory.newOperatorNode(source, Operator.LAND,
//							expr.copy(), result)
//					: expr.copy();
//		}
//		return result;
//	}
//
//	private List<BlockItemNode> createConditionalAssumeOrAssert(
//			boolean isAssume, List<ExpressionNode> conditions,
//			List<ExpressionNode> expressions) {
//		ExpressionNode pred = conjunct(expressions);
//
//		if (pred == null)
//			return Arrays.asList();
//
//		ExpressionNode cond = conjunct(conditions);
//		StatementNode assumeOrAssert;
//
//		if (isAssume)
//			assumeOrAssert = createAssumption(pred);
//		else
//			assumeOrAssert = createAssertion(pred);
//		if (cond != null)
//			return Arrays.asList(nodeFactory.newIfNode(cond.getSource(), cond,
//					assumeOrAssert));
//		else
//			return Arrays.asList(assumeOrAssert);
//	}
//	
//	/**
//	 * Given <code>(term, binders, pred)</code> and a collate state
//	 * <code>cs</code>, return 
//	 * <code>
//	 * for (int i = 0; i < $mpi_comm_size; i++) {
//	 *     if (pred[i/binder])
//	 *        $when collate_arrived(term[i/binder]);
//	 * }
//	 * </code>
//	 * 
//	 * 
//	 * If both binders and pred are null, return
//	 * <code>$collate_arrived(cs, term .. term)</code>
//	 * 
//	 * @param waitsforArg
//	 * @param collateState
//	 * @return
//	 * @throws SyntaxException 
//	 */
//	private StatementNode waitsforArgToCollateArrivedPredicate(
//			GeneralSetComprehensionTriple waitsforArg,
//			ExpressionNode collateState) throws SyntaxException {
//		ExpressionNode collateArrivedArg;
//		Source source = waitsforArg.source;
//
//		if (waitsforArg.term
//				.expressionKind() == ExpressionNode.ExpressionKind.NOTHING) {
//			return nodeFactory.newNullStatementNode(waitsforArg.source);
//		} else
//			collateArrivedArg = makeItRange(waitsforArg.term);
//		
//		ExpressionNode functionIdentifier = nodeFactory
//				.newIdentifierExpressionNode(source,
//						nodeFactory.newIdentifierNode(source,
//								MPIContractUtilities.COLLATE_ARRIVED));
//		StatementNode guardStmt = nodeFactory.newWhenNode(source,
//				nodeFactory.newFunctionCallNode(collateState.getSource(),
//						functionIdentifier.copy(),
//						Arrays.asList(collateState.copy(), collateArrivedArg),
//						null),
//				nodeFactory.newNullStatementNode(source));
//		
//		if (waitsforArg.binders != null) {
//			guardStmt = nodeFactory.newIfNode(source,
//					waitsforArg.restrict.copy(), guardStmt);
//		} else
//			return guardStmt;
//		assert waitsforArg.binders.size() == 1;
//
//		VariableDeclarationNode binder = waitsforArg.binders.get(0).copy();
//		DeclarationListNode forInitializers;
//		ExpressionNode incrementor;
//		ExpressionNode upperBound;
//		IdentifierExpressionNode bindedId = nodeFactory
//				.newIdentifierExpressionNode(binder.getSource(),
//						binder.getIdentifier().copy());
//		IdentifierExpressionNode commSize = identifierExpressionNode(source,
//				MPIContractUtilities.MPI_COMM_SIZE_CONST);
//
//		binder.setInitializer(
//				nodeFactory.newIntegerConstantNode(binder.getSource(), "0"));
//		forInitializers = nodeFactory.newForLoopInitializerNode(
//				binder.getSource(), Arrays.asList(binder));
//		incrementor = nodeFactory.newOperatorNode(source,
//				Operator.POSTINCREMENT, bindedId);
//		upperBound = nodeFactory.newOperatorNode(binder.getSource(),
//				Operator.LT, bindedId.copy(), commSize);
//		return nodeFactory.newForLoopNode(source, forInitializers, upperBound,
//				incrementor, guardStmt, null);	
//		
//		ExpressionNode arrived = nodeFactory.newFunctionCallNode(
//				collateState.getSource(), functionIdentifier.copy(),
//				Arrays.asList(collateState.copy(),
//						makeItRange(waitsforArg.term)),
//				null);

//		if (waitsforArg.binders == null && waitsforArg.restrict == null)
//			return arrived;
//
//		Source bindersSource = null;
//		List<VariableDeclarationNode> copiedBinderList = new LinkedList<>();
//		SequenceNode<VariableDeclarationNode> binders;
//
//		for (VariableDeclarationNode binder : waitsforArg.binders) {
//			bindersSource = bindersSource == null
//					? binder.getSource()
//					: astFactory.getTokenFactory().join(bindersSource,
//							binder.getSource());
//			copiedBinderList.add(binder.copy());
//		}
//
//		binders = nodeFactory.newSequenceNode(bindersSource, "binders",
//				copiedBinderList);
//
//		PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode> bindersAndRestricts = nodeFactory
//				.newPairNode(bindersSource, binders, null);
//		List<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> temp = new LinkedList<>();
//
//		temp.add(bindersAndRestricts);
//		return nodeFactory.newQuantifiedExpressionNode(source,
//				Quantifier.FORALL,
//				nodeFactory.newSequenceNode(bindersSource, "binders", temp),
//				waitsforArg.restrict.copy(), arrived, null);
//	}
}
