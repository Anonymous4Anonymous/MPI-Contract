package edu.udel.cis.vsl.abc.front.c.astgen;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.config.IF.Configuration;
import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.err.IF.ABCException;
import edu.udel.cis.vsl.abc.front.c.astgen.AcslContractWorker.ACSLSpecTranslation;
import edu.udel.cis.vsl.abc.front.c.parse.CAcslParser;
import edu.udel.cis.vsl.abc.front.c.parse.CParser.RuleKind;
import edu.udel.cis.vsl.abc.front.c.ptree.CParseTree;
import edu.udel.cis.vsl.abc.front.common.astgen.SimpleScope;
import edu.udel.cis.vsl.abc.front.common.parse.TreeUtils;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

/**
 * This is responsible for translating a CParseTree that represents an ACSL
 * contract specification for a function or a loop into an ordered list of
 * Contract nodes.
 * 
 * @author Manchun Zheng (zxxxx)
 *
 */
public class AcslContractHandler {

	/**
	 * The kind of the contract: either for a function or for a loop.
	 * 
	 * @author Manchun Zheng
	 *
	 */
	public enum AcslContractKind {
		FUNCTION_CONTRACT, LOOP_CONTRACT
	}

	/**
	 * the node factor
	 */
	private NodeFactory nodeFactory;

	/**
	 * the token factory
	 */
	private TokenFactory tokenFactory;

	/**
	 * parser for ACSL contract annotations
	 */
	private CAcslParser cAcslParser;

	/**
	 * creates a new instance of ACSL contract handler
	 * 
	 * @param factory
	 *            the node factory to be used for creating new AST nodes
	 * @param tokenFactory
	 *            the token factory to be used
	 */
	public AcslContractHandler(NodeFactory factory, TokenFactory tokenFactory) {
		this.nodeFactory = factory;
		this.tokenFactory = tokenFactory;
		cAcslParser = new CAcslParser();
	}

	/**
	 * translates a CivlcTokenSource object representing an ACSL contract block,
	 * which may be in the form of a multiple-line comment (<code>/*@ ...</code>
	 * ) or single-line (<code>//@ ...</code>) comment, into a sequence of
	 * contract nodes. First, the token source is parsed using the ACSL parser
	 * into an ANTLR CommonTree object. Second, a CParseTree object is created
	 * using the ANTLR tree. Finally, an AcslContractWorker is created to walk
	 * through the CParseTree to create AST nodes.
	 * 
	 * @param source
	 *            the source of the whole annotation block
	 * @param tokenSource
	 *            the CivlcTokenSource object representing the annotation, which
	 *            is the result of preprocessing the annotation using the C
	 *            preprocessor
	 * @param scope
	 *            the scope of the annotation. For example, if this is a
	 *            function scope, then it is the scope of the function
	 *            parameters.
	 * @return a {@link ACSLSpecTranslation} which is the result of the
	 *         translation of the ACSL annotation.
	 * @throws SyntaxException
	 *             if there are any syntax errors.
	 */
	public ACSLSpecTranslation translateAcslAnnotation(Source source,
			CivlcTokenSource tokenSource, SimpleScope scope,
			Configuration config) throws SyntaxException {
		TokenStream tokens;
		CommonTree tree;

		tokens = new CommonTokenStream(tokenSource);
		tree = this.cAcslParser.parse(source, tokens);
		TreeUtils.postProcessTree(tree);

		CParseTree parseTree = new CParseTree(Language.CIVL_C,
				RuleKind.CONTRACT, tokenSource, tree);
		AcslContractWorker worker = new AcslContractWorker(nodeFactory,
				tokenFactory, parseTree, config);

		try {
			return worker.generateContractNodes(scope);
		} catch (ABCException e) {
			// convert to SyntaxException:
			throw new SyntaxException(e.getMessage(), source);
		} 
	}
}
