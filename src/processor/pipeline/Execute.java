package processor.pipeline;

import processor.Processor;
import generic.Simulator;

import generic.Element;
import generic.Event;
import generic.ExecutionCompleteEvent;
import processor.Clock;
import configuration.Configuration;

public class Execute {
	Processor containingProcessor;
	OF_EX_LatchType OF_EX_Latch;
	EX_MA_LatchType EX_MA_Latch;
	EX_IF_LatchType EX_IF_Latch;

	//FOR PIPELINING - START
	IF_EnableLatchType IF_EnableLatch;
	IF_OF_LatchType IF_OF_Latch;
	//FOR PIPELINING - END

	int isWriteBack;
	int isR2i;
	int isR3;

	public Execute(Processor containingProcessor, OF_EX_LatchType oF_EX_Latch, EX_MA_LatchType eX_MA_Latch, EX_IF_LatchType eX_IF_Latch, IF_EnableLatchType iF_EnableLatch, IF_OF_LatchType iF_OF_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.OF_EX_Latch = oF_EX_Latch;
		this.EX_MA_Latch = eX_MA_Latch;
		this.EX_IF_Latch = eX_IF_Latch;

		//FOR PIPELINING - START
		this.IF_EnableLatch = iF_EnableLatch;
		this.IF_OF_Latch = iF_OF_Latch;
		//FOR PIPELINING - END

		this.isWriteBack = 0;
		this.isR2i = 0;
		this.isR3 = 0;
	}
	
	public void performEX()
	{
		if(OF_EX_Latch.isEX_stall())
		{
			EX_MA_Latch.setMA_stall(true);
			// OF_EX_Latch.setCurrent_rd(-1);
			OF_EX_Latch.setCurrent_rd(OF_EX_Latch.getNext_rd());

			//Setting the current_rd of EX from THIS CYCLE as the next_rd of MA for NEXT CYCLE  
			EX_MA_Latch.setNext_rd(OF_EX_Latch.getCurrent_rd());
			return;
		}
		else
		{
			EX_MA_Latch.setMA_stall(false);
		}

		if(!EX_MA_Latch.isMA_busy())
		{
			OF_EX_Latch.setEX_busy(false);
		}

		if(OF_EX_Latch.isEX_busy())
		{
			return;
		}

		if(OF_EX_Latch.isEX_enable())
		{
			OF_EX_Latch.setCurrent_rd(OF_EX_Latch.getNext_rd());

			EX_MA_Latch.setNext_rd(OF_EX_Latch.getCurrent_rd());


			if(OF_EX_Latch.isNop())
			{
				System.out.println("nop in EX");

				EX_MA_Latch.setNop(true);
				//EX_MA_Latch.setRd(OF_EX_Latch.getRd());

				OF_EX_Latch.setNop(false);

				OF_EX_Latch.setCurrent_rd(OF_EX_Latch.getNext_rd());
				EX_MA_Latch.setNext_rd(OF_EX_Latch.getCurrent_rd());
				return;
			}
			//System.out.println("PERFORMING EX");
			//EX_MA_Latch.setRd(OF_EX_Latch.getRd());

			//STATISTICS
			EX_IF_Latch.incrementNumberOfInstructions();

			//TODO
			int instruction = OF_EX_Latch.getInstruction();
			int op1 = OF_EX_Latch.getOp1();
			//op2 contains rs2 (incase of R3 type) & rd (incase of R2i type)
			int op2 = OF_EX_Latch.getOp2();
			int immx = OF_EX_Latch.getImmx();
			//immx CONTAINS immediate value FOR R2i TYPE INSTRUCTIONS

			//EXTRACTING OPCODE
			int opcode = instruction >>> 27;

			//BRACH-UNIT
			//HANDLING BRANCH & JMP INSTRUCTIONS
			int branchTarget = OF_EX_Latch.getBranchTarget();
			int isBranchTaken = 0;

			switch (opcode)
			{
			case 24:
				System.out.println("Executing: jmp");
				//jmp INSTRUCTION (R2i TYPE)
				EX_IF_Latch.setBranchPC(branchTarget);
				isBranchTaken = 1;
				EX_IF_Latch.setIsBranchTaken(isBranchTaken);

				//GO TO InstructionFetch STAGE
				EX_IF_Latch.setIF_enable(true);
				// OF_EX_Latch.setEX_enable(false);
				// EX_MA_Latch.setMA_enable(false);

				//CONVERT INSTRUCTION IN IF & OF STAGE TO nop
				IF_EnableLatch.setNop(true);
				IF_OF_Latch.setNop(true);
				return;
			case 25:
				System.out.println("Executing: beq");
				//beq INSTRUCTION
				if(op1 == op2)
				{
					//SET isBrnachTaken as 1 AND THE branchPC as branchTarget
					EX_IF_Latch.setBranchPC(branchTarget);
					isBranchTaken = 1;
					EX_IF_Latch.setIsBranchTaken(isBranchTaken);

					//GO TO InstructionFetch STAGE
					EX_IF_Latch.setIF_enable(true);
					// OF_EX_Latch.setEX_enable(false);
					// EX_MA_Latch.setMA_enable(false);
					
					//CONVERT INSTRUCTION IN IF & OF STAGE TO nop
					IF_EnableLatch.setNop(true);
					IF_OF_Latch.setNop(true);
				}
				return;
			case 26:
				System.out.println("Executing: bne");
				//bne INSTRUCTION
				if(op1 != op2)
				{
					//SET isBrnachTaken as 1 AND THE branchPC as branchTarget
					EX_IF_Latch.setBranchPC(branchTarget);
					isBranchTaken = 1;
					EX_IF_Latch.setIsBranchTaken(isBranchTaken);

					//GO TO InstructionFetch STAGE
					EX_IF_Latch.setIF_enable(true);
					// OF_EX_Latch.setEX_enable(false);
					// EX_MA_Latch.setMA_enable(false);
					
					//CONVERT INSTRUCTION IN IF & OF STAGE TO nop
					IF_EnableLatch.setNop(true);
					IF_OF_Latch.setNop(true);

				}
				return;
			case 27:
				System.out.println("Executing: blt");
				//blt INSTRUCTION
				if(op1 < op2)
				{
					//SET isBrnachTaken as 1 AND THE branchPC as branchTarget
					EX_IF_Latch.setBranchPC(branchTarget);
					isBranchTaken = 1;
					EX_IF_Latch.setIsBranchTaken(isBranchTaken);

					//GO TO InstructionFetch STAGE
					EX_IF_Latch.setIF_enable(true);
					// OF_EX_Latch.setEX_enable(false);
					// EX_MA_Latch.setMA_enable(false);

					//CONVERT INSTRUCTION IN IF & OF STAGE TO nop
					IF_EnableLatch.setNop(true);
					IF_OF_Latch.setNop(true);

				}
				return;
			case 28:
				System.out.println("Executing: bgt");
				// bgt INSTRUCTION
				if (op1 > op2) {
					// If the condition is true, branch is taken
					// Set the branch target and flag
					EX_IF_Latch.setBranchPC(branchTarget);
					isBranchTaken = 1;
					EX_IF_Latch.setIsBranchTaken(isBranchTaken);
			
					// Enable Instruction Fetch stage and disable the current stages
					EX_IF_Latch.setIF_enable(true);  // Start Instruction Fetch
					// OF_EX_Latch.setEX_enable(false); // Disable Execute stage
					// EX_MA_Latch.setMA_enable(false); // Disable Memory Access stage
			
					//CONVERT INSTRUCTION IN IF & OF STAGE TO nop
					IF_EnableLatch.setNop(true);
					IF_OF_Latch.setNop(true);

				}
				return;
			
			default:
				break;
			}
			int AluResult = -1;
			switch(opcode)
			{
			case 0:
				System.out.println("Executing: add");
				//add instruction
				AluResult = op1 + op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 1:
				System.out.println("Executing: addi");
				//addi instruction
				AluResult = op1 + immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 2:
				System.out.println("Executing: sub");
				//sub instruction
				AluResult = op1 - op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 3:
				System.out.println("Executing: subi");
				//subi instruction
				AluResult = op1 - immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 4:
				System.out.println("Executing: mul");
				//mul instruction
				AluResult = op1 * op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.multiplier_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 5:
				System.out.println("Executing: muli");
				//muli instruction
				EX_MA_Latch.setAluResult(op1 * immx);
				AluResult = op1 * immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.multiplier_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 6:
				System.out.println("Executing: div");
				//div instruction
				AluResult = op1 / op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.divider_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);

				int rem = op1 % op2;
				if(OF_EX_Latch.isNop() == false)
				{
					containingProcessor.getRegisterFile().setValue(31, rem);
				}
				break;
			case 7:
				System.out.println("Executing: divi");
				// divi instruction
				AluResult = op1 / immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.divider_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);

				System.out.println("op1: " + op1);
				System.out.println("immx: " + immx);
				System.out.println("Result: " + AluResult);


				int rem1 = op1 % immx;
				if(OF_EX_Latch.isNop() == false)
				{
					containingProcessor.getRegisterFile().setValue(31, rem1);
				}
				break;
			case 8:
				System.out.println("Executing: and");
				//and instruction
				AluResult = op1 & op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 9:
				System.out.println("Executing: andi");
				//andi instruction
				AluResult = op1 & immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 10:
				System.out.println("Executing: or");
				//or instruction
				AluResult = op1 | op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 11:
				System.out.println("Executing: ori");
				//ori instruction
				AluResult = op1 | immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 12:
				System.out.println("Executing: xor");
				//xor instruction
				AluResult = op1 ^ op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 13:
				System.out.println("Executing: xori");
				//xori instruction
				AluResult = op1 ^ immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 14:
				System.out.println("Executing: slt");
				//slt instruction
				AluResult = op1 < op2 ? 1 : 0;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 15:
				System.out.println("Executing: slti");
				//slti instruction
				AluResult = op1 < immx ? 1 : 0;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 16:
				System.out.println("Executing: sll");
				//sll instruction
				AluResult = op1 << op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 17:
				System.out.println("Executing: slli");
				//slli instruction
				AluResult = op1 << immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 18:
				System.out.println("Executing: srl");
				//srl instruction
				AluResult = op1 >>> op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 19:
				System.out.println("Executing: srli");
				//srli instruction
				AluResult = op1 >>> immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 20:
				System.out.println("Executing: sra");
				//sra instruction
				AluResult = op1 >> op2;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			case 21:
				System.out.println("Executing: srai");
				//srai instruction
				AluResult = op1 >> immx;
				Simulator.getEventQueue().addEvent(
					new ExecutionCompleteEvent(
						Clock.getCurrentTime() + Configuration.ALU_latency,
						this,
						this,
						AluResult
					)
				);
				OF_EX_Latch.setEX_busy(true);
				break;
			default:
				break;
			}

			//PAASING op1, op2 AND immx to EX_MA_Latch
			EX_MA_Latch.setOp1(op1);
			EX_MA_Latch.setOp2(op2);
			EX_MA_Latch.setImmx(immx);
			//EX_MA_Latch.setWriteBack(isWriteBack);
			// EX_MA_Latch.setR2i(isR2i);
			// EX_MA_Latch.setR3(isR3);

			//FIXING LATCHES
			EX_MA_Latch.setInstruction(instruction);
			EX_MA_Latch.setCurrentPC(OF_EX_Latch.getCurrentPC());
			// OF_EX_Latch.setEX_enable(false);
			EX_MA_Latch.setMA_enable(true);
			if(EX_MA_Latch.isMA_busy())
			{
				OF_EX_Latch.setEX_busy(true);
			}	

			OF_EX_Latch.setEX_enable(false);
		}
	}

	@Override
	public void handleEvent(Event e) {
		System.out.println("\nExecute.handleEvent\n");

		if (OF_EX_Latch.isEX_busy()) {
			System.out.println("EX IS BUSY");

			e.setEventTime(Clock.getCurrentTime() + 1);
			Simulator.getEventQueue().addEvent(e);
		}
		else {
			System.out.println("EX NOT BUSY");

			ExecutionCompleteEvent event = (ExecutionCompleteEvent) e;

			//PRINTING THE INSTRUCTION & PC FOR DEBUGGING
			int newInstruction = OF_EX_Latch.getInstruction();

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

			int AluResult = event.getAluResult();
			EX_MA_Latch.setAluResult(AluResult);
			
			// OF_EX_Latch.setEX_busy(false);
			EX_MA_Latch.setMA_busy(false);

			//FOR ACCOUNTING - MEMORY LATENCY - START
			OF_EX_Latch.setEX_enable(true);
			//FOR ACCOUNTING - MEMORY LATENCY - END
		}

	}


}