/* 
 * Turingmachinearea: Draws the TM as a graph
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;

public class TuringMachineArea extends JPanel {
        boolean needToInitialize = true;       
	static int vertexRadius;
	int fontSize;
	int areaWidth;
	int areaHeight;
        int arrowHeight;
        int currentEdgeWidth;
        int numVertices, numEdges;
	Machine machine;
        static TuringEdge nextEdge;
        
        Vector<EdgeGraphicsData> edgeDataVector;

        

        final int MAX_VERTEX_RADIUS = 30;
	final int FONT_STYLE = Font.PLAIN;
	final String FONT_NAME = "Monotype";
	final Color BG_COLOR = new Color(200,200,200);

	public static final Color DEFAULT_EDGE_COLOR = Color.black;
	public static final Color ACTIVE_EDGE_COLOR = Color.orange;
	public final Color DEFAULT_VERTEX_COLOR = Color.white;
	public final Color CURRENT_VERTEX_COLOR = ACTIVE_EDGE_COLOR;
        public final Color VERTEX_LABEL_COLOR = new Color(0, 130, 0);

    final Color VERTEX_BORDER_COLOR = Color.black; //Color.blue;
    final Color ACCEPT_BORDER_COLOR = Color.black; //new Color(0, 170, 0);
    final Color REJECT_BORDER_COLOR = Color.black; //Color.red;
	final Color DEFAULT_TEXT_COLOR = Color.black;
	final int ARROW_HEIGHT = 60; 
	final double ARROW_ANGLE = Math.toRadians(30);		
	final FontRenderContext DEFAULT_FONT_RENDER_CONTEXT =
        		new FontRenderContext(null, false, false);
	
        public Color nextEdgeColor = BG_COLOR;
        public Color currentVertexColor = CURRENT_VERTEX_COLOR;

    public TuringMachineArea() {
	needToInitialize = true;
	addComponentListener(new ResizeListener());
    }

    public void setMachine(Machine m) {
	machine = m;
	needToInitialize = true;	
    }
    public Machine getMachine() {
	return machine;
    }

    public void resetColors() {
	nextEdgeColor = BG_COLOR;
	currentVertexColor = CURRENT_VERTEX_COLOR;
    }

    
    /* initialize: computes position, size, etc., of vertices, edges, arrowheads,
     *             and transition symbols and stores this information in the 
     *             appropriate data structures.
     */            

    void initialize(Graphics g) {
      	areaWidth = getWidth();
	areaHeight = getHeight();


	

	numVertices = machine.vertices.size();
	numEdges = machine.edges.size();
	
	if(areaWidth > areaHeight) {
	    vertexRadius = (int)(areaHeight / Math.sqrt(numVertices * 60.0));
	} else {
	    vertexRadius = (int)(areaWidth / Math.sqrt(numVertices * 60.0));
	}
	
	if(vertexRadius > MAX_VERTEX_RADIUS) {
	    vertexRadius = MAX_VERTEX_RADIUS;
	}

	fontSize = (int)(vertexRadius / 1.5);
	g.setFont(new Font(FONT_NAME, FONT_STYLE, fontSize));

	arrowHeight = (int)(vertexRadius / 2.5);

	
	edgeDataVector = new Vector<EdgeGraphicsData>();

	//load edge data
	for(int i = 0; i < numEdges; i++) {
	    int arrowx[] = new int[3];
	    int arrowy[] = new int[3];

	    TuringEdge edge = machine.edges.get(i);
	    TuringVertex v1 = findVertex(edge.getOldState());
	    TuringVertex v2 = findVertex(edge.getNewState());

	    
	    //if the edge connects a state to itself
	    if(v1 == v2) {
		
		//in this case, startAngle indicates the orientation of the 
		//circular edge on the vertex
		if(!edge.getOldSymbol().equals(edge.getNewSymbol())) {
		    double startAngle = Math.toRadians(edge.getCurve());
		    int xpos1 = (int)(v1.getXpos() * areaWidth);
		    int ypos1 = (int)(v1.getYpos() * areaHeight);
		    int cornerx = (int)(xpos1 + vertexRadius * 
				    Math.cos(startAngle) - vertexRadius);
		    int cornery = (int)(ypos1 - vertexRadius * 
				Math.sin(startAngle) - vertexRadius);
		    
		    //compute arrowhead points
		    //The arrowhead is 60 degrees away from the center of the circular edge, 
		    //measuring along the outside of the vertex 
		    arrowx[0] = (int)(xpos1 + vertexRadius * Math.cos(startAngle - Math.PI/3.0));
		    arrowy[0] = (int)(ypos1 - vertexRadius * Math.sin(startAngle - Math.PI/3.0));
		    
		    //The edges of the arrow are oriented as if the arrow was only 30 degrees
		    //away from the center.  This helps the arrowhead line up correctly
		    //with the edge
		    arrowx[1] = (int)(arrowx[0] + arrowHeight * Math.cos(startAngle - Math.PI/6.0 - ARROW_ANGLE));
		    arrowx[2] = (int)(arrowx[0] + arrowHeight * Math.cos(startAngle - Math.PI/6.0 + ARROW_ANGLE));
		    arrowy[1] = (int)(arrowy[0] - arrowHeight * Math.sin(startAngle - Math.PI/6.0 - ARROW_ANGLE));
		    arrowy[2] = (int)(arrowy[0] - arrowHeight * Math.sin(startAngle - Math.PI/6.0 + ARROW_ANGLE)); 

		    //Compute the size and position of the transition symbol
		    String transitionString = edge.getOldSymbol() + " : " + edge.getNewSymbol();

		    Rectangle2D charBounds = g.getFont().getStringBounds(transitionString, 
						DEFAULT_FONT_RENDER_CONTEXT);

		    int stringWidth  = (int)Math.ceil(charBounds.getWidth());
		    int stringHeight = (int)Math.ceil(charBounds.getHeight());

		    //center string in middle of edge
		    int stringx = (int)(xpos1 + vertexRadius * 2.0 * Math.cos(startAngle));
		    stringx -= (int)(stringWidth * 0.5);
		 
		    int stringy = (int)(ypos1 - vertexRadius * 2.0 * Math.sin(startAngle));
		    stringy += (int)(stringHeight * 0.25);

		    
		    edgeDataVector.add(new EdgeGraphicsData(cornerx, cornery, vertexRadius * 2, edge, 
							    new Arrowhead(arrowx, arrowy),
							    new Transition(stringx, stringy,
									   stringWidth, stringHeight,
									   transitionString)));
		}
		continue;
	    }

	    int xpos1 = (int)(v1.getXpos() * areaWidth);
	    int ypos1 = (int)(v1.getYpos() * areaHeight);
	    int xpos2 = (int)(v2.getXpos() * areaWidth);
	    int ypos2 = (int)(v2.getYpos() * areaHeight);

	    double curve = edge.getCurve();

	    //w is one-half the length of the distance between
	    //the centers of the vertices
	    
	    int w = (int)(0.5 * Math.sqrt(((xpos1 - xpos2) * (xpos1 - xpos2))
					+ ((ypos1 - ypos2) * (ypos1 - ypos2))));
	    
	    //h is the height of the arc
	    //it is negative if the 'curve' of the edge is negative
	    //(see documentation on the input format for machines)
	    
	    int h = (int)((double)w * curve);

	    //theta is the angle that a straight line connecting
	    //the two vertices makes with the horizontal

	    double theta;
	    if(xpos2 == xpos1) {
		if(ypos2 > ypos1) {
		    theta = -Math.PI / 2.0;
		} else {
		    theta = Math.PI / 2.0;
		}
	    } else {
		theta = -Math.atan((double)(ypos2 - ypos1)/(double)(xpos2 - xpos1));
	    }

	    if(xpos1 > xpos2) {
		theta -= Math.PI;
	    }

	    //if  h is 0, then the edge is a straight line

	    if(h == 0) {
		//compute points of arrowhead
		arrowx[0] = (int)(xpos2 - vertexRadius * Math.cos(theta));
		arrowy[0] = (int)(ypos2 + vertexRadius * Math.sin(theta));

		//The point of the arrow head has an angle of 2 * ARROW_ANGLE
		arrowx[1] = (int)(arrowx[0] - arrowHeight * Math.cos(theta - ARROW_ANGLE));
		arrowx[2] = (int)(arrowx[0] - arrowHeight * Math.cos(theta + ARROW_ANGLE));
		arrowy[1] = (int)(arrowy[0] + arrowHeight * Math.sin(theta - ARROW_ANGLE));
		arrowy[2] = (int)(arrowy[0] + arrowHeight * Math.sin(theta + ARROW_ANGLE));

		//Compute the size and position of the transition symbol
		String transitionString = edge.getOldSymbol() + " : " + edge.getNewSymbol();

		Rectangle2D charBounds = g.getFont().getStringBounds(transitionString, 
						DEFAULT_FONT_RENDER_CONTEXT);

		int stringWidth  = (int)Math.ceil(charBounds.getWidth());
		int stringHeight = (int)Math.ceil(charBounds.getHeight());

		//center string in middle of edge
		int stringx = (int)(xpos1 + w * Math.cos(theta));
	        stringx -= (int)(stringWidth * 0.5 * 
				 (Math.abs(Math.cos(theta)) + Math.abs(Math.sin(theta))));
		 
		int stringy = (int)(ypos1 - w * Math.sin(theta));
		stringy += (int)(stringHeight * 0.25);

		edgeDataVector.add(new EdgeGraphicsData(xpos1, ypos1, xpos2, ypos2, 
							edge, new Arrowhead(arrowx, arrowy),
							new Transition(stringx, stringy, 
								       stringWidth, stringHeight,
								       transitionString)));
		continue;
      	    }

	    //otherwise, edge is a curved line connecting
	    //two different states

	    //I will express the arc as a section of a circle.
	    //r is the radius of this circle, though
	    //its sign is opposite that of the 'curve' of the arc.
	    
	    int r = -(int)((w * w + h * h)/ (2 * h));

	    //arcAngle is the central angle of the section of the circle.
	    //its sign is the opposite of the 'curve'
	    //(for Java drawing purposes)

	    double arcAngle = 2.0 * Math.asin((double)w / (double)r);

	    //sideAngle is the side angle of the isoceles triangle
	    //formed by the center of the circle and the two vertices,
	    //or its trigonometric equivalent in Quadrant II

	    double sideAngle = (Math.PI - arcAngle) / 2.0;

	    //startAngle is the angle on the circle where the arc begins
	    //this is different from its use in drawing circular edges above

	    double startAngle = sideAngle + theta;
	    if(arcAngle > 0) {
		startAngle += Math.PI;
	    }
	    
	    //centerx, centery are the coordinates of the center of the circle

	    int centerx = (int)(xpos1 - Math.abs(r) * Math.cos(startAngle));
	    int centery = (int)(ypos1 + Math.abs(r) * Math.sin(startAngle));

	    //cornerx, y are the coordinates of the upper left
	    //corner of the square circumscribed around the circle
	    //(for Java drawing purposes)

	    int cornerx = centerx - Math.abs(r);
	    int cornery = centery - Math.abs(r);
	    
	    //compute the arrowhead points
	    
	    
	    //phi is approximately the angular difference between the angle at which
	    //the curved edge enters the vertex, and the angle that it would
	    //enter in at if it were a straight line.
	    
	    double phi = ((arcAngle / 2.0) * (1.0 - ((double)vertexRadius / (2.0 * (double)w))));
	    
	    arrowx[0] = (int)(xpos2 - vertexRadius * Math.cos(theta + phi));
	    arrowy[0] = (int)(ypos2 + vertexRadius * Math.sin(theta + phi));
	    arrowx[1] = (int)(arrowx[0] - arrowHeight * Math.cos(theta - ARROW_ANGLE + phi));
	    arrowx[2] = (int)(arrowx[0] - arrowHeight * Math.cos(theta + ARROW_ANGLE + phi));
	    arrowy[1] = (int)(arrowy[0] + arrowHeight * Math.sin(theta - ARROW_ANGLE + phi));
	    arrowy[2] = (int)(arrowy[0] + arrowHeight * Math.sin(theta + ARROW_ANGLE + phi));

	    //Compute the size and position of the transition symbol
	    String transitionString = edge.getOldSymbol() + " : " + edge.getNewSymbol();

	    Rectangle2D charBounds = g.getFont().getStringBounds(transitionString, 
						DEFAULT_FONT_RENDER_CONTEXT);

	    int stringWidth  = (int)Math.ceil(charBounds.getWidth());
	    int stringHeight = (int)Math.ceil(charBounds.getHeight());

	    //center string in middle of edge
	    //alpha is the angle made by the horizontal and a straight
	    //line from the source vertex to the peak of the arc

	    double alpha = theta + Math.atan(curve);

	    int stringx = (int)(xpos1 + w * Math.cos(theta) - h * Math.sin(theta));
	    stringx -= (int)(stringWidth * 0.5 * 
			     (Math.abs(Math.cos(alpha)) + Math.abs(Math.sin(alpha))));
		 
	    int stringy = (int)(ypos1 - w * Math.sin(theta) - h * Math.cos(theta));
	    stringy += (int)(stringHeight * 0.25);
	   
     	    edgeDataVector.add(new EdgeGraphicsData(cornerx, cornery, Math.abs(r), 
						    (int)Math.toDegrees(startAngle),
						    (int)Math.toDegrees(arcAngle), edge,
						    new Arrowhead(arrowx, arrowy),
						    new Transition(stringx, stringy,
								   stringWidth, stringHeight,
								   transitionString)));
	
	}
        needToInitialize = false;
    }
	
    public void paintComponent(Graphics gr) {
	int i = 0;
	Graphics2D g = (Graphics2D)gr;
	EdgeGraphicsData egd;
	if(machine == null) {   
	    return;
	}

	if(needToInitialize) {
	    initialize(g);
	}

	g.setColor(BG_COLOR);
	g.fillRect(0,0,getWidth(),getHeight());

	fontSize = (int)(vertexRadius / 1.5);
	g.setFont(new Font(FONT_NAME, FONT_STYLE, fontSize));

	nextEdge = machine.getNextEdge();

	//draw edges
	for(i = 0; i < numEdges; i++) {    
	    egd = edgeDataVector.get(i);
	    egd.drawHighlight(g);
	}
	for(i = 0; i < numEdges; i++) {    
	    egd = edgeDataVector.get(i);
	    egd.drawEdge(g);
	}
	//draw vertices
	fontSize = vertexRadius;
	for(i = 0; i < numVertices; i++) {
	    g.setFont(new Font(FONT_NAME, FONT_STYLE, fontSize));
	    drawVertex(machine.vertices.get(i), g);
	}
	
    }
 
        
    private TuringVertex findVertex(String name) {
	int i;
	int vsize = machine.vertices.size();
	TuringVertex v;
	for(i = 0; i < vsize; i++) {
	    v = machine.vertices.get(i);
	    if(name.equals(v.getName())) {
		return v;
	    }
	}
	
	return null;
    }

    private void drawVertex(TuringVertex ver, Graphics2D g) {
	int xpos = (int)(ver.getXpos() * areaWidth) - vertexRadius;
	int ypos = (int)(ver.getYpos() * areaHeight) - vertexRadius;
	int textXpos = xpos + (vertexRadius * 5 / 8); 
	int textYpos = ypos + (vertexRadius * 5 / 4);
	int borderWidth = 1; //vertexRadius / 8;

	//draw border
	if(ver.getHaltStatus() == TuringVertex.ACCEPT) {
	    g.setColor(ACCEPT_BORDER_COLOR);
	} else if(ver.getHaltStatus() == TuringVertex.REJECT) {
	    g.setColor(REJECT_BORDER_COLOR);
	} else {
	    g.setColor(VERTEX_BORDER_COLOR);
	}
	g.fillOval(xpos, ypos, vertexRadius * 2, vertexRadius * 2);
	
	//draw center (colored if current)
	if(ver == machine.getCurrentVertex()) {
	    g.setColor(currentVertexColor);
	} else {
	    g.setColor(DEFAULT_VERTEX_COLOR);
	}
	
	g.fillOval(xpos + borderWidth, ypos + borderWidth, 
		   (vertexRadius - borderWidth) * 2, (vertexRadius - borderWidth) * 2);
	
	//print direction-halt status inside vertex
	g.setColor(DEFAULT_TEXT_COLOR);
	switch(ver.getDirection()) {
	case TuringVertex.LEFT:   g.drawString("L", textXpos, textYpos); break; 
	case TuringVertex.RIGHT:  g.drawString("R", textXpos, textYpos); break;			
	case TuringVertex.ACCEPT: g.drawString("Y", textXpos, textYpos); break; 
	case TuringVertex.REJECT: g.drawString("N", textXpos, textYpos); break;
	default:                  g.drawString("H", textXpos, textYpos); break; 
			
	}
	//print vertex name in vertex
	g.setFont(new Font(FONT_NAME, FONT_STYLE, fontSize / 2));
	g.setColor(VERTEX_LABEL_COLOR);
	textXpos = xpos + (vertexRadius * 7 / 8); 
	textYpos = ypos + (vertexRadius * 16 / 9);
	if(ver.getName().length() > 1) {
	    textXpos = xpos + (vertexRadius * 6 / 8); 
	}
	g.drawString(ver.getName(), textXpos, textYpos);
    }		

    class ResizeListener extends ComponentAdapter {
	public void componentResized(ComponentEvent e) {
	    needToInitialize = true;
	}
	public void componentShown(ComponentEvent e) {
	    needToInitialize = true;
	}
    }

    //The EdgeGraphicsData class stores graphical data
    //about edges, such as position, curvature, arrowhead
    //and transition symbol, and contains methods for
    //drawing and highlighting edges.

    private class EdgeGraphicsData {
	static final int STRAIGHT_LINE = 0;
	static final int CURVED_LINE = 1;
	static final int CIRCLE = 2;

	boolean current = false;

	int xpos1 = 0;
	int ypos1 = 0;
	int xpos2 = 0 ;
	int ypos2 = 0;

	int cornerx = 0;
	int cornery = 0;
	int r = 0;
	int startAngle = 0;
	int arcAngle = 0;

	int curvature;

	TuringEdge edge;

	Arrowhead arrowhead;
	Transition transition;

	//constructor for straight lines and circular lines
	public EdgeGraphicsData(int a, int b, int c, int d, TuringEdge e, Arrowhead f, Transition g) {
	    xpos1 = a;
	    ypos1 = b;
	    xpos2 = c;
	    ypos2 = d;
	    edge = e;
	    arrowhead = f;
	    transition = g;
	    curvature = STRAIGHT_LINE;
	 
	
	}

	//constructor for curved lines
	public EdgeGraphicsData(int a, int b, int c, int d, int e, TuringEdge f, Arrowhead g, Transition h) {
	    cornerx = a;
	    cornery = b;
	    r = c;
	    startAngle = d;
	    arcAngle = e;
	    edge = f;
	    arrowhead = g;
	    transition = h;
	    curvature = CURVED_LINE;
	}
	
	//constructor for circular lines
	//the ones connecting a state to itself

	public EdgeGraphicsData(int a, int b, int c, TuringEdge d, Arrowhead e, Transition f){
	    cornerx = a;
	    cornery = b;
	    r = c;
	    edge = d;
	    arrowhead = e;
	    transition = f;
	    curvature = CIRCLE;
	}
    
	//This method highlights the current edge
	public void drawHighlight(Graphics2D g) {
	    g.setColor(nextEdgeColor);
	    g.setStroke(new BasicStroke(vertexRadius/2));
	    if(edge == nextEdge) {
		if(curvature == CIRCLE) {
		    g.drawOval(cornerx, cornery, r, r);
		} else if(curvature == CURVED_LINE) {
		    g.drawArc(cornerx, cornery, r * 2, r * 2, startAngle, arcAngle);
		} else {
		    g.drawLine(xpos1, ypos1, xpos2, ypos2);
		}
		current = true;
	    } else {
		current = false;
	    }
	}

	//This method draws an edge

	public void drawEdge(Graphics2D g) {
	    g.setStroke(new BasicStroke(1));
	    g.setColor(DEFAULT_EDGE_COLOR);
	    if(curvature == CIRCLE) {
		g.drawOval(cornerx, cornery, r, r);
	    } else if(curvature == CURVED_LINE) {
		g.drawArc(cornerx, cornery, r * 2, r * 2, startAngle, arcAngle);
	    } else {
		g.drawLine(xpos1, ypos1, xpos2, ypos2);
	    }
	    arrowhead.drawArrowhead(g);
	    transition.drawTransition(g, current);
	}
    }

    private class Arrowhead {
	int arrowx[];
	int arrowy[];

	Arrowhead(int x[], int y[]) {
	    arrowx = x;
	    arrowy = y;
	}

	void drawArrowhead(Graphics2D g) {
	    g.setColor(DEFAULT_EDGE_COLOR);
	    g.fillPolygon(arrowx, arrowy, 3);
	}
    }

    private class Transition {
	int stringx;
	int stringy;
	int stringWidth;
	int stringHeight;
	String transition;

	Transition(int a, int b, int c, int d, String s) {
	    stringx = a;
	    stringy = b;
	    stringWidth = c;
	    stringHeight = d;
	    transition = s;
	}
	
	void drawTransition(Graphics g, boolean current) {
	    //First, to block out the part of the edge
	    //that will overlap the symbol

	    if(current) {
		g.setColor(nextEdgeColor);
	    } else {
		g.setColor(BG_COLOR);
	    }

	    g.fillRoundRect(stringx - 5, stringy - (int)(stringHeight * 0.75) - 5, 
			    stringWidth + 10, stringHeight + 10, 
			    (int)(vertexRadius * 1.5),(int)(vertexRadius * 1.5));

	    //drawing the actual string
	    g.setColor(DEFAULT_EDGE_COLOR);
	    g.drawString(transition, stringx, stringy);
	}
    }
}
