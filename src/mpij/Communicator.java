package mpij;

import mpij.msg.Message;
import mpij.msg.op.ReductionOperation;
import mpij.internal.InBox;

public interface Communicator
	{
	public static final int ANY_TAG = InBox.ANY_TAG;
	public static final int ANY_SOURCE = InBox.ANY_SOURCE;
	
	public int getRank();
	public int getSize();
	public void send(Message msg, int dest, int tag)
			throws MPIException;
	public void isend(Message msg, int dest, int tag, Status status)
			throws MPIException;
	public void allReduce(Message msg, ReductionOperation op)
			throws MPIException;
	public void reduce(Message msg, int root, ReductionOperation op)
			throws MPIException;
	public void bcast(Message msg, int root)
			throws MPIException;
	public Status recv(Message msg, int source, int tag)
			throws MPIException;
	}
