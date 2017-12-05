package kh.edu.npic.unitgrader.grade.filters;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

public interface StudentConditionFilter
{
	public <T extends LMSAssignmentManager.LMSDataTag> boolean matches(StudentData<T> data);
}
