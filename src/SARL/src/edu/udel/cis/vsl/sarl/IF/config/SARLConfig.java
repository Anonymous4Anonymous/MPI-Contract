package edu.udel.cis.vsl.sarl.IF.config;

import java.nio.file.Path;

import edu.udel.cis.vsl.sarl.IF.config.ProverInfo.ProverKind;

/**
 * A SARL configuration encapsulates information about the complete set of
 * external theorem provers available to SARL.
 * 
 * @author xxxx
 */
public interface SARLConfig {

	/**
	 * The number of theorem provers supported by this configuration.
	 * 
	 * @return the number of theorem provers
	 */
	int getNumProvers();

	/**
	 * Gets the index-th theorem prover.
	 * 
	 * @param index
	 * @return the index-th prover
	 */
	ProverInfo getProver(int index);

	/**
	 * Returns all the provers supported by this configuration as an iterable
	 * sequence.
	 * 
	 * @return all provers supported by this configuration
	 */
	Iterable<ProverInfo> getProvers();

	/**
	 * Finds a prover supported by this configuration with the given alias.
	 * 
	 * @param alias
	 *            the alias to search for
	 * @return a prover supported by this configuration with given alias or
	 *         <code>null</code> if there is no such prover
	 */
	ProverInfo getProverWithAlias(String alias);

	/**
	 * Finds a prover of the given kind supported by this configuration.
	 * 
	 * @param kind
	 *            the kind to search for
	 * @return a prover supported by this configuration of the given kind, or
	 *         <code>null</code> if there is no such prover
	 */
	ProverInfo getProverWithKind(ProverKind kind);

	/**
	 * Finds the why3 prove platform supported by this configuration
	 * 
	 * @return The {@link ProverInfo} for why3 or null if there is no why3 prove
	 *         platform.
	 */
	ProverInfo getWhy3ProvePlatform();

	/**
	 * Set the why3 prove platform supported by this configuration
	 * 
	 * @param why3Info
	 *            An object contains all the informations of the why3 prove
	 *            platform.
	 */
	void setWhy3ProvePlatform(ProverInfo why3Info);

	/**
	 * @return the directory where SARL should put its generated temporary files
	 *         in
	 */
	Path getOutputFileDir();

}
