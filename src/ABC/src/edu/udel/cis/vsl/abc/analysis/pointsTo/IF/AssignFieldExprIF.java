package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

import edu.udel.cis.vsl.abc.ast.type.IF.Field;

public interface AssignFieldExprIF extends AssignExprIF {

	AssignExprIF struct();

	Field field();
}
