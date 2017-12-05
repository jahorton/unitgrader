package kh.edu.npic.unitgrader.define;

import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class ImportMenuOption extends Option
{

	public ImportMenuOption()
	{
		super("Manage the import list.");
	}

	@Override
	public boolean function()
	{
		NumericalMenu menu = new NumericalMenu();

		menu.add(new PrintListOption("Display the import file list.", TestSpecWriter.state.curr.imports));
		menu.add(new ImportAdditionOption());
		menu.add(new ImportRemoveOption());
		menu.add(new TestSpecWriter.RevertOption());
		menu.add(new TestSpecWriter.SaveOption());
		menu.add(new TestSpecWriter.EndDirtyMenuOption());
		
		menu.run();
		
		return true;
	}

}
