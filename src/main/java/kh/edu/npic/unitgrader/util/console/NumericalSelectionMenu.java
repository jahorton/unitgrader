package kh.edu.npic.unitgrader.util.console;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class NumericalSelectionMenu<T>
{	
	private ArrayList<T> options;
	
	private static boolean recentMenuEnd;
	
	public NumericalSelectionMenu()
	{
		options = new ArrayList<T>();
		recentMenuEnd = false;
	}
	
	public NumericalSelectionMenu(List<T> options)
	{
		this.options = new ArrayList<T>(options);
		recentMenuEnd = false;
	}
	
	public void add(T option)
	{
		options.add(option);
	}
	
	public int size()
	{
		return options.size();
	}
	
	public T remove(int index)
	{
		return options.remove(index);
	}
	
	public void add(int index, T option)
	{
		options.add(index, option);
	}
	
	public boolean remove(T option)
	{
		return options.remove(option);
	}
	
	/**
	 * Executes the menu specified by this object and its children.  Terminates only when
	 * a menu option provides a "return" signal.
	 */
	public T run()
	{
		if(options.size() == 0) throw new IllegalStateException("This menu was never given any options!");
		
		recentMenuEnd = false;		

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
			System.out.println(options.get(i).toString());

		}
		
		System.out.println();
		
		int index = NumericalMenu.getIntOption("Enter your selection: ", options.size()) - 1;
		
		
		if(!recentMenuEnd)
			System.out.println();
		
		recentMenuEnd = true;
		
		return options.get(index);
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
		Scanner input = new Scanner(System.in);

		do
		{
			System.out.print(prompt);
			
			if(input.hasNextInt())
			{
				return input.nextInt();
			}
			else
			{
				input.next();
				System.out.println("Error:  Invalid input (non-integer) detected!");
			}
		} while (true);
	}
	
	public static double getDoubleInput(String prompt)
	{
		if(prompt == null) throw new NullPointerException("Prompt may not be null!");
		Scanner input = new Scanner(System.in);

		do
		{
			System.out.print(prompt);
			
			if(input.hasNextDouble())
			{
				return input.nextDouble();
			}
			else
			{
				input.next();
				System.out.println("Error:  Invalid input (non-numerical type) detected!");
			}
		} while (true);
	}
}
