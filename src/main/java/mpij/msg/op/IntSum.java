package mpij.msg.op;

import mpij.msg.Message;
import mpij.msg.IntMessage;

public class IntSum implements ReductionOperation
	{
	public Message performOperation(Message msg1, Message msg2)
		{
		IntMessage m1 = (IntMessage)msg1;
		IntMessage m2 = (IntMessage)msg2;
		
		int[] ar1 = m1.getArray();
		int[] ar2 = m2.getArray();
		
		// TODO: check to make sure arrays are the same size
		for (int I = 0; I < ar1.length; I++)
			{
			ar1[I] = ar1[I] + ar2[I];
			}
			
		return (m1);
		}
	}
