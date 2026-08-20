package processor.pipeline;

public class EX_IF_LatchType {
	boolean IF_enable;
	int branchPC;
	int isBranchTaken;
	int currentPC;

	//MODIFICATIONS FOR PIPELINING - STARTS
	boolean nop = false;
	int rd;
	int numberOfInstructions = 0;
	int numberOfBranches = 0;

	public boolean isNop() {
		return nop;
	}

	public void setNop(boolean nop) {
		this.nop = nop;
	}

	public int getRd() {
		return rd;
	}

	public void setRd(int rd) {
		this.rd = rd;
	}	

	public int getNumberOfInstructions() {
		return numberOfInstructions;
	}

	public void incrementNumberOfInstructions() {
		this.numberOfInstructions += 1;
	}	

	public int getNumberOfBranches() {
		return numberOfBranches;
	}

	public void incrementNumberOfBranches() {
		this.numberOfBranches += 2;
	}	
	//MODIFICATIONS FOR PIPELINING - ENDS
	
	public EX_IF_LatchType()
	{
		IF_enable = false;
	}

	public boolean isIF_enable() {
		return IF_enable;
	}

	public void setIF_enable(boolean iF_enable) {
		IF_enable = iF_enable;
	}

	public int getBranchPC() {
		return branchPC;
	}

	public void setBranchPC(int branchPC) {
		this.branchPC = branchPC;
	}

	public int getIsBranchTaken() {
		return isBranchTaken;
	}

	public void setIsBranchTaken(int isBranchTaken) {
		this.isBranchTaken = isBranchTaken;
	}
}
