package kh.edu.npic.unitgrader.util.preferences;

import javax.swing.JFileChooser;

import kh.edu.npic.unitgrader.util.console.*;

public class ConfigurationConsoleMenuOption extends Option
{
	private static class PrintDirectoryOptions extends Option
	{

		public PrintDirectoryOptions()
		{
			super("Print current directory settings.");
		}

		@Override
		public boolean function()
		{
			System.out.println("Current base directory for test spec searches: " + Configuration.get().getInitialTestFileDirectory());
			System.out.println("Current base directory for submission base folder searches: " + Configuration.get().getInitialSubmissionDirectory());
			return false;
		}
		
	}
	private static class TestSpecDirectoryOption extends Option
	{
		public TestSpecDirectoryOption()
		{
			super("Set the default directory for test specification searches.");
		}

		@Override
		public boolean function()
		{
			int res;
			
		    JFileChooser chooseBaseFolder = new JFileChooser();
		    chooseBaseFolder.setDialogTitle("Select test spec default directory");
		    chooseBaseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooseBaseFolder.setSelectedFile(Configuration.get().getInitialTestFileDirectory());
		    res = chooseBaseFolder.showOpenDialog(null);
		    
		    switch(res)
		    {
		    	case JFileChooser.CANCEL_OPTION:
		    		return true;
		    	case JFileChooser.APPROVE_OPTION:
		    		break;
		    	case JFileChooser.ERROR_OPTION:
		    		System.err.println("Error detected in results from file selection.");
		    		return true;
		    	default:
		    		System.err.println("Impossible result from file chooser dialog.");
		    		return true;
		    }
		    
		    Configuration.get().setInitialTestFileDirectory(chooseBaseFolder.getSelectedFile());

			return true;
		}
		
	}
	private static class SubmissionDirectoryOption extends Option
	{

		public SubmissionDirectoryOption()
		{
			super("Set the default directory for searching for the base folder of student submissions.");
		}

		@Override
		public boolean function()
		{
			int res;
			
		    JFileChooser chooseBaseFolder = new JFileChooser();
		    chooseBaseFolder.setDialogTitle("Select submission base default directory");
		    chooseBaseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    chooseBaseFolder.setSelectedFile(Configuration.get().getInitialSubmissionDirectory());
		    res = chooseBaseFolder.showOpenDialog(null);
		    
		    switch(res)
		    {
		    	case JFileChooser.CANCEL_OPTION:
		    		return true;
		    	case JFileChooser.APPROVE_OPTION:
		    		break;
		    	case JFileChooser.ERROR_OPTION:
		    		System.err.println("Error detected in results from file selection.");
		    		return true;
		    	default:
		    		System.err.println("Impossible result from file chooser dialog.");
		    		return true;
		    }
		    
		    Configuration.get().setInitialSubmissionDirectory(chooseBaseFolder.getSelectedFile());

			return true;
		}
		
	}
	
	public ConfigurationConsoleMenuOption(String text)
	{
		super(text);
	}
	
	public ConfigurationConsoleMenuOption()
	{
		super("Configure program preferences.");
	}

	@Override
	public boolean function()
	{
		NumericalMenu menu = new NumericalMenu();
		
		menu.add(new PrintDirectoryOptions());
		menu.add(new TestSpecDirectoryOption());
		menu.add(new SubmissionDirectoryOption());
		menu.add(new EndMenuOption());
		
		menu.run();
		
		return true;
	}

}
