package kh.edu.npic.unitgrader.grade;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.zeroturnaround.zip.ZipUtil;

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
	public void run_flawless() throws TestingException {
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
	
	@Test
	public void run_average() throws TestingException {
		CanvasAssignmentManager manager = new CanvasAssignmentManager(SUBMISSIONS_BASE_DIRECTORY, TEST_SPEC, TEST_BASE_DIRECTORY);
		StudentData<CanvasData> studentData = manager.getStudentData("899101");
		
		Map<TestCase, TestResult> results = StudentTester.run(TEST_SPEC, studentData);
		assertNotNull(results);
		assertFalse(results.isEmpty());
		
		assertEquals(1, results.get(TEST_SPEC.testCases.get(1)).crashes.size());
		assertEquals(3, results.get(TEST_SPEC.testCases.get(2)).crashes.size());
	}
}
