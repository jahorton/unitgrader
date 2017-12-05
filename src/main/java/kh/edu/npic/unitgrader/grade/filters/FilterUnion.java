package kh.edu.npic.unitgrader.grade.filters;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

/**
 * Represents the logical Union operation on a set of filters.
 * 
 * @author Joshua A. Horton
 *
 */
public class FilterUnion implements StudentConditionFilter
{
	private StudentConditionFilter[] filters;
	
	public FilterUnion(StudentConditionFilter... filters)
	{
		this.filters = filters;
	}
	
	@Override
	public <T extends LMSAssignmentManager.LMSDataTag> boolean matches(StudentData<T> data)
	{
		for(StudentConditionFilter filter:filters)
		{
			if(filter.matches(data))
				return true;
		}
		
		return false;
	}

}
