package mpij.internal.util;

import mpij.util.BufferPool;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class ByteBufferInputStream extends InputStream
	{
	private BufferPool.BufferSet m_bufferSet;
	private Iterator<ByteBuffer> m_iterator;
	private ByteBuffer m_curBuffer;
	private int m_readCount;
	
	public ByteBufferInputStream(BufferPool.BufferSet bufSet)
		{
		m_bufferSet = bufSet;
		m_iterator = m_bufferSet.getBuffers().iterator();
		m_curBuffer = m_iterator.next();
		m_readCount = 0;
		}
		
	/* public int getReadCount()
		{
		return (m_readCount);
		} */
	
	public int read()
		{
		m_readCount ++;
		int ret = 0;
		
		if (m_curBuffer.position() == m_curBuffer.limit())
			{
			System.out.println("Buffer switch "+m_readCount);
			if (!m_iterator.hasNext())
				return (-1);
				
			m_curBuffer = m_iterator.next();
			}
	
		ret = m_curBuffer.get();
		
		return (ret);
		}
	}
