/**
 * Bank is the primary class for the banking application, housing all methods to process user input
 * The actual account computations occur in the account classes Checking, Saving, and Loan
 */
package edu.gcu.bootcamp.cst135.milestone.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import edu.gcu.bootcamp.cst135.milestone.model.Account;
import edu.gcu.bootcamp.cst135.milestone.model.Customer;

public class Bank {

	//Class data
	private String bankName;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
	private List<Customer> customers = new ArrayList<Customer>();
	private static final int MENU_MIN = 1;
	private static final int MENU_MAX = 7;
	private static final int MENU_EXIT = 9;
	public static Scanner scanner = new Scanner(System.in);

	
	//Constructor
	public Bank(String bankName) {
		this.bankName = bankName;
		/*** CREATE ONE CUSTOMER FOR INITIAL TESTING ***/
		customers.add(new Customer("Roy", "Chancellor", new Date(), "123456789"));
		welcomeCustomer();
	}
	
	//Class methods
	private void welcomeCustomer() {
		System.out.println(customers.get(0).toString(true));
	}
	
	/**
	 * Displays the main menu and gets a user selection.
	 * If the user enters a non-integer, parseInt throws NumberFormatException
	 * which gets caught and calls viewCustomerMenu again
	 */
	public void viewCustomerMenu() {

		try {
			int option;
			do {
				System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				System.out.println("                MAIN MENU");
				System.out.println("                " + bankName);
				System.out.println("        Welcome " + customers.get(0).getFirstName() + " " + customers.get(0).getLastName() + "!");
				System.out.println("          " + dateFormat.format(new Date()));
				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
				System.out.println("Pick an option: ");
				System.out.println("-----------------------");
				System.out.println(" 1: Deposit to Checking");
				System.out.println(" 2: Deposit to Savings");
				System.out.println(" 3: Withdraw from Checking");
				System.out.println(" 4: Withdraw from Savings");			
				System.out.println(" 5: Make a Loan Payment");			
				System.out.println(" 6: Get Account Balances");
				System.out.println(" 7: Get Monthly Statement");
				System.out.println("------------------------");
				System.out.println(" 9: : Logout");
				//try to convert user input into an integer (throws InputMismatchException if not)
				option = scanner.nextInt();
				processCustomerMenu(option);
			} while (option != MENU_EXIT);
		}
		catch(Exception e) {  //generated by nextInt()
			printInputError();
			//System.out.println("Oops, please enter a number from " + MENU_MIN + " to " + MENU_MAX + " or " + MENU_EXIT + " to Logout\n");
			//When a scanner throws an InputMismatchException, the scanner will not pass the token
			//that caused the exception, so that it may be retrieved or skipped via some other method.
			//So, read the token that caused the exception so it's not in the scanner anymore
			scanner.nextLine();
			//Re-call the menu method
			viewCustomerMenu();
		}
	}
	
	private void printInputError() {
		System.out.println("\n** Oops, please enter a number from " + MENU_MIN + " to " + MENU_MAX + " or " + MENU_EXIT + " to Logout\n");		
	}

	/**
	 * Calls a method to display the screen to process the user-selected option from the main menu
	 * After each transaction, calls viewBalances to update the user
	 * @param option
	 */
	private void processCustomerMenu(int option) {

		switch(option) {
		case 1:
			customers.get(0).getChecking().doTransaction(
				Account.DEPOSIT,
				customers.get(0).getChecking().getTransactionValue(Account.AMOUNT_MESSAGE + "deposit: ")
			);
			viewBalances();
			break;
		case 2:
			customers.get(0).getSaving().doTransaction(
				Account.DEPOSIT,
				customers.get(0).getSaving().getTransactionValue(Account.AMOUNT_MESSAGE + "deposit: ")
			);
			viewBalances();
			break;
		case 3:
			customers.get(0).getChecking().doTransaction(
				Account.WITHDRAWAL,
				customers.get(0).getChecking().getTransactionValue(Account.AMOUNT_MESSAGE + "withdraw: ")
			);
			viewBalances();
			break;
		case 4:
			customers.get(0).getSaving().doTransaction(
				Account.WITHDRAWAL,
				customers.get(0).getSaving().getTransactionValue(Account.AMOUNT_MESSAGE + "withdraw: ")
			);
			viewBalances();
			break;
		case 5:
			customers.get(0).getLoan().doTransaction(
				customers.get(0).getLoan().getTransactionValue(Account.AMOUNT_MESSAGE + "pay on the loan: ")
			);
			viewBalances();
			break;
		case 6: viewBalances();
			break;
		case 7:
			viewEndOfMonth();
			viewBalances();
			break;  
		case 9:
			viewExitScreen();
			break;
		default:
			printInputError();
			viewCustomerMenu();
		}
	}
	
	/**
	 * Displays all account balances
	 */
	private void viewBalances() {
		System.out.println(customers.get(0).toString(false));
	}
	
	/**
	 * Shows the end of month screen and performs the end-of-month calculations
	 */
	private void viewEndOfMonth() {

		System.out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
		System.out.println("                 GCU BANK");
		System.out.println("               END OF MONTH");
		System.out.println("      Statement for " + customers.get(0).getFirstName() + " " + customers.get(0).getLastName() + "!");
		System.out.println("          " + dateFormat.format(new Date()));
		System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");

		//Determine if the end of the month has occurred
		boolean endOfMonth = true;
		if(endOfMonth) {
			//Determine if a service fee is required
			if(customers.get(0).getSaving().getAccountBalance() < customers.get(0).getSaving().getMinBalance()) {
				System.out.printf("A $%.2f service fee is being assessed for below minimum balance in savings", customers.get(0).getSaving().getServiceFee());
				customers.get(0).getSaving().setAccountBalance(customers.get(0).getSaving().getAccountBalance() - customers.get(0).getSaving().getServiceFee());
			}
			//Compute interest on any positive balance
			if (customers.get(0).getSaving().getAccountBalance() > 0){
				customers.get(0).getSaving().setAccountBalance(customers.get(0).getSaving().getAccountBalance() + (customers.get(0).getSaving().getInterest() * customers.get(0).getSaving().getAccountBalance()));
			}
		}
		else {
			System.out.println("\nSorry, the <current month> is not complete.");
		}
	}		

	/**
	 * Outputs a message to the customer when exiting the banking app
	 */
	private void viewExitScreen() {
		System.out.println("Goodbye " + customers.get(0).getFirstName() + ". Have a good day!");
	}

}