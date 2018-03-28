package kh.edu.npic.unitgrader.grade;

import kh.edu.npic.unitgrader.grade.filters.FlaggedStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.GradedStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.NewSubmissionFilter;
import kh.edu.npic.unitgrader.grade.filters.NoncompilingStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.RecentlyGradedFilter;
import kh.edu.npic.unitgrader.grade.filters.SkippedStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.grade.filters.UnviewedStudentsFilter;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class ListingSelectionOption extends Option
{
	private LMSAssignmentManager<?> manager;

	public ListingSelectionOption(LMSAssignmentManager<?> manager)
	{
		super("List all students based on selected criteria.");
		
		this.manager = manager;
	}
	
	@Override
	public boolean function()
	{
	    class FilterListingOption extends Option
	    {
	    	StudentConditionFilter filter;
	    	LMSAssignmentManager<?> manager;
	    	String filterExplanation;
	    	
			public FilterListingOption(String text, StudentConditionFilter filter, LMSAssignmentManager<?> manager, String filterExplanation)
			{
				super(text);


				this.filter = filter;
				this.manager = manager;
				this.filterExplanation = filterExplanation;
			}

			@Override
			public boolean function()
			{
				GradingEngine.listMatchingStudents(manager, filter, filterExplanation);
				return true;
			}
	    	
	    }
	    
	    System.out.println();
	    System.out.println("Any of these will list out students matching certain criterion.");
	    
	    NumericalMenu menu = new NumericalMenu();
	    menu.add(new FilterListingOption("List students whose work has been graded.", new GradedStudentsFilter(), manager, "Students whose work has been graded: "));
	    menu.add(new FilterListingOption("List students whose work has been graded since the last export.", new RecentlyGradedFilter(manager.getLastExportTimestamp()), manager, "Students whose work has been graded since the last export: "));
	    menu.add(new FilterListingOption("List students whose work did not compile.", new NoncompilingStudentsFilter(), manager, "Students whose work did not compile: "));
	    menu.add(new FilterListingOption("List students who were skipped.", new SkippedStudentsFilter(), manager, "Students intentionally skipped during grading"));
	    menu.add(new FilterListingOption("List students who were flagged.", new FlaggedStudentsFilter(), manager, "Students flagged during grading for review."));
	    menu.add(new FilterListingOption("List students whose work hasn't been viewed.", new UnviewedStudentsFilter(), manager, "Students whose work has not yet been viewed or analyzed."));
	    menu.add(new FilterListingOption("List students who have new submissions.", new NewSubmissionFilter(manager), manager, "Students with new submissions."));
	    menu.add(new EndMenuOption());
	    
	    menu.run();
	    
		return true;
	}

}
