package generic;

public class MemoryResponseEvent extends Event {

	int value;
	int currentPC;
	
	public MemoryResponseEvent(long eventTime, Element requestingElement, Element processingElement, int value, int currentPC) {
		super(eventTime, EventType.MemoryResponse, requestingElement, processingElement);
		this.value = value;
		this.currentPC = currentPC;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getCurrentPC() {
		return currentPC;
	}

	public void setCurrentPC(int CurrentPC) {
		this.currentPC = CurrentPC;
	}

}
