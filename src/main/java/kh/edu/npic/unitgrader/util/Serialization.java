package kh.edu.npic.unitgrader.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import com.thoughtworks.xstream.XStream;

public final class Serialization 
{
	private static XStream xstream = new XStream();
	
	static
	{
		xstream = new XStream();
		
		setupAliasing();
	}
	
	private static void setupTestDefinitionAliasing()
	{
		xstream.alias("testspec", TestSpecification.class);
		xstream.alias("tests", TestCase.class);
		xstream.alias("result", kh.edu.npic.unitgrader.grade.results.TestResult.class);
		xstream.alias("failure", kh.edu.npic.unitgrader.grade.results.Failure.class);
		xstream.alias("crash", kh.edu.npic.unitgrader.grade.results.Crash.class);
		xstream.alias("config", kh.edu.npic.unitgrader.util.preferences.Configuration.class);
		xstream.alias("student", kh.edu.npic.unitgrader.grade.manager.StudentData.class);
		
		xstream.omitField(Throwable.class, "suppressedExceptions");
	}
	
	private static void setupAliasing()
	{
		setupTestDefinitionAliasing();
	}
	
	public static String toXML(Object obj)
	{
		return xstream.toXML(obj);
	}
	
	public static Object fromXML(String str)
	{
		return xstream.fromXML(str);
	}
	
	public static Object fromFile(String filename)
	{
		return xstream.fromXML(new File(filename));
	}
	
	public static void toFile(String filename, Object obj) throws FileNotFoundException
	{
		String str = toXML(obj);
		
		File f = new File(filename);
		
		PrintWriter out = new PrintWriter(new FileOutputStream(f));
		out.write(str);
		
		out.close();
	}
}
