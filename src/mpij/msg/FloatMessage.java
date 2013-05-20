package mpij.msg;

import mpij.util.BufferPool;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.util.*;

public class FloatMessage
		implements Message
	{
	float[] m_data;
	
	//---------------------------------------------------------------------------
	public FloatMessage(float val)
		{
		m_data = new float[] { val };
		}
	
	//---------------------------------------------------------------------------
	public FloatMessage(float[] vals)
		{
		m_data = vals;
		}
		
	//---------------------------------------------------------------------------
	public FloatMessage()
		{
		m_data = null;
		}
		
	//---------------------------------------------------------------------------
	public float getFloat()
		{
		return (m_data[0]);
		}
		
	//---------------------------------------------------------------------------
	public float[] getArray()
		{
		return (m_data);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public BufferPool.BufferSet getMessage(BufferPool pool)
		{
		ByteBuffer bb;
		FloatBuffer db;
		BufferPool.BufferSet bufferSet = pool.getBufferSet();
		
		int readStart = 0;
		int readLength = 0;
		while ((readStart+readLength) != m_data.length)
			{
			bb = bufferSet.allocateBuffer();
			db = bb.asFloatBuffer();
			
			readStart += readLength;
			readLength = db.remaining();
			if ((readStart + readLength) > m_data.length)
				readLength = (m_data.length - readStart);
			
			db.put(m_data, readStart, readLength);
			bb.limit(db.position() * (Float.SIZE / Byte.SIZE));
			}
		
		return (bufferSet);
		}
		
	//---------------------------------------------------------------------------
	/*Message*/
	public void setMessage(BufferPool.BufferSet bufSet, int size)
		{
		Collection<ByteBuffer> buf = bufSet.getBuffers();
		FloatBuffer db;
		if (m_data == null)
			m_data = new float[size / (Float.SIZE / Byte.SIZE)];
			
		int fillStart = 0;
		int fillLength = 0;
		Iterator<ByteBuffer> it = buf.iterator();
		while (it.hasNext())
			{
			db = it.next().asFloatBuffer();
			fillStart += fillLength;
			fillLength = db.remaining();
			db.get(m_data, fillStart, fillLength);
			}		
		}
	}
