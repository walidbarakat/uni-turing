/*
 *  TuringEdge: contains information about the edges on the TM graph.
 *        In the GUI, an edge is a line connecting two vertices.
 *        It represents the transition between two states, and the conditions
 *        that trigger it.  If the machine is currently in state "oldState" and 
 *        reads "oldSymbol" off of the tape, then the current state will change to
 *        "newState" and the "newSymbol" will be written at the current
 *        tape position.  The variable "curve" is a number between -1.0 and 1.0 which
 *        indicates the curvature of the edge. If curve = 0, a straight line is drawn.
 *        A curve of 1.0 or -1.0 is semicircular.  If the curve is positive, the edge will
 *        swing in a counterclockwise manner from oldState to newState.  If it is positive,
 *        it will swing in a clockwise manner.
 *        
 */

public class TuringEdge {
    public static final int FIELDS = 5;  //total number of parameters in object
    
    private String oldState;  
    private String newState;  
    private String oldSymbol; 
    private String newSymbol; 
    private double curve;
    
    public TuringEdge(String ot, String nt, String oy, String ny, double c) {
	oldState = new String(ot);
	newState = new String(nt);
	oldSymbol = new String(oy);
	newSymbol = new String(ny);
	curve = c;
    }
    
    public String getOldState() {
	return oldState;
    }
    
    public String getNewState() {
	return newState;
    }
    
    public String getOldSymbol() {
	return oldSymbol;
    }
    
    public String getNewSymbol() {
	return newSymbol;
    }
    public double getCurve() {
	return curve;
    }
}
