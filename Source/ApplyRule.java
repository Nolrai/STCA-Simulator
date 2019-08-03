/* Stores all STCA rules and also applies rules to a given cell if available */
public class ApplyRule {

	/* Given a cell and its neighbouring subcells in the form of a list of states of subcells,
	 * it searches for a rule to apply and returns a new list of states of subcells (simulating
	 * the application of the rule */
	static int[] applyRule(int localTop, int localBottom, int localLeft, int localRight, 
			int neighbourTop, int neighbourBottom, int neighbourLeft, int neighbourRight){

		/* Assign the result cell as simply the input cell until we detect a rule to apply */
		int newLocalTop=localTop;
		int newLocalBottom=localBottom;
		int newLocalLeft=localLeft;
		int newLocalRight=localRight;
		int newNeighbourTop=neighbourTop;
		int newNeighbourBottom=neighbourBottom;
		int newNeighbourLeft=neighbourLeft;
		int newNeighbourRight=neighbourRight;
		int updated=0;

		/* Variables for retrieving the source of the rule (for comparison purposes) */
		int lup, ldown, lleft, lright, nup, ndown, nleft, nright;

		/* Make note of the STCA we are using and its number of rules */
		int automata = GlobalAttributes.automata;
		int noOfRules = (Rules.rules[automata].length-2)/16;

		/* For each rule in the STCA */
		for(int i=0; i<noOfRules;i++){

			/* Retrieve the states of the left hand side */
			lup=Rules.rules[automata][i*16+2];
			ldown=Rules.rules[automata][i*16+3];
			lleft=Rules.rules[automata][i*16+4];
			lright=Rules.rules[automata][i*16+5];
			nup=Rules.rules[automata][i*16+6];
			ndown=Rules.rules[automata][i*16+7];
			nleft=Rules.rules[automata][i*16+8];
			nright=Rules.rules[automata][i*16+9];

			/* If we detect a match between the rule and the inputted cell, then set
			 * the resulting cell to the right hand side of the rule, and stop searching
			 * through the rules */
			if(localTop==lup && localBottom==ldown && localLeft==lleft && localRight==lright
					&&neighbourTop==nup && neighbourBottom==ndown && neighbourLeft==nleft &&
					neighbourRight==nright){
				newLocalTop=Rules.rules[automata][i*16+10];
				newLocalBottom=Rules.rules[automata][i*16+11];
				newLocalLeft=Rules.rules[automata][i*16+12];
				newLocalRight=Rules.rules[automata][i*16+13];
				newNeighbourTop=Rules.rules[automata][i*16+14];
				newNeighbourBottom=Rules.rules[automata][i*16+15];
				newNeighbourLeft=Rules.rules[automata][i*16+16];
				newNeighbourRight=Rules.rules[automata][i*16+17];
				updated=1;
				break;
			}
		}

		/* Return the new cell (which may or may not be equivalent to the input cell) and
		 * a flag to indicate whether a change has been detected and applied */
		return new int[]{newLocalTop,newLocalBottom,newLocalLeft,newLocalRight,newNeighbourTop,newNeighbourBottom,
				newNeighbourLeft,newNeighbourRight, updated};
	}

	/* Given the cell space and a set of coordinates, attempt to
	 * apply a transition to the cell at those coordinates */
	static boolean executeTransition(int x, int y, Cell[][] grid) {

		/* Retrieve the states of all relevant subcells (including neighbours) */
		int localTop=grid[x][y].topSubcellValue;
		int localBottom=grid[x][y].bottomSubcellValue;
		int localLeft=grid[x][y].leftSubcellValue;
		int localRight=grid[x][y].rightSubcellValue;
		int neighbourTop=grid[x][y-1].bottomSubcellValue;
		int neighbourRight=grid[x+1][y].leftSubcellValue;
		int neighbourBottom=grid[x][y+1].topSubcellValue;
		int neighbourLeft=grid[x-1][y].rightSubcellValue;

		/* Assume it has not been updated until otherwise */
		boolean updated=false;

		/* Record the number of rotations needed and how
		 * many times we need to reflect */
		int rotations=0;
		int reflect=0;

		/* Check if the automaton is rotation symmetric and set
		 * number of rotations needed */
		if(Rules.rules[GlobalAttributes.automata][0]==1){
			rotations=3;
		}

		/* Set reflection type given by the STCA rules */
		reflect=Rules.rules[GlobalAttributes.automata][1];

		/* Calculate how many different times to reflect */
		int reflectTimes=0;

		/* If only horizontal or only vertical then we only reflect once */
		if(reflect==1 || reflect==2){
			reflectTimes=1;
		}

		/* If both separately then reflect twice */
		else if(reflect==3){
			reflectTimes=2;
		}

		/* If compounded then reflect three times (once for each individual axis,
		 * and then compounded */
		else if(reflect==4){
			reflectTimes=3; /* If rotation symmetric then this is not needed */
		}

		/* Use this to store the subcells' states after rotations */
		int[] subcells;

		/* For each possible reflection (including none) */
		for(int r=0;r<=reflectTimes;r++){

			/* If we are currently reflecting (so every time we visit this after the 0th iteration) */
			if(r>0){
				
				/* This decides whether to reflect horizontally */
				if(reflect==1 /* We are reflecting only horizontally, so we must be doing so now */
						|| (reflect==3 && r==1) /* Reflecting both but not together, so only do it
												the first time arriving here */
						|| (reflect==4 && r!=2) /* Reflecting both separately AND together, so do it, the first
												and third times we are here */){
					
					/* Reflect the subcells horizontally */
					subcells=reflect(localTop,localBottom,localLeft,localRight,
							neighbourTop,neighbourBottom,neighbourLeft,neighbourRight,1);
					localTop=subcells[0];
					localBottom=subcells[1];
					localLeft=subcells[2];
					localRight=subcells[3];
					neighbourTop=subcells[4];
					neighbourBottom=subcells[5];
					neighbourLeft=subcells[6];
					neighbourRight=subcells[7];
				}
				
				/* This decides whether to reflect vertically */
				if(reflect==2 /* We are are reflecting only vertically, so we must be doing so now */
						|| (reflect==3 && r==2) /* Reflecting both but not together, so only do it
												the second time arriving here */
						|| (reflect==4 && r!=1) /* Reflecting both separately AND together, so do it the second
												and third times we are here */){
					
					/* Reflect the subcells vertically */
					subcells=reflect(localTop,localBottom,localLeft,localRight,
							neighbourTop,neighbourBottom,neighbourLeft,neighbourRight,2);
					localTop=subcells[0];
					localBottom=subcells[1];
					localLeft=subcells[2];
					localRight=subcells[3];
					neighbourTop=subcells[4];
					neighbourBottom=subcells[5];
					neighbourLeft=subcells[6];
					neighbourRight=subcells[7];
				}
			}

			/* For each rotation position - including the initial (which is also done when not rotating) */
			for(int i=0; i<=rotations;i++){

				/* Rotate the cells 90*i degrees on the i'th iteration */
				if(i>0){
					subcells=rotate(localTop,localBottom,localLeft,localRight,
							neighbourTop,neighbourBottom,neighbourLeft,neighbourRight,i);
					localTop=subcells[0];
					localBottom=subcells[1];
					localLeft=subcells[2];
					localRight=subcells[3];
					neighbourTop=subcells[4];
					neighbourBottom=subcells[5];
					neighbourLeft=subcells[6];
					neighbourRight=subcells[7];
				}
				
				/* Search and apply a rule if possible */
				int[] updates=applyRule(localTop,localBottom,localLeft,localRight, 
						neighbourTop, neighbourBottom, neighbourLeft, neighbourRight);

				/* If we have rotated at all */
				if(i>0){
					
					/* Keep rotating the result by 4-i to end up back in the initial position
					 * and apply it to the original cell (if no update occurred then it will
					 * just end up the original (possibly still reflected) cell) */
					subcells=rotate(updates[0],updates[1],updates[2],updates[3],
							updates[4],updates[5],updates[6],updates[7],4-i);
					localTop=subcells[0];
					localBottom=subcells[1];
					localLeft=subcells[2];
					localRight=subcells[3];
					neighbourTop=subcells[4];
					neighbourBottom=subcells[5];
					neighbourLeft=subcells[6];
					neighbourRight=subcells[7];
					
				/* Or if no rotation occurred, and an update occurred, then just
				 * equate the original (possibly reflected) cell to the result 
				 * (this stops it bothering if no update occurred) */
				}else if(updates[8]==1){
					localTop=updates[0];
					localBottom=updates[1];
					localLeft=updates[2];
					localRight=updates[3];
					neighbourTop=updates[4];
					neighbourBottom=updates[5];
					neighbourLeft=updates[6];
					neighbourRight=updates[7];
				}

				/* If an update occurred */
				if(updates[8]==1){

					/* Prematurely undo reflection (this is after rotation has been undone) - this is ok as we will
					 * not be doing another pass, so we can undo the reflection early, it is not going to be used for
					 * a future rotation - this is also required to record new result correctly */
					if(r>0){
						
						/* Re-reflect horizontally same as above if required */
						if(reflect==1
								|| (reflect==3 && r==1)
								|| (reflect==4 && r!=2)){
							subcells=reflect(localTop,localBottom,localLeft,localRight,
									neighbourTop,neighbourBottom,neighbourLeft,neighbourRight,1);
							localTop=subcells[0];
							localBottom=subcells[1];
							localLeft=subcells[2];
							localRight=subcells[3];
							neighbourTop=subcells[4];
							neighbourBottom=subcells[5];
							neighbourLeft=subcells[6];
							neighbourRight=subcells[7];
						}
						
						/* Re-reflect vertically same as above if required */
						if(reflect==2 
								|| (reflect==3 && r==2)
								|| (reflect==4 && r!=1)){
							subcells=reflect(localTop,localBottom,localLeft,localRight,
									neighbourTop,neighbourBottom,neighbourLeft,neighbourRight,2);
							localTop=subcells[0];
							localBottom=subcells[1];
							localLeft=subcells[2];
							localRight=subcells[3];
							neighbourTop=subcells[4];
							neighbourBottom=subcells[5];
							neighbourLeft=subcells[6];
							neighbourRight=subcells[7];
						}
					}

					/* Set the new subcell values for the cell */
					grid[x][y].topSubcellValue=localTop;
					grid[x][y].bottomSubcellValue=localBottom;
					grid[x][y].leftSubcellValue=localLeft;
					grid[x][y].rightSubcellValue=localRight;
					
					/* Only set the appropriate neighbours if they actually exist
					 * i.e. we don't attempt to modify cells beyond the edges */
					if(y>0){
						grid[x][y-1].bottomSubcellValue=neighbourTop;
					}
					if(y<GlobalAttributes.yCells-1){
						grid[x][y+1].topSubcellValue=neighbourBottom;
					}
					if(x>0){
						grid[x-1][y].rightSubcellValue=neighbourLeft;}
					if(x<GlobalAttributes.xCells-1){
						grid[x+1][y].leftSubcellValue=neighbourRight;
					}
					
					/* Stop trying new rotations and just break from the loop */
					updated=true;
					break;
				}
			}
			/* Reflection is already undone if update has occurred, so now just break away from whole process */
			if(updated){
				break;
			}
			
			/* Undo reflection ready for next fresh reflection loop if one is active */
			else if(r>0){
				
				/* Re-reflect horizontally same as above if required */
				if(reflect==1
						|| (reflect==3 && r==1)
						|| (reflect==4 && r!=2)){
					subcells=reflect(localTop,localBottom,localLeft,localRight,
							neighbourTop,neighbourBottom,neighbourLeft,neighbourRight,1);
					localTop=subcells[0];
					localBottom=subcells[1];
					localLeft=subcells[2];
					localRight=subcells[3];
					neighbourTop=subcells[4];
					neighbourBottom=subcells[5];
					neighbourLeft=subcells[6];
					neighbourRight=subcells[7];
				}
				
				/* Re-reflect vertically same as above if required */
				if(reflect==2
						|| (reflect==3 && r==2)
						|| (reflect==4 && r!=1)){
					subcells=reflect(localTop,localBottom,localLeft,localRight,
							neighbourTop,neighbourBottom,neighbourLeft,neighbourRight,2);
					localTop=subcells[0];
					localBottom=subcells[1];
					localLeft=subcells[2];
					localRight=subcells[3];
					neighbourTop=subcells[4];
					neighbourBottom=subcells[5];
					neighbourLeft=subcells[6];
					neighbourRight=subcells[7];
				}
			}
		}
		
		/* Return whether there was an update or not */
		return updated;
	}

	/* Rotates subcell states by noOfRotations multiples of 90 degrees just 
	 * by swapping them appropriately, then return the newly rotated cell */
	public static int[] rotate(int top, int bottom, int left, int right, 
			int neighbourUp, int neighbourBottom, int neighbourLeft, int neighbourRight, 
			int noOfRotations){
		int newLocalTop;
		int newLocalBottom;
		int newLocalLeft;
		int newLocalRight;
		int newNeighbourTop;
		int newNeighbourBottom;
		int newNeighbourLeft;
		int newNeighbourRight;

		for(int i=0; i<noOfRotations;i++){
			newLocalTop=left;
			newLocalBottom=right;
			newLocalLeft=bottom;
			newLocalRight=top;
			newNeighbourTop=neighbourLeft;
			newNeighbourBottom=neighbourRight;
			newNeighbourLeft=neighbourBottom;
			newNeighbourRight=neighbourUp;
			top=newLocalTop;
			bottom=newLocalBottom;
			left=newLocalLeft;
			right=newLocalRight;
			neighbourUp=newNeighbourTop;
			neighbourBottom=newNeighbourBottom;
			neighbourLeft=newNeighbourLeft;
			neighbourRight=newNeighbourRight;
		}
		return new int[]{top,bottom,left,right,neighbourUp,neighbourBottom,
				neighbourLeft,neighbourRight};		
	}

	/* Reflect subcell states by just swapping them appropriately
	 * Then return the newly reflected cell
	 * reflection type: 1= horizontal, 2=vertical */
	public static int[] reflect(int top, int bottom, int left, int right, 
			int neighbourUp, int neighbourBottom, int neighbourLeft, int neighbourRight, 
			int reflectType){
		int newLocalTop;
		int newLocalBottom;
		int newLocalLeft;
		int newLocalRight;
		int newNeighbourTop;
		int newNeighbourBottom;
		int newNeighbourLeft;
		int newNeighbourRight;
		if(reflectType==1){
			newLocalLeft=right;
			newLocalRight=left;
			newNeighbourLeft=neighbourRight;
			newNeighbourRight=neighbourLeft;
			left=newLocalLeft;
			right=newLocalRight;
			neighbourLeft=newNeighbourLeft;
			neighbourRight=newNeighbourRight;
		}
		else if(reflectType==2){
			newLocalTop=bottom;
			newLocalBottom=top;
			newNeighbourTop= neighbourBottom;
			newNeighbourBottom=neighbourUp;
			top=newLocalTop;
			bottom=newLocalBottom;
			neighbourUp=newNeighbourTop;
			neighbourBottom=newNeighbourBottom;
		}		
		return new int[]{top,bottom,left,right,neighbourUp,neighbourBottom,
				neighbourLeft,neighbourRight};		
	}
}
