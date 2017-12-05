package kh.edu.npic.unitgrader.main;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;

import kh.edu.npic.unitgrader.grade.FindingSelectionOption;
import kh.edu.npic.unitgrader.grade.GradingSelectionOption;
import kh.edu.npic.unitgrader.grade.ListingSelectionOption;
import kh.edu.npic.unitgrader.grade.ManagementSelectionOption;
import kh.edu.npic.unitgrader.grade.ReportingSelectionOption;
import kh.edu.npic.unitgrader.grade.manager.CanvasAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.SakaiAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.SavedResults;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class ExistingGradingOption extends Option
{

	public ExistingGradingOption()
	{
		super("Load previous grading results and continue analysis.");
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean function()
	{
		int res;
		
		JFileChooser chooseTest = new JFileChooser();
		chooseTest.setDialogTitle("Load existing test result file");
		chooseTest.setSelectedFile(new File(DirectoryManager.baseSubmissionSelectedDirectory, "results.xml"));
		chooseTest.setFileFilter(new javax.swing.filechooser.FileFilter(){

			@Override
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				
				int extStart = f.getName().lastIndexOf('.');
				return (f.getName().substring(extStart).equals(".xml"));
			}

			@Override
			public String getDescription() {
				return "Test Result XML Files (*.xml)";
			}});
		res = chooseTest.showOpenDialog(null);

	    switch(res)
	    {
	    	case JFileChooser.CANCEL_OPTION:
	    		return true;
	    	case JFileChooser.APPROVE_OPTION:
	    		break;
	    	case JFileChooser.ERROR_OPTION:
	    		System.err.println("Error detected in results from file selection.");
	    		return true;
	    	default:
	    		System.err.println("Impossible result from file chooser dialog.");
	    		return true;
	    }
	    
	    SavedResults<?> result;
	    try
		{
			result = SavedResults.load(chooseTest.getSelectedFile());
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Error attempting to open the file.");
			return true;
		}
	    
	    LMSAssignmentManager<?> manager;
	    String lmsName = result.testSpec.getLMSIdentifier();
	    
	    DirectoryManager.testSpecSelectedDirectory = result.getTestDirectory();
	    DirectoryManager.baseSubmissionSelectedDirectory = chooseTest.getCurrentDirectory();
	    
	    if(lmsName.equals("Canvas"))
	    {
	    	manager = new CanvasAssignmentManager(chooseTest.getSelectedFile(), (SavedResults<CanvasAssignmentManager.CanvasData>)result);
	    }
	    else if(lmsName.equals("Sakai"))
	    {
	    	manager = new SakaiAssignmentManager(chooseTest.getSelectedFile(), (SavedResults<SakaiAssignmentManager.SakaiData>)result);
	    }
	    else
	    {
	    	System.err.println("Invalid LMS identifier detected in the Test Specification file.  Cannot continue.");
	    	return true;
	    }
	    
	    NumericalMenu menu = new NumericalMenu();
	    menu.add(new GradingSelectionOption(manager));
	    menu.add(new FindingSelectionOption(manager));
	    menu.add(new ListingSelectionOption(manager));
	    menu.add(new ReportingSelectionOption(manager));
	    menu.add(new ManagementSelectionOption(manager));
	    menu.add(new EndMenuOption());
	    menu.run();
	    
		return true;
	}

}
