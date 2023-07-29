package edu.udel.cis.vsl.civl.transform.common.contracts;

import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.COLLATE_POST_STATE_NAME;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.COLLATE_PRE_STATE_NAME;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.MPI_BARRIER_FUN;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.MPI_COMM_RANK_CONST;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.MPI_COMM_SIZE_CONST;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.MPI_COMM_WORLD_CONST;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.MPI_SNAPSHOT;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.MPI_UNSNAPSHOT;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.POST_STATE_NAME;
import static edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS.PRE_STATE_NAME;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CastNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.transform.IF.ContractTransformer;
import edu.udel.cis.vsl.civl.transform.common.BaseWorker;
import edu.udel.cis.vsl.civl.transform.common.contracts.ContractTransformerWorker2.SourceFilesWithContracts.ContractedFunction;
import edu.udel.cis.vsl.civl.transform.common.contracts.FunctionContractBlock.ContractBehavior;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.CalleeFrameCondTranslator;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.CalleePostconditionTranslator;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.CalleePreconditionTraslator;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.CalleeWaitsforTranslator;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.ContractClauseTranslation;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.MPI_CONTRACT_CONSTS;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.TargetFrameCondTranslator;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.TargetPostconditionTranslator;
import edu.udel.cis.vsl.civl.transform.common.contracts.translators.TargetPreconditionTranslator;
import edu.udel.cis.vsl.civl.util.IF.Triple;

/**
 * This transformer serves for CIVL Contracts mode.
 * 
 * @author xxxx
 *
 */
public class ContractTransformerWorker2 extends BaseWorker {

	/**
	 * Naming suffix for a generated function that contains the original body of
	 * a verifying function:
	 */
	static private final String originalSuffix = "_$origin_";

	/**
	 * Naming suffix for a generated function that drives the verification of a
	 * verifying function:
	 */
	static private final String driverSuffix = "_$driver_";

	/**
	 * The name of main function:
	 */
	private final static String MAIN_NAME = "main";

	/**
	 * MPI_Comm typedef name:
	 */
	private final static String MPI_COMM_TYPE = "MPI_Comm";

	/**
	 * The default MPI communicator identifier:
	 */

	/**
	 * An MPI routine identifier:
	 */
	private final static String MPI_INIT_CALL = "MPI_Init";

	/**
	 * An MPI routine identifier:
	 */
	private final static String MPI_FINALIZE_CALL = "MPI_Finalize";

	/**
	 * A string source for a return statement:
	 */
	private final static String RETURN_RESULT = "return $result;";

	/**
	 * Set of all global variables in source files:
	 */
	private Set<VariableDeclarationNode> globalVarDecls = new HashSet<>();

	/**
	 * Names of all driver functions for all verifying functions:
	 */
	private Set<String> allDriverNames = new HashSet<>();

	/* ********************* Private class fields: ********************** */
	/**
	 * The target function that will be verified independently. Other functions
	 * will be not verified. For other functions that been annotated with
	 * contracts, the transformer will remove their bodies, since only their
	 * contracts are used.
	 */
	private final String targetFunctionName;

	/**
	 * {@link Source} of <code>int $mpi_comm_size, $mpi_comm_rank;</code>
	 */
	private Source mpiCommSizeSource, mpiCommRankSource;

	/**
	 * A int type node
	 */
	private TypeNode intTypeNode;

	public ContractTransformerWorker2(ASTFactory astFactory,
			String targetFunctionName, CIVLConfiguration civlConfig) {
		super(ContractTransformer.LONG_NAME, astFactory);
		identifierPrefix = MPIContractUtilities.CIVL_CONTRACT_PREFIX;
		this.targetFunctionName = targetFunctionName;
		intTypeNode = nodeFactory.newBasicTypeNode(
				newSource("int", CivlcTokenConstant.TYPE), BasicTypeKind.INT);
		this.mpiCommRankSource = this.newSource("$mpi_comm_rank",
				CivlcTokenConstant.IDENTIFIER);
		this.mpiCommSizeSource = this.newSource("$mpi_comm_size",
				CivlcTokenConstant.IDENTIFIER);
	}

	/* ************************* Public methods: ************************** */
	@Override
	public AST transform(AST ast) throws SyntaxException {
		SequenceNode<BlockItemNode> root = ast.getRootNode();
		List<BlockItemNode> externalList = new LinkedList<>();
		SequenceNode<BlockItemNode> newRootNode;
		List<BlockItemNode> sourceFiles = new LinkedList<>();
		boolean hasMPI = false;
		AST newAst;
		int count;

		hasMPI = findMPIAndNodesofSourceFiles(root, sourceFiles);

		// transformed contracted functions:
		List<BlockItemNode> transformedContractedFuncs;
		// extract function definitions and declarations from source files:
		SourceFilesWithContracts contractedFuncsInSrc = extractContractedFunctionsFromSourceFileNodes(
				sourceFiles);

		// targets = analysisContractedFunctions(contractedFuncsInSrc, callees);
		ast.release();
		transformedContractedFuncs = processContractedFunctions(
				contractedFuncsInSrc);
		// takes off the rest in the source files:
		sourceFiles.clear();
		for (BlockItemNode otherInSrc : contractedFuncsInSrc.others)
			if (otherInSrc != null) {
				sourceFiles.add(otherInSrc);
				otherInSrc.remove();
			}
		// inserting the rests in the AST to the new tree:
		count = root.numChildren();
		for (int i = 0; i < count; i++) {
			BlockItemNode child = root.getSequenceChild(i);

			if (child != null) {
				root.removeChild(i);
				externalList.add(child);
			}
		}
		// adds the rest in the source files to the new tree:
		externalList.addAll(sourceFiles);
		// adding transformed functions:
		externalList.addAll(transformedContractedFuncs);
		externalList.add(mainFunction(hasMPI));
		newRootNode = nodeFactory.newSequenceNode(
				newSource("TranslationUnit",
						CivlcTokenConstant.TRANSLATION_UNIT),
				"TranslationUnit", externalList);
		completeSources(newRootNode);
		newAst = astFactory.newAST(newRootNode, ast.getSourceFiles(),
				ast.isWholeProgram());
		// a dirty hack to replace all \mpi_comm_rank & \mpi_comm_size
		// newRootNode = newAst.getRootNode();
		// newAst.release();
		// MPIContractUtilities.replaceMPICommRankAndSizeForall(newRootNode,
		// nodeFactory);
		// MPIContractUtilities.workaroundForTransformedValid(newRootNode,
		// nodeFactory);
		// newAst = astFactory.newAST(newRootNode, ast.getSourceFiles(),
		// ast.isWholeProgram());
		// completeSources(newRootNode);
		//newAst.prettyPrint(System.out, false);
		return newAst;
	}
	/* ******************* Package private methods: ******************** */
	/**
	 * @param type
	 *            a {@link Type} instance
	 * @param source
	 *            {@link Source} will associate to the returned node
	 * @return A {@link TypeNode} of the given type.
	 */
	TypeNode typeNode(Type type, Source source) {
		return super.typeNode(source, type);
	}
	/* ******************* Primary transforming methods: ******************** */
	/**
	 * <p>
	 * <b>Summary: </b> Create a new main function which enables all driver
	 * functions. If the MPI library is included, wrap the call to driver with a
	 * pair of <code>MPI_Init and MPI_Finalize</code>.
	 * 
	 * @param targetFunc
	 *            The target function. The driver of the target function will be
	 *            called in the created main function.
	 * @param hasMPI
	 *            If MPI library is included.
	 * @return The created main function definition node
	 * @throws SyntaxException
	 */
	private FunctionDefinitionNode mainFunction(boolean hasMPI)
			throws SyntaxException {
		List<BlockItemNode> items = new LinkedList<BlockItemNode>();
		List<StatementNode> callDrivers = new LinkedList<>();
		Source combinedSource = null;

		// To enable multiple driver functions without letting their assumptions
		// interfere with each other, non-deterministic choice is used. i.e.
		// x = choose_int(#drivers);
		// if (x == 0) call driver1;
		// if (x == 1) call driver2;

		// the name of the variable taking result of $choose_int:
		String ndVerifyChoicerName = "CIVL_choose";
		int driverCounter = 0;

		for (String driverName : allDriverNames) {
			// creating calls to drivers with a branch condition for
			// non-determinism:
			Source source = newSource(targetFunctionName + "(...);",
					CivlcTokenConstant.CALL);
			StatementNode callDriver = nodeFactory.newExpressionStatementNode(
					nodeFactory.newFunctionCallNode(source,
							identifierExpression(driverName), Arrays.asList(),
							null));
			ExpressionNode choiceCond;

			choiceCond = nodeFactory.newOperatorNode(source, Operator.EQUALS,
					Arrays.asList(identifierExpression(ndVerifyChoicerName),
							nodeFactory.newIntConstantNode(source,
									driverCounter++)));
			callDriver = nodeFactory.newIfNode(source, choiceCond, callDriver);
			callDrivers.add(callDriver);
			combinedSource = combinedSource == null
					? source
					: astFactory.getTokenFactory().join(source, combinedSource);
		}
		// build the body of the generated main function:

		// choose_int call:
		ExpressionNode ndVerifyChoicesCall = functionCall(combinedSource,
				CHOOSE_INT,
				Arrays.asList(nodeFactory.newIntConstantNode(combinedSource,
						callDrivers.size())));
		StatementNode ndVerifyChoices = nodeFactory.newExpressionStatementNode(
				nodeFactory.newOperatorNode(combinedSource, Operator.ASSIGN,
						Arrays.asList(identifierExpression(ndVerifyChoicerName),
								ndVerifyChoicesCall)));
		BlockItemNode ndChoicerDecl = nodeFactory.newVariableDeclarationNode(
				combinedSource, identifier(ndVerifyChoicerName), nodeFactory
						.newBasicTypeNode(combinedSource, BasicTypeKind.INT));

		items.add(ndChoicerDecl);
		items.add(ndVerifyChoices);
		if (hasMPI) {
			// insert MPI_Init and MPI_Destroy
			items.add(createMPIInitCall());
			items.addAll(callDrivers);
			items.add(createMPIFinalizeCall());
		} else
			items.addAll(callDrivers);

		CompoundStatementNode mainBody = nodeFactory.newCompoundStatementNode(
				newSource("main body", CivlcTokenConstant.COMPOUND_STATEMENT),
				items);
		SequenceNode<VariableDeclarationNode> mainFormals = nodeFactory
				.newSequenceNode(this.newSource(
						"formal parameter of the declaration of the main function",
						CivlcTokenConstant.DECLARATION_LIST),
						"FormalParameterDeclarations",
						new ArrayList<VariableDeclarationNode>());
		FunctionTypeNode mainType = nodeFactory.newFunctionTypeNode(
				this.newSource("type of the main function",
						CivlcTokenConstant.TYPE),
				this.basicType(BasicTypeKind.INT), mainFormals, true);

		return nodeFactory.newFunctionDefinitionNode(
				this.newSource("definition of the main function",
						CivlcTokenConstant.FUNCTION_DEFINITION),
				this.identifier(MAIN_NAME), mainType, null, mainBody);
	}

	/**
	 * Classify ASTNodes in source files to 3 groups: target functions T and
	 * their contracts, callee functions C and their contracts and others. Note
	 * that T and C may overlap.
	 * 
	 * @return an instance of {@link SourceFilesWithContracts} which is the
	 *         result of the classification
	 * @throws SyntaxException
	 */
	private SourceFilesWithContracts extractContractedFunctionsFromSourceFileNodes(
			List<BlockItemNode> sourceFileNodes) throws SyntaxException {
		List<FunctionDefinitionNode> targets = new LinkedList<>();
		List<FunctionDeclarationNode> callees = new LinkedList<>();
		List<BlockItemNode> others = new LinkedList<>();
		boolean verifyAll = targetFunctionName == CIVLConstants.CONTRACT_CHECK_ALL;

		for (BlockItemNode child : sourceFileNodes) {
			if (child.nodeKind() == NodeKind.FUNCTION_DECLARATION
					|| child.nodeKind() == NodeKind.FUNCTION_DEFINITION) {
				FunctionDeclarationNode funcDecl = (FunctionDeclarationNode) child;

				// If the function declaration has definition, test if it is
				// the target function:
				if (funcDecl.isDefinition()) {
					boolean isTarget = funcDecl.getName()
							.equals(targetFunctionName);

					if (verifyAll || isTarget)
						if (funcDecl.getContract() != null)
							targets.add((FunctionDefinitionNode) funcDecl);
						else if (!verifyAll)
							throw new CIVLSyntaxException(
									"No contracts specified for the target function");
				}
				// If a function f declaration is contracted, replace its body
				// with abstraction based on its contract, and creates a mirror
				// function f_origin which contains its original body.
				if (funcDecl.getContract() != null) {
					TypeNode funcDeclTypeNode = funcDecl.getTypeNode();

					if (funcDeclTypeNode
							.kind() == TypeNode.TypeNodeKind.FUNCTION)
						callees.add(funcDecl);
				}
			} else
				others.add(child);
		}
		if (targets.isEmpty() && !verifyAll)
			throw new CIVLSyntaxException("Target function: "
					+ this.targetFunctionName + " not exist!");
		if (targets.isEmpty() && verifyAll)
			throw new CIVLSyntaxException(
					"No function will be verified because no function definition has a contract");
		return new SourceFilesWithContracts(targets, callees, others);
	}

	/**
	 * Transform all contracted functions with the given guides
	 * 
	 * @throws SyntaxException
	 */
	// private List<BlockItemNode> processContractedFunctions(
	// List<FunctionContractTransformGuide> targets,
	// List<FunctionContractTransformGuide> callees, boolean hasMPI)
	// throws SyntaxException {
	// List<BlockItemNode> results = new LinkedList<>();
	// FunctionDefinitionNode driver;
	//
	// // transform callees:
	// for (FunctionContractTransformGuide callee : callees) {
	// FunctionDefinitionNode defn = callee.function.getEntity()
	// .getDefinition();
	//
	// if (defn != null)
	// defn.remove();
	// // special handling for main function: remove it from the ASTS
	// // main is never be called:
	// if (defn.getName().equals(MAIN_NAME))
	// continue;
	// // replace body with abstraction based on contracts:
	// results.add(transformCalleeFunction(callee.function,
	// (FunctionTypeNode) callee.function.getTypeNode(), callee));
	// callee.function.remove();
	// }
	// // transform targets:
	// for (FunctionContractTransformGuide target : targets) {
	// // add driver function for verification:
	// driver = transformTargetFunction(
	// (FunctionDefinitionNode) target.function, target, hasMPI);
	//
	// // add a mirror function which contains its original body:
	// FunctionDefinitionNode defn = (FunctionDefinitionNode) target.function;
	// FunctionTypeNode funcType = defn.getTypeNode();
	// CompoundStatementNode defnBody = defn.getBody();
	//
	// defn.remove();
	// funcType.remove();
	// defnBody.remove();
	// defn = nodeFactory.newFunctionDefinitionNode(defn.getSource(),
	// identifier(target.getFunctionNameForOriginalBody()),
	// funcType, null, defnBody);
	// results.add(defn);
	// results.add(driver);
	// allDriverNames.add(driver.getName());
	//
	// }
	// return results;
	// }

	/**
	 * Transform all the contracted functions:
	 */
	private List<BlockItemNode> processContractedFunctions(
			SourceFilesWithContracts contractedFuncsInSrc)
			throws SyntaxException {
		LinkedList<BlockItemNode> results = new LinkedList<>();

		for (ContractedFunction callee : contractedFuncsInSrc.callees) {
			results.add(transformCalleeFunction(callee));
			callee.function.remove();
		}
		for (ContractedFunction target : contractedFuncsInSrc.targets) {
			FunctionDefinitionNode originalDefi = (FunctionDefinitionNode) target.function;
			FunctionDefinitionNode driverDefi = transformTargetFunction(target,
					contractedFuncsInSrc.others);

			originalDefi.remove();
			originalDefi = nodeFactory.newFunctionDefinitionNode(
					originalDefi.getSource(),
					identifier(originalDefi.getName() + originalSuffix),
					originalDefi.getTypeNode().copy(), null,
					originalDefi.getBody().copy());
			results.add(originalDefi);
			results.add(driverDefi);
			allDriverNames.add(driverDefi.getName());
		}
		return results;
	}

	/**
	 * Generates <code>
	 *   int $mpi_comm_rank, $mpi_comm_size;
	 *   $state * pre_state, * post_state;
	 *   _Bool _waitsfor_guard = $true;
	 *   $collate_state cs_pre_0, cs_pre_1, ..., cs_pre_n; // n = #(collective contract blocks)
	 *   
	 *   // translation(waitsfor)
	 *   // prefix(translation(pre-cond))
	 *   cs_pre_0 = $mpi_snapshot(comm0); cs_pre_1 = $mpi_snapshot(comm1); ...;
	 *   // translation(pre-cond)
	 *   // postfix(translation(pre-cond))
	 *   // translation(assigns)
	 *   T $result;
	 *   $havoc(&$result);
	 *   
	 *   $collate_state cs_post_0, cs_post_1, ..., cs_post_n;
	 *   
	 *   // prefix(translation(post-cond))
	 *   cs_post_0 = $mpi_snapshot(comm0); cs_post_1 = $mpi_snapshot(comm1); ...;
	 *   // translation(post-cond);
	 *   // postfix(translation(post-cond))
	 *   collate_free(cs_pre_0, cs_pre_1, ..., cs_pre_n);
	 *   collate_free(cs_post_0, cs_post_1, ..., cs_post_n);
	 * </code>, where
	 * <ol>
	 * <li><code>translation(waitsfor)</code> refers to the result of
	 * {@link #transformCalleeFunctionContractBlockWaitsfor}</li>
	 * 
	 * <li><code>translation(pre-cond), prefix(translation(pre-cond)) and postfix(translation(pre-cond))</code>
	 * refer to the result of
	 * {@link #transformCalleeFunctionContractBlockPrecond} , including the
	 * return value of output arguments.</li>
	 * 
	 * <li>Vice versa for
	 * <code>translation(post-cond), prefix(translation(post-cond)) and postfix(translation(post-cond))</code>
	 * and {@link #transformCalleeFunctionContractBlockPostcond}.</li>
	 * </ol>
	 */
	private FunctionDefinitionNode transformCalleeFunction(
			ContractedFunction contractedFun) throws SyntaxException {
		FunctionDeclarationNode funDecl = contractedFun.function;
		FunctionTypeNode origType = (FunctionTypeNode) funDecl.getTypeNode();
		// creates collates and translate contracts:
		CalleePreconditionTraslator preCondTranslator = new CalleePreconditionTraslator(
				nodeFactory, tokenFactory);
		CalleeFrameCondTranslator frameCondTranslator = new CalleeFrameCondTranslator(
				nodeFactory, tokenFactory);
		List<BlockItemNode> results = new LinkedList<>();
		Source source = funDecl.getSource();
		int collateCounter = 0;

		// declares $mpi_comm_rank and $mpi_comm_size:
		results.addAll(declareMpiCommRankAndSize());
		// transform pre-conditions:
		for (FunctionContractBlock cb : contractedFun.contracts) {
			ExpressionNode preCollateAccess = identifierExpression(
					COLLATE_PRE_STATE_NAME + collateCounter);
			ExpressionNode guard = transformCalleeFunctionContractBlockWaitsfor(
					cb, preCollateAccess);
			ExpressionNode preState;

			if (!cb.isSequentialBlock())
				preState = createCollateGetStateCall(preCollateAccess.copy(),
						preCollateAccess.getSource());
			else
				preState = identifierExpression(PRE_STATE_NAME);
			Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>> tranTriple = transformCalleeFunctionContractBlockPrecond(
					preCondTranslator, cb, preCollateAccess.copy(), preState,
					guard).get();
			StatementNode precondAssertion = tranTriple.first;
			List<BlockItemNode> prefix = tranTriple.second;
			List<BlockItemNode> postfix = tranTriple.third;

			results.addAll(prefix);
			if (!cb.isSequentialBlock()) {
				results.addAll(
						initializeMpiCommRankAndSize(cb.getMPIComm().copy()));
				results.add(createCollateStateInitializer(
						COLLATE_PRE_STATE_NAME + collateCounter,
						cb.getMPIComm().copy()));
			} else
				results.add(createSequentialStateInitializer(PRE_STATE_NAME,
						preState.getSource()));
			results.add(precondAssertion);
			results.addAll(postfix);
			collateCounter++;
		}
		// transform assigns:
		for (FunctionContractBlock cb : contractedFun.contracts)
			results.addAll(transformCalleeFunctionContractBlockAssigns(
					frameCondTranslator, cb));
		// transform $result:
		results.addAll(transformCalleeFunctionReturnValue(funDecl));

		// transform post-condition:
		CalleePostconditionTranslator postCondTranslator = new CalleePostconditionTranslator(
				nodeFactory, tokenFactory,
				preCondTranslator.getDatatypeToTempVarMap());

		collateCounter = 0;
		for (FunctionContractBlock cb : contractedFun.contracts) {
			ExpressionNode postCollateAccess = identifierExpression(
					COLLATE_POST_STATE_NAME + collateCounter);
			ExpressionNode preCollateAccess = identifierExpression(
					COLLATE_PRE_STATE_NAME + collateCounter);
			ExpressionNode guard = transformCalleeFunctionContractBlockWaitsfor(
					cb, postCollateAccess);
			ExpressionNode preState, postState;

			if (!cb.isSequentialBlock()) {
				preState = createCollateGetStateCall(preCollateAccess,
						preCollateAccess.getSource());
				postState = createCollateGetStateCall(postCollateAccess,
						postCollateAccess.getSource());
			} else {
				preState = identifierExpression(PRE_STATE_NAME);
				postState = preState;
			}

			Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>> tranTriple = transformCalleeFunctionContractBlockPostcond(
					postCondTranslator, cb, preCollateAccess.copy(),
					postCollateAccess.copy(), preState, postState, guard).get();
			ExpressionNode mpiComm = cb.getMPIComm();
			List<BlockItemNode> prefix = tranTriple.second;
			List<BlockItemNode> postfix = tranTriple.third;
			StatementNode postcondAssumption = tranTriple.first;

			results.addAll(prefix);
			if (!cb.isSequentialBlock()) {
				results.addAll(
						initializeMpiCommRankAndSize(cb.getMPIComm().copy()));
				results.add(createCollateStateInitializer(
						COLLATE_POST_STATE_NAME + collateCounter,
						cb.getMPIComm().copy()));
			}
			results.add(postcondAssumption);
			results.addAll(postfix);
			// unsnapsot collates:
			if (!cb.isSequentialBlock()) {
				results.add(createMPIUnsnapshotCall(mpiComm.copy(),
						preCollateAccess.copy()));
				results.add(createMPIUnsnapshotCall(mpiComm.copy(),
						postCollateAccess.copy()));
			}
			collateCounter++;
		}
		if (origType.getReturnType().getType().kind() != TypeKind.VOID)
			results.add(nodeFactory.newReturnNode(funDecl.getSource(),
					identifierExpression("$result")));
		// return the new function definition:
		return nodeFactory.newFunctionDefinitionNode(source,
				funDecl.getIdentifier().copy(),
				(FunctionTypeNode) funDecl.getTypeNode().copy(), null,
				nodeFactory.newCompoundStatementNode(source, results));
	}

	/**
	 * @return an expression encoding the waitsfor guard of the function
	 *         contract block. The guard expression is for all behaviors in the
	 *         contract block.
	 */
	private ExpressionNode transformCalleeFunctionContractBlockWaitsfor(
			FunctionContractBlock contractBlk, ExpressionNode collateAccess) {
		CalleeWaitsforTranslator translator = new CalleeWaitsforTranslator(
				nodeFactory, tokenFactory);
		List<ExpressionNode> results = new LinkedList<>();

		for (ContractBehavior cb : contractBlk.getBehaviorsInBlock())
			results.add(translator.translateWaitsforExpressions(
					cb.getConditions(), collateAccess, cb.getWaitsfors()));
		return conjunct(results);
	}

	/**
	 * Translates assigns clauses in a function contract block.
	 */
	private List<BlockItemNode> transformCalleeFunctionContractBlockAssigns(
			CalleeFrameCondTranslator translator,
			FunctionContractBlock contractBlk) {
		List<BlockItemNode> results = new LinkedList<>();

		for (ContractBehavior cb : contractBlk.getBehaviorsInBlock()) {
			results.addAll(translator.translateAssigns(cb.getConditions(),
					cb.getAssignsArgs()));
		}
		return results;
	}

	/**
	 * Generates <code>
	 *   T $result;
	 *   $havoc(&$result);
	 * </code> iff the function return type <code>T</code> is not void.
	 */
	private List<BlockItemNode> transformCalleeFunctionReturnValue(
			FunctionDeclarationNode funDecl) {
		FunctionTypeNode funcTypeNode = (FunctionTypeNode) funDecl
				.getTypeNode();
		TypeNode returnTypeNode = funcTypeNode.getReturnType();
		List<BlockItemNode> results = new LinkedList<>();

		if (returnTypeNode.getType().kind() == Type.TypeKind.VOID)
			return results;

		VariableDeclarationNode resultDecl = nodeFactory
				.newVariableDeclarationNode(returnTypeNode.getSource(),
						identifier("$result"), returnTypeNode.copy());
		StatementNode havocStmt = nodeFactory.newExpressionStatementNode(
				createHavocCall(identifierExpression("$result"), nodeFactory));

		results.add(resultDecl);
		results.add(havocStmt);
		return results;
	}

	/**
	 * Generates: <code>
	 * {
	 *    MPI_Comm_Rank(comm, &$mpi_comm_rank);
	 *    MPI_Comm_Size(comm, &$mpi_comm_size);
	 *    _pre = $collate_get_state(_collate_pre); 
	 *    $assert(local-precond);
	 *    $when($waitsforGuard)
	 *      $assert($value_at(*cs, $mpi_comm_rank, col-precond));
	 * }
	 * </code>
	 * 
	 * @throws SyntaxException
	 */
	private Supplier<Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>>> transformCalleeFunctionContractBlockPrecond(
			CalleePreconditionTraslator preCondTranslator,
			FunctionContractBlock contractBlk, ExpressionNode collateAccess,
			ExpressionNode preState, ExpressionNode waitsforGuard)
			throws SyntaxException {
		List<Supplier<ContractClauseTranslation>> transSuppliers = new LinkedList<>();

		for (ContractBehavior behaviorBlk : contractBlk.getBehaviorsInBlock()) {
			transSuppliers.add(preCondTranslator.translatePrecondition(
					behaviorBlk.getConditions(), behaviorBlk.getRequires(),
					preState, preState.copy(),
					contractBlk.isSequentialBlock()));
		}
		return new Supplier<Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>>>() {
			@Override
			public Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>> get() {
				List<BlockItemNode> result = new LinkedList<>();
				List<BlockItemNode> prefix = new LinkedList<>();
				List<BlockItemNode> postfix = new LinkedList<>();
				List<BlockItemNode> assertionStmts = new LinkedList<>();
				Source combinedSource = null;
				StatementNode theAssert;

				// transform pre-cond assertion:
				for (Supplier<ContractClauseTranslation> tranSupplier : transSuppliers) {
					ContractClauseTranslation tran = tranSupplier.get();

					prefix.addAll(tran.prefix());
					postfix.addAll(tran.postfix());
					theAssert = createAssertion(conjunct(tran.predicates()));
					if (!tran.conditions().isEmpty()) {
						ExpressionNode condition = conjunct(tran.conditions());

						theAssert = nodeFactory.newIfNode(condition.getSource(),
								condition, theAssert);
					}
					assertionStmts.add(theAssert);
					combinedSource = combinedSource == null
							? theAssert.getSource()
							: tokenFactory.join(combinedSource,
									theAssert.getSource());
				}
				// insert pre-cond assertion:
				theAssert = nodeFactory.newCompoundStatementNode(combinedSource,
						assertionStmts);
				if (!contractBlk.isSequentialBlock())
					theAssert = nodeFactory.newWhenNode(combinedSource,
							waitsforGuard, theAssert);
				result.add(theAssert);
				return new Triple<>(nodeFactory.newCompoundStatementNode(
						combinedSource, result), prefix, postfix);
			}
		};
	}

	/**
	 * Generates <code>
	 * {
	 *    MPI_Comm_Rank(comm, &$mpi_comm_rank);
	 *    MPI_Comm_Size(comm, &$mpi_comm_size);
	 *    _pre = $collate_get_state(_collate_pre); 
	 *    _post = $collate_get_state(_collate_post); 
	 *    $assume(local-postcond);
	 *    $when($waitsforGuard)
	 *      $assume($value_at(*cs, $mpi_comm_rank, col-precond));
	 * }
	 *  </code>
	 * 
	 * @throws SyntaxException
	 */
	private Supplier<Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>>> transformCalleeFunctionContractBlockPostcond(
			CalleePostconditionTranslator translator,
			FunctionContractBlock contractBlk, ExpressionNode collatePre,
			ExpressionNode collatePost, ExpressionNode preState,
			ExpressionNode postState, ExpressionNode waitsforGuard)
			throws SyntaxException {
		List<Supplier<ContractClauseTranslation>> transSuppliers = new LinkedList<>();

		for (ContractBehavior cb : contractBlk.getBehaviorsInBlock())
			transSuppliers.add(translator.translatePostcondition(
					cb.getConditions(), cb.getEnsures(), preState.copy(),
					postState.copy(), contractBlk.isSequentialBlock()));
		return new Supplier<Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>>>() {
			@Override
			public Triple<StatementNode, List<BlockItemNode>, List<BlockItemNode>> get() {
				List<BlockItemNode> result = new LinkedList<>();
				List<BlockItemNode> prefix = new LinkedList<>();
				List<BlockItemNode> postfix = new LinkedList<>();

				// transform post-cond assume
				List<BlockItemNode> assumeStmts = new LinkedList<>();
				Source combinedSource = null;
				StatementNode theAssume;

				for (Supplier<ContractClauseTranslation> tranSupplier : transSuppliers) {
					ContractClauseTranslation tran = tranSupplier.get();

					prefix.addAll(tran.prefix());
					postfix.addAll(tran.postfix());
					theAssume = assumeNode(conjunct(tran.predicates()));
					if (!tran.conditions().isEmpty()) {
						ExpressionNode condition = conjunct(tran.conditions());

						theAssume = nodeFactory.newIfNode(condition.getSource(),
								condition, theAssume);
					}
					assumeStmts.add(theAssume);
					combinedSource = combinedSource == null
							? theAssume.getSource()
							: tokenFactory.join(combinedSource,
									theAssume.getSource());
				}
				// insert $assumes(post-cond):
				theAssume = nodeFactory.newCompoundStatementNode(combinedSource,
						assumeStmts);
				if (!contractBlk.isSequentialBlock())
					theAssume = nodeFactory.newWhenNode(combinedSource,
							waitsforGuard, theAssume);
				result.add(theAssume);
				return new Triple<>(nodeFactory.newCompoundStatementNode(
						combinedSource, result), prefix, postfix);
			}
		};
	}

	/**
	 * <code>
	 *   int $mpi_comm_rank;
	 *   int $mpi_comm_size;
	 * </code>
	 */
	private List<BlockItemNode> declareMpiCommRankAndSize() {
		List<BlockItemNode> results = new LinkedList<>();

		VariableDeclarationNode mpiCommRankDecl = nodeFactory
				.newVariableDeclarationNode(mpiCommRankSource,
						identifier(MPI_COMM_RANK_CONST), intTypeNode.copy());
		VariableDeclarationNode mpiCommSizeDecl = nodeFactory
				.newVariableDeclarationNode(mpiCommSizeSource,
						identifier(MPI_COMM_SIZE_CONST), intTypeNode.copy());
		results.add(mpiCommSizeDecl);
		results.add(mpiCommRankDecl);
		return results;
	}

	/**
	 * <code>
	 *    MPI_Comm_Rank(comm, &$mpi_comm_rank);
	 *    MPI_Comm_Size(comm, &$mpi_comm_size);
	 * </code>
	 */
	private List<BlockItemNode> initializeMpiCommRankAndSize(
			ExpressionNode mpiComm) {
		ExpressionNode mpiCommRank = identifierExpression(MPI_COMM_RANK_CONST);
		ExpressionNode mpiCommSize = identifierExpression(MPI_COMM_SIZE_CONST);
		List<BlockItemNode> result = new LinkedList<>();

		// initializes the communicator with MPI_COMM_WORLD:
		result.add(nodeFactory.newExpressionStatementNode(nodeFactory
				.newOperatorNode(mpiComm.getSource(), Operator.ASSIGN, mpiComm,
						identifierExpression(MPI_COMM_WORLD_CONST))));
		// initializes $mpi_comm_rank, $mpi_comm_size:
		result.add(nodeFactory.newExpressionStatementNode(
				mpiCommRankCall(mpiComm.copy(), mpiCommRank)));
		result.add(nodeFactory.newExpressionStatementNode(
				mpiCommSizeCall(mpiComm.copy(), mpiCommSize)));
		return result;
	}

	/**
	 * 
	 * @param source
	 * @return an array with three elements: {ptr2PreStateDecl,
	 *         ptr2PostStateDecl, waitsforGuardVarDecl}
	 */
	private VariableDeclarationNode[] declarePtr2PrePostStates(Source source) {
		// create a pointer to pre-state:
		VariableDeclarationNode ptr2PreStateDecl = nodeFactory
				.newVariableDeclarationNode(source, identifier(PRE_STATE_NAME),
						nodeFactory.newPointerTypeNode(source,
								nodeFactory.newStateTypeNode(source)));
		// create a pointer to post-state:
		VariableDeclarationNode ptr2PostStateDecl = nodeFactory
				.newVariableDeclarationNode(source, identifier(POST_STATE_NAME),
						nodeFactory.newPointerTypeNode(source,
								nodeFactory.newStateTypeNode(source)));
		return new VariableDeclarationNode[]{ptr2PreStateDecl,
				ptr2PostStateDecl};
	}

	/**
	 * <p>
	 * <b>Summary:</b> Wraps the target function with a harness function. The
	 * harness is created based on the contracts of the target function.
	 * </p>
	 * <p>
	 * <b>Details:</b> The contracted function will be transformed into the
	 * following pattern:
	 * <ul>
	 * <b> driver( ... ) { </b>
	 * <li>1 localContractStatements;</li>
	 * <li>2 $mpi_comm_size and $mpi_comm_rank decl;</li>
	 * <li>3 MPI_Comm_size(comm, &$mpi_comm_size) && MPI_Comm_rank( ... );</li>
	 * <li>4 take-snapshot;</li>
	 * <li>5 collectiveContractStatements</li>
	 * <li>6 enters</li>
	 * <li>7 $result declaration && call target function</li>
	 * <li>8 entered check</li>
	 * <li>9 localContractStatements;</li>
	 * <li>10 take-snapshot;</li>
	 * <li>11 collectiveContractStatements</li> <b>}</b>
	 * </p>
	 * <code>
	 * driver( ... ) {
	 *    int $mpi_comm_rank, $mpi_comm_size;
	 *    $state * pre_state, * post_state;
	 *    _Bool waitsforGuard = $true;
	 *    $collate_state cs_pre_0, cs_pre_1, ..., cs_pre_n; // n = #(collective contract blocks)
	 *    
	 *    // formal parameter declarations;
	 *    // havoc formal parameters and global variables;
	 *    // prefix(translation(pre-cond));
	 *    cs_pre_0 = $mpi_snapshot(comm0), cs_pre_1 = $mpi_snapshot(comm1), ...;
	 *    // translation(pre-cond);
	 *    // postfix(translation(pre-cond));
	 *     
	 *    $write_set_push(); 
	 *    $result = original-call;
	 *    $mem ws = $write_set_pop();
	 *    ws = $mem_less(ws, &$result);
	 *    // translation(assigns);
	 *    
	 *    // prefix(translation(post-cond));
	 *    cs_post_0 = $mpi_snapshot(comm0), cs_post_1 = $mpi_snapshot(comm1); ...;
	 *    // translation(post-cond);
	 *    // postfix(translation(post-cond));
	 *    
	 *    $mpi_unsnapshot(cs_pre_0), $mpi_unsnapshot(cs_pre_1), ...;
	 *    $mpi_unsnapshot(cs_post_0), $mpi_unsnapshot(cs_post_1), ...;
	 * }
	 * </code>
	 * 
	 * @param funcDefi
	 *            The definition of the target function
	 * @return A new driver function for the target function.
	 * @throws SyntaxException
	 */
	private FunctionDefinitionNode transformTargetFunction(
			ContractedFunction contractedFun,
			List<BlockItemNode> fileScopeNodes) throws SyntaxException {
		FunctionDefinitionNode funcDefi = (FunctionDefinitionNode) contractedFun.function;
		List<BlockItemNode> results = new LinkedList<>();
		FunctionTypeNode funcTypeNode = funcDefi.getTypeNode();
		List<BlockItemNode> mpiInPlaceNondetermChoices = new LinkedList<>();

		results.addAll(declareMpiCommRankAndSize());
		results.addAll(createVariableDeclsAndInitsForDriver(funcTypeNode,
				mpiInPlaceNondetermChoices));
		results.addAll(havocForGlobalVariables(fileScopeNodes));

		// creates collates and translate contracts:
		TargetPreconditionTranslator preCondTranslator = new TargetPreconditionTranslator(
				nodeFactory, tokenFactory);
		TargetFrameCondTranslator frameCondTranslator = new TargetFrameCondTranslator(
				nodeFactory, tokenFactory);
		int collateCounter = 0;

		results.addAll(mpiInPlaceNondetermChoices);
		for (FunctionContractBlock cb : contractedFun.contracts) {
			List<BlockItemNode> prefix = new LinkedList<>();
			List<BlockItemNode> postfix = new LinkedList<>();
			ExpressionNode preCollateAccess = identifierExpression(
					COLLATE_PRE_STATE_NAME + collateCounter);
			ExpressionNode preState;

			if (!cb.isSequentialBlock())
				preState = createCollateGetStateCall(preCollateAccess,
						preCollateAccess.getSource());
			else
				preState = identifierExpression(PRE_STATE_NAME);

			StatementNode assumption = transformTargetFunctionContractBlockPrecond(
					preCondTranslator, cb, preCollateAccess.copy(), preState,
					prefix, postfix);

			if (!cb.isSequentialBlock()) {
				results.addAll(
						initializeMpiCommRankAndSize(cb.getMPIComm().copy()));
			}
			results.addAll(prefix);
			results.addAll(
					nondetermChoiceStmt(preCondTranslator.nondetermChoices()));
			if (!cb.isSequentialBlock())
				results.add(createCollateStateInitializer(
						COLLATE_PRE_STATE_NAME + collateCounter,
						cb.getMPIComm().copy()));
			else
				results.add(createSequentialStateInitializer(PRE_STATE_NAME,
						preState.getSource()));
			results.add(assumption);
			results.addAll(postfix);
			collateCounter++;
		}
		// TODO: skip frame-condition for now

		// place the original call:
		ExpressionNode fun = nodeFactory.newIdentifierExpressionNode(
				funcDefi.getSource(),
				nodeFactory.newIdentifierNode(funcDefi.getSource(),
						targetFunctionName + originalSuffix));
		List<ExpressionNode> args = new LinkedList<>();
		ExpressionNode originalCall;

		for (VariableDeclarationNode formal : funcTypeNode.getParameters())
			args.add(nodeFactory.newIdentifierExpressionNode(formal.getSource(),
					formal.getIdentifier().copy()));
		originalCall = nodeFactory.newFunctionCallNode(funcDefi.getSource(),
				fun, args, null);
		if (funcTypeNode.getReturnType().getType().kind() != TypeKind.VOID) {
			results.add(nodeFactory.newVariableDeclarationNode(
					funcTypeNode.getSource(), identifier("$result"),
					funcTypeNode.getReturnType().copy()));
			originalCall = nodeFactory.newOperatorNode(originalCall.getSource(),
					Operator.ASSIGN, identifierExpression("$result"),
					originalCall);
		}
		results.add(nodeFactory.newExpressionStatementNode(originalCall));

		TargetPostconditionTranslator postCondTranslator = new TargetPostconditionTranslator(
				nodeFactory, tokenFactory,
				preCondTranslator.datatypeToTempVarMap());

		collateCounter = 0;
		for (FunctionContractBlock cb : contractedFun.contracts) {
			List<BlockItemNode> prefix = new LinkedList<>();
			List<BlockItemNode> postfix = new LinkedList<>();
			ExpressionNode preCollateAccess = identifierExpression(
					COLLATE_PRE_STATE_NAME + collateCounter);
			ExpressionNode postCollateAccess = identifierExpression(
					COLLATE_POST_STATE_NAME + collateCounter);
			ExpressionNode preState, postState;

			if (!cb.isSequentialBlock()) {
				preState = createCollateGetStateCall(preCollateAccess,
						preCollateAccess.getSource());
				postState = createCollateGetStateCall(postCollateAccess,
						postCollateAccess.getSource());
			} else {
				preState = identifierExpression(PRE_STATE_NAME);
				postState = preState;
			}

			StatementNode assertion = transformTargetFunctionContractBlockPostcond(
					postCondTranslator, cb, preCollateAccess.copy(),
					postCollateAccess.copy(), preState, postState, prefix,
					postfix);

			if (!cb.isSequentialBlock())
				results.addAll(
						initializeMpiCommRankAndSize(cb.getMPIComm().copy()));
			results.addAll(prefix);
			if (!cb.isSequentialBlock())
				results.add(createCollateStateInitializer(
						COLLATE_POST_STATE_NAME + collateCounter,
						cb.getMPIComm().copy()));
			results.add(assertion);
			results.addAll(postfix);
			if (!cb.isSequentialBlock()) {
				results.add(createMPIUnsnapshotCall(cb.getMPIComm().copy(),
						preCollateAccess.copy()));
				results.add(createMPIUnsnapshotCall(cb.getMPIComm().copy(),
						postCollateAccess.copy()));
			}
			collateCounter++;
		}
		FunctionTypeNode origType = funcDefi.getTypeNode();
		Source typeSource = origType.getSource();
		FunctionTypeNode driverType = nodeFactory
				.newFunctionTypeNode(typeSource,
						origType.getReturnType().copy(),
						nodeFactory.newSequenceNode(typeSource,
								"driver formal parameters", new LinkedList<>()),
						true);

		if (origType.getReturnType().getType().kind() != TypeKind.VOID)
			results.add(nodeFactory.newReturnNode(funcDefi.getSource(),
					identifierExpression("$result")));
		return nodeFactory.newFunctionDefinitionNode(funcDefi.getSource(),
				identifier(funcDefi.getName() + driverSuffix), driverType, null,
				nodeFactory.newCompoundStatementNode(funcDefi.getSource(),
						results));
	}

	/**
	 * Generates<code>
	 *   MPI_Comm_rank(comm, &$mpi_comm_rank);
	 *   MPI_Comm_size(comm, &$mpi_comm_size);
	 *   _pre_state = get_collate_state(collate);
	 *   $assume(local-precond);
	 *   $when(collate_complete)
	 *     $assume(precond);  
	 * </code>
	 * 
	 * @throws SyntaxException
	 */
	private StatementNode transformTargetFunctionContractBlockPrecond(
			TargetPreconditionTranslator preCondTranslator,
			FunctionContractBlock contractBlk, ExpressionNode collateAccess,
			ExpressionNode preState, List<BlockItemNode> prefix,
			List<BlockItemNode> postfix) throws SyntaxException {
		List<BlockItemNode> results = new LinkedList<>();
		Source source = null;

		for (ContractBehavior cb : contractBlk.getBehaviorsInBlock()) {
			ContractClauseTranslation tran = preCondTranslator
					.translatePrecond4Target(cb.getConditions(),
							cb.getRequires(), preState, preState.copy(),
							contractBlk.isSequentialBlock());

			prefix.addAll(tran.prefix());
			postfix.addAll(tran.postfix());

			ExpressionNode pred = conjunct(tran.predicates());
			StatementNode assume = assumeNode(pred);

			if (!tran.conditions().isEmpty()) {
				ExpressionNode cond = conjunct(tran.conditions());

				assume = nodeFactory.newIfNode(cond.getSource(), cond, assume);
			}
			if (!contractBlk.isSequentialBlock())
				assume = nodeFactory.newWhenNode(assume.getSource(),
						createCollateCompleteCall(collateAccess.copy()),
						assume);
			results.add(assume);
			source = source == null
					? pred.getSource()
					: tokenFactory.join(source, pred.getSource());
		}

		return nodeFactory.newCompoundStatementNode(source, results);
	}

	/**
	 * Generates <code>
	 *   MPI_Comm_rank(comm, &$mpi_comm_rank);
	 *   MPI_Comm_size(comm, &$mpi_comm_size);
	 *   _pre_state_ = get_collate_state(_pre_collate_);
	 *   _post_state_ = get_collate_state(_post_collate_);
	 *   $assert(local-postcond);
	 *   MPI_Barrier(comm);
	 *   $assert(precond);  
	 *   MPI_Barrier(comm);
	 * </code>
	 * 
	 * @throws SyntaxException
	 */
	private StatementNode transformTargetFunctionContractBlockPostcond(
			TargetPostconditionTranslator postCondTranslator,
			FunctionContractBlock contractBlk, ExpressionNode preCollateAccess,
			ExpressionNode postCollateAccess, ExpressionNode preState,
			ExpressionNode postState, List<BlockItemNode> prefix,
			List<BlockItemNode> postfix) throws SyntaxException {
		List<BlockItemNode> results = new LinkedList<>();
		Source source = null;

		if (!contractBlk.isSequentialBlock())
			results.add(createMPIBarrierCall(contractBlk.getMPIComm().copy()));
		for (ContractBehavior cb : contractBlk.getBehaviorsInBlock()) {
			ContractClauseTranslation tran = postCondTranslator
					.translatePostcond4Target(cb.getConditions(),
							cb.getEnsures(), preState.copy(), postState.copy(),
							contractBlk.isSequentialBlock());

			prefix.addAll(tran.prefix());
			postfix.addAll(tran.postfix());

			StatementNode assertion = createAssertion(
					conjunct(tran.predicates()));

			if (!tran.conditions().isEmpty()) {
				ExpressionNode condition = conjunct(tran.conditions());

				assertion = nodeFactory.newIfNode(condition.getSource(),
						condition, assertion);
			}
			results.add(assertion);
			source = source == null
					? assertion.getSource()
					: tokenFactory.join(source, assertion.getSource());
		}
		if (!contractBlk.isSequentialBlock())
			results.add(createMPIBarrierCall(contractBlk.getMPIComm().copy()));
		return nodeFactory.newCompoundStatementNode(source, results);
	}

	/*
	 * ************************* Utility methods ****************************
	 */
	private StatementNode createMPIBarrierCall(ExpressionNode mpiComm) {
		ExpressionNode fun = identifierExpression(MPI_BARRIER_FUN);
		List<ExpressionNode> args = new LinkedList<>();

		args.add(mpiComm.copy());
		return nodeFactory.newExpressionStatementNode(nodeFactory
				.newFunctionCallNode(mpiComm.getSource(), fun, args, null));
	}

	/**
	 * Creates an <code>MPI_Init(NULL, NULL);</code> call statememt node.
	 * 
	 * @return The created statement node
	 * @throws SyntaxException
	 */
	private StatementNode createMPIInitCall() throws SyntaxException {
		IntegerConstantNode zero = nodeFactory.newIntegerConstantNode(
				newSource("0", CivlcTokenConstant.INTEGER_CONSTANT), "0");
		TypeNode ptr2Void = nodeFactory.newPointerTypeNode(
				newSource("(void *)", CivlcTokenConstant.TYPE),
				nodeFactory.newVoidTypeNode(
						newSource("void", CivlcTokenConstant.TYPE)));
		CastNode nullPtr = nodeFactory.newCastNode(
				newSource("(void *)0", CivlcTokenConstant.CAST), ptr2Void,
				zero);
		return nodeFactory
				.newExpressionStatementNode(nodeFactory.newFunctionCallNode(
						newSource("MPI_Init(NULL, NULL);",
								CivlcTokenConstant.CALL),
						identifierExpression(MPI_INIT_CALL),
						Arrays.asList(nullPtr, nullPtr.copy()), null));
	}

	/**
	 * Creates an <code>createMPIFinalizeCall();</code> call statement node.
	 * 
	 * @return The created statement node
	 */
	private StatementNode createMPIFinalizeCall() {
		return nodeFactory
				.newExpressionStatementNode(nodeFactory.newFunctionCallNode(
						newSource("MPI_Finalize();", CivlcTokenConstant.CALL),
						identifierExpression(MPI_FINALIZE_CALL),
						Arrays.asList(), null));
	}

	/**
	 * <p>
	 * <b>Summary: </b> Creates an $havoc function call:<code>
	 * $mpi_snapshot(&var);</code>
	 * </p>
	 * 
	 * @param var
	 *            An {@link ExpressionNode} representing an variable.
	 * @return The created $havoc call expression node.
	 */
	private ExpressionNode createHavocCall(ExpressionNode var,
			NodeFactory nodeFactory) {
		Source source = var.getSource();
		ExpressionNode callIdentifier = identifierExpression(source,
				MPIContractUtilities.HAVOC);
		ExpressionNode addressOfVar = nodeFactory.newOperatorNode(
				var.getSource(), Operator.ADDRESSOF, var.copy());
		FunctionCallNode call = nodeFactory.newFunctionCallNode(source,
				callIdentifier, Arrays.asList(addressOfVar), null);

		return call;
	}

	/**
	 * <p>
	 * <b>Summary: </b> Transform the parameters of the target function into a
	 * sequence of variable declarations. All of them will be initialized with
	 * arbitrary values.
	 * </p>
	 * 
	 * <p>
	 * Specially, for pointer type formal parameters, we add non-deterministic
	 * choices for whether it is an MPI_IN_PLACE or not
	 * </p>
	 * 
	 * @param targetFuncType
	 *            A {@link FunctionTypeNode} which represents the function type
	 *            of the target function.
	 * @return
	 */
	private List<BlockItemNode> createVariableDeclsAndInitsForDriver(
			FunctionTypeNode targetFuncType,
			List<BlockItemNode> nondetermChoices) {
		SequenceNode<VariableDeclarationNode> formals = targetFuncType
				.getParameters();
		List<BlockItemNode> results = new LinkedList<>();

		// create an variable for each formal parameter
		for (VariableDeclarationNode varDecl : formals) {
			VariableDeclarationNode actualDecl;

			// TODO: need a better way: currently for MPI_Comm type
			// parameters, it is always replaced with MPI_COMM_WORLD:
			// if (varDecl.getTypeNode().getType()
			// .kind() == TypeKind.STRUCTURE_OR_UNION) {
			// StructureOrUnionType structType = (StructureOrUnionType) varDecl
			// .getTypeNode().getType();
			//
			// if (structType.getName().equals(MPI_COMM_TYPE)) {
			// results.add(nodeFactory.newVariableDeclarationNode(
			// varDecl.getSource(), identifier(varDecl.getName()),
			// varDecl.getTypeNode().copy(), identifierExpression(
			// MPIContractUtilities.MPI_COMM_WORLD)));
			// continue;
			// }
			// }

			actualDecl = varDecl.copy();

			StatementNode havoc;

			results.add(actualDecl);
			// $havoc for the actual parameter declaration:
			havoc = nodeFactory.newExpressionStatementNode(createHavocCall(
					identifierExpression(actualDecl.getName()), nodeFactory));
			results.add(havoc);
		}
		return results;
	}

	/**
	 * Find out variable declarations in the given list of block item nodes, do
	 * $havoc for them.
	 * 
	 * @param root
	 * @return
	 */
	private List<BlockItemNode> havocForGlobalVariables(
			List<BlockItemNode> root) {
		List<BlockItemNode> havocs = new LinkedList<>();

		for (BlockItemNode item : root) {
			if (item.nodeKind() == NodeKind.VARIABLE_DECLARATION) {
				VariableDeclarationNode decl = ((VariableDeclarationNode) item);
				String name = ((VariableDeclarationNode) item).getName();

				globalVarDecls.add(decl);
				havocs.add(
						nodeFactory.newExpressionStatementNode(createHavocCall(
								identifierExpression(name), nodeFactory)));
			}
		}
		return havocs;
	}

	/**
	 * Return a list of {@link BlockItemNode}s which comes from source code in
	 * "source file"s. Here a "source file" is a file with ".c, .cvl, .h" or
	 * ".cvh" suffix and NOT under the system include path.
	 * 
	 * @param root
	 *            the root node of an AST
	 * @param srcFileNodes
	 *            OUTPUT. all the block item nodes in the given AST that come
	 *            from "source files"
	 * @return true iff there is at least one node comes from "mpi.h"
	 */
	private boolean findMPIAndNodesofSourceFiles(
			SequenceNode<BlockItemNode> root,
			List<BlockItemNode> srcFileNodes) {
		Path civlIncludePath = CIVLConstants.CIVL_INCLUDE_PATH.toPath();
		Path frontendIncludePath = CIVLConstants.FRONT_END_INCLUDE_PATH
				.toPath();
		boolean hasMPI = false;

		for (BlockItemNode node : root) {
			if (node == null)
				continue;

			Path sourceFilePath = node.getSource().getFirstToken()
					.getSourceFile().getFile().toPath();
			String sourceFileName = sourceFilePath.getFileName().toString();

			if (sourceFilePath.startsWith(civlIncludePath)
					|| sourceFilePath.startsWith(frontendIncludePath)) {
				if (!hasMPI && sourceFileName.equals("mpi.h"))
					hasMPI = true;
				continue;
			}
			srcFileNodes.add(node);

			assert sourceFileName.endsWith(".c")
					|| sourceFileName.endsWith(".cvl")
					|| sourceFileName.endsWith(".h")
					|| sourceFileName.endsWith(".cvh");
		}
		return hasMPI;
	}

	/**
	 * <p>
	 * This class is a data structure represents the source files (excludes
	 * CIVL-C libraries). The ASTNodes of source files are organized in three
	 * groups:
	 * <ul>
	 * <li>The target function definitions and their contracts,
	 * {@link SourceFilesWithContracts#targets}</li>
	 * <li>The first encountered contracted callee function declarations and
	 * their contracts, {@link SourceFilesWithContracts#callees}</li>
	 * <li>The rest of the ASTNodes in the source files,
	 * {@link SourceFilesWithContracts#others}</li>
	 * </ul>
	 * </p>
	 * 
	 * note that group 1 and group 2 may have overlaps. Group 3 is disjoint from
	 * group 1 and group 2.
	 * 
	 * TODO: think about conjunctions of contracts over multiple declarations of
	 * the same function
	 * 
	 * @author xxxx
	 */
	class SourceFilesWithContracts {
		/**
		 * A contracted function data structure, including a set of
		 * {@link FunctionContractBlock} and a {@link FunctionDeclarationNode}
		 * of the function.
		 * 
		 * @author xxxx
		 */
		class ContractedFunction {

			final List<FunctionContractBlock> contracts;

			final FunctionDeclarationNode function;

			ContractedFunction(FunctionDeclarationNode function) {
				this.function = function;
				this.contracts = FunctionContractBlock
						.parseContract(function.getContract(), nodeFactory);
			}
		}

		/**
		 * the target function definitions and its contracts
		 */
		final List<ContractedFunction> targets;

		/**
		 * the first encountered contracted callee function declarations and
		 * their contracts
		 */
		final List<ContractedFunction> callees;

		/**
		 * the rest of the ASTNodes in the source files
		 */
		final List<BlockItemNode> others;

		SourceFilesWithContracts(List<FunctionDefinitionNode> targets,
				List<FunctionDeclarationNode> callees,
				List<BlockItemNode> others) {
			this.targets = new LinkedList<>();
			for (FunctionDefinitionNode target : targets)
				this.targets.add(new ContractedFunction(target));
			this.callees = new LinkedList<>();
			for (FunctionDeclarationNode callee : callees)
				this.callees.add(new ContractedFunction(callee));
			this.others = others;
		}
	}

	/**
	 * If the given expression is a cast-expression: <code>(T) expr</code>,
	 * return an expression representing <code>expr</code>, otherwise no-op.
	 * 
	 * @param expression
	 *            An instance of {@link ExpressionNode}
	 * @return An expression who is the argument of a cast expression iff the
	 *         input is a cast expression, otherwise returns input itself.(i.e.
	 *         no-op).
	 */
	static ExpressionNode decast(ExpressionNode expression) {
		if (expression.expressionKind() == ExpressionKind.CAST) {
			CastNode castNode = (CastNode) expression;

			return castNode.getArgument();
		}
		return expression;
	}

	private ExpressionNode mpiCommRankCall(ExpressionNode comm,
			ExpressionNode rank) {
		ExpressionNode mpiCommRankFun = identifierExpression(
				MPI_CONTRACT_CONSTS.MPI_COMM_RANK_FUN);
		List<ExpressionNode> args = new LinkedList<>();

		args.add(comm);
		args.add(nodeFactory.newOperatorNode(mpiCommRankSource,
				Operator.ADDRESSOF, rank));
		return nodeFactory.newFunctionCallNode(mpiCommRankSource,
				mpiCommRankFun, args, null);
	}

	private ExpressionNode mpiCommSizeCall(ExpressionNode comm,
			ExpressionNode size) {
		ExpressionNode mpiCommRankFun = identifierExpression(
				MPI_CONTRACT_CONSTS.MPI_COMM_SIZE_FUN);
		List<ExpressionNode> args = new LinkedList<>();

		args.add(comm);
		args.add(nodeFactory.newOperatorNode(mpiCommRankSource,
				Operator.ADDRESSOF, size));
		return nodeFactory.newFunctionCallNode(mpiCommRankSource,
				mpiCommRankFun, args, null);
	}

	/**
	 * @return a function call expression:
	 *         <code>$collate_get_state(colStateRef) </code>
	 */
	private ExpressionNode createCollateGetStateCall(ExpressionNode colStateRef,
			Source source) {
		return nodeFactory.newFunctionCallNode(source,
				identifierExpression(source,
						MPI_CONTRACT_CONSTS.COLLATE_GET_STATE_FUN),
				Arrays.asList(colStateRef), null);
	}

	private VariableDeclarationNode createCollateStateInitializer(
			String collateStateName, ExpressionNode mpiComm) {
		Source source = mpiComm.getSource();
		InitializerNode initializer = createMPISnapshotCall(mpiComm);
		TypeNode collateStateTypeName = nodeFactory
				.newTypedefNameNode(nodeFactory.newIdentifierNode(source,
						MPI_CONTRACT_CONSTS.COLLATE_STATE_TYPE), null);
		IdentifierNode varIdent = nodeFactory.newIdentifierNode(source,
				collateStateName);

		return nodeFactory.newVariableDeclarationNode(source, varIdent,
				collateStateTypeName, initializer);
	}

	private VariableDeclarationNode createSequentialStateInitializer(
			String stateName, Source source) {
		ExpressionNode getStateFun = identifierExpression(
				MPI_CONTRACT_CONSTS.GET_STATE_FUN);
		ExpressionNode getStateCall = nodeFactory.newFunctionCallNode(source,
				getStateFun, new LinkedList<>(), null);
		VariableDeclarationNode stateDecl = nodeFactory
				.newVariableDeclarationNode(source, identifier(stateName),
						nodeFactory.newStateTypeNode(source), getStateCall);

		return stateDecl;
	}

	private ExpressionNode createMPISnapshotCall(ExpressionNode mpiComm) {
		Source source = mpiComm.getSource();
		ExpressionNode callIdentifier = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source, MPI_SNAPSHOT));
		ExpressionNode hereNode = nodeFactory.newHereNode(source);
		FunctionCallNode call = nodeFactory.newFunctionCallNode(source,
				callIdentifier, Arrays.asList(mpiComm, hereNode), null);

		return call;
	}

	private StatementNode createMPIUnsnapshotCall(ExpressionNode mpiComm,
			ExpressionNode collateAccess) {
		Source source = mpiComm.getSource();
		ExpressionNode callIdentifier = nodeFactory.newIdentifierExpressionNode(
				source, nodeFactory.newIdentifierNode(source, MPI_UNSNAPSHOT));
		FunctionCallNode call = nodeFactory.newFunctionCallNode(source,
				callIdentifier, Arrays.asList(mpiComm, collateAccess), null);

		return nodeFactory.newExpressionStatementNode(call);
	}

	private ExpressionNode conjunct(List<ExpressionNode> exprs) {
		Iterator<ExpressionNode> iter = exprs.iterator();
		ExpressionNode result = null;
		Source source = null;
		TokenFactory tf = astFactory.getTokenFactory();

		while (iter.hasNext()) {
			ExpressionNode expr = iter.next();

			source = source != null
					? tf.join(source, expr.getSource())
					: expr.getSource();
			result = result != null
					? nodeFactory.newOperatorNode(source, Operator.LAND,
							expr.copy(), result)
					: expr.copy();
		}
		return result;
	}

	/**
	 * @param assertion
	 * @return <code>$assert(assertion)</code>
	 */
	private StatementNode createAssertion(ExpressionNode assertion) {
		List<ExpressionNode> args = new LinkedList<>();
		Source source = assertion.getSource();
		ExpressionNode fun = nodeFactory.newIdentifierExpressionNode(source,
				nodeFactory.newIdentifierNode(source, BaseWorker.ASSERT));

		args.add(assertion);
		return nodeFactory.newExpressionStatementNode(
				nodeFactory.newFunctionCallNode(source, fun, args, null));
	}

	private ExpressionNode createCollateCompleteCall(
			ExpressionNode collateAccess) {
		ExpressionNode fun = identifierExpression(
				MPI_CONTRACT_CONSTS.COLLATE_COMPLETE_FUN);
		List<ExpressionNode> args = new LinkedList<>();

		args.add(collateAccess);
		return nodeFactory.newFunctionCallNode(fun.getSource(), fun, args,
				null);
	}

	private List<BlockItemNode> nondetermChoiceStmt(
			List<List<StatementNode>> choiceGroups) {
		List<BlockItemNode> results = new LinkedList<>();

		for (List<StatementNode> group : choiceGroups) {
			results.add(nodeFactory.newChooseStatementNode(
					newSource("elaborate pointers", CivlcTokenConstant.CHOOSE),
					group));
		}
		return results;
	}
}
