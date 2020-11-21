/*
 * TuringMachine.java: Contains main() method for Java Application
 */

import java.awt.event.*;

public class TuringMachine {
    public static void main(String[] args) {

        // put menubar at top for Mac OS X; option ignored by other operating systems
        System.setProperty("apple.laf.useScreenMenuBar", "true");

	TuringMain program = new TuringMain("Turing Machine");
	program.addWindowListener(new WindowAdapter() {
	    public void windowClosed(WindowEvent e) {
		System.exit(0);
	    }
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});
	program.setVisible(true);
	program.runProgram();
    }
}
