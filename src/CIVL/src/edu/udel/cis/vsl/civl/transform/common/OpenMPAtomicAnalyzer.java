package edu.udel.cis.vsl.civl.transform.common;

import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.OperatorNode.Operator;
import edu.udel.cis.vsl.abc.ast.node.IF.omp.OmpAtomicNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.ExpressionStatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode;
import edu.udel.cis.vsl.abc.ast.node.IF.statement.StatementNode.StatementKind;
import edu.udel.cis.vsl.civl.model.IF.CIVLInternalException;
import edu.udel.cis.vsl.civl.model.IF.CIVLSyntaxException;
import edu.udel.cis.vsl.civl.model.IF.CIVLUnimplementedFeatureException;
import edu.udel.cis.vsl.civl.util.IF.Pair;
import edu.udel.cis.vsl.civl.util.IF.Triple;

/**
 * <p>
 * This class deals with OpenMP atomic constructs. Given an sub-AST representing
 * an OpenMP atomic construct, an analysis on it will be performed if this
 * atomic construct is non-sequentially consistent (which is by default). If the
 * atomic construct is specified with a seq_cst clause, the atomic construct is
 * sequentially consistent and no analysis is needed.
 * </p>
 * 
 * <p>
 * For non-sequentially consistent atomic regions, there are flushes at its
 * entry and exit with a list of objects. The objects are obtained by analysis
 * depending on the atomic clauses: read, write, update or capture. Details for
 * the analysis can be found in OpenMP specification 4.5 Page 155.
 * </p>
 * 
 * <p>
 * For sequentially consistent atomic regions, there are flushes without list at
 * its entry and exit.
 * </p>
 * 
 * @author xxxx
 *
 */
class OpenMPAtomicAnalyzer {

	/**
	 * result of the analysis performed by {@link OpenMPAtomicAnalyzer}
	 * 
	 * @author xxxx
	 *
	 */
	class OpenMPAtomicAnalysis {
		/**
		 * a list of expressions that will be flushed before and after an atomic
		 * region:
		 */
		private List<ExpressionNode> flushList;

		/**
		 * a list of pairs, each of which are two expressions refer to separate
		 * storage locations
		 */
		private List<Pair<ExpressionNode, ExpressionNode>> separatePairs;

		OpenMPAtomicAnalysis(List<ExpressionNode> flushList,
				List<Pair<ExpressionNode, ExpressionNode>> separatePairs) {
			this.flushList = flushList;
			this.separatePairs = separatePairs;
			if (this.flushList == null)
				this.flushList = new LinkedList<>();
			if (this.separatePairs == null)
				this.separatePairs = new LinkedList<>();
		}

		/**
		 * 
		 * @return a list of expressions that will be flushed before and after
		 *         an atomic region
		 */
		List<ExpressionNode> flushList() {
			return flushList;
		}

		/**
		 * 
		 * @return a list of pairs, each of which are two expressions refer to
		 *         separate storage locations
		 */
		List<Pair<ExpressionNode, ExpressionNode>> separatePairs() {
			return separatePairs;
		}
	}

	/**
	 * <p>
	 * perform an analysis on an atomic construct and returns a list of
	 * expressions that will be flushed at the entry and exit of the atomic
	 * region
	 * </p>
	 * 
	 * @param atomicNode
	 *            an OmpAtomicNode representing an atomic construct
	 * @return a list expressions that will be flushed at the entry and exit of
	 *         the atomic region; if the list is empty, flushes will be without
	 *         list
	 */
	OpenMPAtomicAnalysis analyzeFlushList(OmpAtomicNode atomicNode) {
		if (atomicNode.seqConsistent() || atomicNode.statementNode() == null)
			return new OpenMPAtomicAnalysis(null, null);
		switch (atomicNode.atomicClause()) {
			case CAPTURE :
				return analyzeCapture(atomicNode.statementNode());
			case READ :
				return analyzeRead(atomicNode.statementNode());
			case UPDATE :
				return analyzeUpdate(atomicNode.statementNode());
			case WRITE :
				return analyzeWrite(atomicNode.statementNode());
			default :
				throw new CIVLInternalException(
						"unknown atomic clause : " + atomicNode.atomicClause(),
						atomicNode.getSource());
		}
	}

	/**
	 * <p>
	 * stmt can only have the form :<code>v = x;</code> v and x must be an
	 * l-value expression of scalar type; v must NOT access the storage location
	 * designated by x.
	 * </p>
	 * 
	 * @param stmt
	 * @return
	 */
	private OpenMPAtomicAnalysis analyzeRead(StatementNode stmt) {
		Triple<Boolean, ExpressionNode, ExpressionNode> isAssign = isScalarTypeAssignStmt(
				stmt);
		List<ExpressionNode> flushList = new LinkedList<>();
		List<Pair<ExpressionNode, ExpressionNode>> separatePairs;
		boolean error = !isAssign.first;

		if (!error)
			error = !isAssign.second.isLvalue() || !isAssign.third.isLvalue();
		if (error)
			throw new CIVLSyntaxException("invalid statement "
					+ stmt.prettyRepresentation() + " for omp atomic read",
					stmt.getSource());
		flushList.add(isAssign.third);
		separatePairs = separate(isAssign.second, isAssign.third);
		return new OpenMPAtomicAnalysis(flushList, separatePairs);
	}

	/**
	 * <p>
	 * stmt can only have the form :<code>x = expr;</code> x must be an l-value
	 * expression of scalar type; expr must NOT access the storage location
	 * designated by x.
	 * </p>
	 * 
	 * @param stmt
	 * @return
	 */
	private OpenMPAtomicAnalysis analyzeWrite(StatementNode stmt) {
		Triple<Boolean, ExpressionNode, ExpressionNode> isAssign = isScalarTypeAssignStmt(
				stmt);
		List<ExpressionNode> flushList = new LinkedList<>();
		List<Pair<ExpressionNode, ExpressionNode>> separatePairs;
		boolean error = !isAssign.first;

		if (!error)
			error = !isAssign.second.isLvalue();
		if (error)
			throw new CIVLSyntaxException("invalid statement "
					+ stmt.prettyRepresentation() + " for omp atomic read",
					stmt.getSource());
		flushList.add(isAssign.second);
		separatePairs = separate(isAssign.second, isAssign.third);
		return new OpenMPAtomicAnalysis(flushList, separatePairs);
	}

	/**
	 * <p>
	 * stmt can have the forms :<code>
	 * x++;
	 * x--;
	 * ++x;
	 * --x;
	 * x binop= expr;
	 * x = x binop expr; 
	 * x = expr binop x;
	 * </code> x must be an l-value expression of scalar type; expr refers to
	 * different object from x
	 * </p>
	 * 
	 * @param stmt
	 * @return
	 */
	private OpenMPAtomicAnalysis analyzeUpdate(StatementNode stmt) {
		Pair<Boolean, ExpressionNode> isPPIncDec = isPrePostIncDecStmt(stmt);
		List<ExpressionNode> flushList = new LinkedList<>();
		List<Pair<ExpressionNode, ExpressionNode>> separateList = new LinkedList<>();

		if (isPPIncDec.left && isPPIncDec.right.isLvalue()) {
			flushList.add(isPPIncDec.right);
			return new OpenMPAtomicAnalysis(flushList, separateList);

		}

		Triple<Boolean, ExpressionNode, ExpressionNode> isAssign = isBinopAssignStmt(
				stmt);

		if (isAssign.first && isAssign.second.isLvalue()) {
			flushList.add(isAssign.second);
			separateList = separate(isAssign.second, isAssign.third);
			return new OpenMPAtomicAnalysis(flushList, separateList);
		}

		throw new CIVLSyntaxException("invalid statement "
				+ stmt.prettyRepresentation() + " for omp atomic update");
	}

	/**
	 * <p>
	 * stmt can only have the form :<code>
	 * v = x++;
	 * v = x--;
	 * v = ++x;
	 * v = --x;
	 * v = x binop= expr; 
	 * v = x = x binop expr; 
	 * v = x = expr binop x;
	 * </code> or <code>
	 * {v = x; x binop= expr;}
	 * {x binop= expr; v = x;} 
	 * {v = x; x = x binop expr;} 
	 * {v = x; x = expr binop x;} 
	 * {x = x binop expr; v = x;} 
	 * {x = expr binop x; v = x;} 
	 * {v = x; x = expr;}
	 * {v = x; x++;}
	 * {v = x; ++x;}
	 * {++x; v = x;}
	 * {x++; v = x;}
	 * {v = x; x--;}
	 * {v = x; --x;}
	 * {--x; v = x;}
	 * {x--; v = x;}
	 * </code> v and x must be an l-value expression of scalar type; v and expr
	 * refers to different object from x
	 * </p>
	 * 
	 * @param stmt
	 * @return
	 */
	private OpenMPAtomicAnalysis analyzeCapture(StatementNode stmt) {
		throw new CIVLUnimplementedFeatureException(
				"support omp atomic capture");
	}

	/* ****************** Statement Patterns ******************** */

	/**
	 * @param stmt
	 *            a StatementNode
	 * @return a triple (true, lhs, rhs) if the given statement is an assignment
	 *         and both sides have scalar type; otherwise, returns a triple
	 *         (false, null, null)
	 */
	private Triple<Boolean, ExpressionNode, ExpressionNode> isScalarTypeAssignStmt(
			StatementNode stmt) {
		if (stmt.statementKind() == StatementKind.EXPRESSION) {
			ExpressionNode expr = ((ExpressionStatementNode) stmt)
					.getExpression();

			return isScalarTypeAssign(expr);
		}
		return new Triple<>(false, null, null);
	}

	/**
	 * @param expr
	 *            a ExpressionNode
	 * @return a triple (true, lhs, rhs) if the given expression is an
	 *         assignment and both sides have scalar type; otherwise, returns a
	 *         triple (false, null, null)
	 */
	private Triple<Boolean, ExpressionNode, ExpressionNode> isScalarTypeAssign(
			ExpressionNode expr) {
		if (expr.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode opNode = (OperatorNode) expr;
			boolean not = false;

			if (opNode.getOperator() != Operator.ASSIGN)
				not = true;
			if (!opNode.getArgument(0).getType().isScalar())
				not = true;
			if (!opNode.getArgument(1).getType().isScalar())
				not = true;
			if (!not)
				return new Triple<>(true, opNode.getArgument(0),
						opNode.getArgument(1));
		}
		return new Triple<>(false, null, null);
	}

	/**
	 * @param stmt
	 *            a StatementNode
	 * @return a triple (true, x, expr) if the given statement is one of these :
	 *         x = x binop expr, x = expr binop x, x = binop= expr; otherwise,
	 *         returns a pair (false, null)
	 */
	private Triple<Boolean, ExpressionNode, ExpressionNode> isBinopAssignStmt(
			StatementNode stmt) {
		if (stmt.statementKind() == StatementKind.EXPRESSION) {
			ExpressionNode expr = ((ExpressionStatementNode) stmt)
					.getExpression();

			return isBinopAssignExpr(expr);
		}
		return new Triple<>(false, null, null);
	}

	/**
	 * @param stmt
	 *            a ExpressionNode
	 * @return a triple (true, x, expr) if the given expression is one of these
	 *         : x = x binop expr, x = expr binop x, x = binop= expr; otherwise,
	 *         returns a pair (false, null)
	 */
	private Triple<Boolean, ExpressionNode, ExpressionNode> isBinopAssignExpr(
			ExpressionNode expr) {
		Triple<Boolean, ExpressionNode, ExpressionNode> isAssign = isScalarTypeAssign(
				expr);

		if (isAssign.first) {
			ExpressionNode x = isAssign.second;
			ExpressionNode rhs = isAssign.third;

			if (rhs.expressionKind() == ExpressionKind.OPERATOR) {
				OperatorNode opNode = (OperatorNode) rhs;

				if (isBinop(opNode.getOperator())) {
					boolean zerothEqX = x.equals(opNode.getArgument(0));
					boolean firstEqX = x.equals(opNode.getArgument(1));

					if (zerothEqX)
						return new Triple<>(true, x, opNode.getArgument(1));
					if (firstEqX)
						return new Triple<>(true, x, opNode.getArgument(0));
				}
			}
		} else if (expr.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode opNode = (OperatorNode) expr;

			if (isBinopEQ(opNode.getOperator()))
				return new Triple<>(true, opNode.getArgument(0),
						opNode.getArgument(1));
		}
		return new Triple<>(false, null, null);
	}

	/**
	 * @param stmt
	 *            a StatementNode
	 * @return a pair (true, x) if the given statement is one of these : x++,
	 *         x--, ++x, --x; otherwise, returns a pair (false, null)
	 */
	private Pair<Boolean, ExpressionNode> isPrePostIncDecStmt(
			StatementNode stmt) {
		if (stmt.statementKind() == StatementKind.EXPRESSION) {
			ExpressionNode expr = ((ExpressionStatementNode) stmt)
					.getExpression();

			return isPrePostIncDecExpr(expr);
		}
		return new Pair<>(false, null);
	}

	/**
	 * @param expr
	 *            a ExpressionNode
	 * @return a pair (true, x) if the given expression is one of these : x++,
	 *         x--, ++x, --x; otherwise, returns a pair (false, null)
	 */
	private Pair<Boolean, ExpressionNode> isPrePostIncDecExpr(
			ExpressionNode expr) {
		if (expr.expressionKind() == ExpressionKind.OPERATOR) {
			OperatorNode opNode = (OperatorNode) expr;

			if (isPrePostIncDec(opNode.getOperator()))
				return new Pair<>(true, opNode.getArgument(0));
		}
		return new Pair<>(false, null);
	}

	/**
	 * 
	 * @param op
	 * @return true if op is one of these: <code> +,*,-,/,&,ˆ,|,<<,or>> </code>
	 */
	private boolean isBinop(Operator op) {
		switch (op) {
			case BITAND :
			case BITOR :
			case BITXOR :
			case DIV :
			case MINUS :
			case PLUS :
			case SHIFTLEFT :
			case SHIFTRIGHT :
			case TIMES :
				return true;
			default :
				return false;
		}
	}

	/**
	 * 
	 * @param op
	 * @return true if op is one of these:
	 *         <code> +=,*=,-=,/=,&=,ˆ=,|=,<<=,or>>=</code>
	 */
	private boolean isBinopEQ(Operator op) {
		switch (op) {
			case BITANDEQ :
			case BITOREQ :
			case BITXOREQ :
			case DIVEQ :
			case MINUSEQ :
			case PLUSEQ :
			case SHIFTLEFTEQ :
			case SHIFTRIGHTEQ :
			case TIMESEQ :
				return true;
			default :
				return false;
		}
	}

	/**
	 * 
	 * @param op
	 * @return true if op is one of these: <code> ++, -- </code>
	 */
	private boolean isPrePostIncDec(Operator op) {
		switch (op) {
			case POSTDECREMENT :
			case PREDECREMENT :
			case POSTINCREMENT :
			case PREINCREMENT :
				return true;
			default :
				return false;
		}
	}

	/* ****************** Check Restrictions ******************** */
	List<Pair<ExpressionNode, ExpressionNode>> separate(ExpressionNode x,
			ExpressionNode y) {
		// TODO: complete me:
		return new LinkedList<>();
	}
}
