package kh.edu.npic.unitgrader.grade;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.junit.Before;
import org.junit.Test;
import org.zeroturnaround.zip.ZipUtil;

import kh.edu.npic.unitgrader.grade.manager.CanvasAssignmentManager;
import kh.edu.npic.unitgrader.grade.results.TestResult;
import kh.edu.npic.unitgrader.util.ManualClassLoader;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.filefilter.JavaClassFileFilter;
import kh.edu.npic.unitgrader.util.filefilter.JavaSourceFileFilter;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class Test_TestCaseRunner {
	// Make sure the paths are fully resolved as absolute - this is necessary for the child process to do its thing.
	public static final File TEST_BASE_DIRECTORY_ORIGIN = new File("samples/1").getAbsoluteFile();
	public static final File SUBMISSIONS_ARCHIVE = new File("samples/1/submissions.zip").getAbsoluteFile();
	public static final File SUBMISSIONS_BASE_DIRECTORY = new File(".testing/tcr/submissions").getAbsoluteFile();
	
	public static final File TCR_WORKING_PATH = new File(".testing/tcr/submissions/student, test CanvasID 899100").getAbsoluteFile();
	public static final File TEST_SPEC_FILE = new File("samples/1/AssignmentGrader.test").getAbsoluteFile();;
	
	private static TestSpecification TEST_SPEC;
	
	
	// Copies the test files over for a special, in-process run of TestCaseRunner's main in order to ensure its validity.
	public void copyClassFiles() throws IOException
	{
		File testBin = new File(TEST_BASE_DIRECTORY_ORIGIN, "bin");
		File[] testResources = testBin.listFiles(new JavaClassFileFilter());
		
		for(File test: testResources)
		{
			FileInputStream srcStream = null;
			FileOutputStream dstStream = null;
			FileChannel src = null;
			FileChannel dst = null;
			try
			{
				srcStream = new FileInputStream(test);
				src = srcStream.getChannel();
				
				File dstFile = new File(TCR_WORKING_PATH, test.getName());
				dstStream = new FileOutputStream(dstFile);
				dst = dstStream.getChannel();
				
				dst.transferFrom(src, 0, src.size());
			}
			finally
			{
				if(srcStream != null)
				{
					srcStream.close();
				}
				if(dstStream != null)
				{
					dstStream.close();
				}
			}
		}
	}
	
	@Before
	public void setup() throws IOException {	
		try 
		{
			ZipUtil.unpack(SUBMISSIONS_ARCHIVE, SUBMISSIONS_BASE_DIRECTORY);
		} 
		catch (Exception e) 
		{
			fail("Could not unzip the submissions archive file.");
		}
		
		try
		{
			DirectoryManager.baseSubmissionSelectedDirectory = SUBMISSIONS_BASE_DIRECTORY;
			TEST_SPEC = (TestSpecification) Serialization.fromFile(TEST_SPEC_FILE.toString());
		}
		catch(ClassCastException e)
		{
			System.err.println("Test file does not contain a proper test definition!");
			fail("Could not load the test specification file!");
		}
		
		// We don't want to actually use the manager; instead, we're relying on it to pre-extract the submission for us.
		CanvasAssignmentManager manager = new CanvasAssignmentManager(SUBMISSIONS_BASE_DIRECTORY, TEST_SPEC, TEST_BASE_DIRECTORY_ORIGIN);
		assertNotNull(manager.getStudentData("899100"));
		
		// Copy the test resources into the folder!
		copyClassFiles();
	}
	
	// Compiles the source files in the specified directory.
	public void compileSources(File path)
	{
		// Extracted from StudentTester.java
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    
    	DiagnosticCollector<JavaFileObject> managerDiagnostics = new DiagnosticCollector<JavaFileObject>();
	    StandardJavaFileManager fileManager = compiler.getStandardFileManager(managerDiagnostics, null, null);
    	
    	File[] javaSourceArray = path.listFiles(new JavaSourceFileFilter());
    	
    	Iterable<? extends JavaFileObject> filesToCompile =
    	           fileManager.getJavaFileObjects(javaSourceArray);
    	
    	ArrayList<String> options = new ArrayList<String>();
    	options.add("-cp");
    	options.add(path.toString());
    	DiagnosticCollector<JavaFileObject> compilationDiagnostics = new DiagnosticCollector<JavaFileObject>();
    	
    	CompilationTask compileTask = compiler.getTask(null, fileManager, compilationDiagnostics, options, null, filesToCompile);
    	
    	if(javaSourceArray.length == 0)
    	{
    		fail("Could not find anything to compile.");
    	}
    	else
    	{
	    	boolean compResult = compileTask.call();
	    	
	    	if(!compResult)
	    	{
	    		fail("Compilation error:  " + compilationDiagnostics.getDiagnostics().toString());
	    	}
    	}
	}

	@Test
	public void testMain() throws IOException {
		compileSources(TCR_WORKING_PATH);
		
		String testName = TEST_SPEC.testCases.get(0).getTestCaseFile().getName();
		// Because we didn't set the classpath, we must directly load the Assignment class ourselves! 
		ManualClassLoader.loadClassFromFile(new File(TCR_WORKING_PATH, "Assignment.class"));
		
		ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
		ByteArrayOutputStream capturedErr = new ByteArrayOutputStream();
		PrintStream tempOut = new PrintStream(capturedOut);
		PrintStream tempErr = new PrintStream(capturedErr);
		PrintStream sysOut = System.out;
		PrintStream sysErr = System.err;
		
		// Capture System.out so we can verify the output contents.
		System.setOut(tempOut);
		System.setErr(tempErr);
		
		TestCaseRunner.main(new String[] {new File(TCR_WORKING_PATH, testName).getAbsolutePath(), testName});
		
		System.out.flush(); // Ensure everything's completely printed before stopping our output capture.
		System.err.flush();
		System.setOut(sysOut);
		System.setErr(sysErr);
		
		String output = capturedOut.toString();
		String errors = capturedErr.toString();
		
		if(errors.length() > 0) {
			fail("Errors detected during test case execution!");
		} else {
			output = output.substring(output.indexOf("<result>"));
			
			TestResult result = (TestResult)Serialization.fromXML(output);
			
			assertEquals(0, result.crashes.size());
			assertEquals(0, result.failures.size());
		}
	}

}
