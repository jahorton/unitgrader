package kh.edu.npic.unitgrader.grade.results;

import java.util.ArrayList;
import java.util.List;

public class TestResult 
{
	public final String testCase;
	public final List<Failure> failures;
	public final List<Crash> crashes;
	
	public TestResult(org.junit.runner.Result src, String testName)
	{
		failures = new ArrayList<Failure>();
		crashes = new ArrayList<Crash>();
		
		for(org.junit.runner.notification.Failure f:src.getFailures())
		{
			if(f.getException() instanceof junit.framework.AssertionFailedError)
			{
				failures.add(new Failure(f));
			}
			else
			{
				crashes.add(new Crash(f));
			}
		}
		
		this.testCase = testName;
	}
	
	public String toString()
	{
		String str = testCase + " results: " + "\n\n";
		
		for(Failure f:failures)
		{
			str += f + "\n";
		}
		
		for(Crash c:crashes)
		{
			str += c + "\n";
		}
		
		return str;
	}
}
