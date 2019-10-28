# MinesweeperGame 
- with AI // Logic agent plays!
Mine Sweeper (also known as Minesweeper) is a one-player puzzle game. The objective of the game is to reveal all the safe squares on the whole board without encountering mines.
As the safe squares are revealing, they give clues about the mines around them. This helps the player to come up with ideas about which squares are safe to open. 

There are 3 levels in the minefield game:
 1) Entry level: 9x9, 10 mines
 2) Intermediate Level: 16x16, 40 mines
 3) Difficult Level: 24x24, 99 mines 

This puzzle can be handled as a constrant satisfaction problem.
Domain D = {0,1} // mine-free, has mine 
Variables V = {1, .., n} // n is the number of squares, agent considers to click each turn

Demo:
Player: Not you! Logic Agent
Board: 16x16, 40 mines

The game board is a 2-dimensional space. 
It consists of NxN squares. Some of these squares contain mines randomly. 
The total number of mines is also predetermined.
The squares can be in two states, revealed or hidden.

  If left-clicking on a hidden square:
    If the square does not contain mines:
      If the neighboring square(s) contain mines, the square that has been opened indicates the number of neighboring mines.
      If adjacent squares do not contain mines, all adjacent squares are automatically opened.
    If the square contains mines the game ends with failure. // Game over!

  If a hidden square is right-clicked: 
    a flag is placed on that frame. This flag means “there is a mine here”.
  
  If all mines-free squares are left-clicked, the game ends successfully.


Space required for configuration = 2 ^ n // to figure out squares in which are safe to click. // 
 >> Big O notaion(2^n) 



