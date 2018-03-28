package kh.edu.npic.unitgrader.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import kh.edu.npic.unitgrader.grade.AutogradeResults;
import kh.edu.npic.unitgrader.grade.GradingEngine;
import kh.edu.npic.unitgrader.grade.manager.CanvasAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager.LMSDataTag;
import kh.edu.npic.unitgrader.grade.manager.StudentData;
import kh.edu.npic.unitgrader.grade.manager.StudentFolderStatus;
import kh.edu.npic.unitgrader.util.DirectoryRestrictedFileSystemView;
import kh.edu.npic.unitgrader.util.Launcher;
import kh.edu.npic.unitgrader.util.console.DeadOption;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;
import kh.edu.npic.unitgrader.util.console.ToggleOption;
import kh.edu.npic.unitgrader.util.preferences.ConfigurationConsoleMenuOption;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;


public class AutogradeDriver 
{
	public static void performInitializationChecks()
	{
	    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
	    
	    if(compiler == null)
	    {
	    	System.out.println("Warning - this program cannot link to the Java runtime compiler.  Program must exit; please reconfigure your Java settings.");
	    	System.exit(-1);
	    }
	}
	
	public static void main(String[] args) 
	{
		performInitializationChecks();
	    
		NumericalMenu menu = new NumericalMenu();
		menu.add(new InitialGradingOption());
		menu.add(new ExistingGradingOption());
		menu.add(new ConfigurationConsoleMenuOption());
		menu.add(new EndMenuOption("Quit."));
		menu.run();	    
	}
	
	public static <T extends LMSDataTag<T>> void studentNoncompileMenu(final StudentData<T> data, final LMSAssignmentManager<T> manager)
	{
		System.out.println();
		System.out.println("Testing attempt failed; cannot continue grading this submission as it is.");
		
		class ManualGradeOption extends Option
		{
			public String originalComments = data.getComments();
					
			public ManualGradeOption()
			{
				super("Manually set the score for this assignment.");
			}

			@Override
			public boolean function()
			{
				int grade;
				do
				{
					grade = NumericalMenu.getIntInput("Enter the score for this assignment: ");
					
					if(grade < 0)
						System.out.println("Score must not be negative.");
				} while(grade < 0);
				
				data.setGrade(grade);
				data.setSkippedFlag(false);
				
				String commentExtension = "Manual override - program never compiled within the autograder.\n";
				
				Scanner input = new Scanner(System.in);
				System.out.print("Reason for adjustment (enter a single line): ");
				commentExtension += "Reason: " + input.nextLine();
				
				data.setComments(originalComments + commentExtension);
				
				return true;
			}
			
		}
		
		class RetestOption extends Option
		{
			public RetestOption()
			{
				this("Re-run the test cases on this submission.");
			}
			
			public RetestOption(String text)
			{
				super(text);
			}

			@Override
			public boolean function()
			{
				boolean flagged = data.getFlaggedStatus();
				data.resetFlags();
				data.setFlaggedStatus(flagged);
				
				boolean result = GradingEngine.runSingle(data, manager);
				
				if(result)
				{
					AutogradeResults grading = AutogradeResults.analyze(data.getAnalysis());
	    			data.setComments(grading.comments);
	    			
	    			System.out.println();
	    			studentGradingMenu(data, manager, grading);
				}
				else
				{
					studentNoncompileMenu(data, manager);
				}
				
				return false;
			}
		}

		class ResetOption extends RetestOption
		{
			public ResetOption()
			{
				super("Reset student's submission folder and retest on the original code.");
			}
			
			@Override
			public boolean function()
			{
				manager.resetStudentFolder(data);
				
				return super.function();
			}
		}

		NumericalMenu menu = new NumericalMenu();
		
		menu.add(new Option("Continue to the next assignment."){

			@Override
			public boolean function()
			{
				data.setSkippedFlag(true);
				return false;
			}
			
		});
		
		if(Launcher.isSupported())
		{
			menu.add(new OpenFolderOption(data));
			if(manager instanceof CanvasAssignmentManager)
				menu.add(new OpenZipOption(data));
		}
		else
		{
			menu.add(new DeadOption("Unable to open this student's submission data folder in a window."));
			if(manager instanceof CanvasAssignmentManager)
				menu.add(new DeadOption("Nor the original submission *.zip."));
		}
				
		StudentFolderStatus status = manager.isStudentFolderPresent(data);
		if(status == StudentFolderStatus.CURRENT)
		{
			menu.add(new RetestOption());
			menu.add(new ResetOption());
		}
		else if(status == StudentFolderStatus.NEW)
		{
			menu.add(new RetestOption("Re-run the test cases on this submission.  WARNING:  you should \"Reset\" first, as this student has new submission data."));
			menu.add(new ResetOption());
		}
		else
		{
			menu.add(new RetestOption("Re-run the test cases on this submission.  WARNING:  system believes its current data to either be missing or outdated."));
			menu.add(new DeadOption("Cannot reset this student's data, as currently-available original source is either missing or outdated."));
		}
		menu.add(new RebaseOption(data));
		menu.add(new Option("Display compilation errors."){

			@Override
			public boolean function()
			{
				System.out.println(data.getComments());
				return true;
			}
			
		});
		menu.add(new ManualGradeOption());

		//menu.add(new EngineExitOption());
		menu.run();
	}
	
	public static <T extends LMSDataTag<T>> void studentGradingMenu(final StudentData<T> data, final LMSAssignmentManager<T> manager, final AutogradeResults gradingResults)
	{
		System.out.println();
		System.out.println("Suggested score for " + data.first + "  " + data.last + ": " + gradingResults.grade);
		if(data.isGraded())
		{
			System.out.println("Presently assigned score: " + data.getGrade());
		}
		
		class ContinueOption extends ToggleOption
		{
			public ContinueOption()
			{
				super("Continue without assigning a grade.", "Go to next assignment.", !data.isGraded());
			}

			@Override
			public boolean function()
			{
				if(isUntoggled())
				{
					data.setSkippedFlag(true);
				}
				return false;
			}
			
		}
		
		final ContinueOption contOption = new ContinueOption();
		
		class SuggestedGradeOption extends Option
		{
			public SuggestedGradeOption()
			{
				super("Use the suggested score for this assignment.");
			}

			@Override
			public boolean function()
			{
				data.setGrade(gradingResults.grade);
				
				if(contOption.isUntoggled()) contOption.toggle();
				data.setSkippedFlag(false);
				return true;
			}
			
		}
		
		class SuggestedGradeContinueOption extends Option
		{
			public SuggestedGradeContinueOption()
			{
				super("Use the suggested score for this assignment and continue to the next assignment.");
			}

			@Override
			public boolean function()
			{
				data.setGrade(gradingResults.grade);
				
				if(contOption.isUntoggled()) contOption.toggle();
				data.setSkippedFlag(false);
				return false;
			}
			
		}
		
		class ManualGradeOption extends Option
		{
			public String originalComments = data.getComments();
					
			public ManualGradeOption()
			{
				super("Manually set the score for this assignment.");
			}

			@Override
			public boolean function()
			{
				double grade;
				grade = NumericalMenu.getDoubleInput("Enter the score for this assignment, or -1 to leave it ungraded: ");
					
				if(grade < 0)
					grade = Double.NaN;
				
				data.setGrade(grade);
				
				Scanner input = new Scanner(System.in);
				String commentExtension;
				
				if(Double.isNaN(grade))
				{
					data.setSkippedFlag(true);
					if(!contOption.isUntoggled()) contOption.toggle();
					
					commentExtension = "Manual adjustment: " + (grade - gradingResults.grade) + " pts.\n";
					System.out.print("Reason for adjustment (enter a single line): ");
					commentExtension += "Reason: " + input.nextLine();
				}
				else
				{
					data.setSkippedFlag(false);
					if(contOption.isUntoggled()) contOption.toggle();
					
					System.out.print("Notes on current grading efforts: ");
					commentExtension = "Instructor/TA notes: " + input.nextLine();
				}				
				
				data.setComments(originalComments + commentExtension);
				
				return true;
			}
			
		}
		
		final ManualGradeOption manualOption = new ManualGradeOption();
		
		class FlaggedStatusOption extends ToggleOption
		{
			public FlaggedStatusOption()
			{
				super("Set flagged status.", "Remove flagged status.", !data.getFlaggedStatus());
			}

			@Override
			public boolean function()
			{
				data.setFlaggedStatus(this.isUntoggled());
				this.toggle();
				
				return true;
			}
		}
		
//		class LateDaysOption extends Option
//		{
//			public LateDaysOption()
//			{
//				super("Mark late days.");
//			}
//
//			@Override
//			public boolean function()
//			{
//				boolean cont = true;
//				
//				do
//				{
//					int days = 	NumericalMenu.getIntInput("Enter the number of days (presently marked as " + data.getDaysLate() + " this submission was late: ");
//					
//					if(days < 0)
//					{
//						System.out.println("Days late must be non-negative!");
//						cont = true;
//					}
//					else 
//					{
//						cont = false;
//						data.setDaysLate(days);
//					}
//				} while(cont);
//				
//				return true;
//			}
//		}
		
		// Is a hacked menu item - returns false, but re-calls the method with the new AutogradeResult.
		// Use GradingEngine.test() to do this.
		class RetestOption extends Option
		{
			public RetestOption()
			{
				this("Re-run the test cases on this submission.");
			}
			
			public RetestOption(String text)
			{
				super(text);
			}

			@Override
			public boolean function()
			{
				boolean flagged = data.getFlaggedStatus();
				data.resetFlags();
				data.setFlaggedStatus(flagged);
				
				boolean result = GradingEngine.runSingle(data, manager);
				
				if(result)
				{
					AutogradeResults grading = AutogradeResults.analyze(data.getAnalysis());
	    			data.setComments(grading.comments);
	    			
	    			System.out.println();
	    			studentGradingMenu(data, manager, grading);
				}
				else
				{
					studentNoncompileMenu(data, manager);
				}
				
				return false;
			}
		}
		
		class ResetOption extends RetestOption
		{
			public ResetOption()
			{
				super("Reset student's submission folder and retest on the original code.");
			}
			
			@Override
			public boolean function()
			{
				manager.resetStudentFolder(data);
				
				return super.function();
			}
		}
		
		class DisplayAutocomments extends Option
		{

			public DisplayAutocomments()
			{
				super("Display grading notes for this submission.");
			}

			@Override
			public boolean function()
			{
				System.out.println(data.getComments());
				return true;
			}
			
		}
		
		NumericalMenu menu = new NumericalMenu();
		menu.add(new SuggestedGradeContinueOption());
		
		if(Launcher.isSupported())
		{
			menu.add(new OpenFolderOption(data));
			if(manager instanceof CanvasAssignmentManager)
				menu.add(new OpenZipOption(data));
		}
		else
		{
			menu.add(new DeadOption("Unable to open this student's submission data folder in a window."));
			if(manager instanceof CanvasAssignmentManager)
				menu.add(new DeadOption("Nor the original submission *.zip."));
		}
		
		StudentFolderStatus status = manager.isStudentFolderPresent(data);
		if(status == StudentFolderStatus.CURRENT)
		{
			menu.add(new RetestOption());
			menu.add(new ResetOption());
		}
		else if(status == StudentFolderStatus.NEW)
		{
			menu.add(new RetestOption("Re-run the test cases on this submission.  WARNING:  you should \"Reset\" first, as this student has new submission data."));
			menu.add(new ResetOption());
		}
		else
		{
			menu.add(new RetestOption("Re-run the test cases on this submission.  WARNING:  system believes its current data to either be missing or outdated."));
			menu.add(new DeadOption("Cannot reset this student's data, as currently-available original source is either missing or outdated."));
		}
		menu.add(new RebaseOption(data));
		menu.add(new DisplayAutocomments());
		menu.add(manualOption);						// Set the score manually, and extend comments.
		menu.add(new SuggestedGradeOption());		// Use score suggestion.
		menu.add(new FlaggedStatusOption());
		//menu.add(new LateDaysOption());
		
		menu.add(contOption);						// Is toggled by the grade assignment options.
		
		//menu.add(new EngineExitOption());
		menu.run();
	}
	
	private static class RebaseOption extends Option
	{
		private final StudentData<?> data;

		public RebaseOption(StudentData<?> data)
		{
			super("Search the submission for the directory with this student's submitted code.");
			
			this.data = data;
		}

		@Override
		public boolean function()
		{
			int res;
			
			Path rootPath = DirectoryManager.baseSubmissionSelectedDirectory.toPath();
			DirectoryRestrictedFileSystemView fsv = new DirectoryRestrictedFileSystemView(new File(rootPath.toFile(), data.getBaseFolder().toString()));
		    JFileChooser chooseBaseFolder = new JFileChooser(new File(rootPath.toFile(), data.getCodeFolder().toString()), fsv);
		    chooseBaseFolder.setDialogTitle("Select directory...");
		    chooseBaseFolder.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    res = chooseBaseFolder.showOpenDialog(null);
		    
		    switch(res)
		    {
		    	case JFileChooser.CANCEL_OPTION:
		    		return true;
		    	case JFileChooser.APPROVE_OPTION:
		    		File folder = rootPath.relativize(chooseBaseFolder.getSelectedFile().toPath()).toFile();
		    		
		    		data.setCodeFolder(folder);
		    		break;
		    	case JFileChooser.ERROR_OPTION:
		    		System.err.println("Error detected in results from file selection.");
		    		return true;
		    	default:
		    		System.err.println("Impossible result from file chooser dialog.");
		    		return true;
		    }

			return true;
		}
		
	}
	
	private static class OpenOption extends Option
	{
		private File fileToLaunch;
		
		public OpenOption(String text, File file)
		{
			super(text);
			this.fileToLaunch = file;
		}
		
		@Override
		public boolean function()
		{
			File path = new File(DirectoryManager.baseSubmissionSelectedDirectory, fileToLaunch.toString());
			
			if (Launcher.isSupported()) {
				try
				{
			    	Launcher.open(path);
				}
				catch (IOException e) { System.out.println("ERROR:  Could not open the file/folder."); }
			}
			else
			{
				System.out.println("Your current OS's current Java installation does not support opening this file/folder automatically.");
			}

			return true;
		}
	}
	
	private static class OpenFolderOption extends OpenOption
	{
		public static final String text = "Open this student's submission data folder in a window.";
		
		public OpenFolderOption(StudentData<?> data)
		{
			super(text, data.getCodeFolder());
		}
	}
	
	private static class OpenZipOption extends OpenOption
	{
		public static final String text = "Open this student's original submission *.zip in a window.";
		
		public OpenZipOption(StudentData<?> data)
		{
			super(text, new File(((CanvasAssignmentManager.CanvasData)data.getTag()).getArchiveName()));
		}
	}
	
	// TODO:  Get this working.
//	private static class EngineExitOption extends Option
//	{
//
//		public EngineExitOption()
//		{
//			super("Return to main menu, leaving the present grading loop.");
//		}
//
//		@Override
//		public boolean function()
//		{
//			return false;
//		}
//	}
}


