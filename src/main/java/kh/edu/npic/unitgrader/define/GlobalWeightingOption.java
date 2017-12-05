package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class GlobalWeightingOption extends Option
{
	private TestCase tc;
	
	public GlobalWeightingOption(TestCase tc)
	{
		super("Fill in all weightings identically.");
		
		this.tc = tc;
	}

	@Override
	public boolean function()
	{
		System.out.println("Setting tests to be worth zero points removes their weighting.");
		int pts;
		
		do
		{
			pts = NumericalMenu.getIntInput("How many points should each method be worth? ");
			
			if(pts < 0)
				System.out.println("Point assignment must be non-negative!");
		}
		while(pts < 0);
		
		for(String method:tc.tests.keySet())
		{
			tc.tests.put(method, pts);
		}
		
		TestSpecWriter.state.dirtyFlag = true;
		
		return true;
	}

}
