package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.util.List;
import java.util.Map;

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
public interface LMSAssignmentManager<TagType extends LMSAssignmentManager.LMSDataTag<TagType>> extends Iterable<StudentData<TagType>>
{
	public static final String EXPORT_INTERMEDIATE_FOLDER = ".exports";
	public static final String EXPORT_FINALIZED_FOLDER = "_results";
	public static final String EXPORT_FINALIZED_FILENAME = "export.zip";
	
	public void save();
	
	public String getName();
	
	// Exports the grading comments for each student.
	public void exportComments();
	
	// The represented analyses and results only apply for this TestSpecification.
	// We throw them out if a different spec is provided at a later point.
	public TestSpecification getTestSpecification();
	
	// These are used for some of our filters - they help us track what work has and hasn't been done.
	public long getLastExportTimestamp();
	public void markExportTimestamp();
	
	// Tracks the status of a student's submissions (or lack thereof).
	public StudentFolderStatus isStudentFolderPresent(StudentData<TagType> data);
	
	// Cleans the build folder, etc for a given student, restoring it to match the original submission.
	public boolean resetStudentFolder(StudentData<TagType> data);
	
	// Gets the StudentData object for a student with the provided id.
	public StudentData<TagType> getStudentData(String id);
	
	// Sets the folder containing test data to be run upon student submissions.
	public void setTestDirectory(File testDir);
	
	// Allows the results of separate grading runs to be merged into a single results file.
	public boolean mergeResults(SavedResults<TagType> setToMerge);
	
	// Facilitate searching for specific students by name, instead of ID.
	public List<StudentData<TagType>> matchLastname(String str);
	public List<StudentData<TagType>> matchFirstname(String str);
	
	// Maps student IDs to their data.
	public Map<String, StudentData<TagType>> getStudentIDMap();
	
	// Utilized to provide CSV export functionality.
	public String getCSV_IDField();
	//public String getCSV_NameField();
	public Map<String, Integer> getCSV_DefaultHeader();
	
	// Prepares the files needed to provide student feedback for the specified LMS.
	public void exportUploadArchive(String filename, StudentConditionFilter filter);
	
	/**
	 * The base type for any LMS-specific data.
	 * @author Joshua A. Horton
	 *
	 */
	static interface LMSDataTag<T extends LMSDataTag<T>>
	{
		StudentFolderStatus getFolderStatus(StudentData<T> data);
	}
}
