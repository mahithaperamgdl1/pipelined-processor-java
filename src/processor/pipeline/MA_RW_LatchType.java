package processor.pipeline;

public class MA_RW_LatchType {
	
	boolean RW_enable;
	int ldResult;
	int instruction;
	int AluResult;
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
	//MODIFICATIONS FOR PIPELINING - ENDS
	
	boolean RW_busy;

	public boolean isRW_busy() {
		return RW_busy;
	}

	public void setRW_busy(boolean mA_busy) {
		RW_busy = mA_busy;
	}

	boolean RW_stall;

	public boolean isRW_stall() {
		return RW_stall;
	}

	public void setRW_stall(boolean mA_stall) {
		RW_stall = mA_stall;
	}
	
	public MA_RW_LatchType()
	{
		RW_enable = false;
		RW_stall = false;
	}

	public boolean isRW_enable() {
		return RW_enable;
	}

	public void setRW_enable(boolean rW_enable) {
		RW_enable = rW_enable;
	}

	public int getLdResult() {
		return ldResult;
	}

	public void setLdResult(int ldResult) {
		this.ldResult = ldResult;
	}

	public int getAluResult() {
		return AluResult;
	}

	public void setAluResult(int aluResult) {
		AluResult = aluResult;
	}

	public int getInstruction() {
		return instruction;
	}

	public void setInstruction(int instruction) {
		this.instruction = instruction;
	}
}
