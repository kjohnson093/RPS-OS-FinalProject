package rockPaperScissors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class RockPaperScissorsServer {


	public static void main (String [] args) throws Exception {
		ServerSocket listener = new ServerSocket(8901);
		System.out.println("Server is Running");
		try {
			//Start a game
			Game game = new Game();
			//Connect players
			Game.Player player1 = game.new Player (listener.accept());
			player1.setNumber(1);
			System.out.println("Player 1 Connected");
			Game.Player player2 = game.new Player (listener.accept());
			player2.setNumber(2);
			System.out.println("Player 2 Connected");
			//Start game
			player1.start();
			player2.start();

		}
		finally {
			listener.close();
		}
	}
}


class Game {
	/*
	 * Each player chooses one of the three options. Paper beats rock. Rock beats scissors. Scissors beat paper.
	 * Best of 3 wins.
	 */

	//0 = Rock, 1 = Paper, 2 = Scissors
	int player1choice = -1;
	int player2choice = -1;
	int player1Wins = 0;
	int player2Wins = 0;

	public void newRound() {
			player1choice = -1;
			player2choice = -1;
	}


	public boolean legalMove(int player, int choice) {
		//Check if player 1
		if (player == 1)
		{
			//Check if choice already selected
			if (player1choice == 0 || player1choice == 1 || player1choice == 2)
			{
				return false;
			}
			//Set player 1 choice
			player1choice = choice;
			return true;
		}
		else
			//Set player 2 choice
		{
			//Check if choice already selected
			if (player2choice == 0 || player2choice == 1 || player2choice == 2)
			{
				return false;
			}
			player2choice = choice;
			return true;
		}
	}

	// Player class to output game state data and input player actions
	class Player extends Thread {
		Socket socket;
		BufferedReader input;
		PrintWriter output;
		private int number;

		//Constructor for the player
		//Creates a read and writer to send to and receive from server
		public Player(Socket socket){
			this.socket = socket;

			try {
				input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				output = new PrintWriter(socket.getOutputStream(), true);
				output.println("WELCOME " + number);
				output.println("MESSAGE Waiting for opponent to connect");
			} catch (Exception e) {
				System.out.println("Player died: " + e);
			}
		}


		public int getNumber() {
			return this.number;
		}

		//Gives the number to the player as assigned by server
		public void setNumber(int n) {
			this.number = n;
		}

		//Runs the game
		public void run() {
			try {
				//Send to clients that player is connected
				output.println("MESSAGE Player Connected - Make your move");

				//Process client commands
				while(true) {
					String command = input.readLine();
					System.out.println(command);
					//Player selects option
					if(command.startsWith("MOVE"))
					{
						System.out.println("Received MOVE from Player" + this.getNumber());
						//Parse choice and send to legalMove to setChoice
						if (legalMove(this.getNumber(), Integer.parseInt(command.substring(5))))
						{
							System.out.println("Sending VALID_MOVE " + (this.getNumber()-1));
							output.println("VALID_MOVE " + (this.getNumber()-1));
							output.println(playRound() ? "WIN_ROUND"
									: hasWinner() ? "VICTORY"
											: tie() ? "TIE"
													: "");
							//Clear choices for new round
							
						}
						else 
						{
							output.println("MESSAGE ?");
						}
					}
					else if (command.startsWith("QUIT"))
					{
						return;
					}
				}
			}
			catch (IOException e) {
				System.out.println("Player died: " + e);
			} finally 
			{
				try 
				{ 
					socket.close();
				}
				catch (IOException e) {}
			}
		}
	}

	public boolean hasWinner() {
		if (player1Wins == 2 || player2Wins == 2 )
		{
			return true;
		}

		return false;
	}


	public boolean playRound() {
		//(WIN)Player 1 -  Rock, Player 2 - Scissors
		if (player1choice == 0 && player2choice == 2)
		{
			player1Wins++;
			return true;
		}
		//(WIN)Player 1 -  Paper, Player 2 - Rock
		else if (player1choice == 1 && player2choice == 0)
		{
			player1Wins++;
			return true;
		}
		//(WIN)Player 1 -  Scissors, Player 2 - Paper
		else if (player1choice == 2 && player2choice == 1)
		{
			player1Wins++;
			return true;
		}

		//(WIN)Player 2 -  Rock, Player 2 - Scissors
		else if (player2choice == 0 && player1choice == 2)
		{
			player2Wins++;
			return true;
		}
		//(WIN)Player 2 -  Paper, Player 2 - Rock
		else if (player2choice == 1 && player1choice == 0)
		{
			player2Wins++;
			return true;
		}
		//(WIN)Player 2 -  Scissors, Player 2 - Paper
		else if (player2choice == 2 && player1choice == 1)
		{
			player2Wins++;
			return true;
		}
		//(TIE)
		else if (player1choice == player2choice)
		{
			player1Wins++;
			player2Wins++;
		return true;
		}
		return false;
	}


	public boolean tie() {
		if (player1Wins == 2 && player2Wins == 2 ) {
			return true;
		}
		return false;
	}

}

