package mpij.internal.util;

import mpij.util.*;
import java.nio.*;


public class ByteBufferStreamTest
	{
	public static void main(String[] args)
			throws Exception
		{
		BufferPool pool = new BufferPool(8);
		ByteBufferOutputStream bbos = new ByteBufferOutputStream(pool);
		
		System.out.println("Writting");
		for (int I = 0; I < 32; I++)
			bbos.write(I);
		System.out.println();
			
		BufferPool.BufferSet bufs = bbos.getBufferSet();
		
		ByteBufferInputStream bbis = new ByteBufferInputStream(bufs);
		
		int read;
		System.out.println("Reading");
		while ((read = bbis.read()) != -1)
			System.out.print(read+" ");
		System.out.println();
		}
	}
