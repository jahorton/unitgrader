package kh.edu.npic.unitgrader.grade;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import kh.edu.npic.unitgrader.grade.manager.StudentData;
import kh.edu.npic.unitgrader.grade.results.TestResult;
import kh.edu.npic.unitgrader.util.Classpaths;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.filefilter.DirectoryFilter;
import kh.edu.npic.unitgrader.util.filefilter.JavaClassFileFilter;
import kh.edu.npic.unitgrader.util.filefilter.JavaSourceFileFilter;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class StudentTester {
	
	private StudentTester()
	{
		
	}
	
	private static String lastCompileDiagnostics;
	private volatile static int currentTestCase = 0;
	private volatile static String currentTestCaseName = "";
	
	public static String getLastCompileDiagnostics()
	{
		return lastCompileDiagnostics;
	}
	public static int getCurrentTestCase()
	{
		return currentTestCase;
	}
	
	public static String getCurrentTestCaseName()
	{
		return currentTestCaseName;
	}

	public static Map<TestCase, TestResult> run(TestSpecification testSpec, StudentData<?> studentData) throws TestingException
	{	
		currentTestCase = 0;
		currentTestCaseName = "";
		
    	String name = studentData.first + " " + studentData.last;
    	studentData.markTested();
		
    	File submission = new File(DirectoryManager.baseSubmissionSelectedDirectory, studentData.getCodeFolder().toString());

		// Step 1:  Remove files as specified by the test spec. 	
    	
    	for(File f:testSpec.deletes)
    	{    		
    		// Copy the selected test case file into their submission directory!
	    	String destTestAddress = submission.getPath() + File.separator + f.getName();
	    	try 
	    	{
	    		Files.delete(new File(destTestAddress).toPath());
			}
	    	catch(NoSuchFileException e)
	    	{
	    		// Do nothing; this is fine.
	    	}
	    	catch(IOException e) 
	    	{
				throw new TestingException("Unable to prepare for code testing:  could not delete file " + f, e);
			}
    	}	
    	
    	// Delete all precompiled files - they are not allowed to exist ahead of time.
    	List<File> precompiledFiles = recursiveJavaClassLister(submission);
    	
    	for(File f:precompiledFiles)
    	{
       		// Copy the selected test case file into their submission directory!
	    	String destTestAddress = submission.getPath() + File.separator + f.getName();
	    	
	    	try 
	    	{
	    		Files.delete(new File(destTestAddress).toPath());
			}
	    	catch(NoSuchFileException e)
	    	{
	    		// Do nothing; this is fine.
	    	}
	    	catch(IOException e) 
	    	{
				throw new TestingException("Unable to prepare for code testing:  could not delete file " + f, e);
			}
    	}
    	
		// Step 2:  Copy over any needed files for test compilation/testing. 	
    	
    	for(File f:testSpec.imports)
    	{    		
    		// Copy the selected test case file into their submission directory!
    		File srcTestAddress = new File(DirectoryManager.testSpecSelectedDirectory, f.toString());
	    	String destTestAddress = submission.getPath() + File.separator + f.getName();
	    	try 
	    	{
				Files.copy(srcTestAddress.toPath(), new File(destTestAddress).toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
	    	catch (IOException e) 
	    	{
				throw new TestingException("Unable to prepare for code testing:  Could not copy file " + f, e);
			}
    	}	
    	
    	// Step 3:  Compile each test.
	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    
    	DiagnosticCollector<JavaFileObject> managerDiagnostics = new DiagnosticCollector<JavaFileObject>();
	    StandardJavaFileManager fileManager = compiler.getStandardFileManager(managerDiagnostics, null, null);

    	List<File> javaSourceFiles = recursiveJavaFileLister(submission);
    	
    	File[] javaSourceArray = new File[javaSourceFiles.size()];
    	javaSourceFiles.toArray(javaSourceArray);
    	
    	Iterable<? extends JavaFileObject> filesToCompile =
    	           fileManager.getJavaFileObjects(javaSourceArray);
    	
    	
    	ArrayList<String> options = new ArrayList<String>();
    	options.add("-cp");
    	options.add(submission.toString());
    	DiagnosticCollector<JavaFileObject> compilationDiagnostics = new DiagnosticCollector<JavaFileObject>();
    	
    	CompilationTask compileTask = compiler.getTask(null, fileManager, compilationDiagnostics, options, null, filesToCompile);
    	
    	if(javaSourceArray.length == 0)
    	{
    		//System.out.println("Could not find anything to compile.");
    		lastCompileDiagnostics = "Could not find anything to compile.";
    		//return null;
    	}
    	else
    	{
	    	boolean compResult = compileTask.call();
	    	
	    	if(compResult)
	    	{
	    		//System.out.println("Compilation is successful");
	    		lastCompileDiagnostics = null;
	    		studentData.setCompiledFlag(true);
	    	}
	    	else
	    	{
	    		//System.out.println("Compilation Failed");
	    		lastCompileDiagnostics = compilationDiagnostics.getDiagnostics().toString();
	    		
	    		return null;
	    	}
    	}
    	
    	// Step 4:  Execute each test.
    	
		Process process;
		Map<TestCase, TestResult> results = new HashMap<TestCase, TestResult>(testSpec.testCases.size());
		
		currentTestCase = 0;
		for(TestCase tc:testSpec.testCases)
		{
			currentTestCase++;
			currentTestCaseName = tc.getTestCaseName();
			
			//System.out.println("\tRunning test " + tc.testCase.toString());
			
			File f = tc.getTestCaseFile();
			File absoluteTestCasePath = new File(DirectoryManager.testSpecSelectedDirectory, f.toString());
			
			try 
			{	
				String fullClasspath = 	DirectoryManager.testSpecSelectedDirectory + File.pathSeparator + // Include the test spec's base folder.  (test code should be filed underneath this as appropriate.
										DirectoryManager.testSpecSelectedDirectory + File.separator + "bin" + File.pathSeparator +  // or within a bin folder inside of that.  Might make things better organized.
										Classpaths.getCurrent() +   // Include LIBs and current code.
										"." + File.pathSeparator; // Include the active directory - which will be that of the STUDENT after an upcoming line. 

				
				//String testName = f.getName().substring(0, f.getName().indexOf('.'));
				
				// Runs the TestCaseRunner program, with full classpath, and the test to be evaluated as a command-line argument.
				ProcessBuilder pb = new ProcessBuilder("java", "-cp", fullClasspath, "kh.edu.npic.unitgrader.grade.TestCaseRunner", absoluteTestCasePath.toString(), currentTestCaseName);
				pb.directory(submission);  // Set the active directory to the student's actual directory.
				process = pb.start();
			} 
			catch (IOException e) 
			{
				throw new TestingException("Could not execute test case " + f.getName() + " for student " + name + ".", e);
			}
			catch (RuntimeException e)
			{
				throw new TestingException("Error occurred during test case setup.", e);
			}
			
			BufferedReader inPipe = new BufferedReader(new InputStreamReader(process.getInputStream()));
			BufferedReader errPipe = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			
			String str;
			TestResult result;
			
			try
			{
				boolean cont = true;
				
				/* This loop is designed as a non-blocking test on whether our subprocess is still running.
				 * Java doesn't have a method built-in to the Process class to do this for us.
				 * 
				 * It's nonblocking so that the master (UI) thread can request termination of this test
				 * (and thus, the subprocess) as a form of manual time-out.
				 */
				do
				{
					try
					{
						process.exitValue();
						cont = false;
						//break;
					}
					catch(IllegalThreadStateException e)
					{
						cont = true;
					}

					try
					{
						Thread.sleep(16);
					}
					catch (InterruptedException e)
					{
						lastCompileDiagnostics = "Testing was interrupted and cancelled.";
						process.destroy();
						return null;
					}					
				} while(cont);
					
					//////////////////////////
				String xmlResults = "";
				
				while((str = inPipe.readLine()) != null)
				{
					//System.out.println(str);
					xmlResults += str;
				}
								
				boolean errPrint = false;
				String xmlError = "";
				while((str = errPipe.readLine()) != null)
				{
					// No need to use the error stream if no internal program errors occur.
					// (JUnit test case evaluation errors occur separately.)
					
					errPrint = true;
					xmlError += str;
				}
				
				if(!errPrint)
				{
					xmlResults = xmlResults.substring(xmlResults.indexOf("<result>"));
				
					result = (TestResult)Serialization.fromXML(xmlResults);
				}
				else
				{
					Exception e = (Exception)Serialization.fromXML(xmlError);
					
					throw new RuntimeException("Exception in testing subprogram thread - see below report.", e);
				}
			}
			catch (IOException e)
			{
				System.err.println("Error printing out test results for " + name);
				result = null;
			}
			
			results.put(tc, result);
		}
		
		return results;
    }
	
	private static List<File> recursiveJavaFileLister(File dir)
	{
		List<File> javaFiles = new ArrayList<File>();
		
		for(File f:dir.listFiles(new JavaSourceFileFilter()))
		{
			javaFiles.add(f);
		}
		
		for(File d:dir.listFiles(new DirectoryFilter()))
		{
			javaFiles.addAll(recursiveJavaFileLister(d));
		}
		
		return javaFiles;
	}
	
	private static List<File> recursiveJavaClassLister(File dir)
	{
		List<File> classFiles = new ArrayList<File>();
		
		for(File f:dir.listFiles(new JavaClassFileFilter()))
		{
			classFiles.add(f);
		}
		
		for(File d:dir.listFiles(new DirectoryFilter()))
		{
			classFiles.addAll(recursiveJavaClassLister(d));
		}
		
		return classFiles;
	}
}
