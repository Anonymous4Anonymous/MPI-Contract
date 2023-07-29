package edu.udel.cis.vsl.sarl.simplify.eval;

import java.math.BigInteger;

import edu.udel.cis.vsl.sarl.IF.SARLException;
import edu.udel.cis.vsl.sarl.IF.number.RationalNumber;

/**
 * A mutable infinite precision rational number.
 * 
 * @author xxxx
 */
class Rat {
	BigInteger a; // numerator
	BigInteger b; // denominator

	/**
	 * Construct new rational number from numerator a and denominator b. Place
	 * it into canonical form: b>0; a/b=0 iff (a=0 and b=1); gcd(a,b)=1.
	 * 
	 * @param a
	 *            the numerator
	 * @param b
	 *            the denominator
	 */
	Rat(BigInteger a, BigInteger b) {
		this.a = a;
		this.b = b;
		normalize();
	}

	/**
	 * Constructs new rational number a=a/1.
	 * 
	 * @param a
	 *            the big integer value
	 */
	Rat(BigInteger a) {
		this.a = a;
		this.b = BigInteger.ONE;
	}

	/**
	 * Constructs new rational number copying the numerator and denominator from
	 * the given rational number.
	 * 
	 * @param that
	 *            the other rational number
	 */
	Rat(Rat that) {
		a = that.a;
		b = that.b;
	}

	/**
	 * Constructs new rational number from a SARL {@link RationalNumber}, which
	 * uses the same canonical form. (The reason why we aren't using SARL
	 * {@link RationalNumber}s is because they are all flyweighted (cached),
	 * which is too expensive for the huge numbers we need in this class.)
	 * 
	 * @param that
	 *            the SARL number to copy
	 */
	Rat(RationalNumber that) {
		a = that.numerator();
		b = that.denominator();
	}

	/**
	 * Places this rational number into canonic form.
	 */
	private void normalize() {
		int signum = b.signum();

		if (signum == 0) {
			throw new ArithmeticException("Division by 0");
		}
		// ensures any negation is in numerator
		if (signum < 0) {
			a = a.negate();
			b = b.negate();
		}
		// canonical form for 0 is 0/1 :
		if (a.signum() == 0) {
			b = BigInteger.ONE;
		} else if (!b.equals(BigInteger.ONE)) {
			BigInteger gcd = a.gcd(b);

			if (!gcd.equals(BigInteger.ONE)) {
				a = a.divide(gcd);
				b = b.divide(gcd);
			}
		}
	}

	/**
	 * Add that number to this one. Modifies this number so that its new value
	 * is the sum of its old value and that. Does not modify that.
	 * 
	 * @param that
	 *            the other value
	 */
	void add(Rat that) {
		a = a.multiply(that.b).add(b.multiply(that.a));
		b = b.multiply(that.b);
		normalize();
	}

	/**
	 * Modifies this number so that its new value is the product of its old
	 * value and that. Does not modify that.
	 * 
	 * @param that
	 *            the other value
	 */
	void multiply(Rat that) {
		a = a.multiply(that.a);
		b = b.multiply(that.b);
		normalize();
	}

	/**
	 * Modifies this number by raising it to the {@code exponent} power.
	 * 
	 * @param exponent
	 *            a positive int
	 */
	void power(int exponent) {
		a = a.pow(exponent);
		b = b.pow(exponent);
		// no need to normalize: (a,b)=1 --> (a^e,b^e)=1
	}

	/**
	 * Modifies this number by raising it to the {@code exp} power.
	 * 
	 * @param exp
	 *            a positive big integer
	 */
	void power(BigInteger exp) {
		try {
			power(exp.intValueExact());
			return;
		} catch (ArithmeticException e) {
			throw new SARLException("to be implemented");
			// need basic implementation using multiplication
		}
	}

	/**
	 * Is this rational number equal to 0?
	 * 
	 * @return {@code true} iff this is 0
	 */
	boolean isZero() {
		return a.signum() == 0;
	}

	@Override
	public boolean equals(Object object) {
		if (object instanceof Rat) {
			Rat that = (Rat) object;

			return a.equals(that.a) && b.equals(that.b);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return a.hashCode() ^ b.hashCode();
	}
}