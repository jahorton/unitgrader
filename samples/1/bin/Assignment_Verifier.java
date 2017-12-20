import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.*;

public class Assignment_Verifier
{
	@Test
	public void testConstructor_test()
	{
		// Expecting constructor: public Assignment()
		try
		{
			Constructor<?> c = Assignment.class.getDeclaredConstructor();
			if(c.getModifiers() != 1) // Must be "public"
				Assert.fail("Constructor is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required constructor is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required constructor!");
		}
	}


	@Test
	public void getSize_long_test()
	{
		// Expecting method: public static int getSize(long arg0)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("getSize", long.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void isValid_long_test()
	{
		// Expecting method: public static boolean isValid(long arg0)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("isValid", long.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void sumOfDoubleEvenPlace_long_test()
	{
		// Expecting method: public static int sumOfDoubleEvenPlace(long arg0)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("sumOfDoubleEvenPlace", long.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void displayPermutation_String_String_test()
	{
		// Expecting method: public static void displayPermutation(String arg0, String arg1)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("displayPermutation", java.lang.String.class, java.lang.String.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void displayPermutation_String_test()
	{
		// Expecting method: public static void displayPermutation(String arg0)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("displayPermutation", java.lang.String.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void sumOfOddPlace_long_test()
	{
		// Expecting method: public static int sumOfOddPlace(long arg0)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("sumOfOddPlace", long.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void prefixMatched_long_int_test()
	{
		// Expecting method: public static boolean prefixMatched(long arg0, int arg1)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("prefixMatched", long.class, int.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void getDigit_int_test()
	{
		// Expecting method: public static int getDigit(int arg0)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("getDigit", int.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}

	@Test
	public void getPrefix_long_int_test()
	{
		// Expecting method: public static long getPrefix(long arg0, int arg1)
		try
		{
			Method m = Assignment.class.getDeclaredMethod("getPrefix", long.class, int.class);
			if(m.getModifiers() != 9) // Must be "public static"
				Assert.fail("Method is improperly declared!  Check your public/protected/private settings!");
		}
		catch (NoSuchMethodException e)
		{
			Assert.fail("Required method is missing!");
		}
		catch (SecurityException e)
		{
			Assert.fail("Somehow unable to test for a required method!");
		}
	}



}
