package kh.edu.npic.unitgrader.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TestSpecification 
{
	public final List<TestCase> testCases;
	public final List<File> imports;
	public final List<File> deletes;
	
	private String lmsIdentifier;
	
	public TestSpecification()
	{
		testCases = new ArrayList<TestCase>();
		imports = new ArrayList<File>();
		deletes = new ArrayList<File>();
		
		lmsIdentifier = null;
	}
	
	public TestSpecification(final TestSpecification spec)
	{
		this.testCases = new ArrayList<TestCase>(spec.testCases.size());
		this.imports = new ArrayList<File>(spec.imports);
		this.deletes = new ArrayList<File>(spec.deletes);
		
		this.lmsIdentifier = spec.lmsIdentifier;
		
		for(TestCase tc:spec.testCases)
		{
			this.testCases.add(new TestCase(tc));
		}
	}
	
	public String getLMSIdentifier()
	{
		return lmsIdentifier;
	}
	
	public void setLMSIdentifier(String lmsIdent)
	{
		this.lmsIdentifier = lmsIdent;
	}
	
	public String toString()
	{
		return "Test Cases:  " + testCases + ", Imports:  " + imports + ", Deletes:  " + deletes;
	}
	
	public boolean equals(Object obj)
	{
		if(!(obj instanceof TestSpecification)) return false;
		
		TestSpecification other = (TestSpecification) obj;
		
		if(!lmsIdentifier.equals(other.lmsIdentifier)) return false;
		if(!testCases.equals(other.testCases)) return false;
		if(!imports.equals(other.imports)) return false;
		if(!deletes.equals(other.deletes)) return false;
		
		return true;
	}
}
