/* 
 * TuringIOProcessor: a group of methods used for reading 
 * information about the TM out of a file
 */

import java.io.*;
import java.util.*;
import java.net.*;
import javax.swing.JOptionPane;

public class TuringIOProcessor {

    BufferedReader fin;
    FileOutputStream fout;
    int line;
    Vector<String> tapes = new Vector<String>();
    Tape defaultTape;


    public Vector getTapes() {
	if(tapes.size() == 0) {
	    tapes.add("[" + Tape.getFillSymbol() + "]");
	}
	return tapes;
    }

    /* copyFile: copies the contents of the URL into the 
     *           location indicated by 'file'.
     */
    public void copyFile(URL url, File file) throws IOException {
	String s;
	int c;
	try {
	    fin  = new BufferedReader(new InputStreamReader(url.openStream()));
	    fout = new FileOutputStream(file);
	} catch(FileNotFoundException e) {
	    e.printStackTrace();
	    return;
	} catch(MalformedURLException e){
	    e.printStackTrace();
	    return;
	} catch(IOException e) {
	    e.printStackTrace();
	    return;
	}
	
	do {
	    
	    c = fin.read();
	    if(c == -1) {
		fout.close();
		return;
	    }
	    fout.write(c);
	} while(true);
	
    }

    /* getMachineName: returns the String in the 'title'
     *                  field of the Turing Machine file f
     *                  If the file has no such field,
     *                  it returns the system's name for the file.
     */
    
    public String getMachineName(File f) throws IOException {
	String s;
	try {
	    fin = new BufferedReader(new FileReader(f));
	} catch(FileNotFoundException e) {
	    e.printStackTrace();
	    return null;
	} 
	
	do {
	    s = fin.readLine();
	    if(s == null) { 
		//no 'title' field encountered
		return f.getName();
	    }
	} while(!s.startsWith("title"));
	return fin.readLine();
    }
	
    /* getMachineDescription: returns the String in the 
     *                        'description' field of the 
     *                        input file, or null if no
     *                        such field exists.
     */

    public String getMachineDescription(File f) {
	String s = null;
	String desc = new String();

        // check if file is a directory
        if (f.isDirectory()) return null;

	try {
	    fin = new BufferedReader(new FileReader(f));
	    
	} catch(FileNotFoundException e) {
	    e.printStackTrace();
	    return null;
	}
		
	do {
	    try {
		s = fin.readLine();
	    } catch(IOException e) {
		e.printStackTrace();
	    }
	    if(s == null) 
		return desc;
	} while(!s.startsWith("description"));
	
	do {
	    try {
		s = fin.readLine();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	    if(s == null)
		break;
	    if(s.startsWith("vertices") || s.startsWith("fill symbol"))
		break;
	    desc = new String(desc + " " + s.trim());
	} while(true);
	
	return desc;
    }
    
    /* getMachine: converts the information about the vertices and
     *             edges in the input file into a Machine
     */
    
    public Machine getMachine(File file)  throws IOException {
	boolean fillSymbolChanged = false;
	int hs;
	double xpos, ypos, curve;
	String s, name;
	String ot, nt, oy, ny;
	TuringVertex ver;
	Vector<TuringVertex> vertices = new Vector<TuringVertex>();
	Vector<TuringEdge> edges = new Vector<TuringEdge>();
	StringTokenizer stok;

	tapes = new Vector<String>();
	
	try {
	    fin = new BufferedReader(new FileReader(file));
	} catch(FileNotFoundException e) {
	    e.printStackTrace();
	    return null;
	}

	line = 0;
	fillSymbolChanged = false;

	
	//scan past comments and inital whitespace, detecting optional fill symbol
	
	do {
	    s = fin.readLine();
	    if(s == null) {
		TuringMain.statusBar.setText("Error: File is not proper format.");
		return null;
	    }
	    if(s.startsWith("fill symbol")) {
		line++;
		fillSymbolChanged = true;
		Tape.setFillSymbol(fin.readLine().trim());
	    }  
	    line++;
	} while(!s.toLowerCase().startsWith("vertices"));
	
	if(!fillSymbolChanged) {
	    Tape.setFillSymbol("#");
	}

	s = fin.readLine();
	if(s == null) {
	    return null;
	}
	stok = new StringTokenizer(s);

	//****************
	//read in vertices
	//**************** 
	
	do {
	    
	    // If an empty line or comment is read, ignore.
	    if(s.startsWith("//") || stok.countTokens() == 0) {
		s = fin.readLine();
		if(s == null) {
		    return null;
		}
		stok = new StringTokenizer(s);
		line++;
		continue;
	    }
	    if((stok.countTokens() != TuringVertex.FIELDS)) {
		
		TuringMain.statusBar.setText("Error in line " + line + ": Wrong number of parameters.");
		return null;
	    }
	    
	    //get name
	    name = new String(stok.nextToken());
	    
	    //get halt status - direction
	    s = stok.nextToken().toUpperCase();
	    if(s.equals("L")) {
		hs = TuringVertex.LEFT;
	    } else if(s.equals("R")) {
		hs = TuringVertex.RIGHT;
	    } else if(s.equals("H")) {
		hs = TuringVertex.HALT; 
	    } else if(s.equals("Y")) {
		hs = TuringVertex.ACCEPT;
	    } else if(s.equals("N")) {
		hs = TuringVertex.REJECT;
	    } else {
		TuringMain.statusBar.setText("Error in line " + line + ": invalid direction - halt status");
		return null;
	    }
	    
	    //get x and y coordinates
	    
	    s = stok.nextToken();
	    try {
		xpos = Double.parseDouble(s);
	    } catch (NumberFormatException e) {
		TuringMain.statusBar.setText("Error in line " + line + ": invalid x-coordinate");
		return null;
	    }

	    s = stok.nextToken();
	    try {
		ypos = Double.parseDouble(s);
	    } catch (NumberFormatException e) {
		TuringMain.statusBar.setText("Error in line " + line +  ": invalid y-coordinate");
		return null;
	    }
	    
	    //create vertex, and add to set of vertices
	    
	    ver = new TuringVertex(name, hs, xpos, ypos);
	    vertices.add(ver);
	    
	    s = fin.readLine();
	    if(s == null) {
		return null;
	    }
	    stok = new StringTokenizer(s);
	    line++;
	    
	} while(!s.toLowerCase().startsWith("edges"));

	s = fin.readLine();
	if(s == null) {
	    return null;
	}
	stok = new StringTokenizer(s);
	
	
	//****************
	//read in the edges
	//****************
	
	
	do {
	    if(s.startsWith("//") || stok.countTokens() == 0) {
		s = fin.readLine();
		if(s == null) {
		    if(edges.size() == 0) {
			return null;
		    } else {
			break;
		    }
		}
		stok = new StringTokenizer(s);
		line++;
		continue;
	    }
	    
	    if(stok.countTokens() != TuringEdge.FIELDS && stok.countTokens() != TuringEdge.FIELDS - 1) {
		TuringMain.statusBar.setText("Error in line " + line + ": wrong number of parameters");
		return null;
	    }
	    ot = stok.nextToken();
	    nt = stok.nextToken();			
	    oy = stok.nextToken();
	    ny = stok.nextToken();
	    
	    if(stok.countTokens() == 0) {
		curve = 0;
	    } else {
		s = stok.nextToken();
		curve = Double.parseDouble(s);
	    }

	    // add edge to Vector of edges, but check if adding this edge makes TM nondeterministic
            // updated by Maia Ginsburg 12/21/06
	    if(!ot.equals(nt) || !oy.equals(ny)) {
	        Enumeration<TuringEdge> e = edges.elements();
	        while (e.hasMoreElements()) {
	            TuringEdge edge = e.nextElement();
	            if (edge.getOldState().equals(ot) && edge.getOldSymbol().equals(oy)) {
	                String err = "Error in this edge: " + s + ": it makes a nondeterministic Turing Machine";
	                TuringMain.statusBar.setText(err);
	                System.out.println(err);
	                JOptionPane.showMessageDialog(TuringMain.statusBar, err); // pop-up dialog box
	            }
	        } 
		edges.add(new TuringEdge(ot, nt, oy, ny, curve));
	    }
	    else {
	        String err = "This edge rejected: " + ot+ " " + nt + " " + oy + " " + ny + ": and not needed";
	        TuringMain.statusBar.setText(err);
	        System.out.println(err);
	        JOptionPane.showMessageDialog(TuringMain.statusBar, err); // pop-up dialog box
	    }



	    s = fin.readLine();
	    if(s == null) {
		if(edges.size() == 0) {
		    return null;
		} else {
		    break;
		}
	    }
	    stok = new StringTokenizer(s);
	    
	    line++;
	} while(!s.toLowerCase().startsWith("tapes"));
	
	s = fin.readLine();
	if(s == null) {
	    defaultTape = new Tape("[" + Tape.getFillSymbol() + "]");
	    return new Machine(vertices, edges, defaultTape);
	}
	stok = new StringTokenizer(s);
	
	//*******************
	// read in the tapes
	//*******************
	
	//tapes are stored in the Vector 'tapes', which is returned when
	//the method 'getTapes()' is called.

	do {	
	    line++;
	    if(s.startsWith("//") || stok.countTokens() == 0) {
		s = fin.readLine();
		if(s == null) {
		    break;
		}
		stok = new StringTokenizer(s);
		continue;
	    }
	    tapes.add(s);
	    s = fin.readLine();
	    if(s == null) {
		break;
	    }
	    stok = new StringTokenizer(s);
	} while(true);

	if(tapes.size() == 0) {
	   defaultTape = new Tape("[" + Tape.getFillSymbol() + "]"); 
      	} else {
	    defaultTape = new Tape(tapes.get(0));
	}

	return new Machine(vertices, edges, defaultTape);
    }
}
