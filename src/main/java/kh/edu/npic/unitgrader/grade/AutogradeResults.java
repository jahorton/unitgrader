package kh.edu.npic.unitgrader.grade;

import java.util.Map;

import kh.edu.npic.unitgrader.grade.results.Crash;
import kh.edu.npic.unitgrader.grade.results.Failure;
import kh.edu.npic.unitgrader.grade.results.TestResult;
import kh.edu.npic.unitgrader.util.TestCase;

public class AutogradeResults
{
	public final int grade;
	public final String comments;
	
	public AutogradeResults(int grade, String comments)
	{
		this.grade = grade;
		this.comments = comments;
	}
	
	public String toString()
	{
		return "Grade: " + grade + "\n\n" + comments;
	}

	public static AutogradeResults analyze(Map<TestCase, TestResult> results)
	{
	String str = "";
		int totalPtsLost = 0;
		int totalPtsMax = 0;
		
		for(Map.Entry<TestCase, TestResult> result:results.entrySet())
		{
			str += result.getKey().getTestCaseFile().getName() + ": \n";
			int max = 0;
			
			for(Integer i:result.getKey().tests.values())
			{
				max += i;
			}
			
			totalPtsMax += max;
		
			if(result.getKey().getCompactCommentsFlag())
			{
				int pts = 0;
				
				for(Failure f:result.getValue().failures)
				{
					pts += result.getKey().tests.get(f.testMethod);
				}
				
				for(Crash c:result.getValue().crashes)
				{
					pts += result.getKey().tests.get(c.testMethod);
				}
				
				str += "\tErrors were detected on this JUnit test suite summing to " + pts + " points lost out of " + max + ".";
				
				totalPtsLost += pts;
			}
			else
			{
				for(Failure f:result.getValue().failures)
				{
					str += "\t (" + result.getKey().tests.get(f.testMethod) + " pts lost) " + f + "\n";
					totalPtsLost += result.getKey().tests.get(f.testMethod);
				}
				
				boolean setupSuccess = true;
				
				if(result.getValue().crashes.size() == 1)
				{
					Crash c = result.getValue().crashes.get(0);
					
					if(c.testMethod == null)
					{
						int testPointTotal = 0;
						
						for(int pts:result.getKey().tests.values())
							testPointTotal += pts;
						
						str += "\t (" + testPointTotal + " pts lost) Could not perform proper setup for test case - " + c.exception + "\n";
						totalPtsLost += testPointTotal;
						
						setupSuccess = false;
					}
					else if(c.testMethod.equals("initializationError"))
					{
						int testPointTotal = 0;
						
						for(int pts:result.getKey().tests.values())
							testPointTotal += pts;
						
						str += "\t (" + testPointTotal + " pts lost) Could not perform proper setup for test case - " + c.exception + "\n";
						totalPtsLost += testPointTotal;
						
						setupSuccess = false;
					}
				}
				
				if(setupSuccess)
				{
					for(Crash c:result.getValue().crashes)
					{ 
						str += "\t (" + result.getKey().tests.get(c.testMethod) + " pts lost) " + c + "\n";
						totalPtsLost += result.getKey().tests.get(c.testMethod);
					}
				}
			}
			
			str += "\n";
		}
		
		return new AutogradeResults(totalPtsMax - totalPtsLost, str);
	}
}