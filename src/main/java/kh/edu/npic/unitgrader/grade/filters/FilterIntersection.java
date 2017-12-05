package kh.edu.npic.unitgrader.grade.filters;

import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.StudentData;

/**
 * Represents the logical Intersection operation on a set of filters.
 * 
 * @author Joshua A. Horton
 *
 */
public class FilterIntersection implements StudentConditionFilter
{
	private StudentConditionFilter[] filters;
	
	public FilterIntersection(StudentConditionFilter... filters)
	{
		this.filters = filters;
	}
	
	@Override
	public <T extends LMSAssignmentManager.LMSDataTag> boolean matches(StudentData<T> data)
	{
		for(StudentConditionFilter filter:filters)
		{
			if(!filter.matches(data))
				return false;
		}
		
		return true;
	}

}
