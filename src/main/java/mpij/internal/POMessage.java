package mpij.internal;

import java.nio.ByteBuffer;
import mpij.util.BufferPool;

public class POMessage
	{
	private BufferPool.BufferSet m_bufferSet;
	private int m_size;
	private int m_source;
	private int m_tag;
	private short m_context;
	
	public POMessage(BufferPool.BufferSet bufSet, int size, int source, int tag, short context)
		{
		m_bufferSet = bufSet;
		m_size = size;
		m_source = source;
		m_tag = tag;
		m_context = context;
		}
		
	public BufferPool.BufferSet getBufferSet()
		{
		return (m_bufferSet);
		}
		
	public int getSize()
		{
		return (m_size);
		}
		
	public int getSource()
		{
		return (m_source);
		}
		
	public int getTag()
		{
		return (m_tag);
		}
		
	public short getContext()
		{
		return (m_context);
		}
	}
