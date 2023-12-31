/**
 * 
 */
package edu.udel.cis.vsl.civl.model.common.expression;

import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.Scope;
import edu.udel.cis.vsl.civl.model.IF.expression.ConditionalExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.DotExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;
import edu.udel.cis.vsl.civl.model.IF.expression.LHSExpression;
import edu.udel.cis.vsl.civl.model.IF.expression.VariableExpression;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLSetType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.variable.Variable;

/**
 * @author zirkel
 * 
 */
public class CommonDotExpression extends CommonExpression
		implements
			DotExpression {

	private Expression structOrUnion;// TODO shall this be of type
										// LHSExpression?
	private int fieldIndex;

	/**
	 * A dot expression is a reference to a field in a struct.
	 * 
	 * @param struct
	 *            The struct referenced by this dot expression.
	 * @param field
	 *            The field referenced by this dot expression.
	 */
	public CommonDotExpression(CIVLSource source, Expression struct,
			int fieldIndex, CIVLType expressionType) {
		super(source, struct.expressionScope(), struct.lowestScope(),
				expressionType);
		assert struct.getExpressionType().isStructType()
				|| struct.getExpressionType().isUnionType()
				|| struct.getExpressionType().isSetType();

		assert struct.getExpressionType().isSetType()
				? ((CIVLSetType) struct.getExpressionType()).elementType()
						.isStructType()
						|| ((CIVLSetType) struct.getExpressionType())
								.elementType().isUnionType()
				: true;
		this.structOrUnion = struct;
		this.fieldIndex = fieldIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.udel.cis.vsl.civl.model.IF.expression.DotExpression#struct()
	 */
	@Override
	public Expression structOrUnion() {
		return structOrUnion;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.udel.cis.vsl.civl.model.IF.expression.DotExpression#field()
	 */
	@Override
	public int fieldIndex() {
		return fieldIndex;
	}

	@Override
	public String toString() {
		return "(" + structOrUnion.toString() + ")." + fieldIndex;
	}

	@Override
	public ExpressionKind expressionKind() {
		return ExpressionKind.DOT;
	}

	@Override
	public void calculateDerefs() {
		this.structOrUnion.calculateDerefs();
		this.hasDerefs = this.structOrUnion.hasDerefs();
	}

	@Override
	public void setPurelyLocal(boolean pl) {
		// TODO what if &(a.index) where a is defined as a struct
		// and index is a field of the struct
	}

	@Override
	public void purelyLocalAnalysisOfVariables(Scope funcScope) {
		this.structOrUnion.purelyLocalAnalysisOfVariables(funcScope);
		if (funcScope.isDescendantOf(this.expressionScope()))
			this.setPurelyLocal(false);
	}

	@Override
	public void purelyLocalAnalysis() {
		if (this.hasDerefs) {
			this.purelyLocal = false;
			return;
		}
		// if (this.expressionType.isPointerType()
		// || this.expressionType.isHandleType()) {
		// this.setPurelyLocal(false);
		// return;
		// }
		this.structOrUnion.purelyLocalAnalysis();
		this.purelyLocal = this.structOrUnion.isPurelyLocal();
	}

	@Override
	public void replaceWith(ConditionalExpression oldExpression,
			VariableExpression newExpression) {
		if (structOrUnion == oldExpression) {
			structOrUnion = newExpression;
			return;
		}
		structOrUnion.replaceWith(oldExpression, newExpression);
	}

	@Override
	public Expression replaceWith(ConditionalExpression oldExpression,
			Expression newExpression) {
		Expression newStruct = structOrUnion.replaceWith(oldExpression,
				newExpression);
		CommonDotExpression result = null;

		if (newStruct != null) {
			result = new CommonDotExpression(this.getSource(), newStruct,
					this.fieldIndex, this.getExpressionType());
		}
		return result;
	}

	@Override
	public Variable variableWritten(Scope scope) {
		if (structOrUnion instanceof LHSExpression) {
			return ((LHSExpression) structOrUnion).variableWritten(scope);
		}
		return null;
	}

	@Override
	public Variable variableWritten() {
		if (structOrUnion instanceof LHSExpression) {
			return ((LHSExpression) structOrUnion).variableWritten();
		}
		return null;
	}

	@Override
	public Set<Variable> variableAddressedOf(Scope scope) {
		Set<Variable> variableSet = new HashSet<>();
		Set<Variable> operandResult = structOrUnion.variableAddressedOf(scope);

		if (operandResult != null)
			variableSet.addAll(operandResult);
		return variableSet;
	}

	@Override
	public Set<Variable> variableAddressedOf() {
		Set<Variable> variableSet = new HashSet<>();
		Set<Variable> operandResult = structOrUnion.variableAddressedOf();

		if (operandResult != null)
			variableSet.addAll(operandResult);
		return variableSet;
	}

	@Override
	public boolean isStruct() {
		CIVLType type = structOrUnion.getExpressionType();

		if (type.isSetType())
			type = ((CIVLSetType) type).elementType();
		return type.isStructType();
	}

	@Override
	public boolean isUnion() {
		CIVLType type = structOrUnion.getExpressionType();

		if (type.isSetType())
			type = ((CIVLSetType) type).elementType();
		return type.isUnionType();
	}

	@Override
	public LHSExpressionKind lhsExpressionKind() {
		return LHSExpressionKind.DOT;
	}

	@Override
	protected boolean expressionEquals(Expression expression) {
		DotExpression that = (DotExpression) expression;

		return this.fieldIndex == that.fieldIndex()
				&& this.structOrUnion.equals(that.structOrUnion());
	}

	@Override
	public boolean containsHere() {
		return this.structOrUnion.containsHere();
	}

}
