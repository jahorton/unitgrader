package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;
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

public class SakaiAssignmentManager implements LMSAssignmentManager<SakaiAssignmentManager.SakaiData>
{
	public class SakaiData implements LMSAssignmentManager.LMSDataTag<SakaiData>
	{
		@Override
		public StudentFolderStatus getFolderStatus(StudentData<SakaiData> data) {
			return SakaiAssignmentManager.this.isStudentFolderPresent(data);
		}
	}
	
	//private final File baseDirectory;  // No longer used; this is replaced by the DirectoryManager.
	            // ID
	private SavedResults<SakaiData> results;
	private Map<String, StudentData<SakaiData>> idDataMap;
	private SortedSet<StudentData<SakaiData>> sortedEntries;
	
	private File resultsFile;
	
	public SakaiAssignmentManager(File existingResultsFile, SavedResults<SakaiData> existingResults)
	{
		results = existingResults;
			
		//baseDirectory = existingResultsFile.getParentFile();
		resultsFile = existingResultsFile;
		
		init();
	}
	
	public SakaiAssignmentManager(File baseDirectory, TestSpecification testCase, File testDirectory)
	{
		//this.baseDirectory = baseDirectory;
		this.resultsFile = new File(baseDirectory, SavedResults.DATA_FILENAME);
		
		results = new SavedResults<SakaiData>(testCase, testDirectory);
		
		init();
	}
	
	private void init()
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
		
		Comparator<StudentData<SakaiData>> comparer = new Comparator<StudentData<SakaiData>>()
		{

			@Override
			public int compare(StudentData<SakaiData> o1, StudentData<SakaiData> o2)
			{
				int res = o1.last.compareTo(o2.last);
				
				if(res != 0) return res;
				
				return o1.first.compareTo(o2.first);
			}
		};
		
		sortedEntries = new TreeSet<StudentData<SakaiData>>(comparer);
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
		long stamp = input.nextLong(); // Is the only line in the file.
		input.close();
		
		return stamp;
	}
	
	public Map<String, StudentData<SakaiData>> getStudentIDMap()
	{
		return Collections.unmodifiableMap(idDataMap);
	}
	
	public Iterator<StudentData<SakaiData>> iterator()
	{
		return sortedEntries.iterator();
	}

	@Override
	public void save()
	{
		results.save(resultsFile);
	}
	
	public TestSpecification getTestSpecification()
	{
		return results.testSpec;
	}

	public void exportComments()
	{
		// Love that I can write this.
		for(StudentData<SakaiData> data:this)
		{
			String comment = data.getComments();
			
			if(comment == null) continue;
			
			File destFile = new File(new File(DirectoryManager.baseSubmissionSelectedDirectory, data.getBaseFolder().toString()), ".." + File.separator + "comments.txt");
			
			PrintWriter out = null;
			try
			{
				out = new PrintWriter(new FileOutputStream(destFile));
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

	@Override
	public StudentFolderStatus isStudentFolderPresent(StudentData<SakaiData> data)
	{
		StudentData<SakaiData> folderData = idDataMap.get(data.id);
		
		if(folderData == null) return StudentFolderStatus.MISSING;
		
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
	public boolean mergeResults(SavedResults<SakaiData> setToMerge)
	{
		boolean success = this.results.merge(setToMerge);

		if(success)
			this.save();
		
		return success;
	}
	

	@Override
	public void setTestDirectory(File testDir)
	{
		results.setTestDirectory(testDir);
	}

	@Override
	public List<StudentData<SakaiData>> matchLastname(String str)
	{
		str = str.toLowerCase();
		
		LinkedList<StudentData<SakaiData>> matches = new LinkedList<StudentData<SakaiData>>();
		
		for(StudentData<SakaiData> data:results.priorData.values())
		{
			if(data.last.toLowerCase().contains(str))
				matches.add(data);
		}
		
		return matches;
	}

	@Override
	public List<StudentData<SakaiData>> matchFirstname(String str)
	{
		str = str.toLowerCase();
		
		LinkedList<StudentData<SakaiData>> matches = new LinkedList<StudentData<SakaiData>>();
		
		for(StudentData<SakaiData> data:results.priorData.values())
		{
			if(data.first.toLowerCase().contains(str))
				matches.add(data);
		}
		
		return matches;
	}

	@Override
	public String getCSV_IDField()
	{
		return "ID";
	}

//	@Override
//	public String getCSV_NameField()
//	{
//		return "Student Name";
//	}

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
	public String getName()
	{
		return "Sakai";
	}

	@Override
	public long getLastExportTimestamp()
	{
		return results.getLastExportTimestamp();
	}

	@Override
	public void markExportTimestamp()
	{
		results.markExportTimestamp();
	}

	@Override
	public StudentData<SakaiData> getStudentData(String id)
	{
		return idDataMap.get(id);
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
