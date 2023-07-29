package edu.udel.cis.vsl.abc.transform.common;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity.EntityKind;
import edu.udel.cis.vsl.abc.ast.entity.IF.OrdinaryEntity;
import edu.udel.cis.vsl.abc.ast.entity.IF.ProgramEntity;
import edu.udel.cis.vsl.abc.ast.entity.IF.Scope.ScopeKind;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.DeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.ArrayTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode.TypeNodeKind;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.BaseTransformer;
import edu.udel.cis.vsl.abc.transform.IF.Transform;
import edu.udel.cis.vsl.abc.transform.IF.Transformer;

/**
 * <p>
 * For declarations of variables with external-linkage, their "extern"
 * specifiers can be removed since all translation units have already been
 * linked togather. Variable declarations in file scopes are still considered
 * having external linkages. For declarations of variables with external-linkage
 * in block scopes, some actions are needed.
 * </p>
 * <p>
 * For declarations of variables with external-linkage in block scopes, one
 * cannot simply remove their "extern" specifiers because that will them no
 * longer have extern linkages. In such case, one must move the declaration to
 * the file scope. Followings are two examples of such cases: <br>
 * <b> Example 1.</b> <code>
 * void f() {
 *   int x;
 *   if (1) {
 *     extern int x;
 *     
 *     $assert(x);
 *   }
 * }
 * int x = 1;
 * int main() {
 *   f();
 * }
 * </code><br>
 * In Example 1, the variable x declaration with external linkage in a block
 * scope has a visible prior local variable declaration x. In this case, even if
 * the declaration of x with external linkage has been moved to the top, there
 * is still incorrect. The identifier x in the assertion will incorrectly refer
 * to the local x. For deal with such cases, one must rename the variable x who
 * has external linkage to a unique name.<br>
 * <b> Example 2.</b> <code>
 * void f() {
 *   int x;
 *   if (1) {
 *     typedef struct {int x;} T;
 *     extern T x;
 *     
 *     $assert(x.x);
 *   }
 * }
 * typedef struct {int x;} T;
 * T x = {1};
 * int main() {
 *   f();
 * }
 * </code> <br>
 * In Example 2, one must not only move the variable declaration of x with
 * external linkage but also move the type definition of T and evething
 * dependent to the top as well.
 * </p>
 * 
 * @author xxxx
 */
public class ExternLinkageVariableRenamer extends BaseTransformer {
	/**
	 * counting instances of this class.
	 */
	private static int instanceId = 0;

	/**
	 * The short code used to identify this {@link Transformer}.
	 */
	public final static String CODE = "extRename";

	/**
	 * The long name used to identify this {@link Transformer}.
	 */
	public final static String LONG_NAME = "ExternLinkageVariableRenamer";

	/**
	 * The short description of what this {@link Transformer} does.
	 */
	public final static String SHORT_DESCRIPTION = "renames variables of external linkage that "
			+ "have declarations in block scopes";

	/**
	 * The name suffix that will be appened to names of the renaming variables
	 * to form new names.
	 */
	private final static String newNameSuffix = "$ext";

	/**
	 * The numeric identifier distinguishes different programs. External linkage
	 * variables in different programs shall be renamed to different unique new
	 * names.
	 */
	private final int programID;

	private TypeDefinitionDependenciesFinder typeDependencyFinder;

	public ExternLinkageVariableRenamer(ASTFactory astFactory) {
		super(CODE, LONG_NAME, SHORT_DESCRIPTION, astFactory);
		// each program has an instance of this transformer:
		this.programID = instanceId++;
		this.typeDependencyFinder = new TypeDefinitionDependenciesFinder(
				astFactory.getNodeFactory());
	}

	/**
	 * @param externVarDecl
	 *                          a variable declaration with external linkage
	 * @return All declarations of the same entity as the given one that appear
	 *         in block scopes.
	 */
	private List<ExternVariableDeclarations> externDeclarationPreprocessing(
			AST ast) {
		Iterator<OrdinaryEntity> ordEntIter = ast.getExternalEntities();
		List<ExternVariableDeclarations> results = new LinkedList<>();

		while (ordEntIter.hasNext()) {
			OrdinaryEntity entity = ordEntIter.next();

			if (entity.getEntityKind() != EntityKind.VARIABLE)
				continue;

			LinkedList<VariableDeclarationNode> allDeclarations = new LinkedList<>();

			for (DeclarationNode varDecl : entity.getDeclarations())
				allDeclarations.add((VariableDeclarationNode) varDecl);

			ExternVariableDeclarations result = new ExternVariableDeclarations(
					allDeclarations);

			results.add(result);
		}
		return results;
	}

	/**
	 * See {@link #cleanExternVariableDeclarations(AST, Map)} point 1, 2 and 3
	 * 
	 * @param root
	 * @param varsDecls
	 */
	private void moveBlockExternDeclarationToTop(
			SequenceNode<BlockItemNode> root,
			List<ExternVariableDeclarations> varsDecls) {
		LinkedHashSet<BlockItemNode> allToTop = new LinkedHashSet<>();

		for (ExternVariableDeclarations varDecls : varsDecls) {
			int numDeclsInBlock = varDecls.numBlockDeclarations();

			for (int i = 0; i < numDeclsInBlock; i++) {
				VariableDeclarationNode varDecl = varDecls
						.getNthDeclarationInBlock(i);
				TypeNode varType = varDecl.getTypeNode();

				// delete const qualifiers
				if (varType.isConstQualified())
					varType.setConstQualified(false);
				allToTop.addAll(typeDependencyFinder
						.getDependentDeclarations(varDecl.getTypeNode()));
				varDecl.setExternStorage(false);
				allToTop.add(varDecl);
			}
		}
		// move all to top:
		for (BlockItemNode item : allToTop)
			item.remove();

		List<BlockItemNode> allToTopList = new LinkedList<>();

		allToTopList.addAll(allToTop);
		root.insertChildren(0, allToTopList);
	}

	/**
	 * see {@link #cleanExternVariableDeclarations(AST, Map)} point 3 and 4
	 * 
	 * @param externVarsDecls
	 */
	private void deleteExternSpecifiers(
			List<ExternVariableDeclarations> externVarsDecls) {
		for (ExternVariableDeclarations externVarDecls : externVarsDecls) {
			int numPureDeclInFile = externVarDecls.numFilePureDeclarations();

			for (int i = 0; i < numPureDeclInFile; i++) {
				externVarDecls.getNthPureDeclarationInFile(i)
						.setExternStorage(false);
				// delete const qualifiers as well:
				externVarDecls.getNthPureDeclarationInFile(i).getTypeNode()
						.setConstQualified(false);
			}
			if (externVarDecls.definitionIdx >= 0) {
				externVarDecls.getDefinition().setExternStorage(false);
				// delete const qualifiers as well:
				externVarDecls.getDefinition().getTypeNode()
						.setConstQualified(false);
			}
			// definition to assignment if there exists a prior declaration:
			if (numPureDeclInFile > 0 && externVarDecls.definitionIdx > 0) {
				VariableDeclarationNode definition = externVarDecls
						.getDefinition();
				InitializerNode initializer = definition.getInitializer();

				if (initializer != null) {
					ExpressionNode rhs;
					StatementNode assignment;
					ASTNode parent = definition.parent();
					int childIdx = definition.childIndex();
					Source source = definition.getSource();
					NodeFactory nf = astFactory.getNodeFactory();

					definition.remove();
					initializer.remove();
					if (initializer.nodeKind() == NodeKind.EXPRESSION)
						rhs = (ExpressionNode) initializer;
					else {
						// compund initializer:
						CompoundInitializerNode compoundInit = (CompoundInitializerNode) initializer;
						TypeNode typeNode = definition.getTypeNode();

						typeNode.remove();
						rhs = nf.newCompoundLiteralNode(initializer.getSource(),
								typeNode, compoundInit);
					}

					IdentifierNode identifier = definition.getIdentifier();

					identifier.remove();
					assignment = nf.newExpressionStatementNode(
							nf.newOperatorNode(source, Operator.ASSIGN,
									astFactory.getNodeFactory()
											.newIdentifierExpressionNode(source,
													identifier),
									rhs));
					parent.setChild(childIdx, assignment);
				}
			}
		}
	}

	/**
	 * Clean and collect Variable Declarations with External Linkages (VDEL):
	 * <ol>
	 * <li>For any VDEL appears in a block scope, and if there exists a prior
	 * variable declaration which refers to a different entity but has the same
	 * identifier with this VDEL, entity referred by this VDEL needs to be
	 * renamed.</li>
	 * <li>For any VDEL appears in a block scope, the VDEL shall be moved to the
	 * very top of this program. If the type of this VDEL, contains
	 * (recursively) either a declaration of a typedef or a struct type, whose
	 * definition is not in the file scope. One must move the type definition to
	 * the very top of the program as well.</li>
	 * <li>For any VDEL, remove its "extern" identifier.</li>
	 * <li>For the definition (if it exists) of a VDEL, transform the definition
	 * to an assignment.</li>
	 * <li>Delete the "const" qualifier for each VDEL if it exists.</li>
	 * </ol>
	 * 
	 * @param ast
	 *                       The AST which represents the program where extern
	 *                       variable declarations will be cleaned.
	 * @param newNameMap
	 *                       A map from {@link Entity} to their new names if
	 *                       they need to be renamed.
	 * @return A new AST where extern variable declarations are cleaned.
	 * @throws SyntaxException
	 *                             If there exists any semantics error after the
	 *                             cleaning work.
	 */
	private AST cleanExternVariableDeclarations(AST ast,
			Map<Entity, String> newNameMap) throws SyntaxException {
		List<ExternVariableDeclarations> externVarsDecls = externDeclarationPreprocessing(
				ast);

		// collecting declarations that needs renaming:
		for (ExternVariableDeclarations decl : externVarsDecls) {
			int numDeclsInBlock = decl.numBlockDeclarations();

			for (int i = 0; i < numDeclsInBlock; i++) {
				VariableDeclarationNode declInBlock = decl
						.getNthDeclarationInBlock(i);
				Entity priorEntity = declInBlock.getScope().getParentScope()
						.getLexicalOrdinaryEntity(false, declInBlock.getName());
				Entity thisEntity = declInBlock.getEntity();

				if (priorEntity != null && !thisEntity.equals(priorEntity)) {
					String newName = ((Variable) thisEntity).getName()
							+ newNameSuffix + programID;

					newNameMap.putIfAbsent(thisEntity, newName);
				}
			}
		}

		SequenceNode<BlockItemNode> root = ast.getRootNode();

		ast.release();
		moveBlockExternDeclarationToTop(root, externVarsDecls);
		deleteExternSpecifiers(externVarsDecls);
		deleteNonfirstDeclarations(externVarsDecls);
		return astFactory.newAST(root, ast.getSourceFiles(),
				ast.isWholeProgram());
	}

	/**
	 * <p>
	 * For all pure declarations of one variable at file scope, ones after the
	 * first declaration are useless. Delete them. However, if any of the
	 * declaration adds more definition to the type of the variable, the type
	 * definition should be kept.
	 * </p>
	 * 
	 * @param externVarsDecls
	 * @throws SyntaxException
	 */
	private void deleteNonfirstDeclarations(
			List<ExternVariableDeclarations> externFileVarsDecls)
			throws SyntaxException {
		for (ExternVariableDeclarations externVarDecl : externFileVarsDecls) {
			int numFileScopeDecls = externVarDecl.numFilePureDeclarations();
			int i = externVarDecl.definitionIdx <= 0 ? 0 : 1;

			for (; i < numFileScopeDecls; i++) {
				VariableDeclarationNode varDecl = externVarDecl
						.getNthPureDeclarationInFile(i);
				TypeNode type = varDecl.getTypeNode();
				DeclarationNode typeDefinition = typeDependencyFinder
						.getDefinition(type);
				ASTNode parent = varDecl.parent();
				int childIdx = varDecl.childIndex();

				varDecl.remove();
				if (typeDefinition != null && parent != null)
					parent.setChild(childIdx, typeDefinition);
			}

			TypeNode typeNode = externVarDecl.allDeclarations[0].getTypeNode();

			if (typeNode.kind() != TypeNodeKind.ARRAY)
				continue;
			if (externVarDecl.definitionIdx <= 0)
				continue;

			if (((ArrayTypeNode) typeNode).getExtent() != null)
				continue;

			// Special handling for arrays:
			// For incomplete arrays declarations,
			ArrayTypeNode completeArrayType = (ArrayTypeNode) externVarDecl
					.getDefinition().getTypeNode();

			if (completeArrayType.hasStaticExtent()) {
				// 1) if the extent eventually is a "constant", move the extent
				// to top:
				ExpressionNode staticExtent = completeArrayType.getExtent();

				staticExtent.remove();
				((ArrayTypeNode) typeNode).setExtent(staticExtent);
			} else {
				// 2) if the extent eventually is a "non-constant" and there is
				// an prior incomplete declaration, throw an error:
				throw new SyntaxException(
						"CIVL-C does not allow an incomplete array to"
								+ " be completed with a non-constant expression.",
						externVarDecl.allDeclarations[0].getSource());
			}
		}
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		Map<Entity, String> newNameMap = new HashMap<>();

		ast = cleanExternVariableDeclarations(ast, newNameMap);
		ast = Transform.nameTransformer(newNameMap, astFactory).transform(ast);
		return ast;
	}

	/**
	 * All categorized declarations for one variable with external-linkage.
	 * 
	 * @author xxxx
	 */
	private class ExternVariableDeclarations {
		/**
		 * All declarations that are stored in an array with the order of their
		 * appearances.
		 */
		final VariableDeclarationNode[] allDeclarations;
		/**
		 * All variable declarations in block scopes
		 */
		int[] declsInBlocks;
		/**
		 * All variable declarations in the file scope (excludes definitions)
		 */
		int[] pureDeclsInFile;
		/**
		 * The definition of the extern variable (optional, can be null)
		 */
		int definitionIdx = -1;

		ExternVariableDeclarations(
				List<VariableDeclarationNode> allDeclarations) {
			this.allDeclarations = new VariableDeclarationNode[allDeclarations
					.size()];
			allDeclarations.toArray(this.allDeclarations);
			build();
		}

		private void build() {
			List<Integer> inBlockIndices = new LinkedList<>();
			List<Integer> inFileIndices = new LinkedList<>();

			for (int i = 0; i < allDeclarations.length; i++) {
				VariableDeclarationNode decl = allDeclarations[i];
				ProgramEntity entity = decl.getEntity();

				if (entity.getEntityKind() != EntityKind.VARIABLE)
					continue;

				if (decl.getScope().getScopeKind() == ScopeKind.BLOCK)
					inBlockIndices.add(i);
				else if (!decl.isDefinition())
					inFileIndices.add(i);
				else {
					assert definitionIdx == -1;
					assert decl.isDefinition();
					definitionIdx = i;
				}
			}

			int i = 0;

			declsInBlocks = new int[inBlockIndices.size()];
			for (int idx : inBlockIndices)
				declsInBlocks[i++] = idx;
			i = 0;
			pureDeclsInFile = new int[inFileIndices.size()];
			for (int idx : inFileIndices)
				pureDeclsInFile[i++] = idx;
		}

		int numBlockDeclarations() {
			return this.declsInBlocks.length;
		}

		int numFilePureDeclarations() {
			return this.pureDeclsInFile.length;
		}

		/**
		 * @return the n-th variable declaration in block scopes.
		 */
		VariableDeclarationNode getNthDeclarationInBlock(int nth) {
			return this.allDeclarations[declsInBlocks[nth]];
		}

		/**
		 * @return the n-th variable declaration in the file scope.
		 */
		VariableDeclarationNode getNthPureDeclarationInFile(int nth) {
			return this.allDeclarations[pureDeclsInFile[nth]];
		}

		/**
		 * @return the variable definition if it exists, otherwise null.
		 */
		VariableDeclarationNode getDefinition() {
			return definitionIdx >= 0 ? allDeclarations[definitionIdx] : null;
		}
	}
}
