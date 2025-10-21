package mpij.msg;

import mpij.util.BufferPool;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.util.*;

public class ByteMessage
		implements Message
	{
	private byte[] m_data;
	
	//---------------------------------------------------------------------------
	public ByteMessage(byte val)
		{
		m_data = new byte[] { val };
		}
	
	//---------------------------------------------------------------------------
	public ByteMessage(byte[] vals)
		{
		m_data = vals;
		}
		
	//---------------------------------------------------------------------------
	public ByteMessage()
		{
		m_data = null;
		}
		
	//---------------------------------------------------------------------------
	public byte getByte()
		{
		return (m_data[0]);
		}
		
	//---------------------------------------------------------------------------
	public byte[] getArray()
		{
		return (m_data);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public BufferPool.BufferSet getMessage(BufferPool pool)
		{
		ByteBuffer bb;
		BufferPool.BufferSet bufferSet = pool.getBufferSet();
		
		int readStart = 0;
		int readLength = 0;
		while ((readStart+readLength) != m_data.length)
			{
			bb = bufferSet.allocateBuffer();
			
			readStart += readLength;
			readLength = bb.remaining();
			if ((readStart + readLength) > m_data.length)
				readLength = (m_data.length - readStart);
			
			bb.put(m_data, readStart, readLength);
			bb.flip();
			}
		
		return (bufferSet);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public void setMessage(BufferPool.BufferSet bufSet, int size)
		{
		Collection<ByteBuffer> buf = bufSet.getBuffers();
		if (m_data == null)
			m_data = new byte[size];
			
		int fillStart = 0;
		int fillLength = 0;
		Iterator<ByteBuffer> it = buf.iterator();
		while (it.hasNext())
			{
			ByteBuffer b = it.next();
			fillStart += fillLength;
			fillLength = b.remaining();
			b.get(m_data, fillStart, fillLength);
			}		
		}
	}
