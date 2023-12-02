package rps;
import java.util.Random;
import java.lang.Thread;
public class RPSsinglelist {

//The rules of the game are simple
//Each team's player must run towards their target, and away from their enemy.
//If a creature is within one unit of its target, the target joins the creature's team
//last team standing wins
//Creatures can move one unit per turn omnidirectionally, but can not go out of bounds
	
	public static void printBoard(String [][] gameBoard, double[][] creatureList) {
		//print game
		int gameSize = gameBoard.length;
		//blank every square
		for (int x = 0; x < gameSize; x++) {
			for (int y = 0; y < gameSize; y++) {
				gameBoard[y][x] = " .";
			}
		}
		//add creatures
		for (int x = 0; x < creatureList.length; x++) {
			if(creatureList[x][2] == 0) {
				gameBoard[(int) creatureList[x][0]][(int) creatureList[x][1]] = " R";
			}
			else if(creatureList[x][2] == 1) {
				gameBoard[(int) creatureList[x][0]][(int) creatureList[x][1]] = " P";
			}
			else if(creatureList[x][2] == 2) {
				gameBoard[(int) creatureList[x][0]][(int) creatureList[x][1]] = " S";
			}
			else {
				System.out.println("Fatal creature error: creature " + x + " = " + creatureList[x]);
				break;
			}
		}
		
		//print to screen
		for (int x = 0; x < gameSize; x++) {
			for (int y = 0; y < gameSize; y++) {
				System.out.print(gameBoard[y][x]);
			}
			System.out.println();
		} 
		
		//print a bar at the end
		for (int y = 0; y < gameSize; y++) {
			System.out.print("==");
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		
		//Initializing values
		Random rand = new Random();
		Thread thread = new Thread();
		final int GAMESIZE = 20;
		final int SQUARESPERCREATURE = 9;
		
		//total squares on the grid are calculated, then divided into chunks. Each creature is given on average that many chunks of space.
		int creatureNum = (int)(GAMESIZE*GAMESIZE)/SQUARESPERCREATURE;
		
		//ensure an equal number of creatures per team by making the number divisible by 3
		creatureNum = ((int)creatureNum/3)*3;
		System.out.println(creatureNum + " creatures");
		
		//Creating the physical board
		String[][] gameBoard = new String[GAMESIZE][GAMESIZE];
		
		//Loading the board with blank squares
		for(int x = 0; x < GAMESIZE; x++) {
			for(int y = 0; y < GAMESIZE; y++) {
				gameBoard[y][x] = " .";
			}
		}
			
		//create arraylist representing every creature's team and location
		//0 = x
		//1 = y
		//2 = team (0 = r, 1 = p, 2 = s)
		
		double[][] creatureList = new double[creatureNum][3];

		
		//fill team lists with random double coordinates representing creature coords
		int team = 0;
		for (int x = 0; x < creatureNum; x++) {
			creatureList[x][0] = rand.nextDouble(GAMESIZE);
			creatureList[x][1] = rand.nextDouble(GAMESIZE);
			creatureList[x][2] = team;
			team++;
			if (team == 3) {
				team = 0;
			}
		}
		
		printBoard(gameBoard, creatureList);
		
		//System.out.println("entering timer");
		//timer for how many turns the game should run
		for (int i = 0; i < 1000; i++) {
			//System.out.println("entering list");

			//move each player
			//Start with 1st rock, 1st paper, 1st scissor, then 2nd rock and so on to maintain fairness
			for (int x = 0; x < creatureNum; x++) {
				if(creatureList[x][2] == 0) {
					//System.out.println("\nRock @ " + creatureList[x][0] + ", " + creatureList[x][1]);

					//closer distances are overridden, so initialize with large value
					int attackNumber = 0;
					double attackDistance = Math.pow(GAMESIZE, 2);
					int fleeNumber = 0;
					double fleeDistance = Math.pow(GAMESIZE, 2);

					//test distance of each attack type
					for(int y = 0; y < creatureList.length; y++) {
						if(creatureList[y][2] == 2) {
							double currentAttack = Math.sqrt(Math.pow(creatureList[y][0] - creatureList[x][0], 2)
									+ Math.pow(creatureList[y][1] - creatureList[x][1], 2));
							if (currentAttack < attackDistance) {
								attackNumber = y;
								attackDistance = currentAttack;
							}
						}
					}

					//test distance of each flee type
					for(int y = 0; y < creatureList.length; y++) {
						if(creatureList[y][2] == 1) {
							double currentFlee = Math.sqrt(Math.pow(creatureList[y][0] - creatureList[x][0], 2)
								+ Math.pow(creatureList[y][1] - creatureList[x][1], 2));
							if (currentFlee < fleeDistance) {
								fleeNumber = y;
								fleeDistance = currentFlee;
							}
						}
					}
					
					//System.out.println("flee = " + fleeDistance + ". attack = " + attackDistance);

					//split into 4 behaviors: an attack, a flee, a kill, and a death
					//System.out.println("Rock is " + attackDistance + " from scissor, and " + fleeDistance + " from paper");
					if (attackDistance <= fleeDistance) {
						//System.out.println("Rock attacks");
						if (attackDistance < 1) {
							//System.out.println("Rock wins!");
							creatureList[attackNumber][2] = 0;
						} else {
							double slopeX = creatureList[attackNumber][0] - creatureList[x][0];
							double slopeY = creatureList[attackNumber][1] - creatureList[x][1];
							double distanceMult = Math.sqrt(1 / (Math.pow(slopeY, 2) + Math.pow(slopeX, 2)));
							
							creatureList[x][0] = (distanceMult * slopeX) + creatureList[x][0];
							creatureList[x][1] = (distanceMult * slopeY) + creatureList[x][1];

							if (creatureList[x][0] < 0) {
								creatureList[x][0] = 0;
							}
							if (creatureList[x][0] > GAMESIZE - 1) {
								creatureList[x][0] = GAMESIZE - 1;
							}
							if (creatureList[x][1] < 0) {
								creatureList[x][1] = 0;
							}
							if (creatureList[x][1] > GAMESIZE - 1) {
								creatureList[x][1] = GAMESIZE - 1;
							}
							//System.out.println("Rock @ " + creatureList[x][0] + ", " + creatureList[x][1]);
		
						}
					} else {
						//System.out.println("Rock flees");
						if (fleeDistance < 1) {
							//System.out.println("Rock loses!");
							creatureList[x][2] = 1;
						} else {
							double slopeX = creatureList[fleeNumber][0] - creatureList[x][0];
							double slopeY = creatureList[fleeNumber][1] - creatureList[x][1];
							//System.out.println("slope x & y = " + slopeX + " " + slopeY);
							double distanceMult = Math.sqrt(1 / (Math.pow(slopeY, 2) + Math.pow(slopeX, 2)));
							//System.out.println("distanceMult = " + distanceMult);
							
							creatureList[x][0] = creatureList[x][0] - (distanceMult * slopeX);
							creatureList[x][1] = creatureList[x][1] - (distanceMult * slopeY);
							//System.out.println("new creature coords pree fix = " + creatureList[x][0] + " " + creatureList[x][1]);

							if (creatureList[x][0] < 0) {
								creatureList[x][0] = 0;
							}
							if (creatureList[x][0] > GAMESIZE - 1) {
								creatureList[x][0] = GAMESIZE - 1;
							}
							if (creatureList[x][1] < 0) {
								creatureList[x][1] = 0;
							}
							if (creatureList[x][1] > GAMESIZE - 1) {
								creatureList[x][1] = GAMESIZE - 1;
							}
							//System.out.println("Rock @ " + creatureList[x][0] + ", " + creatureList[x][1]);
						}
					}
				}
				
				
				//test for paper player num
				if(creatureList[x][2] == 1) {
					
					//System.out.println("\nPaper @ " + creatureList[x][0] + ", " + creatureList[x][1]);

					//closer distances are overridden, so initialize with large value
					int attackNumber = 0;
					double attackDistance = Math.pow(GAMESIZE, 2);
					int fleeNumber = 0;
					double fleeDistance = Math.pow(GAMESIZE, 2);

					//test distance of each attack type
					for(int y = 0; y < creatureList.length; y++) {
						if(creatureList[y][2] == 0) {
							double currentAttack = Math.sqrt(Math.pow(creatureList[y][0] - creatureList[x][0], 2)
									+ Math.pow(creatureList[y][1] - creatureList[x][1], 2));
							if (currentAttack < attackDistance) {
								attackNumber = y;
								attackDistance = currentAttack;
							}
						}
					}

					//test distance of each flee type
					for(int y = 0; y < creatureList.length; y++) {
						if(creatureList[y][2] == 2) {
							double currentFlee = Math.sqrt(Math.pow(creatureList[y][0] - creatureList[x][0], 2)
								+ Math.pow(creatureList[y][1] - creatureList[x][1], 2));
							if (currentFlee < fleeDistance) {
								fleeNumber = y;
								fleeDistance = currentFlee;
							}
						}
					}
					
					//System.out.println("flee = " + fleeDistance + ". attack = " + attackDistance);

					//split into 4 behaviors: an attack, a flee, a kill, and a death
					//System.out.println("Rock is " + attackDistance + " from scissor, and " + fleeDistance + " from paper");
					if (attackDistance <= fleeDistance) {
						//System.out.println("Paper attacks");
						if (attackDistance < 1) {
							//System.out.println("Paper wins!");
							creatureList[attackNumber][2] = 1;

						} else {
							double slopeX = creatureList[attackNumber][0] - creatureList[x][0];
							double slopeY = creatureList[attackNumber][1] - creatureList[x][1];
							double distanceMult = Math.sqrt(1 / (Math.pow(slopeY, 2) + Math.pow(slopeX, 2)));
							
							creatureList[x][0] = (distanceMult * slopeX) + creatureList[x][0];
							creatureList[x][1] = (distanceMult * slopeY) + creatureList[x][1];

							if (creatureList[x][0] < 0) {
								creatureList[x][0] = 0;
							}
							if (creatureList[x][0] > GAMESIZE - 1) {
								creatureList[x][0] = GAMESIZE - 1;
							}
							if (creatureList[x][1] < 0) {
								creatureList[x][1] = 0;
							}
							if (creatureList[x][1] > GAMESIZE - 1) {
								creatureList[x][1] = GAMESIZE - 1;
							}
							//System.out.println("Paper @ " + creatureList[x][0] + ", " + creatureList[x][1]);
		
						}
					} else {
						//System.out.println("Paper flees");
						if (fleeDistance < 1) {
							//System.out.println("Paper loses!");
							creatureList[x][2] = 2;

						} else {
							double slopeX = creatureList[fleeNumber][0] - creatureList[x][0];
							double slopeY = creatureList[fleeNumber][1] - creatureList[x][1];
							double distanceMult = Math.sqrt(1 / (Math.pow(slopeY, 2) + Math.pow(slopeX, 2)));
							
							creatureList[x][0] = creatureList[x][0] - (distanceMult * slopeX);
							creatureList[x][1] = creatureList[x][1] - (distanceMult * slopeY);

							if (creatureList[x][0] < 0) {
								creatureList[x][0] = 0;
							}
							if (creatureList[x][0] > GAMESIZE - 1) {
								creatureList[x][0] = GAMESIZE - 1;
							}
							if (creatureList[x][1] < 0) {
								creatureList[x][1] = 0;
							}
							if (creatureList[x][1] > GAMESIZE - 1) {
								creatureList[x][1] = GAMESIZE - 1;
							}
							//System.out.println("Paper @ " + creatureList[x][0] + ", " + creatureList[x][1]);
						}
					}
				}
				else if(creatureList[x][2] == 2) {
					
					//System.out.println("\nScissors @ " + creatureList[x][0] + ", " + creatureList[x][1]);

					//closer distances are overridden, so initialize with large value
					int attackNumber = 0;
					double attackDistance = Math.pow(GAMESIZE, 2);
					int fleeNumber = 0;
					double fleeDistance = Math.pow(GAMESIZE, 2);

					//test distance of each attack type
					for(int y = 0; y < creatureList.length; y++) {
						if(creatureList[y][2] == 1) {
							double currentAttack = Math.sqrt(Math.pow(creatureList[y][0] - creatureList[x][0], 2)
									+ Math.pow(creatureList[y][1] - creatureList[x][1], 2));
							if (currentAttack < attackDistance) {
								attackNumber = y;
								attackDistance = currentAttack;
							}
						}
					}

					//test distance of each flee type
					for(int y = 0; y < creatureList.length; y++) {
						if(creatureList[y][2] == 0) {
							double currentFlee = Math.sqrt(Math.pow(creatureList[y][0] - creatureList[x][0], 2)
								+ Math.pow(creatureList[y][1] - creatureList[x][1], 2));
							if (currentFlee < fleeDistance) {
								fleeNumber = y;
								fleeDistance = currentFlee;
							}
						}
					}
					
					//System.out.println("flee = " + fleeDistance + ". attack = " + attackDistance);

					//split into 4 behaviors: an attack, a flee, a kill, and a death
					//System.out.println("Rock is " + attackDistance + " from scissor, and " + fleeDistance + " from paper");
					if (attackDistance <= fleeDistance) {
						//System.out.println("Scissors attacks");
						if (attackDistance < 1) {
							//System.out.println("Scissors wins!");
							creatureList[attackNumber][2] = 2;

						} else {
							double slopeX = creatureList[attackNumber][0] - creatureList[x][0];
							double slopeY = creatureList[attackNumber][1] - creatureList[x][1];
							double distanceMult = Math.sqrt(1 / (Math.pow(slopeY, 2) + Math.pow(slopeX, 2)));
							
							creatureList[x][0] = (distanceMult * slopeX) + creatureList[x][0];
							creatureList[x][1] = (distanceMult * slopeY) + creatureList[x][1];

							if (creatureList[x][0] < 0) {
								creatureList[x][0] = 0;
							}
							if (creatureList[x][0] > GAMESIZE - 1) {
								creatureList[x][0] = GAMESIZE - 1;
							}
							if (creatureList[x][1] < 0) {
								creatureList[x][1] = 0;
							}
							if (creatureList[x][1] > GAMESIZE - 1) {
								creatureList[x][1] = GAMESIZE - 1;
							}
							//System.out.println("Scissors @ " + creatureList[x][0] + ", " + creatureList[x][1]);
		
						}
					} else {
						//System.out.println("Scissors flees");
						if (fleeDistance < 1) {
							//System.out.println("Scissors loses!");
							creatureList[x][2] = 0;

						} else {
							double slopeX = creatureList[fleeNumber][0] - creatureList[x][0];
							double slopeY = creatureList[fleeNumber][1] - creatureList[x][1];
							double distanceMult = Math.sqrt(1 / (Math.pow(slopeY, 2) + Math.pow(slopeX, 2)));
							
							creatureList[x][0] = creatureList[x][0] - (distanceMult * slopeX);
							creatureList[x][1] = creatureList[x][1] - (distanceMult * slopeY);

							if (creatureList[x][0] < 0) {
								creatureList[x][0] = 0;
							}
							if (creatureList[x][0] > GAMESIZE - 1) {
								creatureList[x][0] = GAMESIZE - 1;
							}
							if (creatureList[x][1] < 0) {
								creatureList[x][1] = 0;
							}
							if (creatureList[x][1] > GAMESIZE - 1) {
								creatureList[x][1] = GAMESIZE - 1;
							}
							//System.out.println("Scissors @ " + creatureList[x][0] + ", " + creatureList[x][1]);
						}
					}
				}
			}
			
			printBoard(gameBoard, creatureList);
			
			
			try {
	            // Sleep for .1 seconds (100 milliseconds)
	            Thread.sleep(1000/10);
	        } catch (InterruptedException e) {
	            // Handle the interrupted exception (if needed)
	            e.printStackTrace();
	        }
	        
	        
		}
	}
}
