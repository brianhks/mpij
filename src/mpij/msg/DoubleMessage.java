package mpij.msg;

import mpij.util.BufferPool;
import java.nio.DoubleBuffer;
import java.nio.ByteBuffer;
import java.util.*;

public class DoubleMessage
		implements Message
	{
	private double[] m_data;
	
	//---------------------------------------------------------------------------
	public DoubleMessage(double val)
		{
		m_data = new double[] { val };
		}
	
	//---------------------------------------------------------------------------
	public DoubleMessage(double[] vals)
		{
		m_data = vals;
		}
		
	//---------------------------------------------------------------------------
	public DoubleMessage()
		{
		m_data = null;
		}
		
	//---------------------------------------------------------------------------
	public double getDouble()
		{
		return (m_data[0]);
		}
		
	//---------------------------------------------------------------------------
	public double[] getArray()
		{
		return (m_data);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public BufferPool.BufferSet getMessage(BufferPool pool)
		{
		ByteBuffer bb;
		DoubleBuffer db;
		BufferPool.BufferSet bufferSet = pool.getBufferSet();
		
		int readStart = 0;
		int readStop = 0;
		while (readStop != m_data.length)
			{
			bb = bufferSet.allocateBuffer();
			db = bb.asDoubleBuffer();
			
			readStart = readStop;
			readStop = readStart + db.remaining();
			if (readStop > m_data.length)
				readStop = m_data.length;
			
			db.put(m_data, readStart, readStop);
			bb.limit(db.position() * (Double.SIZE / Byte.SIZE));
			}
		
		return (bufferSet);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public void setMessage(BufferPool.BufferSet bufSet, int size)
		{
		Collection<ByteBuffer> buf = bufSet.getBuffers();
		DoubleBuffer db;
		if (m_data == null)
			m_data = new double[size / (Double.SIZE / Byte.SIZE)];
			
		int fillStart = 0;
		int fillStop = 0;
		Iterator<ByteBuffer> it = buf.iterator();
		while (it.hasNext())
			{
			db = it.next().asDoubleBuffer();
			fillStart = fillStop;
			fillStop = fillStart + db.remaining();
			db.get(m_data, fillStart, fillStop);
			}		
		}
	}
