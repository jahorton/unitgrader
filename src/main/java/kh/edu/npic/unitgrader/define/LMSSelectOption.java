package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class LMSSelectOption extends Option
{
	public LMSSelectOption()
	{
		super("Select the Learning Management System used for assignment submissions.");
	}

	@Override
	public boolean function()
	{
		NumericalMenu menu = new NumericalMenu();
		
		class LMSOption extends Option
		{
			private String lms;
			
			public LMSOption(String lms)
			{
				super(lms);
				
				this.lms = lms;
			}

			@Override
			public boolean function()
			{
				if(!lms.equals(TestSpecWriter.state.curr.getLMSIdentifier()))
				{
					System.out.println("LMS set to \"" + lms + "\".");
					
					TestSpecWriter.state.curr.setLMSIdentifier(lms);
					TestSpecWriter.state.dirtyFlag = true;
					TestSpecWriter.state.save();				
				}
				return false;
			}
			
		}
		
		String lms = TestSpecWriter.state.curr.getLMSIdentifier();
		
		if(lms == null)
		{
			System.out.println("Please select the LMS to be used for this assignment's submissions.");
		}
		else
		{
			System.out.println("LMS is presently registered as \"" + lms + "\".  Select which is being used.");
		}
		
		menu.add(new LMSOption("Canvas"));
		menu.add(new LMSOption("Sakai"));
		
		menu.run();
		
		return true;
	}

}
