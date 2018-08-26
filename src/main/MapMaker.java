package main;

import java.util.Random;

/**
 * Creates the map used for the UIController
 *
 * @author Justin Roderman
 * @since August 2, 2018
 */

public class MapMaker
{
	/**
	 * Creates a map with a specified size and amount of mines
	 * @param yLength Length of y-axis
	 * @param xLength Length of x-axis
	 * @param yStart Y-coordinate of the starting position
	 * @param xStart X-coordinate of the starting position
	 * @param numMines Number of mines that should be on the map
	 * @return Fully populated map
	 */
	public static int[][] createMap(int yLength, int xLength, int yStart, int xStart, int numMines) throws RuntimeException
	{
		if(xLength <= 3 || yLength <= 3)
			throw new RuntimeException("Map size too small!");
		if((xLength * yLength - 9) <= numMines)
			throw new RuntimeException("Map size too small for expected number of mines!");

		int[][] map = new int[yLength][xLength];
		popMines(map, yStart, xStart, numMines);
		popIndicators(map);
		return map;
    }

	/**
	 * Populate the map with mines
	 * @param map A empty map
	 * @param yStart Y-coordinate of the starting position
	 * @param xStart X-coordinate of the starting position
	 * @param maxMines The amount of mines to place
	 */
	private static void popMines(int[][] map, int yStart, int xStart, int maxMines)
	{
		int currMines = 0;
		Random r = new Random();
		int x, y;
		while(currMines < maxMines)
		{
			y = r.nextInt(map.length);
			x = r.nextInt(map[0].length);
			if(map[y][x] != 9 &&
					!(y >= yStart - 1 && y <= yStart + 1 &&
					x >= xStart - 1 && x <= xStart + 1))
			{
				map[y][x] = 9;
				currMines++;
			}
		}
	}

	/**
	 * Populate the map with indicators of how many mines are adjacent
	 * @param map The map with the mines
	 */
	private static void popIndicators(int[][] map)
	{
		int numAdjacent = 0;
		for(int i = 0; i < map.length; i++)
		{
			for(int j = 0; j < map[0].length; j++)
			{
				if(map[i][j] != 9) // Make sure the cell is not a mine
				{
					numAdjacent = checkMines(map, i, j);
					map[i][j] = numAdjacent;
				}
			}
		}
	}

	/**
	 * Checks the amount of mines around a specified cell
	 * @param map The map with mines
	 * @param y Y-coord of the specified cell
	 * @param x X-coord of the specified cell
	 * @return The amount of adjacent mines
	 */
	public static int checkMines(int[][] map, int y, int x)
	{
		int count = 0;
		int yLength = map.length;
		int xLength = map[0].length;
		for(int yChange = -1; yChange <= 1; yChange++)
		{
			if(y + yChange >= 0 && y + yChange < yLength) // Y-bounds check
			{
				for(int xChange = -1; xChange <= 1; xChange++)
				{
					if(x + xChange >= 0 && x + xChange < xLength) // X-bounds check
						count += (map[y + yChange][x + xChange] == 9 ? 1 : 0);
				}
			}
		}
		return count;
	}

	/**
	 * Used for debugging
	 * Prints the map to the console
	 * @param map The map to be printed
	 */
	@SuppressWarnings("unused")
	private void showMap(int[][] map)
	{
		for(int i = 0; i < map.length; i++)
		{
			System.out.print("[");
			for(int j = 0; j < map[0].length; j++)
			{
				System.out.print(map[i][j] + (j != (map[0].length - 1) ? ",  " : ""));
			}
			System.out.println("]\n");
		}
	}
}
