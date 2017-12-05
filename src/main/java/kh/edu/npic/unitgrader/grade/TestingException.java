package kh.edu.npic.unitgrader.grade;

public class TestingException extends Exception 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7600041768937890393L;

	public TestingException() 
	{
		super();
	}

	public TestingException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) 
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public TestingException(String message, Throwable cause) 
	{
		super(message, cause);
	}

	public TestingException(String message) 
	{
		super(message);
	}

	public TestingException(Throwable cause) 
	{
		super(cause);
	}

}
