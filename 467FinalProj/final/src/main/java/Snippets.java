import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class to test random snippets of code
 */
public class Snippets extends JFrame implements Runnable {
	private JTextField idField; // textfield to display player's mark
	private JTextArea displayArea; // JTextArea to display output
	private JPanel boardPanel; // panel for tic-tac-toe board
	private JPanel panel2; // panel to hold board
	private Snippets.Square board[][]; // tic-tac-toe board
	private Snippets.Square currentSquare; // current square
	private String myMark; // this client's mark
	private boolean myTurn; // determines which client's turn it is
	private final String X_MARK = "X"; // mark for first client
	private final String O_MARK = "O"; // mark for second client
	private final int bsize = 16;
	
	private final static String OPP_MOVE = "Opponent moved.";
	private final static String OPP_WON = "Opponent Won";
	private WinningLocation[] winLocations = new WinningLocation[5];
	
	// set up user-interface and board
	public Snippets( String host )
	{
		displayArea = new JTextArea( 4, 30 ); // set up JTextArea
		displayArea.setEditable( false );
		add( new JScrollPane( displayArea ), BorderLayout.SOUTH );
		
		boardPanel = new JPanel(); // set up panel for squares in board
		boardPanel.setLayout( new GridLayout( bsize, bsize, 0, 0 ) ); //was 3
		
		board = new Snippets.Square[ bsize ][ bsize ]; // create board
		
		// loop over the rows in the board
		for ( int row = 0; row < board.length; row++ )
		{
			// loop over the columns in the board
			for ( int column = 0; column < board[ row ].length; column++ )
			{
				// create square. initially the symbol on each square is a white space.
				board[ row ][ column ] = new Snippets.Square( " ", row * bsize + column );
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
	} // end Snippets constructor
	
	// start the client thread
	public void startClient()
	{
		// create and start worker thread for this client
		ExecutorService worker = Executors.newFixedThreadPool( 1 );
		worker.execute( this ); // execute client
	} // end method startClient
	
	// control thread that allows continuous update of displayArea
	public void run()
	{
		myMark =  "X"; //Get player's mark (X or O). We hard coded here in demo. In your implementation, you may get this mark dynamically 
		//from the cloud service. This is the initial state of the game.
		
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						// display player's mark
						idField.setText( "You are player \"" + myMark + "\"" );
					} // end method run
				} // end anonymous inner class
		); // end call to SwingUtilities.invokeLater
		
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
		highlightWin();
		
//		closeClient();
	} // end method run
	
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
								displayMessage("Game over");
								return true;
							}
						} // end if (j == 0)
						else if(j+4 < board[i].length) {
							if(checkCenterArea(i, j, curMark)) {
								displayMessage("Game over");
								return true;
							}
						} // end else-if (j+5)
						else {
							if(checkRightArea(i, j, curMark)) {
								displayMessage("Game over");
								return true;
							}
						} // end else
					} // end if (i+5)
					else {
						if(checkBottomArea(i, j, curMark)) {
							displayMessage("Game over");
							return true;
						}
					}
				} // end if board
			} // end for j
		} // end for i
		
		return false;
	}
	
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
					winLocations[i] = new Snippets.WinningLocation(row, col+i);
				}
				break;
			
			case "left":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new Snippets.WinningLocation(row, col-i);
				}
				break;
			
			case "up":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new Snippets.WinningLocation(row+i, col);
				}
				break;
			
			case "down":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new Snippets.WinningLocation(row-i, col);
				}
				break;
			
			case "up-right":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new Snippets.WinningLocation(row-i, col+i);
				}
				break;
			
			case "up-left":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new Snippets.WinningLocation(row-i, col-i);
				}
				break;
			
			case "down-right":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new Snippets.WinningLocation(row+i, col+i);
				}
				break;
			
			case "down-left":
				for(int i = 0; i < winLocations.length; i++) {
					winLocations[i] = new Snippets.WinningLocation(row+i, col-i);
				}
				break;
			
		} // end switch(direction)
	}
	
	private void closeClient() {
		new Thread(() -> {
			for(int i = 5; i > 0; i--) {
				displayMessage("Closing client in " + (i) + "...\n");
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
	
	// This method is not used currently, but it may give you some hints regarding
	// how one client talks to other client through cloud service(s).
	private void processMessage( String message )
	{
		switch(message) {
			case OPP_WON:
				displayMessage("Game over, Opponent won.");
				// highlight winning locations
				highlightWin();
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
			Snippets.Square sq = board[row][col];
			
			sq.setBackground(Color.YELLOW);
			
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException ie) {
				ie.printStackTrace();
			}
			
			sq.setBackground(null);
		}).start();
	}
	
	private void highlightWin() {
		new Thread(() -> {
			for(Snippets.WinningLocation loc : winLocations) {
				board[loc.getRow()][loc.getCol()].setBackground(Color.GREEN);
			}
		}).start();
	}
	
	//Here get move location from opponent
	private int getOpponentMove() {
		// Please write your code here
		return 0;
	}
	// manipulate outputArea in event-dispatch thread
	private void displayMessage( final String messageToDisplay )
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						displayArea.append( messageToDisplay ); // updates output
					} // end method run
				}  // end inner class
		); // end call to SwingUtilities.invokeLater
	} // end method displayMessage
	
	// utility method to set mark on board in event-dispatch thread
	private void setMark(final Snippets.Square squareToMark, final String mark )
	{
		SwingUtilities.invokeLater(
				new Runnable()
				{
					public void run()
					{
						squareToMark.setMark( mark ); // set mark in square
					} // end method run
				} // end anonymous inner class
		); // end call to SwingUtilities.invokeLater
	} // end method setMark
	
	// Send message to cloud service indicating clicked square
	public void sendClickedSquare( int location )
	{
		// if it is my turn
		if ( myTurn )
		{
			// Below you send the clicked location to the cloud service that will notify the opponent,
			// Or the opponent will retrieve the move location itself.
			// Please write your own code below.
			
			myTurn = false; // not my turn anymore
		} // end if
	} // end method sendClickedSquare
	
	// set current Square
	public void setCurrentSquare( Snippets.Square square )
	{
		currentSquare = square; // set current square to argument
	} // end method setCurrentSquare
	
	// private inner class for the squares on the board
	private class Square extends JPanel
	{
		private String mark; // mark to be drawn in this square
		private int location; // location of square
		
		public Square( String squareMark, int squareLocation )
		{
			mark = squareMark; // set mark for this square
			location = squareLocation; // set location of this square
			
			addMouseListener(
					new MouseAdapter() {
						public void mouseReleased( MouseEvent e )
						{
							setCurrentSquare( Snippets.Square.this ); // set current square
							Snippets.this.setMark( currentSquare, myMark );
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
		
		public String getMark() {
			return mark;
		}
		
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
		Snippets application; // declare client application
		
		// if no command line args
		if ( args.length == 0 )
			application = new Snippets( "" ); // 
		else
			application = new Snippets( args[ 0 ] ); // use args
		
		application.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	} // end main
}
