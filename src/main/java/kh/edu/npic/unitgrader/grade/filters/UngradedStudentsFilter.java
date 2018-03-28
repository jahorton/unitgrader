package kh.edu.npic.unitgrader.grade.filters;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

public class UngradedStudentsFilter implements StudentConditionFilter
{

	@Override
	public <T extends LMSAssignmentManager.LMSDataTag<T>> boolean matches(StudentData<T> data)
	{
		return !data.isGraded();
	}

}
