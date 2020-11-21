/*
 * Machine: This class keeps track of the current state of the machine 
 *          and performs transitions between states based on the 
 *          input from the tape.
 */

import java.util.*;

public class Machine {
	
    final int HISTORY_MAX_SIZE = 1000;
    
    public static final int NOT_HALTED = 0;
    public static final int HALTED = 1;
    public static final int ACCEPT = 2;
    public static final int REJECT = 3;

    public Vector<TuringVertex> vertices; 
    public Vector<TuringEdge> edges;
    public int steps;

    private String currentSymbol = null;

    private TuringVertex currentVertex = null;
    
    private TuringEdge lastEdge = null; //the edge taken to enter the current state
    private TuringEdge nextEdge = null;    //the edge that will be taken next
    private TuringIOProcessor iop = new TuringIOProcessor();

    private Tape tape;

    private LinkedList<TuringEdge> history;

    public Machine(Vector<TuringVertex> v, Vector<TuringEdge> e, Tape t) {
	vertices = v;
	edges = e;
	tape = t;

	currentVertex = vertices.get(0);
	currentSymbol = tape.getCurrentSymbol();
	nextEdge = findEdge(currentVertex, t.getCurrentSymbol());
	history = new LinkedList<TuringEdge>();
	steps = 0;
    }	

    public TuringVertex getCurrentVertex() {
	return currentVertex;
    }
    
    public TuringEdge getNextEdge() {
	if(nextEdge == null) {
	    return new TuringEdge(currentVertex.getName(), currentVertex.getName(), 
			    currentSymbol, currentSymbol, 0);
	}
	return nextEdge;
    }

    public void setTape(Tape t) {
	tape = t;
    }
    
    private TuringVertex findVertex(String name) {
	int i;
	int vsize = vertices.size();
	TuringVertex v;
	for(i = 0; i < vsize; i++) {
	    v = vertices.get(i);
	    if(name.equals(v.getName())) {
		return v;
	    }
	}
	System.err.println("Error: " + name + " is not a state.");
	return null;
    }
    
    private TuringEdge findEdge(TuringVertex v, String symbol) {
	int i;
	int esize = edges.size();
	TuringEdge ed;
	for(i = 0; i < esize; i++) {
	    ed = edges.get(i);
	    if(v.getName().equals(ed.getOldState()) 
	       && symbol.equals(ed.getOldSymbol())) {
		return ed;
	    }
	}
	return null;
    }
		
    public void resetMachine() {
	currentVertex = vertices.get(0);
	lastEdge = null;
	currentSymbol = tape.getCurrentSymbol();
	nextEdge = findEdge(currentVertex, currentSymbol);
	
	history = new LinkedList<TuringEdge>();
	TuringMain.repaintMachineArea();
	TuringMain.repaintTapeArea();
	steps = 0;
    }
    
    /* stepForward: This method will perform one transition
     *             based on the current state and tape symbol
     *
     */
    
    
    public void stepForward() {
	boolean symbolChanged;
	
	String state = new String(currentVertex.getName());
	steps++;
	
	currentSymbol = tape.getCurrentSymbol();
	lastEdge = nextEdge;
		
	if(nextEdge != null) {
	    state = nextEdge.getNewState();
	    tape.writeSymbol(nextEdge.getNewSymbol());
	    history.addLast(nextEdge);
	    
	} else {
	    history.addLast(new TuringEdge(currentVertex.getName(), currentVertex.getName(), 
				           currentSymbol, 
                                           currentSymbol, 
				           currentVertex.getDirection()));
	    symbolChanged = false;
	}
	
	if(history.size() > HISTORY_MAX_SIZE) {
	    history.removeFirst();
	}
	
	currentVertex = findVertex(state);
    }
	
    public int shiftAndGetHaltStatus() {
	if(currentVertex.getDirection() == TuringVertex.RIGHT) {
	    tape.shiftRight();
	} else if(currentVertex.getDirection() == TuringVertex.LEFT) {
	    tape.shiftLeft();
	} else {
	    //in this case, halt the machine
	    nextEdge = null;
	    switch(currentVertex.getHaltStatus()) {
	    case TuringVertex.ACCEPT: 
		return ACCEPT;	
	    case TuringVertex.REJECT: 
		return REJECT;
	    default:
		return HALTED;
	    }
	}
	return NOT_HALTED;
    }

    public void updateMachine() {
	currentSymbol = tape.getCurrentSymbol();
	nextEdge = findEdge(currentVertex, currentSymbol);
    }

    
    /* stepBack: restores the machine and tape to the state they were in
     *           in the last step.  Returns false if the stepBack was
     *           unsuccessful (because the history was empty).
     */

    public boolean stepBack() {
	steps--;
	if(steps < 0) {
	    steps = 0;
	}


	//If the current cell was added to the end of the
	//original tape, it is now deleted
	if(history.size() > 0) {
	    TuringEdge ed = history.removeLast();   

	    if((tape.getCurrentPosition() == 0  || 
	    	tape.getCurrentPosition() == tape.getSize() - 1) && 
	        tape.getCurrentSymbol().equals(tape.getFillSymbol()) && 
	        tape.getSize() > TuringMain.originalTape.getSize()) {
		
		tape.deleteCell();
	    
	    } else if(currentVertex.getDirection() == TuringVertex.RIGHT) {
		tape.shiftLeft();
	    } else if(currentVertex.getDirection() == TuringVertex.LEFT) {
		tape.shiftRight();
	    }
	    
	    tape.writeSymbol(ed.getOldSymbol());
	    
	    currentVertex = findVertex(ed.getOldState());
	    
	    nextEdge = lastEdge;
	    if(history.size() > 0) {
		lastEdge = history.getLast();
	    } else {
		lastEdge = null;
	    }
	    return true;
	} else {
	    return false;
	}
    }
}	
