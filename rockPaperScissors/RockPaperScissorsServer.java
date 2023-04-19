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
			while (true) 
			{
				Game game = new Game();
				//Connect players
				Game.Player player1 = game.new Player (listener.accept());
				
				System.out.println("Player 1 Connected");
				Game.Player player2 = game.new Player (listener.accept());
				System.out.println("Player 2 Connected");
				game.setPlayer1(player1);
				game.setPlayer2(player2);
				player1.setOpponent(player2);
				player2.setOpponent(player1);
				//Start game
				player1.start();
				player2.start();
			}

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
	Player player1;
	Player player2;

	public void newRound() {
			player1choice = -1;
			player2choice = -1;
			player1.setRoundWinner(false);
			player2.setRoundWinner(false);
	}
	
	public void setPlayer1(Player p) {
		this.player1 = p;
		p.setNumber(1);
	}
	public void setPlayer2(Player p) {
		this.player2 = p;
		p.setNumber(2);
	}
	

	//Validates move and updates the player's choice
	public synchronized boolean legalMove(int player, int choice) {
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
		Player opponent;
		BufferedReader input;
		PrintWriter output;
		private int number;
		boolean roundWinner = false;

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
		
		//Sets the players opponent to keep threads in sync
		public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }
		
		//Sets the winner of the round, for sending correct messages to each client in a game
		public void setRoundWinner (boolean w) {
			this.roundWinner = w;
		}
		
		
		
		//Method for updating player if the other player played first
		//Returns the other players writer so the server can print to it
		public PrintWriter getPrintWriter() {
			return this.output;
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
						//Verify move
						//Parse choice and send to legalMove 
						if (legalMove(this.getNumber(), Integer.parseInt(command.substring(5))))
						{
							System.out.println("Sending VALID_MOVE " + Integer.parseInt(command.substring(5)));
							
							output.println("VALID_MOVE " + Integer.parseInt(command.substring(5)));
							if (player1choice == -1 || player2choice == -1)
							opponent.getPrintWriter().println("OPPONENT_MOVED " + Integer.parseInt(command.substring(5)));
							//Attempt to play the round - will wait for other player's choice if unavailable
							if (playRound())
							{
								if (roundWinner) {
								output.println("WIN_ROUND");
								opponent.getPrintWriter().println("LOSE_ROUND");
								}
								else if (opponent.roundWinner){
									output.println("LOSE_ROUND");
									opponent.getPrintWriter().println("WIN_ROUND");
								}
								else {
									output.println("TIE");
									opponent.getPrintWriter().println("TIE");
								}
								//Clear choices for new round
								newRound();
							}
							else if (hasWinner()) 
							{
								if (roundWinner) {
									output.println("VICTORY");
									opponent.getPrintWriter().println("DEFEAT");
								}
								else {
									output.println("DEFEAT");
									opponent.getPrintWriter().println("VICTORY");
								}

							}
							else if (tie()) 
							{
								output.println("TIE");
							}
//							output.println(playRound() ? "WIN_ROUND"
//									: hasWinner() ? "VICTORY"
//											: tie() ? "TIE"
//													: "");
							
							
							
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
		if (player1Wins >= 3  )
		{
			player1.setRoundWinner(true);
			return true;
		}
		else if (player2Wins >= 3  )
		{
			player2.setRoundWinner(true);
			return true;
		}

		return false;
	}


	public boolean playRound() {
		//(WIN)Player 1 -  Rock, Player 2 - Scissors
		if (player1choice == 0 && player2choice == 2)
		{
			player1Wins++;
			player1.setRoundWinner(true);
			return true;
		}
		//(WIN)Player 1 -  Paper, Player 2 - Rock
		else if (player1choice == 1 && player2choice == 0)
		{
			player1Wins++;
			player1.setRoundWinner(true);
			return true;
		}
		//(WIN)Player 1 -  Scissors, Player 2 - Paper
		else if (player1choice == 2 && player2choice == 1)
		{
			player1Wins++;
			player1.setRoundWinner(true);
			return true;
		}

		//(WIN)Player 2 -  Rock, Player 2 - Scissors
		else if (player2choice == 0 && player1choice == 2)
		{
			player2Wins++;
			player2.setRoundWinner(true);
			return true;
		}
		//(WIN)Player 2 -  Paper, Player 2 - Rock
		else if (player2choice == 1 && player1choice == 0)
		{
			player2Wins++;
			player2.setRoundWinner(true);
			return true;
		}
		//(WIN)Player 2 -  Scissors, Player 2 - Paper
		else if (player2choice == 2 && player1choice == 1)
		{
			player2Wins++;
			player2.setRoundWinner(true);
			return true;
		}
		//(TIE)
		else if (player1choice == player2choice)
		{
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

