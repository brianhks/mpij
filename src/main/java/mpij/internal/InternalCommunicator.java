package mpij.internal;

import mpij.util.BufferPool;
import mpij.msg.*;
import mpij.*;
import java.io.IOException;
import java.nio.ByteBuffer;

public class InternalCommunicator
	{
	protected static final int BUFFER_SIZE = 1024;
	
	protected PostOffice m_postOffice;
	protected InBox m_inBox;
	protected BufferPool m_bufferPool;
	protected byte m_msgType;
	
	protected short m_context;  //Context used when sending/recv messages to other tasks in this group
	
	protected InternalCommunicator(byte msgType, PostOffice po, short context)
		{
		m_postOffice = po;
		m_inBox = m_postOffice.getInBox(msgType);
		m_context = context;
		m_bufferPool = po.getBufferPool();
		m_msgType = msgType;
		}
	
	//---------------------------------------------------------------------------
	public void send(Message msg, int dest, int tag)
			throws MPIException
		{
		try
			{
			BufferPool.BufferSet bufSet = msg.getMessage(m_bufferPool);
		
			m_postOffice.postMessage(m_msgType, bufSet.getBuffers(), dest, tag, m_context);
			}
		catch (MessageException me)
			{
			}
		catch (IOException ioe)
			{
			}
		}
		
	//---------------------------------------------------------------------------
	public Status recv(Message msg, int source, int tag)
			throws MPIException
		{
		Status ret = null;
		try
			{
			POMessage pom = m_inBox.getMessage(source, tag, m_context);
			
			BufferPool.BufferSet bufSet = pom.getBufferSet();
			
			msg.setMessage(bufSet, pom.getSize());
			
			ret = new Status(pom.getSource(), pom.getTag(), pom.getSize());
			}
		catch (MessageException me)
			{
			}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------
	}
