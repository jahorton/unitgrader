import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Scanner;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StringPermutationGrader
{
	private PrintStream sysout;
	private ByteArrayOutputStream consoleOutput;
	
	@Before
	public void setup()
	{
		consoleOutput = new ByteArrayOutputStream();
		sysout = System.out;
		
		System.setOut(new PrintStream(consoleOutput));
	}
	
	@Test(timeout = 200)
	public void bookExample() 
	{
		Assignment.displayPermutation("abc");
		
		// For the curious:  use of HashSet allows me to ignore the ordering in which you output things.
		// The "equals" check will simply ensure that ALL contents match between the two HashSets.
		
		HashSet<String> permutations = analyzeOutput("[abc]+");
		
		HashSet<String> comparison = parse("[abc]+", "abc acb bac bca cab cba");
		
		Assert.assertEquals(comparison, permutations);
	}
	
	@Test(timeout = 200)
	public void alternateTest() 
	{
		Assignment.displayPermutation("extra");
		
		HashSet<String> permutations = analyzeOutput("[extra]+");
		
		HashSet<String> comparison = parse("[extra]+", "terax raxte aetrx trxae taerx artxe rtxae txear rxtae axret xtrea exatr xaret xeatr erxat rexta arxte rtaxe reatx xater atrxe tarxe raetx rtexa extra xreat atrex rteax eraxt erxta aextr atexr arext xaert traex axter xtare etaxr exrta extar xtear raxet txare xerta texra etxra trexa traxe txera atxer ratex ertax etarx eartx retax txrea exrat aexrt aterx rxeta etrxa arxet axert aertx xtrae xrtae taxer retxa etxar trxea rexat etrax xrate rtxea axtre eatxr aerxt xraet axetr artex raext teaxr taexr rtaex eaxrt txaer tarex rxate xtera xatre treax eratx aretx xaetr texar rxaet xetar rxeat aetxr xtaer taxre exart xeart eatrx xetra ertxa xerat tearx earxt axrte xarte eaxtr atxre ratxe reaxt terxa xreta txrae xrtea rxtea");
		
		Assert.assertEquals(comparison, permutations);
	}
	
	@Test(timeout = 200)
	public void partialTest()
	{
		Assignment.displayPermutation("ab", "abc");
		
		HashSet<String> permutations = analyzeOutput("[abc]+");
		
		//System.out.println(permutations);
		
		HashSet<String> comparison = parse("[abc]+", "ababc abacb abbac abbca abcab abcba");
		
		Assert.assertEquals(comparison, permutations);
	}
	
	
	private HashSet<String> analyzeOutput(String pattern)
	{
		return parse(pattern, consoleOutput.toString());
	}
	
	private HashSet<String> parse(String pattern, String textbody)
	{
		Scanner scan = new Scanner(textbody);
		
		HashSet<String> permutations = new HashSet<String>();
		
		while(scan.hasNext())
		{
			if(scan.hasNext(pattern))
				permutations.add(scan.next(pattern));
			else scan.next();
		}
		
		return permutations;
	}
	
	@After
	public void tearDown()
	{
		// Undoes the console rerouting.
		System.setOut(sysout);
	}
}
