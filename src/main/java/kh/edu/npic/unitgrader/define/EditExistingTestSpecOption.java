package kh.edu.npic.unitgrader.define;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import kh.edu.npic.unitgrader.util.ManualClassLoader;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class EditExistingTestSpecOption extends Option
{

	public EditExistingTestSpecOption()
	{
		super("Edit an existing test specification.");
	}

	@Override
	public boolean function()
	{
		JFileChooser chooseTest = new JFileChooser();
		
		chooseTest.setCurrentDirectory(DirectoryManager.testSpecSelectedDirectory);
		chooseTest.setDialogTitle("Open test specification");
		chooseTest.setFileFilter(new FileNameExtensionFilter("Test Definition Files (*.test)", "test"));

		int res = chooseTest.showOpenDialog(null);

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
		
		try
		{
			TestSpecification test = (TestSpecification)Serialization.fromFile(testFile.toString());
			if(test == null)
			{			
				System.err.println("The specified file does not hold a valid test specification!");
				return true;
			}
		}
		catch (ClassCastException e)
		{
			System.err.println("The specified file does not hold a valid test specification!");
			return true;
		}
		
		ManualClassLoader.setClasspath(new File(DirectoryManager.testSpecSelectedDirectory + File.separator + "bin"));
		TestSpecWriter.manageTestSpecificationFile(testFile);

		return true;
	}

}
