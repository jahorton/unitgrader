package kh.edu.npic.unitgrader.util;

import java.io.File;

import kh.edu.npic.unitgrader.util.filefilter.*;

public final class Classpaths 
{
	private Classpaths() 
	{
	}
	
	public static String forFolderJARs(File folder)
	{
		return forFolderJARs(folder, false, null);
	}
	
	private static String forFolderJARs(File folder, boolean rec, String resolvePath)
	{		
		File[] jars = folder.listFiles(new JavaJARFileFilter());
		
		String cp = "";
		
		for(File jar:jars)
		{
			if(resolvePath == null)
				cp += jar.getPath() + File.pathSeparator;
			else cp += new File(resolvePath).toPath().resolve(jar.toPath()) + File.pathSeparator;
		}
		
		if(rec)
		{
			File[] dirs = folder.listFiles(new DirectoryFilter());
			
			for(File dir:dirs)
			{
				cp += forFolderJARs(dir, true, resolvePath);
			}
		}
		
		return cp;
	}
	
	public static String forLibJARs()
	{
		return forFolderJARs(new File("lib"), true, null);
	}
	
	public static String forLibJARs(String resolvePath)
	{
		return forFolderJARs(new File("lib"), true, resolvePath);
	}
}
