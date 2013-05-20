import mpij.*;

public class ConnectionTest implements MPIRunnable
	{
	public ConnectionTest()
		{
		}
		
	public void run(Communicator commWorld)
		{
		System.out.println(commWorld.getRank()+" is up");
		}
	}
