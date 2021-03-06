// Fig. 24.15: TicTacToeClient.java
// Client that let a user play Tic-Tac-Toe with another across a network.

import com.amazonaws.AmazonWebServiceResult;
import com.amazonaws.ResponseMetadata;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import java.awt.*;
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

import static java.lang.Thread.sleep;

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
	
	private WinningLocation[] winLocations = new WinningLocation[5];
	private boolean won = false;
	
	private final static AmazonSQSClient client = (AmazonSQSClient) AmazonSQSClientBuilder.standard()
			.withRegion(Regions.US_WEST_2).build();
	private final String queue;
	private final static String MSG_GROUP = "CSCD467Final_TicTacToe";
	private final static String OPP_MOVE = "Opponent moved.";
	private final static String OPP_WON = "Opponent Won";
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
			
			m = waitForNewMsg(sendResult.getMessageId());
			displayMessage(m.getBody());
			deleteMessage(m);
			
			sendResult = sendMessage("GAME START");
			displayMessage("GAME START");
		}
		else {
			myMark = O_MARK;
			sendResult = sendMessage(myMark + " connected.");
			displayMessage("Connected. Waiting to start...");
			
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
		displayMessage((myTurn) ? "Your move." : "Opponents move.");
		
		// program the game logic below
		while ( ! isGameOver() && !won)
		{
			// Here in this while body, you will program the game logic.
			// You are free to add any helper methods in this class or other classes.
			// Basically, this client player will retrieve a message from cloud in each while iteration
			// and process it until game over is detected.
			// Please check the processMessage() method below to gain some clues.
			
			while(!myTurn && !won) {
				m = waitForNewMsg(sendResult.getMessageId());
				deleteMessage(m);
				processMessage(m.getBody());
			} // end while !myTurn
		} // end while
		
		highlightWin();
		closeClient();
	} // end method run
	
	private void closeClient() {
		new Thread(() -> {
			for(int i = 10; i > 0; i--) {
				displayMessage("Closing client in " + (i) + "...");
				try {
					Thread.sleep(1000);
				}
				catch(InterruptedException ie) {
					ie.printStackTrace();
				}
			}
			displayMessage("Closing.");
			
			System.exit(0);
		}).start();
	}
	
	/**
	 * Print any errors resulting from a message being sent or received to the Amazon SQS
	 * @param result any MessageResult returned from performing a message operation
	 */
	private void printErrors(AmazonWebServiceResult<ResponseMetadata> result) {
		if(result.getSdkHttpMetadata().getHttpStatusCode() != 200) {
			Map<String, String> headers = result.getSdkHttpMetadata().getHttpHeaders();
			
			for(String key : headers.keySet()) {
				System.err.printf("%s: %s\n", key, headers.get(key));
			} // end foreach
		}
	} // end printErrors
	
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
//		displayMessage("Sending: " + message);
		
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
					.withWaitTimeSeconds(5)
					.withMaxNumberOfMessages(1)
			).getMessages();
			
			result = (messages.size() == 0) ? new Message().withBody("") : messages.get(0);
		}
		
//		displayMessage("Received: " + result.getBody());
		
		return result;
	}
	
	// You have write this method that checks the game board to detect winning status.
	private boolean isGameOver() {
		String curMark;
		
		for(int i = 0; i < board.length; i++) {
			for(int j = 0; j < board[i].length; j++) {
				// make sure current square has a mark in it
				if(!board[i][j].getMark().equals(" ")) {
					curMark = board[i][j].getMark(); // set mark to search for
					
					if(i+4 < board.length) {
						if(j < 4) {
							if(checkLeftArea(i, j, curMark)) {
								sendResult = sendMessage(OPP_WON);
								return true;
							}
						} // end if (j == 0)
						else if(j+4 < board[i].length) {
							if(checkCenterArea(i, j, curMark)) {
								sendResult = sendMessage(OPP_WON);
								return true;
							}
						} // end else-if (j+5)
						else {
							if(checkRightArea(i, j, curMark)) {
								sendResult = sendMessage(OPP_WON);
								return true;
							}
						} // end else
					} // end if (i+5)
					else {
						if(checkBottomArea(i, j, curMark)) {
							sendResult = sendMessage(OPP_WON);
							return true;
						}
					}
				} // end if board
			} // end for j
		} // end for i
		
		return false;
	} // end isGameOver
	
	private boolean checkLeftArea(int row, int col, String mark) {
		int c = 0;
		int markCnt = 0;
		
		// check right
		while(c < 5 && board[row][col+c].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "right");
			return true;
		}
		else {
			c = 0;
			markCnt = 0;
		}
		
		// check down
		while(c < 5 && board[row+c][col].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "down");
			return true;
		}
		else {
			c = 0;
			markCnt = 0;
		}
		
		// check down right
		while(c < 5 && board[row+c][col+c].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "down-right");
			return true;
		}
		else {
			c = 0;
			markCnt = 0;
		}
		
		return false;
	}
	
	private boolean checkCenterArea(int row, int col, String mark) {
		int c = 0;
		int markCnt = 0;
		
		// check right
		while(c < 5 && board[row][col+c].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "right");
			return true;
		}
		else {
			markCnt = 0;
			c = 0;
		}
		
		// check down
		while(c < 5 && board[row+c][col].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "down");
			return true;
		}
		else {
			markCnt = 0;
			c = 0;
		}
		
		// check down-right
		while(c < 5 && board[row+c][col+c].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "down-right");
			return true;
		}
		else {
			markCnt = 0;
			c = 0;
		}
		
		// check down-left
		if(col >= 4) {
			while(c < 5 && board[row+c][col-c].getMark().equals(mark)) {
				c++;
				markCnt++;
			}
			
			if(markCnt == 5) {
				storeWinningLocation(row, col, "down-left");
				return true;
			}
			else {
				c = 0;
				markCnt = 0;
			}
		} //end if (col >= 4)
		
		return false;
	}
	
	private boolean checkRightArea(int row, int col, String mark) {
		int c = 0;
		int markCnt = 0;
		
		// check down
		while(c < 5 && board[row+c][col].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "down");
			return true;
		}
		else {
			markCnt = 0;
			c = 0;
		}
		
		// check down-left
		while(c < 5 && board[row+c][col-c].getMark().equals(mark)) {
			c++;
			markCnt++;
		}
		
		if(markCnt == 5) {
			storeWinningLocation(row, col, "down-left");
			return true;
		}
		else {
			markCnt = 0;
			c = 0;
		}
		
		return false;
	}
	
	private boolean checkBottomArea(int row, int col, String mark) {
		int markCnt = 0;
		int c = 0;
		
		if(col <= 4) {
			// check up
			while(c < 5 && board[row-c][col].getMark().equals(mark)) {
				c++;
				markCnt++;
			}
			
			if(markCnt == 5) {
				storeWinningLocation(row, col, "up");
				return true;
			}
			else {
				markCnt = 0;
				c = 0;
			}
			
			// check right
			while(c < 5 && board[row][col+c].getMark().equals(mark)) {
				c++;
				markCnt++;
			}
			
			if(markCnt == 5) {
				storeWinningLocation(row, col, "right");
				return true;
			}
			else {
				markCnt = 0;
				c = 0;
			}
			
			// check up-right
			while(c < 5 && board[row-c][col+c].getMark().equals(mark)) {
				c++;
				markCnt++;
			}
			
			if(markCnt == 5) {
				storeWinningLocation(row, col, "up-right");
				return true;
			}
			else {
				markCnt = 0;
				c = 0;
			}
		} // end if (col < 4)
		else if(col+5 < board[row].length) {
			// check up
			while(c < 5 && board[row-c][col].getMark().equals(mark)) {
				c++;
				markCnt++;
			}
			
			if(markCnt == 5) {
				storeWinningLocation(row, col, "up");
				return true;
			}
			else {
				c = 0;
				markCnt = 0;
			}
			
			// check up-right
			while(c < 5 && board[row-c][col+c].getMark().equals(mark)) {
				c++;
				markCnt++;
			}
			
			if(markCnt == 5) {
				storeWinningLocation(row, col, "up-right");
				return true;
			}
			else {
				c = 0;
				markCnt = 0;
			}
			
			// check right
			while(c < 5 && board[row][col+c].getMark().equals(mark)) {
				c++;
				markCnt++;
			}
			
			if(markCnt == 5) {
				storeWinningLocation(row, col, "right");
				return true;
			}
			else {
				c = 0;
				markCnt = 0;
			}
		}
		
		return false;
	}
	
	private void storeWinningLocation(int row, int col, String direction) {
		switch(direction) {
			case "right":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row, col+i);
				}
				break;
				
			case "left":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row, col-i);
				}
				break;
				
			case "up":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row-i, col);
				}
				break;
				
			case "down":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row+i, col);
				}
				break;
			
			case "up-right":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row-i, col+i);
				}
				break;
			
			case "up-left":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row-i, col-i);
				}
				break;
			
			case "down-right":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row+i, col+i);
				}
				break;
			
			case "down-left":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new WinningLocation(row+i, col-i);
				}
				break;
			
		} // end switch(direction)
	}
	
	// This method is not used currently, but it may give you some hints regarding
	// how one client talks to other client through cloud service(s).
	private void processMessage( String message )
	{
		switch(message) {
			case OPP_WON:
				displayMessage("Game over, You won.");
				// highlight winning locations
				highlightWin();
				myTurn = true;
				break;
				
			case OPP_MOVE:
				int loc = getOpponentMove();
				int row = loc/bsize;
				int col = loc % bsize;
				
				setMark(board[row][col], (myMark.equals(X_MARK) ? O_MARK : X_MARK));
				displayMessage("Opponent moved. Your turn.");
				myTurn = true;
				
				tempSquareColor(row, col);
				
				break;
				
			default:
				displayMessage(message);
		} // end switch message
	} // end method processMessage
	
	private void tempSquareColor(final int row, final int col) {
		new Thread(()-> {
			Square sq = board[row][col];
			
			if(sq.getBackground() != null) {
				sq.setBackground(Color.YELLOW);
				
				try {
					Thread.sleep(2000);
				}
				catch(InterruptedException ie) {
					ie.printStackTrace();
				}
				
				sq.setBackground(null);
			}
		}).start();
	}
	
	private void highlightWin() {
		displayMessage(OPP_WON);
		
		new Thread(() -> {
			for(WinningLocation loc : winLocations) {
				board[loc.getRow()][loc.getCol()].setBackground(Color.GREEN);
			}
		}).start();
	}
	
	//Here get move location from opponent
	private int getOpponentMove() {
		Message m = receiveMessage();
		int loc = -1;
		
		while(loc < 0) {
			try {
				loc = Integer.parseInt(m.getBody());
			}
			catch(NumberFormatException nfe) {
				loc = -1;
				m = receiveMessage();
			}
		}
		
		deleteMessage(m);
		
		return loc;
	}
	// manipulate outputArea in event-dispatch thread
	private void displayMessage( final String messageToDisplay )
	{
		SwingUtilities.invokeLater(() -> {
			displayArea.append( messageToDisplay + "\n"); // updates output
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
			sendResult = sendMessage(location + "");
//			displayMessage("Not my turn anymore.");
			
			myTurn = false; // not my turn anymore
		} // end if
	} // end method sendClickedSquare
	
	// set current Square
	public void setCurrentSquare( Square square )
	{
		currentSquare = square; // set current square to argument
	} // end method setCurrentSquare
	
	private boolean isValidMove(int location) {
		int row = location / bsize;
		int col = location % bsize;
		
		return board[row][col].getMark().equals(" ") && myTurn;
	}
	
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
					if(isValidMove(Square.this.location)) {
						TicTacToeClient.this.setMark( currentSquare, myMark );
//						displayMessage("You clicked at location: " + getSquareLocation());
						sendResult = sendMessage(OPP_MOVE);
						sendClickedSquare(location);
						
						won = isGameOver();
					}
					else {
						displayMessage((myTurn) ? "Invalid move. Try again." : "Not your turn. Please wait.");
					}
					
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
		
		public String getMark() {
			return mark;
		}
	} // end inner-class Square
	
	private class WinningLocation {
		private int row;
		private int col;
		
		public WinningLocation(int row, int col) {
			this.row = row;
			this.col = col;
		}
		
		public int getRow() {
			return row;
		}
		
		public int getCol() {
			return col;
		}
	}
	
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


