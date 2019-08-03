import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;

/* Object representing the cell space. Strictly speaking it is a JPanel, but it contains
 * also a 2D array of cells (the edge cells are treated as never undergoing transitions
 * and being in the quiescent state which never changes), as well as logic handling mouse 
 * clicks, annotations, and execution of the STCA */
@SuppressWarnings("serial")
public class CellSpace extends JPanel implements MouseListener, Runnable, KeyListener{

	/* 2D array of cell objects based on global variables */
	Cell[][] grid = new Cell[GlobalAttributes.xCells][GlobalAttributes.yCells];

	/* Collection of annotations and their coordinates on the cell space */
	Vector<String> annotationLabels = new Vector<String>();
	Vector<Integer> annotationCoordinates = new Vector<Integer>();

	/* Stores the annotation that is currently being typed, before the final string
	 * can be inferred */
	Vector<Character> currentAnnotationLabel=new Vector<Character>();

	/* Location of the annotation currently being typed */
	int[] currentAnnotationLocation=new int[2];

	/* Thread object which uses the cell space's run() method as its execution logic */
	Thread updaterThread = new Thread(this);

	/* Stores the coordinates of the last cell to be updated by a transition rule */
	int lastUpdatedCellXPosition=-1;
	int lastUpdatedCellYPosition=0;

	/* Records whether the cell space is in "typing" mode (someone is currently typing an annotation) */
	boolean currentlyTyping=false;

	/* Variable to indicate to the updater thread whether to pause */
	boolean shouldPause=true;

	/* Thread-safe way of setting pause to true - claims mutual exclusion of the cellspace's
	 * instance object monitor */
	synchronized void pause() {
		shouldPause = true;
	}

	/* Thread-safe way of setting pause to false - claims mutual exclusion of the cellspace's
	 * instance object monitor. It also wakes up the updater thread, which will then wait upon
	 * the same monitor (released when this method finishes) */
	synchronized void unPause() {
		shouldPause = false;
		notify();
	}
	
	/* Execution logic for the thread object */
	@Override
	public void run() {

		/* Stores 4 random numbers */
		float random1;
		float random2;
		float randomX;
		float randomY;

		/* Variables for choosing a random cell */
		int x;
		int y;

		/* Infinitely do the following until the program closes */
		while(true){

			/* Gains control of this cell space's object monitor */
			synchronized (this) {
				
				/* Until the system is unpaused  */
				while (shouldPause){
					
					/* Keep pausing the thread if it wakes up (releasing the cell space's monitor when asleep) 
					 * This will cause the thread to continue IFF it has been unpaused and then woken up  */
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}

			/* Select 2 random float values between 0 and 1 */
			random1=GlobalAttributes.random.nextFloat();
			random2=GlobalAttributes.random.nextFloat();

			/* Use the 2 above values to picks random float values between
			 * 0 and the no. of cells in the x direction, and
			 * 0 and the no. of cells in the y direction */
			randomX=random1 * (float)(GlobalAttributes.xCells);
			randomY=random2 * (float)(GlobalAttributes.yCells);

			/* Cast these floats to ints, to result in a set of coordinates
			 * of a random cell in the 2D array */
			x=(int)randomX;
			y=(int)randomY;

			/* Execute a transition for the selected cell and repaint the grid
			 * ONLY IF it is not on one of the four edges of the grid */
			if(x>0 && x<GlobalAttributes.xCells-1 && y>0 && y<GlobalAttributes.yCells-1){
				synchronized(grid){
					ApplyRule.executeTransition(x,y,grid);
				}
				lastUpdatedCellXPosition=x;
				lastUpdatedCellYPosition=y;
				repaint();
			}

			/* Sleep for the number of milliseconds indicated by the
			 * global variable (modifiable in the GUI) */
			try {
				Thread.sleep(GlobalAttributes.speed);
			} catch (InterruptedException e) {
			}
		}
	}

	/* Constructor */
	public CellSpace(){
		super();

		/* Create the 2D array of cells and set all subcells across the entire cell space
		 * to the quiescent state */
		for (int i=0; i<grid.length;i++){
			for(int j=0; j<grid[i].length;j++){
				grid[i][j]=new Cell();
				grid[i][j].xPosition=i;
				grid[i][j].yPosition=j;
				grid[i][j].topSubcellValue=0;
				grid[i][j].bottomSubcellValue=0;
				grid[i][j].leftSubcellValue=0;
				grid[i][j].rightSubcellValue=0;
			}
		}

		/* Add mouse/keyboard listeners as this class */
		this.addMouseListener(this);
		this.addKeyListener(this);

		/* Let the last character of the currently typed annotation (non-existant at this point)
		 * be a vertical bar |. This represents the typing cursor to be displayed at the end of
		 * the annotation which is currently being typed */
		currentAnnotationLabel.add(new Character('|'));

		/* Start the execution thread (it won't do much unless run=true) */
		updaterThread.start();
	}

	/* This overrides the JPanel's default paint method */
	public void paint(Graphics g){

		/* Retrieve the Java2D-enabled graphics context for the JPanel */
		Graphics2D g2 = (Graphics2D) g;

		/* Clear the drawing area (including all cell division lines */
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());

		/* Draw all cells */
		synchronized(grid){
			for (int i=0; i<grid.length;i++){
				for(int j=0; j<grid[i].length;j++){
					Cell current=grid[i][j];
					
					int cellsize = GlobalAttributes.cellsize;

					/* Calculate top triangle subcell vertices' locations in the cell space */
					int[] xPoints = {(current.xPosition*cellsize),(current.xPosition*cellsize+cellsize/2),((current.xPosition+1)*cellsize)};
					int[] yPoints={(current.yPosition*cellsize),(current.yPosition*cellsize+cellsize/2),(current.yPosition*cellsize)};

					/* Calculate bottom triangle subcell vertices' locations in the cell space */
					int[] xPoints2 = {(current.xPosition*cellsize),(current.xPosition*cellsize+cellsize/2),((current.xPosition+1)*cellsize)};
					int[] yPoints2={((current.yPosition+1)*cellsize),(current.yPosition*cellsize+cellsize/2),((current.yPosition+1)*cellsize)};

					/* Calculate left triangle subcell vertices' locations in the cell space */
					int[] xPoints3 = {(current.xPosition*cellsize),(current.xPosition*cellsize+cellsize/2),(current.xPosition*cellsize)};
					int[] yPoints3={(current.yPosition*cellsize),(current.yPosition*cellsize+cellsize/2),((current.yPosition+1)*cellsize)};

					/* Calculate right triangle subcell vertices' locations in the cell space */
					int[] xPoints4 = {((current.xPosition+1)*cellsize),(current.xPosition*cellsize+cellsize/2),((current.xPosition+1)*cellsize)};
					int[] yPoints4={(current.yPosition*cellsize),(current.yPosition*cellsize+cellsize/2),((current.yPosition+1)*cellsize)};

					/* Set the colour depending on the state of the top subcell, then paint it */
					if(current.topSubcellValue==1){
						g.setColor(GlobalAttributes.state1);
					}
					else{
						g.setColor(GlobalAttributes.state0);
					}
					g.fillPolygon(xPoints,yPoints,3);

					/* Set the colour depending on the state of the bottom subcell, then paint it */
					if(current.bottomSubcellValue==1){
						g.setColor(GlobalAttributes.state1);
					}
					else{
						g.setColor(GlobalAttributes.state0);
					}
					g.fillPolygon(xPoints2,yPoints2 , 3);

					/* Set the colour depending on the state of the left subcell, then paint it */
					if(current.leftSubcellValue==1){
						g.setColor(GlobalAttributes.state1);
					}
					else{
						g.setColor(GlobalAttributes.state0);
					}
					g.fillPolygon(xPoints3,yPoints3 , 3);

					/* Set the colour depending on the state of the right subcell, then paint it */
					if(current.rightSubcellValue==1){
						g.setColor(GlobalAttributes.state1);
					}
					else{
						g.setColor(GlobalAttributes.state0);
					}
					g.fillPolygon(xPoints4,yPoints4 , 3);

					/* Set the colour to black and draw the black outline of the
					 * square main cell in the correct location in the cellspace */
					g.setColor(Color.BLACK);
					g.drawRect(current.xPosition*cellsize, current.yPosition*cellsize, cellsize, cellsize);

					/* Draw the black outlines of the top and bottom subcells also
					 * This gives the X in the cell which divides the four subcells */
					g.drawPolygon(xPoints,yPoints , 3);
					g.drawPolygon(xPoints2,yPoints2 , 3);			
				}
			}
		}

		/* If a cell has been previously updated (so any state after the very initial
		 * state of the cell space) */
		if(lastUpdatedCellXPosition!=-1){

			/* Draw a thick green square around the location of the
			 * last cell to be updated (has the effect of highlighting the
			 * cell */
			g2.setColor(Color.GREEN);
			Stroke oldStroke = g2.getStroke();
			g2.setStroke(new BasicStroke(5));
			g2.drawRect(lastUpdatedCellXPosition*GlobalAttributes.cellsize,
					lastUpdatedCellYPosition*GlobalAttributes.cellsize, 
					GlobalAttributes.cellsize, 
					GlobalAttributes.cellsize);
			g2.setStroke(oldStroke);
		}

		/* Set the colour to red */
		g2.setColor(Color.RED);
		g2.setFont(new Font("SansSerif",Font.BOLD,16));

		/* Draw all stored annotations over the cell space,
		 * at the appropriate coordinates */
		for(int i=0;i<annotationLabels.size();i++){
			g2.drawString(annotationLabels.get(i), annotationCoordinates.get(i*2), annotationCoordinates.get(i*2+1));
		}

		/* If the user is currently typing something */
		if(currentlyTyping){

			/* Draw the set of temporary chars at the appropriate
			 * location - this set includes the vertical bar |
			 * i.e. the typing cursor */
			char[] chars=new char[currentAnnotationLabel.size()];
			for(int i=0;i<chars.length;i++){
				chars[i]=currentAnnotationLabel.get(i).charValue();
			}
			g2.drawChars(chars, 0, chars.length, currentAnnotationLocation[0], currentAnnotationLocation[1]);
		}
	}

	/* Listens for mouse clicks on the cell space */
	@Override
	public void mouseClicked(MouseEvent e) {

		/* If the program is not in annotate mode */
		if(GlobalAttributes.annotate==0){

			/* Retrieve the position of the mouse 
			 * click within the cell space */
			int x = e.getX();
			int y = e.getY();

			/* Infer the coordinates of the cell which was clicked */
			int cellX = x/GlobalAttributes.cellsize;
			int cellY= y/GlobalAttributes.cellsize;

			/* Further infer the position of the click *within* cell */ 
			int posX = x-(GlobalAttributes.cellsize*cellX);
			int posY = y-(GlobalAttributes.cellsize*cellY);

			synchronized(grid){
			
				/* If the mouse is in the left or bottom subcell of its cell */
				if(posX < posY){
	
					/* If the mouse is in the bottom subcell, toggle the state of
					 * the bottom subcell (it cycles through all states) */
					if(GlobalAttributes.cellsize-posX < posY){
						grid[cellX][cellY].bottomSubcellValue=(grid[cellX][cellY].bottomSubcellValue+1)%GlobalAttributes.noOfStates;
					}
	
					/* If the mouse is in the left subcell, toggle the state of
					 * the left subcell (it cycles through all states) */
					else{
						grid[cellX][cellY].leftSubcellValue=(grid[cellX][cellY].leftSubcellValue+1)%GlobalAttributes.noOfStates;	
					}
				}
	
				/* If the mouse is in the right or top subcell of its cell */
				else{
	
					/* If the mouse is in the right subcell, toggle the state of
					 * the right subcell (it cycles through all states) */
					if(GlobalAttributes.cellsize-posX < posY){
						grid[cellX][cellY].rightSubcellValue=(grid[cellX][cellY].rightSubcellValue+1)%GlobalAttributes.noOfStates;
					}
	
					/* If the mouse is in the top subcell, toggle the state of
					 * the top subcell (it cycles through all states) */
					else{
						grid[cellX][cellY].topSubcellValue=(grid[cellX][cellY].topSubcellValue+1)%GlobalAttributes.noOfStates;
					}
				}

			}
		}

		/* If the software is in "add annotation" mode */
		else if(GlobalAttributes.annotate==1){

			/* If the user is not currently typing an annotation */
			if(!currentlyTyping){

				/* Retrieve the location of the click and allow
				 * the user to start typing an annotation at that
				 * location - the keyboard listener will subsequently
				 * begin recording the input from the keyboard */
				currentAnnotationLocation[0] = e.getX();
				currentAnnotationLocation[1] = e.getY();
				this.requestFocus();
				currentlyTyping=true;
			}

			/* Otherwise stop the user typing an annotation and store
			 * the result */
			else{
				endTyping();
			}
		}

		/* If the software is in "remove annotation" mode */
		else if(GlobalAttributes.annotate==2){

			/* Retrieve the location of the click */
			int x = e.getX();
			int y = e.getY();

			/* Variables for storing the bounding box of the current
			 * annotation we are checking for clicks */
			int lowerX;
			int lowerY;
			int upperX;
			int upperY;

			/* Retrieve the metric data for the font that the annotation
			 * are guaranteed to be written in */
			Font font=new Font("SansSerif",Font.BOLD,16);
			FontMetrics fontmetrics=getFontMetrics(font);

			/* Search through all stored annotations */
			String temp;
			for(int i=0;i<annotationLabels.size();i++){

				/* Retrieve the annotation and work out its
				 * bounding box coordinates */
				temp=annotationLabels.get(i);
				lowerX=annotationCoordinates.get(i*2).intValue();
				upperY=annotationCoordinates.get(i*2+1).intValue();
				upperX=lowerX+fontmetrics.stringWidth(temp);
				lowerY=upperY-fontmetrics.getHeight();

				/* If the click is within the bounding box, conclude
				 * that the current annotation has been clicked,
				 * and remove it from the list of annotations,
				 * then stop the search */
				if(x>=lowerX && x<=upperX && y>=lowerY && y<=upperY){
					annotationLabels.remove(i);
					annotationCoordinates.remove(i*2);
					annotationCoordinates.remove(i*2);
					break;
				}
			}
		}

		/* Make the cell space repaint itself when available */
		this.repaint();
	}

	/* Unused mouse listener methods */
	@Override
	public void mouseEntered(MouseEvent arg0) {
	}
	@Override
	public void mouseExited(MouseEvent arg0) {
	}
	@Override
	public void mousePressed(MouseEvent arg0) {
	}
	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

	/* Iterates through all cells and sets all subcells to state 0
	 * (quiescent state), as well as clearing all annotations and
	 * their coordinates */
	public void clear(){
		for(int i=0;i<grid.length;i++){
			for(int j=0;j<grid[i].length;j++){
				grid[i][j].topSubcellValue=0;
				grid[i][j].bottomSubcellValue=0;
				grid[i][j].leftSubcellValue=0;
				grid[i][j].rightSubcellValue=0;
			}
		}
		annotationLabels.clear();
		annotationCoordinates.clear();
	}

	/* Saves the current configuration of the cell space, as well as
	 * all annotations to the file name.con */
	public void save(String name){
		try {

			/* Create the file if it does not exist */
			File file = new File(name);
			if (!file.exists()){
				file.createNewFile();
			}

			/* Delete the file it is exists and re-create
			 * as empty */
			else{
				file.delete();
				file.createNewFile();
			}

			/* Create java file writer objects */
			FileWriter fileWriter = new FileWriter(file);
			BufferedWriter writer=new BufferedWriter(fileWriter);

			/* Record in the file the number of annotations
			 * in the file (makes reading the file back easier) */
			int noOfLabels = annotationLabels.size();
			writer.write(Integer.toString(noOfLabels));
			writer.newLine();

			/* Variables for storing the current annotation to write
			 * as well as the coordinates */
			String label;
			int x;
			int y;

			/* For every annotation, write the actual string,
			 * then on the next line the x coordinate, then on
			 * the next line the y coordinate */
			for(int i=0;i<noOfLabels;i++){
				label=annotationLabels.get(i);
				x=annotationCoordinates.get(i*2);
				y=annotationCoordinates.get(i*2+1);
				writer.write(label);
				writer.newLine();
				writer.write(Integer.toString(x));
				writer.newLine();
				writer.write(Integer.toString(y));
				writer.newLine();
			}

			/* For every cell in the cell space, write the states of the
			 * four subcells in integer format, one on each line */
			for(int i=0; i<grid.length;i++){
				for(int j=0;j<grid[i].length;j++){
					writer.write(Integer.toString(grid[i][j].topSubcellValue));
					writer.newLine();
					writer.write(Integer.toString(grid[i][j].bottomSubcellValue));
					writer.newLine();
					writer.write(Integer.toString(grid[i][j].leftSubcellValue));
					writer.newLine();
					writer.write(Integer.toString(grid[i][j].rightSubcellValue));

					/* if the cell is not the bottom-right most cell then
					 * add a new line the four subcells */
					if(i!=grid.length-1 || j!=grid[i].length-1){
						writer.newLine();
					}
				}

				/* If the current row is not the bottom row, add
				 * a newRow identifier to the file at the end of a row */
				if(i!=grid.length-1){
					writer.write("newRow");
					writer.newLine();
				}
			}

			/* Close the java file writer objects */
			writer.close();
			fileWriter.close();
		} catch (IOException e1) {
		}
	}

	/* Loads the configuration file name.con into the cell space,
	 * together with any annotations at the appropriate locations */
	public void load(String name){

		/* Record the current row and column number */
		int row=0;
		int column=0;
		try {

			/* Open the file, checking one last time that it definitely
			 * exists */
			File file = new File(name);
			if (file.exists()){

				/* Create java reader objects */
				FileReader fileReader = new FileReader(file);
				BufferedReader reader=new BufferedReader(fileReader);

				/* Clear the loaded set of annotations and coordinates */
				annotationLabels.clear();
				annotationCoordinates.clear();

				/* Read from the first line of the file, the number of annotations
				 * which are stored and to be loaded */
				int noOfLabels = Integer.parseInt(reader.readLine());

				/* Variables for storing the latest annotation read and the
				 * coordinates */
				String label;
				int x;
				int y;

				/* For each annotation stored, read the annotation itself
				 * and then the x,y coordinates, then add it to the program
				 * memory */
				for(int i=0;i<noOfLabels;i++){
					label=reader.readLine();
					x=Integer.parseInt(reader.readLine());
					y=Integer.parseInt(reader.readLine());
					annotationLabels.add(label);
					annotationCoordinates.add(new Integer(x));
					annotationCoordinates.add(new Integer(y));
				}

				/* While there is data to be read left in the file */
				while(reader.ready()){

					/* Read the current line */
					String temp=reader.readLine();

					/* If its a newRow indicator, increment the current row, then
					 * take the next four subcells read for the first cell in the
					 * new row */
					if(temp.equals("newRow")){
						row++;
						grid[row][0].topSubcellValue=Integer.parseInt(reader.readLine());
						grid[row][0].bottomSubcellValue=Integer.parseInt(reader.readLine());
						grid[row][0].leftSubcellValue=Integer.parseInt(reader.readLine());
						grid[row][0].rightSubcellValue=Integer.parseInt(reader.readLine());
						column=1;
					}

					/* Else just read the four subcells and put them
					 * in the current column number, increasing the current
					 * colunm afterwards */
					else{
						grid[row][column].topSubcellValue=Integer.parseInt(temp);
						grid[row][column].bottomSubcellValue=Integer.parseInt(reader.readLine());
						grid[row][column].leftSubcellValue=Integer.parseInt(reader.readLine());
						grid[row][column].rightSubcellValue=Integer.parseInt(reader.readLine());
						column++;
					}
				}

				/* Close the java reader objects */
				reader.close();
				fileReader.close();
			}
		} catch (Exception e) {
		}
	}

	/* Ends the "currently typing" state when in add annotation mode
	 * and stores the typed annotation for permanent display */
	void endTyping(){

		/* If the currently typed annotation is longer than 1 (including 
		 * the cursor) */
		if(currentAnnotationLabel.size()>1){
			/* Store the chars in a new array without the cursor */
			char[] store=new char[currentAnnotationLabel.size()-1];
			for(int i=0;i<store.length;i++){
				store[i]=currentAnnotationLabel.get(i).charValue();
			}

			/* Convert it to a string and store it in the list of labels 
			 * with the coordinates */
			annotationLabels.add(String.valueOf(store));
			annotationCoordinates.add(new Integer(currentAnnotationLocation[0]));
			annotationCoordinates.add(new Integer(currentAnnotationLocation[1]));

			/* Clear the temporary list of chars to be ready for future inputs */
			currentAnnotationLabel.clear();
			currentAnnotationLabel.add(new Character('|'));
		}

		/* Finally leave typing mode */
		currentlyTyping=false;
	}

	/* Listens for backspace or enter/return keyboard events */
	@Override
	public void keyPressed(KeyEvent arg0) {

		/* If backspace is pressed AND the user is currently typing an annotation
		 * and it is non-empty (not including the typing cursor), then remove the last character
		 * (not including the typing cursor) */
		if(arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE && currentlyTyping && currentAnnotationLabel.size()>1){
			currentAnnotationLabel.remove(currentAnnotationLabel.size()-2);
			this.repaint();
		}

		/* If enter/return is pressed AND the user is currently typing an annotation
		 * then end the typing */
		else if(arg0.getKeyCode() ==KeyEvent.VK_ENTER && currentlyTyping){
			endTyping();
			this.repaint();
		}
	}

	/* Unused keyboard listener method */
	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	/* Listens for other keyboard presses and adds them to the currently typed
	 * annotation if it matches one of the allowed keys */
	@Override
	public void keyTyped(KeyEvent arg0) {
		if(currentlyTyping){
			if((arg0.getKeyChar()>='0' && arg0.getKeyChar()<='9') ||
					(arg0.getKeyChar()>='a' && arg0.getKeyChar()<='z') ||
					(arg0.getKeyChar()>='A' && arg0.getKeyChar()<='Z') ||
					arg0.getKeyChar()==' ' || arg0.getKeyChar()=='-'|| 
					arg0.getKeyChar()=='_'|| arg0.getKeyChar()=='='|| 
					arg0.getKeyChar()=='+'|| arg0.getKeyChar()=='('
					|| arg0.getKeyChar()==')'|| arg0.getKeyChar()=='\\'
					|| arg0.getKeyChar()=='/'|| arg0.getKeyChar()==','
					|| arg0.getKeyChar()=='.'|| arg0.getKeyChar()=='<'
					|| arg0.getKeyChar()=='>'|| arg0.getKeyChar()=='?'
					|| arg0.getKeyChar()=='|'|| arg0.getKeyChar()==';'
					|| arg0.getKeyChar()==':'|| arg0.getKeyChar()=='\''
					|| arg0.getKeyChar()=='@'|| arg0.getKeyChar()=='#'
					|| arg0.getKeyChar()=='~'|| arg0.getKeyChar()=='['
					|| arg0.getKeyChar()=='{'|| arg0.getKeyChar()==']'
					|| arg0.getKeyChar()=='}'|| arg0.getKeyChar()=='`'
					|| arg0.getKeyChar()=='¬'|| arg0.getKeyChar()=='!'
					|| arg0.getKeyChar()=='"'|| arg0.getKeyChar()=='£'
					|| arg0.getKeyChar()=='$'|| arg0.getKeyChar()=='%'
					|| arg0.getKeyChar()=='^'|| arg0.getKeyChar()=='&'
					|| arg0.getKeyChar()=='*'|| arg0.getKeyChar()=='-'){
				currentAnnotationLabel.add(currentAnnotationLabel.size()-1,new Character(arg0.getKeyChar()));
				this.repaint();
			}
		}
	}
}
