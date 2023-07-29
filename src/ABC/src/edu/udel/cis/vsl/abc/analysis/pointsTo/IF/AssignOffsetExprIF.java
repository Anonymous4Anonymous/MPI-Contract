package edu.udel.cis.vsl.abc.analysis.pointsTo.IF;

public interface AssignOffsetExprIF extends AssignExprIF {
	AssignExprIF base();

	AssignOffsetIF offset();
}
