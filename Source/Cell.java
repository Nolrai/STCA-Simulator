/* Object representing a cell in the cellspace. It stores a coordinate (the position of where
 * a cell can be found in the grid), as well as the states of its 4 subcells */
public class Cell {

	/* States of the four subcells */
	int topSubcellValue;
	int bottomSubcellValue;
	int leftSubcellValue;
	int rightSubcellValue;

	/* Coordinates of this cell in the grid */
	int xPosition;
	int yPosition;

}
