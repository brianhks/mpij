package mpij.internal;

import java.net.*;


public class HotplateTest
	{
	public static void main(String[] args)
			throws Exception
		{
		int clientCount = 4;
		
		Task t = new Task(null, clientCount, "Hotplate", null);
		int port = t.getPostOffice().getLocalPort();
		InetSocketAddress root = new InetSocketAddress(InetAddress.getLocalHost(), port);
		t.start();
		for (int I = 1; I < clientCount; I++)
			{
			t = new Task(root, clientCount, "Hotplate", null);
			t.start();
			}
		}
	}
