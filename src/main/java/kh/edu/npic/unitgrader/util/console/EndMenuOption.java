package kh.edu.npic.unitgrader.util.console;

public class EndMenuOption extends Option {

	public static String DEFAULT_TEXT = "Return to previous menu.";
	
	public EndMenuOption()
	{
		super(DEFAULT_TEXT);
	}
	
	public EndMenuOption(String text)
	{
		super(text);
	}
	
	@Override
	public boolean function() 
	{
		return false;
	}

}
