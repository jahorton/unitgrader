package kh.edu.npic.unitgrader.define;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Map;

import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.preferences.ConfigurationConsoleMenuOption;

// TODO:  (low priority) Is it possible to write code that enforces this?

/*
 * Notes:  test specs ought be defined such that the test file's directory and 
 * the base Java package directory for the test are one and the same, OR in a /bin folder.
 * 
 * This is an assumption made by StudentTester that ought be enforced.
 */

public class TestSpecWriter 
{
	private TestSpecWriter()
	{
		
	}
	
	/**
	 * Designed to allow interactive construction of test specifications for
	 * use with the main Autograder program.
	 */
	
	static class State
	{
		TestSpecification prev;
		TestSpecification curr;
		
		final File sourceFile;
		
		boolean dirtyFlag = false;
		
		public State(File testSpecFile)
		{
			prev = (TestSpecification) Serialization.fromFile(testSpecFile.toString());
			curr = new TestSpecification(prev);
			
			sourceFile = testSpecFile;
		}
		
		public void revert()
		{
			curr = new TestSpecification(prev);
			dirtyFlag = false;
		}
		
		public boolean save()
		{
			prev = curr;
			curr = new TestSpecification(prev);
			
			try
			{
				Serialization.toFile(sourceFile.toString(), curr);
			}
			catch(RuntimeException | FileNotFoundException e)
			{
				return false;
			}
			
			dirtyFlag = false;
			return true;
		}
	}
	
	static State state;
	
	static class RevertOption extends Option
	{
		public RevertOption()
		{
			super("Revert to prior save.");
		}

		@Override
		public boolean function()
		{
			state.revert();
			
			return true;
		}
	}
	
	static class SaveOption extends Option
	{

		public SaveOption()
		{
			super("Save");
		}

		@Override
		public boolean function()
		{
			if(!state.save())
			{
				System.err.println("Unable to permanently save changes.  Please make sure the file is has not been opened by another program.");
			}
			return true;
		}
		
	}
	
	static class EndDirtyMenuOption extends kh.edu.npic.unitgrader.util.console.EndMenuOption
	{
		public EndDirtyMenuOption()
		{			
			super();
		}
		
		public EndDirtyMenuOption(String str)
		{
			super(str);
		}
		
		@Override
		public boolean function()
		{
			if(state.dirtyFlag)
			{
				if(NumericalMenu.getYesOrNo("Warning:  changes have not been saved. Discard (Y/N)?  "))
				{
					state.revert();
					return true;
				}
				else
				{
					return true;
				}
			}
			else return false;
		}
	}
	
	public static void manageTestCase(final TestCase tc)
	{
		class PrintOption extends Option
		{

			public PrintOption()
			{
				super("Print out current point assignments.");
			}

			@Override
			public boolean function()
			{
				int total = 0;
				
				for(Map.Entry<String, Integer> entry:tc.tests.entrySet())
				{
					System.out.println(entry.getKey() + " (" + entry.getValue() + ")");
					
					total += entry.getValue();
				}
				
				System.out.println();
				System.out.println("Test case's total value: " + total + " pts.");
				
				return true;
			}
		}
			
		NumericalMenu menu = new NumericalMenu();
		PrintOption print = new PrintOption();
		
		print.function();
		System.out.println();
		
		// Define the menu.
		menu.add(new PrintOption());
		menu.add(new IndividualWeightingOption(tc));
		menu.add(new SequentialWeightingOption(tc));
		menu.add(new GlobalWeightingOption(tc));
		menu.add(new ReloadTestCaseOption(tc));
		menu.add(new CompactCommentingTestOption(tc)); 
		menu.add(new RevertOption());
		menu.add(new SaveOption());
		menu.add(new EndDirtyMenuOption());
		
		menu.run();
		
	}
	
	public static void manageTestSpecificationFile(final File testSpecFile)
	{
		state = new State(testSpecFile);
		
		NumericalMenu menu = new NumericalMenu();
		
		menu.add(new AddNewTestCase());
		menu.add(new SelectExistingTestCase());
		menu.add(new ImportMenuOption());
		menu.add(new DeletionListMenuOption());
		menu.add(new LMSSelectOption());
		menu.add(new EndMenuOption());

		menu.run();
		
		state = null;
	}
	
	public static void main(String[] args) 
	{
		NumericalMenu mainMenu = new NumericalMenu();
		
		mainMenu.add(new CreateNewTestSpecOption());
		mainMenu.add(new EditExistingTestSpecOption());
		mainMenu.add(new ConfigurationConsoleMenuOption());
		mainMenu.add(new EndMenuOption("Quit."));
		
		mainMenu.run();
	}

}
