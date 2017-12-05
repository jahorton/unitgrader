package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class IndividualWeightingOption extends Option
{
	private TestCase tc;
	
	public IndividualWeightingOption(TestCase tc)
	{
		super("Set point weighting of a test.");
		
		this.tc = tc;
	}

	@Override
	public boolean function()
	{
		System.out.println("Select the test method to which you wish to assign points: ");

		NumericalMenu selectMenu = new NumericalMenu();
		
		class SelectOption extends Option
		{
			private final String method;

			public SelectOption(String method)
			{
				super(method + " (" + (tc.tests.get(method)) + " pts)");
				
				this.method = method;
			}

			@Override
			public boolean function()
			{
				System.out.println("Setting a test to be worth zero points removes its weighting.");
				int pts;
				
				do
				{
					pts = NumericalMenu.getIntInput("How many points should the test method " + method + " (currently worth " + tc.tests.get(method) + ") be worth? ");
					
					if(pts < 0)
						System.out.println("Point assignment must be non-negative!");
				}
				while(pts < 0);
				
				tc.tests.put(method, pts);
				TestSpecWriter.state.dirtyFlag = true;
				
				return false;
			}
		}
		
		for(String method:tc.tests.keySet())
		{
			selectMenu.add(new SelectOption(method));
		}
		
		selectMenu.add(new EndMenuOption());
		
		selectMenu.run();
		
		return true;
	}

}
