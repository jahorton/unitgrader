package kh.edu.npic.unitgrader.grade;

import kh.edu.npic.unitgrader.grade.manager.CanvasAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.LMSAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.SakaiAssignmentManager;
import kh.edu.npic.unitgrader.util.console.Option;

public class CommentExportOption extends Option
{
	private LMSAssignmentManager<?> manager;

	public CommentExportOption(LMSAssignmentManager<?> manager)
	{
		super("Export grading comments to \"comments.txt\" files in each student's submission folder.");
		
		this.manager = manager;
	}

	@Override
	public boolean function()
	{
		System.out.println("Commencing comment file exports.");
		
		manager.exportComments();
		
		System.out.println("Comment exports complete.");
		
		if(manager instanceof CanvasAssignmentManager)
		{
			System.out.println("An additional copy of the comments has been placed in the \"Comments\" directory under the base submission folder.");
		}
		else if(manager instanceof SakaiAssignmentManager)
		{
			System.out.println("Re-zipping the base submission folder will allow for uploading the comments directly via reupload to Sakai.");
		}
	    
		return true;
	}

}
