package mpij.util;

import java.nio.ByteBuffer;
import java.lang.ref.*;
import java.util.*;
import java.util.concurrent.*;

public class BufferPool
	{
	private static final int MAX_POOL_SIZE = 1000;
	private int m_bufferSize;
	private ByteBuffer[] m_pool;
	private int m_poolPtr;
	private ConcurrentHashMap<BufferSetReference, String> m_refSet;
	private ReferenceQueue m_refQueue;
	
	
	//===========================================================================
	public class BufferSet
		{
		private BufferSetReference m_ref;
		private ArrayList<ByteBuffer> m_buffers;
		//Need to store buffers
		
		private BufferSet()
			{
			m_buffers = new ArrayList<ByteBuffer>();
			}
			
		private void setReference(BufferSetReference ref)
			{
			m_ref = ref;
			}
			
		public ByteBuffer[] allocateBuffers(int size)
			{
			int requiredBuffers = ((size / m_bufferSize) + 1);
			ByteBuffer[] ret = new ByteBuffer[requiredBuffers];
			
			for (int I = 0; I < requiredBuffers; I++)
				{
				ret[I] = allocateBuffer();
				}
				
			ret[requiredBuffers -1].limit(size - (m_bufferSize * (requiredBuffers-1)));
			return (ret);
			}
			
		public ByteBuffer allocateBuffer()
			{
			//ByteBuffer buf = m_ref.getBuffer();
			ByteBuffer buf = getBufferFromPool();
			m_buffers.add(buf);
			return (buf);
			}
			
		/**
		Returns previously allocated buffers
		*/
		public Collection<ByteBuffer> getBuffers()
			{
			//return (m_buffers.toArray(new ByteBuffer[0]));
			return (m_buffers);
			}
		}
		
	//===========================================================================
	private class BufferSetReference extends WeakReference
		{
		private LinkedList<ByteBuffer> m_buffers;
		
		public BufferSetReference(BufferSet bs, ReferenceQueue q)
			{
			super(bs, q);
			bs.setReference(this);
			m_buffers = new LinkedList<ByteBuffer>();
			}
			
		private ByteBuffer getBuffer()
			{
			ByteBuffer buf = getBufferFromPool();
			m_buffers.add(buf);
			
			return (buf);
			}
			
		private void recycleBuffers()
			{
			Iterator<ByteBuffer> it = m_buffers.iterator();
			
			while (it.hasNext())
				{
				returnBuffer(it.next());
				}
			
			m_buffers.clear();
			}
		}
	
	//===========================================================================
	public BufferPool(int bufferSize)
		{
		m_bufferSize = bufferSize;
		//m_pool = new ByteBuffer[MAX_POOL_SIZE];
		/* for (int I = 0; I < MAX_POOL_SIZE; I++)
			m_pool[I] = ByteBuffer.allocateDirect(m_bufferSize); */
		//m_poolPtr = MAX_POOL_SIZE;
		m_poolPtr = 0;
		m_refSet = new ConcurrentHashMap<BufferSetReference, String>();
		m_refQueue = new ReferenceQueue();
		}
		
	//---------------------------------------------------------------------------
	public BufferSet getBufferSet()
		{
		BufferSet bs = new BufferSet();
		//BufferSetReference bsr = new BufferSetReference(bs, m_refQueue);
		//m_refSet.put(bsr, "");
		return (bs);
		}
		
	//---------------------------------------------------------------------------
	private void emptyQueue()
		{
		BufferSetReference bsr;
		
		while ((bsr = (BufferSetReference)m_refQueue.poll()) != null)
			{
			//System.out.println("Recycle");
			bsr.recycleBuffers();
			m_refSet.remove(bsr);
			}
		}
	
	//---------------------------------------------------------------------------
	private synchronized ByteBuffer getBufferFromPool()
		{
		ByteBuffer ret;
		
		/* emptyQueue();
		
		if (m_poolPtr != 0)
			{
			m_poolPtr--;
			ret = m_pool[m_poolPtr];
			}
		else
			{ */
			ret = ByteBuffer.allocateDirect(m_bufferSize);
			//}
			
		return (ret);
		}
		
	//---------------------------------------------------------------------------
	public synchronized void returnBuffer(ByteBuffer buf)
		{
		/* if (m_poolPtr != MAX_POOL_SIZE)
			{
			buf.clear();
			m_pool[m_poolPtr] = buf;
			m_poolPtr++;
			} */
		}
		
	//---------------------------------------------------------------------------
	public void returnBuffers(ByteBuffer[] bufs)
		{
		for (ByteBuffer buf : bufs)
			returnBuffer(buf);
		}
		
	public void printStats()
		{
		}
	}
