package edu.udel.cis.vsl.civl.transform.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.DesignationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.label.SwitchLabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LabeledStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode.LoopKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.SwitchNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.front.IF.Preprocessor;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.transform.IF.DirectingTransformer;

/**
 * This worker transforms branching instructions whose file name and source line number
 * match a "direction target" as follows:
 * 
 * replace 
 *    if ( Cond ) S
 * by
 *   $assume(Lbranch[LbranchIdx++] ? Cond : ! Cond; 
 *   if ( Cond ) S
 *
 * replace 
 *   for ( Init ; Cond ; Incr ) S
 * by
 *   Init; 
 *   while (1) { 
 *     $assume(Lbranch[LbranchIdx++] ? Cond : ! Cond; 
 *     if ( ! Cond ) break; 
 *     S;
 *     Inc;
 *   }
 *
 * replace 
 *   while ( Cond ) S 
 * by
 *   while (1) { 
 *     $assume(Lbranch[LbranchIdx++] ? Cond : ! Cond; 
 *     if ( ! Cond ) break; 
 *     S;
 *   }
 *
 * replace 
 *   do S while ( Cond )
 * by
 *   do { 
 *     S; 
 *     $assume(Lbranch[LbranchIdx++] ? Cond : ! Cond; 
 *   } while ( Cond ) 
 * 
 * TBD: need to support switch, need to make sure that we have access the right variables (e.g., via name)
 * 
 * @author dwyer
 * 
 */
public class DirectingWorker extends BaseWorker {

	private boolean debug = false;
	private CIVLConfiguration config;
	private String indexVarName;
	private String arrayVarName;
	
	private Set<Integer> directingLines;
	private ArrayList<Integer> directions;
	private String directingFile;
	

	public DirectingWorker(ASTFactory astFactory,
			CIVLConfiguration config) {
		super(DirectingTransformer.LONG_NAME, astFactory);
		this.identifierPrefix = "$direct_";
		this.config = config;
		this.indexVarName = identifierPrefix+"index";
		this.arrayVarName = identifierPrefix+"array";
		directingLines = new HashSet<Integer>();
		directions = new ArrayList<Integer>();
		directingFile = null;
	}

	@Override
	public AST transform(AST unit) throws SyntaxException {
		String inputFile = config.directSymEx();
		assert inputFile != null : "Expected lines and directions file for directed symbolic execution";
		
		/**
		 * File format is:
		 *    name.c              // this is the file to instrument
		 *    lines 42 23 1 ...   // these are the lines to instrument
		 *    guide 0 1 1 0 ...   // these are the branch outcomes
		 */
		try(Scanner s = new Scanner(Paths.get(inputFile))) {
			directingFile = s.nextLine();
			s.next(); // absorb the "lines" token
			while (s.hasNextInt()) {
				directingLines.add(s.nextInt());
			}	
			s.next(); // absorb the "guide" token
			while (s.hasNextInt()) {
				directions.add(s.nextInt());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Must include civlc.cvh to resolve $assume if it is not already present
		AST civlcAST = null;
		if (unit.getInternalOrExternalEntity("$assume") == null) {
			civlcAST = this.parseSystemLibrary(new File(
				Preprocessor.ABC_INCLUDE_PATH, "civlc.cvh"), EMPTY_MACRO_MAP);
		}

		SequenceNode<BlockItemNode> rootNode = unit.getRootNode();

		assert this.astFactory == unit.getASTFactory();
		assert this.nodeFactory == astFactory.getNodeFactory();
		unit.release();
		
		instrumentGlobalDefinitions(rootNode);

		instrumentBranchStatements(rootNode);
		
		if (civlcAST != null) {
			return this.combineASTs(
				civlcAST, 
				astFactory.newAST(rootNode, unit.getSourceFiles(), unit.isWholeProgram()));
		} else {
			return astFactory.newAST(rootNode, unit.getSourceFiles(), unit.isWholeProgram());
		}
	}
	
	/**
	 * Instrument definitions of global variables that serve to direct symbolic execution.
	 * These definitions are:
	 * 
	 *     int $direct_index = 0;
	 *     Bool_ $direct_array[] = { direction1, direction2, ... };
	 *     
	 * We insert these as the first definitions.
	 *     
	 * @param root
	 * @throws SyntaxException 
	 */
	private void instrumentGlobalDefinitions(SequenceNode<BlockItemNode> root) throws SyntaxException {
		List<BlockItemNode> directDecls = new ArrayList<BlockItemNode>();
				
		Source src = this.newSource(indexVarName, CivlcTokenConstant.TYPE);
		IdentifierNode branchIdxId = nodeFactory.newIdentifierNode(src,  indexVarName);
		IntegerConstantNode zero = nodeFactory.newIntegerConstantNode(src, "0");
		directDecls.add(nodeFactory.newVariableDeclarationNode(src, branchIdxId, basicType(BasicTypeKind.INT), zero));
		
		List<PairNode<DesignationNode, InitializerNode>> initList = new LinkedList<PairNode<DesignationNode, InitializerNode>>();
		for (Integer d : directions) {
			ExpressionNode initD = nodeFactory.newBooleanConstantNode(src, d.toString().equals("1"));
			initList.add(nodeFactory.newPairNode(src, null, initD));
		}
		CompoundInitializerNode branchInitializer = nodeFactory.newCompoundInitializerNode(src, initList);
		TypeNode arrayOfInt = nodeFactory.newArrayTypeNode(src, basicType(BasicTypeKind.BOOL), null);
		IdentifierNode branchArrayId = nodeFactory.newIdentifierNode(src, arrayVarName);
		directDecls.add(nodeFactory.newVariableDeclarationNode(src, branchArrayId, arrayOfInt, branchInitializer));
		
		root.insertChildren(0, directDecls);		
	}

	private void instrumentBranchStatements(ASTNode node) throws SyntaxException {
		/* Post-order traversal of AST */
		if (node != null) {
			Iterable<ASTNode> children = node.children();
			for (ASTNode child : children) {
				instrumentBranchStatements(child);
			}
		}
		
		if (node instanceof StatementNode) {
			String sourceFile = node.getSource().getFirstToken().getSourceFile().getName();
			if (directingFile.equals(sourceFile) ) {
				if (node instanceof IfNode) {
					int lineNum = ((IfNode)node).getCondition().getSource().getFirstToken().getLine();
					if ( directingLines.contains(Integer.valueOf(lineNum)) ) {
						if (debug) System.out.println("About to instrument if at line: "+lineNum);
						node.parent().setChild(node.childIndex(), instrumentedIf((IfNode)node));
					}
					
				} else if (node instanceof LoopNode) {
					LoopNode ln = (LoopNode) node;
					/* Check for the existence of a loop condition, a statement like:
					 *    for (;;)
					 * will not have directives, so we don't need to instrument it
					 */
					if (ln.getCondition() != null) {
						int lineNum = (ln.getCondition().getSource().getFirstToken().getLine());
						if ( directingLines.contains(Integer.valueOf(lineNum)) ) {
							node.parent().setChild(node.childIndex(), instrumentedLoop((LoopNode)node));
						}
					}

				} else if (node instanceof SwitchNode) {
					
					/* Get the line numbers of all the cases, including the default*/
					Iterator<LabeledStatementNode> casesIter = ((SwitchNode)node).getCases();
					Set<Integer> caseLineNums = new HashSet<Integer>();
					while (casesIter.hasNext()) {
						LabeledStatementNode lsn = casesIter.next();
						int caseLine = lsn.getSource().getFirstToken().getLine();
						if (debug) System.out.println("Case line at: "+caseLine);
						caseLineNums.add(caseLine);
					}
					int defaultLine = ((SwitchNode) node).getDefaultCase().getSource().getFirstToken().getLine();
					if (debug) System.out.println("Default line at: "+defaultLine);
					caseLineNums.add(defaultLine);
					
					/* Intersect the set of directing lines with the case statement lines */
					Set<Integer> caseDirectingLines = caseLineNums;
					caseDirectingLines.retainAll(directingLines);
					if ( !caseDirectingLines.isEmpty() ) {
						node.parent().setChild(node.childIndex(), instrumentedSwitch((SwitchNode)node, caseDirectingLines));
					}

				} 
			}
		}
	}

	/* Build:  
	 *   $assert($direct_index < directions.size() : "Concrete run differs from abstract run");
	 *   $assume($direct_array[LbranchIdx++] ? Cond : ! Cond );
	 */
	private StatementNode instrumentAssume(Source src, ExpressionNode cond) throws SyntaxException {
		/* Boolean casts not happening when the condition is "1" or "0", so forcing this with the following hack */
		if (cond.prettyRepresentation().toString().equals("1")) cond = nodeFactory.newBooleanConstantNode(src, true);
		if (cond.prettyRepresentation().toString().equals("0")) cond = nodeFactory.newBooleanConstantNode(src, false);
		
		ExpressionNode branchArray = nodeFactory.newIdentifierExpressionNode(src, nodeFactory.newIdentifierNode(src, arrayVarName));
		ExpressionNode branchIdx = nodeFactory.newIdentifierExpressionNode(src, nodeFactory.newIdentifierNode(src, indexVarName));
		List<ExpressionNode> accessArgs = new LinkedList<ExpressionNode>();
		accessArgs.add(branchArray);
		accessArgs.add(nodeFactory.newOperatorNode(src, Operator.POSTINCREMENT, Arrays.asList(branchIdx)));
		ExpressionNode branchAccess = nodeFactory.newOperatorNode(src, Operator.SUBSCRIPT, accessArgs);
	
		ExpressionNode negCond = nodeFactory.newOperatorNode(src, Operator.NOT, Arrays.asList(cond.copy()));
		
		List<ExpressionNode> plusArgs = new LinkedList<ExpressionNode>();
		plusArgs.add(branchAccess);
		plusArgs.add(cond.copy());
		plusArgs.add(negCond);
		
		ExpressionNode qmarkExpr = nodeFactory.newOperatorNode(src, Operator.CONDITIONAL, plusArgs);	
		//IntegerConstantNode oneNode = nodeFactory.newIntegerConstantNode(src,  "1");
		//ExpressionNode equalsNode = nodeFactory.newOperatorNode(src,  Operator.EQUALS, qmarkExpr, oneNode);
		
		IdentifierExpressionNode vAssume = nodeFactory.newIdentifierExpressionNode(src, nodeFactory.newIdentifierNode(src, "$assume"));
		StatementNode assumeStatement = nodeFactory.newExpressionStatementNode(nodeFactory.newFunctionCallNode(src, vAssume, Arrays.asList(qmarkExpr), null));
		
		/* This asserts that the branch index doesn't run past the array of given directions */
		StatementNode assertStatement = instrumentAssert(src, branchIdx);
		
		List<BlockItemNode> statements = new LinkedList<BlockItemNode>();
		statements.add(assertStatement);
		statements.add(assumeStatement);
		
		return nodeFactory.newCompoundStatementNode(src, statements);
	}
	
	/* Construct an assert statement to check for indexing beyond branchArray */
	private StatementNode instrumentAssert(Source src, ExpressionNode branchIdx) throws SyntaxException {
		IntegerConstantNode bound = nodeFactory.newIntegerConstantNode(src, ((Integer) directions.size()).toString());
		
		List<ExpressionNode> assertArgs = new LinkedList<>();
		assertArgs.add(branchIdx.copy());
		assertArgs.add(bound);
		
		ExpressionNode ltExpr = nodeFactory.newOperatorNode(src, Operator.LT, assertArgs);
		IdentifierExpressionNode vAssert = nodeFactory.newIdentifierExpressionNode(src, nodeFactory.newIdentifierNode(src, "$assert")).copy();
		StatementNode assertStatement = nodeFactory.newExpressionStatementNode(nodeFactory.newFunctionCallNode(src, vAssert, Arrays.asList(ltExpr), null));
		
		return assertStatement;
	}
	
	/*
	 * Replace the given IfNode with a block 
	 */
	private StatementNode instrumentedIf(IfNode node) throws SyntaxException {
		List<BlockItemNode> statements = new LinkedList<BlockItemNode>();
		statements.add(instrumentAssume(node.getSource(), node.getCondition()));
		statements.add(node.copy());
		return nodeFactory.newCompoundStatementNode(node.getSource(), statements);
	}
	
	private StatementNode instrumentedLoop(LoopNode node) throws SyntaxException {
		StatementNode result = null;
		
		Source src = node.getSource();
		ExpressionNode cond = node.getCondition();
		StatementNode body = node.getBody();
		SequenceNode<ContractNode> contracts = (node.loopContracts() != null) ? node.loopContracts().copy() : null;
		
		if (node.getKind() == LoopKind.WHILE) {

			ExpressionNode trueCondition = nodeFactory.newIntegerConstantNode(src, "1"); 
			
			List<BlockItemNode> statements = new LinkedList<BlockItemNode>();
			statements.add(instrumentAssume(src, cond));
			
			StatementNode conditionalBreak = nodeFactory.newIfNode(src, 
					nodeFactory.newOperatorNode(src, Operator.NOT, Arrays.asList(cond.copy())),
					nodeFactory.newBreakNode(src));
			statements.add(conditionalBreak);
			
			statements.add(body.copy());
			StatementNode instrumentedBody = nodeFactory.newCompoundStatementNode(src, statements);
			
			result = nodeFactory.newWhileLoopNode(src, trueCondition, instrumentedBody, contracts);
			
		} else if (node.getKind() == LoopKind.DO_WHILE) {

			List<BlockItemNode> statements = new LinkedList<BlockItemNode>();
			statements.add(body.copy());
			statements.add(instrumentAssume(src, cond));
			StatementNode instrumentedBody = nodeFactory.newCompoundStatementNode(src, statements);
			
			result = nodeFactory.newDoLoopNode(src, cond.copy(), instrumentedBody, contracts);
			
		} else {			
			ForLoopNode forLoop = (ForLoopNode)node;
			
			List<BlockItemNode> compoundItems = new LinkedList<BlockItemNode>();
			
			ForLoopInitializerNode init = forLoop.getInitializer();
			if (init instanceof DeclarationListNode) {
				for (VariableDeclarationNode vdn : (DeclarationListNode)init) {
					compoundItems.add(vdn.copy());
				}
			} else {
				compoundItems.add(nodeFactory.newExpressionStatementNode((ExpressionNode)init.copy()));
			}

			ExpressionNode trueCondition = nodeFactory.newIntegerConstantNode(src, "1");
			
			List<BlockItemNode> statements = new LinkedList<BlockItemNode>();
			statements.add(instrumentAssume(src, cond));
			
			StatementNode conditionalBreak = nodeFactory.newIfNode(src, 
					nodeFactory.newOperatorNode(src, Operator.NOT, Arrays.asList(cond.copy())),
					nodeFactory.newBreakNode(src));
			statements.add(conditionalBreak);
			
			statements.add(body.copy());
			statements.add(nodeFactory.newExpressionStatementNode(forLoop.getIncrementer().copy()));
			
			StatementNode instrumentedBody = nodeFactory.newCompoundStatementNode(src, statements);
			
			compoundItems.add(nodeFactory.newWhileLoopNode(src,
					trueCondition,
					instrumentedBody,
					contracts));
			
			result = nodeFactory.newCompoundStatementNode(src, compoundItems);
		}
		return result;
	}
	
	private StatementNode instrumentedSwitch(SwitchNode node, Set<Integer> directingLines) throws SyntaxException {
		
		Source src = node.getSource();
		ExpressionNode swc = node.getCondition();
		List<BlockItemNode> statements = new LinkedList<BlockItemNode>();
		
		Iterator<LabeledStatementNode> casesIter = node.getCases();
		List<LabeledStatementNode> casesList = new ArrayList<>();
		casesIter.forEachRemaining(casesList::add); // Use a List so we can add the default case
		LabeledStatementNode defaultCase = node.getDefaultCase();
		casesList.add(defaultCase);
		
		for (LabeledStatementNode currCase : casesList) {
			
			int caseLine = currCase.getSource().getFirstToken().getLine();
			
			if (directingLines.contains(caseLine)) {
				
				if (node.getDefaultCase().equals(currCase)) {
					// default condition is the conjunction of the negation of all case label conditions
					Iterator<LabeledStatementNode> iter = node.getCases();
					if (iter.hasNext()) {
						LabeledStatementNode c = iter.next();
						SwitchLabelNode sln = (SwitchLabelNode) c.getLabel();
						ExpressionNode caseConst = sln.getExpression().copy();
						OperatorNode condition = 
								nodeFactory.newOperatorNode(src, Operator.NEQ, swc.copy(), caseConst);
						
						for (; iter.hasNext();) {
							c = iter.next();
							sln = (SwitchLabelNode) c.getLabel();
	
							// Copy the case constant to assemble the switch edge condition
							caseConst = sln.getExpression().copy();
							OperatorNode caseCompare = nodeFactory.newOperatorNode(src, Operator.NEQ, swc.copy(), caseConst);
	
							condition = nodeFactory.newOperatorNode(src, Operator.LAND, condition, caseCompare);
						}
						
						statements.add(instrumentAssume(src, condition));
					}
					
					
				} else {
					// match the case label and return its condition
					for (Iterator<LabeledStatementNode> iter = node.getCases(); iter.hasNext();) {
						LabeledStatementNode c = iter.next();

						if (c.equals(currCase)) {
							SwitchLabelNode sln = (SwitchLabelNode) c.getLabel();
							
							// Copy the case constant to assemble the switch edge condition
							ExpressionNode caseConst = sln.getExpression().copy();
							OperatorNode caseCompare = nodeFactory.newOperatorNode(src, Operator.EQUALS, swc.copy(), caseConst);
							statements.add(instrumentAssume(src, caseCompare));
						}
					}
				}
			}
		}
		
		assert (!statements.isEmpty()) : "Expected a matching case label";
		statements.add(node.copy());
		return nodeFactory.newCompoundStatementNode(node.getSource(), statements);
	}

}
