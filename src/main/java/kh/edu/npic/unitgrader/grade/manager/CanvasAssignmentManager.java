package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.zeroturnaround.zip.ZipUtil;

import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.filefilter.ZipFilter;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class CanvasAssignmentManager implements LMSAssignmentManager<CanvasAssignmentManager.CanvasData>
{	
	public class CanvasData implements LMSAssignmentManager.LMSDataTag
	{
		private String originalZipName;
		
		public CanvasData(String originalZip)
		{
			this.originalZipName = originalZip;
		}
		
		public String getArchiveName()
		{
			return originalZipName;
		}
	}
	
	//private final File baseDirectory;
    // ID
	private SavedResults<CanvasData> results;

	private Map<String, StudentData<CanvasData>> idDataMap;
	private SortedSet<StudentData<CanvasData>> sortedEntries;
	private Map<String, File> zipMap;
	
	private File resultsFile;

	public CanvasAssignmentManager(File existingResultsFile, SavedResults<CanvasData> existingResults)
	{
		results = existingResults;
			
		//baseDirectory = existingResultsFile.getParentFile();
		resultsFile = existingResultsFile;
		
		init();
	}
	
	public CanvasAssignmentManager(File baseDirectory, TestSpecification testCase, File testDirectory)
	{
		//this.baseDirectory = baseDirectory;
		this.resultsFile = new File(baseDirectory, SavedResults.DATA_FILENAME);
		
		results = new SavedResults<CanvasData>(testCase, testDirectory);
		
		init();
	}
	
	private void init()
	{
		// Load all data existing in the submission directory?
		File[] fileArray = DirectoryManager.baseSubmissionSelectedDirectory.listFiles(new ZipFilter());
		idDataMap = new HashMap<String, StudentData<CanvasData>>(fileArray.length);
		zipMap = new HashMap<String, File>(fileArray.length);
		
		/*
		 * Init method core logic:
		 * 
		 * 1) Do we have an existing record of this student's data?  If so, just use the thing!
		 *    - If it's not current, who cares?  They can always reset the folder, anyway.
		 * 2) At the end of init(), both the idDataMap and sortedEntries map should contain exactly the same elements.
		 */
		
		for(File f:fileArray)
		{
			String[] components = f.getName().split("_");

			if(components.length < 3) continue;
			
			ArrayList<String> nameComps = new ArrayList<String>(Arrays.asList(components[0].split("-")));
			nameComps.remove("");
			nameComps.remove("late");
			if(nameComps.size() < 2) continue;
			
			String last = nameComps.get(0);
			String first = nameComps.get(nameComps.size()-1);

			String id = components[1];
			String textStamp = components[2];
			
			long timestamp = Long.parseLong(textStamp);
			
			StudentData<CanvasData> prevData = null;
			
			// If we haven't already tried to build a StudentData instance on any pass yet... (does NOT check against old SavedResults instances!)
			if(idDataMap.get(id) != null)
			{
				prevData = idDataMap.get(id);
				
				if(prevData.getTimestamp() >= timestamp)
				{
					idDataMap.put(id, results.priorData.get(id));
					continue;
				}
			}
			
			// It's the "latest" file we know, based on currently-analyzed timestamps.  Mark it in the records!
			StudentData<CanvasData> data;
			
			if(prevData != null)
			{
				// Construct a placeholder for the most current file - use the old one's data for efficiency.
				// It will replace the old placeholder version.
				data = new StudentData<CanvasData>(prevData.first, prevData.last, prevData.id, prevData.getBaseFolder(), timestamp);
				data.setTag(prevData.getTag());
			}
			else
			{
				data = new StudentData<CanvasData>(first, last, id, new File(getFolderName(first, last, id)), timestamp);
				data.setTag(new CanvasData(DirectoryManager.baseSubmissionSelectedDirectory.toPath().relativize(f.toPath()).toString()));
			}

			idDataMap.put(id, data);
			zipMap.put(id, f);
		}
		
		// At this stage, we have a placeholder StudentData for the most current file.  Did we already have data for it from a prior run?
		// (if so, we'd better copy over / match that data!)
		
		HashSet<String> errorKeys = new HashSet<String>();
		
		for(Map.Entry<String, StudentData<CanvasData>> entry:idDataMap.entrySet())
		{
			StudentData<CanvasData> oldData = results.priorData.get(entry.getKey());
			StudentData<CanvasData> curData = idDataMap.get(entry.getKey()); 
			
			String first = entry.getValue().first;
			String last = entry.getValue().last;
			
			File folder = new File(DirectoryManager.baseSubmissionSelectedDirectory, curData.getBaseFolder().toString());
			
			if(oldData == null)
			{
				try
				{
					ZipUtil.unpack(zipMap.get(entry.getKey()), folder);
					results.priorData.put(entry.getKey(), entry.getValue());
				}
				catch(RuntimeException e)
				{
					System.err.println("Error opening student submission for \"" + last + ", " + first + ": \n" + e);
					errorKeys.add(entry.getKey());
				}
			}
			else
			{
				idDataMap.put(entry.getKey(), oldData);
				
				if(!folder.exists()) // If it's imported from delegated work, we'll need to unzip it on the spot for our own perusal.
				{
					ZipUtil.unpack(zipMap.get(entry.getKey()), folder);
				}
			}
		}
		
		for(String id:errorKeys)
		{
			// TODO:  Don't remove the student info from the map... but remove the zip map entry.
			// Self note - probably should auto-create folders for students based on CSV imported data when
			// implementing that option.
			
			idDataMap.remove(id);
			zipMap.remove(id);
		}
		
		Comparator<StudentData<CanvasData>> comparer = new Comparator<StudentData<CanvasData>>()
		{

			@Override
			public int compare(StudentData<CanvasData> o1, StudentData<CanvasData> o2)
			{
				int res = o1.last.compareTo(o2.last);
				
				if(res != 0) return res;
				
				res = o1.first.compareTo(o2.first);
				
				if(res != 0) return res;
				
				return o1.id.compareTo(o2.id);
			}
		};
		
		sortedEntries = new TreeSet<StudentData<CanvasData>>(comparer);
		sortedEntries.addAll(results.priorData.values());
	}
	
	private String getFolderName(String first, String last, String id)
	{
		return last + ", " + first + " CanvasID " + id;
	}
	
	public Map<String, StudentData<CanvasData>> getStudentIDMap()
	{
		return Collections.unmodifiableMap(idDataMap);
	}
	
	public Iterator<StudentData<CanvasData>> iterator()
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

	@Override
	public void exportComments()
	{
		File commentDirectory = new File(DirectoryManager.baseSubmissionSelectedDirectory, "Comments");
		commentDirectory.mkdir();
		
		// Love that I can write this.
		for(StudentData<CanvasData> data:this)
		{
			String comment = data.getComments();
			
			if(comment == null) continue;
			
			File destFile = new File(new File(DirectoryManager.baseSubmissionSelectedDirectory, data.getBaseFolder().toString()), "comments.txt");
			File destFile2 = new File(commentDirectory, data.first + " " + data.last + " comments.txt");
			
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
			
			try
			{
				out = new PrintWriter(new FileOutputStream(destFile2));
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
	
	public StudentFolderStatus isStudentFolderPresent(StudentData<CanvasData> data)
	{
		StudentData<CanvasData> folderData = idDataMap.get(data.id);
		
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

	@Override
	public boolean resetStudentFolder(StudentData<CanvasData> data)
	{
		StudentFolderStatus status = isStudentFolderPresent(data);
		
		if(status == StudentFolderStatus.MISSING) return false;
		
		FileVisitor<Path> visitor = new SimpleFileVisitor<Path>(){
	        @Override
	        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
	            throws IOException
	        {
	            Files.delete(file);
	            return FileVisitResult.CONTINUE;
	        }
		};
	
		try
		{
			Files.walkFileTree(data.getBaseFolder().toPath(), visitor);
		}
		catch (NoSuchFileException e)
		{
			
		}
		catch (IOException e)
		{
			System.err.println("Could not clean old data from the directory.");
			return false;
		}

		
		StudentData<CanvasData> folderData = idDataMap.get(data.id);
		
		data.reset(folderData.getBaseFolder(), folderData.getTimestamp());
		
		File folder = new File(DirectoryManager.baseSubmissionSelectedDirectory, folderData.getBaseFolder().toString());
		ZipUtil.unpack(zipMap.get(data.id), folder);
		data.getTag().originalZipName = DirectoryManager.baseSubmissionSelectedDirectory.toPath().relativize(zipMap.get(data.id).toPath()).toString();
		
		return true;
	}

	@Override
	public boolean mergeResults(SavedResults<CanvasData> setToMerge)
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
	public List<StudentData<CanvasData>> matchLastname(String str)
	{
		str = str.toLowerCase();
		
		LinkedList<StudentData<CanvasData>> matches = new LinkedList<StudentData<CanvasData>>();
		
		for(StudentData<CanvasData> data:results.priorData.values())
		{
			if(data.last.toLowerCase().contains(str))
				matches.add(data);
		}
		
		return matches;
	}

	@Override
	public List<StudentData<CanvasData>> matchFirstname(String str)
	{
		str = str.toLowerCase();
		
		LinkedList<StudentData<CanvasData>> matches = new LinkedList<StudentData<CanvasData>>();
		
		for(StudentData<CanvasData> data:results.priorData.values())
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
//		return "Student";
//	}

	@Override
	public Map<String, Integer> getCSV_DefaultHeader()
	{
		// The base Canvas header.
		Map<String, Integer> headerMap = new HashMap<String, Integer>();
		headerMap.put("Student", 0);
		headerMap.put("ID", 1);
		headerMap.put("SIS User ID", 2);
		headerMap.put("SIS Login ID", 3);
		headerMap.put("Section", 4);
		
		return headerMap;
	}
	
	@Override
	public String getName()
	{
		return "Canvas";
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
	public StudentData<CanvasData> getStudentData(String id)
	{
		return idDataMap.get(id);
	}
	
	@Override
	public void exportUploadArchive(String filename, StudentConditionFilter filter)
	{
		File submissionFolder = DirectoryManager.baseSubmissionSelectedDirectory;
		
		exportComments();
		
		File intermediateFolder = new File(submissionFolder, EXPORT_INTERMEDIATE_FOLDER);
		if(!intermediateFolder.exists())
			intermediateFolder.mkdir();
		
		File finalizedFolder = new File(submissionFolder, EXPORT_FINALIZED_FOLDER);
		if(!finalizedFolder.exists())
			finalizedFolder.mkdir();
		
		for(StudentData<CanvasData> data:this)
		{
			if(!filter.matches(data))
				continue;
			
			File studentBaseFolder = new File(DirectoryManager.baseSubmissionSelectedDirectory, data.getBaseFolder().toString());
			
			// Does NOT include the root directory in the *.zip!  This version is recursive with subdirectories, unlike those with listed files.
			ZipUtil.pack(studentBaseFolder, new File(intermediateFolder, data.getTag().originalZipName), false);
		}
		
		// Package ALL the things!
		ZipUtil.pack(intermediateFolder, new File(finalizedFolder, filename));
	}
}
