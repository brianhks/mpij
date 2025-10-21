package mpij.msg;

import mpij.util.BufferPool;
import java.nio.ByteBuffer;

public class IntMessageTest
	{
	public static void main(String[] args)
			throws Exception
		{
		BufferPool pool = new BufferPool(1024);
		//Test one
		testOne(pool);
		testTwo(pool);		
		}
		
	private static void testOne(BufferPool pool)
		{
		int[] message = new int[] { 0,1,2,3,4,5,6,7,8,9 };
		
		IntMessage im = new IntMessage(message);
		
		BufferPool.BufferSet bufferSet = im.getMessage(pool);
		
		IntMessage newmsg = new IntMessage();
		
		newmsg.setMessage(bufferSet, 10 * (Integer.SIZE / Byte.SIZE));
		
		int[] ret = newmsg.getArray();
		for (int val : ret)
			System.out.print(val + " ");
			
		System.out.println();
		}
		
	private static void testTwo(BufferPool pool)
		{
		int message = 42;
		
		IntMessage im = new IntMessage(message);
		BufferPool.BufferSet bufSet = im.getMessage(pool);
		
		IntMessage newmsg = new IntMessage();		
		newmsg.setMessage(bufSet, 1 * (Integer.SIZE / Byte.SIZE));
		
		int ret = newmsg.getInt();
		System.out.println(ret);
		}
	}
