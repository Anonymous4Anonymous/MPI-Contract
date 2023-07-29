package edu.udel.cis.vsl.civl.transform.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity.EntityKind;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.entity.IF.OrdinaryEntity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.DeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.TypedefDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ArrayLambdaNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CastNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.AtomicNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.WithNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode.TypeNodeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.front.c.preproc.CPreprocessor;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.NameTransformer;
import edu.udel.cis.vsl.abc.transform.IF.Transform;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.model.IF.ModelConfiguration;
import edu.udel.cis.vsl.civl.transform.IF.GeneralTransformer;

/**
 * The general transformer performs the following transformations:
 * 
 * <ul>
 * <li>malloc(...) to $malloc($gen_root, ...); where $gen_root is the root scope
 * of this AST (which is reserved as a relative root if other transformers make
 * this AST as part of another)</li>
 * <li>introduce input variables for argc and argv</li>
 * <li>introduce new file scope function $gen_root_function that calls the main
 * function</li>
 * <li>arguments of the main function argc and argv become input variables</li>
 * <li>static variables are all moved to the root scope</li>
 * </ul>
 * 
 * @author Manchun Zheng
 *
 */
public class GeneralWorker extends BaseWorker {
	private final static String STRING_HEADER = "string.h";
	private final static String MALLOC = "malloc";
	private final static String CALLOC = "calloc";
	private final static String MEMSET = "memset";
	final static String GENERAL_ROOT = ModelConfiguration.GENERAL_ROOT;
	private final static String separator = "$";
	private static final String SCOPE_TYPE = "$scope";
	private static final String CIVL_MALLOC = "$malloc";
	@SuppressWarnings("unused")
	private static final String CIVL_SET_DEFAULT = "$set_default";
	private int static_var_count = 0;
	private String CIVL_argc_name;
	private String CIVL_argv_name;
	private Source mainSource;
	/**
	 * static variable declaration nodes of this AST
	 */
	private List<VariableDeclarationNode> static_variables = new LinkedList<>();
	private boolean argcUsed = false;
	private boolean argvUsed = false;
	private boolean callocExists = false;
	private StatementNode argcAssumption = null;

	private BlockItemNode scopeType, mallocDeclaration;

	// private TypeFactory typeFactory;

	public GeneralWorker(ASTFactory astFactory) {
		super(GeneralTransformer.LONG_NAME, astFactory);
		this.identifierPrefix = GeneralTransformer.PREFIX;
		// this.typeFactory = astFactory.getTypeFactory();
	}

	@Override
	public AST transform(AST unit) throws SyntaxException {
		SequenceNode<BlockItemNode> root = unit.getRootNode();
		AST newAst;
		List<VariableDeclarationNode> inputVars = new ArrayList<>();
		List<BlockItemNode> newExternalList = new ArrayList<>();
		OrdinaryEntity mainEntity = unit.getInternalOrExternalEntity(MAIN);
		FunctionDefinitionNode newMainFunction = null;
		Function mainFunction;
		FunctionDefinitionNode mainDef;

		// unit.prettyPrint(System.out, false);
		if (mainEntity == null) {
			throw new CIVLSyntaxException("missing main function",
					unit.getRootNode().getSource());
		}
		if (!(mainEntity instanceof Function)) {
			throw new CIVLSyntaxException(
					"non-function entity with name \"main\"",
					mainEntity.getFirstDeclaration().getSource());
		}

		Iterator<OrdinaryEntity> entityIter = unit.getExternalEntities();
		List<OrdinaryEntity> entities = new LinkedList<>();

		while (entityIter.hasNext())
			entities.add(entityIter.next());
		entityIter = unit.getInternalEntities();
		while (entityIter.hasNext())
			entities.add(entityIter.next());

		mainFunction = (Function) mainEntity;
		mainDef = mainFunction.getDefinition();
		checkAgumentsOfMainFunction(mainDef, root);
		unit = renameStaticVariables(unit);
		unit.release();
		// transform logic functions to stateless form:
		transformLogicFunctions(root, entities);
		this.getCIVLMallocDeclaration(root);
		root = moveStaticVariables(root);
		processMalloc(root);
		// transformWith(root);
		// remove main prototypes...
		for (DeclarationNode decl : mainFunction.getDeclarations()) {
			if (!decl.isDefinition()) {
				decl.parent().removeChild(decl.childIndex());
			}
		}
		this.mainSource = mainDef.getSource();
		inputVars = getInputVariables(mainDef);
		if (inputVars.size() > 0) {
			// the original main has parameters, need to transform main to _main
			// TODO add new argv _argv
			transformMainFunction(root);
			newMainFunction = createNewMainFunction();
		}
		// no need to modify the body of main:
		// argcAssumption must go tightly with the input variable declaration
		// because of the checking of array extent:
		// $input int _argc;
		// $assume (0 < _argc);
		// $input char _argv[_argc][];
		for (VariableDeclarationNode inputVar : inputVars) {
			newExternalList.add(inputVar);
			if (inputVar.getName().equals(CIVL_argc_name)
					&& argcAssumption != null) {
				newExternalList.add(
						assumeFunctionDeclaration(argcAssumption.getSource()));
				newExternalList.add(argcAssumption);
			}
		}
		// add my root
		if (this.scopeType != null)
			newExternalList.add(scopeType);
		if (this.mallocDeclaration != null)
			newExternalList.add(mallocDeclaration);
		newExternalList.add(this.generalRootScopeNode());
		for (BlockItemNode child : root) {
			if (child != null) {
				child.remove();
				newExternalList.add(child);
			}
		}
		if (newMainFunction != null)
			newExternalList.add(newMainFunction);
		root = nodeFactory.newSequenceNode(root.getSource(), "TranslationUnit",
				newExternalList);
		this.completeSources(root);
		newAst = astFactory.newAST(root, unit.getSourceFiles(),
				unit.isWholeProgram());
		// newAst.prettyPrint(System.out, false);
		// TODO: Check if there is a string.h
		if (callocExists && !this.hasHeader(newAst, STRING_HEADER)) {
			AST stringlibHeaderAST = this.parseSystemLibrary(
					new File(CPreprocessor.ABC_INCLUDE_PATH, STRING_HEADER),
					EMPTY_MACRO_MAP);

			newAst = this.combineASTs(stringlibHeaderAST, newAst);
		}
		return newAst;
	}

	/**
	 * translating a $with block, which has the format:
	 * 
	 * <pre>
	 * $with (col) {
	 *   s1;
	 *   s2;
	 *   ...
	 * }
	 * </pre>
	 * 
	 * into
	 * 
	 * <pre>
	 * $atom{
	 * col_old=col;
	 * rs=$enter_collate_state(col);
	 * s1;
	 * s2;
	 * ..
	 * $exit_collate_state(rs, col_old, col);
	 * }
	 * </pre>
	 * 
	 * @param node
	 */
	@SuppressWarnings("unused")
	private void transformWith(ASTNode node) {
		if (node instanceof WithNode) {
			WithNode withNode = (WithNode) node;
			ASTNode parent = withNode.parent();
			StatementNode bodyNode = withNode.getBodyNode();
			ExpressionNode colStateExpr = withNode.getStateReference();
			List<BlockItemNode> items = new LinkedList<>();
			// VariableDeclarationNode realState;
			ExpressionNode enter, exit;
			AtomicNode atomic;

			enter = this.functionCall(newSource("$enter_collate_state", 0),
					"$enter_collate_state",
					Arrays.asList(this.nodeFactory.newOperatorNode(
							colStateExpr.getSource(), Operator.ADDRESSOF,
							colStateExpr.copy())));
			// realState =
			// this.variableDeclaration(this.newUniqueIdentifier("real"),
			// this.typeNode(typeFactory.stateType()), enter);
			// items.add(realState);
			items.add(this.nodeFactory.newExpressionStatementNode(enter));
			bodyNode.remove();
			items.add(bodyNode);
			exit = this.functionCall(newSource("$exit_collate_state", 0),
					"$exit_collate_state", Arrays.asList(
							// this.identifierExpression(oldCol.getName()),
							this.nodeFactory.newOperatorNode(
									colStateExpr.getSource(),
									Operator.ADDRESSOF, colStateExpr.copy())
					// ,this.identifierExpression(realState.getName())
					));
			items.add(nodeFactory.newExpressionStatementNode(exit));
			atomic = nodeFactory.newAtomicStatementNode(withNode.getSource(),
					this.nodeFactory.newCompoundStatementNode(
							withNode.getSource(), items));
			parent.setChild(withNode.childIndex(), atomic);
		} else {
			for (ASTNode child : node.children()) {
				if (child != null)
					transformWith(child);
			}
		}
	}

	private void getCIVLMallocDeclaration(SequenceNode<BlockItemNode> root) {
		for (BlockItemNode child : root) {
			if (child == null)
				continue;
			if (scopeType == null && child instanceof TypedefDeclarationNode) {
				TypedefDeclarationNode typedef = (TypedefDeclarationNode) child;

				if (typedef.getName().equals(SCOPE_TYPE)) {
					scopeType = child;
					child.remove();
				}
			} else if (this.mallocDeclaration == null
					&& child instanceof FunctionDeclarationNode) {
				FunctionDeclarationNode function = (FunctionDeclarationNode) child;

				if (function.getName().equals(CIVL_MALLOC)) {
					this.mallocDeclaration = child;
					child.remove();
				}
			}
			if (scopeType != null && this.mallocDeclaration != null)
				return;
		}
	}

	/**
	 * $assume 0 < argc && argc < MAX_ARGC;
	 * 
	 * @param source
	 * @param argcName
	 * @return
	 * @throws SyntaxException
	 */
	private ExpressionStatementNode argcAssumption(Source source,
			String argcName) throws SyntaxException {
		ExpressionNode lowerBound = nodeFactory.newOperatorNode(source,
				Operator.LT,
				Arrays.asList(nodeFactory.newIntegerConstantNode(source, "0"),
						this.identifierExpression(source, argcName)));

		return nodeFactory.newExpressionStatementNode(
				this.functionCall(source, ASSUME, Arrays.asList(lowerBound)));
	}

	private void checkAgumentsOfMainFunction(FunctionDefinitionNode main,
			SequenceNode<BlockItemNode> root) {
		FunctionTypeNode functionType = main.getTypeNode();
		SequenceNode<VariableDeclarationNode> parameters = functionType
				.getParameters();

		if (parameters.numChildren() == 2)
			this.checkAgumentsOfMainFunctionWorker(
					parameters.getSequenceChild(0),
					parameters.getSequenceChild(1), main.getBody());
	}

	private void checkAgumentsOfMainFunctionWorker(VariableDeclarationNode argc,
			VariableDeclarationNode argv, ASTNode node) {
		if (node instanceof IdentifierExpressionNode) {
			IdentifierNode identifier = ((IdentifierExpressionNode) node)
					.getIdentifier();
			Entity entity = identifier.getEntity();

			if (entity.getEntityKind() == EntityKind.VARIABLE) {
				VariableDeclarationNode variable = ((Variable) entity)
						.getDefinition();

				if (variable != null)
					if (variable.equals(argc))
						this.argcUsed = true;
					else if (variable.equals(argv))
						this.argvUsed = true;
			}
		} else {
			for (ASTNode child : node.children()) {
				if (child == null)
					continue;
				checkAgumentsOfMainFunctionWorker(argc, argv, child);
				if (this.argcUsed && this.argvUsed)
					return;
			}
		}
	}

	private ExpressionNode argv_value_node() throws SyntaxException {
		TypeNode arrayOfCharPointer = nodeFactory.newArrayTypeNode(
				this.newSource("new main function", CivlcTokenConstant.TYPE),
				nodeFactory.newPointerTypeNode(
						this.newSource("new main function",
								CivlcTokenConstant.POINTER),
						this.basicType(BasicTypeKind.CHAR)),
				this.identifierExpression(CIVL_argc_name));
		// ExpressionNode body =
		// this.nodeFactory.newOperatorNode(this.newSource(
		// "address of", CivlcTokenConstant.SUB), Operator.ADDRESSOF,
		// this.nodeFactory.newOperatorNode(this.newSource("subscript",
		// CivlcTokenConstant.SUB), Operator.SUBSCRIPT,
		// this.nodeFactory.newOperatorNode(this.newSource(
		// "subscript", CivlcTokenConstant.SUB),
		// Operator.SUBSCRIPT, this
		// .identifierExpression(CIVL_argv_name),
		// this.identifierExpression("i")), this
		// .integerConstant(0)));

		ExpressionNode body = this.nodeFactory.newOperatorNode(
				this.newSource("subscript", CivlcTokenConstant.SUB),
				Operator.SUBSCRIPT, this.identifierExpression(CIVL_argv_name),
				this.identifierExpression("i"));

		ArrayLambdaNode arrayLambda = this.nodeFactory
				.newArrayLambdaNode(
						this.newSource(
								"array lambda", CivlcTokenConstant.LAMBDA),
						arrayOfCharPointer,
						Arrays.asList(this.variableDeclaration("i",
								this.basicType(BasicTypeKind.INT))),
						null, body);

		// ExpressionNode addressOf_argv0 = nodeFactory.newOperatorNode(this
		// .newSource("new main function", CivlcTokenConstant.OPERATOR),
		// Operator.SUBSCRIPT, Arrays.asList(arrayLambda, nodeFactory
		// .newIntegerConstantNode(this.newSource(
		// "new main function",
		// CivlcTokenConstant.INTEGER_CONSTANT), "0")));
		// addressOf_argv0 = nodeFactory.newOperatorNode(this.newSource(
		// "new main function", CivlcTokenConstant.OPERATOR),
		// Operator.ADDRESSOF, Arrays.asList(addressOf_argv0));

		return arrayLambda;
	}

	// private VariableDeclarationNode create_argv_tmp() throws SyntaxException
	// {
	// TypeNode arrayOfCharPointer = nodeFactory.newArrayTypeNode(this
	// .newSource("new main function", CivlcTokenConstant.TYPE),
	// nodeFactory.newPointerTypeNode(this.newSource(
	// "new main function", CivlcTokenConstant.POINTER), this
	// .basicType(BasicTypeKind.CHAR)), this
	// .identifierExpression(CIVL_argc_name));
	//
	// return this
	// .variableDeclaration(
	// _argv_tmp_name,
	// arrayOfCharPointer,
	// this.nodeFactory.newArrayLambdaNode(
	// this.newSource("array lambda",
	// CivlcTokenConstant.LAMBDA),
	// arrayOfCharPointer.copy(),
	// this.nodeFactory.newSequenceNode(
	// this.newSource(
	// "bound variable of array lambda",
	// CivlcTokenConstant.DECLARATION_LIST),
	// "bound variable list",
	// Arrays.asList(this.nodeFactory.newPairNode(
	// this.newSource("pair", 0),
	// this.nodeFactory.newSequenceNode(
	// this.newSource(
	// "variable declaration",
	// 0),
	// "variables",
	// Arrays.asList(this
	// .variableDeclaration(
	// "i",
	// this.basicType(BasicTypeKind.INT)))),
	// null))),
	// null,
	// this.nodeFactory.newOperatorNode(
	// this.newSource("address of",
	// CivlcTokenConstant.SUB),
	// Operator.ADDRESSOF,
	// this.nodeFactory.newOperatorNode(
	// this.newSource("subscript",
	// CivlcTokenConstant.SUB),
	// Operator.SUBSCRIPT,
	// this.nodeFactory.newOperatorNode(
	// this.newSource(
	// "subscript",
	// CivlcTokenConstant.SUB),
	// Operator.SUBSCRIPT,
	// this.identifierExpression(CIVL_argv_name),
	// this.identifierExpression("i")),
	// this.integerConstant(0)))));
	// }

	/**
	 * creates a new main function:
	 * 
	 * <pre>
	 * void main(){
	 *     for(int i=0; i &lt; 10; i = i + 1)
	 *       _argv[i] = &CIVL_argv[i][0];
	 *     _main(CIVL_argc, &_argv[0]);
	 * }
	 * </pre>
	 * 
	 * @return
	 * @throws SyntaxException
	 */
	private FunctionDefinitionNode createNewMainFunction()
			throws SyntaxException {
		FunctionCallNode callMain;
		List<BlockItemNode> blockItems = new LinkedList<>();
		FunctionTypeNode mainFuncType;

		callMain = nodeFactory.newFunctionCallNode(
				this.newSource("new main function", CivlcTokenConstant.CALL),
				this.identifierExpression(GEN_MAIN),
				Arrays.asList(this.identifierExpression(CIVL_argc_name),
						argv_value_node()),
				null);
		blockItems.add(nodeFactory.newExpressionStatementNode(callMain));
		mainFuncType = nodeFactory.newFunctionTypeNode(mainSource,
				nodeFactory.newBasicTypeNode(mainSource, BasicTypeKind.INT),
				nodeFactory.newSequenceNode(
						this.newSource("new main function",
								CivlcTokenConstant.PARAMETER_TYPE_LIST),
						"formal parameter types",
						new LinkedList<VariableDeclarationNode>()),
				false);

		return nodeFactory.newFunctionDefinitionNode(this.mainSource,
				this.identifier(MAIN), mainFuncType, null,
				nodeFactory.newCompoundStatementNode(mainSource, blockItems));
	}

	private VariableDeclarationNode generalRootScopeNode() {
		return nodeFactory.newVariableDeclarationNode(mainSource,
				nodeFactory.newIdentifierNode(mainSource, GENERAL_ROOT),
				nodeFactory.newScopeTypeNode(mainSource),
				nodeFactory.newHereNode(mainSource));
	}

	/**
	 * Recursively traverse the AST from the given {@link ASTNode}
	 * <code>currentNode</code>to process either 'malloc' or 'calloc' methods.
	 * <br>
	 * <p>
	 * 'malloc' will be transformed as 'civl_malloc'.
	 * </p>
	 * <p>
	 * 'calloc' will be transformed as 'civl_malloc' with 'memset' to initialize
	 * all involved space as '0'.
	 * </p>
	 * 
	 * @param currentNode
	 *            the {@link ASTNode} processed in this recursion
	 */
	private void processMalloc(ASTNode currentNode) throws SyntaxException {
		/* Determine the type of the current node. */
		if (currentNode instanceof FunctionCallNode
				&& ((FunctionCallNode) currentNode).getFunction()
						.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
			FunctionCallNode funcCallNode = (FunctionCallNode) currentNode;
			Source funcCallSource = funcCallNode.getSource();
			IdentifierNode funcIdNode = ((IdentifierExpressionNode) funcCallNode
					.getFunction()).getIdentifier();
			ExpressionNode civlRootIdNode = identifierExpression(funcCallSource,
					GENERAL_ROOT);

			/* Determine the name of the called function */
			if (funcIdNode.name().equals(MALLOC)) {
				int callNodeIndex = funcCallNode.childIndex();
				ASTNode mallocParentNode = funcCallNode.parent();
				ExpressionNode mallocArg = funcCallNode.getArgument(0);

				/* Malloc function has 1 argument (index=0) */
				// Replace the malloc with the $malloc function call
				funcIdNode.setName(CIVL_MALLOC);
				mallocArg.remove();
				funcCallNode.setArguments(nodeFactory.newSequenceNode(
						mallocArg.getSource(), "Actual Parameters",
						Arrays.asList(civlRootIdNode, mallocArg)));
				// Add type-cast if there is no cast operator (for *.c)
				if (!(mallocParentNode instanceof CastNode)) {
					funcCallNode.remove();
					if (mallocParentNode instanceof OperatorNode) {
						// malloc is in an expression.
						ExpressionNode lhsVarIdExprNode = ((OperatorNode) mallocParentNode)
								.getArgument(0);
						Type varType = lhsVarIdExprNode.getInitialType();
						CastNode castNode = nodeFactory.newCastNode(
								funcCallSource,
								typeNode(lhsVarIdExprNode.getSource(), varType),
								funcCallNode);

						// Pointer Type Check
						if (varType.kind() != TypeKind.POINTER)
							throw new CIVLSyntaxException(
									"The left hand side of a malloc call must be of pointer type.",
									lhsVarIdExprNode.getSource());
						mallocParentNode.setChild(callNodeIndex, castNode);
					} else if (mallocParentNode instanceof VariableDeclarationNode) {
						// malloc is in a initializer of a declaration.
						VariableDeclarationNode lhsVarDeclNode = (VariableDeclarationNode) mallocParentNode;
						Type varType = lhsVarDeclNode.getTypeNode().getType();
						CastNode castNode = nodeFactory.newCastNode(
								funcCallSource,
								lhsVarDeclNode.getTypeNode().copy(),
								funcCallNode);

						// Pointer Type Check
						if (varType.kind() != TypeKind.POINTER)
							throw new CIVLSyntaxException(
									"The left hand side of a malloc call must be of pointer type.",
									lhsVarDeclNode.getSource());
						lhsVarDeclNode.setInitializer(castNode);
					}
				}
			} else if (funcIdNode.name().equals(CALLOC)) {
				callocExists = true;

				int callNodeIndex = funcCallNode.childIndex();
				ASTNode callocStatementNode = funcCallNode.parent();
				// Use memset method to initialize memory allocated by calloc
				ExpressionNode memSize = null;
				ExpressionNode numElement = funcCallNode.getArgument(0);
				ExpressionNode typeElement = funcCallNode.getArgument(1);
				Source memsetSource = funcCallNode.getSource();
				IdentifierNode memsetIdNode = nodeFactory
						.newIdentifierNode(memsetSource, MEMSET);
				IdentifierExpressionNode memsetIDExprNode = nodeFactory
						.newIdentifierExpressionNode(memsetSource,
								memsetIdNode);
				ExpressionNode memsetFuncCallArg0ExprNode = null;
				ExpressionNode memsetFuncCallArg1ExprNode = nodeFactory
						.newIntegerConstantNode(memsetSource, "0");
				ExpressionNode memsetFuncCallArg2ExprNode = nodeFactory
						.newOperatorNode(memsetSource, Operator.TIMES,
								numElement.copy(), typeElement.copy());

				/* Malloc function has 1 argument (index=0) */
				// Replace the calloc with the $malloc function call
				funcIdNode.setName(CIVL_MALLOC);
				numElement.remove();
				typeElement.remove();
				// Calculate the multiply of two arguments of the calloc
				memSize = nodeFactory.newOperatorNode(funcCallSource,
						Operator.TIMES, numElement, typeElement);
				funcCallNode.setArguments(nodeFactory.newSequenceNode(
						funcCallSource, "Actual Parameters",
						Arrays.asList(civlRootIdNode, memSize)));
				// Back to the statement node calling the calloc function
				if (callocStatementNode instanceof CastNode) {
					callocStatementNode = callocStatementNode.parent();
				}
				// Get the variable identifier name for memset function.
				if (callocStatementNode instanceof VariableDeclarationNode) {
					memsetFuncCallArg0ExprNode = nodeFactory
							.newIdentifierExpressionNode(
									callocStatementNode.child(0).getSource(),
									(IdentifierNode) callocStatementNode
											.child(0).copy());
				} else if (callocStatementNode instanceof OperatorNode
						&& ((OperatorNode) callocStatementNode).getOperator()
								.equals(Operator.ASSIGN)) {
					memsetFuncCallArg0ExprNode = (ExpressionNode) callocStatementNode
							.child(0).copy();
				} else {
					throw new CIVLUnimplementedFeatureException(
							"\nCurrently, CIVL only supports calloc function call in"
									+ "\n\t a variable declaration in .c file, "
									+ "\n\t\t (e.g., int *p = calloc(..);)"
									+ "\n\t an assignment expression in .c file, "
									+ "\n\t\t (e.g., p = calloc(..);)"
									+ "\n\t or a cast operation expression in .c/.cvl file"
									+ "\n\t\t (e.g., ..(int*)calloc(..);)."
									+ "\n this exception is thrown for: \n\t"
									+ callocStatementNode.getSource());
				}
				// Add type-cast if there is no cast operator (for *.c)
				if (!(callocStatementNode instanceof CastNode)) {
					ASTNode funcCallParentNode = funcCallNode.parent();

					funcCallNode.remove();
					if (callocStatementNode instanceof OperatorNode) {
						// calloc is in an expression.
						ExpressionNode lhsVarIdExprNode = ((OperatorNode) callocStatementNode)
								.getArgument(0);
						Type varType = lhsVarIdExprNode.getInitialType();
						CastNode castNode = nodeFactory.newCastNode(
								funcCallSource,
								typeNode(lhsVarIdExprNode.getSource(), varType),
								funcCallNode);

						// Pointer Type Check
						if (varType.kind() != TypeKind.POINTER)
							throw new CIVLSyntaxException(
									"The left hand side of 'calloc' call must be of pointer type:",
									lhsVarIdExprNode.getSource());
						funcCallParentNode.setChild(callNodeIndex, castNode);
						// Get the variable id for memset function
						memsetFuncCallArg0ExprNode = lhsVarIdExprNode.copy();
					} else if (callocStatementNode instanceof VariableDeclarationNode) {
						// calloc is in a initializer of a declaration.
						VariableDeclarationNode lhsVarDeclNode = (VariableDeclarationNode) callocStatementNode;
						Type varType = lhsVarDeclNode.getTypeNode().getType();
						CastNode castNode = nodeFactory.newCastNode(
								funcCallSource,
								lhsVarDeclNode.getTypeNode().copy(),
								funcCallNode);

						// Pointer Type Check
						if (varType.kind() != TypeKind.POINTER)
							throw new CIVLSyntaxException(
									"The left hand side of 'calloc' call must be of pointer type:",
									lhsVarDeclNode.getSource());
						funcCallParentNode.setChild(callNodeIndex, castNode);
						// Get the variable id for memset function
						memsetFuncCallArg0ExprNode = nodeFactory
								.newIdentifierExpressionNode(
										lhsVarDeclNode.getSource(),
										lhsVarDeclNode.getIdentifier().copy());
					}
				}

				FunctionCallNode memsetCallNode = nodeFactory
						.newFunctionCallNode(memsetSource, memsetIDExprNode,
								Arrays.asList(memsetFuncCallArg0ExprNode,
										memsetFuncCallArg1ExprNode,
										memsetFuncCallArg2ExprNode),
								null);
				ExpressionStatementNode memsetFuncCallNode = nodeFactory
						.newExpressionStatementNode(memsetCallNode);
				ASTNode callocBlockNode = callocStatementNode.parent();
				int callocStatementIndex = callocStatementNode.childIndex();

				if (callocStatementNode instanceof VariableDeclarationNode) {
					// If calloc in a declaration that is in a block,
					// (e.g. int *p = calloc(..);)
					// then insert the memset call as the next statement
					// (e.g., int *p = calloc(..); memeset(..);)
					int bound = callocBlockNode.numChildren();
					ASTNode tempNode = memsetFuncCallNode;

					// insert the memset
					for (int i = callocStatementIndex + 1; i <= bound; i++)
						tempNode = callocBlockNode.setChild(i, tempNode);
				} else { /* Expression */
					// If calloc in an expression
					// (e.g. *p = calloc(..);)
					// then replace the expression with a block containing
					// both calloc and memeset.
					// (e.g., {*p = calloc(..); memeset(..);})
					ASTNode blockNode = callocBlockNode.parent();
					List<BlockItemNode> items = new ArrayList<BlockItemNode>();
					int callocParentIndex = callocBlockNode.childIndex();

					callocBlockNode.remove();
					items.add((BlockItemNode) callocBlockNode);
					items.add(memsetFuncCallNode);
					blockNode.setChild(callocParentIndex, nodeFactory
							.newCompoundStatementNode(funcCallSource, items));
				}
			}
		} else {
			for (int i = 0; i < currentNode.numChildren(); i++) {
				ASTNode child = currentNode.child(i);
				if (child != null)
					processMalloc(child);
			}
		}
	}

	/**
	 * Processes the original main function, including:
	 * <ul>
	 * <li>Removes all arguments of the function;</li>
	 * </ul>
	 * 
	 * @param mainFunction
	 *            The function definition node representing the original main
	 *            function.
	 * @param vars
	 *            The list of variable declaration nodes that are the arguments
	 *            of the original main function. These variables will be moved
	 *            up to the higher scope (i.e., the file scope of the final AST)
	 *            and become $input variables of the final AST. When invoking
	 *            this function, this parameter should be an empty list and this
	 *            function will update this list.
	 * @throws SyntaxException
	 */
	private List<VariableDeclarationNode> getInputVariables(
			FunctionDefinitionNode mainFunction) throws SyntaxException {
		List<VariableDeclarationNode> inputVars = new ArrayList<>();
		FunctionTypeNode functionType = mainFunction.getTypeNode();
		SequenceNode<VariableDeclarationNode> parameters = functionType
				.getParameters();
		int count = parameters.numChildren();

		if (count != 0 && count != 2) {
			if (count == 1) {
				if (parameters.getSequenceChild(0).getTypeNode()
						.kind() != TypeNodeKind.VOID)
					throw new SyntaxException(
							"The main function should have 0 or 2 parameters instead of "
									+ count,
							mainFunction.getSource());
			} else
				throw new SyntaxException(
						"The main function should have 0 or 2 parameters instead of "
								+ count,
						mainFunction.getSource());
		}
		if (count == 2 && (this.argvUsed || this.argcUsed)) {
			VariableDeclarationNode argc = parameters.getSequenceChild(0);
			VariableDeclarationNode argv = parameters.getSequenceChild(1);
			VariableDeclarationNode CIVL_argc = argc.copy();
			VariableDeclarationNode CIVL_argv;
			String argcName = argc.getIdentifier().name();
			String argvName = argv.getIdentifier().name();

			this.CIVL_argc_name = identifierPrefix + argcName;
			this.CIVL_argv_name = identifierPrefix + argvName;
			CIVL_argc.getTypeNode().setInputQualified(true);
			CIVL_argc.getIdentifier().setName(this.CIVL_argc_name);
			inputVars.add(CIVL_argc);
			CIVL_argv = inputArgvDeclaration(argv, CIVL_argv_name);
			inputVars.add(CIVL_argv);
			this.argcAssumption = this.argcAssumption(argc.getSource(),
					this.CIVL_argc_name);
		} else if (count == 2) {
			functionType.setParameters(this.nodeFactory.newSequenceNode(
					parameters.getSource(), "Formal Parameter List",
					new ArrayList<VariableDeclarationNode>(0)));
		}
		return inputVars;
	}

	/**
	 * Declares <code>$input char CIVL_argv[MAX_ARGC][];</code>.
	 * 
	 * @param oldArgv
	 * @return
	 * @throws SyntaxException
	 */
	private VariableDeclarationNode inputArgvDeclaration(
			VariableDeclarationNode oldArgv, String argvNewName)
			throws SyntaxException {
		VariableDeclarationNode __argv = oldArgv.copy();
		Source source = oldArgv.getSource();
		TypeNode arrayOfString = nodeFactory.newArrayTypeNode(source,
				nodeFactory.newArrayTypeNode(oldArgv.getSource(),
						this.basicType(BasicTypeKind.CHAR), null),
				this.identifierExpression(CIVL_argc_name));

		__argv.getIdentifier().setName(argvNewName);
		arrayOfString.setInputQualified(true);
		__argv.setTypeNode(arrayOfString);
		return __argv;
	}

	public enum ArgvTypeKind {
		POINTER_POINTER_CHAR, ARRAY_POINTER_CHAR, ARRAY_ARRAY_CAHR
	};

	private AST renameStaticVariables(AST ast) throws SyntaxException {
		Map<Entity, String> newNameMap = new HashMap<>();
		NameTransformer staticVariableNameTransformer;

		newNameMap = newNameMapOfStaticVariables(ast.getRootNode(),
				ast.getRootNode(), newNameMap);
		staticVariableNameTransformer = Transform.nameTransformer(newNameMap,
				astFactory);
		return staticVariableNameTransformer.transform(ast);
	}

	// TODO can you have static for function parameters?
	// TODO what if the initializer of the variable node access some variables
	// not declared in the root scope?
	// TODO what if the type is defined somewhere in the AST?
	/**
	 * Computes the new name map of static variables. A static variable "var" is
	 * renamed to "var$n", where n is the current static variable ID.
	 * 
	 * @param node
	 * @param newNames
	 * @return
	 */
	private Map<Entity, String> newNameMapOfStaticVariables(ASTNode root,
			ASTNode node, Map<Entity, String> newNames) {
		if (node instanceof VariableDeclarationNode) {
			VariableDeclarationNode variable = (VariableDeclarationNode) node;

			if (variable.hasStaticStorage()) {
				String oldName = variable.getName();
				String newName = oldName + separator + this.static_var_count++;

				newNames.put(variable.getEntity(), newName);
				// don't move the variable if it is already in the root scope
				if (!variable.parent().equals(root))
					this.static_variables.add(variable);
			}
		} else {
			for (ASTNode child : node.children()) {
				if (child == null)
					continue;
				newNames = newNameMapOfStaticVariables(root, child, newNames);
			}
		}
		return newNames;
	}

	private SequenceNode<BlockItemNode> moveStaticVariables(
			SequenceNode<BlockItemNode> root) {
		if (this.static_variables.size() < 1)
			return root;

		List<BlockItemNode> newChildren = new LinkedList<>();
		int count = root.numChildren();

		for (VariableDeclarationNode var : this.static_variables) {
			var.remove();
			newChildren.add(var);
		}
		for (int i = 0; i < count; i++) {
			BlockItemNode child = root.getSequenceChild(i);

			if (child == null)
				continue;
			child.remove();
			newChildren.add(child);
		}
		return nodeFactory.newTranslationUnitNode(root.getSource(),
				newChildren);
	}

	private void transformLogicFunctions(ASTNode root,
			List<OrdinaryEntity> entities) throws SyntaxException {
		LogicFunctionTransformer logicFunctionTransformer = new LogicFunctionTransformer(
				nodeFactory, astFactory.getTokenFactory());
		boolean hasLogicFunction = false;

		for (OrdinaryEntity entity : entities) {
			if (entity.getEntityKind() == EntityKind.FUNCTION) {
				Function function = (Function) entity;

				if (function.isLogic()) {
					if (function.getNumDeclarations() != 1)
						throw new CIVLSyntaxException(
								"Logic function " + function.getName()
										+ " has been declared twice.");
					logicFunctionTransformer.transformDefinition(
							(FunctionDeclarationNode) function
									.getFirstDeclaration());
					hasLogicFunction = true;
				}
			}
		}
		if (!hasLogicFunction)
			return;

		// traverse the ast for logic function calls outside of predicate
		// definition:
		ASTNode node = root;

		do {
			if (node.nodeKind() == NodeKind.FUNCTION_DEFINITION) {
				FunctionDefinitionNode funcDefi = (FunctionDefinitionNode) node;

				if (funcDefi.isLogicFunction()) {
					node = nextDFSSkip(node);
					continue;
				}
			}
			if (node.nodeKind() == NodeKind.EXPRESSION) {
				ExpressionNode expr = (ExpressionNode) node;

				if (expr.expressionKind() == ExpressionKind.FUNCTION_CALL)
					logicFunctionTransformer
							.transformCall((FunctionCallNode) expr);
			}
			node = node.nextDFS();
		} while (node != null);
	}
}