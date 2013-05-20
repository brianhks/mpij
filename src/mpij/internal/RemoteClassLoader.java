package mpij.internal;

import java.util.concurrent.atomic.*;
import mpij.msg.*;
import mpij.MPIException;

public class RemoteClassLoader extends ClassLoader
	{
	private InternalCommunicator m_comm;
	private AtomicInteger m_requestTag;
	
	public RemoteClassLoader(ClassLoader parent, PostOffice po)
		{
		super(parent);
		m_comm = new InternalCommunicator(PostOffice.CLASS_LOADER_MSG, po, (short)0);
		m_requestTag = new AtomicInteger(0);
		}
		
	public Class loadClass(String name)
			throws ClassNotFoundException
		{
		//System.out.println("Loading "+name);
		return super.loadClass(name);
		}
		
	public Class findClass(String name)
			throws ClassNotFoundException
		{
		//System.out.println("findClass("+name+")");
		Class ret = null;
		try
			{
			int tag = m_requestTag.getAndIncrement();
			m_comm.send(new ObjectMessage<String>(name), 0, (short)tag);
			ByteMessage classMsg = new ByteMessage();
			m_comm.recv(classMsg, 0, (short)tag);
			
			byte[] bytes = classMsg.getArray();
			
			if (bytes.length == 0)
				{
				//System.out.println("Can't find "+name);
				throw new ClassNotFoundException(name);
				}
			ret = defineClass(name, bytes, 0, bytes.length);
			//resolveClass(ret);
			}
		catch (MPIException mpie)
			{
			//System.out.println("Exception "+mpie);
			throw new ClassNotFoundException(name);
			}

		return (ret);
		}
	}
