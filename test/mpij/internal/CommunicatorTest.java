package mpij.internal;

import mpij.*;
import mpij.msg.ObjectMessage;
import mpij.msg.IntMessage;
import mpij.msg.op.IntSum;
import mpij.MPIException;

public class CommunicatorTest
	{
	private static class ReduceTest extends Thread
		{
		private Communicator m_comm;
		private int m_root;
		
		public ReduceTest(Communicator comm, int root)
			{
			m_comm = comm;
			m_root = root;
			}
			
		public void run()
			{
			try
				{
				IntMessage msg = new IntMessage(1);
				
				m_comm.reduce(msg, m_root, new IntSum());
				System.out.println(m_comm.getRank()+" "+msg.getInt());
				}
			catch (MPIException mpie)
				{
				mpie.printStackTrace();
				}
			}
		}
		
	//===========================================================================
	private static class BCastTest extends Thread
		{
		private Communicator m_comm;
		private int m_root;
		private int m_value;
		
		public BCastTest(Communicator comm, int root, int value)
			{
			m_comm = comm;
			m_root = root;
			m_value = value;
			}
			
		public void run()
			{
			try
				{
				IntMessage msg = new IntMessage(m_value);
				
				m_comm.bcast(msg, m_root);
				System.out.println(m_comm.getRank()+" "+msg.getInt());
				}
			catch (MPIException mpie)
				{
				mpie.printStackTrace();
				}
			}
		}
		
	//===========================================================================
	private static class AllReduceTest extends Thread
		{
		private Communicator m_comm;
		private int m_value;
		
		public AllReduceTest(Communicator comm, int value)
			{
			m_comm = comm;
			m_value = value;
			}
			
		public void run()
			{
			try
				{
				IntMessage msg = new IntMessage(m_value);
				
				m_comm.allReduce(msg, new IntSum());
				//m_comm.reduce(msg, 0, new IntSum());
				System.out.println(m_comm.getRank()+" "+msg.getInt());
				}
			catch (MPIException mpie)
				{
				mpie.printStackTrace();
				}
			}
		}
	
	//===========================================================================
	public static void main(String[] args)
			throws Exception
		{
		int clientCount = 10;
		String message = "This is my test message";
		PostOffice[] pos = PostOfficeTest.createPostOffices(clientCount);
		Communicator[] coms = new Communicator[clientCount];
		
		for (int I = 0; I < pos.length; I++)
			{
			coms[I] = new CommunicatorImpl(pos[I], (short)0);
			}
			
		coms[0].send(new ObjectMessage<String>(message), 1, 0);
			
		ObjectMessage<String> ret = new ObjectMessage<String>();
		coms[1].recv(ret, 0, 0);
		
		System.out.println(ret.getObject());
		
		System.out.println("Reduce test");
		for (int I = 0; I < coms.length; I++)
			{
			ReduceTest rt = new ReduceTest(coms[I], 0);
			rt.start();
			}
			
		Thread.sleep(1000);
		
		System.out.println("BCastTest");
		for (int I = 0; I < coms.length; I++)
			{
			// TODO: need to catch error when root is outside group size
			int root = 2;
			if (I == root)
				{
				BCastTest bt = new BCastTest(coms[I], root, 42);
				bt.start();
				}
			else
				{
				BCastTest bt = new BCastTest(coms[I], root, 0);
				bt.start();
				}
			}
			
		Thread.sleep(1000);
		
		System.out.println("AllReduce test");
		for (int I = 0; I < coms.length; I++)
			{
			AllReduceTest art = new AllReduceTest(coms[I], 1);
			art.start();
			}
			
		Thread.sleep(1000);
		
		for (int I = 0; I < pos.length; I++)
			{
			pos[I].stopPostOffice();
			}
		}
	}
