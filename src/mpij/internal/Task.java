package mpij.internal;

import mpij.*;
import java.net.*;
import java.io.IOException;
import mpij.msg.*;
import java.io.*;

public class Task extends Thread
	{
	private PostOffice m_postOffice;
	private Communicator m_commWorld;
	private InBox m_inBox;
	private String m_taskClass;
	private Thread m_listenerThread;
	
	public Task(SocketAddress rootHost, int peerCount, String task, Thread listenerThread)
			throws IOException
		{
		m_postOffice = new PostOffice(rootHost, peerCount);
		m_commWorld = new CommunicatorImpl(m_postOffice, (short)0);
		m_inBox = m_postOffice.getInBox(PostOffice.TASK_MSG);
		m_taskClass = task;
		m_listenerThread = listenerThread;
		}
		
	public PostOffice getPostOffice()
		{
		return (m_postOffice);
		}
		
	public void run()
		{
		//System.out.println("Waiting for connect");
		m_postOffice.waitForConnect();
		try
			{
			if (m_postOffice.getLocalId() != 0)
				{
				//System.out.println("Creating remote class loader");
				Thread.currentThread().setContextClassLoader(new RemoteClassLoader(
						ClassLoader.getSystemClassLoader(), m_postOffice));
						
				ObjectMessage<String> task = new ObjectMessage<String>();
				//System.out.println("Calling broadcast");
				m_commWorld.bcast(task, 0);
				
				m_taskClass = task.getObject();
				}
			else
				{
				//System.out.println("Starting class server");
				new RemoteClassServer(m_postOffice);
				//System.out.println("Calling broadcast");
				m_commWorld.bcast(new ObjectMessage<String>(m_taskClass), 0);
				}
				
			//System.out.println("Loading class "+m_taskClass);
			Class taskClass = Thread.currentThread().getContextClassLoader().loadClass(m_taskClass);
			MPIRunnable runnable = (MPIRunnable)taskClass.newInstance();
			
			//System.out.println("Calling run");
			runnable.run(m_commWorld);
			//System.out.println("Task complete");
			Thread.sleep(1000);
			}
		catch (Throwable e)
			{
			try
				{
				e.printStackTrace();
				PrintWriter pw = new PrintWriter(new FileWriter("mpij.log"));
				e.printStackTrace(pw);
				pw.close();
				}
			catch (Exception sube)
				{
				sube.printStackTrace();
				}
			//Shut it all down if exception is thrown
			}
			
		m_postOffice.stopPostOffice();
		if (m_listenerThread != null)
			m_listenerThread.interrupt();
		}
	}
