package kh.edu.npic.unitgrader.util.console;

public abstract class ToggleOption extends Option
{
	String[] texts;
	int i;
	
	public ToggleOption(String origText, String altText)
	{
		this(origText, altText, true);
	}
	
	public ToggleOption(String origText, String altText, boolean useFirst)
	{
		super(useFirst ? origText : altText);
		
		texts = new String[]{origText, altText};
		i = useFirst ? 0 : 1;
	}
	
	public void toggle()
	{
		i = 1 - i;
		
		setText(texts[i]);
	}
	
	public boolean isUntoggled()
	{
		return i == 0;
	}
}

