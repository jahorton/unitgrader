package kh.edu.npic.unitgrader.grade.filters;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

public class SkippedStudentsFilter implements StudentConditionFilter
{

	@Override
	public <T extends LMSAssignmentManager.LMSDataTag> boolean matches(StudentData<T> data)
	{
		return data.getSkippedFlag();
	}

}
