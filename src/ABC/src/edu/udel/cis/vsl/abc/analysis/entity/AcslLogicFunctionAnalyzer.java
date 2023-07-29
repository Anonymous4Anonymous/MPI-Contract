package edu.udel.cis.vsl.abc.analysis.entity;

import edu.udel.cis.vsl.abc.analysis.common.ScopeAnalyzer;
import edu.udel.cis.vsl.abc.ast.conversion.IF.ConversionFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.WildcardNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.CompoundStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.JumpNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.JumpNode.JumpKind;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ReturnNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode.StatementKind;
import edu.udel.cis.vsl.abc.ast.type.IF.ArrayType;
import edu.udel.cis.vsl.abc.ast.type.IF.IntegerType;
import edu.udel.cis.vsl.abc.ast.type.IF.ObjectType;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.TypeFactory;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

/**
 * This class extends {@link ExpressionAnalyzer} to analyze logic function
 * bodies. Logic function bodies are expressions. The front-end translator
 * translates a such body to a return statement in a compound statement form.
 * Given such a compound statement, the
 * {@link #processLogicFunctionBody(CompoundStatementNode)} will extract the
 * expression from the return statement and analyze it.
 * 
 * @author xxxx
 *
 */
public class AcslLogicFunctionAnalyzer extends ExpressionAnalyzer {

	AcslLogicFunctionAnalyzer(EntityAnalyzer entityAnalyzer, ConversionFactory conversionFactory, TypeFactory typeFactory, ScopeAnalyzer scopeAnalyzer) {
		super(entityAnalyzer, conversionFactory, typeFactory, scopeAnalyzer);
	}

	/**
	 * Analyzes a logic function body as an expression.
	 * 
	 * @param body
	 *            the body child of a {@link FunctionDefinitionNode}
	 *            representing a logic function.
	 * @throws SyntaxException
	 */
	void processLogicFunctionBody(CompoundStatementNode body)
			throws SyntaxException {
		if (body.numChildren() == 1) {
			if (body.getSequenceChild(0).nodeKind() == NodeKind.STATEMENT) {
				StatementNode retStmt = (StatementNode) body
						.getSequenceChild(0);

				if (retStmt.statementKind() == StatementKind.JUMP) {
					JumpNode jumpNode = (JumpNode) retStmt;

					if (jumpNode.getKind() == JumpKind.RETURN) {
						processExpression(
								((ReturnNode) jumpNode).getExpression());
						return;
					}
				}
			}
		}
		throw new SyntaxException(
				"Logic function body is in an unexpected form.",
				body.getSource());
	}

	// Compare to the overrided method in the super class, this method relaxed
	// the restrictions from the strict C standard.
	@Override
	protected void processSUBSCRIPT(OperatorNode node) throws SyntaxException {
		ExpressionNode arg0 = node.getArgument(0);
		ExpressionNode arg1 = node.getArgument(1);
		Type type0 = arg0.getConvertedType();
		Type type1 = addStandardConversions(arg1);
		ObjectType rangeType = typeFactory.rangeType();

		if (SetTypeAnalyzer.processSetTypeForSUBSCRIPT(this, arg0, arg1, node))
			return;
		if (!(type1 instanceof IntegerType) && !(type1.equals(rangeType))
				&& !(arg1 instanceof WildcardNode))
			throw error(
					"Subscript does not have integer or range type:\n" + type1,
					arg1);
		// the following will check pointer in any case
		// if strict C, must also be pointer to complete object type:
		if (isArrayType((ObjectType) type0)) {
			node.setInitialType(((ArrayType) type0).getElementType());
		} else {
			// pointer type:
			node.setInitialType(((PointerType) type0).referencedType());
		}
	}
}
