package generic;

import processor.Clock;
import processor.Processor;

//Added - Starts
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;

import processor.memorysystem.MainMemory;
import processor.pipeline.RegisterFile;
//Added - Ends

public class Simulator {
		
	Processor processor;
	static boolean simulationComplete;
	MainMemory mainMemory;	
	
	static EventQueue eventQueue;

	public void setupSimulation(String assemblyProgramFile, Processor p)
	{
		this.processor = p;
		this.mainMemory = new MainMemory();
		
		boolean debug = true;

		eventQueue = new EventQueue();
		loadProgram(assemblyProgramFile, p.getRegisterFile());
			processor.setMainMemory(mainMemory);
		processor.setRegisterFile(p.getRegisterFile());

		simulationComplete = false;
	}
	
	void loadProgram(String assemblyProgramFile, RegisterFile registerFile)
	{
		if(registerFile == null)
		{
			System.err.println("ERROR: RegisterFile is null. Cannot load program.");
			return;
		}

		
		
		FileInputStream inputStream = null;
		try
		{
			inputStream = new FileInputStream(assemblyProgramFile);
		} catch(FileNotFoundException e) {
			Misc.printErrorAndExit(e.toString());
		}
		
		try {
			byte[] buffer = new byte[4]; // Buffer to hold 4 bytes
			
			int wordRead;
			wordRead = inputStream.read(buffer);
			// System.out.println(wordRead);
			
			int firstCodeAddress = ((buffer[0] & 0xFF) << 24) | 
                    ((buffer[1] & 0xFF) << 16) | 
                    ((buffer[2] & 0xFF) << 8)  | 
                    (buffer[3] & 0xFF);
			// System.out.println(firstCodeAddress);
			
			for(int i=0; i < firstCodeAddress; ++i) {
        		wordRead = inputStream.read(buffer);
        		// Convert the 4 bytes to an integer
                int intValueInside = ((buffer[0] & 0xFF) << 24) | 
                               ((buffer[1] & 0xFF) << 16) | 
                               ((buffer[2] & 0xFF) << 8)  | 
                               (buffer[3] & 0xFF);
                // System.out.println(intValueInside);
                mainMemory.setWord(i, intValueInside);
        	}
			
			int i = firstCodeAddress;
            while ((wordRead = inputStream.read(buffer)) != -1) {
            	// Convert the 4 bytes to an integer
                int instructionIntValue = ((buffer[0] & 0xFF) << 24) | 
                               ((buffer[1] & 0xFF) << 16) | 
                               ((buffer[2] & 0xFF) << 8)  | 
                               (buffer[3] & 0xFF);
            	
                mainMemory.setWord(i, instructionIntValue);
                ++i;
                // System.out.println(instructionIntValue);
            }

			registerFile.setProgramCounter(firstCodeAddress);
			

			registerFile.setValue(0, 0);
			
			
			int max = (int) Math.pow(2, 16) - 1;
			registerFile.setValue(1, max );
			registerFile.setValue(2, max );
			
			
			
		}
			catch (IOException e) {
				e.printStackTrace();
		}
	}

	public void printCurrentState()
	{
		//PRINTING REGISTER CONTENT
		String registerContent = this.processor.getRegisterFile().getContentsAsString();
		System.out.println("registerContent:");
		System.out.println(registerContent);

		//PRINTING MEMORY CONTENT
		String memoryContent = this.mainMemory.getContentsAsString(0,30);
		System.out.println("memoryContent:");
		System.out.println(memoryContent);
		//DEBUGGING SECTION ENDS
	}
	
	public void simulate()
	{
		int i = 0;
		while(simulationComplete == false)
		{
			// this.printCurrentState();

			//RW
			processor.getRWUnit().performRW();
//			Clock.incrementClock();
			// this.printCurrentState();


			//MA
			processor.getMAUnit().performMA();
//			Clock.incrementClock();
			// this.printCurrentState();


			//EX
			processor.getEXUnit().performEX();
//			Clock.incrementClock();
			// this.printCurrentState();
			
			eventQueue.processEvents();

			//OF
			processor.getOFUnit().performOF();
//			Clock.incrementClock();
			// this.printCurrentState();
			
			//IF
			processor.getIFUnit().performIF();
//			Clock.incrementClock();
			// this.printCurrentState();
			
			//increment clock by 1 
			System.out.println("Cycle " + ++i);
			if (debug) this.printCurrentState();
			
			Clock.incrementClock();
			
		}
		
		// TODO
		// set statistics
		Statistics.setNumberOfInstructions(this.processor.EX_IF_Latch.getNumberOfInstructions());
		Statistics.setNumberOfCycles(i);
		
		Statistics.setThroughput();
		Statistics.setCpi();
		Statistics.setNumberOfDataHazards(this.processor.OF_EX_Latch.getNumberOfDataHazards());
		Statistics.setNumberOfBranches(this.processor.EX_IF_Latch.getNumberOfBranches());
		Statistics.setNumberOfCyclesOfOFStalls(this.processor.OF_EX_Latch.getNumberOfCyclesOfOFStalls());

//		Statistics.setTimeTaken(Clock.getCurrentTime());
	}
	
	public static void setSimulationComplete(boolean value)
	{
		simulationComplete = value;
	}
	public static EventQueue getEventQueue( )
	{
	return eventQueue ;
	}
}
