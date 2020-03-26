package com.pravat.sutar.analytics.sns.notification;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.pravat.sutar.analytics.sns.beans.Transaction;
import com.pravat.sutar.analytics.sns.common.Constants;
import com.pravat.sutar.analytics.sns.dbfactory.DBConnector;

public class AmazonSNSInvoker {

	private static final Logger logger = LoggerFactory.getLogger(AmazonSNSInvoker.class);

	private static String ACCESS_KEY = Constants.AWS_ACCESS_KEY;
	private static String SECRET_KEY = Constants.SECRET_KEY;

	public static void sendSMSMessage(AmazonSNSClient snsClient, String messageString, String phoneNumber) {
		PublishResult result = snsClient
				.publish(new PublishRequest().withMessage(messageString).withPhoneNumber(phoneNumber));
		System.out.println("Message Sent to Customer with ID: " + result.toString());
	}

	public static void main(String[] args) {
		DBConnector connect = new DBConnector();
		String max_timestamp = "";
		String notification_flag = "";
		
		String data = connect.getTimestamp();
		System.out.println("Data received from rest_control table: " + data);

		if (data != null) {
			String[] control_tbl_Data = data.split("\\|\\|");
			max_timestamp = control_tbl_Data[0];
			notification_flag = control_tbl_Data[1];
		}
		if (notification_flag == null || notification_flag.isEmpty() || notification_flag.equals("null")) {

			try {
				List<Transaction> tranData = connect.getLatestTranDetails(max_timestamp);
				System.out.println("Number of Recent Transactions: " + tranData.size());

				for (Transaction s : tranData) {
					if (s.getCustomerPhone() != null) {

						System.out.println("Transaction Amount: " + s.getTransactionAmt() + "\nTransaction Date: "
								+ s.getTransactionDate() + "\nTransaction Desc: " + s.getTransactionDesc()
								+ "\nCustomer Phone: " + s.getCustomerPhone());

						String message = "Test Message:\n"
								+ "You spent " + s.getTransactionAmt() + " for " +s.getTransactionDesc()+ " on "+s.getTransactionDate()+"";
								

						@SuppressWarnings("deprecation")
						AmazonSNSClient snsClient = new AmazonSNSClient(
								new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY));
						sendSMSMessage(snsClient, message, s.getCustomerPhone());
					} else {
						System.out.println("Customer phone not available!");
					}
				}
				connect.updateNotifyFlag(notification_flag, max_timestamp);
				System.out.println("Message is delivered!. Control table is updated with the flag!");
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Exception Raised while sending the transaction notification message!\n", e.getMessage());
			}
		} else {
			System.out.println(
					"Notification flag is already Y. That means the transaction message notification was send earlier!");
		}
	}
}