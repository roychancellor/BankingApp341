package edu.gcu.cst341.controller;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.gcu.cst341.model.Account;
import edu.gcu.cst341.model.Customer;
import edu.gcu.cst341.model.DatabaseActions;
import edu.gcu.cst341.model.DbConstants;
import edu.gcu.cst341.view.Menus;

/**
 * Bank is the primary class for the banking application, housing all methods to process user input
 * The actual account computations occur in the account classes Checking, Saving, and Loan
 */
public class Bank {

	//Class data
	private String bankName;
	private List<Customer> customers = new ArrayList<Customer>();
	private Map<Integer, Integer> custIdToIndex = new HashMap<Integer, Integer>();
	private int custIndex = 0;
	private DatabaseActions db;
	
	//Format for dates and money outputs in all classes
	public static DecimalFormat money = new DecimalFormat();
	public static final String MONEY_FORMAT = "$#,##0.00;($#,##0.00)";

	//Constructor
	public Bank(String bankName) {
		this.bankName = bankName;
		
		//this.db = new DatabaseActions(DbConstants.SILENT, DbConstants.PRODUCTION);
		this.db = new DatabaseActions(DbConstants.SILENT, DbConstants.LOCAL);
		if(db.connectToDatabase()) {
			//Create the list of customers from the customers database
			customers = db.makeCustomerListFromDatabase();
			//Sort the customer list
			Collections.sort(customers);
			//Make a map of customerId from the database to the index of the customers list
			//which is necessary after the sort
			mapIdtoIndex();
			//Set the money format
			money.applyPattern(MONEY_FORMAT);
		}
		else {
			System.out.println("FATAL ERROR: Unable to open database. BANK IS CLOSED FOR BUSINESS!");
		}
	}
	
	//Class methods

	/**
	 * Helper method that maps database customerId to customer list index
	 * so list can remain sorted without losing track of customerId
	 */
	private void mapIdtoIndex() {
		for(int i = 0; i < customers.size(); i++) {
			custIdToIndex.put(customers.get(i).getCustomerId(), i);
		}
	}
	
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
	 * @param menuOption the menu option the user chose
	 */
	private void processMainMenu(int menuOption) {
		switch(menuOption) {
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
		//Add a new customer to the database of customers
		String lastName = Menus.getCustomerName("Enter LAST name:");
		String firstName = Menus.getCustomerName("Enter FIRST name:");
		String userName = Menus.getCustomerUserName("Enter a user name:");

		//HASH PASSWORD WITH SALT
		//HASHING STILL NEEDS TO BE IMPLEMENTED
		String passHash = Menus.getCustomerPassword("Enter a password\n(8-200 letters and/or numbers):");
		String passSalt = "salt";
		
		//WRITE NEW CUSTOMER AND CUSTOMER'S CREDENTIALS TO DATABASE TABLES
		int custId = db.createCustomer(lastName, firstName, userName, passSalt, passHash);

		//For now, also add a Customer object to the existing list until DB
		//is fully implemented throughout the bank
		customers.add(new Customer(custId, lastName, firstName, new Date()));
		//Sort the customer list
		Collections.sort(customers);
		//Update the id to index map
		mapIdtoIndex();
	}
	
	/**
	 * updates customer first and last name,
	 * re-sorts customer list, and updates the id to index map
	 */
	private void doUpdateCustomer() {
		custIndex = Menus.viewCustomerSelectionMenu(customers);
		if(custIndex != Menus.MENU_EXIT) {
			//Make the user selection into a zero-based index
			custIndex -= 1;
			
			//Get original names
			String origLastName = customers.get(custIndex).getLastName();
			String origFirstName = customers.get(custIndex).getFirstName();
			
			//Set the new names
			customers.get(custIndex).setLastName(Menus.getCustomerName("Enter new LAST name:"));
			customers.get(custIndex).setFirstName(Menus.getCustomerName("Enter new FIRST name:"));
			System.out.println("\nSuccess, " + origFirstName + " " + origLastName
				+ " changed to "
				+ customers.get(custIndex).getFirstName()
				+ " " + customers.get(custIndex).getLastName()
			);
			//Sort the customer list
			Collections.sort(customers);
			//Update the id to index map
			mapIdtoIndex();
		}
	}
	
	/**
	 * prints a welcome message to the customer
	 */
	private void welcomeCustomer() {
		if(this.custIndex < customers.size())
			System.out.println(customers.get(custIndex).toString(false));
	}
	
	/**
	 * gets the customer's transaction selection
	 */
	private void doCustomerTransactions() {
		//Get a customer logged in
		int customerIdFromDb = doCustomerLogin();
		
		if(customerIdFromDb != Menus.MENU_EXIT) {
			custIndex = this.custIdToIndex.get(customerIdFromDb);
			int option = Menus.MENU_EXIT;
			do {
				option = Menus.viewCustomerActionMenu(this.bankName, customers.get(custIndex));
				
				welcomeCustomer();
	
				processCustomerMenu(option);
			} while(option!= Menus.MENU_EXIT);
		}
	}
	
	/**
	 * Method for unit testing the customer login method
	 * @return the result of running doCustomerLogin
	 */
	public int testDoCustomerLogin() {
		return doCustomerLogin();
	}
	
	/**
	 * logs in a customer by checking user-entered credential against the database
	 * @return the customer id if successful and Menus.MENU_EXIT if unsuccessful after 3 tries
	 */
	private int doCustomerLogin() {
		boolean keepGoing = true;
		int numFails = 0;
		final int MAX_TRIES = 3;
		
		//Loop a certain number of times and if customer doesn't enter
		//valid credentials, return the exit value
		//Otherwise, return the customerId
		do {
			keepGoing = true;
			
			//Get username and password from the customer
			//Method returns null if user elects to cancel
			String[] credentialCheck = Menus.viewCustomerLogin();
			
			if(credentialCheck != null) {
				//Query the credentials database for the customer credentials
				int customerId = db.checkLoginCredentials(credentialCheck[0], "salt", credentialCheck[1]);
	
				//If found, log the customer in
				if(customerId > 0) {
					return customerId;
				}
				//If not found, return an error message and have the customer re-enter credentials
				else {
					numFails++;
					if(numFails < MAX_TRIES) {
						System.out.println("\nIncorrect username and/or password.\n" + (MAX_TRIES - numFails)
							+ " attempts remaining. Try again.");
					}
					else {
						System.out.println("\nYou have exceeded the maximum number of login attempts.");
						keepGoing = false;
					}
				}
			}
			else {
				keepGoing = false;
			}
		} while(keepGoing);
		
		return Menus.MENU_EXIT;
	}
	
	/**
	 * Processes the user-selected option from the main menu
	 * After each transaction, calls viewBalances to update the user
	 * @param menuOption the menu option the user selected
	 */
	private void processCustomerMenu(int menuOption) {

		switch(menuOption) {
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
			//Menus.viewBalances(customers.get(custIndex));
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