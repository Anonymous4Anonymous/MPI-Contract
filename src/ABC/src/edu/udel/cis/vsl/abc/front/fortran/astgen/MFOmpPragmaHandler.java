package edu.udel.cis.vsl.abc.front.fortran.astgen;

import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.AMPERSAND;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.ATOMIC;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.BARRIER;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.BITOR;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.BITXOR;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.CAPTURE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.COLLAPSE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.COPYIN;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.COPYPRIVATE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.CRITICAL;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.DATA_CLAUSE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.DEFAULT;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.DYNAMIC;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.END;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.EQ;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.EQV;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.FLUSH;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.FOR;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.FST_PRIVATE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.GUIDED;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.IDENTIFIER;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.IF;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.LAND;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.LOR;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.LST_PRIVATE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.MASTER;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.NE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.NEQV;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.NONE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.NOWAIT;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.NUM_THREADS;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.ORDERED;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.PARALLEL;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.PARALLEL_FOR;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.PARALLEL_SECTIONS;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.PLUS;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.PRIVATE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.READ;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.REDUCTION;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.RUNTIME;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.SCHEDULE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.SECTION;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.SECTIONS;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.SEQ_CST;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.SHARED;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.SINGLE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.STAR;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.STATIC;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.SUB;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.THD_PRIVATE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.UNIQUE_FOR;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.UNIQUE_PARALLEL;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.UPDATE;
import static edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser.WRITE;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTree;

import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.PragmaNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpEndNode.OmpEndType;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpExecutableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode.OmpScheduleKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpParallelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode.OmpReductionOperator;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode.OmpWorksharingNodeKind;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.err.IF.ABCUnsupportedException;
import edu.udel.cis.vsl.abc.front.IF.ParseTree;
import edu.udel.cis.vsl.abc.front.common.astgen.PragmaHandler;
import edu.udel.cis.vsl.abc.front.common.astgen.SimpleScope;
import edu.udel.cis.vsl.abc.front.fortran.parse.MFOmpParser;
import edu.udel.cis.vsl.abc.front.fortran.ptree.MFTree;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.token.IF.CivlcTokenSource;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.abc.token.IF.TokenFactory;

public class MFOmpPragmaHandler extends PragmaHandler {

	private MFTree fortranTree;

	@SuppressWarnings("unused")
	private MFASTBuilderWorker worker;
	// TODO: may be used for processing expressions.

	/**
	 * The node factory used to create new AST nodes.
	 */
	private NodeFactory nodeFactory;

	/**
	 * The token factory used to create new tokens.
	 */
	private TokenFactory tokenFactory;

	private MFOmpParser fOmpParser;

	public MFOmpPragmaHandler(MFASTBuilder builder, MFTree parseTree) {
		ASTFactory astFactory = builder.getASTFactory();

		this.fortranTree = parseTree;
		this.worker = builder.getWorker(parseTree);
		this.nodeFactory = astFactory.getNodeFactory();
		this.tokenFactory = astFactory.getTokenFactory();
		this.fOmpParser = new MFOmpParser();
	}

	private void markTokens(PragmaNode pragmaNode) {
		int number = pragmaNode.getNumTokens();

		for (CivlcToken token : pragmaNode.getTokens()) {
			//int type = token.getType();

			//if (type == IDENTIFIER) {
				switch (token.getText().toUpperCase()) {
					case "ATOMIC" :
						token.setType(ATOMIC);
						break;
					case "BARRIER" :
						token.setType(BARRIER);
						break;
					case "CAPTURE" :
						token.setType(CAPTURE);
						break;
					case "COLLAPSE" :
						token.setType(COLLAPSE);
						break;
					case "COPYIN" :
						token.setType(COPYIN);
						break;
					case "COPYPRIVATE" :
						token.setType(COPYPRIVATE);
						break;
					case "CRITICAL" :
						token.setType(CRITICAL);
						break;
					case "DEFAULT" :
						token.setType(DEFAULT);
						break;
					case "DYNAMIC" :
						token.setType(DYNAMIC);
						break;
					case "FIRSTPRIVATE" :
						token.setType(FST_PRIVATE);
						break;
					case "FLUSH" :
						token.setType(FLUSH);
						break;
					case "GUIDED" :
						token.setType(GUIDED);
						break;
					case "LASTPRIVATE" :
						token.setType(LST_PRIVATE);
						break;
					case "MASTER" :
						token.setType(MASTER);
						break;
					case "NONE" :
						token.setType(NONE);
						break;
					case "NOWAIT" :
						token.setType(NOWAIT);
						break;
					case "NUM_THREADS" :
						token.setType(NUM_THREADS);
						break;
					case "ORDERED" :
						token.setType(ORDERED);
						break;
					case "PARALLEL" :
						token.setType(PARALLEL);
						break;
					case "PRIVATE" :
						token.setType(PRIVATE);
						break;
					case "READ" :
						token.setType(READ);
						break;
					case "REDUCTION" :
						token.setType(REDUCTION);
						break;
					case "RUNTIME" :
						token.setType(RUNTIME);
						break;
					case "SCHEDULE" :
						token.setType(SCHEDULE);
						break;
					case "SECTIONS" :
						token.setType(SECTIONS);
						break;
					case "SECTION" :
						token.setType(SECTION);
						break;
					case "SEQ_CST" :
						token.setType(SEQ_CST);
						break;
					case "SHARED" :
						token.setType(SHARED);
						break;
					case "SINGLE" :
						token.setType(SINGLE);
						break;
					case "STATIC" :
						token.setType(STATIC);
						break;
					case "THREADPRIVATE" :
						token.setType(THD_PRIVATE);
						break;
					case "UPDATE" :
						token.setType(UPDATE);
						break;
					case "WRITE" :
						token.setType(WRITE);
						break;
					case "END" :
						token.setType(END);
					case "DO" :
						token.setType(FOR);
					default :
				}
			//} else if (type == END) {
			//}
		}
		if (number >= 1)
			pragmaNode.getToken(number - 1).setNext(null);
	}

	private OmpWorksharingNode translateWorkshare(Source source,
			CommonTree workshareTree, OmpWorksharingNodeKind kind) {
		int numChildren = workshareTree.getChildCount();
		OmpWorksharingNode workshareNode = nodeFactory
				.newWorksharingNode(source, kind);
		boolean hasNowait = false;

		for (int i = 0; i < numChildren; i++) {
			CommonTree sectionsClause = (CommonTree) workshareTree.getChild(i);
			int type = sectionsClause.getType();

			switch (type) {
				case DATA_CLAUSE :
					this.translateDataClause(source, sectionsClause,
							workshareNode);
					break;
				case NOWAIT :
					if (!hasNowait) {
						hasNowait = true;
					} else {
						throw new ABCRuntimeException(
								"At most one nowait directive is allowed in an OpenMP construct.",
								(tokenFactory.newSource(
										(CivlcToken) sectionsClause.getToken())
										.getSummary(false, true)));
					}
					workshareNode.setNowait(true);
					break;
				default :
					throw new ABCRuntimeException(
							"OMPPragmaHandler: unsupported token");
			}
		}
		return workshareNode;
	}

	private OmpForNode translateFor(Source source, CommonTree forTree)
			throws SyntaxException {
		int numChildren = forTree.getChildCount();
		OmpForNode forNode = nodeFactory.newOmpForNode(source, null);

		for (int i = 0; i < numChildren; i++) {
			CommonTree forClause = (CommonTree) (forTree.getChild(i))
					.getChild(0);
			int type = forClause.getType();

			switch (type) {
				case UNIQUE_FOR :
					translateUniqueForClause((CommonTree) forClause, forNode);
					break;
				case DATA_CLAUSE :
					this.translateDataClause(source, forClause, forNode);
					break;
				case NOWAIT :
					forNode.setNowait(true);
					break;
				default :
					throw new ABCRuntimeException("Unreachable");
			}
		}

		return forNode;
	}

	private void translateUniqueForClause(CommonTree forClause,
			OmpForNode forNode) throws SyntaxException {
		CommonTree uniqueForClause = (CommonTree) forClause.getChild(0);
		int type = uniqueForClause.getType();

		switch (type) {
			case ORDERED :
				break;
			case SCHEDULE :
				int scheduleType = uniqueForClause.getChild(0).getType();

				switch (scheduleType) {
					case STATIC :
						forNode.setSchedule(OmpScheduleKind.STATIC);
						break;
					case DYNAMIC :
						forNode.setSchedule(OmpScheduleKind.DYNAMIC);
						break;
					case GUIDED :
						forNode.setSchedule(OmpScheduleKind.GUIDED);
						break;
					default : // case RUNTIME:
						forNode.setSchedule(OmpScheduleKind.RUNTIME);
				}
				if (uniqueForClause.getChildCount() > 1) {
					@SuppressWarnings("unused")
					CommonTree chunkSizeTree = (CommonTree) uniqueForClause
							.getChild(1);

					// TODO: is null acceptable for a SimpleScope?

					ExpressionNode chunkSizeNode = null;
					// TODO: processExpr
					// worker.translateExpression(chunkSizeTree, null);

					forNode.setChunsize(chunkSizeNode);
				}

				break;
			case COLLAPSE : {
				CommonTree constant = (CommonTree) uniqueForClause.getChild(0);
				IntegerConstantNode constantNode = nodeFactory
						.newIntegerConstantNode(null, constant.getText());

				forNode.setCollapse(constantNode.getConstantValue()
						.getIntegerValue().intValue());
				break;
			}
			default :
				throw new ABCRuntimeException("Unreachable");
		}
	}

	private OmpNode translateParallel(Source source, CommonTree paralle)
			throws SyntaxException {
		int numChildren = paralle.getChildCount();
		OmpParallelNode parallelNode = nodeFactory.newOmpParallelNode(source,
				null);
		boolean hasIf = false;
		boolean hasNumThreads = false;

		for (int i = 0; i < numChildren; i++) {
			CommonTree parallelClause = (CommonTree) paralle.getChild(i);
			int type = parallelClause.getType();

			switch (type) {
				case UNIQUE_PARALLEL :
					int result = this.translateUniqueParallel(parallelClause,
							parallelNode);

					if (result == IF) {
						if (!hasIf) {
							hasIf = true;
						} else {
							throw new ABCRuntimeException(
									"At most one if clause is allowed in an OpenMP parallel construct.",
									(tokenFactory
											.newSource(
													(CivlcToken) parallelClause
															.getToken())
											.getSummary(false, true)));
						}
					} else if (result == NUM_THREADS) {
						if (!hasNumThreads) {
							hasNumThreads = true;
						} else {
							throw new ABCRuntimeException(
									"At most one num_threads() clause is allowed in an OpenMP parallel construct.",
									(tokenFactory
											.newSource(
													(CivlcToken) parallelClause
															.getToken())
											.getSummary(false, true)));
						}
					}
					break;
				case DATA_CLAUSE :
					this.translateDataClause(source, parallelClause,
							parallelNode);
					break;
				default :
					throw new ABCRuntimeException("Unreachable");
			}
		}
		return parallelNode;
	}

	private int translateUniqueParallel(CommonTree parallelClause,
			OmpParallelNode parallelNode) throws SyntaxException {
		CommonTree child = (CommonTree) parallelClause.getChild(0);
		ExpressionNode expression;

		switch (child.getType()) {
			case IF :
				expression = null;
				// TODO: processExpr worker.translateExpression((CommonTree)
				// child.getChild(0), null);
				parallelNode.setIfClause(expression);
				return IF;
			case NUM_THREADS :
				expression = null;
				// TODO: processExpr worker.translateExpression((CommonTree)
				// child.getChild(0), null);
				parallelNode.setNumThreads(expression);
				return NUM_THREADS;
			default :
				throw new ABCRuntimeException("Unreachable");
		}
	}

	private OmpParallelNode translateParallelFor(Source source,
			CommonTree parallelFor) throws SyntaxException {
		int numChildren = parallelFor.getChildCount();
		OmpParallelNode parallelNode = nodeFactory.newOmpParallelNode(source,
				null);
		OmpForNode forNode = nodeFactory.newOmpForNode(source, null);

		for (int i = 0; i < numChildren; i++) {
			CommonTree parallelForClause = (CommonTree) parallelFor.getChild(i);
			int type = parallelForClause.getType();

			switch (type) {
				case UNIQUE_PARALLEL :
					this.translateUniqueParallel(parallelForClause,
							parallelNode);
					break;
				case UNIQUE_FOR :
					this.translateUniqueForClause(parallelForClause, forNode);
					break;
				case DATA_CLAUSE :
					this.translateDataClause(source, parallelForClause,
							parallelNode);
					break;
				default :
					throw new ABCRuntimeException("Unreachable");
			}
		}
		parallelNode.setStatementNode(forNode);
		return parallelNode;
	}

	private OmpParallelNode translateParallelSections(Source source,
			CommonTree parallelSections) throws SyntaxException {
		int numChildren = parallelSections.getChildCount();
		OmpParallelNode parallelNode = nodeFactory.newOmpParallelNode(source,
				null);
		OmpWorksharingNode sectionsNode = nodeFactory.newOmpSectionsNode(source,
				null);

		for (int i = 0; i < numChildren; i++) {
			CommonTree parallelSectionsClause = (CommonTree) parallelSections
					.getChild(i);
			int type = parallelSectionsClause.getType();

			switch (type) {
				case UNIQUE_PARALLEL :
					this.translateUniqueParallel(parallelSectionsClause,
							parallelNode);
					break;
				case DATA_CLAUSE :
					this.translateDataClause(source, parallelSectionsClause,
							parallelNode);
					break;
				default :
					throw new ABCRuntimeException("Unreachable");
			}
		}
		parallelNode.setStatementNode(sectionsNode);
		return parallelNode;
	}

	private void translateDataClause(Source source, CommonTree dataClause,
			OmpExecutableNode ompStatementNode) {
		int numChildren = dataClause.getChildCount();
		CommonTree dataClauseEle;
		int type;

		assert numChildren == 1;
		dataClauseEle = (CommonTree) dataClause.getChild(0);
		type = dataClauseEle.getType();
		switch (type) {
			case PRIVATE :
				ompStatementNode.setPrivateList(translateIdentifierList(source,
						"privateList", (CommonTree) dataClauseEle.getChild(0)));
				break;
			case FST_PRIVATE :
				ompStatementNode.setFirstprivateList(
						translateIdentifierList(source, "firstprivateList",
								(CommonTree) dataClauseEle.getChild(0)));
				break;
			case LST_PRIVATE :
				ompStatementNode.setLastprivateList(
						translateIdentifierList(source, "lastprivateList",
								(CommonTree) dataClauseEle.getChild(0)));
				break;
			case SHARED :
				ompStatementNode.setSharedList(translateIdentifierList(source,
						"sharedList", (CommonTree) dataClauseEle.getChild(0)));
				break;
			case COPYIN :
				ompStatementNode.setCopyinList(translateIdentifierList(source,
						"copyinList", (CommonTree) dataClauseEle.getChild(0)));
				break;
			case COPYPRIVATE :
				ompStatementNode.setCopyprivateList(
						translateIdentifierList(source, "copyprivateList",
								(CommonTree) dataClauseEle.getChild(0)));
				break;
			case DEFAULT :
				if (dataClause.getChild(0).getChild(0).getType() == NONE)
					((OmpParallelNode) ompStatementNode).setDefault(false);
				break;
			case REDUCTION :
				OmpReductionNode reductionNode = translateReductionClause(
						dataClauseEle);
				SequenceNode<OmpReductionNode> reductionList = ompStatementNode
						.reductionList();

				if (reductionList == null) {
					List<OmpReductionNode> nodes = new ArrayList<>(1);

					nodes.add(reductionNode);
					reductionList = nodeFactory.newSequenceNode(
							reductionNode.getSource(), "reductionList", nodes);
				}
				ompStatementNode.setReductionList(reductionList);
				break;
			default :
				throw new ABCRuntimeException("Invalid data clause");
		}
	}

	private OmpReductionNode translateReductionClause(CommonTree reduction) {
		CommonTree opNode = (CommonTree) reduction.getChild(0);
		int operatorType = opNode.getType();
		List<IdentifierExpressionNode> list = translateIdentifierList(
				(CommonTree) reduction.getChild(1));
		Source rootSource = tokenFactory
				.newSource((CivlcToken) reduction.getToken());
		SequenceNode<IdentifierExpressionNode> nodes = nodeFactory
				.newSequenceNode(rootSource, "reductionList", list);

		if (operatorType == IDENTIFIER) {
			String id = opNode.getText().toLowerCase();

			if (id.equals("max")) {
				return this.nodeFactory.newOmpSymbolReductionNode(rootSource,
						OmpReductionOperator.MAX, nodes);
			} else if (id.equals("min")) {
				return this.nodeFactory.newOmpSymbolReductionNode(rootSource,
						OmpReductionOperator.MIN, nodes);
			} else if (id.equals("iand")) {
				return this.nodeFactory.newOmpSymbolReductionNode(rootSource,
						OmpReductionOperator.BAND, nodes);
			} else if (id.equals("ior")) {
				return this.nodeFactory.newOmpSymbolReductionNode(rootSource,
						OmpReductionOperator.BOR, nodes);
			} else if (id.equals("ieor")) {
				return this.nodeFactory.newOmpSymbolReductionNode(rootSource,
						OmpReductionOperator.BXOR, nodes);
			} else {// User Defined Functions
				IdentifierExpressionNode function = this
						.translateIdentifierExpression(
								(CommonTree) reduction.getChild(0));

				return this.nodeFactory.newOmpFunctionReductionNode(rootSource,
						function, nodes);
			}
		} else { // Built-in Symbol Operators
			OmpReductionOperator operator = translateOperator(
					reduction.getChild(0).getType());

			return this.nodeFactory.newOmpSymbolReductionNode(rootSource,
					operator, nodes);
		}
	}

	private OmpReductionOperator translateOperator(int type) {
		switch (type) {
			case PLUS :
				return OmpReductionOperator.SUM;
			case STAR :
				return OmpReductionOperator.PROD;
			case SUB :
				return OmpReductionOperator.MINUS;
			case AMPERSAND :
				return OmpReductionOperator.BAND;
			case BITXOR :
				return OmpReductionOperator.BXOR;
			case BITOR :
				return OmpReductionOperator.BOR;
			case LAND :
				return OmpReductionOperator.LAND;
			case LOR :
				return OmpReductionOperator.LOR;
			case EQ :
			case EQV :
				return OmpReductionOperator.EQV;
			case NE :
			case NEQV :
				return OmpReductionOperator.NEQ;
			default :
				throw new ABCUnsupportedException(
						"reduction operator of type " + type);
		}
	}

	private SequenceNode<IdentifierExpressionNode> translateIdentifierList(
			Source source, String name, CommonTree identifierList) {
		List<IdentifierExpressionNode> list = translateIdentifierList(
				identifierList);

		return nodeFactory.newSequenceNode(source, name, list);
	}

	private List<IdentifierExpressionNode> translateIdentifierList(
			CommonTree identifierList) {
		int numChildren = identifierList.getChildCount();
		List<IdentifierExpressionNode> list = new ArrayList<>(numChildren);

		for (int i = 0; i < numChildren; i++) {
			list.add(translateIdentifierExpression(
					(CommonTree) identifierList.getChild(i)));
		}
		return list;
	}

	private IdentifierExpressionNode translateIdentifierExpression(
			CommonTree identifier) {
		IdentifierNode identifierNode = translateIdentifier(identifier);

		return nodeFactory.newIdentifierExpressionNode(
				identifierNode.getSource(), identifierNode);
	}

	private IdentifierNode translateIdentifier(CommonTree identifier) {
		CivlcToken token = (CivlcToken) identifier.getToken();
		Source source = tokenFactory.newSource(token);

		return nodeFactory.newIdentifierNode(source, token.getText());
	}

	// Public methods....

	@Override
	public EntityKind getEntityKind() {
		return EntityKind.PRAGMA_HANDLER;
	}

	@Override
	public String getName() {
		return "omp";
	}

	@Override
	public ParseTree getParseTree() {
		return fortranTree;
	}

	@Override
	public ASTNode processPragmaNode(PragmaNode pragmaNode, SimpleScope scope)
			throws SyntaxException {
		Source source = pragmaNode.getSource();
		CivlcTokenSource tokenSource;
		TokenStream tokens;
		CommonTree rootTree;
		int type;

		markTokens(pragmaNode);
		tokenSource = pragmaNode.newTokenSource();
		tokens = new CommonTokenStream(tokenSource);
		rootTree = this.fOmpParser.parse(source, tokens);
		type = rootTree.getType();
		switch (type) {
			case PARALLEL_FOR :
				return translateParallelFor(source, rootTree);
			case PARALLEL_SECTIONS :
				return translateParallelSections(source, rootTree);
			case PARALLEL :
				return translateParallel(source, rootTree);
			case FOR :
				return translateFor(source, rootTree);
			case SECTIONS :
				return translateWorkshare(source, rootTree,
						OmpWorksharingNodeKind.SECTIONS);
			case SINGLE :
				return translateWorkshare(source, rootTree,
						OmpWorksharingNodeKind.SINGLE);
			case MASTER :
				return nodeFactory.newOmpMasterNode(source, null);
			case CRITICAL : {
				OmpSyncNode criticalNode = nodeFactory
						.newOmpCriticalNode(source, null, null);

				if (rootTree.getChildCount() > 0) {
					criticalNode.setCriticalName(this.translateIdentifier(
							(CommonTree) rootTree.getChild(0)));
				}
				return criticalNode;
			}
			case ORDERED :
				return nodeFactory.newOmpOrederedNode(source, null);
			case SECTION :
				return nodeFactory.newOmpSectionNode(source, null);
			case BARRIER :
				return nodeFactory.newOmpBarrierNode(source);
			case FLUSH :
				return nodeFactory.newOmpFlushNode(source,
						translateIdentifierList(source, "flushList",
								(CommonTree) rootTree.getChild(0)));
			case THD_PRIVATE :
				return nodeFactory.newOmpThreadprivateNode(source,
						translateIdentifierList(source, "threadprivateList",
								(CommonTree) rootTree.getChild(0)));
			case ATOMIC :
				// TODO: complete handling clause and seq_cst
				return nodeFactory.newOmpAtomicNode(source, null, null, false);
			case END :
				OmpEndType endType = null;

				if (rootTree.getChild(0).toString().toUpperCase()
						.equals("PARALLEL"))
					endType = OmpEndType.PARALLEL;
				else if (rootTree.getChild(0).toString().toUpperCase()
						.equals("SECTIONS"))
					endType = OmpEndType.SECTIONS;
				else if (rootTree.getChild(0).toString().toUpperCase()
						.equals("DO"))
					endType = OmpEndType.DO;
				else
					assert false;

				return nodeFactory.newOmpFortranEndNode(source, endType);
			default :
				throw new ABCRuntimeException("Unreachable");
		}
	}

}
