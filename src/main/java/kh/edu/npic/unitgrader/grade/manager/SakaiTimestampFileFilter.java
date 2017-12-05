package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.io.FileFilter;

public class SakaiTimestampFileFilter implements FileFilter
{

	@Override
	public boolean accept(File pathname)
	{
		return pathname.getName().equals("timestamp.txt");
	}

}
