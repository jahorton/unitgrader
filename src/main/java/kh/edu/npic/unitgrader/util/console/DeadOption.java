package kh.edu.npic.unitgrader.util.console;

public class DeadOption extends Option
{

	public DeadOption(String text)
	{
		super(text);
	}

	@Override
	public boolean function()
	{
		return true;
	}

}
