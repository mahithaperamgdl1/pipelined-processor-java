package processor.pipeline;

public class OF_EX_LatchType {
	
	boolean EX_enable;
	int instruction;
	int branchTarget;
	int immx;
	int op1;
	int op2;
	int currentPC;

	public int getCurrentPC() {
		return currentPC;
	}

	public void setCurrentPC(int currentPC) {
		this.currentPC = currentPC;
	}

	//MODIFICATIONS FOR PIPELINING - STARTS
	boolean nop;
	int current_rd=-1;
	int next_rd=-1;
	int numberOfDataHazards = 0;
	int numberOfCyclesOfOFStalls = 0;

	public boolean isNop() {
		return nop;
	}

	public void setNop(boolean nop) {
		this.nop = nop;
	}

	public int getCurrent_rd() {
		return current_rd;
	}

	public void setCurrent_rd(int Current_rd) {
		this.current_rd = Current_rd;
	}	

	public int getNext_rd() {
		return next_rd;
	}

	public void setNext_rd(int Next_rd) {
		this.next_rd = Next_rd;
	}

	public int getNumberOfDataHazards() {
		return numberOfDataHazards;
	}

	public void incrementNumberOfDataHazards() {
		this.numberOfDataHazards += 1;
	}

	public int getNumberOfCyclesOfOFStalls() {
		return numberOfCyclesOfOFStalls;
	}

	public void incrementNumberOfCyclesOfOFStalls() {
		this.numberOfCyclesOfOFStalls += 1;
	}
	//MODIFICATIONS FOR PIPELINING - ENDS
	
	boolean EX_busy;

	public boolean isEX_busy() {
		return EX_busy;
	}

	public void setEX_busy(boolean eX_busy) {
		EX_busy = eX_busy;
	}

	boolean EX_stall;

	public boolean isEX_stall() {
		return EX_stall;
	}

	public void setEX_stall(boolean eX_stall) {
		EX_stall = eX_stall;
	}

	public OF_EX_LatchType()
	{
		EX_enable = false;
		EX_busy = false;
		EX_stall = false;
	}

	public boolean isEX_enable() {
		return EX_enable;
	}

	public void setEX_enable(boolean eX_enable) {
		EX_enable = eX_enable;
	}

	public int getInstruction() {
		return instruction;
	}

	public void setInstruction(int instruction) {
		this.instruction = instruction;
	}

	public int getImmx() {
		return immx;
	}

	public void setImmx(int immx) {
		this.immx = immx;
	}

	public int getBranchTarget() {
		return branchTarget;
	}

	public void setBranchTarget(int branchTarget) {
		this.branchTarget = branchTarget;
	}

	public int getOp1() {
		return op1;
	}

	public void setOp1(int op1) {
		this.op1 = op1;
	}

	public int getOp2() {
		return op2;	
	}

	public void setOp2(int op2) {
		this.op2 = op2;
	}
}
