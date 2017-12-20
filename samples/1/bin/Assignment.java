// This file serves as an example implementation by the instructor.
public class Assignment 
{
	// First, the CreditCard section. ------------------------------------
		
	// Combines the other methods seen below to determine the validity of a credit card number.
	public static boolean isValid(long number)
	{
		int numSize = getSize(number);
		
		if(numSize > 16 || numSize < 13) return false;
		
		int sum = sumOfDoubleEvenPlace(number) + sumOfOddPlace(number);
		
		if(sum % 10 != 0) return false;
		
		// For the original assignment, these were the only four valid credit card prefixes.
		if(prefixMatched(number, 4)) return true;
		if(prefixMatched(number, 5)) return true;
		if(prefixMatched(number, 37)) return true;
		if(prefixMatched(number, 6)) return true;
		
		return false;
	}
	
	public static int sumOfDoubleEvenPlace(long number)
	{
		int sum = 0;
		
		while(number > 0)
		{
			int digit = (int) ((number % 100) / 10);
			number /= 100;
			
			sum += getDigit(2 * digit);
		}
		
		return sum;
	}
	
	public static int getDigit(int number)
	{
		return (number / 10) + (number % 10); // Still works the same if it's a single digit number.
	}
	
	public static int sumOfOddPlace(long number)
	{
		int sum = 0;
		
		while(number > 0)
		{
			int digit = (int) (number % 10);
			number /= 100;
			
			sum += getDigit(digit);
		}
		
		return sum;
	}
	
	public static boolean prefixMatched(long number, int d)
	{
		return getPrefix(number, getSize(d)) == d;
	}
	
	public static int getSize(long d)
	{
		int digitCount = 0;
		
		while(d > 0)
		{
			d /= 10; // Chop off one digit.
			digitCount++;
		}
		
		return digitCount;
	}
	
	public static long getPrefix(long number, int k)
	{
		int numberSize = getSize(number);
		
		int decimalMovements = numberSize - k;
		
		while(decimalMovements-- > 0)
		{
			number /= 10;
		}
		
		return number;
	}
	
	// Start of the StringPermutation section. ----------------------------------
		
	public static void displayPermutation(String s)
	{
		displayPermutation("", s);
	}
	
	public static void displayPermutation(String s1, String s2) {
		if (s2.length() == 0)
		{
			System.out.println(s1);
		}
		else
		{
			String str1, str2;
			for (int j = 0; j < s2.length(); j++)
			{
				str1 = s1 + s2.substring(j, j + 1);
				str2 = s2.substring(0, j) + s2.substring(j + 1, s2.length());

				// System.out.println("s2 "+s2);
				displayPermutation(str1, str2);
			}
		}
	}
}
