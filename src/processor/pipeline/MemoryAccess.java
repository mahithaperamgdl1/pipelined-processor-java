package processor.pipeline;
import processor.Processor;

//TO ACCESS MainMemory & RegisterFile
// import processor.memorysystem.MainMemory;
// import processor.pipeline.RegisterFile;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.MemoryWriteEvent;
import generic.Event;
import generic.Simulator;
import generic.Element;

import processor.Clock;
import configuration.Configuration;

public class MemoryAccess implements Element {
	Processor containingProcessor;
	public EX_MA_LatchType EX_MA_Latch;
	public MA_RW_LatchType MA_RW_Latch;
	int ldResult;

	public MemoryAccess(Processor containingProcessor, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
		this.ldResult = 0;
	}
	
	public void performMA()
	{
		System.out.println("\nPERFORMING MA");

		if(EX_MA_Latch.isMA_stall())
		{
			//System.out.println("STALL due to IF MemoryFetch Event");
			MA_RW_Latch.setRW_stall(true);
			//Setting the next_rd of MA from PREV CYCLE as the current_rd of MA for THIS CYCLE  
			EX_MA_Latch.setCurrent_rd(EX_MA_Latch.getNext_rd());

			//Setting the current_rd of MA from THIS CYCLE as the next_rd of RW for NEXT CYCLE  
			MA_RW_Latch.setNext_rd(EX_MA_Latch.getCurrent_rd());
			return;
		}
		else
		{
			//System.out.println("NO STALL");
			MA_RW_Latch.setRW_stall(false);
		}

		if(EX_MA_Latch.isMA_busy())
		{
			//System.out.println("MA IS BUSY");
			return;
		}

		if(EX_MA_Latch.isMA_enable())
		{
			System.out.println("MA is ENABLED");

			//Setting the next_rd of MA from PREV CYCLE as the current_rd of MA for THIS CYCLE  
			EX_MA_Latch.setCurrent_rd(EX_MA_Latch.getNext_rd());

			//Setting the current_rd of MA from THIS CYCLE as the next_rd of RW for NEXT CYCLE  
			MA_RW_Latch.setNext_rd(EX_MA_Latch.getCurrent_rd());


			if(EX_MA_Latch.isNop())
			{
				System.out.println("nop in MA");
				MA_RW_Latch.setNop(true);

				EX_MA_Latch.setNop(false);

				//Setting the next_rd of MA from PREV CYCLE as the current_rd of MA for THIS CYCLE  
				EX_MA_Latch.setCurrent_rd(EX_MA_Latch.getNext_rd());

				//Setting the current_rd of MA from THIS CYCLE as the next_rd of RW for NEXT CYCLE  
				MA_RW_Latch.setNext_rd(EX_MA_Latch.getCurrent_rd());
				return;
			}
			
			

			//PRINTING THE INSTRUCTION & PC FOR DEBUGGING
			int newInstruction = EX_MA_Latch.getInstruction();

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
			//GETTING THE INSTRUCTION & ALU RESULT FROM EX_MA_Latch
			int instruction = EX_MA_Latch.getInstruction();
			int AluResult = EX_MA_Latch.getAluResult();
			int op1 = EX_MA_Latch.getOp1();
			int op2 = EX_MA_Latch.getOp2();
			int immx = EX_MA_Latch.getImmx();

			//IMPLEMENTING load & store BOTH R2i-TYPE, rs1 STORED IN op1, immediate STORED IN immx AND rd IS STORED IN 	op2
			int rd = op2;
			int rs1 = op1;
			//NOTE HERE rs1 CONTAINS THE VALUE OF THE REGISTER rs1
			//AND rd CONTAINS THE VALUE OF THE REGISTER rd

			//EXTRACTING OPCODE
			int opcode = instruction >>> 27;
			switch (opcode) {
			case 22: //load
				System.out.println("Executing: load");
				// rd = word at [rs1 + imm]
				int address = rs1 + immx;
				// int value = containingProcessor.getMainMemory().getWord(address);
				// ldResult = value;

				//ADDING MEMORY READ EVENT - START
				System.out.println(Configuration.mainMemoryLatency);
				System.out.println("ADDRESS GIVEN TO MemoryReadEvent: " + address);

				Simulator.getEventQueue().addEvent(
					new MemoryReadEvent(
						Clock.getCurrentTime() + Configuration.mainMemoryLatency,
						this,  
						containingProcessor.getMainMemory(),
						address
					)
				);
				//ADDING MEMORY READ EVENT - END
				EX_MA_Latch.setMA_busy(true);
				MA_RW_Latch.setRW_busy(true);


				break;

			case 23: //store
				System.out.println("Executing: store");
				// word at [rd + imm] = op1 (ie value of rs1)
				int address2 = rd + immx;
				// containingProcessor.getMainMemory().setWord(address2, rs1);

				//ADDING MEMORY WRITE EVENT - START
				System.out.println(Configuration.mainMemoryLatency);
				System.out.println("ADDRESS GIVEN TO MemoryWriteEvent: " + address2);

				Simulator.getEventQueue().addEvent(
					new MemoryWriteEvent(
						Clock.getCurrentTime() + Configuration.mainMemoryLatency,
						this,  
						containingProcessor.getMainMemory(),
						address2,
						op1
					)
				);
				//ADDING MEMORY WRITE EVENT - END
				EX_MA_Latch.setMA_busy(true);
				MA_RW_Latch.setRW_busy(true);


				break;

			default:
				// System.out.println("Instruction not supported");
				break;
			}

			//PASSING ldResult & AluResult TO MA_RW_Latch
			// MA_RW_Latch.setLdResult(ldResult);
			MA_RW_Latch.setAluResult(AluResult);

			//SETTING UP MA_RW_Latch FOR EXECUTION OF RW STAGE
			MA_RW_Latch.setInstruction(instruction);
			MA_RW_Latch.setCurrentPC(EX_MA_Latch.getCurrentPC());
			// EX_MA_Latch.setMA_enable(false);
			MA_RW_Latch.setRW_enable(true);

			//FOR ACCOUNTING - MEMORY LATENCY - START
			// EX_MA_Latch.setMA_enable(false);
			//FOR ACCOUNTING - MEMORY LATENCY - END


			containingProcessor.setStall(true);
		}
	}

	@Override
	public void handleEvent(Event e) {
		System.out.println("\nMemoryAccess.handleEvent\n");

		if (EX_MA_Latch.isMA_busy()) {
			e.setEventTime(Clock.getCurrentTime() + 1);
			Simulator.getEventQueue().addEvent(e);
		}
		else {
			MemoryResponseEvent event = (MemoryResponseEvent) e;
			// IF_EnableLatch.setIF_busy(false);

			//PRINTING THE INSTRUCTION & PC FOR DEBUGGING
			int newInstruction = EX_MA_Latch.getInstruction();

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
			//GETTING THE INSTRUCTION & ALU RESULT FROM EX_MA_Latch
			int instruction = EX_MA_Latch.getInstruction();
			// int AluResult = EX_MA_Latch.getAluResult();
			// int op1 = EX_MA_Latch.getOp1();
			// int op2 = EX_MA_Latch.getOp2();
			// int immx = EX_MA_Latch.getImmx();

			//IMPLEMENTING load & store BOTH R2i-TYPE, rs1 STORED IN op1, immediate STORED IN immx AND rd IS STORED IN 	op2
			// int rd = op2;
			// int rs1 = op1;
			//NOTE HERE rs1 CONTAINS THE VALUE OF THE REGISTER rs1
			//AND rd CONTAINS THE VALUE OF THE REGISTER rd

			//EXTRACTING OPCODE
			int opcode = instruction >>> 27;
			//THIS SWITCH CASE IS NOT REACHABLE BECAUSE INSTRUCTION CHANGES DUE TO EX 
			switch (opcode) {
			case 22: //load
				System.out.println("Executing: load");
				// rd = word at [rs1 + imm]
				// int address = rs1 + immx;
				int value = event.getValue();
				ldResult = value;
				EX_MA_Latch.setMA_busy(false);
				MA_RW_Latch.setRW_busy(false);

				break;
			case 23: //store
				// System.out.println("Executing: store");
				// // word at [rd + imm] = rs1
				// int address2 = rd + immx;
				// containingProcessor.getMainMemory().setWord(address2, rs1);
				// break;
			default:
				// System.out.println("Instruction not supported");
				break;
			}

			System.out.println("Executing: load");
			// rd = word at [rs1 + imm]
			// int address = rs1 + immx;
			int value = event.getValue();
			ldResult = value;

			//PASSING ldResult & AluResult TO MA_RW_Latch
			MA_RW_Latch.setLdResult(ldResult);
			// MA_RW_Latch.setAluResult(AluResult);

			MA_RW_Latch.setRW_enable(true);
			// IF_OF_Latch.setNop(false);
			MA_RW_Latch.setRW_busy(false);
		}


			
	}

}