
package mpij.internal;

import mpij.util.*;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.ListIterator;
import mpij.MPIException;

public class InBox
		implements MessageHandler
	{
	public static final int ANY_TAG = -1;
	public static final int ANY_SOURCE = -1;
	
	private boolean m_closing;
	
	private class QueuedMessage
		{
		public Header m_header;
		public int m_source;
		public BufferPool.BufferSet m_message;
		
		public QueuedMessage(Header header, int src, BufferPool.BufferSet msg)
			{
			m_header = header;
			m_source = src;
			m_message = msg;
			}
		}
	
	private LinkedList<QueuedMessage> m_messageQueue;
	
	//---------------------------------------------------------------------------
	private ListIterator<QueuedMessage> findMessage(int src, int tag, short context)
		{
		ListIterator<QueuedMessage> ret = null;
		
		ListIterator<QueuedMessage> lit;
		lit = m_messageQueue.listIterator();
		while (lit.hasNext())
			{
			QueuedMessage qm = lit.next();
			Header header = qm.m_header;
			
			if (((src == ANY_SOURCE) || (src == qm.m_source)) &&
					((tag == ANY_TAG) || (tag == header.getTag())) &&
					(context == header.getContext()))
				{
				ret = lit;
				break;
				}
			}
			
		return (ret);
		}
	
	//---------------------------------------------------------------------------
	public InBox()
		{
		m_closing = false;
		m_messageQueue = new LinkedList<QueuedMessage>();
		}
		
	//---------------------------------------------------------------------------
	public void newMessage(Header head, int src, BufferPool.BufferSet bufSet)
		{
		//System.out.println("New message from "+src);
		QueuedMessage qm = new QueuedMessage(head, src, bufSet);
		synchronized(m_messageQueue)
			{
			m_messageQueue.add(qm);
			m_messageQueue.notifyAll();
			}
		}
		
	//---------------------------------------------------------------------------
	public boolean checkMessage(int src, int tag, short context)
		{
		boolean ret = false;
		ListIterator<QueuedMessage> lit;
		
		synchronized(m_messageQueue)
			{
			lit = findMessage(src, tag, context);
			ret = (lit != null);
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	public POMessage getMessage(int src, int tag, short context)
			throws MPIException
		{
		POMessage ret = null;
		
		ListIterator<QueuedMessage> lit;
		
		synchronized(m_messageQueue)
			{
			while (ret == null)
				{
				lit = findMessage(src, tag, context);
				if (lit != null)
					{
					QueuedMessage qm = lit.previous();
					lit.remove();
					Header header = qm.m_header;
					ret = new POMessage(qm.m_message, header.getLength(), qm.m_source, header.getTag(),
							header.getContext());
					}
				else
					{
					try
						{
						m_messageQueue.wait();
						}
					catch (InterruptedException ie) {}
					}
				if (m_closing)
					throw new MPIException("InBox closed");
				}
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	public void close()
		{
		m_closing = true;
		synchronized(m_messageQueue)
			{
			
			m_messageQueue.notifyAll();
			}
		}
	}
