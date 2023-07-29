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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import edu.udel.cis.vsl.sarl.IF.SARLException;
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
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;

/**
 * A straightforward implementation of {@link ObjectFactory}. All objects
 * returned by this factory are canonic. The methods in this factory are also
 * thread-safe.
 */
public class CommonObjectFactory implements ObjectFactory {

	// private final ReentrantReadWriteLock objectListReadWriteLock = new
	// ReentrantReadWriteLock();

	// private final Lock objectListReadLock =
	// objectListReadWriteLock.readLock();

	// private final Lock objectListWriteLock = objectListReadWriteLock
	// .writeLock();

	private ArrayList<SymbolicObject> objectList = new ArrayList<>();

	private NumberFactory numberFactory;

	private Map<SymbolicObject, SymbolicObject> objectMap;

	private BooleanObject trueObj;

	private BooleanObject falseObj;

	private IntObject zeroIntObj;

	private IntObject oneIntObj;

	private NumberObject zeroIntegerObj;

	private NumberObject zeroRealObj;

	private NumberObject oneIntegerObj;

	private NumberObject oneRealObj;

	private ObjectComparator comparator;

	private SymbolicSequence<?> emptySequence;

	public CommonObjectFactory(NumberFactory numberFactory) {
		this.numberFactory = numberFactory;
		this.comparator = new ObjectComparator(numberFactory);
	}

	@Override
	public void init() {
		assert comparator.expressionComparator() != null;
		assert comparator.typeComparator() != null;
		assert comparator.typeSequenceComparator() != null;
		this.objectMap = new HashMap<>(); // ConcurrentHashMap<>();
		this.trueObj = canonic(new CommonBooleanObject(true));
		this.falseObj = canonic(new CommonBooleanObject(false));
		this.zeroIntObj = intObject(0);
		this.oneIntObj = intObject(1);
		this.zeroIntegerObj = numberObject(numberFactory.zeroInteger());
		this.zeroRealObj = numberObject(numberFactory.zeroRational());
		this.oneIntegerObj = numberObject(numberFactory.oneInteger());
		this.oneRealObj = numberObject(numberFactory.oneRational());
		this.emptySequence = canonic(new SimpleSequence<SymbolicExpression>());
	}

	@Override
	public NumberFactory numberFactory() {
		return numberFactory;
	}

	@Override
	public void setExpressionComparator(Comparator<SymbolicExpression> c) {
		comparator.setExpressionComparator(c);
	}

	@Override
	public void setTypeComparator(Comparator<SymbolicType> c) {
		comparator.setTypeComparator(c);
	}

	@Override
	public void setTypeSequenceComparator(Comparator<SymbolicTypeSequence> c) {
		comparator.setTypeSequenceComparator(c);
	}

	@Override
	public ObjectComparator comparator() {
		return comparator;
	}

	@Override
	public <T extends SymbolicObject> T canonic(T object) {
		if (object == null)
			throw new SARLException("null object");
		if (object.isCanonic())
			return object;

		SymbolicObject result = objectMap.get(object);

		if (result == null) {
			// Set canonic id to IN_CANONIC to avoid infinite recursion when
			// canonicalizing children.
			((CommonSymbolicObject) object).setInCanonic();
			((CommonSymbolicObject) object).canonizeChildren(this);
			CommonSymbolicObject theObject = (CommonSymbolicObject) objectMap
					.putIfAbsent(object, object);

			if (theObject == null) {
				theObject = (CommonSymbolicObject) object;
				// synchronized (theObject) {
				theObject.setId(numObjects());
				// objectListWriteLock.lock();
				// try {
				objectList.add(theObject);
				// } finally {
				// objectListWriteLock.unlock();
				// }
				// theObject.notifyAll();
				// }
			} // else {
				// synchronized (theObject) {
				// while (theObject.id() < 0)
				// try {
				// theObject.wait();
				// } catch (InterruptedException e) {
				// e.printStackTrace();
				// }
				// }
				// }
			result = theObject;
		}

		@SuppressWarnings("unchecked")
		T resultCastToT = (T) result;

		return resultCastToT;
	}

	@Override
	public BooleanObject trueObj() {
		return trueObj;
	}

	@Override
	public BooleanObject falseObj() {
		return falseObj;
	}

	@Override
	public IntObject zeroIntObj() {
		return zeroIntObj;
	}

	@Override
	public IntObject oneIntObj() {
		return oneIntObj;
	}

	@Override
	public NumberObject zeroIntegerObj() {
		return zeroIntegerObj;
	}

	@Override
	public NumberObject oneIntegerObj() {
		return oneIntegerObj;
	}

	@Override
	public NumberObject zeroRealObj() {
		return zeroRealObj;
	}

	@Override
	public NumberObject oneRealObj() {
		return oneRealObj;
	}

	@Override
	public NumberObject numberObject(Number value) {
		if (value == null)
			throw new SARLException("null value");
		if (value.isInfinite())
			throw new SARLException(
					"Attempt to create an infinite number object");
		return canonic(new CommonNumberObject(value));
	}

	@Override
	public StringObject stringObject(String string) {
		if (string == null)
			throw new SARLException("null string");
		return canonic(new CommonStringObject(string));
	}

	@Override
	public IntObject intObject(int value) {
		return canonic(new CommonIntObject(value));
	}

	@Override
	public CharObject charObject(char value) {
		return canonic(new CommonCharObject(value));
	}

	@Override
	public BooleanObject booleanObject(boolean value) {
		return value ? trueObj : falseObj;
	}

	@Override
	public SymbolicObject objectWithId(int index) {
		// objectListReadLock.lock();
		// try {
		return objectList.get(index);
		// } finally {
		// objectListReadLock.unlock();
		// }
	}

	@Override
	public int numObjects() {
		// objectListReadLock.lock();
		// try {
		return objectList.size();
		// } finally {
		// objectListReadLock.unlock();
		// }
	}

	@Override
	public <T extends SymbolicObject> void canonize(T[] objectArray) {
		int n = objectArray.length;

		for (int i = 0; i < n; i++) {
			T element = objectArray[i];

			if (!element.isCanonic())
				objectArray[i] = canonic(element);
		}
	}

	@Override
	public <T extends SymbolicExpression> SymbolicSequence<T> sequence(
			Iterable<? extends T> elements) {
		return canonic(new SimpleSequence<T>(elements));
	}

	@Override
	public <T extends SymbolicExpression> SymbolicSequence<T> sequence(
			T[] elements) {
		return canonic(new SimpleSequence<T>(elements));
	}

	@Override
	public <T extends SymbolicExpression> SymbolicSequence<T> singletonSequence(
			T element) {
		return canonic(new SimpleSequence<T>(element));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends SymbolicExpression> SymbolicSequence<T> emptySequence() {
		return (SymbolicSequence<T>) emptySequence;
	}

}
