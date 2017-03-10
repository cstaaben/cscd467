import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.util.List;

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
		Message m = waitForMessage();
		if(m.getBody().length() == 0) {
			sendMessage("Player 1 connected");
			System.out.println("Player 1 connected");
			
			m = waitForMessage();
			
			System.out.println(m.getBody());
			deleteMessage(m.getReceiptHandle());
			
			sendMessage("GAME START");
		}
		else {
			deleteMessage(m.getReceiptHandle());
			sendMessage("Player 2 connected.");
			System.out.println("Player 2 connected.");
			
			m = waitForMessage();
			System.out.println(m.getBody());
			deleteMessage(m.getReceiptHandle());
		}
	}
	
	private static void deleteMessage(String id) {
		client.deleteMessage(new DeleteMessageRequest().withQueueUrl(queue).withReceiptHandle(id));
	}
	
	private static void sendMessage(String message) {
		client.sendMessage(new SendMessageRequest()
				.withQueueUrl(queue)
				.withMessageBody(message)
				.withMessageGroupId(MSG_GROUP)
		);
	}
	
	private static Message waitForMessage() {
		Message result = new Message().withBody("");
		List<Message> messages;
		
		while(result.getBody().length() == 0) {
			messages = client.receiveMessage(new ReceiveMessageRequest()
					.withQueueUrl(queue)
					.withWaitTimeSeconds(1)
					.withMaxNumberOfMessages(1)
			).getMessages();
			
			result = (messages.size() == 0) ? new Message().withBody("") : messages.get(0);
		}
		
		return result;
	}
}
