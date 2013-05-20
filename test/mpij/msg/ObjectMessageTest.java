package mpij.msg;

import mpij.util.BufferPool;
import java.nio.ByteBuffer;

public class ObjectMessageTest
	{
	
	public static void main(String[] args)
			throws Exception
		{
		BufferPool pool = new BufferPool(8);
		String message = "This is my message";
		
		ObjectMessage<String> om = new ObjectMessage<String>(message);
		
		BufferPool.BufferSet bufSet = om.getMessage(pool);
		System.out.println("Number of buffers "+bufSet.getBuffers().size());
		
		ObjectMessage<String> newmsg = new ObjectMessage<String>();
		
		newmsg.setMessage(bufSet, bufSet.getBuffers().iterator().next().limit());
		
		//System.out.println("Ramaining "+bufs[bufs.length-1].remaining());
		
		System.out.println(newmsg.getObject());
		
		System.out.println("Messages the same "+ newmsg.getObject().equals(message));
		}
	}
