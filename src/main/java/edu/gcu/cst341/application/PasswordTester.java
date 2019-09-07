package edu.gcu.cst341.application;

import edu.gcu.cst341.controller.PasswordService;

public class PasswordTester {

	public static void main(String[] args) {
		String salt = PasswordService.generateSalt(512).get();
		System.out.println("The salt:\n" + salt);
		System.out.println("The length of salt: " + salt.length());
		String password = "12345678";
		System.out.println("The password:\n" + password);
		String hashedPass = PasswordService.hashPassword(password, salt).get();
		System.out.println("The hashed password:\n" + hashedPass);
		System.out.println("The length of hashedPass: " + hashedPass.length());
		System.out.println("User enters correct password and verifyPassword returns "
			+ PasswordService.verifyPassword(password, hashedPass, salt));
		System.out.println("User enters \"password2\" and verifyPassword returns "
			+ PasswordService.verifyPassword("password2", hashedPass, salt));
	}

}
