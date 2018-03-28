package kh.edu.npic.unitgrader.grade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;

import kh.edu.npic.unitgrader.grade.filters.GradedStudentsFilter;
import kh.edu.npic.unitgrader.grade.filters.RecentlyGradedFilter;
import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.util.console.NumericalSelectionMenu;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class TailoredExportOption extends Option
{
	private LMSAssignmentManager<?> manager;

	public TailoredExportOption(LMSAssignmentManager<?> manager)
	{
		super("Write the results to a *.csv file designed for import to " + manager.getName() + ".");
		
		this.manager = manager;
	}

	@Override
	public boolean function()
	{
		/*
		 * Stage one:  user must select a template CSV for the destination LMS.  Ideally, a copy of the gradebook
		 * with an already-existing header entry for the autograder's results.
		 */
		int res;
		
	    JFileChooser chooseOriginalCSV = new JFileChooser();
	    chooseOriginalCSV.setDialogTitle("Select the base CSV (gradebook) file.");
	    chooseOriginalCSV.setCurrentDirectory(DirectoryManager.baseSubmissionSelectedDirectory);
	    chooseOriginalCSV.setFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)", "csv"));
	    res = chooseOriginalCSV.showOpenDialog(null);
	    
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
	    
	    File sourceCSVFile = chooseOriginalCSV.getSelectedFile();
	    
	    /*
	     * Validate the selection - is the selected CSV appropriate?
	     */
	    FileReader in = null;
	    CSVParser parser = null;
	    
	    Map<String, Integer> reducedHeaderMap;
	    Map<String, Integer> originalHeaderMap;
		try
		{
			in = new FileReader(sourceCSVFile);
			parser = new CSVParser(in, CSVFormat.EXCEL.withHeader());
			
			reducedHeaderMap = manager.getCSV_DefaultHeader();
			originalHeaderMap = parser.getHeaderMap();
			parser.close();
			
			// Condition - if the CSV has a header that matches the standard style for its LMS.
			if(!(originalHeaderMap.entrySet().containsAll(reducedHeaderMap.entrySet())))
			{
				System.out.println("CSV doesn't fit the recognized " + manager.getName() + " CSV format!");
				return true;
			}
		}
		catch(IOException e)
		{
			if(in != null) 
			{
				try
				{
					in.close();
				}
				catch (IOException e1) { }
			}
			
			System.err.println("Error occurred when verifying the base template CSV.");
			
			return true;
		}
		
	    JFileChooser chooseExportCSV = new JFileChooser();
	    chooseExportCSV.setDialogTitle("Export to CSV file...");
	    chooseExportCSV.setCurrentDirectory(DirectoryManager.baseSubmissionSelectedDirectory);
	    chooseExportCSV.setFileFilter(new FileNameExtensionFilter("Comma-separated values file (*.csv)", "csv"));
	    res = chooseExportCSV.showSaveDialog(null);
	    
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
	    
	    File destinationFile = chooseExportCSV.getSelectedFile();

	    boolean fullExport = true;  // False - use RecentlyGradedFilter.  True - use GradedStudentsFilter.
	    
	    // TODO:  Triple option - export literally all, all graded, or newly graded.
	    
	    if(manager.getLastExportTimestamp() > 0)  // No reason to ask if we've never done ANY exports.
	    {
	    	res = JOptionPane.showConfirmDialog(null, "Export data only for changes since prior export?", "Export Option", JOptionPane.YES_NO_OPTION);
	    	
	    	if(res == JOptionPane.YES_OPTION)
	    		fullExport = false;
	    }
	    
	    StudentConditionFilter filter;
	    
	    if(fullExport)
	    	filter = new GradedStudentsFilter();
	    else filter = new RecentlyGradedFilter(manager.getLastExportTimestamp());
	    
	    /////////////////
	    
	    // What are the fields corresponding to gradebook entries?
		List<String> fields = new ArrayList<String>(originalHeaderMap.keySet());
		fields.removeAll(reducedHeaderMap.keySet());
		
		// Trim any blanks.
		for(int i=0; i < fields.size(); i++)
		{
			if(fields.get(i).trim().equals(""))
				fields.remove(i--);
		}
		
		// Select the appropriate field, or define a new field. (options)
		// Toward defining a menu for user selection of the field.
		abstract class Resolver<T>
		{
			public abstract T evaluate();
			
			public abstract String toString();
		}
		
		class StringRunner extends Resolver<String> 
		{
			String title;
			String value;
			
			public StringRunner(String value)
			{
				this(value, value);
			}
			
			public StringRunner(String value, String title)
			{
				this.value = value;
				this.title = title;
			}
			
			public String evaluate()
			{
				return value;
			}

			@Override
			public String toString()
			{
				return title;
			}
		}
		
		class NewFieldResolver extends Resolver<String>
		{
			@Override
			public String evaluate()
			{
				try
				{
					while(System.in.available() > 0)
					{
						System.in.read();
					}
				}
				catch (IOException e) { }
				
				System.out.println("Enter a name for this assignment's field in the gradebook: ");
				Scanner input = new Scanner(System.in);
				
				return input.nextLine();
			}
			
			@Override 
			public String toString()
			{
				return "Enter a new field.";
			}
			
		}
		
		NumericalSelectionMenu<Resolver<String>> menu = new NumericalSelectionMenu<Resolver<String>>();
		
		for(String f:fields)
		{
			menu.add(new StringRunner(f));
		}
		
		menu.add(new NewFieldResolver());
		menu.add(new StringRunner(null, "Cancel."));
		
		String fieldName = menu.run().evaluate();

		try
		{
			TailoredCSVExporter.exportToCSV(manager, sourceCSVFile, destinationFile, fieldName, filter);
			
			System.out.println("Enter a name for the final exported archive file: ");
			Scanner input = new Scanner(System.in);
			
			String archiveFile = input.nextLine();
			
			manager.exportUploadArchive(archiveFile, filter);
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Could not write to that destination.");
		}
	    
		return true;
	}

}
