
/* 
 * TuringTapeArea: Draws and animates the ticker tape
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;

public class TuringTapeArea extends JPanel  {
    boolean machineIsRunning = false;
    boolean tapeInit = false;
    int cellWidth = 40;
    double newLeftCellWidth = 0;
    double newRightCellWidth = 0;
    int cellHeight = 40;
    int fontSize = 20;
    int shift, filler;
    int leftMostCell = 0, rightMostCell = 0;
    Dimension areaSize;

    int origin = 0;

    boolean grow = false;
    boolean newLeftCell = false, newRightCell = false;
    
    double animationInc;
    Tape tape;
    
    Color currentTextColor = Color.black;
    
    final int LEFT_SHIFT = 0;
    final int RIGHT_SHIFT = 1;
  
    final int ADD_DELAY = 10;
  
    final int FONT_STYLE = Font.BOLD;
    final String FONT_NAME = "Monotype";
    final Color BG_COLOR = new Color(200,200,200);
    final Color DEFAULT_CELL_COLOR = Color.white;
    final Color CURRENT_CELL_COLOR = Color.orange;
    final Color CELL_INDEX_COLOR = new Color(0, 130, 0);

    final double beginTab = 0.0;
    final double INITIAL_OFFSET = -1.5;
    double offset = INITIAL_OFFSET; 
    public final Color DEFAULT_TEXT_COLOR = Color.black;
    
    public void setTape(Tape t) {
	tape = t;
    }

    public void resetColors() {
	currentTextColor = Color.black;
    }

    public void removePartialCells() {
	newLeftCellWidth = 0;
	newRightCellWidth = 0;
    }
    
    public void paintComponent(Graphics g) {
	int i;
	int yAlign;     //used for vertical centering of tape
	int tapeLength;  //length in cells, not pixels
	int tapeEndX[] = new int[7];
	int tapeEndY[] = new int[7];
	int fontScalingFactor;
	int stringWidth, stringHeight;
	int index = 0, startAt, cellsToDraw, currentPosition;
	int drawPos;
	String symbol;
	
	FontRenderContext DEFAULT_FONT_RENDER_CONTEXT =
	    new FontRenderContext(null, false, false);
	
	Rectangle2D charBounds;
	
	super.paintComponent(g);
	
	if(tape == null) {
	    return;
	}

	g.setColor(BG_COLOR);
	g.fillRect(0,0,getWidth(), getHeight());

	tapeLength = tape.getSize();
	
	if(areaSize != null) {
	    if(areaSize.width != getSize().width || areaSize.height != getSize().height) { 
		tapeInit = false;
	    }
	}

	areaSize = getSize();
	yAlign = areaSize.height/2;

	cellWidth = getHeight() - 2;
	if(cellWidth > 40) {
	    cellWidth = 40;
	}
	cellHeight = cellWidth;
	fontSize = cellWidth / 2;
	
	if(newLeftCell) {
	    startAt = 1;
	    cellsToDraw = tapeLength - 1;
	    currentPosition = 0;
	} else if(newRightCell) {
	    startAt = 0;
	    cellsToDraw = tapeLength - 1;
	    currentPosition = tapeLength - 2;
	} else {
	    startAt = 0;
	    cellsToDraw = tapeLength;
	    currentPosition = tape.getCurrentPosition();
	}
	
	currentPosition = tape.getCurrentPosition();
	
	if(!tapeInit) {
	    for(filler = 0; cellWidth * (1 + 2*filler) <= areaSize.width; filler++)
	       ;
	    filler += 1;
	    leftMostCell = currentPosition - filler;
	    rightMostCell = leftMostCell + 2*filler;
	    while(-leftMostCell < rightMostCell - tapeLength - 1 && 0 < rightMostCell - tapeLength - 1) {
		leftMostCell--;
		rightMostCell--;
	    }
	    while(-leftMostCell > rightMostCell - tapeLength + 1 && leftMostCell < 0) {
		leftMostCell++;
		rightMostCell++;
	    }
	}


	if(grow) {
	    if(currentPosition - 3 < leftMostCell) {
		while(currentPosition < leftMostCell + 3) {
		    leftMostCell--;
		    rightMostCell--;
		}
	    } else {
		while(currentPosition > rightMostCell - 3) {
		    leftMostCell++;
		    rightMostCell++;
		}
	    }
	    grow = false;
	}
	

	if(origin != tape.getOrigin()) {
	    leftMostCell++;
	    rightMostCell++;
	}
	
	origin = tape.getOrigin();

	tapeInit = true;

	//draw the tape cells

	for(drawPos = 0, i = leftMostCell; i <= rightMostCell; i++, drawPos++) {

	    if(i >= 0 && i < tapeLength)
		symbol = tape.getSymbolAt(i);
	    else
		symbol = tape.getFillSymbol();
	    
	    //indicate current tape cell by coloring it
	    
	    if(i == currentPosition) {
		g.setColor(CURRENT_CELL_COLOR);
	    } else {
		g.setColor(DEFAULT_CELL_COLOR);
	    }
	    
	    g.fillRect((int)((newLeftCellWidth + drawPos + offset) * cellWidth), yAlign - cellHeight/2, 
		       cellWidth, cellHeight);
	    
	    g.setColor(Color.black);
	    g.drawRect((int)((newLeftCellWidth + drawPos + offset) * cellWidth), yAlign - cellHeight/2, 
	           cellWidth, cellHeight);			
	    
	    
	    //determine font size and position of symbol
	    
	    charBounds = g.getFont().getStringBounds(symbol, 
						     DEFAULT_FONT_RENDER_CONTEXT);
	    
	    stringWidth  = (int)Math.ceil(charBounds.getWidth());
	    stringHeight = (int)Math.ceil(charBounds.getHeight());
	    

	    //if symbol is multi-character, font may be adjusted so it will fit
	    fontScalingFactor = (int)Math.ceil((double)stringWidth/(double)cellWidth);
	    g.setFont(new Font(FONT_NAME, FONT_STYLE, fontSize/fontScalingFactor));
	    
	    charBounds = g.getFont().getStringBounds(symbol, 
						     DEFAULT_FONT_RENDER_CONTEXT);
	    
	    stringWidth  = (int)Math.ceil(charBounds.getWidth());
	    stringHeight = (int)Math.ceil(charBounds.getHeight());
	    
	    if(i == tape.getCurrentPosition()) {
		g.setColor(currentTextColor);
	    }
	    
	    
	    //draw the symbol in the tape cell
	    g.drawString(symbol, 
	     (int)((newLeftCellWidth + drawPos + 0.5 + offset) * cellWidth - (stringWidth/2)), 
	    	 (int) (yAlign + (stringHeight/4)));

	    g.setFont(new Font(FONT_NAME, Font.PLAIN, fontSize/(2 * fontScalingFactor)));
	    g.setColor(CELL_INDEX_COLOR);

	    g.drawString(Integer.toString(i - origin), 
	    	 (int)((newLeftCellWidth + drawPos + 0.3 + offset) * cellWidth - (stringWidth/2)), 
	    	 (int)(yAlign + 0.45 * cellHeight));
	   
	    index++;
	}
	
	//draw new cell on right if a cell is currently being added
	g.setColor(DEFAULT_CELL_COLOR);
	g.fillRect((int)((newLeftCellWidth + drawPos + offset) * cellWidth), yAlign - cellHeight/2, 
	   (int)(newRightCellWidth * cellWidth), cellHeight);
	g.setColor(Color.black);
	//g.drawRect((int)((newLeftCellWidth + i + beginTab) * cellWidth), yAlign - cellHeight/2, 
	//   (int)(newRightCellWidth * cellWidth), cellHeight);	
	
	if(newRightCell) {
	    charBounds = g.getFont().getStringBounds(Tape.getFillSymbol(), 
						     DEFAULT_FONT_RENDER_CONTEXT);
	    
	    stringWidth  = (int)Math.ceil(charBounds.getWidth());
	    stringHeight = (int)Math.ceil(charBounds.getHeight());
	    
	    fontScalingFactor = (int)Math.ceil((double)stringWidth/(double)cellWidth);
	    g.setFont(new Font(FONT_NAME, FONT_STYLE, fontSize/fontScalingFactor));
	    
	    charBounds = g.getFont().getStringBounds(Tape.getFillSymbol(), 
						     DEFAULT_FONT_RENDER_CONTEXT);
	    
	    stringWidth  = (int)Math.ceil(charBounds.getWidth());
	    stringHeight = (int)Math.ceil(charBounds.getHeight());
	    
	    //draw the symbol in the tape cell
	    g.drawString(Tape.getFillSymbol(), 
			 (int)((newLeftCellWidth + i + 0.5 + beginTab) * cellWidth - (stringWidth/2)), 
			 (int)(yAlign + (stringHeight/4)));

	    g.setFont(new Font(FONT_NAME, Font.PLAIN, fontSize/(2 * fontScalingFactor)));
	    g.setColor(CELL_INDEX_COLOR);

	    g.drawString(Integer.toString(index), 
	    	 (int)((newLeftCellWidth + i + 0.3 + beginTab) * cellWidth - (stringWidth/2)), 
	    	 (int)(yAlign + 0.45 * cellHeight));
	}
    }
}
