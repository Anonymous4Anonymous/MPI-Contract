package edu.udel.cis.vsl.civl.semantics.common;

import edu.udel.cis.vsl.civl.model.IF.type.CIVLArrayType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLStructOrUnionType;
import edu.udel.cis.vsl.civl.model.IF.type.CIVLType;
import edu.udel.cis.vsl.civl.model.IF.type.StructOrUnionField;

public class Utils {

	/**
	 * <p>
	 * Tests if a {@link CIVLType} contains a CIVL-C sequence (i.e., incomplete
	 * array) subtype
	 * </p>
	 * 
	 * <p>
	 * Note this method assumes that sequence objects cannot be malloc-ed.
	 * </p>
	 * 
	 * @param varType
	 *            the type of CIVL-C variable, instance of {@link CIVLType}
	 * @return true iff the given CIVLType contains a sequence sub-type
	 */
	public static boolean containSequenceType(CIVLType varType) {
		switch (varType.typeKind()) {
			case ARRAY :
				return !((CIVLArrayType) varType).isComplete();
			case COMPLETE_ARRAY :
				return containSequenceType(
						((CIVLArrayType) varType).elementType());
			case STRUCT_OR_UNION :
				Iterable<StructOrUnionField> fields = ((CIVLStructOrUnionType) varType)
						.fields();

				for (StructOrUnionField field : fields)
					if (containSequenceType(field.type()))
						return true;
				return false;
			case BUNDLE :
			case DOMAIN :
			case ENUM :
			case FUNCTION : // no variable in CIVL-C can have function type
			case HEAP :
			case MEM :
			case POINTER :
			case PRIMITIVE :
			case SET :
				return false;
			default :
				assert false : "unreachable";
				return false;
		}
	}
}
