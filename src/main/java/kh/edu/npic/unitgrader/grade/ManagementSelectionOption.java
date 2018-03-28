package kh.edu.npic.unitgrader.grade;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.SavedResults;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class ManagementSelectionOption extends Option
{
	private final LMSAssignmentManager<?> manager;
	
	public ManagementSelectionOption(LMSAssignmentManager<?> manager)
	{
		super("Manage loaded grading results.");
		
		this.manager = manager;
	}
	
	@Override
	public boolean function()
	{
		NumericalMenu menu = new NumericalMenu();
		
		class MergeOption extends Option
		{
			public MergeOption()
			{
				super("Merge another existing results file into this one.");
			}
			
			@Override
			public boolean function()
			{
				// What directory houses all the student submissions?  (Sakai formatting)
				int res;
				
				JFileChooser chooseTest = new JFileChooser();
				chooseTest.setDialogTitle("Load existing test result file");
				chooseTest.setCurrentDirectory(DirectoryManager.baseSubmissionSelectedDirectory);
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
			    		return false;
			    	case JFileChooser.APPROVE_OPTION:
			    		break;
			    	case JFileChooser.ERROR_OPTION:
			    		System.err.println("Error detected in results from file selection.");
			    		return false;
			    	default:
			    		System.err.println("Impossible result from file chooser dialog.");
			    		return false;
			    }
				
			    SavedResults<?> result;
			    try
				{
					result = SavedResults.load(chooseTest.getSelectedFile());
				}
				catch (FileNotFoundException e)
				{
					System.err.println("Error attempting to open the file.");
					return false;
				}
				
			    boolean mergeSuccess = false;
			    if(manager.getName().equals(result.testSpec.getLMSIdentifier())) {
				    if(manager.mergeResults(result))
				    {
				    	System.out.println("Merge successful.");
				    }
			    }

			    if(!mergeSuccess)
			    {
			    	System.out.println("Requested merge could not be performed.");
			    }

				return false;
			}	
		};
		
		class ReselectTestFileOption extends Option
		{

			public ReselectTestFileOption()
			{
				super("Alter the previously-stored testing directory information.");
			}

			@Override
			public boolean function()
			{
				int res;
				
				JFileChooser chooseTest = new JFileChooser();
				chooseTest.setDialogTitle("Select the test definition to run on student assignments.");
				chooseTest.setCurrentDirectory(DirectoryManager.testSpecSelectedDirectory);
				chooseTest.setFileFilter(new javax.swing.filechooser.FileFilter(){

					@Override
					public boolean accept(File f) {
						if(f.isDirectory()) return true;
						
						int extStart = f.getName().lastIndexOf('.');
						return (f.getName().substring(extStart).equals(".test"));
					}

					@Override
					public String getDescription() {
						return "Test Definition Files (*.test)";
					}});
				res = chooseTest.showOpenDialog(null);
				
			    switch(res)
			    {
			    	case JFileChooser.CANCEL_OPTION:
			    		return false;
			    	case JFileChooser.APPROVE_OPTION:
			    		break;
			    	case JFileChooser.ERROR_OPTION:
			    		System.err.println("Error detected in results from file selection.");
			    		return false;
			    	default:
			    		System.err.println("Impossible result from file chooser dialog.");
			    		return false;
			    }

				File testFile = chooseTest.getSelectedFile();
				
				TestSpecification testSpec;
				
				try
				{
					testSpec = (TestSpecification) Serialization.fromFile(testFile.toString());
				}
				catch(ClassCastException e)
				{
					System.err.println("Test file does not contain a proper test definition!");
					return false;
				}
				
				if(!testSpec.equals(manager.getTestSpecification()))
				{
					System.err.println("Specified test file does not match the originally-conducted test!");
					return false;
				}

				manager.setTestDirectory(chooseTest.getCurrentDirectory());
				DirectoryManager.testSpecSelectedDirectory = chooseTest.getCurrentDirectory();
				
				System.out.println("Operation successful.");
				manager.save();
				
				return false;
			}
			
		}
		
		menu.add(new MergeOption());
		menu.add(new ReselectTestFileOption());
		menu.add(new EndMenuOption());
		menu.run();
		
		return true;
	}

}
