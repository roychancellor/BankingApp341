package edu.gcu.cst341.controller;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Optional;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;


import org.springframework.stereotype.Service;

@Service
public class PasswordService {
	private static final SecureRandom RAND = new SecureRandom();
	private static final int ITERATIONS = 65536;
	private static final int KEY_LENGTH = 512;
	private static final String ALGORITHM = "PBKDF2WithHmacSHA512";
	/**
	 * Creates a hashed salt for password security
	 * @param length the length of the salt
	 * @return an Optional object of type String
	 */
	public static Optional<String> generateSalt(final int length) {
		if (length < 1) {
			System.err.println("error in generateSalt: length must be > 0");
			return Optional.empty();
		}

		byte[] salt = new byte[length];
		RAND.nextBytes(salt);

		return Optional.of(Base64.getEncoder().encodeToString(salt));
	}
	
	/**
	 * Hashes a plain text password
	 * @param password the plain text password to hash
	 * @param salt the hashed salt
	 * @return a String containing the hashed password
	 */
	public static Optional<String> hashPassword(String password, String salt) {

		char[] chars = password.toCharArray();
		byte[] bytes = salt.getBytes();

		PBEKeySpec spec = new PBEKeySpec(chars, bytes, ITERATIONS, KEY_LENGTH);

		Arrays.fill(chars, Character.MIN_VALUE);

		try {
			SecretKeyFactory fac = SecretKeyFactory.getInstance(ALGORITHM);
			byte[] securePassword = fac.generateSecret(spec).getEncoded();
			return Optional.of(Base64.getEncoder().encodeToString(securePassword));

		} catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
			System.err.println("Exception encountered in hashPassword()");
			return Optional.empty();

		} finally {
			spec.clearPassword();
		}
	}
	
	/**
	 * For testing only - not used in the application
	 * @param password the plain-text password to test
	 * @param key the stored password for comparison
	 * @param salt the salt used to make the key and to hash the plain-text password
	 * @return true of the passwords match and false if not
	 */
	public static boolean verifyPassword(String password, String key, String salt) {
		Optional<String> optEncrypted = hashPassword(password, salt);
		if (!optEncrypted.isPresent())
			return false;
		return optEncrypted.get().equals(key);
	}
}
