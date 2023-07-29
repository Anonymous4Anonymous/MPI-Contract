package edu.udel.cis.vsl.abc.analysis.pointsTo.common;

import edu.udel.cis.vsl.abc.analysis.pointsTo.IF.AssignOffsetIF;

public class CommonAssignOffset implements AssignOffsetIF {

	private Integer constantValue;

	private boolean hasConstantValue;

	CommonAssignOffset(Integer constantValue) {
		this.hasConstantValue = true;
		this.constantValue = constantValue;
	}

	CommonAssignOffset() {
		this.hasConstantValue = false;
		this.constantValue = null;
	}

	@Override
	public Integer constantValue() {
		return constantValue;
	}

	@Override
	public boolean hasConstantValue() {
		return hasConstantValue;
	}

	@Override
	public String toString() {
		if (this.hasConstantValue())
			return constantValue.toString();
		else
			return "*";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CommonAssignOffset) {
			CommonAssignOffset that = (CommonAssignOffset) obj;

			if (that.hasConstantValue() != hasConstantValue())
				return false;
			if (hasConstantValue())
				return this.constantValue().equals(that.constantValue());
			else
				return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		if (hasConstantValue())
			return constantValue >= 0
					? constantValue
					: 32767 * Math.abs(constantValue);
		else
			return -1;
	}
}
