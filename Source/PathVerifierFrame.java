import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

/* This class represents the window shown when Path Verification is clicked in the main window.
 * It contains GUI logic, as well as the underlying "path-verification" feature. This feature
 * will repeatedly execute an STCA from a given source configuration at high speed and check if it reaches
 * a given (referred to as target) configuration. This is not formal verification however as it executes stochastically. 
 * It also has the side effect of detecting if a deadlock may occur. 
 * The class contains a thread with associated execution logic, and also acts as an event
 * listener */
@SuppressWarnings("serial")
public class PathVerifierFrame extends JFrame implements Runnable, ActionListener, ItemListener{

	/* A 2D array of cells representing the current state of the STCA as we perform the check - 
	 * a CellSpace is not used as we do not need the associated graphical
	 * or interactive features */
	Cell[][] grid = new Cell[GlobalAttributes.xCells][GlobalAttributes.yCells];

	/* A 2D array of cells representing the starting configuration */
	Cell[][] source = new Cell[GlobalAttributes.xCells][GlobalAttributes.yCells];

	/* A 2D array of cells representing the (ideal) final configuration */
	Cell[][] target = new Cell[GlobalAttributes.xCells][GlobalAttributes.yCells];

	/* A 2D array which stores how "different" the current configuration is from the target configuration.
	 * A cell in the array is true iff the corresponding cell in the cell space is different from the 
	 * target configuration's corresponding cell */
	boolean[][] different = new boolean[GlobalAttributes.xCells][GlobalAttributes.yCells];

	/* The fixed "initial" difference matrix used to restore the above variable back to the initial configuration's
	 * difference matrix */
	boolean[][] fixeddifferent = new boolean[GlobalAttributes.xCells][GlobalAttributes.yCells];

	/* Integer counter of the number of differences between the current configuration and the target configuration
	 * (Corresopnds to the number of true values in the difference matrix) */
	int differences;

	/* Fixed number of differences between the initial configuration and the target configuration. (Corresponds
	 * to the number of true values in the fixed difference matrix) */
	int fixeddifferences;

	/* Records how many tests have been carried out since pressing Begin */
	int completed;

	/* Records whether to output debugging information to system console */
	int output=0;

	/* Records whether the system should not attempt an "exhaustive" approach to trying transitions
	 * - after a sufficient number of failed rule matches, the system basically iterates through every
	 * single cell from top-left to bottom-right and tries to match against a rule - this is how deadlock
	 * detection works */
	int exhaust=0;

	/* Records the coordinates of the current cell being used to attempt a transition */
	int x=0;
	int y=0;

	/* Thread object to wrap this class' run method in */
	Thread updater = new Thread(this);

	/* Variable to indicate to the updater thread whether to pause */
	boolean paused=true;

	/* Thread-safe way of setting pause to true - claims mutual exclusion of the jframe's
	 * instance object monitor */
	synchronized void pause() {
		paused = true;
	}

	/* Thread-safe way of setting pause to false - claims mutual exclusion of the jframe's
	 * instance object monitor. It also wakes up the updater thread, which will then wait upon
	 * the same monitor (released when this method finishes) */
	synchronized void unPause() {
		paused = false;
		notify();
	}

	/* Thread-safe way of checking pause - claims mutual exclusion of the jframe's
	 * instance object monitor */
	synchronized boolean isPaused(){
		return paused;
	}

	/* Records whether a pair of configurations has been successfully loaded - and hence testing is allowed */
	int loaded=0;

	/* Records the current number of failed transitions - used to determine whether exhausitve approach
	 * is needed. This is reset to 0 as soon as a successful transition is yielded via an exhaustive approach
	 * and normal randomised mode is resumed */
	int stall=0;

	/* Indicates the maximum number of failed randomised transition attempts before exhausitve mode is used */
	int maxStalls= 100000;

	/* GUI items - */

	/* Allows you to select source and target configuration files */
	@SuppressWarnings("rawtypes")
	JComboBox path1 = new JComboBox();
	@SuppressWarnings("rawtypes")
	JComboBox path2 = new JComboBox();

	/* Loads the selected source and target configuration files into memory */
	JButton load = new JButton("Load");

	/* Displays the names of the configuration files */
	JLabel src = new JLabel("N/A");
	JLabel tgt = new JLabel("N/A");

	/* Lists the current state of the tests - either the number of successful tests,
	 * whether the system is in exhaustive mode, or whether the STCA has deadlocked */
	JLabel result = new JLabel("N/A");

	/* Closes the Path Verification window */
	JButton close = new JButton("Close");

	/* Controls the execution of the tests */
	JButton startcheck = new JButton("Begin");
	JButton stopcheck = new JButton("End");

	/* Decides whether the system performs a single test, or just attempts indefinitely */
	JCheckBox repeat = new JCheckBox();
	@SuppressWarnings("rawtypes")

	/* Allows the selection of the STCA rules used for testing */
	JComboBox automata = new JComboBox();

	/* Execution logic which is wrapped in the above "updater" Thread */
	@Override
	public void run() {

		/* Indefinitely do the following */
		while(true){

			/* Gains control of this jframe's object monitor */
			synchronized (this) {
				
				/* Until the system is unpaused  */
				while (paused){
					
					/* Keep pausing the thread if it wakes up (releasing the jframe's monitor when asleep) 
					 * This will cause the thread to continue IFF it has been unpaused and then woken up  */
					try {
						wait();
					} catch (InterruptedException e) {
					}
				}
			}

			/* Store whether we wish to repeat the test over and over */
			boolean loop=false;
			if(repeat.isSelected()){
				loop=true;
			}

			/* Guarantees that the test is done at least once (in case loop is false) */
			boolean once=true;

			/* Reset the counter that tracks how many times the test has been done */
			completed=0;

			/* Loop is true or if we haven't done it at least once yet */
			while(loop || once){

				/* Give the thread chance to stop if requested */
				if(isPaused()){
					break;
				}

				/* Variables for deciding which cell to attempt a transition on */
				x=0;
				y=0;

				/* Prepare's the difference matrix and difference counter, and sets
				 * the configuration of the STCA to the initial configuration */
				prepare();

				/* While the STCA's current configuration is different from the final
				 * configuration, AND the system has not received the request to pause/stop */
				while(differences>0 && !isPaused()){

					/* If the system is not in exhaustive mode */
					if(exhaust==0){

						/* Decide the cell coordinates randomly */
						float random1=GlobalAttributes.random.nextFloat();
						float random2=GlobalAttributes.random.nextFloat();
						float randomX=random1 * (float)(GlobalAttributes.xCells);
						float randomY=random2 * (float)(GlobalAttributes.yCells);
						x=(int)randomX;
						y=(int)randomY;
					}

					/* Else increment the cell column number, but if the cell
					 * is the last in the column, then increment the row number
					 * and select the cell from the left most column */
					else{
						x++;
						if(x==GlobalAttributes.xCells){
							x=0;
							y++;
						}
					}

					/* Assume that the cell is not updated until otherwise */
					boolean updated=false;

					/* Only execute transition if not a bordering cell */
					if(x>0 && x<GlobalAttributes.xCells-1 && y>0 && y<GlobalAttributes.yCells-1){
						updated=ApplyRule.executeTransition(x,y,grid);
					}

					/* Used for debugging */
					if(output==1){
						System.out.println("got through section 1");
					}

					/* If a cell update has occurred */
					if(updated){

						/* Used for debugging */
						if(output==1){
							System.out.println("a cell was updated - comparing");
						}

						/* If the cell is now in the same state as the final configuration */
						if(target[x][y].topSubcellValue==grid[x][y].topSubcellValue &&
								target[x][y].bottomSubcellValue==grid[x][y].bottomSubcellValue &&
								target[x][y].leftSubcellValue==grid[x][y].leftSubcellValue &&
								target[x][y].rightSubcellValue==grid[x][y].rightSubcellValue){

							/* But was different before */
							if(different[x][y]){

								/* Modify the difference matrix and decrement the difference
								 * counter */
								different[x][y]=false;
								differences--;

								/* Used for debugging */
								if(output==1){
									System.out.println("cells are the same but were different before");}
							}

							/* Else if the cell is now different from the final configuration */
						}else{

							/* But was the same before */
							if(!different[x][y]){

								/* Modify the difference matrix and increment the difference
								 * counter */
								different[x][y]=true;
								differences++;

								/* Used for debugging */
								if(output==1){
									System.out.println("cells are now different but were the same before");}
							}
						}

						/* Used for debugging */
						if(output==1){
							System.out.println("updating left neighbour");
						}

						/* If the current cell has a left neighbour */
						if(x>0){

							/* And the left neighbour cell is now the same as the final configuration */
							if(target[x-1][y].rightSubcellValue==grid[x-1][y].rightSubcellValue &&
									target[x-1][y].bottomSubcellValue==grid[x-1][y].bottomSubcellValue &&
									target[x-1][y].leftSubcellValue==grid[x-1][y].leftSubcellValue &&
									target[x-1][y].topSubcellValue==grid[x-1][y].topSubcellValue){

								/* But was different before */
								if(different[x-1][y]){

									/* Modify the difference matrix and decrement the difference
									 * counter */
									different[x-1][y]=false;
									differences--;

								}

								/* Else if the left neighbour cell is now different from the final configuration */
							}else{

								/* But was the same before */
								if(!different[x-1][y]){

									/* Modify the difference matrix and increment the difference
									 * counter */
									different[x-1][y]=true;
									differences++;
								}
							}
						}

						/* If the current cell has a right neighbour */
						if(x<GlobalAttributes.xCells-1){

							/* And the right neighbour cell is now the same as the final configuration */
							if(target[x+1][y].leftSubcellValue==grid[x+1][y].leftSubcellValue &&
									target[x+1][y].bottomSubcellValue==grid[x+1][y].bottomSubcellValue &&
									target[x+1][y].topSubcellValue==grid[x+1][y].topSubcellValue &&
									target[x+1][y].rightSubcellValue==grid[x+1][y].rightSubcellValue){

								/* But was different before */
								if(different[x+1][y]){

									/* Modify the difference matrix and decrement the difference
									 * counter */
									different[x+1][y]=false;
									differences--;
								}

								/* Else if the right neighbour cell is now different from the final configuration */
							}else{

								/* But was the same before */
								if(!different[x+1][y]){

									/* Modify the difference matrix and increment the difference
									 * counter */
									different[x+1][y]=true;
									differences++;
								}
							}
						}

						/* If the current cell has a top neighbour */
						if(y>0){

							/* And the top neighbour cell is now the same as the final configuration */
							if(target[x][y-1].bottomSubcellValue==grid[x][y-1].bottomSubcellValue &&
									target[x][y-1].topSubcellValue==grid[x][y-1].topSubcellValue &&
									target[x][y-1].leftSubcellValue==grid[x][y-1].leftSubcellValue &&
									target[x][y-1].rightSubcellValue==grid[x][y-1].rightSubcellValue){

								/* But was different before */
								if(different[x][y-1]){

									/* Modify the difference matrix and decrement the difference
									 * counter */
									different[x][y-1]=false;
									differences--;
								}

								/* Else if the top neighbour cell is now different from the final configuration */
							}else{

								/* But was the same before */
								if(!different[x][y-1]){

									/* Modify the difference matrix and increment the difference
									 * counter */
									different[x][y-1]=true;
									differences++;
								}
							}
						}

						/* If the current cell has a bottom neighbour */
						if(y<GlobalAttributes.yCells-1){

							/* And the bottom neighbour cell is now the same as the final configuration */
							if(target[x][y+1].topSubcellValue==grid[x][y+1].topSubcellValue &&
									target[x][y+1].topSubcellValue==grid[x][y+1].topSubcellValue &&
									target[x][y+1].leftSubcellValue==grid[x][y+1].leftSubcellValue &&
									target[x][y+1].rightSubcellValue==grid[x][y+1].rightSubcellValue){

								/* But was different before */
								if(different[x][y+1]){

									/* Modify the difference matrix and decrement the difference
									 * counter */
									different[x][y+1]=false;
									differences--;
								}

								/* Else if the bottom neighbour cell is now different from the final configuration */
							}else{

								/* But was the same before */
								if(!different[x][y+1]){

									/* Modify the difference matrix and increment the difference
									 * counter */
									different[x][y+1]=true;
									differences++;
								}
							}
						}

						/* A successful update means set the failed transition counter to 0 */
						stall=0;

						/* And if the system is in exhaustive mode then set it back to normal mode */
						if(exhaust==1){
							exhaust=0;
							result.setText("Update occurred: returning to random selection");
						}
					}

					/* If an update has NOT occurred */
					else{

						/* And the system is not in exhaustive mode */
						if(exhaust==0){

							/* Increment the failed transition counter */
							stall++;

							/* If the failed transition threshold has been reached */
							if(stall==maxStalls){

								/* Set the system to exhaustive mode */
								exhaust=1;
								result.setText("No updates after 100000 transitions: attempting iterative approach");
								x=0; y=0;
							}
						}

						/* Else if the system is in exhaustive mode, and the bottom-right-most cell
						 * has failed to update */
						else if(x==GlobalAttributes.xCells-1 && y==GlobalAttributes.yCells-1){

							/* Then there is a definite deadlock of the STCA and execution can stop */
							result.setText("Deadlock");
							pause();
							x=0; y=0;
						}
					}

					/* Used for debugging */
					if(output==1){
						System.out.println("loop finished");
					}
				}

				/* If the current configuration is identical to the target configuration */
				if(differences==0){

					/* Stop the test if the user only wishes to run it once */
					if(!loop){
						result.setText("Target reached");
						pause();
					}

					/* Otherwise continue and run the test again (loops back around) while incrementing
					 * the counter of successful tests */
					else{
						completed++;
						result.setText(completed+" runs match target");
					}

					/* Set this to false to ensure that the test doesn't occur again
					 * if the user only wishes a single test */
					once=false;
				}
			}
		}
	}



	/* Restore source configuration and difference matrix from the fixed initial versions */
	public void prepare(){

		/* For every cell in the cell space */
		for (int i=0;i<GlobalAttributes.xCells;i++){
			for(int j=0;j<GlobalAttributes.yCells;j++){

				/* Set the cell state to the starting configuration */
				grid[i][j].topSubcellValue=source[i][j].topSubcellValue;
				grid[i][j].bottomSubcellValue=source[i][j].bottomSubcellValue;
				grid[i][j].leftSubcellValue=source[i][j].leftSubcellValue;
				grid[i][j].rightSubcellValue=source[i][j].rightSubcellValue;

				/* Set the "current" difference matrix value to
				 * the value in the fixed initial matrix */
				different[i][j]=fixeddifferent[i][j];				
			}
		}

		/* Reset the differences counter to the fixed version */
		differences=fixeddifferences;
	}

	/* Calculates the number of differences between the starting configuration
	 * and the target configuration - it also generates the fixed initial 
	 * difference matrix */
	public void differences(){

		/* Set the value of the initial differences to zero (it is calculated through
		 * incrementing the variable) */
		fixeddifferences=0;

		/* For every cell in the cell space */
		for (int i=0;i<GlobalAttributes.xCells;i++){
			for(int j=0;j<GlobalAttributes.yCells;j++){

				/* If the value in the initial configuration differs from the value
				 * in the target configuration */
				if(source[i][j].topSubcellValue != target[i][j].topSubcellValue ||
						source[i][j].bottomSubcellValue != target[i][j].bottomSubcellValue ||
						source[i][j].leftSubcellValue != target[i][j].leftSubcellValue ||
						source[i][j].rightSubcellValue != target[i][j].rightSubcellValue){

					/* Set the value in the fixed initial difference matrix
					 * to true and increment the fixed initial differences counter */
					fixeddifferent[i][j]=true;
					fixeddifferences++;
				}

				/* Else if the value does not differ, set the value in the
				 * fixed initial differences matrix to false and do not increment
				 * the fixed initial differences counter */
				else{
					fixeddifferent[i][j]=false;
				}
			}
		}
	}

	/* Constructor class which sets up the GUI for this window and sets the
	 * event listener for various GUI components as this class - it also
	 * initialises the cell space used in this class */
	@SuppressWarnings("unchecked")
	public PathVerifierFrame(){

		/* Sets the layout of the window to FlowLayout */
		this.getContentPane().setLayout(new FlowLayout());

		/* Creates two JPanels and adds them to the window contents.
		 * These are used to make layout easier. The first JPanel is for
		 * the left of the window, and the second is for the right. */
		JPanel pathPanel = new JPanel();
		this.getContentPane().add(pathPanel);
		JPanel pathPanel2 = new JPanel();
		this.getContentPane().add(pathPanel2);

		/* Add all the components (buttons, textareas, comboboxes), 
		 * labels, and whitespace needed for layout to the two JPanels */
		pathPanel.add(new JLabel("Automaton:"));
		pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel.add(automata);
		pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel.add(new JLabel("_____________________________"));
		pathPanel.add(Box.createRigidArea(new Dimension(500,10)));
		pathPanel.add(new JLabel("Configurations:"));
		pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel.add(new JLabel("Source"));
		pathPanel.add(path1);
		pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel.add(new JLabel("Target"));
		pathPanel.add(path2);
		pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel.add(load);
		pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel.add(new JLabel("Source:"));
		pathPanel.add(src);
		pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel.add(new JLabel("Target"));
		pathPanel.add(tgt);
		pathPanel2.add(new JLabel("Execution:"));
		pathPanel2.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel2.add(new JLabel("Repeat"));
		pathPanel2.add(repeat);pathPanel.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel2.add(startcheck);
		pathPanel2.add(stopcheck);
		pathPanel2.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel2.add(new JLabel("_____________________________"));
		pathPanel2.add(Box.createRigidArea(new Dimension(500,10)));
		pathPanel2.add(new JLabel("Result:"));
		pathPanel2.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel2.add(new JLabel("State:"));
		pathPanel2.add(result);
		pathPanel2.add(Box.createRigidArea(new Dimension(500,1)));
		pathPanel2.add(new JLabel("_____________________________"));
		pathPanel2.add(Box.createRigidArea(new Dimension(500,10)));
		pathPanel2.add(close);

		/* Sets the sizes of various components */
		pathPanel.setPreferredSize(new Dimension(280,270));
		pathPanel2.setPreferredSize(new Dimension(210,270));
		automata.setPreferredSize(new Dimension(280,20));
		result.setPreferredSize(new Dimension(150,50));
		path1.setPreferredSize(new Dimension(200,20));
		path2.setPreferredSize(new Dimension(200,20));

		/* Set the background colour of the two combo boxes to white */
		path1.setBackground(Color.WHITE);
		path2.setBackground(Color.WHITE);

		/* Ensure the stop button cannot be clicked initially */
		stopcheck.setEnabled(false);

		/* Add the list of STCA names to the relevant combo box */
		for(int i=0;i<Rules.names.length;i++){
			automata.addItem(Rules.names[i]);
		}
		automata.setSelectedIndex(GlobalAttributes.automata);

		/* Initialise the 2D cell spaces in this class by just initialising
		 * each cell individually, and by setting each cell's internal
		 * coordinates based on its position in the relevant array */
		for (int i=0; i<GlobalAttributes.xCells;i++){
			for (int j=0;j<GlobalAttributes.yCells;j++){
				grid[i][j]=new Cell();
				grid[i][j].xPosition=i;
				grid[i][j].yPosition=j;
				source[i][j]=new Cell();
				source[i][j].xPosition=i;
				source[i][j].yPosition=j;
				target[i][j]=new Cell();
				target[i][j].xPosition=i;
				target[i][j].yPosition=j;
			}
		}

		/* Load the list of configuration files into the two relevant combo boxes */
		loadFiles();

		/* Start the thread object's execution logic - it won't actually do anything
		 * except for yield CPU control until the user begins the tests */
		updater.start();

		/* Add this class as the event listener for various components */
		startcheck.addActionListener(this);
		stopcheck.addActionListener(this);
		load.addActionListener(this);
		close.addActionListener(this);
		automata.addItemListener(this);

		/* Set various window properties and display it */
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	}

	/* Listens the action events - i.e. button clicks */
	@Override
	public void actionPerformed(ActionEvent arg0) {

		/* If the user clicks the Begin button */
		if (arg0.getActionCommand().equals("Begin")){

			/* And both source/initial and target configurations are loaded */
			if(loaded==1){

				/* Enable/disable GUI components appropriately */
				startcheck.setEnabled(false);
				stopcheck.setEnabled(true);
				close.setEnabled(false);
				path1.setEnabled(false);
				path2.setEnabled(false);
				result.setText("N/A");
				automata.setEnabled(false);
				load.setEnabled(false);
				repeat.setEnabled(false);

				/* Wake up the execution updater thread */
				unPause();

				/* Block the GUI until the updater thread has paused */
				while(updater.getState()==Thread.State.WAITING){
					Thread.yield();
				}
			}
		}

		/* If the user clicks the End button */
		else if (arg0.getActionCommand().equals("End")){

			/* Set the global run variable to false, forcing
			 * the execution thread to stop */
			pause();

			/* Block the GUI until the updater thread has paused */
			while(updater.getState()!=Thread.State.WAITING){
				Thread.yield();
			}

			/* Enable/disable GUI components appropriately */
			stopcheck.setEnabled(false);
			startcheck.setEnabled(true);
			close.setEnabled(true);		
			path1.setEnabled(true);
			path2.setEnabled(true);
			automata.setEnabled(true);
			load.setEnabled(true);
			repeat.setEnabled(true);
		}

		/* If the user clicks the Close button */
		else if (arg0.getActionCommand().equals("Close")){

			/* Re-enable the main window and destroy this one */
			MainFrame.instance.setEnabled(true);
			this.dispose();
		}

		/* If the user clicks the Load button */
		else if (arg0.getActionCommand().equals("Load")){

			/* And if valid source and target configurations are both selected */
			if(path1.getSelectedIndex()!=-1 && path2.getSelectedIndex()!=-1){

				/* Load the two configurations into program memory */
				loadEnds(path1.getSelectedItem().toString()+".con",
						path2.getSelectedItem().toString()+".con");
				loaded=1;
				result.setText("N/A");
			}
		}
	}

	/* Loads the two configurations name1, name2 into program memory as
	 * the source and target configurations respectively */
	public void loadEnds(String name1,String name2){

		/* Load the source configuration */

		/* Records the coordinates of the current cell state being read */
		int row=0;
		int column=0;
		try {

			/* Load the file name1 if it actually exists */
			File file = new File(name1);
			if (file.exists()){

				/* Create java reader objects */
				FileReader fileReader = new FileReader(file);
				BufferedReader reader=new BufferedReader(fileReader);

				/* Read the number of annotations stored and then skip that many lines */
				int noOflabels = Integer.parseInt(reader.readLine());
				for (int i=0;i<noOflabels;i++){
					reader.readLine();
					reader.readLine();
					reader.readLine();
				}

				/* While there is still some of the file left to read */
				while(reader.ready()){

					/* Read the next line from the file */
					String temp=reader.readLine();

					/* If the current row contains only a newRow command */
					if(temp.equals("newRow")){

						/* Increment the row */
						row++;

						/* Read and set the cell's state for the leftmost cell of this row */
						source[row][0].topSubcellValue=Integer.parseInt(reader.readLine());
						source[row][0].bottomSubcellValue=Integer.parseInt(reader.readLine());
						source[row][0].leftSubcellValue=Integer.parseInt(reader.readLine());
						source[row][0].rightSubcellValue=Integer.parseInt(reader.readLine());
						column=1;
					}

					/* Else just read the cell's state for the next cell in this row */
					else{
						source[row][column].topSubcellValue=Integer.parseInt(temp);
						source[row][column].bottomSubcellValue=Integer.parseInt(reader.readLine());
						source[row][column].leftSubcellValue=Integer.parseInt(reader.readLine());
						source[row][column].rightSubcellValue=Integer.parseInt(reader.readLine());
						column++;
					}
				}

				/* Close the java reader objects */
				reader.close();
				fileReader.close();
			}

			/* Display the configuration's name in the source configuration label */
			src.setText(name1);
		} catch (Exception e) {
		}		

		/* Load the target configuration */

		/* Records the coordinates of the current cell state being read */
		row=0;
		column=0;
		try {

			/* Load the file name2 if it actually exists */
			File file = new File(name2);
			if (file.exists()){

				/* Create java reader objects */
				FileReader fileReader = new FileReader(file);
				BufferedReader reader=new BufferedReader(fileReader);

				/* Read the number of annotations stored and then skip that many lines */
				int noOflabels = Integer.parseInt(reader.readLine());
				for (int i=0;i<noOflabels;i++){
					reader.readLine();
					reader.readLine();
					reader.readLine();
				}

				/* While there is still some of the file left to read */
				while(reader.ready()){

					/* Read the next line from the file */
					String temp=reader.readLine();

					/* If the current row contains only a newRow command */
					if(temp.equals("newRow")){

						/* Increment the row */
						row++;

						/* Read and set the cell's state for the leftmost cell of this row */
						target[row][0].topSubcellValue=Integer.parseInt(reader.readLine());
						target[row][0].bottomSubcellValue=Integer.parseInt(reader.readLine());
						target[row][0].leftSubcellValue=Integer.parseInt(reader.readLine());
						target[row][0].rightSubcellValue=Integer.parseInt(reader.readLine());
						column=1;
					}

					/* Else just read the cell's state for the next cell in this row */
					else{
						target[row][column].topSubcellValue=Integer.parseInt(temp);
						target[row][column].bottomSubcellValue=Integer.parseInt(reader.readLine());
						target[row][column].leftSubcellValue=Integer.parseInt(reader.readLine());
						target[row][column].rightSubcellValue=Integer.parseInt(reader.readLine());
						column++;
					}
				}

				/* Close the java reader objects */
				reader.close();
				fileReader.close();
			}

			/* Display the configuration's name in the target configuration label */
			tgt.setText(name2);
		} catch (Exception e) {
		}

		/* Calculate the difference matrix and differences counter between the source
		 * and target configurations */
		differences();
	}

	/* Loads the list of configuration file names into the two relevant combo boxes 
	 * It is faster to code this method again but add the ability to add to two boxes,
	 * rather than call the existing GUI class method twice (once for each combo box), as
	 * this would access the filesystem twice */
	@SuppressWarnings("unchecked")
	public void loadFiles(){

		/* Remove all names from the combo boxes to begin with */
		path1.removeAllItems();
		path2.removeAllItems();

		/* List all files in the current working directory */
		File file = new File("./");
		File[] configs =file.listFiles();

		/* For every file */
		for (int i=0;i<configs.length;i++){

			/* Retrieve the name and if it ends with .con extension then add it
			 * to the two combo boxes */
			String name = configs[i].getName();
			if((!(name.length()<4)) && name.substring(name.length()-4).equals(".con")){
				path1.addItem(name.substring(0,name.length()-4));
				path2.addItem(name.substring(0,name.length()-4));
			}
		}
	}

	/* Listens for item events fired by the STCA selection combo box being changed */
	@SuppressWarnings("rawtypes")
	@Override
	public void itemStateChanged(ItemEvent arg0) {

		/* If it is a selection event, and not a de-selection event (both are typically fired
		 * when the combo box is changed) */
		if (arg0.getStateChange() == ItemEvent.SELECTED) {

			/* Set the selected STCA (program-wide for simplicity) */
			GlobalAttributes.automata=((JComboBox)(arg0.getSource())).getSelectedIndex();
			MainFrame.automata.setSelectedIndex(GlobalAttributes.automata);
			MainFrame.cellSpace.repaint();
		}
	}
}
