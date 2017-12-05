package kh.edu.npic.unitgrader.define;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import kh.edu.npic.unitgrader.util.ManualClassLoader;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class CreateNewTestSpecOption extends Option
{
	public CreateNewTestSpecOption()
	{
		super("Create a new test specification.");
	}
	
	@Override
	public boolean function()
	{
		JFileChooser chooseTest = new JFileChooser();

		chooseTest.setCurrentDirectory(DirectoryManager.testSpecSelectedDirectory);
		chooseTest.setDialogTitle("Save as...");
		chooseTest.setFileFilter(new FileNameExtensionFilter("Test Definition Files (*.test)", "test"));

		int res = chooseTest.showSaveDialog(null);

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
		
		String filename = testFile.toString();
		
		if(filename.indexOf('.') == -1)
		{
			testFile = new File(filename + ".test");
		}
		else if(!filename.substring(filename.lastIndexOf('.')).equals(".test"))
		{
			testFile = new File(filename + ".test");
		}
		
		TestSpecification testSpec = new TestSpecification();
		selectLMS(testSpec);
		
		try
		{
			Serialization.toFile(testFile.toString(), testSpec);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Could not construct a new test specification at the desired location.");
			return true;
		}
		
		ManualClassLoader.setClasspath(new File(DirectoryManager.testSpecSelectedDirectory + File.separator + "bin"));
		TestSpecWriter.manageTestSpecificationFile(testFile);

		return true;
	}
	
	private void selectLMS(final TestSpecification testSpec)
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
				System.out.println("LMS set to \"" + lms + "\".");
					
				testSpec.setLMSIdentifier(lms);
				return false;
			}
			
		}
		
		System.out.println("Please select the LMS to be used for this assignment's submissions.");
		
		menu.add(new LMSOption("Canvas"));
		menu.add(new LMSOption("Sakai"));
		
		menu.run();
	}

}
