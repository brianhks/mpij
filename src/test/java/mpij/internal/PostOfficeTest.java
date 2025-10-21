package mpij.internal;

import mpij.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;	

public class PostOfficeTest
	{
	private static final int POST_OFFICE_COUNT = 5;
	private static final int BASE_ADDRESS = 9654;
	
	private PostOffice[] m_postOffices;
	private Random m_rand;
	private ByteBuffer m_msgBuf;
	
	private class AsyncGetMessage extends Thread
		{
		private int m_source;
		private int m_dest;
		
		public AsyncGetMessage(int source, int dest)
			{
			m_source = source;
			m_dest = dest;
			}
			
		public void run()
			{
			try
				{
				getMessage(m_source, m_dest);
				}
			catch (Exception e)
				{
				e.printStackTrace();
				}
			}
		}
	
	public static void main(String[] args)
			throws Exception
		{
		PostOfficeTest pot = new PostOfficeTest(POST_OFFICE_COUNT);
		Thread.sleep(1000);
		pot.runTests(50);
		pot.orderedMessageTest(POST_OFFICE_COUNT);
		pot.asyncMessageTest();
		pot.shutdown();
		}
		
	public static PostOffice[] createPostOffices(int num)
			throws Exception
		{
		PostOffice[] pos = new PostOffice[num];
		SocketAddress[] testhosts = new SocketAddress[num];
		
		pos[0] = new PostOffice(null, num);
		InetSocketAddress rootAddr = new InetSocketAddress(InetAddress.getLocalHost(), pos[0].getLocalPort());
		
		for (int I = 1; I < pos.length; I++)
			{
			pos[I] = new PostOffice(rootAddr, num);
			}
			
		return (pos);
		}
		
	public PostOfficeTest(int hostCount)
			throws Exception
		{
		m_postOffices = createPostOffices(hostCount);
			
		m_rand = new Random();
		m_msgBuf = ByteBuffer.allocateDirect(4);
		}
		
	private void sendMessage(int source, int dest)
			throws Exception
		{
		m_msgBuf.clear();
		int msg = m_rand.nextInt();
		m_msgBuf.putInt(msg);
		m_msgBuf.flip();

		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
		list.add(m_msgBuf);
		
		m_postOffices[source].postMessage(PostOffice.CLIENT_MSG, list, dest, 0, (short)0);
		}
		
	private void getMessage(int source, int dest)
			throws Exception
		{
		POMessage pom = m_postOffices[dest].getInBox(PostOffice.CLIENT_MSG)
				.getMessage(source, 0, (short)0);
		}
		
	public void orderedMessageTest(int count)
			throws Exception
		{
		System.out.println("OrderedMessageTest");
		for (int I = 1; I < count; I++)
			{
			sendMessage(I, 0);
			}
			
		for (int I = 1; I < count; I++)
			{
			getMessage(I, 0);			
			}
		}
		
	public void asyncMessageTest()
			throws Exception
		{
		System.out.println("AsyncMessageTest");
		sendMessage(1, 0);
		sendMessage(3, 0);
		Thread t = new AsyncGetMessage(2, 0);
		t.start();
		Thread.sleep(1000);
		sendMessage(2, 0);
		
		t.join();
		}
		
		
	public void runTests(int count)
			throws Exception
		{
		int hosts = m_postOffices.length;
		
		for (int I = 0; I < count; I++)
			{
			int source = m_rand.nextInt(hosts);
			int dest = m_rand.nextInt(hosts);
			while (source == dest)
				dest = m_rand.nextInt(hosts);
				
			sendTestMessage(source, dest);
			}
		}
		
	private void sendTestMessage(int source, int dest)
			throws Exception
		{
		m_msgBuf.clear();
		int msg = m_rand.nextInt();
		m_msgBuf.putInt(msg);
		m_msgBuf.flip();

		ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();
		list.add(m_msgBuf);
		
		m_postOffices[source].postMessage(PostOffice.CLIENT_MSG, list, dest, 0, (short)0);
		
		POMessage pom = m_postOffices[dest].getInBox(PostOffice.CLIENT_MSG)
				.getMessage(source, 0, (short)0);
		ByteBuffer retmsg = pom.getBufferSet().getBuffers().iterator().next();

		int retval = retmsg.getInt();
		if (retval != msg)
			throw new Exception("mismatched messages");
		}
		
	public void shutdown()
			throws Exception
		{
		System.out.println("start shutdown");
		for (int I = 0; I < m_postOffices.length; I++)
			{
			m_postOffices[I].stopPostOffice();
			}
		}
	}
