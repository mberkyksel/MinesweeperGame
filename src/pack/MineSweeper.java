package pack;

import java.util.Random;

/**
 * MineSweeper
 * 
 * @author macpro
 *
 */
class MineSweeper {
	private static MineSweeper M;

	int mWidth = 16; // width of the minefield
	int mHeight = 16; // height of the minefield
	int mMines = 40; // number of mines
	int unrevealedSquareCount = mWidth * mHeight - mMines;

	char[][] mMinefield; // 2-dimensional array of chars for our board
	char[][] gameField;
	boolean[][] revealedField;

	
	
	private MineSweeper() {

		System.out.println("Generating minefield...");

		mMinefield = new char[mHeight][mWidth];
		gameField = new char[mHeight][mWidth];
		revealedField = new boolean[mHeight][mWidth];

		System.out.println("Clearing minefield...");

		clearMinefield();

		System.out.println("Placing mines...");

		placeMines();
		drawMinefield();

		System.out.println("Calculating hints...");

		calculateHints();
		drawMinefield();

		System.out.println(".\n.\n.\nGame started, good luck...");

		drawGamefield();
	}

	/**
	 * singleton pattern is applied.
	 * 
	 * @return M, instance of MineSweeper
	 */
	public static MineSweeper getInstance() {
		if (M == null) {
			M = new MineSweeper();
		}
		return M;
	}

	/**
	 * mines are placed into the field.
	 */
	public void placeMines() {
		int minesPlaced = 0;
		Random random = new Random(); // this generates random numbers for us
		while (minesPlaced < mMines) {
			int x = random.nextInt(mWidth); // a number between 0 and mWidth - 1
			int y = random.nextInt(mHeight);
			// make sure we don't place a mine on top of another
			if (mMinefield[y][x] != '*') {
				mMinefield[y][x] = '*';
				minesPlaced++;
			}
		}
	}

	/**
	 * clears the field for game play. 
	 */
	public void clearMinefield() {
		// Set every grid space to a space character.
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				mMinefield[y][x] = '-';
				gameField[y][x] = ' ';
				revealedField[y][x] = false;
			}
		}
	}
	/**
	 * draws the field for game play. 
	 */
	public void drawMinefield() {
		System.out.print("┌");
		for (int y = 0; y < mWidth; y++) {
			if (y != mWidth - 1)
				System.out.print("───┬");
			else
				System.out.print("───");
		}
		System.out.println("┐");
		for (int y = 0; y < mHeight; y++) {
			System.out.print("│");
			for (int x = 0; x < mWidth; x++) {
				System.out.print(" ");
				System.out.print(mMinefield[y][x]);
				System.out.print(" ");
				System.out.print("│");
				if (x == mWidth - 1) {
					if (y != mHeight - 1) {
						System.out.println();
						System.out.print("├");
						for (int k = 0; k < mWidth - 1; k++) {
							System.out.print("───┼");
						}
						System.out.print("───");
					}
				}
			}
			if (y != mHeight)
				if (y == mHeight - 1)
					System.out.print("\n");
				else
					System.out.print("┤\n");

		}
		System.out.print("└");
		for (int y = 0; y < mWidth; y++) {
			if (y != mWidth - 1)
				System.out.print("───┴");
			else
				System.out.print("───");
		}
		System.out.println("┘");
	}

	
	public void calculateHints() {
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				if (mMinefield[y][x] != '*') {
					mMinefield[y][x] = minesNear(y, x);
				}
			}
		}
	}

	/**
	 * checks mines in all directions
	 * @param y Y coordinate of clicked square.
	 * @param x X coordinate of clicked square.
	 * @return "-" char if no mines, # of mines Nearby otherwise.
	 */
	public char minesNear(int y, int x) {
		int mines = 0;
		mines += mineAt(y - 1, x - 1); // NW
		mines += mineAt(y - 1, x); // N
		mines += mineAt(y - 1, x + 1); // NE
		mines += mineAt(y, x - 1); // W
		mines += mineAt(y, x + 1); // E
		mines += mineAt(y + 1, x - 1); // SW
		mines += mineAt(y + 1, x); // S
		mines += mineAt(y + 1, x + 1); // SE
		if (mines > 0) {
			// we're changing an int to a char
			// why?!
			// http://www.asciitable.com/
			// 48 is ASCII code for '0'
			return (char) (mines + 48);
		} else {
			return '-';
		}
	}

	
	/**
	 * @param y Y coordinate of clicked square.
	 * @param x X coordinate of clicked square.
	 * @return 1 if there's a mine at (y,x), 0 otherwise.
	 */
	public int mineAt(int y, int x) {
		if (y >= 0 && y < mHeight && x >= 0 && x < mWidth && mMinefield[y][x] == '*') {
			return 1;
		} else {
			return 0;
		}
	}

	public void drawGamefield() {
		System.out.print("┌");
		for (int y = 0; y < mWidth; y++) {
			if (y != mWidth - 1)
				System.out.print("───┬");
			else
				System.out.print("───");
		}
		System.out.println("┐");
		for (int y = 0; y < mHeight; y++) {
			System.out.print("│");
			for (int x = 0; x < mWidth; x++) {
				System.out.print(" ");
				System.out.print(gameField[y][x]);
				System.out.print(" ");
				System.out.print("│");
				if (x == mWidth - 1) {
					if (y != mHeight - 1) {
						System.out.println();
						System.out.print("├");
						for (int k = 0; k < mWidth - 1; k++) {
							System.out.print("───┼");
						}
						System.out.print("───");
					}
				}
			}
			if (y != mHeight)
				if (y == mHeight - 1)
					System.out.print("\n");
				else
					System.out.print("┤\n");

		}
		System.out.print("└");
		for (int y = 0; y < mWidth; y++) {
			if (y != mWidth - 1)
				System.out.print("───┴");
			else
				System.out.print("───");
		}
		System.out.println("┘");
	}

	public void drawRevealedfield() {
		System.out.print("┌");
		for (int y = 0; y < mWidth; y++) {
			if (y != mWidth - 1)
				System.out.print("───┬");
			else
				System.out.print("───");
		}
		System.out.println("┐");
		for (int y = 0; y < mHeight; y++) {
			System.out.print("│");
			for (int x = 0; x < mWidth; x++) {
				System.out.print(" ");
				System.out.print(revealedField[y][x]);
				System.out.print(" ");
				System.out.print("│");
				if (x == mWidth - 1) {
					if (y != mHeight - 1) {
						System.out.println();
						System.out.print("├");
						for (int k = 0; k < mWidth - 1; k++) {
							System.out.print("───┼");
						}
						System.out.print("───");
					}
				}
			}
			if (y != mHeight)
				if (y == mHeight - 1)
					System.out.print("\n");
				else
					System.out.print("┤\n");

		}
		System.out.print("└");
		for (int y = 0; y < mWidth; y++) {
			if (y != mWidth - 1)
				System.out.print("───┴");
			else
				System.out.print("───");
		}
		System.out.println("┘");
	}

	/**
	 * calculates unrevealed squares on the game field.
	 * 
	 * @return true, if there is no square to reveal, false otherwise.
	 */
	public boolean gameFinished() {
		int unrevealedCount = mHeight * mWidth;
		for (int i = 0; i < mHeight; i++) {
			for (int j = 0; j < mWidth; j++) {
				if (gameField[i][j] != ' ') {
					unrevealedCount--;
				}
			}
		}

		if (unrevealedCount == 0) {
			System.out.println("Mine Sweeper: You Win!");
			return true;
		}
		return false;

	}

	/**
	 * reveals squares around the clicked one unless it's a mine.
	 * game exits, if (x,y) coordinate has a mine.
	 * @param x X coordinate of clicked square
	 * @param y Y coordinate of clicked square
	 */
	public void revealSquaresOnClickedArea(int x, int y) {
		// If mine is clicked
		if (mineAt(x, y) == 1) {
			System.out.println("Clicked on a mine! Game over...");
			drawMinefield();
			System.exit(0);
		}

		// Reveal code here
		revealedField[x][y] = true;
		gameField[x][y] = mMinefield[x][y];

		if (mMinefield[x][y] == '-')
			revealNeighbours(x, y);

	}

	/**
	 * reveals neighbours of (x,y) square 
	 * @param x X coordinate of given square
	 * @param y Y coordinate of given square
	 */
	private void revealNeighbours(int x, int y) {

		int xLower = -1;
		int xUpper = 1;
		int yLower = -1;
		int yUpper = 1;

		if (x == 0)
			xLower = 0;
		if (x == mHeight - 1)
			xUpper = 0;
		if (y == 0)
			yLower = 0;
		if (y == mWidth - 1)
			yUpper = 0;

		for (int i = xLower; i <= xUpper; i++)
			for (int j = yLower; j <= yUpper; j++) {
				if ((i == 0) && (j == 0)) { // s
					continue;
				} else {
					gameField[x + i][y + j] = mMinefield[x + i][y + j];
				}

				if (revealedField[x + i][y + j] == false && gameField[x + i][y + j] == '-') {
					revealedField[x + i][y + j] = true;
					revealNeighbours(x + i, y + j);
				}

			}

	}

	public static void main(String[] args) {
		MineSweeper mineSweeper = new MineSweeper();
		LogicalAgent agent = new LogicalAgent(mineSweeper);
		agent.play();

	}

}
