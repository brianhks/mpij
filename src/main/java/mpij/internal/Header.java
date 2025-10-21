package mpij.internal;

import java.nio.ByteBuffer;

/**
This class contains the header information for data sent on the wire
*/
/*package*/ class Header
	{
	/** Size of the Header */
	public static final int SIZE = 1+4+4+2;
	
	private byte m_msgType;
	private int m_length;   //Length of message after the header
	private int m_tag;
	private short m_context;
	
	public Header(byte msgType, int length, int tag, short context)
		{
		m_msgType = msgType;
		m_tag = tag;
		m_length = length;
		m_context = context;
		}
		
	/**
	Creates a Header object by reading the data from the given buffer
	*/
	public Header(ByteBuffer buf)
		{
		m_msgType = buf.get();
		m_length = buf.getInt();
		m_tag = buf.getInt();
		m_context = buf.getShort();
		}
		
	public void writeHeader(ByteBuffer buf)
		{
		buf.put(m_msgType);
		buf.putInt(m_length);
		buf.putInt(m_tag);
		buf.putShort(m_context);
		}
		
	public byte getType()
		{
		return (m_msgType);
		}
		
	public int getLength()
		{
		return (m_length);
		}
		
	public int getTag()
		{
		return (m_tag);
		}
		
	public short getContext()
		{
		return (m_context);
		}
		
	public String toString()
		{
		StringBuilder sb = new StringBuilder();
		sb.append("Type: ");
		sb.append(m_msgType);
		sb.append("\nLength: ");
		sb.append(m_length);
		sb.append("\nTag: ");
		sb.append(m_tag);
		sb.append("\nContext: ");
		sb.append(m_context);
		sb.append("\n");
		return (sb.toString());
		}
		
	}
