package edu.udel.cis.vsl.sarl.simplify.simplifier;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.sarl.IF.SARLConstants;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.ideal.IF.Polynomial;
import edu.udel.cis.vsl.sarl.ideal.IF.RationalExpression;
import edu.udel.cis.vsl.sarl.simplify.simplification.ArrayLambdaSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.ArrayReadSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.ConditionalSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.ConditionalSimplification2;
import edu.udel.cis.vsl.sarl.simplify.simplification.GenericSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.LambdaSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.ComputerModuloSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.NumericOrSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.OrSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.PolynomialSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.PowerSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.QuantifierSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.RationalPowerSimplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.Simplification;
import edu.udel.cis.vsl.sarl.simplify.simplification.Simplification.SimplificationKind;
import edu.udel.cis.vsl.sarl.simplify.simplification.SubContextSimplification;

/**
 * An ideal simplifier worker is created to simplify one symbolic expression. It
 * disappears once that task has completed. It maintains a reference to a
 * {@link Context} under which the simplification is taking place. It makes no
 * changes to the context, other than to cache the results of simplification in
 * the context's cache.
 * 
 * @author xxxx
 */
public class IdealSimplifierWorker {

	/** The total number of simplifications performed, used for debugging */
	private static int simpCount = 0;

	/** Should we print debugging information ? */
	public static boolean debug = false;

	/** Where the debugging information is sent */
	public static PrintStream out = System.out;

	/**
	 * The context which represents the assumptions under which simplification
	 * is taking place. It is a structured representation of a boolean
	 * expression.
	 */
	Context theContext;

	/**
	 * This is a stack of expressions being simplified, but since an expression
	 * is only allowed to occur at most once on the stack, a set is used. When
	 * simplifying an expression e, first e will be pushed onto the stack. In
	 * the process of simplifying e, other expressions may need to be simplified
	 * and are pushed onto the stack. If at any point an expression is
	 * encountered that is already on the stack, simplification returns
	 * immediately with that expression (no simplification is done). This is to
	 * avoid infinite cycles in the simplification process.
	 * 
	 * <p>
	 * Currently used on in debugging mode.
	 * </p>
	 * 
	 * @see #simplifyExpressionWork(SymbolicExpression)
	 */
	Set<SymbolicExpression> simplificationStack;

	IdealSimplifierWorker(Context context,
			Set<SymbolicExpression> seenExpressions) {
		this.theContext = context;
		this.simplificationStack = SARLConstants.cycleDetection
				? seenExpressions
				: null;
	}

	/**
	 * A {@link Simplification} applicable to all
	 * {@link SymbolicOperator#ARRAY_LAMBDA} expressions. The simplification
	 * sequence for array lambda expressions is: arrayLambda.
	 * 
	 * @return
	 */
	private Simplification arrayLambdaSimplification() {
		return new ArrayLambdaSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all
	 * {@link SymbolicOperator#ARRAY_READ} expressions. The simplification
	 * sequence for array read expressions is: arrayRead.
	 * 
	 * @return
	 */
	private Simplification arrayReadSimplification() {
		return new ArrayReadSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all {@link SymbolicOperator#COND}
	 * expressions. The simplification sequence for conditional expressions is:
	 * substitution, generic, conditional.
	 * 
	 * CURRENTLY unused. Using ConditionalSimplification2 instead.
	 * 
	 * @return
	 */
	@SuppressWarnings("unused")
	private Simplification conditionalSimplification() {
		return new ConditionalSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all {@link SymbolicOperator#COND}
	 * expressions. The simplification sequence for conditional expressions is:
	 * substitution, generic, conditional.
	 * 
	 * @return
	 */
	private Simplification conditionalSimplification2() {
		return new ConditionalSimplification2(this);
	}

	/**
	 * A {@link Simplification} applicable to almost all
	 * {@link SymbolicExpression}s. Simplifies the type, simplifies the
	 * arguments, and puts the expression back together.
	 */
	private Simplification genericSimplification() {
		return new GenericSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all
	 * {@link SymbolicOperator#LAMBDA} expressions. The simplification sequence
	 * for lambda expressions is: substitution, lambda.
	 * 
	 * @return
	 */
	private Simplification lambdaSimplification() {
		return new LambdaSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all {@link SymbolicOperator#OR}
	 * expressions. The simplification sequence for or expressions is:
	 * substitution, generic, or, numericOr.
	 * 
	 * @return
	 */
	private Simplification orSimplification() {
		return new OrSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all {@link SymbolicOperator#OR}
	 * expressions. The simplification sequence for or expressions is:
	 * substitution, generic, or, numericOr.
	 * 
	 * @return
	 */
	private Simplification numericOrSimplification() {
		return new NumericOrSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all instances of
	 * {@link Polynomial}. The simplification sequence for polynomials is:
	 * substitution, generic, polynomial, rationalPower.
	 * 
	 * @return
	 */
	private Simplification polynomialSimplification() {
		return new PolynomialSimplification(this);
	}

	/**
	 * A {@link Simplification} applicable to all {@link SymbolicOperator#POWER}
	 * expressions. The simplification sequence for power expressions is:
	 * substitution, generic, power.
	 * 
	 * @return
	 */
	private Simplification powerSimplification() {
		return new PowerSimplification(this);
	};

	/**
	 * A {@link Simplification} applicable to {@link SymbolicOperator#FORALL}
	 * and {@link SymbolicOperator#EXISTS} expressions. The simplification
	 * sequence for quantified expressions is: substitution, quantifier.
	 * 
	 * @return
	 */
	private Simplification quantifierSimplification() {
		return new QuantifierSimplification(this);
	}

	/**
	 * Attempts to combine compatible power factors in a rational expression.
	 * Use this only if the rational expression is not a POWER expression.
	 * 
	 * @return
	 */
	private Simplification rationalPowerSimplification() {
		return new RationalPowerSimplification(this);
	}

	/**
	 * Used for certain boolean expressions in which a sub-context is formed
	 * AND, LESS_THAN, LESS_THAN_EQUALS, NEQ. EQUALS as long as the type of the
	 * arguments is numeric.
	 * 
	 * @return
	 */
	private Simplification subContextSimplification() {
		return new SubContextSimplification(this);
	}

	/**
	 * Used for simplification of symbolic expression with MODULO operator
	 * 
	 * @return
	 */
	private Simplification moduloSimplification() {
		return new ComputerModuloSimplification(this);
	}

	/**
	 * Returns the appropriate sequence of {@link Simplification}s for a given
	 * symbolic expression {@code x}.
	 * 
	 * @param x
	 *            the symbolic expression to be simplified
	 * @return the sequence of {@link Simplification}s that will be performed on
	 *         {@code x}
	 */
	private Simplification[] getSimplifications(SymbolicExpression x) {
		SymbolicOperator op = x.operator();

		switch (op) {
		case LAMBDA:
			return new Simplification[] { lambdaSimplification() };
		case ARRAY_LAMBDA:
			return new Simplification[] { arrayLambdaSimplification() };
		case ARRAY_READ:
			return new Simplification[] { arrayReadSimplification() };
		case AND:
		case LESS_THAN:
		case LESS_THAN_EQUALS:
		case NEQ:
			return new Simplification[] { subContextSimplification() };
		case EQUALS:
			if (((SymbolicExpression) x.argument(0)).type().isNumeric())
				return new Simplification[] { subContextSimplification() };
			else
				return new Simplification[] { genericSimplification() };
		case FORALL:
		case EXISTS:
			return new Simplification[] { quantifierSimplification() };
		case POWER:
			return new Simplification[] { genericSimplification(),
					powerSimplification(), rationalPowerSimplification() };
		case COND: {
			// struggling to find the "right" way to simplify p?a:b...
			// return new Simplification[] { conditionalSimplification() };
			return new Simplification[] { conditionalSimplification2() };
			// return new Simplification[] { genericSimplification() };
		}
		case OR:
			return new Simplification[] { genericSimplification(),
					orSimplification(), numericOrSimplification() };
		case MODULO:
			return new Simplification[] { genericSimplification(),
					rationalPowerSimplification(), moduloSimplification() };
		default:
		}
		if (x instanceof Polynomial)
			return new Simplification[] { genericSimplification(),
					polynomialSimplification(), rationalPowerSimplification() };
		if (x instanceof RationalExpression)
			return new Simplification[] { genericSimplification(),
					rationalPowerSimplification() };
		return new Simplification[] { genericSimplification() };
	}

	/**
	 * Simplifies an expression that is not a simple constant.
	 * 
	 * @param expression
	 *            a non-{@code null} symbolic expression not a simple constant
	 * @return the simplified version of the given expression
	 */
	SymbolicExpression simplifyNonSimpleConstant(
			SymbolicExpression expression) {
		// It is OK to cache simplification results even if the context
		// is changing because the context clears its cache every time
		// a change is made...
		SymbolicExpression result = (SymbolicExpression) theContext
				.getSimplification(expression);

		if (result == null) {
			result = simplifyExpressionWork(expression);
			theContext.cacheSimplification(expression, result);
		}
		return result;
	}

	/**
	 * Simplifies a symbolic expression by looking in the substitution map of
	 * {@link #theContext} and applying an appropriate sequence of
	 * {@link Simplification}s. These actions are repeated until stabilization.
	 * 
	 * @param expr
	 *            the symbolic expression to be simplified
	 * @return the simplified version of the expression
	 */
	public SymbolicExpression simplifyExpressionWork(SymbolicExpression expr) {
		int id = simpCount++; // TODO: make me atomic
		int outercount = 0;

		if (SARLConstants.cycleDetection) {
			if (!simplificationStack.add(expr)) {
				if (debug)
					out.println(
							"SARL simplification warning: cycle detected on:\n"
									+ expr);
				simplificationStack.remove(expr);
				return expr;
			}
		}
		if (debug) {
			out.println("Simplification " + id + " start : " + expr);
		}

		SymbolicExpression result = expr, x = expr;
		// the last simplification that made a change:
		SimplificationKind lastChange = null;
		Set<SymbolicExpression> seen = SARLConstants.cycleDetection
				? new HashSet<>()
				: null;

		outer: while (true) {
			if (SARLConstants.cycleDetection && !seen.add(x))
				break;

			SymbolicExpression tmp = theContext.getSub(x);

			if (tmp != null) {
				// There are some options here. After a context is completed,
				// the sub map should be idempotent, so we can just break.
				// But during simplification, it might not be, and breaking will
				// cause another simplification round, which will go through the
				// cache. That may or may not save time over iterating here.
				// x = result;
				// lastChange = null;
				// continue;
				result = tmp;
				break;
			}

			Simplification[] simplifications = getSimplifications(x);
			int innercount = 0;

			for (Simplification s : simplifications) {
				if (s.kind() == lastChange) {
					// simplifications are idempotent
					// so no need to do one twice in a row
					continue;
				}
				if (debug) {
					out.println("Simplification " + id + "." + outercount + "."
							+ innercount + ": " + s.getClass().getSimpleName());
				}
				result = s.apply(x);
				if (debug) {
					out.print("Simplification " + id + "." + outercount + "."
							+ innercount + " result : ");
					if (x == result)
						out.println("no change");
					else
						out.println(result);
				}
				if (x != result) {
					x = result;
					outercount++;
					lastChange = s.kind();
					continue outer;
				}
				innercount++;
			}
			break;
		}
		if (debug) {
			out.println("Simplification " + id + " final result : " + result);
		}
		if (SARLConstants.cycleDetection) {
			simplificationStack.remove(expr);
		}
		return result;
	}

	/**
	 * Simplifies a symbolic expression, caching the result in the underlying
	 * {@link Context}.
	 * 
	 * @param expression
	 *            any non-<code>null</code> {@link SymbolicExpression}
	 * @return an expression guaranteed to be equivalent to the given one under
	 *         the assumption of {@link #theContext}
	 */
	public SymbolicExpression simplifyExpression(
			SymbolicExpression expression) {
		if (SimplifierUtility.isSimpleConstant(expression))
			return expression;
		return simplifyNonSimpleConstant(expression);
	}

	/**
	 * Gets the context being used by this worker to simplify expressions.
	 * 
	 * @return the context used by this worker
	 */
	public Context getContext() {
		return theContext;
	}

	/**
	 * Gets the set of symbolic expressions encountered by this worker's
	 * simplify methods. Currently only used in debugging mode.
	 * 
	 * @return the expressions seen by this worker
	 */
	public Set<SymbolicExpression> getSimplificationStack() {
		return simplificationStack;
	}

}
