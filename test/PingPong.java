
import mpij.*;
import mpij.msg.*;

import java.util.*;


public class PingPong implements MPIRunnable
	{
	private int m_nproc;
	private int m_iproc;
	
	
	public PingPong()
		{
		}
		
	public void run(Communicator commWorld)
			throws Exception
		{
		m_nproc = commWorld.getSize();
		m_iproc = commWorld.getRank();
		
		int repeat = 10000;
		
		for (double dataSz = 1; dataSz < (1024 * 1024); dataSz *= 1.25)
			{
			byte[] dataArr = new byte[(int)dataSz];
			Arrays.fill(dataArr, (byte)42);
			ByteMessage data = new ByteMessage(dataArr);
			long start = System.currentTimeMillis();
			
			for (int I = 0; I < repeat; I++)
				{
				if (m_iproc == 0)
					{
					commWorld.send(data, 1, 0);
					commWorld.recv(data, 1, 0);
					}
				else
					{
					commWorld.recv(data, 0, 0);
					commWorld.send(data, 0, 0);
					}
				}
			
			long time = System.currentTimeMillis() - start;
			if (time > 5000)
				{
				repeat -= repeat * 0.25;
				if (repeat <= 0)
					repeat = 1;
				}
			
			if (m_iproc == 0)
				{
				//System.out.println("Data size "+(int)dataSz+" time "+time);
				//divide by 1000 because time is thousaths of a sec and 2 because we want only on direction 
				double dtime = ((double)time / (double)repeat) / 2000;
				double dataflow = (double)(((int)dataSz + 11) * 8) / dtime;
				System.out.println((int)dataSz+","+(int)dataflow);
				}
			}
		}
	}
