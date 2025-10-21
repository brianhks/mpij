package mpij.internal;

import mpij.msg.*;
import mpij.*;
import mpij.util.*;
import mpij.internal.util.*;
import mpij.msg.op.ReductionOperation;

import java.nio.ByteBuffer;
import java.io.IOException;

public class CommunicatorImpl extends InternalCommunicator
		implements Communicator
	{
	private int m_commSize;  //number of procs in communicator
	private int m_commRank;  //This communicators rank
	
	private class SendThread extends Thread
		{
		Message m_msg;
		int m_dest;
		int m_tag;
		Status m_status;
		
		public SendThread(Message msg, int dest, int tag, Status status)
			{
			m_msg = msg;
			m_dest = dest;
			m_tag = tag;
			m_status = status;
			
			start();
			}
			
		public void run()
			{
			try
				{
				send(m_msg, m_dest, m_tag);
				//if (m_status != null)
					//set completion flag
				}
			catch (MPIException mpie)
				{
				//put exception on status
				mpie.printStackTrace();
				}
			}
		}
	
	/*package*/ CommunicatorImpl(PostOffice po, short context)
		{
		super(PostOffice.CLIENT_MSG, po, context);
		m_commSize = po.getPeerCount();
		m_commRank = po.getLocalId();
		}
		
	//---------------------------------------------------------------------------
	private int findMask(int val)
		{
		int count = 0;
		
		if (val == 0)
			return (0);
		
		while (val != 1)
			{
			val >>= 1;
			count ++;
			}
			
		return (1 << count);
		}
		
	//---------------------------------------------------------------------------
	private int shiftId(int id, int root)
		{
		int myId;
		int shift;
		
		shift = m_commSize - root;	
		myId = (id + shift) % m_commSize;
			
		return (myId);
		}
		
	//---------------------------------------------------------------------------
	private int unshiftId(int id, int root)
		{
		int myId;
		int shift;
		
		shift = m_commSize - root;
		myId = (id + m_commSize - shift) % m_commSize;
			
		return (myId);
		}
		
	//---------------------------------------------------------------------------
	//---------------------------------------------------------------------------
	/*Communicator*/
	public int getRank()
		{
		return (m_commRank);
		}
		
	//---------------------------------------------------------------------------
	/*Communicator*/
	public int getSize()
		{
		return (m_commSize);
		}
		
	//---------------------------------------------------------------------------
	/*communicator*/
	public void isend(Message msg, int dest, int tag, Status status)
			throws MPIException
		{
		//This needs to use the util.concurrent package to use pooled threads
		new SendThread(msg, dest, tag, status);
		}
		
	//---------------------------------------------------------------------------
	/*Communicator*/
	public void allReduce(Message msg, ReductionOperation op)
			throws MPIException
		{
		reduce(msg, 0, op);
		//System.out.println("bcasting");
		bcast(msg, 0);
		}
		
	//---------------------------------------------------------------------------
	/*Communicator*/
	public void reduce(Message msg, int root, ReductionOperation op)
			throws MPIException
		{
		int I;
		int mask;
		int remoteNode;
		int myId = shiftId(m_commRank, root);
		try
			{
			Message recMsg = msg.getClass().newInstance();
			
			mask = findMask(m_commSize - 1);
			
			while (mask != 0)
				{
				//If g_iproc is greater then the mask we will not participate
				if ((myId <= (mask + (mask -1))) &&
						((remoteNode = (myId ^ mask)) < m_commSize))
					{
					remoteNode = unshiftId(remoteNode, root);
					if ((myId & mask) > 0)
						{
						//System.out.println(myId+" sending to "+remoteNode);
						//send
						send(msg, remoteNode, 0); 
						}
					else
						{
						//System.out.println(myId+" receiving from "+remoteNode);
						//receive
						recv(recMsg, remoteNode, 0);
						
						msg = op.performOperation(msg, recMsg);
						//System.out.println(myId+" received from "+remoteNode);
						}
					}
					
				mask >>= 1;
				}
			}
		catch (InstantiationException ie)
			{
			ie.printStackTrace();
			}
		catch (IllegalAccessException iae)
			{
			iae.printStackTrace();
			}
		}
		
	//---------------------------------------------------------------------------
	/*Communicator*/
	public void bcast(Message msg, int root)
			throws MPIException
		{
		int I;
		int mask;
		int mymask;
		int remoteNode;
		int myId = shiftId(m_commRank, root);

		mask = findMask(m_commSize - 1);
		mymask = 1;
		
		while (mymask <= mask)
			{
			//If m_commSize is greater then the mask we will not participate
			if ((myId <= (mymask + (mymask -1))) &&
					((remoteNode = (myId ^ mymask)) < m_commSize))
				{
				remoteNode = unshiftId(remoteNode, root);
				if ((myId & mymask) > 0)
					{
					//receive
					recv(msg, remoteNode, 0); 
					}
				else
					{
					//send
					send(msg, remoteNode, 0);
					}
				}
				
			mymask <<= 1;
			}
		}
		
	
	}
