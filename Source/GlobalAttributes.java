import java.awt.Color;
import java.util.Random;

/* List of static attributes which are program-wide */
public class GlobalAttributes {

	/* Number of pixels wide that a square cell should be */
	static int cellsize=30;
	
	/* Number of cells in the x-direction */
	static int xCells = 30;
	
	/* Number of cells in the y-direction */
	static int yCells = 23;
	
	/* Colour of cells in state 0 */
	static Color state0=Color.WHITE;

	/* Colour of cells in state 0 */
	static Color state1=Color.BLACK;
	
	/* Total number of cell states (never modified as we only ever deal with 2) */
	static int noOfStates=2;
	
	/* Execution wait time (milliseconds) between attempting to apply a transition */
	static int speed= 1;
	
	/* STCA (set of rules) which is currently chosen, numbered 0,1,2... etc. */
	static int automata=0;
	
	/* Initial seed for the random number generator */
	static int initialseed=245435;
	
	/* Random number generator */
	static Random random=new Random(initialseed);
	
	/* 1 means the program is in "add annotation mode" where annotations can be added to the cell space
	 * via clicking,
	 * 2 means the program is in "remove annotation mode" where annotations can be removed from the cell space
	 * via clicking,
	 * 0 is the default (non-annotation) mode for the program, where clicking on the cell space modifies cell states */
	static int annotate=0;
}
