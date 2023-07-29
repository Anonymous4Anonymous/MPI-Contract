package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import java.util.HashMap;
import java.util.Map;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignAuxExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignExprIF.AssignExprKind;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignFieldExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignStoreExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignSubscriptExprIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignmentIF;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InsensitiveFlow;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InsensitiveFlowFactory;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNode;
import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.InvocationGraphNodeFactory;
import edu.udel.cis.vsl.abc.ast.entity.IF.Function;
import edu.udel.cis.vsl.abc.ast.entity.IF.Variable;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.FunctionDefinitionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.declaration.VariableDeclarationNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ConstantNode.ConstantKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IntegerConstantNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.FunctionTypeNode;
import edu.udel.cis.vsl.abc.ast.node.IF.type.TypeNode.TypeNodeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.ArrayType;
import edu.udel.cis.vsl.abc.ast.type.IF.Field;
import edu.udel.cis.vsl.abc.ast.type.IF.ObjectType;
import edu.udel.cis.vsl.abc.ast.type.IF.PointerType;
import edu.udel.cis.vsl.abc.ast.type.IF.StandardBasicType.BasicTypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;
import edu.udel.cis.vsl.abc.ast.type.IF.Type.TypeKind;
import edu.udel.cis.vsl.abc.ast.type.IF.TypeFactory;

/**
 * an implementation of {@link InsensitiveFlowFactory}
 * 
 * @author xxxx
 *
 */
public class CommonInsensitiveFlowFactory implements InsensitiveFlowFactory {

	/**
	 * a map from {@link AssignExprIF} to {@link AssignExprIF} for
	 * canonicalization
	 */
	private Map<AssignExprIF, AssignExprIF> allAssignExprs;

	/**
	 * the constant AssignExprIF that represents FULL
	 */
	private final AssignExprIF FULL;

	/**
	 * the AssignOffsetIF that represents a constant integer 0
	 */
	private AssignOffsetIF zeroOffset = null;

	/**
	 * the AssignOffsetIF that represents an arbitrary integer
	 */
	private AssignOffsetIF arbitraryOffset = null;

	/**
	 * a counter for all created AssignExprIFs
	 */
	private int assignExprCounter = 0;

	/**
	 * a reference to {@link InvocationGraphNodeFactory}
	 */
	private InvocationGraphNodeFactory ignFactory;

	/**
	 * a reference to {@link TypeFactory}
	 */
	private TypeFactory typeFactory;

	/**
	 * a simple implementation of {@link AssignmentIF}
	 */
	class CommonAssignment implements AssignmentIF {

		private AssignExprIF lhs, rhs;

		private AssignmentKind kind;

		CommonAssignment(AssignExprIF lhs, boolean lhsDeref, AssignExprIF rhs,
				boolean rhsDeref, boolean rhsAddrof) {
			assert !(rhsDeref && rhsAddrof);
			assert !lhsDeref || (!rhsDeref && !rhsAddrof);
			if (lhsDeref)
				kind = AssignmentKind.COMPLEX_LD;
			else if (rhsDeref)
				kind = AssignmentKind.COMPLEX_RD;
			else if (rhsAddrof)
				kind = AssignmentKind.BASE;
			else
				kind = AssignmentKind.SIMPLE;
			assert lhs != null;
			assert rhs != null;
			this.lhs = lhs;
			this.rhs = rhs;
		}

		@Override
		public AssignExprIF lhs() {
			return lhs;
		}

		@Override
		public AssignExprIF rhs() {
			return rhs;
		}

		@Override
		public AssignmentKind kind() {
			return kind;
		}

		@Override
		public String toString() {
			switch (kind) {
				case BASE :
					return this.lhs.toString() + " = &" + this.rhs.toString();
				case COMPLEX_LD :
					return "*" + this.lhs.toString() + " = "
							+ this.rhs.toString();
				case COMPLEX_RD :
					return this.lhs.toString() + " = *(" + this.rhs.toString()
							+ ")";
				case SIMPLE :
					return this.lhs.toString() + " = " + this.rhs.toString();
				default :
					return null;
			}
		}
	}

	/* ****************** Constructor ****************** */
	CommonInsensitiveFlowFactory(InvocationGraphNodeFactory igFactory,
			TypeFactory typeFactory) {
		this.FULL = new CommonAssignExpr(assignExprCounter++, null);
		this.allAssignExprs = new HashMap<>();
		this.ignFactory = igFactory;
		this.typeFactory = typeFactory;
	}

	@Override
	public AssignmentIF assignment(AssignExprIF lhs, boolean lhsDeref,
			AssignExprIF rhs, boolean rhsDeref, boolean rhsAddrof) {
		return new CommonAssignment(lhs, lhsDeref, rhs, rhsDeref, rhsAddrof);
	}

	@Override
	public AssignExprIF full() {
		return FULL;
	}

	@Override
	public InsensitiveFlow InsensitiveFlow(Function function,
			InvocationGraphNode igNode) {
		FunctionDefinitionNode funcDef = function.getDefinition();
		InsensitiveFlow result = new CommonInsensitiveFlow(funcDef.getBody(),
				funcDef.getBody().getScope(), this, this.ignFactory, igNode,
				typeFactory);
		// set formal parameters:
		FunctionTypeNode funcType = function.getDefinition().getTypeNode();
		AssignExprIF formals[] = new AssignExprIF[funcType.getParameters()
				.numChildren()];
		int i = 0;

		for (VariableDeclarationNode varDecl : function.getDefinition()
				.getTypeNode().getParameters()) {
			if (varDecl.getEntity() == null) {
				assert varDecl.getTypeNode().kind() == TypeNodeKind.VOID;
				assert formals.length == 1;
				formals = new AssignExprIF[0];
				break;
			}
			formals[i++] = assignStoreExpr(
					(Variable) varDecl.getIdentifier().getEntity());
		}
		igNode.setFormalParameters(formals);
		return result;
	}

	/* ******************* Creating AssignExprIFs ***********************/
	@Override
	public AssignStoreExprIF assignStoreExpr(Variable var) {
		AssignStoreExprIF result = new CommonAssignStoreExpr(
				assignExprCounter++, var, var.getType());
		AssignExprIF canonic = allAssignExprs.get(result);

		if (canonic != null)
			return (AssignStoreExprIF) canonic;

		allAssignExprs.put(result, result);
		return result;
	}

	@Override
	public AssignStoreExprIF assignStoreExpr(ExpressionNode store) {
		AssignStoreExprIF result;

		if (store.expressionKind() == ExpressionKind.CONSTANT) {
			// string literal:
			assert ((ConstantNode) store).constantKind() == ConstantKind.STRING;
			result = new CommonAssignStoreExpr(assignExprCounter++, store,
					store.getType());
		} else {
			// allocation:
			PointerType ptrType = (PointerType) store.getType();
			Type referredType = (Type) ptrType.referencedType();
			ObjectType objType;

			if (referredType instanceof ObjectType
					&& referredType.kind() != TypeKind.VOID)
				objType = (ObjectType) referredType;
			else
				objType = typeFactory.basicType(BasicTypeKind.CHAR);

			Type storeType = typeFactory.incompleteArrayType(objType);

			result = new CommonAssignStoreExpr(assignExprCounter++, store,
					storeType);
		}

		AssignExprIF canonic = allAssignExprs.get(result);

		if (canonic != null)
			return (AssignStoreExprIF) canonic;

		allAssignExprs.put(result, result);
		return result;
	}

	@Override
	public AssignFieldExprIF assignFieldExpr(AssignExprIF struct, Field field) {
		Type type = field.getType();
		AssignFieldExprIF result = new CommonAssignFieldExpr(
				assignExprCounter++, type, struct, field);
		AssignExprIF canonic = allAssignExprs.get(result);

		if (canonic != null)
			return (AssignFieldExprIF) canonic;

		allAssignExprs.put(result, result);
		return result;
	}

	@Override
	public AssignSubscriptExprIF assignSubscriptExpr(AssignExprIF array,
			AssignOffsetIF index) {
		Type type = array.type();

		if (type.kind() == TypeKind.ARRAY)
			type = ((ArrayType) type).getElementType();
		else
			type = ((PointerType) type).referencedType();

		AssignSubscriptExprIF result = new CommonAssignSubscriptExpr(
				assignExprCounter++, type, array, index);
		AssignExprIF canonic = allAssignExprs.get(result);

		if (canonic != null)
			return (AssignSubscriptExprIF) canonic;

		allAssignExprs.put(result, result);
		return result;
	}

	@Override
	public AssignOffsetExprIF assignOffsetExpr(AssignExprIF base,
			AssignOffsetIF offset) {
		AssignOffsetExprIF result;

		// invariant: the base of an AssignOffsetExprIF instance shall not have
		// OFFSET kind:
		if (base.kind() == AssignExprKind.OFFSET) {
			AssignOffsetExprIF offsetBase = (AssignOffsetExprIF) base;

			assert offsetBase.base().kind() != AssignExprKind.OFFSET;
			// (P + *) + c/* ==> P + *
			if (!offsetBase.offset().hasConstantValue())
				return offsetBase;
			// (P + c) + * ==> P + *
			if (!offset.hasConstantValue())
				return new CommonAssignOffsetExpr(assignExprCounter++,
						base.type(), offsetBase.base(), arbitraryOffset);

			Integer newOffsetVal = offsetBase.offset().constantValue()
					+ offset.constantValue();

			result = new CommonAssignOffsetExpr(assignExprCounter++,
					base.type(), offsetBase.base(),
					new CommonAssignOffset(newOffsetVal));
		} else
			result = new CommonAssignOffsetExpr(assignExprCounter++,
					base.type(), base, offset);

		AssignExprIF canonic = allAssignExprs.get(result);

		if (canonic != null)
			return (AssignOffsetExprIF) canonic;

		allAssignExprs.put(result, result);
		return result;
	}

	@Override
	public AssignAuxExprIF assignAuxExpr(Type type) {
		return new CommonAssignAuxExpr(assignExprCounter++, type);
	}

	@Override
	public AssignOffsetIF assignOffset(ExpressionNode offset,
			boolean positive) {
		if (offset.isConstantExpression()) {
			IntegerConstantNode constantNode = (IntegerConstantNode) offset;
			Integer val = constantNode.getConstantValue().getIntegerValue()
					.intValueExact();

			val = positive ? val : -val;
			return new CommonAssignOffset(val);
		}
		return assignOffsetWild();
	}

	@Override
	public AssignOffsetIF assignOffset(Integer val) {
		return new CommonAssignOffset(val);
	}

	@Override
	public AssignOffsetIF assignOffsetZero() {
		if (zeroOffset == null)
			zeroOffset = new CommonAssignOffset(0);
		return zeroOffset;
	}

	@Override
	public AssignOffsetIF assignOffsetWild() {
		if (arbitraryOffset == null)
			arbitraryOffset = new CommonAssignOffset();
		return arbitraryOffset;
	}
}
