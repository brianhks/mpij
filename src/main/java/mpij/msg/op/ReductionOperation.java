package mpij.msg.op;

import mpij.msg.Message;

public interface ReductionOperation
	{
	Message performOperation(Message msg1, Message msg2);
	}
