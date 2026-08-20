package processor.pipeline;

import processor.Processor;
import generic.Event;
import generic.Element;
import generic.Simulator;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import processor.Clock;
import configuration.Configuration;

public class InstructionFetch {
	
	Processor containingProcessor;
	IF_EnableLatchType IF_EnableLatch;
	IF_OF_LatchType IF_OF_Latch;
	EX_IF_LatchType EX_IF_Latch;
	
	public InstructionFetch(Processor containingProcessor, IF_EnableLatchType iF_EnableLatch, IF_OF_LatchType iF_OF_Latch, EX_IF_LatchType eX_IF_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.IF_EnableLatch = iF_EnableLatch;
		this.IF_OF_Latch = iF_OF_Latch;
		this.EX_IF_Latch = eX_IF_Latch;
	}
	
	public void performIF()
	{
		
		if(IF_OF_Latch.isOF_busy() || IF_EnableLatch.isIF_busy())
		{
		return;
		}
		
		else
		{
			IF_EnableLatch.setIF_busy(false);
		}
		
		
		if(IF_EnableLatch.isIF_enable())
		{
			if(IF_EnableLatch.isNop())
			{
				System.out.println("nop in IF");

				IF_OF_Latch.setNop(true);
				IF_EnableLatch.setNop(false);
				return;
			}
			//System.out.println("PERFORMING IF");

			int currentPC = containingProcessor.getRegisterFile().getProgramCounter();
			//IF_OF_Latch.setCurrentPC(currentPC);

			if(EX_IF_Latch.isIF_enable())
			{
				// System.out.println("FROM EX TO IF");
				int isBranchTaken = EX_IF_Latch.getIsBranchTaken();
				if(isBranchTaken == 1)
				{
					currentPC = EX_IF_Latch.getBranchPC();
					// System.out.println("Branch taken: " + currentPC);
					containingProcessor.getRegisterFile().setProgramCounter(currentPC);

					//STATISTICS
					//INCREMENTING NUMBER OF BRANCH TAKEN
					EX_IF_Latch.incrementNumberOfBranches();

					EX_IF_Latch.setIF_enable(false);
				}
			}
			
			Simulator.getEventQueue().addEvent(
				new MemoryReadEvent(
					Clock.getCurrentTime() + Configuration.mainMemoryLatency,
					this,  
					containingProcessor.getMainMemory(),
					containingProcessor.getRegisterFile().getProgramCounter()
				)
			);
			
			IF_OF_Latch.setOF_stall(true);
			
			int newInstruction = containingProcessor.getMainMemory().getWord(currentPC);
			//IF_OF_Latch.setInstruction(newInstruction);
			containingProcessor.getRegisterFile().setProgramCounter(currentPC + 1);
			
			//PRINTING THE INSTRUCTION & PC FOR DEBUGGING
			String binaryString = Integer.toBinaryString(newInstruction);
			while(binaryString.length() < 32)
			{
				binaryString = "0" + binaryString;
			}
			String byte1 = binaryString.substring(0, 8);
			String byte2 = binaryString.substring(8, 16);
			String byte3 = binaryString.substring(16, 24);
			String byte4 = binaryString.substring(24, 32);
			
			System.out.println("Instruction fetched: " + byte1 + " " + byte2 + " " + byte3 + " " + byte4);
			System.out.println("Instruction fetched: " + newInstruction);
			System.out.println("PC: " + currentPC);
			//DEBUGGING SECTION ENDS
			int instruct = IF_OF_Latch.getInstruction();
			System.out.println("INSTRUCTION AVAILABLE IN IF-OF LATCH" + instruct);
			

			IF_OF_Latch.setCurrentPC(currentPC);

			// IF_EnableLatch.setIF_enable(false);
			IF_EnableLatch.setIF_busy(true);  // Mark as busy until response is received
		}
	}
	
@Override
	public void handleEvent(Event e) {


		if (IF_OF_Latch.isOF_busy()) {

			e.setEventTime(Clock.getCurrentTime() + 1);
			Simulator.getEventQueue().addEvent(e);
		}
		else {
			//System.out.println("\nOF NOT BUSY - HANDLING EVENT\n");

			MemoryResponseEvent event = (MemoryResponseEvent) e;
			IF_OF_Latch.setInstruction(event.getValue());
			IF_OF_Latch.setOF_enable(true);
			IF_EnableLatch.setIF_busy(false);

			IF_OF_Latch.setCurrentPC(event.getCurrentPC());
			// int currentPC = containingProcessor.getRegisterFile().getProgramCounter();
			// containingProcessor.getRegisterFile().setProgramCounter(currentPC + 1);

			//PRINTING THE INSTRUCTION & PC FOR DEBUGGING
			int currentPC = event.getCurrentPC();
			int newInstruction = IF_OF_Latch.getInstruction();
			
			String binaryString = Integer.toBinaryString(newInstruction);
			while(binaryString.length() < 32)
			{
				binaryString = "0" + binaryString;
			}
			String byte1 = binaryString.substring(0, 8);
			String byte2 = binaryString.substring(8, 16);
			String byte3 = binaryString.substring(16, 24);
			String byte4 = binaryString.substring(24, 32);
			
			System.out.println("Instruction fetched: " + byte1 + " " + byte2 + " " + byte3 + " " + byte4);
			System.out.println("Instruction fetched: " + newInstruction);
			System.out.println("PC: " + currentPC);

			int instruct = IF_OF_Latch.getInstruction();
			System.out.println("INSTRUCTION AVAILABLE IN IF-OF LATCH" + instruct);
			//DEBUGGING SECTION ENDS

			// containingProcessor.setStall(false);
			IF_OF_Latch.setOF_stall(false);
			// IF_OF_Latch.setOF_busy(true);
			IF_EnableLatch.setIF_busy(false);
			// IF_OF_Latch.setNop(false);
		}
	}


}
