package edu.udel.cis.vsl.civl.transform.IF;

import java.util.List;
import java.util.Map;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.ExpressionNode.ExpressionKind;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.FunctionCallNode;
import edu.udel.cis.vsl.abc.ast.node.IF.expression.IdentifierExpressionNode;
import edu.udel.cis.vsl.abc.front.IF.ASTBuilder;
import edu.udel.cis.vsl.abc.program.IF.Program;
import edu.udel.cis.vsl.abc.transform.IF.TransformRecord;
import edu.udel.cis.vsl.abc.transform.IF.Transformer;
import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;

/**
 * This class manages the set of transformations provided by CIVL.
 * 
 * It provides a static method
 * {@link #applyTransformer(Program, String, List, ASTBuilder)} to apply a
 * certain transformer to a given program.
 * 
 * @author Manchun Zheng
 * 
 */
public class TransformerFactory {

	private ASTFactory astFactory;

	private GeneralTransformer generalTransformer;

	// private MacroTransformer macroTransformer;

	private IOTransformer ioTransformer;

	// private OpenMPSimplifier openMPSimplifier;

	private MPI2CIVLTransformer mpi2CivlTransformer;

	private OpenMP2CIVLTransformer openMP2CivlTransformer;

	private OpenMPOrphanTransformer openMPOrphanTransformer;

	private Pthread2CIVLTransformer pthread2CivlTransformer;

	private Cuda2CIVLTransformer cuda2CivlTransformer;

	// private SvcompTransformer svcompTransformer;

	private ContractTransformer contractTransformer;

	// private IntOperationTransformer intOpTransformer;

	private DirectingTransformer directingTransformer;

	/**
	 * A cache for a created {@link LoopContractTransformer}
	 */
	private LoopContractTransformer loopContractTransformer = null;

	public TransformerFactory(ASTFactory astFactory) {
		this.astFactory = astFactory;
	}

	public TransformRecord getGeneralTransformerRecord() {
		return new TransformRecord(GeneralTransformer.CODE,
				GeneralTransformer.LONG_NAME,
				GeneralTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (generalTransformer == null)
					generalTransformer = new GeneralTransformer(astFactory);
				return generalTransformer;
			}
		};
	}

	public TransformRecord getContractTransformerRecord(String targetFunction,
			CIVLConfiguration civlConfig) {
		return new TransformRecord(ContractTransformer.CODE,
				ContractTransformer.LONG_NAME,
				ContractTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (contractTransformer == null)
					contractTransformer = new ContractTransformer(astFactory,
							targetFunction, civlConfig);
				return contractTransformer;
			}
		};
	}

	/**
	 * @return A {@link TransformRecord} for loop contract transformer
	 */
	public TransformRecord getLoopContractTransformerRecord() {
		return new TransformRecord(LoopContractTransformer.CODE,
				LoopContractTransformer.LONG_NAME,
				LoopContractTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (loopContractTransformer == null)
					loopContractTransformer = new LoopContractTransformer(
							astFactory);
				return loopContractTransformer;
			}
		};
	}

	public TransformRecord getMacroTransformerRecord(CIVLConfiguration config) {
		return new TransformRecord(MacroTransformer.CODE,
				MacroTransformer.LONG_NAME,
				MacroTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				return new MacroTransformer(astFactory, config);
			}
		};
	}

	public TransformRecord getIOTransformerRecord(CIVLConfiguration config) {
		return new TransformRecord(IOTransformer.CODE, IOTransformer.LONG_NAME,
				IOTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (ioTransformer == null)
					ioTransformer = new IOTransformer(astFactory, config);
				return ioTransformer;
			}
		};
	}

	public TransformRecord getOpenMPSimplifierRecord(CIVLConfiguration config) {
		return new TransformRecord(OpenMPSimplifier.CODE,
				OpenMPSimplifier.LONG_NAME,
				OpenMPSimplifier.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				return new OpenMPSimplifier(astFactory, config);
			}
		};
	}

	public TransformRecord getMPI2CIVLTransformerRecord() {
		return new TransformRecord(MPI2CIVLTransformer.CODE,
				MPI2CIVLTransformer.LONG_NAME,
				MPI2CIVLTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (mpi2CivlTransformer == null)
					mpi2CivlTransformer = new MPI2CIVLTransformer(astFactory);
				return mpi2CivlTransformer;
			}
		};
	}

	public TransformRecord getOpenMP2CIVLTransformerRecord(
			CIVLConfiguration config) {
		return new TransformRecord(OpenMP2CIVLTransformer.CODE,
				OpenMP2CIVLTransformer.LONG_NAME,
				OpenMP2CIVLTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (openMP2CivlTransformer == null)
					openMP2CivlTransformer = new OpenMP2CIVLTransformer(
							astFactory, config);
				return openMP2CivlTransformer;
			}
		};
	}

	public TransformRecord getOpenMPOrphanTransformerRecord() {
		return new TransformRecord(OpenMPOrphanTransformer.CODE,
				OpenMPOrphanTransformer.LONG_NAME,
				OpenMPOrphanTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (openMPOrphanTransformer == null)
					openMPOrphanTransformer = new OpenMPOrphanTransformer(
							astFactory);
				return openMPOrphanTransformer;
			}
		};
	}

	public TransformRecord getPthread2CIVLTransformerRecord() {
		return new TransformRecord(Pthread2CIVLTransformer.CODE,
				Pthread2CIVLTransformer.LONG_NAME,
				Pthread2CIVLTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (pthread2CivlTransformer == null)
					pthread2CivlTransformer = new Pthread2CIVLTransformer(
							astFactory);
				return pthread2CivlTransformer;
			}
		};
	}

	public TransformRecord getCuda2CIVLTransformerRecord() {
		return new TransformRecord(Cuda2CIVLTransformer.CODE,
				Cuda2CIVLTransformer.LONG_NAME,
				Cuda2CIVLTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (cuda2CivlTransformer == null)
					cuda2CivlTransformer = new Cuda2CIVLTransformer(astFactory);
				return cuda2CivlTransformer;
			}
		};
	}

	public TransformRecord getSvcompTransformerRecord(
			CIVLConfiguration config) {
		return new TransformRecord(SvcompTransformer.CODE,
				SvcompTransformer.LONG_NAME,
				SvcompTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				return new SvcompTransformer(astFactory, config);
			}
		};
	}

	/**
	 * Creates a new instance of a {@link ShortCircuitTransformer}
	 * 
	 * @param config
	 *            A reference to {@link CIVLConfiguration}
	 * @return A {@link TransformRecord} of a {@link ShortCircuitTransformer}.
	 */
	public TransformRecord getShortCircuitTransformerRecord(
			CIVLConfiguration config) {
		return new TransformRecord(ShortCircuitTransformer.CODE,
				ShortCircuitTransformer.LONG_NAME,
				ShortCircuitTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				return new ShortCircuitTransformer(astFactory);
			}
		};
	}

	public TransformRecord getIntOperationTransformerRecord(
			Map<String, String> macros, CIVLConfiguration config) {
		return new TransformRecord(IntOperationTransformer.CODE,
				IntOperationTransformer.LONG_NAME,
				IntOperationTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				return new IntOperationTransformer(astFactory, macros, config);
			}
		};
	}

	public TransformRecord getDirectingTransformerRecord(
			CIVLConfiguration config) {
		return new TransformRecord(DirectingTransformer.CODE,
				DirectingTransformer.LONG_NAME,
				DirectingTransformer.SHORT_DESCRIPTION) {
			@Override
			public Transformer create(ASTFactory astFactory) {
				if (directingTransformer == null)
					directingTransformer = new DirectingTransformer(astFactory,
							config);
				return directingTransformer;
			}
		};
	}

	public static boolean hasFunctionCalls(AST ast, List<String> functions) {
		ASTNode root = ast.getRootNode();

		return checkFunctionCalls(root, functions);
	}

	private static boolean checkFunctionCalls(ASTNode node,
			List<String> functions) {
		int numChildren = node.numChildren();
		boolean result = false;

		for (int i = 0; i < numChildren; i++) {
			ASTNode child = node.child(i);

			if (child != null) {
				result = checkFunctionCalls(child, functions);
				if (result)
					return true;
			}
		}
		if (node instanceof FunctionCallNode) {
			FunctionCallNode functionCall = (FunctionCallNode) node;

			if (functionCall.getFunction()
					.expressionKind() == ExpressionKind.IDENTIFIER_EXPRESSION) {
				IdentifierExpressionNode functionExpression = (IdentifierExpressionNode) functionCall
						.getFunction();
				String functionName = functionExpression.getIdentifier().name();

				if (functions.contains(functionName))
					return true;
			}
		}
		return false;
	}

	public Transformer getOpenMPSimplifier(CIVLConfiguration config) {
		return new OpenMPSimplifier(astFactory, config);
	}

	public Transformer getOpenMP2CIVLTransformer(CIVLConfiguration config) {
		return new OpenMP2CIVLTransformer(astFactory, config);
	}

	//
	// public Transformer getSvcompUnPPTransformer() {
	// if (svcompUnPPTransformer == null)
	// svcompUnPPTransformer = new SvcompUnPPTransformer(astFactory);
	// return svcompUnPPTransformer;
	// }
	//
	// public Transformer getSvcompTransformer() {
	// if (svcompTransformer == null)
	// svcompTransformer = new SvcompTransformer(astFactory);
	// return svcompTransformer;
	// }
	//
	// public Transformer getIntDivTransformer() {
	// if (intDivTransformer == null)
	// intDivTransformer = new IntDivisionTransformer(astFactory);
	// return intDivTransformer;
	// }
}
