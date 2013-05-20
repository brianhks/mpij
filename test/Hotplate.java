
import mpij.*;
import mpij.msg.*;
import mpij.msg.op.*;

import static java.lang.System.out;

class Work
	{
	public int startRow;
	public int endRow;
	
	public Work(int start, int end)
		{
		startRow = start;
		endRow = end;
		}
	}
	



public class Hotplate implements MPIRunnable
	{
	private static final int PLATE_HEIGHT = 768;
	private static final int PLATE_WIDTH = 768;
	
	private Communicator m_commWorld;
	
	private int g_nproc;
	private int g_iproc;
	private Work[] g_workQueue;
	
	private int g_threadCount;
	
	private float[][] g_srcPlate = new float[PLATE_HEIGHT][PLATE_WIDTH];
	private float[][] g_desPlate = new float[PLATE_HEIGHT][PLATE_WIDTH];
	
//------------------------------------------------------------------------------
	private void printPlate(float[][] plate)
		{
		for (int I = 0; I < plate.length; I++)
			{
			for (int J = 0; J < plate[0].length; J++)
				out.print(plate[I][J]+" ");
			
			out.println();
			}
		}

//------------------------------------------------------------------------------
	private void presetInnerCells(float[][] plate)
		{
		for (int I = 0; I <= 330; I++)
			plate[400][I] = (float)100.0;
			
		plate[200][500] = (float)100.0;
		}
		
//------------------------------------------------------------------------------
	private void initializePlate(float[][] plate)
		{
		int I, J;
		int workSize;
		int startRow;
		int endRow;
				
		for (I = 0; I < g_workQueue.length; I++)
			{
			startRow = g_workQueue[I].startRow;
			if (startRow == 1)
				startRow--;
				
			endRow = g_workQueue[I].endRow;
			if (endRow == PLATE_HEIGHT-2)
				endRow++;
				
			workSize = endRow - startRow + 1;
			//plate[startRow] = (PLATE_CELL*)malloc(sizeof(PLATE_CELL) * PLATE_WIDTH * workSize);
	
			//for (J = 1; J < workSize; J++)
			//	plate[startRow + J] = &((plate[startRow])[PLATE_WIDTH * J]);
			}
		
		for (I = 1; I < PLATE_HEIGHT-1; I++)
			for (J = 1; J < PLATE_WIDTH-1; J++)
				{
				//printf("row %d col %d\n", I, J);
				//index(plate, I, J).temperature = 50.0;
				plate[I][J] = (float)50.0;
				}	
			
		for (I = 0; I < PLATE_WIDTH; I++)
			{
			//bottom row
			//presetCell(&index(plate, (PLATE_HEIGHT-1), I), 100.0);
			plate[PLATE_HEIGHT-1][I] = (float)100.0;
					
			//top row
			//presetCell(&index(plate, 0, I), 0.0);
			plate[0][I] = (float)0.0;
			}
			
		for (I = 0; I < PLATE_HEIGHT; I++)
			{
			//left row
			//presetCell(&index(plate, I, 0), 0.0);
			plate[I][0] = (float)0.0;
			
			//right row
			//presetCell(&index(plate, I, (PLATE_WIDTH-1)), 0.0);
			plate[I][PLATE_WIDTH-1] = (float)0.0;
			}
			
		presetInnerCells(plate);
		}
		
//------------------------------------------------------------------------------
	private void calculateTemperature(float[][] sp, float[][] dp, int startRow, int endRow)
		{
		int I, J;
		float avg;
	
		for (I = startRow; I <= endRow; I++)
			for (J = 1; J < PLATE_WIDTH -1; J++)
				{
				/*if (index(dp, I, J).temperature == 100.0)
					continue;*/
					
				avg = (sp[I+1][J] + sp[I-1][J] + sp[I][J+1] + sp[I][J-1]) / 4;
					
				dp[I][J] = (sp[I][J] + avg) / 2;
				
				//if (g_unstableCount == 0)
				/*if (unstable == 0)
					{
					calc = index(sp, I, J).temperature - avg;
					if ((calc > 0.1)||(calc < -0.1))
						unstable ++;
						//g_unstableCount++;
					}*/
				}
		}
		
//------------------------------------------------------------------------------
	private void sendBorder(float[][] plate, int row, int dest)
			throws MPIException
		{
		//Request request = new Request();
		
		m_commWorld.send(new FloatMessage(plate[row]), dest, 0);
		}
		
//------------------------------------------------------------------------------
	private void receiveBorder(float[][] plate, int row, int src)
			throws MPIException
		{
		m_commWorld.recv(new FloatMessage(plate[row]), src, 0);
		}
		
//------------------------------------------------------------------------------
	private int isStable(float[][] plate, int startRow, int endRow)
		{
		int I, J;
		float avg;
		float calc;
		int count = 0;
	
		for (I = startRow; I <= endRow; I++)
			for (J = 1; J < PLATE_WIDTH -1; J++)
				{
				if (plate[I][J] == 100.0)
					continue;
					
				avg = (plate[I+1][J] + plate[I-1][J] + plate[I][J+1] + plate[I][J-1]) / 4;
				
				calc = plate[I][J] - avg;
				
				if ((calc > 0.1)||(calc < -0.1))
					{
					//return (1);
					count++;
					}
				}
	
		return (count);
		}
		
//------------------------------------------------------------------------------
	private int g_waitingThreads = 0;
	//int g_unstableCount = 0;
	private int g_continue = 1;
	private int g_checkCount = 0;	
	
	private int calculate(Work work)
			throws MPIException
		{
		int iterations = 0;
		float[][] tmpPlate;
		int isUnstable = 0;
		int firstRun = 1;
		
		while (g_continue != 0)
			{
			//out.println("Round "+iterations);
			//out.println("Calculating temperature");
			calculateTemperature(g_srcPlate, g_desPlate, work.startRow+1, work.endRow-1);
			
			//if (g_iproc == 1)
			//	printPlate(*g_desPlate);
				
			if (firstRun != 1)
				{
				if (g_iproc != 0)
					{
					//out.println("Recieving top border");
					receiveBorder(g_srcPlate, work.startRow-1, g_iproc-1);
					}
				
				if (g_iproc != (g_nproc -1))
					{
					//out.println("Recieving bottom border");
					receiveBorder(g_srcPlate, work.endRow+1, g_iproc+1);
					}
				}
			firstRun = 0;
			
			//if (g_iproc == 1)
			//	printPlate(*g_desPlate);
			
			//Calculate border rows
			calculateTemperature(g_srcPlate, g_desPlate, work.startRow, work.startRow);
			calculateTemperature(g_srcPlate, g_desPlate, work.endRow, work.endRow);
					
			//printf("isStable check for %d to %d\n", work->startRow, work->endRow);
			isUnstable = isStable(g_srcPlate, work.startRow, work.endRow);
			
			//out.println("All reduce "+isUnstable);
			
			if (g_iproc == 0)
				{
				IntMessage intmsg = new IntMessage(0);
				for (int I = 1; I < g_nproc; I++)
					{
					m_commWorld.recv(intmsg, I, 0);
					isUnstable += intmsg.getInt();
					}
					
				intmsg = new IntMessage(isUnstable);
				for (int I = 1; I < g_nproc; I++)
					{
					m_commWorld.send(intmsg, I, 0);
					}
				}
			else
				{
				IntMessage intmsg = new IntMessage(isUnstable);
				m_commWorld.send(intmsg, 0, 0);
				m_commWorld.recv(intmsg, 0, 0);
				isUnstable = intmsg.getInt();
				}
				
			/* IntMessage intmsg = new IntMessage(isUnstable);
			m_commWorld.allReduce(intmsg, new IntSum());
			isUnstable = intmsg.getInt(); */
			
			//out.println("Done reduce "+isUnstable);
			
			
			if (isUnstable != 0)
				{				
				presetInnerCells(g_desPlate);
				
				//swap plates
				tmpPlate = g_srcPlate;
				g_srcPlate = g_desPlate;
				g_desPlate = tmpPlate;
				
				//We just swapped plates so send the src plate
				if (g_iproc != 0)
					{
					//out.println("Sending top border");
					sendBorder(g_srcPlate, work.startRow, g_iproc-1);
					}
					
				if (g_iproc != (g_nproc -1))
					{
					//out.println("Sending bottom border");
					sendBorder(g_srcPlate, work.endRow, g_iproc+1);
					}
				}
			else
				g_continue = 0;
				
			iterations++;
			}
		
		return (iterations);
		}
//------------------------------------------------------------------------------
	private int checkStability(float[][] dp)
		{
		int count = 0;
		int I, J;
		float avg;
		float calc;
		
		for (I = 1; I < PLATE_HEIGHT -1; I++)
			for (J = 1; J < PLATE_WIDTH -1; J++)
				{
				if (dp[I][J] == 100.0)
					continue;
					
				avg = (dp[I+1][J] + dp[I-1][J] + dp[I][J+1] + dp[I][J-1]) / 4;
						
				calc = Math.abs(dp[I][J] - avg);
				if (calc > 0.1)
					{
					count++;
					}
				
				}
				
		return (count);
		}
		
//------------------------------------------------------------------------------
//------------------------------------------------------------------------------
	
	public Hotplate()
		{
		}
	
	public void run(Communicator commWorld)
		{
		m_commWorld = commWorld;
		int iterations = 0;
		int I, J;
		int above50cnt = 0;
		long startTime = 0;
		long endTime = 0;
	
		int remainder;
		int startRow = 1; //Start this off at 1
		int workSize = 0;
		int workBlockSize;
		
		//printf("Calling MPI_Init()\n");
		try
			{
			//printf("Calling MPI_Comm_size()\n");
			g_nproc = commWorld.getSize();
			g_iproc = commWorld.getRank();
			
			
			startTime = System.currentTimeMillis();
				
			g_threadCount = g_nproc;
			
			g_workQueue = new Work[g_nproc];
			
			
			workBlockSize = ((PLATE_HEIGHT -2) / g_threadCount);
			//printf("workBlockSize = %d\n", workBlockSize);
			remainder = PLATE_HEIGHT - workBlockSize * g_workQueue.length - 2;
			//printf("remainder = %d\n", remainder);
			
			for (I = 0; I < g_workQueue.length; I++)
				{
				workSize = workBlockSize;
				
				if (remainder > 0)
					{
					workSize++;
					remainder--;
					}
					
				g_workQueue[I] = new Work(startRow, (startRow + workSize -1));
							
				startRow += workSize;
				//out.println("workSize = "+workSize);
				}
				
			//out.println("Initializing plates");
			initializePlate(g_srcPlate);
			initializePlate(g_desPlate);
			
			/* System.out.println(g_iproc +" Start row: "+g_workQueue[g_iproc].startRow+
					" End row: "+g_workQueue[g_iproc].endRow); */
			
			out.println("Calling calculate");
			iterations = calculate(g_workQueue[g_iproc]);
			out.println("Done calculating");
			
			for (I = g_workQueue[g_iproc].startRow; I <= g_workQueue[g_iproc].endRow; I++)
				for (J = 0; J < PLATE_WIDTH; J++)
					{
					if (g_desPlate[I][J] > 50.0)
						above50cnt ++;
					}
				
			//printf("Calling MPI_Reduce() on %d\n", g_iproc);
			
			commWorld.reduce(new IntMessage(above50cnt), 0, new IntSum());			
		
			endTime = System.currentTimeMillis();
			
			if (g_iproc == 0)
				{
				startTime = endTime - startTime;
				out.println("total "+((double)startTime)/1000+" sec");
				out.println(iterations+" it, ");
				out.println(above50cnt);
				}	
			
			//printf("Stability %d\n", checkStability(*srcPlate));
			//printf("Calling MPI_Finalize() on %d\n", g_iproc);
			}
		catch (MPIException mpie)
			{
			mpie.printStackTrace();
			}
		}
	
	}
