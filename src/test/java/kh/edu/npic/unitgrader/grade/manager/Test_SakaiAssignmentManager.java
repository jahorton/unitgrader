package kh.edu.npic.unitgrader.grade.manager;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.zeroturnaround.zip.ZipUtil;

import kh.edu.npic.unitgrader.grade.manager.SakaiAssignmentManager.SakaiData;
import kh.edu.npic.unitgrader.util.Serialization;
import kh.edu.npic.unitgrader.util.TestSpecification;
import kh.edu.npic.unitgrader.util.preferences.DirectoryManager;

public class Test_SakaiAssignmentManager {
	public static final File SUBMISSIONS_ARCHIVE = new File("samples/1/sakai/submissions.zip").getAbsoluteFile();
	public static final File SUBMISSIONS_BASE_DIRECTORY = new File(".testing/1/sakai/submissions").getAbsoluteFile();
	public static final File TEST_SPEC_FILE = new File("samples/1/sakai/AssignmentGrader.test").getAbsoluteFile();
	public static final File TEST_BASE_DIRECTORY = new File("samples/1/sakai").getAbsoluteFile();
	
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
		SakaiAssignmentManager manager = new SakaiAssignmentManager(SUBMISSIONS_BASE_DIRECTORY, TEST_SPEC, TEST_BASE_DIRECTORY);
		
		assertEquals(2, manager.getStudentIDMap().size());
		assertNotNull(manager.getStudentData("899100"));
		assertEquals(1, manager.matchFirstname("secondtest").size());
		assertEquals(2, manager.matchLastname("student").size());
		
		StudentData<SakaiData> student = manager.getStudentData("899100");
		File studentPath = student.getCodeFolder();
		assertEquals(new File("student, test (899100)" + File.separator + "files"), studentPath);
	}

}
