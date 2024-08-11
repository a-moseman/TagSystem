package org.amoseman.tagsystem.backend.authentication;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class Hashing {
    private final int hashLength;
    private final int saltLength;
    private final SecureRandom random;
    private final Argon2IDConfig config;

    public Hashing(int hashLength, int saltLength, SecureRandom random, Argon2IDConfig config) {
        this.hashLength = hashLength;
        this.saltLength = saltLength;
        this.random = random;
        this.config = config;
    }

    public byte[] salt() {
        byte[] salt = new byte[saltLength];
        random.nextBytes(salt);
        return salt;
    }

    public String hash(String password, byte[] salt) {
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(config.getBuilder(salt).build());
        byte[] hash = new byte[hashLength];
        generator.generateBytes(password.getBytes(StandardCharsets.UTF_8), hash, 0, hashLength);
        return Base64.getEncoder().encodeToString(hash);
    }

    public boolean verify(String password, byte[] salt, String hash) {
        String actual = hash(password, salt);
        return actual.equals(hash);
    }
}