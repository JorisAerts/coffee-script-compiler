package com.jorisaerts.cscompiler;

import java.io.File;
import java.security.InvalidParameterException;

import com.jorisaerts.cscompiler.compilation.Compilation;
import com.jorisaerts.cscompiler.helpers.FileHelper;

public class Main {

	public static void main(String[] args) throws Throwable {
		validateArguments(args);
		compileAll(args);
	}

	public static void compileAll(String[] args) throws Throwable {
		File inputFile = new File(args[0]);
		File outputDirectory = FileHelper.getDirectory(inputFile);
		Compilation compilation = new Compilation(inputFile, outputDirectory);
		compilation.out = System.out;
		compilation.compile();
	}

	public static void validateArguments(String[] args) throws Throwable {
		if (args.length < 1)
			throw new InvalidParameterException(new StringBuilder().append("You need to specify at least the input path.").toString());
	}

}
