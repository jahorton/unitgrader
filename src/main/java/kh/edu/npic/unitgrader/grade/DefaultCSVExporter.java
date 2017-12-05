package kh.edu.npic.unitgrader.grade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.TreeSet;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

public final class DefaultCSVExporter
{
	private DefaultCSVExporter()
	{	
	}
	
	public static void exportToCSV(LMSAssignmentManager<?> manager, File destFile) throws FileNotFoundException
	{
		Comparator<StudentData<?>> comparer = new Comparator<StudentData<?>>(){

			
			@Override
			public int compare(StudentData<?> d1, StudentData<?> d2)
			{
				int res;
				
				res = d1.last.compareTo(d2.last);
				
				if(res != 0) return res;
				res = d1.first.compareTo(d2.first);
				
				if(res != 0) return res;
				else return d1.id.compareTo(d2.id);
			}
			
		};
		
		exportToCSV(manager, destFile, comparer);
	}
	
	public static void exportToCSV(LMSAssignmentManager<?> manager, File destFile, Comparator<StudentData<?>> comparer) throws FileNotFoundException
	{
		TreeSet<StudentData<?>> dataSorter = new TreeSet<StudentData<?>>(comparer);
		
		for(StudentData<?> data:manager)
		{
			dataSorter.add(data); 
		}
		
		System.out.println("Potental CSV output: ");
		System.out.println("");
		
		StringBuilder builder = new StringBuilder();
		builder.append("Last,\tFirst,\tID,\tGrade\n\n");
				
		for(StudentData<?> data:dataSorter)
		{
			builder.append(data.last + ",\t" + data.first + ",\t" + data.id + ",\t");
			builder.append((Double.isNaN(data.getGrade()) ? " " : data.getGrade()) + ",\t");
			
			builder.append("\n");
		}
		
		String csvData = builder.toString();
		
		System.out.println(csvData);
		
		PrintWriter out = null;
		try
		{
			out = new PrintWriter(new FileOutputStream(destFile));
			out.write(csvData);
		}
		finally
		{
			if(out != null)
				out.close();
		}
	}
}
