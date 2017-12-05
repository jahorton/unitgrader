package kh.edu.npic.unitgrader.grade.manager;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import kh.edu.npic.unitgrader.grade.results.TestResult;
import kh.edu.npic.unitgrader.util.TestCase;

public class StudentData<Tag extends LMSAssignmentManager.LMSDataTag>
{
	// These two ought be set once the student's submission is properly decompressed/readied for test file application.
	// Compare id + timestamp being loaded to know if the submission should be replaced.
	public final String first;
	public final String last;
	public final String id;
	private String baseFolder;
	private String codeFolder;
	
	private Tag tag;

	private long timestamp; // Sakai timestamp:  YYYYMMDDHHmmSS - the largest digits are always the most significant.  Can compare on standard int format safely.
    // Canvas "timestamp" is more of a version/ID timestamp... but is also smaller for earlier versions.  (Only problem is using it for lateness detection.)

	private Map<TestCase, TestResult> analysis;
	private double baseGrade;
	private long gradeTimestamp;
	private String comments;
	
//	private int daysLate;
	
	private boolean tested;
	private boolean compiled;
	private boolean skipped;
	private boolean flagged;
	private boolean processed;
	
	public StudentData(String first, String last, String id, File folder, long timestamp)
	{
		this.first = first;
		this.last = last;
		this.id = id;
		this.baseFolder = folder.toString().replaceAll("\\\\", "/");
		this.codeFolder = folder.toString().replaceAll("\\\\", "/");
		this.timestamp = timestamp;
		
		this.tested = false;
		this.compiled = false;
		this.skipped = false;
		this.flagged = false;
		
		baseGrade = Double.NaN;
//		daysLate = 0;
	}
	
	public void resetFlags()
	{
		this.baseGrade = Double.NaN;
		this.gradeTimestamp = 0;
		this.tested = false;
		this.compiled = false;
		this.skipped = false;
		this.processed = false;
		this.flagged = false;
	}
	
	public File getCodeFolder()
	{
		return new File(codeFolder);
	}
	
	public void setCodeFolder(File folder)
	{
		this.codeFolder = folder.toString().replaceAll("\\\\", "/");
	}
	
	public File getBaseFolder()
	{
		return new File(baseFolder);
	}
	
	public Tag getTag()
	{
		return tag;
	}
	
	// TODO:  Is this really appropriate?
	public void setTag(Tag tag)
	{
		this.tag = tag;
	}
	
	public long getTimestamp()
	{
		return timestamp;
	}
	
	public boolean isTested()
	{
		return tested;
	}
	
	public void markTested()
	{
		tested = true;
	}
	
	public boolean getCompiledFlag()
	{
		return compiled;
	}
	
	public void setCompiledFlag(boolean flag)
	{
		this.compiled = flag;
	}
	
	public boolean getSkippedFlag()
	{
		return skipped;
	}
	
	public void setSkippedFlag(boolean flag)
	{
		this.skipped = flag;
	}
	
	public double getGrade()
	{
		return baseGrade;
	}
	
	public long getGradeTimestamp()
	{
		return gradeTimestamp;
	}
	
	public boolean isGraded()
	{
		return !Double.isNaN(baseGrade);
	}
	
	public String getComments()
	{
		return comments;
	}
	
	public void setGrade(double grade)
	{
		this.baseGrade = grade;
		this.gradeTimestamp = System.currentTimeMillis();
	}
	
	public void setComments(String comments)
	{
		this.comments = comments;
	}
	
	public boolean getFlaggedStatus()
	{
		return this.flagged;
	}
	
	public void setFlaggedStatus(boolean flag)
	{
		this.flagged = flag;
	}
	
	public boolean getProcessedFlag()
	{
		return this.processed;
	}
	
	public void setProcessedFlag(boolean flag)
	{
		this.processed = true;
	}
	
	public Map<TestCase, TestResult> getAnalysis()
	{
		if(analysis == null)
			return null;
		else
			return Collections.unmodifiableMap(analysis);
	}
	
	public void setAnalysis(Map<TestCase, TestResult> analysis)
	{
		this.analysis = new HashMap<TestCase, TestResult>(analysis);
		
		boolean flagged = this.flagged;	
		this.resetFlags();
		this.setFlaggedStatus(flagged);
	}
	
//	public int getDaysLate()
//	{
//		return daysLate;
//	}
//	
//	public void setDaysLate(int days)
//	{
//		this.daysLate = days;
//	}
	
	public String toString()
	{
		return first + " " + last + ": " + baseFolder;
	}
	
	void reset(File folder, long timestamp)
	{
		if(folder == null) throw new NullPointerException();
		
		resetFlags();
		
		this.baseFolder = folder.toString().replaceAll("\\\\", "/");
		this.timestamp = timestamp;
	}
}
