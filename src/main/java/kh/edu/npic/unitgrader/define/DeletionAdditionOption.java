package kh.edu.npic.unitgrader.define;

import java.io.File;
import java.util.Scanner;

import kh.edu.npic.unitgrader.util.console.Option;

public class DeletionAdditionOption extends Option
{

	public DeletionAdditionOption()
	{
		super("Add a file to the deletion list.");
	}

	@Override
	public boolean function()
	{
		System.out.print("What should the path of the file be, relative to the student's base submission directory?  ");
		
		Scanner input = new Scanner(System.in);
		String filename = input.nextLine();
		
		if(filename == null) return true;
		
		filename = filename.trim();
		
		if(filename.equals("")) return true;
		
		File file = new File(filename);
		
		TestSpecWriter.state.curr.deletes.add(file);
		TestSpecWriter.state.dirtyFlag = true;
		
		return true;
	}

}
