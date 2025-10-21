package mpij.internal;

import mpij.msg.*;
import mpij.*;
import java.io.*;

public class RemoteClassServer extends Thread
	{
	private InternalCommunicator m_comm;
	
	public RemoteClassServer(PostOffice po)
		{
		m_comm = new InternalCommunicator(PostOffice.CLASS_LOADER_MSG, po, (short)0);
		this.start();
		}
		
	public void run()
		{
		try
			{
			for (;;)
				{
				ObjectMessage<String> msg = new ObjectMessage<String>();
				Status stat = m_comm.recv(msg, Communicator.ANY_SOURCE, 
						Communicator.ANY_TAG);
						
				String name = msg.getObject();
				//System.out.println("Recieved load message for "+name);	
				
				ByteMessage retMsg;
				InputStream classIS = getClass().getClassLoader().getResourceAsStream(name.replace('.', '/')+".class");
		
				if (classIS == null)
					{
					retMsg = new ByteMessage(new byte[0]);
					}
				else
					{
					int data;
					ByteArrayOutputStream classOS = new ByteArrayOutputStream();
					
					try
						{
						while ((data = classIS.read()) != -1)
							classOS.write(data);
						classOS.flush();
						}
					catch(IOException ioe)
						{
						ioe.printStackTrace();
						}
						
					retMsg = new ByteMessage(classOS.toByteArray());
					}
					
				//System.out.println("Sending to "+stat.getSource()+" tag "+stat.getTag());
				m_comm.send(retMsg, stat.getSource(), stat.getTag());
				}
			}
		catch (MPIException mpie)
			{
			//mpie.printStackTrace();
			}
		}
	}
