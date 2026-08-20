package processor.pipeline;

import processor.Processor;

public class OperandFetch {
	Processor containingProcessor;
	IF_OF_LatchType IF_OF_Latch;
	OF_EX_LatchType OF_EX_Latch;
	
	//FOR PIPELINING - START
	IF_EnableLatchType IF_EnableLatch;
	EX_MA_LatchType EX_MA_Latch;
	MA_RW_LatchType MA_RW_Latch;
	//FOR PIPELINING - END
	

	public OperandFetch(Processor containingProcessor, IF_OF_LatchType iF_OF_Latch, OF_EX_LatchType oF_EX_Latch, IF_EnableLatchType iF_EnableLatch, EX_MA_LatchType eX_MA_Latch, MA_RW_LatchType mA_RW_Latch)
	{
		this.containingProcessor = containingProcessor;
		this.IF_OF_Latch = iF_OF_Latch;
		this.OF_EX_Latch = oF_EX_Latch;

		//FOR PIPELINING - START
		this.IF_EnableLatch = iF_EnableLatch;
		this.EX_MA_Latch = eX_MA_Latch;
		this.MA_RW_Latch = mA_RW_Latch;
		//FOR PIPELINING - END

	}
	
	public void performOF()
	{
		System.out.println("\nPERFORMING OF");

		if(IF_OF_Latch.isOF_stall())
		{
			//System.out.println("STALL due to IF MemoryFetch Event");
			OF_EX_Latch.setEX_stall(true);
			return;
		}
		else
		{
			//System.out.println("NO STALL");
			OF_EX_Latch.setEX_stall(false);
		}

		if(OF_EX_Latch.isEX_busy())
		{
			//IF_OF_Latch.setOF_busy(true);
			System.out.println("OF IS BUSY");

			return;
		}
		else
		{
			//System.out.println("OF NOT BUSY");
			IF_OF_Latch.setOF_busy(false);
		}
		
		if(IF_OF_Latch.isOF_enable())
		{

			if(IF_OF_Latch.isNop())
			{
				System.out.println("nop in OF");

				OF_EX_Latch.setNop(true);
				IF_OF_Latch.setNop(false);
				return;
			}

			//TODO
			//FETCHING currentPC & INSTRUCTION
			// int currentPC = containingProcessor.getRegisterFile().getProgramCounter();
			int currentPC = IF_OF_Latch.getCurrentPC();
			int instruction = IF_OF_Latch.getInstruction();
			OF_EX_Latch.setInstruction(instruction);

			//PRINTING THE INSTRUCTION & PC FOR DEBUGGING
			int instruct = IF_OF_Latch.getInstruction();

			System.out.println("INSTRUCTION AVAILABLE IN IF-OF LATCH" + instruct);

			String binaryString = Integer.toBinaryString(instruction);
			while(binaryString.length() < 32)
			{
				binaryString = "0" + binaryString;
			}
			String byte1 = binaryString.substring(0, 8);
			String byte2 = binaryString.substring(8, 16);
			String byte3 = binaryString.substring(16, 24);
			String byte4 = binaryString.substring(24, 32);
			
			System.out.println("Instruction in OF: " + byte1 + " " + byte2 + " " + byte3 + " " + byte4);
			System.out.println("Instruction: " + instruction);
			System.out.println("PC: " + currentPC);
			//DEBUGGING SECTION ENDS

			//
			//CALCULATING IMMEDIATE VALUE AS immx
			int immx = instruction & 0x0001ffff;
			OF_EX_Latch.setImmx(immx);

			//CALCULATING branchTarget = currentPC + offset
			int offset=0;
			int isJmp = 0;
			int opcode = instruction >>> 27;

			//HANDLING - END - DISABLING IF & OF - STARTS
			switch (opcode)
			{
				case 29:
					System.out.println("HANDLING: end");
					// end INSTRUCTION
					// Simulator.setSimulationComplete(true);
					IF_EnableLatch.setIF_enable(false);
					IF_OF_Latch.setOF_enable(false);
					// OF_EX_Latch.setEX_enable(false);
				default:
					break;
			}
			//HANDLING - END - DISABLING IF & OF - ENDS

			isJmp = (opcode == 24) ? 1 : 0;

			if(isJmp == 1)
			{
				// System.out.println("Executing: jump");
				//registerNumber IS STORED IN instruction[6:10]
				int registerNumber = (instruction << 5 & 0xf8000000) >>> 27;
				int isRegister =  (registerNumber == 0) ? 0 : 1;
				// System.out.println("Register number: " + registerNumber);

				if(isRegister == 1)
				{
					// System.out.println("isRegister");
					offset = containingProcessor.getRegisterFile().getValue(registerNumber);
				} else {
					//offset IS STORED IN instruction[11:32]
					// System.out.println("isImmediate");
					offset = instruction & 0x003fffff;
					offset = offset << 10 >> 10;
				}
			} else {
				// System.out.println("Executing: branch");
				offset = (instruction & 0x0001ffff) << 15 >> 15;
			}
			// System.out.println("Offset: " + offset);

			//CALCULATING branchTarget = currentPC + offset
			int branchTarget = currentPC + offset;
			// System.out.println("currentPC = " + currentPC);
			// System.out.println("Branch Target: " + branchTarget);

			OF_EX_Latch.setBranchTarget(branchTarget);
			OF_EX_Latch.setCurrentPC(currentPC);
			//CALCULATING THE REGISTER NUMBERS rs1 & rs2 FROM instruction[6:10] & instruction[11:15] RESPECTIVELY
			int rs1 = 0;
			rs1 = (instruction << 5 & 0xf8000000) >>> 27;

			int rs2 = 0;
			rs2 = (instruction << 10 & 0xf8000000) >>> 27;	

			// System.out.println("rs1 = " + rs1 + " rs2 = " + rs2);
			
			//FETCHING THE VALUES OF THE REGISTERS rs1 & rs2 AS op1 & op2 RESPECTIVELY
			int op1 = containingProcessor.getRegisterFile().getValue(rs1);
			OF_EX_Latch.setOp1(op1);

			int op2 = containingProcessor.getRegisterFile().getValue(rs2);
			OF_EX_Latch.setOp2(op2);

			System.out.println("op1 = " + op1 + " op2 = " + op2);

			//CORRECTING RS1 AND RS2 FOR DATA HAZARD
			//R3-TYPE: rs1: [1:5], rs2: [6:10] and rd: [11:15]
			//R2I-TYPE: rs1: [1:5], rd: [6:10] from (opcode = 0) to (opcode = 22, load) for store(both are source registers)

			if(opcode >= 0 && opcode <= 22)
			{
				if(opcode % 2 == 1)
				{
					rs2 = -2;
				}
			}

			System.out.println("rd in EX: " + OF_EX_Latch.getCurrent_rd());
			System.out.println("rd in MA: " + EX_MA_Latch.getCurrent_rd());
			System.out.println("rd in RW: " + MA_RW_Latch.getCurrent_rd());
			System.out.println("rs1 in OF: " + rs1);
			System.out.println("rs2 in OF: " + rs2);

			//DATA HAZARD - STALL OF - STARTS
			int rd = -1;

			OF_EX_Latch.setCurrent_rd(OF_EX_Latch.getNext_rd());
			if (OF_EX_Latch.getCurrent_rd() == rs1 || OF_EX_Latch.getCurrent_rd() == rs2)
			{
				System.out.println("rd in EX = rs1/rs2 in OF");
				
				OF_EX_Latch.setNop(true);
				// IF_OF_Latch.setOF_enable(false);
				IF_EnableLatch.setIF_enable(false);

				// OF_EX_Latch.setRd(-1);
				//FOR STOPPING IF EVENT EXECUTION WHILE OF IS HANDLING A DATA HAZARD
				IF_OF_Latch.setOF_busy(true);

				//STATISTICS
				OF_EX_Latch.incrementNumberOfCyclesOfOFStalls();
			} 
			else if (EX_MA_Latch.getCurrent_rd() == rs1 || EX_MA_Latch.getCurrent_rd() == rs2)
			{
				System.out.println("rd in MA = rs1/rs2 in OF");

				OF_EX_Latch.setNop(true);
				// IF_OF_Latch.setOF_enable(false);
				IF_EnableLatch.setIF_enable(false);

				//FOR STOPPING IF EVENT EXECUTION WHILE OF IS HANDLING A DATA HAZARD
				IF_OF_Latch.setOF_busy(true);

				//STATISTICS
				OF_EX_Latch.incrementNumberOfCyclesOfOFStalls();

			}
			else if (MA_RW_Latch.getCurrent_rd() == rs1 || MA_RW_Latch.getCurrent_rd() == rs2)
			{
				System.out.println("rd in RW = rs1/rs2 in OF");

				OF_EX_Latch.setNop(true);
				// IF_OF_Latch.setOF_enable(false);
				IF_EnableLatch.setIF_enable(false);

				//FOR STOPPING IF EVENT EXECUTION WHILE OF IS HANDLING A DATA HAZARD
				IF_OF_Latch.setOF_busy(true);

				//STATISTICS
				OF_EX_Latch.incrementNumberOfCyclesOfOFStalls();
			}
			else
			{
				System.out.println("No rd src conflict");

				if(IF_EnableLatch.isIF_enable() == false)
				{
					//STATISTICS
					// System.out.println("INCREMENTING NUMBER OF DATA HAZARDS");
					OF_EX_Latch.incrementNumberOfDataHazards();
				}

				IF_EnableLatch.setIF_enable(true);
				OF_EX_Latch.setNop(false);
				// OF_EX_Latch.setRd(-1);

				//FOR STOPPING IF EVENT EXECUTION WHILE OF IS HANDLING A DATA HAZARD
				IF_OF_Latch.setOF_busy(false);
			}
			
			if(opcode== 22) //load INSTRUCTIONS
			{
				// result = MA_RW_Latch.getLdResult();
				//THE rd FOR load INSTRUCTION IS STORED IN instruction[11:15]
				rd = (instruction << 10 & 0xf8000000) >>> 27;
				// containingProcessor.getRegisterFile().setValue(rd, result);
			}
			else if(opcode == 24)
			{
				//jmp INSTRUCTIONS
				//THE rd FOR jmp INSTRUCTION IS STORED IN instruction[6:10]
				rd = (instruction << 5 & 0xf8000000) >>> 27;
			}
			else //THIS MEANS alu RESULT IS TO BE WRITTEN BACK
			{
				// result = MA_RW_Latch.getAluResult();
				// System.out.println("RegisterWrite: opcode = " + opcode);
				if (opcode >= 0 && opcode <= 21) {
					if(opcode % 2 == 0) //R3 TYPE
					{
						//FOR R3-TYPE, THE rd FOR alu INSTRUCTION IS STORED IN instruction[16:20]
						rd = (instruction << 15 & 0xf8000000) >>> 27;
						// containingProcessor.getRegisterFile().setValue(rd, result);
					}
					else //R2i TYPE
					{
						//FOR R2i-TYPE, THE rd FOR alu INSTRUCTION IS STORED IN instruction[11:15]
						rd = (instruction << 10 & 0xf8000000) >>> 27;
						// containingProcessor.getRegisterFile().setValue(rd, result);
					}
				}
			}
			//DATA HAZARD (RAW) IDENTIFICATION - ENDS

			if(OF_EX_Latch.isNop())
			{
				System.out.println("nop in EX - setting Rd for EX as -1");
				OF_EX_Latch.setNext_rd(-1);
			}
			else
			{
				OF_EX_Latch.setNext_rd(rd);
			}


			//SETTING ENABLE LATCHES
			// IF_OF_Latch.setOF_enable(false);
			if(opcode == 29) {
				IF_EnableLatch.setIF_enable(false);
			}
			OF_EX_Latch.setEX_enable(true);

			// IF_OF_Latch.setOF_enable(false);
		}
	}

}
