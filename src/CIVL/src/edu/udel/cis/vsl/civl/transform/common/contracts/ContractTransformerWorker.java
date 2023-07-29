package edu.udel.cis.vsl.civl.transform.common.contracts;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.transform.common.BaseWorker;

/**
 * This transformer serves for CIVL Contracts mode.
 * 
 * @author xxxx
 *
 */
public class ContractTransformerWorker extends BaseWorker {

	protected ContractTransformerWorker(String transformerName,
			ASTFactory astFactory) {
		super(transformerName, astFactory);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AST transform(AST ast) throws SyntaxException {
		// TODO Auto-generated method stub
		return null;
	}
	//
//	/**
//	 * <p>
//	 * Analyzes the contracted functions and their contracts in source files and
//	 * generates transform guides for those contracts.
//	 * </p>
//	 * 
//	 * @param contractedFuncsInSrc
//	 *            an instance of {@link SourceFilesWithContracts}
//	 * @param callees
//	 *            output, a list of {@link FunctionContractTransformGuide}s for
//	 *            all contracted callee functions
//	 * @return the {@link FunctionContractTransformGuide} for the contracted
//	 *         target function
//	 * @throws SyntaxException
//	 */
//	private List<FunctionContractTransformGuide> analysisContractedFunctions(
//			SourceFilesWithContracts contractedFuncsInSrc,
//			List<FunctionContractTransformGuide> callees)
//			throws SyntaxException {
//		Map<String, String> globalMpiDatatype2IntermediateNames = new HashMap<>();
//
//		// analyze callees:
//		for (ContractedFunction callee : contractedFuncsInSrc.callees) {
//			MemoryLocationManager memoryLocationManager = new MemoryLocationManager(
//					nodeFactory);
//			ContractClauseTransformer clauseTransformer = new ContractClauseTransformer(
//					astFactory, memoryLocationManager);
//			boolean purelyLocal = callee.contracts.size() == 1
//					&& callee.contracts.get(0).isSequentialBlock();
//			FunctionContractTransformGuide info = new FunctionContractTransformGuide(
//					callee.function, memoryLocationManager);
//
//			for (FunctionContractBlock block : callee.contracts) {
//				List<ClauseTransformGuide> requiresTuples = new LinkedList<>();
//				List<ClauseTransformGuide> ensuresTuples = new LinkedList<>();
//
//				clauseTransformer.analysisContractBlock(block, true,
//						purelyLocal, globalMpiDatatype2IntermediateNames,
//						requiresTuples, ensuresTuples);
//				info.addGuide(block, requiresTuples, ensuresTuples);
//			}
//			callees.add(info);
//		}
//		// analyze target:
//		MemoryLocationManager memoryLocationManager = new MemoryLocationManager(
//				nodeFactory);
//		ContractClauseTransformer clauseTransformer = new ContractClauseTransformer(
//				astFactory, memoryLocationManager);
//		List<ContractedFunction> targets = contractedFuncsInSrc.targets;
//		List<FunctionContractTransformGuide> targetInfos = new LinkedList<>();
//
//		for (ContractedFunction target : targets) {
//			boolean purelyLocal = target.contracts.size() == 1
//					&& target.contracts.get(0).isSequentialBlock();
//			FunctionContractTransformGuide targetInfo = new FunctionContractTransformGuide(
//					target.function, memoryLocationManager);
//
//			for (FunctionContractBlock block : target.contracts) {
//				List<ClauseTransformGuide> requiresTuples = new LinkedList<>();
//				List<ClauseTransformGuide> ensuresTuples = new LinkedList<>();
//
//				clauseTransformer.analysisContractBlock(block, false,
//						purelyLocal, globalMpiDatatype2IntermediateNames,
//						requiresTuples, ensuresTuples);
//				targetInfo.addGuide(block, requiresTuples, ensuresTuples);
//			}
//			targetInfos.add(targetInfo);
//		}
//		return targetInfos;
//	}
//
//	/**
//	 * <p>
//	 * Transform a non-target contracted function into a deductive executable
//	 * form.
//	 * </p>
//	 * 
//	 * <p>
//	 * The body of a non-target contracted function f will be added or replaced
//	 * its definition with: <code>
//	 * f () {
//	 *   assert ( seq-requires );
//	 *   cp = snapshot();
//	 *   $run $when($collate_arrived(cp, args .. )) $with(cp) 
//	 *      if (assumes-cond)
//	 *         assert ( col-requires );
//	 *         
//	 *   int $result;
//	 *   
//	 *   $havoc(&$result);
//	 *   assume( seq-ensures);
//	 *   if (assume-cond)
//	 *      $wit(cp) assume(non-sync-col-ensures);
//	 *   $run {  
//	 *     if (assume-cond)
//	 *        $when($collate_arrived(cp, args .. )) $with(cp)
//	 *           assume(sync-col-ensures);
//	 *   }
//	 * }
//	 * </code>
//	 * </p>
//	 * 
//	 * @param funcDecl
//	 *            The {@link FunctionDeclarationNode} of the transformed
//	 *            function. It's original body will be removed.
//	 * @return
//	 * @throws SyntaxException
//	 */
//	private FunctionDefinitionNode transformCalleeFunction(
//			FunctionDeclarationNode funcDecl, FunctionTypeNode funcTypeNode,
//			FunctionContractTransformGuide guide) throws SyntaxException {
//		CompoundStatementNode body;
//		Source contractSource = funcDecl.getContract().getSource();;
//		ContractClauseTransformer clauseTransformer = new ContractClauseTransformer(
//				astFactory, guide.memoryLocationManager);
//		/*
//		 * Requirements (TODO: including assigns) of callees will be transformed
//		 * to assertions
//		 */
//		List<BlockItemNode> transformedRequirements = new LinkedList<>();
//		/* Ensurances of callees will be transformed to assumptions */
//		List<BlockItemNode> transformedEnsurances = new LinkedList<>();
//		List<ClauseTransformGuide> reqGuides4SideCond = new LinkedList<>();
//		List<ClauseTransformGuide> ensGuides4SideCond = new LinkedList<>();
//
//		if (guide.localBlock != null) {
//			TransformedPair localPair = clauseTransformer.transformLocalBlock(
//					guide.localREGuides.requiresGuides,
//					guide.localREGuides.ensuresGuides, guide.localBlock,
//					guide.collectiveREGuides.isEmpty(), true);
//
//			reqGuides4SideCond.addAll(guide.localREGuides.requiresGuides);
//			ensGuides4SideCond.addAll(guide.localREGuides.ensuresGuides);
//			transformedRequirements.addAll(localPair.before);
//			transformedEnsurances.addAll(localPair.after);
//		}
//		for (Pair<FunctionContractBlock, REGuidePair> collectiveTuples : guide.collectiveREGuides) {
//			TransformedPair transformedBlockPair = clauseTransformer
//					.transformMPICollectiveBlock(
//							collectiveTuples.right.requiresGuides,
//							collectiveTuples.right.ensuresGuides,
//							collectiveTuples.left, false);
//
//			reqGuides4SideCond.addAll(collectiveTuples.right.requiresGuides);
//			ensGuides4SideCond.addAll(collectiveTuples.right.ensuresGuides);
//			transformedRequirements.addAll(transformedBlockPair.before);
//			transformedEnsurances.addAll(transformedBlockPair.after);
//		}
//		/* check side conditions */
//		transformedRequirements.addAll(
//				clauseTransformer.checkSideConditions(reqGuides4SideCond));
//		transformedEnsurances.addAll(
//				clauseTransformer.checkSideConditions(ensGuides4SideCond));
//
//		/* inserts $mpi_comm_rank and $mpi_comm_size: */
//		transformedRequirements.add(0,
//				nodeFactory.newVariableDeclarationNode(mpiCommRankSource,
//						identifier(MPIContractUtilities.MPI_COMM_RANK_CONST),
//						intTypeNode.copy()));
//		transformedRequirements.add(0,
//				nodeFactory.newVariableDeclarationNode(mpiCommSizeSource,
//						identifier(MPIContractUtilities.MPI_COMM_SIZE_CONST),
//						intTypeNode.copy()));
//		List<BlockItemNode> bodyItems = new LinkedList<>();
//		boolean returnVoid = false;
//
//		bodyItems.addAll(transformedRequirements);
//		returnVoid = isVoidType(funcTypeNode.getReturnType().getType());
//		if (!returnVoid) {
//			bodyItems.add(nodeFactory.newVariableDeclarationNode(contractSource,
//					identifier(MPIContractUtilities.ACSL_RESULT_VAR),
//					funcTypeNode.getReturnType().copy()));
//			bodyItems
//					.add(nodeFactory.newExpressionStatementNode(createHavocCall(
//							identifierExpression(
//									MPIContractUtilities.ACSL_RESULT_VAR),
//							nodeFactory)));
//		}
//		bodyItems.addAll(transformedEnsurances);
//		if (!returnVoid)
//			bodyItems.add(nodeFactory.newReturnNode(
//					newSource(RETURN_RESULT, CivlcTokenConstant.RETURN),
//					identifierExpression(
//							MPIContractUtilities.ACSL_RESULT_VAR)));
//		body = nodeFactory.newCompoundStatementNode(funcDecl.getSource(),
//				bodyItems);
//		return nodeFactory.newFunctionDefinitionNode(funcDecl.getSource(),
//				funcDecl.getIdentifier().copy(), funcTypeNode.copy(), null,
//				body);
//	}
//
//	/**
//	 * <p>
//	 * <b>Summary:</b> Wraps the target function with a harness function. The
//	 * harness is created based on the contracts of the target function.
//	 * </p>
//	 * <p>
//	 * <b>Details:</b> The contracted function will be transformed into the
//	 * following pattern:
//	 * <ul>
//	 * <b> driver( ... ) { </b>
//	 * <li>1 localContractStatements;</li>
//	 * <li>2 $mpi_comm_size and $mpi_comm_rank decl;</li>
//	 * <li>3 MPI_Comm_size(comm, &$mpi_comm_size) && MPI_Comm_rank( ... );</li>
//	 * <li>4 take-snapshot;</li>
//	 * <li>5 collectiveContractStatements</li>
//	 * <li>6 enters</li>
//	 * <li>7 $result declaration && call target function</li>
//	 * <li>8 entered check</li>
//	 * <li>9 localContractStatements;</li>
//	 * <li>10 take-snapshot;</li>
//	 * <li>11 collectiveContractStatements</li> <b>}</b>
//	 * </p>
//	 * 
//	 * @param funcDefi
//	 *            The definition of the target function
//	 * @return A new driver function for the target function.
//	 * @throws SyntaxException
//	 */
//	private FunctionDefinitionNode transformTargetFunction(
//			FunctionDefinitionNode funcDefi,
//			FunctionContractTransformGuide guide, boolean hasMPI)
//			throws SyntaxException {
//		CompoundStatementNode body;
//		String driverName = guide.getDriverNameForVerification();
//		Source contractSource = funcDefi.getContract().getSource();
//		Source driverSource = newSource(driverName,
//				CivlcTokenConstant.FUNCTION_DEFINITION);
//		ContractClauseTransformer clauseTransformer = new ContractClauseTransformer(
//				astFactory, guide.memoryLocationManager);
//
//		List<BlockItemNode> requirements = new LinkedList<>();
//		List<BlockItemNode> ensurances = new LinkedList<>();
//		List<ClauseTransformGuide> reqGuides4SideCond = new LinkedList<>();
//		List<ClauseTransformGuide> ensGuides4SideCond = new LinkedList<>();
//
//		if (guide.localBlock != null) {
//			TransformedPair localPair = clauseTransformer.transformLocalBlock(
//					guide.localREGuides.requiresGuides,
//					guide.localREGuides.ensuresGuides, guide.localBlock,
//					guide.collectiveREGuides.isEmpty(), false);
//
//			reqGuides4SideCond.addAll(guide.localREGuides.requiresGuides);
//			ensGuides4SideCond.addAll(guide.localREGuides.ensuresGuides);
//			requirements.addAll(localPair.before);
//			ensurances.addAll(localPair.after);
//		}
//		// for each MPI block, translate requirements:
//		for (Pair<FunctionContractBlock, REGuidePair> collectiveGuide : guide.collectiveREGuides) {
//			TransformedPair pair = clauseTransformer
//					.transformMPICollectiveBlock(
//							collectiveGuide.right.requiresGuides,
//							collectiveGuide.right.ensuresGuides,
//							collectiveGuide.left, true);
//
//			reqGuides4SideCond.addAll(collectiveGuide.right.requiresGuides);
//			ensGuides4SideCond.addAll(collectiveGuide.right.ensuresGuides);
//			requirements.addAll(pair.before);
//			ensurances.addAll(pair.after);
//		}
//		/* check side conditions */
//		requirements.addAll(
//				clauseTransformer.checkSideConditions(reqGuides4SideCond));
//		ensurances.addAll(
//				clauseTransformer.checkSideConditions(ensGuides4SideCond));
//
//		if (hasMPI) {
//			// add $mpi_comm_rank and $mpi_comm_size variables:
//			requirements.add(0,
//					nodeFactory.newVariableDeclarationNode(mpiCommRankSource,
//							identifier(
//									MPIContractUtilities.MPI_COMM_RANK_CONST),
//							intTypeNode.copy()));
//			requirements.add(0,
//					nodeFactory.newVariableDeclarationNode(mpiCommSizeSource,
//							identifier(
//									MPIContractUtilities.MPI_COMM_SIZE_CONST),
//							intTypeNode.copy()));
//		}
//
//		List<BlockItemNode> driverComponents = new LinkedList<>();
//		ExpressionNode targetCall;
//		ExpressionNode originalBodyIdentifier = identifierExpression(
//				guide.getFunctionNameForOriginalBody());
//		FunctionTypeNode funcTypeNode = funcDefi.getTypeNode();
//		List<ExpressionNode> funcParamIdentfiers = new LinkedList<>();
//
//		for (VariableDeclarationNode param : funcTypeNode.getParameters())
//			funcParamIdentfiers
//					.add(identifierExpression(param.getIdentifier().name()));
//		targetCall = nodeFactory.newFunctionCallNode(driverSource,
//				originalBodyIdentifier, funcParamIdentfiers, null);
//
//		// Create variable declarations which are actual parameters of the
//		// target function:
//		driverComponents
//				.addAll(createVariableDeclsAndInitsForDriver(funcTypeNode));
//		driverComponents.addAll(requirements);
//		if (!isVoidType(funcTypeNode.getReturnType().getType()))
//			driverComponents.add(nodeFactory.newVariableDeclarationNode(
//					contractSource,
//					identifier(MPIContractUtilities.ACSL_RESULT_VAR),
//					funcDefi.getTypeNode().getReturnType().copy(), targetCall));
//		else
//			driverComponents
//					.add(nodeFactory.newExpressionStatementNode(targetCall));
//
//		if (hasMPI)
//			// if function has collective contract, add a Barrier with
//			// MPI_COMM_WORLD at the end of the driver:
//			driverComponents
//					.add(nodeFactory.newExpressionStatementNode(functionCall(
//							driverSource, MPIContractUtilities.MPI_BARRIER_CALL,
//							Arrays.asList(identifierExpression(
//									MPIContractUtilities.MPI_COMM_WORLD)))));
//		driverComponents.addAll(ensurances);
//		body = nodeFactory.newCompoundStatementNode(driverSource,
//				driverComponents);
//		funcTypeNode = nodeFactory.newFunctionTypeNode(funcTypeNode.getSource(),
//				funcTypeNode.getReturnType().copy(),
//				nodeFactory.newSequenceNode(
//						funcTypeNode.getParameters().getSource(),
//						"contract_driver_parameters", Arrays.asList()),
//				funcTypeNode.hasIdentifierList());
//		return nodeFactory.newFunctionDefinitionNode(driverSource,
//				identifier(driverName), funcTypeNode.copy(), null, body);
//	}
//
//	/*
//	 * ************************* Utility methods ****************************
//	 */
//	/**
//	 * Creates an <code>MPI_Init(NULL, NULL);</code> call statememt node.
//	 * 
//	 * @return The created statement node
//	 * @throws SyntaxException
//	 */
//	private StatementNode createMPIInitCall() throws SyntaxException {
//		IntegerConstantNode zero = nodeFactory.newIntegerConstantNode(
//				newSource("0", CivlcTokenConstant.INTEGER_CONSTANT), "0");
//		TypeNode ptr2Void = nodeFactory.newPointerTypeNode(
//				newSource("(void *)", CivlcTokenConstant.TYPE),
//				nodeFactory.newVoidTypeNode(
//						newSource("void", CivlcTokenConstant.TYPE)));
//		CastNode nullPtr = nodeFactory.newCastNode(
//				newSource("(void *)0", CivlcTokenConstant.CAST), ptr2Void,
//				zero);
//		return nodeFactory
//				.newExpressionStatementNode(nodeFactory.newFunctionCallNode(
//						newSource("MPI_Init(NULL, NULL);",
//								CivlcTokenConstant.CALL),
//						identifierExpression(MPI_INIT_CALL),
//						Arrays.asList(nullPtr, nullPtr.copy()), null));
//	}
//
//	/**
//	 * Creates an <code>createMPIFinalizeCall();</code> call statement node.
//	 * 
//	 * @return The created statement node
//	 */
//	private StatementNode createMPIFinalizeCall() {
//		return nodeFactory
//				.newExpressionStatementNode(nodeFactory.newFunctionCallNode(
//						newSource("MPI_Finalize();", CivlcTokenConstant.CALL),
//						identifierExpression(MPI_FINALIZE_CALL),
//						Arrays.asList(), null));
//	}
//
//	/**
//	 * <p>
//	 * <b>Summary: </b> Creates an $havoc function call:<code>
//	 * $mpi_snapshot(&var);</code>
//	 * </p>
//	 * 
//	 * @param var
//	 *            An {@link ExpressionNode} representing an variable.
//	 * @return The created $havoc call expression node.
//	 */
//	private ExpressionNode createHavocCall(ExpressionNode var,
//			NodeFactory nodeFactory) {
//		Source source = var.getSource();
//		ExpressionNode callIdentifier = identifierExpression(source,
//				MPIContractUtilities.HAVOC);
//		ExpressionNode addressOfVar = nodeFactory.newOperatorNode(
//				var.getSource(), Operator.ADDRESSOF, var.copy());
//		FunctionCallNode call = nodeFactory.newFunctionCallNode(source,
//				callIdentifier, Arrays.asList(addressOfVar), null);
//
//		return call;
//	}
//
//	/**
//	 * <p>
//	 * <b>Summary: </b> Transform the parameters of the target function into a
//	 * sequence of variable declarations. All of them will be initialized with
//	 * arbitrary values.
//	 * </p>
//	 * 
//	 * @param targetFuncType
//	 *            A {@link FunctionTypeNode} which represents the function type
//	 *            of the target function.
//	 * @return
//	 */
//	private List<BlockItemNode> createVariableDeclsAndInitsForDriver(
//			FunctionTypeNode targetFuncType) {
//		SequenceNode<VariableDeclarationNode> formals = targetFuncType
//				.getParameters();
//		List<BlockItemNode> results = new LinkedList<>();
//
//		// create an variable for each formal parameter
//		for (VariableDeclarationNode varDecl : formals) {
//			VariableDeclarationNode actualDecl;
//
//			// TODO: need a better way: currently for MPI_Comm type
//			// parameters, it is always replaced with MPI_COMM_WORLD:
//			if (varDecl.getTypeNode().getType()
//					.kind() == TypeKind.STRUCTURE_OR_UNION) {
//				StructureOrUnionType structType = (StructureOrUnionType) varDecl
//						.getTypeNode().getType();
//
//				if (structType.getName().equals(MPI_COMM_TYPE)) {
//					results.add(nodeFactory.newVariableDeclarationNode(
//							varDecl.getSource(), identifier(varDecl.getName()),
//							varDecl.getTypeNode().copy(), identifierExpression(
//									MPIContractUtilities.MPI_COMM_WORLD)));
//					continue;
//				}
//			}
//			actualDecl = varDecl.copy();
//
//			StatementNode havoc;
//
//			results.add(actualDecl);
//			// $havoc for the actual parameter declaration:
//			havoc = nodeFactory.newExpressionStatementNode(createHavocCall(
//					identifierExpression(actualDecl.getName()), nodeFactory));
//			results.add(havoc);
//		}
//		return results;
//	}
//
//	/**
//	 * Find out variable declarations in the given list of block item nodes, do
//	 * $havoc for them.
//	 * 
//	 * @param root
//	 * @return
//	 */
//	private List<BlockItemNode> havocForGlobalVariables(
//			List<BlockItemNode> root) {
//		List<BlockItemNode> havocs = new LinkedList<>();
//
//		for (BlockItemNode item : root) {
//			if (item.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
//				VariableDeclarationNode decl = ((VariableDeclarationNode) item);
//				String name = ((VariableDeclarationNode) item).getName();
//
//				globalVarDecls.add(decl);
//				havocs.add(
//						nodeFactory.newExpressionStatementNode(createHavocCall(
//								identifierExpression(name), nodeFactory)));
//			}
//		}
//		return havocs;
//	}
//
//	/**
//	 * Return a list of {@link BlockItemNode}s which comes from source code in
//	 * "source file"s. Here a "source file" is a file with ".c, .cvl, .h" or
//	 * ".cvh" suffix and NOT under the system include path.
//	 * 
//	 * @param root
//	 *            the root node of an AST
//	 * @param srcFileNodes
//	 *            OUTPUT. all the block item nodes in the given AST that come
//	 *            from "source files"
//	 * @return true iff there is at least one node comes from "mpi.h"
//	 */
//	private boolean findMPIAndNodesofSourceFiles(
//			SequenceNode<BlockItemNode> root,
//			List<BlockItemNode> srcFileNodes) {
//		Path civlIncludePath = CIVLConstants.CIVL_INCLUDE_PATH.toPath();
//		Path frontendIncludePath = CIVLConstants.FRONT_END_INCLUDE_PATH
//				.toPath();
//		boolean hasMPI = false;
//
//		for (BlockItemNode node : root) {
//			if (node == null)
//				continue;
//
//			Path sourceFilePath = node.getSource().getFirstToken()
//					.getSourceFile().getFile().toPath();
//			String sourceFileName = sourceFilePath.getFileName().toString();
//
//			if (sourceFilePath.startsWith(civlIncludePath)
//					|| sourceFilePath.startsWith(frontendIncludePath)) {
//				if (!hasMPI && sourceFileName.equals("mpi.h"))
//					hasMPI = true;
//				continue;
//			}
//			srcFileNodes.add(node);
//
//			assert sourceFileName.endsWith(".c")
//					|| sourceFileName.endsWith(".cvl")
//					|| sourceFileName.endsWith(".h")
//					|| sourceFileName.endsWith(".cvh");
//		}
//		return hasMPI;
//	}
//
//	/**
//	 * <p>
//	 * This class represents a transformation guide for a whole function
//	 * contract. A function contract guide consists of
//	 * {@link ClauseTransformGuide}s for contract clauses and an instance of
//	 * {@link MemoryLocationManager} which is a stateful object deals with
//	 * allocation and refreshment.
//	 * </p>
//	 * 
//	 * TODO: make a guide for assigns and waitsfor too ?
//	 * 
//	 * @author xxxx
//	 *
//	 */
//	class FunctionContractTransformGuide {
//
//		/**
//		 * a reference to the function declaration
//		 */
//		FunctionDeclarationNode function;
//		/**
//		 * a sole local block, a function will have at most one local function
//		 * contract block
//		 */
//		FunctionContractBlock localBlock;
//		/**
//		 * a pair of {@link ClauseTransformGuide}s for requirements and ensures
//		 * in the local contract block
//		 */
//		REGuidePair localREGuides;
//		/**
//		 * a list of {@link ClauseTransformGuide}s for requirements and ensures
//		 * in collective contract blocks
//		 */
//		List<Pair<FunctionContractBlock, REGuidePair>> collectiveREGuides;
//		/**
//		 * a instance of a {@link MemoryLocationManager}
//		 */
//		MemoryLocationManager memoryLocationManager;
//
//		FunctionContractTransformGuide(FunctionDeclarationNode function,
//				MemoryLocationManager memoryLocationManager) {
//			this.function = function;
//			this.localREGuides = new REGuidePair();
//			this.collectiveREGuides = new LinkedList<>();
//			this.memoryLocationManager = memoryLocationManager;
//			localBlock = null;
//		}
//
//		void addGuide(FunctionContractBlock block,
//				List<ClauseTransformGuide> requiresGuides,
//				List<ClauseTransformGuide> ensuresGuides) {
//			if (block.isSequentialBlock()) {
//				assert localBlock == null;
//				localBlock = block;
//				localREGuides.requiresGuides.addAll(requiresGuides);
//				localREGuides.ensuresGuides.addAll(ensuresGuides);
//			} else {
//				REGuidePair coPair = new REGuidePair();
//
//				coPair.requiresGuides.addAll(requiresGuides);
//				coPair.ensuresGuides.addAll(ensuresGuides);
//				this.collectiveREGuides.add(new Pair<>(block, coPair));
//			}
//		}
//
//		/**
//		 * @return the name of the function that contains the original function
//		 *         body of the corresponding function
//		 */
//		String getFunctionNameForOriginalBody() {
//			return this.function.getName() + originalSuffix;
//		}
//
//		/**
//		 * @return the name of the function that launches the contract
//		 *         verification of this function
//		 */
//		String getDriverNameForVerification() {
//			return this.function.getName() + driverSuffix;
//		}
//
//		/**
//		 * a simple data structure for clause transform guides of
//		 * <code>requires</code> and <code>ensures</code>
//		 * 
//		 * @author xxxx
//		 *
//		 */
//		class REGuidePair {
//			List<ClauseTransformGuide> requiresGuides;
//			List<ClauseTransformGuide> ensuresGuides;
//
//			REGuidePair() {
//				this.requiresGuides = new LinkedList<>();
//				this.ensuresGuides = new LinkedList<>();
//			}
//		}
//	}
//
//	/**
//	 * <p>
//	 * This class is a data structure represents the source files (excludes
//	 * CIVL-C libraries). The ASTNodes of source files are organized in three
//	 * groups:
//	 * <ul>
//	 * <li>The target function definitions and their contracts,
//	 * {@link SourceFilesWithContracts#targets}</li>
//	 * <li>The first encountered contracted callee function declarations and
//	 * their contracts, {@link SourceFilesWithContracts#callees}</li>
//	 * <li>The rest of the ASTNodes in the source files,
//	 * {@link SourceFilesWithContracts#others}</li>
//	 * </ul>
//	 * </p>
//	 * 
//	 * note that group 1 and group 2 may have overlaps. Group 3 is disjoint from
//	 * group 1 and group 2.
//	 * 
//	 * TODO: think about conjunctions of contracts over multiple declarations of
//	 * the same function
//	 * 
//	 * @author xxxx
//	 */
//	class SourceFilesWithContracts {
//		/**
//		 * A contracted function data structure, including a set of
//		 * {@link FunctionContractBlock} and a {@link FunctionDeclarationNode}
//		 * of the function.
//		 * 
//		 * @author xxxx
//		 */
//		class ContractedFunction {
//
//			final List<FunctionContractBlock> contracts;
//
//			final FunctionDeclarationNode function;
//
//			ContractedFunction(FunctionDeclarationNode function) {
//				this.function = function;
//				this.contracts = FunctionContractBlock
//						.parseContract(function.getContract(), nodeFactory);
//			}
//		}
//
//		/**
//		 * the target function definitions and its contracts
//		 */
//		final List<ContractedFunction> targets;
//
//		/**
//		 * the first encountered contracted callee function declarations and
//		 * their contracts
//		 */
//		final List<ContractedFunction> callees;
//
//		/**
//		 * the rest of the ASTNodes in the source files
//		 */
//		final List<BlockItemNode> others;
//
//		SourceFilesWithContracts(List<FunctionDefinitionNode> targets,
//				List<FunctionDeclarationNode> callees,
//				List<BlockItemNode> others) {
//			this.targets = new LinkedList<>();
//			for (FunctionDefinitionNode target : targets)
//				this.targets.add(new ContractedFunction(target));
//			this.callees = new LinkedList<>();
//			for (FunctionDeclarationNode callee : callees)
//				this.callees.add(new ContractedFunction(callee));
//			this.others = others;
//		}
//	}
//
//	/**
//	 * If the given expression is a cast-expression: <code>(T) expr</code>,
//	 * return an expression representing <code>expr</code>, otherwise no-op.
//	 * 
//	 * @param expression
//	 *            An instance of {@link ExpressionNode}
//	 * @return An expression who is the argument of a cast expression iff the
//	 *         input is a cast expression, otherwise returns input itself.(i.e.
//	 *         no-op).
//	 */
//	static ExpressionNode decast(ExpressionNode expression) {
//		if (expression.expressionKind() == ExpressionKind.CAST) {
//			CastNode castNode = (CastNode) expression;
//
//			return castNode.getArgument();
//		}
//		return expression;
//	}
}
