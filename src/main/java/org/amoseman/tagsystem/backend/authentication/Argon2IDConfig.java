package org.amoseman.tagsystem.backend.authentication;

import org.bouncycastle.crypto.params.Argon2Parameters;

/**
 * Encapsulates the configuration for Argon2ID.
 */
public class Argon2IDConfig {
    private final int iterations;
    private final int memory;
    private final int parallelism;

    /**
     * Instantiate a configuration.
     * @param iterations the number of iterations.
     * @param memory the amount of memory in kilobytes.
     * @param parallelism the number of cores to use.
     */
    public Argon2IDConfig(int iterations, int memory, int parallelism) {
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    /**
     * Get an Argon2Parameters builder, using the encapsulated parameters.
     * @param salt the salt to use.
     * @return the builder.
     */
    public Argon2Parameters.Builder getBuilder(byte[] salt) {
        return new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(iterations)
                .withMemoryAsKB(memory)
                .withParallelism(parallelism)
                .withSalt(salt);
    }
}
