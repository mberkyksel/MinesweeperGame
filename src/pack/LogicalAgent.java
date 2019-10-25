package pack;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

public class LogicalAgent {	
	MineSweeper mineSweeper;
	ArrayList<Square> checkList;
	

	ArrayList<Coordinate> partialInformationSquaresList;
	ArrayList<Coordinate> allRemainingSquares;

	Stack<Coordinate> calculatedMineSquaresGoingToBeFlagged;
	Stack<Coordinate> calculatedSafeSquaresGoingToBeClicked;

	
	public LogicalAgent(MineSweeper mineSw) {
		mineSweeper = mineSw;
		checkList = new ArrayList<Square>();
		
		partialInformationSquaresList = new ArrayList<Coordinate>();
		allRemainingSquares = new ArrayList<Coordinate>();
		
		calculatedMineSquaresGoingToBeFlagged = new Stack<Coordinate>();
		calculatedSafeSquaresGoingToBeClicked = new Stack<Coordinate>();

	}

	public int calculateRemainingSquaresCount() {
		int count = 0;
		for (int i = 0; i < mineSweeper.mHeight; i++) {
			for (int j = 0; j < mineSweeper.mWidth; j++) {
				if (mineSweeper.gameField[i][j] == ' ')
					count++;
			}
		}
		return count;
	}

	public void play() {

		Random random = new Random(); // this generates random numbers for us

		int x = random.nextInt(mineSweeper.mWidth); // a number between 0 and
													// mWidth - 1
		int y = random.nextInt(mineSweeper.mHeight);
		// make sure we don't place a mine on top of another
		clickSquareOnField(x, y);
		System.out.println("Clicking a random coordinate: " + x + ", " + y);
		mineSweeper.drawGamefield();
		createCheckList();
		System.out.println("Finding guaranteed mines and flagging them...");
		int placedFlagCount = findGuaranteedMinesAndFlagGameField();

		if (placedFlagCount > 0) {
			System.out.println(placedFlagCount + " flags Placed on game field");
			mineSweeper.drawGamefield();
			placedFlagCount = 0;
		} else {
			System.out.println("No guaranteed mines found, no flags placed.");
		}
		clearCheckList();

		outerGameLoop: while (mineSweeper.gameFinished() == false) {
			System.out.println("----------------------------------------------------------------");

			while (ifThereIsSafeSquareClickIt()) {
				if (mineSweeper.gameFinished()) {
					break outerGameLoop;
				}

				mineSweeper.drawGamefield();

				System.out.println("Finding guaranteed mines and flagging them...");
				placedFlagCount = findGuaranteedMinesAndFlagGameField();

				if (mineSweeper.gameFinished()) {
					break outerGameLoop;
				}

				if (placedFlagCount > 0) {
					System.out.println(placedFlagCount + " flags placed on game field");
					mineSweeper.drawGamefield();
				} else {
					System.out.println("No guaranteed mines found, no flags placed.");
				}
			}

			System.out.println("Finding guaranteed mines and flagging them...");
			placedFlagCount = findGuaranteedMinesAndFlagGameField();

			if (mineSweeper.gameFinished()) {
				break outerGameLoop;
			}

			if (placedFlagCount > 0) {
				System.out.println(placedFlagCount + " flags placed on game field");
				mineSweeper.drawGamefield();
				continue;
			} else {
				System.out.println("No guaranteed mines found, no flags placed.");
			}

			makeListOfPartialInfoSquares();
			ArrayList<Configuration> feasibleConfs = findFeasibleConfigurations(
					calculateAllPossibles(partialInformationSquaresList));
			boolean anySimilarityFound = findSimilaritiesInFeasibles(feasibleConfs);
			System.out.println("Looking for multi squares together. Lets see if I can find anything...");

			if (anySimilarityFound) {
				System.out.println("Found some information by looking multi squares together...");
				// Flags from Multi Algo
				int fromStackFlags = findGuaranteedMinesFromStackAndFlagGameField();
				if (fromStackFlags > 0) {
					System.out.println(fromStackFlags + " flags calculated from multi algorithm and placed.");
					mineSweeper.drawGamefield();
				} else {
					System.out.println("No flags calculated from multi algorithm.");
				} //

				// Safe squares from multi algo
				while (calculatedSafeSquaresGoingToBeClicked.size() > 0) {
					Coordinate c = calculatedSafeSquaresGoingToBeClicked.pop();
					System.out.println("Safe square " + c.getX() + ", " + c.getY()
							+ " is calculated from multi algorithm and I am clicking on it...");
					clickSquareOnField(c.getX(), c.getY());
					mineSweeper.drawGamefield();

				} // Safe squares from multi algo

				// Check flags again if there are any (guaranteed)
				placedFlagCount = findGuaranteedMinesAndFlagGameField();
				if (placedFlagCount > 0) {
					System.out.println(placedFlagCount + " flags placed on game field");
					mineSweeper.drawGamefield();
				} else {
					System.out.println("No guaranteed mines found, no flags placed.");
				} //

			} else { // Probabilistic clicking, no other chances left
				System.out
						.println("There is no guaranteed mines or safe places left. I am calculating probabilities...");

				if (calculateRemainingSquaresCount() < 12) {
					makeListOfAllRemainingSquares();
					ArrayList<Configuration> feasibleConfsSmallArea = findFeasibleConfigurations(
							calculateAllPossibles(allRemainingSquares));

					CoordinateWithProbability[] coordinatesWithProbabilities = calculateProbabilities(
							feasibleConfsSmallArea);

					System.out.println("Here are the probabilities");
					for (int i = 0; i < coordinatesWithProbabilities.length; i++) {
						System.out.println("Coordinate: " + coordinatesWithProbabilities[i].getX() + ", "
								+ coordinatesWithProbabilities[i].getY() + ": "
								+ coordinatesWithProbabilities[i].getProbability());
					}

					// Finding the box with the lowest chance of containing a
					// mine
					CoordinateWithProbability min = null;
					for (int i = 0; i < coordinatesWithProbabilities.length; i++) {
						if (i == 0) {
							min = coordinatesWithProbabilities[0];
						} else {
							if (coordinatesWithProbabilities[i].getProbability() < min.getProbability()) {
								min = coordinatesWithProbabilities[i];
							}
						}
					}

					System.out.println(min.getX() + ", " + min.getY()
							+ " has the lowest or one of the lowest probability. I am clicking on it...");
					clickSquareOnField(min.getX(), min.getY());
					mineSweeper.drawGamefield();
				} else {
					CoordinateWithProbability[] coordinatesWithProbabilities = calculateProbabilities(feasibleConfs);

					System.out.println("Here are the probabilities");
					for (int i = 0; i < coordinatesWithProbabilities.length; i++) {
						System.out.println("Coordinate: " + coordinatesWithProbabilities[i].getX() + ", "
								+ coordinatesWithProbabilities[i].getY() + ": "
								+ coordinatesWithProbabilities[i].getProbability());
					}

					// Finding the box with the lowest chance of containing a
					// mine
					CoordinateWithProbability min = null;
					for (int i = 0; i < coordinatesWithProbabilities.length; i++) {
						if (i == 0) {
							min = coordinatesWithProbabilities[0];
						} else {
							if (coordinatesWithProbabilities[i].getProbability() < min.getProbability()) {
								min = coordinatesWithProbabilities[i];
							}
						}
					}

					System.out.println(min.getX() + ", " + min.getY()
							+ " has the lowest or one of the lowest probability. I am clicking on it...");
					clickSquareOnField(min.getX(), min.getY());
					mineSweeper.drawGamefield();
				}

			}

		}
		mineSweeper.drawMinefield();
		System.exit(0);

	}

	public CoordinateWithProbability[] calculateProbabilities(ArrayList<Configuration> feasibleConfs) {
		CoordinateWithProbability[] coordinatesWithProbabilities;
		if (feasibleConfs.size() > 0) {
			coordinatesWithProbabilities = new CoordinateWithProbability[feasibleConfs.get(0).getBooleanMines()
					.length()];
		} else {
			return null;
		}

		for (Configuration c : feasibleConfs) {
			for (int i = 0; i < c.getBooleanMines().length(); i++) {
				if (coordinatesWithProbabilities[i] == null) {
					coordinatesWithProbabilities[i] = new CoordinateWithProbability(0, c.getCoordinates()[i].getX(),
							c.getCoordinates()[i].getY());
				}
				if (c.getBooleanMines().charAt(i) == '1') {
					int probability = coordinatesWithProbabilities[i].getProbability();
					probability++;
					coordinatesWithProbabilities[i].setProbability(probability);
				}
			}
		}

		return coordinatesWithProbabilities;

	}

	public int findGuaranteedMinesFromStackAndFlagGameField() {
		int flagCount = calculatedMineSquaresGoingToBeFlagged.size();
		if (calculatedMineSquaresGoingToBeFlagged.size() > 0) {
			Coordinate c = calculatedMineSquaresGoingToBeFlagged.pop();
			mineSweeper.gameField[c.getX()][c.getY()] = 'F';
		}
		return flagCount;
	}

	public boolean findSimilaritiesInFeasibles(ArrayList<Configuration> feasibleConfs) {
		String minesInAllConfs = "";
		String safeInAllConfs = "";
		boolean anySimilarityFound = false;

		for (int i = 0; i < feasibleConfs.get(0).booleanMines.length(); i++) {
			char tempAnd = '1';
			char tempOr = '0';

			for (Configuration c : feasibleConfs) {
				tempAnd = (char) (tempAnd & c.booleanMines.charAt(i));
				tempOr = (char) (tempOr | c.booleanMines.charAt(i));
			}
			minesInAllConfs = minesInAllConfs + tempAnd;
			safeInAllConfs = safeInAllConfs + tempOr;
			// System.out.println("MINES " + minesInAllConfs);
			// System.out.println("SAFES " + safeInAllConfs);

		}

		/*
		 * Example: minesInAllConfs 0000000 > 0 means no mine, 1 means mine
		 * safeInAllConfs 1111011 > 0 means safe, 1 means not safe
		 */

		// Calculate Mine Coordinates
		for (int i = 0; i < minesInAllConfs.length(); i++) {
			if (minesInAllConfs.charAt(i) == '1') {
				if (feasibleConfs.get(0).getCoordinates().length > 0) {
					Coordinate c = feasibleConfs.get(0).getCoordinates()[i];
					calculatedMineSquaresGoingToBeFlagged.push(c);
					anySimilarityFound = true;
				}
			}
		}

		for (int i = 0; i < safeInAllConfs.length(); i++) {
			if (safeInAllConfs.charAt(i) == '0') {
				if (feasibleConfs.get(0).getCoordinates().length > 0) {
					Coordinate c = feasibleConfs.get(0).getCoordinates()[i];
					calculatedSafeSquaresGoingToBeClicked.push(c);
					anySimilarityFound = true;
				}

			}
		}

		return anySimilarityFound;

	}

	public ArrayList<Configuration> findFeasibleConfigurations(ArrayList<Configuration> allConfigurations) {

		ArrayList<Configuration> feasibleConfs = new ArrayList<Configuration>();

		for (int i = 0; i < allConfigurations.size(); i++) {
			if (isConfigurationValid(allConfigurations.get(i))) {
				feasibleConfs.add(allConfigurations.get(i));
			}

		}
		return feasibleConfs;
	}

	public int minesLeft() {
		int mineCountInField = 0;
		for (int i = 0; i < mineSweeper.gameField.length; i++) {
			for (int j = 0; j < mineSweeper.gameField.length; j++) {
				if (mineSweeper.gameField[i][j] == 'F') {
					mineCountInField++;
				}
			}
		}
		return mineSweeper.mMines - mineCountInField;
	}

	private boolean isConfigurationValid(Configuration c) {
		clearCheckList();
		createCheckList();

		// Count mines in config, in config mine count is higher than mines
		// left, config is invalid. No need to make further calculations
		int mineCountInConfig = 0;
		for (int i = 0; i < c.booleanMines.length(); i++) {
			String booleanMinesString = c.booleanMines;
			if (booleanMinesString.charAt(i) == '1') {
				mineCountInConfig++;
			}
		}
		if (mineCountInConfig > minesLeft()) {
			return false;
		}

		for (Square square : checkList) {

			int xLower = -1;
			int xUpper = 1;
			int yLower = -1;
			int yUpper = 1;

			if (square.getX() == 0)
				xLower = 0;
			if (square.getX() == mineSweeper.mHeight - 1)
				xUpper = 0;
			if (square.getY() == 0)
				yLower = 0;
			if (square.getY() == mineSweeper.mWidth - 1)
				yUpper = 0;

			int realMineCount = Integer.parseInt(square.getValue() + "");

			int configurationMineCount = 0;

			boolean[] mineCounted = new boolean[c.booleanMines.length()];

			for (int i = xLower; i <= xUpper; i++) {
				for (int j = yLower; j <= yUpper; j++) {
					if (i == 0 && j == 0) {
						continue;
					} else {
						if (mineSweeper.gameField[square.getX() + i][square.getY() + j] == 'F') {
							realMineCount--;
						}

						for (int unrevealedSquareIndex = 0; unrevealedSquareIndex < c.coordinates.length; unrevealedSquareIndex++) {
							if ((c.coordinates[unrevealedSquareIndex].getX() == square.getX() + i)
									&& (c.coordinates[unrevealedSquareIndex].getY() == square.getY() + j)) {

								if (c.booleanMines.charAt(unrevealedSquareIndex) == '1'
										&& mineCounted[unrevealedSquareIndex] == false) {
									configurationMineCount++;
									mineCounted[unrevealedSquareIndex] = true;
								}
							}
						}
					}
				}
			}

			if (realMineCount != configurationMineCount) {
				return false;
			}
		}
		return true;

	}

	public ArrayList<Configuration> calculateAllPossibles(ArrayList<Coordinate> unrevealedList) {

		/*
		 * unrevealedList = 2 2*2=4 00 01 10 11
		 * 
		 */

		//String of mine states
		String[] booleanStrings;

		booleanStrings = new String[(int) Math.pow(2, unrevealedList.size())];

		for (int i = 0; i < Math.pow(2, unrevealedList.size()); i++) {

			String s = Integer.toBinaryString(i);

			if (s.length() < unrevealedList.size()) {
				while (s.length() < unrevealedList.size()) {
					s = "0" + s;
				}
			}
			booleanStrings[i] = s;
		}

		
		Coordinate[] coordinates = new Coordinate[unrevealedList.size()];

		for (int i = 0; i < unrevealedList.size(); i++) {
			coordinates[i] = new Coordinate(unrevealedList.get(i).getX(), unrevealedList.get(i).getY());
		}

		ArrayList<Configuration> allConfigurations = new ArrayList<Configuration>();

		for (int i = 0; i < Math.pow(2, unrevealedList.size()); i++) {

			Configuration c = new Configuration(true, booleanStrings[i], coordinates);
			allConfigurations.add(c);
		}
		return allConfigurations;
	}

	public void makeListOfPartialInfoSquares() {
		clearPartialInfoSquareList();

		for (int i = 0; i < mineSweeper.mHeight; i++) {
			for (int j = 0; j < mineSweeper.mWidth; j++) {
				if ((mineSweeper.gameField[i][j] == ' ') && (hasNumberNeighbour(new Coordinate(i, j))))
					partialInformationSquaresList.add(new Coordinate(i, j));
			}
		}

	}

	public void makeListOfAllRemainingSquares() {
		clearAllRemainingSquares();

		for (int i = 0; i < mineSweeper.mHeight; i++) {
			for (int j = 0; j < mineSweeper.mWidth; j++) {
				if ((mineSweeper.gameField[i][j] == ' '))
					allRemainingSquares.add(new Coordinate(i, j));
			}
		}

	}

	public int findGuaranteedMinesAndFlagGameField() {
		clearCheckList();
		createCheckList();
		int placedFlagCount = 0;

		for (Square square : checkList) {

			int xLower = -1;
			int xUpper = 1;
			int yLower = -1;
			int yUpper = 1;

			if (square.getX() == 0)
				xLower = 0;
			if (square.getX() == mineSweeper.mHeight - 1)
				xUpper = 0;
			if (square.getY() == 0)
				yLower = 0;
			if (square.getY() == mineSweeper.mWidth - 1)
				yUpper = 0;

			int mineCount = Integer.parseInt(square.getValue() + "");

			ArrayList<Coordinate> c = new ArrayList<Coordinate>();

			for (int i = xLower; i <= xUpper; i++) {
				for (int j = yLower; j <= yUpper; j++) {
					if (i == 0 && j == 0) {
						continue;
					} else {
						if (mineSweeper.gameField[square.getX() + i][square.getY() + j] == 'F') {
							mineCount--;
						} else if (mineSweeper.gameField[square.getX() + i][square.getY() + j] == ' ') {
							c.add(new Coordinate(square.getX() + i, square.getY() + j));
						}
					}
				}
			}
			if (mineCount == c.size()) {
				for (int i = 0; i < c.size(); i++) {
					mineSweeper.gameField[c.get(i).getX()][c.get(i).getY()] = 'F';
					placedFlagCount++;
				}
			}
		}
		return placedFlagCount;

	}

	public void clickSquareOnField(int x, int y) {
		mineSweeper.revealSquaresOnClickedArea(x, y);
	}

	public void createCheckList() {
		for (int i = 0; i < mineSweeper.mHeight; i++) {
			for (int j = 0; j < mineSweeper.mWidth; j++) {
				if (isNumber(mineSweeper.gameField[i][j]) && hasUnrevealedNeighbour(new Coordinate(i, j))) {// Say�ysa
																											// ve
																											// bos
																											// komsusu
																											// varsa
																											// listeye
																											// ekle.
					checkList.add(createSquare(mineSweeper.gameField[i][j], i, j));
				}
			}
		}

	}

	public void clearCheckList() {
		checkList.clear();
	}

	public void clearPartialInfoSquareList() {
		partialInformationSquaresList.clear();
	}

	public void clearAllRemainingSquares() {
		allRemainingSquares.clear();
	}

	public boolean ifThereIsSafeSquareClickIt() {
		clearCheckList();
		createCheckList();

		Coordinate c = findSafeSquare();
		if (c.getX() == -1) {
			System.out.println("No safe square left.");
			return false;
		}

		else {
			System.out.println("Safe Square found: " + c.getX() + ", " + c.getY() + ". Clicking on it...");
			clickSquareOnField(c.getX(), c.getY());
			return true;
		}
	}

	private Coordinate findSafeSquare() {
		Coordinate safeSquareCoordinate = new Coordinate(-1, -1);

		for (Square square : checkList) {

			int xLower = -1;
			int xUpper = 1;
			int yLower = -1;
			int yUpper = 1;

			if (square.getX() == 0)
				xLower = 0;
			if (square.getX() == mineSweeper.mHeight - 1)
				xUpper = 0;
			if (square.getY() == 0)
				yLower = 0;
			if (square.getY() == mineSweeper.mWidth - 1)
				yUpper = 0;

			int mineCount = Integer.parseInt(square.getValue() + "");

			int flagCount = 0;

			ArrayList<Coordinate> c = new ArrayList<Coordinate>();

			for (int i = xLower; i <= xUpper; i++) {
				for (int j = yLower; j <= yUpper; j++) {
					if (i == 0 && j == 0) {
						continue;
					} else {
						if (mineSweeper.gameField[square.getX() + i][square.getY() + j] == 'F') {
							flagCount++;
						} else if ((mineSweeper.gameField[square.getX() + i][square.getY() + j] == ' ')) {
							c.add(new Coordinate(square.getX() + i, square.getY() + j));
						}

					}

				}
			}
			if (mineCount == flagCount) {
				if (c.size() > 0) {
					safeSquareCoordinate = c.get(0);
					return safeSquareCoordinate;
				}
			}

		}

		return safeSquareCoordinate;
	}

	private Square createSquare(char v, int x, int y) {
		Square s = new Square(v, x, y);

		return s;
	}

	private boolean isNumber(char c) {
		if (c == '-')
			return false;
		else if (c == ' ')
			return false;
		else if (c == 'F')
			return false;

		return true;
	}

	

	/**
	 * @param c
	 * @return true if the given coordinate C has neighbors that are not revealed yet, false otherwise. 
	 */
	private boolean hasUnrevealedNeighbour(Coordinate c) {
		int x = c.getX();
		int y = c.getY();

		int xLower = -1;
		int xUpper = 1;
		int yLower = -1;
		int yUpper = 1;

		if (x == 0)
			xLower = 0;
		if (x == mineSweeper.mHeight - 1)
			xUpper = 0;
		if (y == 0)
			yLower = 0;
		if (y == mineSweeper.mWidth - 1)
			yUpper = 0;

		for (int i = xLower; i <= xUpper; i++) {
			for (int j = yLower; j <= yUpper; j++) {
				if (mineSweeper.gameField[x + i][y + j] == ' ')
					return true;
			}
		}

		return false;
	}

	/**
	 * @param c
	 * @return true if the given coordinate's ,C, has a neighbor contains a number; false otherwise.
	 */
	private boolean hasNumberNeighbour(Coordinate c) {
		int x = c.getX();
		int y = c.getY();

		int xLower = -1;
		int xUpper = 1;
		int yLower = -1;
		int yUpper = 1;

		if (x == 0)
			xLower = 0;
		if (x == mineSweeper.mHeight - 1)
			xUpper = 0;
		if (y == 0)
			yLower = 0;
		if (y == mineSweeper.mWidth - 1)
			yUpper = 0;

		for (int i = xLower; i <= xUpper; i++) {
			for (int j = yLower; j <= yUpper; j++) {
				if (mineSweeper.gameField[x + i][y + j] != ' ' && mineSweeper.gameField[x + i][y + j] != 'F')
					return true;
			}
		}

		return false;
	}

	class minesAndCoordinates {
		private String[] booleanMines;
		private Coordinate[] coordinates;

		public minesAndCoordinates(boolean b, String[] boolsAl, Coordinate[] c) {
			booleanMines = boolsAl;
			coordinates = c;

		}

		public String[] getMines() {
			return booleanMines;
		}

		public Coordinate[] getCoordinates() {
			return coordinates;
		}

	}

	class Configuration {
		private boolean valid;
		private String booleanMines;
		private Coordinate[] coordinates;

		public Configuration(boolean b, String boolsAl, Coordinate[] c) {
			valid = b;
			booleanMines = boolsAl;
			coordinates = c;
		}

		public boolean getValid() {
			return valid;
		}

		public void setValid(boolean b) {
			valid = b;
		}

		public Coordinate[] getCoordinates() {
			return coordinates;
		}

		public String getBooleanMines() {
			return booleanMines;
		}

	}

	class Square {
		private char value;
		private int x;
		private int y;

		public Square(char v, int x, int y) {
			value = v;
			this.x = x;
			this.y = y;
		}

		public char getValue() {
			return value;
		}

		public void setValue(char c) {
			this.value = c;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

	}

	class CoordinateWithProbability {
		private int probability;
		private int x;
		private int y;

		public CoordinateWithProbability(int probability, int x, int y) {
			this.probability = probability;
			this.x = x;
			this.y = y;
		}

		public int getProbability() {
			return probability;
		}

		public void setProbability(int p) {
			this.probability = p;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

	}

	class Coordinate {
		private int x;
		private int y;

		public Coordinate(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}

}
