package kh.edu.npic.unitgrader.util;

import java.io.File;

import kh.edu.npic.unitgrader.util.filefilter.*;

public final class Classpaths 
{
	private Classpaths() 
	{
	}
	
	public static String getCurrent( ) {
		String cp = System.getProperty("java.class.path");
		String[] cpElements = cp.split(File.pathSeparator);
		
		StringBuilder cpBuilder = new StringBuilder();
		
		// For Windows:  \jre\lib\ denotes a Java system library.
		// We'll let Java handle its own requirements for the classpath.
		String jreLibFilter = File.separator + "jre" + File.separator + "lib" + File.separator;
		
		for(String s:cpElements)
		{
			if(!s.contains(jreLibFilter))
			{
				cpBuilder.append(s);
				cpBuilder.append(File.pathSeparator);
			}
		}
		
		return cpBuilder.toString();
	}
}
