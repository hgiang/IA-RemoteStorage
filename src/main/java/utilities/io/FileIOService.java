package utilities.io;

public interface FileIOService {

	byte[] readFile(String inputFileName);

	String readFileToString(String inputFileName);

	void outputDataToFile(byte[] data, String outputFileName);
	
	void logErrors(Exception ex);
}
