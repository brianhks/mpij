package mpij.msg;

import mpij.util.BufferPool;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.util.*;

public class IntMessage
		implements Message
	{
	private int[] m_data;
	
	//---------------------------------------------------------------------------
	public IntMessage(int val)
		{
		m_data = new int[] { val };
		}
	
	//---------------------------------------------------------------------------
	public IntMessage(int[] vals)
		{
		m_data = vals;
		}
		
	//---------------------------------------------------------------------------
	public IntMessage()
		{
		m_data = null;
		}
		
	//---------------------------------------------------------------------------
	public int getInt()
		{
		return (m_data[0]);
		}
		
	//---------------------------------------------------------------------------
	public int[] getArray()
		{
		return (m_data);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public BufferPool.BufferSet getMessage(BufferPool pool)
		{
		ByteBuffer bb;
		IntBuffer ib;
		BufferPool.BufferSet bufferSet = pool.getBufferSet();
		
		int readStart = 0;
		int readLength = 0;
		while ((readStart+readLength) != m_data.length)
			{
			bb = bufferSet.allocateBuffer();
			ib = bb.asIntBuffer();
			
			readStart += readLength;
			readLength = ib.remaining();
			if ((readStart + readLength) > m_data.length)
				readLength = (m_data.length - readStart);
			
			ib.put(m_data, readStart, readLength);
			bb.limit(ib.position() * (Integer.SIZE / Byte.SIZE));
			}
		
		return (bufferSet);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public void setMessage(BufferPool.BufferSet bufSet, int size)
		{
		Collection<ByteBuffer> buf = bufSet.getBuffers();
		IntBuffer ib;
		if (m_data == null)
			m_data = new int[size / (Integer.SIZE / Byte.SIZE)];
			
		int fillStart = 0;
		int fillLength = 0;
		Iterator<ByteBuffer> it = buf.iterator();
		while (it.hasNext())
			{
			ib = it.next().asIntBuffer();
			fillStart += fillLength;
			fillLength = ib.remaining();
			ib.get(m_data, fillStart, fillLength);
			}		
		}
	}
