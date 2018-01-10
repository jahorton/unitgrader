package kh.edu.npic.unitgrader.main;

import java.util.ArrayList;
import java.util.List;

import kh.edu.npic.unitgrader.define.TestSpecWriter;
import kh.edu.npic.unitgrader.util.console.EndMenuOption;
import kh.edu.npic.unitgrader.util.console.NumericalMenu;
import kh.edu.npic.unitgrader.util.console.Option;

public class SuiteEntry {

	public static void main(String[] args) {
		class GradeOption extends Option {
			GradeOption() {
				super("Grade student assignments.");
			}
			
			public boolean function() {
				AutogradeDriver.main(args);
			
				return true;
			};
		}
		
		class TestWriteOption extends Option {
			TestWriteOption() {
				super("Write a test specification.");
			}
			
			public boolean function() {
				TestSpecWriter.main(args);
			
				return true;
			};
		}
		
		List<Option> options = new ArrayList<Option>();
		options.add(new GradeOption());
		options.add(new TestWriteOption());
		options.add(new EndMenuOption("Quit."));
		
		NumericalMenu menu = new NumericalMenu(options);
		menu.run();
	}

}
