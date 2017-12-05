package kh.edu.npic.unitgrader.grade.results;

/**
 * Used to represent JUnit test failures due to generated runtime error crashes.
 * @author Joshua A. Horton
 *
 */
public class Crash extends FailureBase
{
	public final Throwable exception;
	
	Crash(org.junit.runner.notification.Failure src)
	{
		super(src);
		
		if(src.getException() instanceof junit.framework.AssertionFailedError)
		{
			throw new IllegalArgumentException("Crash instances must correspond to unexpected thrown exceptions during JUnit test case evaluation!");
		}
		
		exception = src.getException();
	}
	
	public String toString()
	{
		if(exception.getStackTrace().length > 0)
			return testMethod + ": " + exception + " @ " + exception.getStackTrace()[0];
		else return testMethod + ": " + exception;
	}
}
