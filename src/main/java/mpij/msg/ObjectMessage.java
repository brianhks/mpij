package mpij.msg;

import mpij.util.BufferPool;

import mpij.internal.util.ByteBufferInputStream;
import mpij.internal.util.ByteBufferOutputStream;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;


public class ObjectMessage<T>
		implements Message
	{
	T m_messageObj;
	
	public ObjectMessage(T obj)
		{
		m_messageObj = obj;
		}
	
	//---------------------------------------------------------------------------
	public ObjectMessage()
		{
		m_messageObj = null;
		}
	
	//---------------------------------------------------------------------------
	public T getObject()
		{
		return (m_messageObj);
		}
		
	//---------------------------------------------------------------------------
	public BufferPool.BufferSet getMessage(BufferPool pool)
			throws MessageException
		{
		ByteBufferOutputStream bbos = new ByteBufferOutputStream(pool);
		
		try
			{
			ObjectOutputStream oos = new ObjectOutputStream(bbos);
		
			oos.writeObject(m_messageObj);
			//oos.close();
			
			}
		catch (IOException ioe)
			{
			ioe.printStackTrace();
			}
			
		//System.out.println("Write count "+bbos.getWriteCount());
		return (bbos.getBufferSet());
		}
		
	//---------------------------------------------------------------------------
	public void setMessage(BufferPool.BufferSet bufSet, int size)
			throws MessageException
		{
		ByteBufferInputStream bbis = new ByteBufferInputStream(bufSet);
		
		try
			{
			ObjectInputStream ois = new ObjectInputStream(bbis);
			
			m_messageObj = (T)ois.readObject();
			//System.out.println("Read count "+bbis.getReadCount());
			//ois.close();
			}
		catch (IOException ioe)
			{
			ioe.printStackTrace();
			}
		catch (ClassNotFoundException cnfe)
			{
			cnfe.printStackTrace();
			}
		//System.out.println("Read count "+bbis.getReadCount());
		}
	}
