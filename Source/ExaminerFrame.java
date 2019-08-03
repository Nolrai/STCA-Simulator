import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

/* This class represents the window shown when Examine Rules is clicked in the main window.
 * It contains GUI logic, as well as the underlying "check determinism" feature.
 * This checks the set of rules for a given STCA and calculate whether they are locally deterministic and
 * locally reversible. It will also graphically depict the STCA rules (with the illustrations generated on-the-fly!
 * rather than being stored as images or hardcoded), via the ExaminerPanel class. It will also tell the user if the rules are rotation
 * or reflection-symmetric (information that IS hardcoded with the rules, in order to minimise the number of rules
 * that have to be stored) - The class also contains event listener logic for the GUI items */
@SuppressWarnings("serial")
public class ExaminerFrame extends JFrame implements ActionListener, ItemListener{

	/* Allows the rule illustration panel to be scrollable */
	JScrollPane scrollPane;

	/* The actual rule illustration panel, itself a JPanel which contains rendering logic for the rules */
	ExaminerPanel ExaminerPanel = new ExaminerPanel();

	/* Allows the selection of the STCA */
	@SuppressWarnings("rawtypes")
	JComboBox automata = new JComboBox();

	/* Clicking allows the user to check if the rules are locally deterministic and locally reversible */
	JButton check = new JButton("Check Determinism");

	/* Labels for displaying whether the rules are rotation-symmetric, reflection-symmetric,
	 * locally deterministic and locally reversible */
	JLabel rotation=new JLabel();
	JLabel reflection=new JLabel();
	JLabel forwardresult = new JLabel("N/A");
	JLabel backwardsresult = new JLabel("N/A");

	/* Closes this window */
	JButton close = new JButton("Close");

	/* Constructor class which basically sets the window properties, adds various GUI items,
	 * and sets the event listener for various objects (which is itself this class) */
	@SuppressWarnings("unchecked")
	public ExaminerFrame(){

		/* Creates a JPanel and adds it to the window contents.
		 * This is used to make layout easier. */
		JPanel pathPanel = new JPanel();
		this.getContentPane().add(pathPanel);

		/* Add all the components (buttons, textareas, comboboxes), 
		 * labels, and whitespace needed for layout to the JPanel */
		pathPanel.add(new JLabel("Automaton:"));
		pathPanel.add(Box.createRigidArea(new Dimension(1100,1)));
		pathPanel.add(automata);
		pathPanel.add(check);
		pathPanel.add(Box.createRigidArea(new Dimension(1100,1)));
		pathPanel.add(new JLabel("Rotation-symmetric:"));
		pathPanel.add(rotation);
		pathPanel.add(new JLabel("Reflection-symmetric:"));
		pathPanel.add(reflection);
		pathPanel.add(new JLabel("Forwards deterministic:"));
		pathPanel.add(forwardresult);
		pathPanel.add(new JLabel("Backwards deterministic:"));
		pathPanel.add(backwardsresult);
		pathPanel.add(Box.createRigidArea(new Dimension(1100,1)));
		pathPanel.add(new JLabel("___________________________________"
				+ "__________________________________________________"
				+ "__________________________________________________"
				+ "__________________"));
		pathPanel.add(Box.createRigidArea(new Dimension(1100,10)));
		scrollPane = new JScrollPane(ExaminerPanel);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pathPanel.add(scrollPane);
		pathPanel.add(new JLabel("___________________________________"
				+ "__________________________________________________"
				+ "__________________________________________________"
				+ "__________________"));
		pathPanel.add(Box.createRigidArea(new Dimension(1100,10)));
		pathPanel.add(close);

		/* Sets the sizes of various components */
		rotation.setPreferredSize(new Dimension(80,10));
		pathPanel.setPreferredSize(new Dimension(1100,600));
		automata.setPreferredSize(new Dimension(500,20));
		reflection.setPreferredSize(new Dimension(170,20));
		forwardresult.setPreferredSize(new Dimension(80,10));
		backwardsresult.setPreferredSize(new Dimension(80,10));
		ExaminerPanel.setPreferredSize(new Dimension(1100,2000));	
		scrollPane.setPreferredSize(new Dimension(1100,380));

		/* Add the list of STCA names to the relevant combo box */
		for(int i=0;i<Rules.names.length;i++){
			automata.addItem(Rules.names[i]);
		}
		automata.setSelectedIndex(GlobalAttributes.automata);

		/* Checks the current STCA's rules and sees if it is rotation or reflection-symmetric
		 * (via hard-coded flags) - displaying the result */
		checkProperties();

		/* Sets the size of the rule panel depending on the current STCA's number of rules */
		resizePanel();

		/* Paints the rules panel */
		ExaminerPanel.repaint();

		/* Add this class as the event listener for various components */
		close.addActionListener(this);
		automata.addItemListener(this);
		check.addActionListener(this);

		/* Set various window properties and display it */
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.setResizable(false);
		this.pack();
		this.setVisible(true);
	}

	/* Listens the action events - i.e. button clicks */
	@Override
	public void actionPerformed(ActionEvent arg0) {

		/* If the user clicks the Close button */
		if (arg0.getActionCommand().equals("Close")){

			/* Re-enable the main window and destroy this one */
			MainFrame.instance.setEnabled(true);
			this.dispose();
		}

		/* If the user clicks the Check Determinism button */
		if (arg0.getActionCommand().equals("Check Determinism")){

			/* Run the check */
			checkDeterminism();
		}
	}

	/* Listens for item events fired by the STCA selection combo box being changed */
	@SuppressWarnings({ "static-access", "rawtypes" })
	@Override
	public void itemStateChanged(ItemEvent arg0) {

		/* If it is a selection event, and not a de-selection event (both are typically fired
		 * when the combo box is changed) */
		if (arg0.getStateChange() == arg0.SELECTED) {

			/* Set the selected STCA (program-wide for simplicity) */
			GlobalAttributes.automata=((JComboBox)(arg0.getSource())).getSelectedIndex();
			MainFrame.automata.setSelectedIndex(GlobalAttributes.automata);
			MainFrame.cellSpace.repaint();

			/* Checks the current STCA's rules and sees if it is rotation or reflection-symmetric
			 * (via hard-coded flags) - displaying the result */
			checkProperties();

			/* Reset the labels for local determinism/reversibility */
			forwardresult.setText("N/A");
			backwardsresult.setText("N/A");

			/* Resize the rules panel based on the new STCA's number of rules  */
			resizePanel();

			/* Display the new rules */
			ExaminerPanel.repaint();
		}
	}

	/* Sets the size of the rules panel based on the selected STCA's number of rules */
	private void resizePanel() {

		/* Retrieve the number of rules */
		int noOfRules = Rules.rules[GlobalAttributes.automata].length/16;

		/* Height is calculated based on the number of rules */
		int totalheight= ((noOfRules/3)+5)*GlobalAttributes.cellsize*3;

		/* The width is fixed but the height is calculated (above) */
		ExaminerPanel.setPreferredSize(new Dimension(1100,totalheight));
		scrollPane.revalidate();
	}

	/* Checks rotation and reflection-symmetry properties of the current rules (hard-coded in the rules' array)
	 * and displays them in the appropriate GUI labels */
	public void checkProperties(){
		if(Rules.rules[GlobalAttributes.automata][0]==1){
			rotation.setText("Yes");
		}
		else{
			rotation.setText("No");
		}
		if(Rules.rules[GlobalAttributes.automata][1]==0){
			reflection.setText("No");
		}
		else if(Rules.rules[GlobalAttributes.automata][1]==1){
			reflection.setText("Horizontal");
		}
		else if(Rules.rules[GlobalAttributes.automata][1]==2){
			reflection.setText("Vertical");
		}	
		else if(Rules.rules[GlobalAttributes.automata][1]==3){
			reflection.setText("Horizontal and Vertical");
		}
		else if(Rules.rules[GlobalAttributes.automata][1]==4){
			reflection.setText("Horizontal and Vertical compounded");
		}
	}

	/* Checks local determinism and local reversibility of the current rules via on-the-fly calculation
	 * (performed in the called method with true for forwards determinism, false for backwards determinism) 
	 * and displays them in the appropriate GUI labels */
	public void checkDeterminism() {
		if(isDeterministic(true)){
			forwardresult.setText("Yes");
		}
		else{
			forwardresult.setText("No");
		}
		if(isDeterministic(false)){
			backwardsresult.setText("Yes");
		}
		else{
			backwardsresult.setText("No");
		}
	}

	/* Calculates whether the rules are locally deterministic */
	public boolean isDeterministic(boolean forwards){

		/* Assumes they are locally deterministic until falsified */
		boolean deterministic=true;

		/* For every rule in the STCA */
		for(int i=0;i<(Rules.rules[GlobalAttributes.automata].length)/16;i++){

			int top1,bottom1,left1,right1,ntop1,nbottom1,nleft1,nright1;
			
			/* If we are checking forwards determinism */
			if(forwards){
				
				/* Retrieve the domain of the rule in the form of its subcell states */
				top1=Rules.rules[GlobalAttributes.automata][i*16+2];
				bottom1=Rules.rules[GlobalAttributes.automata][i*16+3];
				left1=Rules.rules[GlobalAttributes.automata][i*16+4];
				right1=Rules.rules[GlobalAttributes.automata][i*16+5];
				ntop1=Rules.rules[GlobalAttributes.automata][i*16+6];
				nbottom1=Rules.rules[GlobalAttributes.automata][i*16+7];
				nleft1=Rules.rules[GlobalAttributes.automata][i*16+8];
				nright1=Rules.rules[GlobalAttributes.automata][i*16+9];
			}
			
			/* If we are checking backwards determinism (reversibility) */
			else{
				
				/* Retrieve the codomain of the rule in the form of its subcell states */
				top1=Rules.rules[GlobalAttributes.automata][i*16+10];
				bottom1=Rules.rules[GlobalAttributes.automata][i*16+11];
				left1=Rules.rules[GlobalAttributes.automata][i*16+12];
				right1=Rules.rules[GlobalAttributes.automata][i*16+13];
				ntop1=Rules.rules[GlobalAttributes.automata][i*16+14];
				nbottom1=Rules.rules[GlobalAttributes.automata][i*16+15];
				nleft1=Rules.rules[GlobalAttributes.automata][i*16+16];
				nright1=Rules.rules[GlobalAttributes.automata][i*16+17];
			}

			/* For every rule after it in the list - including itself (avoids double checking) */
			for(int j=i;j<(Rules.rules[GlobalAttributes.automata].length)/16;j++){

				/* If STCA is not rotation-symmetric and not reflection-symmetric, then avoid
				 * comparing rule against itself completely - otherwise if it is either rotation-symmetric
				 * or reflection-symmetric, then continue and compare against its own rotations/reflections */
				if(!(Rules.rules[GlobalAttributes.automata][0]==0 && 
						Rules.rules[GlobalAttributes.automata][1]==0 && j==i)){

					int top2,bottom2,left2,right2,ntop2,nbottom2,nleft2,nright2;
					
					
					/* If we are checking forwards determinism */
					if(forwards){
						
						/* Retrieve domain of the 2nd rule in the form of its subcell states */
						top2=Rules.rules[GlobalAttributes.automata][j*16+2];
						bottom2=Rules.rules[GlobalAttributes.automata][j*16+3];
						left2=Rules.rules[GlobalAttributes.automata][j*16+4];
						right2=Rules.rules[GlobalAttributes.automata][j*16+5];
						ntop2=Rules.rules[GlobalAttributes.automata][j*16+6];
						nbottom2=Rules.rules[GlobalAttributes.automata][j*16+7];
						nleft2=Rules.rules[GlobalAttributes.automata][j*16+8];
						nright2=Rules.rules[GlobalAttributes.automata][j*16+9];

					}
					
					/* If we are checking backwards determinism (reversibility)  */
					else{
						
						/* Retrieve the codomain of the rule in the form of its subcell states */
						top2=Rules.rules[GlobalAttributes.automata][j*16+10];
						bottom2=Rules.rules[GlobalAttributes.automata][j*16+11];
						left2=Rules.rules[GlobalAttributes.automata][j*16+12];
						right2=Rules.rules[GlobalAttributes.automata][j*16+13];
						ntop2=Rules.rules[GlobalAttributes.automata][j*16+14];
						nbottom2=Rules.rules[GlobalAttributes.automata][j*16+15];
						nleft2=Rules.rules[GlobalAttributes.automata][j*16+16];
						nright2=Rules.rules[GlobalAttributes.automata][j*16+17];
					}
						
					/* Calculate how many different times to rotate */
					int rotations=0;
					if(Rules.rules[GlobalAttributes.automata][0]==1){
						rotations=3;
					}

					/* Set reflection type based on stored value */
					int reflect=0;
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
						reflectTimes=3;  /* If rotation symmetric then this is not needed */
					}

					/* Use this to store the subcells' states after rotations */
					int[] subcells;

					/* For each possible reflection of the 2nd rule (including none) */
					for(int r=0;r<=reflectTimes;r++){

						/* If there are no rotations, avoid non-reflective
						 * comparison of the rule against itself (when j=i)
						 *  and skip to the next iteration - if there is such
						 *  a subsequent iteration (when reflectTimes>0) */
						if(j==i && rotations==0 && r==0 && reflectTimes>0){
							r++;
						}

						/* If we are currently reflecting the 2nd rule
						 * (so every time we visit this after the 0th iteration of this for-loop) */
						if(r>0){

							/* This decides whether to reflect horizontally */
							if(reflect==1 /* We are reflecting only horizontally, so we must be doing so now */
									|| (reflect==3 && r==1) /* Reflecting both but not together, so only do it
															the first time arriving here */
									|| (reflect==4 && r!=2) /* Reflecting both separately AND together, so do it, the first
															and third times we are here */){

								/* Reflect the 2nd rule's subcells horizontally */
								subcells=ApplyRule.reflect(top2,bottom2,left2,right2,
										ntop2,nbottom2,nleft2,nright2,1);
								top2=subcells[0];
								bottom2=subcells[1];
								left2=subcells[2];
								right2=subcells[3];
								ntop2=subcells[4];
								nbottom2=subcells[5];
								nleft2=subcells[6];
								nright2=subcells[7];
							}

							/* This decides whether to reflect vertically */
							if(reflect==2 /* We are are reflecting only vertically, so we must be doing so now */
									|| (reflect==3 && r==2) /* Reflecting both but not together, so only do it
															the second time arriving here */
									|| (reflect==4 && r!=1) /* Reflecting both separately AND together, so do it the second
															and third times we are here */){

								/* Reflect the 2nd rule's subcells vertically */
								subcells=ApplyRule.reflect(top2,bottom2,left2,right2,
										ntop2,nbottom2,nleft2,nright2,2);
								top2=subcells[0];
								bottom2=subcells[1];
								left2=subcells[2];
								right2=subcells[3];
								ntop2=subcells[4];
								nbottom2=subcells[5];
								nleft2=subcells[6];
								nright2=subcells[7];
							}
						}

						/* For each rotation position - including the initial (which is also done when not rotating) */
						for(int k=0; k<=rotations;k++){

							/* If we have not reflected, avoid non-rotated
							 * comparison against itself and skip to the next rotation 
							 * (if rotations are specified) */
							if(j==i && r==0 && k==0 && rotations>0){
								k++;
							}

							/* Rotate the cells by k lots of 90 degrees */
							if(k>0){
								subcells=ApplyRule.rotate(top2,bottom2,left2,right2,
										ntop2,nbottom2,nleft2,nright2,k);
								top2=subcells[0];
								bottom2=subcells[1];
								left2=subcells[2];
								right2=subcells[3];
								ntop2=subcells[4];
								nbottom2=subcells[5];
								nleft2=subcells[6];
								nright2=subcells[7];
							}
							int[] rule1={top1,bottom1,left1,right1,ntop1,nbottom1,nleft1,nright1};
							int[] rule2={top2,bottom2,left2,right2,ntop2,nbottom2,nleft2,nright2};

							/* Only check rule1 against rule2 if the rules are different or rule 2
							 * is a rotation/reflection of rule 1 */
							if(j!=i || r>0 || k>0){

								/* If the rules match (left hand side if forwards=true, right hand side if forwards=false */
								if(matches(rule1,rule2)){

									/* We now need to check if the opposite side also matches - if they do, then it just means
									 * the same rule as showed up more than once (due to reflection and rotation combinations), if
									 * they do not then forwards or backwards determinism (depending on how the function was called)
									 * has been broken  */

									int rtop1,rbottom1,rleft1,rright1,rntop1,rnbottom1,rnleft1,rnright1;
									
									/* If we are checking forwards determinism */
									if(forwards){
										
										/* Retrieve the CODOMAIN this time of the first rule in the form of its subcell states */
										rtop1=Rules.rules[GlobalAttributes.automata][i*16+10];
										rbottom1=Rules.rules[GlobalAttributes.automata][i*16+11];
										rleft1=Rules.rules[GlobalAttributes.automata][i*16+12];
										rright1=Rules.rules[GlobalAttributes.automata][i*16+13];
										rntop1=Rules.rules[GlobalAttributes.automata][i*16+14];
										rnbottom1=Rules.rules[GlobalAttributes.automata][i*16+15];
										rnleft1=Rules.rules[GlobalAttributes.automata][i*16+16];
										rnright1=Rules.rules[GlobalAttributes.automata][i*16+17];
									}
									
									/* If we are checking backwards determinism (reversibility) */
									else{
										
										/* Retrieve the DOMAIN this time of the first rule in the form of its subcell states */
										rtop1=Rules.rules[GlobalAttributes.automata][i*16+2];
										rbottom1=Rules.rules[GlobalAttributes.automata][i*16+3];
										rleft1=Rules.rules[GlobalAttributes.automata][i*16+4];
										rright1=Rules.rules[GlobalAttributes.automata][i*16+5];
										rntop1=Rules.rules[GlobalAttributes.automata][i*16+6];
										rnbottom1=Rules.rules[GlobalAttributes.automata][i*16+7];
										rnleft1=Rules.rules[GlobalAttributes.automata][i*16+8];
										rnright1=Rules.rules[GlobalAttributes.automata][i*16+9];
									}
									int[] rules3={rtop1,rbottom1,rleft1,rright1,rntop1,rnbottom1,rnleft1,rnright1};
									int rtop2,rbottom2,rleft2,rright2,rntop2,rnbottom2,rnleft2,rnright2;
									
									/* If we are checking forwards determinism */
									if(forwards){
										
										/* Retrieve the CODOMAIN this time of the second rule in the form of its subcell states */
										rtop2=Rules.rules[GlobalAttributes.automata][j*16+10];
										rbottom2=Rules.rules[GlobalAttributes.automata][j*16+11];
										rleft2=Rules.rules[GlobalAttributes.automata][j*16+12];
										rright2=Rules.rules[GlobalAttributes.automata][j*16+13];
										rntop2=Rules.rules[GlobalAttributes.automata][j*16+14];
										rnbottom2=Rules.rules[GlobalAttributes.automata][j*16+15];
										rnleft2=Rules.rules[GlobalAttributes.automata][j*16+16];
										rnright2=Rules.rules[GlobalAttributes.automata][j*16+17];
									}
									
									/* If we are checking backwards determinism (reversibility) */
									else{
										
										/* Retrieve the DOMAIN this time of the second rule in the form of its subcell states */
										rtop2=Rules.rules[GlobalAttributes.automata][j*16+2];
										rbottom2=Rules.rules[GlobalAttributes.automata][j*16+3];
										rleft2=Rules.rules[GlobalAttributes.automata][j*16+4];
										rright2=Rules.rules[GlobalAttributes.automata][j*16+5];
										rntop2=Rules.rules[GlobalAttributes.automata][j*16+6];
										rnbottom2=Rules.rules[GlobalAttributes.automata][j*16+7];
										rnleft2=Rules.rules[GlobalAttributes.automata][j*16+8];
										rnright2=Rules.rules[GlobalAttributes.automata][j*16+9];
									}
									
									/* If we are currently reflecting the 2nd rule */
									if(r>0){
										
										/* This decides whether to reflect horizontally */
										if(reflect==1 /* We are reflecting only horizontally, so we must be doing so now */
												|| (reflect==3 && r==1) /* Reflecting both but not together, so only do it
																		the first time arriving here */
												|| (reflect==4 && r!=2) /* Reflecting both separately AND together, so do it, the first
																		and third times we are here */){
											
											/* Reflect the 2nd rule's subcells horizontally */
											subcells=ApplyRule.reflect(rtop2,rbottom2,rleft2,rright2,
													rntop2,rnbottom2,rnleft2,rnright2,1);
											rtop2=subcells[0];
											rbottom2=subcells[1];
											rleft2=subcells[2];
											rright2=subcells[3];
											rntop2=subcells[4];
											rnbottom2=subcells[5];
											rnleft2=subcells[6];
											rnright2=subcells[7];
										}
										
										/* This decides whether to reflect vertically */
										if(reflect==2 /* We are are reflecting only vertically, so we must be doing so now */
												|| (reflect==3 && r==2) /* Reflecting both but not together, so only do it
																		the second time arriving here */
												|| (reflect==4 && r!=1) /* Reflecting both separately AND together, so do it the second
																		and third times we are here */){

											/* Reflect the 2nd rule's subcells vertically */
											subcells=ApplyRule.reflect(rtop2,rbottom2,rleft2,rright2,
													rntop2,rnbottom2,rnleft2,rnright2,2);
											rtop2=subcells[0];
											rbottom2=subcells[1];
											rleft2=subcells[2];
											rright2=subcells[3];
											rntop2=subcells[4];
											rnbottom2=subcells[5];
											rnleft2=subcells[6];
											rnright2=subcells[7];
										}
									}

									/* Rotate the 2nd rules' subcells by k lots of 90 degrees (to synchronise it with the first rule) */
									if(k>0){
										subcells=ApplyRule.rotate(rtop2,rbottom2,rleft2,rright2,
												rntop2,rnbottom2,rnleft2,rnright2,k);
										rtop2=subcells[0];
										rbottom2=subcells[1];
										rleft2=subcells[2];
										rright2=subcells[3];
										rntop2=subcells[4];
										rnbottom2=subcells[5];
										rnleft2=subcells[6];
										rnright2=subcells[7];
									}
									int[] rules4={rtop2,rbottom2,rleft2,rright2,
											rntop2,rnbottom2,rnleft2,rnright2};
									
									/* If the resulting sets of cells don't match (codomains if checking forwards determinism,
									 * domains if checking backward determinism), then the rules are different DESPITE sharing
									 * domains when forwards, codomains when backwards, and thus local determinism or local reversibility
									 * is broken  */
									if(!matches(rules3,rules4)){
										deterministic=false;
										break;
									}
								}

								/* If the second rule has been rotated at all, then rotate
								 * it back to the initial orientation (so in future loops
								 * we can apply new reflections/rotations appropriately)  */
								if(k>0){
									subcells=ApplyRule.rotate(top2,bottom2,left2,right2,
											ntop2,nbottom2,nleft2,nright2,4-k);
									top2=subcells[0];
									bottom2=subcells[1];
									left2=subcells[2];
									right2=subcells[3];
									ntop2=subcells[4];
									nbottom2=subcells[5];
									nleft2=subcells[6];
									nright2=subcells[7];
								}
							}
						}

						/* If the latest rules to be checked are not forwards deterministic or backwards
						 * deterministic (depending on how the function was called) then prematurely 
						 * break out of the check  */
						if(!deterministic){
							break;
						}
						/* Undo reflection of 2nd rule if one is applied so it is ready for next fresh reflection loop */
						else 				
							
							/* If we are currently reflecting the 2nd rule */
							if(r>0){
								
								/* This decides whether to reflect horizontally */
								if(reflect==1 /* We are reflecting only horizontally, so we must be doing so now */
										|| (reflect==3 && r==1) /* Reflecting both but not together, so only do it
																the first time arriving here */
										|| (reflect==4 && r!=2) /* Reflecting both separately AND together, so do it, the first
																and third times we are here */){
									
									/* Reflect the 2nd rule's subcells horizontally */
									subcells=ApplyRule.reflect(top2,bottom2,left2,right2,
											ntop2,nbottom2,nleft2,nright2,1);
									top2=subcells[0];
									bottom2=subcells[1];
									left2=subcells[2];
									right2=subcells[3];
									ntop2=subcells[4];
									nbottom2=subcells[5];
									nleft2=subcells[6];
									nright2=subcells[7];
								}
								
								/* This decides whether to reflect vertically */
								if(reflect==2 /* We are are reflecting only vertically, so we must be doing so now */
										|| (reflect==3 && r==2) /* Reflecting both but not together, so only do it
																the second time arriving here */
										|| (reflect==4 && r!=1) /* Reflecting both separately AND together, so do it the second
																and third times we are here */){

									/* Reflect the 2nd rule's subcells vertically */
									subcells=ApplyRule.reflect(top2,bottom2,left2,right2,
											ntop2,nbottom2,nleft2,nright2,2);
									top2=subcells[0];
									bottom2=subcells[1];
									left2=subcells[2];
									right2=subcells[3];
									ntop2=subcells[4];
									nbottom2=subcells[5];
									nleft2=subcells[6];
									nright2=subcells[7];
								}
							}
					}
					
					/* If the latest rules to be checked are not forwards deterministic or backwards
					 * deterministic (depending on how the function was called) then prematurely 
					 * break out of the check  */
					if(!deterministic){
						break;
					}

				}
				
				/* If the latest rules to be checked are not forwards deterministic or backwards
				 * deterministic (depending on how the function was called) then prematurely 
				 * break out of the check  */
			} if(!deterministic){
				break;
			}
		}

		/* Return the final result of forward determinism or backwards determinism */
		return deterministic;
	}

	/* Checks whether two neighbourhoods are equal, where neighbourhoods are represented by
	 * integer arrays of length 8. Each component represents one of the 8 subcells in the neighbourhood
	 * and contains an integer representing that subcell's state. Neighbourhoods are equal if all 8
	 * subcells are equal between the two */
	public boolean matches(int[] rule1, int[] rule2){
		if(rule1[0]==rule2[0] && rule1[1]==rule2[1] &&
				rule1[2]==rule2[2] && rule1[3]==rule2[3] &&
				rule1[4]==rule2[4] && rule1[5]==rule2[5] &&
				rule1[6]==rule2[6] && rule1[7]==rule2[7]){
			return true;
		}
		else{
			return false;
		}
	}
}
