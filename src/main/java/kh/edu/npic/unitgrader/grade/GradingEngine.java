package kh.edu.npic.unitgrader.grade;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;
import kh.edu.npic.unitgrader.grade.manager.StudentFolderStatus;
import kh.edu.npic.unitgrader.grade.results.TestResult;
import kh.edu.npic.unitgrader.main.AutogradeDriver;
import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.TestSpecification;

public class GradingEngine
{
	private static class TestingTask<TagType extends LMSAssignmentManager.LMSDataTag> implements Callable<Boolean>
	{
		StudentData<TagType> data;
		LMSAssignmentManager<TagType> manager;
		
		public TestingTask(StudentData<TagType> data, LMSAssignmentManager<TagType> manager)
		{
			this.data = data;
			this.manager = manager;
		}

		@Override
		public Boolean call() throws Exception
		{
			return GradingEngine.test(data, manager);
		}
	}
	
	/**
	 * Used internally to report on the status of a pending asynchronous student testing run.  Can also
	 * trigger a Thread interrupt on the testing thread if so directed by user input.
	 * @param testSpec
	 * @param test
	 * @return
	 */
	private static boolean awaitAsyncTestCompletion(TestSpecification testSpec, FutureTask<Boolean> test)
	{
		boolean testSuccess = false;
		int currentTestCase = 0;
		
		boolean firstPrint = false;
		
		// Erase any lingering input - anything not skipped will auto-cancel the test.
		try
		{					
			while(System.in.available() > 0)
			{
				System.in.skip(System.in.available());
			}
		}
		catch (IOException e) {}
		
		while(true)
		{
			try
			{
				testSuccess = test.get(16, TimeUnit.MILLISECONDS);
			}
			catch(TimeoutException e)
			{
				if(!firstPrint)
				{
					firstPrint = true;
					System.out.println("Hit the enter key to cancel testing.");
				}
				
				// Interpret any input as a request for test interruption.				
				try
				{
					if(System.in.available() > 0)
					{
						System.in.skip(System.in.available());
						
						boolean cancelled = test.cancel(true);
						if(cancelled)
							return false;
					}
				}
				catch (IOException e1) 	
				{
					// If we somehow have an error occur on retrieval of data from System.in... yeah, let's auto-interrupt.
					boolean cancelled = test.cancel(true);
					if(cancelled)
						return false;
				}
				
				int prevTestCase = currentTestCase;
				currentTestCase = StudentTester.getCurrentTestCase();
				
				if(prevTestCase != currentTestCase)
				{
					System.out.println("Evaluating test case \"" + StudentTester.getCurrentTestCaseName() + "\" (" + currentTestCase + " of " + testSpec.testCases.size() + ").");
				}
				
				continue;
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
			catch (ExecutionException e)
			{
				throw new RuntimeException(e);
			}
			
			break;
		}
		
		return testSuccess;
	}
	
	public static <T extends LMSAssignmentManager.LMSDataTag> void run(LMSAssignmentManager<T> manager, StudentConditionFilter filter)
	{
		TestSpecification testSpec = manager.getTestSpecification();
		
		Iterator<StudentData<T>> dataIter = manager.iterator();
		
		// Special setup for first element to process.
		StudentData<T> data = dataIter.next();
		StudentData<T> nextData = data;
		StudentFolderStatus status;
		
		// Find the first filter-matching element in the represented directory.
		while(true)
		{
			while(!filter.matches(nextData)) 
			{
				nextData = dataIter.next();
				continue;
			}
			
			status = manager.isStudentFolderPresent(nextData);
			if(nextData.getAnalysis() == null && (status == StudentFolderStatus.MISSING || status == StudentFolderStatus.OLD))
			{
				nextData = dataIter.next();
				continue;
			}
			
			break;
		}
		
		status = manager.isStudentFolderPresent(nextData);
		if(status == StudentFolderStatus.NEW)
		{
			manager.resetStudentFolder(nextData);
		}
		
		AutogradeResults grading = null;	
		FutureTask<Boolean> nextTest = null;
		
		if(nextData.getComments() == null) // This data hasn't been adequately tested yet!  Set it going!
		{
			manager.resetStudentFolder(nextData);
			nextTest = new FutureTask<Boolean>(new TestingTask<T>(nextData, manager));//(test(data, testSpec))  // We'll want output.
			new Thread(nextTest).start(); // Asynchronously begin testing.
		}
		
		// Start the main loop.
		do
		{
			System.out.println("Student: " + nextData.first + " " + nextData.last);
			
			// Are tests still running for this student?  If so, wait on them.
			if(nextTest != null)
			{
				boolean testSuccess = awaitAsyncTestCompletion(testSpec, nextTest);
				
				// Async testing complete.
    			if(testSuccess)
    			{
	    			grading = AutogradeResults.analyze(nextData.getAnalysis());
	    			nextData.setComments(grading.comments);
	    		}
	    		else
	    		{
	    			grading = null;
	    		}
			}
			else
			{
				if(nextData.getAnalysis() == null)
					grading = null;
				else grading = AutogradeResults.analyze(nextData.getAnalysis());
			}
			
			data = nextData;
			nextData = null;

			// Start async testing the next data element!
			if(dataIter.hasNext())
			{
				nextData = dataIter.next();
				while(true)
				{
					while(!filter.matches(nextData)) 
					{
						if(dataIter.hasNext())
						{
							nextData = dataIter.next();
							continue;
						}
						else 
						{
							nextData = null;
							break;
						}
					}
					
					if(nextData == null) break;
					
					status = manager.isStudentFolderPresent(nextData);
					if(nextData.getAnalysis() == null && (status == StudentFolderStatus.MISSING || status == StudentFolderStatus.OLD))
						continue;
					
					break;
				}
				
				if(manager.isStudentFolderPresent(nextData) == StudentFolderStatus.NEW)
				{
					manager.resetStudentFolder(nextData);
				}
				
				if(nextData != null)
				{
					if(nextData.getComments() == null) // This data hasn't been adequately tested yet!  Set it going!
					{
						manager.resetStudentFolder(nextData);
						nextTest = new FutureTask<Boolean>(new TestingTask<T>(nextData, manager));//(test(data, testSpec))  // We'll want output.
						new Thread(nextTest).start(); // Asynchronously begin testing.
					}
					else nextTest = null;
				}
			}
			
			
			if(grading == null)
			{
    			AutogradeDriver.studentNoncompileMenu(data, manager); 			
			}
			else
			{
				AutogradeDriver.studentGradingMenu(data, manager, grading);
			}
			
			data.markTested();
			
			manager.save();	
		} 
		while(dataIter.hasNext() || nextData != null);
	}
	
	public static <T extends LMSAssignmentManager.LMSDataTag> void review(LMSAssignmentManager<T> manager, StudentConditionFilter filter)
	{
		Iterator<StudentData<T>> dataIter = manager.iterator();
		
		// Special setup for first element to process.
		StudentData<T> data = dataIter.next();
		while(!filter.matches(data)) 
		{
			data = dataIter.next();
			continue;
		}
		
		AutogradeResults grading = null;	
		
		while(dataIter.hasNext())
		{
			data = dataIter.next();
			while(!filter.matches(data))
			{
				if(dataIter.hasNext())
					data = dataIter.next();
				else 
				{
					data = null;
					return;
				}
			}
			
			if(!data.getCompiledFlag())
			{
    			AutogradeDriver.studentNoncompileMenu(data, manager); 			
			}
			else
			{
				grading = AutogradeResults.analyze(data.getAnalysis());
				AutogradeDriver.studentGradingMenu(data, manager, grading);
			}
			
			manager.save();	
		}
	}

	public static <T extends LMSAssignmentManager.LMSDataTag> boolean runSingle(StudentData<T> data, LMSAssignmentManager<T> manager)
	{
		TestSpecification testSpec = manager.getTestSpecification();
		
		// Special setup for first element to process.
		StudentFolderStatus status = manager.isStudentFolderPresent(data);
		if(status == StudentFolderStatus.NEW)
		{
			manager.resetStudentFolder(data);
		}
		
		FutureTask<Boolean> nextTest = null;
		
		nextTest = new FutureTask<Boolean>(new TestingTask<T>(data, manager));//(test(data, testSpec))  // We'll want output.
		new Thread(nextTest).start(); // Asynchronously begin testing.
		
		System.out.println("Student: " + data.first + " " + data.last);
		
		boolean testSuccess = awaitAsyncTestCompletion(testSpec, nextTest);		
		data.markTested();
		
		return testSuccess;
	}
	
	private static <T extends LMSAssignmentManager.LMSDataTag> boolean test(StudentData<T> data, LMSAssignmentManager<T> manager)
	{    	
    	//System.out.println();
    	//System.out.println("Student: " + data.first + " " + data.last);
		
		TestSpecification testSpec = manager.getTestSpecification();
    	
    	Map<TestCase, TestResult> results = null;
    	TestingException error = null;
    	
    	StudentFolderStatus status = manager.isStudentFolderPresent(data);
		if(status == StudentFolderStatus.NEW)
		{
			manager.resetStudentFolder(data);
		}
		else if(status == StudentFolderStatus.MISSING || status == StudentFolderStatus.OLD) throw new IllegalStateException(); 
    	
    	try 
    	{
			results = StudentTester.run(testSpec, data);
			if(results != null)
			{
				data.setAnalysis(results);
			}
		} 
    	catch (TestingException ex) 
		{
			error = ex;
			System.err.println("ERROR: " + error);
		}
    	
    	if(error != null)
    	{
    		data.setCompiledFlag(false);
    		data.setFlaggedStatus(true);
    		
    		return false;
    	}
    	else
    	{
    		if(results == null)
    		{
    			data.setCompiledFlag(false);
    			data.setComments(StudentTester.getLastCompileDiagnostics());
    			
    			return false;
    		}
    		else
    		{
    			data.setAnalysis(results);
    			data.setCompiledFlag(true);    	
    			
    			return true;
    		}
    	}
	}
	
	public static void listMatchingStudents(LMSAssignmentManager<?> manager, StudentConditionFilter filter, String conditionString)
	{
		System.out.println(conditionString);
		
		for(StudentData<?> data:manager)
		{
			if(filter.matches(data))
			{
				System.out.println("\t" + data.first + " " + data.last);
			}
		}
		
		//System.out.println();
	}
}
