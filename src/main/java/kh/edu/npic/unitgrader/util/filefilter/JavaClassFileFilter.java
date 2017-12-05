package kh.edu.npic.unitgrader.util.filefilter;

import java.io.File;
import java.io.FileFilter;

public class JavaClassFileFilter implements FileFilter {

	@Override
	public boolean accept(File f) 
	{
		if(f.isDirectory()) return false;
		
		int extStart = f.getName().lastIndexOf('.');
		if(extStart == -1) return false;
		
		return (f.getName().substring(extStart).equals(".class"));
	}

}