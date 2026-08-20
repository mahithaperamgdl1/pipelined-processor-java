package generic;

public class ExecutionCompleteEvent extends Event {
	int AluResult;

	public ExecutionCompleteEvent(long eventTime, Element requestingElement, Element processingElement, int AluResult)
	{
		super(eventTime, EventType.ExecutionComplete, requestingElement, processingElement);
		this.AluResult = AluResult;
	}

	public int getAluResult() {
		return AluResult;
	}

}
