package mpij.msg;

import java.nio.ByteBuffer;
import mpij.util.BufferPool;


public interface Message
	{
	public BufferPool.BufferSet getMessage(BufferPool pool) throws MessageException;
	public void setMessage(BufferPool.BufferSet buf, int size) throws MessageException;
	
	}
