package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class SelectExistingTestCase extends Option
{

	public SelectExistingTestCase()
	{
		super("Edit an existing test case's properties.");
	}

	@Override
	public boolean function()
	{
		System.out.println("Select an existing test case to modify: ");
		
		NumericalMenu selectMenu = new NumericalMenu();
		
		class TestCaseOption extends Option
		{
			private final TestCase tc;

			public TestCaseOption(TestCase tc)
			{
				super(tc.getTestCaseFile().toString());
				
				this.tc = tc;
			}

			@Override
			public boolean function()
			{
				TestSpecWriter.manageTestCase(tc);
				return false;
			}
		}
		
		for(TestCase tc:TestSpecWriter.state.curr.testCases)
		{
			selectMenu.add(new TestCaseOption(tc));
		}
		
		selectMenu.add(new EndMenuOption());
		
		selectMenu.run();
		
		return true;
	}

}
