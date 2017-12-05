package kh.edu.npic.unitgrader.grade;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Set;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

import kh.edu.npic.unitgrader.grade.results.TestResult;
import kh.edu.npic.unitgrader.util.ManualClassLoader;
import kh.edu.npic.unitgrader.util.Serialization;

// Success - manually loading the .class (compiled) version of a test and using
// it works here!
//
// Confirmed when removing this project's version of the test and using the one from
// Project Stage 2's folder!

public class TestCaseRunner 
{
	public static void main(String[] args) 
	{
		if(args.length == 0)
		{
			System.out.println("This program requires input of a JUnit test case .class file.");
			return;
		}
		
		// Select a single JUnit test file for the operation.

		File testFile = new File(args[0]);
		String testName = args[1];
		Class<?> test;
		
		try 
		{
			test = ManualClassLoader.loadClassFromFile(testFile);
		} 
		catch (IOException e) 
		{
			//e.printStackTrace();
			System.err.println(Serialization.toXML(e));
			return;
		}
		
		Result result;
		
		PrintStream sysOut = System.out;
		System.setOut(new PrintStream(new ByteArrayOutputStream()));
		
		PrintStream sysErr = System.err;
		System.setErr(new PrintStream(new ByteArrayOutputStream()));
		
		try
		{
			result = JUnitCore.runClasses(test);
		}
		catch(Exception e)
		{
			System.err.println(Serialization.toXML(e));
			killStillRunningJUnitTestcaseThreads();
			return;
		}
		
		System.setOut(sysOut);
		System.setErr(sysErr);
		
	    TestResult exportableResult = new TestResult(result, testName);
	       
	    //File outFileName = new File(test.getCanonicalName() + ".txt");
	    
	    String xmlExport = Serialization.toXML(exportableResult);
	    
//	    try 
//	    {
//			PrintStream out = new PrintStream(outFileName);
//			out.print(xmlExport);
//		}
//	    catch (FileNotFoundException e) 
//	    {
//	    	System.err.println(Serialization.toXML(e));
//	    	//System.err.println("Could not create the log file for student corresponding to directory " + System.getProperty("user.dir"));
//		}
	    System.out.println();
	    System.out.println();
	    
	    killStillRunningJUnitTestcaseThreads();
	    
	    System.out.println(xmlExport);
	}

	// Thanks to http://stackoverflow.com/questions/11088830/using-runclasses-junit-tests-not-shutting-down-after-timing-out-if-test-class.
	@SuppressWarnings("deprecation")
	private static void killStillRunningJUnitTestcaseThreads() {
	    Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
	    for (Thread thread : threadSet) {
	        if (!(thread.isDaemon())) {
	            final StackTraceElement[] threadStackTrace = thread.getStackTrace();
	            if (threadStackTrace.length > 1) {
	                StackTraceElement firstMethodInvocation = threadStackTrace[threadStackTrace.length - 1];
	                if (firstMethodInvocation.getClassName().startsWith("org.junit")) {
	                    // HACK: must use deprecated method
	                    thread.stop();
	                }
	            }
	        }
	    }
	}
}
