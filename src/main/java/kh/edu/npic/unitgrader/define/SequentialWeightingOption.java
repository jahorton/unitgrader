package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class SequentialWeightingOption extends Option
{
	private TestCase tc;
	
	public SequentialWeightingOption(TestCase tc)
	{
		super("Set each test's weighting sequentially.");
		
		this.tc = tc;
	}

	@Override
	public boolean function()
	{
		System.out.println("Setting a test to be worth zero points removes its weighting.");
		
		for(String method:tc.tests.keySet())
		{
			int pts;
			
			do
			{
				pts = NumericalMenu.getIntInput("How many points should the test method " + method + " (currently worth " + tc.tests.get(method) + ") be worth? ");
				
				if(pts < 0)
					System.out.println("Point assignment must be non-negative!");
			}
			while(pts < 0);
			
			tc.tests.put(method, pts);
		}

		TestSpecWriter.state.dirtyFlag = true;

		return true;
	}

}
