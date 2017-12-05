package kh.edu.npic.unitgrader.define;

import java.io.File;

import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class ImportRemoveOption extends Option
{

	public ImportRemoveOption()
	{
		super("Remove a file from the import list.");
	}

	@Override
	public boolean function()
	{
		System.out.println("Select the import file you wish to remove from the list: ");

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
				TestSpecWriter.state.curr.imports.remove(file);
				TestSpecWriter.state.dirtyFlag = true;
				
				return false;
			}
		}
		
		for(File file:TestSpecWriter.state.curr.imports)
		{
			selectMenu.add(new SelectOption(file));
		}
		
		selectMenu.add(new EndMenuOption());
		
		selectMenu.run();
		
		return true;
	}

}
