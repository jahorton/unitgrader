package kh.edu.npic.unitgrader.util.console;

public abstract class Option 
{
	String text;
	
	public Option(String text)
	{
		if(text == null) throw new NullPointerException();
		
		this.text = text;	
	}
	
	protected void setText(String text)
	{
		this.text = text;
	}
	
	/**
	 * Called upon selection of the menu item in order to provide functionality.
	 * 
	 *  @return <code><b>true</b></code> if the menu should continue execution after functionality is complete.
	 *  <code><b>false</b></code> if the menu should complete upon completion of this option's functionality.
	 */
	public abstract boolean function();
}
