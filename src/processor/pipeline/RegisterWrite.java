package processor.pipeline;

import generic.Simulator;
import generic.Simulator;
import processor.Processor;

public class RegisterWrite {
	Processor containingProcessor;
	MA_RW_LatchType MA_RW_Latch;
	IF_EnableLatchType IF_EnableLatch;
	Simulator simulator;
	
	public RegisterWrite(Processor containingProcessor, MA_RW_LatchType mA_RW_Latch, IF_EnableLatchType iF_EnableLatch, Simulator simulator)
	{
		this.containingProcessor = containingProcessor;
		this.MA_RW_Latch = mA_RW_Latch;
		this.IF_EnableLatch = iF_EnableLatch;
		this.simulator = simulator;
	}
	
	public void performRW()
	{
		if(MA_RW_Latch.isRW_stall())
		{
			//System.out.println("STALL due to IF MemoryFetch Event");
			MA_RW_Latch.setCurrent_rd(MA_RW_Latch.getNext_rd());
			return;
		}

		if(MA_RW_Latch.isRW_busy())
		{
			//System.out.println("RW IS BUSY");
			return;
		}
		
		if(MA_RW_Latch.isRW_enable())
		{
			MA_RW_Latch.setCurrent_rd(MA_RW_Latch.getNext_rd());
			if(MA_RW_Latch.isNop())
			{
				System.out.println("nop in RW");
				MA_RW_Latch.setNop(false);
				MA_RW_Latch.setCurrent_rd(MA_RW_Latch.getNext_rd());
				return;
			}
			//System.out.println("PERFORMING RW");
			
			//PRINTING THE INSTRUCTION & PC FOR DEBUGGING
			int newInstruction = MA_RW_Latch.getInstruction();

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
			// System.out.println("PC: " + currentPC);
			//DEBUGGING SECTION ENDS

			//TODO
			int instruction = MA_RW_Latch.getInstruction();
			int opcode = instruction >>> 27;
			int result = 0;
			int rd;

			// if instruction being processed is an end instruction, remember to call Simulator.setSimulationComplete(true);
			//THE ABOVE THING OF CALLING Simulator.setSimulationComplete(true) IS DONE IN EXECUTE.performEX()
			if(opcode == 29) //end INSTRUCTION
			{
				System.out.println("Executing: end");
				int currentPC = MA_RW_Latch.getCurrentPC();
				containingProcessor.getRegisterFile().setProgramCounter(currentPC + 1);
				Simulator.setSimulationComplete(true);
				return;
			}
		
			
			
			if(opcode ==22) //load INSTRUCTION
			{
				result = MA_RW_Latch.getLdResult();
				//THE rd FOR load INSTRUCTION IS STORED IN instruction[11:15]
				rd = (instruction << 10 & 0xf8000000) >>> 27;
				containingProcessor.getRegisterFile().setValue(rd, result);
			}
			else //THIS MEANS alu RESULT IS TO BE WRITTEN BACK
			{
				result = MA_RW_Latch.getAluResult();
				System.out.println("RegisterWrite: opcode = " + opcode);
				if (opcode >= 0 && opcode <= 21) {
					if(opcode % 2 == 0) //R3 TYPE
					{
						//FOR R3-TYPE, THE rd FOR alu INSTRUCTION IS STORED IN instruction[16:20]
						rd = (instruction << 15 & 0xf8000000) >>> 27;
						containingProcessor.getRegisterFile().setValue(rd, result);
					}
					else //R2i TYPE
					{
						//FOR R2i-TYPE, THE rd FOR alu INSTRUCTION IS STORED IN instruction[11:15]
						rd = (instruction << 10 & 0xf8000000) >>> 27;
						containingProcessor.getRegisterFile().setValue(rd, result);
					}
				}
			}

			//PRINTING REGISTER CONTENT
			/*
			String registerContent = containingProcessor.getRegisterFile().getContentsAsString();
			System.out.println("registerContent:");
			System.out.println(registerContent);
			*/

			// MA_RW_Latch.setRW_enable(false);
			// IF_EnableLatch.setIF_enable(true);
		}
	}

}
