package kh.edu.npic.unitgrader.grade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import kh.edu.npic.unitgrader.grade.filters.StudentConditionFilter;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

public final class TailoredCSVExporter
{
	private TailoredCSVExporter()
	{	
	}
	
	public static void exportToCSV(LMSAssignmentManager<?> manager, File srcFile, File destFile, String fieldName, StudentConditionFilter filter) throws FileNotFoundException
	{
//		System.out.println("Source CSV: " + srcFile);
//		System.out.println("Output CSV: " + destFile);
//		System.out.println("Field name: " + fieldName);
//		
//		throw new UnsupportedOperationException();
		
	    CSVParser parser = null;
	    CSVPrinter printer = null;
	    Reader in;
	    
		try
		{
			in = new FileReader(srcFile);
			parser = new CSVParser(in, CSVFormat.EXCEL.withHeader());
			
			//Map<String, Integer> headerMap = parser.getHeaderMap();
			Map<String, Integer> reducedHeaderMap = manager.getCSV_DefaultHeader();
			int destinationFieldIndex = reducedHeaderMap.size();
			reducedHeaderMap.put(fieldName, destinationFieldIndex);
			
			List<CSVRecord> records = parser.getRecords();
			parser.close();	
			
			printer = new CSVPrinter(new PrintStream(destFile), CSVFormat.EXCEL.withHeader(toPrinterHeaderSpec(reducedHeaderMap)));
			
			// For each original record, replicate the RELEVANT parts.
			for(CSVRecord record : records)
			{
				// Manually reconstituted record procedure.  From this, I can edit the headers and data to include only what I want while keeping the same code pattern.
				Map<String, String> csvRecord = record.toMap();
				
				String[] serializedRecord = new String[reducedHeaderMap.size()];
				
				for(Map.Entry<String, Integer> field:reducedHeaderMap.entrySet())
				{
					serializedRecord[field.getValue()] = csvRecord.get(field.getKey());
				}
				
				// The one thing missing here - get the relevant student data that matches the record.  (By ID, not SIS User ID)
				// Get our generated grade for that and assign it here.
				
				String csvRecordID = csvRecord.get(manager.getCSV_IDField());
				StudentData<?> data = manager.getStudentData(csvRecordID);
				if(data == null)
				{
					continue;
				}
				
				if(!filter.matches(data))
					continue; // We're not outputting this student's data to the CSV.
				
				serializedRecord[destinationFieldIndex] = data.getGrade() + "";  
				
				printer.printRecord((Object[]) serializedRecord);
			}
			
			printer.flush();
			printer.close();
	    }
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if(parser != null) 
			{
				try
				{
					parser.close();
				}
				catch (IOException e) { }
			}
		}

	}
	
	private static String[] toPrinterHeaderSpec(Map<String, Integer> header)
	{
		String[] headerSpec = new String[header.size()];
		
		for(Map.Entry<String, Integer> entry:header.entrySet())
		{
			headerSpec[entry.getValue()] = entry.getKey();
		}
		
		return headerSpec;
	}
}
