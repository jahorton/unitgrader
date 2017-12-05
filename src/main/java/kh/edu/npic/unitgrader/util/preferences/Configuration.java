package kh.edu.npic.unitgrader.util.preferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

import kh.edu.npic.unitgrader.util.Serialization;

/**
 * For XML import and export of persistent program options.
 * 
 * @author Joshua A. Horton
 *
 */
public class Configuration
{
	public static final String CONFIG_FILENAME = "config.ini";
	
	private static Configuration liveConfig;
	
	static
	{
		if(Files.exists(new File(CONFIG_FILENAME).toPath()))
		{
			liveConfig = (Configuration) Serialization.fromFile(CONFIG_FILENAME);
		}
		else
		{
			liveConfig = new Configuration();
			try
			{
				Serialization.toFile(CONFIG_FILENAME, liveConfig);
			}
			catch (FileNotFoundException e)
			{
				System.err.println("Could not create the program's global initialization file: " + CONFIG_FILENAME);
			}
		}
	}
	
	private File testFileInitialDirectory;
	private File submissionInitialDirectory;
	
	private Configuration()
	{
		testFileInitialDirectory = new File(System.getProperty("user.dir"));
		submissionInitialDirectory = new File(System.getProperty("user.dir"));
	}
	
	public static Configuration get()
	{
		return liveConfig;
	}
	
	public File getInitialTestFileDirectory()
	{
		return this.testFileInitialDirectory;
	}
	
	public File getInitialSubmissionDirectory()
	{
		return this.submissionInitialDirectory;
	}
	
	private void saveSettings()
	{
		try
		{
			Serialization.toFile(CONFIG_FILENAME, liveConfig);
		}
		catch (FileNotFoundException e)
		{
			System.err.println("Could not save the program's global initialization file: " + CONFIG_FILENAME);
		}
	}
	
	public void setInitialTestFileDirectory(File f)
	{
		if(Files.exists(f.toPath()) && f.isDirectory())
		{
			this.testFileInitialDirectory = f;
			saveSettings();
		}
		else throw new IllegalArgumentException();
	}
	
	public void setInitialSubmissionDirectory(File f)
	{
		if(Files.exists(f.toPath()) && f.isDirectory())
		{
			this.submissionInitialDirectory = f;
			saveSettings();
		}
		else throw new IllegalArgumentException();
	}
}
