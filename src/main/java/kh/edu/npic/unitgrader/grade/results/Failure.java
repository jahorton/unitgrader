package kh.edu.npic.unitgrader.grade.results;

/**
 * Used to represent any test failures due to unmet assertions.
 * @author Joshua A. Horton
 *
 */
public class Failure extends FailureBase
{
	public final StackTraceElement[] stack;
	public final String description;
	
	Failure(org.junit.runner.notification.Failure src)
	{
		super(src);
		
		if(!(src.getException() instanceof java.lang.AssertionError))
		{
			throw new IllegalArgumentException("Failures must correspond to \"assert\" tests in JUnit test cases!");
		}
		
		stack = src.getException().getStackTrace();
		description = src.getException().getMessage();
	}
	
	public String toString()
	{
		return testMethod + ": " + description + " @ " + stack[0];
	}
}
