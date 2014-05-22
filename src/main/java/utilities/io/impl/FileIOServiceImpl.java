package utilities.io.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.apache.commons.io.IOUtils;

public class FileIOServiceImpl implements utilities.io.FileIOService {

	/**
	 * Read file data into a byte[]
	 * */
	public byte[] readFile(String inputFileName) {

		try {
			FileInputStream insputStream = new FileInputStream(new File(
					inputFileName));
			byte[] inputBytes = IOUtils.toByteArray(insputStream);
			insputStream.close();

			return inputBytes;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Read file data into a String
	 * */
	public String readFileToString(String inputFileName) {

		try {
			FileInputStream insputStream = new FileInputStream(new File(
					inputFileName));
			byte[] inputBytes = IOUtils.toByteArray(insputStream);
			insputStream.close();

			return new String(inputBytes, "UTF-8");
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Write byte[] data into a file
	 * */
	public void outputDataToFile(byte[] data, String outputFileName) {
		try {
			FileOutputStream fileOuputStream = new FileOutputStream(new File(
					outputFileName));
			IOUtils.write(data, fileOuputStream);
			fileOuputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void logErrors(Exception ex){
		File file = new File("logs/Error Log.txt");
		try {
			PrintStream ps = new PrintStream(file);
			ex.printStackTrace(ps);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
