package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class CompactCommentingTestOption extends Option
{
	private TestCase tc;
	
	public CompactCommentingTestOption(TestCase tc)
	{
		super("Set the default behavior of automatically-generated comments.");
		this.tc = tc;
	}

	@Override
	public boolean function()
	{
		System.out.println("By default, automatic comments are generated on a per-test-method basis instead of per-test-case.");
		
		boolean setting = NumericalMenu.getYesOrNo("Should an automatic comment be generated for each test method (Y/N)?  ");
		
		if(tc.getCompactCommentsFlag() != setting)
		{
			tc.setCompactCommentsFlag(setting);
			TestSpecWriter.state.dirtyFlag = true;
		}
		
		return true;
	}

}
