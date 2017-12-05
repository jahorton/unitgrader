package kh.edu.npic.unitgrader.grade;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class DefaultCSVExportOption extends Option
{
	private LMSAssignmentManager manager;

	public DefaultCSVExportOption(LMSAssignmentManager manager)
	{
		super("Write the results to a generic *.csv file, importable to Excel.");
		
		this.manager = manager;
	}

	@Override
	public boolean function()
	{
		int res;
		
	    JFileChooser chooseBaseFolder = new JFileChooser();
	    chooseBaseFolder.setDialogTitle("Export to CSV file...");
	    chooseBaseFolder.setCurrentDirectory(DirectoryManager.baseSubmissionSelectedDirectory);
	    chooseBaseFolder.setFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)", "csv"));
	    res = chooseBaseFolder.showSaveDialog(null);
	    
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
	    
	    File destinationFile = chooseBaseFolder.getSelectedFile();

	    try
		{
			DefaultCSVExporter.exportToCSV(manager, destinationFile);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Could not write to that destination.");
		}
	    
		return true;
	}

}
