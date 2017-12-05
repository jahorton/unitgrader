package kh.edu.npic.unitgrader.grade;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class ReportingSelectionOption extends Option
{
	LMSAssignmentManager<?> manager;

	public ReportingSelectionOption(LMSAssignmentManager<?> manager)
	{
		super("Reporting options for grading results.");
		this.manager = manager;
	}

	@Override
	public boolean function()
	{
		NumericalMenu menu = new NumericalMenu();
		menu.add(new TailoredExportOption(manager));
	    menu.add(new DefaultCSVExportOption(manager));
	    menu.add(new CommentExportOption(manager));
	    menu.add(new DataEnumerationOption(manager));
	    menu.add(new EndMenuOption());
	    menu.run();
	    
		return true;
	}

}
