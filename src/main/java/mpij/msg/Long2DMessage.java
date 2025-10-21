package mpij.msg;

import mpij.util.BufferPool;
import java.nio.LongBuffer;
import java.nio.ByteBuffer;
import java.util.*;

public class Long2DMessage
		implements Message
	{
	private long[][] m_data;
	
	//---------------------------------------------------------------------------
	public Long2DMessage(long[][] vals)
		{
		m_data = vals;
		}
		
	//---------------------------------------------------------------------------
	public Long2DMessage()
		{
		m_data = null;
		}
		
	//---------------------------------------------------------------------------
	public long[][] get2DArray()
		{
		return (m_data);
		}
		
	//---------------------------------------------------------------------------
	private ByteBuffer packBuffer(ByteBuffer curBuf, BufferPool.BufferSet bufSet,
			long[] data)
		{
		LongBuffer lb = curBuf.asLongBuffer();
		int readStart = 0;
		int readLength = 0;
		
		while ((readStart+readLength) != data.length)
			{
			readStart += readLength;
			readLength = lb.remaining();
			if ((readStart + readLength) > data.length)
				readLength = (data.length - readStart);
				
			lb.put(data, readStart, readLength);
			curBuf.limit(curBuf.position() + (lb.position() * (Long.SIZE / Byte.SIZE)));
			
			if ((readStart+readLength) != data.length)
				{
				curBuf = bufSet.allocateBuffer();
				lb = curBuf.asLongBuffer();
				}
			}
			
		return (curBuf);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public BufferPool.BufferSet getMessage(BufferPool pool)
		{
		ByteBuffer bb;
		BufferPool.BufferSet bufferSet = pool.getBufferSet();
		
		//Write buffer dimensions
		bb = bufferSet.allocateBuffer();
		bb.putInt(m_data.length);
		bb.putInt(m_data[0].length);
		
		for (int I = 0; I < m_data.length; I++)
			{
			long[] data = m_data[I];
			LongBuffer lb = bb.asLongBuffer();
			int readStart = 0;
			int readLength = 0;
			
			while ((readStart+readLength) != data.length)
				{
				readStart += readLength;
				readLength = lb.remaining();
				if ((readStart + readLength) > data.length)
					readLength = (data.length - readStart);
					
				lb.put(data, readStart, readLength);
				bb.limit(bb.position() + (lb.position() * (Long.SIZE / Byte.SIZE)));
				
				if ((readStart+readLength) != data.length)
					{
					bb = bufferSet.allocateBuffer();
					lb = bb.asLongBuffer();
					}
				}
			}
		
		return (bufferSet);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public void setMessage(BufferPool.BufferSet bufSet, int size)
			throws MessageException
		{
		Collection<ByteBuffer> buf = bufSet.getBuffers();
		
		Iterator<ByteBuffer> it = buf.iterator();
		ByteBuffer bb = it.next();
		int rows = bb.getInt();
		int cols = bb.getInt();
		
		// TODO: check rows * cols + 8 == size If not throw exception
		
		if (m_data == null)
			m_data = new long[rows][cols];
		else
			{
			if ((rows != m_data.length) || (cols != m_data[0].length))
				throw new MessageException("2D arrays do not match");
			}
		
		
		LongBuffer lb = bb.asLongBuffer();
		
		for (int I = 0; I < rows; I++)
			{
			long[] data = m_data[I];
			int fillStart = 0;
			int fillLength = 0;
			while ((fillStart+fillLength) != data.length)
				{
				if (lb.remaining() == 0)
					{
					bb = it.next();
					lb = bb.asLongBuffer();
					}
				
				fillStart += fillLength;
				fillLength = lb.remaining();
				lb.get(data, fillStart, fillLength);
				}
			}
		}
	}
