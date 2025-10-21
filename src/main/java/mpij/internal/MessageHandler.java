package mpij.internal;

import mpij.util.BufferPool;

public interface MessageHandler
	{
	public void newMessage(Header head, int src, BufferPool.BufferSet bufSet);
	}
