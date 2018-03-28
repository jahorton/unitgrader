package kh.edu.npic.unitgrader.grade;

import kh.edu.npic.unitgrader.grade.filters.AllStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.FlaggedStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.NewSubmissionFilter;
import kh.edu.npic.unitgrader.grade.filters.SkippedStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.grade.filters.UngradedStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.UnviewedStudentsFilter;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;


public class GradingSelectionOption extends Option
{
	private LMSAssignmentManager<?> manager;

	public GradingSelectionOption(LMSAssignmentManager<?> manager)
	{
		super("Grade/regrade assignments.");
		
		this.manager = manager;
	}

	@Override
	public boolean function()
	{
	    class FilterGradingOption extends Option
	    {
	    	StudentConditionFilter filter;
	    	LMSAssignmentManager<?> manager;
	    	
			public FilterGradingOption(String text, StudentConditionFilter filter, LMSAssignmentManager<?> manager)
			{
				super(text);


				this.filter = filter;
				this.manager = manager;
			}

			@Override
			public boolean function()
			{
				GradingEngine.run(manager, filter);
				return true;
			}
	    	
	    }
	    
	    System.out.println();
	    
	    System.out.println("Any of these options will automatically grade / regrade each student that matches the chosen selection.");
	    
	    NumericalMenu menu = new NumericalMenu();
	    menu.add(new FilterGradingOption("Grade all students' data.", new AllStudentsFilter(), manager));
	    menu.add(new FilterGradingOption("Grade unviewed submissions, skipping any previously skipped submission.", new UnviewedStudentsFilter(), manager));
	    menu.add(new FilterGradingOption("Grade ungraded submissions.", new UngradedStudentsFilter(), manager));
	    menu.add(new FilterGradingOption("Grade only updated submissions.", new NewSubmissionFilter(manager), manager));
	    menu.add(new FilterGradingOption("Grade previously skipped submissions.", new SkippedStudentsFilter(), manager));
	    menu.add(new FilterGradingOption("Grade previously flagged submissions.", new FlaggedStudentsFilter(), manager));
	    menu.add(new EndMenuOption());
	    menu.run();
	    
		return true;
	}

}
