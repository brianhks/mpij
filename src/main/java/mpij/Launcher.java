package mpij;

import org.jargp.*;
import java.net.*;
import java.io.*;
import java.util.*;
import mpij.internal.Task;
import mpij.internal.util.Pair;

import static java.lang.System.out;

/**
Launches the mpij program and starts tasks on listeners
*/
public class Launcher
	{
	public static final Integer DEFAULT_PORT = new Integer(8540);
	
	//File types
	private static final String MPIJ_FILE_TYPE = "mpij";
	private static final String P4PG_FILE_TYPE = "p4pg";
	
	private static class PeerData
		{
		public InetSocketAddress m_address;
		public String m_command;
		public int m_taskCount;
		
		public PeerData(InetSocketAddress addr, String command, int taskCount)
			{
			m_address = addr;
			m_command = command;
			m_taskCount = taskCount;
			}
		}
	
	private static class CommandLine
		{
		public String peerFile;
		public String fileType;
		public String mpiRunnable;
		public String startCommand;
		public boolean help;
		
		public CommandLine()
			{
			peerFile = null;
			fileType = null;
			mpiRunnable = null;
			startCommand = null;
			help = false;
			}
		}
		
	private static final ParameterDef[] PARAMETERS = 
		{
		new StringDef('p', "peerFile"),
		new StringDef('t', "fileType"),
		new StringDef('c', "mpiRunnable"),
		new StringDef('s', "startCommand"),
		new BoolDef('?', "help"),
		};
		
		
	private static void printHelp()
		{
		out.println("  -p: Process file");
		out.println("  -t: Process file type");
		out.println("  -c: Java class that implements MPIRunnable");
		out.println("  -s: Launch command (ex ssh, rsh, etc...)");
		out.println("  -?: Print this help");
		}
		
	
	//---------------------------------------------------------------------------
	private static Collection<PeerData> readP4PGFile(String peerFile)
			throws UnknownHostException, IOException
		{
		LinkedList<PeerData> peers = new LinkedList<PeerData>();
		
		BufferedReader br = new BufferedReader(new FileReader(peerFile));
		
		String line;
		while ((line = br.readLine()) != null)
			{
			int endaddr = line.indexOf(' ');
			String addrstr = line.substring(0, endaddr);
			
			int endcount = line.indexOf(' ', endaddr+1);
			if (endcount == -1)
				endcount = line.length();
				
			int clientCount = Integer.parseInt(line.substring(endaddr+1, endcount));
			
			String command = "";
			if (endcount < line.length())
				command = line.substring(endcount+1); 
			
			System.out.println("Command: "+command);
			peers.add(new PeerData(new InetSocketAddress(InetAddress.getByName(addrstr), DEFAULT_PORT),
					command, clientCount));
			}
			
		return (peers);
		}
	
	//---------------------------------------------------------------------------
	private static InetSocketAddress[] readMPIJFile(String peerFile)
			throws UnknownHostException, IOException
		{
		Vector<InetSocketAddress> addresses = new Vector<InetSocketAddress>();
		Map<String, Integer> portMap = new HashMap<String, Integer>();
		BufferedReader br = new BufferedReader(new FileReader(peerFile));
		
		String line;
		while ((line = br.readLine()) != null)
			{
			String[] split = line.split(" ");
			
			Integer port = portMap.get(split[0]);
			if (port == null)
				{
				port = DEFAULT_PORT;
				}
			else
				{
				port = new Integer(port.intValue() + 5);
				}
				
			portMap.put(split[0], port);
			
			addresses.add(new InetSocketAddress(InetAddress.getByName(split[0]), port.intValue()));
			}
		
		
		/* InetSocketAddress[] ret = new InetSocketAddress[]
			{
			new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9000),
			new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9100),
			}; */
		/* InetSocketAddress[] ret = new InetSocketAddress[]
			{
			new InetSocketAddress(InetAddress.getByName("192.168.1.104"), 9000),
			new InetSocketAddress(InetAddress.getByName("192.168.1.4"), 9100),
			}; */
		/* InetSocketAddress[] ret = new InetSocketAddress[] 
			{
			new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9000),
			new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9100),
			new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9200),
			new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 9300)
			}; */
			
		return (addresses.toArray(new InetSocketAddress[0]));
		}
		
	//---------------------------------------------------------------------------
	public static void main(String[] args)
			throws Exception
		{
		CommandLine cl = new CommandLine();
		
		ArgumentProcessor proc = new ArgumentProcessor(PARAMETERS);
		proc.processArgs(args, cl);
		
		if (cl.help)
			{
			printHelp();
			return;
			}
		
		Collection<PeerData> peers;
		LinkedList<Process> procs = new LinkedList<Process>();
		
		/* if (cl.fileType.equals(MPIJ_FILE_TYPE))
			peers = readMPIJFile(cl.peerFile);
		else */ 
		if (cl.fileType.equals(P4PG_FILE_TYPE))
			{
			peers = readP4PGFile(cl.peerFile);
			}
		else
			{
			System.out.println("Unknown file type");
			return;
			}
		
		Iterator<PeerData> it = peers.iterator();
		int peerCount = 1;
		while (it.hasNext())
			peerCount += it.next().m_taskCount;
			
		Task task = new Task(null, peerCount, cl.mpiRunnable, null);
		int port = task.getPostOffice().getLocalPort();
		
		it = peers.iterator();
		PeerData pd = it.next(); //get the root node to see if more task are to be ran
		for (int I = 0; I < pd.m_taskCount; I++)
			{
			Task t = new Task(new InetSocketAddress(InetAddress.getLocalHost(), port),
					peerCount, cl.mpiRunnable, null);
			t.start();
			}
			
		if (cl.startCommand != null)
			{
			System.out.println("Starting ssh connections");
			//Start ssh connections
			int clientNum = 0;
			while (it.hasNext())
				{
				clientNum++;
				pd = it.next();
				String cmd = cl.startCommand+" "+pd.m_address.getHostName()+" "+
						pd.m_command+" mpij.Listener -p"+port+" -n"+peerCount+
						" -r "+InetAddress.getLocalHost().getHostName()+
						" -t"+pd.m_taskCount+" -c "+cl.mpiRunnable;
					
				System.out.println(cmd);
				Process p = Runtime.getRuntime().exec(cmd);
				
				StreamPipe sp = new StreamPipe(p.getInputStream(), System.out, true);
				sp.setPrefix(clientNum+": ");
				sp.start();
				sp = new StreamPipe(p.getErrorStream(), System.out, true);
				sp.setPrefix(clientNum+": ");
				sp.start();
				procs.add(p);
				}
			Thread.sleep(1000);
			}
		//Else connect to listening server and start tasks
		
				
		
		/* for (int I = 1; I < peers.length; I++)
			{
			Socket socket = new Socket(peers[I].getAddress(), peers[I].getPort()+1);
			
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(I);
			dos.writeInt(peers.length);
			
			for (int J = 0; J < peers.length; J++)
				{
				System.out.println("Connecting to peer "+J);
				dos.writeInt(peers[J].getPort());
				
				byte[] addr = peers[J].getAddress().getAddress();
				//System.out.println("Addr length "+addr.length);
				dos.write((byte)addr.length);
				dos.write(addr, 0, addr.length);
				}
				
			dos.flush();
			dos.close();
			socket.close();
			} */
		
		//Launch the task on this thread so we can do cleanup when done
		task.run();
		System.out.println("Launch task complete");
		
		//for (int I = 1; I < procs.length; I++)
		//	procs[I].waitFor();
		//Close ssh connections
		}
	}
