package mpij.internal;

import mpij.*;

import mpij.util.*;
import mpij.internal.util.*;

import java.nio.*;
import java.nio.channels.*;
import java.net.*;
import java.io.IOException;

import java.util.*;


public class PostOffice
	{
	private static final int PORT_CUTOFF = Launcher.DEFAULT_PORT + 100;

	public static final byte CLIENT_MSG = 0x00;
	public static final byte TASK_MSG = 0x01;
	public static final byte LAUNCHER_MSG = 0x02;
	public static final byte CLASS_LOADER_MSG = 0x03;

	public static final byte MAX_MSG = 0x04; //must be one more then highest message number

	private static final int BUFFER_SIZE = 1024;

	private int m_localId;
	private int m_peerCount;
	private SocketChannel[] m_peers;
	private Acceptor m_acceptor;
	private MessageAcceptor m_msgAcceptor;
	private Selector m_selector;
	private BufferPool m_bufferPool;
	private InBox[] m_inBox;
	private Queue<Pair<Integer, SocketChannel> > m_registerQueue;
	private int m_connectedPeers;
	private BufferPool m_headerBufferPool;
	private InetSocketAddress[] m_hosts;

//==============================================================================
/**
	This class runs and accepts connections from other PostOffices.  The resulting
	Channels are added to the peers array and added to this post offices selector
*/
	private class Acceptor
			implements Runnable
		{
		private int m_localPort;
		private ServerSocketChannel m_server;
		private boolean m_isRoot;

		public Acceptor(boolean isRoot)
				throws IOException
			{
			m_isRoot = isRoot;
			m_server = ServerSocketChannel.open();
			boolean connected = false;
			m_localPort = Launcher.DEFAULT_PORT;
			while (!connected && ((m_localPort < PORT_CUTOFF)))
				{
				try
					{
					m_server.socket().bind(new InetSocketAddress(m_localPort));
					connected = true;
					//System.out.println("Server socket open and bound");
					}
				catch (IOException ioe)
					{
					m_localPort ++;
					}
				}

			if (m_localPort >= PORT_CUTOFF)
				throw new IOException();

			Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
			}

		//------------------------------------------------------------------------
		public int getPort()
			{
			return (m_localPort);
			}

		//------------------------------------------------------------------------
		private void runRoot()
			{
			SocketChannel channel;
			ByteBuffer buf = ByteBuffer.allocateDirect(256);

			try
				{
				while ((channel = m_server.accept()) != null)
					{
					int peerId = m_connectedPeers +1;
					sendInt(channel, buf, peerId);
					sendInt(channel, buf, m_peerCount);
					int port = getInt(channel, buf);

					m_hosts[peerId] = new InetSocketAddress(
							channel.socket().getInetAddress(), port);

					for (int I = 1; I < peerId; I++)
						{
						sendInt(channel, buf, m_hosts[I].getPort());
						byte[] addr = m_hosts[I].getAddress().getAddress();
						sendInt(channel, buf, addr.length);
						sendBytes(channel, buf, addr);
						}

					registerPeer(peerId, channel);
					}
				}
			catch (IOException ioe)
				{
				}
			}

		//------------------------------------------------------------------------
		private void registerPeer(int peerId, SocketChannel channel)
			{
			m_peers[peerId] = channel;
			synchronized(m_registerQueue)
				{
				m_registerQueue.offer(new Pair<Integer, SocketChannel>(
						new Integer(peerId), channel));
				}
			m_selector.wakeup();
			incrementPeerCount();
			}

		//------------------------------------------------------------------------
		public void run()
			{
			if (m_isRoot)
				runRoot();
			else
				{
				ByteBuffer buf = ByteBuffer.allocateDirect(4);
				SocketChannel channel;

				//Accept connections and add them to the peers list
				try
					{
					while ((channel = m_server.accept()) != null)
						{
						channel.read(buf);
						buf.flip();

						int peerId = buf.getInt();
						buf.clear();

						registerPeer(peerId, channel);
						}
					}
				catch (IOException ioe)
					{
					//maybe log error
					//ioe.printStackTrace();
					}
				//System.out.println("Acceptor died");
				}
			}

		//------------------------------------------------------------------------
		public void close()
			{
			try
				{
				m_server.close();
				}
			catch (IOException ioe)
				{
				}
			}
		}

//==============================================================================
/**
	This class selects on the selector and reads messages from the channels.
	Messages are then passed to the InBox
*/
	private class MessageAcceptor extends Thread
		{
		public MessageAcceptor()
			{
			setDaemon(true);
			this.start();
			}

		public void run()
			{
			ByteBuffer headbuf = ByteBuffer.allocateDirect(Header.SIZE);

			try
				{
				for (;;)
					{
					int msgCount = m_selector.select();

					//First see if any new channels are waiting to be registered
					synchronized(m_registerQueue)
						{
						Pair<Integer, SocketChannel> p;
						while ((p = m_registerQueue.poll()) != null)
							{
							SocketChannel channel = p.getSecond();
							channel.configureBlocking(false);
							channel.register(m_selector, SelectionKey.OP_READ, p.getFirst());
							}
						}

					if (msgCount != 0)
						{
						Iterator<SelectionKey> it = m_selector.selectedKeys().iterator();
						while (it.hasNext())
							{
							SelectionKey selKey = it.next();
							it.remove();
							if (selKey.isReadable())
								{
								SocketChannel channel = (SocketChannel)selKey.channel();
								Integer source = (Integer)selKey.attachment();
								//System.out.println(m_localId+" Message from "+source);

								try
									{
									readFromChannel(channel, new ByteBuffer[] {headbuf}, Header.SIZE);
									headbuf.flip();
									Header header = new Header(headbuf);
									//System.out.println(header);
									headbuf.clear();

									//System.out.println("Message received size: "+header.getLength());
									int msgLength = header.getLength();
									BufferPool.BufferSet bufSet = m_bufferPool.getBufferSet();
									ByteBuffer[] msgBuf = bufSet.allocateBuffers(msgLength);
									readFromChannel(channel, msgBuf, msgLength);
									for (int I = 0; I < msgBuf.length; I++)
										msgBuf[I].flip();

									//System.out.println("new message from "+source+" type "+header.getType());
									m_inBox[header.getType()].newMessage(header, source.intValue(), bufSet);
									}
								catch (IOException ioe)
									{
									channel.close();
									}
								}
							}
						}
					}
				}
			catch (IOException ioe)
				{
				ioe.printStackTrace();
				}
			catch (ClosedSelectorException cse)
				{
				}

			//System.out.println("MessageAcceptor Died");
			}
		}
//==============================================================================
//==============================================================================
	public PostOffice(SocketAddress rootHost, int peerCount)
			throws IOException
		{
		m_peerCount = peerCount;
		m_connectedPeers = 0;
		m_inBox = new InBox[MAX_MSG];
		for (int I = 0; I < (int)MAX_MSG; I++)
			{
			m_inBox[I] = new InBox();
			}

		m_registerQueue = new LinkedList<Pair<Integer, SocketChannel> >();

		m_selector = Selector.open();
		m_acceptor = new Acceptor(rootHost == null);  //Start server
		//Get port after it opens

		int myPort = m_acceptor.getPort();

		m_bufferPool = new BufferPool(BUFFER_SIZE);
		m_headerBufferPool = new BufferPool(Header.SIZE);

		ByteBuffer buf = ByteBuffer.allocateDirect(4);

		if (rootHost != null)
			{
			SocketChannel root = SocketChannel.open();
			root.configureBlocking(true);
			root.connect(rootHost);
			m_localId = getInt(root, buf);
			m_peerCount = getInt(root, buf);
			sendInt(root, buf, myPort);

			m_hosts = getPeerList(root, m_localId);

			root.configureBlocking(false);
			root.register(m_selector, SelectionKey.OP_READ, new Integer(0));

			m_peers = new SocketChannel[m_peerCount];
			m_peers[0] = root;

			incrementPeerCount();
			}
		else
			{
			m_peers = new SocketChannel[m_peerCount];
			m_hosts = new InetSocketAddress[m_peerCount];
			m_localId = 0;
			}

		buf.clear();
		buf.putInt(m_localId);
		for (int I = 1; I < m_localId; I++)
			{
			// IDEA: if multiple mpij on a single host we need to check if
			// connection to host is already established
			//System.out.println(locId+" connecting to "+I);
			SocketChannel channel = SocketChannel.open();
			channel.configureBlocking(true);
			channel.connect(m_hosts[I]);
			m_peers[I] = channel;
			buf.flip();
			channel.write(buf);
			channel.configureBlocking(false);
			channel.register(m_selector, SelectionKey.OP_READ, new Integer(I));
			incrementPeerCount();
			}

		m_msgAcceptor = new MessageAcceptor();
		}

//------------------------------------------------------------------------------
	private InetSocketAddress[] getPeerList(SocketChannel root, int localId)
			throws UnknownHostException, IOException
		{
		InetSocketAddress[] hosts = new InetSocketAddress[localId];
		ByteBuffer buf = ByteBuffer.allocateDirect(256);

		for (int I = 1; I < localId; I++)
			{
			int port = getInt(root, buf);
			int addrSz = getInt(root, buf);
			byte[] addr = new byte[addrSz];
			getBytes(root, buf, addr);

			hosts[I] = new InetSocketAddress(InetAddress.getByAddress(addr), port);
			}

		return (hosts);
		}

//------------------------------------------------------------------------------
	private synchronized void incrementPeerCount()
		{
		m_connectedPeers ++;
		if (m_connectedPeers == m_peers.length -1)
			notifyAll();
		}

	private void sendInt(WritableByteChannel channel, ByteBuffer buf, int val)
			throws IOException
		{
		buf.clear();
		buf.putInt(val);
		buf.flip();
		channel.write(buf);
		}

	private int getInt(ReadableByteChannel channel, ByteBuffer buf)
			throws IOException
		{
		buf.clear();
		buf.limit(4);
		channel.read(buf);
		buf.flip();
		int val = buf.getInt();
		return (val);
		}

	private void sendBytes(WritableByteChannel channel, ByteBuffer buf, byte[] data)
			throws IOException
		{
		buf.clear();
		buf.put(data);
		buf.flip();
		channel.write(buf);
		}

	private void getBytes(ReadableByteChannel channel, ByteBuffer buf, byte[] data)
			throws IOException
		{
		buf.clear();
		buf.limit(data.length);
		channel.read(buf);
		buf.flip();
		buf.get(data);
		}

//------------------------------------------------------------------------------
	private void readFromChannel(ScatteringByteChannel channel, ByteBuffer[] buf, int bytesToRead)
			throws IOException
		{
		int bytesRead = 0;
		int offset = 0;

		while (bytesRead < bytesToRead)
			{
			while (buf[offset].position() == buf[offset].capacity())
				offset++;
			bytesRead += channel.read(buf, offset, (buf.length - offset));
			if (bytesRead < 0)
				throw (new IOException("Channel is closed"));
			}
		}

//------------------------------------------------------------------------------
	private void writeToChannel(GatheringByteChannel channel, ByteBuffer[] buf, int bytesToWrite)
			throws IOException
		{
		int bytesWritten = 0;
		int offset = 0;

		while (bytesWritten < bytesToWrite)
			{
			while (buf[offset].position() == buf[offset].capacity())
				offset++;
			bytesWritten += channel.write(buf, offset, (buf.length - offset));
			}
		}

//------------------------------------------------------------------------------
	public void postMessage(byte type, Collection<ByteBuffer> bufs, int dest, int tag, short context)
			throws IOException
		{
		// TODO: Handle if dest is this instance
		ByteBuffer[] sendBuffer = new ByteBuffer[bufs.size() +1];
		//sendBuffer[0]  = m_headerBufferPool.getBuffer();
		sendBuffer[0] = ByteBuffer.allocateDirect(Header.SIZE);
		int msgSize = 0;
		Iterator<ByteBuffer> it = bufs.iterator();
		int I = 1;
		while (it.hasNext())
			{
			ByteBuffer buf = it.next();
			sendBuffer[I] = buf;
			msgSize += buf.remaining();
			I++;
			}

		Header header = new Header(type, msgSize, tag, context);
		//System.out.println(header);
		header.writeHeader(sendBuffer[0]);
		sendBuffer[0].flip();

		//System.out.println(m_localId+" Sending to "+dest);
		synchronized(m_peers[dest])
			{
			writeToChannel(m_peers[dest], sendBuffer, (msgSize + Header.SIZE));
			}

		}

//------------------------------------------------------------------------------
	public InBox getInBox(byte type)
		{
		return (m_inBox[(int)type]);
		}

//------------------------------------------------------------------------------
	public void stopPostOffice()
		{
		m_acceptor.close();

		try
			{
			m_selector.close();
			}
		catch (IOException ioe) {}

		for (int I = 0; I < m_peers.length; I++)
			{
			try
				{
				if (m_peers[I] != null)
					m_peers[I].close();
				}
			catch (IOException ioe) {}
			}

		//Close inboxes
		for (int I = 0; I < (int)MAX_MSG; I++)
			{
			m_inBox[I].close();
			}

		m_bufferPool.printStats();
		}

//------------------------------------------------------------------------------
	public int getPeerCount()
		{
		return (m_peerCount);
		}

//------------------------------------------------------------------------------
	public int getLocalId()
		{
		return (m_localId);
		}

//------------------------------------------------------------------------------
	public int getLocalPort()
		{
		return (m_acceptor.getPort());
		}

//------------------------------------------------------------------------------
	public void printPeers()
		{
		for (int I = 0; I < m_peers.length; I++)
			{
			System.out.println("Peer "+I+": "+m_peers[I]);
			}
		}

	//---------------------------------------------------------------------------
	public void waitForConnect()
		{
		synchronized(this)
			{
			while (m_connectedPeers != m_peerCount -1)
				{
				try
					{
					wait();
					}
				catch (InterruptedException ie)
					{
					}
				}
			}
		}

	//---------------------------------------------------------------------------
	protected BufferPool getBufferPool()
		{
		return (m_bufferPool);
		}

	}
