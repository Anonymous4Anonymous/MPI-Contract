package edu.udel.cis.vsl.abc.main;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.udel.cis.vsl.abc.config.IF.Configuration.Architecture;
import edu.udel.cis.vsl.abc.config.IF.Configurations;
import edu.udel.cis.vsl.abc.config.IF.Configurations.Language;
import edu.udel.cis.vsl.abc.err.IF.ABCException;
import edu.udel.cis.vsl.abc.transform.IF.Transform;
import edu.udel.cis.vsl.abc.transform.IF.TransformRecord;

/**
 * A {@link TranslationTask} object specifies all of the options and parameters
 * needed to perform a complete translation task, from preprocessing source
 * files, to linking (i.e., merging), analyzing, and transforming.
 * 
 * @author xxxx
 */
public class TranslationTask {

	public static enum TranslationStage {
		/**
		 * For each source unit, construct preprocessor output stream but do not
		 * consume the tokens from it.
		 */
		PREPROCESS,
		/**
		 * For each source unit, construct the preprocessor output stream and
		 * consume all token emanating from it, printing them to the
		 * {@link PrintStream} specified by method
		 * {@link TranslationTask#getOut()}.
		 */
		PREPROCESS_CONSUME,
		/** Preprocess and parse each source unit to form a parse tree. */
		PARSE,
		/** Preprocess, parse, and generate an AST for each translation unit. */
		GENERATE_ASTS,
		/**
		 * Preprocess, parse, generate and analyze an AST for each translation
		 * unit.
		 */
		ANALYZE_ASTS,
		/**
		 * Preprocess, parse, generate, analyze and transform the AST for each
		 * translation unit.
		 */
		TRANSFORM_ASTS,
		/**
		 * Preprocess, parse, generate, analyze, and transform an AST for each
		 * translation unit, and link those translation units to form a whole,
		 * analyzed program.
		 */
		LINK,
		/**
		 * Preprocess, parse, generate, analyze, and transform the translation
		 * units, link the translation units and and analyze the resulting
		 * program, and finally perform the specified transformations to the
		 * whole program.
		 */
		TRANSFORM_PROGRAM
	}

	/**
	 * The objects specifying how to construct each translation unit. Each
	 * translation unit is constructed by parsing and preprocessing a sequence
	 * of files. There is no default value; must be set at construction.
	 */
	private UnitTask[] unitTasks;

	/**
	 * At which stage of translation should this task stop?
	 */
	private TranslationStage stage = TranslationStage.TRANSFORM_PROGRAM;

	/**
	 * The language used to link the translation units. Default is determined by
	 * the languages of the unit tasks. If all are in one language, that is used
	 * as the link language. Otherwise, {@link Language#CIVL_C} is the link
	 * language.
	 */
	private Language linkLanguage = Language.CIVL_C;

	/**
	 * Records for the transformations to apply to the program after linking the
	 * translation units. Default is empty list.
	 */
	private List<TransformRecord> transformRecords = new LinkedList<>();

	/**
	 * Output stream: where to print human-readable descriptions of translation
	 * artifacts. Default is standard out.
	 */
	private PrintStream out = System.out;

	/**
	 * If true, show preprocessor output as individual tokens with complete
	 * token information, instead of just the text. Very detailed and long
	 * output. Default is <code>false</code>.
	 */
	private boolean preprocTokens = false;

	/**
	 * Print out intermediate artifacts? Default is <code>false</code>.
	 */
	private boolean verbose = false;

	/**
	 * Print out program in original language, as opposed to a hierarchical
	 * representation. Default is <code>false</code>.
	 */
	private boolean prettyPrint = false;

	/**
	 * Show symbol and type tables. Default is <code>false</code>.
	 */
	private boolean showTables = false;

	/**
	 * Show the timing of each phase. Default is <code>false</code>.
	 */
	private boolean showTime = false;

	/**
	 * A special task used to show the difference between exactly two ASTs.
	 * Default is <code>false</code>.
	 */
	private boolean showDiff = false;

	/**
	 * Print nothing? Default is <code>true</code>.
	 */
	private boolean silent = true;

	/**
	 * Print the list of functions that are used but do not have definitions?
	 * Default is <code>false</code>.
	 */
	private boolean showUndefinedFunctions = false;

	/**
	 * Summarize files and external entities.
	 */
	private boolean summarize = false;

	/**
	 * Is this an SV-COMP problem?
	 */
	private boolean svcomp = false;

	/**
	 * Are the GNU extensions to the C language allowed?
	 */
	private boolean gnuc = false;

	/**
	 * Which architecture is this translation targeting?
	 */
	private Architecture architecture = Architecture.UNKNOWN;

	private DynamicTask dynamicTask = null;

	/**
	 * Constructs a new translation task from given unit tasks. All other
	 * parameters have their default values.
	 * 
	 * @param unitTasks
	 *            the unit tasks
	 */
	public TranslationTask(UnitTask[] unitTasks) {
		this.unitTasks = unitTasks;
		initialize();
	}

	/**
	 * Constructs new translation task from a list of list of files. Each
	 * element of the list represents one translation unit. The files in that
	 * element are the source files which are concatenated to form the input to
	 * the preprocessor.
	 * 
	 * @param sourceUnits
	 *            list of preprocessor input units
	 */
	public TranslationTask(File[][] sourceUnits) {
		int numTasks = sourceUnits.length;

		unitTasks = new UnitTask[numTasks];
		for (int i = 0; i < numTasks; i++) {
			unitTasks[i] = new UnitTask(sourceUnits[i]);
		}
		initialize();
	}

	// Helpers...

	private Iterable<Language> getUnitLanguages() {
		return new Iterable<Language>() {
			@Override
			public Iterator<Language> iterator() {
				return new Iterator<Language>() {
					int i = 0;
					int n = unitTasks.length;

					@Override
					public boolean hasNext() {
						return i < n;
					}

					@Override
					public Language next() {
						Language result = unitTasks[i].getLanguage();

						i++;
						return result;
					}
				};
			}
		};
	}

	private void initialize() {
		this.linkLanguage = Configurations.commonLanguage(getUnitLanguages());
	}

	/**
	 * Constructs a new translation task in which each source file represents a
	 * distinct translation unit. Same as specifying a 2-dimensional array in
	 * which each element is an array of length 1.
	 * 
	 * @param sourceFiles
	 *            source files; each is compiled as a separate translation unit
	 *            and linked
	 */
	public TranslationTask(File[] sourceFiles) {
		int numFiles = sourceFiles.length;

		unitTasks = new UnitTask[numFiles];
		for (int i = 0; i < numFiles; i++) {
			unitTasks[i] = new UnitTask(new File[]{sourceFiles[i]});
		}
		initialize();
	}

	/**
	 * Constructs a new translation task consisting of a single source file.
	 * 
	 * @param sourceFile
	 *            the source file
	 */
	public TranslationTask(File sourceFile) {
		unitTasks = new UnitTask[1];
		unitTasks[0] = new UnitTask(new File[]{sourceFile});
		initialize();
	}

	/**
	 * Gets the unit tasks, the objects specifying how to construct each
	 * translation unit. Each translation unit is constructed by parsing and
	 * preprocessing a sequence of files. There is no default value; must be set
	 * at construction.
	 * 
	 * @return the unit tasks
	 */
	public UnitTask[] getUnitTasks() {
		return unitTasks;
	}

	/**
	 * Specifies how far into the translation process this task should go before
	 * stopping. Default is to go all the way, i.e.,
	 * {@link TranslationStage#TRANSFORM_PROGRAM}.
	 * 
	 * @return the final translation stage of this task
	 */
	public TranslationStage getStage() {
		return stage;
	}

	/**
	 * Specifies how far into the translation process this task should go before
	 * stopping. Default is to go all the way, i.e.,
	 * {@link TranslationStage#TRANSFORM_PROGRAM}.
	 * 
	 * @param statege
	 *            the final translation stage of this task
	 */
	public void setStage(TranslationStage stage) {
		this.stage = stage;
	}

	/**
	 * Gets the output stream --- where to print human-readable descriptions of
	 * translation artifacts. Default is standard out.
	 * 
	 * @return the output stream
	 */
	public PrintStream getOut() {
		return out;
	}

	/**
	 * Sets the output stream --- where to print human-readable descriptions of
	 * translation artifacts. Default is standard out.
	 * 
	 * @param out
	 *            the output stream
	 */
	public void setOut(PrintStream out) {
		this.out = out;
	}

	/**
	 * Should preprocessing output be displayed as individual tokens (very
	 * detailed information)? Default is <code>false</code>.
	 * 
	 * @return <code>true</code> iff preprocessing output should be displayed as
	 *         individual tokens
	 */
	public boolean getPreprocTokens() {
		return preprocTokens;
	}

	/**
	 * Specifies whether preprocessing output should be displayed as individual
	 * tokens (very detailed information). Default is <code>false</code>.
	 * 
	 * @param flag
	 *            <code>true</code> iff preprocessing output should be displayed
	 *            as individual tokens
	 */
	public void setPreprocTokens(boolean flag) {
		this.preprocTokens = flag;
	}

	/**
	 * Should very detailed information be printed during processing? Default is
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> iff very detailed information should be printed
	 *         during processing
	 */
	public boolean getVerbose() {
		return verbose;
	}

	/**
	 * Specifies whether very detailed information should be printed during
	 * processing.
	 * 
	 * @param verbose
	 *            <code>true</code> iff very detailed information should be
	 *            printed during processing
	 */
	public void setVerbose(boolean flag) {
		this.verbose = flag;
		if (verbose)
			silent = false;
	}

	/**
	 * Returns the sequence of transform records as a Java {@link Collection} .
	 * The order does matter. These are the transformations that will be applied
	 * to the program AFTER linking the translation units to form the whole
	 * program.
	 * 
	 * @return the sequence of transformers
	 */
	public Collection<TransformRecord> getTransformRecords() {
		return transformRecords;
	}

	/**
	 * Adds the given transform record to the end of the transform record
	 * sequence.
	 * 
	 * @param record
	 *            a non-<code>null</code> transform record
	 */
	public void addTransformRecord(TransformRecord record) {
		transformRecords.add(record);
	}

	/**
	 * Adds the transformation record specified by the given code to the end of
	 * the transform record sequence. These are the transformations that will be
	 * applied to the program AFTER linking the translation units to form the
	 * whole program.
	 * 
	 * @param code
	 *            an AST transformation code
	 * @throws ABCException
	 *             if no record for that code exists
	 */
	public void addTransformCode(String code) throws ABCException {
		TransformRecord record = Transform.getRecord(code);

		if (record == null)
			throw new ABCException("Unknown transformer code: " + code);
		transformRecords.add(record);
	}

	/**
	 * Add records for all of the given transformation codes (in order) to the
	 * transform record sequence of this translation task.
	 * 
	 * @param codes
	 *            a sequence of AST transformation codes
	 * @throws ABCException
	 *             if for some code in the collection, no record for that code
	 *             exists
	 */
	public void addAllTransformCodes(Collection<String> codes)
			throws ABCException {
		for (String code : codes)
			addTransformCode(code);
	}

	/**
	 * Should the program be displayed as CIVL-C code, as opposed to a
	 * hierarchical representation? Default is <code>false</code>.
	 * 
	 * @return <code>true</code> iff program should be displayed as CIVL-C code
	 */
	public boolean getPrettyPrint() {
		return prettyPrint;
	}

	/**
	 * Specifies whether program should be displayed as CIVL-C code, as opposed
	 * to a hierarchical representation. Default is <code>false</code>.
	 * 
	 * @param flag
	 *            <code>true</code> iff program should be displayed as CIVL-C
	 *            code
	 */
	public void setPrettyPrint(boolean flag) {
		this.prettyPrint = flag;
	}

	/**
	 * Should symbol and type tables be displayed? Default is <code>false</code>
	 * .
	 * 
	 * @return <code>true</code> iff symbol and type tables should be displayed
	 */
	public boolean getShowTables() {
		return showTables;
	}

	/**
	 * Specifies whether the symbol and type tables should be displayed. Default
	 * is <code>false</code>.
	 * 
	 * @param flag
	 *            <code>true</code> iff symbol and type tables should be
	 *            displayed
	 */
	public void setShowTables(boolean flag) {
		this.showTables = flag;
	}

	/**
	 * Show timing information for each phase of translation? Default is
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> iff timing information should be displayed
	 */
	public boolean getShowTime() {
		return showTime;
	}

	/**
	 * Is this task to show the difference between two ASTs? Default is
	 * <code>false</code>.
	 * 
	 * @return <code>true</code> iff the task is to show the difference between
	 *         two ASTs
	 */
	public boolean getShowDiff() {
		return this.showDiff;
	}

	/**
	 * Specifies whether to show timing information for each translation phase.
	 * Default is <code>false</code>.
	 * 
	 * @param flag
	 *            <code>true</code> iff timing information should be displayed
	 */
	public void setShowTime(boolean flag) {
		this.showTime = flag;
	}

	/**
	 * Specifies whether this is a task to show the difference between two ASTs.
	 * Default is <code>false</code>.
	 * 
	 * @param flag
	 *            <code>true</code> iff the task is to show the difference
	 *            between two ASTs
	 */
	public void setShowDiff(boolean flag) {
		this.showDiff = flag;
	}

	/**
	 * Should very little output be produced? Default is <code>false</code>.
	 * 
	 * @return <code>true</code> iff very little output should be produced
	 */
	public boolean isSilent() {
		return silent;
	}

	/**
	 * Specifies whether to show very little output. Default is
	 * <code>false</code>.
	 * 
	 * @param flag
	 *            <code> true</code> iff very little output should be produced
	 */
	public void setSilent(boolean flag) {
		this.silent = flag;
		if (silent)
			verbose = false;
	}

	/**
	 * Should the output list all called functions which do not have
	 * definitions? Default is <code>false</code>.
	 * 
	 * @return <code>true</code> iff ABC should output all called functions
	 *         without definitions
	 */
	public boolean getShowUndefinedFunctions() {
		return showUndefinedFunctions;
	}

	/**
	 * Specifies whether the output should list all called functions which do
	 * not have definitions. Default is <code>false</code>.
	 * 
	 * @param flag
	 *            <code>true</code> iff ABC should output all called functions
	 *            without definitions
	 */
	public void setShowUndefinedFunctions(boolean flag) {
		this.showUndefinedFunctions = flag;
	}

	/**
	 * Gets the language that will be in effect when the translation units are
	 * linked. Default is determined by the languages of the unit tasks. If all
	 * are in one language, that is used as the link language. Otherwise,
	 * {@link Language#CIVL_C} is the link language.
	 * 
	 * @return the language that will be in effect when the translation units
	 *         are linked
	 */
	public Language getLinkLanguage() {
		return linkLanguage;
	}

	/**
	 * Sets the language that will be in effect when the translation units are
	 * linked. Default is determined by the languages of the unit tasks. If all
	 * are in one language, that is used as the link language. Otherwise,
	 * {@link Language#CIVL_C} is the link language.
	 * 
	 * @param language
	 *            the language that will be in effect when the translation units
	 *            are linked
	 */
	public void setLinkLanguage(Language language) {
		this.linkLanguage = language;
	}

	/**
	 * Is this configuration being used to solve an SV-COMP problem?
	 * 
	 * @return <code>true</code> iff this is an SV-COMP problem
	 */
	public boolean getSVCOMP() {
		return svcomp;
	}

	/**
	 * Sets the SVCOMP flag, which specifies whether this task is being used to
	 * solve an SV-COMP problem. If <code>true</code>, also automatically sets
	 * GNUC to <code>true</code>.
	 * 
	 * @param flag
	 *            <code>true</code> iff this is an SV-COMP problem
	 */
	public void setSVCOMP(boolean flag) {
		this.svcomp = flag;
		if (flag)
			this.gnuc = true;
	}

	public boolean getSummarize() {
		return summarize;
	}

	public void setSummarize(boolean value) {
		this.summarize = value;
	}

	/**
	 * Gets the architecture type for this translation task. Default is
	 * {@link Architecture#UNKNOWN}.
	 * 
	 * @return the architecture type
	 */
	public Architecture getArchitecture() {
		return architecture;
	}

	/**
	 * Sets the architecture type for this translation task. Default is
	 * {@link Architecture#UNKNOWN}.
	 * 
	 * @param architecture
	 *            the architecture type
	 */
	public void setArchitecture(Architecture arch) {
		this.architecture = arch;
	}

	/**
	 * Are the GNU extensions to the C language allowed?
	 * 
	 * @return value of the GNUC flag
	 */
	public boolean getGNUC() {
		return gnuc;
	}

	/**
	 * Specifies whether the GNU extensions to the C language are allowed.
	 * Default is false. This flag is also automatically set to true when the
	 * SVCOMP flag is set to true
	 * 
	 * @param flag
	 *            value of GNUC flag
	 */
	public void setGNUC(boolean flag) {
		this.gnuc = flag;
	}

	public DynamicTask getDynamicTask() {
		return dynamicTask;
	}

	public void setDynamicTask(DynamicTask dtask) {
		this.dynamicTask = dtask;
	}
}
