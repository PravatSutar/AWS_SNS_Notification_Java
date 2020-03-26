package com.pravat.sutar.analytics.sns.beans;

import java.io.Serializable;

public class Transaction implements Serializable{
	
	private static final long serialVersionUID = -2838438882154739187L;

	private final String transactionAmt;
	private final String transactionDesc;
	private final String transactionDate;
	private final String customerPhone;
	
	/**
	 * Instantiates a new transaction.
	 *
	 * @param tran_id the tran id
	 * @param tran_desc the tran desc
	 * @param tran_ts the tran ts
	 */
	public Transaction(String transactionAmt,
			String transactionDesc, String transactionDate, String customerPhone) {
		super();
		this.transactionAmt = transactionAmt;
		this.transactionDesc = transactionDesc;
		this.transactionDate= transactionDate;
		this.customerPhone= customerPhone;
	}

	public String getTransactionAmt() {
		return transactionAmt;
	}

	public String getTransactionDesc() {
		return transactionDesc;
	}

	public String getTransactionDate() {
		return transactionDate;
	}
	public String getCustomerPhone() {
		return customerPhone;
	}

	/**
	 * toString()
	 *
	 * @return the string
	 */
	public String toString() {
		return transactionAmt+transactionDesc+transactionDate+customerPhone;
	}
}
