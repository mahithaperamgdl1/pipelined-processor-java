package generic;

import java.io.PrintWriter;

public class Statistics {
	
	// TODO add your statistics here
	static int numberOfInstructions;
	static int numberOfCycles;
	static long timeTaken;

	//FOR PIPELINING - START
	static int numberOfBranches;
	static int numberOfDataHazards;
	static int numberOfCyclesOfOFStalls;
	//FOR PIPELINING - END

	public static void printStatistics(String statFile)
	{
		try
		{
			PrintWriter writer = new PrintWriter(statFile);
			
			writer.println("Number of instructions executed = " + numberOfInstructions);
			
			//FOR REPORT
			//1: THE NUMBER OF CYCLES TAKEN BY THE PROGRAM
			writer.println("Number of cycles taken = " + numberOfCycles);

			//2: THE NUMBER OF TIMES THE OF STAGE NEEDED TO STALL BECAUSE OF A DATA HAZARD
			writer.println("Number of times the OF stage needed to stall because of a data hazard = " + numberOfDataHazards);

			//3: THE NUMBER OF TIMES AN INSTRUCTION ON A WRONG BRANCH PATH ENTERED THE PIPELINE
			writer.println("Number of times an instruction on a wrong branch path entered the pipeline = " + numberOfBranches);

			//4: NUMBER OF TIMES THE OF STAGE NEEDED TO STALL BECAUSE OF A DATA HAZARD
			writer.println("Number of cycles the OF stage needed to stall because of a data hazard = " + numberOfCyclesOfOFStalls);

			
			
			writer.close();
		}
		catch(Exception e)
		{
			Misc.printErrorAndExit(e.getMessage());
		}
	}
	
	// TODO write functions to update statistics
	public static void setNumberOfInstructions(int numberOfInstructions) {
		Statistics.numberOfInstructions = numberOfInstructions;
	}

	public static void setNumberOfCycles(int numberOfCycles) {
		Statistics.numberOfCycles = numberOfCycles;
	}

	public static void setTimeTaken(long timeTaken) {
		Statistics.timeTaken = timeTaken;
	}

	//FOR PIPELINING - START
	public static void 	setNumberOfBranches(int numberOfBranches) {
		Statistics.numberOfBranches = numberOfBranches;
	}

	public static void 	setNumberOfDataHazards(int numberOfDataHazards) {
		Statistics.numberOfDataHazards = numberOfDataHazards;
	}

	public static void 	setNumberOfCyclesOfOFStalls(int numberOfCyclesOfOFStalls) {
		Statistics.numberOfCyclesOfOFStalls = numberOfCyclesOfOFStalls;
	}
	//FOR PIPELINING - END
}
