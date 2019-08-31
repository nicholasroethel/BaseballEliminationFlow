/* BaseballElimination.java
   CSC 226 - Spring 2019
   Assignment 3- Baseball Elimination Program
   
   This template includes some testing code to help verify the implementation.
   To interactively provide test inputs, run the program with
	java BaseballElimination
	
   To conveniently test the algorithm with a large input, create a text file
   containing one or more test divisions (in the format described below) and run
   the program with
	java -cp .;algs4.jar BaseballElimination file.txt (Windows)
   or
    java -cp .:algs4.jar BaseballElimination file.txt (Linux or Mac)
   where file.txt is replaced by the name of the text file.
   
   The input consists of an integer representing the number of teams in the division and then
   for each team, the team name (no whitespace), number of wins, number of losses, and a list
   of integers represnting the number of games remaining against each team (in order from the first
   team to the last). That is, the text file looks like:
   
	<number of teams in division>
	<team1_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>
	...
	<teamn_name wins losses games_vs_team1 games_vs_team2 ... games_vs_teamn>

	
   An input file can contain an unlimited number of divisions but all team names are unique, i.e.
   no team can be in more than one division.


   R. Little - 03/22/2019
*/

   import edu.princeton.cs.algs4.*;
   import java.util.*;
   import java.io.File;

//Do not change the name of the BaseballElimination class
   public class BaseballElimination{

	// We use an ArrayList to keep track of the eliminated teams.
   	public ArrayList<String> eliminated = new ArrayList<String>();

	/* BaseballElimination(s)
		Given an input stream connected to a collection of baseball division
		standings we determine for each division which teams have been eliminated 
		from the playoffs. For each team in each division we create a flow network
		and determine the maxflow in that network. If the maxflow exceeds the number
		of inter-divisional games between all other teams in the division, the current
		team is eliminated.
	*/
		public BaseballElimination(Scanner s){

		int size = s.nextInt();
		int rowLength = size +3;
		String[] teams = new String[size]; //gets the team names
		int[] wins = new int[size]; //gets the wins of each time
		int[] gamesLeft = new int[size]; //gets the games left for each team
		int[][] gameMatrix = new int[size][size]; //gets the games left
		boolean[] isEliminated = new boolean[size];


		for(int x = 0; x<size; x++){ //fils the array from the scanner
			for(int y = 0; y<rowLength; y++){
				if(y==0){
					teams[x] = s.next();
				}
				else if(y==1){
					wins[x] = s.nextInt();
				}
				else if(y==2){
					gamesLeft[x] = s.nextInt();
				}
				else{
					gameMatrix[x][y-3] = s.nextInt();
				}
			}

		}

	
		for(int team = 0; team<size; team++){ //builds a flownetwork for each team to see whos eliminated
			isEliminated[team] = buildFlowNetwork(size, team, gameMatrix, wins, gamesLeft);
		}

		for(int x = 0; x<size; x++){ //add the eliminated teams to the array list
			if(isEliminated[x] == true){
				eliminated.add(teams[x]);
			}
		}
	
		
		/* ... Your code here ... */	

	}

	/* main()
	   Contains code to test the BaseballElimantion function. You may modify the
	   testing code if needed, but nothing in this function will be considered
	   during marking, and the testing process used for marking will not
	   execute any of the code below.
	*/

	   public boolean buildFlowNetwork(int size, int team, int gameMatrix[][], int wins[], int gamesLeft[]){

			int matchups = ((size*size - size)/2); //the amount of matchups
			int flowVertices = matchups + size + 3; //the amount of vertices in the graph
			double inf = Double.POSITIVE_INFINITY;

			int eliminationFlow = 0; //the flow amount for the team to be eliminated

			int source = 0; 
			int sink = flowVertices-1;


			int maxwins = wins[team] + gamesLeft[team]; //max wins a team can get, before the tested team gets eliminated

			FlowNetwork G = new FlowNetwork(flowVertices);


			//building the team edges
			for(int x = 1; x<= size; x++){
				if(x - 1 == team){
					continue;
				}
				int val = maxwins - wins[x-1];
				if(val<0){ //early detection that checks if its impossible for the team to win
					return true;
				}
				FlowEdge e = new FlowEdge(x,sink,val);
				G.addEdge(e);
			}


			//building the game edges
			int count = 1;
			for(int x=0; x<size; x++){
				for(int y=x+1; y<size; y++){
					if((x == team) || (y ==team)){
						count ++;
						
					}
					else{
						FlowEdge e = new FlowEdge(source,count+size,gameMatrix[x][y]);
						eliminationFlow = eliminationFlow + gameMatrix[x][y];
						G.addEdge(e);
						e = new FlowEdge(count+size,x+1, inf);
						G.addEdge(e);
						e = new FlowEdge(count+size,y+1, inf);
						G.addEdge(e);
						count ++;
					} 
				}
			}

			FordFulkerson F = new FordFulkerson(G,source,sink); 

			if(F.value()<eliminationFlow){ //checks if the team is eliminated
				return true;
			}
			
			else{
				return false;
			}
		}

		public static void main(String[] args){
			Scanner s;
			if (args.length > 0){
				try{
					s = new Scanner(new File(args[0]));
				} catch(java.io.FileNotFoundException e){
					System.out.printf("Unable to open %s\n",args[0]);
					return;
				}
				System.out.printf("Reading input values from %s.\n",args[0]);
			}else{
				s = new Scanner(System.in);
				System.out.printf("Reading input values from stdin.\n");
			}

			BaseballElimination be = new BaseballElimination(s);		

			if (be.eliminated.size() == 0)
				System.out.println("No teams have been eliminated.");
			else
				System.out.println("Teams eliminated: " + be.eliminated);
		}
	}
