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

import edu.udel.cis.vsl.sarl.IF.expr.ArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.BooleanExpression;
import edu.udel.cis.vsl.sarl.IF.expr.NumericExpression;
import edu.udel.cis.vsl.sarl.IF.expr.OffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.ReferenceExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicConstant;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression;
import edu.udel.cis.vsl.sarl.IF.expr.SymbolicExpression.SymbolicOperator;
import edu.udel.cis.vsl.sarl.IF.expr.TupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.UnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArrayElementReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSArraySectionReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSIdentityReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSOffsetReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSTupleComponentReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.VSUnionMemberReference;
import edu.udel.cis.vsl.sarl.IF.expr.valueSetReference.ValueSetReference;
import edu.udel.cis.vsl.sarl.IF.object.IntObject;
import edu.udel.cis.vsl.sarl.IF.object.StringObject;
import edu.udel.cis.vsl.sarl.IF.object.SymbolicObject;
import edu.udel.cis.vsl.sarl.IF.type.SymbolicType;
import edu.udel.cis.vsl.sarl.object.IF.ObjectFactory;
import edu.udel.cis.vsl.sarl.type.IF.SymbolicTypeFactory;

/**
 * An ExpressionFactory is used to instantiate instances of SymbolicExpression.
 * 
 * @author xxxx
 * 
 */
public interface ExpressionFactory {

	/**
	 * Initialize this expression factory. This should be called before it is
	 * used.
	 * 
	 * Preconditions: the object factory and type factory have already been
	 * initialized.
	 */
	void init();

	/**
	 * Returns the numeric expression factory used by this expression factory.
	 * 
	 * @return the numeric expression factory
	 */
	NumericExpressionFactory numericFactory();

	/**
	 * Returns the boolean expression factory used by this expression factory.
	 * 
	 * @return the boolean expression factory
	 */
	BooleanExpressionFactory booleanFactory();

	/**
	 * Returns the symbolic type factory used by this expression factory.
	 * 
	 * @return the symbolic type factory
	 */
	SymbolicTypeFactory typeFactory();

	/**
	 * Returns the object factory used by this expression factory.
	 * 
	 * @return the object factory
	 */
	ObjectFactory objectFactory();

	/**
	 * Returns a comparator on all SymbolicExpression objects. The comparator
	 * defines a total order on the set of symbolic expressions.
	 * 
	 * @return a comparator on symbolic expressions
	 */
	Comparator<SymbolicExpression> comparator();

	/**
	 * Returns an expression with the given operator, type, and argument
	 * sequence.
	 * 
	 * @param operator
	 *            a symbolic operator
	 * @param type
	 *            a symbolic type
	 * @param arguments
	 *            the arguments to the operator as an array
	 * @return the expression specified by above
	 */
	SymbolicExpression expression(SymbolicOperator operator, SymbolicType type,
			SymbolicObject... arguments);

	SymbolicConstant symbolicConstant(StringObject name, SymbolicType type);

	SymbolicType referenceType();

	/**
	 * Returns the special expression "NULL", which has the NULL operator, null
	 * type, and no arguments.
	 * 
	 * @return the NULL symbolic expression
	 */
	SymbolicExpression nullExpression();

	/**
	 * Returns the "null reference", a symbolic expression of reference type
	 * which is not equal to a reference value returned by any of the other
	 * methods, and which cannot be dereferenced.
	 */
	ReferenceExpression nullReference();

	/**
	 * Returns the identity (or "trivial") reference I. This is the reference
	 * characterized by the property that dereference(I,v) returns v for any
	 * symbolic expression v.
	 */
	ReferenceExpression identityReference();

	/**
	 * Given a reference to an array and an index (integer), returns a reference
	 * to the element of the array at that index
	 */
	ArrayElementReference arrayElementReference(
			ReferenceExpression arrayReference, NumericExpression index);

	/**
	 * Given a reference to a tuple, and a field index, returns a reference to
	 * that component of the tuple
	 */
	TupleComponentReference tupleComponentReference(
			ReferenceExpression tupleReference, IntObject fieldIndex);

	/**
	 * Given a reference to a union (expression of union type) and an index of a
	 * member type of that union, returns a reference to the underlying element
	 */
	UnionMemberReference unionMemberReference(
			ReferenceExpression unionReference, IntObject memberIndex);

	/**
	 * Given a ReferenceExpression and a NumericExpression offset, returns an
	 * offsetReference. (reference+offset)
	 * 
	 * @param reference
	 * @param offset
	 * 
	 * @return OffsetReference
	 */
	OffsetReference offsetReference(ReferenceExpression reference,
			NumericExpression offset);

	/* ************ methods for value set references ************ */
	/**
	 * <p>
	 * Test if an array of value set references "refArr0", that is associated
	 * with the given "valueType", contains another array of value set
	 * references "refArr1", that is associated with the given "valueType" as
	 * well.
	 * </p>
	 * 
	 * @param valueType
	 *            the type of the value where the given value set references in
	 *            "refArr0" and "refArr1" refer to
	 * @param refArr0
	 *            a concrete array of value set references
	 * @param refArr1
	 *            a concrete array of value set references
	 * @return a boolean expression representing the result of the test
	 */
	BooleanExpression valueSetContains(SymbolicType valueType,
			SymbolicExpression refArr0, SymbolicExpression refArr1);

	/**
	 * <p>Tests if the two given {@link ValueSetReference}s have NO intersection, i.e.,
	 * if applying the two reference to the same object, if their referred parts
	 * have no overlap.
	 * </p>
	 *
	 * <p>returns the condition that is true iff the given two value set
	 * reference have no intersection.
	 * </p>
	 *
	 * @param valueType
	 *         the type of the value that the two given references can be
	 *         applied to
	 * @param ref0
	 *         an instance of {@link ValueSetReference}
	 * @param ref1
	 *         an instance of {@link ValueSetReference}
	 * @return the condition that is true iff the two value set references have
	 * no intersection
	 */
	BooleanExpression valueSetRefereceNoIntersect(SymbolicType valueType,
			ValueSetReference ref0, ValueSetReference ref1);

	/**
	 * <p>
	 * Apply a default widening operator to a value set template, which is in
	 * the form of an array of value set references and a symbolic type that is
	 * referred by these references.
	 * </p>
	 * 
	 * @param valueType
	 *            the type of the value where the given value set references in
	 *            "refArr" refer to
	 * @param refArr
	 *            a concrete array of value set references
	 * @return a symbolic expression of
	 *         {@link #valueSetTemplateType()} type, where value set
	 *         references have been widening-ed
	 */
	SymbolicExpression valueSetWidening(SymbolicType valueType,
			SymbolicExpression refArr);

	/**
	 * Returns the identity (or "trivial") value set reference I. This is the
	 * reference characterized by the property that dereference(I,v) returns v
	 * for any symbolic expression v.
	 */
	VSIdentityReference vsIdentityReference();

	/**
	 * Given a value set reference to a (set-of) array(s) and an index
	 * (integer), returns a reference to the (set-of) elements of the (set-of)
	 * array(s) at that index
	 */
	VSArrayElementReference vsArrayElementReference(ValueSetReference parent,
			NumericExpression index);

	/**
	 * Given a reference to a (set-of) array(s) and an inclusive lower index
	 * bound, an exclusive upper index bound and a step, returns a reference to
	 * the (set-of) section(s) of the array(s) with the given bounds.
	 */
	VSArraySectionReference vsArraySectionReference(ValueSetReference parent,
			NumericExpression lower, NumericExpression upper,
			NumericExpression step);

	/**
	 * Given a reference to a (set-of) tuple(s), and a field index, returns a
	 * reference to that (set-of) component(s) of the tuple(s).
	 */
	VSTupleComponentReference vsTupleComponentReference(
			ValueSetReference parent, IntObject fieldIndex);

	/**
	 * Given a reference to a (set-of) union(s) (expression of union type) and
	 * an index of a member type of that union, returns a reference to the
	 * (set-of) underlying element(s).
	 */
	VSUnionMemberReference vsUnionMemberReference(ValueSetReference parent,
			IntObject memberIndex);

	/**
	 * Given a reference to a (set-of) value(s) and a integral offset, returns a
	 * reference to a (set-of) value(s), which is obtained by applying the
	 * (set-of) offset(s) to the given (set-of) value(s).
	 */
	VSOffsetReference vsOffsetReference(ValueSetReference parent,
			NumericExpression offset);

	/**
	 * @return the symbolic type of {@link ValueSetReference}s
	 */
	SymbolicType valueSetReferenceType();

	/**
	 * <p>
	 * Given a symbolic type of a symbolic value and a list of
	 * {@link ValueSetReference}s, returns symbolic expression representing a
	 * value set template.
	 * </p>
	 * 
	 * <p>
	 * A value set template consists of a type <code>t</code> of some value and
	 * a set of ValueSetReferences. Applying a value set template to a symbolic
	 * value <code>v</code> of the type <code>t</code> results in a subset of
	 * the value <code>v</code>.
	 * </p>
	 * 
	 * @param valueType
	 *            symbolic type of some value <code>v</code>
	 * @param vsRefs
	 *            references to subsets of some value <code>v</code>
	 * @return a symbolic expression which is a value set template
	 */
	SymbolicExpression valueSetTemplate(SymbolicType valueType,
			ValueSetReference vsRefs[]);

	/**
	 * Returns the type of a value set template. A value set template is a
	 * symbolic expression that can be applied to a value, which is a instance
	 * of {@link SymbolicExpression}, in order to obtain a subset of the value.
	 * 
	 * @return The symbolic type of a value set template.
	 */
	SymbolicType valueSetTemplateType();

	/**
	 * @param type
	 *            a symbolic type
	 * @return true iff the given type is created by the method
	 *         {@link #valueSetTemplateType()}
	 */
	boolean isValueSetTemplateType(SymbolicType type);
}
