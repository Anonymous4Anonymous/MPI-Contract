package edu.udel.cis.vsl.abc.ast.conversion.common;

import edu.udel.cis.vsl.abc.ast.conversion.IF.MemConversion;
import edu.udel.cis.vsl.abc.ast.type.IF.MemType;
import edu.udel.cis.vsl.abc.ast.type.IF.Type;

public class CommonMemoryConversion extends CommonConversion
		implements
			MemConversion {

	public CommonMemoryConversion(Type oldType, MemType newType) {
		super(oldType, newType);
	}

	@Override
	public MemType getNewType() {
		return (MemType) super.getNewType();
	}

	@Override
	public String toString() {
		return "Memory" + super.toString();
	}

	@Override
	public ConversionKind conversionKind() {
		return ConversionKind.MEM;
	}
}
