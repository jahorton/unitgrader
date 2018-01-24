package kh.edu.npic.unitgrader.util.console;

import java.io.PrintStream;
import java.util.Scanner;

public final class Console {
	private static Scanner scanner = new Scanner(System.in);
	
	private Console() 
	{
		/* This class should not be instantiated; it exists to provide 
		 * console-oriented utility methods.
		 */
	}
	
	public static PrintStream out( ) 
	{
		return System.out;
	}
	
	public static Scanner in() {
		return scanner;
	}
}
