package kh.edu.npic.unitgrader.grade.manager;

public enum StudentFolderStatus
{
	/**
	 * The student's folder does not exist in the specified submission directory.
	 */
	MISSING,
	/**
	 * The student's folder matches the previously-obtained results.
	 */
	CURRENT, 
	/**
	 * The student's folder is of an outdated submission.
	 */
	OLD,
	/**
	 * The student's folder is of a newer submission and ought be retested.
	 */
	NEW
}
