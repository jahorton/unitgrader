package kh.edu.npic.unitgrader.define;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class AddNewTestCase extends Option
{

	public AddNewTestCase()
	{
		super("Add a new JUnit test case.");
	}

	@Override
	public boolean function()
	{
		JFileChooser chooseTest = new JFileChooser();

		chooseTest.setCurrentDirectory(DirectoryManager.testSpecSelectedDirectory);
		chooseTest.setDialogTitle("Open JUnit test bytecode file");
		chooseTest.setFileFilter(new FileNameExtensionFilter("Java Class Bytecode (*.class)", "class"));
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
		
		Path workingPath = DirectoryManager.testSpecSelectedDirectory.toPath();
		Path relativePath = workingPath.relativize(testFile.toPath());
		
		File file = relativePath.toFile();
		
		if(testFile.toString() == file.toString())
		{
			System.out.println("Warning - could not establish a relative path for the specified test file!");
		}
		
		TestCase tc;
		
		try
		{
			tc = TestCase.constructForTestingFile(file);
		}
		catch(RuntimeException e)
		{
			System.err.println(e);
			return true;
		}

		TestSpecWriter.state.curr.testCases.add(tc);
		TestSpecWriter.state.dirtyFlag = true;
		
		TestSpecWriter.manageTestCase(tc);
		
		return true;
	}

}
