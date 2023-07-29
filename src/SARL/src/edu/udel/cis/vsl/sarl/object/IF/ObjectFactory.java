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
package edu.udel.cis.vsl.sarl.object.IF;

import java.util.Comparator;

import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.object.BooleanObject;
import edu.udel.cis.vsl.sarl.IF.object.CharObject;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;

/**
 * A factory for producing certain {@link SymbolicObject}s.
 * 
 * @author xxxx
 *
 */
public interface ObjectFactory {

	/**
	 * @return the numberFactory of the ObjectFactory
	 */
	NumberFactory numberFactory();

	/**
	 * Sets the expression comparator for this object factory. This needs to be
	 * done before this factory is initialized with method {@link #init()}.
	 * 
	 * @param c
	 *            the expression comparator
	 */
	void setExpressionComparator(Comparator<SymbolicExpression> c);

	/**
	 * Sets the type comparator of for this factory. This needs to be done
	 * before this factory is initialized with method {@link #init()}.
	 * 
	 * @param c
	 *            the type comparator
	 */
	void setTypeComparator(Comparator<SymbolicType> c);

	/**
	 * Sets the type sequence comparator of this factory. This needs to be done
	 * before this factory is initialized with method {@link #init()}.
	 * 
	 * @param c
	 *            the type sequence comparator
	 */
	void setTypeSequenceComparator(Comparator<SymbolicTypeSequence> c);

	/**
	 * Initializes the fields of this factory.
	 * 
	 * Preconditions: The expression comparator, collection comparator, type
	 * comparator, and type sequence comparator have all been set for this
	 * object.
	 */
	public void init();

	/**
	 * Returns a {@link Comparator} on all {@link SymbolicObject}s. This object
	 * comparator is based on the expression comparator, collection comparator,
	 * type comparator, and type sequence comparator that were provided to this
	 * factory.
	 * 
	 * Preconditions: the factory has been initialized via method
	 * {@link #init()}
	 * 
	 * @return the object comparator
	 */
	Comparator<SymbolicObject> comparator();

	/**
	 * Returns the canonic representative of the object's equivalence class.
	 * This will be used for the "canonicalization" of all symbolic objects in a
	 * universe. See "Flyweight Pattern".
	 * 
	 * @param object
	 *            any symbolic object
	 * @return the canonic representative
	 */
	<T extends SymbolicObject> T canonic(T object);

	/**
	 * Canonizes each object in an array.
	 * 
	 * @see #canonic(SymbolicObject)
	 * 
	 * @param objectArray
	 *            array of {@link SymbolicObject}s, none of which is
	 *            <code>null</code>
	 */
	<T extends SymbolicObject> void canonize(T[] objectArray);

	/**
	 * @return the {@link BooleanObject} with value true
	 */
	BooleanObject trueObj();

	/**
	 * @return the {@link BooleanObject} with value false
	 */
	BooleanObject falseObj();

	/**
	 * @return the {@link IntObject} with value 0
	 */
	IntObject zeroIntObj();

	/**
	 * @return the {@link IntObject} with value 1
	 */
	IntObject oneIntObj();

	/**
	 * @return the {@link NumberObject} with value ({@link IntegerNumber}) 0
	 */
	NumberObject zeroIntegerObj();

	/**
	 * @return the {@link NumberObject} with value ({@link IntegerNumber}) 1
	 */
	NumberObject oneIntegerObj();

	/**
	 * @return the {@link NumberObject} with value ({@link RationalNumber}) 0
	 */
	NumberObject zeroRealObj();

	/**
	 * @return the {@link NumberObject} with value ({@link RationalNumber}) 1
	 */
	NumberObject oneRealObj();

	/**
	 * @return the {@link NumberObject} wrapping the given value
	 */
	NumberObject numberObject(Number value);

	/**
	 * @return the {@link CharObject} wrapping the given char
	 */
	CharObject charObject(char value);

	/**
	 * @return the {@link StringObject} wrapping the given string
	 */
	StringObject stringObject(String string);

	/**
	 * @return the {@link IntObject} wrapping the given int
	 */
	IntObject intObject(int value);

	/**
	 * @return the {@link BooleanObject} wrapping the given boolean value
	 */
	BooleanObject booleanObject(boolean value);

	/**
	 * Gets the canonic object with the given ID number. This factory stores all
	 * canonic objects. This method should return the object in constant time.
	 * 
	 * Preconditions: the <code>index</code> should in the range [0,n), where n
	 * is the current number of canonic objects.
	 * 
	 * @return the canonic object with the given ID number
	 */
	SymbolicObject objectWithId(int index);

	/**
	 * @return the current number of canonic objects
	 */
	int numObjects();

	/**
	 * Returns a {@link SymbolicSequence} comprising the given sequence of
	 * elements. The elements must all be non-<code>null</code>.
	 * 
	 * @param elements
	 *            any object providing an iterator over
	 *            {@link SymbolicExpression}
	 * @return a single {@link SymbolicSequence} which wraps the given list of
	 *         elements
	 */
	<T extends SymbolicExpression> SymbolicSequence<T> sequence(
			Iterable<? extends T> elements);

	/**
	 * Returns a {@link SymbolicSequence} comprising the sequence of elements
	 * specified as an array.
	 * 
	 * @param elements
	 *            any array of {@link SymbolicExpression}, all elements of which
	 *            must be non-<code>null</code>
	 * @return a single {@link SymbolicSequence} which wraps the given list of
	 *         elements
	 */
	<T extends SymbolicExpression> SymbolicSequence<T> sequence(T[] elements);

	/**
	 * Returns the sequence of length 1 consisting of the given element.
	 * 
	 * @param element
	 *            a non-<code>null</code> element of T
	 * @return the sequence consisting of just the one element
	 */
	<T extends SymbolicExpression> SymbolicSequence<T> singletonSequence(
			T element);

	/**
	 * Returns the empty sequence.
	 * 
	 * @return the empty sequence
	 */
	<T extends SymbolicExpression> SymbolicSequence<T> emptySequence();

}
