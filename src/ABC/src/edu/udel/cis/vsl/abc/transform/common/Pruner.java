package edu.udel.cis.vsl.abc.transform.common;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTException;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.AttributeKey;
import edu.udel.cis.vsl.abc.ast.node.IF.NodePredicate;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.transform.IF.BaseTransformer;
import edu.udel.cis.vsl.abc.transform.IF.Transformer;

/**
 * <p>
 * Prunes unreachable objects from an AST. Starting from the "main" function,
 * this transformer performs a search of the nodes that can be reached by
 * following children other than ordinary declarations and typedef declarations.
 * When an identifier is encountered, the definition or declaration of the
 * entity to which it refers is also searched. Hence only those
 * declarations/definitions that are actually used will be encountered in the
 * search.
 * </p>
 * 
 * <p>
 * Once the reachable nodes have been determined, the set of reachable nodes is
 * "closed" by marking all ancestors of reachable nodes reachable.
 * </p>
 * 
 * <p>
 * This transformer assumes the given AST is a closed program. It also assumes
 * that the standard analysis has been performed, so that identifiers have
 * entities associated to them.
 * </p>
 * 
 * <p>
 * The AST nodes are modified and re-used. If you want to keep the original AST
 * intact, you should clone it before performing this transformation.
 * </p>
 * 
 * <p>
 * The AST returned will be pruned, but will not have the standard analyses
 * encoded in it. If you want them, they should be invoked on the new AST.
 * </p>
 * 
 * <p>
 * What is reachable:
 * </p>
 * 
 * <ul>
 * <li>the main function is reachable</li>
 * <li>if an IdentifierNode is reachable, the result of exploring its Entity is
 * reachable</li>
 * <li>if a node is reachable, so are all its ancestors</li>
 * <li>if a node is reachable, so are all its children, EXCEPT: a declaration of
 * something in a sequence of block items UNLESS that declaration is an input or
 * output declaration, or that declaration has an initializer with a side effect
 * </li>
 * </ul>
 * 
 * <p>
 * The result of exploring an Entity: the definition node, and: for a tagged
 * entity, the first declaration, and for an ordinary entity: all declarations
 * that are not equivalent to previous declarations.
 * </p>
 * 
 * @author xxxx
 */
public class Pruner extends BaseTransformer {

	/**
	 * The short code used to identify this {@link Transformer}.
	 */
	public final static String CODE = "prune";

	/**
	 * The long name used to identify this {@link Transformer}.
	 */
	public final static String LONG_NAME = "Pruner";

	/**
	 * The short description of what this {@link Transformer} does.
	 */
	public final static String SHORT_DESCRIPTION = "removes unreachable objects from the AST";

	/**
	 * The attribute key used to make a node as reachable.
	 */
	private AttributeKey reachedKey;

	/**
	 * The predicate on {@link ASTNode} which returns true iff the node's
	 * reachable attribute key has value <code>true</code>.
	 */
	private NodePredicate reachable;

	public Pruner(ASTFactory astFactory) {
		super(CODE, LONG_NAME, SHORT_DESCRIPTION, astFactory);
		reachedKey = nodeFactory.newAttribute("reached", Boolean.class);
		reachable = new NodePredicate() {
			@Override
			public boolean holds(ASTNode node) {
				Boolean result = (Boolean) node.getAttribute(reachedKey);

				return result != null && result;
			}
		};
	}

	/**
	 * Marks every node reachable from <code>node</code> (through the child
	 * relation) as unreachable, i.e., sets the value associated to the
	 * {@link #reachedKey} to <code>false</code>.
	 * 
	 * @param node
	 *            an AST node (non-<code>null</code>)
	 */
	private void markAllUnreachable(ASTNode node) {
		if (node == null)
			return;
		else {
			Iterable<ASTNode> children = node.children();

			node.setAttribute(reachedKey, false);
			for (ASTNode child : children)
				markAllUnreachable(child);
		}
	}

	@Override
	public AST transform(AST ast) throws SyntaxException {
		SequenceNode<BlockItemNode> root = ast.getRootNode();
		Function main = (Function) root.getScope().getOrdinaryEntity(false,
				"main");

		assert this.astFactory == ast.getASTFactory();
		assert this.nodeFactory == astFactory.getNodeFactory();
		if (main == null)
			return ast;
		if (main.getDefinition() == null)
			throw new ASTException("Main function missing definition");
		else {
			ast.release();
			markAllUnreachable(root);
			new PrunerWorker(reachedKey, root);
			root.keepOnly(reachable);

			AST newAst = astFactory.newAST(root, ast.getSourceFiles(),
					ast.isWholeProgram());

			return newAst;
		}
	}
}
