package kh.edu.npic.unitgrader.util.preferences;

import java.io.File;

public final class DirectoryManager
{
	public static File testSpecSelectedDirectory = Configuration.get().getInitialTestFileDirectory();
	public static File baseSubmissionSelectedDirectory = Configuration.get().getInitialSubmissionDirectory();
}
