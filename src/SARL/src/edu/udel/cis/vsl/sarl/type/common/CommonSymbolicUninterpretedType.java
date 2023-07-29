package edu.udel.cis.vsl.sarl.type.common;

import java.util.function.Function;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicUninterpretedType;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;

public class CommonSymbolicUninterpretedType extends CommonSymbolicType
		implements SymbolicUninterpretedType {

	/**
	 * An extractor which extracts the key value from a symbolic expression of
	 * an uninterpreted type. If a symbolic expression of an uninterpreted type
	 * is non-concrete, the extraction may fail and return a java null.
	 * 
	 * @author xxxx
	 */
	private class Locksmith implements Function<SymbolicExpression, IntObject> {
		@Override
		public IntObject apply(SymbolicExpression t) {
			if (t.operator() == SymbolicOperator.CONCRETE)
				return (IntObject) t.argument(0);
			return null;
		}
	}

	/**
	 * An instance which converts a symbolic expression of uninterpreted type to
	 * its key.
	 */
	private Locksmith locksmith = null;

	/**
	 * a constant to store the hashCode of this object, so that it will be
	 * calculated once and saved.
	 */
	private final static int classCode = CommonSymbolicArrayType.class
			.hashCode();

	/**
	 * Name of this uninterpreted function
	 */
	private StringObject name;

	CommonSymbolicUninterpretedType(StringObject name) {
		super(SymbolicTypeKind.UNINTERPRETED);
		this.name = name;
	}

	@Override
	public StringBuffer toStringBuffer(boolean atomize) {
		StringBuffer result = new StringBuffer();

		result.append(name.toStringBuffer(atomize) + "_t");
		return result;
	}

	@Override
	public boolean containsQuantifier() {
		return false;
	}

	@Override
	public StringObject name() {
		return name;
	}

	@Override
	protected boolean typeEquals(CommonSymbolicType that) {
		if (that instanceof SymbolicUninterpretedType)
			return ((SymbolicUninterpretedType) that).name() == name;
		return false;
	}

	@Override
	public SymbolicType getPureType() {
		return this;
	}

	@Override
	protected void canonizeChildren(ObjectFactory factory) {
		name = factory.canonic(name);
	}

	@Override
	protected int computeHashCode() {
		return name.hashCode() ^ classCode;
	}

	@Override
	public Function<SymbolicExpression, IntObject> soleSelector() {
		if (locksmith == null)
			locksmith = new Locksmith();
		return locksmith;
	}

	@Override
	public boolean containsSubobject(SymbolicObject obj) {
		return this == obj;
	}
}
