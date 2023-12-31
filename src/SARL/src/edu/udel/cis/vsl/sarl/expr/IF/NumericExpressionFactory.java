/*******************************************************************************
 * Copyright (c) 2013 Stephen F. Siegel, University of Delaware.
 * 
 * This file is part of SARL.
 * 
 * SARL is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * SARL is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with SARL. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.udel.cis.vsl.sarl.expr.IF;

import java.util.Comparator;

import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericSymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;

/**
 * A NumericExpressionFactory provides all of the functionality needed to create
 * and manipulate expressions of numeric type. For example, it must provide
 * methods to instantiate new numeric expression instances, to add, subtract,
 * multiply, and divide numeric expressions, etc. A general ExpressionFactory
 * will use one or more NumericExpressionFactorys by delegating out all
 * numerical operations to it.
 * 
 * Different implementations of NumericExpressionFactory can deal with
 * arithmetic issues in different ways, e.g., by treating real types as the
 * mathematical reals or as finite-precision floating point numbers, and so on.
 * 
 * @author xxxx
 * 
 */
public interface NumericExpressionFactory {

	/**
	 * Initialize this numeric expression factory. This factory should not be
	 * used until it has been initialized.
	 * 
	 * Pre-conditions: the boolean factory, object factory, and type factory
	 * have alrady been initialized.
	 */
	void init();

	/**
	 * Returns the boolean expression factory used by this numeric expression
	 * factory. The boolean factory is needed to produce relational expressions
	 * such as "x<y".
	 * 
	 * @return the boolean expression factory used by this factory
	 */
	BooleanExpressionFactory booleanFactory();

	/**
	 * Returns the number factory used by this numeric factory.
	 * 
	 * @return the number factory
	 */
	NumberFactory numberFactory();

	/**
	 * Returns the object factory used by this numeric factory.
	 * 
	 * @return the object factory
	 */
	ObjectFactory objectFactory();

	/**
	 * Returns the type factory used by this numeric expression factory.
	 * 
	 * @return the type factory
	 */
	SymbolicTypeFactory typeFactory();

	/**
	 * Returns a comparator on all numeric expressions that are controlled by
	 * this factory
	 * 
	 * @return a comparator on numeric expressions
	 */
	Comparator<NumericExpression> comparator();

	NumericExpression number(NumberObject numberObject);

	NumericSymbolicConstant symbolicConstant(StringObject name,
			SymbolicType type);

	/**
	 * Returns an expression, given the operator, type, and array of arguments
	 * 
	 * @param operator
	 *            A SymbolicOperator
	 * @param numericType
	 *            A SymbolicType
	 * @param arguments
	 *            array of arguments
	 * @return Returns a NumericExpression
	 */
	NumericExpression expression(SymbolicOperator operator,
			SymbolicType numericType, SymbolicObject... arguments);

	/**
	 * Returns a symbolic expression of integer type with the value of 0
	 * 
	 * @return
	 */
	NumericExpression zeroInt();

	/**
	 * Returns a symbolic expression of real type with the value of 0
	 * 
	 * @return
	 */
	NumericExpression zeroReal();

	/**
	 * Returns a symbolic expression of integer type with the value of 1
	 * 
	 * @return
	 */
	NumericExpression oneInt();

	/**
	 * Returns a symbolic expression of real type with the value of 1
	 * 
	 * @return
	 */
	NumericExpression oneReal();

	/**
	 * Returns a symbolic expression which is the result of adding arg1 from
	 * arg0. The two given expressions must have the same (numeric) type: either
	 * both integers, or both real.
	 * 
	 * @param arg0
	 *            a symbolic expression of a numeric type
	 * @param arg1
	 *            a symbolic expression of the same numeric type
	 * @return arg0+arg1
	 */
	NumericExpression add(NumericExpression arg0, NumericExpression arg1);

	/**
	 * Returns a symbolic expression which is the result of subtracting arg1 and
	 * arg0. The two given expressions must have the same (numeric) type: either
	 * both integers, or both real.
	 * 
	 * @param arg0
	 *            a symbolic expression of a numeric type
	 * @param arg1
	 *            a symbolic expression of the same numeric type
	 * @return arg0-arg1
	 */
	NumericExpression subtract(NumericExpression arg0, NumericExpression arg1);

	/**
	 * Returns a symbolic expression which is the result of multiplying the two
	 * given symbolic exprssions. The two given expressions must have the same
	 * (numeric) type: either both integers, or both real.
	 * 
	 * @param arg0
	 *            a symbolic expression of a numeric type
	 * @param arg1
	 *            a symbolic expression of the same numeric type
	 * @return arg0 * arg1, the product of arg0 and arg1.
	 */
	NumericExpression multiply(NumericExpression arg0, NumericExpression arg1);

	/**
	 * Returns a symbolic expression which is the result of dividing arg0 by
	 * arg1. The two given expressions must have the same (numeric) type: either
	 * both integers, or both real. In the integer case, division is interpreted
	 * as "integer division", which rounds towards 0.
	 * 
	 * @param arg0
	 *            a symbolic expression of a numeric type
	 * @param arg1
	 *            a symbolic expression of the same numeric type
	 * @return arg0 / arg1
	 */
	NumericExpression divide(NumericExpression arg0, NumericExpression arg1);

	/**
	 * Returns a symbolic expression which represents arg0 modulo arg1. The two
	 * given expressions must have the integer type. What happens for negative
	 * integers is unspecified.
	 * 
	 * @param arg0
	 *            a symbolic expression of integer type
	 * @param arg1
	 *            a symbolic expression of integer type
	 * @return arg0 % arg1
	 */
	NumericExpression modulo(NumericExpression arg0, NumericExpression arg1);

	/**
	 * Returns a symbolic expression which is the negative of the given
	 * numerical expression. The given expression must be non-null and have
	 * either integer or real type.
	 * 
	 * @param arg
	 *            a symbolic expression of integer or real type
	 * @return -arg
	 */
	NumericExpression minus(NumericExpression arg);

	/**
	 * Concrete power operator: e^b, where b is a {@link NumberObject} with a
	 * concrete non-negative integer value. This method might actually multiply
	 * out the expression, i.e., it does not necessarily return an expression
	 * with operator POWER.
	 * 
	 * @param base
	 *            the base expression in the power expression
	 * @param exponent
	 *            a non-negative concrete {@link NumberObject} exponent
	 */
	NumericExpression power(NumericExpression base, NumberObject exponent);

	/**
	 * General power operator: e^b. Both e and b are numeric expressions.
	 * 
	 * @param base
	 *            the base expression in the power expression
	 * @param exponent
	 *            the exponent in the power expression
	 */
	NumericExpression power(NumericExpression base, NumericExpression exponent);

	/**
	 * Returns a NumericExpression with the symbolic type passed in the second
	 * argument
	 * 
	 * 
	 * @param numericExpression
	 *            The NumericExpression that will receive the new type
	 * 
	 * @param newType
	 *            The new SymbolicType for the numericExpression
	 * 
	 * @return NumericExpression
	 */
	NumericExpression cast(NumericExpression numericExpression,
			SymbolicType newType);

	/**
	 * Attempts to interpret the given symbolic expression as a concrete number.
	 * If this is not possible, returns null.
	 */
	Number extractNumber(NumericExpression expression);

	/**
	 * Returns a BooleanExpression with the result of the lessThan operation on
	 * 2 NumericExpressions
	 */
	BooleanExpression lessThan(NumericExpression arg0, NumericExpression arg1);

	/**
	 * Returns a BooleanExpression with the result of the lessThanEquals
	 * operation on 2 NumericExpressions
	 */
	BooleanExpression lessThanEquals(NumericExpression arg0,
			NumericExpression arg1);

	/**
	 * Returns a BooleanExpression with the result of the notLessThan operation
	 * on 2 NumericExpressions
	 */
	BooleanExpression notLessThan(NumericExpression arg0,
			NumericExpression arg1);

	/**
	 * Returns a BooleanExpression with the result of the notLessThanEquals
	 * operation on 2 NumericExpressions
	 */
	BooleanExpression notLessThanEquals(NumericExpression arg0,
			NumericExpression arg1);

	/**
	 * Returns a BooleanExpression with the result of the equals operation on 2
	 * NumericExpressions
	 */
	BooleanExpression equals(NumericExpression arg0, NumericExpression arg1);

	/**
	 * Returns a BooleanExpression with the result of the notEquals operation on
	 * 2 NumericExpressions
	 */
	BooleanExpression neq(NumericExpression arg0, NumericExpression arg1);

	NumericExpression[] expand(NumericExpression expr);

	NumericExpression floor(NumericExpression expr);

	NumericExpression ceil(NumericExpression expr);

	NumericExpression roundToZero(NumericExpression expr);

}
