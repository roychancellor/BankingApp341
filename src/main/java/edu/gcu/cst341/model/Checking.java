package edu.gcu.cst341.model;

/**
 * Child class of Account that creates Checking account objects
 */
public class Checking extends Account {

	//Unique child class data and getters/setters
	private double overdraftFee;
	//Account fees and minimum balances
	public static final double OVERDRAFT_FEE = 25.0;
	
	public double getOverdraftFee() {
		return overdraftFee;
	}

	public void setOverdraftFee(double overdraftFee) {
		this.overdraftFee = overdraftFee;
	}

	/**
	 * Constructor for Checking objects. There is no default constructor...must use this one
	 * @param accountNumber account number
	 * @param accountBalance opening balance
	 * @param overdraftFee amount of overdraftFee fee assessed every time the user withdraws more than available
	 */
	public Checking(String accountNumber, double accountBalance, double overdraftFee) {
		//Call the superclass (Account) constructor
		super(accountNumber, accountBalance);
		//Unique to Checking objects
		this.overdraftFee = overdraftFee;
	}
	
	/**
	 * Implements processTransaction that was left abstract in the superclass
	 * unique to Checking accounts (logic for overdraftFee fee)
	 * @param transType a multiplier for withdrawals (-1) or deposits (+1)
	 * @param amount the amount to withdraw or deposit
	 */
	public void doTransaction(int transType, double amount) {
		double feeAmount = 0;
		
		//WITHDRAWAL: If the amount is more than the balance, add the overdraft fee
		//The customer will have been advised on the front end
		if(transType == Account.WITHDRAWAL && amount > getAccountBalance()) {
			feeAmount = getOverdraftFee();
			this.addTransaction(feeAmount, "Overdraft fee");
		}
		
		//Once validated, process the transaction
		setAccountBalance(getAccountBalance() + transType * (amount + feeAmount));
		
		//Record the transaction
		if(transType == Account.WITHDRAWAL) {
			this.addTransaction(-amount, "Withdrawal");
		}
		if(transType == Account.DEPOSIT) {
			this.addTransaction(amount, "Deposit");
		}
	}
	
	/**
	 * Implements the iTrans interface: doEndOfMonth method
	 */
	public void doEndOfMonth() {
		//Future functionality for Checking objects
	}
	
	/**
	 * Implements the iTrans interface: checkLateFee method
	 */
	public boolean isFeeRequired() {
		return false;
	}

}