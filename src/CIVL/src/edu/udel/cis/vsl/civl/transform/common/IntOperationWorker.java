package edu.udel.cis.vsl.civl.transform.common;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Entity;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.SequenceNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.common.acsl.CommonContractNode;
import edu.udel.cis.vsl.abc.ast.node.common.expression.CommonQuantifiedExpressionNode;
import edu.udel.cis.vsl.abc.ast.type.IF.IntegerType;
import edu.udel.cis.vsl.abc.ast.type.IF.SignedIntegerType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardUnsignedIntegerType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardUnsignedIntegerType.UnsignedIntKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.front.IF.CivlcTokenConstant;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.transform.IF.IntOperationTransformer;
import edu.udel.cis.vsl.civl.util.IF.Utils;

/**
 * <p>
 * IntDivWorker is used by {@link IntOperationTransformer} to conduct the
 * transform.
 * </p>
 * 
 * @author xxxxxxxx
 *
 */
public class IntOperationWorker extends BaseWorker {

	/* *******************static constants************************ */
	/**
	 * below are names of files of libraries and functions in libraries.
	 */
	private static final String INT_DIV = "$int_div";
	private static final String INT_MOD = "$int_mod";
	private static final String ASSERT = "$assert";
	private static final String REMAINDER = "$remainder";
	private static final String QUOTIENT = "$quotient";
	private static final String INT_DIV_SOURCE_FILE = "int_div.cvl";
	private static final String UNSIGNED_ADD = "$unsigned_add";
	private static final String UNSIGNED_SUBSTRACT = "$unsigned_subtract";
	private static final String UNSIGNED_MULTIPLY = "$unsigned_multiply";
	private static final String SIGNED_TO_UNSIGNED = "$signed_to_unsigned";
	private static final String UNSIGNED_NEG = "$unsigned_neg";
	private static final String UNSIGNED_ARITH_SOURCE_FILE = "unsigned_arith.cvl";
	private Entity divEntity = null, modEntity = null, unsignedAddEntity = null,
			unsignedSubstractEntity = null, unsignedMultiplyEntity = null,
			signedToUnsignedEntity = null, unsignedNegEntity = null;
	private Map<String, String> macros;
	/**
	 * intDivProcessed is true iff int_div.cvl is already linked.
	 */
	private boolean intDivProcessed = false;
	/**
	 * unsignedArithProcessed is true iff unsigned_arith.cvl is already linked.
	 */
	private boolean unsignedArithProcessed = false;
	/**
	 * civlConfig.getIntBit() tells the size of integer.
	 */
	private CIVLConfiguration civlConfig;

	public IntOperationWorker(ASTFactory astFactory, Map<String, String> macros,
			CIVLConfiguration civlConfig) {
		super(IntOperationTransformer.LONG_NAME, astFactory);
		this.identifierPrefix = "_int_div_";
		this.macros = macros;
		this.civlConfig = civlConfig;
	}

	@Override
	public AST transform(AST unit) throws SyntaxException {
		SequenceNode<BlockItemNode> root = unit.getRootNode();
		AST newAst;

		divEntity = unit.getInternalOrExternalEntity(INT_DIV);
		modEntity = unit.getInternalOrExternalEntity(INT_MOD);
		unsignedAddEntity = unit.getInternalOrExternalEntity(UNSIGNED_ADD);
		unsignedSubstractEntity = unit
				.getInternalOrExternalEntity(UNSIGNED_SUBSTRACT);
		unsignedMultiplyEntity = unit
				.getInternalOrExternalEntity(UNSIGNED_MULTIPLY);
		signedToUnsignedEntity = unit
				.getInternalOrExternalEntity(SIGNED_TO_UNSIGNED);
		unsignedNegEntity = unit.getInternalOrExternalEntity(UNSIGNED_NEG);

		if (divEntity != null || modEntity != null) {
			intDivProcessed = true;
		}
		if (unsignedAddEntity != null || unsignedSubstractEntity != null
				|| unsignedMultiplyEntity != null
				|| signedToUnsignedEntity != null
				|| unsignedNegEntity != null) {
			unsignedArithProcessed = true;
		}

		// if both libraries are processed, then return.
		if (intDivProcessed && unsignedArithProcessed)
			return unit;

		unit.release();
		if (!unsignedArithProcessed)
			linkUnsignedArithLibrary(root);
		if (!intDivProcessed)
			linkIntDivLibrary(root);
		processIntegerOperation(root);
		this.completeSources(root);
		newAst = astFactory.newAST(root, unit.getSourceFiles(),
				unit.isWholeProgram());

		return newAst;
	}

	/**
	 * <p>
	 * Go through the AST from the root node,
	 * <ul>
	 * <li>replace {@link OperatorNode}s whose {@link Operator}s are
	 * {@link Operator#DIV} or {@link Operator#MOD} with functions
	 * {@link #INT_DIV} or {@link #INT_MOD} defined in
	 * {@link #INT_DIV_SOURCE_FILE} respectively.</li>
	 * <li>replace unsigned arithmetic operations ({@link Operator#PLUS},
	 * {@link Operator#MINUS}, {@link Operator#TIMES}) with corresponding
	 * functions defined in {@link #UNSIGNED_ARITH_SOURCE_FILE}</li>
	 * <li>transform uanry arithmetic operations for unsigned integers.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param node
	 *            the node to be transformed
	 * @throws SyntaxException
	 */
	private void processIntegerOperation(ASTNode node) throws SyntaxException {
		if (node instanceof OperatorNode) {
			OperatorNode opn = (OperatorNode) node;
			Operator op = opn.getOperator();

			if (op == Operator.DIV || op == Operator.MOD) {
				processIntDivNode((OperatorNode) node);
			} else if (op == Operator.PLUS || op == Operator.MINUS
					|| op == Operator.TIMES) {
				processUnsignedArithNode(opn);
			} else if (op == Operator.UNARYMINUS || op == Operator.POSTINCREMENT
					|| op == Operator.PREINCREMENT
					|| op == Operator.POSTDECREMENT
					|| op == Operator.PREDECREMENT) {
				processUnaryNode(opn);
			} else {
				processOtherNodes(node);
			}
		} else {
			processOtherNodes(node);
		}
	}

	/**
	 * Transform unary operatorNode.
	 * 
	 * @param opn
	 *            Unary {@link OperatorNode} which includes Pre/Postincrement
	 *            and Pre/Postdecrement.
	 * @throws SyntaxException
	 */
	private void processUnaryNode(OperatorNode opn) throws SyntaxException {
		ExpressionNode operand = opn.getArgument(0);

		if (!this.isUnsignedIntegerType(operand.getConvertedType())
				&& !this.isUnsignedIntegerType(opn.getConvertedType()))
			return;

		Operator op = opn.getOperator();
		ASTNode parent = opn.parent();
		int childIndex = opn.childIndex();

		switch (op) {
			case UNARYMINUS : {
				if (operand instanceof IntegerConstantNode) {
					IntegerConstantNode intNode = (IntegerConstantNode) operand;
					BigInteger value = intNode.getConstantValue()
							.getIntegerValue();
					int intBit = civlConfig.getIntBit();

					if (intBit < 32 && value.bitCount() < 32) {
						int intValue = 0 - value.intValue();
						int bound = (int) Math.pow(2, intBit);

						if (intValue < 0 || intValue >= bound) {
							intValue = intValue % bound;
							if (intValue < 0)
								intValue += bound;
							parent.setChild(childIndex,
									this.integerConstant(intValue));
						}
						break;
					}

					String funcName = UNSIGNED_NEG;
					String method = funcName + "()";
					Source source = this.newSource(method,
							CivlcTokenConstant.CALL);
					List<ExpressionNode> args = new ArrayList<ExpressionNode>();

					opn.remove();
					args.add(operand.copy());
					args.add(getBound());

					FunctionCallNode funcCallNode = functionCall(source,
							funcName, args);

					funcCallNode.setInitialType(opn.getConvertedType());
					parent.setChild(childIndex, funcCallNode);
				}
				break;
			}
			case POSTINCREMENT : {
				OperatorNode replacement = postIncrementReplacement(operand);

				opn.remove();
				replacement.setInitialType(opn.getConvertedType());
				parent.setChild(childIndex, replacement);
				break;
			}
			case PREINCREMENT : {
				OperatorNode replacement = preIncrementReplacement(operand);

				opn.remove();
				replacement.setInitialType(opn.getConvertedType());
				parent.setChild(childIndex, replacement);
				break;
			}
			case POSTDECREMENT : {
				OperatorNode replacement = postDecrementReplacement(operand);

				opn.remove();
				replacement.setInitialType(opn.getConvertedType());
				parent.setChild(childIndex, replacement);
				break;
			}
			case PREDECREMENT : {
				OperatorNode replacement = preDecrementReplacement(operand);

				opn.remove();
				replacement.setInitialType(opn.getConvertedType());
				parent.setChild(childIndex, replacement);
				break;
			}
			default :
				break;
		}
	}

	/**
	 * <p>
	 * Transform preIncrement operator node:
	 * </p>
	 * 
	 * <p>
	 * ++x is transformed to (x &lt; bound-1 ? ++x : (x=0))
	 * </p>
	 * 
	 * @param operand
	 *            The operand of the unary operator node.
	 * 
	 * @return the transformed node which is a conditional operator node.
	 * @throws SyntaxException
	 */
	private OperatorNode preIncrementReplacement(ExpressionNode operand)
			throws SyntaxException {
		IntegerConstantNode constantOne = null, constantZero = null,
				bound = getBound();
		OperatorNode boundMinusOneNode = null, assignedZeroNode = null,
				lessThanNode = null, preIncreNode = null, conditionNode = null;
		String one = "1", zero = "0", boundMinusOne = "bound - 1",
				assignedZero = operand.toString() + "=0",
				lessThan = operand.toString() + "<" + boundMinusOne,
				preIncre = "++" + operand.toString(),
				condition = lessThan + "?" + preIncre + ":" + assignedZero;
		Source oneSource = this.newSource(one,
				CivlcTokenConstant.INTEGER_CONSTANT);
		Source zeroSource = this.newSource(zero,
				CivlcTokenConstant.INTEGER_CONSTANT);
		Source boundMinusOneSource = this.newSource(boundMinusOne,
				CivlcTokenConstant.SUB);
		Source assignedZeroSource = this.newSource(assignedZero,
				CivlcTokenConstant.ASSIGNS);
		Source lessThanSource = this.newSource(lessThan, CivlcTokenConstant.LT);
		Source preIncreSource = this.newSource(preIncre,
				CivlcTokenConstant.PRE_INCREMENT);
		Source conditionSource = this.newSource(condition,
				CivlcTokenConstant.IF);

		constantOne = this.nodeFactory.newIntegerConstantNode(oneSource, one);
		constantZero = this.nodeFactory.newIntegerConstantNode(zeroSource,
				zero);
		boundMinusOneNode = this.nodeFactory.newOperatorNode(
				boundMinusOneSource, Operator.MINUS, bound, constantOne);
		assignedZeroNode = this.nodeFactory.newOperatorNode(assignedZeroSource,
				Operator.ASSIGN, operand.copy(), constantZero);
		lessThanNode = this.nodeFactory.newOperatorNode(lessThanSource,
				Operator.LT, operand.copy(), boundMinusOneNode.copy());
		preIncreNode = this.nodeFactory.newOperatorNode(preIncreSource,
				Operator.PREINCREMENT, operand.copy());
		conditionNode = this.nodeFactory.newOperatorNode(conditionSource,
				Operator.CONDITIONAL, lessThanNode, preIncreNode,
				assignedZeroNode);
		return conditionNode;
	}

	/**
	 * <p>
	 * Transform post increment operator node.
	 * </p>
	 * 
	 * <p>
	 * x++ is transformed to (x &lt; bound-1 ? x++ : ((x=0), bound-1))
	 * </p>
	 * 
	 * @param operand
	 *            The operand of the unary operator node.
	 * @return the transformed node which is a conditional operator node.
	 * @throws SyntaxException
	 */
	private OperatorNode postIncrementReplacement(ExpressionNode operand)
			throws SyntaxException {
		IntegerConstantNode constantOne = null, constantZero = null,
				bound = getBound();
		OperatorNode boundMinusOneNode = null, assignedZeroNode = null,
				commaNode = null, lessThanNode = null, postIncreNode = null,
				conditionNode = null;
		String one = "1", zero = "0", boundMinusOne = "bound - 1",
				assignedZero = operand.toString() + "=0",
				comma = assignedZero + " " + boundMinusOne,
				lessThan = operand.toString() + "<" + boundMinusOne,
				postIncre = operand.toString() + "++",
				condition = lessThan + "?" + postIncre + ":" + comma;
		Source oneSource = this.newSource(one,
				CivlcTokenConstant.INTEGER_CONSTANT);
		Source zeroSource = this.newSource(zero,
				CivlcTokenConstant.INTEGER_CONSTANT);
		Source boundMinusOneSource = this.newSource(boundMinusOne,
				CivlcTokenConstant.SUB);
		Source assignedZeroSource = this.newSource(assignedZero,
				CivlcTokenConstant.ASSIGNS);
		Source commaSource = this.newSource(comma, CivlcTokenConstant.COMMA);
		Source lessThanSource = this.newSource(lessThan, CivlcTokenConstant.LT);
		Source postIncreSource = this.newSource(postIncre,
				CivlcTokenConstant.POST_DECREMENT);
		Source conditionSource = this.newSource(condition,
				CivlcTokenConstant.IF);

		constantOne = this.nodeFactory.newIntegerConstantNode(oneSource, one);
		constantZero = this.nodeFactory.newIntegerConstantNode(zeroSource,
				zero);
		boundMinusOneNode = this.nodeFactory.newOperatorNode(
				boundMinusOneSource, Operator.MINUS, bound, constantOne);
		assignedZeroNode = this.nodeFactory.newOperatorNode(assignedZeroSource,
				Operator.ASSIGN, operand.copy(), constantZero);
		commaNode = this.nodeFactory.newOperatorNode(commaSource,
				Operator.COMMA, assignedZeroNode, boundMinusOneNode);
		lessThanNode = this.nodeFactory.newOperatorNode(lessThanSource,
				Operator.LT, operand.copy(), boundMinusOneNode.copy());
		postIncreNode = this.nodeFactory.newOperatorNode(postIncreSource,
				Operator.POSTINCREMENT, operand.copy());
		conditionNode = this.nodeFactory.newOperatorNode(conditionSource,
				Operator.CONDITIONAL, lessThanNode, postIncreNode, commaNode);
		return conditionNode;
	}

	/**
	 * <p>
	 * Transform post decrement operator node.
	 * </p>
	 * 
	 * <p>
	 * x-- is transformed to (x &lt; 1-bound ? ((x=0), -bound) : x--)
	 * </p>
	 * 
	 * @param operand
	 *            The operand of the unary operator node.
	 * @return the transformed node which is a conditional operator node.
	 * @throws SyntaxException
	 */
	private OperatorNode postDecrementReplacement(ExpressionNode operand)
			throws SyntaxException {
		IntegerConstantNode bound = getBound();
		OperatorNode assignedMaxNode = null, commaNode = null,
				isZeroNode = null, postDecreNode = null, conditionNode = null;
		String oneMinusBound = "1 - bound",
				assignedMax = operand.toString() + "=bound - 1",
				comma = assignedMax + " " + oneMinusBound,
				lessThan = operand.toString() + "<" + oneMinusBound,
				postDecre = operand.toString() + "--",
				condition = lessThan + "?" + comma + ":" + postDecre;
		Source assignedMaxSource = this.newSource(assignedMax,
				CivlcTokenConstant.ASSIGNS);
		Source commaSource = this.newSource(comma, CivlcTokenConstant.COMMA);
		Source lessThanSource = this.newSource(lessThan, CivlcTokenConstant.LT);
		Source postDecreSource = this.newSource(postDecre,
				CivlcTokenConstant.POST_DECREMENT);
		Source conditionSource = this.newSource(condition,
				CivlcTokenConstant.IF);

		assignedMaxNode = this.nodeFactory.newOperatorNode(assignedMaxSource,
				Operator.ASSIGN, operand.copy(),
				nodeFactory.newOperatorNode(assignedMaxSource, Operator.MINUS,
						bound, this.integerConstant(1)));
		commaNode = this.nodeFactory.newOperatorNode(commaSource,
				Operator.COMMA, assignedMaxNode, this.integerConstant(0));
		isZeroNode = this.nodeFactory.newOperatorNode(lessThanSource,
				Operator.EQUALS, operand.copy(), this.integerConstant(0));
		postDecreNode = this.nodeFactory.newOperatorNode(postDecreSource,
				Operator.POSTDECREMENT, operand.copy());
		conditionNode = this.nodeFactory.newOperatorNode(conditionSource,
				Operator.CONDITIONAL, isZeroNode, commaNode, postDecreNode);
		return conditionNode;
	}

	/**
	 * <p>
	 * Transform pre decrement operator node.
	 * </p>
	 * 
	 * <p>
	 * --x is transformed to (x &lt; 1-bound ? (x=0) : --x)
	 * </p>
	 * 
	 * @param operand
	 *            The operand of the unary operator node.
	 * @return the transformed node which is a conditional operator node.
	 * @throws SyntaxException
	 */
	private OperatorNode preDecrementReplacement(ExpressionNode operand)
			throws SyntaxException {
		IntegerConstantNode bound = getBound();
		OperatorNode assignedMaxNode = null, isZeroNode = null,
				preDecreNode = null, conditionNode = null;
		String assignedMax = operand.toString() + "=bound-1",
				preDecre = "--" + operand.toString(),
				condition = "isZero?" + assignedMax + ":" + preDecre;
		Source assignedMaxSource = this.newSource(assignedMax,
				CivlcTokenConstant.ASSIGNS);
		Source lessThanSource = this.newSource("isZero", CivlcTokenConstant.LT);
		Source preDecreSource = this.newSource(preDecre,
				CivlcTokenConstant.PRE_DECREMENT);
		Source conditionSource = this.newSource(condition,
				CivlcTokenConstant.IF);

		assignedMaxNode = this.nodeFactory.newOperatorNode(assignedMaxSource,
				Operator.ASSIGN, operand.copy(),
				nodeFactory.newOperatorNode(assignedMaxSource, Operator.MINUS,
						bound, this.integerConstant(1)));
		isZeroNode = this.nodeFactory.newOperatorNode(lessThanSource,
				Operator.EQUALS, operand.copy(), this.integerConstant(0));
		preDecreNode = this.nodeFactory.newOperatorNode(preDecreSource,
				Operator.PREDECREMENT, operand.copy());
		conditionNode = this.nodeFactory.newOperatorNode(conditionSource,
				Operator.CONDITIONAL, isZeroNode, assignedMaxNode,
				preDecreNode);
		return conditionNode;
	}

	private void processOtherNodes(ASTNode node) throws SyntaxException {
		if (node instanceof FunctionDeclarationNode) {
			FunctionDeclarationNode funcDeclNode = (FunctionDeclarationNode) node;
			String name = funcDeclNode.getName();

			if (name.equals(INT_DIV) || name.equals(INT_MOD)
					|| name.equals(UNSIGNED_ADD)
					|| name.equals(UNSIGNED_SUBSTRACT)
					|| name.equals(UNSIGNED_MULTIPLY)
					|| name.equals(SIGNED_TO_UNSIGNED)) {
				return;
			}
		}

		for (ASTNode child : node.children()) {
			if (child != null) {
				if ((child instanceof CommonQuantifiedExpressionNode
						|| child instanceof CommonContractNode)) {
					// quantified nodes are not transformed.
					return;
				} else
					processIntegerOperation(child);
			}
		}

		if (node instanceof ExpressionNode) {
			ExpressionNode en = (ExpressionNode) node;
			Type convertedType = null;

			if (en.getInitialType() instanceof SignedIntegerType
					&& (convertedType = en
							.getConvertedType()) instanceof StandardUnsignedIntegerType
					&& !(((StandardUnsignedIntegerType) convertedType)
							.getIntKind() == UnsignedIntKind.BOOL)) {
				signedToUnsigned(en);
			}
		}

	}

	private void signedToUnsigned(ExpressionNode en) throws SyntaxException {
		if (en instanceof IntegerConstantNode) {
			IntegerConstantNode intNode = (IntegerConstantNode) en;
			BigInteger value = intNode.getConstantValue().getIntegerValue();
			int intBit = civlConfig.getIntBit();

			if (intBit < 32 && value.bitCount() < 32) {
				int intValue = value.intValue();
				int bound = (int) Math.pow(2, intBit);

				if (intValue < 0 || intValue >= bound) {
					intValue = intValue % bound;
					if (intValue < 0)
						intValue += bound;
					intNode.parent().setChild(intNode.childIndex(),
							this.integerConstant(intValue));

				}
				return;
			}
		}

		ASTNode parent = en.parent();
		int childIndex = en.childIndex();
		String funcName = SIGNED_TO_UNSIGNED;
		String method = funcName + "()";
		Source source = this.newSource(method, CivlcTokenConstant.CALL);
		List<ExpressionNode> args = new ArrayList<ExpressionNode>();

		en.remove();
		args.add(en);
		args.add(getBound());

		FunctionCallNode funcCallNode = functionCall(source, funcName, args);
		funcCallNode.setInitialType(en.getConvertedType());
		parent.setChild(childIndex, funcCallNode);
	}

	/**
	 * Transform unsigned arithmetic operations ({@link Operator#PLUS},
	 * {@link Operator#MINUS}, {@link Operator#TIMES}) into corresponding
	 * functions defined in {@link #UNSIGNED_ARITH_SOURCE_FILE}.
	 * 
	 * @param opn
	 *            the binary operator node.
	 * @throws SyntaxException
	 */
	private void processUnsignedArithNode(OperatorNode opn)
			throws SyntaxException {
		if (opn.getNumberOfArguments() != 2) {
			throw new CIVLSyntaxException(
					"plus , minus or substract operator can only have two operands.",
					opn.getSource());
		}
		ASTNode parent = opn.parent();
		int childIndex = opn.childIndex();
		Operator op = opn.getOperator();
		ExpressionNode operand1 = opn.getArgument(0);
		ExpressionNode operand2 = opn.getArgument(1);

		processIntegerOperation(operand1);
		processIntegerOperation(operand2);
		operand1 = opn.getArgument(0);
		operand2 = opn.getArgument(1);
		if (isUnsignedIntegerType(operand1.getConvertedType())
				&& isUnsignedIntegerType(operand2.getConvertedType())) {
			// construct a new functionCallNode.
			String funcName = "";

			switch (op) {
				case TIMES :
					funcName = UNSIGNED_MULTIPLY;
					break;
				case PLUS :
					funcName = UNSIGNED_ADD;
					break;
				case MINUS :
					funcName = UNSIGNED_SUBSTRACT;
					break;
				default :
					break;
			}
			String method = funcName + "()";
			Source source = this.newSource(method, CivlcTokenConstant.CALL);
			List<ExpressionNode> args = new ArrayList<ExpressionNode>();

			operand1.remove();
			operand2.remove();
			args.add(operand1);
			args.add(operand2);
			args.add(getBound());

			FunctionCallNode funcCallNode = functionCall(source, funcName,
					args);

			funcCallNode.setInitialType(opn.getConvertedType());
			parent.setChild(childIndex, funcCallNode);
		}
	}

	/**
	 * Transform {@link OperatorNode} with {@link Operator#DIV} and
	 * {@link Operator#MOD} into corresponding functions defined in
	 * {@link #INT_DIV_SOURCE_FILE}.
	 * 
	 * @param opn
	 *            the binary operator node.
	 * @throws SyntaxException
	 */
	private void processIntDivNode(OperatorNode opn) throws SyntaxException {
		if (opn.getNumberOfArguments() != 2) {
			throw new CIVLSyntaxException(
					"div or mod operator can only have two operands.",
					opn.getSource());
		}

		ASTNode parent = opn.parent();
		int childIndex = opn.childIndex();
		Operator op = opn.getOperator();
		ExpressionNode operand1 = opn.getArgument(0);
		ExpressionNode operand2 = opn.getArgument(1);

		// Constant division will not be transformed.
		if (operand1.expressionKind() == ExpressionKind.CONSTANT
				&& operand2.expressionKind() == ExpressionKind.CONSTANT) {
			return;
		}
		processIntegerOperation(operand1);
		processIntegerOperation(operand2);
		operand1 = opn.getArgument(0);
		operand2 = opn.getArgument(1);
		if (operand1.getConvertedType() instanceof IntegerType
				&& operand2.getConvertedType() instanceof IntegerType) {
			// construct a new functionCallNode.
			String funcName = op == Operator.DIV ? INT_DIV : INT_MOD;
			String method = op == Operator.DIV
					? INT_DIV + "()"
					: INT_MOD + "()";
			Source source = this.newSource(method, CivlcTokenConstant.CALL);
			List<ExpressionNode> args = new ArrayList<ExpressionNode>();

			operand1.remove();
			operand2.remove();
			args.add(operand1);
			args.add(operand2);

			FunctionCallNode funcCallNode = functionCall(source, funcName,
					args);

			funcCallNode.setInitialType(opn.getConvertedType());
			parent.setChild(childIndex, funcCallNode);
		}
	}

	/**
	 * This method will construct an AST from {@link #INT_DIV_SOURCE_FILE} ,
	 * then retrieve the declaration of function '$assert' and the definitions
	 * of functions '$int_div' and '$int_mod', then insert them to the top of
	 * the ast
	 * 
	 * @param ast
	 *            the root node of the AST into which the declarations are
	 *            inserted.
	 * @throws SyntaxException
	 *             when there are syntax error in {@link #INT_DIV_SOURCE_FILE}
	 */
	private void linkIntDivLibrary(SequenceNode<BlockItemNode> ast)
			throws SyntaxException {
		AST intDivLib;

		// if (check_division_by_zero)
		// intDivLib = this.parseSystemLibrary(new File(
		// CIVLConstants.CIVL_INCLUDE_PATH,
		// INT_DIV_NO_CHECKING_SOURCE_FILE), macros);
		// else
		intDivLib = this.parseSystemLibrary(
				new File(CIVLConstants.CIVL_INCLUDE_PATH, INT_DIV_SOURCE_FILE),
				macros);

		SequenceNode<BlockItemNode> root = intDivLib.getRootNode();
		List<BlockItemNode> funcDefinitions = new ArrayList<>();

		intDivLib.release();
		for (BlockItemNode child : root) {
			if (child instanceof FunctionDeclarationNode) {
				FunctionDeclarationNode function = (FunctionDeclarationNode) child;
				String name = function.getName();
				if (name.equals(INT_DIV) && this.divEntity == null
						|| name.equals(INT_MOD) && this.modEntity == null
						|| name.equals(REMAINDER) || name.equals(QUOTIENT)
						|| name.equals(ASSERT)) {
					child.remove();
					funcDefinitions.add(child);
				}
			}
		}
		ast.insertChildren(0, funcDefinitions);
	}

	/**
	 * Retrieve the function declarations from
	 * {@link #UNSIGNED_ARITH_SOURCE_FILE} and insert them to the top of the
	 * AST.
	 * 
	 * @param ast
	 *            the root node of the AST into which the declarations are
	 *            inserted.
	 * @throws SyntaxException
	 *             when there are syntax error in
	 *             {@link #UNSIGNED_ARITH_SOURCE_FILE}
	 */
	private void linkUnsignedArithLibrary(SequenceNode<BlockItemNode> ast)
			throws SyntaxException {
		AST unsignedArithLib = this
				.parseSystemLibrary(new File(CIVLConstants.CIVL_INCLUDE_PATH,
						UNSIGNED_ARITH_SOURCE_FILE), macros);
		SequenceNode<BlockItemNode> root = unsignedArithLib.getRootNode();
		List<BlockItemNode> funcDefinitions = new ArrayList<>();

		unsignedArithLib.release();
		for (BlockItemNode child : root) {
			if (child instanceof FunctionDeclarationNode) {
				FunctionDeclarationNode function = (FunctionDeclarationNode) child;
				String name = function.getName();

				if (name.equals(UNSIGNED_ADD) && this.unsignedAddEntity == null
						|| name.equals(UNSIGNED_SUBSTRACT)
								&& this.unsignedSubstractEntity == null
						|| name.equals(UNSIGNED_MULTIPLY)
								&& this.unsignedMultiplyEntity == null
						|| name.equals(SIGNED_TO_UNSIGNED)
								&& this.signedToUnsignedEntity == null
						|| name.equals(UNSIGNED_NEG)
								&& this.unsignedNegEntity == null) {
					child.remove();
					funcDefinitions.add(child);
				}
			}
		}
		ast.insertChildren(0, funcDefinitions);
	}

	/**
	 * 
	 * @return the uppper bound of integer.
	 * @throws SyntaxException
	 */
	private IntegerConstantNode getBound() throws SyntaxException {
		int numberOfBits = civlConfig.getIntBit();
		BigInteger bound;
		IntegerConstantNode boundNode = null;

		if (numberOfBits < 63) {
			bound = new BigInteger(Utils.myPower(2, numberOfBits) + "");
		} else {
			bound = Utils.myMathPower(2, numberOfBits);
		}

		String boundParameter = "int bound";
		Source boundSource = this.newSource(boundParameter,
				CivlcTokenConstant.PARAMETER_DECLARATION);

		boundNode = this.nodeFactory.newIntegerConstantNode(boundSource,
				bound.toString());
		return boundNode;
	}

}
