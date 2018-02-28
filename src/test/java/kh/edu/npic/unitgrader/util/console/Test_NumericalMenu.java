package kh.edu.npic.unitgrader.util.console;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

class IntHolder 
{
	public int value = 0;
}

class SimpleOption extends Option
{
	private int val;
	private IntHolder shared;
	
	public SimpleOption(IntHolder shared, int val) {
		super("Option " + val);
		this.val = val;
		this.shared = shared;
	}

	@Override
	public boolean function() {
		shared.value = val;
		return false;
	}
}

class ArrayHolder
{
	public ArrayList<Integer> values = new ArrayList<Integer>();
}

class SimpleRepeatOption extends Option
{
	private int val;
	private ArrayHolder shared;
	
	public SimpleRepeatOption(ArrayHolder shared, int val) {
		super("Option " + val);
		this.val = val;
		this.shared = shared;
	}

	@Override
	public boolean function() {
		shared.values.add(val);
		return true;
	}
}

public class Test_NumericalMenu {

	@Test
	public void TestOptionExecution() {
		IntHolder shared = new IntHolder();
		
		NumericalMenu menu = new NumericalMenu();
		
		menu.add(new SimpleOption(shared, 1));
		menu.runOptionAtIndex(0);
		
		assertEquals(1, shared.value);
	}
	
	@Test
	public void SimpleMenuExecution()
	{
		InputStream sysIn = System.in;
		
		String inputString = "1\n";
		ByteArrayInputStream in = new ByteArrayInputStream(inputString.getBytes());
		
		System.setIn(in);
		
		IntHolder shared = new IntHolder();
		NumericalMenu menu = new NumericalMenu();
		
		menu.add(new SimpleOption(shared, 1));
		menu.run();
		
		assertEquals(1, shared.value);
		System.setIn(sysIn);
	}
	
//	@Test
//	public void RepeatingMenuExecution()
//	{
//		InputStream sysIn = System.in;
//		
//		// Fails because the first Scanner absorbs all of this input, leaving
//		// nothing for the later created Scanners.  :(
//		String inputString = "1\n";
//		inputString += "2\n";
//		inputString += "4\n";
//		inputString += "5\n";
//		ByteArrayInputStream in = new ByteArrayInputStream(inputString.getBytes());
//		
//		System.setIn(in);
//		
//		ArrayHolder shared = new ArrayHolder();
//		NumericalMenu menu = new NumericalMenu();
//		
//		menu.add(new SimpleRepeatOption(shared, 1));
//		menu.add(new SimpleRepeatOption(shared, 2));
//		menu.add(new SimpleRepeatOption(shared, 3));
//		menu.add(new SimpleRepeatOption(shared, 4));
//		menu.add(new EndMenuOption());
//		menu.run();
//		
//		ArrayList<Integer> expectedOutput = new ArrayList<Integer>();
//		expectedOutput.add(1);
//		expectedOutput.add(2);
//		expectedOutput.add(4);
//		assertEquals(expectedOutput, shared.values);
//		
//		System.setIn(sysIn);
//	}
	
	@Test
	public void getIntInput()
	{
		InputStream sysIn = System.in;
		
		String inputString = "a\n";
		inputString += "(\n";
		inputString += "lol\n";
		inputString += "3\n";
		ByteArrayInputStream in = new ByteArrayInputStream(inputString.getBytes());
		
		System.setIn(in);
		
		int var = NumericalMenu.getIntInput("Please enter an integer: ");
		assertEquals(3, var);
		
		System.setIn(sysIn);
	}
	
	@Test
	public void getYesOrNo()
	{
		InputStream sysIn = System.in;
		
		String inputString = "a\n";
		inputString += "(\n";
		inputString += "lol\n";
		inputString += "3\n";
		inputString += "y\n";
		ByteArrayInputStream in = new ByteArrayInputStream(inputString.getBytes());
		
		System.setIn(in);
		
		boolean var = NumericalMenu.getYesOrNo("Please enter 'y' or 'n': ");
		assertEquals(true, var);
		
		System.setIn(sysIn);
	}
}
