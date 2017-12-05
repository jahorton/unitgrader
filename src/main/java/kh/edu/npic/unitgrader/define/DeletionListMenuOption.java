package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class DeletionListMenuOption extends Option
{

	public DeletionListMenuOption()
	{
		super("Manage the removal list.");
	}

	@Override
	public boolean function()
	{
		NumericalMenu menu = new NumericalMenu();

		menu.add(new PrintListOption("Display the deletion file list.", TestSpecWriter.state.curr.deletes));
		menu.add(new DeletionAdditionOption());
		menu.add(new DeletionRemoveOption());
		menu.add(new TestSpecWriter.RevertOption());
		menu.add(new TestSpecWriter.SaveOption());
		menu.add(new TestSpecWriter.EndDirtyMenuOption());
		
		menu.run();
		
		return true;
	}

}
