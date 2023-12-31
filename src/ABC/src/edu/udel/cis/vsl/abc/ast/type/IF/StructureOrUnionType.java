package edu.udel.cis.vsl.abc.ast.type.IF;

import edu.udel.cis.vsl.abc.ast.entity.IF.TaggedEntity;

/**
 * <p>
 * A structure or union type. Such a type is specified by (0) a key, (1) a bit
 * which says whether this is a structure or a union, (2) a tag (which is a
 * string which names the type), and (3) a sequence of Fields, which are the
 * members of the type. The type may be incomplete (the fields have not yet been
 * specified) or complete (the fields have been specified).
 * </p>
 * 
 * <p>
 * Two instances are considered equal if they have equal keys, isStruct bits,
 * and tags.
 * </p>
 * 
 * 
 * TODO: Idea for dealing better with anonymous members. Introduce new
 * methods getMember(String), getNumMembers(), getMember(int). Check that no two
 * members have the same name --- even if they are deep.
 * 
 * @author xxxx
 */
public interface StructureOrUnionType
		extends UnqualifiedObjectType, TaggedEntity {

	/**
	 * Returns the key associated to this instance. The key is used in the
	 * determination of equality of two instances of StructureOrUnionType.
	 * 
	 * @return the key
	 */
	Object getKey();

	/**
	 * Returns the tag of this type. The tag is the string that occurs in the
	 * declaration. For example, in a declaration "struct foo {...}", the tag is
	 * "foo".
	 * 
	 * @return the tag of this type
	 */
	String getTag();

	/**
	 * Is this a struct, not a union?
	 * 
	 * @return true if struct, false if union
	 */
	boolean isStruct();

	/**
	 * Is this a union, not a struct?
	 * 
	 * @return true if union, false if struct
	 */
	boolean isUnion();

	/**
	 * Returns the number of fields (members) of this structure or union type.
	 * 
	 * @exception RuntimeException
	 *                if this type is not yet complete
	 * @return the number of fields in this type
	 */
	int getNumFields();

	/**
	 * Returns the index-th field in this structure or union type.
	 * 
	 * @param index
	 *            an integer between 0 and the number of fields minus 1,
	 *            inclusive
	 * @return the index-th field
	 * @exception RuntimeException
	 *                if this type is not yet complete
	 */
	Field getField(int index);

	Field getField(String fieldName);

	/**
	 * Returns an iterator over the fields in this type, or null if this type is
	 * not yet complete.
	 * 
	 * @return an iterable over the fields, in order, or null
	 */
	Iterable<Field> getFields();

	/**
	 * Completes this structure of union type by specifying its contents, i.e.,
	 * the list of fields.
	 * 
	 * @exception RuntimeException
	 *                if this type is already complete
	 * 
	 * @param fields
	 *            an ordered list of fields
	 */
	void complete(Iterable<Field> fields);

	/** Make incomplete. */
	void clear();

	@Override
	StructureOrUnionType getType();

	/**
	 * Finds the field with the given name by looking not only in the immediate
	 * scope but also recursively through anonymous structure and union members.
	 * According to C11, these deep fields are also members of this structure or
	 * union.
	 * 
	 * @param fieldName
	 *            the name of the field to search for
	 * @return the sequence of fields that navigate to the deep field named
	 *         <code>fieldName</code>, or <code>null</code> if no such such
	 *         field exists. The first elements of this sequence will be an
	 *         immediate field. The last element will be the Field named
	 *         <code>fieldName</code>.
	 */
	Field[] findDeepField(String fieldName);
}
