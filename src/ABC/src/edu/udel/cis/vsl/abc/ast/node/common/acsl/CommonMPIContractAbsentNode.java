package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.token.IF.Source;

public class CommonMPIContractAbsentNode
        extends CommonMPIContractExpressionNode
        implements MPIContractAbsentNode {

    public CommonMPIContractAbsentNode(Source source,
            List<ExpressionNode> events) {
        super(source, events, MPIContractExpressionKind.MPI_ABSENT,
                "\\absent");
    }

    @Override
    public MPIContractAbsentEventNode absentEvent() {
        return (MPIContractAbsentEventNode) child(0);
    }

    @Override
    public MPIContractAbsentEventNode fromEvent() {
        return (MPIContractAbsentEventNode) child(1);
    }

    @Override
    public MPIContractAbsentEventNode untilEvent() {
        return (MPIContractAbsentEventNode) child(2);
    }

    @Override
    public MPIContractAbsentNode copy() {
        List<ExpressionNode> events = new LinkedList<>();

        for (int i = 0; i < 3; i++)
            events.add(duplicate(getArgument(i)));
        return new CommonMPIContractAbsentNode(getSource(), events);
    }

    @Override
    public boolean isSideEffectFree(boolean errorsAreSideEffects) {
        return true;
    }

    @Override
    public boolean isConstantExpression() {
        return false;
    }

    @Override
    protected void printBody(PrintStream out) {
        out.print(toString());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append("\\absent(");
        sb.append(absentEvent().prettyRepresentation() + ", ");
        sb.append(fromEvent().prettyRepresentation() + ", ");
        sb.append(untilEvent().prettyRepresentation() + ")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MPIContractAbsentNode) {
            MPIContractAbsentNode other = (MPIContractAbsentNode) obj;

            if (other.absentEvent().equals(absentEvent()))
                if (other.fromEvent().equals(fromEvent()))
                    if (other.untilEvent().equals(untilEvent()))
                        return true;
        }
        return false;
    }
}
