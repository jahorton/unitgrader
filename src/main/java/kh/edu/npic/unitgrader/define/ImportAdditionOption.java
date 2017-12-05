package kh.edu.npic.unitgrader.define;

import java.io.File;
import java.nio.file.Path;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class ImportAdditionOption extends Option
{

	public ImportAdditionOption()
	{
		super("Add a new import file.");
	}

	@Override
	public boolean function()
	{
		// Add a new import file -> FileChooser, relativize.
		JFileChooser chooseTest = new JFileChooser();

		chooseTest.setCurrentDirectory(DirectoryManager.testSpecSelectedDirectory);
		chooseTest.setDialogTitle("Select file for import");
		chooseTest.setFileFilter(new FileFilter(){

			@Override
			public boolean accept(File f)
			{
				return true;
			}

			@Override
			public String getDescription()
			{
				return "All Files (*.*)";
			}
			
		});
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
			System.out.println("Warning - could not establish a relative path for the specified import file!");
		}

		TestSpecWriter.state.curr.imports.add(file);
		TestSpecWriter.state.dirtyFlag = true;
		
		return true;
	}

}
