package processor.pipeline;

public class IF_OF_LatchType {
	
	boolean OF_enable;
	int instruction;
	int currentPC;
	
	boolean nop ;

	public boolean isNop() {
		return nop;
	}

	public void setNop(boolean nop) {
		this.nop = nop;
	}	
	
	boolean OF_busy;

	public boolean isOF_busy() {
		return OF_busy;
	}

	public void setOF_busy(boolean eX_busy) {
		OF_busy = eX_busy;
	}

	boolean OF_stall;

	public boolean isOF_stall() {
		return OF_stall;
	}

	public void setOF_stall(boolean oF_stall) {
		OF_stall = oF_stall;
	}

	public IF_OF_LatchType()
	{
		OF_enable = false;
		nop = false;
		OF_busy = false;
		OF_stall = false;
	}

	public boolean isOF_enable() {
		return OF_enable;
	}

	public void setOF_enable(boolean oF_enable) {
		OF_enable = oF_enable;
	}

	public int getInstruction() {
		return instruction;
	}

	public void setInstruction(int instruction) {
		this.instruction = instruction;
	}

	public int getCurrentPC() {
		return currentPC;
	}

	public void setCurrentPC(int currentPC) {
		this.currentPC = currentPC;
	}

	

}
