package rockPaperScissors;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import rockPaperScissors.RockPaperScissorsClient;

public class RockPaperScissorsClient {

	//creates JFrame to hold all the content
	private JFrame frame = new JFrame("Rock Paper Scissors");
	//create an empty label that will prompt the user
	private JLabel messageLabel = new JLabel("");
	
	//create a variable that will display the rock, paper, scissors objects
	private ImageIcon icon;
	//will be used to display the image the opponent chose
	private String opponentIcon;
	//displays what the image current player chose
	private String currentOption;
	//array the stores a string version of the options
	String[] optionLabels = { "Rock", "Paper", "Scissors" };
	//creates the buttons that the user will use to choose rock, paper, or scissors
	private JButton rockButton = new JButton();
	private JButton paperButton = new JButton();
	private JButton scissorsButton = new JButton();

	//sets the port number the server will be on 
	private static int PORT = 8901;
	//creates a socket to allow a thread connection to the server
	private Socket socket;
	//buffered reader is used to get socket input from the server
	private BufferedReader in;
	//printwrtier sends the socket ouptut to the server 
	private PrintWriter out;

	public RockPaperScissorsClient(String serverAddress) throws Exception {

		// Setup networking
		socket = new Socket(serverAddress, PORT);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// adds the message area to the bottom of the screen
		messageLabel.setBackground(Color.lightGray);
		frame.getContentPane().add(messageLabel, "South");
		//creates a JPanel that will store the rock paper scissors buttons
		JPanel optionsPanel = new JPanel();
		optionsPanel.setBackground(Color.black);
		//sets the layout for the buttons across the screen
		optionsPanel.setLayout(new GridLayout(1, 3, 2, 2));

		
		
		//set the image of the rock button and implement actionListener that will assign the String value of the choice
		rockButton.setIcon(new ImageIcon("rockPaperScissors/rock.png"));
		rockButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println("MOVE 0");
				//System.out.print("rock was sent");
				currentOption = optionLabels[0];
				
			}
		});
		//add button to the panel
		optionsPanel.add(rockButton);

		//sets the paper button image and implements the actionListoner that will assign the String value of the choice
		paperButton.setIcon(new ImageIcon("rockPaperScissors/paper.png"));
		paperButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println("MOVE 1");
				System.out.print("paper was sent");
				currentOption = optionLabels[1];
			}
		});
		//adds the button to the panel
		optionsPanel.add(paperButton);

		//sets the image for the scissors button and implements the actionListoner that will assign the String value of the choice
		scissorsButton.setIcon(new ImageIcon("rockPaperScissors/scissors.png"));
		scissorsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				out.println("MOVE 2");
				System.out.print("scissors was sent");
				currentOption = optionLabels[2];
			}
		});
		//adds the button to the JPanel
		optionsPanel.add(scissorsButton);
		//aligns the panels in the center of the JFrame
		frame.getContentPane().add(optionsPanel, "Center");

	}
	//class that updates the client gui based on server validation
	public void play() throws Exception {
		String response;
		int playerScore = 0;
		int opponentScore = 0;
		int roundsPlayed = 0;
		try {
			//a loop to run through the input from the server
			while (true) {
				//gets the response from the socket
				response = in.readLine();
				//looks for welcome message and sets the player message provide id and tell you to move
				if (response.startsWith("WELCOME")) {
					messageLabel.setText("You are player " + response.charAt(8) + ". Make your move.");
				} 
				//looks for validation from server to see if the opponent has moved
				else if (response.startsWith("VALID_MOVE")) {
					int id = Integer.parseInt(response.substring(11));
					if (id == 0 )
					{
						opponentIcon  = optionLabels[0];
					}
					else if (id == 1)
					{
						opponentIcon = optionLabels[1];
					}
					else 
					{
						opponentIcon = optionLabels[2];
					}
				    //sets the label text to display each players choices
				    messageLabel.setText("You chose " + currentOption + ". Opponent chose " + opponentIcon + ".");
				} 
			
				//looks for victory to identify a win for the current player
				else if (response.startsWith("WIN_ROUND")) {
					//updates the players score and number of rounds played
					playerScore++;
					roundsPlayed++;
					//checks for the number of times a player has won and if it equals 2, tells them they won the game or lost the game. otherwise it will display the score and the score of both players.
					if (playerScore == 2) {
						messageLabel.setText("Congratulations, you win the game!");
						break;
					} else if (opponentScore == 2) {
						messageLabel.setText("Sorry, you lost the game.");
						break;
					} else {
						messageLabel.setText(
								"You won this round! Score: " + playerScore + "-" + opponentScore + ". Next round!");
					}
				} 
				//checks to see if the player has won or lost the game 
				else if (response.startsWith("DEFEAT")) {
					//updates the opponents scores and number of rounds played
					opponentScore++;
					roundsPlayed++;
					//checks for the number of times a player has won and if it equals 2, tells them they won the game or lost the game. otherwise it will display the score and the score of both players.
					if (playerScore == 2) {
						messageLabel.setText("Congratulations, you win the game!");
						break;
					} else if (opponentScore == 2) {
						messageLabel.setText("Sorry, you lost the game.");
						break;
					} else {
						messageLabel.setText(
								"You lost this round. Score: " + playerScore + "-" + opponentScore + ". Next round!");
					}
				}
				//looks for TIE identifier and will notify the players that neither win
				else if (response.startsWith("TIE")) {
					messageLabel.setText("It's a tie.");
				} 
				//looks of message identifier to change the message label with a message from the server
				else if (response.startsWith("MESSAGE")) {
					messageLabel.setText(response.substring(8));
				} 
			}
		} catch (Exception e) {
			// handle the exception
			e.printStackTrace();
		}
	}

	private boolean wantsToPlayAgain() {
		int response = JOptionPane.showConfirmDialog(frame, "Want to play again?", "For round two select yes.",
				JOptionPane.YES_NO_OPTION);
		frame.dispose();
		return response == JOptionPane.YES_OPTION;
	}

	public static void main(String[] args) throws Exception {
		while (true) {
			String serverAddress = (args.length == 0) ? "localhost" : args[0];
			RockPaperScissorsClient client = new RockPaperScissorsClient(serverAddress);
			client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			client.frame.setSize(320, 240);
			client.frame.setVisible(true);
			client.frame.setResizable(false);
			client.play();
			if (!client.wantsToPlayAgain()) {
				break;
			}
		}
	}
}
