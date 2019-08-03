import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/* The class which generates the GUI and also listens to GUI events */
@SuppressWarnings("serial")
public class MainFrame extends JFrame implements ActionListener, ItemListener{

	/* JPanel for the set of controls on the right */
	JPanel controlPanel = new JPanel();

	/* Controls the execution of the STCA */
	JButton start = new JButton("Start");
	JButton stop = new JButton("Stop");

	/* Clears the STCA so every cell is in the quiescent state (blank) */
	JButton reset = new JButton("Reset");

	/* Save the current STCA configuration to a file - causes an input prompt to give a name */
	JButton save = new JButton("Save");

	/* Loads the currently selected configuration file (given in the "files" JComboBox) */
	JButton load= new JButton("Load");

	/* Modify the wait time wait time (milliseconds) between 
	 * attempting to apply a transition when executing */
	JTextField speed = new JTextField(3);

	/* Lists all of the saved configuration files */
	@SuppressWarnings("rawtypes")
	JComboBox files = new JComboBox();

	/* Lists all of the available STCA for loading */
	@SuppressWarnings("rawtypes")
	static JComboBox automata = new JComboBox();

	/* Loads the path verification window */
	JButton path = new JButton("Path Verification");

//	/* Local references to global variables for convenience */
	static JFrame instance;
	static CellSpace cellSpace = new CellSpace();

	/* Field for the user to change the random number generator seed value */
	JTextField seed = new JTextField(5);

	/* Loads the rule examination window */
	JButton examine = new JButton("Examine Rules");

	/* Changes the annotation value in GlobalAttributes so that the user 
	 * can add annotations to the cellspace by clicking */
	JButton annotate = new JButton("Add annotation");

	/* Change the annotation value in GlobalAttributes so that the user can 
	 * remove annotations to the cellspace by clicking */
	JButton deAnnotate = new JButton("Remove annotation");

	/* Change the annotation value in GlobalAttributes so that 
	 * the program returns to normal mode */
	JButton exitAnnotate = new JButton("Exit annotation");
	
	/* Displays information about the software */
	JButton about = new JButton("About");

	/* Class constructor - sets various properties of the program window, and also
	 * configures and adds both the cellspace (JPanel with 2D array and rendering logic) 
	 * and the control panel */
	public MainFrame(){
		instance=this;
		this.setTitle("STCA Simulator");
		
		/* Set the size of the cellspace based on the number of cells in the horizontal direction
		 * multiplied by their size, and the same for the vertical direction, then make it visible */
		cellSpace.setPreferredSize(new Dimension(GlobalAttributes.xCells*GlobalAttributes.cellsize,
				GlobalAttributes.yCells*GlobalAttributes.cellsize));
		cellSpace.setVisible(true);

		/* Make the control panel always 200 pixels wide, and the height the same as the cellspace */
		controlPanel.setPreferredSize(new Dimension(300,
				GlobalAttributes.yCells*GlobalAttributes.cellsize));

		/* Create the control panel - separate method as its a big task */
		createControlPanel();

		/* Add the 2 panels to the main window - Cellspace on the left
		 * and control panel on the right */
		getContentPane().setLayout(new FlowLayout());
		getContentPane().add(cellSpace);
		getContentPane().add(controlPanel);

		/* Make the window visible, non-resizable and able to terminate the program */
		setVisible(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();

		/* Invoke an initial cellspace paint */
		cellSpace.repaint();
	}

	/* Create the control panel by adding components to the control panel's JPanel */
	@SuppressWarnings("unchecked")
	private void createControlPanel() {

		/* Set the size and colour of the STCA selection combo box 
		 * and add all of the STCA names */
		automata.setPreferredSize(new Dimension(300,20));
		automata.setBackground(Color.WHITE);
		for(int i=0;i<Rules.names.length;i++){
			automata.addItem(Rules.names[i]);
		}

		/* Set size and colour of saved configuration files combobox */
		files.setBackground(Color.WHITE);
		files.setPreferredSize(new Dimension(300,20));

		/* Add all the components (buttons, textareas, comboboxes), 
		 * labels, and whitespace needed for layout */
		controlPanel.add(new JLabel("Automaton:"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(automata);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(examine);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(new JLabel("_____________________________"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,10)));
		controlPanel.add(new JLabel("Execution:"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(start);
		controlPanel.add(stop);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(reset);
		controlPanel.add(new JLabel("Update speed:"));
		controlPanel.add(speed);
		controlPanel.add(new JLabel("ms"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(new JLabel("Random Seed:"));
		controlPanel.add(seed);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(new JLabel("____________________________"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,10)));
		controlPanel.add(new JLabel("Configurations:"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(files);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(save);
		controlPanel.add(load);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(annotate);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(deAnnotate);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(exitAnnotate);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(reset);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(new JLabel("____________________________"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,10)));
		controlPanel.add(new JLabel("Additional:"));
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(path);
		controlPanel.add(Box.createRigidArea(new Dimension(500,1)));
		controlPanel.add(about);
		
		
		/* Set the initial states of various components */
		stop.setEnabled(false);
		speed.setText(Integer.toString(GlobalAttributes.speed));
		seed.setText(Integer.toString(GlobalAttributes.initialseed));

		/* Set various components event listener as this class */
		start.addActionListener(this);
		stop.addActionListener(this);
		reset.addActionListener(this);
		save.addActionListener(this);
		load.addActionListener(this);
		path.addActionListener(this);
		examine.addActionListener(this);
		annotate.addActionListener(this);
		deAnnotate.addActionListener(this);
		exitAnnotate.addActionListener(this);
		automata.addItemListener(this);
		exitAnnotate.setEnabled(false);
		about.addActionListener(this);

		/* Load the list of saved configuration files into the right combobox */
		loadFiles();
	}

	/* Action listener handling JButton clicks */
	@Override
	public void actionPerformed(ActionEvent arg0) {

		/* If the Start button is clicked */
		if (arg0.getActionCommand().equals("Start")){

			/* Enable and disable certain buttons and components
			 * appropriately which are allowed/disallowed during
			 * STCA execution */
			start.setEnabled(false);
			load.setEnabled(false);
			save.setEnabled(false);
			reset.setEnabled(false);
			automata.setEnabled(false);
			files.setEnabled(false);
			speed.setEnabled(false);
			examine.setEnabled(false);
			path.setEnabled(false);
			annotate.setEnabled(false);
			deAnnotate.setEnabled(false);
			exitAnnotate.setEnabled(false);
			seed.setEnabled(false);

			/* Try to parse a new speed (wait time) value and set it
			 * if successful. Otherwise don't do anything (not even
			 * worth throwing an error) */
			try{
				int newSpeed=Integer.parseInt(speed.getText());
				GlobalAttributes.speed=newSpeed;
			}
			catch(Exception e){
			}

			/* Try to parse a new seed (for the random number generator) 
			 * value and set it if successful. Otherwise don't do 
			 * anything (not even worth throwing an error) */
			try{
				int newseed=Integer.parseInt(seed.getText());
				GlobalAttributes.random.setSeed(newseed);
			}
			catch(Exception e){
			}

			/* Wake up the execution updater thread */
			cellSpace.unPause();

			/* Block the GUI until the updater thread has paused */
			while(cellSpace.updaterThread.getState()==Thread.State.WAITING){
				Thread.yield();
			}

			/* Finally (after everything else is done) make
			 * the stop button to be clickable */
			stop.setEnabled(true);
		}

		/* If the stop button is clicked */
		else if (arg0.getActionCommand().equals("Stop")){

			/* Prevent the button from being clicked again */
			stop.setEnabled(false);

			/* Set the global run variable to false, forcing
			 * the execution thread to stop */
			cellSpace.pause();

			/* Block the GUI until the updater thread has paused */
			while(cellSpace.updaterThread.getState()!=Thread.State.WAITING){
				Thread.yield();
			}

			/* Enable and disable certain buttons and components
			 * appropriately which are allowed/disallowed during
			 * STCA non-execution */
			start.setEnabled(true);
			reset.setEnabled(true);
			load.setEnabled(true);
			save.setEnabled(true);
			reset.setEnabled(true);
			automata.setEnabled(true);
			files.setEnabled(true);
			speed.setEnabled(true);
			examine.setEnabled(true);
			path.setEnabled(true);
			annotate.setEnabled(true);
			deAnnotate.setEnabled(true);
			exitAnnotate.setEnabled(true);
			seed.setEnabled(true);
		}

		/* If the reset button is clicked then signal the cellspace
		 * to wipe all cells */
		else if (arg0.getActionCommand().equals("Reset")){
			cellSpace.clear();
			cellSpace.repaint();
		}

		/* If the save button is clicked */
		else if (arg0.getActionCommand().equals("Save")){

			/* Assume that the inputted name is false */
			boolean valid=false;

			/* Variable for storing the user input string */
			String temp = null;

			try{
				/* Until a good string is input */
				while(valid==false){
	
					/* Show the input dialog and store the user input in temp */
					temp = JOptionPane.showInputDialog("Please enter a filename using alphanumeric characters");
	
					/* Assume that its a valid input */
					valid=true;
	
					/* Iterate through each character and check if it is either a-z, A-Z or 0-9
					 * If any character is not one of these then the name is invalid! and the while
					 * loop repeats */
					for(int i=0;i<temp.length();i++){
						if((temp.charAt(i)<'0' || temp.charAt(i)>'9') &&
								(temp.charAt(i)<'A' || temp.charAt(i)>'Z') &&
								(temp.charAt(i)<'a' || temp.charAt(i)>'z') &&
										temp.charAt(i)!=' '){
							valid=false;
							break;
						}
					}
				}
	
				/* When a valid name is input, signal the cellspace object to save the state of the
				 * cell space to the name followed by .con extension */
				cellSpace.save(temp+".con");
	
				/* Reload the list of configuration files, which will now include the new file */
				loadFiles();
			}
			catch(Exception e){}
		}

		/* If Load is clicked */
		else if (arg0.getActionCommand().equals("Load")){

			/* If a file is actually selected, signal the cellspace to clear the cells, load the selected
			 * file, and then repaint itself in the new configuration */
			if(files.getSelectedIndex()!=-1){
				cellSpace.clear();
				cellSpace.load(files.getSelectedItem().toString()+".con");
				cellSpace.repaint();
			}
		}

		/* If the Path Verification button is clicked */
		else if (arg0.getActionCommand().equals("Path Verification")){

			/* Disable the current window (it will be reenabled by the Path
			 * Verification window when the user is finished), and load
			 * the Path Verification window */
			setEnabled(false);
			@SuppressWarnings("unused")
			PathVerifierFrame path = new PathVerifierFrame();
		}
		
		/* If the About button is clicked */
		else if (arg0.getActionCommand().equals("About")){

			/* Shows a simple message window */
			JOptionPane.showMessageDialog(null,"STCA Simulator by Daniel Morrison\n\n"
					+ "This software is free to be redistributed for academic use. It has been produced "
					+ "in support \nof a degree in Doctor of Philosophy at the University of Leicester, 2016.\n\n"
					+ "For further information please see the README and relevant chapter of the doctoral thesis.");
		}

		/* If the Examine Rules button is clicked */
		else if (arg0.getActionCommand().equals("Examine Rules")){

			/* Disable the current window (it will be reenabled by the Rule
			 * Examination window when the user is finished), and load
			 * the Path Verification window */
			setEnabled(false);
			@SuppressWarnings("unused")
			ExaminerFrame examiner=new ExaminerFrame();
		}

		/* If the add annotation button is clicked, then simply disable all of the controls
		 * except for the exit annotation button, and set the annotation mode of the software
		 * to 1 - the actual annotation functionality is provided by the cellspace object */
		else if (arg0.getActionCommand().equals("Add annotation")){
			start.setEnabled(false);
			load.setEnabled(false);
			save.setEnabled(false);
			reset.setEnabled(false);
			automata.setEnabled(false);
			files.setEnabled(false);
			speed.setEnabled(false);
			examine.setEnabled(false);
			path.setEnabled(false);
			annotate.setEnabled(false);
			deAnnotate.setEnabled(false);
			exitAnnotate.setEnabled(true);
			GlobalAttributes.annotate=1;
		}

		/* If the remove annotation button is clicked, then simply disable all of the controls
		 * except for the exit annotation button, and set the annotation mode of the software
		 * to 2 - the actual annotation functionality is provided by the cellspace object */
		else if (arg0.getActionCommand().equals("Remove annotation")){
			start.setEnabled(false);
			load.setEnabled(false);
			save.setEnabled(false);
			reset.setEnabled(false);
			automata.setEnabled(false);
			files.setEnabled(false);
			speed.setEnabled(false);
			examine.setEnabled(false);
			path.setEnabled(false);
			annotate.setEnabled(false);
			deAnnotate.setEnabled(false);
			exitAnnotate.setEnabled(true);
			GlobalAttributes.annotate=2;
		}

		/* If the exit annotation button is clicked, then reenable all of the default
		 * controls, signal to the cellspace to remove any potential typing cursor which
		 * may be active, set the annotation state of the software back to 0,
		 * and ask the cellspace to repaint itself */
		else if (arg0.getActionCommand().equals("Exit annotation")){
			start.setEnabled(true);
			load.setEnabled(true);
			save.setEnabled(true);
			reset.setEnabled(true);
			automata.setEnabled(true);
			files.setEnabled(true);
			speed.setEnabled(true);
			examine.setEnabled(true);
			path.setEnabled(true);
			annotate.setEnabled(true);
			deAnnotate.setEnabled(true);
			exitAnnotate.setEnabled(false);
			GlobalAttributes.annotate=0;
			cellSpace.endTyping();
			cellSpace.repaint();
		}
	}

	/* Loads the list of names of all configuration files found in the program folder
	 * and adds them to the files combobox */
	@SuppressWarnings("unchecked")
	public void loadFiles(){

		/* Current path is the same as the java class file (ASSUMES that the java
		 * command is run while the working folder is the same as the software's) */
		String s ="./";

		/* Clear the list of file names */
		files.removeAllItems();

		/* Retrieve the list of all files in this folder */
		File file = new File(s);
		File[] configs =file.listFiles();

		/* For every file in this folder, if it ends with ".con" extension, add it
		 * to the list of configurations in the file combobox */
		for (int i=0;i<configs.length;i++){
			String name = configs[i].getName();
			if((!(name.length()<4)) && name.substring(name.length()-4).equals(".con")){
				files.addItem(name.substring(0,name.length()-4));
			}
		}
	}

	/* Item listener which only checks if a new STCA has been selected */
	@SuppressWarnings("rawtypes")
	@Override
	public void itemStateChanged(ItemEvent arg0) {
		if (arg0.getStateChange() == ItemEvent.SELECTED) {

			/* Set the current STCA to the selected one, software-wide, and repaint the cellspace
			 * (in case no. of states has changed and this results in new colours) */
			GlobalAttributes.automata=((JComboBox)(arg0.getSource())).getSelectedIndex();
			cellSpace.repaint();
		}
	}

}
