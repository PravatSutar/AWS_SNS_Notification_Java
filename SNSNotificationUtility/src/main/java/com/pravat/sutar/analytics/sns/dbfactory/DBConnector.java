package com.pravat.sutar.analytics.sns.dbfactory;

import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pravat.sutar.analytics.sns.beans.Transaction;
import com.pravat.sutar.analytics.sns.common.CryptoUtils;

public class DBConnector {
	private static final Logger logger = LoggerFactory.getLogger(DBConnector.class);
	private Connection con = null;
	private Connection conSNS = null;
	String dbDriverClass = null;
	String dbConnUrl = null;
	String dbUserName = null;
	String dbPassword = null;

	public DBConnector() {
		try {
			Properties props = new Properties();
			String dbPropertyFile = "C:/SNSNotification/src/main/resources/dbconnection.properties";
			FileReader fReader = new FileReader(dbPropertyFile);

			props.load(fReader);

			dbDriverClass = props.getProperty("db.driver.class");
			dbConnUrl = props.getProperty("db.conn.url");
			dbUserName = props.getProperty("db.username");
			dbPassword = props.getProperty("db.password");

		} catch (Exception e) {
			logger.error("Exception @DBConnector" + e.getMessage());
		}
	}

	public void updateNotifyFlag(String notificationFlag, String max_timestamp) {
		logger.debug("Notification flag and timestamp parameter received: " + notificationFlag+" and "+max_timestamp);

		if (notificationFlag != null) {
			try {
				Class.forName(dbDriverClass);
				con = DriverManager.getConnection(dbConnUrl, dbUserName, CryptoUtils.decrypt(dbPassword));

				Statement stmt = con.createStatement();
				String query = "UPDATE src.control_table SET notification= 'Y' where last_ts = '" + max_timestamp + "'";

				stmt.executeUpdate(query);
				System.out.println("Update of control table with notification flag as Y is complete!");
				con.close();
			} catch (Exception e) {
				logger.error("Exception while updating control table! ", e.getMessage());
			}
		}
	}

	/**
	 * getLastTimeStamp() retrieves latest time stamp from the control table based
	 * on which the next set up records are pulled from REST API call.
	 * 
	 * @return
	 */
	public String getTimestamp() {
		String max_timestamp = "";
		String notify_flag = "";
		
		try {
			Class.forName(dbDriverClass);
			if (!"".equals(dbDriverClass) && !"".equals(dbConnUrl)) {
				con = DriverManager.getConnection(dbConnUrl, dbUserName, CryptoUtils.decrypt(dbPassword));
				logger.debug("Connection Established!");
			}
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(
					"SELECT rc1.last_ts, rc1.notification " + "FROM src.rest_control rc1 " + "INNER JOIN ( "
							+ "SELECT MAX(DATE_FORMAT( last_ts, '%Y:%m:%d:%H:%i:%S.%f')) as last_ts, notification "
							+ "FROM src.control_table rc2) rc2 " + "ON rc1.last_ts = rc2.last_ts");

			while (rs.next()) {
				max_timestamp = rs.getString(1);
				notify_flag = rs.getString(2);
				logger.debug("Latest timestamp and notify flag received from control table: " + max_timestamp
						+ " and " + notify_flag);
			}
			con.close();
		} catch (Exception e) {
			logger.error("Exception while getting latest timestamp from control table! ", e.getMessage());
		}
		return max_timestamp + "||" + notify_flag;
	}

	public List<Transaction> getLatestTranDetails(String max_timestamp) {
		ArrayList<Transaction> transactions = new ArrayList<Transaction>();

		String transactionAmt = "";
		String transactionDesc = "";
		String transactionDate = "";
		String customerPhone = "";

		try {
			Class.forName(dbDriverClass);
			if (!"".equals(dbDriverClass) && !"".equals(dbConnUrl)) {
				conSNS = DriverManager.getConnection(dbConnUrl, dbUserName, CryptoUtils.decrypt(dbPassword));
			logger.debug("Connection Established!");
			}
			Statement stmtSNS = conSNS.createStatement();

			ResultSet rsSNS = stmtSNS.executeQuery(
					"SELECT tran.transactionAmt, tran.transactionDesc,tran.transactionDate, cust.phone, acct.accountID "
							+ "from src.TRAN tran " + "INNER JOIN src.ACCT acct "
							+ "ON tran.accountID = acct.accountID " + "INNER JOIN src.CUST cust "
							+ "ON cust.customerID = acct.customerID " + "WHERE tran.transactionDate > '" + max_timestamp
							+ "' ORDER BY transactionDate asc");
			
			while (rsSNS.next()) {
				transactionAmt = rsSNS.getString(1);
				transactionDesc = rsSNS.getString(2);
				transactionDate = rsSNS.getString(3);
				customerPhone = rsSNS.getString(4);
				
				System.out.println("Transaction Amt: "+transactionAmt+ "\nTransaction Desc: "+transactionDesc+
						"\nTransaction Date: "+transactionDate+"\nCustomer Phone: "+customerPhone);
				
				Transaction dataAck = new Transaction(rsSNS.getString(1), rsSNS.getString(2), rsSNS.getString(3),
						rsSNS.getString(4));
				transactions.add(dataAck);
			}
			con.close();
		} catch (Exception e) {
			logger.error("Exception while getLastTimestamp! ", e.getMessage());
		}
		return transactions;
	}

	public static void main(String[] args) {
		DBConnector dbConnector = new DBConnector();
		System.out.println("Main(): DB details are " + dbConnector.getLatestTranDetails("2019-08-01 00:00:01.100010"));

		List<Transaction> tranData = dbConnector.getLatestTranDetails("2019-08-01 00:00:01.100010");
		System.out.println("Main(): transactions record size: " + tranData.size());
		for (Transaction s : tranData) {
			System.out.println("Main(): Transaction Amount:" + s.getTransactionAmt());
			System.out.println("Main(): Transaction Date: " + s.getTransactionDate());
			System.out.println("Main(): Transaction Desc: " + s.getTransactionDesc());
			System.out.println("Main(): Customer Phone: " + s.getCustomerPhone());
		}
	}
}
