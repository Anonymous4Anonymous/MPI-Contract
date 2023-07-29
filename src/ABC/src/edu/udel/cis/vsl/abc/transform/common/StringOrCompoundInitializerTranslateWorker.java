package edu.udel.cis.vsl.abc.transform.common;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;

import edu.udel.cis.vsl.abc.ast.node.IF.NodeFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.PairNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundInitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.CompoundLiteralObject;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.DesignationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.LiteralObject;
import edu.udel.cis.vsl.abc.ast.node.IF.compound.ScalarLiteralObject;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.InitializerNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.StringLiteralNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.BlockItemNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.DeclarationListNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode;
import edu.udel.cis.vsl.abc.ast.type.IF.ArrayType;
import edu.udel.cis.vsl.abc.ast.type.IF.AtomicType;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.QualifiedObjectType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardSignedIntegerType.SignedIntKind;
import edu.udel.cis.vsl.abc.ast.type.IF.StructureOrUnionType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.TypeFactory;
import edu.udel.cis.vsl.abc.ast.type.IF.UnqualifiedObjectType;
import edu.udel.cis.vsl.abc.ast.value.IF.StringValue;
import edu.udel.cis.vsl.abc.ast.value.IF.Value;
import edu.udel.cis.vsl.abc.ast.value.IF.ValueFactory.Answer;
import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.token.IF.Source;
import edu.udel.cis.vsl.abc.token.IF.StringLiteral;

/**
 * <p>
 * This class translates a {@link CompoundInitializerNode} and an expression,
 * which represents the object that is associated to the compound initializer,
 * to a sequence of assignments.
 * </p>
 * 
 * <p>
 * <b>Foe example</b> expression : <code>e : struct</code> ,initializer :
 * <code>{a, b, c}</code>, the translation result is
 * <code>e.0 = a; e.1 = b; e.2 = c;</code>
 * </p>
 * 
 * <p>
 * Notes:
 * <ul>
 * <li>This class <b>ASSUMES</b> that the aggregate object that is associated
 * with the compound initializer has been set to have its default value if it
 * has or is suppose to be treated as having static storage.</li>
 * <li>This class guarantees that the output contains no compound initializer
 * node <b>EXCEPT FOR</b> $domain initializers</li>
 * <li>TODO: this worker currently is used by the {@link SideEffectRemover} due
 * to the fact that side-effect remover has a mature structure for traversing a
 * tree and factoring statements from expressions. Semantically, this is not a
 * "side-effect removing" work.</li>
 * </ul>
 * </p>
 * 
 * @author xxxx
 *
 * 
 */
class StringOrCompoundInitializerTranslateWorker {

	private NodeFactory nodeFactory;

	private BiFunction<Source, Type, VariableDeclarationNode> tmpVarCreator;

	private BiFunction<Source, Type, TypeNode> typeNodeCreator;

	private Language language;

	// constructor
	StringOrCompoundInitializerTranslateWorker(NodeFactory nodeFactory,
			BiFunction<Source, Type, VariableDeclarationNode> tmpVarCreator,
			BiFunction<Source, Type, TypeNode> typeNodeCreator,
			Language language) {
		this.nodeFactory = nodeFactory;
		this.tmpVarCreator = tmpVarCreator;
		this.typeNodeCreator = typeNodeCreator;
		this.language = language;
	}

	/**
	 * <p>
	 * translates a {@link CompoundInitializerNode} and an expression, which
	 * represents the object that is associated to the compound initializer, to
	 * a sequence of assignments. Note that the assignments are NOT guaranteed
	 * side-effect free.
	 * </p>
	 * 
	 * @param compound
	 *            a compound initializer
	 * @param objExpr
	 *            an expression that represents the object that will be
	 *            initialized by the compound initializer
	 * @return a list of assignments which deliver the same functionality as the
	 *         compound initializer
	 */
	List<BlockItemNode> translateCompoundInitializer(
			CompoundInitializerNode compound, ExpressionNode objExpr) {
		LiteralObject lt = compound.getLiteralObject();

		return translateInitializerWorker(objExpr, lt);
	}

	/**
	 * <p>
	 * Given an char array expression and a string literal, translates the
	 * string literal to a sequence of assignments to the array. Note that the
	 * assignments are NOT guaranteed side-effect free.
	 * </p>
	 * 
	 * <p>
	 * Assuming the array has assigned default value '0's
	 * </p>
	 * 
	 * @param stringLit
	 *            a string literal node
	 * @param lhs
	 *            an expression node of array of char type
	 * @return a list of assignment expressions
	 */
	List<BlockItemNode> translateStringLiteralInitializer(
			StringLiteralNode stringLitNode, ExpressionNode lhs) {
		StringValue strVal = stringLitNode.getConstantValue();
		StringLiteral strLit = strVal.getLiteral();

		return translateStringLiteralInitializerWorker(lhs,
				stringLitNode.getType(), strLit, stringLitNode.getSource());
	}

	private List<BlockItemNode> translateInitializerWorker(ExpressionNode obj,
			LiteralObject litObj) {
		List<BlockItemNode> results = new LinkedList<>();

		if (litObj instanceof CompoundLiteralObject) {
			// compound:
			CompoundLiteralObject compoundLitObj = (CompoundLiteralObject) litObj;
			int size = compoundLitObj.size();
			Source source = obj.getSource();
			Type dequalifiedType = litObj.getType();

			if (dequalifiedType.kind() == TypeKind.QUALIFIED)
				dequalifiedType = ((QualifiedObjectType) dequalifiedType)
						.getBaseType();
			if (dequalifiedType.kind() == TypeKind.ATOMIC)
				dequalifiedType = ((AtomicType) dequalifiedType).getBaseType();
			if (dequalifiedType.kind() == TypeKind.ARRAY)
				for (int i = 0; i < size; i++) {
					ExpressionNode subObj = nodeFactory.newOperatorNode(source,
							Operator.SUBSCRIPT, obj.copy(),
							nodeFactory.newIntConstantNode(source, i));

					results.addAll(translateInitializerWorker(subObj,
							compoundLitObj.get(i)));
				}
			else if (dequalifiedType.kind() == TypeKind.STRUCTURE_OR_UNION) {
				StructureOrUnionType type = (StructureOrUnionType) litObj
						.getType();
				int i = 0;

				if (type.isUnion())
					i = size - 1;
				for (; i < size; i++) {
					ExpressionNode subObj = nodeFactory.newDotNode(source,
							obj.copy(), nodeFactory.newIdentifierNode(source,
									type.getField(i).getName()));

					results.addAll(translateInitializerWorker(subObj,
							compoundLitObj.get(i)));
				}
			}
		} else {
			// scalar (including $domain type):
			ExpressionNode init = ((ScalarLiteralObject) litObj)
					.getExpression();

			init.remove();
			// no need to assign 0s to scalar, which should have been dealt with
			// by its default value:
			if (init.expressionKind() == ExpressionKind.CONSTANT) {
				Value val = ((ConstantNode) init).getConstantValue();

				if (val != null && val.getType().kind() == TypeKind.BASIC)
					if (val.isZero() == Answer.YES)
						return results;
				if (val instanceof StringValue)
					return translateStringLiteralInitializerWorker(obj,
							litObj.getType(), ((StringValue) val).getLiteral(),
							init.getSource());
			}
			results.add(nodeFactory.newExpressionStatementNode(
					nodeFactory.newOperatorNode(init.getSource(),
							Operator.ASSIGN, obj.copy(), init)));
		}
		return results;
	}

	/**
	 * <p>
	 * Translates a string literal initialization. There are two cases:
	 * <ul>
	 * <li>If the initialization has the form:
	 * <code>char * obj = string-literal</code>, the translation will be as
	 * follows: <code>
	 * char tmp[size(string-literal)];
	 * 
	 * // replacing "obj" with tmp then recursively call this method ...
	 * obj = tmp;
	 * </code></li>
	 * <li>If the initialization has the form:
	 * <code>char obj[c] = string-literal</code>, where <code>c</code> is either
	 * a fixed length or absent, the translation will be as follows: <code>
	 * obj[0] = string-literal[0];
	 * obj[1] = string-literal[1];
	 * ...
	 * </code>, where <code>string-literal[i]</code> means the i-th character in
	 * the given string literal.</li>
	 * </ul>
	 * 
	 * </p>
	 * 
	 * @param obj
	 *            the object that will be initialized, suppose to either have
	 *            array-of-char type or pointer-to-char type but this node may
	 *            not have been assigned type
	 * @param stringLiteralType
	 *            the type of the string literal expression node. The type of
	 *            this expression after applying all conversions reflects the
	 *            type of the "obj"
	 * @param strlit
	 *            the {@link StringLiteral} value of the string literal
	 *            expression
	 * @param strLitSource
	 *            the {@link Source} of the string literal expression
	 * @return a list of translated statements
	 */
	private List<BlockItemNode> translateStringLiteralInitializerWorker(
			ExpressionNode obj, Type stringLiteralType, StringLiteral strlit,
			Source strLitSource) {
		List<BlockItemNode> results = new LinkedList<>();
		ExpressionNode newInit;

		// at this point, "obj" may not have type but the type of the literal
		// object reflects whether the object has array-of-char type or
		// pointer-to-char type:
		if (stringLiteralType.isScalar()) {
			TypeFactory tf = nodeFactory.typeFactory();
			ArrayType charArrayType = tf.arrayType(
					tf.basicType(BasicTypeKind.CHAR),
					nodeFactory.getValueFactory().integerValue(
							tf.signedIntegerType(SignedIntKind.INT),
							strlit.getNumCharacters()));
			VariableDeclarationNode tmpVarDecl = tmpVarCreator
					.apply(strLitSource, charArrayType);

			results.add(tmpVarDecl);
			newInit = nodeFactory.newIdentifierExpressionNode(strLitSource,
					tmpVarDecl.getIdentifier().copy());
			results.addAll(translateStringLiteralInitializerWorker(newInit,
					charArrayType, strlit, strLitSource));
			results.add(nodeFactory.newExpressionStatementNode(
					nodeFactory.newOperatorNode(newInit.getSource(),
							Operator.ASSIGN, obj.copy(), newInit.copy())));
		} else {
			assert stringLiteralType.kind() == TypeKind.ARRAY;
			StringLiteral strLit = strlit;
			int size = strLit.getNumCharacters();

			for (int i = 0; i < size; i++) {
				ExpressionNode subObj = nodeFactory.newOperatorNode(
						strLitSource, Operator.SUBSCRIPT, obj.copy(),
						nodeFactory.newIntConstantNode(strLitSource, i));

				newInit = nodeFactory.newOperatorNode(strLitSource,
						Operator.ASSIGN, subObj,
						nodeFactory.newCharacterConstantNode(strLitSource,
								strLit.getCharacter(i).rawString(),
								strLit.getCharacter(i)));
				results.add(nodeFactory.newExpressionStatementNode(newInit));
			}
		}
		return results;
	}

	/* *************** methods for setting default values **************** */
	/**
	 * <p>
	 * Assigns a default value to an object as if the object has static storage
	 * </p>
	 * 
	 * @param obj
	 *            the object that will be assigned
	 * @param objType
	 *            the type of the object
	 * @return a triple that either 1) only contains "after" statements (i.e. no
	 *         expression, no before statements); 2) before statements,
	 *         translated initializer and after statements.
	 */
	ExprTriple defaultValues(ExpressionNode obj, Type objType) {
		Source source = obj.getSource();

		// de-qualifiers:
		if (objType.kind() == TypeKind.QUALIFIED)
			objType = ((QualifiedObjectType) objType).getBaseType();
		if (objType.isScalar())
			return new ExprTriple(defaultValueOfScalarType(objType, source));
		else {
			if (objType.kind() == TypeKind.ARRAY)
				return defaultValuesToArray(obj, (ArrayType) objType,
						obj.getSource());
			else if (objType.kind() == TypeKind.STRUCTURE_OR_UNION) {
				ExprTriple result = new ExprTriple(null);

				result.addAllAfter(defaultValuesToStructOrUnion(obj,
						(StructureOrUnionType) objType));
				return result;
			} else {
				TypeKind kind = objType.kind();

				if (kind == TypeKind.DOMAIN || kind == TypeKind.RANGE) {
					ConstantNode zero = nodeFactory.newIntConstantNode(source,
							0);
					ExpressionNode r = nodeFactory.newRegularRangeNode(source,
							zero, zero.copy());

					if (kind == TypeKind.DOMAIN) {
						List<PairNode<DesignationNode, InitializerNode>> initList = new LinkedList<>();
						TypeNode domainTypeNode = nodeFactory
								.newDomainTypeNode(source);

						initList.add(nodeFactory.newPairNode(source, null, r));
						r = nodeFactory.newCompoundLiteralNode(source,
								domainTypeNode,
								nodeFactory.newCompoundInitializerNode(source,
										initList));
					}
					return new ExprTriple(r);
				}
				throw new ABCRuntimeException(
						"Unexpected aggregate type kind: " + kind);
			}
		}
	}

	/**
	 * <p>
	 * This method returns two kinds of default values for arrays (as if the
	 * arrays have static storage):
	 * <ul>
	 * <li>If the current language is CIVL-C language, default values of arrays
	 * are array lambdas, which can be used to initialize array with either
	 * constant or variable length.</li>
	 * <li>Otherwise, to conform C11 standard, no initializer expression but a
	 * sequence of assignments will be given for initialization of arrays with
	 * constant length. Attemptence to initialize variable size array will be
	 * reported.</li>
	 * </ul>
	 * </p>
	 *
	 * @param array
	 *            an array object
	 * @param arrType
	 *            the array type of the object
	 * @return triple that contains an array lambda expression that is
	 *         representing the default value with a sequence of "before"
	 *         statements; no "after" statement.
	 */
	private ExprTriple defaultValuesToArray(ExpressionNode arr,
			ArrayType arrType, Source source) {
		if (language == Language.CIVL_C)
			return defaultValuesToArrayLambda(arrType, source);
		else
			return defaultValuesToArrayStrict(arr, arrType, source);
	}

	/**
	 * worker method of {@link #defaultValuesToArray(ArrayType, Source)} for
	 * creating array lambda kind default value
	 */
	private ExprTriple defaultValuesToArrayLambda(ArrayType arrType,
			Source source) {
		ExprTriple result;
		Type baseType = arrType;
		ExpressionNode elementDefaultVal;
		int dims = arrType.getDimension();
		List<VariableDeclarationNode> varDecls = new LinkedList<>();
		List<BlockItemNode> before = new LinkedList<BlockItemNode>();

		for (int i = 0; i < dims; i++) {
			baseType = ((ArrayType) baseType).getElementType();
			varDecls.add(nodeFactory.newVariableDeclarationNode(source,
					nodeFactory.newIdentifierNode(source, "i" + i),
					nodeFactory.newBasicTypeNode(source, BasicTypeKind.INT)));
		}
		if (!baseType.isScalar()) {
			VariableDeclarationNode tmpVar = tmpVarCreator.apply(source,
					baseType);
			ExprTriple subResult;

			elementDefaultVal = nodeFactory.newIdentifierExpressionNode(source,
					tmpVar.getIdentifier().copy());
			subResult = defaultValues(elementDefaultVal, baseType);
			if (subResult != null) {
				before.addAll(subResult.getBefore());
				before.add(tmpVar);
				if (subResult.getNode() != null)
					tmpVar.setInitializer(subResult.getNode());
				before.addAll(subResult.getAfter());
			} else
				before.add(tmpVar);
		} else
			elementDefaultVal = defaultValueOfScalarType(
					(UnqualifiedObjectType) baseType, source);

		ExpressionNode arrLambda = nodeFactory.newArrayLambdaNode(source,
				typeNodeCreator.apply(source, arrType), varDecls, null,
				elementDefaultVal.copy());

		result = new ExprTriple(arrLambda);
		result.addAllBefore(before);
		return result;
	}

	/**
	 * worker method of {@link #defaultValuesToArray(ArrayType, Source)} for
	 * creating default value strictly conforming C11 standard
	 */
	private ExprTriple defaultValuesToArrayStrict(ExpressionNode arr,
			ArrayType arrType, Source source) {
		assert !arrType
				.isVariableLengthArrayType() : "Initializer cannot be used to initialize variable "
						+ "length array in C language.\nNote that CIVL-C programs, "
						+ "whose source files end with suffix \".cvl\", support such feature.";
		assert arrType.isComplete();

		Type elementType = arrType.getElementType();
		ExpressionNode elementDefaultVal;
		List<BlockItemNode> result = new LinkedList<>();

		if (!elementType.isScalar()) {
			VariableDeclarationNode tmpVar = tmpVarCreator.apply(source,
					elementType);
			ExprTriple subResult;

			elementDefaultVal = nodeFactory.newIdentifierExpressionNode(source,
					tmpVar.getIdentifier().copy());
			subResult = defaultValues(elementDefaultVal, elementType);
			assert subResult.getNode() == null;
			assert subResult.getBefore().isEmpty();
			result.add(tmpVar);
			result.addAll(subResult.getAfter());
		} else {
			elementDefaultVal = nodeFactory.newIntConstantNode(source, 0);
		}

		// make a for-loop to initialize the array:
		int size = arrType.getConstantSize().getIntegerValue().intValueExact();
		ExpressionNode sizeNode = nodeFactory.newIntConstantNode(source, size);
		VariableDeclarationNode decl = tmpVarCreator.apply(source,
				nodeFactory.typeFactory().basicType(BasicTypeKind.INT));
		List<VariableDeclarationNode> loopVarDecls = new LinkedList<>();
		DeclarationListNode forLoopInit;
		ExpressionNode forLoopCond, forLoopId, forLoopInc;
		StatementNode forLoopBody;

		decl.setInitializer(nodeFactory.newIntConstantNode(source, 0));
		loopVarDecls.add(decl);
		forLoopId = nodeFactory.newIdentifierExpressionNode(source,
				decl.getIdentifier().copy());
		forLoopInit = nodeFactory.newForLoopInitializerNode(source,
				loopVarDecls);
		forLoopCond = nodeFactory.newOperatorNode(source, Operator.LT,
				forLoopId, sizeNode);
		forLoopInc = nodeFactory.newOperatorNode(source, Operator.PLUS,
				forLoopId.copy(), nodeFactory.newIntConstantNode(source, 1));
		forLoopInc = nodeFactory.newOperatorNode(source, Operator.ASSIGN,
				forLoopId.copy(), forLoopInc);
		forLoopBody = nodeFactory.newExpressionStatementNode(
				nodeFactory.newOperatorNode(source, Operator.ASSIGN,
						nodeFactory.newOperatorNode(source, Operator.SUBSCRIPT,
								arr.copy(), forLoopId.copy()),
						elementDefaultVal.copy()));
		result.add(nodeFactory.newForLoopNode(source, forLoopInit, forLoopCond,
				forLoopInc, forLoopBody, null));

		ExprTriple ret = new ExprTriple(null);

		ret.setAfter(result);
		return ret;
	}

	/**
	 * <p>
	 * Assigns a default value to an struct or union object as if the object has
	 * static storage.
	 * </p>
	 * 
	 * @param obj
	 *            a struct or union object
	 * @param objType
	 *            the type of the struct or union object
	 * @return a list of assignment statements that goes "after" the first
	 *         appearance of the given object
	 */
	private List<BlockItemNode> defaultValuesToStructOrUnion(ExpressionNode obj,
			StructureOrUnionType objType) {
		int numFields = objType.getNumFields();
		Source source = obj.getSource();
		List<BlockItemNode> results = new LinkedList<>();

		if (objType.isUnion())
			numFields = 1;
		for (int i = 0; i < numFields; i++) {
			Field field = objType.getField(i);
			ExpressionNode fieldObj;
			String fieldName = field.getName();

			/*
			 * C11 sec 6.7.9 semantics 9: Except where explicitly stated
			 * otherwise, for the purposes of this subclause unnamed members of
			 * objects of structure and union type do not participate in
			 * initialization. Unnamed members of structure objects have
			 * indeterminate value even after initialization.
			 */
			if (fieldName == null)
				continue;
			fieldObj = nodeFactory.newDotNode(source, obj.copy(),
					nodeFactory.newIdentifierNode(source, fieldName));

			Type fieldType = objType.getField(i).getType();
			ExprTriple subResult = defaultValues(fieldObj, fieldType);

			if (subResult != null) {
				results.addAll(subResult.getBefore());
				results.addAll(subResult.getAfter());
				if (subResult.getNode() != null)
					results.add(nodeFactory.newExpressionStatementNode(
							nodeFactory.newOperatorNode(source, Operator.ASSIGN,
									fieldObj.copy(), subResult.getNode())));
			}
		}
		return results;
	}

	/**
	 * <p>
	 * returns an expression that represents default value of a scalar type
	 * object as if the object has static storage
	 * </p>
	 * 
	 * @param scalarType
	 *            the scalar type
	 * @param source
	 *            the {@link Source} that is associated to the returned
	 *            expression
	 * @return an expression that represents default value of a scalar type
	 *         object as if the object has static storage
	 */
	private ExpressionNode defaultValueOfScalarType(Type scalarType,
			Source source) {
		switch (scalarType.kind()) {
			case BASIC :
			case ENUMERATION :
			case OTHER_INTEGER :
				return nodeFactory.newIntConstantNode(source, 0);
			case POINTER :
			case RANGE :
			case SCOPE : {
				return nodeFactory.newCastNode(source,
						typeNodeCreator.apply(source, scalarType),
						nodeFactory.newIntConstantNode(source, 0));
			}
			case PROCESS :
				return nodeFactory.newProcnullNode(source);
			case STATE :
				return nodeFactory.newStatenullNode(source);
			default :
				throw new ABCRuntimeException(
						"unexpected scalar type kind for inferring its default value : "
								+ scalarType.kind());
		}
	}
}
