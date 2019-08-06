/**
 * Bank is the primary class for the banking application, housing all methods to process user input
 * The actual account computations occur in the account classes Checking, Saving, and Loan
 */
package edu.gcu.cst235.milestone.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.gcu.cst235.milestone.model.Account;
import edu.gcu.cst235.milestone.model.Customer;
import edu.gcu.cst235.milestone.model.DatabaseActions;
import edu.gcu.cst235.milestone.model.DbConstants;
import edu.gcu.cst235.milestone.view.Menus;

public class Bank {

	//Class data
	private String bankName;
	private List<Customer> customers = new ArrayList<Customer>();
	private int custIndex = 0;
	private DatabaseActions db;
	
	//Format for dates and money outputs in all classes
	public static DecimalFormat money = new DecimalFormat();
	public static final String MONEY_FORMAT = "$#,##0.00;($#,##0.00)";

	//Constructor
	public Bank(String bankName) {
		this.bankName = bankName;
		
		this.db = new DatabaseActions(DbConstants.VERBOSE, DbConstants.LOCAL);
		if(db.connectToDatabase()) {
			//Create the list of customers from the customers database
			customers = db.makeCustomerListFromDatabase();
			
			//Set the money format
			money.applyPattern(MONEY_FORMAT);
		}
		else {
			System.out.println("FATAL ERROR: Unable to open database. BANK IS CLOSED FOR BUSINESS!");
		}
	}
	
	//Class methods
	
	/**
	 * the highest level method that controls the bank execution
	 * runs until user chooses to exit
	 */
	public void runBank() {
		int selection;
		
		do {
			selection = Menus.viewMainMenu(this.bankName);
			processMainMenu(selection);
		} while(selection != Menus.MENU_EXIT);

		//Close the database connection
		db.close();
	}
	
	/**
	 * Calls the Create Customer menu or the Customer Selection Menu based on the option selected
	 * @param option
	 */
	private void processMainMenu(int option) {
		switch(option) {
			case 1:
				doManageCustomers();
				break;
			case 2:
				doCustomerTransactions();
				break;
			case Menus.MENU_EXIT:
				viewBankingAppExit();
				break;
			default:
				Menus.viewMainMenu(this.bankName);
		}
	}
	
	/**
	 * Outputs a message to the banker when exiting the banking application completely
	 */
	private void viewBankingAppExit() {
		System.out.println("\nGoodbye banker. Application closed at "
			+ Menus.dateFormat.format(new Date()) + ". Have a good day!\n");
	}
	
	/**
	 * Gets customer first and last name and creates a new customer object
	 * Sorts the entire customer list
	 */
	private void doManageCustomers() {
		int option = Menus.viewManageCustomerMenu();
				
		switch(option) {
			case 1: doCreateCustomer(); break;
			case 2: doUpdateCustomer(); break;
		}
	}

	/**
	 * creates a new customer and adds to the customer database table
	 */
	private void doCreateCustomer() {
		//Clear the scanner from previous nextInt call
		Menus.scan.nextLine();
		
		//Add a new Customer object to the existing list of customers
		String firstName = Menus.getName("Enter first name:");
		String lastName = Menus.getName("Enter last name:");
//		String userName = Menus.getUserName("Enter a user name:");
//		String passWord = Menus.getPassWord("Enter a password (8 characters minimum):");
		//HASH PASSWORD WITH SALT
		//WRITE NEW CUSTOMER TO DATABASE
		customers.add(new Customer(999, firstName, lastName, new Date()));
		System.out.println("\nSuccess, "
			+ customers.get(customers.size() - 1).getFirstName()
			+ " " + customers.get(customers.size() - 1).getLastName()
			+ " created."
		);
		
		//Sort the list of Bank customers by lastName, firstName
		Collections.sort(customers);
	}
	
	/**
	 * updates customer first and last name
	 */
	private void doUpdateCustomer() {
		custIndex = Menus.viewCustomerSelectionMenu(customers);
		if(custIndex != Menus.MENU_EXIT) {
			//Clear the scanner from previous nextInt call
			Menus.scan.nextLine();
			
			//Make the user selection into a zero-based index
			custIndex -= 1;
			
			//Get original names
			String origLastName = customers.get(custIndex).getLastName();
			String origFirstName = customers.get(custIndex).getFirstName();
			
			//Set the new names
			customers.get(custIndex).setFirstName(Menus.getName("Enter new first name:"));
			customers.get(custIndex).setLastName(Menus.getName("Enter new last name:"));
			System.out.println("\nSuccess, " + origFirstName + " " + origLastName
				+ " changed to "
				+ customers.get(custIndex).getFirstName()
				+ " " + customers.get(custIndex).getLastName()
			);
			//Sort the list of Bank customers by lastName, firstName
			Collections.sort(customers);
		}
	}
	
	/**
	 * prints a welcome message to the customer, verbose with loan details
	 */
	private void welcomeCustomer() {
		if(this.custIndex < customers.size())
			System.out.println(customers.get(custIndex).toString(false));
	}
	
	/**
	 * gets the customer's transaction selection
	 */
	private void doCustomerTransactions() {
		boolean credentialsMismatch = false;
		
		do {
			credentialsMismatch = false;
			//Get username and password from the customer
			String[] credentials = Menus.viewCustomerLogin();
			
			//BANK
			//Query the credentials database for the customer credentials
			int customerId = db.checkLoginCredentials(credentials[0], "salt", credentials[1]);
			if(customerId > 0) {
				System.out.println("CustomerId " + customerId + " successfully logged in.");
			}
			else {
				System.out.println("\nIncorrect username and/or password. Try again.");
			}
			//If found, log the customer in
			//If not found, return an error message and have the customer re-enter credentials
			//Loop through a certain number of times and if customer doesn't enter
			//valid credentials, return to the main menu
			//Otherwise, get the customer id from the database and set the custIndex to that number - 1
		} while(credentialsMismatch);
		
		custIndex = Menus.viewCustomerSelectionMenu(customers);
			
		if(custIndex != Menus.MENU_EXIT) {
			custIndex -= 1;
			int option = Menus.MENU_EXIT;
			do {
				option = Menus.viewCustomerActionMenu(this.bankName, customers.get(custIndex));
				
				welcomeCustomer();
	
				processCustomerMenu(option);
			} while(option!= Menus.MENU_EXIT);
		}
	}
	/**
	 * Calls a method to display the screen to process the user-selected option from the main menu
	 * After each transaction, calls viewBalances to update the user
	 * @param option
	 */
	private void processCustomerMenu(int option) {

		switch(option) {
		case 1:
			customers.get(custIndex).getChecking().doTransaction(
				Account.DEPOSIT,
				customers.get(custIndex).getChecking().getTransactionValue(Account.AMOUNT_MESSAGE + "deposit: ")
			);
			Menus.viewBalances(customers.get(custIndex));
			break;
		case 2:
			customers.get(custIndex).getSaving().doTransaction(
				Account.DEPOSIT,
				customers.get(custIndex).getSaving().getTransactionValue(Account.AMOUNT_MESSAGE + "deposit: ")
			);
			Menus.viewBalances(customers.get(custIndex));
			break;
		case 3:
			customers.get(custIndex).getChecking().doTransaction(
				Account.WITHDRAWAL,
				customers.get(custIndex).getChecking().getTransactionValue(Account.AMOUNT_MESSAGE + "withdraw: ")
			);
			Menus.viewBalances(customers.get(custIndex));
			break;
		case 4:
			customers.get(custIndex).getSaving().doTransaction(
				Account.WITHDRAWAL,
				customers.get(custIndex).getSaving().getTransactionValue(Account.AMOUNT_MESSAGE + "withdraw: ")
			);
			Menus.viewBalances(customers.get(custIndex));
			break;
		case 5:
			System.out.println("\nYour minimum monthly payment is "
				+ money.format(customers.get(custIndex).getLoan().getMonthlyPaymentAmount()));
			customers.get(custIndex).getLoan().doTransaction(
				customers.get(custIndex).getLoan().getTransactionValue(Account.AMOUNT_MESSAGE + "pay on the loan: ")
			);
			Menus.viewBalances(customers.get(custIndex));
			break;
		case 6:
			customers.get(custIndex).getLoan().viewAmortization();
		case 7:
			Menus.viewBalances(customers.get(custIndex));
			break;
		case 8:
			Menus.viewEndOfMonth(customers.get(custIndex));
			break;  
		case Menus.MENU_EXIT:
			Menus.viewCustomerExit(customers.get(custIndex));
			break;
		default:
			Menus.viewCustomerActionMenu(this.bankName, customers.get(custIndex));
		}
	}	
}