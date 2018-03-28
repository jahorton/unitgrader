package kh.edu.npic.unitgrader.grade.filters;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

public class RecentlyGradedFilter implements StudentConditionFilter
{
	private long thresholdTimestamp;
	
	public RecentlyGradedFilter(long thresholdTimestamp)
	{
		this.thresholdTimestamp = thresholdTimestamp;
	}

	@Override
	public <T extends LMSAssignmentManager.LMSDataTag<T>> boolean matches(StudentData<T> data)
	{
		return data.isGraded() && data.getGradeTimestamp() > thresholdTimestamp;
	}

}
