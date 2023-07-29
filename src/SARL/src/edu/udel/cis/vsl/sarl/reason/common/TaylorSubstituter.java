package edu.udel.cis.vsl.sarl.reason.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.sarl.IF.Reasoner;
import edu.udel.cis.vsl.sarl.IF.UnaryOperator;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicFunctionType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType.SymbolicTypeKind;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.preuniverse.IF.PreUniverse;
import edu.udel.cis.vsl.sarl.preuniverse.common.ExpressionSubstituter;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;

/**
 * A substituter that replaces certain function calls with their truncated
 * Taylor polynomial expansions.
 * 
 * @author xxxx
 */
public class TaylorSubstituter extends ExpressionSubstituter {

	/**
	 * Reasoner containing expanded context, which includes assumptions on index
	 * variables.
	 */
	private Reasoner reasoner;

	/**
	 * The variables (h0, h1, ...) tending to 0. These are essentially the bound
	 * variables of a big-O expression. The length of this array is n, where n
	 * is the dimension of the domain.
	 */
	private NumericSymbolicConstant[] limitVars;

	/**
	 * The orders of the corresponding bound variables. Also an array of length
	 * n, where n is the dimension of the domain and the length of
	 * {@link #limitVars}. These are nonnegative integers. If orders[i] is k,
	 * that means we are trying to prove that something is O(hi^k).
	 */
	private int[] orders;

	/**
	 * Not sure if this is really needed, but the super-class requires some
	 * state.
	 */
	private SubstituterState trivialState = new SubstituterState() {
		@Override
		public boolean isInitial() {
			return true;
		}
	};

	/**
	 * Creates new {@link TaylorSubstituter}.
	 * 
	 * @param universe
	 *            the symbolic universe to be used to create new
	 *            {@link SymbolicExpression}s
	 * @param objectFactory
	 *            the {@link SymbolicObjectFactory} used to create new
	 *            {@link SymbolicObject}s
	 * @param typeFactory
	 *            the {@link SymbolicTypeFactory} used to create new
	 *            {@link SymbolicType}s
	 * @param reasoner
	 *            the {@link Reasoner} with the expanded context that includes
	 *            assumptions on the integer bound variables that index the grid
	 *            points
	 * @param limitVars
	 *            the real variables that are tending to 0: h0, h1, ...; an
	 *            array of length n, where n is the dimension of the domain
	 * @param orders
	 *            the "orders" of the corresponding <code>limitVars</code>;
	 *            i.e., the nonnegative integers n0, n1, ..., where we are
	 *            trying to prove O(h0^n0)+O(h1^n1)+...
	 * @param lowerBounds
	 *            lower bounds of rectangular domain (length n)
	 * @param upperBounds
	 *            upper bounds of rectangular domain (length n)
	 */
	public TaylorSubstituter(PreUniverse universe, ObjectFactory objectFactory,
			SymbolicTypeFactory typeFactory, Reasoner reasoner,
			NumericSymbolicConstant[] limitVars, int[] orders) {
		super(universe, objectFactory, typeFactory);
		this.reasoner = reasoner;
		this.limitVars = limitVars;
		this.orders = orders;
	}

	@Override
	protected SubstituterState newState() {
		return trivialState;
	}

	/**
	 * Is the expression one of : h, C*h, or h*C where C is a concrete real?
	 * 
	 * @param expr
	 *            the expression you want to check to determine if it is a
	 *            constant multiple of <code>h</code>
	 * @param h
	 *            a symbolic constant of real type
	 * @return <code>true</code> iff <code>expr</code> is h, C*h, or h*C.
	 */
	private boolean isConstantMultiple(NumericExpression expr,
			NumericSymbolicConstant h) {
		if (expr.equals(h))
			return true;
		if (expr.operator() == SymbolicOperator.MULTIPLY) {
			int numArgs = expr.numArguments();

			if (numArgs == 2) {
				NumericExpression arg0 = (NumericExpression) expr.argument(0),
						arg1 = (NumericExpression) expr.argument(1);

				if (arg0.operator() == SymbolicOperator.CONCRETE
						&& arg1.equals(h))
					return true;
				if (arg1.operator() == SymbolicOperator.CONCRETE
						&& arg0.equals(h))
					return true;
			} else { // numArgs == 1
				@SuppressWarnings("unchecked")
				Iterable<? extends SymbolicExpression> args = (Iterable<? extends SymbolicExpression>) expr
						.argument(0);
				boolean foundH = false, foundC = false;

				for (SymbolicExpression arg : args) {
					if (arg.operator() == SymbolicOperator.CONCRETE && !foundC)
						foundC = true;
					else if (arg.equals(h) && !foundH)
						foundH = true;
					else
						return false;
				}
				if (foundH)
					return true;
			}
		}
		return false;
	}

	class ExpansionSpec {
		/**
		 * Index of the component in <code>point</code> (the function call
		 * argument list). Indexed from 0
		 */
		int argumentIndex;

		/**
		 * Index in {@link #limitVars}, specifying the limiting variable that
		 * occurs in the argument.
		 */
		int limitVarIndex;

		/**
		 * The actual summand in the argument which is a multiple of the
		 * limiting variable h.
		 */
		NumericExpression hTerm;

		/**
		 * The argument minus <code>hTerm</code>. Hence the argument is the sum
		 * of <code>remains</code> and <code>hTerm</code>.
		 */
		NumericExpression remains;
	}

	private ExpansionSpec findExpansionPoint(NumericExpression[] point,
			int maxDegree) {
		int n = point.length;
		ExpansionSpec result;

		for (int i = 0; i < n; i++) {// look for an index i that can be expanded
			NumericExpression arg = point[i];
			NumericExpression[] terms = universe.expand(arg);

			for (NumericExpression term : terms) {
				for (int j = 0; j < limitVars.length; j++) {
					if (orders[j] <= maxDegree
							&& isConstantMultiple(term, limitVars[j])) {
						result = new ExpansionSpec();
						result.argumentIndex = i;
						result.limitVarIndex = j;
						result.hTerm = term;
						result.remains = universe.subtract(arg, term);
						return result;
					}
				}
			}
			// } else if (op == SymbolicOperator.SUBTRACT) {
			// NumericExpression arg0 = (NumericExpression) arg.argument(0),
			// arg1 = (NumericExpression) arg.argument(1);
			//
			// for (int j = 0; j < limitVars.length; j++) {
			// if (orders[j] > maxDegree)
			// continue;
			//
			// NumericSymbolicConstant h = limitVars[j];
			//
			// if (isConstantMultiple(arg0, h)) {
			// result = new ExpansionSpec();
			// result.argumentIndex = i;
			// result.limitVarIndex = j;
			// result.hTerm = arg0;
			// result.remains = universe.minus(arg1);
			// return result;
			// } else if (isConstantMultiple(arg1, h)) {
			// result = new ExpansionSpec();
			// result.argumentIndex = i;
			// result.limitVarIndex = j;
			// result.hTerm = universe.minus(arg1);
			// result.remains = arg0;
			// return result;
			// }
			// }
			// }
		} // end loop over arguments
		return null;
	}

	/**
	 * Attempts to perform a Taylor expansion of the given function evaluated at
	 * a point in R^n. This method looks for an appropriate component to expand.
	 * If i-th argument is a sum in which a term is a constant multiple of one
	 * of the accuracy variables (limitVars), it satisfies the heuristic. The
	 * least such is chosen. The expansion is truncated according to the degree
	 * of that limit variable. {@link #orders}. If no such i is found, returns
	 * <code>null</code>.
	 * 
	 * @param function
	 *            a function which accepts n real inputs (for some positive
	 *            integer n) and returns real
	 * @param maxDegree
	 *            the maximum number of derivatives that can be taken of
	 *            <code>function</code>
	 * @param point
	 *            an array of length n consisting of the arguments to
	 *            <code>function</code>; this is the "point" in R^n at which
	 *            <code>function</code> is evaluated
	 * @return a truncated Taylor expansion or <code>null</code>
	 */
	private NumericExpression taylorExpansion(SymbolicExpression function,
			int maxDegree, NumericExpression[] point) {
		ExpansionSpec spec = findExpansionPoint(point, maxDegree);

		if (spec == null)
			return null;

		int order = orders[spec.limitVarIndex];
		NumericExpression result = universe.zeroReal();
		int n = point.length;

		if (order >= 1) {
			IntObject indexObj = universe.intObject(spec.argumentIndex);
			int j = 0;
			NumericExpression hPower = universe.oneReal(); // hTerm^j
			int jFactorial = 1; // j!
			List<NumericExpression> newArgs = new LinkedList<>();

			for (int i = 0; i < n; i++)
				newArgs.add(i == spec.argumentIndex ? spec.remains : point[i]);
			while (true) {
				SymbolicExpression deriv = j == 0 ? function
						: universe.derivative(function, indexObj,
								universe.intObject(j));
				NumericExpression derivApplication = (NumericExpression) universe
						.apply(deriv, newArgs);

				result = universe.add(result,
						universe.divide(
								universe.multiply(derivApplication, hPower),
								universe.rational(jFactorial)));
				j++;
				if (j == order)
					break;
				hPower = universe.multiply(hPower, spec.hTerm);
				jFactorial *= j;
			}
		}
		return result;
	}

	/**
	 * Attempts to find, in the context of the {@link #reasoner}, a clause which
	 * states the differentiability of the given <code>function</code>. This is
	 * a clause with operator {@link SymbolicOperator#DIFFERENTIABLE} and with
	 * the function argument (argument 0) equal to <code>function</code>.
	 * 
	 * @param function
	 *            the function for which a differentiability claim is sought
	 * @return a clause in the context dealing with the differentiability of
	 *         <code>function</code>, or <code>null</code> if no such clause is
	 *         found.
	 */
	private BooleanExpression findDifferentiableClaim(
			SymbolicExpression function) {
		for (BooleanExpression clause : reasoner.getReducedContext()
				.getClauses()) {
			if (clause.operator() != SymbolicOperator.DIFFERENTIABLE)
				continue;

			if (clause.argument(0).equals(function))
				return clause;
		}
		return null;
	}

	private NumericExpression[] toArray(SymbolicObject sequence, int length) {
		int count = 0;
		@SuppressWarnings("unchecked")
		Iterable<? extends NumericExpression> iterable = (Iterable<? extends NumericExpression>) sequence;
		NumericExpression[] result = new NumericExpression[length];

		for (NumericExpression x : iterable) {
			result[count] = x;
			count++;
		}
		return result;
	}

	/**
	 * Determines whether this is a function from R^n (for some n) to R.
	 * 
	 * @param function
	 *            a symbolic expression, non-<code>null</code>
	 * @return the number of inputs n, if <code>function</code> is a function
	 *         from R^n to R, else -1
	 */
	private int getNumRealFunctionInputs(SymbolicExpression function) {
		if (function.type().typeKind() != SymbolicTypeKind.FUNCTION)
			return -1;

		SymbolicFunctionType functionType = (SymbolicFunctionType) function
				.type();
		SymbolicTypeSequence inputTypes = functionType.inputTypes();
		int n = inputTypes.numTypes();

		if (!functionType.outputType().isReal())
			return -1;
		for (int i = 0; i < n; i++) {
			if (!inputTypes.getType(i).isReal())
				return -1;
		}
		return n;
	}

	/**
	 * Checks that the point is in the domain defined by the given bounds.
	 * 
	 * @param point
	 *            a point (x_i) in R^n
	 * @param lowerBounds
	 *            a point (a_i) in R^n
	 * @param upperBounds
	 *            a point (b_i) in R^n
	 * @return true if it can be proved a_i<x_i<b_i for all i.
	 */
	private boolean checkDomain(NumericExpression[] point,
			NumericExpression[] lowerBounds, NumericExpression[] upperBounds) {
		// need to substitute 0 for all limitVars in the argArray
		// before checking the arguments are in range. Otherwise, without
		// any restriction on limitVars, who knows.
		// Actually you should be taking limit as h->0, but for now...
		int n = point.length;
		Map<SymbolicExpression, SymbolicExpression> zeroMap = new HashMap<>();
		NumericExpression zero = universe.zeroReal();

		for (NumericSymbolicConstant limitVar : limitVars) {
			zeroMap.put(limitVar, zero);
		}

		UnaryOperator<SymbolicExpression> zeroSubber = universe
				.mapSubstituter(zeroMap);

		for (int i = 0; i < n; i++) {
			NumericExpression arg = (NumericExpression) zeroSubber
					.apply(point[i]);
			BooleanExpression inDomain = universe.and(
					universe.lessThan(lowerBounds[i], arg),
					universe.lessThan(arg, upperBounds[i]));

			if (!reasoner.isValid(inDomain))
				return false;
		}
		return true;
	}

	/**
	 * Given a function from R^n to R for some n, and a nonnegative integer d,
	 * and point x in R^n, determines if the application of a d-th derivative of
	 * f to x is bounded.
	 * 
	 * @param function
	 * @param degree
	 * @param point
	 * @return
	 */
	private boolean isBoundedApplicationOfDeriv(SymbolicExpression function,
			int degree, NumericExpression[] point) {
		BooleanExpression claim = this.findDifferentiableClaim(function);

		if (claim == null)
			return false;

		int degree1 = ((IntObject) claim.argument(1)).getInt();

		if (degree > degree1)
			return false;

		int n = point.length;
		NumericExpression[] lowerBounds = toArray(claim.argument(2), n);
		NumericExpression[] upperBounds = toArray(claim.argument(3), n);

		return checkDomain(point, lowerBounds, upperBounds);
	}

	/**
	 * Given a function from R^n to R, and a point in R^n, determines if this is
	 * a bounded application of the function.
	 * 
	 * @param function
	 *            function from R^n to R
	 * @param point
	 *            in R^n
	 * @return
	 */
	private boolean isBoundedApplication(SymbolicExpression function,
			NumericExpression[] point) {
		SymbolicOperator op = function.operator();

		if (op == SymbolicOperator.DERIV) {
			SymbolicExpression f0 = (SymbolicExpression) function.argument(0);
			int degree0 = ((IntObject) function.argument(2)).getInt();

			return isBoundedApplicationOfDeriv(f0, degree0, point);
		} else if (op == SymbolicOperator.SYMBOLIC_CONSTANT) {
			return isBoundedApplicationOfDeriv(function, 0, point);
		} else {
			return false;
		}
	}

	private boolean isBounded(NumericExpression expr) {
		switch (expr.operator()) {
		case ADD:
		case SUBTRACT:
		case MULTIPLY:
		case NEGATIVE: {
			int n = expr.numArguments();

			for (int i = 0; i < n; i++) {
				if (!isBounded((NumericExpression) expr.argument(i)))
					return false;
			}
			return true;
		}
		case DIVIDE: {
			if (!isBounded((NumericExpression) expr.argument(0)))
				return false;
			return ((NumericExpression) expr.argument(1))
					.operator() == SymbolicOperator.CONCRETE;
		}
		case CONCRETE:
			return true;
		case SYMBOLIC_CONSTANT: {
			for (SymbolicConstant h : limitVars)
				if (h.equals(expr))
					return true;
		}
		case APPLY: {
			// is this a function from R^n to R?
			SymbolicExpression f = (SymbolicExpression) expr.argument(0);
			int n = getNumRealFunctionInputs(f);

			if (n < 0)
				return false;
			return isBoundedApplication(f, toArray(expr.argument(1), n));
		}
		default:
			return false;
		}
	}

	public NumericExpression reduceModLimits(NumericExpression expr) {
		NumericExpression[] terms = universe.expand(expr);
		int numVars = limitVars.length;
		boolean change = false;

		for (NumericExpression term : terms) {
			for (int i = 0; i < numVars; i++) {
				NumericSymbolicConstant h = limitVars[i];
				int order = orders[i];
				NumericExpression hton = universe.power(h, order);
				NumericExpression q = universe.divide(term, hton);

				if (isBounded(q)) {
					change = true;
					terms[i] = null;
					break;
				}
			}
		}
		if (change) {
			NumericExpression result = universe.zeroReal();

			for (NumericExpression term : terms) {
				if (term != null)
					result = universe.add(result, term);
			}
			return result;
		} else {
			return expr;
		}
	}

	private SymbolicExpression tryToExpand(SymbolicExpression expression,
			SubstituterState state) {
		if (expression.operator() != SymbolicOperator.APPLY)
			return null;

		SymbolicExpression function = (SymbolicExpression) expression
				.argument(0);

		if (function.operator() != SymbolicOperator.SYMBOLIC_CONSTANT)
			return null;

		SymbolicFunctionType functionType = (SymbolicFunctionType) function
				.type();
		SymbolicTypeSequence inputTypes = functionType.inputTypes();
		int n = inputTypes.numTypes();

		if (!functionType.outputType().isReal())
			return null;
		for (int i = 0; i < n; i++) {
			if (!inputTypes.getType(i).isReal())
				return null;
		}

		BooleanExpression diffClaim = findDifferentiableClaim(function);

		// Arg0 is a function from R^n to R for some positive
		// integer n. Arg1 is the degree, a nonnegative integer
		// {@link IntObject} which tells the number of partial derivatives (of
		// any combination) that exist and are continuous. Arg2 is a sequence of
		// real-valued expressions which are the lower bounds of the intervals
		// in the domain; the length is n. Arg3 is a similar sequence of upper
		// bounds.

		if (diffClaim == null)
			return null;

		NumericExpression[] argArray = toArray(expression.argument(1), n);
		NumericExpression[] lowerBounds = toArray(diffClaim.argument(2), n);
		NumericExpression[] upperBounds = toArray(diffClaim.argument(3), n);

		if (!checkDomain(argArray, lowerBounds, upperBounds))
			return null;

		int maxDegree = ((IntObject) diffClaim.argument(1)).getInt();
		SymbolicExpression result = taylorExpansion(function, maxDegree,
				argArray);

		// TODO: debugging:
		System.out.println("Taylor: expression   : " + expression);
		System.out.println("Taylor: result       : " + result);
		System.out.println();
		System.out.flush();

		return result;
	}

	@Override
	protected SymbolicExpression substituteExpression(
			SymbolicExpression expression, SubstituterState state) {
		SymbolicExpression result = tryToExpand(expression, state);

		if (result == null)
			result = super.substituteExpression(expression, state);
		return result;
	}

}
