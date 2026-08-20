package processor.pipeline;

public class EX_MA_LatchType {
	
	boolean MA_enable;
	int instruction;
	int AluResult;
	int op1;
	int op2;
	int immx;
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
	}	
	//MODIFICATIONS FOR PIPELINING - ENDS
	
	boolean MA_busy;

	public boolean isMA_busy() {
		return MA_busy;
	}

	public void setMA_busy(boolean mA_busy) {
		MA_busy = mA_busy;
	}

	boolean MA_stall;

	public boolean isMA_stall() {
		return MA_stall;
	}

	public void setMA_stall(boolean mA_stall) {
		MA_stall = mA_stall;
	}	

	public EX_MA_LatchType()
	{
		MA_enable = false;
		nop = false;
		MA_busy = false;
		MA_stall = false;
	}

	public boolean isMA_enable() {
		return MA_enable;
	}

	public void setMA_enable(boolean mA_enable) {
		MA_enable = mA_enable;
	}

	public int getInstruction() {
		return instruction;
	}

	public void setInstruction(int instruction) {
		this.instruction = instruction;
	}

	public int getAluResult() {
		return AluResult;
	}

	public void setAluResult(int aluResult) {
		AluResult = aluResult;
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

	public int getImmx() {
		return immx;
	}

	public void setImmx(int immx) {
		this.immx = immx;
	}
}
