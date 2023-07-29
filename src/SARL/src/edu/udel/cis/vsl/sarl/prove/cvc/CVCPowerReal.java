package edu.udel.cis.vsl.sarl.prove.cvc;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;

/**
 * <p>
 * This is a class provides a PowerReal theory for CVC translator.
 * </p>
 * 
 * <p>
 * This class provides
 * <li>a function <code>pow</code> for translating SARL Power operator when
 * exponents are real numbers,</li>
 * <li>a set of axioms for <code>pow</code> which are all borrowed from
 * why3.</li>
 * </p>
 * 
 * @author xxxx
 */
public class CVCPowerReal {
	/**
	 * The name of this theory
	 */
	static String TheoryName = "POW";

	/**
	 * The name of the function representing power operations
	 */
	static String pow = "pow";

	/**
	 * A set of axioms over the {@link #powFunction}
	 */
	private BooleanExpression[] axioms = null;

	/**
	 * The symbolic constant of the function representing the power operation:
	 */
	private SymbolicConstant powFunction = null;

	/**
	 * A reference to the {@link PreUniverse}
	 */
	private PreUniverse universe = null;

	CVCPowerReal(PreUniverse universe) {
		this.universe = universe;
	}

	/**
	 * @return a set of boolean valued expression which are axioms defined in
	 *         the Power theory. All axioms are quantified and have no bound
	 *         variable.
	 */
	BooleanExpression[] getAxioms() {
		if (axioms != null)
			return axioms;
		return generatesAxioms();
	}

	/**
	 * Returns a function call to {@link #powFunction} which represents a power
	 * operation expression
	 * 
	 * @param base
	 *            the base of the power operation, either integer or real type
	 * @param exp
	 *            the exponent of the power operation, must be real type
	 * @return the function call representing the power operation
	 */
	NumericExpression applyPow(NumericExpression base, NumericExpression exp) {
		assert exp.type().isReal();
		// make base real type:
		NumericExpression realBase = (NumericExpression) universe
				.cast(universe.realType(), base);

		assert !base.type().isReal() || realBase == base;
		if (powFunction != null)
			return (NumericExpression) universe.apply(powFunction,
					Arrays.asList(base, exp));
		else
			return (NumericExpression) universe.apply(generatesPow(),
					Arrays.asList(base, exp));
	}

	private SymbolicExpression generatesPow() {
		SymbolicType realType = universe.realType();

		powFunction = universe.symbolicConstant(universe.stringObject(pow),
				universe.functionType(Arrays.asList(realType, realType),
						realType));
		return powFunction;
	}

	private BooleanExpression[] generatesAxioms() {
		List<BooleanExpression> axioms = new LinkedList<>();
		NumericSymbolicConstant x, y, z;
		SymbolicType realType = universe.realType();
		NumericExpression pow_x_y;
		NumericExpression realZero = universe.zeroReal();

		x = (NumericSymbolicConstant) universe
				.symbolicConstant(universe.stringObject("x"), realType);
		y = (NumericSymbolicConstant) universe
				.symbolicConstant(universe.stringObject("y"), realType);
		z = (NumericSymbolicConstant) universe
				.symbolicConstant(universe.stringObject("z"), realType);

		/*
		 * ;; Pow_pos (assert (forall ((x Real) (y Real)) (=> (< 0.0 x) (< 0.0
		 * (pow x y)))))
		 */
		pow_x_y = (NumericExpression) universe.apply(powFunction,
				Arrays.asList(x, y));

		BooleanExpression Pow_pos = universe.forall(x,
				universe.forall(y,
						universe.implies(universe.lessThan(realZero, x),
								universe.lessThan(realZero, pow_x_y))));
		axioms.add(Pow_pos);
		// System.out.println(Pow_pos + "\n");

		/*
		 * ;; Pow_plus (assert (forall ((x Real) (y Real) (z Real)) (=> (< 0.0
		 * z) (= (pow z (+ x y)) (* (pow z x) (pow z y))))))
		 */
		NumericExpression pow_z_yPLUSx = (NumericExpression) universe
				.apply(powFunction, Arrays.asList(z, universe.add(y, x)));
		NumericExpression pow_z_x_PLUS_pow_z_y = (NumericExpression) universe
				.multiply(
						(NumericExpression) universe.apply(powFunction,
								Arrays.asList(z, x)),
						(NumericExpression) universe.apply(powFunction,
								Arrays.asList(z, y)));
		BooleanExpression Pow_plus = universe.forall(x,
				universe.forall(y, universe.forall(z, universe.implies(
						universe.lessThan(realZero, z),
						universe.equals(pow_z_yPLUSx, pow_z_x_PLUS_pow_z_y)))));
		axioms.add(Pow_plus);
		// System.out.println(Pow_plus + "\n");
		/*
		 * ;; Pow_mult (assert (forall ((x Real) (y Real) (z Real)) (=> (< 0.0
		 * x) (= (pow (pow x y) z) (pow x (* y z))) )))
		 */
		NumericExpression pow_xy_z = (NumericExpression) universe
				.apply(powFunction, Arrays.asList(
						universe.apply(powFunction, Arrays.asList(x, y)), z));
		NumericExpression pow_x_yTIMESz = (NumericExpression) universe
				.apply(powFunction, Arrays.asList(x, universe.multiply(y, z)));
		BooleanExpression Pow_mult = universe.forall(x,
				universe.forall(y,
						universe.forall(z, universe.implies(
								universe.lessThan(realZero, x),
								universe.equals(pow_xy_z, pow_x_yTIMESz)))));

		axioms.add(Pow_mult);
		// System.out.println(Pow_mult + "\n");

		/*
		 * ;; Pow_x_zero (assert (forall ((x Real)) (=> (< 0.0 x) (= (pow x 0.0)
		 * 1.0))))
		 */
		NumericExpression pow_x_0 = (NumericExpression) universe
				.apply(powFunction, Arrays.asList(x, realZero));
		BooleanExpression Pow_x_zero = universe.forall(x,
				universe.implies(universe.lessThan(realZero, x),
						universe.equals(universe.oneReal(), pow_x_0)));
		axioms.add(Pow_x_zero);
		// System.out.println(Pow_x_zero + "\n");
		/*
		 * ;; Pow_x_one (assert (forall ((x Real)) (=> (< 0.0 x) (= (pow x 1.0)
		 * x))))
		 */
		NumericExpression pow_x_1 = (NumericExpression) universe
				.apply(powFunction, Arrays.asList(x, universe.oneReal()));
		BooleanExpression Pow_x_one = universe.forall(x, universe.implies(
				universe.lessThan(realZero, x), universe.equals(x, pow_x_1)));
		axioms.add(Pow_x_one);
		// System.out.println(Pow_x_one + "\n");
		/*
		 * ;; Pow_one_y (assert (forall ((y Real)) (= (pow 1.0 y) 1.0)))
		 */
		NumericExpression pow_1_y = (NumericExpression) universe
				.apply(powFunction, Arrays.asList(universe.oneReal(), y));
		BooleanExpression pow_one_y = universe.forall(y,
				universe.equals(universe.oneReal(), pow_1_y));
		axioms.add(pow_one_y);
		// System.out.println(pow_one_y + "\n");

		/*
		 * TODO: power operation with sqrt operation:
		 * 
		 * ;; Pow_x_two (assert (forall ((x Real)) (=> (< 0.0 x) (= (pow x 2.0)
		 * (sqr x)))))
		 * 
		 * ;; Pow_half (assert (forall ((x Real)) (=> (< 0.0 x) (= (pow x (/ 5.0
		 * 10.0)) (sqrt x)))))
		 * 
		 */

		BooleanExpression results[] = new BooleanExpression[axioms.size()];
		axioms.toArray(results);
		return results;
	}
}
