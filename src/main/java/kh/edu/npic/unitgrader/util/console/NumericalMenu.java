package kh.edu.npic.unitgrader.util.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NumericalMenu 
{	
	private ArrayList<Option> options;
	
	private static boolean recentMenuEnd;
	
	public NumericalMenu()
	{
		options = new ArrayList<Option>();
		recentMenuEnd = false;
	}
	
	public NumericalMenu(List<Option> options)
	{
		this.options = new ArrayList<Option>(options);
		recentMenuEnd = false;
	}
	
	public void add(Option option)
	{
		options.add(option);
	}
	
	public int size()
	{
		return options.size();
	}
	
	public Option remove(int index)
	{
		return options.remove(index);
	}
	
	public void add(int index, Option option)
	{
		options.add(index, option);
	}
	
	public boolean remove(Option option)
	{
		return options.remove(option);
	}
	
	/**
	 * Executes the menu specified by this object and its children.  Terminates only when
	 * a menu option provides a "return" signal.
	 */
	public void run()
	{
		if(options.size() == 0) throw new IllegalStateException("This menu was never given any options!");
		
		recentMenuEnd = false;		
		boolean cont = false;
		
		do
		{
			System.out.println();
			
			for(int i=0; i < options.size(); i++)
			{
				if(!(options.get(i) instanceof DeadOption))
				{
					System.out.print((i+1) + ") ");
				}
				else
				{
					System.out.print("   ");
				}
				System.out.println(options.get(i).text);

			}
			
			System.out.println();
			
			int index = getIntOption("Enter your selection: ", options.size()) - 1;
			
			cont = options.get(index).function();
			
			if(!recentMenuEnd)
				System.out.println();
			
		} while(cont);
		
		recentMenuEnd = true;
	}
	
//	static boolean consoleClearFailure = false;
//	
//	public final static void clearConsole()
//	{
//	    try
//	    {
//	        final String os = System.getProperty("os.name");
//
//	        if (os.contains("Windows"))
//	        {
//	            Runtime.getRuntime().exec("cls");
//	        }
//	        else
//	        {
//	            Runtime.getRuntime().exec("clear");
//	        }
//	    }
//	    catch (final Exception e)
//	    {
//	    	if(!consoleClearFailure)
//	    	{
//	    		consoleClearFailure = true;
//	    		System.err.println("Unable to clear the console!");
//	    	}
//	    }
//	}
	
	public static int getIntInput(String prompt)
	{
		if(prompt == null) throw new NullPointerException("Prompt may not be null!");

		do
		{
			System.out.print(prompt);
			
			if(Console.in().hasNextInt())
			{
				return Console.in().nextInt();
			}
			else
			{
				Console.in().next();
				System.out.println("Error:  Invalid input (non-integer) detected!");
			}
		} while (true);
	}
	
	public static double getDoubleInput(String prompt)
	{
		if(prompt == null) throw new NullPointerException("Prompt may not be null!");

		do
		{
			System.out.print(prompt);
			
			if(Console.in().hasNextDouble())
			{
				return Console.in().nextDouble();
			}
			else
			{
				Console.in().next();
				System.out.println("Error:  Invalid input (non-numerical type) detected!");
			}
		} while (true);
	}
	
	/**
	 * 
	 * @param prompt
	 * @return <b>true</b> if yes, <b>false</b> if no.
	 */
	public static boolean getYesOrNo(String prompt)
	{
		do
		{
			System.out.print(prompt);
			
			String str = Console.in().next();
		
			char c = str.charAt(0);
			
			if(c == 'Y' || c == 'y')
			{
				return true;
			}
				
			if(c == 'N' || c == 'n')
			{
				return false;
			}
				
			System.out.println("Invalid input detected.  Please retry.");
		}
		while(true);
	}
	
	public static int getIntOption(String prompt, int maxOption)
	{
		int option = 0;
		boolean cont = false;
		
		if(maxOption < 1) throw new IllegalArgumentException("There must be at least one option to select from!");
		
		do
		{
			option = getIntInput(prompt);
			
			cont = (option <= 0 || option > maxOption);
			
			if(cont)
			{
				System.out.println("Option " + option + " is not a valid menu option!");
			}
		} while (cont);
		
		return option;
	}
}
