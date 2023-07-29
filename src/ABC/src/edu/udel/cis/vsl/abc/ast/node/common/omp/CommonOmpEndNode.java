package edu.udel.cis.vsl.abc.ast.node.common.omp;

import java.io.PrintStream;

import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpEndNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonOmpEndNode extends CommonOmpNode implements OmpEndNode {

	private OmpEndType endType;

	public CommonOmpEndNode(Source source, OmpEndType ompEndType) {
		super(source);
		this.endType = ompEndType;
	}

	@Override
	public OmpNodeKind ompNodeKind() {
		return OmpNodeKind.DECLARATIVE;
	}

	@Override
	public CommonOmpEndNode copy() {
		return new CommonOmpEndNode(getSource(), this.endType);
	}

	@Override
	public OmpEndType ompEndType() {
		return this.endType;
	}

	@Override
	protected void printBody(PrintStream out) {
		switch (this.endType) {
			case PARALLEL :
				out.print("END PARALLEL");
				break;
			case SECTIONS :
				out.print("END SECTIONS");
				break;
			case DO :
				out.print("END DO");
				break;
			default :
		}
	}

	@Override
	public StatementKind statementKind() {
		return StatementKind.OMP;
	}

	@Override
	public BlockItemKind blockItemKind() {
		return BlockItemKind.OMP_DECLARATIVE;
	}

}
