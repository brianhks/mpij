package mpij;


public class Status
	{
	private int m_source;
	private int m_tag;
	private int m_size;
	
	public Status(int source, int tag, int size)
		{
		m_source = source;
		m_tag = tag;
		m_size = size;
		}
		
	public int getSource()
		{
		return (m_source);
		}
		
	public int getTag()
		{
		return (m_tag);
		}
		
	public int getSize()
		{
		return (m_size);
		}
	
	}
