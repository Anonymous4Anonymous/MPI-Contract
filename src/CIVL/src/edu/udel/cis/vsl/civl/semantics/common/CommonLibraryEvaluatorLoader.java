package edu.udel.cis.vsl.civl.semantics.common;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.udel.cis.vsl.civl.config.IF.CIVLConfiguration;
import edu.udel.cis.vsl.civl.config.IF.CIVLConstants;
import edu.udel.cis.vsl.civl.dynamic.IF.SymbolicUtility;
import edu.udel.cis.vsl.civl.model.IF.ModelFactory;
import edu.udel.cis.vsl.civl.semantics.IF.Evaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluator;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryEvaluatorLoader;
import edu.udel.cis.vsl.civl.semantics.IF.LibraryLoaderException;
import edu.udel.cis.vsl.civl.semantics.IF.SymbolicAnalyzer;

public class CommonLibraryEvaluatorLoader implements LibraryEvaluatorLoader {

	/* *************************** Instance Fields ************************* */

	/**
	 * The cache of known library executors.
	 */
	private Map<String, LibraryEvaluator> libraryEvaluatorCache = new LinkedHashMap<>();

	private CIVLConfiguration civlConfig;

	public CommonLibraryEvaluatorLoader(CIVLConfiguration civlConfig) {
		this.civlConfig = civlConfig;
	}

	@Override
	public LibraryEvaluator getLibraryEvaluator(String name,
			Evaluator primaryEvaluator, ModelFactory modelFacotry,
			SymbolicUtility symbolicUtil, SymbolicAnalyzer symbolicAnalyzer)
			throws LibraryLoaderException {
		LibraryEvaluator result;

		if (name.equals("assert"))
			name = "asserts";
		if (name.equals("civl-mpi"))
			name = "mpi";
		if (name.equals("civl-pthread"))
			name = "pthread";
		result = libraryEvaluatorCache.get(name);
		if (result == null) {
			String aClassName = className(name, "Evaluator");

			try {
				@SuppressWarnings("unchecked")
				Class<? extends LibraryEvaluator> aClass = (Class<? extends LibraryEvaluator>) Class
						.forName(aClassName);
				Constructor<? extends LibraryEvaluator> constructor = aClass
						.getConstructor(String.class, Evaluator.class,
								ModelFactory.class, SymbolicUtility.class,
								SymbolicAnalyzer.class,
								CIVLConfiguration.class,
								LibraryEvaluatorLoader.class);

				result = constructor.newInstance(name, primaryEvaluator,
						modelFacotry, symbolicUtil, symbolicAnalyzer,
						this.civlConfig, this);
			} catch (Exception e) {
				throw new LibraryLoaderException(e.getMessage());
			}
			libraryEvaluatorCache.put(name, result);
		}
		result.setPrimaryEvaluator(primaryEvaluator);
		return result;
	}

	/* *************************** Private Methods ************************* */

	/**
	 * Computes the full name of the class of an enabler/executor of a library.
	 * 
	 * @param library
	 *            The name of the library.
	 * @param suffix
	 *            "Enabler" or "Executor", depending on whether the enabler or
	 *            executor is to be used.
	 * @return The full name of the class of the enabler or executor of the
	 *         given library.
	 */
	private String className(String library, String suffix) {
		String result = CIVLConstants.LIBRARY_PREFIX + library + ".Lib"
				+ library + suffix;

		return result;
	}

}
