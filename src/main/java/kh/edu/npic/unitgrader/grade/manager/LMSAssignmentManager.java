package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.util.TestSpecification;

/**
 * Implementations of this interface ought be used to represent LMS-specific functions
 * within the system.  A Sakai implementation and a Canvas implementation have already
 * been coded.
 *  
 * @author Joshua A. Horton
 *
 */
public abstract class LMSAssignmentManager<TagType extends LMSAssignmentManager.LMSDataTag<TagType>> implements Iterable<StudentData<TagType>>
{
	// Internal file management constants.
	public static final String EXPORT_INTERMEDIATE_FOLDER = ".exports";
	public static final String EXPORT_FINALIZED_FOLDER = "_results";
	public static final String EXPORT_FINALIZED_FILENAME = "export.zip";
	
	// Class properties
	protected Map<String, StudentData<TagType>> idDataMap;
	protected SortedSet<StudentData<TagType>> sortedEntries;
	protected SavedResults<TagType> results;
	
	private File resultsFile;
	
	private final String managerName;
	
	// Class constructors
	protected LMSAssignmentManager(String managerName, File existingResultsFile, SavedResults<TagType> existingResults)
	{
		this.managerName = managerName;
		
		results = existingResults;
			
		//baseDirectory = existingResultsFile.getParentFile();
		resultsFile = existingResultsFile;
		
		init();
	}
	
	protected LMSAssignmentManager(String managerName, File baseDirectory, TestSpecification testCase, File testDirectory)
	{
		this.managerName = managerName;
		
		//this.baseDirectory = baseDirectory;
		this.resultsFile = new File(baseDirectory, SavedResults.DATA_FILENAME);
		
		results = new SavedResults<TagType>(testCase, testDirectory);
		
		init();
	}
	
	// Abstract methods.
	
	// Used for initialization.  Performs base folder analysis and builds the list of student submissions.
	protected abstract void init();
	
	// Cleans the build folder, etc for a given student, restoring it to match the original submission.
	public abstract boolean resetStudentFolder(StudentData<TagType> data);
	
	// Utilized to provide CSV export functionality.
	public abstract Map<String, Integer> getCSV_DefaultHeader();
	
	// Prepares the files needed to provide student feedback for the specified LMS.
	public abstract void exportUploadArchive(String filename, StudentConditionFilter filter);
	
	// Defines the desired location to export student comment files as plain text.
	protected abstract List<File> getCommentExportFileList(StudentData<TagType> data);
	
	// Implemented methods
	
	public String getName()
	{
		return this.managerName;
	}
	
	// Facilitate searching for specific students by name, instead of ID.
	public List<StudentData<TagType>> matchLastname(String str)
	{
		str = str.toLowerCase();
		
		LinkedList<StudentData<TagType>> matches = new LinkedList<StudentData<TagType>>();
		
		for(StudentData<TagType> data:results.priorData.values())
		{
			if(data.last.toLowerCase().contains(str))
				matches.add(data);
		}
		
		return matches;
	}

	// Facilitate searching for specific students by name, instead of ID.
	public List<StudentData<TagType>> matchFirstname(String str)
	{
		str = str.toLowerCase();
		
		LinkedList<StudentData<TagType>> matches = new LinkedList<StudentData<TagType>>();
		
		for(StudentData<TagType> data:results.priorData.values())
		{
			if(data.first.toLowerCase().contains(str))
				matches.add(data);
		}
		
		return matches;
	}
	
	// Maps student IDs to their data.
	public Map<String, StudentData<TagType>> getStudentIDMap()
	{
		return Collections.unmodifiableMap(idDataMap);
	}
	
	// The represented analyses and results only apply for this TestSpecification.
	// We throw them out if a different spec is provided at a later point.
	public TestSpecification getTestSpecification()
	{
		return results.testSpec;
	}
	
	public void save()
	{
		results.save(resultsFile);
	}
	
	public Iterator<StudentData<TagType>> iterator()
	{
		return sortedEntries.iterator();
	}
	
	// Tracks the status of a student's submissions (or lack thereof).
	public StudentFolderStatus isStudentFolderPresent(StudentData<TagType> data)
	{
		StudentData<TagType> folderData = idDataMap.get(data.id);
		
		if(folderData == null) return StudentFolderStatus.MISSING;
		if(folderData.getTimestamp() == 0) return StudentFolderStatus.MISSING;
		
		long compResult = data.getTimestamp() - folderData.getTimestamp();
		
		if(compResult < 0)
		{
			return StudentFolderStatus.NEW;
		}
		else if(compResult > 0)
		{
			return StudentFolderStatus.OLD;
		}
		else 
		{
			return StudentFolderStatus.CURRENT;
		}
	}
	
	// Allows the results of separate grading runs to be merged into a single results file.
	public boolean mergeResults(SavedResults<?> setToMerge)
	{
		boolean success = this.results.merge(setToMerge);

		if(success)
			this.save();
		
		return success;
	}
	
	// These are used for some of our filters - they help us track what work has and hasn't been done.
	public long getLastExportTimestamp()
	{
		return results.getLastExportTimestamp();
	}

	public void markExportTimestamp()
	{
		results.markExportTimestamp();
	}

	// Gets the StudentData object for a student with the provided id.
	public StudentData<TagType> getStudentData(String id)
	{
		return idDataMap.get(id);
	}
	
	// Sets the folder containing test data to be run upon student submissions.
	public void setTestDirectory(File testDir)
	{
		results.setTestDirectory(testDir);
	}

	// Utilized to provide CSV export functionality.
	public String getCSV_IDField()
	{
		return "ID";
	}
	
	// Defines a default (null) set of directories to create on comment export.
	protected List<File> getCommentExportDirectories() 
	{
		return null;
	}
	
	// Exports the grading comments for each student.
	public void exportComments()
	{
		List<File> mkdirs = this.getCommentExportDirectories();
		for(File dir: mkdirs)
		{
			dir.mkdir();
		}
		
		// Love that I can write this.
		for(StudentData<TagType> data:this)
		{
			String comment = data.getComments();			
			if(comment == null) continue;
			
			List<File> targets = this.getCommentExportFileList(data);
			
			for(File target: targets)
			{
				PrintWriter out = null;
				try
				{
					out = new PrintWriter(new FileOutputStream(target));
					out.write(comment);
				}
				catch (FileNotFoundException e)
				{
					System.err.println("Error attempting to export comments for student " + data.first + " " + data.last);
				}
				finally
				{
					if(out != null)
						out.close();
				}
			}
		}
	}
	
	/**
	 * The base type for any LMS-specific data.
	 * @author Joshua A. Horton
	 *
	 */
	public static interface LMSDataTag<T extends LMSDataTag<T>>
	{
		StudentFolderStatus getFolderStatus(StudentData<T> data);
		boolean resetStudentFolder(StudentData<T> data);
	}
	
	static class StudentDataComparator<TagType extends LMSAssignmentManager.LMSDataTag<TagType>> implements Comparator<StudentData<TagType>>
	{

		@Override
		public int compare(StudentData<TagType> o1, StudentData<TagType> o2)
		{
			int res = o1.last.compareTo(o2.last);
			
			if(res != 0) return res;
			
			res = o1.first.compareTo(o2.first);
			
			if(res != 0) return res;
			
			return o1.id.compareTo(o2.id);
		}
	};
}
