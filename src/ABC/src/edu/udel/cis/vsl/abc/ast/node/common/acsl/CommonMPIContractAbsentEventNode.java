package edu.udel.cis.vsl.abc.ast.node.common.acsl;

import edu.udel.cis.vsl.abc.ast.node.IF.acsl.MPIContractAbsentEventNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.err.IF.ABCRuntimeException;
import edu.udel.cis.vsl.abc.token.IF.Source;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommonMPIContractAbsentEventNode extends CommonMPIContractExpressionNode
        implements MPIContractAbsentEventNode {

    private MPIAbsentEventKind kind;

    public CommonMPIContractAbsentEventNode(Source source,
            MPIAbsentEventKind kind,
            List<ExpressionNode> args) {
        super(source, args, MPIContractExpressionKind.MPI_ABSENT_EVENT,
                "\\absent_event");
        this.kind = kind;
    }

    @Override
    public MPIAbsentEventKind absentEventKind() {
        return kind;
    }

    @Override
    public ExpressionNode[] arguments() {
        ExpressionNode[] args = new ExpressionNode[numChildren()];

        for (int i = 0; i < args.length; i++)
            args[i] = getArgument(i);
        return args;
    }

    @Override
    public MPIContractAbsentEventNode copy() {
        List<ExpressionNode> args = new LinkedList<>();
        int numArgs = numArguments();

        for (int i = 0; i < numArgs; i++)
            args.add(duplicate(getArgument(i)));
        return new CommonMPIContractAbsentEventNode(getSource(), kind, args);
    }
    
    @Override
    public boolean isSideEffectFree(boolean errorsAreSideEffects) {
        return true;
    }

    @Override
    protected void printBody(PrintStream out) {
        out.print(toString());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        switch (kind) {
            case SENDTO:
                sb.append("\\sendto");
                break;
            case SENDFROM:
                sb.append("\\sendfrom");
                break;
            case ENTER:
                sb.append("\\enter");
                break;
            case EXIT:
                sb.append("\\exit");
                break;
            default:
                throw new ABCRuntimeException("unknown MPI absent event kind " +
                                              kind);
        }
        sb.append("(");

        int numArgs = this.numArguments();

        for (int i = 0; i < numArgs; i++) {
            sb.append(getArgument(i).prettyRepresentation());
            if (i < numArgs - 1)
                sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MPIContractAbsentEventNode) {
            MPIContractAbsentEventNode other = (MPIContractAbsentEventNode) obj;

            if (other.absentEventKind() != absentEventKind())
                return false;
            return Arrays.equals(this.arguments(), other.arguments());
        }
        return false;
    }
}
