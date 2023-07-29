package edu.udel.cis.vsl.sarl.simplify.eval;

import java.math.BigInteger;

/**
 * A node representing a power operation: a base raised to some fixed power.
 * This node has one child, the base. The exponent is a constant number, so a
 * field in this node.
 * 
 * @author xxxx
 */
class EvalNodeIntPow extends EvalNodeInt {
	private EvalNodeInt base;
	protected BigInteger exponent;

	EvalNodeIntPow(EvalNodeInt base, BigInteger exponent) {
		this.base = base;
		this.exponent = exponent;
		base.addParent(this);
	}

	@Override
	BigInteger evaluate() {
		if (value == null) {
			BigInteger ct = BigInteger.ZERO;

			value = base.evaluate();
			while (ct.compareTo(exponent) < 0) {
				value = base.value.multiply(ct);
				ct.add(BigInteger.ONE);
			}
		}
		return clearOnCount();
	}

	@Override
	public EvalNodeKind kind() {
		return EvalNodeKind.POW;
	}

	@Override
	public int isoCode() {
		if (isoCode == 0) {
			isoCode = base.isoCode;
			isoCode = isoCode ^ EvalNodeKind.POW.hashCode()
					^ (depth() * 179426339) ^ parents.size()
					^ exponent.hashCode();
		}
		return isoCode;
	}
}