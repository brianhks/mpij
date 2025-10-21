package mpij;

import org.jargp.*;
import java.net.*;
import java.io.*;
import java.util.*;
import mpij.internal.Task;

public class Listener
	{
	private int m_port;
	private boolean m_runOnce;
	private int m_timeout;
	
	private static class CommandLine
		{
		public boolean runOnce;
		public int port;
		public int timeout;  //Timeout before the sytem exits
		public String rootHost;
		public String runnableClass;
		public int peerCount;
		public int taskCount;
		
		public CommandLine()
			{
			//Defaults
			runOnce = false;
			port = Launcher.DEFAULT_PORT +1;
			timeout = 60000 * 30;  //Default to 30 min
			rootHost = "";
			taskCount = 1;
			}
		}
		
	private static final ParameterDef[] PARAMETERS = 
		{
		new BoolDef('o', "runOnce"),
		new IntDef('p', "port"),
		new IntDef('t', "taskCount"),
		new StringDef('r', "rootHost"),
		new StringDef('c', "runnableClass"),
		new IntDef('n', "peerCount"),
		};
		
		
	public static void main(String[] args)
			throws Exception
		{
		CommandLine cl = new CommandLine();
		LinkedList<Task> taskList = new LinkedList<Task>();
		
		ArgumentProcessor proc = new ArgumentProcessor(PARAMETERS);
		proc.processArgs(args, cl);
		
		for (int I = 0; I < cl.taskCount; I++)
			{
			Task task = new Task(new InetSocketAddress(cl.rootHost, cl.port), cl.peerCount,
					cl.runnableClass, null);
			task.start();
			taskList.add(task);
			}
			
		Iterator<Task> it = taskList.iterator();
		while (it.hasNext())
			{
			it.next().join();
			}
		}
		
	public Listener(int port, boolean runOnce, int timeout)
		{
		m_port = port;
		m_runOnce = runOnce;
		m_timeout = timeout;
		}
		
	public void runServer()
		{
		int locId;
		Vector<SocketAddress> peers;
		
		try
			{
			ServerSocket server = new ServerSocket(m_port);
			
			Socket socket;
			System.out.println("Waiting for connect");
			while ((socket = server.accept()) != null)
				{
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				peers = new Vector<SocketAddress>();
				int myPort = 0;
				
				locId = dis.readInt();
				System.out.println("locId "+locId);
				int peerCount = dis.readInt();
				System.out.println("peerCount "+peerCount);
				
				for (int I = 0; I < peerCount; I++)
					{
					int port = dis.readInt();
					System.out.println("port "+port);
					if (I == locId)
						myPort = port;
					int addrSize = (int)dis.readByte();
					System.out.println("addrSize "+addrSize);
					byte[] addr = new byte[addrSize];
					
					for (int J = 0; J < addrSize; J++)
						addr[J] = dis.readByte();
						
					
					peers.add(new InetSocketAddress(InetAddress.getByAddress(addr), port));
					}
					
				dis.close();
				socket.close();
				
				/* Task task = new Task((SocketAddress[])peers.toArray(new SocketAddress[0]), 
						myPort, locId, null, Thread.currentThread());
				task.start(); */
				
				
				if (m_runOnce)
					server.close();
				}
			}
		catch (Exception e)
			{
			//if (!(e instanceof SocketException))
				e.printStackTrace();
			}
			
		try
			{
			Thread.sleep(m_timeout);
			System.exit(-1);
			}
		catch (InterruptedException ie)
			{
			}
		
		}
		
	}
