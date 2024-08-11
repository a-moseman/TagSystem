package org.amoseman.tagsystem.backend.authentication;

import org.bouncycastle.crypto.params.Argon2Parameters;

public class Argon2IDConfig {
    private final int iterations;
    private final int memory;
    private final int parallelism;

    public Argon2IDConfig(int iterations, int memory, int parallelism) {
        this.iterations = iterations;
        this.memory = memory;
        this.parallelism = parallelism;
    }

    public Argon2Parameters.Builder getBuilder(byte[] salt) {
        return new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withIterations(iterations)
                .withMemoryAsKB(memory)
                .withParallelism(parallelism)
                .withSalt(salt);
    }
}
