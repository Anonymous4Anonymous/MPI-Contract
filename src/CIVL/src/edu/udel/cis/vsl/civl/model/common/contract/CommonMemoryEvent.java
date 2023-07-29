package edu.udel.cis.vsl.civl.model.common.contract;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.udel.cis.vsl.civl.model.IF.CIVLSource;
import edu.udel.cis.vsl.civl.model.IF.contract.DependsEvent;
import edu.udel.cis.vsl.civl.model.IF.contract.MemoryEvent;
import edu.udel.cis.vsl.civl.model.IF.expression.Expression;

public class CommonMemoryEvent extends CommonDependsEvent implements
		MemoryEvent {

	private Set<Expression> memoryUnits = new HashSet<>();

	public CommonMemoryEvent(CIVLSource source, DependsEventKind kind,
			Set<Expression> memoryUnits) {
		super(source, kind);
		assert kind == DependsEventKind.READ || kind == DependsEventKind.WRITE
				|| kind == DependsEventKind.REACH;
		this.memoryUnits = memoryUnits;
	}

	@Override
	public Set<Expression> memoryUnits() {
		return this.memoryUnits;
	}

	@Override
	public int numMemoryUnits() {
		return this.memoryUnits.size();
	}

	@Override
	public boolean isRead() {
		return this.dependsEventKind() == DependsEventKind.READ;
	}

	@Override
	public boolean isWrite() {
		return dependsEventKind() == DependsEventKind.WRITE;
	}

	@Override
	public boolean equalsWork(DependsEvent that) {
		if (that.dependsEventKind() == this.dependsEventKind()) {
			MemoryEvent readOrWrite = (MemoryEvent) that;

			if (this.numMemoryUnits() != readOrWrite.numMemoryUnits())
				return false;
			return memoryUnits.containsAll((Collection<Expression>) readOrWrite
					.memoryUnits());
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		boolean first = true;

		if (isRead())
			result.append("read");
		else if (isWrite())
			result.append("write");
		else
			result.append("access");
		result.append("(");
		for (Expression mu : this.memoryUnits) {
			if (!first)
				result.append(", ");
			else
				first = false;
			result.append(mu);
		}
		result.append(")");
		return result.toString();
	}

	@Override
	public boolean isReach() {
		return this.dependsEventKind() == DependsEventKind.REACH;
	}
}
