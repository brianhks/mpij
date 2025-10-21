package mpij.msg;

import mpij.util.BufferPool;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.util.*;

public class LongMessage
		implements Message
	{
	private long[] m_data;
	
	//---------------------------------------------------------------------------
	public LongMessage(long val)
		{
		m_data = new long[] { val };
		}
	
	//---------------------------------------------------------------------------
	public LongMessage(long[] vals)
		{
		m_data = vals;
		}
		
	//---------------------------------------------------------------------------
	public LongMessage()
		{
		m_data = null;
		}
		
	//---------------------------------------------------------------------------
	public long getLong()
		{
		return (m_data[0]);
		}
		
	//---------------------------------------------------------------------------
	public long[] getArray()
		{
		return (m_data);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public BufferPool.BufferSet getMessage(BufferPool pool)
		{
		ByteBuffer bb;
		LongBuffer lb;
		BufferPool.BufferSet bufferSet = pool.getBufferSet();
		
		int readStart = 0;
		int readLength = 0;
		while ((readStart+readLength) != m_data.length)
			{
			bb = bufferSet.allocateBuffer();
			lb = bb.asLongBuffer();
			
			readStart += readLength;
			readLength = lb.remaining();
			if ((readStart + readLength) > m_data.length)
				readLength = (m_data.length - readStart);
			
			lb.put(m_data, readStart, readLength);
			bb.limit(lb.position() * (Long.SIZE / Byte.SIZE));
			}
		
		return (bufferSet);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public void setMessage(BufferPool.BufferSet bufSet, int size)
		{
		Collection<ByteBuffer> buf = bufSet.getBuffers();
		LongBuffer lb;
		if (m_data == null)
			m_data = new long[size / (Long.SIZE / Byte.SIZE)];
			
		int fillStart = 0;
		int fillLength = 0;
		Iterator<ByteBuffer> it = buf.iterator();
		while (it.hasNext())
			{
			lb = it.next().asLongBuffer();
			fillStart += fillLength;
			fillLength = lb.remaining();
			lb.get(m_data, fillStart, fillLength);
			}		
		}
	}
