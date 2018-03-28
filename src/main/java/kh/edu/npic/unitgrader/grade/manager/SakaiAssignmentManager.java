package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

import org.zeroturnaround.zip.ZipUtil;

import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.filefilter.DirectoryFilter;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

// Sakai timestamps:
// 2014 11 26 04 53 21714
// DATE------
//            GMT TIME.
//            hr mn

public class SakaiAssignmentManager extends LMSAssignmentManager<SakaiAssignmentManager.SakaiData>
{
	public class SakaiData implements LMSAssignmentManager.LMSDataTag<SakaiData>
	{
		@Override
		public StudentFolderStatus getFolderStatus(StudentData<SakaiData> data) {
			return SakaiAssignmentManager.this.isStudentFolderPresent(data);
		}
		
		@Override
		public boolean resetStudentFolder(StudentData<SakaiData> data) {
			return SakaiAssignmentManager.this.resetStudentFolder(data);
		}
	}
	
	//private final File baseDirectory;  // No longer used; this is replaced by the DirectoryManager.
	            // ID
	
	public SakaiAssignmentManager(File existingResultsFile, SavedResults<SakaiData> existingResults)
	{
		super("Sakai", existingResultsFile, existingResults);
	}
	
	public SakaiAssignmentManager(File baseDirectory, TestSpecification testCase, File testDirectory)
	{
		super("Sakai", baseDirectory, testCase, testDirectory);
	}
	
	protected void init()
	{
		// Load all data existing in the submission directory?
		File[] fileArray = DirectoryManager.baseSubmissionSelectedDirectory.listFiles(new DirectoryFilter());
		idDataMap = new HashMap<String, StudentData<SakaiData>>(fileArray.length);
		
		for(File f:fileArray)
		{
			if(!isSubmission(f))
			{
				continue;
			}
			
			String id = getSubmissionID(f);
			if(id == null)
			{
				continue;
			}
			
			long timestamp;
			try
			{
				timestamp = getSubmissionTimestamp(f);
			}
			catch (Exception e)//(FileNotFoundException e)
			{
				e.printStackTrace(); // TODO:  Is there a more proper/graceful way to handle this?
				System.exit(-1);
				return;
			}
			
			StudentData<SakaiData> oldData = results.priorData.get(id);
			
			//else // must load it afresh and do any needed prep.
			
			String name = f.getName();
    		name = name.substring(0, name.indexOf('('));

    		String[] split = name.split(", ");
			
    		Path basePath = DirectoryManager.baseSubmissionSelectedDirectory.toPath();
    		Path submissionPath = f.listFiles(new DirectoryFilter())[0].toPath();
    		
    		StudentData<SakaiData> data = new StudentData<SakaiData>(split[1], split[0], id, basePath.relativize(submissionPath).toFile(), timestamp);
    		
			if(oldData == null)
			{
				results.priorData.put(id, data);
				idDataMap.put(id, data);
			}
			else 
			{
				idDataMap.put(id, results.priorData.get(id));
			}
		}
		
		sortedEntries = new TreeSet<StudentData<SakaiData>>(new LMSAssignmentManager.StudentDataComparator<SakaiData>());
		sortedEntries.addAll(results.priorData.values());
	}
	
	private boolean isSubmission(File submission)
	{
		if(!submission.isDirectory())
			return false;
		
		// Being a Sakai submission thing, there WILL be a single submission subfolder.  Is *that* empty?
		File directory = submission.listFiles(new DirectoryFilter())[0];
		
		int count = directory.list().length;
		
		return count != 0;
	}
	
	private String getSubmissionID(File submission)
	{
		//if(!submission.isDirectory()) throw new IllegalArgumentException();
		
		String name = submission.getName();
		
		if(name.indexOf('(') == -1) return null;
		else
		{
			return name.substring(name.indexOf('(') + 1, name.length()-1);
		}
	}
	
	private long getSubmissionTimestamp(File submission) throws FileNotFoundException
	{
		//if(!submission.isDirectory()) throw new IllegalArgumentException();
		
		File[] files = submission.listFiles(new SakaiTimestampFileFilter());
		
		if(files.length == 0) throw new RuntimeException("isSubmission() did not detect the lack of submission here!");
		
		// Open the file, read its contents.
		Scanner input = new Scanner(new FileInputStream(files[0]));
		
		long val = input.nextLong(); // Is the only line in the file.
		input.close();
		
		return val;
	}

	@Override
	protected List<File> getCommentExportFileList(StudentData<SakaiData> data)
	{
		List<File> files = new ArrayList<File>(1);
		files.add(new File(new File(DirectoryManager.baseSubmissionSelectedDirectory, data.getBaseFolder().toString()), ".." + File.separator + "comments.txt"));
		return files;
	}
	
	@Override
	public boolean resetStudentFolder(StudentData<SakaiData> data)
	{
		StudentFolderStatus status = isStudentFolderPresent(data);
		
		if(status == StudentFolderStatus.MISSING) return false;
		
		StudentData<SakaiData> folderData = idDataMap.get(data.id);
		
		data.reset(folderData.getBaseFolder(), folderData.getTimestamp());
		
		return true;
	}

	@Override
	public Map<String, Integer> getCSV_DefaultHeader()
	{
		//TODO:  Sakai full-auto-export note:  choose fieldname "grades" automatically!  And "grades.csv".
		
		// The base Sakai header.
		Map<String, Integer> headerMap = new HashMap<String, Integer>();
		headerMap.put("Display ID", 0);
		headerMap.put("ID", 1);
		headerMap.put("Last Name", 2);
		headerMap.put("First Name", 3);
		
		return headerMap;
	}
	
	@Override
	public void exportUploadArchive(String filename, StudentConditionFilter filter)
	{
		File submissionFolder = DirectoryManager.baseSubmissionSelectedDirectory;
		
		File finalizedFolder = new File(submissionFolder, EXPORT_FINALIZED_FOLDER);
		if(!finalizedFolder.exists())
			finalizedFolder.mkdir();
		
		exportComments();
		
		// Package ALL the things!
		
		File zipFile = new File(finalizedFolder, filename);
		ZipUtil.pack(submissionFolder, zipFile);
		ZipUtil.removeEntries(zipFile, new String[]{EXPORT_FINALIZED_FOLDER, EXPORT_FINALIZED_FOLDER + File.separator + EXPORT_FINALIZED_FILENAME});
	}
}
