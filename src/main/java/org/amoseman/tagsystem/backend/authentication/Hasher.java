package org.amoseman.tagsystem.backend.authentication;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * A class for password hashing.
 */
public class Hasher {
    private final int hashLength;
    private final int saltLength;
    private final SecureRandom random;
    private final Argon2IDConfig config;

    /**
     * Instantiate a password hasher.
     * @param hashLength the length hashed passwords will be.
     * @param saltLength the length of salt to use.
     * @param random the random to use.
     * @param config configuration values for Argon2ID.
     */
    public Hasher(final int hashLength, final int saltLength, final SecureRandom random, final Argon2IDConfig config) {
        this.hashLength = hashLength;
        this.saltLength = saltLength;
        this.random = random;
        this.config = config;
    }

    /**
     * Generate salt.
     * @return the salt.
     */
    public byte[] salt() {
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Generate the hash of the provided password, encoded in base 64.
     * @param password the password to hash.
     * @param salt the salt to use in hashing.
     * @return the base 64 encoding of the hash of the password.
     */
    public String hash(final String password, final byte[] salt) {
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(config.getBuilder(salt).build());
        byte[] hash = new byte[hashLength];
        generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), hash, 0, hashLength);
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verify the provided password against the provided hash.
     * @param password the password to verify.
     * @param salt the salt to use.
     * @param hash the hash to verify against.
     * @return the result of the check.
     */
    public boolean verify(final String password, final byte[] salt, final String hash) {
        String actual = hash(password, salt);
        return actual.equals(hash);
    }
}