import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Class to test random snippets of code
 */
public class Snippets {
	private static final String QUEUE_NAME = "CSCD467Final_TicTacToe.fifo";
	private static final String MSG_GROUP = "CSCD467Final_TicTacToe";
	private final static AmazonSQSClient client = (AmazonSQSClient) AmazonSQSClientBuilder.standard()
			.withRegion(Regions.US_WEST_2)
			.build();
	private static String queue = client.getQueueUrl(new GetQueueUrlRequest().withQueueName(QUEUE_NAME)).getQueueUrl();
	
	public static void main(String[] args) {
		Message m = receiveMessage();
		SendMessageResult sendResult;
		
		if(m.getBody().length() == 0) {
			sendResult = sendMessage("Player X connected.");
		}
		else {
			sendResult = sendMessage("Player O connected.");
		}
		
		if(sendResult.getSdkHttpMetadata().getHttpStatusCode() == 200) {
			System.out.println("Message sent");
		}
		else {
			Map<String, String> headers = sendResult.getSdkHttpMetadata().getHttpHeaders();
			for(String key : headers.keySet()) {
				System.out.printf("%s\t%s", key, headers.get(key));
			}
		}
		m = waitForNewMsg(sendResult.getMessageId());
		System.out.println(m.getBody());
		deleteMessage(m);
	}
	
	/**
	 * Delete an entire collection of messages from the queue
	 * @param messages the messages to be deleted
	 */
	private static void deleteMessages(List<Message> messages) {
		for(Message message : messages) {
			client.deleteMessage(new DeleteMessageRequest().withQueueUrl(queue).withReceiptHandle(message.getReceiptHandle()));
		}
	}
	
	/**
	 * Delete a single message from the queue
	 * @param message the message to be deleted
	 */
	private static void deleteMessage(Message message) {
		client.deleteMessage(new DeleteMessageRequest().withQueueUrl(queue).withReceiptHandle(message.getReceiptHandle()));
	}
	
	/**
	 * Sends a message to the AWS FIFO SQS with an unique ID to allow for duplicate messages to be treated individually
	 * @param message the message to be sent to the queue
	 * @return the SendMessageResult returned from the AmazonSQSClient.sendMessage function
	 */
	private static SendMessageResult sendMessage(String message) {
		return client.sendMessage(new SendMessageRequest()
				.withQueueUrl(queue)
				.withMessageBody(message)
				.withMessageGroupId(MSG_GROUP)
				.withMessageDeduplicationId(UUID.randomUUID().toString())
		);
	}
	
	/**
	 * Receive a single message with a default wait time of 20 seconds; used to test if current instance is Player 1 or 2
	 * @return a new Message object with an empty body if there are no messages in the queue, otherwise the
	 * 			received Message object
	 */
	private static Message receiveMessage() {
		List<Message> messages =  client.receiveMessage(new ReceiveMessageRequest()
				.withQueueUrl(queue)
				.withWaitTimeSeconds(20)
				.withMaxNumberOfMessages(1)
		).getMessages();
		
		return (messages.size() == 0) ? new Message().withBody("") : messages.get(0);
	}
	
	/**
	 * Continually poll the queue for a message with a new message ID, with a default setting of long polling for 20 seconds
	 * and a maximum of 1 message returned
	 * @param oldMsgId a previously obtained message ID
	 * @return the new Message object
	 */
	private static Message waitForNewMsg(String oldMsgId) {
		Message result = new Message().withBody("");
		List<Message> messages;
		
		while(result.getBody().length() == 0 || result.getMessageId().equals(oldMsgId)) {
			System.out.println("Waiting...");
			
			messages = client.receiveMessage(new ReceiveMessageRequest()
					.withQueueUrl(queue)
					.withWaitTimeSeconds(20)
					.withMaxNumberOfMessages(1)
			).getMessages();
			
			result = (messages.size() == 0) ? new Message().withBody("") : messages.get(0);
		}
		
		return result;
	}
}
