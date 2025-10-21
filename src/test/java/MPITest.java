
import mpij.*;
import mpij.msg.*;

public class MPITest implements MPIRunnable
	{
	public MPITest()
		{
		System.out.println("Test constructor called");
		}
		
	public void run(Communicator commWorld)
			throws Exception
		{
		System.out.println("I'm running weeeee");
		Thread.sleep(1000);
		}
	}
