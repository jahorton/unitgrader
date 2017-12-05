package kh.edu.npic.unitgrader.grade.results;

import java.util.ArrayList;

/** Common base class of Failure and Crash.
 * 
 * @author Joshua A. Horton
 *
 */
public class FailureBase 
{
	public final String testMethod;
	//public final String testCase;
	
	FailureBase(org.junit.runner.notification.Failure src)
	{
		testMethod = src.getDescription().getMethodName();
		//testCase = src.getDescription().getClassName();
		
		stackShrink(src.getException());
	}
	
	private void stackShrink(Throwable src)
	{
		StackTraceElement[] originalTrace = src.getStackTrace();
		
		ArrayList<StackTraceElement> traceList = new ArrayList<StackTraceElement>(originalTrace.length);
		
		for(int i=0; i < originalTrace.length; i++)
		{
			String className = originalTrace[i].getClassName();
			
			if(className.indexOf("junit") != -1) continue;
			if(originalTrace[i].isNativeMethod()) continue;
			if(className.contains("java.util.")) continue;
			if(className.contains("sun.reflect.")) continue;
			if(className.contains("java.lang.reflect.")) continue;
			if(className.contains("autograde.TestCaseRunner")) continue;
			if(className.contains("scratch.TestCaseRunner")) continue;
			
			traceList.add(originalTrace[i]);
		}
		
		StackTraceElement[] shrunkenTrace = new StackTraceElement[traceList.size()];
		
		int i=0;
		for(StackTraceElement ele:traceList)
		{
			shrunkenTrace[i++] = ele;
		}
		
		src.setStackTrace(shrunkenTrace);
	}
}
