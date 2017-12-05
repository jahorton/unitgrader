package kh.edu.npic.unitgrader.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class TestCase
{
	private final String testCase;

	public final Map<String, Integer> tests;
	
	private boolean compactComments;

	private TestCase(File testCase)
	{
		String temp = testCase.toString();
		
		this.testCase = temp.replaceAll("\\\\", "/");
		tests = new HashMap<String, Integer>();
		compactComments = false;
	}
	
	public TestCase(final TestCase tc)
	{
		this.testCase = tc.testCase;
		this.tests = new HashMap<String, Integer>(tc.tests);
		this.compactComments = tc.compactComments;
	}
	
	public File getTestCaseFile()
	{
		return new File(testCase);
	}
	
	public String getTestCaseName()
	{
		return testCase;
	}

	public static TestCase constructForTestingFile(File testCase)
	{
		Class<?> clazz;
		try
		{
			clazz = ManualClassLoader.loadClassFromFile(new File(DirectoryManager.testSpecSelectedDirectory, testCase.toString()));
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Could not load the test case's code for analysis!");
		}

		Method[] methods = clazz.getMethods();

		TestCase tc = new TestCase(testCase);

		// Construct a spot for each testing method in the test spec.
		for (Method m : methods)
		{
			org.junit.Test t = m.getAnnotation(org.junit.Test.class);

			if (t == null) continue;

			tc.tests.put(m.getName(), 0);
		}
		
		return tc;
	}
	
	public String toString()
	{
		return testCase.toString() + ":  " + tests;
	}
	
	public boolean getCompactCommentsFlag()
	{
		return compactComments;
	}
	
	public void setCompactCommentsFlag(boolean flag)
	{
		compactComments = flag;
	}
	
	public boolean equals(Object obj)
	{
		if(!(obj instanceof TestCase)) return false;
		
		TestCase other = (TestCase) obj;
		
		return testCase.equals(other.testCase);
	}
}
