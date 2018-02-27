package kh.edu.npic.unitgrader.grade;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import kh.edu.npic.unitgrader.grade.manager.CanvasAssignmentManager;
import kh.edu.npic.unitgrader.grade.manager.CanvasAssignmentManager.CanvasData;
import kh.edu.npic.unitgrader.grade.manager.StudentData;
import kh.edu.npic.unitgrader.grade.results.TestResult;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestCase;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class Test_StudentTester {
	// Make sure the paths are fully resolved as absolute - this is necessary for the child process to do its thing.
	public static final File SUBMISSIONS_ARCHIVE = new File("samples/1/submissions.zip").getAbsoluteFile();
	public static final File SUBMISSIONS_BASE_DIRECTORY = new File("samples/1/submissions").getAbsoluteFile();
	public static final File TEST_SPEC_FILE = new File("samples/1/AssignmentGrader.test").getAbsoluteFile();
	public static final File TEST_BASE_DIRECTORY = new File("samples/1").getAbsoluteFile();
	
	private static TestSpecification TEST_SPEC;
	
	@Before
	public void setup() {	
		// TODO:  Auto-extract the submissions archive into a nice, isolated 'test' location,
		//        rather than relying on an end-user to extract it for us.
		try
		{
			DirectoryManager.baseSubmissionSelectedDirectory = SUBMISSIONS_BASE_DIRECTORY;
			DirectoryManager.testSpecSelectedDirectory = TEST_BASE_DIRECTORY;
			TEST_SPEC = (TestSpecification) Serialization.fromFile(TEST_SPEC_FILE.toString());
		}
		catch(ClassCastException e)
		{
			System.err.println("Test file does not contain a proper test definition!");
			fail("Could not load the test specification file!");
		}
	}
	
	@Test
	public void run() throws TestingException {
		CanvasAssignmentManager manager = new CanvasAssignmentManager(SUBMISSIONS_BASE_DIRECTORY, TEST_SPEC, TEST_BASE_DIRECTORY);
		StudentData<CanvasData> studentData = manager.getStudentData("899100");
		
		Map<TestCase, TestResult> results = StudentTester.run(TEST_SPEC, studentData);
		assertNotNull(results);
		assertFalse(results.isEmpty());
		
		for(TestCase tc: TEST_SPEC.testCases)
		{
			assertNotNull(results.get(tc));
			assertEquals(0, results.get(tc).failures.size());
			assertEquals(0, results.get(tc).crashes.size());
		}
	}

}
