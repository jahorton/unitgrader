package kh.edu.npic.unitgrader.grade.manager;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.zeroturnaround.zip.ZipUtil;

import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class Test_CanvasAssignmentManager {
	public static final File SUBMISSIONS_ARCHIVE = new File("samples/1/canvas/submissions.zip").getAbsoluteFile();
	public static final File SUBMISSIONS_BASE_DIRECTORY = new File(".testing/1/canvas/submissions").getAbsoluteFile();
	public static final File TEST_SPEC_FILE = new File("samples/1/canvas/AssignmentGrader.test").getAbsoluteFile();
	public static final File TEST_BASE_DIRECTORY = new File("samples/1/canvas").getAbsoluteFile();
	
	private static TestSpecification TEST_SPEC;
	
	@Before
	public void setup() {
		try 
		{
			ZipUtil.unpack(SUBMISSIONS_ARCHIVE, SUBMISSIONS_BASE_DIRECTORY);
		} 
		catch (Exception e) 
		{
			fail("Could not unzip the submissions archive file.");
		}
		
		try
		{
			DirectoryManager.baseSubmissionSelectedDirectory = SUBMISSIONS_BASE_DIRECTORY;
			TEST_SPEC = (TestSpecification) Serialization.fromFile(TEST_SPEC_FILE.toString());
		}
		catch(ClassCastException e)
		{
			fail("Could not load the test specification file!");
		}
	}
	
	// Ensure that our Canvas-based sample can load properly.
	@Test
	public void init() {
		CanvasAssignmentManager manager = new CanvasAssignmentManager(SUBMISSIONS_BASE_DIRECTORY, TEST_SPEC, TEST_BASE_DIRECTORY);
		
		assertEquals(1, manager.getStudentIDMap().size());
		assertNotNull(manager.getStudentData("899100"));
		assertEquals(1, manager.matchFirstname("test").size());
		assertEquals(1, manager.matchLastname("student").size());
	}

}
