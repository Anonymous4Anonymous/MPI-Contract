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
package edu.udel.cis.vsl.sarl.object.common;

import java.util.Comparator;

import edu.udel.cis.vsl.sarl.IF.SARLInternalException;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.number.IntegerNumber;
import edu.udel.cis.vsl.sarl.IF.number.Number;
import edu.udel.cis.vsl.sarl.IF.number.NumberFactory;
import edu.udel.cis.vsl.sarl.IF.object.BooleanObject;
import edu.udel.cis.vsl.sarl.IF.object.CharObject;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.NumberObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject.SymbolicObjectKind;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicSequence;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicTypeSequence;

/**
 * The default {@link Comparator} on all {@link SymbolicObject}s. It requires
 * {@link Comparator}s for {@link SymbolicExpression}s, {@link SymbolicType}s,
 * and {@link SymbolicTypeSequence}s. It harness together all of these other
 * comparators in order to form a more general comparator on all symbolic
 * objects.
 * 
 * @author xxxx
 */
public class ObjectComparator implements Comparator<SymbolicObject> {

	/**
	 * The {@link Comparator} on {@link SymbolicExpression}s used by this
	 * {@link Comparator}.
	 */
	private Comparator<SymbolicExpression> expressionComparator;

	/**
	 * The factory used to perform exact, unbounded arithmetic on integer and
	 * real numbers.
	 */
	private NumberFactory numberFactory;

	/**
	 * The {@link Comparator} on {@link SymbolicType}s used by this
	 * {@link Comparator}.
	 */
	private Comparator<SymbolicType> typeComparator;

	/**
	 * The {@link Comparator} on {@link SymbolicTypeSequence}s used by this
	 * {@link Comparator}.
	 */
	private Comparator<SymbolicTypeSequence> typeSequenceComparator;

	/**
	 * Creates a new instance using the given number factory. The specialized
	 * comparators are initially <code>null</code>. They must be set later using
	 * the setter methods provided here before the first comparison is
	 * performed.
	 * 
	 * @param numberFactory
	 *            the factory used to perform exact, unbounded real and integer
	 *            arithmetic
	 */
	public ObjectComparator(NumberFactory numberFactory) {
		this.numberFactory = numberFactory;
	}

	private int compareSequences(SymbolicSequence<?> seq1,
			SymbolicSequence<?> seq2) {
		int size = seq1.size();
		int result = size - seq2.size();

		if (result != 0)
			return result;
		for (int i = 0; i < size; i++) {
			result = expressionComparator.compare(seq1.get(i), seq2.get(i));
			if (result != 0)
				return result;
		}
		return 0;
	}

	/**
	 * Sets the expression comparator for this object.
	 * 
	 * @param c
	 *            the expression comparator to be used by this object
	 */
	public void setExpressionComparator(Comparator<SymbolicExpression> c) {
		expressionComparator = c;
	}

	/**
	 * Sets the type comparator for this object.
	 * 
	 * @param c
	 *            the type comparator to be used by this object
	 */
	public void setTypeComparator(Comparator<SymbolicType> c) {
		typeComparator = c;
	}

	/**
	 * Sets the type sequence comparator for this object.
	 * 
	 * @param c
	 *            the type sequence comparator to be used by this object
	 */
	public void setTypeSequenceComparator(Comparator<SymbolicTypeSequence> c) {
		typeSequenceComparator = c;
	}

	/**
	 * Gets the expression comparator.
	 * 
	 * @return this object's expression comparator
	 */
	public Comparator<SymbolicExpression> expressionComparator() {
		return expressionComparator;
	}

	/**
	 * Gets the type comparator.
	 * 
	 * @return the object's type comparator
	 */
	public Comparator<SymbolicType> typeComparator() {
		return typeComparator;
	}

	/**
	 * Gets the type sequence comparator.
	 * 
	 * @return the object's type sequence comparator
	 */
	public Comparator<SymbolicTypeSequence> typeSequenceComparator() {
		return typeSequenceComparator;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Compares any two non-<code>null</code> {@link SymbolicObject}s. The
	 * objects are ordered first by "kind".
	 * 
	 * @param o1
	 *            first object
	 * @param o2
	 *            second object
	 * @return 0 a negative integer if the first object precedes the second in
	 *         the total order; 0 if the two objects are equal; a positive
	 *         integer if the second object precedes the first
	 */
	@Override
	public int compare(SymbolicObject o1, SymbolicObject o2) {
		if (o1 == o2)
			return 0;

		SymbolicObjectKind kind = o1.symbolicObjectKind();
		int result = kind.compareTo(o2.symbolicObjectKind());

		if (result != 0)
			return result;
		switch (kind) {
		case EXPRESSION:
			return expressionComparator.compare((SymbolicExpression) o1,
					(SymbolicExpression) o2);
		case SEQUENCE:
			return compareSequences((SymbolicSequence<?>) o1,
					(SymbolicSequence<?>) o2);
		case TYPE:
			return typeComparator.compare((SymbolicType) o1, (SymbolicType) o2);
		case TYPE_SEQUENCE:
			return typeSequenceComparator.compare((SymbolicTypeSequence) o1,
					(SymbolicTypeSequence) o2);
		case BOOLEAN:
			return ((BooleanObject) o1).getBoolean()
					? (((BooleanObject) o2).getBoolean() ? 0 : 1)
					: (((BooleanObject) o2).getBoolean() ? -1 : 0);
		case INT:
			return ((IntObject) o1).getInt() - ((IntObject) o2).getInt();
		case NUMBER: {
			Number num1 = ((NumberObject) o1).getNumber(),
					num2 = ((NumberObject) o2).getNumber();
			boolean isInt1 = num1 instanceof IntegerNumber,
					isInt2 = num2 instanceof IntegerNumber;

			if (isInt1 && !isInt2)
				return 1;
			if ((!isInt1) && isInt2)
				return -1;
			return numberFactory.compare(num1, num2);
		}
		case STRING:
			return ((StringObject) o1).getString()
					.compareTo(((StringObject) o2).getString());
		case CHAR:
			return Character.compare(((CharObject) o1).getChar(),
					((CharObject) o2).getChar());
		default:
			throw new SARLInternalException(
					"unreachable: unknown object kind: " + kind);
		}
	}
}
