package com.jorisaerts.cscompiler.compilers;

import java.io.File;

import com.jorisaerts.cscompiler.helpers.FileIOHelper;

public abstract class CoffeeScriptCompiler implements AutoCloseable {

	protected static boolean init = false;

	protected final static String COMPILER_FILENAME = "./coffee/coffee-script.js";

	public abstract String compile(String coffeeScript) throws Throwable;

	public String compile(File filename) throws Throwable {
		return compile(FileIOHelper.readFile(filename));
	}

	/** Closes the current instance of CoffeeScriptCompiler */
	public void close() {
	}

}
