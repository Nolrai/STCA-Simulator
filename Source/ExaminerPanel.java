import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

/* This JPanel is displayed in the Examiner JFrame and is used to graphically represent
 * the STCA transition rules - it contains the rendering logic to display the rules */
@SuppressWarnings("serial")
public class ExaminerPanel extends JPanel {

	/* Overrides default JPanel paint method */
	public void paint(Graphics g){

		/* Clear the panel completely and make it white */
		this.setBackground(Color.WHITE);
		Graphics2D g2 = (Graphics2D) g;
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());

		/* The structure of each rule */
		/* <30 pixel gap><30 pixel left><30 pixel center><30pixel right><90 pixel gap with arrow>
		 * <30 pixel left><30 pixel center> <30pixel right><90 pixel gap> */

		/* Stored locally for convenience */
		int cellsize = GlobalAttributes.cellsize;

		/* These coordinates increment throughout the method, and indiciate where to start
		 * drawing the current cell */
		int x=cellsize; 
		int y=cellsize;

		/* For every rule of the current STCA */
		for(int i=0;i<Rules.rules[GlobalAttributes.automata].length/16; i++){

			/* If the righthand size of the current rule is located past the right
			 * edge of the panel, reset the x coordinate back to the left of the panel
			 * and increment the y coordinate (has the effect of starting on a new row */
			if(x+11*cellsize>this.getWidth()){
				x=cellsize;
				y+=4*cellsize;
			}

			/* Transition rule source cell (and neighbouring subcells) */

			/* Top subcell vertices coordinates */
			int[] xPoints = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] yPoints={(y+cellsize),(y+cellsize+cellsize/2),(y+cellsize)};

			/* Bottom subcell vertices coordinates */
			int[] xPoints2 = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] yPoints2={(y+2*cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Left subcell vertices coordinates */
			int[] xPoints3 = {(x+cellsize),(x+cellsize+cellsize/2),(x+cellsize)};
			int[] yPoints3={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Right subcell vertices coordinates */
			int[] xPoints4 = {(x+2*cellsize),(x+cellsize+cellsize/2),(x+2*cellsize)};
			int[] yPoints4={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Top neighbouring subcell vertices coordinates */
			int[] xPoints5 = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] yPoints5={(y+cellsize),(y+cellsize-cellsize/2),(y+cellsize)};

			/* Bottom neighbouring subcell vertices coordinates */
			int[] xPoints6 = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] yPoints6={(y+2*cellsize),(y+2*cellsize+cellsize/2),(y+2*cellsize)};

			/* Left neighbouring subcell vertices coordinates */
			int[] xPoints7 = {(x+cellsize),(x+cellsize-cellsize/2),(x+cellsize)};
			int[] yPoints7={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Right neighbouring subcell vertices coordinates */
			int[] xPoints8 = {(x+2*cellsize),(x+2*cellsize+cellsize/2),(x+2*cellsize)};
			int[] yPoints8={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Set the appropriate colour for the top subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+2]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints,yPoints,3);
			
			/* Set the appropriate colour for the bottom subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+3]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints2,yPoints2,3);
			
			/* Set the appropriate colour for the left subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+4]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints3,yPoints3,3);
			
			/* Set the appropriate colour for the right subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+5]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints4,yPoints4,3);
			
			/* Set the appropriate colour for the top neighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+6]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints5,yPoints5,3);		
			
			/* Set the appropriate colour for the bottom neighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+7]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints6,yPoints6,3);
			
			/* Set the appropriate colour for the left neighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+8]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints7,yPoints7,3);	
			
			/* Set the appropriate colour for the right neighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+9]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(xPoints8,yPoints8,3);

			/* Draw black outlines around all the source of the rule's subcells */
			g.setColor(Color.BLACK);
			g.drawRect(x+cellsize, y+cellsize, cellsize, cellsize);
			g.drawPolygon(xPoints,yPoints,3);
			g.drawPolygon(xPoints2,yPoints2,3);
			g.drawPolygon(xPoints5,yPoints5,3);
			g.drawPolygon(xPoints6,yPoints6,3);
			g.drawPolygon(xPoints7,yPoints7,3);
			g.drawPolygon(xPoints8,yPoints8,3);

			/* Draw an arrow to connect the source of the rule to the
			 * target of the rule */
			drawArrow(g2,x+9*cellsize/2,y+3*cellsize/2,cellsize);

			/* Transition rule target cell (and neighbouring subcells) */

			/* Add some distance horizontally from the source of the rule */
			x+=6*cellsize;

			/* Top subcell vertices coordinates */
			int[] RxPoints = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] RyPoints={(y+cellsize),(y+cellsize+cellsize/2),(y+cellsize)};

			/* Bottom subcell vertices coordinates */
			int[] RxPoints2 = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] RyPoints2={(y+2*cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Left subcell vertices coordinates */
			int[] RxPoints3 = {(x+cellsize),(x+cellsize+cellsize/2),(x+cellsize)};
			int[] RyPoints3={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Right subcell vertices coordinates */
			int[] RxPoints4 = {(x+2*cellsize),(x+cellsize+cellsize/2),(x+2*cellsize)};
			int[] RyPoints4={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Top neighbouring subcell vertices coordinates */
			int[] RxPoints5 = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] RyPoints5={(y+cellsize),(y+cellsize-cellsize/2),(y+cellsize)};

			/* Bottom neighbouring subcell vertices coordinates */
			int[] RxPoints6 = {(x+cellsize),(x+cellsize+cellsize/2),((x+cellsize+cellsize))};
			int[] RyPoints6={(y+2*cellsize),(y+2*cellsize+cellsize/2),(y+2*cellsize)};

			/* Left neighbouring subcell vertices coordinates */
			int[] RxPoints7 = {(x+cellsize),(x+cellsize-cellsize/2),(x+cellsize)};
			int[] RyPoints7={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Right neighbouring subcell vertices coordinates */
			int[] RxPoints8 = {(x+2*cellsize),(x+2*cellsize+cellsize/2),(x+2*cellsize)};
			int[] RyPoints8={(y+cellsize),(y+cellsize+cellsize/2),(y+2*cellsize)};

			/* Set the appropriate colour for the top subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+10]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints,RyPoints,3);
			
			/* Set the appropriate colour for the bottom subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+11]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints2,RyPoints2,3);
			
			/* Set the appropriate colour for the left subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+12]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints3,RyPoints3,3);
			
			/* Set the appropriate colour for the right subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+13]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints4,RyPoints4,3);
			
			/* Set the appropriate colour for the top neighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+14]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints5,RyPoints5,3);		
			
			/* Set the appropriate colour for the bottom neighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+15]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints6,RyPoints6,3);
			
			/* Set the appropriate colour for the left neighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+16]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints7,RyPoints7,3);		
			
			/* Set the appropriate colour for the rightneighbouring subcell based on the rules and
			 * fill it with the colour */
			if(Rules.rules[GlobalAttributes.automata][i*16+17]==1){
				g.setColor(GlobalAttributes.state1);
			}
			else{
				g.setColor(GlobalAttributes.state0);
			}
			g.fillPolygon(RxPoints8,RyPoints8,3);

			/* Draw black outlines around all the target of the rule's subcells */
			g.setColor(Color.BLACK);
			g.drawRect(x+cellsize, y+cellsize, cellsize, cellsize);
			g.drawPolygon(RxPoints,RyPoints,3);
			g.drawPolygon(RxPoints2,RyPoints2,3);
			g.drawPolygon(RxPoints5,RyPoints5,3);
			g.drawPolygon(RxPoints6,RyPoints6,3);
			g.drawPolygon(RxPoints7,RyPoints7,3);
			g.drawPolygon(RxPoints8,RyPoints8,3);

			/* Add some width to put distance between this rule and the next */
			x+=6*cellsize;
		}
	}  

	/* Draws an arrow pointing from left to right, twice as wide as the cellsize,
	 * where the center of the arrow is the coordinates x,y */
	public void drawArrow(Graphics2D g, int x, int y,int cellsize){
		g.drawLine(x-cellsize, y, x+cellsize, y);
		g.drawLine(x+cellsize, y, x+cellsize/2, y-cellsize/4);
		g.drawLine(x+cellsize, y, x+cellsize/2, y+cellsize/4);
	}

}
