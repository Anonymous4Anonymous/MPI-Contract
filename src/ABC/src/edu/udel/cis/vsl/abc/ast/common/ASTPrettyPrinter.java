package edu.udel.cis.vsl.abc.ast.common;

import java.io.PrintStream;
import java.util.Stack;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.IdentifierNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.PragmaNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.StaticAssertionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AllocationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssignsOrReadsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.AssumesNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.BehaviorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CallEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompositeEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.CompositeEventNode.EventOperator;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ContractNode.ContractKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsEventNode.DependsEventNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.DependsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.EnsuresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ExtendedQuantifiedExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.GuardsNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.InvariantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPICollectiveBlockNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractExpressionNode.MPIContractExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIUsesNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MemoryEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.ObjectOrRegionOfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.RequiresNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.WaitsforNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.ArrayDesignatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.DesignationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.DesignatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.FieldDesignatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.AbstractFunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.EnumeratorDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FieldDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.TypedefDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.AlignOfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ArrayLambdaNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ArrowNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CastNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.CompoundLiteralNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.DerivativeExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.DotNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.LambdaNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.QuantifiedExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.RegularRangeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ScopeOfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SizeofNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.SpawnNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.StatementExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ValueAtNode;
import edu.udel.cis.vsl.abc.ast.node.IF.label.LabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.label.OrdinaryLabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.label.SwitchLabelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpDeclarativeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpDeclarativeNode.OmpDeclarativeNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpExecutableNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpExecutableNode.OmpExecutableKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpForNode.OmpScheduleKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpFunctionReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpNode.OmpNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpParallelNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSimdNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSymbolReductionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpSyncNode.OmpSyncNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpWorksharingNode.OmpWorksharingNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.AtomicNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode.BlockItemKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ChooseStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CivlForNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ForLoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.GotoNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.IfNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.JumpNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.JumpNode.JumpKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LabeledStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.LoopNode.LoopKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ReturnNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.RunNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode.StatementKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.SwitchNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.UpdateNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.WhenNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.WithNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.ArrayTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.BasicTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.DomainTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.EnumerationTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.LambdaTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.PointerTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.StructureOrUnionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode.TypeNodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypedefNameNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeofNode;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.err.IF.ABCException;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.err.IF.ABCUnsupportedException;
import edu.udel.cis.vsl.abc.token.IF.CivlcToken;
import edu.udel.cis.vsl.abc.util.IF.Pair;

/**
 * This class implements the pretty printing of AST nodes. The purpose is to
 * print an AST in the original programming language and the output is expected
 * to be compiled into an equivalent AST.
 *
 * @author Manchun Zheng
 *
 */
public class ASTPrettyPrinter {

	/* ************************ Private Static Fields ********************** */

	private static String indention = "  ";

	private static int headerLength = 60;

	private static final StringBuffer EMPTY_STRING_BUFFER = new StringBuffer(0);

	/* ******************* Package-private Static Methods ****************** */

	/**
	 * Returns the pretty representation of the given AST node (and its
	 * descendants) in a form that should be similar to the actual programming
	 * language.
	 *
	 * @param node
	 *            the given AST node
	 * @param maxLength
	 *            the maximal length of the string representation of this node;
	 *            -1 if the length is unlimited
	 * @return the pretty representation of this AST node (and its descendants)
	 *         in a form that should be similar to the actual programming
	 *         language.
	 */
	@SuppressWarnings("unchecked")
	public static StringBuffer prettyRepresentation(ASTNode node,
			int maxLength) {
		NodeKind kind = node.nodeKind();

		switch (kind) {
			case DECLARATION_LIST :
				return declarationList2Pretty((DeclarationListNode) node,
						maxLength);
			case ENUMERATOR_DECLARATION :
				return enumeratorDeclaration2Pretty(
						(EnumeratorDeclarationNode) node, maxLength);
			case EXPRESSION :
				return expression2Pretty((ExpressionNode) node, maxLength);
			case FIELD_DECLARATION :
				return fieldDeclaration2Pretty("", (FieldDeclarationNode) node,
						maxLength);
			case FUNCTION_DECLARATION :
				return functionDeclaration2Pretty("",
						(FunctionDeclarationNode) node, maxLength);
			case FUNCTION_DEFINITION :
				return functionDeclaration2Pretty("",
						(FunctionDeclarationNode) node, maxLength);
			case IDENTIFIER :
				return new StringBuffer(((IdentifierNode) node).name());
			case OMP_NODE :
				return ompNode2Pretty("", (OmpNode) node, maxLength);
			case OMP_REDUCTION_OPERATOR :
				return ompReduction2Pretty((OmpReductionNode) node, maxLength);
			case ORDINARY_LABEL :
			case SWITCH_LABEL :
				return labelNode2Pretty((LabelNode) node, maxLength);
			case PRAGMA :
				return pragma2Pretty("", (PragmaNode) node, maxLength);
			case STATEMENT :
				return statement2Pretty("", (StatementNode) node, true, false,
						maxLength);
			case STATIC_ASSERTION :
				return staticAssertion2Pretty("", (StaticAssertionNode) node,
						maxLength);
			case TYPE :
				return type2Pretty("", (TypeNode) node, true, maxLength);
			case TYPEDEF :
				return typedefDeclaration2Pretty("",
						(TypedefDeclarationNode) node, maxLength);
			case VARIABLE_DECLARATION :
				return variableDeclaration2Pretty("",
						(VariableDeclarationNode) node, maxLength);
			case SEQUENCE :
				return sequenceNode2Pretty((SequenceNode<ASTNode>) node,
						maxLength);
			case PAIR :
				return pairNode2Pretty((PairNode<ASTNode, ASTNode>) node,
						maxLength);
			case CONTRACT :
				return contractNode2Pretty("", (ContractNode) node, maxLength);
			default :
				throw new ABCUnsupportedException(
						"the pretty printing of AST node of " + kind
								+ " kind is not supported yet.",
						node.getSource().getLocation(false));
		}
	}

	// FIX ME : I am public now
	@SuppressWarnings("unchecked")
	/**
	 * Pretty print an AST node to the given output stream in a user-friendly
	 * way.
	 *
	 * @param node
	 *            the node to be printed
	 * @param out
	 *            the output stream
	 */
	public static void prettyPrint(ASTNode node, PrintStream out) {
		NodeKind kind = node.nodeKind();

		switch (kind) {
			case DECLARATION_LIST :
				out.print(
						declarationList2Pretty((DeclarationListNode) node, -1));
				break;
			case ENUMERATOR_DECLARATION :
				out.print(enumeratorDeclaration2Pretty(
						(EnumeratorDeclarationNode) node, -1));
				break;
			case EXPRESSION :
				out.print(expression2Pretty((ExpressionNode) node, -1));
				break;
			case FIELD_DECLARATION :
				out.print(fieldDeclaration2Pretty("",
						(FieldDeclarationNode) node, -1));
				break;
			case FUNCTION_DECLARATION :
				pPrintFunctionDeclaration(out, "",
						(FunctionDeclarationNode) node);
				break;
			case FUNCTION_DEFINITION :
				pPrintFunctionDeclaration(out, "",
						(FunctionDeclarationNode) node);
				break;
			case IDENTIFIER :
				out.print(((IdentifierNode) node).name());
				break;
			case OMP_NODE :
				pPrintOmpNode(out, "", (OmpNode) node);
				break;
			case OMP_REDUCTION_OPERATOR :
				out.print(ompReduction2Pretty((OmpReductionNode) node, -1));
				break;
			case ORDINARY_LABEL :
			case SWITCH_LABEL :
				out.print(labelNode2Pretty((LabelNode) node, -1));
				break;
			case PRAGMA :
				pPrintPragma(out, "", (PragmaNode) node);
				break;
			case STATEMENT :
				pPrintStatement(out, "", (StatementNode) node, true, false);
				break;
			case STATIC_ASSERTION :
				pPrintStaticAssertion(out, "", (StaticAssertionNode) node);
				break;
			case TYPE :
				out.print(type2Pretty("", (TypeNode) node, true, -1));
				break;
			case TYPEDEF :
				pPrintTypedefDeclaration(out, "",
						(TypedefDeclarationNode) node);
				break;
			case VARIABLE_DECLARATION :
				out.print(variableDeclaration2Pretty("",
						(VariableDeclarationNode) node, -1));
				break;
			case SEQUENCE :
				pPrintSequenceNode((SequenceNode<ASTNode>) node, out);
				break;
			case PAIR :
				pPrintPairNode((PairNode<ASTNode, ASTNode>) node, out);
				break;
			default :
				throw new ABCUnsupportedException(
						"the pretty printing of AST node of " + kind
								+ " kind is not supported yet.",
						node.getSource().getLocation(false));
		}
	}

	private static void pPrintPairNode(PairNode<ASTNode, ASTNode> pair,
			PrintStream out) {
		ASTNode left = pair.getLeft(), right = pair.getRight();

		out.print("(");
		if (left != null)
			prettyPrint(left, out);
		else
			out.print("NULL");
		out.print(",");
		if (right != null)
			prettyPrint(right, out);
		else
			out.print("NULL");
	}

	private static StringBuffer pairNode2Pretty(PairNode<ASTNode, ASTNode> pair,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		ASTNode left = pair.getLeft(), right = pair.getRight();

		result.append("(");
		if (left != null)
			result.append(prettyRepresentation(left,
					vacantLength(maxLength, result)));
		else
			result.append("NULL");
		result.append(",");
		if (right != null)
			result.append(prettyRepresentation(right,
					vacantLength(maxLength, result)));
		else
			result.append("NULL");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintSequenceNode(
			SequenceNode<? extends ASTNode> sequence, PrintStream out) {
		int numChildren = sequence.numChildren();
		for (int i = 0; i < numChildren; i++) {
			ASTNode node = sequence.getSequenceChild(i);
			if (i != 0)
				out.print(", ");
			if (node != null)
				prettyPrint(node, out);
		}
	}

	private static StringBuffer sequenceNode2Pretty(
			SequenceNode<? extends ASTNode> sequence, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		int numChildren = sequence.numChildren();

		for (int i = 0; i < numChildren; i++) {
			ASTNode node = sequence.getSequenceChild(i);

			if (i != 0)
				result.append(", ");
			if (node != null)
				result.append(prettyRepresentation(node,
						vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	static void prettyPrint(AST ast, PrintStream out, boolean ignoreStdLibs) {
		SequenceNode<BlockItemNode> root = ast.getRootNode();
		int numChildren = root.numChildren();
		String currentFile = null;

		for (int i = 0; i < numChildren; i++) {
			BlockItemNode child = root.getSequenceChild(i);

			if (child != null) {
				String sourceFile = child.getSource().getFirstToken()
						.getSourceFile().getName();

				if (ignoreStdLibs)
					switch (sourceFile) {
						case "assert.h" :
						case "cuda.h" :
						case "civlc.cvh" :
						case "bundle.cvh" :
						case "comm.cvh" :
						case "concurrency.cvh" :
						case "pointer.cvh" :
						case "scope.cvh" :
						case "seq.cvh" :
						case "float.h" :
						case "math.h" :
						case "mpi.h" :
						case "omp.h" :
						case "op.h" :
						case "pthread.h" :
						case "stdarg.h" :
						case "stdbool.h" :
						case "stddef.h" :
						case "stdio.h" :
						case "stdlib.h" :
						case "string.h" :
						case "time.h" :
						case "civl-omp.cvh" :
						case "civl-mpi.cvh" :
						case "civl-cuda.cvh" :
						case "civl-omp.cvl" :
						case "civl-mpi.cvl" :
						case "civl-cuda.cvl" :
						case "civlc.cvl" :
						case "concurrency.cvl" :
						case "stdio-c.cvl" :
						case "stdio.cvl" :
						case "omp.cvl" :
						case "cuda.cvl" :
						case "mpi.cvl" :
						case "pthread-c.cvl" :
						case "pthread.cvl" :
						case "math.cvl" :
						case "seq.cvl" :
						case "string.cvl" :
							continue;
						default :
					}
				if (currentFile == null || !currentFile.equals(sourceFile)) {
					int fileLength = sourceFile.length();
					int leftBarLength, rightBarLength;

					rightBarLength = (headerLength - fileLength - 4) / 2;
					leftBarLength = headerLength - fileLength - 4
							- rightBarLength;
					out.print("//");
					printBar(leftBarLength, '=', out);
					out.print(" ");
					out.print(sourceFile);
					out.print(" ");
					printBar(rightBarLength, '=', out);
					out.print("\n");
					currentFile = sourceFile;
				}
				// pPrintExternalDef(out, child);
				pPrintBlockItem(out, "", child);
				out.println();
			}
		}
	}

	private static void printBar(int length, char symbol, PrintStream out) {
		for (int i = 0; i < length; i++)
			out.print(symbol);
	}

	/* *************************** Private Methods ************************* */

	private static void pPrintOmpNode(PrintStream out, String prefix,
			OmpNode ompNode) {
		OmpNodeKind kind = ompNode.ompNodeKind();

		switch (kind) {
			case DECLARATIVE :
				out.print(ompDeclarative2Pretty(prefix,
						(OmpDeclarativeNode) ompNode, -1));
			default :// EXECUTABLE
				pPrintOmpStatement(out, prefix, (OmpExecutableNode) ompNode);
		}
	}

	private static StringBuffer ompNode2Pretty(String prefix, OmpNode ompNode,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		OmpNodeKind kind = ompNode.ompNodeKind();

		switch (kind) {
			case DECLARATIVE :
				result.append(ompDeclarative2Pretty(prefix,
						(OmpDeclarativeNode) ompNode,
						vacantLength(maxLength, result)));
			default :// EXECUTABLE
				result.append(
						ompStatement2Pretty(prefix, (OmpExecutableNode) ompNode,
								vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer structOrUnion2Pretty(String prefix,
			StructureOrUnionTypeNode strOrUnion, boolean isTypeDeclaration,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		String myIndent = prefix + indention;
		SequenceNode<FieldDeclarationNode> fields = strOrUnion
				.getStructDeclList();

		result.append(prefix);
		if (strOrUnion.isStruct())
			result.append("struct ");
		else
			result.append("union ");
		if (strOrUnion.getName() != null)
			result.append(strOrUnion.getName());
		if (/* isTypeDeclaration && */fields != null) {
			int numFields = fields.numChildren();

			result.append("{");
			for (int i = 0; i < numFields; i++) {
				FieldDeclarationNode field = fields.getSequenceChild(i);

				result.append("\n");
				// if (!(field.getTypeNode() instanceof
				// StructureOrUnionTypeNode))
				// result.append(myIndent);
				result.append(fieldDeclaration2Pretty(myIndent, field,
						vacantLength(maxLength, result)));
				result.append(";");
			}
			result.append("\n");
			result.append(prefix);
			result.append("}");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer fieldDeclaration2Pretty(String prefix,
			FieldDeclarationNode field, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		String type;
		StringBuffer result = new StringBuffer();
		String fieldName = field.getName();

		type = type2Pretty(prefix, field.getTypeNode(), true, maxLength)
				.toString();
		if (type.endsWith("]")) {
			Pair<String, String> typeResult = processArrayType(type);

			result.append(typeResult.left);
			result.append(" ");
			if (fieldName != null) {
				result.append(" ");
				result.append(fieldName);
			}
			result.append(typeResult.right);
		} else {
			result.append(type);
			if (fieldName != null) {
				result.append(" ");
				result.append(field.getName());
			}
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintStaticAssertion(PrintStream out, String prefix,
			StaticAssertionNode assertion) {
		out.print(prefix);
		out.print("(");
		out.print(expression2Pretty(assertion.getExpression(), -1));
		out.print(", \"");
		out.print(assertion.getMessage().getStringRepresentation());
		out.print("\")");
	}

	private static StringBuffer staticAssertion2Pretty(String prefix,
			StaticAssertionNode assertion, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append(prefix);
		result.append("(");
		result.append(expression2Pretty(assertion.getExpression(),
				vacantLength(maxLength, result)));
		result.append(", \"");
		result.append(assertion.getMessage().getStringRepresentation());
		result.append("\")");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintPragma(PrintStream out, String prefix,
			PragmaNode pragma) {
		Iterable<CivlcToken> tokens = pragma.getTokens();

		out.print(prefix);
		out.print("#pragma ");
		out.print(pragma.getPragmaIdentifier().name());

		for (CivlcToken token : tokens) {
			out.print(" ");
			out.print(token.getText());
		}
	}

	private static StringBuffer pragma2Pretty(String prefix, PragmaNode pragma,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		Iterable<CivlcToken> tokens = pragma.getTokens();

		result.append(prefix);
		result.append("#pragma ");
		result.append(pragma.getPragmaIdentifier().name());

		for (CivlcToken token : tokens) {
			result.append(" ");
			result.append(token.getText());
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer enumType2Pretty(String prefix,
			EnumerationTypeNode enumeration, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		IdentifierNode tag = enumeration.getTag();
		SequenceNode<EnumeratorDeclarationNode> enumerators = enumeration
				.enumerators();
		String myIndent = prefix + indention;

		result.append(prefix);
		result.append("enum ");
		if (tag != null)
			result.append(tag.name());
		if (enumerators != null) {
			int num = enumerators.numChildren();

			result.append("{");
			for (int i = 0; i < num; i++) {
				EnumeratorDeclarationNode enumerator = enumerators
						.getSequenceChild(i);

				if (i != 0)
					result.append(",");
				result.append("\n");
				result.append(myIndent);
				result.append(enumeratorDeclaration2Pretty(enumerator,
						vacantLength(maxLength, result)));
			}
			result.append("\n");
			result.append(prefix);
			result.append("}");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer enumeratorDeclaration2Pretty(
			EnumeratorDeclarationNode enumerator, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append(enumerator.getName());
		if (enumerator.getValue() != null) {
			result.append("=");
			result.append(expression2Pretty(enumerator.getValue(),
					maxLength - result.length()));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer trimStringBuffer(StringBuffer input,
			int length) {
		if (length > 0 && input.length() > length)
			return new StringBuffer(input.substring(0, length));
		return input;
	}

	private static StringBuffer ompDeclarative2Pretty(String prefix,
			OmpDeclarativeNode ompDeclarative, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		OmpDeclarativeNodeKind kind = ompDeclarative.ompDeclarativeNodeKind();

		result.append("#pragma omp ");
		switch (kind) {
			case REDUCTION :
				result.append("reduction");
				break;
			case THREADPRIVATE :
				result.append("threadprivate");
				break;
			default :
				throw new ABCUnsupportedException(
						"The OpenMP declarative directive " + kind
								+ " is not supported yet.",
						ompDeclarative.getSource().getLocation(false));
		}
		result.append("(");
		result.append(sequenceExpression2Pretty(ompDeclarative.variables(),
				vacantLength(maxLength, result)));
		result.append(")");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintFunctionDeclaration(PrintStream out,
			String prefix, FunctionDeclarationNode function) {
		TypeNode typeNode = function.getTypeNode();
		SequenceNode<ContractNode> contracts = function.getContract();

		if (contracts != null && contracts.numChildren() > 0)
			pPrintContracts(out, prefix, contracts);
		out.print(prefix);
		if (function instanceof AbstractFunctionDefinitionNode) {
			out.print("$abstract ");

			AbstractFunctionDefinitionNode af = (AbstractFunctionDefinitionNode) function;
			SequenceNode<PairNode<ExpressionNode, ExpressionNode>> intervals = af
					.getIntervals();

			if (intervals != null) {
				out.print("$differentiable(" + af.continuity() + ", ");
				for (PairNode<ExpressionNode, ExpressionNode> interval : intervals) {
					out.print("[");
					out.print(expression2Pretty(interval.getLeft(), -1));
					out.print(",");
					out.print(expression2Pretty(interval.getRight(), -1));
					out.print("]");
				}
				out.print(") ");
			}
		}
		if (function.hasGlobalFunctionSpecifier())
			out.print("__global__ ");
		if (function.hasAtomicFunctionSpecifier())
			out.print("$atomic_f ");
		if (function.hasSystemFunctionSpecifier()) {
			String fileName = function.getSource().getFirstToken()
					.getSourceFile().getName();
			int dotIndex = fileName.lastIndexOf(".");

			out.print("$system");
			if (dotIndex >= 0) {
				String systemLib = function.getSystemLibrary();

				out.print("[");
				if (systemLib != null)
					out.print(systemLib);
				else
					out.print(fileName.substring(0, fileName.lastIndexOf(".")));
				out.print("] ");
			}
		}
		if (function.hasStatefFunctionSpecifier())
			out.print("$state_f ");
		if (function.hasPureFunctionSpecifier())
			out.print("$pure ");
		if (function.hasInlineFunctionSpecifier())
			out.print("inline ");
		if (function.hasNoreturnFunctionSpecifier())
			out.print("_Noreturn ");

		if (typeNode instanceof FunctionTypeNode) {
			FunctionTypeNode functionTypeNode = (FunctionTypeNode) typeNode;

			TypeNode returnType = functionTypeNode.getReturnType();
			SequenceNode<VariableDeclarationNode> paras = functionTypeNode
					.getParameters();
			int numOfParas = paras.numChildren();

			out.print(type2Pretty("", returnType, false, -1));
			out.print(" ");
			out.print(function.getName());
			out.print("(");
			for (int i = 0; i < numOfParas; i++) {
				if (i != 0)
					out.print(", ");
				out.print(variableDeclaration2Pretty("",
						paras.getSequenceChild(i), -1));
			}
			if (functionTypeNode.hasVariableArgs())
				out.print(", ...");
			out.print(")");
		} else {
			out.print(type2Pretty("", typeNode, false, -1));
			out.print(" ");
			out.print(function.getName());
		}
		if (function instanceof FunctionDefinitionNode) {
			CompoundStatementNode body = ((FunctionDefinitionNode) function)
					.getBody();

			out.print(" ");
			pPrintCompoundStatement(out, prefix + indention, body, true, false);
		} else {
			out.print(";");
		}
	}

	private static StringBuffer functionDeclaration2Pretty(String prefix,
			FunctionDeclarationNode function, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		SequenceNode<ContractNode> contracts = function.getContract();

		if (contracts != null && contracts.numChildren() > 0) {
			result.append(contracts2Pretty(prefix, contracts,
					vacantLength(maxLength, result)));
		}
		result.append(prefix);
		if (function instanceof AbstractFunctionDefinitionNode) {
			result.append("$abstract ");

			AbstractFunctionDefinitionNode af = (AbstractFunctionDefinitionNode) function;
			SequenceNode<PairNode<ExpressionNode, ExpressionNode>> intervals = af
					.getIntervals();

			if (intervals != null) {
				result.append("$differentiable(" + af.continuity() + ", ");
				for (PairNode<ExpressionNode, ExpressionNode> interval : intervals) {
					result.append("[");
					result.append(
							expression2Pretty(interval.getLeft(), maxLength));
					result.append(",");
					result.append(
							expression2Pretty(interval.getRight(), maxLength));
					result.append("]");
				}
				result.append(") ");
			}

		}
		if (function.hasGlobalFunctionSpecifier())
			result.append("__global__ ");
		if (function.hasAtomicFunctionSpecifier())
			result.append("$atomic_f ");
		if (function.hasSystemFunctionSpecifier()) {
			String fileName = function.getSource().getFirstToken()
					.getSourceFile().getName();
			int dotIndex = fileName.lastIndexOf(".");

			result.append("$system");
			if (dotIndex >= 0) {
				String systemLib = function.getSystemLibrary();

				result.append("[");
				if (systemLib != null)
					result.append(systemLib);
				else
					result.append(
							fileName.substring(0, fileName.lastIndexOf(".")));
				result.append("] ");
			}
		}
		if (function.hasStatefFunctionSpecifier())
			result.append("$state_f ");
		if (function.hasPureFunctionSpecifier())
			result.append("$pure ");
		if (function.hasInlineFunctionSpecifier())
			result.append("inline ");
		if (function.hasNoreturnFunctionSpecifier())
			result.append("_Noreturn ");

		TypeNode typeNode = function.getTypeNode();

		if (typeNode instanceof FunctionTypeNode) {
			FunctionTypeNode functionTypeNode = (FunctionTypeNode) typeNode;
			TypeNode returnType = functionTypeNode.getReturnType();
			SequenceNode<VariableDeclarationNode> paras = functionTypeNode
					.getParameters();
			int numOfParas = paras.numChildren();

			result.append(type2Pretty("", returnType, false,
					vacantLength(maxLength, result)));
			result.append(" ");
			result.append(function.getName());
			result.append("(");
			for (int i = 0; i < numOfParas; i++) {
				if (i != 0)
					result.append(", ");
				result.append(variableDeclaration2Pretty("",
						paras.getSequenceChild(i),
						vacantLength(maxLength, result)));
			}
			if (functionTypeNode.hasVariableArgs())
				result.append(", ...");
			result.append(")");
		} else {
			result.append(type2Pretty("", typeNode, false,
					vacantLength(maxLength, result)));
			result.append(" ");
			result.append(function.getName());
		}
		if (function instanceof FunctionDefinitionNode) {
			CompoundStatementNode body = ((FunctionDefinitionNode) function)
					.getBody();

			result.append(" ");
			compoundStatement2Pretty(prefix + indention, body, true, false,
					vacantLength(maxLength, result));
		} else {
			result.append(";");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintContracts(PrintStream out, String prefix,
			SequenceNode<ContractNode> contracts) {
		String newLinePrefix = "\n" + prefix + "  @ ";
		int numContracts = contracts.numChildren();
		boolean isFirst = true;

		out.print(prefix);
		out.print("/*@ ");
		for (int i = 0; i < numContracts; i++) {
			ContractNode contract = contracts.getSequenceChild(i);

			if (isFirst)
				isFirst = false;
			else
				out.print(newLinePrefix);
			out.print(contractNode2Pretty(newLinePrefix, contract, -1));
		}
		out.print("\n");
		out.print(prefix);
		out.print("  @*/\n");

	}

	private static StringBuffer contracts2Pretty(String prefix,
			SequenceNode<ContractNode> contracts, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		String newLinePrefix = "\n" + prefix + "  @ ";
		int numContracts = contracts.numChildren();
		boolean isFirst = true;

		result.append(prefix);
		result.append("/*@ ");
		for (int i = 0; i < numContracts; i++) {
			ContractNode contract = contracts.getSequenceChild(i);

			if (isFirst)
				isFirst = false;
			else
				result.append(newLinePrefix);
			result.append(contractNode2Pretty(newLinePrefix, contract,
					vacantLength(maxLength, result)));
		}
		result.append("\n");
		result.append(prefix);
		result.append("  @*/\n");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer contractNode2Pretty(String prefix,
			ContractNode contract, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		ContractKind kind = contract.contractKind();

		switch (kind) {
			case ALLOCATES_OR_FREES : {
				AllocationNode allocation = (AllocationNode) contract;

				if (allocation.isAllocates())
					result.append("allocates ");
				else
					result.append("frees ");
				result.append(sequenceExpression2Pretty(allocation.memoryList(),
						vacantLength(maxLength, result)));
				break;
			}
			case ASSUMES : {
				AssumesNode assumes = (AssumesNode) contract;

				result.append("assumes ");
				result.append(expression2Pretty(assumes.getPredicate(),
						vacantLength(maxLength, result)));
				result.append(";");
				break;
			}
			case ASSIGNS_READS : {
				AssignsOrReadsNode assignsOrReads = (AssignsOrReadsNode) contract;
				// ExpressionNode condition = assignsOrReads.getCondition();

				if (assignsOrReads.isAssigns())
					result.append("assigns ");
				else
					result.append("reads ");
				result.append(
						sequenceNode2Pretty(assignsOrReads.getMemoryList(),
								vacantLength(maxLength, result)));
				result.append(";");
				break;
			}
			case DEPENDS : {
				DependsNode depends = (DependsNode) contract;

				result.append("depends_on ");
				result.append(
						sequenceDependsEvent2Pretty(depends.getEventList(),
								vacantLength(maxLength, result)));
				result.append(";");
				break;
			}
			case ENSURES : {
				EnsuresNode ensures = (EnsuresNode) contract;

				result.append("ensures ");
				result.append(expression2Pretty(ensures.getExpression(),
						vacantLength(maxLength, result)));
				result.append(";");
				break;
			}
			case GUARDS : {
				GuardsNode guard = (GuardsNode) contract;

				result.append("executes_when ");
				result.append(expression2Pretty(guard.getExpression(),
						vacantLength(maxLength, result)));
				result.append(";");
				break;
			}
			case MPI_COLLECTIVE: {
				MPICollectiveBlockNode colBlock = (MPICollectiveBlockNode) contract;

				result.append(prefix);
				prefix += "  ";
				result.append("mpi collective(");
				result.append(expression2Pretty(colBlock.getMPIComm(),
						vacantLength(maxLength, result)));
				result.append("):\n");
				for (ContractNode clause : colBlock.getBody())
					result.append(contractNode2Pretty(prefix, clause,
							vacantLength(maxLength, result)));
				break;
			}
			case REQUIRES : {
				RequiresNode requires = (RequiresNode) contract;

				result.append("requires ");
				result.append(expression2Pretty(requires.getExpression(),
						vacantLength(maxLength, result)));
				result.append(";");
				break;
			}
			case BEHAVIOR : {
				BehaviorNode behavior = (BehaviorNode) contract;
				SequenceNode<ContractNode> body = behavior.getBody();
				String indentedNewLinePrefix = prefix + "  ";

				result.append("behavior ");
				result.append(behavior.getName().name());
				result.append(":");
				for (ContractNode clause : body) {
					// result.append("\n");
					result.append(indentedNewLinePrefix);
					result.append(contractNode2Pretty(indentedNewLinePrefix,
							clause, vacantLength(maxLength, result)));
				}
				break;
			}
			case INVARIANT : {
				InvariantNode invariant = (InvariantNode) contract;

				if (invariant.isLoopInvariant())
					result.append("loop ");
				result.append("invariant ");
				result.append(expression2Pretty(invariant.getExpression(),
						vacantLength(maxLength, result)));
				result.append(";");
				break;
			}
			case PURE : {
				result.append("pure;");
				break;
			}
			case WAITSFOR : {
				WaitsforNode waitsforNode = (WaitsforNode) contract;

				result.append("waitsfor ");
				result.append(sequenceNode2Pretty(waitsforNode.getArguments(),
						vacantLength(maxLength, result)));
				break;
			}
			case MPI_USES : {
				MPIUsesNode mpiUsesNode = (MPIUsesNode) contract;
				result.append("mpi uses ");
				result.append(sequenceNode2Pretty(mpiUsesNode.getArguments(),
						vacantLength(maxLength, result)));
			}
			case MPI_EVENT : {
				MPIContractAbsentEventNode eventNode = (MPIContractAbsentEventNode) contract;

				result.append(prettyAbsentEvent(eventNode, maxLength));
				break;
			}
			default :
				throw new ABCUnsupportedException(
						"pretty printing contract node of " + kind + " kind");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer sequenceDependsEvent2Pretty(
			SequenceNode<DependsEventNode> eventList, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		boolean isFirst = true;

		for (DependsEventNode event : eventList) {
			if (isFirst)
				isFirst = false;
			else
				result.append(", ");
			result.append(dependsEvent2Pretty(event,
					vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer dependsEvent2Pretty(DependsEventNode event,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		DependsEventNodeKind kind = event.getEventKind();
		StringBuffer result = new StringBuffer();

		switch (kind) {
			case MEMORY : {
				MemoryEventNode rwEvent = (MemoryEventNode) event;

				if (rwEvent.isRead())
					result.append("\\read");
				else if (rwEvent.isWrite())
					result.append("\\write");
				else
					result.append("\\access");
				result.append("(");
				result.append(sequenceExpression2Pretty(rwEvent.getMemoryList(),
						vacantLength(maxLength, result)));
				result.append(")");
				break;
			}
			case COMPOSITE : {
				CompositeEventNode opEvent = (CompositeEventNode) event;
				EventOperator op = opEvent.eventOperator();

				result.append("(");
				result.append(dependsEvent2Pretty(opEvent.getLeft(),
						vacantLength(maxLength, result)));
				result.append(")");
				switch (op) {
					case UNION :
						result.append(" + ");
						break;
					case DIFFERENCE :
						result.append(" - ");
						break;
					case INTERSECT :
						result.append(" & ");
						break;
					default :
						throw new ABCUnsupportedException(
								"pretty printing depends event node with "
										+ kind + " operator");
				}
				result.append("(");
				result.append(dependsEvent2Pretty(opEvent.getRight(),
						vacantLength(maxLength, result)));
				result.append(")");
				break;
			}
			case CALL : {
				CallEventNode callEvent = (CallEventNode) event;
				SequenceNode<ExpressionNode> args = callEvent.arguments();

				result.append("\\call(");
				result.append(callEvent.getFunction().getIdentifier().name());
				if (args.numChildren() > 0)
					result.append(", ");
				result.append(sequenceExpression2Pretty(callEvent.arguments(),
						vacantLength(maxLength, result)));
				result.append(")");
				break;
			}
			case NOACT :
				result.append("\\nothing");
				break;
			case ANYACT :
				result.append("\\anyact");
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty printing depends event node of " + kind
								+ " kind");
		}
		return trimStringBuffer(result, maxLength);
	}

	@SuppressWarnings("unused")
	private static void pPrintContract(PrintStream out, String prefix,
			ContractNode contract) {
		ContractKind kind = contract.contractKind();

		out.print(prefix);
		switch (kind) {
			case ASSIGNS_READS : {
				AssignsOrReadsNode assignsOrReads = (AssignsOrReadsNode) contract;
				// ExpressionNode condition = assignsOrReads.getCondition();

				if (assignsOrReads.isAssigns())
					out.print("$assigns");
				else
					out.print("$reads");
				// if (condition != null) {
				// out.print(" [");
				// out.print(expression2Pretty(condition));
				// out.print("] ");
				// }
				out.print("{");
				pPrintSequenceNode(assignsOrReads.getMemoryList(), out);
				out.print("}");
				break;
			}
			case DEPENDS : {
				DependsNode depends = (DependsNode) contract;
				// ExpressionNode condition = depends.getCondition();

				out.print("depends");
				// if (condition != null) {
				// out.print(" [");
				// out.print(expression2Pretty(condition));
				// out.print("] ");
				// }
				out.print("{");
				// out.print(sequenceExpression2Pretty(depends.getEventList()));
				out.print("}");
				break;
			}
			case ENSURES : {
				EnsuresNode ensures = (EnsuresNode) contract;

				out.print("$ensures");
				out.print("{");
				out.print(expression2Pretty(ensures.getExpression(), -1));
				out.print("}");
				break;
			}
			case GUARDS : {
				GuardsNode guard = (GuardsNode) contract;

				out.print("$guard");
				out.print("{");
				out.print(expression2Pretty(guard.getExpression(), -1));
				out.print("}");
				break;
			}
			case REQUIRES : {
				RequiresNode requires = (RequiresNode) contract;

				out.print("$requires");
				out.print("{");
				out.print(expression2Pretty(requires.getExpression(), -1));
				out.print("}");
				break;
			}
			default :
				throw new ABCUnsupportedException(
						"pretty printing contract node of " + kind + " kind");
		}
	}

	private static void pPrintCompoundStatement(PrintStream out, String prefix,
			CompoundStatementNode compound, boolean isBody,
			boolean isSwitchBody) {
		int numChildren = compound.numChildren();
		String myIndent = prefix;
		String myPrefix = prefix;

		if (isBody) {
			myPrefix = prefix.substring(0,
					prefix.length() - 2 > 0 ? prefix.length() - 2 : 0);
		} else {
			out.print(myPrefix);
			myIndent = prefix + indention;
		}
		out.print("{\n");
		for (int i = 0; i < numChildren; i++) {
			BlockItemNode child = compound.getSequenceChild(i);

			if (child != null) {
				if (isSwitchBody && !(child instanceof LabeledStatementNode))
					pPrintBlockItem(out, myIndent + indention, child);
				else
					pPrintBlockItem(out, myIndent, child);
				out.print("\n");
			}
		}
		out.print(myPrefix);
		out.print("}");
	}

	private static StringBuffer compoundStatement2Pretty(String prefix,
			CompoundStatementNode compound, boolean isBody,
			boolean isSwitchBody, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		int numChildren = compound.numChildren();
		String myIndent = prefix;
		String myPrefix = prefix;

		if (isBody) {
			myPrefix = prefix.substring(0,
					prefix.length() - 2 > 0 ? prefix.length() - 2 : 0);
			// result.append(myPrefix);
		} else {
			result.append(myPrefix);
			myIndent = prefix + indention;
		}
		result.append("{\n");
		for (int i = 0; i < numChildren; i++) {
			BlockItemNode child = compound.getSequenceChild(i);

			if (child != null) {
				if (isSwitchBody && !(child instanceof LabeledStatementNode))
					result.append(blockItem2Pretty(myIndent + indention, child,
							vacantLength(maxLength, result)));
				else
					result.append(blockItem2Pretty(myIndent, child,
							vacantLength(maxLength, result)));
				result.append("\n");
			}
		}
		result.append(myPrefix);
		result.append("}");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintBlockItem(PrintStream out, String prefix,
			BlockItemNode block) {
		BlockItemKind kind = block.blockItemKind();

		switch (kind) {
			case STATEMENT :
				pPrintStatement(out, prefix, (StatementNode) block, false,
						false);
				break;
			case ORDINARY_DECLARATION :
				if (block instanceof VariableDeclarationNode) {
					out.print(variableDeclaration2Pretty(prefix,
							(VariableDeclarationNode) block, -1));

					out.print(";");
				} else if (block instanceof FunctionDeclarationNode)
					pPrintFunctionDeclaration(out, prefix,
							(FunctionDeclarationNode) block);
				break;
			case TYPEDEF :
				pPrintTypedefDeclaration(out, prefix,
						(TypedefDeclarationNode) block);
				out.print(";");
				break;
			case ENUMERATION :
				out.print(
						enumType2Pretty(prefix, (EnumerationTypeNode) block, -1)
								+ ";");
				break;
			case OMP_DECLARATIVE :
				out.print(ompDeclarative2Pretty(prefix,
						(OmpDeclarativeNode) block, -1));
				break;
			case PRAGMA :
				pPrintPragma(out, prefix, (PragmaNode) block);
				break;
			case STRUCT_OR_UNION :
				out.print(structOrUnion2Pretty(prefix,
						(StructureOrUnionTypeNode) block, true, -1));
				out.print(";");
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty print of block item node of " + kind + " kind");
		}
	}

	private static StringBuffer blockItem2Pretty(String prefix,
			BlockItemNode block, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		BlockItemKind kind = block.blockItemKind();

		switch (kind) {
			case STATEMENT :
				return statement2Pretty(prefix, (StatementNode) block, false,
						false, maxLength);
			case ORDINARY_DECLARATION :
				if (block instanceof VariableDeclarationNode) {
					result.append(variableDeclaration2Pretty(prefix,
							(VariableDeclarationNode) block,
							vacantLength(maxLength, result)));
					result.append(";");
				} else if (block instanceof FunctionDeclarationNode)
					return functionDeclaration2Pretty(prefix,
							(FunctionDeclarationNode) block, maxLength);
				break;
			case TYPEDEF :
				result.append(typedefDeclaration2Pretty(prefix,
						(TypedefDeclarationNode) block, maxLength));
				result.append(";");
				break;
			case ENUMERATION :
				result.append(enumType2Pretty(prefix,
						(EnumerationTypeNode) block, maxLength));
				result.append(";");
				break;
			case OMP_DECLARATIVE :
				return ompDeclarative2Pretty(prefix, (OmpDeclarativeNode) block,
						maxLength);
			case PRAGMA :
				return pragma2Pretty(prefix, (PragmaNode) block, maxLength);
			case STRUCT_OR_UNION :
				result.append(structOrUnion2Pretty(prefix,
						(StructureOrUnionTypeNode) block, true, maxLength));
				result.append(";");
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty print of block item node of " + kind + " kind");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintTypedefDeclaration(PrintStream out, String prefix,
			TypedefDeclarationNode typedef) {

		out.print(prefix);
		out.print("typedef ");
		out.print(type2Pretty(prefix, typedef.getTypeNode(), true, -1));
		out.print(" ");
		out.print(typedef.getName());
	}

	private static StringBuffer typedefDeclaration2Pretty(String prefix,
			TypedefDeclarationNode typedef, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append(prefix);
		result.append("typedef ");
		result.append(type2Pretty(prefix, typedef.getTypeNode(), true,
				vacantLength(maxLength, result)));
		result.append(" ");
		result.append(typedef.getName());
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintStatement(PrintStream out, String prefix,
			StatementNode statement, boolean isBody, boolean isSwitchBody) {
		StatementKind kind = statement.statementKind();

		switch (kind) {
			// case ASSUME:
			// pPrintAssume(out, prefix, (AssumeNode) statement);
			// break;
			// case ASSERT:
			// pPrintAssert(out, prefix, (AssertNode) statement);
			// break;
			case ATOMIC :
				pPrintAtomic(out, prefix, (AtomicNode) statement);
				break;
			case COMPOUND :
				pPrintCompoundStatement(out, prefix,
						(CompoundStatementNode) statement, isBody,
						isSwitchBody);
				break;
			case EXPRESSION :
				pPrintExpressionStatement(out, prefix,
						(ExpressionStatementNode) statement);
				break;
			case CHOOSE :
				pPrintChooseStatement(out, prefix,
						(ChooseStatementNode) statement);
				break;
			case CIVL_FOR :
				pPrintCivlForStatement(out, prefix, (CivlForNode) statement);
				break;
			case IF :
				pPrintIf(out, prefix, (IfNode) statement);
				break;
			case JUMP :
				pPrintJump(out, prefix, (JumpNode) statement);
				break;
			case LABELED :
				pPrintLabeled(out, prefix, (LabeledStatementNode) statement);
				break;
			case LOOP :
				pPrintLoop(out, prefix, (LoopNode) statement);
				break;
			case NULL :
				out.print(prefix);
				out.print(";");
				break;
			case OMP :
				pPrintOmpStatement(out, prefix, (OmpExecutableNode) statement);
				break;
			case RUN :
				out.print(run2Pretty(prefix, (RunNode) statement, -1));
				break;
			case SWITCH :
				pPrintSwitch(out, prefix, (SwitchNode) statement);
				break;
			case UPDATE :
				pPrintUpdate(out, prefix, (UpdateNode) statement);
				break;
			case WHEN :
				pPrintWhen(out, prefix, (WhenNode) statement);
				break;
			case WITH :
				out.print(with2Pretty(prefix, (WithNode) statement, -1));
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty print of statement node of " + kind + " kind");
		}
	}

	private static StringBuffer statement2Pretty(String prefix,
			StatementNode statement, boolean isBody, boolean isSwitchBody,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StatementKind kind = statement.statementKind();

		switch (kind) {
			case ATOMIC :
				return atomic2Pretty(prefix, (AtomicNode) statement, maxLength);
			case COMPOUND :
				return compoundStatement2Pretty(prefix,
						(CompoundStatementNode) statement, isBody, isSwitchBody,
						maxLength);
			case EXPRESSION :
				return expressionStatement2Pretty(prefix,
						(ExpressionStatementNode) statement, maxLength);
			case CHOOSE :
				return chooseStatement2Pretty(prefix,
						(ChooseStatementNode) statement, maxLength);
			case CIVL_FOR :
				return civlForStatement2Pretty(prefix, (CivlForNode) statement,
						maxLength);
			case IF :
				return if2Pretty(prefix, (IfNode) statement, maxLength);
			case JUMP :
				return jump2Pretty(prefix, (JumpNode) statement, maxLength);
			case LABELED :
				return labeled2Pretty(prefix, (LabeledStatementNode) statement,
						maxLength);
			case LOOP :
				return loop2Pretty(prefix, (LoopNode) statement, maxLength);
			case NULL : {
				StringBuffer result = new StringBuffer();

				result.append(prefix);
				result.append(";");
				return trimStringBuffer(result, maxLength);
			}
			case OMP :
				return ompStatement2Pretty(prefix,
						(OmpExecutableNode) statement, maxLength);
			case RUN :
				return run2Pretty(prefix, (RunNode) statement, maxLength);
			case SWITCH :
				return switch2Pretty(prefix, (SwitchNode) statement, maxLength);
			case UPDATE :
				return update2Pretty(prefix, (UpdateNode) statement, maxLength);
			case WHEN :
				return when2Pretty(prefix, (WhenNode) statement, maxLength);
			case WITH :
				return with2Pretty(prefix, (WithNode) statement, maxLength);

			default :
				throw new ABCUnsupportedException(
						"pretty print of statement node of " + kind + " kind");
		}
	}

	private static void pPrintChooseStatement(PrintStream out, String prefix,
			ChooseStatementNode choose) {
		int numChildren = choose.numChildren();
		String myIndent = prefix + indention;

		out.print(prefix);
		out.println("$choose {");
		for (int i = 0; i < numChildren; i++) {
			StatementNode statement = choose.getSequenceChild(i);

			pPrintStatement(out, myIndent, statement, false, true);
			out.print("\n");
		}
		out.print(prefix);
		out.print("}");
	}

	private static StringBuffer chooseStatement2Pretty(String prefix,
			ChooseStatementNode choose, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		int numChildren = choose.numChildren();
		String myIndent = prefix + indention;

		result.append(prefix);
		result.append("$choose{\n");

		for (int i = 0; i < numChildren; i++) {
			StatementNode statement = choose.getSequenceChild(i);

			result.append(statement2Pretty(myIndent, statement, false, true,
					vacantLength(maxLength, result)));
			result.append("\n");
		}
		result.append(prefix);
		result.append("}");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintOmpStatement(PrintStream out, String prefix,
			OmpExecutableNode ompStmt) {
		OmpExecutableKind kind = ompStmt.ompExecutableKind();
		SequenceNode<IdentifierExpressionNode> privateList = ompStmt
				.privateList(), firstPrivateList = ompStmt.firstprivateList(),
				sharedList = ompStmt.sharedList(),
				copyinList = ompStmt.copyinList(),
				copyPrivateList = ompStmt.copyprivateList(),
				lastPrivateList = ompStmt.lastprivateList();
		SequenceNode<OmpReductionNode> reductionList = ompStmt.reductionList();
		boolean nowait = ompStmt.nowait();
		// String myIndent = prefix + indention;
		StatementNode block = ompStmt.statementNode();

		out.print(prefix);
		out.print("#pragma omp ");
		switch (kind) {
			case PARALLEL :
				out.print(ompParallel2Pretty(prefix, (OmpParallelNode) ompStmt,
						-1));
				break;
			case SYNCHRONIZATION :
				out.print(ompSync2Pretty(prefix, (OmpSyncNode) ompStmt, -1));
				break;
			case SIMD :
				out.print(ompSimd2Pretty(prefix, (OmpSimdNode) ompStmt, -1));
				break;
			default : // case WORKSHARING:
				out.print(ompWorksharing2Pretty(prefix,
						(OmpWorksharingNode) ompStmt, -1));
				break;
		}
		if (nowait)
			out.print("nowait");
		if (privateList != null) {
			out.print("private(");
			out.print(sequenceExpression2Pretty(privateList, -1));
			out.print(") ");
		}
		if (firstPrivateList != null) {
			out.print("firstprivate(");
			out.print(sequenceExpression2Pretty(firstPrivateList, -1));
			out.print(") ");
		}
		if (sharedList != null) {
			out.print("shared(");
			out.print(sequenceExpression2Pretty(sharedList, -1));
			out.print(") ");
		}
		if (copyinList != null) {
			out.print("copyin(");
			out.print(sequenceExpression2Pretty(copyinList, -1));
			out.print(") ");
		}
		if (copyPrivateList != null) {
			out.print("copyprivate(");
			out.print(sequenceExpression2Pretty(copyPrivateList, -1));
			out.print(") ");
		}
		if (lastPrivateList != null) {
			out.print("lastprivate(");
			out.print(sequenceExpression2Pretty(lastPrivateList, -1));
			out.print(") ");
		}
		if (reductionList != null) {
			out.print(sequenceReduction2Pretty(reductionList, -1));
		}

		if (block != null) {
			out.print("\n");
			pPrintStatement(out, prefix, block, false, false);
		}
	}

	private static StringBuffer ompStatement2Pretty(String prefix,
			OmpExecutableNode ompStmt, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		OmpExecutableKind kind = ompStmt.ompExecutableKind();
		SequenceNode<IdentifierExpressionNode> privateList = ompStmt
				.privateList(), firstPrivateList = ompStmt.firstprivateList(),
				sharedList = ompStmt.sharedList(),
				copyinList = ompStmt.copyinList(),
				copyPrivateList = ompStmt.copyprivateList(),
				lastPrivateList = ompStmt.lastprivateList();
		SequenceNode<OmpReductionNode> reductionList = ompStmt.reductionList();
		boolean nowait = ompStmt.nowait();
		// String myIndent = prefix + indention;
		StatementNode block = ompStmt.statementNode();

		result.append(prefix);
		result.append("#pragma omp ");
		switch (kind) {
			case PARALLEL :
				result.append(
						ompParallel2Pretty(prefix, (OmpParallelNode) ompStmt,
								vacantLength(maxLength, result)));
				break;
			case SYNCHRONIZATION :
				result.append(ompSync2Pretty(prefix, (OmpSyncNode) ompStmt,
						vacantLength(maxLength, result)));
				break;
			case SIMD :
				result.append(
						ompSimd2Pretty(prefix, (OmpSimdNode) ompStmt, -1));
				break;
			default : // case WORKSHARING:
				result.append(ompWorksharing2Pretty(prefix,
						(OmpWorksharingNode) ompStmt,
						vacantLength(maxLength, result)));
				break;
		}
		if (nowait)
			result.append("nowait");
		if (privateList != null) {
			result.append("private(");
			result.append(sequenceExpression2Pretty(privateList,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (firstPrivateList != null) {
			result.append("firstprivate(");
			result.append(sequenceExpression2Pretty(firstPrivateList,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (sharedList != null) {
			result.append("shared(");
			result.append(sequenceExpression2Pretty(sharedList,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (copyinList != null) {
			result.append("copyin(");
			result.append(sequenceExpression2Pretty(copyinList,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (copyPrivateList != null) {
			result.append("copyprivate(");
			result.append(sequenceExpression2Pretty(copyPrivateList,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (lastPrivateList != null) {
			result.append("lastprivate(");
			result.append(sequenceExpression2Pretty(lastPrivateList,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (reductionList != null) {
			result.append(sequenceReduction2Pretty(reductionList,
					vacantLength(maxLength, result)));
		}
		if (block != null) {
			result.append("\n");
			result.append(statement2Pretty(prefix, block, false, false,
					vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer ompWorksharing2Pretty(String prefix,
			OmpWorksharingNode ompWs, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		OmpWorksharingNodeKind kind = ompWs.ompWorkshareNodeKind();

		switch (kind) {
			case FOR : {
				OmpForNode forNode = (OmpForNode) ompWs;
				int collapse = forNode.collapse();
				OmpScheduleKind schedule = forNode.schedule();

				result.append("for ");
				if (schedule != OmpScheduleKind.NONE) {
					result.append("schedule(");
					switch (forNode.schedule()) {
						case AUTO :
							result.append("auto");
							break;
						case DYNAMIC :
							result.append("dynamic");
							break;
						case GUIDED :
							result.append("guided");
							break;
						case RUNTIME :
							result.append("runtime");
							break;
						default :// STATIC
							result.append("static");
							break;
					}
					if (forNode.chunkSize() != null) {
						result.append(", ");
						result.append(expression2Pretty(forNode.chunkSize(),
								vacantLength(maxLength, result)));
					}
					result.append(") ");
				}
				if (collapse > 1) {
					result.append("collapse(");
					result.append(collapse);
					result.append(") ");
				}
				if (forNode.isOrdered()) {
					result.append("ordered ");
					if (forNode.ordered() > 1)
						result.append("(" + forNode.ordered() + ") ");
				}
				break;
			}
			case SECTIONS :
				result.append("sections ");
				break;
			case SINGLE :
				result.append("single ");
				break;
			default : // case SECTION:
				result.append("section ");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer ompSync2Pretty(String prefix,
			OmpSyncNode ompSync, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		OmpSyncNodeKind kind = ompSync.ompSyncNodeKind();

		switch (kind) {
			case MASTER :
				result.append("master ");
				break;
			case CRITICAL :
				result.append("critical");
				if (ompSync.criticalName() != null) {
					result.append("(");
					result.append(ompSync.criticalName().name());
					result.append(")");
				}
				result.append(" ");
				break;
			case BARRIER :
				result.append("barrier ");
				break;
			case FLUSH :
				result.append("flush ");
				if (ompSync.flushedList() != null) {
					result.append("(");
					result.append(
							sequenceExpression2Pretty(ompSync.flushedList(),
									vacantLength(maxLength, result)));
					result.append(")");
				}
				break;
			case OMPATOMIC :
				result.append("atomic ");
				break;
			default :// ORDERED
				result.append("ordered ");
		}
		return trimStringBuffer(result, maxLength);
	}

	/**
	 * <p>
	 * print: <code>simd safelen(c) simdlen(c)</code> where safelen and simdlen
	 * maybe absent
	 * </p>
	 *
	 * TODO: cannot understand the arguments: "prefix" and "maxLength". This
	 * method is created by mimic-ing ompParallel2Pretty where the use of
	 * "prefix" and "maxLength" is confusing. Need better doc.
	 */
	private static StringBuffer ompSimd2Pretty(String prefix,
			OmpSimdNode ompSimd, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append("simd ");
		if (ompSimd.safeLen() != null)
			result.append("safelen(" + expression2Pretty(ompSimd.safeLen(),
					vacantLength(maxLength, result)) + ") ");
		if (ompSimd.simdLen() != null)
			result.append("simdlen(" + expression2Pretty(ompSimd.simdLen(),
					vacantLength(maxLength, result)) + ") ");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer ompParallel2Pretty(String prefix,
			OmpParallelNode para, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		ExpressionNode ifClause = para.ifClause(),
				numThreads = para.numThreads();
		boolean isDefaultShared = para.isDefaultShared();

		result.append("parallel ");
		if (ifClause != null) {
			result.append("if(");
			result.append(expression2Pretty(ifClause,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (numThreads != null) {
			result.append("num_threads(");
			result.append(expression2Pretty(numThreads,
					vacantLength(maxLength, result)));
			result.append(") ");
		}
		if (isDefaultShared)
			result.append("default(shared) ");
		else
			result.append("default(none) ");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer sequenceReduction2Pretty(
			SequenceNode<OmpReductionNode> sequence, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		int num = sequence.numChildren();

		for (int i = 0; i < num; i++) {
			OmpReductionNode reduction = sequence.getSequenceChild(i);

			result.append(ompReduction2Pretty(reduction,
					vacantLength(maxLength, result)));
			if (i < num - 1)
				result.append(" ");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer ompReduction2Pretty(OmpReductionNode reduction,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append("reduction(");
		switch (reduction.ompReductionOperatorNodeKind()) {
			case FUNCTION : {
				OmpFunctionReductionNode funcNode = (OmpFunctionReductionNode) reduction;

				result.append(
						expression2Pretty(funcNode.function(), maxLength));
				break;
			}
			default : // operator
			{
				OmpSymbolReductionNode symbol = (OmpSymbolReductionNode) reduction;

				switch (symbol.operator()) {
					case SUM :
						result.append("+");
						break;
					case MINUS :
						result.append("-");
						break;
					case PROD :
						result.append("*");
						break;
					case BAND :
						result.append("&");
						break;
					case BOR :
						result.append("|");
						break;
					case BXOR :
						result.append("^");
						break;
					case LAND :
						result.append("&&");
						break;
					case LOR :
						result.append("||");
						break;
					case EQV :
						result.append("==");
						break;
					case NEQ :
						result.append("!=");
						break;
					default :
						throw new ABCRuntimeException(
								"Invalid operator for OpenMP reduction: "
										+ symbol.operator(),
								reduction.getSource().getLocation(false));
				}
			}
		}
		result.append(": ");
		result.append(sequenceExpression2Pretty(reduction.variables(),
				vacantLength(maxLength, result)));
		result.append(")");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer sequenceExpression2Pretty(
			SequenceNode<? extends ExpressionNode> sequence, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		int numExpressions = sequence.numChildren();

		for (int i = 0; i < numExpressions; i++) {
			ExpressionNode expression = sequence.getSequenceChild(i);

			if (i != 0)
				result.append(", ");
			result.append(expression2Pretty(expression,
					vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintCivlForStatement(PrintStream out, String prefix,
			CivlForNode civlFor) {
		DeclarationListNode vars = civlFor.getVariables();
		int numVars = vars.numChildren();
		StatementNode body = civlFor.getBody();

		out.print(prefix);
		if (civlFor.isParallel())
			out.print("$parfor");
		else
			out.print("$for");
		out.print(" (int ");
		for (int i = 0; i < numVars; i++) {
			if (i != 0)
				out.print(", ");
			out.print(vars.getSequenceChild(i).getName());
		}
		out.print(": ");
		out.print(expression2Pretty(civlFor.getDomain(), -1));
		out.print(")");
		if (body.statementKind() == StatementKind.COMPOUND)
			out.print(" ");
		else
			out.println();
		pPrintStatement(out, prefix + indention, body, true, false);
	}

	private static StringBuffer civlForStatement2Pretty(String prefix,
			CivlForNode civlFor, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		DeclarationListNode vars = civlFor.getVariables();
		int numVars = vars.numChildren();
		StatementNode body = civlFor.getBody();

		result.append(prefix);
		if (civlFor.isParallel())
			result.append("$parfor");
		else
			result.append("$for");
		result.append(" (int ");
		for (int i = 0; i < numVars; i++) {
			if (i != 0)
				result.append(", ");
			result.append(vars.getSequenceChild(i).getName());
		}
		result.append(": ");
		result.append(expression2Pretty(civlFor.getDomain(),
				vacantLength(maxLength, result)));
		result.append(")");
		if (body.statementKind() == StatementKind.COMPOUND)
			result.append(" ");
		else
			result.append("\n");
		result.append(statement2Pretty(prefix + indention, civlFor.getBody(),
				true, false, vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintLoop(PrintStream out, String prefix,
			LoopNode loop) {
		LoopKind loopKind = loop.getKind();
		StringBuffer condition = new StringBuffer();
		String myIndent = prefix + indention;
		StatementNode bodyNode = loop.getBody();
		SequenceNode<ContractNode> contracts = loop.loopContracts();

		if (contracts != null)
			pPrintContracts(out, prefix, contracts);
		if (loop.getCondition() != null)
			condition = expression2Pretty(loop.getCondition(), -1);
		switch (loopKind) {
			case WHILE :
				out.print(prefix);
				out.print("while (");
				out.print(condition);
				out.print(")");
				if (bodyNode == null)
					out.print(";");
				else {
					if (bodyNode.statementKind() == StatementKind.COMPOUND)
						out.print(" ");
					else
						out.println();
					pPrintStatement(out, myIndent, bodyNode, true, false);

				}
				break;
			case DO_WHILE :
				out.print(prefix);
				out.print("do");
				if (bodyNode == null)
					out.print(";");
				else {
					if (bodyNode.statementKind() == StatementKind.COMPOUND)
						out.print(" ");
					else
						out.println();
					pPrintStatement(out, myIndent, bodyNode, true, false);
				}
				if (bodyNode != null
						&& !(bodyNode instanceof CompoundStatementNode)) {
					out.print("\n");
					out.print(prefix);
				}
				out.print("while (");
				out.print(condition);
				out.print(");");
				break;
			default : // case FOR:
				pPrintFor(out, prefix, (ForLoopNode) loop);
		}
	}

	private static StringBuffer loop2Pretty(String prefix, LoopNode loop,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		LoopKind loopKind = loop.getKind();
		StringBuffer condition = new StringBuffer();
		String myIndent = prefix + indention;
		StatementNode bodyNode = loop.getBody();
		SequenceNode<ContractNode> contracts = loop.loopContracts();

		if (contracts != null)
			result.append(contracts2Pretty(prefix, contracts, maxLength));
		if (loop.getCondition() != null)
			condition = expression2Pretty(loop.getCondition(),
					vacantLength(maxLength, result));
		switch (loopKind) {
			case WHILE :
				result.append(prefix);
				result.append("while (");
				result.append(condition);
				result.append(")");
				if (bodyNode == null)
					result.append(";");
				else {
					result.append("\n");
					result.append(statement2Pretty(myIndent, bodyNode, true,
							false, vacantLength(maxLength, result)));
				}
				break;
			case DO_WHILE :
				result.append(prefix);
				result.append("do");
				if (bodyNode == null)
					result.append(";");
				else {
					result.append("\n");
					result.append(statement2Pretty(myIndent, bodyNode, true,
							false, vacantLength(maxLength, result)));
				}
				if (bodyNode != null
						&& !(bodyNode instanceof CompoundStatementNode)) {
					result.append("\n");
					result.append(prefix);
				}
				result.append("while (");
				result.append(condition);
				result.append(");");
				break;
			default : // case FOR:
				result.append(for2Pretty(prefix, (ForLoopNode) loop,
						vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintAtomic(PrintStream out, String prefix,
			AtomicNode atomicNode) {
		StatementNode body = atomicNode.getBody();

		out.print(prefix);
		out.print("$atomic");
		if (body.statementKind() == StatementKind.COMPOUND)
			out.print(" ");
		else
			out.println();
		pPrintStatement(out, prefix + indention, body, true, false);
	}

	private static StringBuffer atomic2Pretty(String prefix,
			AtomicNode atomicNode, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		result.append(prefix);
		result.append("$atomic\n");
		result.append(statement2Pretty(prefix + indention, atomicNode.getBody(),
				true, false, vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);

	}

	private static StringBuffer goto2Pretty(String prefix, GotoNode go2,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		result.append(prefix);
		result.append("goto ");
		result.append(go2.getLabel().name());
		result.append(";");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintLabeled(PrintStream out, String prefix,
			LabeledStatementNode labeled) {
		LabelNode label = labeled.getLabel();
		StatementNode statement = labeled.getStatement();
		String myIndent = prefix + indention;

		out.print(prefix);
		out.print(labelNode2Pretty(label, -1));
		out.println("");
		pPrintStatement(out, myIndent, statement, true, false);
	}

	private static StringBuffer labeled2Pretty(String prefix,
			LabeledStatementNode labeled, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		LabelNode label = labeled.getLabel();
		StatementNode statement = labeled.getStatement();
		String myIndent = prefix + indention;

		result.append(prefix);
		result.append(labelNode2Pretty(label, maxLength));
		result.append("\n");
		result.append(statement2Pretty(myIndent, statement, true, false,
				vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer labelNode2Pretty(LabelNode label,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		if (label instanceof OrdinaryLabelNode) {
			OrdinaryLabelNode ordinary = (OrdinaryLabelNode) label;
			result.append(ordinary.getName());
			result.append(":");
		} else {
			// switch label
			SwitchLabelNode switchLabel = (SwitchLabelNode) label;
			boolean isDefault = switchLabel.isDefault();

			if (isDefault)
				result.append("default:");
			else {
				result.append("case ");
				result.append(expression2Pretty(switchLabel.getExpression(),
						vacantLength(maxLength, result)));
				result.append(":");
			}
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintSwitch(PrintStream out, String prefix,
			SwitchNode switchNode) {
		out.print(prefix);
		out.print("switch (");
		out.print(expression2Pretty(switchNode.getCondition(), -1));
		out.print(") ");
		pPrintStatement(out, prefix + indention, switchNode.getBody(), true,
				true);
	}

	private static StringBuffer switch2Pretty(String prefix,
			SwitchNode switchNode, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append(prefix);
		result.append("switch (");
		result.append(expression2Pretty(switchNode.getCondition(),
				vacantLength(maxLength, result)));
		result.append(")\n");
		result.append(statement2Pretty(prefix + indention, switchNode.getBody(),
				true, true, vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintJump(PrintStream out, String prefix,
			JumpNode jump) {
		JumpKind kind = jump.getKind();

		switch (kind) {
			case GOTO :
				out.print(goto2Pretty(prefix, (GotoNode) jump, -1));
				break;
			case CONTINUE :
				out.print(prefix);
				out.print("continue;");
				break;
			case BREAK :
				out.print(prefix);
				out.print("break;");
				break;
			default : // case RETURN:
				pPrintReturn(out, prefix, (ReturnNode) jump);
		}
	}

	private static StringBuffer jump2Pretty(String prefix, JumpNode jump,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		JumpKind kind = jump.getKind();

		switch (kind) {
			case GOTO :
				return goto2Pretty(prefix, (GotoNode) jump, maxLength);
			case CONTINUE :
				result.append(prefix);
				result.append("continue;");
				break;
			case BREAK :
				result.append(prefix);
				result.append("break;");
				break;
			default : // case RETURN:
				return return2Pretty(prefix, (ReturnNode) jump, maxLength);
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintUpdate(PrintStream out, String prefix,
			UpdateNode update) {
		ExpressionNode collator = update.getCollator();
		FunctionCallNode call = update.getFunctionCall();

		out.print(prefix);
		out.print("$update (");
		out.print(expression2Pretty(collator, -1));
		out.print(") ");
		out.print(expression2Pretty(call, -1));
		out.print(";");
	}

	private static void pPrintReturn(PrintStream out, String prefix,
			ReturnNode returnNode) {
		ExpressionNode expr = returnNode.getExpression();

		out.print(prefix);
		out.print("return");
		if (expr != null) {
			out.print(" ");
			out.print(expression2Pretty(expr, -1));
		}
		out.print(";");
	}

	private static StringBuffer return2Pretty(String prefix,
			ReturnNode returnNode, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		ExpressionNode expr = returnNode.getExpression();

		result.append(prefix);
		result.append("return");
		if (expr != null) {
			result.append(" ");
			result.append(
					expression2Pretty(expr, vacantLength(maxLength, result)));
		}
		result.append(";");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintIf(PrintStream out, String prefix,
			IfNode ifNode) {
		ExpressionNode condition = ifNode.getCondition();
		StatementNode trueBranch = ifNode.getTrueBranch();
		StatementNode falseBranch = ifNode.getFalseBranch();
		String myIndent = prefix + indention;

		out.print(prefix);
		out.print("if (");
		if (condition != null)
			out.print(expression2Pretty(condition, -1));
		out.print(")");
		if (trueBranch == null)
			out.print(";");
		else {
			if (trueBranch.statementKind() == StatementKind.COMPOUND)
				out.print(" ");
			else
				out.println();
			pPrintStatement(out, myIndent, trueBranch, true, false);
		}
		if (falseBranch != null) {
			out.print("\n");
			out.print(prefix);
			out.print("else");
			if (falseBranch.statementKind() == StatementKind.COMPOUND)
				out.print(" ");
			else
				out.println();
			pPrintStatement(out, myIndent, falseBranch, true, false);
		}
	}

	private static StringBuffer if2Pretty(String prefix, IfNode ifNode,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		ExpressionNode condition = ifNode.getCondition();
		StatementNode trueBranch = ifNode.getTrueBranch();
		StatementNode falseBranch = ifNode.getFalseBranch();
		String myIndent = prefix + indention;

		result.append(prefix);
		result.append("if (");
		if (condition != null)
			result.append(expression2Pretty(condition,
					vacantLength(maxLength, result)));
		result.append(")");

		if (trueBranch == null)
			result.append(";");
		else {
			if (trueBranch.statementKind() == StatementKind.COMPOUND)
				result.append(" ");
			else
				result.append("\n");
			result.append(statement2Pretty(myIndent, trueBranch, true, false,
					vacantLength(maxLength, result)));
		}
		if (falseBranch != null) {
			result.append("\n");
			result.append(prefix);
			result.append("else");
			if (falseBranch.statementKind() == StatementKind.COMPOUND)
				result.append(" ");
			else
				result.append("\n");
			result.append(statement2Pretty(myIndent, falseBranch, true, false,
					vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintFor(PrintStream out, String prefix,
			ForLoopNode loop) {
		ForLoopInitializerNode init = loop.getInitializer();
		ExpressionNode condition = loop.getCondition();
		ExpressionNode incrementer = loop.getIncrementer();
		StatementNode body = loop.getBody();
		String myIndent = prefix + indention;
		SequenceNode<ContractNode> contracts = loop.loopContracts();

		if (contracts != null)
			pPrintContracts(out, prefix, contracts);
		out.print(prefix);
		out.print("for (");
		if (init != null) {
			if (init instanceof ExpressionNode)
				out.print(expression2Pretty((ExpressionNode) init, -1));
			else if (init instanceof DeclarationListNode)
				out.print(
						declarationList2Pretty((DeclarationListNode) init, -1));
		}
		out.print("; ");
		if (condition != null) {
			out.print(expression2Pretty(condition, -1));
		}
		out.print("; ");
		if (incrementer != null) {
			out.print(expression2Pretty(incrementer, -1));
		}
		out.print(")");
		if (body == null)
			out.print(";");
		else {
			if (body.statementKind() == StatementKind.COMPOUND)
				out.print(" ");
			else
				out.println();
			pPrintStatement(out, myIndent, body, true, false);
		}
	}

	private static StringBuffer for2Pretty(String prefix, ForLoopNode loop,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		ForLoopInitializerNode init = loop.getInitializer();
		ExpressionNode condition = loop.getCondition();
		ExpressionNode incrementer = loop.getIncrementer();
		StatementNode body = loop.getBody();
		String myIndent = prefix + indention;
		SequenceNode<ContractNode> contracts = loop.loopContracts();

		if (contracts != null)
			result.append(contracts2Pretty(prefix, contracts, maxLength));
		result.append(prefix);
		result.append("for (");
		if (init != null) {
			if (init instanceof ExpressionNode)
				result.append(expression2Pretty((ExpressionNode) init,
						vacantLength(maxLength, result)));
			else if (init instanceof DeclarationListNode)
				result.append(declarationList2Pretty((DeclarationListNode) init,
						vacantLength(maxLength, result)));
		}
		result.append("; ");
		if (condition != null) {
			result.append(expression2Pretty(condition,
					vacantLength(maxLength, result)));
		}
		result.append("; ");
		if (incrementer != null) {
			result.append(expression2Pretty(incrementer,
					vacantLength(maxLength, result)));
		}
		result.append(")");
		if (body == null)
			result.append(";");
		else {
			result.append("\n");
			result.append(statement2Pretty(myIndent, body, true, false,
					vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer declarationList2Pretty(DeclarationListNode list,
			int maxLength) {
		int num = list.numChildren();
		StringBuffer result = new StringBuffer();

		for (int i = 0; i < num; i++) {
			VariableDeclarationNode var = list.getSequenceChild(i);

			if (var == null)
				continue;
			if (i != 0)
				result.append(", ");
			result.append(variableDeclaration2Pretty("", var,
					maxLength - result.length()));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintExpressionStatement(PrintStream out,
			String prefix, ExpressionStatementNode expr) {
		out.print(prefix);
		out.print(expression2Pretty(expr.getExpression(), -1));
		out.print(";");
	}

	private static StringBuffer expressionStatement2Pretty(String prefix,
			ExpressionStatementNode expr, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		result.append(prefix);
		result.append(expression2Pretty(expr.getExpression(),
				vacantLength(maxLength, result)));
		result.append(";");
		return trimStringBuffer(result, maxLength);
	}

	private static void pPrintWhen(PrintStream out, String prefix,
			WhenNode when) {
		String myIndent = prefix + indention;
		StatementNode body = when.getBody();

		out.print(prefix);
		out.print("$when (");
		out.print(expression2Pretty(when.getGuard(), -1));
		out.print(")");
		if (body.statementKind() == StatementKind.COMPOUND)
			out.print(" ");
		else
			out.println();
		pPrintStatement(out, myIndent, body, true, false);
	}

	private static StringBuffer when2Pretty(String prefix, WhenNode when,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		String myIndent = prefix + indention;

		result.append(prefix);
		result.append("$when (");
		result.append(expression2Pretty(when.getGuard(),
				vacantLength(maxLength, result)));
		result.append(")\n");
		result.append(statement2Pretty(myIndent, when.getBody(), true, false,
				vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);

	}

	private static StringBuffer update2Pretty(String prefix, UpdateNode update,
			int maxLength) {
		ExpressionNode call;
		StringBuffer result = new StringBuffer();

		result.append(prefix);
		result.append("$update(");
		result.append(expression2Pretty(update.getCollator(),
				vacantLength(maxLength, result)));
		result.append(") ");
		call = update.getFunctionCall();
		result.append(expression2Pretty(call, vacantLength(maxLength, result)));
		return result;
	}

	/**
	 * Pretty printing for {@link WithNode}
	 *
	 * @param prefix
	 *            Anything will be printed before the content of the withNode
	 *            (e.g. white spaces)
	 * @param withNode
	 *            The {@link WithNode} that will be printed
	 * @param maxLength
	 *            The maximum length of a printed line
	 */
	private static StringBuffer with2Pretty(String prefix, WithNode withNode,
			int maxLength) {
		StatementNode stmt;
		String myIndent = prefix + indention;
		StringBuffer result = new StringBuffer();

		result.append(prefix);
		result.append("$with(");
		result.append(expression2Pretty(withNode.getStateReference(),
				vacantLength(maxLength, result)));
		result.append(") ");
		stmt = withNode.getBodyNode();
		result.append("\n");
		result.append(statement2Pretty(myIndent, stmt, true, false,
				vacantLength(maxLength, result)));
		return result;
	}

	/**
	 * Pretty printing for {@link RunNode}
	 *
	 * @param prefix
	 *            Anything will be printed before the content of the runNode
	 *            (e.g. white spaces)
	 * @param runNode
	 *            The {@link RunNode} that will be printed
	 * @param maxLength
	 *            The maximum length of a printed line
	 * @return
	 */
	private static StringBuffer run2Pretty(String prefix, RunNode runNode,
			int maxLength) {
		String myIndent = prefix + indention;
		StringBuffer result = new StringBuffer();

		result.append(prefix);
		result.append("$run");
		result.append(statement2Pretty(myIndent, runNode.getStatement(), true,
				false, vacantLength(maxLength, result)));
		return result;
	}

	static private StringBuffer variableDeclaration2Pretty(String prefix,
			VariableDeclarationNode variable, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		InitializerNode init = variable.getInitializer();
		TypeNode typeNode = variable.getTypeNode();
		String type;
		String varName = variable.getName();

		result.append(prefix);
		if (typeNode.isInputQualified())
			result.append("$input ");
		if (typeNode.isOutputQualified())
			result.append("$output ");
		if (typeNode.isAtomicQualified())
			result.append("_Atomic ");
		if (typeNode.isConstQualified())
			result.append("const ");
		if (typeNode.isVolatileQualified())
			result.append("volatile ");
		if (variable.hasExternStorage())
			result.append("extern ");
		if (variable.hasAutoStorage())
			result.append("auto ");
		if (variable.hasRegisterStorage())
			result.append("register ");
		if (variable.hasStaticStorage())
			result.append("static ");
		if (variable.hasThreadLocalStorage())
			result.append("_Thread_local ");
		if (variable.hasSharedStorage())
			result.append("__shared__ ");
		type = type2Pretty("", typeNode, false, vacantLength(maxLength, result))
				.toString();
		if (type.endsWith("]")) {
			Pair<String, String> typeResult = processArrayType(type);

			result.append(typeResult.left);
			result.append(" ");
			if (typeNode.isRestrictQualified())
				result.append("restrict ");
			if (varName != null) {
				result.append(" ");
				result.append(varName);
			}
			result.append(typeResult.right);
		} else {
			result.append(type);
			if (typeNode.isRestrictQualified())
				result.append(" restrict");
			if (varName != null) {
				result.append(" ");
				result.append(varName);
			}
		}
		if (init != null) {
			result.append(" = ");
			result.append(
					initializer2Pretty(init, vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	// TODO need to be improved for more complicated types
	// currently works for multiple dimension arrays
	// e.g. translating (int [20])[10] into int [10][20]
	private static Pair<String, String> processArrayType(String type) {
		int start = type.indexOf('[');
		String typeSuffix = type.substring(start);
		int length = typeSuffix.length();
		Stack<String> extents = new Stack<>();
		String newSuffix = "";
		String extent = "";

		for (int i = 0; i < length; i++) {
			char current = typeSuffix.charAt(i);

			extent += current;
			if (current == ']') {
				extents.push(extent);
				extent = "";
			}
		}
		while (!extents.empty()) {
			newSuffix += extents.pop();
		}
		return new Pair<>(type.substring(0, start), newSuffix);
	}

	private static StringBuffer initializer2Pretty(InitializerNode init,
			int maxLength) {
		if (init instanceof CompoundInitializerNode) {
			return compoundInitializer2Pretty((CompoundInitializerNode) init,
					maxLength);
		} else if (init instanceof ExpressionNode)
			return expression2Pretty((ExpressionNode) init, maxLength);
		else
			throw new ABCRuntimeException(
					"Invalid initializer: " + init.toString());
	}

	private static StringBuffer compoundInitializer2Pretty(
			CompoundInitializerNode compound, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		int numPairs = compound.numChildren();

		result.append("{");
		for (int i = 0; i < numPairs; i++) {
			PairNode<DesignationNode, InitializerNode> pair = compound
					.getSequenceChild(i);
			DesignationNode left = pair.getLeft();
			InitializerNode right = pair.getRight();
			int numDesig = left == null ? 0 : left.numChildren();

			if (i != 0)
				result.append(", ");
			if (numDesig > 0) {
				for (int j = 0; j < numDesig; j++) {
					result.append(designator2Pretty(left.getSequenceChild(j),
							vacantLength(maxLength, result)));
				}
				result.append("=");
			}
			result.append(
					initializer2Pretty(right, vacantLength(maxLength, result)));
		}
		result.append("}");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer designator2Pretty(DesignatorNode designator,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		if (designator instanceof ArrayDesignatorNode) {
			result.append("[");
			result.append(expression2Pretty(
					((ArrayDesignatorNode) designator).getIndex(),
					vacantLength(maxLength, result)));
			result.append("]");
		} else {// FieldDesignatorNode
			result.append(".");
			result.append(((FieldDesignatorNode) designator).getField().name());
		}
		return trimStringBuffer(result, maxLength);
	}

	// private static void pPrintAssume(PrintStream out, String prefix,
	// AssumeNode assume) {
	// out.print(prefix);
	// out.print("$assume ");
	// out.print(expression2Pretty(assume.getExpression()));
	// out.print(";");
	// }

	private static StringBuffer expression2Pretty(ExpressionNode expression,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		if (expression == null)
			return result;

		ExpressionKind kind = expression.expressionKind();

		switch (kind) {
			case ALIGNOF : {
				AlignOfNode align = (AlignOfNode) expression;

				result.append("_Alignof(");
				result.append(type2Pretty("", align.getArgument(), false,
						vacantLength(maxLength, result)));
				result.append(")");
				break;
			}
			case ARRAY_LAMBDA :
				result.append(arrayLambda2Pretty((ArrayLambdaNode) expression,
						maxLength));
				break;
			case ARROW : {
				ArrowNode arrow = (ArrowNode) expression;

				result.append("(");
				result.append(expression2Pretty(arrow.getStructurePointer(),
						vacantLength(maxLength, result)));
				result.append(")");
				result.append("->");
				result.append(arrow.getFieldName().name());
				break;
			}
			case CAST : {
				CastNode cast = (CastNode) expression;
				ExpressionNode arg = cast.getArgument();
				ExpressionKind argKind = arg.expressionKind();
				boolean parenNeeded = true;

				result.append("(");
				result.append(type2Pretty("", cast.getCastType(), false,
						vacantLength(maxLength, result)));
				result.append(")");
				if (argKind == ExpressionKind.IDENTIFIER_EXPRESSION
						|| argKind == ExpressionKind.CONSTANT
						|| argKind == ExpressionKind.COMPOUND_LITERAL)
					parenNeeded = false;
				if (parenNeeded)
					result.append("(");
				result.append(expression2Pretty(arg,
						vacantLength(maxLength, result)));
				if (parenNeeded)
					result.append(")");
				break;
			}
			case COMPOUND_LITERAL :
				return compoundLiteral2Pretty((CompoundLiteralNode) expression,
						maxLength);
			case CONSTANT : {
				String constant = ((ConstantNode) expression)
						.getStringRepresentation();

				if (constant.equals("\\false"))
					constant = "$false";
				else if (constant.equals("\\true"))
					constant = "$true";
				result.append(constant);
				break;
			}
			case CURLY_BRACED_TERM_SET :
				return expression.prettyRepresentation(maxLength);
			case DERIVATIVE_EXPRESSION :
				return derivative2Pretty((DerivativeExpressionNode) expression,
						maxLength);
			case DOT : {
				DotNode dot = (DotNode) expression;

				result.append(expression2Pretty(dot.getStructure(), maxLength));
				result.append(".");
				result.append(dot.getFieldName().name());
				break;
			}
			case FUNCTION_CALL :
				return functionCall2Pretty((FunctionCallNode) expression,
						maxLength);
			// TODO
			// case GENERIC_SELECTION:
			// break;
			case IDENTIFIER_EXPRESSION :
				result.append(((IdentifierExpressionNode) expression)
						.getIdentifier().name());
				break;
			case MPI_CONTRACT_EXPRESSION :
				result.append(mpiContractExpression2Pretty(
						(MPIContractExpressionNode) expression, maxLength));
				break;
			case OPERATOR :
				return operator2Pretty((OperatorNode) expression, maxLength);
			case QUANTIFIED_EXPRESSION :
				return quantifiedExpression2Pretty(
						(QuantifiedExpressionNode) expression, maxLength);
			case REGULAR_RANGE :
				return regularRange2Pretty((RegularRangeNode) expression,
						maxLength);
			// TODO
			// case REMOTE_REFERENCE:
			// break;
			case SCOPEOF :
				result.append("$scopeof(");
				result.append(expression2Pretty(
						((ScopeOfNode) expression).expression(),
						vacantLength(maxLength, result)));
				result.append(")");
				break;
			case SIZEOF :
				result.append("sizeof(");
				result.append(
						sizeable2Pretty(((SizeofNode) expression).getArgument(),
								vacantLength(maxLength, result)));
				result.append(")");
				break;
			case SPAWN :
				result.append("$spawn ");
				result.append(
						functionCall2Pretty(((SpawnNode) expression).getCall(),
								vacantLength(maxLength, result)));
				break;
			case RESULT :
				result.append("\\result");
				break;
			case SEPARATED:
				result.append(expression.prettyRepresentation(maxLength));
				break;
			case STATEMENT_EXPRESSION :
				return statementExpression2Pretty(
						(StatementExpressionNode) expression, maxLength);
			case NOTHING :
				result.append("\\nothing");
				break;
			case WILDCARD :
				result.append("...");
				break;
			case OBJECT_OR_REGION_OF : {
				ObjectOrRegionOfNode objectRegion = (ObjectOrRegionOfNode) expression;

				if (objectRegion.isObjectOf())
					result.append("$object_of");
				else
					result.append("$region_of");
				result.append("(");
				result.append(expression2Pretty(objectRegion.operand(),
						vacantLength(maxLength, result)));
				result.append(")");
				break;
			}
			case EXTENDED_QUANTIFIED :
				result.append(extendedQuantifiedExpression2Pretty(
						(ExtendedQuantifiedExpressionNode) expression,
						maxLength));
				break;
			case LAMBDA :
				result.append(
						lambda2Pretty((LambdaNode) expression, maxLength));
				break;
			case VALUE_AT :
				result.append(
						valueAt2Pretty((ValueAtNode) expression, maxLength));
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty print of expression node of " + kind + " kind");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer valueAt2Pretty(ValueAtNode valueAt,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append("$value_at");
		result.append(" (");
		result.append(expression2Pretty(valueAt.stateNode(),
				vacantLength(maxLength, result)));
		result.append(", ");
		result.append(expression2Pretty(valueAt.pidNode(),
				vacantLength(maxLength, result)));
		result.append(", ");
		result.append(expression2Pretty(valueAt.expressionNode(),
				vacantLength(maxLength, result)));
		result.append(")");
		return result;
	}

	private static StringBuffer lambda2Pretty(LambdaNode lambda,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append("$lambda");
		result.append(" (");
		result.append(variableDeclaration2Pretty("", lambda.freeVariable(),
				vacantLength(maxLength, result)));
		result.append(") ");
		result.append(expression2Pretty(lambda.lambdaFunction(),
				vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer extendedQuantifiedExpression2Pretty(
			ExtendedQuantifiedExpressionNode extQuantified, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append(extQuantified.extQuantifier());
		result.append("(");
		result.append(expression2Pretty(extQuantified.lower(),
				vacantLength(maxLength, result)));
		result.append(", ");
		result.append(expression2Pretty(extQuantified.higher(),
				vacantLength(maxLength, result)));
		result.append(", ");
		result.append(expression2Pretty(extQuantified.function(),
				vacantLength(maxLength, result)));
		result.append(")");
		return result;
	}

	private static StringBuffer statementExpression2Pretty(
			StatementExpressionNode statementExpression, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		CompoundStatementNode compound = statementExpression
				.getCompoundStatement();

		result.append("(");
		result.append(compoundStatement2Pretty("", compound, false, false,
				maxLength));
		result.append(")");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer derivative2Pretty(
			DerivativeExpressionNode deriv, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		int numPartials = deriv.getNumberOfPartials();
		int numArgs = deriv.getNumberOfArguments();

		result.append("$D[");
		result.append(expression2Pretty(deriv.getFunction(),
				vacantLength(maxLength, result)));
		for (int i = 0; i < numPartials; i++) {
			PairNode<IdentifierExpressionNode, IntegerConstantNode> partial = deriv
					.getPartial(i);

			result.append(", {");
			result.append(partial.getLeft().getIdentifier().name());
			result.append(",");
			result.append(partial.getRight().getConstantValue());
			result.append("}");
		}
		result.append("](");
		for (int i = 0; i < numArgs; i++) {
			ExpressionNode arg = deriv.getArgument(i);

			if (i != 0)
				result.append(", ");
			result.append(
					expression2Pretty(arg, vacantLength(maxLength, result)));
		}
		result.append(")");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer quantifiedExpression2Pretty(
			QuantifiedExpressionNode quantified, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		String quantifier;

		switch (quantified.quantifier()) {
			case FORALL :
				quantifier = "$forall";
				break;
			case EXISTS :
				quantifier = "$exists";
				break;
			default :// UNIFORM
				quantifier = "$uniform";
		}
		result.append(quantifier);
		result.append(" (");
		result.append(boundVariableList2Pretty(quantified.boundVariableList(),
				vacantLength(maxLength, result)));
		if (quantified.restriction() != null) {
			result.append(" | ");
			result.append(expression2Pretty(quantified.restriction(),
					vacantLength(maxLength, result)));
		}
		result.append(") ");
		result.append(expression2Pretty(quantified.expression(),
				vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer arrayLambda2Pretty(ArrayLambdaNode quantified,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();

		result.append("(");
		result.append("(");
		result.append(type2Pretty("", quantified.type(), false,
				vacantLength(maxLength, result)));
		result.append(") ");
		result.append("$lambda");
		result.append(" (");
		result.append(boundVariableList2Pretty(quantified.boundVariableList(),
				vacantLength(maxLength, result)));
		if (quantified.restriction() != null) {
			result.append(" | ");
			result.append(expression2Pretty(quantified.restriction(),
					vacantLength(maxLength, result)));
		}
		result.append(") ");
		result.append(expression2Pretty(quantified.expression(),
				vacantLength(maxLength, result)));
		result.append(")");
		return trimStringBuffer(result, maxLength);
	}

	private static int vacantLength(int total, StringBuffer buf) {
		if (total == -1)
			return -1;

		int result = total - buf.length();

		return result > 0 ? result : 0;
	}

	private static StringBuffer boundVariableList2Pretty(
			SequenceNode<PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode>> boundVariableList,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		boolean isFirstBoundVarSubList = true;

		for (PairNode<SequenceNode<VariableDeclarationNode>, ExpressionNode> boundVariableSubList : boundVariableList) {
			boolean isFirstVariable = true;

			if (isFirstBoundVarSubList)
				isFirstBoundVarSubList = false;
			else
				result.append("; ");
			for (VariableDeclarationNode variable : boundVariableSubList
					.getLeft()) {
				if (isFirstVariable) {
					result.append(variableDeclaration2Pretty("", variable,
							vacantLength(maxLength, result)));
					isFirstVariable = false;
				} else
					result.append(", " + variable.getName());
			}
			if (boundVariableSubList.getRight() != null) {
				result.append(": ");
				result.append(expression2Pretty(boundVariableSubList.getRight(),
						vacantLength(maxLength, result)));
			}
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer compoundLiteral2Pretty(
			CompoundLiteralNode compound, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		CompoundInitializerNode list = compound.getInitializerList();
		// int numPairs = list.numChildren();
		TypeNode typeNode = compound.getTypeNode();

		if (typeNode != null) {
			result.append("(");
			result.append(type2Pretty("", compound.getTypeNode(), false,
					vacantLength(maxLength, result)));
			result.append(")");
		}
		result.append(compoundInitializer2Pretty(list,
				vacantLength(maxLength, result)));
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer regularRange2Pretty(RegularRangeNode range,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		ExpressionNode step = range.getStep();

		result.append(expression2Pretty(range.getLow(),
				vacantLength(maxLength, result)));
		result.append(" .. ");
		result.append(expression2Pretty(range.getHigh(),
				vacantLength(maxLength, result)));
		if (step != null) {
			result.append(" # ");
			result.append(
					expression2Pretty(step, vacantLength(maxLength, result)));
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer sizeable2Pretty(SizeableNode argument,
			int maxLength) {
		if (argument instanceof ExpressionNode)
			return expression2Pretty((ExpressionNode) argument, maxLength);
		return type2Pretty("", (TypeNode) argument, false, maxLength);
	}

	private static StringBuffer functionCall2Pretty(FunctionCallNode call,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		int argNum = call.getNumberOfArguments();
		StringBuffer result = new StringBuffer();

		result.append(expression2Pretty(call.getFunction(),
				vacantLength(maxLength, result)));
		if (call.getNumberOfContextArguments() > 0) {
			result.append("<<<");
			for (int i = 0; i < call.getNumberOfContextArguments(); i++) {
				if (i > 0)
					result.append(", ");
				result.append(expression2Pretty(call.getContextArgument(i),
						vacantLength(maxLength, result)));
			}
			result.append(">>>");
		}
		result.append("(");
		for (int i = 0; i < argNum; i++) {
			if (i > 0)
				result.append(", ");
			result.append(expression2Pretty(call.getArgument(i),
					vacantLength(maxLength, result)));
		}
		result.append(")");
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer operator2Pretty(OperatorNode operator,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		Operator op = operator.getOperator();
		ExpressionNode argNode0 = operator.getArgument(0);
		ExpressionNode argNode1 = operator.numChildren() > 1
				? operator.getArgument(1)
				: null;
		StringBuffer arg0Buf = expression2Pretty(argNode0, maxLength);
		String arg0 = arg0Buf.toString();
		String arg1 = argNode1 != null
				? expression2Pretty(argNode1, vacantLength(maxLength, arg0Buf))
						.toString()
				: null;
		String argWtP0 = arg0, argWtP1 = arg1;

		if (argNode0.expressionKind() == ExpressionKind.OPERATOR)
			argWtP0 = "(" + arg0 + ")";
		if (argNode1 != null
				&& argNode1.expressionKind() == ExpressionKind.OPERATOR)
			argWtP1 = "(" + arg1 + ")";
		switch (op) {
			case ADDRESSOF :
				result.append("&(");
				result.append(arg0);
				result.append(")");
				break;
			case APPLY :
				result.append("(");
				result.append(arg0);
				result.append(")");
				result.append("(");
				result.append(arg1);
				result.append(")");
				break;
			case ASSIGN :
				result.append(arg0);
				result.append(" = ");
				result.append(arg1);
				break;
			case HASH :
				result.append(arg0);
				result.append("#");
				result.append(arg1);
				break;
			case BIG_O :
				result.append("$O(");
				result.append(arg0);
				result.append(")");
				break;
			case BITAND :
				result.append(argWtP0);
				result.append(" & ");
				result.append(argWtP1);
				break;
			case BITANDEQ :
				result.append(argWtP0);
				result.append(" &= ");
				result.append(argWtP1);
				break;
			case BITCOMPLEMENT :
				result.append("~");
				result.append(argWtP0);
				break;
			case BITEQUIV :
				result.append(argWtP0);
				result.append("<-->");
				result.append(argWtP1);
				break;
			case BITIMPLIES :
				result.append(argWtP0);
				result.append("-->");
				result.append(argWtP1);
				break;
			case BITOR :
				result.append(argWtP0);
				result.append(" | ");
				result.append(argWtP1);
				break;
			case BITOREQ :
				result.append(argWtP0);
				result.append(" |= ");
				result.append(argWtP1);
				break;
			case BITXOR :
				result.append(argWtP0);
				result.append(" ^ ");
				result.append(argWtP1);
				break;
			case BITXOREQ :
				result.append(argWtP0);
				result.append(" ^= ");
				result.append(argWtP1);
				break;
			case COMMA :
				result.append(arg0);
				result.append(", ");
				result.append(arg1);
				break;
			case CONDITIONAL :
				result.append(arg0);
				result.append(" ? ");
				result.append(arg1);
				result.append(" : ");
				result.append(expression2Pretty(operator.getArgument(2),
						vacantLength(maxLength, result)));
				break;
			case DEREFERENCE :
				result.append("*");
				result.append(arg0);
				break;
			case DIV :
				result.append(argWtP0);
				result.append(" / ");
				result.append(argWtP1);
				break;
			case DIVEQ :
				result.append(argWtP0);
				result.append(" /= ");
				result.append(argWtP1);
				break;
			case EQUALS :
				result.append(argWtP0);
				result.append(" == ");
				result.append(argWtP1);
				break;
			case GT :
				result.append(argWtP0);
				result.append(" > ");
				result.append(argWtP1);
				break;
			case GTE :
				result.append(argWtP0);
				result.append(" >= ");
				result.append(argWtP1);
				break;
			case IMPLIES :
				result.append(argWtP0);
				result.append(" => ");
				result.append(argWtP1);
				break;
			case LAND :
				result.append(argWtP0);
				result.append(" && ");
				result.append(argWtP1);
				break;
			case LOR :
				result.append(argWtP0);
				result.append(" || ");
				result.append(argWtP1);
				break;
			case LXOR :
				result.append(argWtP0);
				result.append(" ^^ ");
				result.append(argWtP1);
				break;
			case LEQ :
				result.append(argWtP0);
				result.append(" <==> ");
				result.append(argWtP1);
				break;
			case LT :
				result.append(argWtP0);
				result.append(" < ");
				result.append(argWtP1);
				break;
			case LTE :
				result.append(argWtP0);
				result.append(" <= ");
				result.append(argWtP1);
				break;
			case MINUS :
				result.append(argWtP0);
				result.append(" - ");
				result.append(argWtP1);
				break;
			case MINUSEQ :
				result.append(argWtP0);
				result.append(" -= ");
				result.append(argWtP1);
				break;
			case MOD :
				result.append(argWtP0);
				result.append(" % ");
				result.append(argWtP1);
				break;
			case MODEQ :
				result.append(argWtP0);
				result.append(" %= ");
				result.append(argWtP1);
				break;
			case NEQ :
				result.append(argWtP0);
				result.append(" != ");
				result.append(argWtP1);
				break;
			case NOT :
				result.append("!");
				result.append(argWtP0);
				break;
			case OLD :
				result.append("\\old(");
				result.append(arg0);
				result.append(")");
				break;
			case PLUS :
				result.append(argWtP0);
				result.append(" + ");
				result.append(argWtP1);
				break;
			case PLUSEQ :
				result.append(arg0);
				result.append(" += ");
				result.append(arg1);
				break;
			case POSTDECREMENT :
				result.append(arg0);
				result.append("--");
				break;
			case POSTINCREMENT :
				result.append(arg0);
				result.append("++");
				break;
			case PREDECREMENT :
				result.append("--");
				result.append(arg0);
				break;
			case PREINCREMENT :
				result.append("++");
				result.append(arg0);
				break;
			case SHIFTLEFT :
				result.append(argWtP0);
				result.append(" << ");
				result.append(argWtP1);
				break;
			case SHIFTLEFTEQ :
				result.append(argWtP0);
				result.append(" <<= ");
				result.append(argWtP1);
				break;
			case SHIFTRIGHT :
				result.append(argWtP0);
				result.append(" >> ");
				result.append(argWtP1);
				break;
			case SHIFTRIGHTEQ :
				result.append(argWtP0);
				result.append(" >>= ");
				result.append(argWtP1);
				break;
			case SUBSCRIPT :
				result.append(arg0);
				result.append("[");
				result.append(arg1);
				result.append("]");
				break;
			case TIMES :
				result.append(argWtP0);
				result.append(" * ");
				result.append(argWtP1);
				break;
			case TIMESEQ :
				result.append(argWtP0);
				result.append(" -= ");
				result.append(argWtP1);
				break;
			case UNARYMINUS :
				result.append("-");
				result.append(argWtP0);
				break;
			case UNARYPLUS :
				result.append("+");
				result.append(argWtP0);
				break;
			case VALID :
				result.append("\\valid(");
				result.append(arg0);
				result.append(")");
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty print of operator node of " + op + " kind");
		}
		return trimStringBuffer(result, maxLength);
	}

	private static StringBuffer type2Pretty(String prefix, TypeNode type,
			boolean isTypeDeclaration, int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		TypeNodeKind kind = type.kind();

		result.append(prefix);
		switch (kind) {
			case ARRAY : {
				ArrayTypeNode arrayType = (ArrayTypeNode) type;
				ExpressionNode extent = arrayType.getExtent();

				// result.append("(");
				result.append(type2Pretty("", arrayType.getElementType(),
						isTypeDeclaration, maxLength));
				// result.append(")");
				result.append("[");
				if (extent != null)
					result.append(expression2Pretty(extent,
							vacantLength(maxLength, result)));
				result.append("]");
			}
				break;
			case DOMAIN : {
				DomainTypeNode domainType = (DomainTypeNode) type;
				ExpressionNode dim = domainType.getDimension();

				result.append("$domain");
				if (dim != null) {
					result.append("(");
					result.append(expression2Pretty(dim,
							vacantLength(maxLength, result)));
					result.append(")");
				}
				break;
			}
			case MEM :
				result.append(prefix + "$mem");
				break;
			case LAMBDA :
				LambdaTypeNode lambdaType = (LambdaTypeNode) type;
				result.append(prefix + "$lambda(");
				result.append(type2Pretty("", lambdaType.freeVariableType(),
						false, vacantLength(maxLength, result)));
				result.append(":");
				result.append(type2Pretty("", lambdaType.freeVariableType(),
						false, vacantLength(maxLength, result)));
				result.append(")");
				break;
			case VOID :
				result.append("void");
				break;
			case BASIC :
				result.append(
						basicType2Pretty((BasicTypeNode) type, maxLength));
				break;
			case ENUMERATION :
				EnumerationTypeNode enumType = (EnumerationTypeNode) type;
				return enumType2Pretty(prefix, enumType, maxLength);
			case STRUCTURE_OR_UNION : {
				StructureOrUnionTypeNode strOrUnion = (StructureOrUnionTypeNode) type;

				return structOrUnion2Pretty(prefix, strOrUnion,
						isTypeDeclaration, maxLength);
			}
			case POINTER :
				result.append(type2Pretty("",
						((PointerTypeNode) type).referencedType(),
						isTypeDeclaration, maxLength));
				result.append("*");
				break;
			case TYPEDEF_NAME :
				result.append(((TypedefNameNode) type).getName().name());
				break;
			case SCOPE :
				result.append("$scope");
				break;
			case FUNCTION : {
				FunctionTypeNode funcType = (FunctionTypeNode) type;
				SequenceNode<VariableDeclarationNode> paras = funcType
						.getParameters();
				int i = 0;

				result.append(" (");
				result.append(type2Pretty(prefix, funcType.getReturnType(),
						false, maxLength - 1));
				result.append(" (");
				for (VariableDeclarationNode para : paras) {
					if (i != 0)
						result.append(", ");
					result.append(variableDeclaration2Pretty("", para,
							vacantLength(maxLength, result)));
					i++;
				}
				result.append(")");
				result.append(")");
				break;
			}
			case RANGE :
				result.append("$range");
				break;
			case TYPEOF :
				result.append("typeof(");
				result.append(expression2Pretty(
						((TypeofNode) type).getExpressionOperand(),
						vacantLength(maxLength, result)));
				result.append(")");
				break;
			case STATE :
				result.append("$state");
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty print of type node of " + kind + " kind");
		}
		return trimStringBuffer(result, maxLength);
	}

	/**
	 * Pretty printing for {@link MPIContractExpressionNode}.
	 *
	 * @param node
	 *            A instance of the {@link MPIContractExpressionNode}.
	 * @return
	 * @throws ABCException
	 */
	private static StringBuffer mpiContractExpression2Pretty(
			MPIContractExpressionNode node, int maxLength) {
		MPIContractExpressionKind kind = node.MPIContractExpressionKind();
		StringBuffer result = new StringBuffer();
		int numArgs = -1;
		String prettyName;

		switch (kind) {
		case MPI_AGREE:
			numArgs = 1;
			prettyName = "\\mpi_agree(";
			break;
		case MPI_BACK_MESSAGES:
			numArgs = 0;
			prettyName = "\\mpi_back_messages";
			break;
		case MPI_BUF:
			numArgs = 3;
			prettyName = "\\mpi_buf(";
			break;
		case MPI_COMM_RANK:
			numArgs = 0;
			prettyName = "\\mpi_comm_rank";
			break;
		case MPI_COMM_SIZE:
			numArgs = 0;
			prettyName = "\\mpi_comm_size";
			break;
		case MPI_EXTENT:
			numArgs = 1;
			prettyName = "\\mpi_extent(";
			break;
		case MPI_ON:
			numArgs = 2;
			prettyName = "\\mpi_on(";
			break;
	
		case MPI_NONOVERLAPPING:
			numArgs = 1;
			prettyName = "\\mpi_nonoverlapping(";
			break;
		case MPI_SIG:
			numArgs = 1;
			prettyName = "\\mpi_sig(";
			break;
		case MPI_REDUCE:
			numArgs = 5;
			prettyName = "\\mpi_reduce(";
			break;
		case MPI_ABSENT: {
			MPIContractAbsentNode absentNode = (MPIContractAbsentNode) node;

			result.append("$absent(");
			result.append(prettyAbsentEvent(absentNode.absentEvent(), maxLength)
					+ ", ");
			result.append(prettyAbsentEvent(absentNode.fromEvent(), maxLength)
					+ ", ");
			result.append(
					prettyAbsentEvent(absentNode.untilEvent(), maxLength));
			result.append(")");
			return result;
		}
		case MPI_ABSENT_EVENT:
		default:
			throw new ABCUnsupportedException(
					"Unknown MPI contract expression kind : " + kind);
		}
		result.append(prettyName);
		if (numArgs > 0) {
			result.append(expression2Pretty(node.getArgument(0), maxLength));
			for (int i = 1; i < numArgs; i++)
				result.append(", "
						+ expression2Pretty(node.getArgument(i), maxLength));
			result.append(")");
		}
		return result;
	}

	private static String prettyAbsentEvent(MPIContractAbsentEventNode event,
			int maxLength) {
		String result;

		switch (event.absentEventKind()) {
			case SENDTO :
				result = "$sendto(";
				break;
			case SENDFROM :
				result = "$sendfrom(";
				break;
			case ENTER :
				result = "$enter(";
				break;
			case EXIT :
				result = "$exit(";
				break;
			default :
				throw new ABCUnsupportedException("Unknown MPI absent event "
						+ "kind " + event.absentEventKind());
		}
		for (ExpressionNode arg : event.arguments()) {
			result += expression2Pretty(arg, maxLength) + ", ";
		}
		result = result.substring(0, result.length() - 2);
		return result += ")";
	}

	private static StringBuffer basicType2Pretty(BasicTypeNode type,
			int maxLength) {
		if (maxLength == 0)
			return EMPTY_STRING_BUFFER;

		StringBuffer result = new StringBuffer();
		BasicTypeKind basicKind = type.getBasicTypeKind();

		switch (basicKind) {
			case BOOL :
				result.append("_Bool");
				break;
			case CHAR :
				result.append("char");
				break;
			case DOUBLE :
				result.append("double");
				break;
			case DOUBLE_COMPLEX :
				result.append("double _Complex");
				break;
			case FLOAT :
				result.append("float");
				break;
			case FLOAT_COMPLEX :
				result.append("float _Complex");
				break;
			case INT :
				result.append("int");
				break;
			case LONG :
				result.append("long");
				break;
			case LONG_DOUBLE :
				result.append("long double");
				break;
			case LONG_DOUBLE_COMPLEX :
				result.append("long double _Complex");
				break;
			case LONG_LONG :
				result.append("long long");
				break;
			case REAL :
				result.append("$real");
				break;
			case SHORT :
				result.append("short");
				break;
			case SIGNED_CHAR :
				result.append("signed char");
				break;
			case UNSIGNED :
				result.append("unsigned");
				break;
			case UNSIGNED_CHAR :
				result.append("unsigned char");
				break;
			case UNSIGNED_LONG :
				result.append("unsigned long");
				break;
			case UNSIGNED_LONG_LONG :
				result.append("unsigned long long");
				break;
			case UNSIGNED_SHORT :
				result.append("unsigned short");
				break;
			default :
				throw new ABCUnsupportedException(
						"pretty print of basic type node of " + basicKind
								+ " kind");
		}
		return trimStringBuffer(result, maxLength);
	}
}
