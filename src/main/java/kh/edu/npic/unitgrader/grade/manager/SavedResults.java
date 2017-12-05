package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;

public class SavedResults<TagType extends LMSAssignmentManager.LMSDataTag>
{
	public static final String DATA_FILENAME = "results.xml";
	
	// FIXME:  So, this isn't very encapsulated here...
	
	private File testDirectory; // Whoops.  This is the test spec file's directory, actually.  (Where to find them *.class files?)
	public final TestSpecification testSpec;
	public Map<String, StudentData<TagType>> priorData; // String = Canvas ID or Sakai Gatorlink ID.
	private long lastExportTimestamp = 0;
	
	public long getLastExportTimestamp()
	{
		return lastExportTimestamp;
	}
	
	public void markExportTimestamp()
	{
		lastExportTimestamp = System.currentTimeMillis();
	}
	
	public SavedResults(TestSpecification testCase, File testDirectory)
	{
		if(!testDirectory.isDirectory()) throw new IllegalArgumentException();
		
		if(testCase == null) throw new NullPointerException();
		
		this.priorData = new HashMap<String, StudentData<TagType>>();
		this.testSpec = testCase;
		this.testDirectory = testDirectory;
	}
	
	public static SavedResults<? extends LMSAssignmentManager.LMSDataTag> load(File resultsFile) throws FileNotFoundException
	{
		if(Files.exists(resultsFile.toPath()))
		{
			return ((SavedResults<?>) Serialization.fromFile(resultsFile.toString()));
		}
		else throw new FileNotFoundException();
	}
	
	public void save(File saveFilename)
	{
		try
		{
			Serialization.toFile(saveFilename.toString(), this);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Could not store data!");
		}
		
	}
	
	public File getTestDirectory()
	{
		return testDirectory;
	}
	
	public void setTestDirectory(File testDirectory)
	{
		if(!testDirectory.isDirectory()) throw new IllegalArgumentException();
		
		this.testDirectory = testDirectory;
	}
	
	public boolean merge(SavedResults<TagType> other)
	{
		//if(testDirectory.equals(other.testDirectory))  // The test directory won't match if sourced from diff comps.
		
		if(!testSpec.equals(other.testSpec)) return false; // Cannot merge - the results aren't from the same test spec.
		
		for(Map.Entry<String, StudentData<TagType>> entry:other.priorData.entrySet())
		{
			StudentData<TagType> curData = priorData.get(entry.getKey());
			
			if(curData != null)
			{
				if(curData.getTimestamp() >= entry.getValue().getTimestamp()) continue; // If the imported entry is older or of the same age, ignore it.
			}
			
			priorData.put(entry.getKey(), entry.getValue());
		}
		return true;
	}
}
