package kh.edu.npic.unitgrader.define;

import java.util.ArrayList;

import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.console.Option;

public class ReloadTestCaseOption extends Option
{
	private TestCase tc;

	public ReloadTestCaseOption(TestCase tc)
	{
		super("Reload the JUnit test case to ensure all test methods are represented.");
		
		this.tc = tc;
	}

	@Override
	public boolean function()
	{
		// Load up the testing file again in order to detect changes.
		TestCase newTC = TestCase.constructForTestingFile(tc.getTestCaseFile());
		
		// Compare / contrast, copying over persistent test methods' data.
		ArrayList<String> originalMethods = new ArrayList<String>(tc.tests.keySet());
		ArrayList<String> currentMethods = new ArrayList<String>(newTC.tests.keySet());
		
		ArrayList<String> removedMethods = new ArrayList<String>();
		ArrayList<String> addedMethods = new ArrayList<String>(currentMethods);
		
		for(String method:originalMethods)
		{
			if(currentMethods.contains(method))
			{
				newTC.tests.put(method, tc.tests.get(method));
				addedMethods.remove(method);
			}
			else
			{
				removedMethods.add(method);
			}
		}

		if(removedMethods.size() == 0 && addedMethods.size() == 0)
		{
			// Avoid affecting the dirty bit.
			System.out.println("No relevant changes were made.");
			return true;
		}
		//else
		
		int index = TestSpecWriter.state.curr.testCases.indexOf(tc);
		TestSpecWriter.state.curr.testCases.set(index, newTC);
		
		// Report on any detected changes.
		if(removedMethods.size() != 0)
		{
			System.out.println("Removed methods: ");
			for(String method:removedMethods)
			{
				System.out.println("\t" + method + " (" + tc.tests.get(method) + ")");
			}
			System.out.println();
		}
		
		if(addedMethods.size() != 0)
		{
			System.out.println("Newly added methods: ");
			for(String method:addedMethods)
			{
				System.out.println("\t" + method + " (0)");
			}
			System.out.println();
		}

		TestSpecWriter.state.dirtyFlag = true;

		// Do not continue with the old version of the menu; replace it with a reconstructed menu for the new TestCase.
		TestSpecWriter.manageTestCase(newTC);
		return false;
	}

}
