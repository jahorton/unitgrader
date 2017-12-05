package kh.edu.npic.unitgrader.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public final class ManualClassLoader extends ClassLoader 
{
	static ManualClassLoader loader = new ManualClassLoader(ClassLoader.getSystemClassLoader());
	
	private ManualClassLoader(ClassLoader systemClassLoader) 
	{
		super(systemClassLoader);
	}

	private Class<?> classDefine_internal(String name, byte[] data, int off, int len)
	{
		return defineClass(name, data, off, len);
	}
	
	public static void setClasspath(File directory)
	{
		try
		{
			loader = new ManualClassLoader(new URLClassLoader(new URL[]{directory.toURI().toURL()}));
		}
		catch (MalformedURLException e)
		{
			System.err.println("Error when trying to set classpath!");
		}
	}
	
	public static Class<?> loadClassFromFile(File file) throws IOException
	{
		InputStream input = new FileInputStream(file);
		
	    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	    int data = input.read();
	
	    while(data != -1){
	        buffer.write(data);
	        data = input.read();
	    }
	
	    byte[] classData = buffer.toByteArray();
	    
		return loader.classDefine_internal(null, classData, 0, classData.length);
	}

}
