/* Tape: This object represents the logical aspects of the ticker tape
 *       (The class 'TuringTapeArea' handles the graphical aspects).
 *       The tape is a linked list of strings ('symbols')
 *       and an index to the current position on the tape.
 */


import java.util.*;

public class Tape {
	
    private static String FILL_SYMBOL = "#";
    private static int TRAILING_EDGE = 0;

    private int position = 0; 
    private LinkedList<String> tape;
    private int origin = TRAILING_EDGE;

    /* This constructor creates a tape from a string.  The symbols must be separated
     * by at least one space.  A pair of square brackets should be placed around
     * the starting position. If no symbol has square brackets on each side, the 
     * position of the tape reader will be the default position.  If more than
     * one symbol is bracketed, the last one will be the starting position.
     */

    public Tape(String s) {
        StringTokenizer stok = new StringTokenizer(s);

        tape = new LinkedList<String>();

        int i;
        for (i = 0; i < TRAILING_EDGE; i++) {
            tape.add(FILL_SYMBOL);
        }

        for (i = TRAILING_EDGE; stok.hasMoreTokens(); i++) {
            String symbol = stok.nextToken();
            if (symbol.startsWith("[") && symbol.endsWith("]")) {
                position = i;
                tape.add(symbol.substring(1, symbol.length() - 1));
            }
            else {
                tape.add(symbol);
            }
        }

        for (int len = i; i < len + TRAILING_EDGE; i++) {
            tape.add(FILL_SYMBOL);
        }
    }
	
    // This constructor simply clones the tape
    // passed to it.
    public Tape(Tape t) {
	position = t.position;
        tape = new LinkedList<String>();
        for (String s : t.tape)
            tape.add(s);
	// tape = (LinkedList<String>) t.tape.clone();
    }

    public int getCurrentPosition() {
	return position;
    }

    public void setCurrentPosition(int i) {
	position = i;
    }

    public String getCurrentSymbol() {
	return tape.get(position);
    }

    public String getSymbolAt(int i) {
	return tape.get(i);
    }
    
    public int getSize() {
	return tape.size();
    }

    public static String getFillSymbol() {
	return FILL_SYMBOL;
    }

    public static void setFillSymbol(String s) {
	FILL_SYMBOL = s;
    }
    
    public void deleteCell() {
	tape.remove(position);
	if (position != 0) 
	    position--;
	else
	    origin--;
    }

    //move tape position one cell the left, adding a new cell if necessary
    public void shiftLeft() {
	if (position == 0) {
	    addSymbolFirst(FILL_SYMBOL);
	} else {
	    position--;
	}
    }

    public void addSymbolFirst(String s) {
	origin++;
	tape.addFirst(s);
	
    }

    //move tape position one cell the right, adding a new cell if necessary
    public void shiftRight() {
	if (position == tape.size() - 1) {
	    addSymbolLast(FILL_SYMBOL);
	}
	position++;
    }

    public void addSymbolLast(String s) {
	
	tape.addLast(s);
    }	
    public void writeSymbol(String symbol) {
	if (!(tape.get(position)).equals(symbol)) {
	    tape.remove(position);
	    tape.add(position, symbol);
	}
    }

    public int getOrigin() {
	return origin;
    }
}		
		
		
