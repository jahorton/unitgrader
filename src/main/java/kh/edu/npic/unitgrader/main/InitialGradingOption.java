package kh.edu.npic.unitgrader.main;

import java.io.File;

import javax.swing.JFileChooser;

import kh.edu.npic.unitgrader.grade.GradingEngine;
import kh.edu.npic.unitgrader.grade.filters.AllStudentsFilter;
import kh.edu.npic.unitgrader.grade.manager.CanvasAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.SakaiAssignmentManager;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class InitialGradingOption extends Option
{

	public InitialGradingOption()
	{
		super("Commence student submission grading from scratch.");
	}

	@Override
	public boolean function()
	{
		// What directory houses all the student submissions?  (Sakai formatting)
		int res;
		
	    JFileChooser chooseBaseFolder = new JFileChooser();
	    chooseBaseFolder.setDialogTitle("Select the base folder for student assignments.");
	    chooseBaseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooseBaseFolder.setCurrentDirectory(DirectoryManager.baseSubmissionSelectedDirectory);
	    res = chooseBaseFolder.showOpenDialog(null);
	    
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
	    
	    File baseDirectory = chooseBaseFolder.getSelectedFile();
	    DirectoryManager.baseSubmissionSelectedDirectory = baseDirectory;
	    
		JFileChooser chooseTest = new JFileChooser();
		chooseTest.setDialogTitle("Select the test definition to run on student assignments.");
		chooseTest.setCurrentDirectory(DirectoryManager.testSpecSelectedDirectory);
		chooseTest.setFileFilter(new javax.swing.filechooser.FileFilter(){

			@Override
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				
				int extStart = f.getName().lastIndexOf('.');
				if(extStart < 0) return false;
				else return (f.getName().substring(extStart).equals(".test"));
			}

			@Override
			public String getDescription() {
				return "Test Definition Files (*.test)";
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

		File testFile = chooseTest.getSelectedFile();
		DirectoryManager.testSpecSelectedDirectory = chooseTest.getCurrentDirectory();
		//System.out.println("Selected file's path: " + testFile.getParent());
		//System.out.println("Selected file: " + testFile.getName());
		
		TestSpecification testSpec;
		
		try
		{
			testSpec = (TestSpecification) Serialization.fromFile(testFile.toString());
		}
		catch(ClassCastException e)
		{
			System.err.println("Test file does not contain a proper test definition!");
			return true;
		}
	    
	    System.out.println();

	    String lmsName = testSpec.getLMSIdentifier();
	    
	    if(lmsName.equals("Canvas"))
	    {
	    	CanvasAssignmentManager manager = new CanvasAssignmentManager(baseDirectory, testSpec, new File(testFile.getParent()));
	    	GradingEngine.run(manager, new AllStudentsFilter());
	    }
	    else if(lmsName.equals("Sakai"))
	    {
	    	SakaiAssignmentManager manager = new SakaiAssignmentManager(baseDirectory, testSpec, new File(testFile.getParent()));
	    	GradingEngine.run(manager, new AllStudentsFilter());
	    }
	    else
	    {
	    	System.err.println("Invalid LMS identifier detected in the Test Specification file.  Cannot continue.");
	    	return true;
	    }
	    
		return true;
	}

}
