package edu.udel.cis.vsl.civl.run.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.udel.cis.vsl.abc.ast.IF.AST;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode;
import edu.udel.cis.vsl.abc.ast.node.IF.ASTNode.NodeKind;
import edu.udel.cis.vsl.abc.main.ABCExecutor;
import edu.udel.cis.vsl.abc.main.DynamicTask;
import edu.udel.cis.vsl.abc.main.UnitTask;
import edu.udel.cis.vsl.abc.token.IF.SourceFile;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;

public class ParseSystemLibrary implements DynamicTask {

	private ABCExecutor executor;
	private Map<String, String> macros;

	/**
	 * the index of the AST that has been last visited, initially -1
	 */
	private int lastSeenAST = -1;

	/**
	 * true iff the unit task for system library implementation civl-omp.cvl has
	 * been added.
	 * 
	 */
	private boolean civlOmpAdded = false;

	/**
	 * true iff the OpenMP header omp.h is present. Initialized as false.
	 */
	private boolean hasOmpHeader = false;

	public ParseSystemLibrary(ABCExecutor executor,
			Map<String, String> macros) {
		this.executor = executor;
		this.macros = macros;
	}

	private Set<String> getExistingFiles() {
		int num = executor.getNumCompleteUnitTasks();
		Set<String> fileNames = new HashSet<>();

		for (int i = 0; i < num; i++) {
			AST ast = executor.getAST(i);
			Collection<SourceFile> files = ast.getSourceFiles();

			for (SourceFile file : files) {
				String filename = file.getName();

				fileNames.add(filename);
			}
		}
		return fileNames;
	}

	@Override
	public UnitTask[] generateTasks() {
		int num = executor.getNumCompleteUnitTasks();
		List<UnitTask> result = new ArrayList<>();
		Set<String> existingFiles = getExistingFiles();
		Set<String> processedFiles = new HashSet<>();

		for (int i = this.lastSeenAST + 1; i < num; i++) {
			AST ast = executor.getAST(i);
			Collection<SourceFile> files = ast.getSourceFiles();

			for (SourceFile file : files) {
				String filename = file.getName();

				if (!processedFiles.add(filename))
					continue;

				File systemFile = getSystemImplementation(file.getFile());

				if (filename.equals(CIVLConstants.OMP))
					this.hasOmpHeader = true;
				if (systemFile != null) {
					String systemFilename = systemFile.getName();

					if (!existingFiles.contains(systemFilename)
							&& processedFiles.add(systemFilename)) {
						result.add(newUnitTask(systemFile));
					}
				}
			}
			if (!hasOmpHeader && !this.civlOmpAdded) {
				if (this.hasOmpPragma(ast.getRootNode())) {
					result.add(newUnitTask(
							new File(CIVLConstants.CIVL_INCLUDE_PATH,
									CIVLConstants.CIVL_OMP_IMP)));
					this.civlOmpAdded = true;
				}
			}
		}
		this.lastSeenAST = num - 1;
		UnitTask[] tasks = new UnitTask[result.size()];
		return result.toArray(tasks);
	}

	private UnitTask newUnitTask(File systemFile) {
		UnitTask newTask = new UnitTask(new File[]{systemFile});

		newTask.setMacros(macros);
		return newTask;
	}

	private boolean hasOmpPragma(ASTNode node) {
		if (node.nodeKind() == NodeKind.OMP_NODE)
			return true;
		for (ASTNode child : node.children()) {
			if (child != null) {
				if (hasOmpPragma(child))
					return true;
			}
		}
		return false;
	}

	/**
	 * Finds out the file name of the system implementation of a header file,
	 * which stands for a certain system library, such as civlc.cvh, mpi.h,
	 * omp.h, stdio.h, etc.
	 * 
	 * @param file
	 * @return The file name of the system implementation of the given header
	 *         file, or null if there is no implementation of the header file.
	 */
	private File getSystemImplementation(File file) {
		// Check that the path is one of the system include
		// paths. Because a user could create their own "pthread.h"
		// (for example), in which case we don't want to link in the
		// system implementation.
		if (!file.getPath().startsWith("/include"))
			return null;
		// for debugging...
		// System.out.println("Including system file: "+file.getPath());

		String name = file.getName();

		if (CIVLConstants.getAllCivlLibs().contains(name))
			return new File(CIVLConstants.CIVL_INCLUDE_PATH,
					name.substring(0, name.length() - 1) + "l");
		else if (CIVLConstants.getCinterfaces().contains(name))
			return new File(CIVLConstants.CIVL_INCLUDE_PATH,
					name.substring(0, name.length() - 1) + "cvl");
		return null;
	}

}
