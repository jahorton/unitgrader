package kh.edu.npic.unitgrader.define;

import java.io.File;
import java.util.List;

import kh.edu.npic.unitgrader.util.console.Option;

public class PrintListOption extends Option
{
	private List<File> fileList;
	
	public PrintListOption(String text, List<File> fileList)
	{
		super(text);
		
		this.fileList = fileList;
	}

	@Override
	public boolean function()
	{
		System.out.println("Files on the list: ");
		
		for(File f:fileList)
		{
			System.out.println("\t" + f);
		}
		
		System.out.println();
		
		return true;
	}

}
