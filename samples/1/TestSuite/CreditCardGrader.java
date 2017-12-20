import org.junit.Test;
import org.junit.Assert;

public class CreditCardGrader 
{

	@Test(timeout = 200)
	public void debug_getPrefixTest() 
	{
		Assert.assertEquals(43, Assignment.getPrefix(4388576018410707L, 2));
		Assert.assertEquals(34, Assignment.getPrefix(344381667384076L, 2));
	}
	
	@Test(timeout = 200)
	public void debug_getSizeTests() 
	{
		Assert.assertEquals(16, Assignment.getSize(4388576018410707L));
		Assert.assertEquals(15, Assignment.getSize(344381667384076L));
		
		Assert.assertEquals(2, Assignment.getSize(43));
	}
	
	@Test(timeout = 200)
	public void debug_prefixMatchTest()
	{
		Assert.assertEquals(true, Assignment.prefixMatched(4388576018410707L, 43));
	}
	
	@Test(timeout = 200)
	public void debug_evenSumTest()
	{
		Assert.assertEquals(37, Assignment.sumOfDoubleEvenPlace(4388576018402626L));
		Assert.assertEquals(38, Assignment.sumOfDoubleEvenPlace(344381667384076L));
	}
	
	@Test(timeout = 200)
	public void debug_oddSumTest()
	{
		Assert.assertEquals(38, Assignment.sumOfOddPlace(4388576018402626L));
		Assert.assertEquals(42, Assignment.sumOfOddPlace(344381667384076L));
	}
	
	@Test(timeout = 200)
	public void bookExamplesTest()
	{
		Assert.assertEquals(true, Assignment.isValid(4388576018410707L));
		Assert.assertEquals(false, Assignment.isValid(4388576018402626L));
	}
	
	@Test(timeout = 200)
	public void standardEvenDigits()
	{
		Assert.assertEquals(true,  Assignment.isValid(6011326990276126L));		
		Assert.assertEquals(false, Assignment.isValid(6011326990276123L));  // Fails the Luhn check.
		Assert.assertEquals(true,  Assignment.isValid(5436863455832646L));	
	}
	
	@Test(timeout = 200)
	public void standardOddDigits()
	{
		Assert.assertEquals(true,  Assignment.isValid(371121541135179L));
		Assert.assertEquals(true,  Assignment.isValid(4131402767965L));		
	}
	
	@Test(timeout = 200)
	public void badPrefix_OddDigits()
	{
		Assert.assertEquals(false, Assignment.isValid(1305791468566L));   // Bad prefix, good number.  Is a short one.
		Assert.assertEquals(false, Assignment.isValid(344381667384076L)); // 34 isn't supposed to be a valid prefix for this assignment.
	}

	@Test(timeout = 200)
	public void badPrefix_EvenDigits()
	{
		Assert.assertEquals(false, Assignment.isValid(9160266726415254L)); // Bad prefix, good number.  Is a long one.	
	}
	
	@Test(timeout = 200)
	public void badLengthTests()
	{
		Assert.assertEquals(false, Assignment.isValid(48706286977L));  // Too short, but meets the other checks.
		Assert.assertEquals(false, Assignment.isValid(601132699027612659L));  // Too long, meets other checks.
	}
	
//	// Used to test numbers that meet the constraints, but fail in other aspects.
//	@Test(timeout = 200)
//	public void luhnCheckTester()
//	{
//		long number = 9160266726415254L;
//		
//		int sum = Assignment.sumOfDoubleEvenPlace(number) + Assignment.sumOfOddPlace(number);
//		
//		if(sum % 10 != 0) fail("Obtained Luhn check sum for " + number + " is " + sum + " - not divisible by 10!");
//	}
}
