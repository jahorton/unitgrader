package kh.edu.npic.unitgrader.grade;

import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeSet;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;
import kh.edu.npic.unitgrader.util.console.Option;

public class DataEnumerationOption extends Option
{
	private LMSAssignmentManager<?> manager;

	public DataEnumerationOption(LMSAssignmentManager<?> manager)
	{
		super("Output student data individually for info copying.");
		
		this.manager = manager;
	}

	@Override
	public boolean function()
	{
		Comparator<StudentData<?>> comparer = new Comparator<StudentData<?>>(){

			
			@Override
			public int compare(StudentData<?> d1, StudentData<?> d2)
			{
				int res;
				
				res = d1.last.compareTo(d2.last);
				
				if(res != 0) return res;
				else return d1.first.compareTo(d2.first);
			}
			
		};

		TreeSet<StudentData<?>> dataSorter = new TreeSet<StudentData<?>>(comparer);
		
		for(StudentData<?> data:manager)
		{
			dataSorter.add(data); 
		}
		
		Scanner input = new Scanner(System.in);
		
		for(StudentData<?> data:dataSorter)
		{
			System.out.println(data.last + "," + data.first + " - ID: " + data.id);
			System.out.println("Grade: " + (Double.isNaN(data.getGrade()) ? " " : data.getGrade()));
			
			System.out.println("Comments: \n");
			System.out.println(data.getComments());
			System.out.println();
			
			System.out.println("Press enter for the next student's info.");
			input.nextLine();
		}
	    
		return true;
	}

}