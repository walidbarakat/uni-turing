/* TuringMain: Coordinates GUI components, events, etc.
 *             with logical aspects of TM (Machine, Tape, etc.)
 */

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.border.*;
import java.beans.*;
import java.util.*;
import java.util.jar.*;
import java.net.*;

import java.lang.reflect.Field;

public class TuringMain extends JFrame {
    
    static final int MIN_SPEED = 0;
    static final int MAX_SPEED = 100;
    static final int INIT_SPEED = 50;
    
    static Box mainBox = new Box(BoxLayout.Y_AXIS);
    static TuringTapeArea tapeArea = new TuringTapeArea();
    static TuringMachineArea machineArea = new TuringMachineArea(); 
    
    static JComboBox tapeSelector;
    static JLabel tapeLabel = new JLabel("Input:  ");
    static JPanel inputPanel = new JPanel(); 

    static JMenuBar menuBar    = new JMenuBar();
    static JMenu fileMenu      = new JMenu("File");
    static JMenuItem openItem  = new JMenuItem("Load Machine...   ");
    static JMenuItem quitItem  = new JMenuItem("Quit");
    static JMenu controlMenu   = new JMenu("Controls");
    static JMenuItem runItem   = new JMenuItem("Run/Pause");
    static JMenuItem backItem  = new JMenuItem("Step Back");
    static JMenuItem forwItem  = new JMenuItem("Step Forward");
    static JMenuItem resetItem = new JMenuItem("Reset");
    static JMenuItem jumpItem  = new JMenuItem("Jump To Step...   ");
    static JMenu animationMenu = new JMenu("Animation");
    static ButtonGroup tapeGroup        = new ButtonGroup();
    static JRadioButtonMenuItem animOn  = new JRadioButtonMenuItem("Animation On");
    static JRadioButtonMenuItem animOff = new JRadioButtonMenuItem("Animation Off");
    
    
    static JToolBar controlBar = new JToolBar(JToolBar.HORIZONTAL);		
    static JButton runButton;
    static JButton backButton;
    static JButton stepButton; 
    static JButton resetButton;
    static ImageIcon runIcon;
    static ImageIcon pauseIcon;
    static ImageIcon backIcon;
    static ImageIcon stepIcon;
    static ImageIcon resetIcon;
    static JLabel speedLabel = new JLabel("  Speed:"); 
    static JSlider speedSlider = new JSlider(JSlider.HORIZONTAL, MIN_SPEED, 
					     MAX_SPEED, INIT_SPEED);
    
    public static JTextArea statusBar = new JTextArea();
    
    
    public static javax.swing.Timer stepTimer;
    public static javax.swing.Timer animationTimer;
 
    static TuringIOProcessor iop = new TuringIOProcessor();
    
    static JFileChooser machineChooser;
    static JumpDialog jumpDialog;

    static File filename;

    static Machine machine;
    static Tape tape;
    static Tape originalTape = new Tape("");
    static Vector<JarEntry> machineVector;
    static Vector tapeVector;
    
    public static int delay;
    static boolean frozen = true;
    static int halted = Machine.NOT_HALTED;	
    static boolean componentsLoaded = false;
    public static boolean animationOn = true;
    public static boolean animationInitialized = false;

    static final int FADE_IN = 0;
    static final int FADE_OUT = 1;
    static final int GROW_TAPE_LEFT = 2;
    static final int GROW_TAPE_RIGHT = 3;
    static int fade = FADE_OUT;
    static int oldTapeLength;

    static String statusString = "Ready";
    
    static JarFile jf;
    
    public TuringMain(String s) {
	super(s);
	setForeground(new Color(200,200,200));
	setBackground(new Color(200,200,200));
    }
    
    public void runProgram() {
       	setSize(new Dimension(900, 700));
	if(!componentsLoaded) {
	    try {
		jf = getJarFile();
                System.out.println("jf = " + jf);
		machineVector = getMachines();
	    } catch(MalformedURLException e) {
		e.printStackTrace();
		return;
	    } catch(IOException e) {
		e.printStackTrace();
		return;
	    }
	    

	    machineChooser = new JFileChooser();
	    machineChooser.addChoosableFileFilter(new TuringFilter()); 
	    machineChooser.setAccessory(new TuringDescription(machineChooser));
	    machineChooser.setFileView(new TuringFileView());
	    machineChooser.setMultiSelectionEnabled(false);
	    machineChooser.setDialogTitle("Load Machine");
	    machineChooser.setPreferredSize(new Dimension((int)(getWidth() * 0.9), 500));
       	    tapeSelector = new JComboBox();
	    tapeSelector.addActionListener(new TapeSelectorListener());
	    tapeSelector.setEnabled(false);
	    
	    jumpDialog = new JumpDialog(this, "Jump To Step...");
	    jumpDialog.addWindowListener(new JumpWindowListener());

	    setJMenuBar(menuBar);
	    menuBar.add(fileMenu);
	    fileMenu.add(openItem);
	    fileMenu.add(quitItem);
	    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    openItem.addActionListener(new GetMachinesMenuItemListener());
	    quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    quitItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    stepTimer.stop();
		    dispose();
		}
	    });

	    
	    menuBar.add(controlMenu);
	    controlMenu.add(runItem);
	    controlMenu.add(backItem);
	    controlMenu.add(forwItem);
	    controlMenu.add(resetItem);
	    controlMenu.add(jumpItem);
	    runItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    runItem.addActionListener(new RunButtonListener());
	    backItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    backItem.addActionListener(new BackButtonListener());
	    forwItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    forwItem.addActionListener(new StepButtonListener());
	    resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    resetItem.addActionListener(new ResetButtonListener());
	    
	    jumpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
	    jumpItem.addActionListener(new JumpItemListener());

	    menuBar.add(animationMenu);
	    tapeGroup.add(animOn);
	    tapeGroup.add(animOff);
	    animationMenu.add(animOn);
	    animationMenu.add(animOff);
	    
	    animOn.addActionListener(new ActionListener () {
		public void actionPerformed(ActionEvent e) {
		    animationOn = true;
		}
	    });
	    
	    animOff.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    animationOn = false;
		}
	    });
	    
	    animOn.setSelected(true);

//	    menuBar.setForeground(new Color(200,200,200));
//	    menuBar.setBackground(new Color(200,200,200));
	    
	    runIcon   = new ImageIcon(getClass().getResource("Play24.gif"));
	    pauseIcon = new ImageIcon(getClass().getResource("Pause24.gif"));
	    backIcon  = new ImageIcon(getClass().getResource("StepBack24.gif"));
	    stepIcon  = new ImageIcon(getClass().getResource("StepForward24.gif"));
	    resetIcon = new ImageIcon(getClass().getResource("Stop24.gif"));
	    
	    runButton = new JButton(runIcon);
	    runButton.addActionListener(new RunButtonListener());
	    runButton.setToolTipText("Run Machine");
	    
	    backButton = new JButton(backIcon);
	    backButton.addActionListener(new BackButtonListener());
	    backButton.setToolTipText("Step Back");
	    
	    stepButton = new JButton(stepIcon);
	    stepButton.addActionListener(new StepButtonListener());
	    stepButton.setToolTipText("Step Forward");
	    
	    resetButton = new JButton(resetIcon);
	    resetButton.addActionListener(new ResetButtonListener());	
	    resetButton.setToolTipText("Reset Machine");
	    
	    speedSlider.addChangeListener(new SpeedSliderListener());
	    speedSlider.setMajorTickSpacing(20);
	    speedSlider.setMinorTickSpacing(5);
	    speedSlider.setPaintTicks(true);	
	    speedSlider.setPaintLabels(true);	
	    speedSlider.setToolTipText("Adjust Running Speed");
	    speedSlider.setPreferredSize(new Dimension(500, 30));
	    
	    tapeSelector.setEditable(true);
	    tapeSelector.setToolTipText("Select an input tape, or enter your own");
	    inputPanel.setLayout(new BorderLayout());
	    inputPanel.add(tapeLabel, BorderLayout.WEST);
	    inputPanel.add(tapeSelector, BorderLayout.SOUTH);
	    
	    
	    delay = (int)((Math.exp(MAX_SPEED/10.0) / Math.exp(speedSlider.getValue()/10.0))/3.0);
	    
	    stepTimer = new javax.swing.Timer(delay, new StepTimerListener());
	    stepTimer.setInitialDelay(delay);
	    
	    animationTimer = new javax.swing.Timer(30, new AnimationTimerListener());
	    animationTimer.setInitialDelay(30);
	    
	    tapeArea.setPreferredSize(new Dimension(getWidth(), 50));
	    
	    machineArea.setPreferredSize(new Dimension(getWidth(), 700));
	    machineArea.setOpaque(false);
	    
	    speedSlider.setMaximumSize(new Dimension(300, 50));
	    speedSlider.setPreferredSize(new Dimension(300, 50));
	    speedSlider.setForeground(new Color(200,200,200));
	    speedSlider.setBackground(new Color(200,200,200));


	    inputPanel.setMaximumSize(new Dimension(300, 50));
	    inputPanel.setPreferredSize(new Dimension(300, 50));
	    inputPanel.setForeground(new Color(200,200,200));
	    inputPanel.setBackground(new Color(200,200,200));
    
	    controlBar.setFloatable(false);
	    controlBar.add(runButton);
	    controlBar.add(backButton);
	    controlBar.add(stepButton);
	    controlBar.add(resetButton);
	    
	    controlBar.add(speedLabel);
	    controlBar.add(speedSlider);
	    
	    controlBar.add(Box.createRigidArea(new Dimension(10, 50)));
	    controlBar.add(inputPanel);
	    controlBar.setPreferredSize(new Dimension(500, 50));
	    controlBar.setForeground(new Color(200,200,200));
	    controlBar.setBackground(new Color(200,200,200));
	    
	    statusBar.setEditable(false);		
       	    statusBar.setBackground(new Color(200,200,200));
	
	    JPanel statusAndControl = new JPanel();
	    statusAndControl.setLayout(new BorderLayout());
	    statusAndControl.setBorder(new EmptyBorder(5, 5, 5, 5));
	    statusAndControl.add(statusBar, BorderLayout.NORTH);
	    statusAndControl.add(controlBar, BorderLayout.CENTER);
	    statusAndControl.setMaximumSize(new Dimension(1000, 50));
	    statusAndControl.setForeground(new Color(200,200,200));
	    statusAndControl.setBackground(new Color(200,200,200));

	    mainBox.add(machineArea);
	    mainBox.add(tapeArea);
	    mainBox.add(statusAndControl);

	    getContentPane().add(mainBox);
      	    this.setVisible(true);
	    addComponentListener(new ComponentAdapter() {
		public void componentShown() {
		    repaint();
		}
		public void componentResized() {
		    repaint();
		}
	    });
	    //////////// openItem.doClick();
	    
	}
    }

    public static void repaintMachineArea() {
	machineArea.repaint();
    }
    
    public static void repaintTapeArea() {
	tapeArea.repaint();
    }


    /* printStatus: prints machine status and number of steps in status bar
     */

    static void printStatus() {
	if (machine != null) {
            // statusBar.setText(TuringMain.class.getResource("turing.jpg").toString() + ": " + statusString);
	    statusBar.setText("Status: " + statusString);
	    for(int i=0; i < 13 - statusString.length(); i++)
		statusBar.append(" ");
	    if (machine != null)
		statusBar.append("Steps: " + Integer.toString(machine.steps));
	}
    }
	
    /* getMachines: returns a Vector of JarEntries that
     *              contain the data for the sample machines.
     */
		
    static Vector<JarEntry> getMachines() 
	throws MalformedURLException, IOException {
	JarEntry je;
	Vector<JarEntry> v = new Vector<JarEntry>();
	Enumeration<JarEntry> enumeration = jf.entries();
	while(enumeration.hasMoreElements()) {
	    je = enumeration.nextElement();
	    if(je.getName().endsWith(".tur")) {
		v.add(je);
	    }
	}
	return v;
    }
    
    /* getJarFile: returns the name of the Jar File where the 
     *             resource files are stored
     */

    JarFile getJarFile() throws IOException {
        // only works if files are inside .jar  [Maia]
System.out.println("getClass() = " + getClass());
	URL url = getClass().getResource("turing.jpg");
System.out.println("URL of turing.jpg = " + url);
	JarURLConnection juc = (JarURLConnection) url.openConnection();
System.out.println("JarURLConnection = " + juc);
System.out.println("jar file = " + juc.getJarFile());

        // try the getJarFile method first.
        // Under webstart in 1.5.0_16 this is overriden to return null
        // http://www.objectdefinitions.com/odblog/2008/workaround-for-bug-id-6753651-find-path-to-jar-in-cache-under-webstart/
        JarFile jf = juc.getJarFile();
        if (jf == null) {
            try {
                jf = getJarFileByReflection(juc);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
	return jf;
    }    

   private static JarFile getJarFileByReflection(JarURLConnection jarUrlConnection) throws Exception {
        //this class only exists in webstart.jar for 1.5.0_16 and later
        Class jnlpConnectionClass = Class.forName("com.sun.jnlp.JNLPCachedJarURLConnection");
        Field jarFileField;
        try {
            jarFileField = jnlpConnectionClass.getDeclaredField("jarFile");
        } catch (Throwable t) {
            jarFileField = jnlpConnectionClass.getDeclaredField("_jarFile");
        }
        jarUrlConnection.connect(); //this causes the connection to set the jarFile field
        jarFileField.setAccessible(true);
        return (JarFile)jarFileField.get(jarUrlConnection);
    }


    /* getFilesDirectory: gets the pathname for the directory
     *                   containing the sample Turing Machine files,
     *                   or creates this directory if it does not
     *                   yet exist.  Also, copies the TUR files into
     *                   the  directory if the corresponding files
     *                   in that directory have a less recent modification
     *                   date.
     */

    File getFilesDirectory() {
	File f = new File(jf.getName());
	String s = f.getParent();
	// s = s + "files";
	s = s + File.separator + "tur";
	f = new File(s);
	if (!f.isDirectory()) {
	    f.mkdirs();
	}
	if (f.isDirectory()) {
	    for (int i = 0; i < machineVector.size(); i++) {
		if (machineVector.get(i).toString().endsWith(".tur")) {
		    try {
    			File newFile = new File(f + File.separator 
					   + (machineVector.get(i)).getName());
			JarEntry je = machineVector.get(i);

			if (filename == null) {
			    filename = newFile;
			}
			
			if(newFile.lastModified() < je.getTime()) {
			    newFile.createNewFile();
			    iop.copyFile(getClass().getResource(je.getName()), newFile);
			}		
		    } catch (IOException e) {
			e.printStackTrace();
		    };
			
		}
	    }
	} else {
	    return null;
	}	    
	return f;
    }

    /* setPaused: changes the GUI to reflect that
     *            the machine is paused.
     */

    static void setPaused() {
	runButton.setIcon(runIcon);
	runButton.setToolTipText("Run Machine");
	frozen = true;
	stepTimer.stop();
	tapeArea.machineIsRunning = false;
	halted = Machine.NOT_HALTED;
        statusString = "Paused";
	printStatus();
    }

    static void setHalted() {
	runButton.setIcon(runIcon);
	runButton.setToolTipText("Run Machine");
	frozen = true;
	stepTimer.stop();
    }

    static void setReady() {
	runButton.setIcon(runIcon);
	runButton.setToolTipText("Run Machine");
	frozen = true;
	stepTimer.stop();
	halted = Machine.NOT_HALTED;
	statusString = "Ready";
	printStatus();
    }

    static void setRunning() {
	runButton.setIcon(pauseIcon);
	runButton.setToolTipText("Pause Machine");
	frozen = false;
	halted = Machine.NOT_HALTED;
	statusString = "Running";
	stepTimer.start();
    }

    /* The following Listener is activated when the 'Load Machine..'
     * option is chosen from the File Menu.  It fetches the machine
     * data and forwards it to the drawing areas.
     */

    class GetMachinesMenuItemListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    setPaused();
	    File filesdir = getFilesDirectory();

            // [wayne s17] removed, in case no directory found with .tur files		    
	    // if (filesdir != null) {

	    // Pull up file chooser dialog and find name of selected machine.
            machineChooser.setFileFilter(new TuringFilter());
            machineChooser.setCurrentDirectory(filesdir);
		
            int accept = machineChooser.showOpenDialog(TuringMain.this);

            if (accept == JFileChooser.APPROVE_OPTION) {
                filename = machineChooser.getSelectedFile();
                try {
		    machine = iop.getMachine(filename);
		    if (machine != null) {
		        tapeVector = iop.getTapes();
		    }
	        } catch (IOException ex) {
		    ex.printStackTrace();
		    return;
	        }
	        // set GUI
	        if (machine != null) {
		    machineArea.setMachine(machine);
		    repaintMachineArea();
		    tapeSelector.setModel(new DefaultComboBoxModel(tapeVector));
		    tapeSelector.setSelectedIndex(0);
		    tapeSelector.setEnabled(true);
		    statusString = "Ready";
		    printStatus();
		    setTitle("Turing Machine:  " + filename.getName());    // added 12/21/06 by Kevin Wayne
		    controlBar.requestFocus();
	        }   
	    }
        }
    }
    
    /* The following listener is activated whenever a new tape
     * is chosen or entered in the Input Combo Box
     */

    class TapeSelectorListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    int i, len;
	    boolean match = false;
	    String s;
	    String newString = (String)tapeSelector.getSelectedItem();

	    if(newString == null) {
		tapeSelector.setSelectedIndex(0);
		newString = (String)tapeSelector.getItemAt(0);
	    }
	    
	    DefaultComboBoxModel model = (DefaultComboBoxModel)tapeSelector.getModel();
	    if(!newString.equals(originalTape)) {
		originalTape = new Tape(newString);
		tape = new Tape(originalTape);
		machine.setTape(tape);
		tapeArea.setTape(tape);	
		oldTapeLength = tape.getSize();
		tapeArea.repaint();
		machine.resetMachine();
		setReady();

		//If the entered input is not already
		//a sample tape, add it to the list
		//of sample tapes

		len = tapeSelector.getItemCount();
		for(i = 0; i < len; i++) {
		    s = (String)model.getElementAt(i);
		    if(newString.equals(s)) {
			match = true;
		    }
		}
		resetButton.doClick();
		if(!match) {
		    model.insertElementAt(newString, 0);
		}
	    }
	}
    }

    class RunButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {	
	    if(machine != null) {
		if(halted != Machine.NOT_HALTED) {
		    resetButton.doClick();
		}
		if(frozen) {
		    setRunning();
		} else {
		    setPaused();
		}
	    }
	}	
    } 
    
    class BackButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if(machine != null) {
		if(!animationTimer.isRunning()) {
		    if(machine.stepBack()) {
			oldTapeLength = tape.getSize();
			setPaused();
			repaintTapeArea();
			repaintMachineArea();
		    }
		}
	    }
	}
    }
    
    class StepButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if(halted == Machine.NOT_HALTED)
		setPaused();
	    if(!animationTimer.isRunning()) {
		doStep(1);
	    }
	}
    }
    
    class StepTimerListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    doStep(1);
	}
    }

    
    class ResetButtonListener implements ActionListener {
	public void actionPerformed(ActionEvent e){
	    stepTimer.stop();
	    animationTimer.stop();
	    tapeArea.resetColors();
	    tapeArea.removePartialCells();
	    tapeArea.tapeInit = false;
	    machineArea.resetColors();
	    tape = new Tape(originalTape);
	    oldTapeLength = tape.getSize();
	    fade = FADE_OUT;
	    tapeArea.setTape(tape);
	    machine.setTape(tape);
	    machine.resetMachine();
	    setReady();
	    tapeArea.repaint();
	}
    }

    class JumpItemListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    setPaused();
	    jumpDialog.setVisible(true);
	}
    }
    
    void doStep(int n) {
	if(machine != null) {
	    if(halted == Machine.NOT_HALTED && !animationTimer.isRunning()) {
		animationInitialized = false;
		if(animationOn) {
		    animationTimer.start();
		} else {
		    for(int i = 0; i < n; i++) {
			machine.stepForward();
			halted = machine.shiftAndGetHaltStatus();
			machine.updateMachine();
			if(halted != Machine.NOT_HALTED) {
			    setHalted();
			    switch(halted) {
			    case Machine.ACCEPT:
				statusString = "Accepted";
				break;
			    case Machine.REJECT:
				statusString = "Rejected";
				break;
			    default:
				statusString = "Halted";
			    }
			    break;
			}
		    }
		    tapeArea.repaint();
		    machineArea.repaint();
		    printStatus();
		}
	    }
	}
    }

    class SpeedSliderListener implements ChangeListener {
	public void stateChanged(ChangeEvent e) {
	    int fps = (int)speedSlider.getValue();
	    if(fps == 0) {   
		stepTimer.stop();
	    } else {
		delay = 
		    (int)((Math.exp(MAX_SPEED/10.0) / Math.exp(speedSlider.getValue()/10.0))/3.0);
		stepTimer.setInitialDelay(delay);
		stepTimer.setDelay(delay);
		if(!frozen) {
		    stepTimer.start();
		}
	    }
	}
    }

    /* This listener implements the animation for the color fading 
     * on the tape symbols, vertex centers, and highlighted edges.
     */

    //The way it works is to gradually change the RGB values of the
    //colors used to draw the vertex centers, tape symbols, and highlighted edges
    //from their current color to the color they will be at the end of the animation,
    //the "goal" (as it is called here).  As soon as one of the R, G, or B values for
    //one of these graphical items reaches the goal, all components are set to
    //their final color value, and the animation ends.  Even in the default case
    //(no edge exists for a certain transition, so the state and symbol stay
    //the same), this method is still called, so ensure a uniform delay for all steps.
 

    class AnimationTimerListener implements ActionListener {

	int tapeRedGoal;
	int tapeGreenGoal;
        int tapeBlueGoal;
	int machineRedGoal;
	int machineGreenGoal;
	int machineBlueGoal;
	int edgeRedGoal;
	int edgeGreenGoal;
	int edgeBlueGoal;
	
	int tapeRedInc;
	int tapeGreenInc;
	int tapeBlueInc;
	int machineRedInc;
	int machineGreenInc;
	int machineBlueInc;
	int edgeRedInc;
	int edgeGreenInc;
	int edgeBlueInc;
	
	double growInc;

	Color newTapeColor;
	Color newMachineColor;
	Color newEdgeColor;
	

	public void animationInit() {
	    double colorInc;

	    growInc = 5.0 / TuringMain.stepTimer.getDelay();
	
	    if(growInc < 0.03) {
		growInc = 0.03;
	    }

	    if(!machine.getNextEdge().getOldSymbol().equals(machine.getNextEdge().getNewSymbol()) ||
		!machine.getNextEdge().getOldState().equals(machine.getNextEdge().getNewState())) {
		colorInc = 15.0 / (double)delay;
	    } else {
		colorInc = 60.0 / (double)delay;
	    }
	    if(colorInc < 0.1) {
		colorInc = 0.1;
	    }
	    if(fade == FADE_OUT){
		tapeRedGoal = tapeArea.CURRENT_CELL_COLOR.getRed();
		tapeGreenGoal = tapeArea.CURRENT_CELL_COLOR.getGreen();
		tapeBlueGoal = tapeArea.CURRENT_CELL_COLOR.getBlue();
		machineRedGoal = machineArea.DEFAULT_VERTEX_COLOR.getRed();
		machineGreenGoal = machineArea.DEFAULT_VERTEX_COLOR.getGreen();
		machineBlueGoal = machineArea.DEFAULT_VERTEX_COLOR.getBlue();
		edgeRedGoal = machineArea.ACTIVE_EDGE_COLOR.getRed();
		edgeGreenGoal = machineArea.ACTIVE_EDGE_COLOR.getGreen();
		edgeBlueGoal = machineArea.ACTIVE_EDGE_COLOR.getBlue();
		
		tapeRedInc = (int)((tapeRedGoal - tapeArea.DEFAULT_TEXT_COLOR.getRed()) * colorInc);
		tapeGreenInc = (int)((tapeGreenGoal - tapeArea.DEFAULT_TEXT_COLOR.getGreen()) * colorInc);
		tapeBlueInc = (int)((tapeBlueGoal - tapeArea.DEFAULT_TEXT_COLOR.getBlue()) * colorInc);
		machineRedInc = (int)((machineRedGoal - machineArea.CURRENT_VERTEX_COLOR.getRed()) * colorInc);
		machineGreenInc = (int)((machineGreenGoal - machineArea.CURRENT_VERTEX_COLOR.getGreen()) * colorInc);
		machineBlueInc = (int)((machineBlueGoal - machineArea.CURRENT_VERTEX_COLOR.getBlue()) * colorInc);
		edgeRedInc = (int)((edgeRedGoal - machineArea.BG_COLOR.getRed()) * colorInc);
		edgeGreenInc = (int)((edgeGreenGoal - machineArea.BG_COLOR.getGreen()) * colorInc);
		edgeBlueInc = (int)((edgeBlueGoal - machineArea.BG_COLOR.getBlue()) * colorInc);
		
		newTapeColor = tapeArea.DEFAULT_TEXT_COLOR;
		newMachineColor = machineArea.CURRENT_VERTEX_COLOR;
		newEdgeColor = machineArea.BG_COLOR;
		animationInitialized = true;
	    } else if(fade == FADE_IN){
		tapeRedGoal = tapeArea.DEFAULT_TEXT_COLOR.getRed();
		tapeGreenGoal = tapeArea.DEFAULT_TEXT_COLOR.getGreen();
		tapeBlueGoal = tapeArea.DEFAULT_TEXT_COLOR.getBlue();
		machineRedGoal = machineArea.CURRENT_VERTEX_COLOR.getRed();
		machineGreenGoal = machineArea.CURRENT_VERTEX_COLOR.getGreen();
		machineBlueGoal = machineArea.CURRENT_VERTEX_COLOR.getBlue();
		edgeRedGoal = machineArea.BG_COLOR.getRed();
		edgeGreenGoal = machineArea.BG_COLOR.getGreen();
		edgeBlueGoal = machineArea.BG_COLOR.getBlue();
		
		tapeRedInc = -(int)((tapeRedGoal - tapeArea.CURRENT_CELL_COLOR.getRed()) * colorInc);
		tapeGreenInc = -(int)((tapeGreenGoal - tapeArea.CURRENT_CELL_COLOR.getGreen()) * colorInc);
		tapeBlueInc = -(int)((tapeBlueGoal - tapeArea.CURRENT_CELL_COLOR.getBlue()) * colorInc);
		machineRedInc = -(int)((machineRedGoal - machineArea.DEFAULT_VERTEX_COLOR.getRed()) * colorInc);
		machineGreenInc = -(int)((machineGreenGoal - machineArea.DEFAULT_VERTEX_COLOR.getGreen()) * colorInc);
		machineBlueInc = -(int)((machineBlueGoal - machineArea.DEFAULT_VERTEX_COLOR.getBlue()) * colorInc);
		edgeRedInc = -(int)((edgeRedGoal - machineArea.ACTIVE_EDGE_COLOR.getRed()) * colorInc);
		edgeGreenInc = -(int)((edgeGreenGoal - machineArea.ACTIVE_EDGE_COLOR.getGreen()) * colorInc);
		edgeBlueInc = -(int)((edgeBlueGoal - machineArea.ACTIVE_EDGE_COLOR.getBlue()) * colorInc);
		
		newTapeColor = tapeArea.CURRENT_CELL_COLOR;
		newMachineColor = machineArea.DEFAULT_VERTEX_COLOR;
		newEdgeColor = machineArea.ACTIVE_EDGE_COLOR;
		animationInitialized = true;
	    }
	}

	public void actionPerformed(ActionEvent e) {
	    if(!animationInitialized) {
		animationInit();
	    }
	    if(fade == GROW_TAPE_LEFT) {
		
		tapeArea.offset += growInc;
		if(tapeArea.offset > tapeArea.INITIAL_OFFSET + 1.0) {
		    //animationTimer.stop();
		    finishAddLeft();
		}
	    } else if(fade == GROW_TAPE_RIGHT) {
		
		tapeArea.offset -= growInc;
		if(tapeArea.offset < tapeArea.INITIAL_OFFSET - 1.0) {
		    //tapeAnimationTimer.stop();
		    finishAddRight();
		}
	    } else if(fade == FADE_OUT) {
		if((0 > tapeRedGoal - newTapeColor.getRed()) ^ 
		   (0 > tapeRedGoal - (newTapeColor.getRed() + tapeRedInc))) {
		    finishFadeOut();
		    return;
		}
		if((0 > tapeGreenGoal - newTapeColor.getGreen()) ^ 
		   (0 > tapeGreenGoal - (newTapeColor.getGreen() + tapeGreenInc))) {
		    finishFadeOut();
		    return;
		}
		if((0 > tapeBlueGoal - newTapeColor.getBlue()) ^ 
		   (0 > tapeBlueGoal - (newTapeColor.getBlue() + tapeBlueInc))) {
		    finishFadeOut();
		    return;
		}
		if((0 > machineRedGoal - newMachineColor.getRed()) ^ 
		   (0 > machineRedGoal - (newMachineColor.getRed() + machineRedInc))) {
		    finishFadeOut();
		    return;
		}
		if((0 > machineGreenGoal - newMachineColor.getGreen()) ^ 
		   (0 > machineGreenGoal - (newMachineColor.getGreen() + machineGreenInc))) {
		    finishFadeOut();
		    return;
		}
		if((0 > machineBlueGoal - newMachineColor.getBlue()) ^ 
		   (0 > machineBlueGoal - (newMachineColor.getBlue() + machineBlueInc))) {
		    finishFadeOut();
		    return;
		}
		
		if((0 > edgeRedGoal - newEdgeColor.getRed()) ^ 
		   (0 > edgeRedGoal - (newEdgeColor.getRed() + edgeRedInc))) {
		    finishFadeOut();
		    return;
		}
		if((0 > edgeGreenGoal - newEdgeColor.getGreen()) ^ 
		   (0 > edgeGreenGoal - (newEdgeColor.getGreen() + edgeGreenInc))) {
		    finishFadeOut();
		    return;
		}
		if((0 > edgeBlueGoal - newEdgeColor.getBlue()) ^ 
		   (0 > edgeBlueGoal - (newEdgeColor.getBlue() + edgeBlueInc))) { 
		    finishFadeOut();
		    return;
		}
		
		newTapeColor = new Color(newTapeColor.getRed() + tapeRedInc, 
					 newTapeColor.getGreen() + tapeGreenInc,
					 newTapeColor.getBlue() + tapeBlueInc);
		newMachineColor = new Color(newMachineColor.getRed() + machineRedInc, 
					    newMachineColor.getGreen() + machineGreenInc,
					    newMachineColor.getBlue() + machineBlueInc);
		newEdgeColor = new Color(newEdgeColor.getRed() + edgeRedInc, 
					 newEdgeColor.getGreen() + edgeGreenInc,
					 newEdgeColor.getBlue() + edgeBlueInc);
		
		if(!machine.getNextEdge().getOldSymbol().equals(machine.getNextEdge().getNewSymbol())) {
		    tapeArea.currentTextColor = newTapeColor;
		    
		}
		if(!machine.getNextEdge().getOldState().equals(machine.getNextEdge().getNewState())) {
		    machineArea.currentVertexColor = newMachineColor;
		}
		
		machineArea.nextEdgeColor = newEdgeColor;
      
	    } else {
		
		if((0 > tapeRedGoal - newTapeColor.getRed()) ^ 
		   (0 > tapeRedGoal - (newTapeColor.getRed() - tapeRedInc))) {
		    finishFadeIn();
		    return;
		}
		if((0 > tapeGreenGoal - newTapeColor.getGreen()) ^ 
		   (0 > tapeGreenGoal - (newTapeColor.getGreen() - tapeGreenInc))) {
		    finishFadeIn();
		    return;
		}
		if((0 > tapeBlueGoal - newTapeColor.getBlue()) ^ 
		   (0 > tapeBlueGoal - (newTapeColor.getBlue() - tapeBlueInc))) {
		    finishFadeIn();
		    return;
		}
		
		if((0 > machineRedGoal - newMachineColor.getRed()) ^ 
		   (0 > machineRedGoal - (newMachineColor.getRed() - machineRedInc))) {
		    finishFadeIn();
		    return;
		}
		if((0 > machineGreenGoal - newMachineColor.getGreen()) ^ 
		   (0 > machineGreenGoal - (newMachineColor.getGreen() - machineGreenInc))) {
		    finishFadeIn();
		    return;
		}
		if((0 > machineBlueGoal - newMachineColor.getBlue()) ^ 
		   (0 > machineBlueGoal - (newMachineColor.getBlue() - machineBlueInc))) {
		    finishFadeIn();
		    return;
		}
		
		if((0 > edgeRedGoal - newEdgeColor.getRed()) ^ 
		   (0 > edgeRedGoal - (newEdgeColor.getRed() - edgeRedInc))) {
		    finishFadeIn();
		    return;
		}
		if((0 > edgeGreenGoal - newEdgeColor.getGreen()) ^ 
		   (0 > edgeGreenGoal - (newEdgeColor.getGreen() - edgeGreenInc))) {
		    finishFadeIn();
		    return;
		}
		if((0 > edgeBlueGoal - newEdgeColor.getBlue()) ^ 
		   (0 > edgeBlueGoal - (newEdgeColor.getBlue() - edgeBlueInc))) {
		    finishFadeIn();
		    return;
		}
	    
		newTapeColor = new Color(newTapeColor.getRed() - tapeRedInc, 
					 newTapeColor.getGreen() - tapeGreenInc,
					 newTapeColor.getBlue() - tapeBlueInc);
		newMachineColor = new Color(newMachineColor.getRed() - machineRedInc, 
					    newMachineColor.getGreen() - machineGreenInc,
					    newMachineColor.getBlue() - machineBlueInc);
		newEdgeColor = new Color(newEdgeColor.getRed() - edgeRedInc, 
					 newEdgeColor.getGreen() - edgeGreenInc,
					 newEdgeColor.getBlue() - edgeBlueInc);
		

		if(!machine.getNextEdge().getOldSymbol().equals(machine.getNextEdge().getNewSymbol())) {
		    tapeArea.currentTextColor = newTapeColor;
		}
		if(!machine.getNextEdge().getOldState().equals(machine.getNextEdge().getNewState())) {
		    machineArea.currentVertexColor = newMachineColor;
		}

		machineArea.nextEdgeColor = newEdgeColor;
	    }
	    tapeArea.repaint();
	    machineArea.repaint();
	    try {
		Thread.sleep(30);
	    } catch(InterruptedException ex) {
		ex.printStackTrace();
	    };
	}

	void finishAddLeft() { 
	    tapeArea.offset = tapeArea.INITIAL_OFFSET;
	    tapeArea.newLeftCell = false;
	    tapeArea.repaint();
	    tapeArea.newLeftCellWidth = 0;
	    machine.updateMachine();
	    animationTimer.stop();
	    fade = FADE_OUT;
	    tapeArea.grow = true;
	}

	void finishAddRight() { 
	    tapeArea.offset = tapeArea.INITIAL_OFFSET;
	    tapeArea.newRightCell = false;
	    tapeArea.repaint();
	    tapeArea.newRightCellWidth = 0;
	    machine.updateMachine();
	    animationTimer.stop();
	    fade = FADE_OUT;
	    tapeArea.grow = true;
	}

	void finishFadeOut() {
	    if(!machine.getNextEdge().getOldSymbol().equals(machine.getNextEdge().getNewSymbol())) {
		tapeArea.currentTextColor = tapeArea.CURRENT_CELL_COLOR;
		
	    }
	    if(!machine.getNextEdge().getOldState().equals(machine.getNextEdge().getNewState())) {
		machineArea.currentVertexColor = machineArea.DEFAULT_VERTEX_COLOR;
		
	    }
	    machineArea.nextEdgeColor = machineArea.ACTIVE_EDGE_COLOR;
	    tapeArea.repaint();
	    machineArea.repaint();
	    animationTimer.stop();
	    fade = FADE_IN;
	    animationInitialized = false;
	    machine.stepForward();
	    if(animationOn) {
		animationTimer.start();
	    }
	}

	void finishFadeIn() {
	    machineArea.nextEdgeColor = newEdgeColor;
	    tapeArea.currentTextColor = tapeArea.DEFAULT_TEXT_COLOR;
	    machineArea.currentVertexColor = machineArea.CURRENT_VERTEX_COLOR;
	    machineArea.nextEdgeColor = machineArea.BG_COLOR;
	    animationInitialized = false;
	    fade = FADE_OUT;	         	
	    halted = machine.shiftAndGetHaltStatus();
	    machine.updateMachine();
	    if(halted != Machine.NOT_HALTED) {
		    setHalted();
		    switch(halted) {
		    case Machine.ACCEPT:
			statusString = "Accepted";
			break;
		    case Machine.REJECT:
			statusString = "Rejected";
			break;
		    default:
			statusString = "Halted";
		    }
		    
	    }


	    
	    if(tape.getCurrentPosition() - 3 < tapeArea.leftMostCell) {
		fade = GROW_TAPE_LEFT;
	    } else if(tape.getCurrentPosition() + 3 > tapeArea.rightMostCell) {
		fade = GROW_TAPE_RIGHT;
	    } else {
		animationTimer.stop();
	    }

	 
	    oldTapeLength = tape.getSize();
	    tapeArea.repaint();
	    machineArea.repaint();
	    printStatus();
	}
    }

       
    //The class 'TuringFileView' is used to tell the
    //file chooser how to display the files.  Intead
    //of the actual file name, the file chooser will
    //display the name in the 'title' field
    //of the input file. 

    public class TuringFileView extends FileView {
	public String getTypeDescription(File f) {
	    return null;
	}
	public Icon getIcon(File f) {
	    return null;
	}
	public String getName(File f) {
	    try {
		if(f.getName().endsWith(".tur")) {
		    TuringIOProcessor iop = new TuringIOProcessor();
		    return iop.getMachineName(f);
		}
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    return null;
	}
	public String getDescription(File f) {
	    return null;
	}
	public Boolean isTraversable(File f) {
	    return null;
	}
    }


    //The class 'TuringFilter' is used in the file chooser
    //to filter out all files except those with the
    // '.tur' suffix

    class TuringFilter extends javax.swing.filechooser.FileFilter {
	public boolean accept(File f) {
	    if(f.isDirectory()) {
		return true;
	    }
	    if(f.getName().endsWith(".tur")) {
		return true;
	    } else {
		return false;
	    }
	}
	public String getDescription() {
	    return "TUR files";
	}
    }


    //The class 'TuringDescription' is the component
    //that displays the machine descriptions in the file chooser

    class TuringDescription extends JPanel 
	implements PropertyChangeListener {
	
	JLabel label = new JLabel("  Description:");
	JTextArea jta;
	JScrollPane jsp; 
	
	public TuringDescription(JFileChooser fc) {
	    setPreferredSize(new Dimension(300, 200));
	    setLayout(new BorderLayout());
	    jta = new JTextArea();
	    jsp = new JScrollPane(jta);
	    fc.addPropertyChangeListener(this);
	    jta.setLineWrap(true);
	    jta.setWrapStyleWord(true);
	    jta.setBackground(fc.getBackground());
	    add(label, BorderLayout.NORTH);
	    add(Box.createRigidArea(new Dimension(10, 10)), 
		BorderLayout.WEST);
	    add(jsp, BorderLayout.CENTER);    
	}

	public void propertyChange(PropertyChangeEvent e) {
	    File f;
	    TuringIOProcessor iop = new TuringIOProcessor();
	    if(e.getPropertyName().equals(
		 JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
		f = (File) e.getNewValue();
		if(f != null) {
		    jta.setText(iop.getMachineDescription(f));
		    //scroll to beginning of description
		    jta.moveCaretPosition(0);
		}
	    }
	}
    }

    class JumpWindowListener extends WindowAdapter {
	public void windowClosed(WindowEvent e) {
	    if(jumpDialog.isValid()) {
		stepTimer.stop();
		animationTimer.stop();
		tapeArea.resetColors();
		tapeArea.removePartialCells();
		machineArea.resetColors();
		tape = new Tape(originalTape);
		tapeArea.setTape(tape);
		tapeArea.tapeInit = false;
		machine.setTape(tape);
		machine.resetMachine();
		int j = jumpDialog.getJump();
		boolean a = animationOn;
		animationOn = false;
		if(j == -1)
		    j = 200000;
		doStep(j);
		animationOn = a;
	    }
	}
    }	    
}






