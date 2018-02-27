package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.zeroturnaround.zip.ZipUtil;

import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.filefilter.ZipFilter;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class CanvasAssignmentManager extends LMSAssignmentManager<CanvasAssignmentManager.CanvasData>
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
	private Map<String, File> zipMap;

	public CanvasAssignmentManager(File existingResultsFile, SavedResults<CanvasData> existingResults)
	{
		super("Canvas", existingResultsFile, existingResults);
	}
	
	public CanvasAssignmentManager(File baseDirectory, TestSpecification testCase, File testDirectory)
	{
		super("Canvas", baseDirectory, testCase, testDirectory);
	}
	
	protected void init()
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
		
		sortedEntries = new TreeSet<StudentData<CanvasData>>(new LMSAssignmentManager.StudentDataComparator<CanvasData>());
		sortedEntries.addAll(results.priorData.values());
	}
	
	private String getFolderName(String first, String last, String id)
	{
		return last + ", " + first + " CanvasID " + id;
	}

	// Defines a default (null) set of directories to create on comment export.
	@Override
	protected List<File> getCommentExportDirectories() 
	{
		List<File> commentDirs = new ArrayList<File>(1);
		commentDirs.add(new File(DirectoryManager.baseSubmissionSelectedDirectory, "Comments"));
		return commentDirs;
	}
	
	@Override
	protected List<File> getCommentExportFileList(StudentData<CanvasData> data)
	{
		File commentDirectory = this.getCommentExportDirectories().get(0);
		
		List<File> files = new ArrayList<File>(2);
		files.add(new File(new File(DirectoryManager.baseSubmissionSelectedDirectory, data.getBaseFolder().toString()), "comments.txt"));
		files.add(new File(commentDirectory, data.first + " " + data.last + " comments.txt"));
		return files;
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
