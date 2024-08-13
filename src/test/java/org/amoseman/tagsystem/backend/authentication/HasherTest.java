package org.amoseman.tagsystem.backend.authentication;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class HasherTest {

    @Test
    void hash() {
        String password = "this_is_a_terrible_password";
        SecureRandom random = new SecureRandom();
        Hasher hasher = new Hasher(24, 16, random, new Argon2IDConfig(2, 66536, 1));
        byte[] salt = "salt1".getBytes();
        String hash = hasher.hash(password, salt);
        boolean verification = hasher.verify(password, salt, hash);
        assertTrue(verification);

        byte[] salt2 = "salt2".getBytes();
        String password2 = "another_terrible_password";
        verification = hasher.verify(password2, salt2, hash);
        assertFalse(verification);
    }
}