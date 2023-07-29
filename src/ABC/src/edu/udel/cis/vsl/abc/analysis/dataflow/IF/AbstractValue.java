package edu.udel.cis.vsl.abc.analysis.dataflow.IF;

/**
 * This class is the representation of abstract values. It is the basic general type
 * of value types used in different analysis. e.g. Interval, Integer.
 *  
 * This class defines a series of common methods that are used for the calculation
 * of the same abstract value type.
 * 
 * 
 *          
 * @author dxu
 */

public abstract class AbstractValue {
	
	public abstract AbstractValue top();
	
	public AbstractValue plus(AbstractValue leftValue, AbstractValue rightValue) {
		return leftValue.top();
	};

	public AbstractValue minus(AbstractValue leftValue, AbstractValue rightValue) {
		return leftValue.top();
	};

	public AbstractValue multiply(AbstractValue leftValue, AbstractValue rightValue) {
		return leftValue.top();
	};

	public AbstractValue divide(AbstractValue leftValue, AbstractValue rightValue) {
		return leftValue.top();
	};
	
	public AbstractValue union(AbstractValue leftValue, AbstractValue rightValue){
		return leftValue.top();
	}

	public abstract AbstractValue setValue(long value);
	
	public abstract boolean equals(Object obj);
	
	public abstract int hashCode();
	
}