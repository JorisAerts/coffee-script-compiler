package com.jorisaerts.cscompiler.helpers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

public class FileIOHelper {

	private FileIOHelper() {
	}

	public static Reader getFileReader(String filename) throws FileNotFoundException {
		return getFileReader(new File(filename));
	}

	public static Reader getFileReader(File file) throws FileNotFoundException {
		return new FileReader(file);
	}

	public static String readFile(String filename) throws IOException {
		return readFile(new File(filename));
	}

	public static String readFile(File file) throws IOException {
		Reader inputScript = getFileReader(file);
		BufferedReader br = new BufferedReader(inputScript);
		String line;
		StringBuffer sb = new StringBuffer();

		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}

		br.close();
		return sb.toString();
	}

	public static void writeFile(String filename, String contents) throws IOException {
		writeFile(new File(filename), contents);
	}

	public static void writeFile(File file, String contents) throws IOException {
		FileWriter writer = new FileWriter(file);
		BufferedWriter br = new BufferedWriter(writer);
		br.write(contents);
		br.close();
	}
}