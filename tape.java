import java.util.ArrayList;
import java.util.Collections;
import java.util.Stack;

/**
 * Contains a single 2-way infinite tape. 
 */
class Tape
{
	private Stack<Character> left;
	private char current;
	private Stack<Character> right;
	
	private final char empty; // the tape's blank symbol
	
	public Tape(String contents, char empty)
	{
		this.empty = empty;
		
		left = new Stack<Character>();
		right = new Stack<Character>();
		
		char[] contentsArray = new StringBuilder(contents.substring(1)).reverse().toString().toCharArray();
		
		for (char c : contentsArray) { right.push(c); }
		
		current = contents.charAt(0);
	}
	
	public char scan()
	{
		return current;
	}
	
	public void write(char c)
	{
		current = c;
	}

	public void move(Direction d)
	{
		switch (d)
		{
			case LEFT:
				right.push(current);
				if (left.empty())
				{
					current = empty;
				}
				else
				{
					current = left.pop();
				}
				break;
			case RIGHT:
				left.push(current);
				if (right.empty())
				{
					current = empty;
				}
				else
				{
					current = right.pop();
				}
				break;
		}
	}
	
	@Override
	public String toString()
	{
		ArrayList<Character> leftArray = new ArrayList<Character>(left);
		ArrayList<Character> rightArray = new ArrayList<Character>(right);
		
		Collections.reverse(rightArray);
		
		ArrayList<Character> outputArray = new ArrayList<Character>();
		outputArray.add(empty);
		outputArray.addAll(leftArray);
		outputArray.add(current);
		outputArray.addAll(rightArray);
		outputArray.add(empty);
		
		StringBuilder output = new StringBuilder("|");
		
		for (int i = 0; i < outputArray.size(); i++)
		{
			if (i == (leftArray.size() + 1))
			{
				output.append("[" + outputArray.get(i) + "]|");
			}
			else
			{
				output.append(" " + outputArray.get(i) + " |");
			}
		}
		
		return output.toString();
	}
}