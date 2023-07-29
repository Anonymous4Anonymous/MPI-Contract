package edu.udel.cis.vsl.civl.dynamic.immutable;

import edu.udel.cis.vsl.civl.dynamic.IF.DynamicMemoryLocationSet;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;

/**
 * An immutable pattern implementaion of {@Link DynamicWriteSet}
 * 
 * @author xxxx (Ziqing Luo)
 */
public class ImmutableDynamicMemoryLocationSet
		implements
			DynamicMemoryLocationSet {

	private final SymbolicExpression memValue;

	/**
	 * A unary operator that collects the references in the "memValue", which
	 * are referring to non-alive objects:
	 */
	private UnaryOperator<SymbolicExpression> collector;

	ImmutableDynamicMemoryLocationSet(SymbolicExpression memValue,
			UnaryOperator<SymbolicExpression> collector) {
		this.memValue = memValue;
		this.collector = collector;
	}

	/* ***************** public methods from DynamicWriteSet *****************/
	@Override
	public SymbolicExpression getMemValue() {
		return memValue;
	}

	@Override
	public ImmutableDynamicMemoryLocationSet apply(
			UnaryOperator<SymbolicExpression> operator) {
		SymbolicExpression newMemValue = operator.apply(memValue);

		newMemValue = collector.apply(newMemValue);
		if (newMemValue != memValue)
			return new ImmutableDynamicMemoryLocationSet(newMemValue,
					collector);
		else
			return this;
	}

	/* ***************** Public methods from Objects ******************* */

	@Override
	public String toString() {
		return "MemLocSet{" + memValue + "}";
	}

	@Override
	public int hashCode() {
		return memValue.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ImmutableDynamicMemoryLocationSet) {
			ImmutableDynamicMemoryLocationSet other = (ImmutableDynamicMemoryLocationSet) obj;

			return other.getMemValue().equals(memValue);
		}
		return false;
	}
}
