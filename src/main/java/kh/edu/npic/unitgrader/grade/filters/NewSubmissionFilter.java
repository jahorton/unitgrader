package kh.edu.npic.unitgrader.grade.filters;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;
import kh.edu.npic.unitgrader.grade.manager.StudentFolderStatus;

public class NewSubmissionFilter implements StudentConditionFilter
{
	private LMSAssignmentManager<?> manager;
	
	public NewSubmissionFilter(LMSAssignmentManager<?> manager)
	{
		this.manager = manager;
	}

	@Override
	public <T extends LMSAssignmentManager.LMSDataTag> boolean matches(StudentData<T> data)
	{
		return (manager.isStudentFolderPresent((StudentData)data) == StudentFolderStatus.NEW) || !data.isGraded() && !data.getSkippedFlag();
	}

}
