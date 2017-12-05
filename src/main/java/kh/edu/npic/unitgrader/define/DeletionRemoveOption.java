package kh.edu.npic.unitgrader.define;

import java.io.File;

import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class DeletionRemoveOption extends Option
{

	public DeletionRemoveOption()
	{
		super("Remove a file from the deletion list.");
	}

	@Override
	public boolean function()
	{
		System.out.println("Select the deletion file you wish to remove from the list: ");

		NumericalMenu selectMenu = new NumericalMenu();
		
		class SelectOption extends Option
		{
			private final File file;

			public SelectOption(File file)
			{
				super(file.toString());
				
				this.file = file;
			}

			@Override
			public boolean function()
			{
				TestSpecWriter.state.curr.deletes.remove(file);
				TestSpecWriter.state.dirtyFlag = true;
				
				return false;
			}
		}
		
		for(File file:TestSpecWriter.state.curr.deletes)
		{
			selectMenu.add(new SelectOption(file));
		}
		
		selectMenu.add(new EndMenuOption());
		
		selectMenu.run();
		
		return true;
	}

}
