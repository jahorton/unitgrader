package kh.edu.npic.unitgrader.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class Launcher
{
	private Launcher()
	{
	}
	
	public static boolean isSupported()
	{
		String osName = System.getProperty("os.name");
		if(osName.indexOf("win") > 0) return true;
		else return Desktop.isDesktopSupported();
	}
	
	public static void open(File file) throws IOException
	{
		if (isSupported()) {
			String osName = System.getProperty("os.name").toLowerCase();

	    	// Strange known bug with Java's Desktop functions on certain Windows configurations.
	    	// We avoid this by having a special Windows check first.
			if(osName.indexOf("win") >= 0)
			{
				Runtime.getRuntime().exec("explorer.exe \"" + file.toString() + "\"");
			}
			else
			{
			    Desktop.getDesktop().open(file);
			}
		}
		else throw new UnsupportedOperationException();
	}
}
