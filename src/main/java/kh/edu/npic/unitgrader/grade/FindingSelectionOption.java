package kh.edu.npic.unitgrader.grade;

import java.util.Scanner;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;
import kh.edu.npic.unitgrader.main.AutogradeDriver;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

@SuppressWarnings({"rawtypes", "unchecked"})
public class FindingSelectionOption extends Option
{
	LMSAssignmentManager<?> manager;
	
	public FindingSelectionOption(LMSAssignmentManager manager)
	{
		super("Find feedback regarding a specific student.");
		
		this.manager = manager;
	}

	@Override
	public boolean function()
	{
		class MatchOption extends Option
		{
			StudentData data;
			
			public MatchOption(StudentData data)
			{
				super(data.first + " " + data.last);
				this.data = data;
			}

			@Override
			public boolean function()
			{
				if(data.getComments() == null)
				{
					boolean testSuccess = GradingEngine.runSingle(data, manager);
					
					if(testSuccess)
					{
						AutogradeResults grading = AutogradeResults.analyze(data.getAnalysis());
						data.setComments(grading.comments);
						
						AutogradeDriver.studentGradingMenu(data, manager, grading);
					}
					else
					{
						AutogradeDriver.studentNoncompileMenu(data, manager); 			
					}
				}
				else if(data.getCompiledFlag())
				{
					AutogradeResults grading = AutogradeResults.analyze(data.getAnalysis());
					
					AutogradeDriver.studentGradingMenu(data, manager, grading);
				}
				else
				{
					// Never compiled - but what if they want to re-test it?
					boolean testSuccess = GradingEngine.runSingle(data, manager);
					
					if(testSuccess)
					{
						AutogradeResults grading = AutogradeResults.analyze(data.getAnalysis());
						data.setComments(grading.comments);
						
						AutogradeDriver.studentGradingMenu(data, manager, grading);
					}
					else
					{
						AutogradeDriver.studentNoncompileMenu(data, manager); 			
					}
				}	
				
				manager.save();
				
				return false;
			}
			
		}
		
		class LastNameMatchOption extends Option
		{
			public LastNameMatchOption()
			{
				super("Search by last name.");
			}

			@Override
			public boolean function()
			{
				Scanner input = new Scanner(System.in);
				
				System.out.print("Enter part of the last name you wish to search for: ");
				String str = input.nextLine().trim();
				
				System.out.println();
				System.out.println("Students whose last names match \"" + str + "\":");
				
				NumericalMenu menu = new NumericalMenu();
				
				for(StudentData data:manager.matchLastname(str))
				{
					menu.add(new MatchOption(data));
				}

				menu.add(new EndMenuOption());
				menu.run();
				return true;
			}				
		}
		
		class FirstNameMatchOption extends Option
		{
			public FirstNameMatchOption()
			{
				super("Search by first name.");
			}

			@Override
			public boolean function()
			{
				Scanner input = new Scanner(System.in);
				
				System.out.print("Enter part of the first name you wish to search for: ");
				String str = input.nextLine().trim();
				
				System.out.println();
				System.out.println("Students whose first names match \"" + str + "\":");
				
				NumericalMenu menu = new NumericalMenu();
				
				for(StudentData data:manager.matchFirstname(str))
				{
					menu.add(new MatchOption(data));
				}

				menu.add(new EndMenuOption());
				menu.run();
				
				return true;
			}				
		}
		
		NumericalMenu menu = new NumericalMenu();
		menu.add(new LastNameMatchOption());
		menu.add(new FirstNameMatchOption());
		menu.add(new EndMenuOption());
		menu.run();
		
		return true;
	}

}
