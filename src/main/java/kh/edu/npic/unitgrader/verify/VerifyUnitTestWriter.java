package kh.edu.npic.unitgrader.verify;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.*;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import kh.edu.npic.unitgrader.util.ManualClassLoader;

/**
 * Analyzes a Java file in order to create a spec-checking JUnit test for distribution to students.
 * Not completely ironclad, but is designed to be easily sufficient for the courses COP 2800 and COP 3502.
 * <p>
 * Note:  certain field-oriented tests that are produced should be eliminated as appropriate per assignment.
 * (Deletion is easy, manual addition is harder.)
 * @author Joshua A. Horton
 *
 */
public class VerifyUnitTestWriter 
{
	private StringBuilder text;
	private String className;
	
	public static void main(String[] args)
	{
		int res;
		
		JFileChooser chooseTest = new JFileChooser();
		chooseTest.setDialogTitle("Select the Java *.class file whose signatures ought be used for verification.");
		chooseTest.setFileFilter(new FileNameExtensionFilter("Java Class Bytecode (*.class)", "class"));

		res = chooseTest.showOpenDialog(null);

	    switch(res)
	    {
	    	case JFileChooser.CANCEL_OPTION:
	    		return;
	    	case JFileChooser.APPROVE_OPTION:
	    		//System.out.println("We would open " + chooseTest.getSelectedFile() + " as the JUnit test.");
	    		break;
	    	case JFileChooser.ERROR_OPTION:
	    		System.err.println("Error detected in results from file selection.");
	    		return;
	    	default:
	    		System.err.println("Impossible result from file chooser dialog.");
	    		return;
	    }

		File testFile = chooseTest.getSelectedFile();
		Class<?> clazz;
		
		try
		{
			clazz = ManualClassLoader.loadClassFromFile(testFile);
		}
		catch (IOException e) 
		{
			System.err.println("Error when attempting to read class file.");
			return;
		}

		// Determine default save filename.
		
		String defaultDirectory = testFile.getParent();
		
		File saveFile = new File(defaultDirectory + File.separator + getSuggestedTestClassName(clazz));
		
		// Choose save destination
		JFileChooser chooseTest2 = new JFileChooser();
		chooseTest2.setDialogTitle("Where would you like to save the resulting verification JUnit test source?");
		chooseTest2.setFileFilter(new javax.swing.filechooser.FileFilter(){

			@Override
			public boolean accept(File f) {
				if(f.isDirectory()) return true;
				
				int extStart = f.getName().lastIndexOf('.');
				return (f.getName().substring(extStart).equals(".java"));
			}

			@Override
			public String getDescription() {
				return "JUnit Test file (*.java)";
			}});
		chooseTest2.setSelectedFile(saveFile);
		res = chooseTest2.showSaveDialog(null);

	    switch(res)
	    {
	    	case JFileChooser.CANCEL_OPTION:
	    		return;
	    	case JFileChooser.APPROVE_OPTION:
	    		//System.out.println("We would save " + chooseTest.getSelectedFile() + " as the JUnit test.");
	    		break;
	    	case JFileChooser.ERROR_OPTION:
	    		System.err.println("Error detected in results from file selection.");
	    		return;
	    	default:
	    		System.err.println("Impossible result from file chooser dialog.");
	    		return;
	    }

		saveFile = chooseTest2.getSelectedFile();
		
		if(saveFile.toString().indexOf(".java") == -1)
		{
			saveFile = new File(saveFile.toString() + ".java");
		}

		try 
		{
			create(clazz, saveFile);
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("Could not generate an appropriate save file.");
		}
	}
	
	private VerifyUnitTestWriter(String name)
	{
		this.className = name;
		text = new StringBuilder(4096);
	}
	
	public static void create(Class<?> clazz, File saveFile) throws FileNotFoundException
	{
		if(saveFile.isDirectory()) throw new IllegalArgumentException("Parameter \"saveFile\" must not represent a directory!");
		
		String name = saveFile.getName().substring(0, saveFile.getName().indexOf('.'));
		
		VerifyUnitTestWriter builder = new VerifyUnitTestWriter(name);
		
		builder.addImports();
		builder.writeClassBody(clazz);
				
		PrintWriter out = new PrintWriter(new FileOutputStream(saveFile));
		out.write(builder.text.toString());
		
		out.close();
	}
	
	private String getTestClassName(Class<?> clazz)
	{
		return className;
	}
	
	public static String getSuggestedTestClassName(Class<?> clazz)
	{
		return clazz.getSimpleName() + "_Verifier";
	}
	
	private void writeClassBody(Class<?> clazz)
	{
		text.append("public class " + getTestClassName(clazz) + "\n");
		text.append("{\n");
		
		try 
		{
			clazz.getDeclaredConstructor();
		}
		catch (NoSuchMethodException e) 
		{
			writeNoDefaultConstructorTest(clazz);
		}
		
		writeConstructorTests(clazz);
		writeMethodTests(clazz);
		writeFieldTests(clazz);
		
		text.append("}\n");
	}
	
	// It's possible to break a resulting code file by having two diff parameters
	// with the same name in a different package.  Fixable by hand, though, and
	// the resulting code can always be fixed before being made public.
	
	private String getConstructorTestName(Constructor<?> c)
	{
		String str = "testConstructor";
		
		for(Class<?> param:c.getParameterTypes())
		{
			str += "_" + param.getSimpleName();
		}
		
		str += "_test";
		
		return str;
	}
	
	private void writeConstructorTests(Class<?> clazz)
	{
		for(Constructor<?> cons:clazz.getDeclaredConstructors())
		{
			text.append("\t@Test\n");
			text.append("\tpublic void " + getConstructorTestName(cons) + "()\n");
			text.append("\t{\n");
			
				Class<?>[] params = cons.getParameterTypes();
			
				// Build the easily-readable "expecting" comment for future reference.
				text.append("\t\t// Expecting constructor: " + Modifier.toString(cons.getModifiers()) + " " + clazz.getSimpleName() + "(");
				for(int i=0; i < params.length; i++)
				{
					if(i != 0) text.append(", ");
					text.append(params[i].getSimpleName() + " arg" + i);
				}
				text.append(")\n");
				
				text.append("\t\ttry\n");
				text.append("\t\t{\n");
					
					text.append("\t\t\tConstructor<?> c = " + clazz.getName() + ".class.getDeclaredConstructor(");
					for(int i=0; i < params.length; i++)
					{
						if(i != 0) text.append(", ");
						
						text.append(params[i].getName() + ".class");
					}
					text.append(");\n");
					
					text.append("\t\t\tif(c.getModifiers() != " + cons.getModifiers() + ") // Must be \"" + Modifier.toString(cons.getModifiers()) + "\"\n");
						text.append("\t\t\t\tAssert.fail(\"Constructor is improperly declared!  Check your public/protected/private settings!\");\n");
				
				text.append("\t\t}\n");
				text.append("\t\tcatch (NoSuchMethodException e)\n");
				text.append("\t\t{\n");
				
					text.append("\t\t\tAssert.fail(\"Required constructor is missing!\");\n");
				
				text.append("\t\t}\n");
				text.append("\t\tcatch (SecurityException e)\n");
				text.append("\t\t{\n");
				
					text.append("\t\t\tAssert.fail(\"Somehow unable to test for a required constructor!\");\n");
				
				text.append("\t\t}\n");
			
			text.append("\t}\n\n");
		}
		text.append("\n");
	}
	
	private void writeNoDefaultConstructorTest(Class<?> clazz)
	{
		text.append("\t@Test\n");
		text.append("\tpublic void " + "testConstructor_noDefault_test" + "()\n");
		text.append("\t{\n");
		
			// Build the easily-readable "expecting" comment for future reference.
			text.append("\t\t// Expecting NO constructor: " + "public " + clazz.getSimpleName() + "()\n");
			
			text.append("\t\ttry\n");
			text.append("\t\t{\n");
				
				text.append("\t\t\tConstructor<?> c = " + clazz.getName() + ".class.getDeclaredConstructor();\n");
				
				text.append("\t\t\tif(c.getModifiers() != " + Modifier.PRIVATE + ") // Must be private if it exists. \n");
					text.append("\t\t\t\tAssert.fail(\"Default constructor should not be declared!\");\n");
			
			text.append("\t\t}\n");
			text.append("\t\tcatch (NoSuchMethodException e)\n");
			text.append("\t\t{\n");
			
				text.append("\t\t\treturn;\n");
			
			text.append("\t\t}\n");
			text.append("\t\tcatch (SecurityException e)\n");
			text.append("\t\t{\n");
			
				text.append("\t\t\tAssert.fail(\"Somehow unable to test for non-existence of a constructor!\");\n");
			
			text.append("\t\t}\n");
		
		text.append("\t}\n\n");
	}
	
	private String getMethodTestName(Method m)
	{
		String str = m.getName();
		
		for(Class<?> param:m.getParameterTypes())
		{
			str += "_" + param.getSimpleName();
		}
		
		str += "_test";
		
		return str;
	}
	
	private void writeMethodTests(Class<?> clazz)
	{
		for(Method method:clazz.getDeclaredMethods())
		{
			//Only do the ones defined directly within this class.
			if(!method.getDeclaringClass().equals(clazz)) continue;
			
			text.append("\t@Test\n");
			text.append("\tpublic void " + getMethodTestName(method) + "()\n");
			text.append("\t{\n");
			
				Class<?>[] params = method.getParameterTypes();

				// Build the easily-readable "expecting" comment for future reference.
				text.append("\t\t// Expecting method: " + Modifier.toString(method.getModifiers()) + " " + method.getReturnType().getSimpleName() + " " + method.getName() + "(");
				for(int i=0; i < params.length; i++)
				{
					if(i != 0) text.append(", ");
					text.append(params[i].getSimpleName() + " arg" + i);
				}
				text.append(")\n");
			
				text.append("\t\ttry\n");
				text.append("\t\t{\n");
					
					text.append("\t\t\tMethod m = " + clazz.getName() + ".class.getDeclaredMethod(\"" + method.getName() + "\"");
					for(int i=0; i < params.length; i++)
					{
						text.append(", ");
						text.append(params[i].getName() + ".class");
					}
					text.append(");\n");
					
					text.append("\t\t\tif(m.getModifiers() != " + method.getModifiers() + ") // Must be \"" + Modifier.toString(method.getModifiers()) + "\"\n");
						text.append("\t\t\t\tAssert.fail(\"Method is improperly declared!  Check your public/protected/private settings!\");\n");
				
				text.append("\t\t}\n");
				text.append("\t\tcatch (NoSuchMethodException e)\n");
				text.append("\t\t{\n");
				
					text.append("\t\t\tAssert.fail(\"Required method is missing!\");\n");
				
				text.append("\t\t}\n");
				text.append("\t\tcatch (SecurityException e)\n");
				text.append("\t\t{\n");
				
					text.append("\t\t\tAssert.fail(\"Somehow unable to test for a required method!\");\n");
				
				text.append("\t\t}\n");
			
			text.append("\t}\n\n");
		}
		text.append("\n");
	}

	private String getFieldTestName(Field f)
	{
		String str = f.getName();
		
		//for(Class<?> param:f.getParameterTypes())
		str += "_as_" + f.getType().getSimpleName();
		
		str += "_test";
		
		return str;
	}
	
	private void writeFieldTests(Class<?> clazz)
	{
		for(Field field:clazz.getDeclaredFields())
		{
			//Only do the ones defined directly within this class.
			if(!field.getDeclaringClass().equals(clazz)) continue;
			
			text.append("\t@Test\n");
			text.append("\tpublic void " + getFieldTestName(field) + "()\n");
			text.append("\t{\n");
			
				// Build the easily-readable "expecting" comment for future reference.
				text.append("\t\t// Expecting field: " + Modifier.toString(field.getModifiers()) + " " + field.getType().getSimpleName() + " " + field.getName() + "\n");
			
				text.append("\t\ttry\n");
				text.append("\t\t{\n");
					
					text.append("\t\t\tField f = " + clazz.getName() + ".class.getDeclaredField(\"" + field.getName() + "\");\n");
					
					text.append("\t\t\tif(f.getModifiers() != " + field.getModifiers() + ") // Must be \"" + Modifier.toString(field.getModifiers()) + "\"\n");
						text.append("\t\t\t\tAssert.fail(\"Field is improperly declared!  Check your public/protected/private settings!\");\n");
						
					text.append("\t\t\tif(!f.getType().equals(" + field.getType().getName() + ".class" + "))\n");
					text.append("\t\t\t\tAssert.fail(\"Field isn't of the correct type!\");\n");
				
				text.append("\t\t}\n");
				text.append("\t\tcatch (NoSuchFieldException e)\n");
				text.append("\t\t{\n");
				
					text.append("\t\t\tAssert.fail(\"Required field is missing!\");\n");
				
				text.append("\t\t}\n");
				text.append("\t\tcatch (SecurityException e)\n");
				text.append("\t\t{\n");
				
					text.append("\t\t\tAssert.fail(\"Somehow unable to test for a required field!\");\n");
				
				text.append("\t\t}\n");
			
			text.append("\t}\n\n");
		}
		text.append("\n");
	}
	
	private void addImports()
	{
		text.append("import org.junit.Assert;\n");
		text.append("import org.junit.Test;\n");
		text.append("\n");
		text.append("import java.lang.reflect.*;\n");
		text.append("\n");
	}
}