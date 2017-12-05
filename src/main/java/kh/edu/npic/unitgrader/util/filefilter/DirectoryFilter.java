package kh.edu.npic.unitgrader.util.filefilter;

import java.io.File;
import java.io.FileFilter;

public class DirectoryFilter implements FileFilter {

	@Override
	public boolean accept(File f) 
	{
		if(f.isDirectory()) return true;
		
		else return false;
	}

}