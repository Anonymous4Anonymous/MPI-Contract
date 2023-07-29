package edu.udel.cis.vsl.abc.front.fortran.astgen;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.IF.ASTFactory;
import edu.udel.cis.vsl.abc.config.IF.Configuration;
import edu.udel.cis.vsl.abc.front.IF.ASTBuilder;
import edu.udel.cis.vsl.abc.front.IF.ParseTree;
import edu.udel.cis.vsl.abc.front.common.astgen.PragmaFactory;
import edu.udel.cis.vsl.abc.front.fortran.ptree.MFTree;
import edu.udel.cis.vsl.abc.token.IF.SyntaxException;

public class MFASTBuilder implements ASTBuilder {

	private ASTFactory astFactory;

	private PragmaFactory pragmaFactory;

	private Configuration config;

	private String filePath;


	public MFASTBuilder(Configuration configuration,
			ASTFactory astFactory) {
		this.config = configuration;
		this.astFactory = astFactory;
		pragmaFactory = new PragmaFactory(this);
		this.filePath = "";
	}
	
	public MFASTBuilder(Configuration configuration,
			ASTFactory astFactory, String filePath) {
		this.config = configuration;
		this.astFactory = astFactory;
		pragmaFactory = new PragmaFactory(this);
		this.filePath = filePath;
	}

	public MFASTBuilderWorker getWorker(MFTree tree) {
		return new MFASTBuilderWorker(config, tree, astFactory, filePath,
				pragmaFactory);
	}

	@Override
	public AST getTranslationUnit(ParseTree tree) throws SyntaxException {
		MFTree fTree = (MFTree) tree;
		MFASTBuilderWorker worker = new MFASTBuilderWorker(config,
				fTree, astFactory, filePath, pragmaFactory);

		return worker.generateAST();
	}

	@Override
	public ASTFactory getASTFactory() {
		return this.astFactory;
	}

	@Override
	public PragmaFactory getPragmaFactory() {
		return null; // No progma for Fortran
	}
}
