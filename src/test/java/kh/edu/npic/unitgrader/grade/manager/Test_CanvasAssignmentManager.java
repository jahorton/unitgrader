package kh.edu.npic.unitgrader.grade.manager;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class Test_CanvasAssignmentManager {
	public static final File SUBMISSIONS_ARCHIVE = new File("samples/1/submissions.zip");
	public static final File SUBMISSIONS_BASE_DIRECTORY = new File("samples/1/submissions");
	public static final File TEST_SPEC_FILE = new File("samples/1/AssignmentGrader.test");
	public static final File TEST_BASE_DIRECTORY = new File("samples/1/bin");
	
	private static TestSpecification TEST_SPEC;
	
	@Before
	public void setup() {	
		// TODO:  Auto-extract the submissions archive into a nice, isolated 'test' location,
		//        rather than relying on an end-user to extract it for us.
		try
		{
			DirectoryManager.baseSubmissionSelectedDirectory = SUBMISSIONS_BASE_DIRECTORY;
			TEST_SPEC = (TestSpecification) Serialization.fromFile(TEST_SPEC_FILE.toString());
		}
		catch(ClassCastException e)
		{
			System.err.println("Test file does not contain a proper test definition!");
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
