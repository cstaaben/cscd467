// Fig. 24.15: TicTacToeClient.java
// Client that let a user play Tic-Tac-Toe with another across a network.

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

public class TicTacToeClient extends JFrame implements Runnable 
{
	private JTextField idField; // textfield to display player's mark
	private JTextArea displayArea; // JTextArea to display output
	private JPanel boardPanel; // panel for tic-tac-toe board
	private JPanel panel2; // panel to hold board
	private Square board[][]; // tic-tac-toe board
	private Square currentSquare; // current square
	private String myMark; // this client's mark
	private boolean myTurn; // determines which client's turn it is
	private final String X_MARK = "X"; // mark for first client
	private final String O_MARK = "O"; // mark for second client
	private final int bsize = 16;
	
	private final static AmazonSQSClient client = (AmazonSQSClient) AmazonSQSClientBuilder.standard()
			.withRegion(Regions.US_WEST_2).build();
	private final String queue;
	private final String MSG_GROUP = "CSCD467Final_TicTacToe";
	private SendMessageResult sendResult;
	
	// set up user-interface and board
	public TicTacToeClient( String host )
	{
		queue = createOrFetchQueue(host);
		
		displayArea = new JTextArea( 4, 30 ); // set up JTextArea
		displayArea.setEditable( false );
		add( new JScrollPane( displayArea ), BorderLayout.SOUTH );
		
		boardPanel = new JPanel(); // set up panel for squares in board
		boardPanel.setLayout( new GridLayout( bsize, bsize, 0, 0 ) ); //was 3
		
		board = new Square[ bsize ][ bsize ]; // create board
		
		// loop over the rows in the board
		for ( int row = 0; row < board.length; row++ )
		{
			// loop over the columns in the board
			for ( int column = 0; column < board[ row ].length; column++ )
			{
				// create square. initially the symbol on each square is a white space.
				board[ row ][ column ] = new Square( " ", row * bsize + column );
				boardPanel.add( board[ row ][ column ] ); // add square
			} // end inner for
		} // end outer for
		
		idField = new JTextField(); // set up textfield
		idField.setEditable( false );
		add( idField, BorderLayout.NORTH );
		
		panel2 = new JPanel(); // set up panel to contain boardPanel
		panel2.add( boardPanel, BorderLayout.CENTER ); // add board panel
		add( panel2, BorderLayout.CENTER ); // add container panel
		
		setSize( 600, 600 ); // set size of window
		setVisible( true ); // show window
		
		startClient();
	} // end TicTacToeClient constructor
	
	private String createOrFetchQueue(String host) {
		String q;
		String name = (host.length() > 0) ? host : MSG_GROUP + ".fifo";
		
		HashMap<String, String> attributes = new HashMap<>();
		attributes.put("FifoQueue", "true");
		attributes.put("ContentBasedDeduplication", "true");
		attributes.put("MessageRetentionPeriod", "86400");
		
		q = client.createQueue(new CreateQueueRequest()
				.withQueueName(name)
				.withAttributes(attributes)) // end createQueue
				.getQueueUrl();
		
		return q;
	}
	
	// start the client thread
	public void startClient()
	{
		// create and start worker thread for this client
		ExecutorService worker = Executors.newFixedThreadPool( 1 );
		worker.execute( this ); // execute client
	} // end method startClient
	
	// control thread that allows continuous update of displayArea
	public void run() {
//		myMark =  "X"; //Get player's mark (X or O). We hard coded here in demo. In your implementation, you may get this mark dynamically
			 //from the cloud service. This is the initial state of the game.
		Message m = receiveMessage();
//		myMark = (m.getBody().length() == 0) ? X_MARK : O_MARK;
		if(m.getBody().length() == 0) {
			myMark = X_MARK;
			sendResult = sendMessage(myMark + " connected.");
			displayMessage("Waiting for " + O_MARK + " player to connect.");
			
			m = waitForNewMsg(m.getMessageId());
			displayMessage(m.getBody());
			deleteMessage(m);
			
			sendResult = sendMessage("GAME START");
			displayMessage("GAME START");
		}
		else {
			myMark = O_MARK;
			sendResult = sendMessage(myMark + " connected.");
			displayMessage("Connected.");
			
			m = waitForNewMsg(sendResult.getMessageId());
			displayMessage(m.getBody());
			deleteMessage(m);
		}
		printErrors(sendResult);
		
		SwingUtilities.invokeLater(() -> {
			// display player's mark
			idField.setText( "You are player \"" + myMark + "\"" );
		}); // end call to SwingUtilities.invokeLater
		
		myTurn = ( myMark.equals( X_MARK ) ); // determine if client's turn
		
		// program the game logic below
		while ( ! isGameOver() )
		{
			// Here in this while body, you will program the game logic.
			// You are free to add any helper methods in this class or other classes.
			// Basically, this client player will retrieve a message from cloud in each while iteration
			// and process it until game over is detected.
			// Please check the processMessage() method below to gain some clues.
			
			
		
		} // end while
		

	} // end method run
	
	private void printErrors(AmazonWebServiceResult<ResponseMetadata> result) {
		if(result.getSdkHttpMetadata().getHttpStatusCode() != 200) {
			Map<String, String> headers = result.getSdkHttpMetadata().getHttpHeaders();
			
			for(String key : headers.keySet()) {
				System.err.printf("%s: %s\n", key, headers.get(key));
			} // end foreach
		}
	} // end printErrors
	
	/**
	 * Delete an entire collection of messages from the queue
	 * @param messages the messages to be deleted
	 */
	private void deleteMessages(List<Message> messages) {
		for(Message message : messages) {
			client.deleteMessage(new DeleteMessageRequest().withQueueUrl(queue).withReceiptHandle(message.getReceiptHandle()));
		}
	}
	
	/**
	 * Delete a single message from the queue
	 * @param message the message to be deleted
	 */
	private void deleteMessage(Message message) {
		client.deleteMessage(new DeleteMessageRequest().withQueueUrl(queue).withReceiptHandle(message.getReceiptHandle()));
	}
	
	/**
	 * Sends a message to the AWS FIFO SQS with an unique ID to allow for duplicate messages to be treated individually
	 * @param message the message to be sent to the queue
	 * @return the SendMessageResult returned from the AmazonSQSClient.sendMessage function
	 */
	private SendMessageResult sendMessage(String message) {
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
	private Message receiveMessage() {
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
	private Message waitForNewMsg(String oldMsgId) {
		Message result = new Message().withBody("");
		List<Message> messages;
		
		while(result.getBody().length() == 0 || result.getMessageId().equals(oldMsgId)) {
			messages = client.receiveMessage(new ReceiveMessageRequest()
					.withQueueUrl(queue)
					.withWaitTimeSeconds(20)
					.withMaxNumberOfMessages(1)
			).getMessages();
			
			result = (messages.size() == 0) ? new Message().withBody("") : messages.get(0);
		}
		
		return result;
	}
	
	// You have write this method that checks the game board to detect winning status.
	private boolean isGameOver() {
	   return false;
	}
	
	// This method is not used currently, but it may give you some hints regarding
	// how one client talks to other client through cloud service(s).
	private void processMessage( String message )
	{
		// valid move occurred
		if ( message.equals( "Opponent Won" ) )
		{
			displayMessage( "Game over, Opponent won.\n" );
			// then highlight the winning locations down below.
		
		} // end if
		else if ( message.equals( "Opponent moved" ) )
		{
			int location = getOpponentMove(); // Here get move location from opponent
										
			int row = location / bsize; // calculate row
			int column = location % bsize; // calculate column
			
			setMark(  board[ row ][ column ],
			  ( myMark.equals( X_MARK ) ? O_MARK : X_MARK ) ); // mark move
			displayMessage( "Opponent moved. Your turn.\n" );
			myTurn = true; // now this client's turn
		} // end else if
		else
			displayMessage( message + "\n" ); // display the message
	} // end method processMessage
	
	//Here get move location from opponent
	private int getOpponentMove() {
		// Please write your code here
		return 0;
	}
	// manipulate outputArea in event-dispatch thread
	private void displayMessage( final String messageToDisplay )
	{
		SwingUtilities.invokeLater(() -> {
			displayArea.append( messageToDisplay ); // updates output
		}); // end call to SwingUtilities.invokeLater
	} // end method displayMessage
	
	// utility method to set mark on board in event-dispatch thread
	private void setMark( final Square squareToMark, final String mark )
	{
		SwingUtilities.invokeLater(() ->
			  {
				squareToMark.setMark( mark ); // set mark in square
			}); // end call to SwingUtilities.invokeLater
	} // end method setMark
	
	// Send message to cloud service indicating clicked square
	public void sendClickedSquare( int location )
	{
		// if it is my turn
		if ( myTurn ) {
			// Below you send the clicked location to the cloud service that will notify the opponent,
			// Or the opponent will retrieve the move location itself.
			// Please write your own code below.
			
			myTurn = false; // not my turn anymore
		} // end if
	} // end method sendClickedSquare
	
	// set current Square
	public void setCurrentSquare( Square square )
	{
		currentSquare = square; // set current square to argument
	} // end method setCurrentSquare
	
	// private inner class for the squares on the board
	private class Square extends JPanel {
		private String mark; // mark to be drawn in this square
		private int location; // location of square
		
		public Square( String squareMark, int squareLocation ) {
			mark = squareMark; // set mark for this square
			location = squareLocation; // set location of this square
			
			addMouseListener(new MouseAdapter() {
				public void mouseReleased( MouseEvent e )
				{
					setCurrentSquare( Square.this ); // set current square
					TicTacToeClient.this.setMark( currentSquare, myMark );
					displayMessage("You clicked at location: " + getSquareLocation() + "\n");
					
					// You may have to send location of this square to
					// the cloud service that will notify the opponent player.
					//if(isValidMove()) // you have write your own method isValidMove().
					    //sendClickedSquare( getSquareLocation() );
					
					} // end method mouseReleased
				} // end anonymous inner class
			); // end call to addMouseListener
		} // end Square constructor
		
		// return preferred size of Square
		public Dimension getPreferredSize()
		{
			return new Dimension( 30, 30 ); // return preferred size
		} // end method getPreferredSize
		
		// return minimum size of Square
		public Dimension getMinimumSize()
		{
			return getPreferredSize(); // return preferred size
		} // end method getMinimumSize
		
		// set mark for Square
		public void setMark( String newMark )
		{
			mark = newMark; // set mark of square
			repaint(); // repaint square
		} // end method setMark
		
		// return Square location
		public int getSquareLocation()
		{
			return location; // return location of square
		} // end method getSquareLocation
		
		// draw Square
		public void paintComponent( Graphics g )
		{
			super.paintComponent( g );
			
			g.drawRect( 0, 0, 29, 29 ); // draw square
			g.drawString( mark, 11, 20 ); // draw mark
		} // end method paintComponent
	} // end inner-class Square
	
	
	
	public static void main( String args[] )
	{
		TicTacToeClient application; // declare client application
		
		// if no command line args
		if ( args.length == 0 )
			application = new TicTacToeClient( "" ); //
		else
			application = new TicTacToeClient( args[ 0 ] ); // use args
		
		application.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				client.purgeQueue(new PurgeQueueRequest(client.getQueueUrl(new GetQueueUrlRequest(args[0])).getQueueUrl()));
			}
		});
		
		application.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
	} // end main

} // end class TicTacToeClient


