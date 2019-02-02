import java.util.*;
import java.io.*;
import java.util.concurrent.ThreadLocalRandom;



class homework {

	//ArrayList used to keep track of file contents
	static ArrayList<String> fileContents;
	static int FAIL = 10;
	static int TREE = 2;
	static int LIZARD = 1;
	static int ILLEGAL = -1;

	public static int[][] initializeSolution() {

		//initialize basic problem parameters and data structures
		String action = fileContents.get(0);
		int start = 3;
		int end = fileContents.size();
		int range = end-start;
		int dimensions = Integer.parseInt(fileContents.get(1));
		int numLizards = Integer.parseInt(fileContents.get(2));
		int[][] matrixBoard = new int[dimensions][dimensions];
		int[][] failureMatrix = new int[1][1];
		LinkedList<Node> frontier = new LinkedList<Node>();	
		int maxLizards = (int) Math.pow( (range-range/2), 2 );
		int numAvailable = range*range;

		//initialize matrix board
		char[] chArr = new char[range];
		for (int row = 0; row < range; ++row) {
			chArr = fileContents.get(row+3).toCharArray();
			for (int col = 0; col < range; ++col) {
				matrixBoard[row][col] = chArr[col] - '0';
				if (matrixBoard[row][col] == TREE) numAvailable--;  
			}
		}

		if (numLizards <= maxLizards) {
			if (numLizards < numAvailable) { 

				Node root = new Node(matrixBoard, 0);
				Node solution;

				if (action.equals("SA")) {
					//method to randomly create board (choose random numbers)
					solution = generateMatrix(root, numLizards);
					return simulatedAnnealing(solution.state);
				} else {
					if (action.equals("BFS")) frontier.offer(root);
					else if (action.equals("DFS")) frontier.offerFirst(root);

					do {

						solution = frontier.remove();

						if (solution.getDepth() == numLizards) {
							return solution.getState();
						} else {
							if (action.equals("BFS")) frontier.addAll(expand(solution));
							else if (action.equals("DFS")) frontier.addAll(0, expand(solution));
						}
					} while (!frontier.isEmpty());
				}
			}

		}
		failureMatrix[0][0] = FAIL;
		return failureMatrix;
	}

	public static Node generateMatrix(Node node, int numLizards) {

		int[][] randomMatrix = node.getState();
		int length = randomMatrix.length;
		int randomRow;
		int randomCol;

		if (numLizards > length*length) return node;
		//randomly place lizards 
		do {

			randomRow = ThreadLocalRandom.current().nextInt(0, length);
			randomCol = ThreadLocalRandom.current().nextInt(0, length);
			if (randomMatrix[randomRow][randomCol] != TREE && randomMatrix[randomRow][randomCol] != LIZARD) {
				randomMatrix[randomRow][randomCol] = LIZARD;
			} else {
				while (randomMatrix[randomRow][randomCol] > 0 && randomMatrix[randomRow][randomCol] <= TREE) {
					randomRow = ThreadLocalRandom.current().nextInt(0, length);
					randomCol = ThreadLocalRandom.current().nextInt(0, length);
				}
				randomMatrix[randomRow][randomCol] = LIZARD;
			}
			numLizards--;

		} while (numLizards > 0);

		node.state = randomMatrix;
		return node;

	}

	public static int[][] simulatedAnnealing(int[][] state) {

		double T;
		double deltaE;
		Random r;
		int length = state.length;
		int[][] snapShot = new int[length][length]; 
		int[][] prevState = state;

		r = new Random();
		//Set timer for simulated annealing
		long initTime = System.currentTimeMillis();
		//4.5 minutes
		long endTime = initTime + 270*1000;
		//set temperature and cooling rate
		T = 10000;
		double coolRate = .00004;

		//run for 4.5 minutes before breaking
		while (System.currentTimeMillis() < endTime) {

			//copy over new values
			snapShot = new int[length][length];
			for (int i = 0; i < length; ++i) {
				System.arraycopy(prevState[i], 0, snapShot[i], 0, length);
			}
			//exit
			if (numConflicts(snapShot) == 0) {
				return snapShot;
			}

			snapShot = randomWalk(snapShot);
			deltaE = numConflicts(snapShot) - numConflicts(prevState);

			if (deltaE < 0) {
				prevState = snapShot;
			}
			//decision to choose suboptimal state
			else {
				double p = Math.exp(-deltaE/T);
				double chance = ThreadLocalRandom.current().nextDouble(0, 1);
				if (chance <= p) prevState = snapShot;
			}
			//cool 
			T *= 1-coolRate;
		}
		//if no solution found, return failure matrix
		snapShot[0][0] = FAIL;
		return snapShot;

	}

	public static int numConflicts(int[][] state) {
		int conflict = 0;
		int length = state.length;
		int range;
		Set<List> visited = new HashSet<List>();
		ArrayList<Integer> coords;
		ArrayList<Integer> temp;

		for (int row = 0; row < length; ++row) {
			for (int col = 0; col < length; ++col) {
				if (state[row][col] == LIZARD) {
					coords = new ArrayList<Integer>();
					coords.add(row);
					coords.add(col);
					//add position to set 
					visited.add(coords);

					//calculate number of conflicts with specific coordinate

					//up
					range = row;
					for (int i = range-1; i >= 0; ++i) {
						if (state[i][col] == TREE) break;
						 else if (state[i][col] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(i);
							temp.add(col);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
					//down
					range = length;
					for (int i = row+1; i < length; ++i) {
						if (state[i][col] == TREE) break;
						else if (state[i][col] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(i);
							temp.add(col);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
					//left
					range = col;
					for (int i = range-1; i >= 0; --i) {
						if (state[row][i] == TREE) break;
						else if (state[row][i] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(row);
							temp.add(i);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
					//right
					range = length;
					for (int i = col+1; i < range; ++i) {
						if (state[row][i] == TREE) break;
						else if (state[row][i] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(row);
							temp.add(i);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
					//bot-left
					range = length;
					for (int i = row+1, j = col-1; j >= 0 && i < length; i++, j--) {
						if (state[i][j] == TREE) break;
						else if (state[i][j] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(i);
							temp.add(j);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
					//bot-right
					range = length;
					for (int i = row+1, j = col+1; i < length && j < length; ++i, ++j) {
						if (state[i][j] == TREE) break;
						else if (state[i][j] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(i);
							temp.add(j);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
					//top-left
					for (int i = row-1, j = col-1; i >= 0 && j >= 0; i--, j--) {
						if (state[i][j] == TREE) break;
						else if (state[i][j] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(i);
							temp.add(j);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
					//top-right
					for (int i = row-1, j = col+1; i >= 0 && j < length; --i, ++j) {
						if (state[i][j] == TREE) break;
						else if (state[i][j] == LIZARD) {
							temp = new ArrayList<Integer>();
							temp.add(i);
							temp.add(j);
							if (visited.contains(temp)) break; 
							else {
								conflict++;
								break;
							}
						}
					}
				}
			}
		}

		return conflict;
	}

	public static int[][] randomWalk(int[][] state) {

		int randRow;
		int randCol;
		int[][] randomMatrix = state;
		int length = state.length;
		int count = 0;

		int randomIndex = ThreadLocalRandom.current().nextInt(0, length);
		for (int i = 0; i < length; ++i) {
			for (int j = 0; j < length; ++j) {
				if (state[i][j] == LIZARD) {
					if (randomIndex == count) {
						//move lizard to random new location
						randRow = ThreadLocalRandom.current().nextInt(0, length);
						randCol = ThreadLocalRandom.current().nextInt(0, length);
						while (state[randRow][randCol] == LIZARD || state[randRow][randCol] == TREE) {
							randRow = ThreadLocalRandom.current().nextInt(0, length);
							randCol = ThreadLocalRandom.current().nextInt(0, length);
						}
						state[i][j] = 0;
						state[randRow][randCol] = LIZARD;

					}
					count++;
				}

			}
		}
		

		return randomMatrix;
	}

	public static Node propagateConstraint(Node node, int row, int col) {

		//generate state of board for current hypothesized node
		int[][] updatedState = node.getState();
		int length = updatedState.length;
		int range;
		
		//check up
		range = row;
		for (int i = range-1; i >= 0; --i) {
			if (updatedState[i][col] == TREE) break;
			updatedState[i][col] = ILLEGAL;
		}
		// //check down
		range = length;
		for (int i = row+1; i < range; ++i) {
			if (updatedState[i][col] == TREE) break;
			updatedState[i][col] = ILLEGAL;
		}
		// //check left
		range = col;
		for (int i = range-1; i >= 0; --i) {
			if (updatedState[row][i] == TREE) break;
			updatedState[row][i] = ILLEGAL;
		}
		// //check right
		range = length;
		for (int i = col+1; i < range; ++i) {
			if (updatedState[row][i] == TREE) break;
			updatedState[row][i] = ILLEGAL;
		}
		//check bot-left
		range = length;
		for (int i = row+1, j = col-1; j >= 0 && i < length; i++, j--) {
			if (updatedState[i][j] == TREE) break;
			updatedState[i][j] = ILLEGAL;
		}
		//check bot-right
		range = length;
		for (int i = row+1, j = col+1; i < length && j < length; ++i, ++j) {
			if (updatedState[i][j] == TREE) break;
			updatedState[i][j] = ILLEGAL;
		}
		//check top-left
		for (int i = row-1, j = col-1; i >= 0 && j >= 0; i--, j--) {
			if (updatedState[i][j] == TREE) break;
			updatedState[i][j] = ILLEGAL;
		}
		//check top-right
		for (int i = row-1, j = col+1; i >= 0 && j < length; --i, ++j) {
			if (updatedState[i][j] == TREE) break;
			updatedState[i][j] = ILLEGAL;
		}
		
		updatedState[row][col] = LIZARD;
		node.state = updatedState;

		return node;
	}

	public static LinkedList<Node> expand(Node expansionCandidate) {

		LinkedList<Node> successors = new LinkedList<Node>();
		int[][] state = expansionCandidate.getState();
		int depth = expansionCandidate.getDepth();
		int length = state.length;

		for (int row = 0; row < length; ++row) {
			for (int col = 0; col < length; ++col) {
				if (state[row][col] == 0) {
					//create state snap-shop
					int[][] snapShot = new int[length][length];
					for (int i = 0; i < length; ++i) {
						System.arraycopy(state[i], 0, snapShot[i], 0, length);
					}
					//place a node with new constraints
					Node child = propagateConstraint(new Node(snapShot, depth+1), row, col);
					//add node to frontier
					successors.offer(child);
				}
			}
		}
		return successors;
	}

	public static String formatBoard(int[][] solution) {

		String output = "OK";
		output+="\n";

		for (int i = 0; i < solution.length; ++i) {
			for (int j = 0; j < solution.length; ++j) {
				if (solution[i][j] == ILLEGAL || solution[i][j] == 0) {
					solution[i][j] = 0;
					output+="0";
				} else if (solution[i][j] == LIZARD) output+="1";
				else if (solution[i][j] == TREE) output+="2";
				if (j == solution.length-1) {
					output+="\n";
				} 
			}
		}
		return output;
	}

	public static void main(String[] args) {

		//initialize fileContents and assign values to input.txt lines
		fileContents = new ArrayList<String>();
		int[][] solutionSpace;

		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {

			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				fileContents.add(currentLine); 
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		solutionSpace = initializeSolution();
		try {
		    PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
		    if (solutionSpace[0][0] == 10) {
				writer.println("FAIL");
				System.out.println("FAIL");
			} else {
				writer.println(formatBoard(solutionSpace));
			}
			writer.close();
		} catch (IOException e) {
		   System.out.println("Error");
		}
	}
}

class Node {

	protected int[][] state;
	protected int depth;

	public Node(int[][] array, int depth) {
		this.state = array;
		this.depth = depth;
	}

	public int[][] getState() {
		return this.state;
	}

	public int getDepth() {
		return this.depth;
	}
}