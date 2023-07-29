package edu.udel.cis.vsl.abc.front.fortran.astgen;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.AttributeKey;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PragmaNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.DesignationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FieldDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CastNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CompoundLiteralNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FloatingConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.QuantifiedExpressionNode.Quantifier;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeofNode;
import edu.udel.cis.vsl.abc.ast.node.IF.label.OrdinaryLabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpExecutableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode.BlockItemKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode.StatementKind;
import edu.udel.cis.vsl.abc.ast.node.IF.type.ArrayTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.PointerTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode.TypeNodeKind;
import edu.udel.cis.vsl.abc.ast.node.common.type.CommonTypedefNameNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.config.IF.Configuration;
import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.front.IF.Front;
import edu.udel.cis.vsl.abc.front.IF.ParseException;
import edu.udel.cis.vsl.abc.front.IF.PreprocessorException;
import edu.udel.cis.vsl.abc.front.common.astgen.LibraryASTFactory;
import edu.udel.cis.vsl.abc.front.common.astgen.PragmaFactory;
import edu.udel.cis.vsl.abc.front.common.astgen.PragmaHandler;
import edu.udel.cis.vsl.abc.front.fortran.ptree.MFPUtils;
import edu.udel.cis.vsl.abc.front.fortran.ptree.MFPUtils.PRPair;
import edu.udel.cis.vsl.abc.front.fortran.ptree.MFTree;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken.TokenVocabulary;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSequence;
import edu.udel.cis.vsl.abc.token.IF.Formation;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;
import edu.udel.cis.vsl.abc.token.IF.StringToken;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

public class MFASTBuilderWorker {
	// Static fields

	private static enum FORTRAN_ARRAY_DESCRIPTOR_KIND {
		ORIGIN, RESHAPE, SECTION_ARG, SECTION_FULL
	}

	/**
	 * The source information for nodes inserted by {@link MFASTWorker}
	 */
	static final private String SRC_INFO = "Fortran2CivlTransformer";

	static final private String IMPLICIT_DEFAULT_PATTERN = "^(I|J|K|L|M|N).*$";

	private static final String FORTRAN_ARRAY_PARAM_PREFIX = "__";

	private static final String FORTRAN_ARRAY_LOCAL_PREFIX = "__L_";

	private static final String FORTRAN_ARRAY_ARG_PREFIX = "__ARG_";

	private static final String FORTRAN_COMMON_BLOCK_PREFIX = "__FCB_";

	private static final String FORTRAN_FUNCTION_RETURN_PREFIX = "__RTN_";

	static private AttributeKey keyAttrPure = null;

	static private AttributeKey keyAttrRecursive = null;

	// Dynamic fields
	/**
	 * The path directing to the source file containing the entry of the
	 * translation unit processed by <code>this</code> {@link MFASTWorker}
	 */
	private String filePath;

	/**
	 * The input FORTRAN parse tree
	 */
	private MFTree ptree;

	private ASTFactory astFactory;
	private NodeFactory nodeFactory;
	private TokenFactory tokenFactory;
	private PragmaFactory pragmaFactory;
	private LibraryASTFactory libFactory;

	private Source dummySrc = null;

	private boolean isInputVarDecl = false;
	private boolean isOutputVarDecl = false;

	/**
	 * The list storing all program units involved in the FORTRAN program
	 */
	private ArrayList<BlockItemNode> programUnits;

	/**
	 * The list storing all dummy function/subroutine declarations
	 */
	private HashMap<String, FunctionDeclarationNode> funcDeclNodes;

	private List<BlockItemNode> inputOutputVarDeclNodes;

	private List<BlockItemNode> commonVarDeclNodes = new LinkedList<>();

	/**
	 * The root node of the output CIVL AST.
	 */
	private SequenceNode<BlockItemNode> root;

	/**
	 * Stores all involved {@link SourceFile}s
	 */
	private HashMap<Integer, SourceFile> srcFiles;

	/**
	 * Stores all involved Fortran Formats with their unique IDs
	 */
	private HashMap<String, String> formats;

	/**
	 * Indicates whether the FORTRAN program entry (<code>PROGRAM</code>)
	 * appears
	 */
	private boolean hasProgramEntry = false;

	/**
	 * Indicates whether CIVL-C <strong>math</strong> library implementation is
	 * involved in this transformation.
	 */
	private boolean useMATH = false;

	/**
	 * Indicates whether CIVL-C <strong>omp</strong> library implementation is
	 * involved in this transformation.
	 */
	private boolean useOMP = false;

	/**
	 * Indicates whether CIVL-C <strong>stdio</strong> library implementation is
	 * involved in this transformation.
	 */
	private boolean useSTDIO = false;

	/**
	 * Indicates whether CIVL-C <strong>stdlib</strong> library implementation
	 * is involved in this transformation.
	 */
	private boolean useSTDLIB = false;

	/**
	 * Indicates whether CIVL-C <strong>civlc</strong> library implementation is
	 * involved in this transformation.
	 */
	private boolean useCIVLC = false;

	/**
	 * Indicates whether CIVL-C <strong>fortran_array</strong> library
	 * implementation is involved in this transformation.
	 */
	private boolean useFORTRAN_ARRAY = false;

	/**
	 * Tracks the formation associated with the parent scope of the current file
	 * scope (e.g., FORTRAN's 'INCLUDE' statements will import a code segment
	 * from a file named as the statement specified.)
	 */
	private Stack<Formation> formations = new Stack<>();

	private Stack<String> freedArrays = new Stack<>();

	private Stack<VariableDeclarationNode> arraySectionDecls = new Stack<>();

	/**
	 * Tracks the program unit name involved in this translation task.
	 */
	private Stack<IdentifierNode> puIdStack = new Stack<>();

	private HashMap<String, PragmaHandler> pragmaMap = new HashMap<>();

	private HashMap<String, ExpressionNode> commonblockMemberMap = new HashMap<>();

	// Constructor
	public MFASTBuilderWorker(Configuration config, MFTree parseTree,
			ASTFactory astFactory, String filePath,
			PragmaFactory pragmaFactory) {
		this.ptree = parseTree;
		this.filePath = filePath;
		this.astFactory = astFactory;
		this.nodeFactory = astFactory.getNodeFactory();
		this.tokenFactory = astFactory.getTokenFactory();
		this.pragmaFactory = pragmaFactory;
		this.libFactory = new LibraryASTFactory(
				// CIVL Library Implementation Preprocessor
				Front.newPreprocessor(Language.C, config,
						tokenFactory.newFileIndexer(), tokenFactory),
				// CIVL Library Implementation Parser
				Front.newParser(Language.C),
				// CIVL Library Implementation ASTBuilder
				Front.newASTBuilder(Language.C, config, astFactory));
		this.programUnits = new ArrayList<BlockItemNode>();
		this.funcDeclNodes = new HashMap<String, FunctionDeclarationNode>();
		this.inputOutputVarDeclNodes = new ArrayList<BlockItemNode>();
		this.srcFiles = new HashMap<>();
		this.formats = new HashMap<>();
		if (MFASTBuilderWorker.keyAttrPure == null)
			keyAttrPure = nodeFactory.newAttribute("MFFuncAttrPure",
					Boolean.class);
		if (MFASTBuilderWorker.keyAttrRecursive == null)
			keyAttrRecursive = nodeFactory.newAttribute("MFFuncAttrRecursive",
					Boolean.class);
	}

	// Helper private functions

	// For a given library name, add all nodes from CIVL's implementation
	private void addLibASTNodes(String libName)
			throws PreprocessorException, ParseException, SyntaxException {
		List<BlockItemNode> libNodes = new ArrayList<BlockItemNode>();
		AST libAST = libFactory.getASTofLibrary(libName);
		SequenceNode<BlockItemNode> libRoot = libAST.getRootNode();

		libAST.release();
		for (BlockItemNode node : libRoot) {
			node.remove();
			libNodes.add(node);
		}
		root.insertChildren(0, libNodes);
		for (SourceFile srcFile : libAST.getSourceFiles())
			srcFiles.put(srcFiles.size(), srcFile);
	}

	// For involved libraries, Add all nodes from CIVL's implementation
	private void addLibASTNodes()
			throws PreprocessorException, ParseException, SyntaxException {
		if (useFORTRAN_ARRAY)
			addLibASTNodes(LibraryASTFactory.FORTRAN_ARRAY);
		else if (useSTDIO) // stdio.h is included in FORTRAN_ARRAY
			addLibASTNodes(LibraryASTFactory.STDIO);
		if (useSTDLIB)
			addLibASTNodes(LibraryASTFactory.STDLIB);
		if (useMATH)
			addLibASTNodes(LibraryASTFactory.MATH);
		if (useOMP)
			addLibASTNodes(LibraryASTFactory.OMP);
		if (useCIVLC)
			addLibASTNodes(LibraryASTFactory.CIVLC);
	}

	// private void addCommonBlockUnions(SequenceNode<BlockItemNode> root) {
	// boolean isStruct = false;
	// List<BlockItemNode> commonblockVars = new LinkedList<>();
	//
	// for (Entry<String, List<StructureOrUnionTypeNode>> block :
	// commonblockViewMap
	// .entrySet()) {
	// String blockName = block.getKey();
	// List<StructureOrUnionTypeNode> blockViews = block.getValue();
	// List<FieldDeclarationNode> fieldDecls = new LinkedList<>();
	//
	// for (StructureOrUnionTypeNode view : blockViews) {
	// String viewFieldName = view.getTag().name()
	// .substring(FORTRAN_COMMON_BLOCK_PREFIX.length());
	// IdentifierNode viewFieldIdNode = nodeFactory.newIdentifierNode(
	// view.getTag().getSource(), viewFieldName);
	// FieldDeclarationNode viewDeclNode = nodeFactory
	// .newFieldDeclarationNode(view.getSource(),
	// viewFieldIdNode, view);
	//
	// fieldDecls.add(viewDeclNode);
	// }
	//
	// IdentifierNode commonblockTypeIdNode = nodeFactory
	// .newIdentifierNode(dummySrc,
	// FORTRAN_COMMON_BLOCK_PREFIX + blockName);
	// IdentifierNode commonblockVarIdNode = nodeFactory
	// .newIdentifierNode(dummySrc, blockName);
	// SequenceNode<FieldDeclarationNode> commonblockViewStructs = nodeFactory
	// .newSequenceNode(dummySrc, "CommonBlockViewDecls",
	// fieldDecls);
	// StructureOrUnionTypeNode commonblockUnionTypeNode = nodeFactory
	// .newStructOrUnionTypeNode(dummySrc, isStruct,
	// commonblockTypeIdNode, commonblockViewStructs);
	// VariableDeclarationNode commonblockVarDeclNode = nodeFactory
	// .newVariableDeclarationNode(dummySrc, commonblockVarIdNode,
	// commonblockUnionTypeNode);
	//
	// commonblockVars.add(commonblockVarDeclNode);
	// }
	// root.insertChildren(0, commonblockVars);
	// }

	// Find the left-most token contained by the given parse tree node
	private CivlcToken findLToken(MFTree pNode) {
		int index = 0;
		int numChildren = pNode.numChildren();
		CivlcToken[] tokens = pNode.cTokens();

		if (tokens != null && tokens.length > 0)
			return tokens[index];
		else if (numChildren > 0) {
			MFTree tmpNode = null;
			CivlcToken tmpToken = null;

			while (index < numChildren) {
				tmpNode = pNode.getChildByIndex(index);
				if (tmpNode != null) {
					tmpToken = findLToken(tmpNode);
					if (tmpToken != null)
						return tmpToken;
				}
				index++;
			}
		}
		return null;
	}

	// Find the right-most token contained by the given parse tree node
	private CivlcToken findRToken(MFTree pNode) {
		int index = 0;
		int numChildren = pNode.numChildren();
		CivlcToken[] tokens = pNode.cTokens();

		if (tokens != null && (index = tokens.length - 1) >= 0)
			return tokens[index];
		else if ((index = numChildren - 1) >= 0) {
			MFTree tmpNode = null;
			CivlcToken tmpToken = null;

			while (index >= 0) {
				tmpNode = pNode.getChildByIndex(index);
				if (tmpNode != null) {
					tmpToken = findRToken(tmpNode);
					if (tmpToken != null)
						return tmpToken;
				}
				index--;
			}
		}
		return null;
	}

	private String getName(MFTree variable) {
		CivlcToken[] tokens = variable.cTokens();
		while (tokens.length < 1)
			tokens = variable.cTokens();
		return tokens[0].getText();
	}

	// Generate CIVL Source for given parse tree (or sub-tree).
	private Source newSource(MFTree... pNodes) {
		Source source = null;
		CivlcToken lToken = null, rToken = null, tmpToken = null;
		int ctr = 0;
		int numSrcNodes = pNodes.length;

		for (ctr = 0; ctr < numSrcNodes; ctr++)
			if (pNodes[ctr] != null) {
				tmpToken = findLToken(pNodes[ctr]);
				if (tmpToken != null) {
					lToken = tmpToken;
					break;
				}
			}
		for (ctr = numSrcNodes - 1; ctr > 0; ctr--)
			if (pNodes[ctr] != null) {
				tmpToken = findRToken(pNodes[ctr]);
				if (tmpToken != null) {
					rToken = tmpToken;
					break;
				}
			}
		if (lToken == null)
			source = tokenFactory.newSource(tokenFactory.newCivlcToken(
					CivlcTokenConstant.ABSENT, SRC_INFO, formations.peek(),
					TokenVocabulary.FORTRAN));
		else if (rToken == null)
			source = tokenFactory.newSource(lToken);
		else
			source = tokenFactory.newSource(lToken, rToken);
		return source;
	}

	/**
	 * R603: name
	 * 
	 * @param progId
	 * @return
	 */
	private IdentifierNode translateIdentifier(MFTree id) {
		Source src = newSource(id);
		String name = getName(id);
		IdentifierNode idNode = nodeFactory.newIdentifierNode(src, name);

		return idNode;
	}

	private IdentifierNode translateIdentifierLabel(MFTree label) {
		String C_LABEL_PREFIX = "L";
		Source src = newSource(label);
		String name = C_LABEL_PREFIX + (label.cTokens())[0].getText();

		return nodeFactory.newIdentifierNode(src, name);
	}

	/**
	 * R1401: main program <br>
	 * R1529: function subprogram<br>
	 * R1534: subroutine subprogram<br>
	 * Note1: Both `PROGRAM` and `SUBROUTINE` will return 'void'<br>
	 * Note2: All FORTRAN arguments are passed-by-reference, so scalar types of
	 * parameters will be casted to their corresponding pointer type.
	 * 
	 * @param prefix
	 * @param params
	 * @param formalMap
	 * @param prp
	 * @return {@link FunctionTypeNode} based on given info.
	 */
	private FunctionTypeNode translateFunctionType(MFTree prefix, MFTree name,
			MFTree params, PRPair prp, MFScope scope) {
		Source funcSrc = newSource(prefix, name, params);
		TypeNode returnTypeNode = null;
		List<VariableDeclarationNode> formalNodes = new LinkedList<>();
		SequenceNode<VariableDeclarationNode> formalsNode = null;
		boolean hasFormals = params != null;

		if (prp == MFPUtils.MAIN_PROGRAM)
			returnTypeNode = nodeFactory.newBasicTypeNode(funcSrc,
					BasicTypeKind.INT);
		else if (prp == MFPUtils.SUBROUTINE_SUBPROGRAM)
			returnTypeNode = nodeFactory.newVoidTypeNode(funcSrc);
		else if (prp == MFPUtils.FUNCTION_SUBPROGRAM) {
			if (prefix != null) {
				for (int i = 0; i < prefix.numChildren(); i++) {
					MFTree prefixSpec = prefix.getChildByIndex(i);

					if (prefixSpec.numChildren() > 0) {
						prefixSpec = prefixSpec.getChildByIndex(0);
						assert prefixSpec
								.prp() == MFPUtils.DECLARATION_TYPE_SPEC;
						returnTypeNode = translateType(
								prefixSpec.getChildByIndex(0), scope);
						break;
					}
				}
			}
			if (returnTypeNode == null)
				returnTypeNode = nodeFactory.newVoidTypeNode(funcSrc);
		} else
			assert false;
		if (hasFormals) {
			int numFormals = params.numChildren();
			MFTree formal = null;
			Source formalSrc = null;
			TypeNode dummyFormalType = null;
			IdentifierNode formalNameNode = null;
			VariableDeclarationNode formalNode = null;

			// Types of parameters are unknown in this scope
			// Assigns default implicit types
			// I, J, K, L, M, or N initials imply `INTEGER`
			// other ones imply `REAL`
			// unless `IMPLICIT` statements used.
			for (int i = 0; i < numFormals; i++) {
				formal = params.getChildByIndex(i);
				while (formal.numChildren() > 0)
					formal = formal.getChildByIndex(0);
				formalSrc = newSource(formal);
				formalNameNode = translateIdentifier(formal);
				if (formalNameNode.name().matches(IMPLICIT_DEFAULT_PATTERN))
					dummyFormalType = nodeFactory.newBasicTypeNode(formalSrc,
							BasicTypeKind.INT);
				else
					dummyFormalType = nodeFactory.newBasicTypeNode(formalSrc,
							BasicTypeKind.FLOAT);
				// Because all FORTRAN parameters are passed-by-reference,
				// scalar types are converted to corresponding pointer-types.
				dummyFormalType = nodeFactory.newPointerTypeNode(formalSrc,
						dummyFormalType);
				formalNode = nodeFactory.newVariableDeclarationNode(formalSrc,
						formalNameNode, dummyFormalType);
				formalNodes.add(formalNode);
			}
		}
		formalsNode = nodeFactory.newSequenceNode(newSource(params), "Formals",
				formalNodes);
		return nodeFactory.newFunctionTypeNode(funcSrc, returnTypeNode,
				formalsNode, hasFormals);
	}

	private TypeNode translateType(MFTree typeSpec, MFScope scope) {
		Source src = newSource(typeSpec);
		TypeNode typeNode = null;
		PRPair prp = typeSpec.prp();

		if (prp == MFPUtils.INTRINSIC_TYPE_SPEC) {
			int kind = typeSpec.kind();

			if (kind == MFPUtils.TYPE_INT)
				typeNode = nodeFactory.newBasicTypeNode(src, BasicTypeKind.INT);
			else if (kind == MFPUtils.TYPE_REAL) {

				if (typeSpec.numChildren() == 2)
					typeNode = nodeFactory.newBasicTypeNode(src,
							BasicTypeKind.FLOAT);
				else {
					final int REAL_SELECT_AS_DOUBLE = 8;
					MFTree kindSelector = typeSpec.getChildByIndex(2);
					String byteStr = getName(kindSelector.getChildByIndex(1));

					switch (Integer.parseInt(byteStr)) {
						case REAL_SELECT_AS_DOUBLE :
							typeNode = nodeFactory.newBasicTypeNode(src,
									BasicTypeKind.DOUBLE);
							break;
						default :
					}
				}
			} else if (kind == MFPUtils.TYPE_DBL)
				typeNode = nodeFactory.newBasicTypeNode(src,
						BasicTypeKind.DOUBLE);
			else if (kind == MFPUtils.TYPE_CPLX)
				typeNode = nodeFactory.newBasicTypeNode(src,
						BasicTypeKind.FLOAT_COMPLEX);
			else if (kind == MFPUtils.TYPE_DCPLX)
				typeNode = nodeFactory.newBasicTypeNode(src,
						BasicTypeKind.DOUBLE_COMPLEX);
			else if (kind == MFPUtils.TYPE_BOOL)
				typeNode = nodeFactory.newBasicTypeNode(src,
						BasicTypeKind.BOOL);
			else if (kind == MFPUtils.TYPE_CHAR)
				typeNode = nodeFactory.newBasicTypeNode(src,
						BasicTypeKind.CHAR);
			else
				assert false;
		} else if (prp == MFPUtils.T_TYPE) {
			MFTree derivedTypeSpec = typeSpec.getParent().getChildByIndex(1);
			MFTree derivedTypeName = derivedTypeSpec.getChildByIndex(0);
			IdentifierNode derivedTypeNameNode = translateIdentifier(
					derivedTypeName);

			typeNode = nodeFactory.newStructOrUnionTypeNode(src,
					true /* isStruct */, derivedTypeNameNode, null);
			scope.addDerivedTypeName(derivedTypeNameNode.name());
		} else
			assert false;
		return typeNode;
	}

	private InitializerNode translateInitializer(MFTree init, MFScope scope)
			throws SyntaxException {
		MFTree initVal = init.getChildByIndex(0);

		return translateExpr(newSource(initVal), initVal, scope);
	}
	private CompoundLiteralNode createArrayDimInfoNode(Source src,
			ExpressionNode dimInfo[][]) {
		int LBND = 0, UBND = 1, STRD = 2, DIM_INFO_SIZE = 3;
		LinkedList<PairNode<DesignationNode, InitializerNode>> lbndNodes = new LinkedList<PairNode<DesignationNode, InitializerNode>>();
		LinkedList<PairNode<DesignationNode, InitializerNode>> ubndNodes = new LinkedList<PairNode<DesignationNode, InitializerNode>>();
		LinkedList<PairNode<DesignationNode, InitializerNode>> strdNodes = new LinkedList<PairNode<DesignationNode, InitializerNode>>();
		LinkedList<PairNode<DesignationNode, InitializerNode>> dimInfoNode = new LinkedList<PairNode<DesignationNode, InitializerNode>>();
		ExpressionNode numDimNode = nodeFactory.newIntConstantNode(dummySrc,
				dimInfo.length);
		ExpressionNode dimInfoSize = nodeFactory.newIntConstantNode(dummySrc,
				DIM_INFO_SIZE);
		TypeNode subDimInfoTypeNode = nodeFactory.newArrayTypeNode(dummySrc,
				nodeFactory.newBasicTypeNode(dummySrc, BasicTypeKind.INT),
				numDimNode);
		TypeNode dimInfoTypeNode = nodeFactory.newArrayTypeNode(dummySrc,
				subDimInfoTypeNode, dimInfoSize);
		CompoundLiteralNode dimInfoLiteralNode = null;

		for (int d = 0; d < dimInfo.length; d++) {
			ExpressionNode lbndNode = dimInfo[d][LBND];
			ExpressionNode ubndNode = dimInfo[d][UBND];
			ExpressionNode strdNode = dimInfo[d][STRD];

			if (lbndNode == null || ubndNode == null || strdNode == null) {
				return null;
			}

			lbndNodes.add(nodeFactory.newPairNode(lbndNode.getSource(), null,
					lbndNode.copy()));
			ubndNodes.add(nodeFactory.newPairNode(ubndNode.getSource(), null,
					ubndNode.copy()));
			strdNodes.add(nodeFactory.newPairNode(strdNode.getSource(), null,
					strdNode.copy()));
		}
		dimInfoNode.add(nodeFactory.newPairNode(src, null,
				nodeFactory.newCompoundInitializerNode(src, lbndNodes)));
		dimInfoNode.add(nodeFactory.newPairNode(src, null,
				nodeFactory.newCompoundInitializerNode(src, ubndNodes)));
		dimInfoNode.add(nodeFactory.newPairNode(src, null,
				nodeFactory.newCompoundInitializerNode(src, strdNodes)));
		dimInfoLiteralNode = nodeFactory.newCompoundLiteralNode(src,
				dimInfoTypeNode,
				nodeFactory.newCompoundInitializerNode(src, dimInfoNode));
		return dimInfoLiteralNode;
	}

	private VariableDeclarationNode createArrayDesc(Source src,
			IdentifierNode varNameNode, ExpressionNode dimInfo[][],
			TypeNode baseTypeNode, MFScope scope,
			FORTRAN_ARRAY_DESCRIPTOR_KIND kind) {
		if (dimInfo == null && baseTypeNode == null) {
			TypeNode fArrDescType = genArrDescType(src);
			Source ssrcArrDescSrc = varNameNode.getSource();
			String ssrcArrDescName = varNameNode.name()
					.substring(FORTRAN_ARRAY_ARG_PREFIX.length());
			IdentifierNode ssrcArrDescId = nodeFactory
					.newIdentifierNode(ssrcArrDescSrc, ssrcArrDescName);
			ExpressionNode ssrcArrDescIdExpr = nodeFactory
					.newIdentifierExpressionNode(ssrcArrDescSrc, ssrcArrDescId);

			String farr_desc = "farr_section_full";
			List<ExpressionNode> args = Arrays.asList(ssrcArrDescIdExpr);

			IdentifierNode farrDescNode = nodeFactory.newIdentifierNode(src,
					farr_desc);
			ExpressionNode funcIdNode = nodeFactory
					.newIdentifierExpressionNode(dummySrc, farrDescNode);
			FunctionCallNode farrDescCallNode = nodeFactory
					.newFunctionCallNode(src, funcIdNode, args, null);

			scope.addArrayDecl(varNameNode.name(), baseTypeNode);
			freedArrays.push(varNameNode.name());
			return nodeFactory.newVariableDeclarationNode(src, varNameNode,
					fArrDescType, farrDescCallNode);
		}

		// Translate Fortran Array into CIVL-Fortran-array-desc:
		// Fortran: TYPE VARNAME(LBND:RBND:STRD, ..)
		// CIVL: farr_desc VARNAME =
		// farr_create(sizeof(TYPE), NUMDIM,
		// (int[3][NUMDIM]){{LBND,..}, {RBND, ..}, {STRD,..}});
		// TODO: Handle incomplete array type and attribute!
		String farr_desc = null;
		List<ExpressionNode> args = null;
		// Type:
		TypeNode fArrDescType = genArrDescType(src);
		// Init:
		// -- sizeof(TYPE)
		SizeofNode sizeofNode = nodeFactory.newSizeofNode(dummySrc,
				baseTypeNode);
		// -- NUMDIM
		ExpressionNode numDimNode = nodeFactory.newIntConstantNode(dummySrc,
				dimInfo.length);
		// -- (int[3][NUMDIM]){{LBND,..}, {RBND, ..}, {STRD,..}}
		CompoundLiteralNode dimInfoNode = createArrayDimInfoNode(src, dimInfo);
		if (dimInfoNode == null)
			kind = FORTRAN_ARRAY_DESCRIPTOR_KIND.SECTION_FULL;

		switch (kind) {
			case ORIGIN :
				farr_desc = "farr_create";
				args = Arrays.asList(sizeofNode, numDimNode, dimInfoNode);
				break;
			case RESHAPE :
				// srcName = "_" + varName
				Source rsrcArrDescSrc = varNameNode.getSource();
				String rsrcArrDescName = FORTRAN_ARRAY_PARAM_PREFIX
						+ varNameNode.name();
				IdentifierNode rsrcArrDescId = nodeFactory
						.newIdentifierNode(rsrcArrDescSrc, rsrcArrDescName);
				ExpressionNode rsrcArrDescIdExpr = nodeFactory
						.newIdentifierExpressionNode(rsrcArrDescSrc,
								rsrcArrDescId);

				farr_desc = "farr_reshape";
				args = Arrays.asList(rsrcArrDescIdExpr, numDimNode,
						dimInfoNode);
				break;
			case SECTION_ARG :
				// srcName = varName.
				Source ssrcArrDescSrc = varNameNode.getSource();
				String ssrcArrDescName = varNameNode.name()
						.substring(FORTRAN_ARRAY_ARG_PREFIX.length());
				IdentifierNode ssrcArrDescId = nodeFactory
						.newIdentifierNode(ssrcArrDescSrc, ssrcArrDescName);
				ExpressionNode ssrcArrDescIdExpr = nodeFactory
						.newIdentifierExpressionNode(ssrcArrDescSrc,
								ssrcArrDescId);

				farr_desc = "farr_section";
				args = Arrays.asList(ssrcArrDescIdExpr, dimInfoNode);
				break;
			case SECTION_FULL :
				Source fsrcArrDescSrc = varNameNode.getSource();
				String fsrcArrDescName = "__" + varNameNode.name();
				IdentifierNode fsrcArrDescId = nodeFactory
						.newIdentifierNode(fsrcArrDescSrc, fsrcArrDescName);
				ExpressionNode fsrcArrDescIdExpr = nodeFactory
						.newIdentifierExpressionNode(fsrcArrDescSrc,
								fsrcArrDescId);
				farr_desc = "farr_section_full";
				args = Arrays.asList(fsrcArrDescIdExpr);
				break;
		}

		IdentifierNode farrDescNode = nodeFactory.newIdentifierNode(src,
				farr_desc);
		ExpressionNode funcIdNode = nodeFactory
				.newIdentifierExpressionNode(dummySrc, farrDescNode);
		FunctionCallNode farrDescCallNode = nodeFactory.newFunctionCallNode(src,
				funcIdNode, args, null);

		scope.addArrayDecl(varNameNode.name(), baseTypeNode);
		freedArrays.push(varNameNode.name());
		return nodeFactory.newVariableDeclarationNode(src, varNameNode,
				fArrDescType, farrDescCallNode);
	}

	private BlockItemNode createArrayDestroy(String arrayVarName) {
		String FARR_DESTROY = "farr_destroy";
		IdentifierNode arrIdNode = nodeFactory.newIdentifierNode(dummySrc,
				arrayVarName);
		ExpressionNode arrIdExprNode = nodeFactory
				.newIdentifierExpressionNode(dummySrc, arrIdNode);
		IdentifierNode farrDestroyNode = nodeFactory.newIdentifierNode(dummySrc,
				FARR_DESTROY);
		ExpressionNode funcIdNode = nodeFactory
				.newIdentifierExpressionNode(dummySrc, farrDestroyNode);
		FunctionCallNode farrDestroyCallNode = nodeFactory.newFunctionCallNode(
				dummySrc, funcIdNode, Arrays.asList(arrIdExprNode), null);
		return nodeFactory.newExpressionStatementNode(farrDestroyCallNode);
	}

	private ExpressionNode[][] processArrayDimInfo(MFTree arrSpec,
			MFScope scope) throws SyntaxException {
		int LBND = 0, UBND = 1, STRD = 2;
		int arrDimNum = arrSpec.numChildren();
		ExpressionNode[][] dimInfo = new ExpressionNode[arrDimNum][3];

		useFORTRAN_ARRAY = true;
		for (int d = 0; d < arrDimNum; d++) {
			MFTree dimSpec = arrSpec.getChildByIndex(d);
			Source src = newSource(dimSpec);

			switch (dimSpec.kind()) {
				case MFPUtils.ASE_1X : // (1 :) *
				case MFPUtils.ASE_NN : // <null> : <null>
					dimInfo[d][LBND] = nodeFactory.newIntConstantNode(src, 1);
					break;
				case MFPUtils.ASE_LU : // Expr0 : Expr1
					dimInfo[d][UBND] = translateExpr(src,
							dimSpec.getChildByIndex(1), scope);
				case MFPUtils.ASE_LN : // Expr0 : <null>
				case MFPUtils.ASE_LX : // Expr0 : *
					dimInfo[d][LBND] = translateExpr(src,
							dimSpec.getChildByIndex(0), scope);
					break;
				case MFPUtils.ASE_RK : // ..
					break; // TODO:
				case MFPUtils.ASE_1U : // (1 :) Expr0
					dimSpec = dimSpec.getChildByIndex(0);
				default : // Expr
					dimInfo[d][LBND] = nodeFactory.newIntConstantNode(src, 1);
					dimInfo[d][UBND] = translateExpr(src, dimSpec, scope);
					break;

			}
			dimInfo[d][STRD] = nodeFactory.newIntConstantNode(src, 1);
		}
		return dimInfo;
	}

	private TypeNode processArrayType(Source src, ExpressionNode[][] dimInfo,
			TypeNode baseType, MFScope scope) throws SyntaxException {
		TypeNode arrayTypeNode = baseType;

		for (int k = 0; k < dimInfo.length; k++) {
			ExpressionNode idxLBExprNode = dimInfo[k][0];
			ExpressionNode idxUBExprNode = dimInfo[k][1];
			OperatorNode extentNode = null;
			List<ExpressionNode> extentArgExprs = new ArrayList<ExpressionNode>();

			if (idxUBExprNode != null) {
				extentArgExprs.add(idxUBExprNode);
				extentArgExprs.add(idxLBExprNode);
				extentNode = nodeFactory.newOperatorNode(src, Operator.MINUS,
						extentArgExprs);
				extentArgExprs.clear();
				extentArgExprs.add(extentNode);
				extentArgExprs
						.add(nodeFactory.newIntegerConstantNode(src, "1"));
				extentNode = nodeFactory.newOperatorNode(src, Operator.PLUS,
						extentArgExprs);
				arrayTypeNode = nodeFactory.newArrayTypeNode(src, arrayTypeNode,
						extentNode, idxLBExprNode.copy());
			} else {
				arrayTypeNode = nodeFactory.newArrayTypeNode(src, arrayTypeNode,
						null, idxLBExprNode);
				((ArrayTypeNode) arrayTypeNode)
						.setUnspecifiedVariableLength(true);
			}
		}
		return arrayTypeNode;
	}

	private void processDummyFuncOrSubrDeclaration(String funcName,
			FunctionDeclarationNode dummyDeclNode) {
		if (!funcDeclNodes.containsKey(funcName)) {
			funcDeclNodes.put(funcName, dummyDeclNode);
			programUnits.add(0, dummyDeclNode);
		}
	}

	private ExpressionNode createArraySubscript(Source src,
			IdentifierNode varIdNode, ExpressionNode idxInfo[],
			TypeNode baseTypeNode, MFScope scope) {
		// Translate Fortran Array Subscript into CIVL style:
		// (int*) farr_subscript()
		// Fortran: VARNAME(IDX0, .., IDXN)
		// CIVL: *(TYPE*) farr_subscript(VARNAME,
		// int[NUMDIM]{IDX0, IDXN});
		// TODO: Handle incomplete array type and attribute!
		String FARR_SUBSCRIPT = "farr_subscript";
		// int array for indices:
		ExpressionNode idxNode = null;
		TypeNode idxInfoTypeNode = nodeFactory.newArrayTypeNode(dummySrc,
				nodeFactory.newBasicTypeNode(dummySrc, BasicTypeKind.INT),
				nodeFactory.newIntConstantNode(dummySrc, idxInfo.length));
		LinkedList<PairNode<DesignationNode, InitializerNode>> idxInfoNodes = new LinkedList<PairNode<DesignationNode, InitializerNode>>();

		for (int d = 0; d < idxInfo.length; d++) {
			idxNode = idxInfo[d].copy();
			idxInfoNodes.add(nodeFactory.newPairNode(idxNode.getSource(), null,
					idxNode));
		}

		CompoundLiteralNode idxInfoLiteralNode = nodeFactory
				.newCompoundLiteralNode(src, idxInfoTypeNode, nodeFactory
						.newCompoundInitializerNode(src, idxInfoNodes));
		ExpressionNode isDirectNode = nodeFactory.newIntConstantNode(dummySrc,
				0);
		// Call on farr_subscript
		ExpressionNode arrayIdExprNode = nodeFactory
				.newIdentifierExpressionNode(varIdNode.getSource(),
						varIdNode.copy());
		IdentifierNode farrSubscriptNode = nodeFactory.newIdentifierNode(src,
				FARR_SUBSCRIPT);
		ExpressionNode funcIdNode = nodeFactory.newIdentifierExpressionNode(
				varIdNode.getSource(), farrSubscriptNode);
		FunctionCallNode farrSubscriptCallNode = nodeFactory
				.newFunctionCallNode(src, funcIdNode,
						Arrays.asList(arrayIdExprNode, idxInfoLiteralNode,
								isDirectNode),
						null);
		// Cast returned value to corresponding pointer
		CastNode ptrToArraySubscriptNode = nodeFactory.newCastNode(src,
				nodeFactory.newPointerTypeNode(dummySrc, baseTypeNode),
				farrSubscriptCallNode);
		// De-reference the pointer to the corresponding array element.
		return nodeFactory.newOperatorNode(src, Operator.DEREFERENCE,
				ptrToArraySubscriptNode);
	}

	private ExpressionNode createNullConstantNode(Source src,
			TypeNode pointerType) throws SyntaxException {
		ExpressionNode int0Node = nodeFactory.newIntegerConstantNode(dummySrc,
				"0");
		TypeNode voidTypeNode = nodeFactory.newVoidTypeNode(dummySrc);
		TypeNode voidPtrNode = nodeFactory.newPointerTypeNode(dummySrc,
				voidTypeNode);
		ExpressionNode nullConstNode = nodeFactory.newCastNode(dummySrc,
				voidPtrNode, int0Node);
		return nodeFactory.newCastNode(src, pointerType.copy(), nullConstNode);
	}

	// R801: type declaration stmt
	private List<BlockItemNode> translateTypeDeclaration(MFTree decl,
			MFScope scope) throws SyntaxException {
		int indexDeclSpec = 1;
		int indexStartDeclAttrSpec = 2;
		int numDeclChildren = decl.numChildren();
		int indexDeclEntities = numDeclChildren - 1;
		Source src = newSource(decl);
		ArrayList<BlockItemNode> declNodes = new ArrayList<>();
		MFTree declSpec = decl.getChildByIndex(indexDeclSpec);
		MFTree declEntities = decl.getChildByIndex(indexDeclEntities);
		int numDeclObj = declEntities.numChildren();
		TypeNode baseTypeNode = translateType(declSpec.getChildByIndex(0),
				scope);
		TypeNode refinedTypeNode = baseTypeNode;
		ExpressionNode dimInfo[][] = null;

		// Process shared type info
		if (numDeclChildren > 3) {
			for (int i = indexStartDeclAttrSpec; i < indexDeclEntities; i++) {
				MFTree attrSpec = decl.getChildByIndex(i);

				switch (attrSpec.kind()) {
					case MFPUtils.ATTR_ACCESS :
					case MFPUtils.ATTR_ALLOCATABLE :
					case MFPUtils.ATTR_ASYNCHRONOUS :
					case MFPUtils.ATTR_BIND :
					case MFPUtils.ATTR_CODIMENSION :
					case MFPUtils.ATTR_CONTIGUOUS :
						assert false;
						break;
					case MFPUtils.ATTR_DIMENSION :
						// !!!TODO: re-process for dimension(*)
						dimInfo = processArrayDimInfo(
								attrSpec.getChildByIndex(1), scope);
						refinedTypeNode = processArrayType(src, dimInfo,
								baseTypeNode.copy(), scope);
						break;
					case MFPUtils.ATTR_INTENT :
						// TODO: Impl Intent IN,OUT
						break;
					case MFPUtils.ATTR_POINTER :
						break;
					case MFPUtils.ATTR_TARGET :
						break;
					case MFPUtils.ATTR_EXTERNAL :
					case MFPUtils.ATTR_INTRINSIC :
					case MFPUtils.ATTR_OPTIONAL :
					case MFPUtils.ATTR_PARAMETER :
					case MFPUtils.ATTR_PROTECTED :
					case MFPUtils.ATTR_SAVE :
					case MFPUtils.ATTR_VALUE :
					case MFPUtils.ATTR_VOLATILE :
					case MFPUtils.ATTR_OTHER :
					default : // do nothing
						assert false;
				}
			}
		}
		// Process unique type info
		assert numDeclObj > 0;
		for (int i = 0; i < numDeclObj; i++) {
			MFTree declEntity = declEntities.getChildByIndex(i);
			MFTree declName = declEntity.getChildByIndex(0);
			IdentifierNode nameNode = translateIdentifier(declName);
			TypeNode typeNode = refinedTypeNode.copy();
			VariableDeclarationNode declNode = null;
			InitializerNode initNode = null;
			String varName = nameNode.name().toUpperCase();

			typeNode.setInputQualified(isInputVarDecl);
			typeNode.setOutputQualified(isOutputVarDecl);
			for (int j = 1; j < declEntity.numChildren(); j++) {
				MFTree spec = declEntity.getChildByIndex(j);
				PRPair prp = spec.prp();

				if (prp == MFPUtils.ARRAY_SPEC
						|| prp == MFPUtils.COARRAY_SPEC) {
					/*
					 * Fortran 2018: 8.2 Type declaration statement: Item.2 The
					 * type declaration statement also specifies the attributes
					 * whose keywords appear in the attr-spec, except that the
					 * DIMENSION attribute may be specified or overridden for an
					 * entity by the appearance of array-spec in its
					 * entity-decl, and the CODIMENSION attribute may be
					 * specified or overridden for an entity by the appearance
					 * of coarray-spec in its entity-decl.
					 */
					dimInfo = processArrayDimInfo(spec, scope);
					typeNode = processArrayType(src, dimInfo,
							baseTypeNode.copy(), scope);
				} else if (prp == MFPUtils.CHAR_LENGTH) {
					assert false;
				} else if (prp == MFPUtils.INITIALIZATION) {
					initNode = translateInitializer(spec, scope);
				} else
					assert false;
			}

			if (scope.isParameter(varName)) { // Formals
				TypeNode formalTypeNode = typeNode;

				declNode = scope.getParameterVarDeclNode(varName);
				scope.addVarDeclNode(varName, declNode);
				if (formalTypeNode.kind() == TypeNodeKind.BASIC) {
					// Parameters w/ scalar types are passed-by-ref.
					formalTypeNode = nodeFactory.newPointerTypeNode(src,
							formalTypeNode);
					declNode.setTypeNode(formalTypeNode);
				} else if (formalTypeNode.kind() == TypeNodeKind.ARRAY) {
					// Create array dummy parameter
					IdentifierNode newParamIdNode = nodeFactory
							.newIdentifierNode(dummySrc,
									FORTRAN_ARRAY_PARAM_PREFIX + varName);

					formalTypeNode = genArrDescType(src);
					declNode.setIdentifier(newParamIdNode);
					declNode.setTypeNode(formalTypeNode);
					// Reshape array argument
					declNode = createArrayDesc(src, nameNode, dimInfo,
							baseTypeNode.copy(), scope,
							FORTRAN_ARRAY_DESCRIPTOR_KIND.RESHAPE);
					declNodes.add(declNode);
					scope.addVarDeclNode(varName, declNode);
				} else
					assert false;
			} else if (typeNode.kind() == TypeNodeKind.ARRAY) {
				declNode = createArrayDesc(src, nameNode, dimInfo,
						baseTypeNode.copy(), scope,
						FORTRAN_ARRAY_DESCRIPTOR_KIND.ORIGIN);
				declNodes.add(declNode);
				scope.addVarDeclNode(varName, declNode);
			} else { // Locals
				declNode = nodeFactory.newVariableDeclarationNode(src, nameNode,
						typeNode, initNode);
				declNodes.add(declNode);
				scope.addVarDeclNode(varName, declNode);
			}
		}

		if (isInputVarDecl || isOutputVarDecl) {
			// $input/$output variables shall be in root scope
			isInputVarDecl = false;
			isOutputVarDecl = false;
			inputOutputVarDeclNodes.addAll(declNodes);
			return new ArrayList<>();
		} else
			return declNodes;
	}

	private ExpressionNode translateConstantChar(Source source, MFTree constant)
			throws SyntaxException {
		CivlcToken strToken = constant.getChildByIndex(0).cTokens()[0];
		String constantText = strToken.getText().replace('\'', '\"');

		return nodeFactory.newStringLiteralNode(source, constantText,
				tokenFactory.newStringToken(strToken).getStringLiteral());
	}

	private ExpressionNode translateConstantFloating(Source source,
			MFTree constant) throws SyntaxException {
		String constantText = constant.getChildByIndex(0).cTokens()[0].getText()
				.toUpperCase();
		int eIdx = constantText.indexOf('D'); // for double
		String suffix = "l";

		if (eIdx < 0) {
			eIdx = constantText.indexOf('E');
			suffix = "f";
		}
		if (eIdx > 0) {
			String baseStr = constantText.substring(0, eIdx);
			String exp10Str = constantText.substring(eIdx + 1);
			double base = Double.valueOf(baseStr);
			double exp10 = Double.valueOf(exp10Str);
			double result = base * Math.pow(10.0, exp10);

			constantText = Double.toString(result);
		}
		constantText += suffix;
		return nodeFactory.newFloatingConstantNode(source, constantText);
	}

	private ExpressionNode translateConstantInteger(Source source,
			MFTree constant) throws SyntaxException {
		String constantText = constant.getChildByIndex(0).cTokens()[0]
				.getText();

		return nodeFactory.newIntegerConstantNode(source, constantText);
	}

	private ExpressionNode translateConstantLogical(Source source,
			MFTree constant) {
		if (constant.getChildByIndex(0).prp() == MFPUtils.T_TRUE)
			return nodeFactory.newBooleanConstantNode(source, true);
		else
			return nodeFactory.newBooleanConstantNode(source, false);
	}

	private ExpressionNode translateOperatorExpression(Source source,
			MFTree exprStmt, MFScope scope) throws SyntaxException {
		PRPair prp = exprStmt.prp();
		Operator op = null;
		LinkedList<ExpressionNode> argNodes = null;

		if (prp == MFPUtils.PART_REF) {
			return translateExprPartRef(exprStmt, scope);
		} else if (prp == MFPUtils.ASSIGNMENT_STMT) {
			ExpressionNode lhsExprNode = translateExpr(source,
					exprStmt.getChildByIndex(1), scope);
			ExpressionNode rhsExprNode = translateExpr(source,
					exprStmt.getChildByIndex(2), scope);

			op = Operator.ASSIGN;
			argNodes = new LinkedList<>();
			argNodes.add(lhsExprNode);
			argNodes.add(rhsExprNode);
			return nodeFactory.newOperatorNode(source, op, argNodes);
		} else if (prp == MFPUtils.LEVEL_3_EXPR) {
			ExpressionNode lhsExprNode = translateExpr(source,
					exprStmt.getChildByIndex(0), scope);
			ExpressionNode rhsExprNode = translateExpr(source,
					exprStmt.getChildByIndex(2), scope);

			prp = exprStmt.getChildByIndex(1).prp();
			if (prp == MFPUtils.T_GE)
				op = Operator.GTE;
			else if (prp == MFPUtils.T_GT)
				op = Operator.GT;
			else if (prp == MFPUtils.T_EQ || prp == MFPUtils.T_EQ_EQ)
				op = Operator.EQUALS;
			else if (prp == MFPUtils.T_LT)
				op = Operator.LT;
			else if (prp == MFPUtils.T_LE)
				op = Operator.LTE;
			else if (prp == MFPUtils.T_NE)
				op = Operator.NEQ;
			else
				assert false;
			argNodes = new LinkedList<ExpressionNode>();
			argNodes.add(lhsExprNode);
			argNodes.add(rhsExprNode);
			return nodeFactory.newOperatorNode(source, op, argNodes);
		} else if (prp == MFPUtils.ADD_OPERAND) {
			Source src = newSource(exprStmt);
			MFTree addOperand = exprStmt.getChildByIndex(0);
			MFTree val = addOperand;
			ExpressionNode lhsExprNode = null;
			ExpressionNode rhsExprNode = null;

			if (exprStmt.kind() == MFPUtils.ADD_OPERAND_SIGN) {
				op = Operator.UNARYPLUS;
				val = exprStmt.getChildByIndex(1);
				if (exprStmt.getChildByIndex(0).prp() == MFPUtils.T_MINUS)
					op = Operator.UNARYMINUS;
				return nodeFactory.newOperatorNode(src, op,
						translateExpr(newSource(val), val, scope));

			}
			prp = addOperand.prp();
			if (prp == MFPUtils.ADD_OPERAND) {
				op = Operator.UNARYPLUS;
				val = val.getChildByIndex(1);
				lhsExprNode = translateExpr(newSource(val), val, scope);
				if (addOperand.getChildByIndex(0).prp() == MFPUtils.T_MINUS)
					op = Operator.UNARYMINUS;
				lhsExprNode = nodeFactory.newOperatorNode(src, op, lhsExprNode);
			} else
				lhsExprNode = translateExpr(newSource(val), val, scope);
			for (int i = 1; i < exprStmt.numChildren(); i++) {
				addOperand = exprStmt.getChildByIndex(i);
				op = Operator.PLUS;
				val = addOperand.getChildByIndex(1);
				rhsExprNode = translateExpr(newSource(val), val, scope);
				if (addOperand.getChildByIndex(0).prp() == MFPUtils.T_MINUS)
					op = Operator.MINUS;
				lhsExprNode = nodeFactory.newOperatorNode(src, op, lhsExprNode,
						rhsExprNode);
			}
			return lhsExprNode;
		} else if (prp == MFPUtils.MULT_OPERAND) {
			Source src = newSource(exprStmt);
			MFTree multOperand = exprStmt.getChildByIndex(0);
			MFTree val = multOperand;
			ExpressionNode lhsExprNode = translateExpr(newSource(val), val,
					scope);
			if (lhsExprNode.parent() != null) {
				lhsExprNode.remove();
			}
			ExpressionNode rhsExprNode = null;

			if (exprStmt.kind() == MFPUtils.MULT_OPERAND_POW) {
				Source powSrc = newSource(exprStmt.getChildByIndex(1));
				IdentifierNode powFuncNameNode = nodeFactory
						.newIdentifierNode(powSrc, "pow");
				ExpressionNode powFuncNode = nodeFactory
						.newIdentifierExpressionNode(powSrc, powFuncNameNode);

				useMATH = true;
				val = exprStmt.getChildByIndex(2);
				rhsExprNode = translateExpr(newSource(val), val, scope);
				return nodeFactory.newFunctionCallNode(src, powFuncNode,
						Arrays.asList(lhsExprNode, rhsExprNode), null);
			}
			for (int i = 1; i < exprStmt.numChildren(); i++) {
				multOperand = exprStmt.getChildByIndex(i);
				op = Operator.TIMES;
				val = multOperand.getChildByIndex(1);
				rhsExprNode = translateExpr(newSource(val), val, scope);
				if (multOperand.getChildByIndex(0).prp() == MFPUtils.T_SLASH)
					op = Operator.DIV;
				lhsExprNode = nodeFactory.newOperatorNode(src, op, lhsExprNode,
						rhsExprNode);
			}
			return lhsExprNode;
		} else if (prp == MFPUtils.AND_OPERAND) {
			Source src = newSource(exprStmt);
			int kind = exprStmt.kind();

			if (kind == MFPUtils.LAO_LST) {
				MFTree val = exprStmt.getChildByIndex(0);
				ExpressionNode lhsExprNode = translateExpr(newSource(val), val,
						scope);
				ExpressionNode rhsExprNode = null;

				for (int i = 1; i < exprStmt.numChildren(); i++) {
					op = Operator.LAND;
					val = exprStmt.getChildByIndex(i);
					rhsExprNode = translateExpr(newSource(val), val, scope);
					lhsExprNode = nodeFactory.newOperatorNode(src, op,
							lhsExprNode, rhsExprNode);
				}
				return lhsExprNode;
			} else { // kind == MFPUtils.LAO_NOT
				MFTree val = exprStmt.getChildByIndex(1);
				ExpressionNode unaryExprNode = translateExpr(newSource(val),
						val, scope);

				return nodeFactory.newOperatorNode(src, Operator.NOT,
						unaryExprNode);
			}
		} else if (prp == MFPUtils.OR_OPERAND) {
			Source src = newSource(exprStmt);
			MFTree val = exprStmt.getChildByIndex(0);
			ExpressionNode lhsExprNode = translateExpr(newSource(val), val,
					scope);
			ExpressionNode rhsExprNode = null;

			for (int i = 1; i < exprStmt.numChildren(); i++) {
				op = Operator.LOR;
				val = exprStmt.getChildByIndex(i);
				rhsExprNode = translateExpr(newSource(val), val, scope);
				lhsExprNode = nodeFactory.newOperatorNode(src, op, lhsExprNode,
						rhsExprNode);
			}
			return lhsExprNode;
		} else
			assert false;
		return null;
	}

	private ExpressionNode translateExprDataRef(Source src, MFTree refs,
			MFScope scope) {
		int numDataRefs = refs.numChildren();
		MFTree hostName = refs.getChildByIndex(0).getChildByIndex(0);
		MFTree refName = null;
		IdentifierNode hostNameNode = null;
		ExpressionNode refExpr = null;

		assert numDataRefs > 1;
		hostNameNode = translateIdentifier(hostName);
		refExpr = nodeFactory.newIdentifierExpressionNode(newSource(hostName),
				hostNameNode);
		for (int i = 1; i < numDataRefs; i++) {
			refName = refs.getChildByIndex(i).getChildByIndex(0);
			refExpr = nodeFactory.newDotNode(src, refExpr,
					translateIdentifier(refName));
		}
		return refExpr;
	}

	int dummyFuncRefArgsCtr = 0;
	static final String FUNC_REF_ARG_PREFIX = "__FUNCREF_ARG_";
	private LinkedList<BlockItemNode> dummyFuncRefArgs = new LinkedList<>();
	private LinkedList<BlockItemNode> dummyFuncRefArrayArgPreStmts = new LinkedList<>();
	private LinkedList<BlockItemNode> dummyFuncRefArrayArgPostStmts = new LinkedList<>();

	private ExpressionNode translateExprFuncRef(MFTree funcRef, MFScope scope)
			throws SyntaxException {
		int numArrayArgs = 0;
		Source src = newSource(funcRef);
		List<BlockItemNode> itemNodes = new LinkedList<>();
		MFTree funcName = funcRef.getChildByIndex(0);
		Boolean hasArgList = funcRef.numChildren() > 1;
		IdentifierNode funcIdNode = translateIdentifier(funcName);
		ExpressionNode funcRefNode = nodeFactory
				.newIdentifierExpressionNode(src, funcIdNode);
		List<ExpressionNode> actualCallArgNodes = new LinkedList<ExpressionNode>();
		List<VariableDeclarationNode> dummyFuncDeclFormalNodes = new LinkedList<VariableDeclarationNode>();
		SequenceNode<VariableDeclarationNode> formalsNode = null;
		TypeNode formalTypeNode = null;
		TypeNode tempNode = null;

		if (hasArgList) {
			MFTree args = funcRef.getChildByIndex(1);
			int numArgs = args.numChildren();

			for (int i = 0; i < numArgs; i++) {
				MFTree arg = args.getChildByIndex(i).getChildByIndex(0);
				Source argSrc = newSource(arg);
				ExpressionNode argNode = translateExpr(argSrc, arg, scope);
				IdentifierNode formalNameNode = nodeFactory.newIdentifierNode(
						argNode.getSource(), "__civl_dummy_arg_" + i);
				Boolean notSection = arraySectionDecls.isEmpty();

				while (!arraySectionDecls.isEmpty())
					itemNodes.add(0, arraySectionDecls.pop());
				switch (argNode.expressionKind()) {
					case OPERATOR :
						if (((OperatorNode) argNode)
								.getOperator() == Operator.DEREFERENCE) {
							argNode = ((OperatorNode) argNode).getArgument(0)
									.copy();
							argNode.remove();

							if (argNode instanceof IdentifierExpressionNode) {
								// Arg is an identifier w/ a scalar type
								formalTypeNode = analyzeRawExprType(argNode,
										scope);
								tempNode = formalTypeNode;
								if (tempNode.kind() == TypeNodeKind.BASIC) {
									argNode = nodeFactory.newOperatorNode(src,
											Operator.ADDRESSOF, argNode);
									formalTypeNode = nodeFactory
											.newPointerTypeNode(
													argNode.getSource(),
													formalTypeNode.copy());
								}
							} else if (argNode instanceof CastNode) {
								formalTypeNode = ((CastNode) argNode)
										.getCastType();
							}
						} else {
							formalTypeNode = analyzeRawExprType(argNode, scope)
									.copy();
							argNode = processExprInFuncRefArgs(formalTypeNode,
									argNode);
						}
						break;
					case IDENTIFIER_EXPRESSION :
						tempNode = analyzeRawExprType(argNode, scope);
						formalTypeNode = tempNode;
						if (tempNode.kind() == TypeNodeKind.BASIC) {
							argNode = nodeFactory.newOperatorNode(src,
									Operator.ADDRESSOF, argNode);
							formalTypeNode = nodeFactory.newPointerTypeNode(src,
									tempNode);
						}
						if (notSection && tempNode
								.kind() == TypeNodeKind.TYPEDEF_NAME) {
							// Array type arg
							// TODO: if this FuncRef is in an expression,
							// then a dummy variable is required for
							// removing side-effects caused by
							// inserting wraps of array type args.
							IdentifierNode arrayArgIdNode = nodeFactory
									.newIdentifierNode(src,
											FORTRAN_ARRAY_ARG_PREFIX
													+ ((IdentifierExpressionNode) argNode)
															.getIdentifier()
															.name());
							VariableDeclarationNode arrayArgVarDeclNode = createArrayDesc(
									dummySrc, arrayArgIdNode, null, null, scope,
									FORTRAN_ARRAY_DESCRIPTOR_KIND.SECTION_ARG);

							dummyFuncRefArrayArgPreStmts
									.add(arrayArgVarDeclNode);
							argNode = nodeFactory.newIdentifierExpressionNode(
									src, arrayArgIdNode.copy());
							numArrayArgs++;
						}
						if (tempNode.kind() == TypeNodeKind.ARRAY)
							assert false;
						break;
					case CONSTANT :
						argNode = argNode.copy();
						tempNode = analyzeRawExprType(argNode, scope);
						formalTypeNode = nodeFactory
								.newPointerTypeNode(dummySrc, tempNode);
						argNode = translateExprArg(argNode, scope);
						break;
					default :
						assert false;
				}
				actualCallArgNodes.add(argNode);
				dummyFuncDeclFormalNodes
						.add(nodeFactory.newVariableDeclarationNode(argSrc,
								formalNameNode, formalTypeNode.copy()));
			}
		}
		formalsNode = nodeFactory.newSequenceNode(src,
				"DummySubroutineFormalDeclList", dummyFuncDeclFormalNodes);

		FunctionCallNode callNode = nodeFactory.newFunctionCallNode(src,
				funcRefNode, actualCallArgNodes, null);
		FunctionTypeNode dummyFuncTypeNode = nodeFactory.newFunctionTypeNode(
				src, nodeFactory.newVoidTypeNode(src), formalsNode, false);
		FunctionDeclarationNode dummyFuncDeclNode = nodeFactory
				.newFunctionDeclarationNode(src, funcIdNode.copy(),
						dummyFuncTypeNode, null);

		processDummyFuncOrSubrDeclaration(getName(funcName), dummyFuncDeclNode);
		while (numArrayArgs > 0) {
			itemNodes.add(createArrayDestroy(freedArrays.pop()));
			numArrayArgs--;
		}
		return callNode;
	}

	private ExpressionNode processExprInFuncRefArgs(TypeNode exprType,
			ExpressionNode argNode) {
		// Expressions, which are replaced with pointers to
		// intermediate arg var.
		// e.g. <code> foo(x+1,y+1.0) </code> is translated as
		// <code>
		// int __FUNCREF_ARG_0 = x+1;
		// float __FUNCREF_ARG_1 = y+1.0;
		// foo(&__FUNCREF_ARG_0, &__FUNCREF_ARG_1);
		// </code>
		Source src = argNode.getSource();
		String funcRefArgName = FUNC_REF_ARG_PREFIX + dummyFuncRefArgsCtr;
		IdentifierNode argIdNode = nodeFactory.newIdentifierNode(src,
				funcRefArgName);
		ExpressionNode argIdExprNode = nodeFactory
				.newIdentifierExpressionNode(src, argIdNode.copy());
		ExpressionNode addrOfArgNode = nodeFactory.newOperatorNode(src,
				Operator.ADDRESSOF, argIdExprNode);
		VariableDeclarationNode argDeclNode = nodeFactory
				.newVariableDeclarationNode(dummySrc, argIdNode,
						exprType.copy(), argNode);

		dummyFuncRefArgs.add(argDeclNode);
		dummyFuncRefArgsCtr++;
		return addrOfArgNode;
	}

	private TypeNode analyzeRawExprType(ExpressionNode exprNode,
			MFScope scope) {
		TypeNode rawExprTypeNode = null;

		switch (exprNode.expressionKind()) {
			case OPERATOR :
				if (((OperatorNode) exprNode)
						.getOperator() == Operator.DEREFERENCE) {
					exprNode = ((OperatorNode) exprNode).getArgument(0).copy();
					exprNode.remove();

					if (exprNode instanceof IdentifierExpressionNode) {
						// Arg is an identifier w/ a scalar type
						rawExprTypeNode = scope.getTypeByName(
								((IdentifierExpressionNode) exprNode)
										.getIdentifier().name());
						if (rawExprTypeNode.kind() == TypeNodeKind.POINTER)
							rawExprTypeNode = ((PointerTypeNode) rawExprTypeNode)
									.referencedType();
					} else if (exprNode instanceof CastNode)
						rawExprTypeNode = ((CastNode) exprNode).getCastType();
				} else {
					OperatorNode opExprNode = (OperatorNode) exprNode;
					TypeNode tempTypeNode = null;

					rawExprTypeNode = analyzeRawExprType(
							opExprNode.getArgument(0), scope);
					for (int i = 1; i < opExprNode
							.getNumberOfArguments(); i++) {
						tempTypeNode = analyzeRawExprType(
								((OperatorNode) exprNode).getArgument(i),
								scope);
						assert (rawExprTypeNode.kind() == tempTypeNode.kind());
					}
				}
				break;
			case IDENTIFIER_EXPRESSION :
				String varName = ((IdentifierExpressionNode) exprNode)
						.getIdentifier().name();

				rawExprTypeNode = scope.getTypeByName(varName);
				if (rawExprTypeNode.kind() == TypeNodeKind.POINTER)
					rawExprTypeNode = ((PointerTypeNode) rawExprTypeNode)
							.referencedType();
				if (rawExprTypeNode.kind() == TypeNodeKind.TYPEDEF_NAME) {
					if (scope.hasArrayType(varName)) {
						rawExprTypeNode = scope
								.getArrayElementTypeByName(varName);
					} else
						assert false;
				}
				if (rawExprTypeNode.kind() == TypeNodeKind.ARRAY)
					assert false;
				break;
			case CONSTANT :
				if (exprNode instanceof IntegerConstantNode)
					rawExprTypeNode = nodeFactory.newBasicTypeNode(
							exprNode.getSource(), BasicTypeKind.INT);
				else if (exprNode instanceof FloatingConstantNode)
					rawExprTypeNode = nodeFactory.newBasicTypeNode(
							exprNode.getSource(), BasicTypeKind.FLOAT);
				else
					assert false;
				break;
			default :
				assert false;
		}
		if (rawExprTypeNode.parent() != null)
			rawExprTypeNode = rawExprTypeNode.copy();
		rawExprTypeNode.setInputQualified(false);
		rawExprTypeNode.setOutputQualified(false);
		return rawExprTypeNode;
	}

	private ExpressionNode translateExprPartRef(MFTree exprStmt, MFScope scope)
			throws SyntaxException {
		Source src = newSource(exprStmt);
		MFTree varId = exprStmt.getChildByIndex(0);
		MFTree subscripts = exprStmt.getChildByIndex(1);
		int numDim = subscripts.numChildren();
		IdentifierNode varIdNode = translateIdentifier(varId);
		String varIdStr = varIdNode.name().toUpperCase();
		ExpressionNode varExprNode = nodeFactory
				.newIdentifierExpressionNode(newSource(varId), varIdNode);

		if (numDim > 0) {
			// Array unit/section subscript
			Boolean isUnit = true;
			ExpressionNode idxNodes[][] = new ExpressionNode[numDim][3];
			TypeNode arrayElementTypeNode = scope
					.getArrayElementTypeByName(varIdStr).copy();
			MFTree subscript, lowerIndex, upperIndex, stride;

			for (int i = 0; i < numDim; i++) {
				subscript = subscripts.getChildByIndex(i);
				lowerIndex = subscript.getChildByIndex(0);
				idxNodes[i][0] = translateExpr(newSource(lowerIndex),
						lowerIndex, scope);
				if (subscript.numChildren() > 1) {
					isUnit = false;
					upperIndex = subscript.getChildByIndex(1);
					idxNodes[i][1] = translateExpr(newSource(upperIndex),
							upperIndex, scope);
					if (subscript.numChildren() > 2) {
						stride = subscript.getChildByIndex(2);
						idxNodes[i][2] = translateExpr(newSource(stride),
								stride, scope);
					} else // Section stride is 1 by default
						idxNodes[i][2] = nodeFactory
								.newIntConstantNode(dummySrc, 1);
				}
			}
			if (isUnit) {
				ExpressionNode unitIdxNodes[] = new ExpressionNode[numDim];

				for (int i = 0; i < numDim; i++)
					unitIdxNodes[i] = idxNodes[i][0];
				return createArraySubscript(src, varIdNode, unitIdxNodes,
						arrayElementTypeNode, scope);
			} else {
				String arraySectionName = "__arg_" + varIdStr;
				IdentifierNode argArraySectionId = nodeFactory
						.newIdentifierNode(dummySrc, arraySectionName);
				VariableDeclarationNode argArraySectionDecl = createArrayDesc(
						dummySrc, argArraySectionId, idxNodes,
						arrayElementTypeNode, scope,
						FORTRAN_ARRAY_DESCRIPTOR_KIND.SECTION_ARG);

				arraySectionDecls.push(argArraySectionDecl);
				scope.addVarDeclNode(arraySectionName, argArraySectionDecl);
				return nodeFactory.newIdentifierExpressionNode(dummySrc,
						argArraySectionId.copy());
			}
			// TODO: common block tranformation
		}
		return (OperatorNode) varExprNode;
	}

	private ExpressionNode translateExprStructure(Source src,
			MFTree structConstructor, MFScope scope) throws SyntaxException {
		MFTree derivedTypeName = structConstructor.getChildByIndex(0);
		MFTree derivedTypeVals = structConstructor.getChildByIndex(1);
		IdentifierNode derivedTypeNameNode = translateIdentifier(
				derivedTypeName);
		TypeNode derivedTypeNode = nodeFactory.newStructOrUnionTypeNode(
				newSource(derivedTypeName), true /* isStruct */,
				derivedTypeNameNode, null);
		LinkedList<PairNode<DesignationNode, InitializerNode>> structureLiteralNode = new LinkedList<PairNode<DesignationNode, InitializerNode>>();

		for (int i = 0; i < derivedTypeVals.numChildren(); i++) {
			MFTree fieldVal = derivedTypeVals.getChildByIndex(i)
					.getChildByIndex(0);
			ExpressionNode fieldExprNode = translateExpr(newSource(fieldVal),
					fieldVal, scope);

			structureLiteralNode.add(nodeFactory.newPairNode(
					fieldExprNode.getSource(), null, fieldExprNode));
		}

		return nodeFactory.newCompoundLiteralNode(src, derivedTypeNode,
				nodeFactory.newCompoundInitializerNode(
						newSource(derivedTypeVals), structureLiteralNode));
	}

	private ExpressionNode translateExprVariable(Source src, MFTree ref,
			MFScope scope, boolean isPureDeisgnator) throws SyntaxException {
		boolean hasSubscriptsOrArgs = ref.numChildren() > 1;
		MFTree refName = ref.getChildByIndex(0);
		String refNameText = getName(refName).toUpperCase();
		ExpressionNode refExprNode = null;

		if (scope.isDerivedTypeName(refNameText))
			return translateExprStructure(src, ref, scope);
		if (currentFunctionName != null
				&& currentFunctionName.equals(refNameText)
				&& !hasSubscriptsOrArgs)
			refNameText = FORTRAN_FUNCTION_RETURN_PREFIX + refNameText;
		if (translatedCommonVarNames.containsKey(refNameText))
			refNameText = translatedCommonVarNames.get(refNameText);
		refName.setNodeName(refNameText);
		if (hasSubscriptsOrArgs) {
			if (!isPureDeisgnator) {
				MFTree subscriptsOrArgs = ref.getChildByIndex(1);

				if (refNameText.equals("MOD") || refNameText.equals("MODULO")) {
					List<ExpressionNode> argNodes = new LinkedList<ExpressionNode>();

					assert subscriptsOrArgs.numChildren() == 2;
					for (int i = 0; i < subscriptsOrArgs.numChildren(); i++) {
						MFTree arg = subscriptsOrArgs.getChildByIndex(i);

						argNodes.add(translateExpr(newSource(arg),
								arg.getChildByIndex(0), scope));
					}
					return nodeFactory.newOperatorNode(src, Operator.MOD,
							argNodes);
				} else if (refNameText.equals("MAX"))
					return replaceFunctionMax(src, ref, scope);
				else if (refNameText.equals("ABS"))
					return replaceFunctionAbs(src, ref, scope);
				else if (refNameText.equals("PRESENT"))
					return replaceFunctionPresent(src, ref, scope);
				else if (refNameText.equals("SIN") || //
						refNameText.equals("COS") || //
						refNameText.equals("ATAN") || //
						refNameText.equals("SQRT"))
					return processMathFunction(refNameText.toLowerCase(), ref,
							scope);
			}
			if (scope.hasArrayType(refNameText))
				// Array Subscription.
				refExprNode = translateOperatorExpression(src, ref, scope);
			else
				// Function reference
				refExprNode = translateExprFuncRef(ref, scope);
		} else {
			// Scalar Variable
			refExprNode = nodeFactory.newIdentifierExpressionNode(src,
					translateIdentifier(refName));
			if (scope.isParameter(refNameText) && !(scope.getTypeByName(
					refNameText) instanceof CommonTypedefNameNode))
				// Dereference when it is a parameter with scalar type.
				refExprNode = nodeFactory.newOperatorNode(
						refExprNode.getSource(), Operator.DEREFERENCE,
						refExprNode);
			else if (commonblockMemberMap.containsKey(refNameText))
				refExprNode = commonblockMemberMap.get(refNameText).copy();
		}
		if (!hasSubscriptsOrArgs && !scope.isDeclared(refNameText)
				&& !refNameText.startsWith("_"))
			scope.addUndeclaredIdentifiers(refNameText);
		return refExprNode;
	}

	private ExpressionNode replaceFunctionAbs(Source src, MFTree absCall,
			MFScope scope) throws SyntaxException {
		MFTree arg = absCall.getChildByIndex(1).getChildByIndex(0)
				.getChildByIndex(0);
		ExpressionNode exprNode = translateExpr(src, arg, scope);
		ExpressionNode negExprNode = nodeFactory.newOperatorNode(src,
				Operator.UNARYMINUS, exprNode.copy());
		ExpressionNode int0Node = nodeFactory.newIntConstantNode(src, 0);
		ExpressionNode condExprNode = nodeFactory.newOperatorNode(src,
				Operator.GTE, exprNode.copy(), int0Node.copy());
		return nodeFactory.newOperatorNode(src, Operator.CONDITIONAL,
				Arrays.asList(condExprNode, exprNode, negExprNode));
	}

	private ExpressionNode processMathFunction(String funcName, MFTree call,
			MFScope scope) throws SyntaxException {
		Source src = newSource(call);
		MFTree arg = call.getChildByIndex(1).getChildByIndex(0)
				.getChildByIndex(0);
		ExpressionNode argNode = translateExpr(src, arg, scope);
		IdentifierNode funcNameNode = nodeFactory.newIdentifierNode(src,
				funcName);
		ExpressionNode atanFuncNode = nodeFactory
				.newIdentifierExpressionNode(src, funcNameNode);

		useMATH = true;
		return nodeFactory.newFunctionCallNode(src, atanFuncNode,
				Arrays.asList(argNode), null);
	}

	private ExpressionNode replaceFunctionMax(Source src, MFTree maxCall,
			MFScope scope) throws SyntaxException {
		MFTree arg0 = maxCall.getChildByIndex(1).getChildByIndex(0)
				.getChildByIndex(0);
		MFTree arg1 = maxCall.getChildByIndex(1).getChildByIndex(1)
				.getChildByIndex(0);
		ExpressionNode Expr0Node = translateExpr(src, arg0, scope);
		ExpressionNode Expr1Node = translateExpr(src, arg1, scope);
		ExpressionNode condExprNode = nodeFactory.newOperatorNode(src,
				Operator.GTE, Expr0Node.copy(), Expr1Node.copy());
		return nodeFactory.newOperatorNode(src, Operator.CONDITIONAL,
				Arrays.asList(condExprNode, Expr0Node, Expr1Node));
	}

	private ExpressionNode translateExprConstants(MFTree constant,
			MFScope scope) throws SyntaxException {
		Source src = newSource(constant);
		PRPair tConstPrp = constant.prp();
		ExpressionNode constantExprNode = null;

		if (tConstPrp == MFPUtils.SIGNED_REAL_LITERAL_CONSTANT)
			constant = constant.getChildByIndex(0);
		tConstPrp = constant.prp();
		if (tConstPrp == MFPUtils.INT_LITERAL_CONSTANT)
			constantExprNode = translateConstantInteger(src, constant);
		else if (tConstPrp == MFPUtils.REAL_LITERAL_CONSTANT)
			return translateConstantFloating(src, constant);
		else if (tConstPrp == MFPUtils.CHAR_LITERAL_CONSTANT)
			return translateConstantChar(src, constant);
		else if (tConstPrp == MFPUtils.LOGICAL_LITERAL_CONSTANT)
			return translateConstantLogical(src, constant);
		else
			assert false;
		return constantExprNode;
	}

	private ExpressionNode translateExprArg(ExpressionNode exprArgNode,
			MFScope scope) {
		Source src = exprArgNode.getSource();
		ExpressionNode indexNode = nodeFactory.newIntConstantNode(src, 0);
		ExpressionNode extentNode = nodeFactory.newIntConstantNode(src, 1);
		TypeNode exprArgType = analyzeRawExprType(exprArgNode, scope);
		TypeNode exprArgCompoundLiteTypeNode = nodeFactory.newArrayTypeNode(src,
				exprArgType, extentNode);
		CompoundInitializerNode exprArgCompoundInitNode = nodeFactory
				.newCompoundInitializerNode(src, Arrays.asList(
						nodeFactory.newPairNode(src, null, exprArgNode)));
		CompoundLiteralNode exprArgCompoundLiteNode = nodeFactory
				.newCompoundLiteralNode(src, exprArgCompoundLiteTypeNode,
						exprArgCompoundInitNode);
		OperatorNode exprArgValueNode = nodeFactory.newOperatorNode(src,
				Operator.SUBSCRIPT, exprArgCompoundLiteNode, indexNode);
		return nodeFactory.newOperatorNode(src, Operator.ADDRESSOF,
				exprArgValueNode);
	}

	private ExpressionNode replaceFunctionPresent(Source src,
			MFTree presentCall, MFScope scope) throws SyntaxException {
		MFTree varExpr = presentCall.getChildByIndex(1).getChildByIndex(0)
				.getChildByIndex(0);
		ExpressionNode varExprNode = translateExpr(src, varExpr, scope);
		IdentifierExpressionNode varIdExprNode = (IdentifierExpressionNode) varExprNode;
		TypeNode varTypeNode = scope
				.getTypeByName(varIdExprNode.getIdentifier().name());
		ExpressionNode presentNode = nodeFactory.newOperatorNode(src,
				Operator.EQUALS, varExprNode,
				createNullConstantNode(src, varTypeNode));
		return presentNode;
	}

	private List<BlockItemNode> translateDerivedTypeDef(MFTree derivedTypeDef,
			MFScope scope) {
		Source src = newSource(derivedTypeDef);
		LinkedList<BlockItemNode> derivedTypeDefNodes = new LinkedList<>();
		MFTree derivedTypeStmt = derivedTypeDef.getChildByIndex(0);
		MFTree derivedTypeName = derivedTypeStmt.getChildByIndex(2);
		MFTree derivedTypeParam = derivedTypeStmt.getChildByIndex(3);
		MFTree derivedTypeAttrs = derivedTypeStmt.getChildByIndex(4);
		IdentifierNode derivedTypeNameNode = null;
		List<FieldDeclarationNode> compNodes = new LinkedList<>();
		SequenceNode<FieldDeclarationNode> componentsNode = null;

		derivedTypeNameNode = translateIdentifier(derivedTypeName);
		if (derivedTypeParam.prp() != MFPUtils.ABSENT)
			assert false;
		if (derivedTypeAttrs.prp() != MFPUtils.ABSENT)
			assert false;
		for (int i = 1; i < derivedTypeDef.numChildren() - 1; i++) {
			MFTree compOrTypeParam = derivedTypeDef.getChildByIndex(i);

			if (compOrTypeParam.prp() == MFPUtils.DATA_COMPONENT_DEF_STMT) {
				compNodes.addAll(translateCompDecls(compOrTypeParam, scope));
			} else
				assert false;
		}
		componentsNode = nodeFactory.newSequenceNode(newSource(derivedTypeDef),
				"DerivedTypeMembers", compNodes);
		derivedTypeDefNodes.add(nodeFactory.newStructOrUnionTypeNode(src,
				true /* isStruct */, derivedTypeNameNode, componentsNode));
		return derivedTypeDefNodes;
	}

	private List<FieldDeclarationNode> translateCompDecls(
			MFTree compOrTypeParam, MFScope scope) {
		Source src = newSource(compOrTypeParam);
		LinkedList<FieldDeclarationNode> compNodes = new LinkedList<>();
		MFTree compType = compOrTypeParam.getChildByIndex(1);
		MFTree compEntities = compOrTypeParam.getChildByIndex(2);
		MFTree compTypeAttr = compOrTypeParam.getChildByIndex(3);
		TypeNode rawTypeNode = translateType(compType.getChildByIndex(0),
				scope);

		if (compTypeAttr.prp() != MFPUtils.ABSENT)
			assert false;
		for (int i = 0; i < compEntities.numChildren(); i++) {
			MFTree compEntity = compEntities.getChildByIndex(i);
			MFTree compName = compEntity.getChildByIndex(0);

			if (compEntity.getChildByIndex(1).prp() != MFPUtils.ABSENT)
				assert false;
			if (compEntity.getChildByIndex(2).prp() != MFPUtils.ABSENT)
				assert false;
			if (compEntity.getChildByIndex(3).prp() != MFPUtils.ABSENT)
				assert false;
			if (compEntity.getChildByIndex(4).prp() != MFPUtils.ABSENT)
				assert false;
			compNodes.add(nodeFactory.newFieldDeclarationNode(src,
					translateIdentifier(compName), rawTypeNode.copy()));
		}

		if (compTypeAttr.prp() != MFPUtils.ABSENT)
			assert false;
		return compNodes;
	}

	private ExpressionNode translateExpr(Source src, MFTree exprStmt,
			MFScope scope) throws SyntaxException {
		PRPair prp = exprStmt.prp();
		ExpressionNode exprNode = null;

		if (prp == MFPUtils.ASSIGNMENT_STMT)
			return translateOperatorExpression(src, exprStmt, scope);
		else if (prp == MFPUtils.VARIABLE) {
			assert exprStmt.getChildByIndex(0).prp() == MFPUtils.DESIGNATOR;

			MFTree ref = exprStmt.getChildByIndex(0).getChildByIndex(0)
					.getChildByIndex(0);

			return translateExprVariable(newSource(ref), ref, scope, true);
		} else if (prp == MFPUtils.PRIMARY) {
			MFTree primary = exprStmt.getChildByIndex(0);
			PRPair tmpPrp = primary.prp();

			if (tmpPrp == MFPUtils.DESIGNATOR_OR_FUNC_REF) {
				MFTree refs = primary.getChildByIndex(0);
				MFTree ref = refs.getChildByIndex(0);

				if (refs.numChildren() > 1)
					return translateExprDataRef(newSource(refs), refs, scope);
				return translateExprVariable(newSource(ref), ref, scope, false);
			} else if (tmpPrp == MFPUtils.LITERAL_CONSTANT) {
				MFTree constant = exprStmt.getChildByIndex(0)
						.getChildByIndex(0);

				return translateExprConstants(constant, scope);
			} else if (tmpPrp == MFPUtils.ARRAY_CONSTRUCTOR) {
				MFTree acSpec = exprStmt.getChildByIndex(0).getChildByIndex(0);

				return translateArrayConstructor(acSpec, scope);
			} else if (tmpPrp == MFPUtils.STRUCTURE_CONSTRUCTOR)
				assert false;
			else if (tmpPrp == MFPUtils.EXPR)
				assert false;
			else
				return translateExpr(newSource(primary), primary, scope);
		} else if (prp == MFPUtils.MULT_OPERAND || //
				prp == MFPUtils.ADD_OPERAND || //
				prp == MFPUtils.LEVEL_3_EXPR || //
				prp == MFPUtils.AND_OPERAND || //
				prp == MFPUtils.OR_OPERAND)
			return translateOperatorExpression(src, exprStmt, scope);
		else if (prp == MFPUtils.QUANTIFIED_EXPR) {
			MFTree quantifier = exprStmt.getChildByIndex(0);
			MFTree boundVarType = exprStmt.getChildByIndex(1);
			MFTree boundVarNames = exprStmt.getChildByIndex(2);
			MFTree boundVarName = null;
			MFTree rExpr = null;
			MFTree pExpr = exprStmt.getChildByIndex(3);
			Quantifier q = null;
			TypeNode boundVarTypeNode = null;
			IdentifierNode boundVarIdNode = null;
			List<VariableDeclarationNode> boundVarTypeDeclNodes = new LinkedList<>();
			SequenceNode<VariableDeclarationNode> boundVarTypeDeclsNode = null;
			List<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVariableList = new LinkedList<>();
			SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVarDeclsNode = null;
			ExpressionNode restrictExprNode = null;
			ExpressionNode predExprNode = null;

			switch (getName(quantifier)) {
				case "$FORALL" :
					q = Quantifier.FORALL;
					break;
				case "$EXISTS" :
					q = Quantifier.EXISTS;
					break;
				case "$UNIFORM" :
					q = Quantifier.UNIFORM;
					break;
				default :
					assert false;
			}
			boundVarTypeNode = translateType(boundVarType, scope);
			for (int i = 0; i < boundVarNames.numChildren(); i++) {
				boundVarName = boundVarNames.getChildByIndex(i);
				boundVarIdNode = translateIdentifier(
						boundVarName.getChildByIndex(0));
				boundVarTypeDeclNodes.add(nodeFactory
						.newVariableDeclarationNode(newSource(boundVarName),
								boundVarIdNode, boundVarTypeNode.copy()));
			}
			boundVarTypeDeclsNode = nodeFactory.newSequenceNode(src,
					"Binder List", boundVarTypeDeclNodes);
			boundVariableList.add(
					nodeFactory.newPairNode(src, boundVarTypeDeclsNode, null));
			boundVarDeclsNode = nodeFactory.newSequenceNode(src,
					"bound variable declaration list", boundVariableList);
			if (exprStmt.numChildren() > 4) {
				// Process restrict Expr, if it exits
				rExpr = pExpr;
				pExpr = exprStmt.getChildByIndex(4);
				restrictExprNode = translateExpr(src, rExpr, scope);
			}
			predExprNode = translateExpr(src, pExpr, scope);
			return nodeFactory.newQuantifiedExpressionNode(src, q,
					boundVarDeclsNode, restrictExprNode, predExprNode, null);
		} else
			assert false;

		return exprNode;
	}

	private BlockItemNode translateStmtExpr(MFTree exprStmt, MFScope scope)
			throws SyntaxException {
		Source src = newSource(exprStmt);
		ExpressionNode exprNode = translateExpr(src, exprStmt, scope);

		if (exprNode == null)
			return nodeFactory.newNullStatementNode(src);
		else
			return nodeFactory.newExpressionStatementNode(exprNode);
	}

	private List<BlockItemNode> translateStmtPrint(MFTree printStmt,
			MFScope scope) throws SyntaxException {
		LinkedList<BlockItemNode> printfNodes = new LinkedList<>();
		MFTree print = printStmt.getChildByIndex(1);
		Source src = newSource(printStmt);
		Source printSrc = newSource(print);
		MFTree outputFormat = printStmt.getChildByIndex(2);
		MFTree outputFormatId = outputFormat.getChildByIndex(0);
		MFTree outputList = printStmt.getChildByIndex(3);
		IdentifierNode printfIdNode = nodeFactory.newIdentifierNode(printSrc,
				"printf");
		ExpressionNode printfFuncNode = nodeFactory
				.newIdentifierExpressionNode(printSrc, printfIdNode);
		ExpressionNode formatNode = null;
		FunctionCallNode printfCallNode = null;
		LinkedList<ExpressionNode> printfArgNodes = new LinkedList<>();
		String formatStr = "\"";
		StringToken formatToken = null;

		useSTDIO = true;
		while (outputFormatId.numChildren() != 0)
			outputFormatId = outputFormatId.getChildByIndex(0);
		for (int j = 0; j < outputList.numChildren(); j++) {
			MFTree outputItem = outputList.getChildByIndex(j);
			ExpressionNode printfArgNode = translateExpr(newSource(outputItem),
					outputItem.getChildByIndex(0), scope);

			printfArgNodes.add(printfArgNode);
		}
		if (outputFormatId.prp() == MFPUtils.T_ASTERISK) {
			for (int j = 0; j < outputList.numChildren(); j++)
				formatStr += "%s ";
		} else {
			String fmtKey = getName(outputFormatId);

			if (formats.containsKey(fmtKey))
				formatStr += formats.get(fmtKey);
			else
				assert false;
		}
		formatStr += "\\n\"";
		formatToken = tokenFactory.newStringToken(tokenFactory.newCivlcToken(0,
				formatStr, print.cTokens()[0].getFormation(),
				TokenVocabulary.FORTRAN));
		formatNode = nodeFactory.newStringLiteralNode(src, formatStr,
				formatToken.getStringLiteral());
		printfArgNodes.add(0, formatNode);
		printfCallNode = nodeFactory.newFunctionCallNode(src, printfFuncNode,
				printfArgNodes, null);
		printfNodes.add(nodeFactory.newExpressionStatementNode(printfCallNode));
		return printfNodes;
	}

	private List<BlockItemNode> translateStmtReturn(MFTree returnStmt,
			MFScope scope) {
		Source src = newSource(returnStmt);
		ExpressionNode returnExprNode = null;
		List<BlockItemNode> returnNodes = new LinkedList<>();

		// if (!freedArrays.isEmpty())
		// for (int i = freedArrays.size() - 1; i >= 0; i--)
		// returnNodes.add(createArrayDestroy(freedArrays.get(i)));
		if (currentFunctionName != null) {
			returnExprNode = nodeFactory.newIdentifierExpressionNode(src,
					nodeFactory.newIdentifierNode(src,
							FORTRAN_FUNCTION_RETURN_PREFIX
									+ currentFunctionName));
		}
		returnNodes.add(nodeFactory.newReturnNode(src, returnExprNode));
		return returnNodes;
	}

	private List<BlockItemNode> translateStmtStop(MFTree stopStmt,
			MFScope scope) {
		assert stopStmt.numChildren() < 3; // No code no quiet

		int defaultStopCode = 0;
		List<BlockItemNode> stopNodes = new LinkedList<>();
		Source stopSrc = newSource(stopStmt);
		String exitFuncName = "exit";
		IdentifierNode exitFuncIdNode = nodeFactory.newIdentifierNode(stopSrc,
				exitFuncName);
		IdentifierExpressionNode exitFuncRefExprNode = nodeFactory
				.newIdentifierExpressionNode(stopSrc, exitFuncIdNode);
		List<ExpressionNode> argNodes = Arrays.asList(
				nodeFactory.newIntConstantNode(stopSrc, defaultStopCode));
		FunctionCallNode exitCallNode = nodeFactory.newFunctionCallNode(stopSrc,
				exitFuncRefExprNode, argNodes, null);

		useSTDLIB = true;
		if (!freedArrays.isEmpty())
			for (int i = freedArrays.size() - 1; i >= 0; i--)
				stopNodes.add(createArrayDestroy(freedArrays.get(i)));
		stopNodes.add(nodeFactory.newReturnNode(stopSrc, null));
		stopNodes.add((BlockItemNode) nodeFactory
				.newExpressionStatementNode(exitCallNode));
		return stopNodes;
	}

	private List<BlockItemNode> translateStmtWrite(MFTree writeStmt,
			MFScope scope) throws SyntaxException {
		LinkedList<BlockItemNode> printfNodes = new LinkedList<>();
		MFTree write = writeStmt.getChildByIndex(1);
		Source src = newSource(writeStmt);
		Source printSrc = newSource(write);
		MFTree outputList = writeStmt.getChildByIndex(3);
		IdentifierNode printfIdNode = nodeFactory.newIdentifierNode(printSrc,
				"printf");
		ExpressionNode printfFuncNode = nodeFactory
				.newIdentifierExpressionNode(printSrc, printfIdNode);
		ExpressionNode formatNode = null;
		FunctionCallNode printfCallNode = null;
		LinkedList<ExpressionNode> printfArgNodes = new LinkedList<>();
		String formatStr = "\"";
		StringToken formatToken = null;

		useSTDIO = true;
		for (int j = 0; j < outputList.numChildren(); j++) {
			MFTree outputItem = outputList.getChildByIndex(j);
			ExpressionNode printfArgNode = translateExpr(newSource(outputItem),
					outputItem.getChildByIndex(0), scope);

			printfArgNodes.add(printfArgNode);
			formatStr += "%s ";
		}
		formatStr += "\"";
		formatToken = tokenFactory.newStringToken(tokenFactory.newCivlcToken(0,
				formatStr, write.cTokens()[0].getFormation(),
				TokenVocabulary.FORTRAN));
		formatNode = nodeFactory.newStringLiteralNode(src, formatStr,
				formatToken.getStringLiteral());
		printfArgNodes.add(0, formatNode);
		printfCallNode = nodeFactory.newFunctionCallNode(src, printfFuncNode,
				printfArgNodes, null);
		printfNodes.add(nodeFactory.newExpressionStatementNode(printfCallNode));
		return printfNodes;
	}

	private BlockItemNode translateStmtIf(MFTree ifConstruct, MFScope scope)
			throws SyntaxException, ParseException {
		// TODO: else if & else
		// int numOfChildren = ifConstruct.numChildren();
		// PRPair prp = ifConstruct.prp();
		MFTree cond = ifConstruct.getChildByIndex(2);
		MFTree block = ifConstruct.getChildByIndex(3);
		ExpressionNode condExprNode = null;
		StatementNode trueBranchNode = null;
		StatementNode falseBranchNode = null;
		StatementNode ifStmtNode = null;
		// MFScope fScope = new MFScope(scope);

		condExprNode = translateExpr(newSource(cond), cond, scope);
		if (block.prp() == MFPUtils.ACTION_STMT)
			trueBranchNode = nodeFactory.newCompoundStatementNode(
					newSource(block),
					translateBlockItem(block.getChildByIndex(0),
							new MFScope(scope), null));

		if (falseBranchNode == null)
			ifStmtNode = nodeFactory.newIfNode(newSource(ifConstruct),
					condExprNode, trueBranchNode);
		// else
		// ifStmtNode = nodeFactory.newIfNode(newSource(ifConstruct),
		// condExprNode, trueBranchNode, falseBranchNode);
		return ifStmtNode;
	}

	private List<BlockItemNode> translateStmtParameter(MFTree parameterStmt,
			MFScope scope) throws SyntaxException {
		MFTree consts = parameterStmt.getChildByIndex(2);
		int numConsts = consts.numChildren();
		List<BlockItemNode> implicitConstDecls = new LinkedList<>();

		for (int i = 0; i < numConsts; i++) {
			MFTree constant = consts.getChildByIndex(0);
			MFTree constName = constant.getChildByIndex(0);
			MFTree constVal = constant.getChildByIndex(1);
			String constNameText = getName(constName);
			VariableDeclarationNode constVarDeclNode = scope
					.getLocalVarDeclNode(constNameText);
			ExpressionNode constInitValNode = translateExpr(newSource(constVal),
					constVal, scope);

			if (constVarDeclNode == null) {
				// The variable associated w/ parameter stmt is NOT declared.
				assert !scope.isImplicitNone();

				Source parameterSrc = newSource(parameterStmt);
				IdentifierNode constNameNode = nodeFactory
						.newIdentifierNode(parameterSrc, constNameText);

				constVarDeclNode = nodeFactory.newVariableDeclarationNode(
						parameterSrc, constNameNode,
						scope.getImplicitType(constNameText.charAt(0)),
						constInitValNode);
				implicitConstDecls.add(constVarDeclNode);
				scope.addVarDeclNode(constNameText, constVarDeclNode);
			} else {
				// The variable associated w/ parameter stmt is declared.
				constVarDeclNode.setInitializer(constInitValNode);
				constVarDeclNode.getTypeNode().setConstQualified(true);
			}
		}
		return implicitConstDecls;
	}

	private BlockItemNode translateStmtGoto(MFTree gotoStmt, MFScope scope) {
		MFTree targetLabel = gotoStmt
				.getChildByIndex(gotoStmt.numChildren() - 1);

		return nodeFactory.newGotoNode(newSource(gotoStmt),
				translateIdentifierLabel(targetLabel));
	}

	private BlockItemNode translateStmtExit(MFTree exitStmt, MFScope scope) {
		return nodeFactory.newBreakNode(newSource(exitStmt));
	}

	private ExpressionNode translateArrayConstructor(MFTree acSpec,
			MFScope scope) throws SyntaxException {
		MFTree acType = acSpec.getChildByIndex(0);
		MFTree acVals = acSpec.getChildByIndex(1);
		TypeNode arrayLiteralType = null;
		LinkedList<PairNode<DesignationNode, InitializerNode>> arrayLiteralNode = new LinkedList<PairNode<DesignationNode, InitializerNode>>();
		Boolean hasExplicitType = acType.prp() != MFPUtils.ABSENT;

		if (hasExplicitType) {
			assert false; // Has explicit type for array literals
		}
		for (int i = 0; i < acVals.numChildren(); i++) {
			MFTree acVal = acVals.getChildByIndex(i).getChildByIndex(0);
			ExpressionNode constantNode = null;

			if (acVal.prp() == MFPUtils.AC_IMPLIED_DO) {
				assert false; // Not an expr
			} else
				constantNode = translateExpr(newSource(acVal), acVal, scope);
			if (arrayLiteralType == null)
				arrayLiteralType = analyzeRawExprType(constantNode, scope);
			arrayLiteralNode.add(nodeFactory
					.newPairNode(constantNode.getSource(), null, constantNode));
		}
		return nodeFactory.newCompoundLiteralNode(newSource(acSpec),
				arrayLiteralType, nodeFactory.newCompoundInitializerNode(
						newSource(acVals), arrayLiteralNode));
	}

	/**
	 * R507: declaration construct <br>
	 * R508: specification construct <br>
	 * R513: other specification stmt <br>
	 * TODO: R1510 generic stmt
	 * 
	 * @param item
	 * @param bodyScope
	 * @param argsMap
	 * @param funcTypeNode
	 * @return
	 * @throws SyntaxException
	 * @throws ParseException
	 */
	private List<BlockItemNode> translateBlockItem(MFTree item, MFScope scope,
			FunctionTypeNode funcTypeNode)
			throws SyntaxException, ParseException {
		MFTree label = item.getChildByIndex(0);
		List<BlockItemNode> itemNodes = new ArrayList<BlockItemNode>();
		PRPair prp = item.prp();

		if (prp == MFPUtils.ACCESS_STMT)
			assert false;
		else if (prp == MFPUtils.ALLOCATABLE_STMT)
			assert false;
		else if (prp == MFPUtils.ASYNCHRONOUS_STMT)
			assert false;
		else if (prp == MFPUtils.BIND_STMT)
			assert false;
		else if (prp == MFPUtils.CODIMENSION_STMT)
			assert false;
		else if (prp == MFPUtils.COMMON_STMT)
			translateStmtCommon(item, scope);
		else if (prp == MFPUtils.DATA_STMT)
			itemNodes.addAll(translateStmtData(item, scope));
		else if (prp == MFPUtils.DERIVED_TYPE_DEF) {
			itemNodes.addAll(translateDerivedTypeDef(item, scope));
		} else if (prp == MFPUtils.DIMENSION_STMT)
			itemNodes.addAll(translateStmtDimension(item, scope));
		else if (prp == MFPUtils.ENTRY_STMT)
			assert false;
		else if (prp == MFPUtils.ENUM_DEF)
			assert false;
		else if (prp == MFPUtils.EQUIVALENCE_STMT)
			assert false;
		else if (prp == MFPUtils.EXTERNAL_STMT)
			assert false;
		else if (prp == MFPUtils.FORMAT_STMT)
			assert false;
		else if (prp == MFPUtils.INTENT_STMT)
			assert false;
		else if (prp == MFPUtils.INTERFACE_BLOCK)
			assert false;
		else if (prp == MFPUtils.INTRINSIC_STMT)
			assert false;
		else if (prp == MFPUtils.NAMELIST_STMT)
			assert false;
		else if (prp == MFPUtils.OPTIONAL_STMT)
			assert false;
		else if (prp == MFPUtils.PARAMETER_STMT)
			itemNodes.addAll(translateStmtParameter(item, scope));
		else if (prp == MFPUtils.POINTER_DECL)
			assert false;
		else if (prp == MFPUtils.PROCEDURE_DECLARATION_STMT)
			assert false;
		else if (prp == MFPUtils.PROTECTED_STMT)
			assert false;
		else if (prp == MFPUtils.SAVE_STMT)
			assert false;
		else if (prp == MFPUtils.TARGET_STMT)
			assert false;
		else if (prp == MFPUtils.TYPE_DECLARATION_STMT) {
			itemNodes.addAll(translateTypeDeclaration(item, scope));
		} else if (prp == MFPUtils.VOLATILE_STMT)
			assert false;
		else if (prp == MFPUtils.VALUE_STMT)
			assert false;
		else if (prp == MFPUtils.STMT_FUCNTION_STMT)
			assert false;
		else if (prp == MFPUtils.ALLOCATABLE_STMT)
			assert false;
		else if (prp == MFPUtils.ASSIGNMENT_STMT) {
			BlockItemNode exprStmtNode = translateStmtExpr(item, scope);

			if (dummyFuncRefArgs.size() > 0) {
				itemNodes.addAll(dummyFuncRefArgs);
				dummyFuncRefArgs.clear();
			}
			if (dummyFuncRefArrayArgPreStmts.size() > 0) {
				itemNodes.addAll(dummyFuncRefArrayArgPreStmts);
				dummyFuncRefArrayArgPreStmts.clear();
			}
			itemNodes.add(exprStmtNode);
			if (dummyFuncRefArrayArgPostStmts.size() > 0) {
				itemNodes.addAll(dummyFuncRefArrayArgPostStmts);
				dummyFuncRefArrayArgPostStmts.clear();
			}
		} else if (prp == MFPUtils.BACKSPACE_STMT)
			assert false;
		else if (prp == MFPUtils.CALL_STMT)
			itemNodes.addAll(translateStmtCall(item, scope));
		else if (prp == MFPUtils.CLOSE_STMT)
			assert false;
		else if (prp == MFPUtils.COMPUTED_GOTO_STMT)
			assert false;
		else if (prp == MFPUtils.CONTINUE_STMT)
			itemNodes.add((BlockItemNode) nodeFactory
					.newNullStatementNode(newSource(item)));
		else if (prp == MFPUtils.CYCLE_STMT)
			itemNodes.add((BlockItemNode) nodeFactory
					.newContinueNode(newSource(item)));
		else if (prp == MFPUtils.DEALLOCATE_STMT)
			assert false;
		else if (prp == MFPUtils.ENDFILE_STMT)
			assert false;
		else if (prp == MFPUtils.EXIT_STMT)
			itemNodes.add(translateStmtExit(item, scope));
		else if (prp == MFPUtils.FLUSH_STMT)
			assert false;
		else if (prp == MFPUtils.FORALL_STMT)
			assert false;
		else if (prp == MFPUtils.GOTO_STMT)
			itemNodes.add(translateStmtGoto(item, scope));
		else if (prp == MFPUtils.IF_STMT)
			itemNodes.add(translateStmtIf(item, scope));
		else if (prp == MFPUtils.INQUIRE_STMT)
			assert false;
		else if (prp == MFPUtils.LOCK_STMT)
			assert false;
		else if (prp == MFPUtils.NULLIFY_STMT)
			assert false;
		else if (prp == MFPUtils.OPEN_STMT)
			assert false;
		else if (prp == MFPUtils.POINTER_ASSIGNMENT_STMT)
			assert false;
		else if (prp == MFPUtils.PRINT_STMT)
			itemNodes.addAll(translateStmtPrint(item, scope));
		else if (prp == MFPUtils.READ_STMT)
			assert false;
		else if (prp == MFPUtils.RETURN_STMT)
			itemNodes.addAll(translateStmtReturn(item, scope));
		else if (prp == MFPUtils.REWIND_STMT)
			assert false;
		else if (prp == MFPUtils.STOP_STMT)
			itemNodes.addAll(translateStmtStop(item, scope));
		else if (prp == MFPUtils.SYNC_ALL_STMT)
			assert false;
		else if (prp == MFPUtils.SYNC_IMAGES_STMT)
			assert false;
		else if (prp == MFPUtils.SYNC_MEMORY_STMT)
			assert false;
		else if (prp == MFPUtils.UNLOCK_STMT)
			assert false;
		else if (prp == MFPUtils.WAIT_STMT)
			assert false;
		else if (prp == MFPUtils.WHERE_STMT)
			assert false;
		else if (prp == MFPUtils.WRITE_STMT)
			itemNodes.addAll(translateStmtWrite(item, scope));
		else if (prp == MFPUtils.FAIL_IMAGE_STMT)
			assert false;
		else if (prp == MFPUtils.SYNC_TEAM_STMT)
			assert false;
		else if (prp == MFPUtils.EVENT_POST_STMT)
			assert false;
		else if (prp == MFPUtils.EVENT_WAIT_STMT)
			assert false;
		else if (prp == MFPUtils.FORM_TEAM_STMT)
			assert false;
		else if (prp == MFPUtils.PRAGMA_STMT)
			itemNodes.add((BlockItemNode) processPragma(item, scope));
		else if (prp == MFPUtils.PRAGMA_TYPE_QUALIFIER_STMT)
			itemNodes.add((BlockItemNode) processPragma(item, scope));
		else {
			System.err.println(prp.toString());
			assert false;
		}
		if (label.prp() == MFPUtils.LABEL && !label.isNullToken(0)) {
			StatementNode labelledStmtNode = (StatementNode) itemNodes.get(0);
			IdentifierNode lblIdNode = translateIdentifierLabel(label);
			OrdinaryLabelNode lblDeclNode = nodeFactory
					.newStandardLabelDeclarationNode(newSource(label),
							lblIdNode, labelledStmtNode);
			itemNodes.set(0, nodeFactory.newLabeledStatementNode(
					newSource(item), lblDeclNode, labelledStmtNode));
		}
		return itemNodes;
	}

	private List<BlockItemNode> translateStmtData(MFTree stmtData,
			MFScope scope) throws SyntaxException {
		Source src = newSource(stmtData);
		List<BlockItemNode> itemNodes = new LinkedList<>();
		MFTree dataInitSet, dataVars, dataVals, dataVar, dataVal;
		TypeNode dataVarTypeNode;
		IdentifierNode dataVarIdNode;
		ExpressionNode dataValExprNode;
		VariableDeclarationNode dataVarDeclNode;

		for (int i = 2; i < stmtData.numChildren(); i++) {
			dataInitSet = stmtData.getChildByIndex(i);
			dataVals = dataInitSet.getChildByIndex(0);
			dataVars = dataInitSet.getChildByIndex(1);

			if (dataVals.numChildren() == 1) {
				dataVal = dataVals.getChildByIndex(0).getChildByIndex(0);
				dataValExprNode = translateExprConstants(dataVal, scope);
				for (int j = 0; j < dataVars.numChildren(); j++) {
					dataVar = dataVars.getChildByIndex(j);
					while (dataVar.numChildren() > 0)
						dataVar = dataVar.getChildByIndex(0);
					dataVarIdNode = this.translateIdentifier(dataVar);
					dataVarTypeNode = scope.getTypeByName(dataVarIdNode.name());
					if (dataVarTypeNode.kind() == TypeNodeKind.ARRAY) {
						assert false;
					} else {
						dataVarDeclNode = nodeFactory
								.newVariableDeclarationNode(src, dataVarIdNode,
										dataVarTypeNode,
										dataValExprNode.copy());
						itemNodes.add(dataVarDeclNode);
						scope.addVarDeclNode(dataVarIdNode.name(),
								dataVarDeclNode);
					}
				}
			} else
				assert false;
		}
		return itemNodes;
	}

	private List<BlockItemNode> translateStmtDimension(MFTree stmtDim,
			MFScope scope) throws SyntaxException {
		Source arraySrc;
		List<BlockItemNode> itemNodes = new LinkedList<>();
		MFTree declDims = stmtDim.getChildByIndex(2);
		MFTree declDim, arrayId, arrayDims;
		int numDeclDims = declDims.numChildren();
		String arrayName;
		ExpressionNode dimInfo[][];
		TypeNode arrayBaseTypeNode;
		IdentifierNode arrayIdNode;
		VariableDeclarationNode arrayDeclNode;
		FORTRAN_ARRAY_DESCRIPTOR_KIND arrDescKind = FORTRAN_ARRAY_DESCRIPTOR_KIND.ORIGIN;

		for (int i = 0; i < numDeclDims; i++) {
			declDim = declDims.getChildByIndex(i);
			arraySrc = newSource(declDim);
			arrayId = declDim.getChildByIndex(0);
			arrayName = getName(arrayId);
			arrayDims = declDim.getChildByIndex(1);
			dimInfo = processArrayDimInfo(arrayDims, scope);
			arrayBaseTypeNode = scope.getArrayElementTypeByName(arrayName);
			arrayIdNode = translateIdentifier(arrayId);

			if (scope.isParameter(arrayName)) {
				// A parameter named as {arrayName} shall have a
				// corresponding array type if the variable is
				// associated with a Dimension Statement.
				// Then, the actual variable 'X' used will be reshaped
				// from its original parameter, which is wrapped as '__X'.
				VariableDeclarationNode formalDeclNode = scope
						.getParameterVarDeclNode(arrayName);

				formalDeclNode.setIdentifier(nodeFactory.newIdentifierNode(
						dummySrc, FORTRAN_ARRAY_PARAM_PREFIX + arrayName));
				formalDeclNode.setTypeNode(genArrDescType(arraySrc));
				arrDescKind = FORTRAN_ARRAY_DESCRIPTOR_KIND.RESHAPE;
			} else if (scope.isDeclared(arrayName)) {
				// A local variable named as {arrayName} shall have a
				// corresponding array type if the variable is
				// associated with a Dimension Statement.
				// The out-dated declaration shall be updated.
				VariableDeclarationNode localDeclNode = scope
						.getLocalVarDeclNode(arrayName);

				localDeclNode.setIdentifier(nodeFactory.newIdentifierNode(
						dummySrc, FORTRAN_ARRAY_LOCAL_PREFIX + arrayName));
				localDeclNode.setTypeNode(genArrDescType(arraySrc));
			} // else do nothing additional for newly declared variables
			arrayDeclNode = createArrayDesc(arraySrc, arrayIdNode, dimInfo,
					arrayBaseTypeNode.copy(), scope, arrDescKind);
			scope.addVarDeclNode(arrayName, arrayDeclNode);
			itemNodes.add(arrayDeclNode);
		}
		return itemNodes;
	}

	private List<BlockItemNode> translateBlockItems(MFTree execPart,
			MFScope scope) throws SyntaxException, ParseException {
		int numExec = execPart.numChildren();
		OmpExecutableNode ompExecNode = null;
		List<BlockItemNode> itemNodes = new LinkedList<>();

		for (int i = 0; i < numExec; i++) {
			MFTree execCstr = execPart.getChildByIndex(i);
			PRPair prp = execCstr.prp();
			StatementNode stmtNode = null;

			// Extract the child from EXECUTION_PART_CONSTRUCT
			if (prp == MFPUtils.EXECUTION_PART_CONSTRUCT) {
				execCstr = execCstr.getChildByIndex(0);
				prp = execCstr.prp();
			}
			// Extract the child from EXECUTABLE_CONSTRUCT
			if (prp == MFPUtils.EXECUTABLE_CONSTRUCT) {
				execCstr = execCstr.getChildByIndex(0);
				prp = execCstr.prp();
			}

			if (prp == MFPUtils.ACTION_STMT) {
				List<BlockItemNode> tmpItemNodes = translateBlockItem(
						execCstr.getChildByIndex(0), scope, null);

				if (tmpItemNodes.size() == 1 && tmpItemNodes.get(0)
						.blockItemKind() == BlockItemKind.STATEMENT)
					stmtNode = (StatementNode) tmpItemNodes.get(0);
				else
					itemNodes.addAll(tmpItemNodes);
			} else if (prp == MFPUtils.ASSOCIATE_CONSTRUCT)
				assert false;
			else if (prp == MFPUtils.BLOCK_CONSTRUCT)
				assert false;
			else if (prp == MFPUtils.CASE_CONSTRUCT)
				assert false;
			else if (prp == MFPUtils.CRITICAL_CONSTRUCT)
				assert false;
			else if (prp == MFPUtils.DO_CONSTRUCT)
				itemNodes.add(translateConstructDo(execCstr, scope));
			else if (prp == MFPUtils.FORALL_CONSTRUCT)
				assert false;
			else if (prp == MFPUtils.IF_CONSTRUCT)
				itemNodes.add(translateConstructIf(execCstr, scope));
			else if (prp == MFPUtils.SELECT_TYPE_CONSTRUCT)
				assert false;
			else if (prp == MFPUtils.WHERE_CONSTRUCT)
				assert false;
			else if (prp == MFPUtils.PRAGMA_STMT)
				stmtNode = (StatementNode) processPragma(execCstr, scope);
			else if (prp == MFPUtils.FORMAT_STMT)
				processStmtFormat(execCstr, scope);
			else
				assert false;
			if (stmtNode != null)
				if (ompExecNode != null) {
					ompExecNode.setStatementNode(stmtNode);
					ompExecNode = null;
				} else {
					itemNodes.add(stmtNode);
					if (stmtNode.statementKind() == StatementKind.OMP) {
						ompExecNode = (OmpExecutableNode) stmtNode;
						if (ompExecNode.isComplete())
							ompExecNode = null;
					}
				}
		}
		return itemNodes;
	}

	private ASTNode translateCIVLPrimitives(MFTree civl_stmt, MFScope scope)
			throws SyntaxException {
		MFTree civlKey = civl_stmt;

		if (civlKey.numChildren() > 0)
			civlKey = civlKey.getChildByIndex(0);

		String keyStr = getName(civlKey).toLowerCase();

		useCIVLC = true;
		switch (keyStr) {
			case "$input" :
				isInputVarDecl = true;
				return null;
			case "$output" :
				isOutputVarDecl = true;
				return null;
			case "$assume" :
			case "$assert" :
				int numArgs = civl_stmt.numChildren() - 1;
				assert numArgs >= 0;
				Source civlKeySrc = newSource(civlKey);
				IdentifierNode civlFuncIDNode = nodeFactory
						.newIdentifierNode(civlKeySrc, keyStr);
				ExpressionNode civlFuncIDExprNode = nodeFactory
						.newIdentifierExpressionNode(civlKeySrc,
								civlFuncIDNode);
				ExpressionNode[] civlFuncArgNodes = new ExpressionNode[numArgs];

				for (int i = 0; i < numArgs; i++)
					civlFuncArgNodes[i] = translateExpr(newSource(),
							civl_stmt.getChildByIndex(i + 1), scope);
				return nodeFactory.newFunctionCallNode(newSource(civl_stmt),
						civlFuncIDExprNode, Arrays.asList(civlFuncArgNodes),
						null);
			default :
				throw new SyntaxException(
						"Syntax Error: invalid CIVL-F primitive: '" + keyStr
								+ "'",
						newSource(civl_stmt));
		}
	}

	private ASTNode translatePragma(MFTree pragma_stmt, MFScope scope)
			throws SyntaxException, ParseException {
		Source src = newSource(pragma_stmt);
		IdentifierNode pragmaNameNode = translateIdentifier(
				pragma_stmt.getChildByIndex(0));
		String pragmaName = pragmaNameNode.name().toUpperCase();
		PragmaNode pragmaNode = null;
		PragmaHandler pHandler = null;

		if (pragmaName.equals("CVL")) {
			return translateCIVLPrimitives(pragma_stmt.getChildByIndex(1),
					scope);
		} else {
			CivlcTokenSequence pragmaTokens = ptree
					.getTokenSourceProducer(pragma_stmt.getChildByIndex(1));
			CivlcToken pragmaEndToken = (CivlcToken) pragma_stmt
					.getChildByIndex(2).cTokens()[0];

			pragmaNode = nodeFactory.newPragmaNode(src, pragmaNameNode,
					pragmaTokens, pragmaEndToken);
			assert false;
		}
		pHandler = pragmaMap.get(pragmaName);
		if (pHandler == null) {
			pHandler = pragmaFactory.newHandler(pragmaName, ptree);
			pragmaMap.put(pragmaName, pHandler);
		}
		pragmaNameNode.setEntity(pHandler);
		return pHandler.processPragmaNode(pragmaNode, scope);
	}

	private StatementNode translateConstructDo(MFTree doConstruct,
			MFScope scope) throws SyntaxException, ParseException {
		IdentifierNode doVarIdNode = null;
		ExpressionNode doVarExprNode = null;
		ForLoopInitializerNode initNode = null;
		ExpressionNode condNode = null;
		ExpressionNode stepNode = null;
		MFTree doStmt = doConstruct.getChildByIndex(0);
		MFTree doBody = doConstruct.getChildByIndex(1);
		MFTree doEnd = doConstruct.getChildByIndex(2).getChildByIndex(0);
		StatementNode bodyNode = null;
		boolean hasDoCtrl = doStmt.numChildren() > 4;
		boolean hasDoEndStmt = doEnd.numChildren() > 4;

		// Proc Do Stmt
		if (hasDoCtrl) {
			MFTree doCtrl = doStmt.getChildByIndex(4);
			MFTree doVar = doCtrl.getChildByIndex(0);
			MFTree doVarInit = doCtrl.getChildByIndex(1);
			MFTree doVarCond = doCtrl.getChildByIndex(2);
			Source doCtrlSrc = newSource(doCtrl);
			ExpressionNode stepValNode = null;

			doVarIdNode = translateIdentifier(doVar);
			doVarExprNode = nodeFactory.newIdentifierExpressionNode(doCtrlSrc,
					doVarIdNode);
			initNode = nodeFactory.newOperatorNode(doCtrlSrc, Operator.ASSIGN,
					doVarExprNode.copy(),
					translateExpr(newSource(doVarInit), doVarInit, scope));
			condNode = nodeFactory.newOperatorNode(doCtrlSrc, Operator.LTE,
					doVarExprNode.copy(),
					translateExpr(newSource(doVarInit), doVarCond, scope));
			if (doCtrl.numChildren() < 4)
				stepValNode = nodeFactory.newIntConstantNode(doCtrlSrc, 1);
			else
				stepValNode = translateExpr(newSource(doVarInit),
						doCtrl.getChildByIndex(3), scope);
			stepNode = nodeFactory.newOperatorNode(doCtrlSrc, Operator.PLUSEQ,
					doVarExprNode.copy(), stepValNode);
			if (!scope.isDeclared(doVarIdNode.name()))
				scope.addUndeclaredIdentifiers(doVarIdNode.name());
		}
		// Proc Do End
		if (hasDoEndStmt) {
			MFTree doEndLabel = doEnd.getChildByIndex(0);
			MFTree doEndAction = doEnd.getChildByIndex(4);
			MFTree dummyExecConstruct = new MFTree(
					MFPUtils.EXECUTABLE_CONSTRUCT);
			String lblTxt = getName(doEndLabel);

			// Proc End Do
			if (!scope.containsLabel(lblTxt)) {
				doEndLabel.release();
				doEndAction.getChildByIndex(0).setChild(0, doEndLabel);
				doEndAction.release();
				dummyExecConstruct.addChild(doEndAction);
				doBody.addChild(dummyExecConstruct);
				scope.addLabels(lblTxt);
			}
		}
		// Proc Body
		bodyNode = translateBody(null, doBody, new MFScope(scope), null,
				doConstruct.prp());
		// Gen Civl For Loop
		return nodeFactory.newForLoopNode(newSource(doConstruct), initNode,
				condNode, stepNode, bodyNode, null);
	}

	private void processStmtFormat(MFTree formatStmt, MFScope scope) {
		MFTree formatKey = formatStmt.getChildByIndex(0);
		MFTree formatVal = formatStmt.getChildByIndex(2);
		MFTree formatItems = formatVal.getChildByIndex(0);
		int numFormatItems = formatItems.numChildren();
		String fmtKey = getName(formatKey);
		String fmtPattern = "";

		for (int i = 0; i < numFormatItems; i++) {
			String fmtItemText = getName(
					formatItems.getChildByIndex(i).getChildByIndex(0));

			if (fmtItemText.startsWith("'") || fmtItemText.startsWith("\"")) {
				fmtPattern += fmtItemText.substring(1,
						fmtItemText.length() - 1);
			} else {
				fmtPattern += " %s";
			}
		}
		formats.put(fmtKey, fmtPattern);
	}

	/**
	 * R863: implicit stmt
	 * 
	 * @param spec
	 * @param puScope
	 */
	private void processStmtImplicit(MFTree stmt, MFScope puScope) {
		int indexImplicitSpec = 2;
		MFTree spec = stmt.getChildByIndex(indexImplicitSpec);

		if (spec.prp() == MFPUtils.IMPLICIT_SPEC) {
			MFTree implicitSpec, typeSpec, letterSpecs, letterSpec;
			char charStart, charEnd;
			TypeNode implicitTypeNode = null;
			TypeNode implicitParamTypeNode = null;

			for (int i = 0; i < spec.numChildren(); i++) {
				implicitSpec = spec.getChildByIndex(i);
				typeSpec = implicitSpec.getChildByIndex(0).getChildByIndex(0);
				implicitTypeNode = translateType(typeSpec, puScope);
				if (implicitTypeNode.kind() == TypeNodeKind.BASIC) {
					implicitParamTypeNode = nodeFactory.newPointerTypeNode(
							implicitTypeNode.getSource(),
							implicitTypeNode.copy());
				} else {
					implicitParamTypeNode = implicitTypeNode.copy();
					assert false;
				}
				letterSpecs = implicitSpec.getChildByIndex(1);
				for (int j = 0; j < letterSpecs.numChildren(); j++) {
					letterSpec = letterSpecs.getChildByIndex(j);
					charStart = getName(letterSpec.getChildByIndex(0))
							.charAt(0);
					if (letterSpec.numChildren() == 1) {
						puScope.setImplicitTypes(charStart, charStart,
								implicitTypeNode, implicitParamTypeNode);
					} else {
						charEnd = getName(letterSpec.getChildByIndex(1))
								.charAt(0);
						puScope.setImplicitTypes(charStart, charEnd,
								implicitTypeNode, implicitParamTypeNode);
					}
				}
			}
		} else if (spec.prp() == MFPUtils.IMPLICIT_NONE_SPEC) {
			switch (spec.kind()) {
				case MFPUtils.NONE_PURE :
					puScope.setImplicitNone();
					break;
				case MFPUtils.NONE_EXTN :
				case MFPUtils.NONE_TYPE :
				default :
					assert false;
			}
		}
	}

	private void processStmtUse(MFTree useStmt, MFScope puScope) {
		MFTree useModule = useStmt.getChildByIndex(2);
		String moduleName = getName(useModule);

		switch (moduleName) {
			case "OMP_LIB" :
				useOMP = true;
				break;
			default :
				assert false;
		}
	}

	private ASTNode processPragma(MFTree pragma, MFScope scope)
			throws SyntaxException, ParseException {
		ASTNode pragmaNode = translatePragma(pragma, scope);

		if (pragmaNode == null)
			return null;
		else if (pragmaNode instanceof ExpressionNode)
			return nodeFactory
					.newExpressionStatementNode((ExpressionNode) pragmaNode);
		else if (pragmaNode instanceof StatementNode)
			return (StatementNode) pragmaNode;
		else
			assert false;

		return null;
	}

	private BlockItemNode translateConstructIf(MFTree ifConstruct,
			MFScope bodyScope) throws SyntaxException, ParseException {
		int numChildren = ifConstruct.numChildren();
		int numBlock = numChildren / 2;
		MFTree condStmt = null;
		MFTree condExpr = null;
		MFTree block = null;
		PRPair condPrp = null;
		ExpressionNode condExprNode = null;
		StatementNode trueBranchNode = null;
		StatementNode falseBranchNode = null;
		StatementNode ifStmtNode = null;

		for (int i = (numBlock - 1) * 2; i >= 0; i -= 2) {
			condStmt = ifConstruct.getChildByIndex(i);
			block = ifConstruct.getChildByIndex(i + 1);
			condPrp = condStmt.prp();
			if (condPrp == MFPUtils.ELSE_STMT) // else_stmt
				falseBranchNode = nodeFactory.newCompoundStatementNode(
						newSource(block),
						translateBlockItems(block, new MFScope(bodyScope)));
			else {
				condExpr = condStmt.getChildByIndex(condStmt.numChildren() - 2);
				condExprNode = translateExpr(newSource(condExpr), condExpr,
						bodyScope);
				trueBranchNode = nodeFactory.newCompoundStatementNode(
						newSource(block),
						translateBlockItems(block, new MFScope(bodyScope)));
				if (condPrp == MFPUtils.IF_THEN_STMT) // if_stmt
					ifStmtNode = nodeFactory.newIfNode(newSource(ifConstruct),
							condExprNode, trueBranchNode, falseBranchNode);
				else // else_if_stmt
					falseBranchNode = nodeFactory.newIfNode(
							newSource(ifConstruct), condExprNode,
							trueBranchNode, falseBranchNode);
			}
		}
		return ifStmtNode;
	}

	private String currentFunctionName = null;

	/**
	 * R504: specification part <br>
	 * R509: execution part <br>
	 * TODO: R511: internal subprogram part<br>
	 * 
	 * @param specPart
	 * @param execPart
	 * @param puScope
	 * @param funcTypeNode
	 * @param BodyPrp
	 * @return
	 * @throws SyntaxException
	 * @throws ParseException
	 */
	private CompoundStatementNode translateBody(MFTree specPart,
			MFTree execPart, MFScope puScope, FunctionTypeNode funcTypeNode,
			PRPair BodyPrp) throws SyntaxException, ParseException {
		int indexEndSpec = -1;

		// init
		dummyFuncRefArgsCtr = 0;
		if (BodyPrp == MFPUtils.MAIN_PROGRAM) {
			MFScope bodyScope = new MFScope(puScope);
			Source src = null;
			List<BlockItemNode> itemNodes = new LinkedList<>();
			OmpExecutableNode ompExecNode = null;

			// Process specification_part
			if (specPart != null) {
				int numSpec = specPart.numChildren();

				src = newSource(specPart);
				for (int i = 0; i < numSpec; i++) {
					MFTree spec = specPart.getChildByIndex(i);
					PRPair prp = spec.prp();

					if (prp == MFPUtils.DECLARATION_CONSTRUCT)
						itemNodes.addAll(
								translateBlockItem(spec.getChildByIndex(0),
										bodyScope, funcTypeNode));
					else if (prp == MFPUtils.IMPLICIT_STMT)
						processStmtImplicit(spec, puScope);
					else if (prp == MFPUtils.PARAMETER_STMT)
						assert false;
					else if (prp == MFPUtils.FORMAT_STMT)
						assert false;
					else if (prp == MFPUtils.ENTRY_STMT)
						assert false;
					else if (prp == MFPUtils.IMPORT_STMT)
						assert false;
					else if (prp == MFPUtils.USE_STMT)
						processStmtUse(spec, puScope);
					else
						assert false;
				}
			}
			indexEndSpec = itemNodes.size();
			// Process execution_part
			if (execPart != null)
				itemNodes.addAll(translateBlockItems(execPart, bodyScope));

			// Transformation caused by Omp pragma.
			int numItemNode = itemNodes.size();
			boolean isChanged = false;

			for (int i = 0; i < numItemNode; i++) {
				BlockItemNode itemNode = itemNodes.get(i);

				if (itemNode != null && itemNode instanceof OmpExecutableNode) {
					ompExecNode = (OmpExecutableNode) itemNode;
					if (!ompExecNode.isComplete()) {
						isChanged = true;
						if (ompExecNode instanceof OmpForNode) {
							OmpForNode ompForNode = (OmpForNode) ompExecNode;
							int collapse = ompForNode.collapse();

							if (collapse == 1) {
								StatementNode forStmtNode = (StatementNode) itemNodes
										.get(i + 1);

								itemNodes.set(i + 1, null);
								ompForNode.setStatementNode(forStmtNode);
							} else {
								ArrayList<BlockItemNode> forStmtNodes = new ArrayList<>(
										collapse);
								CompoundStatementNode stmtsNode;

								src = itemNodes.get(i + 1).getSource();
								for (int j = 1; j < collapse; j++) {
									StatementNode forStmtNode = (StatementNode) itemNodes
											.get(i + j);

									itemNodes.set(i + j, null);
									forStmtNodes.add(forStmtNode);
								}
								stmtsNode = nodeFactory
										.newCompoundStatementNode(src,
												forStmtNodes);
								ompForNode.setStatementNode(stmtsNode);
							}
							itemNodes.set(i, ompForNode);
						} else {
							StatementNode stmtNode = (StatementNode) itemNodes
									.get(i + 1);

							itemNodes.set(i + 1, null);
							ompExecNode.setStatementNode(stmtNode);
							itemNodes.set(i, ompExecNode);
						}
					}
				}
			}
			if (isChanged) {
				// Clean up 'null' elements in the old list
				LinkedList<BlockItemNode> newItemNodes = new LinkedList<>();

				for (int i = 0; i < numItemNode; i++) {
					BlockItemNode itemNode = itemNodes.get(i);

					if (itemNode != null)
						newItemNodes.add(itemNode);
				}
				itemNodes = newItemNodes;
			}
			if (!bodyScope.isImplicitNone()) {
				for (String undeclIdent : bodyScope.undeclaredIdentifiers()) {
					IdentifierNode identNode = nodeFactory
							.newIdentifierNode(dummySrc, undeclIdent);
					TypeNode typeNode = bodyScope
							.getImplicitType(undeclIdent.charAt(0));
					VariableDeclarationNode implicitVarDeclNode = nodeFactory
							.newVariableDeclarationNode(dummySrc, identNode,
									typeNode);

					itemNodes.add(indexEndSpec, implicitVarDeclNode);
				}
			}
			while (!freedArrays.isEmpty())
				itemNodes.add(createArrayDestroy(freedArrays.pop()));
			removeDummyVarDeclForFunction(itemNodes, indexEndSpec);
			return nodeFactory.newCompoundStatementNode(
					newSource(specPart, execPart), itemNodes);
		} else if (BodyPrp == MFPUtils.SUBROUTINE_SUBPROGRAM || //
				BodyPrp == MFPUtils.FUNCTION_SUBPROGRAM) {
			MFScope bodyScope = new MFScope(puScope);
			Source src = null;
			List<BlockItemNode> itemNodes = new LinkedList<>();
			VariableDeclarationNode rtnVarDeclNode = null;
			OmpExecutableNode ompExecNode = null;

			// Process specification_part
			if (specPart != null) {
				int numSpec = specPart.numChildren();

				src = newSource(specPart);
				for (int i = 0; i < numSpec; i++) {
					MFTree spec = specPart.getChildByIndex(i);
					PRPair prp = spec.prp();

					if (prp == MFPUtils.DECLARATION_CONSTRUCT)
						itemNodes.addAll(
								translateBlockItem(spec.getChildByIndex(0),
										bodyScope, funcTypeNode));
					else if (prp == MFPUtils.IMPLICIT_STMT)
						processStmtImplicit(spec, puScope);
					else if (prp == MFPUtils.PARAMETER_STMT)
						assert false;
					else if (prp == MFPUtils.FORMAT_STMT)
						assert false;
					else if (prp == MFPUtils.ENTRY_STMT)
						assert false;
					else if (prp == MFPUtils.IMPORT_STMT)
						assert false;
					else if (prp == MFPUtils.USE_STMT)
						assert false;
					else
						assert false;
				}
			}
			if (BodyPrp == MFPUtils.FUNCTION_SUBPROGRAM) {
				// Process return variable declaration
				IdentifierNode newRtnVarIdentNode = null;
				Boolean hasNoReturnType = funcTypeNode.getReturnType().kind()
						.equals(TypeNodeKind.VOID);

				// Check func. rtn. val. decl.
				for (BlockItemNode itemNode : itemNodes) {
					if (itemNode instanceof VariableDeclarationNode) {
						VariableDeclarationNode varDeclNode = (VariableDeclarationNode) itemNode;

						if (varDeclNode.getName().equals(currentFunctionName)) {
							rtnVarDeclNode = varDeclNode;
							break;
						}
					}
				}
				if (hasNoReturnType) {
					// Rtn. Type must be defined in func. body
					assert rtnVarDeclNode != null;
					// Update rtn. var. name
					IdentifierNode oldRtnVarIdentNode = rtnVarDeclNode
							.getIdentifier();
					TypeNode rtnVarTypeNode = rtnVarDeclNode.getTypeNode()
							.copy();

					oldRtnVarIdentNode.remove();
					newRtnVarIdentNode = nodeFactory.newIdentifierNode(
							oldRtnVarIdentNode.getSource(),
							FORTRAN_FUNCTION_RETURN_PREFIX
									+ currentFunctionName);
					rtnVarDeclNode.setIdentifier(newRtnVarIdentNode);
					// Update func. type
					funcTypeNode.setReturnType(rtnVarTypeNode);
				} else if (rtnVarDeclNode == null) {
					// Rtn. Type has been defined in func. stmt.
					// Rtn. var. does not defined in func. body
					newRtnVarIdentNode = nodeFactory.newIdentifierNode(
							funcTypeNode.getSource(),
							FORTRAN_FUNCTION_RETURN_PREFIX
									+ currentFunctionName);
					rtnVarDeclNode = nodeFactory.newVariableDeclarationNode(
							funcTypeNode.getSource(), newRtnVarIdentNode,
							funcTypeNode.getReturnType().copy());
					// Add rtn. var. decl.
					itemNodes.add(rtnVarDeclNode);
				}
			}
			indexEndSpec = itemNodes.size();
			// Process execution_part
			if (execPart != null)
				itemNodes.addAll(translateBlockItems(execPart, bodyScope));

			// Transformation caused by Omp pragma.
			int numItemNode = itemNodes.size();
			boolean isChanged = false;

			for (int i = 0; i < numItemNode; i++) {
				BlockItemNode itemNode = itemNodes.get(i);

				if (itemNode != null && itemNode instanceof OmpExecutableNode) {
					ompExecNode = (OmpExecutableNode) itemNode;
					if (!ompExecNode.isComplete()) {
						isChanged = true;
						if (ompExecNode instanceof OmpForNode) {
							OmpForNode ompForNode = (OmpForNode) ompExecNode;
							int collapse = ompForNode.collapse();

							if (collapse == 1) {
								StatementNode forStmtNode = (StatementNode) itemNodes
										.get(i + 1);

								itemNodes.set(i + 1, null);
								ompForNode.setStatementNode(forStmtNode);
							} else {
								ArrayList<BlockItemNode> forStmtNodes = new ArrayList<>(
										collapse);
								CompoundStatementNode stmtsNode;

								src = itemNodes.get(i + 1).getSource();
								for (int j = 1; j < collapse; j++) {
									StatementNode forStmtNode = (StatementNode) itemNodes
											.get(i + j);

									itemNodes.set(i + j, null);
									forStmtNodes.add(forStmtNode);
								}
								stmtsNode = nodeFactory
										.newCompoundStatementNode(src,
												forStmtNodes);
								ompForNode.setStatementNode(stmtsNode);
							}
							itemNodes.set(i, ompForNode);
						} else {
							StatementNode stmtNode = (StatementNode) itemNodes
									.get(i + 1);

							itemNodes.set(i + 1, null);
							ompExecNode.setStatementNode(stmtNode);
							itemNodes.set(i, ompExecNode);
						}
					}
				}
			}
			if (isChanged) {
				// Clean up 'null' elements in the old list
				LinkedList<BlockItemNode> newItemNodes = new LinkedList<>();

				for (int i = 0; i < numItemNode; i++) {
					BlockItemNode itemNode = itemNodes.get(i);

					if (itemNode != null)
						newItemNodes.add(itemNode);
				}
				itemNodes = newItemNodes;
			}
			if (!bodyScope.isImplicitNone()) {
				for (String undeclIdent : bodyScope.undeclaredIdentifiers()) {
					IdentifierNode identNode = nodeFactory
							.newIdentifierNode(dummySrc, undeclIdent);
					TypeNode typeNode = bodyScope
							.getImplicitType(undeclIdent.charAt(0));
					VariableDeclarationNode implicitVarDeclNode = nodeFactory
							.newVariableDeclarationNode(dummySrc, identNode,
									typeNode);

					itemNodes.add(indexEndSpec, implicitVarDeclNode);
				}
			}
			while (!freedArrays.isEmpty())
				itemNodes.add(createArrayDestroy(freedArrays.pop()));
			if (rtnVarDeclNode != null)
				itemNodes.add(nodeFactory.newReturnNode(src,
						nodeFactory.newIdentifierExpressionNode(src,
								nodeFactory.newIdentifierNode(src,
										FORTRAN_FUNCTION_RETURN_PREFIX
												+ currentFunctionName))));
			removeDummyVarDeclForFunction(itemNodes, indexEndSpec);
			return nodeFactory.newCompoundStatementNode(
					newSource(specPart, execPart), itemNodes);
		} else if (BodyPrp == MFPUtils.DO_CONSTRUCT
				|| BodyPrp == MFPUtils.IF_STMT) {
			MFScope bodyScope = new MFScope(puScope);
			Source src = null;
			List<BlockItemNode> itemNodes = new LinkedList<>();
			OmpExecutableNode ompExecNode = null;

			// Process execution_part
			if (execPart != null)
				itemNodes.addAll(translateBlockItems(execPart, bodyScope));

			// Transformation caused by Omp pragma.
			int numItemNode = itemNodes.size();
			boolean isChanged = false;

			for (int i = 0; i < numItemNode; i++) {
				BlockItemNode itemNode = itemNodes.get(i);

				if (itemNode != null && itemNode instanceof OmpExecutableNode) {
					ompExecNode = (OmpExecutableNode) itemNode;
					if (!ompExecNode.isComplete()) {
						isChanged = true;
						if (ompExecNode instanceof OmpForNode) {
							OmpForNode ompForNode = (OmpForNode) ompExecNode;
							int collapse = ompForNode.collapse();

							if (collapse == 1) {
								StatementNode forStmtNode = (StatementNode) itemNodes
										.get(i + 1);

								itemNodes.set(i + 1, null);
								ompForNode.setStatementNode(forStmtNode);
							} else {
								ArrayList<BlockItemNode> forStmtNodes = new ArrayList<>(
										collapse);
								CompoundStatementNode stmtsNode;

								src = itemNodes.get(i + 1).getSource();
								for (int j = 1; j < collapse; j++) {
									StatementNode forStmtNode = (StatementNode) itemNodes
											.get(i + j);

									itemNodes.set(i + j, null);
									forStmtNodes.add(forStmtNode);
								}
								stmtsNode = nodeFactory
										.newCompoundStatementNode(src,
												forStmtNodes);
								ompForNode.setStatementNode(stmtsNode);
							}
							itemNodes.set(i, ompForNode);
						} else {
							StatementNode stmtNode = (StatementNode) itemNodes
									.get(i + 1);

							itemNodes.set(i + 1, null);
							ompExecNode.setStatementNode(stmtNode);
							itemNodes.set(i, ompExecNode);
						}
					}
				}
			}
			if (isChanged) {
				// Clean up 'null' elements in the old list
				LinkedList<BlockItemNode> newItemNodes = new LinkedList<>();

				for (int i = 0; i < numItemNode; i++) {
					BlockItemNode itemNode = itemNodes.get(i);

					if (itemNode != null)
						newItemNodes.add(itemNode);
				}
				itemNodes = newItemNodes;
			}
			return nodeFactory.newCompoundStatementNode(
					newSource(specPart, execPart), itemNodes);
		} else
			assert false;
		return null;
	}

	private void removeDummyVarDeclForFunction(List<BlockItemNode> nodes,
			int end) {
		BlockItemNode itemNode;

		for (int i = 0; i < end; i++) {
			itemNode = nodes.get(i);
			if (itemNode instanceof VariableDeclarationNode
					&& funcDeclNodes.keySet().contains(
							((VariableDeclarationNode) itemNode).getName())) {
				nodes.remove(i);
				end--;
			}
		}
	}

	/**
	 * R1401: main program <br>
	 * R1529: function subprogram
	 * 
	 * @param progUnit
	 * @param scope
	 * @param rule
	 * @return
	 * @throws SyntaxException
	 * @throws ParseException
	 */
	private BlockItemNode translateMainProgramUnit(MFTree progUnit,
			MFScope scope, PRPair prp) throws SyntaxException, ParseException {
		BlockItemNode puItem = null;
		int numChildren = progUnit.numChildren();
		MFTree progStmt = progUnit.getChildByIndex(0);
		MFTree progId = progStmt.getChildByIndex(2);
		MFTree args = null;
		MFTree specPart = progUnit.getChildByIndex(1);
		MFTree execPart = numChildren > 3 ? progUnit.getChildByIndex(2) : null;
		MFScope puScope = null;
		Source src = newSource(progUnit);
		IdentifierNode nameNode;
		FunctionTypeNode typeNode;
		CompoundStatementNode bodyNode = null;

		translatedCommonVarNames.clear();
		if (prp == MFPUtils.MAIN_PROGRAM)
			nameNode = nodeFactory.newIdentifierNode(newSource(progId), "main");
		else
			nameNode = translateIdentifier(progId);
		puIdStack.push(nameNode);
		if (progStmt.numChildren() > 3)
			args = progStmt.getChildByIndex(3);
		// TODO: prefix, suffix
		typeNode = translateFunctionType(null, progId, args, prp, scope);
		puScope = new MFScope(scope, typeNode, getName(progId), astFactory);
		bodyNode = translateBody(specPart, execPart, puScope, null, prp);
		puItem = nodeFactory.newFunctionDefinitionNode(src, nameNode, typeNode,
				null, bodyNode);
		commonblockMemberMap = new HashMap<>();
		puIdStack.pop();
		return puItem;
	}

	private BlockItemNode translateProgramFunction(MFTree function,
			MFScope scope, PRPair prp) throws SyntaxException, ParseException {
		BlockItemNode funcItem = null;
		int numChildren = function.numChildren();
		String funcNameText = null;
		MFTree funcPrfx = function.getChildByIndex(0);
		MFTree funcStmt = function.getChildByIndex(1);
		MFTree funcName = null;
		MFTree funcArgs = null;
		// MFTree funcRtrn = null;
		MFTree specPart = function.getChildByIndex(2);
		MFTree execPart = numChildren > 4 ? function.getChildByIndex(3) : null;
		MFScope funcScope = null;
		Source src = newSource(function);
		IdentifierNode nameNode = null;
		FunctionTypeNode typeNode = null;
		CompoundStatementNode bodyNode = null;
		FunctionDeclarationNode dummyFuncDeclNode = null;

		translatedCommonVarNames.clear();
		// Check Function Label
		if (funcStmt.getChildByIndex(0).numChildren() > 0) {
			// Translate Function Label
			assert false;
		}
		// Translate Function Name
		funcName = funcStmt.getChildByIndex(2);
		nameNode = translateIdentifier(funcName);
		funcNameText = getName(funcName);
		puIdStack.push(nameNode);
		// Translate Function Arg. List
		if (funcStmt.numChildren() > 3)
			funcArgs = funcStmt.getChildByIndex(3);
		// Translate Function Type
		typeNode = translateFunctionType(funcPrfx, funcName, funcArgs, prp,
				scope);
		funcScope = new MFScope(scope, typeNode, funcNameText, astFactory);
		if (funcDeclNodes.containsKey(funcNameText)) {
			// Func. decl. has been created when any of its calls was processed.
			// Then, func. type info is updated based on func. def.
			dummyFuncDeclNode = funcDeclNodes.get(funcNameText);
			dummyFuncDeclNode.setTypeNode(typeNode);
		} else {
			// Func. decl. has not been added
			// Then, the decl. is created based on func. def.
			dummyFuncDeclNode = nodeFactory.newFunctionDeclarationNode(src,
					translateIdentifier(funcName), typeNode.copy(), null);
			processDummyFuncOrSubrDeclaration(funcNameText, dummyFuncDeclNode);
		}
		// Check function prefix
		if (funcPrfx != null) {
			// Process funcPrfx:
			for (int i = 0; i < funcPrfx.numChildren(); i++) {
				MFTree prefixSpec = funcPrfx.getChildByIndex(i);
				int prefixSpecKind = prefixSpec.kind();

				if (prefixSpec.numChildren() > 0) {
					// Omitted, prefix for return type has been processed.
				} else if (prefixSpecKind == MFPUtils.PFX_PURE) {
					dummyFuncDeclNode.setAttribute(keyAttrPure, Boolean.TRUE);
				} else if (prefixSpecKind == MFPUtils.PFX_RECURSIVE) {
					dummyFuncDeclNode.setAttribute(keyAttrRecursive,
							Boolean.TRUE);
				} else
					assert false;
			}
		}
		// TODO: suffix

		currentFunctionName = funcNameText;
		bodyNode = translateBody(specPart, execPart, funcScope, typeNode, prp);
		funcItem = nodeFactory.newFunctionDefinitionNode(src, nameNode,
				typeNode.copy(), null, bodyNode);
		// Update possible function decl type info
		if (currentFunctionName != null
				&& funcDeclNodes.containsKey(currentFunctionName))
			this.funcDeclNodes.get(this.currentFunctionName)
					.setTypeNode(typeNode.copy());
		commonblockMemberMap = new HashMap<>();
		currentFunctionName = null;
		puIdStack.pop();
		return funcItem;
	}

	/**
	 * R502: program unit<br>
	 * R503: external subprogram
	 * 
	 * @param ptree
	 *                  the root of a FORTRAN parse tree
	 * @param scope
	 *                  the root scope
	 * @return a {@link List} of {@link BlockItemNode} representing each program
	 *         unit.
	 * @throws SyntaxException
	 * @throws ParseException
	 */
	private List<BlockItemNode> translateProgramUnit(MFTree ptree,
			MFScope scope) throws SyntaxException, ParseException {
		PRPair prp = ptree.prp();
		List<BlockItemNode> itemNodes = new LinkedList<>();

		// Get the kind of program unit 'ptree'
		if (prp == MFPUtils.MAIN_PROGRAM)
			itemNodes.add(translateMainProgramUnit(ptree, scope, prp));
		else if (prp == MFPUtils.SUBROUTINE_SUBPROGRAM)
			itemNodes.add(translateMainProgramUnit(ptree, scope, prp));
		else if (prp == MFPUtils.FUNCTION_SUBPROGRAM)
			itemNodes.add(translateProgramFunction(ptree, scope, prp));
		else if (prp == MFPUtils.MODULE_SUBPROGRAM)
			assert false;
		else if (prp == MFPUtils.SUBROUTINE_SUBPROGRAM)
			assert false;
		else if (prp == MFPUtils.BLOCK_DATA)
			assert false;
		else
			assert false;
		return itemNodes;
	}

	private List<BlockItemNode> translateStmtCall(MFTree callStmt,
			MFScope scope) throws SyntaxException {
		int numArrayArgs = 0;
		Source src = newSource(callStmt);
		List<BlockItemNode> itemNodes = new LinkedList<>();
		MFTree funcRef = callStmt.getChildByIndex(2).getChildByIndex(0)
				.getChildByIndex(0);
		MFTree funcName = funcRef.getChildByIndex(0);
		Boolean hasArgList = funcRef.numChildren() > 1;
		IdentifierNode funcIdNode = translateIdentifier(funcName);
		ExpressionNode funcRefNode = nodeFactory
				.newIdentifierExpressionNode(src, funcIdNode);
		List<ExpressionNode> actualCallArgNodes = new LinkedList<ExpressionNode>();
		List<VariableDeclarationNode> dummyFuncDeclFormalNodes = new LinkedList<VariableDeclarationNode>();
		SequenceNode<VariableDeclarationNode> formalsNode = null;
		String argName;
		TypeNode formalTypeNode = null;
		TypeNode tempNode = null;

		if (hasArgList) {
			MFTree args = funcRef.getChildByIndex(1);
			int numArgs = args.numChildren();

			for (int i = 0; i < numArgs; i++) {
				MFTree arg = args.getChildByIndex(i).getChildByIndex(0);
				Source argSrc = newSource(arg);
				ExpressionNode argNode = translateExpr(argSrc, arg, scope);
				IdentifierNode formalNameNode = nodeFactory.newIdentifierNode(
						argNode.getSource(), "__civl_dummy_arg_" + i);
				Boolean notSection = arraySectionDecls.isEmpty();

				while (!arraySectionDecls.isEmpty()) {
					itemNodes.add(arraySectionDecls.pop());
					numArrayArgs++;
				}
				switch (argNode.expressionKind()) {
					case OPERATOR :
						if (((OperatorNode) argNode)
								.getOperator() == Operator.DEREFERENCE) {
							argNode = ((OperatorNode) argNode).getArgument(0)
									.copy();
							argNode.remove();

							if (argNode instanceof IdentifierExpressionNode) {
								// Arg is an identifier w/ a scalar type
								argName = ((IdentifierExpressionNode) argNode)
										.getIdentifier().name();
								formalTypeNode = scope.getTypeByName(argName)
										.copy();
								tempNode = formalTypeNode;
								if (tempNode.kind() == TypeNodeKind.BASIC) {
									argNode = nodeFactory.newOperatorNode(src,
											Operator.ADDRESSOF, argNode);
									formalTypeNode = nodeFactory
											.newPointerTypeNode(
													argNode.getSource(),
													formalTypeNode.copy());
								}
							} else if (argNode instanceof CastNode) {
								formalTypeNode = ((CastNode) argNode)
										.getCastType();
							}
						} else
							assert false;
						break;
					case IDENTIFIER_EXPRESSION :
						argName = ((IdentifierExpressionNode) argNode)
								.getIdentifier().name();
						formalTypeNode = scope.getTypeByName(argName).copy();
						tempNode = formalTypeNode;
						if (tempNode.kind() == TypeNodeKind.BASIC) {
							argNode = nodeFactory.newOperatorNode(src,
									Operator.ADDRESSOF, argNode);
							formalTypeNode = nodeFactory.newPointerTypeNode(
									argNode.getSource(), formalTypeNode.copy());
						}
						if (notSection && tempNode
								.kind() == TypeNodeKind.TYPEDEF_NAME) {
							IdentifierNode arrayArgIdNode = nodeFactory
									.newIdentifierNode(src,
											FORTRAN_ARRAY_ARG_PREFIX + argName);
							VariableDeclarationNode arrayArgVarDeclNode = createArrayDesc(
									dummySrc, arrayArgIdNode, null, null, scope,
									FORTRAN_ARRAY_DESCRIPTOR_KIND.SECTION_ARG);

							itemNodes.add(arrayArgVarDeclNode);
							argNode = nodeFactory.newIdentifierExpressionNode(
									src, arrayArgIdNode.copy());
							numArrayArgs++;
						}
						if (tempNode.kind() == TypeNodeKind.ARRAY)
							assert false;
						break;
					case CONSTANT :
						argNode = argNode.copy();
						if (argNode instanceof IntegerConstantNode) {
							tempNode = nodeFactory.newBasicTypeNode(
									argNode.getSource(), BasicTypeKind.INT);
							formalTypeNode = nodeFactory
									.newPointerTypeNode(dummySrc, tempNode);
						} else
							assert false;
						break;
					default :
						assert false;
				}
				actualCallArgNodes.add(argNode);
				dummyFuncDeclFormalNodes
						.add(nodeFactory.newVariableDeclarationNode(argSrc,
								formalNameNode, formalTypeNode.copy()));
			}
		}
		formalsNode = nodeFactory.newSequenceNode(src,
				"DummySubroutineFormalDeclList", dummyFuncDeclFormalNodes);

		FunctionCallNode callNode = nodeFactory.newFunctionCallNode(src,
				funcRefNode, actualCallArgNodes, null);
		FunctionTypeNode dummyFuncTypeNode = nodeFactory.newFunctionTypeNode(
				src, nodeFactory.newVoidTypeNode(src), formalsNode, false);
		FunctionDeclarationNode dummyFuncDeclNode = nodeFactory
				.newFunctionDeclarationNode(src, funcIdNode.copy(),
						dummyFuncTypeNode, null);

		processDummyFuncOrSubrDeclaration(getName(funcName), dummyFuncDeclNode);
		itemNodes.add((BlockItemNode) nodeFactory
				.newExpressionStatementNode(callNode));
		while (numArrayArgs > 0) {
			itemNodes.add(createArrayDestroy(freedArrays.pop()));
			numArrayArgs--;
		}
		return itemNodes;
	}

	private HashSet<String> commonBlockNames = new HashSet<String>();
	private HashMap<String, String> translatedCommonVarNames = new HashMap<>();

	private void translateStmtCommon(MFTree commonStmt, MFScope scope)
			throws SyntaxException {
		int INDEX_FIRST_BLOCK = 2;
		int idxBlock = INDEX_FIRST_BLOCK;

		while (idxBlock < commonStmt.numChildren()) {
			MFTree commonblock = commonStmt.getChildByIndex(idxBlock);
			MFTree commonblockFields = commonblock.getChildByIndex(0);
			Source srcFields = newSource(commonblockFields);
			String commonblockName = getName(commonblock);
			int numFields = commonblockFields.numChildren();
			int idxField = 0;

			if (commonBlockNames.contains(commonblockName)) {
				assert false;
			} else {
				commonBlockNames.add(commonblockName);
				while (idxField < numFields) {
					MFTree fieldObj = commonblockFields
							.getChildByIndex(idxField);
					MFTree fieldId = fieldObj.getChildByIndex(0);
					String fieldName = getName(fieldId);
					String commonVarName = FORTRAN_COMMON_BLOCK_PREFIX
							+ commonblockName + "_" + fieldName;
					Source srcFieldName = newSource(fieldId);
					IdentifierNode commonVarIdNode = nodeFactory
							.newIdentifierNode(srcFieldName, commonVarName);
					TypeNode commonVarTypeNode = scope.getTypeByName(fieldName)
							.copy();
					VariableDeclarationNode commonVarDeclNode = null;

					if (fieldObj.numChildren() > 1) {
						// Array Type
						Source srcFieldObj = newSource(fieldObj);
						MFTree arraySpec = fieldObj.getChildByIndex(1);
						ExpressionNode dimInfo[][] = processArrayDimInfo(
								arraySpec.getChildByIndex(0), scope);

						commonVarTypeNode = genArrDescType(srcFieldObj);
						commonVarDeclNode = createArrayDesc(srcFieldObj,
								commonVarIdNode, dimInfo, commonVarTypeNode,
								scope, FORTRAN_ARRAY_DESCRIPTOR_KIND.ORIGIN);
					} else {
						// Scalar Type
						commonVarDeclNode = nodeFactory
								.newVariableDeclarationNode(srcFields,
										commonVarIdNode,
										scope.getTypeByName(fieldName).copy());
					}
					commonVarDeclNodes.add(commonVarDeclNode);
					translatedCommonVarNames.put(fieldName, commonVarName);
					idxField++;
				}
			}
			idxBlock++;
		}
	}

	private TypeNode genArrDescType(Source src) {
		String FARR_DESC = "farr_desc";
		IdentifierNode fArrDescNode = nodeFactory.newIdentifierNode(src,
				FARR_DESC);

		return nodeFactory.newTypedefNameNode(fArrDescNode, null);
	}

	// The driver of generating CIVL AST from FORTRAN parse tree.
	private void genASTRoot() throws SyntaxException, ParseException {
		int numProgUnit = ptree.numChildren();
		MFScope rootScope = new MFScope();
		Source rootSrc = newSource(ptree);

		assert numProgUnit > 0;
		for (int i = 0; i < numProgUnit; i++)
			programUnits.addAll(
					translateProgramUnit(ptree.getChildByIndex(i), rootScope));
		root = nodeFactory.newTranslationUnitNode(rootSrc, programUnits);
	}

	// Interfaces or non-private functions
	/**
	 * @return a CIVL AST generated from {@link MFTree} <code>this.root</code>
	 */
	public AST generateAST() {
		AST civlAst = null;
		SourceFile rootSrcFile = new SourceFile(new File(filePath),
				srcFiles.size());

		srcFiles.put(rootSrcFile.getIndex(), rootSrcFile);
		formations.add(tokenFactory.newInclusion(rootSrcFile));
		dummySrc = tokenFactory
				.newSource(tokenFactory.newCivlcToken(CivlcTokenConstant.ABSENT,
						SRC_INFO, formations.peek(), TokenVocabulary.FORTRAN));
		try {
			genASTRoot();
			// Add global variables translated from common block.
			root.insertChildren(0, commonVarDeclNodes);
			// Add $input/$output variables
			root.insertChildren(0, inputOutputVarDeclNodes);
			addLibASTNodes();
			civlAst = astFactory.newAST(root, srcFiles.values(),
					hasProgramEntry);
			// civlAst.prettyPrint(System.out, true);
		} catch (SyntaxException | PreprocessorException | ParseException e) {
			e.printStackTrace();
		}
		return civlAst;
	}

}
