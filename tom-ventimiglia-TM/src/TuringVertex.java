/* 
 * TuringVertex: A vertex represents a state of the machine.
 *         The 'name' is a string used to uniquely identify the state.
 *         The 'haltStatus' can have the value LEFT, RIGHT, HALT, ACCEPT, or REJECT and indicates
 *         the direction to shift the tape upon entering the state, or the halt status of
 *         the machine upon entering the state.  'xpos' and 'ypos' are values between
 *         0 and 1.0 which represent the position of the center of the vertex in 
 *         the machine area.  (These are scaled against the width and height of the machine
 *         area.  (0, 0) is the upper left corner of the screen, and (1.0, 1.0) is the lower 
 *         right corner.  
 */


public class TuringVertex {
    public static final int HALT = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int ACCEPT = 3;
    public static final int REJECT = 4;
    
    public static final int FIELDS = 4;
    
    private String name;
    private int haltStatus;
    private double xpos;
    private double ypos;
    
    public TuringVertex(String n, int h, double x, double y) {
	name = new String(n);
	haltStatus = h;
	xpos = x;
	ypos = y;
    }	

    public String getName() {
	return name;
    }

    //Two methods are provided that
    //return haltStatus because this
    //variable is used for two purposes.

    public int getDirection() {
	return haltStatus;
    }

    public int getHaltStatus() {
	return haltStatus;
    }
    
    public double getXpos() {
	return xpos;
    }
	
    public double getYpos() {
	return ypos;
    }
}
